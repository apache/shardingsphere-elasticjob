# Dockerfile for Elastic-Job-Cloud Mesos framework
FROM ubuntu:14.04
MAINTAINER caohao@dangdang.com

# set JAVA_HOME depending if JAVA_PRODUCT_VERSION is set
ARG JAVA_PRODUCT_VERSION=''
ENV TARGET_JAVA_HOME ${JAVA_PRODUCT_VERSION:+/usr/lib/jvm/java-$JAVA_PRODUCT_VERSION-openjdk-amd64}
ENV JAVA_HOME ${TARGET_JAVA_HOME:-/usr/lib/jvm/java-7-openjdk-amd64}
ENV JAVA_PRODUCT_VERSION ${JAVA_PRODUCT_VERSION:-7}

# Add mesosphere
# install mesos version, build arg, MESOS_RELEASE with a default value
ARG MESOS_RELEASE=1.1.0
RUN DISTRO=$(lsb_release -is | tr "[:upper:]" "[:lower:]") && \
        CODENAME=$(lsb_release -cs) && \
        echo "deb http://repos.mesosphere.com/${DISTRO} ${CODENAME} main" > /etc/apt/sources.list.d/mesosphere.list && \
        apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF && \
        apt-get -y update && \
        apt-get -y install mesos=`apt-cache madison mesos | grep " ${MESOS_RELEASE}" | head -1 | awk '{print $3}'` && \
        apt-get clean

# set build arg ELASTIC_JOB_CLOUD_RELEASE, MIRROR with defaults values
ARG ELASTIC_JOB_CLOUD_RELEASE=2.1.2
ARG MIRROR=''
ARG RELEASE=''

# set runtime environment variables
ENV MESOS_RELEASE ${MESOS_RELEASE:-1.1.0}
ENV MESOS_NATIVE_JAVA_LIBRARY ${MESOS_NATIVE_JAVA_LIBRARY:-/usr/lib/libmesos.so}

# Add files for elastic-job-cloud package building
COPY . /tmp

# building elastic-job-cloud package
RUN cd /tmp && \
        apt-get -y install openjdk-${JAVA_PRODUCT_VERSION}-jdk maven wget curl && \
                                update-alternatives --install /usr/bin/java java ${JAVA_HOME%/}/bin/java 20000 && \
                                update-alternatives --install /usr/bin/javac javac ${JAVA_HOME%/}/bin/javac 20000 && \
        mkdir -p /opt/elastic-job-cloud && \
        wget http://dangdangdotcom.github.io/elastic-job/elastic-job-cloud/dist/elastic-job-cloud-scheduler-${ELASTIC_JOB_CLOUD_RELEASE}.tar.gz && \
        tar -xvf /tmp/elastic-job-cloud-scheduler-${ELASTIC_JOB_CLOUD_RELEASE}.tar.gz -C /opt/elastic-job-cloud --strip=1 && \
        rm -rf /tmp/* ~/.m2 && \
        apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

WORKDIR /opt/elastic-job-cloud