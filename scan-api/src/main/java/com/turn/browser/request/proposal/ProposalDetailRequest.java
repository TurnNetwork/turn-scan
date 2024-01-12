package com.turn.browser.request.proposal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Proposal details request recipients
 */
public class ProposalDetailRequest {
    @NotBlank(message = "{proposalHash not null}")
    @Size(min = 60,max = 66)
    private String proposalHash;

	public String getProposalHash() {
		return proposalHash;
	}

	public void setProposalHash(String proposalHash) {
		this.proposalHash = proposalHash;
	}
    
}