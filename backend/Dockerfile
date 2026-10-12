# 通用后端镜像：通过 --build-arg MODULE=xxx 指定要构建的微服务
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
ARG MODULE=system-service
COPY settings-docker.xml /root/.m2/settings.xml
COPY pom.xml .
COPY smart-common ./smart-common
COPY system-service ./system-service
COPY wms-service ./wms-service
COPY mes-service ./mes-service
COPY qms-service ./qms-service
COPY gateway ./gateway
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -q clean package -pl ${MODULE} -am -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE=system-service
COPY --from=build /build/${MODULE}/target/${MODULE}.jar /app/app.jar
ENV TZ=Asia/Shanghai
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
