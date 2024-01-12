package com.turn.browser.v0152.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bubble.crypto.Credentials;
import com.bubble.crypto.Keys;
import com.bubble.parameters.NetworkParameters;
import com.bubble.protocol.core.DefaultBlockParameter;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.core.Response;
import com.bubble.protocol.core.methods.request.Transaction;
import com.bubble.protocol.core.methods.response.BubbleCall;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import com.bubble.tx.exceptions.BubbleCallTimeoutException;
import com.bubble.tx.exceptions.ContractCallException;
import com.bubble.tx.gas.ContractGasProvider;
import com.bubble.tx.gas.GasProvider;
import com.turn.browser.bean.CommonConstant;
import com.turn.browser.bean.Receipt;
import com.turn.browser.client.TurnClient;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.contract.GameContract;
import com.turn.browser.enums.ErcTypeEnum;
import com.turn.browser.enums.GameTypeEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.v0152.bean.ErcContractId;
import com.turn.browser.v0152.bean.GameContractId;
import com.turn.browser.v0152.contract.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;


@Slf4j
@Service
public class GameDetectService {

    @Resource
    private BlockChainConfig chainConfig;

    public static Credentials CREDENTIALS;

    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(2104836);

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(100000000000L);

    public static final ContractGasProvider GAS_PROVIDER = new ContractGasProvider(GAS_PRICE, GAS_LIMIT);

    @Resource
    private TurnClient turnClient;

    @PostConstruct
    public void init() {
        NetworkParameters.init(chainConfig.getChainId());
        CREDENTIALS = Credentials.create("4484092b68df58d639f11d59738983e2b8b81824f3c0c759edd6773f9adadfe7");
    }

    /**
     * Detect input data--without retry mechanism
     *
     * @param contractAddress
     * @param inputData
     */
    @Retryable(value = BubbleCallTimeoutException.class, maxAttempts = CommonConstant.reTryNum)
    private String detectInputData(String contractAddress, String inputData) throws BubbleCallTimeoutException {
        Transaction transaction = null;
        BubbleCall turnCall = null;
        try {
            transaction = Transaction.createEthCallTransaction(Credentials.create(Keys.createEcKeyPair()).getAddress(),
                                                               contractAddress,
                                                               inputData);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error(StrUtil.format("Contract address [{}] detects abnormal input data", contractAddress), e);
            throw new BusinessException(e.getMessage());
        }
        try {
            turnCall = turnClient.getWeb3jWrapper()
                    .getWeb3j()
                    .bubbleCall(transaction, DefaultBlockParameterName.LATEST)
                    .send();
            if (turnCall.hasError()) {
                Response.Error error = turnCall.getError();
                String message = error.getMessage();
                String lowMessage = !StrUtil.isBlank(message) ? message.toLowerCase() : null;
                // If timeout is included, a timeout exception will be thrown, and for other errors, a runtime exception will be thrown directly.
                if (!StrUtil.isBlank(lowMessage) && lowMessage.contains("timeout")) {
                    log.error("Contract address [{}] detects input data timeout exception.error_code[{}],error_msg[{}]",
                            contractAddress,
                            error.getCode(),
                            error.getMessage());
                    throw new BubbleCallTimeoutException(error.getCode(), error.getMessage(), turnCall);
                }
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return turnCall.getResult();
    }

    /**
     * Detect input data--without retry mechanism
     *
     * @param contractAddress
     * @param inputData
     */
    @Retryable(value = BubbleCallTimeoutException.class, maxAttempts = CommonConstant.reTryNum)
    private String detectInputData(String contractAddress, String inputData, BigInteger blockNumber) throws
            BubbleCallTimeoutException {
        Transaction transaction = null;
        BubbleCall turnCall = null;
        try {
            transaction = Transaction.createEthCallTransaction(Credentials.create(Keys.createEcKeyPair()).getAddress(),
                    contractAddress,
                    inputData);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error(StrUtil.format("Contract address [{}] detects abnormal input data", contractAddress), e);
            throw new BusinessException(e.getMessage());
        }
        try {
            turnCall = turnClient.getWeb3jWrapper()
                                     .getWeb3j()
                                     .bubbleCall(transaction, DefaultBlockParameter.valueOf(blockNumber))
                                     .send();
            if (turnCall.hasError()) {
                Response.Error error = turnCall.getError();
                String message = error.getMessage();
                String lowMessage = !StrUtil.isBlank(message) ? message.toLowerCase() : null;
                // If timeout is included, a timeout exception will be thrown, and for other errors, a runtime exception will be thrown directly.
                if (!StrUtil.isBlank(lowMessage) && lowMessage.contains("timeout")) {
                    log.error("Contract address [{}] detects input data timeout exception.error_code[{}],error_msg[{}]",
                            contractAddress,
                            error.getCode(),
                            error.getMessage());
                    throw new BubbleCallTimeoutException(error.getCode(), error.getMessage(), turnCall);
                }
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return turnCall.getResult();
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back.
     *
     * @param e:
     */
    @Recover
    public String recoverDetectInputData(Exception e) {
        log.error("Retry is completed or the business fails, please contact the administrator for processing");
        return null;
    }

    /**
     * Whether to support Erc165 standard
     *
     * @param contractAddress:
     * @return: boolean
     */
    private boolean isSupportErc165(String contractAddress) throws BubbleCallTimeoutException {
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a701ffc9a700000000000000000000000000000000000000000000000000000000");
        if (!"0x0000000000000000000000000000000000000000000000000000000000000001".equals(result)) {
            return false;
        }
        result = detectInputData(contractAddress,
                                 "0x01ffc9a7ffffffff00000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000000".equals(result);
    }

    /**
     * Whether to support Erc165 standard
     *
     * @param contractAddress:
     * @param blockNumber:
     */
    private boolean isSupportErc165(String contractAddress, BigInteger blockNumber) throws BubbleCallTimeoutException {
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a701ffc9a700000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        if (!"0x0000000000000000000000000000000000000000000000000000000000000001".equals(result)) {
            return false;
        }
        result = detectInputData(contractAddress,
                                 "0x01ffc9a7ffffffff00000000000000000000000000000000000000000000000000000000",
                                 blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000000".equals(result);
    }

    public boolean isSupportErc721Metadata(String contractAddress) throws BubbleCallTimeoutException {
        // To support erc721, you must support erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a75b5e139f00000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public boolean isSupportErc721Metadata(String contractAddress, BigInteger blockNumber) throws
                                                                                           BubbleCallTimeoutException {
        // To support erc721, you must support erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a75b5e139f00000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public boolean isSupportErc721Enumerable(String contractAddress) throws BubbleCallTimeoutException {
        // To support erc721, you must support erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7780e9d6300000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public boolean isSupportErc721Enumerable(String contractAddress, BigInteger blockNumber) throws
                                                                                             BubbleCallTimeoutException {
        // To support erc721, you must support erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7780e9d6300000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * Whether to support Erc721 contract
     *
     * @param contractAddress:
     */
    private boolean isSupportErc721(String contractAddress) throws BubbleCallTimeoutException {
        // To support erc721, you must support erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a780ac58cd00000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * Whether to support Erc721 contract
     *
     * @param contractAddress:
     * @param blockNumber:
     */
    private boolean isSupportErc721(String contractAddress, BigInteger blockNumber) throws BubbleCallTimeoutException {
        // To support erc721, you must support erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a780ac58cd00000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * Whether to support Erc1155 contract
     *
     * @param contractAddress:
     */
    private boolean isSupportErc1155(String contractAddress) throws BubbleCallTimeoutException {
        // To support erc1155, you must support erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7d9b67a2600000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * Whether to support Erc1155 contract
     *
     * @param contractAddress:
     * @param blockNumber:
     */
    private boolean isSupportErc1155(String contractAddress, BigInteger blockNumber) throws BubbleCallTimeoutException {
        // To support erc1155, you must support erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7d9b67a2600000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public Boolean isSupportErc1155Metadata(String contractAddress, BigInteger blockNumber) throws
                                                                                            BubbleCallTimeoutException {
        // To support erc1155, you must support erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("This contract [{}] does not support erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a70e89341c00000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    private ErcContractId getErc20ContractId(String contractAddress) throws BubbleCallTimeoutException {
        ErcContract ercContract = Erc20Contract.load(contractAddress,
                                                     turnClient.getWeb3jWrapper().getWeb3j(),
                                                     CREDENTIALS,
                                                     GAS_PROVIDER);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC20);
        return contractId;
    }

    private ErcContractId getErc20ContractId(String contractAddress, BigInteger blockNumber) throws
                                                                                             BubbleCallTimeoutException {
        ErcContract ercContract = Erc20Contract.load(contractAddress,
                                                     turnClient.getWeb3jWrapper().getWeb3j(),
                                                     CREDENTIALS,
                                                     GAS_PROVIDER,
                                                     blockNumber);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC20);
        return contractId;
    }

    private ErcContractId getErc721ContractId(String contractAddress) throws BubbleCallTimeoutException {
        ErcContract ercContract = Erc721Contract.load(contractAddress,
                                                      turnClient.getWeb3jWrapper().getWeb3j(),
                                                      CREDENTIALS,
                                                      GAS_PROVIDER);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC721);
        return contractId;
    }

    private ErcContractId getErc721ContractId(String contractAddress, BigInteger blockNumber) throws
                                                                                              BubbleCallTimeoutException {
        ErcContract ercContract = Erc721Contract.load(contractAddress,
                                                      turnClient.getWeb3jWrapper().getWeb3j(),
                                                      CREDENTIALS,
                                                      GAS_PROVIDER,
                                                      blockNumber);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC721);
        return contractId;
    }

    private ErcContractId getErc1155ContractId(String contractAddress) throws BubbleCallTimeoutException {
        ErcContract ercContract = Erc721Contract.load(contractAddress,
                                                      turnClient.getWeb3jWrapper().getWeb3j(),
                                                      CREDENTIALS,
                                                      GAS_PROVIDER);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC1155);
        return contractId;
    }

    private ErcContractId getErc1155ContractId(String contractAddress, BigInteger blockNumber) throws
                                                                                               BubbleCallTimeoutException {
        ErcContract ercContract = Erc1155Contract.load(contractAddress,
                                                       turnClient.getWeb3jWrapper().getWeb3j(),
                                                       CREDENTIALS,
                                                       GAS_PROVIDER,
                                                       blockNumber);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC1155);
        return contractId;
    }

    // Detect Erc20 contract identification
    private ErcContractId resolveContractId(ErcContract ercContract) throws BubbleCallTimeoutException {
        ErcContractId contractId = new ErcContractId();
        try {
            try {
                contractId.setName(ercContract.name().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC gets name timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get name error", e);
            }
            try {
                contractId.setSymbol(ercContract.symbol().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC acquisition symbol timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get symbol error", e);
            }
            try {
                contractId.setDecimal(ercContract.decimals().send().intValue());
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC gets decimal timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get decimal error", e);
            }
            try {
                contractId.setTotalSupply(new BigDecimal(ercContract.totalSupply().send()));
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC gets totalSupply timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get totalSupply error", e);
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (ContractCallException e) {
            log.error(" not erc contract,{}", ercContract, e);
        }
        return contractId;
    }

    @Retryable(value = BubbleCallTimeoutException.class, maxAttempts = CommonConstant.reTryNum)
    public GameContractId getContractId(String contractAddress) throws BubbleCallTimeoutException {
        GameContractId gameContractId = null;
        try {
            gameContractId = isGameContract(contractAddress);

            if (StrUtil.isNotBlank(gameContractId.getName())) {
                // Get game contract information
                log.info("This contract [{}] is a game contract", contractAddress);
                gameContractId.setTypeEnum(GameTypeEnum.GAME);
                return gameContractId;
            }

        } catch (BubbleCallTimeoutException e) {
            log.error("Timeout exception in obtaining contract[{}]id", contractAddress);
            throw e;
        } catch (Exception e) {
            log.error(StrUtil.format("Exception in getting contract [{}]id", contractAddress), e);
            throw e;
        }
        return gameContractId;
    }

    private GameContractId isGameContract(String contractAddress) throws
            BubbleCallTimeoutException {
        GameContract gameContract = GameContract.load(contractAddress,
                turnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return resolveContractIdGameContract(gameContract);
    }

    private GameContractId resolveContractIdGameContract(GameContract gameContract) throws BubbleCallTimeoutException {
        GameContractId gameContractId = new GameContractId();
        try {
            try {
                gameContractId.setName(gameContract.Name().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("Game contract got name timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get name error", e);
            }
            try {
                gameContractId.setWebsite(gameContract.Website().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("game contract gets website timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get website error", e);
            }
            try {
                gameContractId.setIntroduce(gameContract.Introduce().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("game contract gets introduce timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get introduce error", e);
            }
            try {
                gameContractId.setGameType(gameContract.GameType().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("game contract gets gameType timeout exception", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get gameType error", e);
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (ContractCallException e) {
            log.error(" not game contract,{}", gameContract, e);
        }
        return gameContractId;
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back
     *
     * @param e:
     */
    @Recover
    public ErcContractId recover(Exception e) {
        log.error("If the retry is completed or the service fails, please contact the administrator for processing.");
        return null;
    }

    public List<GameContract.GameTxEvent> getGameDetectTxEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                                                     turnClient.getWeb3jWrapper().getWeb3j(),
                                                     CREDENTIALS,
                                                     GAS_PROVIDER);
        return gameContract.getTxEvents(receipt);
    }

    /**
     * Get join game event
     * @param receipt
     * @return
     */
    public List<GameContract.JoinGameEventResponse> getJoinGameEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                turnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return gameContract.getJoinGameEvents(receipt);
    }

    /**
     * Get the creation game event
     * @param receipt
     * @return
     */
    public List<GameContract.CreateGameEventResponse> getCreateGameEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                turnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return gameContract.getCreateGameEvents(receipt);
    }

    /**
     * Get end game event
     * @param receipt
     * @return
     */
    public List<GameContract.EndGameEventResponse> getEndGameEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                turnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return gameContract.getEndGameEvents(receipt);
    }
}
