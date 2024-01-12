#Basic image, jkd uses version 8
FROM openjdk:8
# Declare a mount point. This path in the container will correspond to a folder on the host machine.
VOLUME /tmp
# author
MAINTAINER bubble <bubble@ql.com>
# system encoding
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV FIRPATH=/opt
#ENV JAVA_OPTS="-server -XX:MetaspaceSize=256m -Xms2048m -Xmx2048m -Xmn1024m -Xss256k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
#Expose port
#EXPOSE10010
# Work list
WORKDIR $FIRPATH
# After the application is successfully built, the jar file is copied to the image and its name is changed to app.jar
ADD ./scan-job-*.jar app.jar
ADD ./application-*.yml config/
ADD ./jasypt.properties jasypt.properties
# Process when starting the container
#ENTRYPOINT exec java -jar $JAVA_OPTS app.jar
ENTRYPOINT ["java","-jar","app.jar"]
# Build image command [docker build -t usafe-facade:1.0.0 .]