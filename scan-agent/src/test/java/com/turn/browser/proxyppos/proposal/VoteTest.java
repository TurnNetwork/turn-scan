package com.turn.browser.proxyppos.proposal;

import com.bubble.abi.solidity.datatypes.BytesType;
import com.bubble.abi.solidity.datatypes.generated.Uint32;
import com.bubble.abi.solidity.datatypes.generated.Uint8;
import com.bubble.contracts.dpos.abi.Function;
import com.bubble.contracts.dpos.dto.common.FunctionType;
import com.bubble.contracts.dpos.dto.enums.VoteOption;
import com.bubble.contracts.dpos.utils.EncoderUtils;
import com.bubble.protocol.core.methods.response.bean.ProgramVersion;
import com.bubble.utils.Numeric;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.util.Arrays;

public class VoteTest extends ProposalBase {

    private byte[] encode(ProgramVersion pv, VoteOption voteOption, String verifier, String proposalID){
        Function function = new Function(FunctionType.VOTE_FUNC_TYPE,
                Arrays.asList(new BytesType(Numeric.hexStringToByteArray(verifier)),
                        new BytesType(Numeric.hexStringToByteArray(proposalID)), new Uint8(voteOption.getValue()),
                        new Uint32(pv.getProgramVersion()),
                        new BytesType(Numeric.hexStringToByteArray(pv.getProgramVersionSign()))));
        byte [] d = Hex.decode(EncoderUtils.functionEncoder(function));
        return d;
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void vote() throws Exception {
        ProgramVersion pv = defaultWeb3j.getProgramVersion().send().getAdminProgramVersion();
//        pv.setVersion(BigInteger.valueOf(7800));
        sendRequest(
                encode(pv,VoteOption.YEAS,nodeId1,"0x7403a411f7159b1e594ebface0b2fa47ccc5e1033e806218cc3a67a4f689587d"),
                encode(pv,VoteOption.YEAS,nodeId2,"0x7403a411f7159b1e594ebface0b2fa47ccc5e1033e806218cc3a67a4f689587d")
        );
    }

}
