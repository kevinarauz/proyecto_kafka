# Usar OpenJDK 8 como base (compatible con Java 1.8)
FROM openjdk:8-jdk

# Metadata del contenedor
LABEL maintainer="Global HITSS <arauzk@hitss.com>"
LABEL project="Transforma Ecuador"
LABEL client="Claro Ecuador"
LABEL version="1.0.0"

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargar dependencias (para optimizar capas Docker)
RUN MAVEN_OPTS="-Xmx1024m -Xms512m" ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar la aplicación con más memoria
RUN MAVEN_OPTS="-Xmx1024m -Xms512m" ./mvnw clean package -DskipTests

# Exponer puerto de la aplicación
EXPOSE 8082

# Configurar variables de entorno para Kafka
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENV KAFKA_GROUP_ID=microservicio-eventos-group

# Configurar variables de entorno para base de datos
ENV SPRING_DATASOURCE_URL=jdbc:h2:mem:transforma_ecuador
ENV SPRING_DATASOURCE_USERNAME=sa
ENV SPRING_DATASOURCE_PASSWORD=password

# Variables de entorno para logging
ENV LOGGING_LEVEL_ROOT=INFO
ENV LOGGING_LEVEL_COM_CLARO=DEBUG

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "-Xmx512m", "-Xms256m", "target/transforma-eventos-1.0.0.jar"]