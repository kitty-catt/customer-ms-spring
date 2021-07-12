# Build on centos unless our pipeline overrides the FROM image.
FROM  centos:7

MAINTAINER kitty-catt <kitty-catt@example.com>

LABEL io.openshift.expose-services="8080/http"

# command line options to pass to the JVM
ENV	  JAVA_OPTIONS -Xmx512m

# Install the Java runtime, create a user for running the app, and set permissions
RUN   yum install -y --disableplugin=subscription-manager java-1.8.0-openjdk-headless && \
      yum clean all --disableplugin=subscription-manager -y && \
      mkdir -p /opt/app-root/bin

WORKDIR /opt/app-root/bin

#COPY generic-nexus-download.sh /opt/app-root/bin/generic-nexus-download.sh
#RUN /opt/app-root/bin/generic-nexus-download.sh

COPY target/customer-ms-spring-0.1-SNAPSHOT.jar app.jar

RUN   chown -R root:0 /opt/app-root && \
      chmod -R g=u /opt/app-root &&\
      chgrp -R 0 /opt/app-root

#USER root
EXPOSE 8080

#ENTRYPOINT ["java"]
CMD ["java","-jar","/opt/app-root/bin/app.jar"]