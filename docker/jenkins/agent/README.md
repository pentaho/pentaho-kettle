# Jenkins Docker swarm agent

A [Jenkins swarm](https://wiki.jenkins-ci.org/display/JENKINS/Swarm+Plugin) agent based of [`csanchez/jenkins-swarm-slave`](https://registry.hub.docker.com/u/csanchez/jenkins-swarm-slave/).

## Building

    docker build -t hv/jenkins-agent .

## Running

To run a Docker container passing [any parameters](https://wiki.jenkins-ci.org/display/JENKINS/Swarm+Plugin#SwarmPlugin-AvailableOptions) to the slave

    docker run hv/jenkins-agent -master http://jenkins:8080 -username jenkins -password jenkins -executors 1



