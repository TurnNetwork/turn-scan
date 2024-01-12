package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.turn.browser.constant.Browser;
import com.turn.browser.dao.custommapper.CustomNodeMapper;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.NavigateEnum;
import com.turn.browser.request.PageReq;
import com.turn.browser.request.newblock.BlockDetailNavigateReq;
import com.turn.browser.request.newblock.BlockDetailsReq;
import com.turn.browser.request.newblock.BlockDownload;
import com.turn.browser.request.newblock.BlockListByNodeIdReq;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.block.BlockDetailResp;
import com.turn.browser.response.block.BlockListResp;
import com.turn.browser.service.elasticsearch.EsBlockRepository;
import com.turn.browser.service.elasticsearch.bean.ESResult;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilderConstructor;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilders;
import com.turn.browser.utils.*;
import com.bubble.utils.Convert;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Specific implementation of block method logic
 */
@Service
public class BlockService {

    private final Logger logger = LoggerFactory.getLogger(BlockService.class);

    @Resource
    private StatisticCacheService statisticCacheService;

    @Resource
    private EsBlockRepository esBlockRepository;

    @Resource
    private I18nUtil i18n;

    @Resource
    private CommonService commonService;

    @Resource
    private CustomNodeMapper customNodeMapper;

    @Value("${turn.valueUnit}")
    private String valueUnit;

    private static final String ERROR_TIPS = "Get block error。";

    public RespPage<BlockListResp> blockList(PageReq req) {
        long startTime = System.currentTimeMillis();
        RespPage<BlockListResp> respPage = new RespPage<>();
        List<BlockListResp> lists = new ArrayList<>();
        /** Query the current maximum number of blocks */
        NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
        Long bNumber = networkStatRedis.getCurNumber();
        /** Query redis for less than 500,000 items */
        if (req.getPageNo() * req.getPageSize() <= Browser.MAX_NUM) {
            /**
             * When the page number is equal to 1, re-obtain the data and keep it consistent with the home page.
             */
            List<Block> items;
            if (req.getPageNo() == 1) {
                /** Query the latest eight block information cached */
                items = statisticCacheService.getBlockCache(0, 1);
                if (!items.isEmpty()) {
                    /**
                     * If the statistical block is smaller than the block transaction, re-query the new block.
                     */
                    Long dValue = items.get(0).getNum() - bNumber;
                    if (dValue > 0) {
                        items = statisticCacheService.getBlockCache(dValue.intValue() / req.getPageSize() + 1, req.getPageSize());
                    } else {
                        items = statisticCacheService.getBlockCache(req.getPageNo(), req.getPageSize());
                    }
                }
            } else {
                items = statisticCacheService.getBlockCache(req.getPageNo(), req.getPageSize());
            }
            lists.addAll(this.transferBlockListResp(items));
        }
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        long totalCount = networkStatRedis.getCurNumber() == null ? 0 : networkStatRedis.getCurNumber();
        if (totalCount > Browser.MAX_NUM) {
            page.setTotal(Browser.MAX_NUM);
        } else {
            page.setTotal(networkStatRedis.getCurNumber() == null ? 0 : networkStatRedis.getCurNumber());
        }
        long displayTotalCount = totalCount > Browser.MAX_NUM ? Browser.MAX_NUM : totalCount;
        respPage.init(lists, totalCount, displayTotalCount, page.getPages());
        if (System.currentTimeMillis() - startTime > 100) {
            logger.error("perform-blockList,time:{}", System.currentTimeMillis() - startTime);
        }
        return respPage;
    }

    /**
     * Unified conversion of block lists
     *
     * @return
     * @method transferBlockListResp
     */
    private List<BlockListResp> transferBlockListResp(List<Block> blocks) {
        List<BlockListResp> blockListResps = new ArrayList<>();
        Set<String> nodeIdList = blocks.stream().map(Block::getNodeId).collect(Collectors.toSet());
        List<Node> nodeNames = new ArrayList<>();
        if (CollUtil.isNotEmpty(nodeIdList)) {
            nodeNames = customNodeMapper.batchFindNodeNameByNodeId(nodeIdList);
        }
        for (Block block : blocks) {
            BlockListResp blockListResp = new BlockListResp();
            BeanUtils.copyProperties(block, blockListResp);
            blockListResp.setBlockReward(new BigDecimal(block.getReward()));
            blockListResp.setNumber(block.getNum());
            blockListResp.setStatTxGasLimit(block.getTxGasLimit());
            blockListResp.setStatTxQty(block.getTxQty());
            blockListResp.setServerTime(System.currentTimeMillis());
            blockListResp.setTimestamp(block.getTime().getTime());
            for (Node node : nodeNames) {
                if (node.getNodeId().equalsIgnoreCase(block.getNodeId())) {
                    blockListResp.setNodeName(node.getNodeName());
                }
            }
            if (StrUtil.isEmpty(blockListResp.getNodeName())) {
                logger.error("The nodeId:{} did not query the nodeName", block.getNodeId());
            }
            blockListResps.add(blockListResp);
        }
        nodeNames.clear();
        return blockListResps;
    }

    public RespPage<BlockListResp> blockListByNodeId(BlockListByNodeIdReq req) {
        RespPage<BlockListResp> respPage = new RespPage<>();
        /** Query the block list based on nodeId, in descending order of block number  */
        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().term("nodeId", req.getNodeId()));
        constructor.setDesc("num");
        constructor.setResult(new String[]{"num", "time", "txQty", "reward"});
        ESResult<Block> blocks = new ESResult<>();
        try {
            blocks = esBlockRepository.search(constructor, Block.class, req.getPageNo(), req.getPageSize());
        } catch (Exception e) {
            logger.error(ERROR_TIPS, e);
            return respPage;
        }
        /** Initialize return object */
        List<BlockListResp> lists = new ArrayList<>();
        for (Block block : blocks.getRsData()) {
            BlockListResp blockListResp = new BlockListResp();
            blockListResp.setBlockReward(new BigDecimal(block.getReward()));
            blockListResp.setNumber(block.getNum());
            blockListResp.setStatTxQty(block.getTxQty());
            blockListResp.setServerTime(System.currentTimeMillis());
            blockListResp.setTimestamp(block.getTime().getTime());
            lists.add(blockListResp);
        }
        /** Set the returned paging data */
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        if (blocks.getTotal() > 5000) {
            page.setTotal(5000);
        } else {
            page.setTotal(blocks.getTotal());
        }
        respPage.init(page, lists);
        return respPage;
    }

    public BlockDownload blockListByNodeIdDownload(String nodeId, Long date, String local, String timeZone) {
        /** Set download return object */
        BlockDownload blockDownload = new BlockDownload();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.error("now time:{}", format.format(now));
        String msg = dateFormat.format(now);
        String msg2 = dateFormat.format(new Date(date));
        logger.info("Export data start date: {}, end time:{}", msg2, msg);
        /** Limit export to 30,000 records */
        /** Set up query data based on time and nodeId */

        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().term("nodeId", nodeId));
        constructor.must(new ESQueryBuilders().range("time", new Date(date).getTime(), now.getTime()));
        constructor.setDesc("num");
        constructor.setResult(new String[]{"num", "time", "txQty", "reward", "txFee"});
        ESResult<Block> blockList = new ESResult<>();
        try {
            blockList = esBlockRepository.search(constructor, Block.class, 1, 30000);
        } catch (Exception e) {
            logger.error(ERROR_TIPS, e);
            return blockDownload;
        }
        /** Convert query data into corresponding list */
        List<Object[]> rows = new ArrayList<>();
        blockList.getRsData().forEach(block -> {
            Object[] row = {
                    block.getNum(),
                    DateUtil.timeZoneTransfer(block.getTime(), "0", timeZone),
                    block.getTxQty(),
                    HexUtil.append(EnergonUtil.format(Convert.fromVon(block.getReward(), Convert.Unit.KPVON).setScale(18, RoundingMode.DOWN))),
                    HexUtil.append(EnergonUtil.format(Convert.fromVon(block.getTxFee(), Convert.Unit.KPVON).setScale(18, RoundingMode.DOWN)))
            };
            rows.add(row);
        });

        /** Initialize the output stream object */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            /** Set the returned header to prevent csv garbled characters */
            baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        } catch (IOException e) {
            logger.error("Data output error:", e);
        }
        Writer outputWriter = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
        /** Initialize writer object */
        CsvWriter writer = new CsvWriter(outputWriter, new CsvWriterSettings());
        writer.writeHeaders(
                i18n.i(I18nEnum.DOWNLOAD_BLOCK_CSV_NUMBER, local),
                i18n.i(I18nEnum.DOWNLOAD_BLOCK_CSV_TIMESTAMP, local),
                i18n.i(I18nEnum.DOWNLOAD_BLOCK_CSV_TRANSACTION_COUNT, local),
                i18n.i(I18nEnum.DOWNLOAD_BLOCK_CSV_REWARD, local) + "(" + valueUnit + ")",
                i18n.i(I18nEnum.DOWNLOAD_BLOCK_CSV_TXN_FEE, local) + "(" + valueUnit + ")"
        );
        writer.writeRowsAndClose(rows);

        blockDownload.setData(baos.toByteArray());
        blockDownload.setFilename("block-" + nodeId + "-" + date + ".csv");
        blockDownload.setLength(baos.size());
        return blockDownload;
    }

    public BlockDetailResp blockDetails(BlockDetailsReq req) {
        return this.queryBlockByNumber(req.getNumber().longValue());
    }

    public BlockDetailResp blockDetailNavigate(BlockDetailNavigateReq req) {
        long blockNumber = req.getNumber();
        /** Distinguish whether to query the previous block or the next block */
        NavigateEnum navigateEnum = NavigateEnum.valueOf(req.getDirection().toUpperCase());
        if (navigateEnum == NavigateEnum.PREV) {
            blockNumber -= 1;
        } else if (navigateEnum == NavigateEnum.NEXT) {
            blockNumber += 1;
        }
        return this.queryBlockByNumber(blockNumber);
    }

    private BlockDetailResp queryBlockByNumber(long blockNumber) {
        /** Query corresponding data based on block number */

        Block block = null;
        try {
            block = esBlockRepository.get(String.valueOf(blockNumber), Block.class);
        } catch (IOException e) {
            logger.error(ERROR_TIPS, e);
        }
        BlockDetailResp blockDetailResp = new BlockDetailResp();
        if (block != null) {
            BeanUtils.copyProperties(block, blockDetailResp);
            blockDetailResp.setBlockReward(new BigDecimal(block.getReward()));
            blockDetailResp.setDelegateQty(block.getDQty());
            blockDetailResp.setExtraData(block.getExtra());
            blockDetailResp.setNumber(block.getNum());
            blockDetailResp.setParentHash(block.getPHash());
            blockDetailResp.setProposalQty(block.getPQty());
            blockDetailResp.setStakingQty(block.getSQty());
            blockDetailResp.setStatTxGasLimit(block.getTxGasLimit());
            blockDetailResp.setTimestamp(block.getTime().getTime());
            blockDetailResp.setServerTime(System.currentTimeMillis());
            blockDetailResp.setTransferQty(block.getTranQty());
            blockDetailResp.setNodeName(commonService.getNodeName(block.getNodeId(), null));

            /** Get the previous block and set the identifier and hash if it exists */
            blockDetailResp.setFirst(false);
            if (blockNumber == 0) {
                blockDetailResp.setTimeDiff(0L);
                blockDetailResp.setFirst(true);
            }

            /** Set the last identifier **/
            /** Query the current maximum number of blocks */
            blockDetailResp.setLast(false);
            NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
            Long bNumber = networkStatRedis.getCurNumber();
            if (blockNumber >= bNumber) {
                /** If there is no next block in the current block, it means that this is the last block and the last flag is set.   */
                blockDetailResp.setLast(true);
            }

            blockDetailResp.setTimestamp(block.getTime().getTime());

            // Only block 0 has postscript
            if (0 == blockNumber) {
                blockDetailResp.setPostscript(BlockUtil.getPostscriptFromExtraData(blockDetailResp.getExtraData()));
            } else {
                blockDetailResp.setPostscript("");
            }
        }
        return blockDetailResp;
    }

}
