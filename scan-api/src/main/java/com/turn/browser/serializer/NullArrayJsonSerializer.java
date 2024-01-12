package com.turn.browser.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 	Array returns null value to handle serialization
 */
public class NullArrayJsonSerializer extends JsonSerializer<Object>{
	@Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
        	/**
    		 * 	If the array is null, fill in the empty array.
    		 */
            jgen.writeStartArray();
            jgen.writeEndArray();
        } else {
        	/**
    		 * 	Determine if the array is not empty and fill in the original value.
    		 */
            jgen.writeObject(value);
        }
    }
}
