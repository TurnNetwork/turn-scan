package com.turn.browser.client;

import com.turn.browser.enums.Web3jProtocolEnum;
import com.turn.browser.exception.ConfigLoadingException;
import com.bubble.contracts.dpos.*;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.Web3jService;
import com.bubble.protocol.http.HttpService;
import com.bubble.protocol.websocket.WebSocketService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Chain parameter unified configuration items
 *
 * @Description:
 */
@Slf4j
@Component
public class RetryableClient {

    private static final ReentrantReadWriteLock WEB3J_CONFIG_LOCK = new ReentrantReadWriteLock();

    @Value("${turn.web3j.protocol}")
    private Web3jProtocolEnum protocol;

    @Getter
    @Value("${turn.web3j.addresses}")
    private List<String> addresses;

    private List<Web3jWrapper> web3jWrappers = new ArrayList<>();

    private Web3jWrapper currentWeb3jWrapper;

    // Delegation contract interface
    private DelegateContract delegateContract;

    public DelegateContract getDelegateContract() {
        return delegateContract;
    }

    // Node contract interface
    private NodeContract nodeContract;

    public NodeContract getNodeContract() {
        return nodeContract;
    }

    // Proposal contract interface
    private ProposalContract proposalContract;

    public ProposalContract getProposalContract() {
        return proposalContract;
    }

    // Lock contract interface
    private RestrictingPlanContract restrictingPlanContract;

    public RestrictingPlanContract getRestrictingPlanContract() {
        return restrictingPlanContract;
    }

    // Slash contract interface
    private SlashContract slashContract;

    public SlashContract getSlashContract() {
        return slashContract;
    }

    // Staking contract interface
    private StakingContract stakingContract;

    public StakingContract getStakingContract() {
        return stakingContract;
    }

    // Reward contract interface
    private RewardContract rewardContract;

    public RewardContract getRewardContract() {
        return rewardContract;
    }

    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public void init() throws ConfigLoadingException {
        WEB3J_CONFIG_LOCK.writeLock().lock();
        try {
            web3jWrappers.clear();
            addresses.forEach(address -> {
                Web3jService service = null;
                if (protocol == Web3jProtocolEnum.WS) {
                    WebSocketService wss = new WebSocketService(protocol.getHead() + address, true);
                    try {
                        wss.connect();
                        service = wss;
                    } catch (ConnectException e) {
                        log.error("Websocket address ({}) cannot be connected:", protocol.getHead() + address, e);
                    }
                } else if (protocol == Web3jProtocolEnum.HTTP) {
                    service = new HttpService(protocol.getHead() + address);
                } else {
                    log.error("WWeb3j connection protocol [{}] is illegal!", protocol.getHead());
                    System.exit(1);
                }
                Web3jWrapper web3j = Web3jWrapper.builder()
                        .address(protocol.getHead() + address)
                        .web3jService(service)
                        .web3j(Web3j.build(service))
                        .build();
                web3jWrappers.add(web3j);
            });
            if (web3jWrappers.isEmpty())
                throw new ConfigLoadingException("No Web3j instance available!");
            updateCurrentWeb3jWrapper();
        } catch (Exception e) {
            log.error("Error loading Web3j configuration, will try again:", e);
            throw e;
        } finally {
            WEB3J_CONFIG_LOCK.writeLock().unlock();
        }
    }

    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public Web3jWrapper getWeb3jWrapper() {
        WEB3J_CONFIG_LOCK.readLock().lock();
        try {
            return currentWeb3jWrapper;
        } catch (Exception e) {
            log.error("Error loading Web3j configuration:", e);
        } finally {
            WEB3J_CONFIG_LOCK.readLock().unlock();
        }
        return null;
    }

    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public void updateContract() {
        rewardContract = RewardContract.load(currentWeb3jWrapper.getWeb3j());
        delegateContract = DelegateContract.load(currentWeb3jWrapper.getWeb3j());
        nodeContract = NodeContract.load(currentWeb3jWrapper.getWeb3j());
        proposalContract = ProposalContract.load(currentWeb3jWrapper.getWeb3j());
        restrictingPlanContract = RestrictingPlanContract.load(currentWeb3jWrapper.getWeb3j());
        slashContract = SlashContract.load(currentWeb3jWrapper.getWeb3j());
        stakingContract = StakingContract.load(currentWeb3jWrapper.getWeb3j());
    }

    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public void updateCurrentWeb3jWrapper() {
        WEB3J_CONFIG_LOCK.writeLock().lock();
        try {
            Web3jWrapper preWeb3j = currentWeb3jWrapper;
            // Check the connectivity of all Web3j, and take the one with the highest block height as the current web3j
            long maxBlockNumber = -1;
            for (Web3jWrapper wrapper : web3jWrappers) {
                try {
                    BigInteger blockNumber = wrapper.getWeb3j().bubbleBlockNumber().send().getBlockNumber();
                    if (blockNumber.longValue() >= maxBlockNumber) {
                        maxBlockNumber = blockNumber.longValue();
                        currentWeb3jWrapper = wrapper;
                    }
                } catch (Exception e2) {
                    log.info("Candidate Web3j instance ({}) is invalid！", wrapper.getAddress());
                }
            }
            if (preWeb3j == null || preWeb3j != currentWeb3jWrapper) {
                // If the previous web3j is empty or there is a change in Web3j, the contract variables will be updated.
                updateContract();
            }
            if (maxBlockNumber == -1) {
                log.info("All candidate Web3j instances are currently unreachable!");
                if (protocol == Web3jProtocolEnum.WS) {
                    log.info("Reinitialize websocket connection!");
                    try {
                        init();
                    } catch (ConfigLoadingException e) {
                        log.error("Reinitialization failed!");
                    }
                }
            }
        } finally {
            WEB3J_CONFIG_LOCK.writeLock().unlock();
        }
    }
}
