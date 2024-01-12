package com.turn.browser.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.turn.browser.utils.ChainVersionUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Return version conversion implementation class
 * @file CustomVersionSerializer.java
 * @description Use method to add @JsonSerialize(using = CustomVersionSerializer.class) to the specific get method
 */
public class CustomVersionSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            gen.writeString(ChainVersionUtil.toStringVersion(new BigInteger(value)));
        } else {
            gen.writeString("");
        }
    }

}