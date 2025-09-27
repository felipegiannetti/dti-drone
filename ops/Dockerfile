# Multi-stage build para otimizar o tamanho da imagem
FROM maven:3.9-eclipse-temurin-21 AS builder

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos do Maven
COPY pom.xml .
COPY src ./src

# Compilar a aplicação
RUN mvn clean package -DskipTests

# Estágio final - imagem runtime
FROM eclipse-temurin:21-jre-alpine

# Instalar dependências necessárias
RUN apk add --no-cache curl

# Criar usuário não-root
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser

# Definir diretório de trabalho
WORKDIR /app

# Copiar o JAR da aplicação do estágio builder
COPY --from=builder /app/target/*.jar app.jar

# Mudar proprietário dos arquivos
RUN chown -R appuser:appuser /app

# Mudar para usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]