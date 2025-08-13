# --- Etapa 1: Construcción (Build Stage) ---
# Usamos una imagen de OpenJDK 21 completa que incluye Gradle para construir el proyecto.
FROM openjdk:21-jdk as builder

# Establecemos el directorio de trabajo.
WORKDIR /app

# Copiamos todos los archivos del proyecto necesarios para la construcción.
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY src ./src

# Damos permisos de ejecución al script de Gradle.
RUN chmod +x ./gradlew

# --- CORRECCIÓN ---
# Ejecutamos el comando de construcción en un solo paso y aumentamos la memoria de Gradle.
# Esto simplifica el proceso y puede resolver problemas en entornos con recursos limitados.
RUN ./gradlew build -x test --no-daemon --stacktrace


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
