package com.turn.browser.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.turn.browser.bean.*;
import com.turn.browser.bean.http.CustomHttpClient;
import com.turn.browser.dao.custommapper.*;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.*;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.enums.AddressTypeEnum;
import com.turn.browser.enums.ErcTypeEnum;
import com.turn.browser.service.erc.ErcServiceImpl;
import com.turn.browser.utils.AddressUtil;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.TaskUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * token scheduled task
 */
@Slf4j
@Component
public class ErcTokenUpdateTask {

    /**
     * token_inventory table retry times
     */
    @Value("${turn.token-retry-num:3}")
    private int tokenRetryNum;

    @Resource
    private TokenInventoryMapper token721InventoryMapper;

    @Resource
    private Token1155InventoryMapper token1155InventoryMapper;

    @Resource
    private CustomTokenInventoryMapper customToken721InventoryMapper;

    @Resource
    private CustomToken1155InventoryMapper customToken1155InventoryMapper;

    @Resource
    private Token1155HolderMapper token1155HolderMapper;

    @Resource
    private TokenHolderMapper tokenHolderMapper;

    @Resource
    private CustomTokenHolderMapper customTokenHolderMapper;

    @Resource
    private CustomToken1155HolderMapper customToken1155HolderMapper;

    @Resource
    private CustomTokenMapper customTokenMapper;

    @Resource
    private TokenMapper tokenMapper;

    @Resource
    private CustomAddressMapper customAddressMapper;

    @Resource
    private ErcServiceImpl ercServiceImpl;

    @Resource
    private PointLogMapper pointLogMapper;

    @Resource
    private TxErc20BakMapper txErc20BakMapper;

    @Resource
    private TxErc721BakMapper txErc721BakMapper;

    private static final int TOKEN_BATCH_SIZE = 10;

    private static final ExecutorService TOKEN_UPDATE_POOL = Executors.newFixedThreadPool(TOKEN_BATCH_SIZE);

    private static final int HOLDER_BATCH_SIZE = 10;

    private static final ExecutorService HOLDER_UPDATE_POOL = Executors.newFixedThreadPool(HOLDER_BATCH_SIZE);

    private final Lock lock = new ReentrantLock();

    private final Lock tokenInventoryLock = new ReentrantLock();

    private final Lock tokenHolderLock = new ReentrantLock();

    private final Lock token1155HolderLock = new ReentrantLock();

    /**
     * Full update of the total supply of tokens
     * Updated every 5 minutes
     *
     * @return void
     */
    @XxlJob("totalUpdateTokenTotalSupplyJobHandler")
    public void totalUpdateTokenTotalSupply() {
        lock.lock();
        try {
            updateTokenTotalSupply();
        } catch (Exception e) {
            log.warn("The total supply of full update tokens is abnormal.", e);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Incrementally update token holder balance
     * Run every 1 minute
     *
     * @param
     * @return void
     * @date 2021/2/1
     */
    @XxlJob("incrementUpdateTokenHolderBalanceJobHandler")
    public void incrementUpdateTokenHolderBalance() {
        if (tokenHolderLock.tryLock()) {
            try {
                incrementUpdateErc20TokenHolderBalance();
                incrementUpdateErc721TokenHolderBalance();
                incrementUpdateErc1155TokenHolderBalance();
            } catch (Exception e) {
                log.error("Abnormality in incremental update of token holder balance", e);
            } finally {
                tokenHolderLock.unlock();
            }
        }
    }

    /**
     * Fully update token holder balances
     * Executed once a day at 00:00:00
     */
    @XxlJob("totalUpdateTokenHolderBalanceJobHandler")
    public void totalUpdateTokenHolder() {
        totalUpdateTokenHolderBalance();
        totalUpdateToken1155HolderBalance();
    }

    /**
     * Update erc20/erc721 holder balance
     *
     * @param :
     * @return: void
     * @date: 2022/8/3
     */
    private void totalUpdateTokenHolderBalance() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        tokenHolderLock.lock();
        try {
            // Update holder's balance in pages
            List<TokenHolder> batch;
            int page = 0;
            do {
                TokenHolderExample condition = new TokenHolderExample();
                condition.setOrderByClause(" token_address asc, address asc limit " + page * HOLDER_BATCH_SIZE + "," + HOLDER_BATCH_SIZE);
                batch = tokenHolderMapper.selectByExample(condition);
                List<ErcToken> ercTokens = getErcTokens();
                // Filter destroyed contracts
                List<TokenHolder> res = subtractToList(batch, getDestroyContracts());
                List<TokenHolder> updateParams = new ArrayList<>();
                if (CollUtil.isNotEmpty(res)) {
                    CountDownLatch latch = new CountDownLatch(res.size());
                    res.forEach(holder -> {
                        HOLDER_UPDATE_POOL.submit(() -> {
                            try {
                                // Check balance and backfill
                                ErcToken token = CollUtil.findOne(ercTokens, ercToken -> ercToken.getAddress().equalsIgnoreCase(holder.getTokenAddress()));
                                if (token != null) {
                                    BigInteger balance = ercServiceImpl.getBalance(holder.getTokenAddress(), token.getTypeEnum(), holder.getAddress(), null);
                                    if (ObjectUtil.isNull(holder.getBalance()) || new BigDecimal(holder.getBalance()).compareTo(new BigDecimal(balance)) != 0) {
                                        log.info("token[{}]address[{}] balance has changed and needs to be updated, old value[{}] new value[{}]", holder.getTokenAddress(), holder.getAddress(), holder.getBalance (), balance.toString());
                                        //Add to the update list only when the balance changes to avoid frequent access to the table
                                        holder.setBalance(balance.toString());
                                        updateParams.add(holder);
                                    }
                                } else {
                                    String msg = StrUtil.format("Cannot find corresponding token[{}]", holder.getTokenAddress());
                                    XxlJobHelper.log(msg);
                                    log.error(msg);
                                }
                            } catch (Exception e) {
                                XxlJobHelper.log(StrUtil.format("Failed to query token holder's balance, contract [{}] address [{}]", holder.getTokenAddress(), holder.getAddress()));
                                log.warn(StrFormatter.format("Failed to query balance, address [{}], contract address [{}]", holder.getAddress(), holder.getTokenAddress()), e);
                            } finally {
                                latch.countDown();
                            }
                        });
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        log.error("", e);
                    }
                }
                if (CollUtil.isNotEmpty(updateParams)) {
                    customTokenHolderMapper.batchUpdate(updateParams);
                    TaskUtil.console("Update token holder balance{}", JSONUtil.toJsonStr(updateParams));
                }
                page++;
            } while (!batch.isEmpty());
            XxlJobHelper.log("Full update of token holder balance successful");
        } catch (Exception e) {
            log.error("Exception in updating address token balance", e);
        } finally {
            tokenHolderLock.unlock();
        }
    }

    /**
     * Update erc1155 holder balance
     *
     * @param :
     * @return: void
     */
    private void totalUpdateToken1155HolderBalance() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        token1155HolderLock.lock();
        try {
            // Update holder's balance in pages
            List<Token1155Holder> batch;
            int page = 0;
            do {
                Token1155HolderExample condition = new Token1155HolderExample();
                condition.setOrderByClause(" id asc limit " + page * HOLDER_BATCH_SIZE + "," + HOLDER_BATCH_SIZE);
                batch = token1155HolderMapper.selectByExample(condition);
                // Filter destroyed contracts
                List<Token1155Holder> res = subtractErc1155ToLis(batch, getDestroyContracts());
                List<Token1155Holder> updateParams = new ArrayList<>();
                if (CollUtil.isNotEmpty(res)) {
                    CountDownLatch latch = new CountDownLatch(res.size());
                    res.forEach(holder -> {
                        HOLDER_UPDATE_POOL.submit(() -> {
                            try {
                                BigInteger balance = ercServiceImpl.getBalance(holder.getTokenAddress(), ErcTypeEnum.ERC1155, holder.getAddress(), new BigInteger(holder.getTokenId()));
                                if (!balance.toString().equalsIgnoreCase(holder.getBalance())) {
                                    log.info("1155token[{}][{}]address[{}] balance has changed and needs to be updated, old value [{}] new value [{}]",
                                            holder.getTokenAddress(),
                                            holder.getTokenId(),
                                            holder.getAddress(),
                                            holder.getBalance(),
                                            balance.toString());
                                    //Add to the update list only when the balance changes to avoid frequent access to the table
                                    holder.setBalance(balance.toString());
                                    updateParams.add(holder);
                                }
                            } catch (Exception e) {
                                XxlJobHelper.log(StrUtil.format("Failed to query the balance of 1155token holder, contract [{}][{}] address [{}]", holder.getTokenAddress(), holder.getTokenId(), holder.getAddress()) );
                                log.warn(StrFormatter.format("Failed to query 1155 balance, address [{}], contract address [{}][{}]", holder.getAddress(), holder.getTokenAddress(), holder.getTokenId()) , e);
                            } finally {
                                latch.countDown();
                            }
                        });
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        log.error("Exception", e);
                    }
                }
                if (CollUtil.isNotEmpty(updateParams)) {
                    customToken1155HolderMapper.batchUpdate(updateParams);
                    TaskUtil.console("Update 1155token holder balance{}", JSONUtil.toJsonStr(updateParams));
                }
                page++;
            } while (!batch.isEmpty());
            XxlJobHelper.log("Full update of 155token holder balance successful");
        } catch (Exception e) {
            log.error("Exception in updating the token balance of address 1155", e);
        } finally {
            token1155HolderLock.unlock();
        }
    }

    /**
     * Fully update token inventory information
     * Executed once every day at 1 am
     *
     * @param
     * @return void
     */
    @XxlJob("totalUpdateTokenInventoryJobHandler")
    public void totalUpdateTokenInventory() {
        tokenInventoryLock.lock();
        try {
            updateToken721Inventory();
            updateToken1155Inventory();
        } catch (Exception e) {
            log.error("Update token inventory information", e);
        } finally {
            tokenInventoryLock.unlock();
        }
    }

    /**
     * Incrementally update token inventory information
     * Executed every 1 minute
     *
     * @param
     * @return void
     */
    @XxlJob("incrementUpdateTokenInventoryJobHandler")
    public void incrementUpdateTokenInventory() {
        if (tokenInventoryLock.tryLock()) {
            try {
                cronIncrementUpdateToken721Inventory();
                cronIncrementUpdateToken1155Inventory();
            } catch (Exception e) {
                log.warn("Incremental update token inventory information exception", e);
            } finally {
                tokenInventoryLock.unlock();
            }
        } else {
            log.warn("The token inventory incremental update cannot obtain the lock");
        }
    }

    /**
     * The destroyed 721 contract updates the balance
     * Executed every 10 minutes
     *
     * @param :
     * @return: void
     */
    @XxlJob("contractDestroyUpdateBalanceJobHandler")
    public void contractDestroyUpdateBalance() {
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        contractErc20DestroyUpdateBalance();
        contractErc721DestroyUpdateBalance();
        contractErc1155DestroyUpdateBalance();
    }

    /**
     * Update the total supply of ERC20 and Erc721Enumeration token ===》Full update
     *
     * @return void
     */
    private void updateTokenTotalSupply() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        Set<ErcToken> updateParams = new ConcurrentHashSet<>();
        List<List<ErcToken>> batchList = new ArrayList<>();
        List<ErcToken> batch = new ArrayList<>();
        batchList.add(batch);
        List<ErcToken> tokens = getErcTokens();
        for (ErcToken token : tokens) {
            if (token.isDirty()) {
                updateParams.add(token);
            }
            if (!(token.getTypeEnum() == ErcTypeEnum.ERC20 || token.getIsSupportErc721Enumeration())) {
                continue;
            }
            if (batch.size() == TOKEN_BATCH_SIZE) {
                // If this batch reaches the size limit, create a new batch and add it to the batch list
                batch = new ArrayList<>();
                batchList.add(batch);
            }
            //Add to batch
            batch.add(token);
        }
        // Query Token totalSupply concurrently in batches
        batchList.forEach(b -> {
            // Filter destroyed contracts
            List<ErcToken> res = tokenSubtractToList(b, getDestroyContracts());
            if (CollUtil.isNotEmpty(res)) {
                CountDownLatch latch = new CountDownLatch(res.size());
                for (ErcToken token : res) {
                    TOKEN_UPDATE_POOL.submit(() -> {
                        try {
                            //Query the total supply
                            BigInteger totalSupply = ercServiceImpl.getTotalSupply(token.getAddress());
                            totalSupply = totalSupply == null ? BigInteger.ZERO : totalSupply;
                            if (ObjectUtil.isNull(token.getTotalSupply()) || !token.getTotalSupply().equalsIgnoreCase(totalSupply.toString())) {
                                TaskUtil.console("Token[{}]'s total supply has changed and needs to update the old value [{}] and the new value [{}]", token.getAddress(), token.getTotalSupply(), totalSupply);
                                // Changes are added to the update list
                                token.setTotalSupply(totalSupply.toString());
                                updateParams.add(token);
                            }
                        } catch (Exception e) {
                            XxlJobHelper.log(StrUtil.format("The total supply of this token[{}] is abnormal", token.getAddress()));
                            log.error("Exception in querying total supply", e);
                        }finally {
                            latch.countDown();
                        }
                    });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("", e);
                }
            }
        });
        if (!updateParams.isEmpty()) {
            // Batch update records with changes in total supply
            customTokenMapper.batchUpdateTokenTotalSupply(new ArrayList<>(updateParams));
            XxlJobHelper.handleSuccess("Full update of total supply of token successful");
            updateParams.forEach(token -> token.setDirty(false));
        }
        XxlJobHelper.log("Full update of total token supply successful");
        updateTokenHolderCount();
    }

    /**
     * Update the balance of erc20 token holder
     *
     * @param :
     * @return: void
     */
    private void incrementUpdateErc20TokenHolderBalance() throws Exception {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        try {
            int pageSize = Convert.toInt(XxlJobHelper.getJobParam(), 500);
            PointLog pointLog = pointLogMapper.selectByPrimaryKey(5);
            long oldPosition = Convert.toLong(pointLog.getPosition());
            TxErc20BakExample example = new TxErc20BakExample();
            example.setOrderByClause("id asc limit " + pageSize);
            example.createCriteria().andIdGreaterThan(oldPosition);
            List<TxErc20Bak> list = txErc20BakMapper.selectByExample(example);
            List<TokenHolder> updateParams = new ArrayList<>();
            TaskUtil.console("[erc20]The current page number is [{}], the breakpoint is [{}]", pageSize, oldPosition);
            if (CollUtil.isEmpty(list)) {
                TaskUtil.console("[erc20] No transaction found for this breakpoint [{}]", oldPosition);
                return;
            }
            HashMap<String, HashSet<String>> map = new HashMap();
            list.sort(Comparator.comparing(ErcTx::getSeq));
            list.forEach(v -> {
                if (map.containsKey(v.getContract())) {
                    // Determine whether it is 0 address
                    if (!AddressUtil.isAddrZero(v.getTo())) {
                        map.get(v.getContract()).add(v.getTo());
                    }
                    if (!AddressUtil.isAddrZero(v.getFrom())) {
                        map.get(v.getContract()).add(v.getFrom());
                    }
                } else {
                    HashSet<String> addressSet = new HashSet<String>();
                    // Determine whether it is 0 address
                    if (!AddressUtil.isAddrZero(v.getTo())) {
                        addressSet.add(v.getTo());
                    }
                    if (!AddressUtil.isAddrZero(v.getFrom())) {
                        addressSet.add(v.getFrom());
                    }
                    map.put(v.getContract(), addressSet);
                }
            });
            // Filter destroyed contracts
            HashMap<String, HashSet<String>> res = subtractToMap(map, getDestroyContracts());
            if (MapUtil.isNotEmpty(res)) {
                //Serial balance check
                res.forEach((contract, addressSet) -> {
                    addressSet.forEach(address -> {
                        try {
                            BigInteger balance = ercServiceImpl.getBalance(contract, ErcTypeEnum.ERC20, address, BigInteger.ZERO);
                            TokenHolder holder = new TokenHolder();
                            holder.setTokenAddress(contract);
                            holder.setAddress(address);
                            holder.setBalance(balance.toString());
                            updateParams.add(holder);
                            log.info("[erc20] token holder query balance [{}], contract [{}] address [{}]", balance, contract, address);
                            try {
                                TimeUnit.MILLISECONDS.sleep(100);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        } catch (Exception e) {
                            String msg = StrFormatter.format("Failed to query the balance of [erc20] token holder, contract:{},address:{}", contract, address);XxlJobHelper.log(msg);
                            log.warn(msg, e);
                        }
                    });
                });
            }
            if (CollUtil.isNotEmpty(updateParams)) {
                customTokenHolderMapper.batchUpdate(updateParams);
                TaskUtil.console("Update the balance of [erc20] token holder{}", JSONUtil.toJsonStr(updateParams));
                XxlJobHelper.handleSuccess("Updating the balance of [erc20] token holder successfully");
            }
            String newPosition = CollUtil.getLast(list).getId().toString();
            pointLog.setPosition(newPosition);
            pointLogMapper.updateByPrimaryKeySelective(pointLog);
            XxlJobHelper.log("Updating the balance of [erc20] token holder successfully, the breakpoint is [{}]->[{}]", oldPosition, newPosition);
        } catch (Exception e) {
            log.error("Exception in updating token holder balance", e);
            throw e;
        }
    }

    /**
     * Update the balance of erc721’s token holder
     *
     * @param :
     * @return: void
     */
    private void incrementUpdateErc721TokenHolderBalance() throws Exception {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        try {
            int pageSize = Convert.toInt(XxlJobHelper.getJobParam(), 500);
            PointLog pointLog = pointLogMapper.selectByPrimaryKey(6);
            long oldPosition = Convert.toLong(pointLog.getPosition());
            TxErc721BakExample example = new TxErc721BakExample();
            example.setOrderByClause("id asc limit " + pageSize);
            example.createCriteria().andIdGreaterThan(oldPosition);
            List<TxErc721Bak> list = txErc721BakMapper.selectByExample(example);
            List<TokenHolder> updateParams = new ArrayList<>();
            TaskUtil.console("[erc721]The current page number is [{}], the breakpoint is [{}]", pageSize, oldPosition);
            if (CollUtil.isEmpty(list)) {
                TaskUtil.console("[erc721] No transaction found for this breakpoint [{}]", oldPosition);
                return;
            }
            HashMap<String, HashSet<String>> map = new HashMap();
            list.sort(Comparator.comparing(ErcTx::getSeq));
            list.forEach(v -> {
                if (map.containsKey(v.getContract())) {
                    // Determine whether it is 0 address
                    if (!AddressUtil.isAddrZero(v.getTo())) {
                        map.get(v.getContract()).add(v.getTo());
                    }
                    if (!AddressUtil.isAddrZero(v.getFrom())) {
                        map.get(v.getContract()).add(v.getFrom());
                    }
                } else {
                    HashSet<String> addressSet = new HashSet<String>();
                    // Determine whether it is 0 address
                    if (!AddressUtil.isAddrZero(v.getTo())) {
                        addressSet.add(v.getTo());
                    }
                    if (!AddressUtil.isAddrZero(v.getFrom())) {
                        addressSet.add(v.getFrom());
                    }
                    map.put(v.getContract(), addressSet);
                }
            });
            // Filter destroyed contracts
            HashMap<String, HashSet<String>> res = subtractToMap(map, getDestroyContracts());
            if (MapUtil.isNotEmpty(res)) {
                //Serial balance check
                res.forEach((contract, addressSet) -> {
                    addressSet.forEach(address -> {
                        try {
                            BigInteger balance = ercServiceImpl.getBalance(contract, ErcTypeEnum.ERC721, address, BigInteger.ZERO);
                            TokenHolder holder = new TokenHolder();
                            holder.setTokenAddress(contract);
                            holder.setAddress(address);
                            holder.setBalance(balance.toString());
                            updateParams.add(holder);
                            log.info("[erc721] token holder query balance [{}], contract [{}] address [{}]", balance, contract, address);
                            try {
                                TimeUnit.MILLISECONDS.sleep(100);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        } catch (Exception e) {
                            String msg = StrFormatter.format("Failed to query the balance of [erc721] token holder, contract:{},address:{}", contract, address);XxlJobHelper.log(msg);
                            log.warn(msg, e);
                        }
                    });
                });
            }
            if (CollUtil.isNotEmpty(updateParams)) {
                customTokenHolderMapper.batchUpdate(updateParams);
                TaskUtil.console("Update the balance of [erc721] token holder{}", JSONUtil.toJsonStr(updateParams));
                XxlJobHelper.handleSuccess("Updating the balance of [erc721] token holder successfully");
            }
            String newPosition = CollUtil.getLast(list).getId().toString();
            pointLog.setPosition(newPosition);
            pointLogMapper.updateByPrimaryKeySelective(pointLog);
            XxlJobHelper.log("Updating the balance of [erc721] token holder successfully, the breakpoint is [{}]->[{}]", oldPosition, newPosition);
        } catch (Exception e) {
            log.error("Exception in updating token holder balance", e);
            throw e;
        }
    }

    /**
     * Update the balance of the token holder of erc1155
     *
     * @param :
     * @return: void
     */
    private void incrementUpdateErc1155TokenHolderBalance() throws Exception {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        try {
            int pageSize = Convert.toInt(XxlJobHelper.getJobParam(), 500);
            PointLog pointLog = pointLogMapper.selectByPrimaryKey(8);
            long oldPosition = Convert.toLong(pointLog.getPosition());
            Token1155HolderExample example = new Token1155HolderExample();
            example.setOrderByClause("id");
            example.createCriteria().andIdGreaterThan(oldPosition).andIdLessThanOrEqualTo(oldPosition + pageSize);
            List<Token1155Holder> list = token1155HolderMapper.selectByExample(example);
            if (CollUtil.isEmpty(list)) {
                TaskUtil.console("[erc1155] No transaction found for this breakpoint [{}]", oldPosition);
                return;
            }
            // Filter destroyed contracts
            List<Token1155Holder> res = subtractErc1155ToLis(list, getDestroyContracts());
            List<Token1155Holder> updateParams = new ArrayList<>();
            if (CollUtil.isNotEmpty(res)) {
                res.forEach(token1155Holder -> {
                    try {
                        BigInteger balance = ercServiceImpl.getBalance(token1155Holder.getTokenAddress(),
                                                                       ErcTypeEnum.ERC1155,
                                                                       token1155Holder.getAddress(),
                                                                       new BigInteger(token1155Holder.getTokenId()));
                        Token1155Holder holder = new Token1155Holder();
                        holder.setTokenAddress(token1155Holder.getTokenAddress());
                        holder.setAddress(token1155Holder.getAddress());
                        holder.setTokenId(token1155Holder.getTokenId());
                        holder.setBalance(balance.toString());
                        updateParams.add(holder);
                        log.info("[erc1155] token holder query balance [{}]", JSONUtil.toJsonStr(holder));
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    } catch (Exception e) {
                        String msg = StrFormatter.format("Failed to query the balance of [erc1155] token holder, contract:{},tokenId:{},address:{}",
                                token1155Holder.getTokenAddress(),
                                token1155Holder.getTokenId(),
                                token1155Holder.getAddress());
                        XxlJobHelper.log(msg);
                        log.warn(msg, e);
                    }
                });
            }
            if (CollUtil.isNotEmpty(updateParams)) {
                customToken1155HolderMapper.batchUpdate(updateParams);
                TaskUtil.console("Update the balance of [erc1155] token holder{}", JSONUtil.toJsonStr(updateParams));
            }
            String newPosition = CollUtil.getLast(list).getId().toString();
            pointLog.setPosition(newPosition);
            pointLogMapper.updateByPrimaryKeySelective(pointLog);
            XxlJobHelper.log("Updating the balance of [erc1155] token holder successfully, the breakpoint is [{}]->[{}]", oldPosition, newPosition);
        } catch (Exception e) {
            log.error("Abnormal update of 1155token holder balance", e);throw e;
        }
    }

    /**
     * Update token inventory information
     *
     * @param :
     * @return: void
     */
    private void updateToken721Inventory() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        long id = customToken721InventoryMapper.findMaxId() + 1;
        // Update token inventory related information in pages
        List<TokenInventoryWithBLOBs> batch = null;
        int batchSize = Convert.toInt(XxlJobHelper.getJobParam(), 100);
        do {
            //The current number of failed items
            AtomicInteger errorNum = new AtomicInteger(0);
            //The number of items updated this time
            AtomicInteger updateNum = new AtomicInteger(0);
            try {
                TokenInventoryExample condition = new TokenInventoryExample();
                condition.setOrderByClause(" id desc limit " + batchSize);
                condition.createCriteria().andRetryNumLessThan(tokenRetryNum).andImageIsNull().andIdLessThan(id);
                batch = token721InventoryMapper.selectByExampleWithBLOBs(condition);
                List<TokenInventoryWithBLOBs> updateParams = new ArrayList<>();
                if (CollUtil.isNotEmpty(batch)) {
                    batch.forEach(inventory -> {
                        TokenInventoryWithBLOBs updateTokenInventory = new TokenInventoryWithBLOBs();
                        updateTokenInventory.setTokenId(inventory.getTokenId());
                        updateTokenInventory.setTokenAddress(inventory.getTokenAddress());
                        updateTokenInventory.setTokenUrl(inventory.getTokenUrl());
                        try {
                            if (StrUtil.isNotBlank(inventory.getTokenUrl())) {
                                Request request = new Request.Builder().url(inventory.getTokenUrl()).build();
                                Response response = CustomHttpClient.getOkHttpClient().newCall(request).execute();
                                if (response.code() == 200) {
                                    String resp = response.body().string();
                                    UpdateTokenInventory newTi = JSONUtil.toBean(resp, UpdateTokenInventory.class);
                                    newTi.setTokenId(inventory.getTokenId());
                                    newTi.setTokenAddress(inventory.getTokenAddress());
                                    boolean changed = false;
                                    // Whenever an attribute changes, it is added to the update list
                                    if (ObjectUtil.isNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImageUrl())) {
                                        updateTokenInventory.setImage(newTi.getImageUrl());
                                        changed = true;
                                    } else if (ObjectUtil.isNotNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImageUrl()) && !inventory.getImage().equals(newTi.getImageUrl())) {
                                        updateTokenInventory.setImage(newTi.getImageUrl());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImage())) {
                                        updateTokenInventory.setImage(newTi.getImage());
                                        changed = true;
                                    } else if (ObjectUtil.isNotNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImage()) && !inventory.getImage().equals(newTi.getImage())) {
                                        updateTokenInventory.setImage(newTi.getImage());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getDescription()) && ObjectUtil.isNotNull(newTi.getDescription())) {
                                        updateTokenInventory.setDescription(newTi.getDescription());
                                        changed = true;
                                    } else if (ObjectUtil.isNotNull(inventory.getDescription()) && ObjectUtil.isNotNull(newTi.getDescription()) && !inventory.getDescription()
                                                                                                                                                             .equals(newTi.getDescription())) {
                                        updateTokenInventory.setDescription(newTi.getDescription());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getName()) && ObjectUtil.isNotNull(newTi.getName())) {
                                        updateTokenInventory.setName(newTi.getName());
                                        changed = true;
                                    } else if (ObjectUtil.isNotNull(inventory.getName()) && ObjectUtil.isNotNull(newTi.getName()) && !inventory.getName().equals(newTi.getName())) {
                                        updateTokenInventory.setName(newTi.getName());
                                        changed = true;
                                    }
                                    if (changed) {
                                        updateNum.getAndIncrement();
                                        updateTokenInventory.setRetryNum(0);
                                        updateParams.add(updateTokenInventory);
                                        log.info("There are attribute changes in the inventory that need to be updated, token[{}]", JSONUtil.toJsonStr(updateTokenInventory));
                                    }
                                } else {
                                    errorNum.getAndIncrement();
                                    updateTokenInventory.setRetryNum(inventory.getRetryNum() + 1);
                                    updateParams.add(updateTokenInventory);
                                    log.warn("http request exception: http status code: {}, http message: {}, token: {}", response.code(), response.message(), JSONUtil.toJsonStr(updateTokenInventory));}
                            } else {
                                errorNum.getAndIncrement();
                                updateTokenInventory.setRetryNum(inventory.getRetryNum() + 1);
                                updateParams.add(updateTokenInventory);
                                String msg = StrUtil.format("The request TokenURI is empty,,token:{}", JSONUtil.toJsonStr(updateTokenInventory));
                                XxlJobHelper.log(msg);
                                log.warn(msg);
                            }
                        } catch (Exception e) {
                            errorNum.getAndIncrement();
                            updateTokenInventory.setRetryNum(inventory.getRetryNum() + 1);
                            updateParams.add(updateTokenInventory);
                            log.warn(StrUtil.format("Exception in full update of token inventory information, token: {}", JSONUtil.toJsonStr(updateTokenInventory)), e);
                        }
                    });
                    id = batch.get(batch.size() - 1).getId();
                }
                if (CollUtil.isNotEmpty(updateParams)) {
                    customToken721InventoryMapper.batchUpdateTokenInfo(updateParams);
                    XxlJobHelper.log("Full update of token inventory information{}", JSONUtil.toJsonStr(updateParams));
                }
                String msg = StrUtil.format("Full update of token inventory information: The number of queried items is {}, the number of updated items is: {}, and the number of failed items is: {}", batch.size(), updateNum .get(), errorNum.get());
                XxlJobHelper.log(msg);
                log.info(msg);
            } catch (Exception e) {
                log.error(StrUtil.format("Exception in full update of token inventory information"), e);
            }
        } while (CollUtil.isNotEmpty(batch));
    }

    /**
     * Update token inventory information
     *
     * @param :
     * @return: void
     */
    private void updateToken1155Inventory() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        //Update token inventory related information in pages
        List<Token1155InventoryWithBLOBs> batch = null;
        long id = customToken1155InventoryMapper.findMaxId() + 1;
        int batchSize = Convert.toInt(XxlJobHelper.getJobParam(), 100);
        do {
            //The number of items currently queried
            int batchNum = 0;
            //The current number of failed items
            AtomicInteger errorNum = new AtomicInteger(0);
            //The number of items updated this time
            AtomicInteger updateNum = new AtomicInteger(0);
            try {
                Token1155InventoryExample condition = new Token1155InventoryExample();
                condition.setOrderByClause(" id desc limit " + batchSize);
                condition.createCriteria().andRetryNumLessThan(tokenRetryNum).andImageIsNull().andIdLessThan(id);
                batch = token1155InventoryMapper.selectByExampleWithBLOBs(condition);
                // Filter destroyed contracts
                List<Token1155InventoryWithBLOBs> res = token1155InventorySubtractToList(batch, getDestroyContracts());
                List<Token1155InventoryWithBLOBs> updateParams = new ArrayList<>();
                if (CollUtil.isNotEmpty(res)) {
                    batchNum = res.size();
                    res.forEach(inventory -> {
                        try {
                            if (StrUtil.isNotBlank(inventory.getTokenUrl())) {
                                Request request = new Request.Builder().url(inventory.getTokenUrl()).build();
                                Response response = CustomHttpClient.getOkHttpClient().newCall(request).execute();
                                if (response.code() == 200) {
                                    String resp = response.body().string();
                                    Token1155InventoryWithBLOBs newTi = JSONUtil.toBean(resp, Token1155InventoryWithBLOBs.class);
                                    newTi.setTokenId(inventory.getTokenId());
                                    newTi.setTokenAddress(inventory.getTokenAddress());
                                    boolean changed = false;
                                    // Whenever an attribute changes, it is added to the update list
                                    if (ObjectUtil.isNull(inventory.getImage()) || !newTi.getImage().equals(inventory.getImage())) {
                                        inventory.setImage(newTi.getImage());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getDescription()) || !newTi.getDescription().equals(inventory.getDescription())) {
                                        inventory.setDescription(newTi.getDescription());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getName()) || !newTi.getName().equals(inventory.getName())) {
                                        inventory.setName(newTi.getName());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getDecimal()) || !newTi.getDecimal().equals(inventory.getDecimal())) {
                                        inventory.setDecimal(newTi.getDecimal());
                                        changed = true;
                                    }
                                    if (changed) {
                                        updateNum.getAndIncrement();
                                        inventory.setRetryNum(0);
                                        updateParams.add(inventory);
                                        log.info("1155token[{}] has attribute changes in inventory that need to be updated, tokenURL[{}], tokenName[{}], tokenDesc[{}], tokenImage[{}],ecimal[{}]",
                                                inventory.getTokenAddress(),
                                                inventory.getTokenUrl(),
                                                inventory.getName(),
                                                inventory.getDescription(),
                                                inventory.getImage(),
                                                inventory.getDecimal());
                                    }
                                } else {
                                    errorNum.getAndIncrement();
                                    inventory.setRetryNum(inventory.getRetryNum() + 1);
                                    updateParams.add(inventory);
                                    log.warn("http request exception: http status code: {}, http message: {}, 1155token_address: {}, token_id: {}, tokenURI: {}, number of retries: {}",
                                            response.code(),
                                            response.message(),
                                            inventory.getTokenAddress(),
                                            inventory.getTokenId(),
                                            inventory.getTokenUrl(),
                                            inventory.getRetryNum());
                                }
                            } else {
                                errorNum.getAndIncrement();
                                inventory.setRetryNum(inventory.getRetryNum() + 1);
                                updateParams.add(inventory);
                                String msg = StrUtil.format("The request TokenURI is empty, 1155token_address: {}, token_id: {}, number of retries: {}", inventory.getTokenAddress(), inventory.getTokenId(), inventory.getRetryNum());
                                XxlJobHelper.log(msg);
                                log.warn(msg);
                            }
                        } catch (Exception e) {
                            errorNum.getAndIncrement();
                            inventory.setRetryNum(inventory.getRetryNum() + 1);
                            updateParams.add(inventory);
                            log.warn(StrUtil.format("Exception in full update of 1155 token inventory information, token_address: {}, token_id: {}, tokenURI: {}, number of retries: {}",
                                    inventory.getTokenAddress(),
                                    inventory.getTokenId(),
                                    inventory.getTokenUrl(),
                                    inventory.getRetryNum()), e);
                        }
                    });
                    id = batch.get(batch.size() - 1).getId();
                }
                if (CollUtil.isNotEmpty(updateParams)) {
                    customToken1155InventoryMapper.batchInsertOrUpdateSelective(updateParams, Token1155Inventory.Column.values());
                    XxlJobHelper.log("Full update of 1155token inventory information{}", JSONUtil.toJsonStr(updateParams));
                }
                String msg = StrUtil.format("Full update of 1155token inventory information: the number of queried items is {}, the number of filtered items: {}, the number of updated items: {}, the number of failed items: {} ", batch.size(), batchNum, updateNum.get(), errorNum.get());
                XxlJobHelper.log(msg);
                log.info(msg);
            } catch (Exception e) {
                log.error(StrUtil.format("Full update of 1155token inventory information is abnormal, the current identification is "), e);
            }
        } while (CollUtil.isNotEmpty(batch));
    }

    /**
     * Update token inventory information => incremental update
     *
     * @return void
     */
    private void cronIncrementUpdateToken721Inventory() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        //The current number of failed items
        AtomicInteger errorNum = new AtomicInteger(0);
        //The number of items updated this time
        AtomicInteger updateNum = new AtomicInteger(0);
        PointLog pointLog = pointLogMapper.selectByPrimaryKey(7);
        Long oldPosition = Convert.toLong(pointLog.getPosition());
        int batchSize = Convert.toInt(XxlJobHelper.getJobParam(), 10);
        XxlJobHelper.log("The current page number is [{}], the breakpoint is [{}]", batchSize, oldPosition);
        try {
            TokenInventoryExample condition = new TokenInventoryExample();
            condition.setOrderByClause(" id asc limit " + batchSize);
            condition.createCriteria().andIdGreaterThan(oldPosition).andRetryNumLessThan(tokenRetryNum).andImageIsNull();
            List<TokenInventoryWithBLOBs> batch = token721InventoryMapper.selectByExampleWithBLOBs(condition);
            if (CollUtil.isNotEmpty(batch)) {
                List<TokenInventoryWithBLOBs> updateParams = new ArrayList<>();
                batch.forEach(inventory -> {
                    TokenInventoryWithBLOBs updateTokenInventory = new TokenInventoryWithBLOBs();
                    updateTokenInventory.setTokenId(inventory.getTokenId());
                    updateTokenInventory.setTokenAddress(inventory.getTokenAddress());
                    updateTokenInventory.setTokenUrl(inventory.getTokenUrl());
                    try {
                        if (StrUtil.isNotBlank(inventory.getTokenUrl())) {
                            Request request = new Request.Builder().url(inventory.getTokenUrl()).build();
                            Response response = CustomHttpClient.getOkHttpClient().newCall(request).execute();
                            if (response.code() == 200) {
                                String resp = response.body().string();
                                UpdateTokenInventory newTi = JSONUtil.toBean(resp, UpdateTokenInventory.class);
                                newTi.setTokenId(inventory.getTokenId());
                                newTi.setTokenAddress(inventory.getTokenAddress());
                                boolean changed = false;
                                // Whenever an attribute changes, it is added to the update list
                                if (ObjectUtil.isNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImageUrl())) {
                                    updateTokenInventory.setImage(newTi.getImageUrl());
                                    changed = true;
                                } else if (ObjectUtil.isNotNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImageUrl()) && !inventory.getImage().equals(newTi.getImageUrl())) {
                                    updateTokenInventory.setImage(newTi.getImageUrl());
                                    changed = true;
                                }
                                if (ObjectUtil.isNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImage())) {
                                    updateTokenInventory.setImage(newTi.getImage());
                                    changed = true;
                                } else if (ObjectUtil.isNotNull(inventory.getImage()) && ObjectUtil.isNotNull(newTi.getImage()) && !inventory.getImage().equals(newTi.getImage())) {
                                    updateTokenInventory.setImage(newTi.getImage());
                                    changed = true;
                                }
                                if (ObjectUtil.isNull(inventory.getDescription()) && ObjectUtil.isNotNull(newTi.getDescription())) {
                                    updateTokenInventory.setDescription(newTi.getDescription());
                                    changed = true;
                                } else if (ObjectUtil.isNotNull(inventory.getDescription()) && ObjectUtil.isNotNull(newTi.getDescription()) && !inventory.getDescription()
                                                                                                                                                         .equals(newTi.getDescription())) {
                                    updateTokenInventory.setDescription(newTi.getDescription());
                                    changed = true;
                                }
                                if (ObjectUtil.isNull(inventory.getName()) && ObjectUtil.isNotNull(newTi.getName())) {
                                    updateTokenInventory.setName(newTi.getName());
                                    changed = true;
                                } else if (ObjectUtil.isNotNull(inventory.getName()) && ObjectUtil.isNotNull(newTi.getName()) && !inventory.getName().equals(newTi.getName())) {
                                    updateTokenInventory.setName(newTi.getName());
                                    changed = true;
                                }
                                if (changed) {
                                    updateNum.getAndIncrement();
                                    updateTokenInventory.setRetryNum(0);
                                    updateParams.add(updateTokenInventory);
                                    String msg = StrUtil.format("There are attribute changes in the inventory that need to be updated, token[{}]", JSONUtil.toJsonStr(updateTokenInventory));
                                    XxlJobHelper.log(msg);
                                    log.info(msg);
                                }
                            } else {
                                errorNum.getAndIncrement();
                                updateTokenInventory.setRetryNum(inventory.getRetryNum() + 1);
                                updateParams.add(updateTokenInventory);
                                String msg = StrUtil.format("http request exception: http status code: {}, http message: {}, breakpoint: {}, token: {}", response.code(), response.message(), oldPosition , JSONUtil.toJsonStr(updateTokenInventory));
                                XxlJobHelper.log(msg);
                                log.warn(msg);
                            }
                        } else {
                            errorNum.getAndIncrement();
                            updateTokenInventory.setRetryNum(inventory.getRetryNum() + 1);
                            updateParams.add(updateTokenInventory);
                            String msg = StrUtil.format("The request TokenURI is empty, breakpoint: {}, token: {}", oldPosition, JSONUtil.toJsonStr(updateTokenInventory));
                            XxlJobHelper.log(msg);
                            log.warn(msg);
                        }
                    } catch (Exception e) {
                        errorNum.getAndIncrement();
                        updateTokenInventory.setRetryNum(inventory.getRetryNum() + 1);
                        updateParams.add(updateTokenInventory);
                        log.warn(StrUtil.format("Exception in incremental update of token inventory information, breakpoint: {}, token: {}", oldPosition, JSONUtil.toJsonStr(updateTokenInventory)), e);
                    }
                });
                if (CollUtil.isNotEmpty(updateParams)) {
                    customToken721InventoryMapper.batchUpdateTokenInfo(updateParams);
                }
                TokenInventory lastTokenInventory = CollUtil.getLast(batch);
                String newPosition = Convert.toStr(lastTokenInventory.getId());
                pointLog.setPosition(newPosition);
                pointLogMapper.updateByPrimaryKeySelective(pointLog);
                String msg = StrUtil.format("Incremental update of token inventory information: breakpoint is [{}]->[{}], the number of items queried is: {}, the number of items updated is: {}, failed The number of items is: {}", oldPosition, newPosition, batch.size(), updateNum.get(), errorNum.get());
                XxlJobHelper.log(msg);
                log.info(msg);
                XxlJobHelper.handleSuccess(msg);
            } else {
                XxlJobHelper.log("Incremental update of token inventory information completed, no data found, breakpoint is [{}]", oldPosition);
            }
        } catch (Exception e) {
            log.error(StrUtil.format("Incremental update of token inventory information exception, breakpoint: {}", oldPosition), e);
        }
    }

    /**
     * Update token1155 inventory information => incremental update
     * Update token inventory information => incremental update
     *
     * @return void
     */
    private void cronIncrementUpdateToken1155Inventory() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        //The number of items currently queried
        int batchNum = 0;
        //The current number of failed items
        AtomicInteger errorNum = new AtomicInteger(0);
        //The number of items updated this time
        AtomicInteger updateNum = new AtomicInteger(0);
        PointLog pointLog = pointLogMapper.selectByPrimaryKey(9);
        Long oldPosition = Convert.toLong(pointLog.getPosition());
        int batchSize = Convert.toInt(XxlJobHelper.getJobParam(), 100);
        XxlJobHelper.log("The current page number is [{}], the breakpoint is [{}]", batchSize, oldPosition);try {
            Token1155InventoryExample condition = new Token1155InventoryExample();
            condition.setOrderByClause("id");
            condition.createCriteria().andIdGreaterThan(oldPosition).andIdLessThanOrEqualTo(oldPosition + batchSize).andRetryNumLessThan(tokenRetryNum);
            //Update token inventory related information in pages
            List<Token1155InventoryWithBLOBs> batch = token1155InventoryMapper.selectByExampleWithBLOBs(condition);
            if (CollUtil.isNotEmpty(batch)) {
                List<Token1155InventoryWithBLOBs> res = token1155InventorySubtractToList(batch, getDestroyContracts());
                List<Token1155InventoryWithBLOBs> updateParams = new ArrayList<>();
                if (CollUtil.isNotEmpty(res)) {
                    batchNum = res.size();
                    res.forEach(inventory -> {
                        try {
                            if (StrUtil.isNotBlank(inventory.getTokenUrl())) {
                                Request request = new Request.Builder().url(inventory.getTokenUrl()).build();
                                Response response = CustomHttpClient.getOkHttpClient().newCall(request).execute();
                                if (response.code() == 200) {
                                    String resp = response.body().string();
                                    Token1155InventoryWithBLOBs newTi = JSONUtil.toBean(resp, Token1155InventoryWithBLOBs.class);
                                    newTi.setTokenId(inventory.getTokenId());
                                    newTi.setTokenAddress(inventory.getTokenAddress());
                                    boolean changed = false;
                                    // Whenever an attribute changes, it is added to the update list
                                    if (ObjectUtil.isNull(inventory.getImage()) || !newTi.getImage().equals(inventory.getImage())) {
                                        inventory.setImage(newTi.getImage());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getDescription()) || !newTi.getDescription().equals(inventory.getDescription())) {
                                        inventory.setDescription(newTi.getDescription());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getName()) || !newTi.getName().equals(inventory.getName())) {
                                        inventory.setName(newTi.getName());
                                        changed = true;
                                    }
                                    if (ObjectUtil.isNull(inventory.getDecimal()) || !newTi.getDecimal().equals(inventory.getDecimal())) {
                                        inventory.setDecimal(newTi.getDecimal());
                                        changed = true;
                                    }
                                    if (changed) {
                                        updateNum.getAndIncrement();
                                        inventory.setRetryNum(0);
                                        updateParams.add(inventory);
                                        String msg = StrUtil.format("token[{}] inventory has attribute changes that need to be updated, tokenURL[{}], tokenName[{}], tokenDesc[{}], tokenImage[{}],decimal[{}]" ,
                                                inventory.getTokenAddress(),
                                                inventory.getTokenUrl(),
                                                inventory.getName(),
                                                inventory.getDescription(),
                                                inventory.getImage(),
                                                inventory.getDecimal());XxlJobHelper.log(msg);
                                        log.info(msg);
                                    }
                                } else {
                                    errorNum.getAndIncrement();
                                    inventory.setRetryNum(inventory.getRetryNum() + 1);
                                    updateParams.add(inventory);
                                    String msg = StrUtil.format("http request exception: http status code: {}, http message: {}, breakpoint: {}, token_address: {}, token_id: {}, tokenURI: {}, number of retries: {}",
                                            response.code(),
                                            response.message(),
                                            oldPosition,
                                            inventory.getTokenAddress(),
                                            inventory.getTokenId(),
                                            inventory.getTokenUrl(),
                                            inventory.getRetryNum());
                                    XxlJobHelper.log(msg);
                                    log.warn(msg);
                                }
                            } else {
                                errorNum.getAndIncrement();
                                inventory.setRetryNum(inventory.getRetryNum() + 1);
                                updateParams.add(inventory);
                                String msg = StrUtil.format("The request TokenURI is empty, breakpoint: {}, token_address: {}, token_id: {}, number of retries: {}",
                                        oldPosition,
                                        inventory.getTokenAddress(),
                                        inventory.getTokenId(),
                                        inventory.getRetryNum());XxlJobHelper.log(msg);
                                log.warn(msg);
                            }
                        } catch (Exception e) {
                            errorNum.getAndIncrement();
                            inventory.setRetryNum(inventory.getRetryNum() + 1);
                            updateParams.add(inventory);
                            log.warn(StrUtil.format("Exception in incremental update of token inventory information, breakpoint: {}, token_address: {}, token_id: {}, tokenURI: {}, number of retries: {}",
                                    oldPosition,
                                    inventory.getTokenAddress(),
                                    inventory.getTokenId(),
                                    inventory.getTokenUrl(),
                                    inventory.getRetryNum()), e);
                        }
                    });
                }
                if (CollUtil.isNotEmpty(updateParams)) {
                    customToken1155InventoryMapper.batchInsertOrUpdateSelective(updateParams, Token1155Inventory.Column.values());
                }
                Token1155Inventory lastTokenInventory = CollUtil.getLast(batch);
                String newPosition = Convert.toStr(lastTokenInventory.getId());
                pointLog.setPosition(newPosition);
                pointLogMapper.updateByPrimaryKeySelective(pointLog);
                String msg = StrUtil.format("Incremental update of token inventory information: breakpoint is [{}]->[{}], number of queried items: {}, number of filtered items: {}, updated The number of items is: {}, the number of failed items is: {}", oldPosition, newPosition, batch.size(), batchNum, updateNum.get(), errorNum.get());
                XxlJobHelper.log(msg);
                log.info(msg);
            } else {
                XxlJobHelper.log("Incremental update of token inventory information completed, no data found, breakpoint is [{}]", oldPosition);
            }
        } catch (Exception e) {
            log.error(StrUtil.format("Incremental update of token inventory information exception, breakpoint: {}", oldPosition), e);
        }
    }

    private void contractErc20DestroyUpdateBalance() {
        try {
            List<DestroyContract> tokenList = customTokenMapper.findDestroyContract(ErcTypeEnum.ERC20.getDesc());
            if (CollUtil.isNotEmpty(tokenList)) {
                List<TokenHolder> updateList = new ArrayList<>();
                for (DestroyContract destroyContract : tokenList) {
                    try {
                        BigInteger balance = ercServiceImpl.getErc20HistoryBalance(destroyContract.getTokenAddress(),
                                                                                   destroyContract.getAccount(),
                                                                                   BigInteger.valueOf(destroyContract.getContractDestroyBlock() - 1));
                        TokenHolder tokenHolder = new TokenHolder();
                        tokenHolder.setTokenAddress(destroyContract.getTokenAddress());
                        tokenHolder.setAddress(destroyContract.getAccount());
                        tokenHolder.setBalance(balance.toString());
                        updateList.add(tokenHolder);
                    } catch (Exception e) {
                        log.error(StrUtil.format("The destroyed erc20 contract [{}] account [{}] updated the balance abnormally", destroyContract.getTokenAddress(), destroyContract.getAccount()), e);
                    }
                }
                if (CollUtil.isNotEmpty(updateList)) {
                    customTokenHolderMapper.batchUpdate(updateList);
                    Set<String> destroyContractSet = updateList.stream().map(TokenHolderKey::getTokenAddress).collect(Collectors.toSet());
                    for (String destroyContract : destroyContractSet) {
                        Token token = new Token();
                        token.setAddress(destroyContract);
                        token.setContractDestroyUpdate(true);
                        tokenMapper.updateByPrimaryKeySelective(token);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Abnormal update of destroyed erc20 contract balance", e);
        }
    }

    /**
     * Filter destroyed contracts
     *
     * @param list:
     * @param destroyContracts:
     * @return: java.util.List<com.turn.browser.dao.entity.TokenInventory>
     */
    private List<Token1155InventoryWithBLOBs> token1155InventorySubtractToList(List<Token1155InventoryWithBLOBs> list, Set<String> destroyContracts) {
        List<Token1155InventoryWithBLOBs> res = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(list)) {
            for (Token1155InventoryWithBLOBs tokenInventory : list) {
                if (!destroyContracts.contains(tokenInventory.getTokenAddress())) {
                    res.add(tokenInventory);
                }
            }
        }
        return res;
    }

    /**
     * Destroyed erc721 updates balance
     *
     * @param :
     * @return: void
     */
    private void contractErc721DestroyUpdateBalance() {
        try {
            List<String> contractErc721Destroys = customAddressMapper.findContractDestroy(AddressTypeEnum.ERC721_EVM_CONTRACT.getCode());
            if (CollUtil.isNotEmpty(contractErc721Destroys)) {
                for (String tokenAddress : contractErc721Destroys) {
                    List<Erc721ContractDestroyBalanceVO> list = customToken721InventoryMapper.findErc721ContractDestroyBalance(tokenAddress);
                    Page<CustomTokenHolder> ids = customTokenHolderMapper.selectERC721Holder(tokenAddress);
                    List<TokenHolder> updateParams = new ArrayList<>();
                    StringBuilder res = new StringBuilder();
                    for (CustomTokenHolder tokenHolder : ids) {
                        List<Erc721ContractDestroyBalanceVO> filterList = list.stream().filter(v -> v.getOwner().equalsIgnoreCase(tokenHolder.getAddress())).collect(Collectors.toList());
                        int balance = 0;
                        if (CollUtil.isNotEmpty(filterList)) {
                            balance = filterList.get(0).getNum();
                        }
                        if (!tokenHolder.getBalance().equalsIgnoreCase(cn.hutool.core.convert.Convert.toStr(balance))) {
                            TokenHolder updateTokenHolder = new TokenHolder();
                            updateTokenHolder.setTokenAddress(tokenHolder.getTokenAddress());
                            updateTokenHolder.setAddress(tokenHolder.getAddress());
                            updateTokenHolder.setBalance(cn.hutool.core.convert.Convert.toStr(balance));
                            updateParams.add(updateTokenHolder);
                            res.append(StrUtil.format("[Contract{}, Balance{}->{}] ", tokenHolder.getAddress(), tokenHolder.getBalance(), cn.hutool.core.convert.Convert.toStr(balance) ));
                        }
                    }
                    if (CollUtil.isNotEmpty(updateParams)) {
                        customTokenHolderMapper.batchUpdate(updateParams);
                        log.info("The destroyed erc721[{}] updated the balance successfully, and the result is {}", tokenAddress, res.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Destroyed erc721 update balance exception", e);
        }
    }

    /**
     * Destroyed erc1155 updates balance
     *
     * @param :
     * @return: void
     */
    private void contractErc1155DestroyUpdateBalance() {
        try {
            List<Erc1155ContractDestroyBean> contractErc1155Destroys = customToken1155HolderMapper.findDestroyContract(AddressTypeEnum.ERC1155_EVM_CONTRACT.getCode());
            if (CollUtil.isNotEmpty(contractErc1155Destroys)) {
                List<Token1155Holder> updateList = new ArrayList<>();
                for (Erc1155ContractDestroyBean erc1155ContractDestroyBean : contractErc1155Destroys) {
                    try {
                        BigInteger balance = ercServiceImpl.getErc1155HistoryBalance(erc1155ContractDestroyBean.getTokenAddress(),
                                                                                     new BigInteger(erc1155ContractDestroyBean.getTokenId()),
                                                                                     erc1155ContractDestroyBean.getAddress(),
                                                                                     BigInteger.valueOf(erc1155ContractDestroyBean.getContractDestroyBlock() - 1));
                        Token1155Holder tokenHolder = new Token1155Holder();
                        tokenHolder.setTokenAddress(erc1155ContractDestroyBean.getTokenAddress());
                        tokenHolder.setTokenId(erc1155ContractDestroyBean.getTokenId());
                        tokenHolder.setAddress(erc1155ContractDestroyBean.getAddress());
                        tokenHolder.setBalance(balance.toString());
                    } catch (Exception e) {
                        log.error(StrUtil.format("Destroyed erc1155 contract [{}][{}] account [{}] updated balance abnormally",erc1155ContractDestroyBean.getTokenAddress(),
                                                 erc1155ContractDestroyBean.getTokenId(),
                                                 erc1155ContractDestroyBean.getAddress()), e);
                    }
                }
                if (CollUtil.isNotEmpty(updateList)) {
                    customToken1155HolderMapper.batchUpdate(updateList);
                    Set<String> destroyContractSet = updateList.stream().map(Token1155Holder::getTokenAddress).collect(Collectors.toSet());
                    for (String destroyContract : destroyContractSet) {
                        Token token = new Token();
                        token.setAddress(destroyContract);
                        token.setContractDestroyUpdate(true);
                        tokenMapper.updateByPrimaryKeySelective(token);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Destroyed erc1155 update balance exception", e);
        }
    }

    /**
     * Get ercToken
     *
     * @param :
     * @return: java.util.List<com.turn.browser.v0152.bean.ErcToken>
     */
    private List<ErcToken> getErcTokens() {
        List<ErcToken> ercTokens = new ArrayList<>();
        List<Token> tokens = tokenMapper.selectByExample(null);
        tokens.forEach(token -> {
            ErcToken et = new ErcToken();
            BeanUtils.copyProperties(token, et);
            ErcTypeEnum typeEnum = ErcTypeEnum.valueOf(token.getType().toUpperCase());
            et.setTypeEnum(typeEnum);
            ercTokens.add(et);
        });
        return ercTokens;
    }

    /**
     * Get the destroyed contract
     *
     * @param :
     * @return: java.util.Set<java.lang.String>
     */
    private Set<String> getDestroyContracts() {
        Set<String> destroyContracts = new HashSet<>();
        List<String> list = customAddressMapper.findContractDestroy(null);
        destroyContracts.addAll(list);
        return destroyContracts;
    }

    /**
     * Filter destroyed contracts
     *
     * @param ercTokens:
     * @param destroyContracts:
     * @return: java.util.List<com.turn.browser.v0152.bean.ErcToken>
     */
    private List<ErcToken> tokenSubtractToList(List<ErcToken> ercTokens, Set<String> destroyContracts) {
        List<ErcToken> res = CollUtil.newArrayList();
        for (ErcToken ercToken : ercTokens) {
            if (!destroyContracts.contains(ercToken.getAddress())) {
                res.add(ercToken);
            }
        }
        return res;
    }

    /**
     * Update the number of holders corresponding to the token
     *
     * @param
     * @return void
     */
    private void updateTokenHolderCount() {
        List<Token> updateTokenList = new ArrayList<>();
        List<TokenHolderCount> list = customTokenHolderMapper.findTokenHolderCount();
        List<Token> tokenList = tokenMapper.selectByExample(null);
        if (CollUtil.isNotEmpty(list) && CollUtil.isNotEmpty(tokenList)) {
            list.forEach(tokenHolderCount -> {
                tokenList.forEach(token -> {
                    if (token.getAddress().equalsIgnoreCase(tokenHolderCount.getTokenAddress()) && !token.getHolder().equals(tokenHolderCount.getTokenHolderCount())) {
                        token.setHolder(tokenHolderCount.getTokenHolderCount());
                        updateTokenList.add(token);
                        TaskUtil.console("Update the number of holders corresponding to token[{}][{}]", token.getAddress(), token.getHolder());
                    }
                });
            });
        }
        List<TokenHolderCount> token1155List = customToken1155HolderMapper.findToken1155Holder();
        if (CollUtil.isNotEmpty(token1155List) && CollUtil.isNotEmpty(tokenList)) {
            token1155List.forEach(tokenHolderCount -> {
                tokenList.forEach(token -> {
                    if (token.getAddress().equalsIgnoreCase(tokenHolderCount.getTokenAddress()) && !token.getHolder().equals(tokenHolderCount.getTokenHolderCount())) {
                        token.setHolder(tokenHolderCount.getTokenHolderCount());
                        updateTokenList.add(token);
                        TaskUtil.console("Update the number of holders corresponding to token[{}][{}]", token.getAddress(), token.getHolder());
                    }
                });
            });
        }
        if (CollUtil.isNotEmpty(updateTokenList)) {
            customTokenMapper.batchUpdateTokenHolder(updateTokenList);
        }
        XxlJobHelper.log("Update the number of holders corresponding to the token completed");
    }

    private HashMap<String, HashSet<String>> subtractToMap(HashMap<String, HashSet<String>> map, Set<String> destroyContracts) {
        HashMap<String, HashSet<String>> res = CollUtil.newHashMap();
        if (CollUtil.isNotEmpty(map)) {
            for (Map.Entry<String, HashSet<String>> entry : map.entrySet()) {
                if (!destroyContracts.contains(entry.getKey())) {
                    res.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return res;
    }

    /**
     * Filter erc1155 destroyed addresses
     *
     * @param list:
     * @param destroyContracts:
     * @return: java.util.List<com.turn.browser.dao.entity.Token1155Holder>
     */
    private List<Token1155Holder> subtractErc1155ToLis(List<Token1155Holder> list, Set<String> destroyContracts) {
        List<Token1155Holder> newList = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            for (Token1155Holder token1155Holder : list) {
                if (!destroyContracts.contains(token1155Holder.getTokenAddress())) {
                    newList.add(token1155Holder);
                }
            }
        }
        return newList;
    }

    /**
     * Filter destroyed contracts
     *
     * @param list:
     * @param destroyContracts:
     * @return: java.util.List<com.turn.browser.dao.entity.TokenHolder>
     */
    private List<TokenHolder> subtractToList(List<TokenHolder> list, Set<String> destroyContracts) {
        List<TokenHolder> res = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(list)) {
            for (TokenHolder tokenHolder : list) {
                if (!destroyContracts.contains(tokenHolder.getTokenAddress())) {
                    res.add(tokenHolder);
                }
            }
        }
        return res;
    }

}
