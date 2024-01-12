package com.turn.browser.service;

import com.turn.browser.utils.NetworkParams;
import com.bubble.utils.Convert;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.constant.Browser;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.entity.ProposalExample;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.service.elasticsearch.EsBlockRepository;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.ErrorCodeEnum;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.PageReq;
import com.turn.browser.request.proposal.ProposalDetailRequest;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.proposal.ProposalDetailsResp;
import com.turn.browser.response.proposal.ProposalListResp;
import com.turn.browser.utils.ConvertUtil;
import com.turn.browser.utils.I18nUtil;
import com.turn.browser.utils.ChainVersionUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class ProposalInfoService {

    Logger logger = LoggerFactory.getLogger(ProposalInfoService.class);

    @Resource
    private I18nUtil i18n;

    @Resource
    private ProposalMapper proposalMapper;

    @Resource
    private StatisticCacheService statisticCacheService;

    @Resource
    private BlockChainConfig blockChainConfig;

    @Resource
    private EsBlockRepository ESBlockRepository;

    @Resource
    private NetworkParams networkParams;

    public RespPage<ProposalListResp> list(PageReq req) {
        RespPage<ProposalListResp> respPage = new RespPage<>();
        req = req == null ? new PageReq() : req;
        Page<?> page = PageHelper.startPage(req.getPageNo(), req.getPageSize(), true);
        /** Data for which the total number of people is 0 is not displayed for the time being, otherwise the page will display the voting percentage incorrectly.   */
        ProposalExample proposalExample = new ProposalExample();
        proposalExample.setOrderByClause(" timestamp desc");
        ProposalExample.Criteria criteria = proposalExample.createCriteria();
        criteria.andAccuVerifiersNotEqualTo(0l);
        List<Proposal> list = proposalMapper.selectByExample(proposalExample);
        /** Query proposal data by page */
        if (!CollectionUtils.isEmpty(list)) {
            List<ProposalListResp> listResps = new ArrayList<>(list.size());
            for (Proposal proposal : list) {
                /**
                 * Loop through data
                 */
                ProposalListResp proposalListResp = new ProposalListResp();
                BeanUtils.copyProperties(proposal, proposalListResp);
                proposalListResp.setTopic(Browser.INQUIRY.equals(proposal.getTopic()) ? "" : proposal.getTopic());
                proposalListResp.setProposalHash(proposal.getHash());
                proposalListResp.setEndVotingBlock(String.valueOf(proposal.getEndVotingBlock()));
                proposalListResp.setType(String.valueOf(proposal.getType()));
                proposalListResp.setStatus(String.valueOf(proposal.getStatus()));
                proposalListResp.setTimestamp(proposal.getTimestamp().getTime());
                proposalListResp.setInBlock(proposal.getBlockNumber());
                /** Get the statistics of the latest block height */
                NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
                if (networkStatRedis != null) {
                    proposalListResp.setCurBlock(String.valueOf(networkStatRedis.getCurNumber()));
                }
                listResps.add(proposalListResp);
            }
            respPage.init(listResps, page.getTotal(), page.getTotal(), page.getPages());
        }
        return respPage;
    }

    public BaseResp<ProposalDetailsResp> get(ProposalDetailRequest req) {
        /** Query proposals based on hash */
        Proposal proposal = proposalMapper.selectByPrimaryKey(req.getProposalHash());
        if (Objects.isNull(proposal)) {
            logger.error("## ERROR # get record not exist proposalHash:{}", req.getProposalHash());
            return BaseResp.build(ErrorCodeEnum.RECORD_NOT_EXIST.getCode(), i18n.i(I18nEnum.RECORD_NOT_EXIST, req.getProposalHash()), null);
        }
        ProposalDetailsResp proposalDetailsResp = new ProposalDetailsResp();
        BeanUtils.copyProperties(proposal, proposalDetailsResp);
        proposalDetailsResp.setTopic(Browser.INQUIRY.equals(proposal.getTopic()) ? "" : proposal.getTopic());
        proposalDetailsResp.setProposalHash(req.getProposalHash());
        proposalDetailsResp.setNodeId(proposal.getNodeId());
        proposalDetailsResp.setNodeName(proposal.getNodeName());
        proposalDetailsResp.setDescription(Browser.INQUIRY.equals(proposal.getDescription()) ? "" : proposal.getDescription());
        proposalDetailsResp.setCanceledTopic(Browser.INQUIRY.equals(proposal.getCanceledTopic()) ? "" : proposal.getCanceledTopic());
        proposalDetailsResp.setEndVotingBlock(String.valueOf(proposal.getEndVotingBlock()));
        proposalDetailsResp.setAccuVerifiers(String.valueOf(proposal.getAccuVerifiers()));
        proposalDetailsResp.setAbstentions(proposal.getAbstentions().intValue());
        proposalDetailsResp.setActiveBlock(String.valueOf(proposal.getActiveBlock()));
        proposalDetailsResp.setNays(proposal.getNays().intValue());
        proposalDetailsResp.setTimestamp(proposal.getTimestamp().getTime());
        proposalDetailsResp.setYeas(proposal.getYeas().intValue());
        NetworkStat networkStat = statisticCacheService.getNetworkStatCache();
        proposalDetailsResp.setCurBlock(String.valueOf(networkStat.getCurNumber()));
        /** Different types have different pass rates */
        switch (CustomProposal.TypeEnum.getEnum(proposal.getType())) {
            /**
             * text proposal
             */
            case TEXT:
                proposalDetailsResp.setSupportRateThreshold(blockChainConfig.getMinProposalTextSupportRate().toString());
                proposalDetailsResp.setParticipationRate(blockChainConfig.getMinProposalTextParticipationRate().toString());
                break;
            /**
             * Upgrade proposal
             */
            case UPGRADE:
                proposalDetailsResp.setSupportRateThreshold(blockChainConfig.getMinProposalUpgradePassRate().toString());
                break;
            /**
             * Parameter proposal
             */
            case PARAMETER:
                proposalDetailsResp.setParamName(ConvertUtil.captureName(proposal.getName()));
                String currentValue = proposal.getStaleValue();
                String newValue = proposal.getNewValue();
                /**
                 * If the parameter needs to be converted to turn, then further conversion
                 */
                if (Browser.EXTRA_TURN_PARAM.contains(proposal.getName())) {
                    currentValue = Convert.fromVon(currentValue, Convert.Unit.KPVON).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + networkParams.getUnit();
                    newValue = Convert.fromVon(newValue, Convert.Unit.KPVON).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + networkParams.getUnit();
                }
                /**
                 * Remove useless 0
                 */
                if ("slashFractionDuplicateSign".contains(proposal.getName())) {
                    currentValue = new BigDecimal(currentValue).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                    newValue = new BigDecimal(newValue).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                }
                proposalDetailsResp.setCurrentValue(currentValue);
                proposalDetailsResp.setNewValue(newValue);
                proposalDetailsResp.setSupportRateThreshold(blockChainConfig.getParamProposalSupportRate().toString());
                proposalDetailsResp.setParticipationRate(blockChainConfig.getParamProposalVoteRate().toString());
                break;
            /**
             * Cancel proposal
             */
            case CANCEL:
                /**
                 * If the canceled proposal title does not have the transaction title returned by querying the corresponding proposal confirmation
                 */
                if (StringUtils.isBlank(proposalDetailsResp.getCanceledTopic())) {
                    Proposal cancelProposal = proposalMapper.selectByPrimaryKey(proposal.getCanceledPipId());
                    if (cancelProposal != null && CustomProposal.TypeEnum.getEnum(cancelProposal.getType()) == CustomProposal.TypeEnum.UPGRADE) {
                        proposalDetailsResp.setCanceledTopic("version upgrade-V" + ChainVersionUtil.toStringVersion(new BigInteger(cancelProposal.getNewVersion())));
                    }
                }
                proposalDetailsResp.setSupportRateThreshold(blockChainConfig.getMinProposalCancelSupportRate().toString());
                proposalDetailsResp.setParticipationRate(blockChainConfig.getMinProposalCancelParticipationRate().toString());
                break;
            default:
                break;
        }
        /**
         * If the difference between the end block and the current block is greater than 0, the calculation will be based on the block time. Otherwise, the time of the ended block will be obtained directly.
         */
        BigDecimal diff = new BigDecimal(proposalDetailsResp.getEndVotingBlock()).subtract(new BigDecimal(proposalDetailsResp.getCurBlock()));
        Block block;
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            /** Estimated end time: (effective block - current block) * block interval + current block time */
            try {
                block = ESBlockRepository.get(proposalDetailsResp.getCurBlock(), Block.class);
                BigDecimal endTime = diff.multiply(new BigDecimal(networkStat.getAvgPackTime())).add(new BigDecimal(block.getTime().getTime()));
                proposalDetailsResp.setEndVotingBlockTime(endTime.longValue());
            } catch (IOException e) {
                logger.error("Getting block error.", e);
            }
        } else {
            try {
                block = ESBlockRepository.get(proposalDetailsResp.getEndVotingBlock(), Block.class);
                proposalDetailsResp.setEndVotingBlockTime(block.getTime().getTime());
            } catch (IOException e) {
                logger.error("Getting block error.", e);
            }
        }

        proposalDetailsResp.setInBlock(proposal.getBlockNumber());
        return BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), proposalDetailsResp);
    }

}
