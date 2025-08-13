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

# Damos permisos de ejecución al script de Gradle.
RUN chmod +x ./gradlew

# Copiamos el resto del código fuente de la aplicación.
COPY src ./src

# --- CORRECCIÓN ---
# Ejecutamos el comando para construir el proyecto y crear el archivo JAR.
# Se elimina el paso 'dependencies' y se añade '--stacktrace' para obtener logs de error detallados.
RUN ./gradlew build --no-daemon --stacktrace


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
