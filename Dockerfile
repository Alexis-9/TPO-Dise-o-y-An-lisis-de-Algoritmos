# Etapa 1: compila el proyecto con Maven y junta las dependencias
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q package dependency:copy-dependencies -DincludeScope=runtime -DskipTests

# Etapa 2: imagen liviana que solo corre el servidor
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./lib
EXPOSE 7070
CMD ["java", "-cp", "classes:lib/*", "Main"]
