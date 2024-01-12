package com.turn.browser.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Declare the json configuration file and process the returned null value uniformly.
 */
@Configuration
public class ClassJsonConfiguration {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {

    	/**
    	 * Declare converter to obtain mapper
    	 */
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        ObjectMapper mapper = converter.getObjectMapper();

        /**
         * 	 Register a Factory with SerializerModifier for mapper. The main thing this modifier does is: determine the serialization type and determine the value when the type is specified as null.
         */
        mapper.setSerializerFactory(mapper.getSerializerFactory().withSerializerModifier(new CustomBeanSerializerModifier()));

        return converter;
    }

}
