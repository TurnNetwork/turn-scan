package com.turn.browser.bootstrap.bean;

import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.elasticsearch.dto.Transaction;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class SyncData {

    private Set<Block> blockSet = new HashSet<>();

    private Set<Transaction> txBakSet = new HashSet<>();

    private Set<ErcTx> erc20BakSet = new HashSet<>();

    private Set<ErcTx> erc721BakSet = new HashSet<>();

    private Set<ErcTx> erc1155BakSet = new HashSet<>();

    private Set<DelegationReward> delegationRewardBakSet = new HashSet<>();

}
