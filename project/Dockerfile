FROM openjdk:17-jdk-slim
WORKDIR /app
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 重点：Jar 在【上级目录】，用 ../ 跳出构建上下文
COPY ../shortlink-project-*.jar app.jar
EXPOSE 8001

ENTRYPOINT [\
    "java",\
    "-Xms128m",\
    "-Xmx256m",\
    "-XX:+UseG1GC",\
    "-XX:MaxGCPauseMillis=200",\
    "-XX:MaxMetaspaceSize=192m",\
    "-XX:MaxDirectMemorySize=128m",\
    "-Xss512k",\
    "-XX:+HeapDumpOnOutOfMemoryError",\
    "-XX:HeapDumpPath=/app/logs",\
    "-XX:+ExitOnOutOfMemoryError",\
    "-jar",\
    "app.jar",\
    "--spring.profiles.active=prod"\
]