package com.claro.ecuador.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Aplicación principal para Events Processor
 * Microservicio específico para topic: Events
 *
 * @author Global HITSS
 * @project Transforma Ecuador
 * @client Claro Ecuador
 */
@SpringBootApplication
@EnableKafka
public class EventsProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventsProcessorApplication.class, args);
    }
}