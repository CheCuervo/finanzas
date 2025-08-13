# --- Etapa 1: Construcción (Build Stage) ---
# Usamos una imagen de OpenJDK 21 completa que incluye Gradle para construir el proyecto.
FROM openjdk:21-jdk as builder

# --- CORRECCIÓN ---
# La imagen base usa 'microdnf' como gestor de paquetes, no 'apt-get'.
# Instalamos 'procps-ng' que contiene la utilidad 'xargs'.
RUN microdnf install -y procps-ng

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

# Ejecutamos el comando de construcción en un solo paso.
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
