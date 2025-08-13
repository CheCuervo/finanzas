# --- Etapa 1: Construcción (Build Stage) ---
# Usamos una imagen de OpenJDK 21 completa que incluye Gradle para construir el proyecto.
FROM openjdk:21-jdk as builder

# Establecemos el directorio de trabajo.
WORKDIR /app

# Damos permisos de ejecución al script de Gradle.
RUN chmod +x ./gradlew

# Copiamos los archivos de Gradle primero para aprovechar el cache de Docker.
# Si estos archivos no cambian, Docker no volverá a descargar las dependencias.
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Descargamos las dependencias. Este paso se cachea si build.gradle no cambia.
RUN ./gradlew dependencies --no-daemon

# Ahora copiamos el resto del código fuente. Si solo cambia el código,
# el paso anterior no se vuelve a ejecutar.
COPY src ./src

# Ejecutamos el comando para construir el proyecto, saltando las pruebas.
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
