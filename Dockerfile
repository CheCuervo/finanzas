# --- Etapa 1: Construcción (Build Stage) ---
# Usamos una imagen de OpenJDK 21 completa que incluye Gradle para construir el proyecto.
FROM openjdk:21-jdk as builder

# Establecemos el directorio de trabajo.
WORKDIR /app

# Copiamos los archivos de Gradle para descargar las dependencias.
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# --- CORRECCIÓN ---
# Damos permisos de ejecución al script de Gradle.
RUN chmod +x ./gradlew

# Descargamos las dependencias de Gradle. Esto se cachea para acelerar builds futuros.
RUN ./gradlew dependencies

# Copiamos el resto del código fuente de la aplicación.
COPY src ./src

# Ejecutamos el comando para construir el proyecto y crear el archivo JAR.
# El --no-daemon es importante para entornos de CI/CD como Render.
RUN ./gradlew build --no-daemon


# --- Etapa 2: Ejecución (Runtime Stage) ---
# Usamos una imagen 'slim' mucho más ligera para la ejecución, lo que es más eficiente.
FROM openjdk:21-jdk-slim

# Establecemos el directorio de trabajo.
WORKDIR /app

# Copiamos únicamente el archivo JAR desde la etapa de construcción ('builder').
COPY --from=builder /app/build/libs/*.jar app.jar

# Exponemos el puerto 8080.
EXPOSE 8080

# El comando para ejecutar la aplicación.
ENTRYPOINT ["java","-jar","app.jar"]
