
package com.turn.browser.param.evidence;

import lombok.Data;

@Data
public class VoteEvidence extends Evidence{

    private Vote voteA;
    private Vote voteB;

}