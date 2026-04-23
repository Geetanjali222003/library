FROM eclipse-temurin:21-jdk

WORKDIR /App

COPY target/hello.jar app.jar

# Container's port
EXPOSE 8081

# What to run
CMD ["java", "-jar", "app.jar"]