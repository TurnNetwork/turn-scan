package com.turn.browser.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Returns the percentage conversion implementation class
 * @file CustomRateSerializer.java
 * @description Use method to add @JsonSerialize(using = CustomRateSerializer.class) to the specific get method
 */
public class CustomRateSerializer extends JsonSerializer<String>{

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        /** Uniform percentage conversion, retaining two decimal places */
		if(StringUtils.isNotBlank(value)) {
			String transEner = new BigDecimal(value).multiply(new BigDecimal("100")).setScale(2) + "%";
			gen.writeString(transEner);
		} else {
			gen.writeString("");
		}
	}

}