package com.turn.browser.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 	Number returns null value handling serialization
 */
public class NullIntegerJsonSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
        	/**
    		 * 	If the array is null, fill in 0.
    		 */
            jgen.writeNumber(0);
        }
    }
}
