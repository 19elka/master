FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY ../build.gradle.kts ./
COPY ../gradlew ./
COPY ../gradlew.bat ./
COPY ../gradle gradle
COPY ../settings.gradle ./
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -D spring
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown -R spring:spring /app
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]