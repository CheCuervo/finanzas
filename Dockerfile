# 1. Usar una imagen base oficial de Java 21.
# 'slim' es una versión más ligera, ideal para producción.
FROM openjdk:21-jdk-slim

# 2. Establecer el directorio de trabajo dentro del contenedor.
# Todos los comandos siguientes se ejecutarán desde esta carpeta.
WORKDIR /app

# 3. Copiar el archivo JAR compilado al contenedor.
# Gradle construye el JAR en la carpeta 'build/libs/'. El '*' se usa
# para que coincida con el nombre del archivo JAR sin importar la versión.
COPY build/libs/*.jar app.jar

# 4. Exponer el puerto en el que corre tu aplicación.
# Spring Boot usa el puerto 8080 por defecto.
EXPOSE 8080

# 5. El comando para ejecutar tu aplicación cuando el contenedor inicie.
ENTRYPOINT ["java","-jar","app.jar"]
