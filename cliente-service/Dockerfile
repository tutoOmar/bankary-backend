FROM gradle:8.4-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
