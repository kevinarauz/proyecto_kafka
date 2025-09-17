package com.claro.ecuador.errors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal para Errors Handler
 * Microservicio específico para topic: errors.events (Dead Letter Queue)
 *
 * @author Global HITSS
 * @project Transforma Ecuador
 * @client Claro Ecuador
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class ErrorsHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErrorsHandlerApplication.class, args);
    }
}