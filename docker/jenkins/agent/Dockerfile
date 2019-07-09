FROM openjdk:8-jdk-slim

ARG user=jenkins
ARG group=jenkins
ARG uid=1000
ARG gid=1000
ARG home=/var/jenkins_home

ENV LANG C.UTF-8
ENV JENKINS_SWARM_VERSION 3.17
ENV JENKINS_SWARM_URL https://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/$JENKINS_SWARM_VERSION/swarm-client-$JENKINS_SWARM_VERSION.jar

# install netstat to allow connection health check with
# netstat -tan | grep ESTABLISHED
RUN apt-get update && apt-get install -y --no-install-recommends \
    net-tools \
    curl \
    wget \
    unzip \
    bzip2 \
    xz-utils \
    apt-transport-https \
    ca-certificates \
    gnupg2 \
    software-properties-common \
    git \
  && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
RUN add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/debian \
    $(lsb_release -cs) \
    stable"

RUN apt-get update && apt-get install -y --no-install-recommends \
    docker-ce-cli \
  && rm -rf /var/lib/apt/lists/*

RUN mkdir -p $home && mkdir $home/workspace \
  && chown -R ${uid}:${gid} $home \
  && groupadd -o -g ${gid} ${group} \
  && useradd -o -d "$home" -u ${uid} -g ${gid} -m -s /bin/bash ${user}

RUN curl --create-dirs -sSLo /usr/share/jenkins/swarm-client-$JENKINS_SWARM_VERSION.jar $JENKINS_SWARM_URL && chmod 755 /usr/share/jenkins

COPY agent.sh /usr/local/bin/agent.sh

USER $user

ENTRYPOINT ["/usr/local/bin/agent.sh"]