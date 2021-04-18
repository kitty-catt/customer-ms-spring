FROM default-route-openshift-image-registry.apps-crc.testing/tools-images/openjdk18-openshift:1.8

MAINTAINER kitty-catt <kitty-catt@example.com>

LABEL io.openshift.expose-services="8080/http"

ENV WORKDIR="/opt/app-root"

RUN mkdir -pv ${WORKDIR} && \
    chown -R 1001:0 ${WORKDIR} && \
    chmod -R g=u ${WORKDIR} && \
    chgrp -R 0 ${WORKDIR}

WORKDIR $WORKDIR

ADD generic-nexus-download.sh .

RUN generic-nexus-download.sh

CMD ["java -jar /opt/app-root/app.jar"]