package com.turn.browser.cache;

import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.mapper.ProposalMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Parameter proposal cache
 * Cache structure: Map<Proposal Effective Block Number, List<Parameter Proposal Transaction Hash>>
 */
@Slf4j
@Component
public class ProposalCache {

    @Resource
    private ProposalMapper proposalMapper;

    //<effective block number->proposal entity list>
    private static final Map<Long, Set<String>> cache = new HashMap<>();

    public void add(Long activeBlockNumber, String proposalId) {
        Set<String> proposalIdList = cache.computeIfAbsent(activeBlockNumber, k -> new HashSet<>());
        proposalIdList.add(proposalId);
    }

    public Set<String> get(Long activeBlockNumber) {
        return cache.get(activeBlockNumber);
    }

    /**
     * Initialize proposal cache
     *
     * @param
     * @return void
     */
    public void init() {
        log.info("Initializing proposal cache");
        // Initialize proposal cache: cache all [Parameter Proposals] and [Upgrade Proposals] that are in voting status into memory
        List<Proposal> proposalList = proposalMapper.selectByExample(null);
        proposalList.forEach(p -> add(p.getActiveBlock(), p.getHash()));
    }

}