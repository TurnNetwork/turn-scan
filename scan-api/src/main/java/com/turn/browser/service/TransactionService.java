package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.bean.keybase.KeyBaseUserInfo;
import com.turn.browser.cache.TransactionCacheDto;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.constant.Browser;
import com.turn.browser.dao.custommapper.CustomToken1155InventoryMapper;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.mapper.TokenInventoryMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.DelegationReward.Extra;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.elasticsearch.dto.Transaction.StatusEnum;
import com.turn.browser.elasticsearch.dto.Transaction.ToTypeEnum;
import com.turn.browser.elasticsearch.dto.Transaction.TypeEnum;
import com.turn.browser.enums.AddressTypeEnum;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RedeemStatusEnum;
import com.turn.browser.enums.ReqTransactionTypeEnum;
import com.turn.browser.param.*;
import com.turn.browser.param.claim.Reward;
import com.turn.browser.request.PageReq;
import com.turn.browser.request.newtransaction.TransactionDetailsReq;
import com.turn.browser.request.newtransaction.TransactionListByAddressRequest;
import com.turn.browser.request.newtransaction.TransactionListByBlockRequest;
import com.turn.browser.request.staking.QueryClaimByStakingReq;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.response.staking.QueryClaimByStakingResp;
import com.turn.browser.response.transaction.*;
import com.turn.browser.service.elasticsearch.EsDelegationRewardRepository;
import com.turn.browser.service.elasticsearch.EsTransactionRepository;
import com.turn.browser.service.elasticsearch.bean.ESResult;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilderConstructor;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilders;
import com.turn.browser.utils.*;
import com.bubble.utils.Convert;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Trading method logic implementation
 */
@Service
public class TransactionService {

    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Resource
    private EsTransactionRepository ESTransactionRepository;

    @Resource
    private EsDelegationRewardRepository ESDelegationRewardRepository;

    @Resource
    private I18nUtil i18n;

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private ProposalMapper proposalMapper;

    @Resource
    private TokenInventoryMapper tokenInventoryMapper;

    @Resource
    private CustomToken1155InventoryMapper customToken1155InventoryMapper;

    @Resource
    private StatisticCacheService statisticCacheService;

    @Resource
    private BlockChainConfig blockChainConfig;

    @Resource
    private CommonService commonService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private AddressMapper addressMapper;

    private static final String ERROR_TIPS = "Get block error。";

    @Value("${turn.valueUnit}")
    private String valueUnit;

    public RespPage<TransactionListResp> getTransactionList(PageReq req) {
        RespPage<TransactionListResp> result = new RespPage<>();
        /** Query redis transaction data by page */
        TransactionCacheDto transactionCacheDto = this.statisticCacheService.getTransactionCache(req.getPageNo(),
                                                                                                 req.getPageSize());
        List<Transaction> items = transactionCacheDto.getTransactionList();
        /**
         * data conversion
         */
        List<TransactionListResp> lists = this.transferList(items);
        NetworkStat networkStat = this.statisticCacheService.getNetworkStatCache();
        result.init(lists,
                    null == networkStat.getTxQty() ? 0 : networkStat.getTxQty(),
                    transactionCacheDto.getPage().getTotalCount(),
                    transactionCacheDto.getPage().getTotalPages());
        return result;
    }

    public RespPage<TransactionListResp> getTransactionListByBlock(TransactionListByBlockRequest req) {
        RespPage<TransactionListResp> result = new RespPage<>();
        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().term("num", req.getBlockNumber()));
        ESResult<Transaction> items = new ESResult<>();
        if (req.getTxType() != null && !req.getTxType().isEmpty()) {
            constructor.must(new ESQueryBuilders().terms("type", ReqTransactionTypeEnum.getTxType(req.getTxType())));
        }
        constructor.setDesc("seq");
        constructor.setUnmappedType("long");
        constructor.setResult(new String[]{"hash",
                                           "time",
                                           "status",
                                           "from",
                                           "to",
                                           "value",
                                           "num",
                                           "type",
                                           "toType",
                                           "cost",
                                           "failReason"});
        /** Query transaction information in pages based on block number and type */
        try {
            items = this.ESTransactionRepository.search(constructor,
                                                        Transaction.class,
                                                        req.getPageNo(),
                                                        req.getPageSize());
        } catch (Exception e) {
            this.logger.error(ERROR_TIPS, e);
            return result;
        }
        List<TransactionListResp> lists = this.transferList(items.getRsData());
        /** Statistical transaction information */
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        result.init(page, lists);
        result.setTotalCount(items.getTotal());
        return result;
    }

    public RespPage<TransactionListResp> getTransactionListByAddress(TransactionListByAddressRequest req) {
        RespPage<TransactionListResp> result = new RespPage<>();

        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();

        ESResult<Transaction> items = new ESResult<>();
        if (req.getTxType() != null && !req.getTxType().isEmpty()) {
            constructor.must(new ESQueryBuilders().terms("type", ReqTransactionTypeEnum.getTxType(req.getTxType())));
        }
        constructor.buildMust(new BoolQueryBuilder().should(QueryBuilders.termQuery("from", req.getAddress()))
                                                    .should(QueryBuilders.termQuery("to", req.getAddress())));
        constructor.setDesc("seq");
        constructor.setUnmappedType("long");
        constructor.setResult(new String[]{"hash",
                                           "time",
                                           "status",
                                           "from",
                                           "to",
                                           "value",
                                           "num",
                                           "type",
                                           "toType",
                                           "cost",
                                           "failReason"});
        try {
            items = this.ESTransactionRepository.search(constructor,
                                                        Transaction.class,
                                                        req.getPageNo(),
                                                        req.getPageSize());
        } catch (Exception e) {
            this.logger.error(ERROR_TIPS, e);
            return result;
        }
        List<TransactionListResp> lists = this.transferList(items.getRsData());
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        result.init(page, lists);
        result.setTotalCount(items.getTotal());
        return result;
    }

    private List<TransactionListResp> transferList(List<Transaction> items) {
        List<TransactionListResp> lists = new LinkedList<>();
        for (Transaction transaction : items) {
            TransactionListResp transactionListResp = new TransactionListResp();
            BeanUtils.copyProperties(transaction, transactionListResp);
            transactionListResp.setTxHash(transaction.getHash());
            transactionListResp.setActualTxCost(new BigDecimal(transaction.getCost()));
            transactionListResp.setBlockNumber(transaction.getNum());
            transactionListResp.setReceiveType(String.valueOf(transaction.getToType()));
            /**
             * wasm is also a contract creation
             */
            if (transaction.getType() == TypeEnum.WASM_CONTRACT_CREATE.getCode()) {
                transactionListResp.setTxType(String.valueOf(TypeEnum.EVM_CONTRACT_CREATE.getCode()));
            } else {
                transactionListResp.setTxType(String.valueOf(transaction.getType()));
            }
            transactionListResp.setServerTime(new Date().getTime());
            transactionListResp.setTimestamp(transaction.getTime().getTime());
            transactionListResp.setValue(new BigDecimal(transaction.getValue()));
            /**
             * Failure information internationalization
             */
            I18nEnum i18nEnum = I18nEnum.getEnum("CODE" + transaction.getFailReason());
            if (i18nEnum != null) {
                transactionListResp.setFailReason(this.i18n.i(i18nEnum));
            }
            if (StatusEnum.FAILURE.getCode() == transaction.getStatus()) {
                transactionListResp.setTxReceiptStatus(0);
            } else {
                transactionListResp.setTxReceiptStatus(transaction.getStatus());
            }
            lists.add(transactionListResp);
        }
        return lists;
    }

    public AccountDownload transactionListByAddressDownload(String address, Long date, String local, String timeZone) {
        AccountDownload accountDownload = new AccountDownload();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentServerTime = new Date();
        this.logger.info("Export address transaction list data start date: {}, end date: {}",
                         dateFormat.format(date),
                         dateFormat.format(currentServerTime));

        /** Limit export to 30,000 records at most */

        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().range("time", new Date(date).getTime(), currentServerTime.getTime()));
        constructor.buildMust(new BoolQueryBuilder().should(QueryBuilders.termQuery("from", address))
                                                    .should(QueryBuilders.termQuery("to", address)));
        ESResult<Transaction> items = new ESResult<>();
        constructor.setDesc("seq");
        constructor.setUnmappedType("long");
        constructor.setResult(new String[]{"hash",
                                           "time",
                                           "status",
                                           "from",
                                           "to",
                                           "value",
                                           "num",
                                           "type",
                                           "toType",
                                           "cost"});
        try {
            items = this.ESTransactionRepository.search(constructor, Transaction.class, 1, 30000);
        } catch (Exception e) {
            this.logger.error(ERROR_TIPS, e);
            return accountDownload;
        }
        List<Object[]> rows = new ArrayList<>();
        items.getRsData().forEach(transaction -> {
            /**
             * Determine whether it is a to address. If it is a to address, export the report as the income amount. If it is the from address, export the report as the expenditure amount.
             */
            boolean toIsAddress = address.equals(transaction.getTo());
            String valueIn = toIsAddress ? transaction.getValue() : "0";
            String valueOut = !toIsAddress ? transaction.getValue() : "0";
            Object[] row = {transaction.getHash(),
                            transaction.getNum(),
                            DateUtil.timeZoneTransfer(transaction.getTime(), "0", timeZone),
                            /**
                             * The enumeration type name needs to correspond to
                            */
                            this.i18n.getMessageForStr(Transaction.TypeEnum.getEnum(transaction.getType()).toString(),
                                                       local),
                            transaction.getFrom(),
                            transaction.getTo(),
                            /** 数值AAA转换成TURN，并保留十八位精确度 Convert the value AAA to lat, retaining eighteen digits of precision*/
                            HexUtil.append(EnergonUtil.format(Convert.fromVon(valueIn, Convert.Unit.KPVON)
                                                                     .setScale(18, RoundingMode.DOWN), 18)),
                            HexUtil.append(EnergonUtil.format(Convert.fromVon(valueOut, Convert.Unit.KPVON)
                                                                     .setScale(18, RoundingMode.DOWN), 18)),
                            HexUtil.append(EnergonUtil.format(Convert.fromVon(transaction.getCost(), Convert.Unit.KPVON)
                                                                     .setScale(18, RoundingMode.DOWN), 18))};
            rows.add(row);
        });
        String[] headers = {this.i18n.i(I18nEnum.DOWNLOAD_ACCOUNT_CSV_HASH, local),
                            this.i18n.i(I18nEnum.DOWNLOAD_BLOCK_CSV_NUMBER, local),
                            this.i18n.i(I18nEnum.DOWNLOAD_BLOCK_CSV_TIMESTAMP, local),
                            this.i18n.i(I18nEnum.DOWNLOAD_ACCOUNT_CSV_TYPE, local),
                            this.i18n.i(I18nEnum.DOWNLOAD_ACCOUNT_CSV_FROM, local),
                            this.i18n.i(I18nEnum.DOWNLOAD_ACCOUNT_CSV_TO, local),
                            this.i18n.i(I18nEnum.DOWNLOAD_ACCOUNT_CSV_VALUE_IN, local) + "(" + valueUnit + ")",
                            this.i18n.i(I18nEnum.DOWNLOAD_ACCOUNT_CSV_VALUE_OUT, local) + "(" + valueUnit + ")",
                            this.i18n.i(I18nEnum.DOWNLOAD_ACCOUNT_CSV_FEE, local) + "(" + valueUnit + ")"};
        return this.downFileCommon.writeDate("Transaction-" + address + "-" + date + ".CSV", rows, headers);
    }

    public TransactionDetailsResp transactionDetails(TransactionDetailsReq req) {
        TransactionDetailsResp resp = new TransactionDetailsResp();
        /** Query specific transaction data based on hash */
        Transaction transaction = null;
        try {
            transaction = this.ESTransactionRepository.get(req.getTxHash(), Transaction.class);
        } catch (Exception e) {
            this.logger.error(ERROR_TIPS, e);
            return resp;
        }
        if (transaction != null) {
            BeanUtils.copyProperties(transaction, resp);
            resp.setActualTxCost(new BigDecimal(transaction.getCost()));
            resp.setBlockNumber(transaction.getNum());
            resp.setGasLimit(transaction.getGasLimit());
            resp.setGasUsed(transaction.getGasUsed());
            /**
             * wasm is also a contract creation
             */
            if (transaction.getType() == TypeEnum.WASM_CONTRACT_CREATE.getCode()) {
                resp.setTxType(String.valueOf(TypeEnum.EVM_CONTRACT_CREATE.getCode()));
            } else {
                resp.setTxType(String.valueOf(transaction.getType()));
            }
            resp.setTxHash(transaction.getHash());
            resp.setTimestamp(transaction.getTime().getTime());
            resp.setServerTime(new Date().getTime());
            resp.setTxInfo(transaction.getInfo());
            resp.setGasPrice(new BigDecimal(transaction.getGasPrice()));
            resp.setValue(new BigDecimal(transaction.getValue()));
            /**
             * Set contract type
             */
            if (transaction.getToType() == ToTypeEnum.ACCOUNT.getCode()) {
                resp.setReceiveType("2");
            } else {
                resp.setReceiveType("1");
            }
            resp.setContractName(transaction.getMethod());
            /**
             * Failure information internationalization
             */
            String name = "CODE" + transaction.getFailReason();
            // 针对304013特殊处理
            if ("304013".equalsIgnoreCase(transaction.getFailReason()) && TypeEnum.DELEGATE_CREATE.getCode() == transaction.getType()) {
                name = "DELETEGATE_CODE304013";
            }
            I18nEnum i18nEnum = I18nEnum.getEnum(name);
            if (i18nEnum != null) {
                resp.setFailReason(this.i18n.i(i18nEnum));
            }
            if (StatusEnum.FAILURE.getCode() == transaction.getStatus()) {
                resp.setTxReceiptStatus(0);
            } else {
                resp.setTxReceiptStatus(transaction.getStatus());
            }
            List<Block> blocks = this.statisticCacheService.getBlockCache(0, 1);
            /** The number of confirmed blocks is equal to the current block book minus the number of transaction blocks */
            if (!blocks.isEmpty()) {
                resp.setConfirmNum(String.valueOf(blocks.get(0).getNum() - transaction.getNum()));
            } else {
                resp.setConfirmNum("0");
            }

            /** If the data value is null, set it to empty */
            if ("null".equals(transaction.getInfo())) {
                resp.setTxInfo("0x");
            }

            resp.setFirst(false);
            if (transaction.getId() == 1) {
                resp.setFirst(true);
            } else {
                /**
                 * Query whether there is previous data transaction data based on id
                 */
                ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
                constructor.must(new ESQueryBuilders().term("id", transaction.getId() - 1));
                constructor.setResult(new String[]{"hash"});
                ESResult<Transaction> first = new ESResult<>();
                try {
                    first = this.ESTransactionRepository.search(constructor, Transaction.class, 1, 1);
                } catch (Exception e) {
                    this.logger.error("Get transaction error。", e);
                    return resp;
                }
                if (first.getTotal() > 0l) {
                    resp.setPreHash(first.getRsData().get(0).getHash());
                }
            }

            resp.setLast(true);
            /**
             * Query whether there is a next item based on id
             */
            ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
            constructor.must(new ESQueryBuilders().term("id", transaction.getId() + 1));
            constructor.setResult(new String[]{"hash"});
            ESResult<Transaction> last = new ESResult<>();
            try {
                last = this.ESTransactionRepository.search(constructor, Transaction.class, 1, 1);
            } catch (Exception e) {
                this.logger.error("Get transaction error。", e);
                return resp;
            }
            if (last.getTotal() > 0) {
                resp.setLast(false);
                resp.setNextHash(last.getRsData().get(0).getHash());
            }

            String txInfo = transaction.getInfo();
            /** Determine logic based on different transaction types */
            if (StringUtils.isNotBlank(txInfo) && (!"null".equals(txInfo) && (!"{}".equals(txInfo)))) {
                switch (Transaction.TypeEnum.getEnum(transaction.getType())) {

                    case STAKE_CREATE:
                        try {
                            StakeCreateParam createValidatorParam = JSON.parseObject(txInfo, StakeCreateParam.class);
                            resp.setBenefitAddr(createValidatorParam.getBenefitAddress());
                            resp.setNodeId(createValidatorParam.getNodeId());
                            resp.setNodeName(createValidatorParam.getNodeName());
                            resp.setExternalId(createValidatorParam.getExternalId());
                            resp.setWebsite(createValidatorParam.getWebsite());
                            resp.setDetails(createValidatorParam.getDetails());
                            resp.setProgramVersion(createValidatorParam.getProgramVersion().toString());
                            resp.setTxAmount(createValidatorParam.getAmount());
                            resp.setExternalUrl(this.getStakingUrl(createValidatorParam.getExternalId(),
                                                                   resp.getTxReceiptStatus()));
                            resp.setDelegationRatio(new BigDecimal(createValidatorParam.getDelegateRewardPer()).divide(
                                    Browser.PERCENTAGE).toString());
                        } catch (Exception e) {
                            logger.error("Create validator transaction information parsing exception", e);
                        }
                        break;

                    case STAKE_MODIFY:
                        try {
                            StakeModifyParam editValidatorParam = JSON.parseObject(txInfo, StakeModifyParam.class);
                            resp.setBenefitAddr(editValidatorParam.getBenefitAddress());
                            resp.setNodeId(editValidatorParam.getNodeId());
                            resp.setExternalId(editValidatorParam.getExternalId());
                            resp.setWebsite(editValidatorParam.getWebsite());
                            resp.setDetails(editValidatorParam.getDetails());
                            resp.setNodeName(this.commonService.getNodeName(editValidatorParam.getNodeId(),
                                                                            editValidatorParam.getNodeName()));
                            resp.setExternalUrl(this.getStakingUrl(editValidatorParam.getExternalId(),
                                                                   resp.getTxReceiptStatus()));
                            String delegationRatio = null;
                            if (editValidatorParam.getDelegateRewardPer() != null) {
                                delegationRatio = new BigDecimal(editValidatorParam.getDelegateRewardPer()).divide(
                                        Browser.PERCENTAGE).toString();
                            }
                            resp.setDelegationRatio(delegationRatio);
                        } catch (Exception e) {
                            logger.error("Editing validator transaction information parsing exception", e);
                        }
                        break;

                    case STAKE_INCREASE:
                        try {
                            StakeIncreaseParam increaseStakingParam = JSON.parseObject(txInfo,
                                                                                       StakeIncreaseParam.class);
                            resp.setNodeId(increaseStakingParam.getNodeId());
                            resp.setTxAmount(increaseStakingParam.getAmount());
                            /**
                             * Node name settings
                             */
                            resp.setNodeName(this.commonService.getNodeName(increaseStakingParam.getNodeId(),
                                                                            increaseStakingParam.getNodeName()));
                        } catch (Exception e) {
                            logger.error("Added pledge transaction information parsing exception", e);
                        }
                        break;

                    case STAKE_EXIT:
                        try {
                            // nodeId + nodeName + applyAmount + redeemLocked + redeemStatus + redeemUnLockedBlock
                            StakeExitParam exitValidatorParam = JSON.parseObject(txInfo, StakeExitParam.class);
                            resp.setNodeId(exitValidatorParam.getNodeId());
                            resp.setNodeName(this.commonService.getNodeName(exitValidatorParam.getNodeId(),
                                                                            exitValidatorParam.getNodeName()));
                            resp.setApplyAmount(exitValidatorParam.getAmount());
                            StakingKey stakingKeyE = new StakingKey();
                            stakingKeyE.setNodeId(exitValidatorParam.getNodeId());
                            stakingKeyE.setStakingBlockNum(exitValidatorParam.getStakingBlockNum().longValue());
                            Staking staking = this.stakingMapper.selectByPrimaryKey(stakingKeyE);
                            if (staking != null) {
                                resp.setRedeemLocked(staking.getStakingReduction());
                                // Added pledge transaction information parsing exception
                                if (staking.getStatus() == CustomStaking.StatusEnum.EXITED.getCode()) {
                                    resp.setRedeemStatus(RedeemStatusEnum.EXITED.getCode());
                                } else {
                                    resp.setRedeemStatus(RedeemStatusEnum.EXITING.getCode());
                                }
                                resp.setRedeemUnLockedBlock(exitValidatorParam.getWithdrawBlockNum().toString());
                            }
                        } catch (Exception e) {
                            logger.error("Exit validator transaction information parsing exception", e);
                        }
                        break;

                    case DELEGATE_CREATE:
                        try {
                            DelegateCreateParam delegateParam = JSON.parseObject(txInfo, DelegateCreateParam.class);
                            resp.setNodeId(delegateParam.getNodeId());
                            resp.setTxAmount(delegateParam.getAmount());
                            resp.setNodeName(this.commonService.getNodeName(delegateParam.getNodeId(),
                                                                            delegateParam.getNodeName()));
                        } catch (Exception e) {
                            logger.error("Delegate transaction information parsing exception", e);
                        }
                        break;

                    case DELEGATE_EXIT:
                        try {
                            // nodeId + nodeName + applyAmount + redeemLocked + redeemStatus
                            // Associate un_delegation table through txHash
                            DelegateExitParam unDelegateParam = JSON.parseObject(txInfo, DelegateExitParam.class);
                            resp.setNodeId(unDelegateParam.getNodeId());
                            resp.setApplyAmount(unDelegateParam.getRealAmount());
                            resp.setTxAmount(unDelegateParam.getReward());
                            resp.setNodeName(this.commonService.getNodeName(unDelegateParam.getNodeId(),
                                                                            unDelegateParam.getNodeName()));
                        } catch (Exception e) {
                            logger.error("Exception in parsing of entrusted redemption transaction information", e);
                        }
                        break;

                    case REDEEM_DELEGATION:
                        try {
                            RedeemDelegationParm redeemDelegationParm = JSON.parseObject(txInfo,
                                                                                         RedeemDelegationParm.class);
                            resp.setRedeemDelegationValue(ConvertUtil.convertByFactor(redeemDelegationParm.getValue(),
                                                                                      18));
                        } catch (Exception e) {
                            logger.error("Exception when receiving unlocked commission", e);
                        }
                        break;

                    case PROPOSAL_TEXT:
                        try {
                            ProposalTextParam createProposalTextParam = JSON.parseObject(txInfo,
                                                                                         ProposalTextParam.class);
                            if (StringUtils.isNotBlank(createProposalTextParam.getPIDID())) {
                                resp.setPipNum("PIP-" + createProposalTextParam.getPIDID());
                            }
                            resp.setNodeId(createProposalTextParam.getVerifier());
                            resp.setProposalHash(req.getTxHash());
                            resp.setNodeName(this.commonService.getNodeName(createProposalTextParam.getVerifier(),
                                                                            createProposalTextParam.getNodeName()));
                            /** If the database has a value, the database shall prevail. */
                            this.transferTransaction(resp, req.getTxHash());
                        } catch (Exception e) {
                            logger.error("Text proposal transaction information parsing exception", e);
                        }
                        break;

                    case PROPOSAL_UPGRADE:
                        try {
                            ProposalUpgradeParam createProposalUpgradeParam = JSON.parseObject(txInfo,
                                                                                               ProposalUpgradeParam.class);
                            resp.setProposalNewVersion(String.valueOf(createProposalUpgradeParam.getNewVersion()));
                            if (StringUtils.isNotBlank(createProposalUpgradeParam.getPIDID())) {
                                resp.setPipNum("PIP-" + createProposalUpgradeParam.getPIDID());
                            }
                            resp.setNodeId(createProposalUpgradeParam.getVerifier());
                            resp.setProposalHash(req.getTxHash());
                            resp.setNodeName(this.commonService.getNodeName(createProposalUpgradeParam.getVerifier(),
                                                                            createProposalUpgradeParam.getNodeName()));
                            /** If the database has a value, the database shall prevail. */
                            this.transferTransaction(resp, req.getTxHash());
                        } catch (Exception e) {
                            logger.error("Upgrade proposal transaction information parsing exception", e);
                        }
                        break;

                    case PROPOSAL_PARAMETER:
                        try {
                            ProposalParameterParam proposalParameterParam = JSON.parseObject(txInfo,
                                                                                             ProposalParameterParam.class);
                            if (StringUtils.isNotBlank(proposalParameterParam.getPIDID())) {
                                resp.setPipNum("PIP-" + proposalParameterParam.getPIDID());
                            }
                            resp.setNodeId(proposalParameterParam.getVerifier());
                            resp.setProposalHash(req.getTxHash());
                            resp.setNodeName(this.commonService.getNodeName(proposalParameterParam.getVerifier(),
                                                                            proposalParameterParam.getNodeName()));
                            /** If the database has a value, the database shall prevail. */
                            this.transferTransaction(resp, req.getTxHash());
                        } catch (Exception e) {
                            logger.error("Parameter proposal transaction information parsing exception", e);
                        }
                        break;

                    case PROPOSAL_CANCEL:
                        try {
                            ProposalCancelParam cancelProposalParam = JSON.parseObject(txInfo,
                                                                                       ProposalCancelParam.class);
                            if (StringUtils.isNotBlank(cancelProposalParam.getPIDID())) {
                                resp.setPipNum("PIP-" + cancelProposalParam.getPIDID());
                            }
                            resp.setNodeId(cancelProposalParam.getVerifier());
                            resp.setProposalHash(req.getTxHash());
                            resp.setNodeName(this.commonService.getNodeName(cancelProposalParam.getVerifier(),
                                                                            cancelProposalParam.getNodeName()));
                            /** If the database has a value, the database shall prevail. */
                            this.transferTransaction(resp, req.getTxHash());
                        } catch (Exception e) {
                            logger.error("Cancel proposal transaction information parsing exception", e);
                        }
                        break;

                    case PROPOSAL_VOTE:
                        try {
                            // nodeId + nodeName + txType + proposalUrl + proposalHash + proposalNewVersion + proposalOption
                            ProposalVoteParam votingProposalParam = JSON.parseObject(txInfo, ProposalVoteParam.class);
                            resp.setNodeId(votingProposalParam.getVerifier());
                            resp.setProposalOption(votingProposalParam.getProposalType());
                            resp.setProposalHash(votingProposalParam.getProposalId());
                            resp.setProposalNewVersion(votingProposalParam.getProgramVersion());
                            resp.setNodeName(this.commonService.getNodeName(votingProposalParam.getVerifier(),
                                                                            votingProposalParam.getNodeName()));
                            if (StringUtils.isNotBlank(votingProposalParam.getPIDID())) {
                                resp.setPipNum("PIP-" + votingProposalParam.getPIDID());
                            }
                            resp.setVoteStatus(votingProposalParam.getOption());
                            /**
                             * 获取提案信息
                             */
                            Proposal proposal = this.proposalMapper.selectByPrimaryKey(votingProposalParam.getProposalId());
                            if (proposal != null) {
                                resp.setPipNum(proposal.getPipNum());
                                resp.setProposalTitle(Browser.INQUIRY.equals(proposal.getTopic()) ? "" : proposal.getTopic());
                                resp.setProposalUrl(proposal.getUrl());
                                resp.setProposalOption(String.valueOf(proposal.getType()));
                            }
                        } catch (Exception e) {
                            logger.error("Abnormal resolution of proposal voting transaction information", e);
                        }
                        break;

                    case VERSION_DECLARE:
                        try {
                            VersionDeclareParam declareVersionParam = JSON.parseObject(txInfo,
                                                                                       VersionDeclareParam.class);
                            resp.setNodeId(declareVersionParam.getActiveNode());
                            resp.setDeclareVersion(String.valueOf(declareVersionParam.getVersion()));
                            resp.setNodeName(this.commonService.getNodeName(declareVersionParam.getActiveNode(),
                                                                            declareVersionParam.getNodeName()));
                        } catch (Exception e) {
                            logger.error("Version statement transaction information parsing exception", e);
                        }
                        break;
                    /**
                     * Report double signing
                     */
                    case REPORT:
                        try {
                            ReportParam reportValidatorParam = JSON.parseObject(txInfo, ReportParam.class);
                            List<TransactionDetailsEvidencesResp> transactionDetailsEvidencesResps = new ArrayList<>();
                            TransactionDetailsEvidencesResp transactionDetailsEvidencesResp = new TransactionDetailsEvidencesResp();
                            transactionDetailsEvidencesResp.setVerify(reportValidatorParam.getVerify());
                            transactionDetailsEvidencesResp.setNodeName(this.commonService.getNodeName(
                                    reportValidatorParam.getVerify(),
                                    reportValidatorParam.getNodeName()));
                            resp.setEvidence(reportValidatorParam.getData());
                            transactionDetailsEvidencesResps.add(transactionDetailsEvidencesResp);
                            /**
                             * Check whether you exited after reporting to determine whether the transaction is correct.
                             */
                            resp.setReportStatus(transaction.getStatus() == 1 ? 2 : 1);
                            resp.setReportRewards(transaction.getStatus() == StatusEnum.SUCCESS.getCode() ? reportValidatorParam.getReward() : BigDecimal.ZERO);
                            resp.setReportType(reportValidatorParam.getType().intValue());
                            resp.setEvidences(transactionDetailsEvidencesResps);
                        } catch (Exception e) {
                            logger.error("Report anomalies in double-signature transaction information parsing", e);
                        }
                        break;

                    case RESTRICTING_CREATE:
                        try {
                            // RPAccount + value + RPPlan
                            RestrictingCreateParam createRestrictingParam = JSON.parseObject(txInfo,
                                                                                             RestrictingCreateParam.class);
                            List<TransactionDetailsRPPlanResp> rpPlanResps = new ArrayList<>();
                            resp.setRPAccount(createRestrictingParam.getAccount());
                            BigDecimal amountSum = new BigDecimal(0);
                            for (RestrictingCreateParam.RestrictingPlan p : createRestrictingParam.getPlans()) {
                                TransactionDetailsRPPlanResp transactionDetailsRPPlanResp = new TransactionDetailsRPPlanResp();
                                amountSum = amountSum.add(p.getAmount());
                                transactionDetailsRPPlanResp.setAmount(p.getAmount());
                                transactionDetailsRPPlanResp.setEpoch(String.valueOf(p.getEpoch()));
                                /**
                                 * The lock-up period corresponds to the number of settlement periods * epoch + number. If it is not an integer multiple, it will be: settlement period * (epoch-1) + excess number
                                 */
                                BigInteger number;
                                long remainder = transaction.getNum() % this.blockChainConfig.getSettlePeriodBlockCount()
                                                                                             .longValue();
                                if (remainder == 0l) {
                                    number = this.blockChainConfig.getSettlePeriodBlockCount()
                                                                  .multiply(p.getEpoch())
                                                                  .add(BigInteger.valueOf(transaction.getNum()));
                                } else {
                                    number = this.blockChainConfig.getSettlePeriodBlockCount()
                                                                  .multiply(p.getEpoch().subtract(BigInteger.ONE))
                                                                  .add(BigInteger.valueOf(transaction.getNum()))
                                                                  .add(this.blockChainConfig.getSettlePeriodBlockCount()
                                                                                            .subtract(BigInteger.valueOf(
                                                                                                    remainder)));
                                }
                                transactionDetailsRPPlanResp.setBlockNumber(String.valueOf(number));
                                rpPlanResps.add(transactionDetailsRPPlanResp);
                            }
                            // Accumulate
                            resp.setRPNum(amountSum);
                            resp.setRPPlan(rpPlanResps);
                        } catch (Exception e) {
                            logger.error("Create restricting transaction information parsing exception", e);
                        }
                        break;

                    case CLAIM_REWARDS:
                        try {
                            DelegateRewardClaimParam delegateRewardClaimParam = JSON.parseObject(txInfo,
                                                                                                 DelegateRewardClaimParam.class);
                            List<TransactionDetailsRewardsResp> rewards = new ArrayList<>();
                            BigDecimal rewardSum = BigDecimal.ZERO;
                            Map<String, Reward> rewrdsMap = new HashMap<String, Reward>();
                            /**
                             * Rewards for the same nodeId need to be accumulated, and different ones can be directly set and returned.
                             */
                            for (Reward reward : delegateRewardClaimParam.getRewardList()) {
                                if (rewrdsMap.containsKey(reward.getNodeId())) {
                                    Reward reward2 = rewrdsMap.get(reward.getNodeId());
                                    reward2.setReward(reward2.getReward().add(reward.getReward()));
                                } else {
                                    rewrdsMap.put(reward.getNodeId(), reward);
                                }
                            }
                            for (String nodeId : rewrdsMap.keySet()) {
                                Reward reward = rewrdsMap.get(nodeId);
                                TransactionDetailsRewardsResp transactionDetailsRewardsResp = new TransactionDetailsRewardsResp();
                                transactionDetailsRewardsResp.setVerify(reward.getNodeId());
                                transactionDetailsRewardsResp.setNodeName(reward.getNodeName());
                                transactionDetailsRewardsResp.setReward(reward.getReward());
                                rewardSum = rewardSum.add(reward.getReward());
                                rewards.add(transactionDetailsRewardsResp);
                            }
                            resp.setTxAmount(rewardSum);
                            resp.setRewards(rewards);
                        } catch (Exception e) {
                            logger.error("Abnormal analysis of transaction information for receiving rewards", e);
                        }
                        break;
                }
            }
            //Supplement and fill in relevant data of the contract
            switch (Transaction.TypeEnum.getEnum(transaction.getType())) {
                /**
                 * contract
                 */
                case EVM_CONTRACT_CREATE:
                case WASM_CONTRACT_CREATE:
                case ERC20_CONTRACT_CREATE:
                case ERC721_CONTRACT_CREATE:
                    /**
                     * The to address is set to the contract address
                     */
                    resp.setTo(transaction.getContractAddress());
                    resp.setTxInfo(transaction.getInput());
                    break;
                case CONTRACT_EXEC:
                    resp.setTexasHoldemParam(JSONUtil.toList(transaction.getTexasHoldemTxInfo(),
                                                             TexasHoldemParam.class));
                case ERC20_CONTRACT_EXEC:
                case ERC721_CONTRACT_EXEC:
                    //Get arc20 transaction for conversion
                    List<ErcTx> erc20List = JSONObject.parseArray(transaction.getErc20TxInfo(), ErcTx.class);
                    if (erc20List != null) {
                        List<Arc20Param> arc20Params = new ArrayList<>();
                        erc20List.forEach(erc20 -> {
                            // Precision conversion
                            int decimal = Integer.parseInt(String.valueOf(erc20.getDecimal()));
                            BigDecimal afterConverValue = ConvertUtil.convertByFactor(new BigDecimal(erc20.getValue()),
                                                                                      decimal);
                            Arc20Param arc20Param = Arc20Param.builder()
                                                              .innerContractAddr(erc20.getContract())
                                                              .innerContractName(erc20.getName())
                                                              .innerDecimal(String.valueOf(erc20.getDecimal()))
                                                              .innerFrom(erc20.getFrom())
                                                              .fromType(erc20.getFromType())
                                                              .innerSymbol(erc20.getSymbol())
                                                              .innerTo(erc20.getTo())
                                                              .toType(erc20.getToType())
                                                              .innerValue(afterConverValue.toString())
                                                              .build();
                            arc20Params.add(arc20Param);
                        });
                        resp.setErc20Params(arc20Params);
                    }
                    //Get arc721 transaction for conversion
                    List<ErcTx> erc721List = JSONObject.parseArray(transaction.getErc721TxInfo(), ErcTx.class);
                    if (erc721List != null) {
                        List<Arc721Param> arc721Params = new ArrayList<>();
                        erc721List.forEach(erc721 -> {
                            Arc721Param arc721Param = Arc721Param.builder()
                                                                 .innerContractAddr(erc721.getContract())
                                                                 .innerContractName(erc721.getName())
                                                                 .innerDecimal(String.valueOf(erc721.getDecimal()))
                                                                 .innerFrom(erc721.getFrom())
                                                                 .fromType(erc721.getFromType())
                                                                 .innerSymbol(erc721.getSymbol())
                                                                 .innerTo(erc721.getTo())
                                                                 .toType(erc721.getToType())
                                                                 .innerValue(erc721.getTokenId())
                                                                 .build();
                            //Query the corresponding image and backfill it
                            TokenInventoryExample example = new TokenInventoryExample();
                            example.createCriteria()
                                   .andTokenAddressEqualTo(erc721.getContract())
                                   .andTokenIdEqualTo(erc721.getValue());
                            List<TokenInventoryWithBLOBs> tokenInventoryWithBLOBs = tokenInventoryMapper.selectByExampleWithBLOBs(
                                    example);
                            if (CollUtil.isNotEmpty(tokenInventoryWithBLOBs)) {
                                TokenInventoryWithBLOBs tokenInventory = tokenInventoryWithBLOBs.get(0);
                                // Default to medium thumbnail
                                String image = "";
                                if (StrUtil.isNotEmpty(tokenInventory.getMediumImage())) {
                                    image = tokenInventory.getMediumImage();
                                } else {
                                    image = tokenInventory.getImage();
                                }
                                arc721Param.setInnerImage(image);
                            }
                            arc721Params.add(arc721Param);
                        });
                        resp.setErc721Params(arc721Params);
                    }
                    resp.setTxInfo(transaction.getInput());
                    break;
                case ERC1155_CONTRACT_EXEC:
                    List<ErcTx> erc1155List = JSONObject.parseArray(transaction.getErc1155TxInfo(), ErcTx.class);
                    List<Arc1155Param> list = new ArrayList<>();
                    if (CollUtil.isNotEmpty(erc1155List)) {
                        for (ErcTx ercTx : erc1155List) {
                            Arc1155Param arc1155Param = new Arc1155Param();
                            Token1155InventoryKey token1155InventoryKey = new Token1155InventoryKey();
                            token1155InventoryKey.setTokenAddress(ercTx.getContract());
                            token1155InventoryKey.setTokenId(ercTx.getTokenId());
                            Token1155InventoryWithBLOBs token1155Inventory = customToken1155InventoryMapper.findOneByUK(
                                    token1155InventoryKey);
                            arc1155Param.setContract(ercTx.getContract())
                                        .setTokenId(ercTx.getTokenId())
                                        .setName(token1155Inventory.getName())
                                        .setDecimal(token1155Inventory.getDecimal())
                                        .setImage(token1155Inventory.getImage())
                                        .setFrom(ercTx.getFrom())
                                        .setFromType(ercTx.getFromType())
                                        .setTo(ercTx.getTo())
                                        .setToType(ercTx.getToType())
                                        .setValue(ercTx.getValue());
                            list.add(arc1155Param);
                        }
                    }
                    resp.setErc1155Params(list);
                    break;
                default:
                    break;
            }
        }
        return resp;
    }

    /**
     * Unified conversion of proposal information
     *
     * @param resp
     * @param hash
     * @return
     * @method transferTransaction
     */
    private TransactionDetailsResp transferTransaction(TransactionDetailsResp resp, String hash) {
        Proposal proposal = this.proposalMapper.selectByPrimaryKey(hash);
        if (proposal != null) {
            resp.setNodeId(proposal.getNodeId());
            resp.setNodeName(proposal.getNodeName());
            resp.setPipNum(proposal.getPipNum());
            resp.setProposalTitle(Browser.INQUIRY.equals(proposal.getTopic()) ? "" : proposal.getTopic());
            resp.setProposalStatus(proposal.getStatus());
            resp.setProposalOption(String.valueOf(proposal.getType()));
            resp.setProposalNewVersion(proposal.getNewVersion());
            resp.setProposalUrl(proposal.getUrl());
        }
        return resp;
    }

    /**
     * Set the validator keybaseurl uniformly
     *
     * @param externalId
     * @param txReceiptStatus
     * @return
     * @method getStakingUrl
     */
    private String getStakingUrl(String externalId, Integer txReceiptStatus) {

        String keyBaseUrl = this.blockChainConfig.getKeyBase();
        String keyBaseApi = this.blockChainConfig.getKeyBaseApi();
        String defaultBaseUrl = this.blockChainConfig.getKeyBase();
        /**
         * If the externalId is empty, it will not be returned to the front-end URL and the jump will be reversed.
         */
        if (StringUtils.isNotBlank(externalId)) {
            /**
             * If you directly set the default URL for the failed transaction and then jump out
             */
            if (txReceiptStatus == Transaction.StatusEnum.FAILURE.getCode()) {
                return defaultBaseUrl;
            }
            /**
             * Check whether redis has been stored
             */
            String userName = redisTemplate.opsForValue().get(externalId);
            if (StringUtils.isNotBlank(userName)) {
                defaultBaseUrl += userName;
                return defaultBaseUrl;
            }
            String url = keyBaseUrl.concat(keyBaseApi.concat(externalId));
            try {
                KeyBaseUserInfo keyBaseUser = HttpUtil.get(url, KeyBaseUserInfo.class);
                userName = KeyBaseAnalysis.getKeyBaseUseName(keyBaseUser);
            } catch (Exception e) {
                this.logger.error("getStakingUrl error.externalId:{},txReceiptStatus:{},error:{}",
                                  externalId,
                                  txReceiptStatus,
                                  e.getMessage());
                return defaultBaseUrl;
            }
            if (StringUtils.isNotBlank(userName)) {
                /**
                 * Set up redis
                 */
                redisTemplate.opsForValue().set(externalId, userName);
                defaultBaseUrl += userName;
            }
            return defaultBaseUrl;
        }
        return null;
    }

    public RespPage<QueryClaimByAddressResp> queryClaimByAddress(TransactionListByAddressRequest req) {
        RespPage<QueryClaimByAddressResp> result = new RespPage<>();
        /** Query specific reward data based on address */
        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().term("addr", req.getAddress()));
        constructor.setDesc("time");
        ESResult<DelegationReward> delegationRewards = null;
        try {
            delegationRewards = this.ESDelegationRewardRepository.search(constructor,
                                                                         DelegationReward.class,
                                                                         req.getPageNo(),
                                                                         req.getPageSize());
        } catch (Exception e) {
            this.logger.error(ERROR_TIPS, e);
            return result;
        }
        if (delegationRewards == null) {
            // Prevent null pointer exception
            delegationRewards = new ESResult<>();
            delegationRewards.setTotal(0L);
            delegationRewards.setRsData(Collections.emptyList());
        }
        List<QueryClaimByAddressResp> queryClaimByAddressResps = new ArrayList<>();
        for (DelegationReward delegationReward : delegationRewards.getRsData()) {
            QueryClaimByAddressResp queryClaimByAddressResp = new QueryClaimByAddressResp();
            queryClaimByAddressResp.setTxHash(delegationReward.getHash());
            queryClaimByAddressResp.setTimestamp(delegationReward.getTime().getTime());
            List<TransactionDetailsRewardsResp> rewardsDetails = new ArrayList<>();
            /**
             * Parse json to obtain specific extraction reward data
             */
            List<Extra> extras = JSONObject.parseArray(delegationReward.getExtra(), DelegationReward.Extra.class);
            BigDecimal allRewards = BigDecimal.ZERO;
            /**
             * Set the node from which each reward will be obtained.
             */
            Map<String, Extra> rewrdsMap = new HashMap<String, Extra>();
            for (Extra extra : extras) {
                /**
                 * Rewards for the same nodeId need to be accumulated, and different ones can be directly set and returned.
                 */
                if (rewrdsMap.containsKey(extra.getNodeId())) {
                    Extra reward2 = rewrdsMap.get(extra.getNodeId());
                    reward2.setReward(new BigDecimal(reward2.getReward()).add(new BigDecimal(reward2.getReward()))
                                                                         .toString());
                } else {
                    rewrdsMap.put(extra.getNodeId(), extra);
                }
            }
            for (String nodeId : rewrdsMap.keySet()) {
                Extra extra = rewrdsMap.get(nodeId);
                TransactionDetailsRewardsResp transactionDetailsRewardsResp = new TransactionDetailsRewardsResp();
                transactionDetailsRewardsResp.setVerify(extra.getNodeId());
                transactionDetailsRewardsResp.setNodeName(extra.getNodeName());
                transactionDetailsRewardsResp.setReward(new BigDecimal(extra.getReward()));
                allRewards = allRewards.add(new BigDecimal(extra.getReward()));
                rewardsDetails.add(transactionDetailsRewardsResp);
            }
            queryClaimByAddressResp.setRewardsDetails(rewardsDetails);
            /**
             * All rewards are accumulated based on transactions
             */
            queryClaimByAddressResp.setAllRewards(allRewards);
            queryClaimByAddressResps.add(queryClaimByAddressResp);
        }

        result.init(queryClaimByAddressResps, delegationRewards.getTotal(), delegationRewards.getTotal(), 0L);
        return result;
    }

    public RespPage<QueryClaimByStakingResp> queryClaimByStaking(QueryClaimByStakingReq req) {
        /** Query specific reward data based on address */
        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().fuzzy("extraClean", req.getNodeId()));
        constructor.setDesc("time");
        ESResult<DelegationReward> delegationRewards = null;
        try {
            delegationRewards = this.ESDelegationRewardRepository.search(constructor,
                                                                         DelegationReward.class,
                                                                         req.getPageNo(),
                                                                         req.getPageSize());
        } catch (Exception e) {
            this.logger.error(ERROR_TIPS, e);
        }
        if (delegationRewards == null) {
            // Prevent null pointer exception
            delegationRewards = new ESResult<>();
            delegationRewards.setTotal(0L);
            delegationRewards.setRsData(Collections.emptyList());
        }
        List<QueryClaimByStakingResp> queryClaimByStakingResps = new ArrayList<>();
        for (DelegationReward delegationReward : delegationRewards.getRsData()) {
            QueryClaimByStakingResp queryClaimByStakingResp = new QueryClaimByStakingResp();
            BeanUtils.copyProperties(delegationReward, queryClaimByStakingResp);
            Address address = addressMapper.selectByPrimaryKey(queryClaimByStakingResp.getAddr());
            queryClaimByStakingResp.setAddrType(CommonUtil.ofNullable(() -> address.getType())
                                                          .orElse(AddressTypeEnum.ACCOUNT.getCode()));
            queryClaimByStakingResp.setTime(delegationReward.getTime().getTime());
            /**
             * Parse json to obtain specific extraction reward data
             */
            List<Extra> extras = JSONObject.parseArray(delegationReward.getExtra(), DelegationReward.Extra.class);
            BigDecimal allRewards = BigDecimal.ZERO;
            /**
             * All rewards received from accumulated transactions
             */
            for (Extra extra : extras) {
                /**
                 * Only those with the same address need to be accumulated
                 */
                if (req.getNodeId().equals(extra.getNodeId())) {
                    allRewards = allRewards.add(new BigDecimal(extra.getReward()));
                }
            }
            /**
             * All rewards are accumulated based on transactions
             */
            queryClaimByStakingResp.setReward(allRewards);
            queryClaimByStakingResps.add(queryClaimByStakingResp);
        }
        RespPage<QueryClaimByStakingResp> result = new RespPage<>();
        result.init(queryClaimByStakingResps, delegationRewards.getTotal(), delegationRewards.getTotal(), 0l);
        return result;
    }

}
