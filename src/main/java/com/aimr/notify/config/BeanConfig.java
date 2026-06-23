package com.aimr.notify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.aimr.notify.constant.ApplicationConstants.*;

@Configuration(proxyBeanMethods = false)
public class BeanConfig {

    @Bean("jsonMapper")
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    @Bean(name = "emailVirtualThreadExecutor")
    public Executor emailWorkerExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new Argon2PasswordEncoder(PASSWORD_ENCODER_SALT_LENGTH, PASSWORD_ENCODER_HASH_LENGTH,
                PASSWORD_ENCODER_PARALLELISM, PASSWORD_ENCODER_MEMORY_SIZE, PASSWORD_ENCODER_ITERATION_COUNT);
    }
}
