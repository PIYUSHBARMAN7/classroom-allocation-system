# Build the React frontend
FROM node:24-bookworm-slim AS frontend-build

WORKDIR /frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build


# Build Spring Boot and include the frontend
FROM maven:3.9-eclipse-temurin-21 AS backend-build

WORKDIR /backend

COPY backend/pom.xml ./
COPY backend/src ./src

COPY --from=frontend-build /frontend/dist ./src/main/resources/static

RUN mvn -B --no-transfer-progress -DskipTests package \
    && cp target/*.jar /backend/app.jar


# Run the application
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY --from=backend-build --chown=10001:10001 /backend/app.jar ./app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=55.0 -XX:+ExitOnOutOfMemoryError -XX:ActiveProcessorCount=2"
ENV TZ=Asia/Kolkata

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]