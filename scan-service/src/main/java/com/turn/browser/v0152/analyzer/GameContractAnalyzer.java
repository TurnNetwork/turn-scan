package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bubble.crypto.Credentials;
import com.bubble.parameters.NetworkParameters;
import com.bubble.protocol.Web3j;
import com.bubble.tx.exceptions.BubbleCallTimeoutException;
import com.bubble.tx.gas.ContractGasProvider;
import com.bubble.tx.gas.GasProvider;
import com.turn.browser.bean.CollectionTransaction;
import com.turn.browser.bean.GameContract;
import com.turn.browser.client.TurnClient;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.contract.GameContract.CreateGameEventResponse;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.AddrGameMapper;
import com.turn.browser.dao.mapper.GameMapper;
import com.turn.browser.dao.mapper.RoundMapper;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.elasticsearch.dto.GameContractTx;
import com.turn.browser.enums.GameEventTypeEnum;
import com.turn.browser.enums.GameTypeEnum;
import com.turn.browser.enums.RoundStatusEnum;
import com.turn.browser.service.AddrGameCacheService;
import com.turn.browser.service.BubbleCacheService;
import com.turn.browser.utils.CommonUtil;
import com.turn.browser.v0152.bean.AddrGameDetailDto;
import com.turn.browser.v0152.bean.BubbleDetailDto;
import com.turn.browser.v0152.bean.ErcTxInfo;
import com.turn.browser.v0152.bean.GameContractId;
import com.turn.browser.v0152.contract.Erc20Contract;
import com.turn.browser.v0152.contract.ErcContract;
import com.turn.browser.v0152.service.GameDetectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameContractAnalyzer {

    @Resource
    private GameDetectService gameDetectService;

    @Resource
    private GameCache gameCache;

    @Resource
    private GameMapper gameMapper;

    @Resource
    private AddrGameMapper addrGameMapper;

    @Resource
    private RoundMapper roundMapper;

    @Resource
    private AddrGameCacheService addrGameCacheService;

    @Resource
    private TurnClient turnClient;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private BubbleCacheService bubbleCacheService;

    public static Credentials CREDENTIALS;

    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(2104836);

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(100000000000L);

    public static final GasProvider GAS_PROVIDER = new ContractGasProvider(GAS_PRICE, GAS_LIMIT);

    @PostConstruct
    public void init() {
        NetworkParameters.init(chainConfig.getChainId());
        CREDENTIALS = Credentials.create("4484092b68df58d639f11d59738983e2b8b81824f3c0c759edd6773f9adadfe7");
    }

    /**
     * Parse the game contract and call it when the contract is created
     *
     * @param contractAddress
     */
    public GameContract resolveGameContract(String contractAddress) {
        GameContract gameContract = new GameContract();
        gameContract.setTypeEnum(GameTypeEnum.UNKNOWN);
        try {
            gameContract.setContractAddress(contractAddress);
            GameContractId contractId = gameDetectService.getContractId(contractAddress);
            BeanUtils.copyProperties(contractId, gameContract);
            gameContract.setTypeEnum(contractId.getTypeEnum());
            switch (contractId.getTypeEnum()) {
                case GAME:
                    gameCache.gameAddressCache.add(contractAddress);
                    break;
                default:
            }
            if(gameContract.getTypeEnum() == GameTypeEnum.GAME){
                Game game = new Game();
                BeanUtils.copyProperties(contractId, game);
                game.setContractAddress(contractAddress);
                game.setCreateTime(new Date());
                game.setStatus(RoundStatusEnum.START.getCode());
                gameMapper.insert(game);
                gameCache.gameMapCache.put(contractAddress, game);
            } else {
                log.warn("The contract address [{}] cannot recognize the type [{}]", gameContract.getContractAddress(), gameContract.getTypeEnum());
            }
        } catch (Exception e) {
            log.error("Game contract creation, parsing exception", e);
        }
        return gameContract;
    }

    /**
     * Parse the transaction from the transaction receipt event
     *
     * @param game game
     * @param tx transaction
     * @param eventList event list
     * @return java.util.List<com.turn.browser.elasticsearch.dto.ErcTx> erc交易列表
     */
    private List<GameContractTx> resolveGameTxFromEvent(Game game, CollectionTransaction tx, List<com.turn.browser.contract.GameContract.GameTxEvent> eventList, Long seq) {
        List<GameContractTx> gameContractTxList = new ArrayList<>();
        eventList.forEach(event -> {

            GameContractTx gameContractTx = GameContractTx.builder()
                               .seq(seq)
                               .bn(tx.getNum())
                               .hash(tx.getHash())
                               .bTime(tx.getTime())
                               .txFee(tx.getCost())
                               .gameContractAddress(event.getTo())
                               .contract(event.getOperator())
                               .operator(event.getOperator())
                               .from(event.getFrom())
                               .to(event.getTo())
                               .value(event.getValue().toString())
                               .name(game.getName())
                               .contract(game.getContractAddress())
                               .build();
            gameContractTxList.add(gameContractTx);
        });
        return gameContractTxList;
    }

    /**
     * Get transaction information
     *
     * @param txList transaction list
     * @return java.lang.String
     */
    private String getErcTxInfo(List<ErcTx> txList) {
        List<ErcTxInfo> infoList = new ArrayList<>();
        txList.forEach(tx -> {
            ErcTxInfo eti = new ErcTxInfo();
            BeanUtils.copyProperties(tx, eti);
            infoList.add(eti);
        });
        return JSON.toJSONString(infoList);
    }

    /**
     * Parse ERC transactions, called when the contract is called
     *
     * @param tx transaction object
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void resolveTx(CollectionTransaction tx) {
        try {

            if (CollUtil.isEmpty(tx.getGameContractEventInfo())) {
                return;
            }
            Game game = gameCache.gameMapCache.get(tx.getTo());
            tx.getGameContractEventInfo().forEach(eventInfo -> {
                Map<String,String> map = JSONObject.parseObject(eventInfo, Map.class);
                map.keySet().stream().forEach(x->{
                    if(GameEventTypeEnum.CREATE_GAME_EVENT.getDesc().equals(x)){
                        //Handle creation game events
                        String eventStr = map.get(x);
                        List<JSONObject> createGameEventResponses = JSONObject.parseObject(eventStr, List.class);
                        createGameHandle(game,createGameEventResponses);
                    }
                    if(GameEventTypeEnum.JOIN_GAME_EVENT.getDesc().equals(x)){
                        //Handle the event of joining the game round
                        String eventStr = map.get(x);
                        List<JSONObject> joinGameEventResponses = JSONObject.parseObject(eventStr, List.class);
                        joinGameHandle(game,joinGameEventResponses);
                    }
                    if(GameEventTypeEnum.END_GAME_EVENT.getDesc().equals(x)){
                        //Handle the end game round event
                        String eventStr = map.get(x);
                        List<JSONObject> endGameEventResponses = JSONObject.parseObject(eventStr, List.class);
                        endGameHandle(game,endGameEventResponses);
                    }
                });
            });
            log.info("The current game transaction [{}] has [{}] events",
                    tx.getHash(),
                    CommonUtil.ofNullable(() -> tx.getGameContractEventInfo().size()).orElse(0));
        } catch (Exception e) {
            log.error(StrUtil.format("Exception in parsing Game transaction of current transaction [{}]", tx.getHash()), e);
        }
    }

    /**
     * Handle end game round events
     * @param game
     * @param endGameEvents
     */
    private void endGameHandle(Game game, List<JSONObject> endGameEvents) {
        if(CollUtil.isNotEmpty(endGameEvents)){
            List<Long> roundIds = endGameEvents.stream().map(x -> {
                com.turn.browser.contract.GameContract.EndGameEventResponse item = JSONObject.toJavaObject(x, com.turn.browser.contract.GameContract.EndGameEventResponse.class);
                return item.boundId.longValue();
            }).collect(Collectors.toList());

            //Update round information
            roundMapper.endRound(roundIds.get(0),game.getId());

            //Update address information
            addrGameMapper.endGame(roundIds.get(0),game.getId());

            AddrGameExample addrGameExample = new AddrGameExample();
            AddrGameExample.Criteria criteria = addrGameExample.createCriteria();
            criteria.andRoundIdIn(roundIds);
            criteria.andGameIdEqualTo(game.getId());

            // refresh cache
            List<AddrGame> addrGameList = addrGameMapper.selectByExample(addrGameExample);
            addrGameCacheService.delAddrGameCache(addrGameList);
            bubbleCacheService.delBubbleCache(roundMapper.selectByPrimaryKey(roundIds.get(0)).getBubbleId());
        }
    }

    /**
     * Handle join game events
     *
     * @param game
     * @param joinGameEventResponses
     */
    private void joinGameHandle(Game game, List<JSONObject> joinGameEventResponses) {
        if(CollUtil.isNotEmpty(joinGameEventResponses)){
            RoundExample roundExample = new RoundExample();
            RoundExample.Criteria criteria = roundExample.createCriteria();
            criteria.andGameIdEqualTo(game.getId());
            List<Round> roundList = roundMapper.selectByExample(roundExample);
            Map<Long, Round> roundMap = roundList.stream().collect(Collectors.toMap(round->round.getRoundId(), round->round));

            joinGameEventResponses.forEach(x->{
                com.turn.browser.contract.GameContract.JoinGameEventResponse item = JSONObject.toJavaObject(x, com.turn.browser.contract.GameContract.JoinGameEventResponse.class);
                Round round = roundMap.get(item.boundId.longValue());
                AddrGame addrGame = new AddrGame();
                addrGame.setAddress(item.player.toLowerCase());
                addrGame.setBubbleId(round.getBubbleId());
                addrGame.setRoundId(item.boundId.longValue());
                addrGame.setGameId(game.getId());
                addrGame.setGameContractAddress(game.getContractAddress());
                addrGame.setStatus(RoundStatusEnum.START.getCode());
                addrGame.setCreateTime(new Date());
                addrGameMapper.insert(addrGame);
                AddrGameDetailDto addrGameDetailDto = new AddrGameDetailDto();
                BeanUtils.copyProperties(addrGame,addrGameDetailDto);
                addrGameDetailDto.setTokenAddress(round.getTokenAddress());
                addrGameDetailDto.setTokenDecimal(round.getTokenDecimal());
                addrGameDetailDto.setTokenSymbol(round.getTokenSymbol());
                addrGameDetailDto.setTokenRpc(round.getTokenRpc());
                addrGameCacheService.addAddrGameCache(addrGameDetailDto);
            });
        }
    }

    /**
     * Handle create game turn event
     *
     * @param game
     * @param createGameEventResponses
     */
    private void createGameHandle(Game game, List<JSONObject> createGameEventResponses) {
        if(CollUtil.isNotEmpty(createGameEventResponses)){

            List<Round> roundList = new ArrayList<>(createGameEventResponses.size());
            createGameEventResponses.forEach(x->{
                CreateGameEventResponse item = JSONObject.toJavaObject(x,CreateGameEventResponse.class);
                Round round = new Round();
                round.setCreator(item.creator);
                round.setGameId(game.getId());
                round.setBubbleId(item.bubbleId.longValue());
                round.setRoundId(item.boundId.longValue());
                round.setTokenAddress(item.tokenAddress);
                round.setStatus(RoundStatusEnum.START.getCode());
                round.setCreateTime(new Date());
                BubbleDetailDto bubbleCache = bubbleCacheService.getBubbleCache(item.bubbleId.longValue());
                if(ObjectUtil.isNotNull(bubbleCache)){
                    round.setTokenRpc(JSONObject.toJSONString(bubbleCache.getRpcUris()));
                }else {
                    round.setTokenRpc("");
                }
                getTokenInfo(round);
                roundList.add(round);
            });
            roundMapper.batchInsert(roundList);
        }
    }

    /**
     * Get round currency information
     * @param round
     */
    public void getTokenInfo(Round round){
        ErcContract ercContract = Erc20Contract.load(round.getTokenAddress(),
                turnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        try {
            round.setTokenSymbol(ercContract.symbol().send());
        } catch (BubbleCallTimeoutException e) {
            log.error("getTokenInfo gets name timeout exception", e);
        } catch (Exception e) {
            log.warn("getTokenInfo get name error", e);
        }
        try {
            round.setTokenDecimal(ercContract.decimals().send().intValue());
        } catch (BubbleCallTimeoutException e) {
            log.error("ERC gets decimal timeout exception", e);
        } catch (Exception e) {
            log.warn("erc get decimal error", e);
        }
    }

}
