package com.flowops.bootstrap.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.math.BigInteger;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Serialize Long and BigInteger as String in JSON to prevent JavaScript precision loss
        SimpleModule stringIdModule = new SimpleModule("StringIdModule");
        stringIdModule.addSerializer(Long.class, ToStringSerializer.instance);
        stringIdModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        stringIdModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        mapper.registerModule(stringIdModule);

        return mapper;
    }
}
