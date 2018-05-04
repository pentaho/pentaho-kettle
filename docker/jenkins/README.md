# Pentaho Jenkins Docker image

The Jenkins Continuous Integration and Delivery server.

This image is provided to you with a minimal set of plugins to run pipelines, and 
it comes pre-configured with the [Jenkins Shared Libraries](https://github.com/pentaho/jenkins-shared-libraries).

NOTE: For jenkins to be able to clone the Shared Libraries, you must provide it with your git credentials. If you haven't created a personal access token yet, 
see [Creating a personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/)
for steps on how to create one.

# Usage
The simplier way to get started is to use the provided startup script `go.sh`.  
Edit `secrets/credentials` file with the necessary credentials, and run the `go.sh` shell script.

```
./go.sh
```

This will build/start a fully configured jenkins container.\
You can now point your browser to [0.0.0.0:8080](http://0.0.0.0:8080) and start using.

Alternatively you can build the ***alpine*** version.

```
./go.sh -v alpine
```

NOTE: This script automatically creates a *jenkins_pipeline* volume on docker host, that will survive container start/stop/deletion.

```
./go.sh -h
usage:  [options]
  options:
    -h, --help              This help message
    -v, --variant string    Sets the variant of the jenkins container.
        --debug             Enable debug mode, this will only print the commands that will be run.
    -q, --quiet             Enable quiet mode.
    -d, --daemon            Run in daemon mode.
    -n, --no-build          Skip the building part.
    -f, --force             Force building from the start.
``` 

# Manual usage

```
docker run -p 8080:8080 \
  -v jenkins_pipeline:/var/jenkins_home \
  -v <credentials-absolute-path>:/usr/share/jenkins/secrets/credentials \
  pentaho/jenkins:lts
```

Alternatively, not using an secret file, you can pass the credentials as system variables:

```
docker run -p 8080:8080 \
  -e JENKINS_USER=<jenkins_user>
  -e JENKINS_PASS=<jenkins_pass>
  -e GIT_USER=<git_user> \
  -e GIT_PASS=<git_token> \
  pentaho/jenkins:lts-alpine
``` 

For more information, check the official jenkins docker image: [https://github.com/jenkinsci/docker](https://github.com/jenkinsci/docker#official-jenkins-docker-image)


# Building manually

Build with the usual

```
docker build -t pentaho/jenkins:lts .
```

If you'd like to try the alpine version build with

```
docker build --file Dockerfile-alpine -t pentaho/jenkins:lts-alpine .
```
