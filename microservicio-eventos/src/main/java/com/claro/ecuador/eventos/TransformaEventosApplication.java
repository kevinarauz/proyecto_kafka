package com.claro.ecuador.eventos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Aplicación principal del microservicio Transforma Ecuador - Eventos
 *
 * @author Global HITSS
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
public class TransformaEventosApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransformaEventosApplication.class, args);
    }
}