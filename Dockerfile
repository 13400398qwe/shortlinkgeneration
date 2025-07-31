# 1. 选择一个包含 Java 运行环境的基础镜像
# 我们选择一个轻量级的 OpenJDK 21 版本
FROM openjdk:21-jdk-slim

# 2. 设置容器内的工作目录
WORKDIR /app

# 3. 将我们打包好的 jar 文件复制到容器中，并重命名为 app.jar
# 注意：请将下面的 jar 文件名替换为您 target 目录下的实际文件名
COPY target/scupsychological-0.0.1-SNAPSHOT.jar app.jar

# 4. 声明应用将要监听的端口
EXPOSE 8080

# 5. 设置容器启动时要执行的命令
# ["java", "-jar", "app.jar"] 是在容器内运行 `java -jar app.jar`
ENTRYPOINT ["java","-jar","app.jar"]