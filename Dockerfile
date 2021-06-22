## Builder Image MVN - descobrir como fazer com gradle
#FROM maven:3.8.1-openjdk-11-slim AS builder
#COPY src /usr/src/app/src
#COPY pom.xml /usr/src/app
#RUN mvn -f /usr/src/app/pom.xml clean package

## Runner Image
# openjdk:11.0.11-jre-slim
FROM adoptopenjdk/openjdk11:jre11u-alpine-nightly
RUN addgroup micronauta
RUN adduser -DH micronaut -G micronauta
USER micronaut:micronauta
ARG JAR_FILE=/build/libs/*all.jar
COPY ${JAR_FILE} /usr/app/app.jar
#COPY --from=builder /usr/src/app/build/libs/*all.jar /usr/app/app.jar
ENTRYPOINT ["java","-Xmx512m","-Dfile.encoding=UTF-8","-jar","/usr/app/app.jar"]