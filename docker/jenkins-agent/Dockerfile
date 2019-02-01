FROM openjdk:8-jdk-slim

ENV JENKINS_SWARM_VERSION 3.14
ENV JENKINS_SWARM_URL https://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/$JENKINS_SWARM_VERSION/swarm-client-$JENKINS_SWARM_VERSION.jar

# install netstat to allow connection health check with
# netstat -tan | grep ESTABLISHED
RUN apt-get update && apt-get install -y --no-install-recommends \
    net-tools \
    curl \
    wget \
    bzip2 \
    git \
  && rm -rf /var/lib/apt/lists/*

RUN useradd -c "Jenkins Agent user" -d /var/jenkins_home -m jenkins-agent
RUN curl --create-dirs -sSLo /usr/share/jenkins/swarm-client-$JENKINS_SWARM_VERSION.jar $JENKINS_SWARM_URL && chmod 755 /usr/share/jenkins

COPY agent.sh /usr/local/bin/agent.sh

USER jenkins-agent
VOLUME /var/jenkins_home

ENTRYPOINT ["/usr/local/bin/agent.sh"]