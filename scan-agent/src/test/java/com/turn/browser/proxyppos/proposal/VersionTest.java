package com.turn.browser.proxyppos.proposal;

import com.bubble.abi.solidity.datatypes.BytesType;
import com.bubble.abi.solidity.datatypes.generated.Uint32;
import com.bubble.contracts.dpos.abi.Function;
import com.bubble.contracts.dpos.dto.common.FunctionType;
import com.bubble.contracts.dpos.utils.EncoderUtils;
import com.bubble.protocol.core.methods.response.bean.ProgramVersion;
import com.bubble.utils.Numeric;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.util.Arrays;

public class VersionTest extends ProposalBase {


    protected byte[] encode(String nodeId, ProgramVersion pv){
        Function function = new Function(FunctionType.DECLARE_VERSION_FUNC_TYPE,
                Arrays.asList(new BytesType(Numeric.hexStringToByteArray(nodeId)),
                        new Uint32(pv.getProgramVersion()),
                        new BytesType(Numeric.hexStringToByteArray(pv.getProgramVersionSign()))));
        byte [] d = Hex.decode(EncoderUtils.functionEncoder(function));
        return d;
    }

    @Test
    public void version() throws Exception {
        ProgramVersion pv = defaultWeb3j.getProgramVersion().send().getAdminProgramVersion();
        sendRequest(
                encode(nodeId1,pv),
                encode(nodeId2,pv)
        );
    }
}
