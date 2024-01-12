package com.turn.browser.service.erc;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import com.turn.browser.client.TurnClient;
import com.turn.browser.enums.ErcTypeEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.v0152.bean.ErcContractId;
import com.turn.browser.v0152.contract.Erc1155Contract;
import com.turn.browser.v0152.contract.Erc20Contract;
import com.turn.browser.v0152.contract.Erc721Contract;
import com.turn.browser.v0152.contract.ErcContract;
import com.turn.browser.v0152.service.ErcDetectService;
import com.bubble.tx.exceptions.BubbleCallException;
import com.bubble.tx.exceptions.BubbleCallTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ErcServiceImpl {

    @Resource
    private TurnClient turnClient;

    @Resource
    private ErcDetectService ercDetectService;

    /**
     * Get the address token balance, ERC20 is the amount, ERC721 is the tokenId number
     *
     * @param tokenAddress contract address
     * @param type contract type
     * @param account user address
     * @param id tokenId
     * @return java.math.BigInteger
     */
    public BigInteger getBalance(String tokenAddress, ErcTypeEnum type, String account, BigInteger id) {
        BigInteger balance = BigInteger.ZERO;
        try {
            ErcContract ercContract = getErcContract(tokenAddress, type);
            if (ObjectUtil.isNotNull(ercContract)) {
                balance = ercContract.balanceOf(account, id).send();
            }
        } catch (Exception e) {
            log.warn(StrFormatter.format("Exception in obtaining address token balance, contractAddress:{},account:{}", tokenAddress, account), e);
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException interruptedException) {
                log.warn("InterruptedException", interruptedException);
            }
            throw new BusinessException("Failed to query Token balance!");
        }
        return balance;
    }

    /**
     * Get the total supply
     *
     * @param contractAddress contract address
     * @return java.math.BigInteger
     * @date 2021/1/18
     */
    public BigInteger getTotalSupply(String contractAddress) {
        BigInteger totalSupply = null;
        try {
            ErcContractId ercContractId = ercDetectService.getContractId(contractAddress);
            ErcContract ercContract = getErcContract(contractAddress, ercContractId.getTypeEnum());
            if (ObjectUtil.isNotNull(ercContract)) {
                totalSupply = ercContract.totalSupply().send();
            }
        } catch (Exception e) {
            log.warn(StrFormatter.format("Exception in obtaining total supply, contractAddress: {}", contractAddress), e);
        }
        return totalSupply;
    }

    /**
     * Get the corresponding type of ErcContract based on contractAddress and ercTypeEnum
     *
     * @param contractAddress contract address
     * @param ercTypeEnum contract type
     * @return com.turn.browser.v0151.contract.ErcContract
     */
    private ErcContract getErcContract(String contractAddress, ErcTypeEnum ercTypeEnum) {
        ErcContract ercContract = null;
        if (ErcTypeEnum.ERC20.equals(ercTypeEnum)) {
            ercContract = Erc20Contract.load(contractAddress, turnClient.getWeb3jWrapper().getWeb3j(), ErcDetectService.CREDENTIALS, ErcDetectService.GAS_PROVIDER);
        } else if (ErcTypeEnum.ERC721.equals(ercTypeEnum)) {
            ercContract = Erc721Contract.load(contractAddress, turnClient.getWeb3jWrapper().getWeb3j(), ErcDetectService.CREDENTIALS, ErcDetectService.GAS_PROVIDER);
        } else if (ErcTypeEnum.ERC1155.equals(ercTypeEnum)) {
            ercContract = Erc1155Contract.load(contractAddress, turnClient.getWeb3jWrapper().getWeb3j(), ErcDetectService.CREDENTIALS, ErcDetectService.GAS_PROVIDER);
        }
        return ercContract;
    }

    /**
     * Get the corresponding type of ErcContract based on contractAddress and ercTypeEnum
     *
     * @param contractAddress: contract address
     * @param ercTypeEnum: contract type
     * @param blockNumber: block height
     * @return: com.turn.browser.v0152.contract.ErcContract
     */
    private ErcContract getErcContract(String contractAddress, ErcTypeEnum ercTypeEnum, BigInteger blockNumber) {
        ErcContract ercContract = null;
        if (ErcTypeEnum.ERC20.equals(ercTypeEnum)) {
            ercContract = Erc20Contract.load(contractAddress, turnClient.getWeb3jWrapper().getWeb3j(), ErcDetectService.CREDENTIALS, ErcDetectService.GAS_PROVIDER, blockNumber);
        } else if (ErcTypeEnum.ERC721.equals(ercTypeEnum)) {
            ercContract = Erc721Contract.load(contractAddress, turnClient.getWeb3jWrapper().getWeb3j(), ErcDetectService.CREDENTIALS, ErcDetectService.GAS_PROVIDER, blockNumber);
        } else if (ErcTypeEnum.ERC1155.equals(ercTypeEnum)) {
            ercContract = Erc1155Contract.load(contractAddress, turnClient.getWeb3jWrapper().getWeb3j(), ErcDetectService.CREDENTIALS, ErcDetectService.GAS_PROVIDER, blockNumber);
        }
        return ercContract;
    }

    /**
     * Get the contract balance of erc20 with the highest historical block
     *
     * @param tokenAddress:
     * @param account:
     * @param blockNumber:
     * @return: java.math.BigInteger
     */
    public BigInteger getErc20HistoryBalance(String tokenAddress, String account, BigInteger blockNumber) {
        BigInteger balance = BigInteger.ZERO;
        try {
            ErcContract ercContract = getErcContract(tokenAddress, ErcTypeEnum.ERC20, blockNumber);
            if (ObjectUtil.isNotNull(ercContract)) {
                balance = ercContract.balanceOf(account, BigInteger.ZERO).send();
            }
        } catch (Exception e) {
            log.warn(StrFormatter.format("Exception in obtaining address token balance, contractAddress:{},account:{}", tokenAddress, account), e);
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException interruptedException) {
                log.warn("InterruptedException", interruptedException);
            }
            throw new BusinessException("Failed to query Token balance!");
        }
        return balance;
    }

    /**
     * Get the contract balance of erc1155 with the highest historical block
     *
     * @param tokenAddress:
     * @param tokenId:
     * @param account:
     * @param blockNumber:
     * @return: java.math.BigInteger
     */
    public BigInteger getErc1155HistoryBalance(String tokenAddress, BigInteger tokenId, String account, BigInteger blockNumber) {
        BigInteger balance = BigInteger.ZERO;
        try {
            ErcContract ercContract = getErcContract(tokenAddress, ErcTypeEnum.ERC1155, blockNumber);
            if (ObjectUtil.isNotNull(ercContract)) {
                balance = ercContract.balanceOf(account, tokenId).send();
            }
        } catch (Exception e) {
            log.warn(StrFormatter.format("Exception in obtaining address token balance, contractAddress:{},tokenId:{},account:{}", tokenAddress, tokenId, account), e);
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException interruptedException) {
                log.warn("InterruptedException", interruptedException);
            }
            throw new BusinessException("Failed to query Token balance!");
        }
        return balance;
    }

    /**
     * Get TokenURI
     *
     * @param contractAddress
     * @param tokenId
     * @return java.lang.String
     */
    public String getTokenURI(String contractAddress, BigInteger tokenId) {
        String tokenURI = "";
        try {
            ErcContract ercContract = getErcContract(contractAddress, ErcTypeEnum.ERC721);
            if (ObjectUtil.isNotNull(ercContract)) {
                tokenURI = ercContract.getTokenURI(tokenId).send();
            }
        } catch (BubbleCallException e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}, msg: {}", contractAddress, tokenId, e.getMsg()), e);
        } catch (BubbleCallTimeoutException e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}, msg: {}", contractAddress, tokenId, e.getMsg()), e);
        } catch (Exception e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}", contractAddress, tokenId), e);
        }
        return tokenURI;
    }

    /**
     * Get TokenURI
     *
     * @param contractAddress contract address
     * @param tokenId token id
     * @param blockNumber: block height
     * @return: java.lang.String
     */
    public String getTokenURI(String contractAddress, BigInteger tokenId, BigInteger blockNumber) {
        String tokenURI = "";
        try {
            ErcContract ercContract = getErcContract(contractAddress, ErcTypeEnum.ERC721, blockNumber);
            if (ObjectUtil.isNotNull(ercContract)) {
                tokenURI = ercContract.getTokenURI(tokenId).send();
            }
        } catch (BubbleCallException e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}, msg: {}", contractAddress, tokenId, e.getMsg()), e);
        } catch (BubbleCallTimeoutException e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}, msg: {}", contractAddress, tokenId, e.getMsg()), e);
        } catch (Exception e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}", contractAddress, tokenId), e);
        }
        return tokenURI;
    }

    /**
     * Get TokenURI
     *
     * @param contractAddress contract address
     * @param tokenId token id
     * @param blockNumber: block height
     * @return: java.lang.String
     */
    public String getToken1155URI(String contractAddress, BigInteger tokenId, BigInteger blockNumber) {
        String tokenURI = "";
        try {
            ErcContract ercContract = getErcContract(contractAddress, ErcTypeEnum.ERC1155, blockNumber);
            if (ObjectUtil.isNotNull(ercContract)) {
                tokenURI = ercContract.getTokenURI(tokenId).send();
            }
        } catch (BubbleCallException e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}, msg: {}", contractAddress, tokenId, e.getMsg()), e);
        } catch (BubbleCallTimeoutException e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}, msg: {}", contractAddress, tokenId, e.getMsg()), e);
        } catch (Exception e) {
            log.warn(StrFormatter.format("getTokenURI exception, token_address: {}, token_id: {}", contractAddress, tokenId), e);
        }
        return tokenURI;
    }

}
