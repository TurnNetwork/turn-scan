package com.turn.browser.proxyppos.proposal;

import com.bubble.contracts.dpos.dto.resp.Proposal;
import org.junit.Test;

public class TextTest extends ProposalBase {
    @Test
    public void text() throws Exception {
        Proposal p1 = Proposal.createSubmitTextProposalParam(nodeId1, "1");
        Proposal p2 = Proposal.createSubmitTextProposalParam(nodeId2, "2");
        sendRequest(
                encode(p1),
                encode(p2)
        );
    }
}
