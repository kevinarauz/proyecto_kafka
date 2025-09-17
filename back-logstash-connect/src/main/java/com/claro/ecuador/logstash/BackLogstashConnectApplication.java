package com.claro.ecuador.logstash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Aplicación principal para Back Logstash Connect
 * Microservicio específico para topic: back-logstash-connect
 *
 * @author Global HITSS
 * @project Transforma Ecuador
 * @client Claro Ecuador
 */
@SpringBootApplication
@EnableKafka
public class BackLogstashConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackLogstashConnectApplication.class, args);
    }
}