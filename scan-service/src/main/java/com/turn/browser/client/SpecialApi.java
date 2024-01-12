package com.turn.browser.client;

import com.alibaba.fastjson.JSON;
import com.bubble.abi.solidity.datatypes.BytesType;
import com.bubble.abi.solidity.datatypes.Utf8String;
import com.bubble.abi.solidity.datatypes.generated.Uint256;
import com.turn.browser.bean.*;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.ContractInvokeException;
import com.turn.browser.utils.HexUtil;
import com.turn.browser.v0150.bean.AdjustParam;
import com.bubble.contracts.dpos.BaseContract;
import com.bubble.contracts.dpos.abi.Function;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.common.ErrorCode;
import com.bubble.contracts.dpos.dto.resp.Node;
import com.bubble.contracts.dpos.utils.EncoderUtils;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.core.RemoteCall;
import com.bubble.protocol.core.Request;
import com.bubble.protocol.core.methods.request.Transaction;
import com.bubble.protocol.core.methods.response.BubbleCall;
import com.bubble.tx.exceptions.ContractCallException;
import com.bubble.utils.JSONUtil;
import com.bubble.utils.Numeric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Description:
 */
@Slf4j
@Component
public class SpecialApi {


    /**
     * Query the historical validator queue of settlement cycle
     */
    public static final int GET_HISTORY_VERIFIER_LIST_FUNC_TYPE = 1300;

    /**
     * Query the validator list of the historical consensus cycle
     */
    public static final int GET_HISTORY_VALIDATOR_LIST_FUNC_TYPE = 1301;

    /**
     *Historically low block production penalty information
     */
    public static final int GET_HISTORY_LOW_RATE_SLASH_LIST_FUNC_TYPE = 1304;

    /**
     * Query version list
     */
    public static final int GET_NODE_VERSION = 1302;

    /**
     * Query version list
     */
    public static final int GET_HISTORY_REWARD = 1303;

    /**
     * Get available and locked balances
     */
    public static final int GET_RESTRICTING_BALANCE_FUNC_TYPE = 4200;

    /**
     * Get proposal results
     */
    public static final int GET_PROPOSAL_RES_FUNC_TYPE = 2105;

    /**
     * Query contract call PPOS information
     */
    public static final int GET_PPOS_INFO_FUNC_TYPE = 1305;

    /**
     * Query pledge entrustment adjustment information
     */
    public static final int GET_STAKING_DELEGATE_ADJUST_DATA_FUNC_TYPE = 1112;

    private static final String BLANK_RES = "The result is empty!";

    /**
     * rpc calling interface
     *
     * @param web3j
     * @param function
     * @param from
     * @param to
     * @return
     * @throws Exception
     */
    private CallResponse<String> rpc(Web3j web3j, Function function, String from, String to) throws
                                                                                             ContractInvokeException {
        CallResponse<String> br;
        try {
            br = new RemoteCall<>(() -> {
                BubbleCall ethCall = web3j.bubbleCall(Transaction.createEthCallTransaction(from,
                                                                                           to,
                                                                                           EncoderUtils.functionEncoder(
                                                                                                   function)),
                                                      DefaultBlockParameterName.LATEST).send();
                if (ethCall.hasError()) {
                    throw new ContractInvokeException(ethCall.getError().getMessage());
                }
                String value = ethCall.getValue();
                if ("0x".equals(value)) {
                    // Prove there is no data and return an empty response
                    CallResponse<String> data = new CallResponse<>();
                    data.setData(null);
                    data.setErrMsg(null);
                    data.setCode(ErrorCode.SUCCESS);
                    return data;
                }
                String decodedValue = new String(Numeric.hexStringToByteArray(value));
                BaseContract.CallRet callRet = JSONUtil.parseObject(decodedValue, BaseContract.CallRet.class);
                if (callRet == null) {
                    throw new ContractCallException("Unable to convert response: " + decodedValue);
                }
                CallResponse<String> callResponse = new CallResponse<>();
                if (callRet.isStatusOk()) {
                    callResponse.setCode(callRet.getCode());
                    callResponse.setData(JSONUtil.toJSONString(callRet.getRet()));
                } else {
                    callResponse.setCode(callRet.getCode());
                    callResponse.setErrMsg(callRet.getRet().toString());
                }
                return callResponse;
            }).send();
        } catch (Exception e) {
            log.error("get rpc error", e);
            throw new ContractInvokeException(e.getMessage());
        }
        return br;
    }

    /**
     * Get the list of validators for the settlement cycle based on the block number
     *
     * @param blockNumber
     * @return
     * @throwsException
     */
    public List<Node> getHistoryVerifierList(Web3j web3j, BigInteger blockNumber) throws
            ContractInvokeException,
            BlankResponseException {
        return nodeCall(web3j, blockNumber, GET_HISTORY_VERIFIER_LIST_FUNC_TYPE);
    }

    /**
     * Get the consensus cycle validator list based on the block number
     *
     * @param blockNumber
     * @return
     * @throws Exception
     */
    public List<Node> getHistoryValidatorList(Web3j web3j, BigInteger blockNumber) throws
                                                                                   ContractInvokeException,
                                                                                   BlankResponseException {
        return nodeCall(web3j, blockNumber, GET_HISTORY_VALIDATOR_LIST_FUNC_TYPE);
    }

    /**
     * Get the historical low block penalty information list based on the block number
     *
     * @param blockNumber
     * @return
     * @throwsException
     */
    public List<HistoryLowRateSlash> getHistoryLowRateSlashList(Web3j web3j, BigInteger blockNumber) throws
            ContractInvokeException,
            BlankResponseException {
        final Function function = new Function(GET_HISTORY_LOW_RATE_SLASH_LIST_FUNC_TYPE,
                Collections.singletonList(new Uint256(blockNumber)));
        CallResponse<String> br = rpc(web3j,
                function,
                InnerContractAddrEnum.NODE_CONTRACT.getAddress(),
                InnerContractAddrEnum.NODE_CONTRACT.getAddress());
        if (br == null) {
            throw new BlankResponseException(String.format("[Error in querying historical low block penalty information] Function type: %s, block number: %s, return is empty!%s",
                    String.valueOf(GET_HISTORY_LOW_RATE_SLASH_LIST_FUNC_TYPE),
                    blockNumber,
                    JSON.toJSONString(Thread.currentThread().getStackTrace())));
        }
        if (br.getData() == null) {
            //No data found, returns empty list
            return Collections.emptyList();
        }
        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            return JSON.parseArray(data, HistoryLowRateSlash.class);
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error querying historical low block penalty information] Function type: %s, block number: %s, return data: %s",
                    GET_HISTORY_LOW_RATE_SLASH_LIST_FUNC_TYPE,
                    blockNumber.toString(),
                    msg));
        }
    }

    /**
     * Get the node list based on the block number
     *
     * @return
     * @throws Exception
     */
    private List<Node> nodeCall(Web3j web3j, BigInteger blockNumber, int funcType) throws
                                                                                   ContractInvokeException,
                                                                                   BlankResponseException {

        final Function function = new Function(funcType, Collections.singletonList(new Uint256(blockNumber)));

        CallResponse<String> br = rpc(web3j,
                                      function,
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress(),
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress());
        if (br == null || br.getData() == null) {
            throw new BlankResponseException(String.format("[Error querying validator] Function type: %s, block number: %s, return is empty!%s",
                    String.valueOf(funcType),
                    blockNumber,
                    JSON.toJSONString(Thread.currentThread().getStackTrace())));
        }
        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            return JSON.parseArray(data, Node.class);
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error querying validator] Function type: %s, block number: %s, return data: %s",
                    funcType,
                    blockNumber.toString(),
                    msg));
        }
    }

    /**
     * Get the node list based on the block number
     *
     * @return
     * @throws Exception
     */
    public List<NodeVersion> getNodeVersionList(Web3j web3j) throws ContractInvokeException, BlankResponseException {
        final Function function = new Function(GET_NODE_VERSION, Collections.emptyList());
        CallResponse<String> br = rpc(web3j,
                                      function,
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress(),
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress());
        if (br == null || br.getData() == null) {
            throw new BlankResponseException(String.format("[Error querying node version] Function type: %s, return is empty!%s",
                    GET_NODE_VERSION,
                    JSON.toJSONString(Thread.currentThread().getStackTrace())));
        }
        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            return JSON.parseArray(data, NodeVersion.class);
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error querying node version] Function type: %s, return data: %s",
                    GET_NODE_VERSION,
                    msg));
        }
    }

    /**
     * Get the locked balance according to the account address
     *
     * @param addresses
     * @return
     * @throws Exception
     */
    public List<RestrictingBalance> getRestrictingBalance(Web3j web3j, String addresses) throws
                                                                                         ContractInvokeException,
                                                                                         BlankResponseException {
        final Function function = new Function(GET_RESTRICTING_BALANCE_FUNC_TYPE,
                                               Collections.singletonList(new Utf8String(addresses)));
        CallResponse<String> br = rpc(web3j,
                                      function,
                                      InnerContractAddrEnum.RESTRICTING_PLAN_CONTRACT.getAddress(),
                                      InnerContractAddrEnum.RESTRICTING_PLAN_CONTRACT.getAddress());
        if (br == null || br.getData() == null) {
            throw new BlankResponseException(String.format("Error in querying lock balance [addresses:%s)], the return is empty!", addresses));
        }
        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            return JSON.parseArray(data, RestrictingBalance.class);
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error querying lock balance] Address: %s, return data: %s", addresses, msg));
        }
    }

    /**
     * Obtain historical cycle information based on block number
     *
     * @param blockNumber
     * @return
     * @throws Exception
     */
    public EpochInfo getEpochInfo(Web3j web3j, BigInteger blockNumber) throws
                                                                       ContractInvokeException,
                                                                       BlankResponseException {
        final Function function = new Function(GET_HISTORY_REWARD, Collections.singletonList(new Uint256(blockNumber)));
        CallResponse<String> br = rpc(web3j,
                                      function,
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress(),
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress());
        if (br == null || br.getData() == null) {
            throw new BlankResponseException(String.format("Error in querying historical period information [blockNumber: %s)], the return is empty!",
                    blockNumber));
        }

        log.info("Total circulation special node req = {} resp = {}", blockNumber, cn.hutool.json.JSONUtil.toJsonStr(br));

        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            EpochInfo ei = JSON.parseObject(data, EpochInfo.class);
            if (ei.getYearEndNum().compareTo(ei.getYearStartNum()) < 0) {
                String msg = "Error in querying historical period information [blockNumber:" + blockNumber + ")]: End block number of the additional issuance period [" + ei.getYearEndNum() + "]<Start block number [" + ei.getYearStartNum() + "】";
                log.error(msg);
                throw new ContractInvokeException(msg);
            }
            return ei;
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error querying historical period information] Block number: %s, return data: %s",
                    blockNumber,
                    msg));
        }
    }

    /**
     * Get all participants of a proposal
     *
     * @param web3j
     * @param proposalHash
     * @param blockHash
     * @return
     * @throws Exception
     */
    public ProposalParticipantStat getProposalParticipants(Web3j web3j, String proposalHash, String blockHash) throws
                                                                                                               ContractInvokeException,
                                                                                                               BlankResponseException {

        final Function function = new Function(GET_PROPOSAL_RES_FUNC_TYPE,
                                               Arrays.asList(new BytesType(Numeric.hexStringToByteArray(proposalHash)),
                                                             new BytesType(Numeric.hexStringToByteArray(blockHash))));
        CallResponse<String> br = rpc(web3j,
                                      function,
                                      InnerContractAddrEnum.PROPOSAL_CONTRACT.getAddress(),
                                      InnerContractAddrEnum.PROPOSAL_CONTRACT.getAddress());
        if (br == null || br.getData() == null) {
            throw new BlankResponseException(String.format("Error in querying proposal participants [Proposal Hash: %s, Block Hash: %s]",
                    proposalHash,
                    blockHash));
        }
        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            String[] a = data.replace("[", "").replace("]", "").split(",");
            String voterCount = "0";
            String supportCount = "0";
            String opposeCount = "0";
            String abstainCount = "0";
            if (a.length >= 4) {
                voterCount = a[0].trim();
                supportCount = a[1].trim();
                opposeCount = a[2].trim();
                abstainCount = a[3].trim();
            }
            ProposalParticipantStat pps = new ProposalParticipantStat();
            pps.setVoterCount(Long.parseLong(voterCount));
            pps.setSupportCount(Long.parseLong(supportCount));
            pps.setOpposeCount(Long.parseLong(opposeCount));
            pps.setAbstainCount(Long.parseLong(abstainCount));
            return pps;
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error in querying proposal participants] Proposal Hash: %s, Block Hash: %s, Return data: %s",
                    proposalHash,
                    blockHash,
                    msg));
        }
    }

    public ReceiptResult getReceiptResult(Web3jWrapper web3jWrapper, BigInteger blockNumber) throws IOException {
        Request<?, ReceiptResult> request = new Request<>("bub_getTransactionByBlock",
                Arrays.asList(blockNumber),
                web3jWrapper.getWeb3jService(),
                ReceiptResult.class);
        return request.send();
    }

    /**
     * Obtain contract call PPOS information based on block number
     *
     * @param blockNumber
     * @return
     * @throws Exception
     */
    public List<PPosInvokeContractInput> getPPosInvokeInfo(Web3j web3j, BigInteger blockNumber) throws
                                                                                                ContractInvokeException,
                                                                                                BlankResponseException {
        final Function function = new Function(GET_PPOS_INFO_FUNC_TYPE,
                                               Collections.singletonList(new Uint256(blockNumber)));
        CallResponse<String> br = rpc(web3j,
                                      function,
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress(),
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress());
        if (br == null || br.getData() == null) {
            return Collections.EMPTY_LIST;
        }
        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            return JSON.parseArray(data, PPosInvokeContractInput.class);
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error querying PPOS call information] Function type: %s, block number: %s, return data: %s",
                    GET_PPOS_INFO_FUNC_TYPE,
                    blockNumber.toString(),
                    msg));
        }
    }

    /**
     * Obtain pledge entrustment adjustment information based on block number
     *
     * @param blockNumber
     * @return
     * @throws Exception
     */
    public List<AdjustParam> getStakingDelegateAdjustDataList(Web3j web3j, BigInteger blockNumber) throws
                                                                                                   ContractInvokeException,
                                                                                                   BlankResponseException {
        final Function function = new Function(GET_STAKING_DELEGATE_ADJUST_DATA_FUNC_TYPE,
                                               Collections.singletonList(new Uint256(blockNumber)));
        CallResponse<String> br = rpc(web3j,
                                      function,
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress(),
                                      InnerContractAddrEnum.NODE_CONTRACT.getAddress());
        if (br == null || br.getData() == null) {
            return Collections.EMPTY_LIST;
        }
        if (br.isStatusOk()) {
            String data = br.getData();
            if (data == null) {
                throw new BlankResponseException(BLANK_RES);
            }
            data = data.replace("delete", "delegate");
            List<AdjustParam> adjustParams = JSON.parseArray(data, AdjustParam.class);
            adjustParams.forEach(param -> {
                param.setNodeId(HexUtil.prefix(param.getNodeId()));
                param.setCurrBlockNum(blockNumber);
            });
            return adjustParams;
        } else {
            String msg = JSON.toJSONString(br);
            throw new ContractInvokeException(String.format("[Error in querying pledge commission adjustment information] Function type: %s, block number: %s, return data: %s",
                    GET_STAKING_DELEGATE_ADJUST_DATA_FUNC_TYPE,
                    blockNumber.toString(),
                    msg));
        }
    }

}
