FROM gradle:8.10.0-jdk21-alpine AS builder
WORKDIR /app
COPY . /app
RUN ./gradlew test jar

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/ip-counter-0.1.0.jar /app/ip-counter.jar
ENTRYPOINT ["java", "-jar", "/app/ip-counter.jar"]
CMD ["--help"]