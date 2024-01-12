package com.turn.browser.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;
import com.bubble.utils.Convert;

import java.io.IOException;
import java.math.RoundingMode;

/**
 * Return amount conversion implementation class
 * @file CustomLatSerializer.java
 * @description Use method to add @JsonSerialize(using = CustomLatSerializer.class) to the specific get method
 */
public class CustomLowLatSerializer extends JsonSerializer<String>{

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

		if(StringUtils.isNotBlank(value)) {
        /** Amount conversion AAA is uniformly converted into TURN with 12 decimal places rounded down */
			String transEner = Convert.fromVon(value, Convert.Unit.KPVON).setScale(2,RoundingMode.DOWN).toString();
			gen.writeString(transEner);
		} else {
			gen.writeString("");
		}
	}

}