# syntax=docker/dockerfile:1

# =====================================================================
# Estagio 1 - BUILD: compila e testa com Maven + JDK 21
# =====================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# O pom e copiado primeiro para o cache de dependencias sobreviver a mudancas no codigo
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package

# =====================================================================
# Estagio 2 - RUNTIME: apenas o JRE e o jar (imagem bem menor)
# =====================================================================
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# A aplicacao nao roda como root
RUN groupadd --system api && useradd --system --gid api --home /app api

COPY --from=build /build/target/sistema-chamados-*.jar app.jar
RUN chown -R api:api /app

USER api
EXPOSE 8080

# Ajuste de memoria por porcentagem do limite do container
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=America/Fortaleza"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
