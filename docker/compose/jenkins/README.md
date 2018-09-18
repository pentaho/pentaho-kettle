# Jenkins Docker Compose
This compose allows booting up a jenkins container with any number of attached nodes.

## Usage
With the defaults it will go up with a master and a single node.

```console
docker-compose up
```

To increase the number of agent nodes you can pass the number you want as parameter.

```console
docker-compose up --scale agent=N
```

Where **N** is the number of agent nodes you want.

## Configuration
The following variables can be configured in a `.env` file in the current directory.

- JENKINS_VOLUME_NAME - The docker volume name used by the [jenkins image](../../jenkins). Default: jenkins-pipelines
- NETWORK_NAME - The docker network name. Default: hv-pipelines
- JENKINS_IMAGE_NAME - The image tag name when building the [jenkins image](../../jenkins). Default: hv/jenkins
- MASTER_LABELS - The labels set on the master node. Defaults: master
- CREDENTIALS - The path to the credentials file to be used by the [jenkins image](../../jenkins). Default: ./credentials
- PROJECTS_DIR - The projects directory that will be mapped into all the nodes, this allows building 
of local projects in your pipelines. The directory will be mapped to `/projects` inside the container. Default: ./projects
- AGENT_EXECUTORS - The number of executors of the node agents. Default: 2
- AGENT_LABELS - The labels set on the agent nodes. Default: non-master