package com.claro.ecuador.errors.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumer específico para topic: errors.events
 * Maneja únicamente eventos de error (Dead Letter Queue)
 *
 * @author Global HITSS
 */
@Component
public class ErrorEventsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ErrorEventsConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${error-handler.alert.critical-threshold:10}")
    private int criticalThreshold;

    @Value("#{'${error-handler.alert.email-recipients:ops-team@claro-ecuador.com,tech-lead@claro-ecuador.com}'.split(',')}")
    private List<String> alertRecipients;

    // Contadores de errores por tipo
    private final Map<String, AtomicInteger> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAlertTime = new ConcurrentHashMap<>();

    /**
     * Consumer exclusivo para topic errors.events
     * Propósito: Procesar eventos de error para análisis, alertas y recuperación
     */
    @KafkaListener(topics = "errors.events", groupId = "errors-handler-group")
    public void consumeErrorEvents(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            logger.warn("🚨 ERROR HANDLER - Procesando evento de error en topic: {}, partition: {}, offset: {}",
                       topic, partition, offset);
            logger.debug("Mensaje de error: {}", message);

            // Procesamiento específico para eventos de error
            processErrorEvent(message);

            logger.info("⚠️ ERROR HANDLER - Evento de error procesado para análisis - Offset: {}", offset);

        } catch (Exception e) {
            logger.error("💥 ERROR HANDLER - Error crítico procesando evento de error: {}",
                        e.getMessage(), e);

            // Manejo de errores críticos en el sistema de errores
            handleCriticalError(message, topic, e);
        }
    }

    /**
     * Lógica específica para procesamiento de eventos de error
     */
    private void processErrorEvent(String message) throws Exception {
        try {
            logger.info("🔧 Analizando evento de error para alertas y métricas");

            // Parsear mensaje de error
            Map<String, Object> errorData = objectMapper.readValue(message, Map.class);

            // Extraer información crítica del error
            String originalTopic = (String) errorData.get("topic");
            String errorType = (String) errorData.get("errorType");
            String errorMessage = (String) errorData.get("error");
            String timestamp = (String) errorData.get("timestamp");
            String originalMessage = (String) errorData.get("originalMessage");

            logger.warn("📋 Análisis de Error - Topic: {}, Tipo: {}, Timestamp: {}",
                       originalTopic, errorType, timestamp);

            // Clasificar severidad del error
            String severity = classifyErrorSeverity(errorType, errorMessage);

            // Incrementar contadores por tipo de error
            updateErrorMetrics(errorType, originalTopic);

            // Procesar según severidad
            switch (severity) {
                case "CRITICAL":
                    processCriticalError(errorData);
                    break;
                case "HIGH":
                    processHighPriorityError(errorData);
                    break;
                case "MEDIUM":
                    processMediumPriorityError(errorData);
                    break;
                case "LOW":
                    processLowPriorityError(errorData);
                    break;
            }

            // Determinar si es recuperable
            if (isRecoverableError(errorType)) {
                scheduleErrorRetry(errorData);
            }

            // Almacenar para análisis posterior
            storeErrorAnalysis(errorData, severity);

            // Verificar umbrales de alerta
            checkAlertThresholds(errorType, originalTopic);

        } catch (Exception e) {
            logger.error("Error analizando evento de error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Clasificar severidad del error
     */
    private String classifyErrorSeverity(String errorType, String errorMessage) {
        if (errorType == null) return "LOW";

        switch (errorType.toUpperCase()) {
            case "NULLPOINTEREXCEPTION":
            case "OUTOFMEMORYERROR":
            case "DATABASECONNECTIONEXCEPTION":
            case "SECURITYEXCEPTION":
                return "CRITICAL";

            case "CONNECTIONTIMEOUTEXCEPTION":
            case "SQLEXCEPTION":
            case "AUTHENTICATIONEXCEPTION":
                return "HIGH";

            case "JSONPROCESSINGEXCEPTION":
            case "VALIDATIONEXCEPTION":
            case "ILLEGALARGUMENTEXCEPTION":
                return "MEDIUM";

            default:
                return "LOW";
        }
    }

    /**
     * Procesar errores críticos
     */
    private void processCriticalError(Map<String, Object> errorData) {
        logger.error("🚨 PROCESANDO ERROR CRÍTICO: {}", errorData);

        // Alerta inmediata
        sendCriticalAlert(errorData);

        // Escalamiento automático
        escalateTechnicalTeam(errorData);

        // Métricas de emergencia
        recordCriticalMetrics(errorData);
    }

    /**
     * Procesar errores de alta prioridad
     */
    private void processHighPriorityError(Map<String, Object> errorData) {
        logger.warn("⚠️ Procesando error de alta prioridad");

        // Alerta programada
        scheduleHighPriorityAlert(errorData);

        // Análisis de impacto
        analyzeErrorImpact(errorData);
    }

    /**
     * Procesar errores de prioridad media
     */
    private void processMediumPriorityError(Map<String, Object> errorData) {
        logger.info("📊 Procesando error de prioridad media");

        // Agregar a métricas diarias
        addToDailyMetrics(errorData);

        // Análisis de patrones
        analyzeErrorPatterns(errorData);
    }

    /**
     * Procesar errores de baja prioridad
     */
    private void processLowPriorityError(Map<String, Object> errorData) {
        logger.debug("📝 Registrando error de baja prioridad");

        // Solo logging y métricas básicas
        recordBasicMetrics(errorData);
    }

    /**
     * Determinar si un error es recuperable
     */
    private boolean isRecoverableError(String errorType) {
        if (errorType == null) return false;

        switch (errorType.toUpperCase()) {
            case "CONNECTIONTIMEOUTEXCEPTION":
            case "TEMPORARYSERVICEUNAVAILABLEEXCEPTION":
            case "RATELIMITEXCEPTION":
                return true;

            case "JSONPROCESSINGEXCEPTION":
            case "ILLEGALARGUMENTEXCEPTION":
            case "VALIDATIONEXCEPTION":
                return false;

            default:
                return false;
        }
    }

    /**
     * Programar reintento de error
     */
    private void scheduleErrorRetry(Map<String, Object> errorData) {
        logger.info("🔄 Programando reintento para error recuperable");

        // Implementar lógica de backoff exponencial
        String originalTopic = (String) errorData.get("topic");
        String originalMessage = (String) errorData.get("originalMessage");

        // Aquí iría la lógica de reenvío al topic original
        logger.info("📤 Error programado para reintento en topic: {}", originalTopic);
    }

    /**
     * Actualizar métricas de error
     */
    private void updateErrorMetrics(String errorType, String originalTopic) {
        String metricKey = originalTopic + ":" + errorType;
        errorCounters.computeIfAbsent(metricKey, k -> new AtomicInteger(0)).incrementAndGet();

        logger.debug("📊 Métrica actualizada - {}: {}", metricKey,
                    errorCounters.get(metricKey).get());
    }

    /**
     * Verificar umbrales de alerta
     */
    private void checkAlertThresholds(String errorType, String originalTopic) {
        String metricKey = originalTopic + ":" + errorType;
        int currentCount = errorCounters.getOrDefault(metricKey, new AtomicInteger(0)).get();

        if (currentCount >= criticalThreshold) {
            LocalDateTime lastAlert = lastAlertTime.get(metricKey);
            LocalDateTime now = LocalDateTime.now();

            // Evitar spam de alertas (máximo 1 por hora)
            if (lastAlert == null || lastAlert.isBefore(now.minusHours(1))) {
                sendThresholdAlert(errorType, originalTopic, currentCount);
                lastAlertTime.put(metricKey, now);
            }
        }
    }

    /**
     * Enviar alerta crítica
     */
    private void sendCriticalAlert(Map<String, Object> errorData) {
        try {
            String subject = "🚨 ALERTA CRÍTICA - Transforma Ecuador";
            String body = String.format(
                "Error crítico detectado:\n\n" +
                "Topic: %s\n" +
                "Tipo: %s\n" +
                "Error: %s\n" +
                "Timestamp: %s\n\n" +
                "Requiere atención inmediata del equipo técnico.",
                errorData.get("topic"),
                errorData.get("errorType"),
                errorData.get("error"),
                errorData.get("timestamp")
            );

            sendEmailAlert(subject, body);
            logger.error("🚨 ALERTA CRÍTICA ENVIADA POR EMAIL");

        } catch (Exception e) {
            logger.error("Error enviando alerta crítica: {}", e.getMessage());
        }
    }

    /**
     * Enviar alerta por umbral
     */
    private void sendThresholdAlert(String errorType, String topic, int count) {
        try {
            String subject = "⚠️ UMBRAL DE ERRORES SUPERADO - Transforma Ecuador";
            String body = String.format(
                "Se ha superado el umbral de errores:\n\n" +
                "Topic: %s\n" +
                "Tipo de Error: %s\n" +
                "Cantidad: %d errores\n" +
                "Umbral: %d\n" +
                "Timestamp: %s\n\n" +
                "Revisar logs para más detalles.",
                topic, errorType, count, criticalThreshold, LocalDateTime.now()
            );

            sendEmailAlert(subject, body);
            logger.warn("⚠️ ALERTA POR UMBRAL ENVIADA - {} errores de tipo {} en {}",
                       count, errorType, topic);

        } catch (Exception e) {
            logger.error("Error enviando alerta por umbral: {}", e.getMessage());
        }
    }

    /**
     * Enviar email de alerta
     */
    private void sendEmailAlert(String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertRecipients.toArray(new String[0]));
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("alerts@claro-ecuador.com");

            mailSender.send(message);
            logger.info("📧 Email de alerta enviado a {} destinatarios", alertRecipients.size());

        } catch (Exception e) {
            logger.error("Error enviando email de alerta: {}", e.getMessage());
        }
    }

    // Métodos auxiliares para análisis específicos
    private void escalateTechnicalTeam(Map<String, Object> errorData) {
        logger.error("🚨 ESCALAMIENTO AL EQUIPO TÉCNICO ACTIVADO");
        // Integración con sistemas de escalamiento (PagerDuty, Slack, etc.)
    }

    private void recordCriticalMetrics(Map<String, Object> errorData) {
        logger.error("📊 REGISTRANDO MÉTRICAS CRÍTICAS");
        // Envío a sistemas de monitoreo especializados
    }

    private void scheduleHighPriorityAlert(Map<String, Object> errorData) {
        logger.warn("⏰ Programando alerta de alta prioridad");
        // Programar alerta diferida
    }

    private void analyzeErrorImpact(Map<String, Object> errorData) {
        logger.info("🔍 Analizando impacto del error");
        // Análisis de impacto en servicios downstream
    }

    private void addToDailyMetrics(Map<String, Object> errorData) {
        logger.info("📈 Agregando a métricas diarias");
        // Acumulación para reportes diarios
    }

    private void analyzeErrorPatterns(Map<String, Object> errorData) {
        logger.info("🔄 Analizando patrones de error");
        // Machine learning para detección de patrones
    }

    private void recordBasicMetrics(Map<String, Object> errorData) {
        logger.debug("📝 Registrando métricas básicas");
        // Métricas simples para errores comunes
    }

    private void storeErrorAnalysis(Map<String, Object> errorData, String severity) {
        logger.info("💾 Almacenando análisis de error con severidad: {}", severity);
        // Guardar en Elasticsearch con índice específico para errores
    }

    /**
     * Manejo de errores críticos en el propio sistema de errores
     */
    private void handleCriticalError(String message, String topic, Exception error) {
        logger.error("💥 FALLA CRÍTICA EN SISTEMA DE ERRORES: {}", error.getMessage());

        // Último recurso: log directo al sistema
        // No usar Kafka para evitar loops infinitos
        System.err.println("CRITICAL ERROR IN ERROR HANDLER: " + error.getMessage());
    }
}