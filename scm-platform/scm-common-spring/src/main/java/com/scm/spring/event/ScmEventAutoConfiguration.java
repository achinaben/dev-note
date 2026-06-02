package com.scm.spring.event;



import org.apache.kafka.clients.consumer.ConsumerConfig;

import org.apache.kafka.clients.producer.ProducerConfig;

import org.apache.kafka.common.serialization.StringDeserializer;

import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import org.springframework.kafka.core.ConsumerFactory;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.core.ProducerFactory;



import java.util.HashMap;

import java.util.Map;



@Configuration
@EnableKafka
@EnableConfigurationProperties(ScmEventProperties.class)
public class ScmEventAutoConfiguration {



    @Bean

    EventDispatcher eventDispatcher() {

        return new EventDispatcher();

    }



    @Bean

    @ConditionalOnProperty(name = "scm.event.transport", havingValue = "kafka")

    ProducerFactory<String, String> scmKafkaProducerFactory(ScmEventProperties props) {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafkaBootstrapServers());

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);

    }



    @Bean

    @ConditionalOnProperty(name = "scm.event.transport", havingValue = "kafka")

    KafkaTemplate<String, String> scmKafkaTemplate(ProducerFactory<String, String> factory) {

        return new KafkaTemplate<>(factory);

    }



    @Bean

    @ConditionalOnProperty(name = "scm.event.transport", havingValue = "kafka")

    ConsumerFactory<String, String> scmKafkaConsumerFactory(ScmEventProperties props) {

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafkaBootstrapServers());

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(config);

    }



    @Bean

    @ConditionalOnProperty(name = "scm.event.transport", havingValue = "kafka")

    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(

            ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =

                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;

    }



    @Bean

    @ConditionalOnProperty(name = "scm.event.transport", havingValue = "kafka")

    KafkaScmEventListener kafkaScmEventListener(EventDispatcher dispatcher) {

        return new KafkaScmEventListener(dispatcher);

    }



    @Bean

    @ConditionalOnProperty(name = "scm.event.transport", havingValue = "kafka")

    ScmEventPublisher kafkaScmEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {

        return new KafkaScmEventPublisher(kafkaTemplate);

    }



    @Bean

    @ConditionalOnProperty(name = "scm.event.transport", havingValue = "memory", matchIfMissing = true)

    ScmEventPublisher memoryScmEventPublisher(EventDispatcher dispatcher) {

        return new MemoryScmEventPublisher(dispatcher);

    }

}

