#BUILDER

FROM eclipse-temurin:21-jdk-jammy as builder

#Cria e define o diretório de tabalho 
WORKDIR /app

# Copia os arquivos de definição do projeto, acelerando builds futuros
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Executa o comando para baixar as dependencias do projeto
RUN ./mvnw dependency:go-offline

# Copia o código fonte
COPY src ./src

# A flag -DskipTests pula a execução dos testes para agilizar o build da imagem.
# Isso gera o arquivo .jar final em /app/target/
RUN ./mvnw package -DskipTests 

#_____
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

#Copia somente o arquivo ..jar compilado do estágio builder
COPY --from=builder /app/target/*.jar app.jar

#Informa a porta que o container escutará
EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar"]