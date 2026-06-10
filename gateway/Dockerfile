FROM openjdk:17-jdk-slim
WORKDIR /app
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY target/*.jar app.jar
EXPOSE 8000

ENTRYPOINT [\
    "java",\
    "-Xms64m",\
    "-Xmx128m",\
    "-XX:+UseG1GC",\
    "-XX:MaxGCPauseMillis=200",\
    "-XX:MaxMetaspaceSize=128m",\
    "-XX:MaxDirectMemorySize=96m",\
    "-Xss512k",\
    "-XX:+HeapDumpOnOutOfMemoryError",\
    "-XX:HeapDumpPath=/app/logs",\
    "-XX:+ExitOnOutOfMemoryError",\
    "-jar",\
    "app.jar",\
    "--spring.profiles.active=prod"\
]