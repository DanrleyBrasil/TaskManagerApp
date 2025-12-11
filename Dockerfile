# ==========================================================
# STAGE 1: BUILD - Compilação e Empacotamento
# ==========================================================
FROM maven:3.9-amazoncorretto-21 AS build

WORKDIR /app

# Copia apenas o pom.xml primeiro para aproveitar o cache de dependências do Docker
COPY pom.xml .
# Baixa as dependências (sem o código fonte ainda)
RUN mvn dependency:go-offline

# Copia o código fonte
COPY src ./src

# Compila o projeto e gera o .jar (pula testes aqui pois serão feitos no CI)
RUN mvn clean package -DskipTests

# ==========================================================
# STAGE 2: RUNTIME - Execução da Aplicação
# ==========================================================
FROM amazoncorretto:21-alpine

WORKDIR /app

# Cria um usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o .jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta 8080
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]