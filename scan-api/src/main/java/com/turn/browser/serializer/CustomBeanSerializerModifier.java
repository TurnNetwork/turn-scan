package com.turn.browser.serializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;
import java.util.Set;

/**
 * json serialization editor
 */
public class CustomBeanSerializerModifier extends BeanSerializerModifier {
	/**
	 * 	Declare a global empty array serialization object
	 */
	private JsonSerializer<Object> nullArrayJsonSerializer = new NullArrayJsonSerializer();
	/**
	 * 	Declare a global empty String serialization object
	 */
    private JsonSerializer<Object> nullStringJsonSerializer = new NullStringJsonSerializer();
    /**
	 * 	Declare a global empty String serialization object
	 */
    private JsonSerializer<Object> nullIntegerJsonSerializer = new NullIntegerJsonSerializer();

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        /**
         * 	 Loop through all beanPropertyWriters
         */
        for (int i = 0; i < beanProperties.size(); i++) {
            BeanPropertyWriter writer = beanProperties.get(i);
            /**
             * 	 判Determine the type of field. If it is array, list, or set, register nullSerializer.
             */
            if (isArrayType(writer)) {
                /**
                 * 	Register a nullSerializer of your own for the writer
                 */
                writer.assignNullSerializer(this.defaultNullArrayJsonSerializer());
            }
            /**
             * 	 Determine the type of field, if it is String, register nullSerializer
             */
            if (isStringType(writer)) {
                writer.assignNullSerializer(this.defaultNullStringJsonSerializer());
            }
            /**
             * 	 Determine the type of field, if it is a number, register nullSerializer
             */
            if (isNumberType(writer)) {
                writer.assignNullSerializer(this.defaultNullIntegerJsonSerializer());
            }
        }
        return beanProperties;
    }

    /**
     * 	 Determine whether it is an array type
     */
    protected boolean isArrayType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.isArray() || clazz.equals(List.class) || clazz.equals(Set.class);
    }

    /**
     * 	Determine String type
     * @method isStringType
     */
    protected boolean isStringType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(String.class);
    }

    /**
     * 	Determine number type
     * @method isNumberType
     */
    protected boolean isNumberType(BeanPropertyWriter writer) {
		Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(Integer.class) || clazz.equals(int.class)|| clazz.equals(Long.class)
        		|| clazz.equals(long.class)|| clazz.equals(Double.class)|| clazz.equals(double.class);
    }

    protected JsonSerializer<Object> defaultNullArrayJsonSerializer() {
        return nullArrayJsonSerializer;
    }

    protected JsonSerializer<Object> defaultNullStringJsonSerializer() {
        return nullStringJsonSerializer;
    }

    protected JsonSerializer<Object> defaultNullIntegerJsonSerializer() {
        return nullIntegerJsonSerializer;
    }

}
