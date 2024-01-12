package com.turn.browser.enums;


import com.bubble.parameters.NetworkParameters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum InnerContractAddrEnum {
    RESTRICTING_PLAN_CONTRACT(NetworkParameters.getDposContractAddressOfRestrctingPlan(), "Locked Contract"),
    STAKING_CONTRACT(NetworkParameters.getDposContractAddressOfStaking(), "Staking Contract"),
    DELEGATE_CONTRACT(NetworkParameters.getDposContractAddressOfStaking(), "Staking Contract"),
    SLASH_CONTRACT(NetworkParameters.getDposContractAddressOfSlash(), "Penalty Contract"),
    PROPOSAL_CONTRACT(NetworkParameters.getDposContractAddressOfProposal(), "Governance (Proposal) Contract"),
    INCENTIVE_POOL_CONTRACT(NetworkParameters.getDposContractAddressOfIncentivePool(), "Incentive Pool Contract"),
    NODE_CONTRACT(NetworkParameters.getDposContractAddressOfStaking(), "Node related contract"),
    REWARD_CONTRACT(NetworkParameters.getDposContractAddressOfReward(), "Receive reward contract"),
    STAKINGL2_CONTRACT(NetworkParameters.getDposContractAddressOfL2Staking(), "Layer 2 network micro-node pledge contract"),
    BUBBLE_CONTRACT(NetworkParameters.getDposContractAddressOfBubble(), "Bubble sub-network management contract");
    private String address;

    private String desc;

    InnerContractAddrEnum(String address, String desc) {
        this.address = address;
        this.desc = desc;
    }

    public String getAddress() {
        return address;
    }

    public String getDesc() {
        return desc;
    }

    private static final Set<String> ADDRESSES = new HashSet<>();

    public static Set<String> getAddresses() {
        return ADDRESSES;
    }

    static {
        Arrays.asList(InnerContractAddrEnum.values())
              .forEach(innerContractAddEnum -> ADDRESSES.add(innerContractAddEnum.address));
    }
}
