FROM default-route-openshift-image-registry.apps-crc.testing/tools-images/openjdk18-openshift:1.8

MAINTAINER kitty-catt <kitty-catt@example.com>

LABEL io.openshift.expose-services="8080/http"

ENV MYDIR="/home/jboss/app"

RUN mkdir -pv ${MYDIR} 

COPY generic-nexus-download.sh ${MYDIR}/generic-nexus-download.sh

RUN ${MYDIR}/generic-nexus-download.sh

RUN chown -R 1001:0 ${MYDIR} && \
    chmod -R g=u ${MYDIR} && \
    chgrp -R 0 ${MYDIR}

USER 1001
EXPOSE 8080

CMD ["java -jar /home/jboss/app/app.jar"]