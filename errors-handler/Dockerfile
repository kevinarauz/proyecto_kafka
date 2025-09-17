FROM openjdk:8-jdk-alpine

# Información del proyecto
LABEL maintainer="Global HITSS"
LABEL project="Transforma Ecuador"
LABEL client="Claro Ecuador"
LABEL component="Errors Handler"
LABEL topic="errors.events"

# Variables de entorno
ENV APP_NAME=errors-handler
ENV APP_VERSION=1.0.0
ENV MAVEN_OPTS="-Xmx512m -Xms256m"

# Crear directorio de trabajo
WORKDIR /app

# Copiar wrapper de Maven
COPY mvnw .
COPY .mvn .mvn

# Dar permisos de ejecución
RUN chmod +x mvnw

# Copiar archivos de dependencias primero (para cache)
COPY pom.xml .

# Descargar dependencias
RUN ./mvnw dependency:resolve

# Copiar código fuente
COPY src src

# Compilar aplicación
RUN ./mvnw clean package -DskipTests

# Punto de entrada
EXPOSE 8084

# Configuración de memoria JVM
ENV JAVA_OPTS="-Xmx256m -Xms128m"

# Ejecutar aplicación
CMD ["java", "-jar", "target/errors-handler-1.0.0.jar"]