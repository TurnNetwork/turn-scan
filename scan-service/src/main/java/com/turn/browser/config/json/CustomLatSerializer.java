package com.turn.browser.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.turn.browser.utils.EnergonUtil;
import com.bubble.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Return amount conversion implementation class
 * @file CustomLatSerializer.java
 * @description Use method to add @JsonSerialize(using = CustomLatSerializer.class) to the specific get method
 */
public class CustomLatSerializer extends JsonSerializer<BigDecimal>{

	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if(value != null) {
            /** Amount conversion AAA is uniformly converted into TURN with 12 decimal places rounded down */
			String transEner = EnergonUtil.format(Convert.fromVon(value, Convert.Unit.KPVON).setScale(12,RoundingMode.DOWN), 12);
			gen.writeString(transEner);
		} else {
			gen.writeString("");
		}
	}

}