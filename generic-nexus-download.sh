#!/bin/bash

WHOAMI=$(whoami)
echo "I am ${whoami}"

# internal form
export NEXUS3=nexus3.tools.svc.cluster.local:80
export APP_HOME=/opt/app-root/bin
rm -Rf $APP_HOME/app.jar

# Parameter section:
export REPOSITORY=maven-snapshots
export GROUP=com.ibm.aot.solo
export NAME=customer-ms-spring
export BASE_VERSION=0.0.1-SNAPSHOT
export CLASSIFIER=jar-with-dependencies
export SHA1_EXT=jar.sha1

# Required SHA1SUM
export SHA1SUM_REQUIRED="http://${NEXUS3}/service/rest/v1/search/assets/download" &&\
export SHA1SUM_REQUIRED="${SHA1SUM_REQUIRED}?sort=version" &&\
export SHA1SUM_REQUIRED="${SHA1SUM_REQUIRED}&repository=${REPOSITORY}" &&\
export SHA1SUM_REQUIRED="${SHA1SUM_REQUIRED}&group=${GROUP}" &&\
export SHA1SUM_REQUIRED="${SHA1SUM_REQUIRED}&name=${NAME}" &&\
export SHA1SUM_REQUIRED="${SHA1SUM_REQUIRED}&maven.baseVersion=${BASE_VERSION}" &&\
export SHA1SUM_REQUIRED="${SHA1SUM_REQUIRED}&maven.extension=${SHA1_EXT}" 

# File Download
export ARTIFACT="http://${NEXUS3}/service/rest/v1/search/assets/download" &&\
export ARTIFACT="${ARTIFACT}?sort=version" &&\
export ARTIFACT="${ARTIFACT}&repository=${REPOSITORY}" &&\
export ARTIFACT="${ARTIFACT}&group=${GROUP}" &&\
export ARTIFACT="${ARTIFACT}&name=${NAME}" &&\
export ARTIFACT="${ARTIFACT}&maven.baseVersion=${BASE_VERSION}" &&\
export ARTIFACT="${ARTIFACT}&maven.extension=jar" 

if [ -v ${CLASSIFIER} ]; then
   echo "CLASSIFIER not set, no need to add it"
else
    export SHA1SUM_REQUIRED="${SHA1SUM_REQUIRED}&maven.classifier=${CLASSIFIER}" 
    export ARTIFACT="${ARTIFACT}&maven.classifier=${CLASSIFIER}"
fi

# Required checksum
echo ${SHA1SUM_REQUIRED}
curl -s -L -X GET ${SHA1SUM_REQUIRED} --output $APP_HOME/app.${SHA1_EXT}
SHA1SUM_REQUIRED=$(cat $APP_HOME/app.${SHA1_EXT})

# Download with actual checksum
echo ${ARTIFACT}
curl -s -L -X GET ${ARTIFACT} --output $APP_HOME/app.jar
SHA1SUM_ACTUAL=$(sha1sum $APP_HOME/app.jar | awk '{ print $1}')

if [[ ${SHA1SUM_ACTUAL} == ${SHA1SUM_REQUIRED} ]]; then
    echo "checksum match (${SHA1SUM_ACTUAL} vs ${SHA1SUM_REQUIRED}), download is OK"
    ls -l $APP_HOME/app.jar
    exit 0
else
    echo "checksum of downloaded file does not match (${SHA1SUM_ACTUAL} vs ${SHA1SUM_REQUIRED}), download is not OK"
    ls -l $APP_HOME/app.jar
    exit 1
fi

