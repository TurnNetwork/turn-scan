# 基础镜像，jkd使用8版本
FROM openjdk:8
# 声明一个挂载点，容器内此路径会对应宿主机的某个文件夹
VOLUME /tmp
# 作者
MAINTAINER bubble <bubble@ql.com>
# 系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV FIRPATH=/opt
#ENV JAVA_OPTS="-server -XX:MetaspaceSize=256m -Xms2048m -Xmx2048m -Xmn1024m -Xss256k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
# 暴露端口
#EXPOSE 10010
# 工作目录
WORKDIR $FIRPATH
# 应用构建成功后的jar文件被复制到镜像内，名字也改成app.jar
ADD ./scan-agent-*.jar app.jar
ADD ./application-*.yml config/
ADD ./jasypt.properties jasypt.properties
# 启动容器时的进程
#ENTRYPOINT exec java -jar $JAVA_OPTS app.jar
ENTRYPOINT ["java","-jar","app.jar"]
# 构建镜像命令 [docker build -t usafe-facade:1.0.0 .]