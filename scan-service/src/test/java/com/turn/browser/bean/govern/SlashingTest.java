package com.turn.browser.bean.govern;

import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.govern.Slashing;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

/**
 * @Description:
 */
@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class SlashingTest {

    @Test
    public void slashingTest(){
        Slashing slashing = Slashing.builder()
                .slashFractionDuplicateSign(BigDecimal.TEN)
                .slashBlocksReward(BigDecimal.TEN)
                .duplicateSignReportReward(BigDecimal.TEN)
                .maxEvidenceAge(BigDecimal.TEN)
                .build();
        slashing.setDuplicateSignReportReward(BigDecimal.ONE);
        slashing.setMaxEvidenceAge(BigDecimal.ONE);
        slashing.setSlashBlocksReward(BigDecimal.ONE);
        slashing.setSlashFractionDuplicateSign(BigDecimal.ONE);
        log.debug("slashing : {}", JSON.toJSONString(slashing));
        slashing.getDuplicateSignReportReward();
        slashing.getMaxEvidenceAge();
        slashing.getSlashBlocksReward();
        slashing.getSlashFractionDuplicateSign();
        log.debug("value : {} ,{} ,{} ,{}",slashing.getDuplicateSignReportReward(),slashing.getMaxEvidenceAge(),slashing.getSlashBlocksReward(),slashing.getSlashFractionDuplicateSign());
    }
}