package com.aimr.notify.config;

import com.aimr.notify.models.dto.ChannelDispatchDTO;
import com.aimr.notify.models.dto.IngestTopicDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final ApplicationProperties properties;
    private final JsonMapper jsonMapper;

 //************************PRODUCER CONFIG************************************
    @Bean
    public ProducerFactory<@NonNull String, @NonNull String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapKafkaServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate(
            ProducerFactory<@NonNull String, @NonNull String> stringProducerFactory) {
        return new KafkaTemplate<>(stringProducerFactory);
    }

    @Bean
    public ProducerFactory<@NonNull String, @NonNull ChannelDispatchDTO> channelDispatchProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapKafkaServers());
        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                new JacksonJsonSerializer<@NonNull ChannelDispatchDTO>(jsonMapper)
        );
    }

    @Bean
    public KafkaTemplate<@NonNull String, @NonNull ChannelDispatchDTO> channelDispatchKafkaTemplate(
            ProducerFactory<@NonNull String, @NonNull ChannelDispatchDTO> channelDispatchProducerFactory) {
        return new KafkaTemplate<>(channelDispatchProducerFactory);
    }

    //************************CONSUMER CONFIG************************************
    @Bean
    public ConsumerFactory<@NonNull String, ChannelDispatchDTO> channelDispatchConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapKafkaServers());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);  // fetch max 50 per poll
        
        JacksonJsonDeserializer<@NonNull ChannelDispatchDTO> jsonDeserializer =
                new JacksonJsonDeserializer<>(ChannelDispatchDTO.class, jsonMapper);
        jsonDeserializer.addTrustedPackages("*");
        
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull ChannelDispatchDTO> emailListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull ChannelDispatchDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(channelDispatchConsumerFactory());
        factory.setBatchListener(true);   // enable batch mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConsumerFactory<@NonNull String, IngestTopicDTO> ingestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapKafkaServers());
        
        JacksonJsonDeserializer<@NonNull IngestTopicDTO> jsonDeserializer =
                new JacksonJsonDeserializer<>(IngestTopicDTO.class, jsonMapper);
        jsonDeserializer.addTrustedPackages("*");
        
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull IngestTopicDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull IngestTopicDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ingestConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
