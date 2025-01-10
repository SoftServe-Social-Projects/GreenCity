FROM openjdk:22-ea-21-jdk-slim as runner
WORKDIR runner
COPY **/target/app.jar runner/
CMD java -jar runner/app.jar 
