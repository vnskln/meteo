FROM eclipse-temurin:25.0.4.1_1-jdk-noble AS build
WORKDIR /build
COPY pom.xml mvnw ./
COPY .mvn/ ./.mvn/
RUN ./mvnw -B dependency:go-offline
COPY src/ ./src/
RUN ./mvnw -B -DskipTests package && cp target/*.jar app.jar

FROM eclipse-temurin:25.0.4.1_1-jre-noble AS runtime
WORKDIR /app
COPY --from=build /build/app.jar ./
USER 1001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]