package com.scm.spring.event;



import org.springframework.boot.context.properties.ConfigurationProperties;



@ConfigurationProperties(prefix = "scm.event")

public class ScmEventProperties {

    /** memory | kafka */

    private String transport = "memory";

    private String kafkaBootstrapServers = "localhost:9092";

    private String producer = "scm";



    public String getTransport() {

        return transport;

    }



    public void setTransport(String transport) {

        this.transport = transport;

    }



    public String getKafkaBootstrapServers() {

        return kafkaBootstrapServers;

    }



    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {

        this.kafkaBootstrapServers = kafkaBootstrapServers;

    }



    public String getProducer() {

        return producer;

    }



    public void setProducer(String producer) {

        this.producer = producer;

    }



    public boolean isKafka() {

        return "kafka".equalsIgnoreCase(transport);

    }

}

