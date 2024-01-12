package com.turn.browser.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 	String returns null value to handle serialization
 */
public class NullStringJsonSerializer extends JsonSerializer<Object> {
	@Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null || "null".equals(value)) {
        	/**
    		 * 	If the array is null, fill in "".
    		 */
            jgen.writeString("");
        }
    }
}
