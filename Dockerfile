FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY src src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system hr && useradd --system --gid hr hr
COPY --from=build /build/target/hr-system-0.0.1-SNAPSHOT.jar app.jar
USER hr
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
