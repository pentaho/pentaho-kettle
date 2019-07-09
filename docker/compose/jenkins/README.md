# Jenkins Docker Compose
This compose allows booting up a jenkins container with any number of attached nodes.

## Usage
A file named `.env` needs to be created in the current directory, this file can hold any environment configuration required. It needs to be created even if it's to be left empty.

With the defaults it will go up with a master and a single node.

```console
docker-compose up
```

To increase the number of agent nodes you can pass the number you want as parameter.

```console
docker-compose up --scale agent=N
```

Where **N** is the number of agent nodes you want.

To cleanup everything it was created you can use the `down` command. This will remove images, containers, networks and volumes.

```console
docker-compose down --volumes -rmi all
```

## Configuration
The following variables can be configured in the `.env` file in the current directory.

- NETWORK_NAME - The docker network name. Default: hv-pipelines
- PORT - The jenkins container externally mapped port. Default: 8080
- HOME_VOLUME_NAME - The jenkins home volume name. Default: hv-jenkins-home
- WORK_VOLUME_NAME - The shared workspace volume name. Default: hv-jenkins-workspace
- VOLUME_NAME - The docker volume name. Default: hv-jenkins-home
- JENKINS_IMAGE_NAME - The image tag name when building the [jenkins image](../../jenkins). Default: hv/jenkins
- MASTER_LABELS - The labels set on the master node. Defaults: master
- PROJECTS_DIR - The projects directory that will be mapped into all the nodes, this allows building 
of local projects in your pipelines. The directory will be mapped to `/projects` inside the container. Default: ./projects
- JENKINS_AGENT_IMAGE_NAME - The image tag name when building the [jenkins agent image](../../jenkins-agent). Default: hv/jenkins-agent
- AGENT_EXECUTORS - The number of executors of the node agents. Default: 2
- AGENT_LABELS - The labels set on the agent nodes. Default: non-master

## Other Useful Configurations
The compose uses a custom jenkins master Dockerfile so that we can have a _docker in docker_ setup. This allows you access to some extra configurations. You want to have the GID at least set to the one that has permissions to use `/var/run/docker.sock` on your system so that the jenkins user can access it.

- JENKINS_VERSION - The jenkins docker image tag to use for the master. See https://hub.docker.com/r/jenkins/jenkins. Default: lts 
- GID - The user id to use when configuring the jenkins user. Default: 1000
- UID - The user group id to use when configuring the jenkins user. Default: 1000

## Credentials
Considering this compose should be used for testing/development the credentials file mount was left out so the credentials variables are available through the `.env` file same as the rest of the configuration.