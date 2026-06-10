FROM openjdk:17-jdk-slim
WORKDIR /app
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY ../shortlink-admin-*.jar app.jar
EXPOSE 8002

ENTRYPOINT [\
    "java",\
    "-Xms96m",\
    "-Xmx192m",\
    "-XX:+UseG1GC",\
    "-XX:MaxGCPauseMillis=200",\
    "-XX:MaxMetaspaceSize=128m",\
    "-XX:MaxDirectMemorySize=64m",\
    "-Xss512k",\
    "-XX:+HeapDumpOnOutOfMemoryError",\
    "-XX:HeapDumpPath=/app/logs",\
    "-XX:+ExitOnOutOfMemoryError",\
    "-jar",\
    "app.jar",\
    "--spring.profiles.active=prod"\
]