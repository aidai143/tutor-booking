# Stage 1: build the jar from sources
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Stage 2: run it — only the JRE and the jar, no Maven, no sources
FROM eclipse-temurin:21-jre
# Lesson times are local Bishkek time; without this the container uses UTC (6 hours off)
ENV TZ=Asia/Bishkek
RUN useradd --system --no-create-home app
USER app
WORKDIR /app
COPY --from=build /src/target/tutor-booking-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
