# Pentaho Jenkins Docker image

The Jenkins Continuous Integration and Delivery server.

This image is provided to you with a minimal set of plugins to run pipelines, and 
it comes pre-configured with the [Jenkins Shared Libraries](https://github.com/pentaho/jenkins-shared-libraries).

NOTE: For jenkins to be able to clone the Shared Libraries, you must provide it with your git credentials. If you haven't created a personal access token yet, 
see [Creating a personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/)
for steps on how to create one.

## Usage
The simplier way to get started is to use the provided startup script `go.sh`.  
Copy the file `secrets/credentials.template` to `secrets/credentials` and edit with the necessary credentials, then run the shell script.

```console
./go.sh
```

This will build/start a fully configured jenkins container.\
You can now point your browser to [0.0.0.0:8080](http://0.0.0.0:8080) and start using.

Alternatively you can build the ***alpine*** version.

```console
./go.sh -v alpine
```

NOTE: This script automatically creates a *jenkins_pipeline* volume on docker host, that will survive container start/stop/deletion.

```
./go.sh -h
usage: go.sh [options]
  options:
    -h, --help                  This help message
    -v, --variant string        Sets the variant of the jenkins container.
        --debug                 Enable debug mode, this will only print the commands that will be issued.
    -q, --quiet                 Enable quiet mode.
    -d, --daemon                Run in daemon mode.
    -n, --no-build              Skip the building part.
    -b, --build                 Skip the run part.
    -f, --force                 Force building from the start.
    -c, --credentials file      Use this file as credentials.
        --env-file file         Use this file to set the container's environment variables.
        --bind-mount string     Mount a file or directory into the container.
                                example: <host-file>:<destination>
``` 

#### Available Container's Environment Variables

These are the container's available environment variables and their defaults.

```ini
SAMPLE_PIPELINE_NAME=sample-pipeline
SAMPLE_PIPELINE_REPO=https://github.com/pentaho/jenkins-pipelines.git
SAMPLE_PIPELINE_BRANCH=master

SHARED_LIBRARIES_NAME=jenkins-shared-libraries
SHARED_LIBRARIES_REPO=https://github.com/pentaho/jenkins-shared-libraries.git
SHARED_LIBRARIES_BRANCH=master
```

## Manual usage

```console
docker run -p 8080:8080 \
  -v jenkins_pipeline:/var/jenkins_home \
  -v <credentials-absolute-path>:/usr/share/jenkins/secrets/credentials \
  pentaho/jenkins:lts
```

Alternatively, **but not recommended**, you can pass the credentials into the container as system variables.

```console
docker run -p 8080:8080 \
  -e JENKINS_USER=<jenkins_user> \
  -e JENKINS_PASS=<jenkins_pass> \
  -e GIT_USER=<git_user> \
  -e GIT_PASS=<git_token> \
  pentaho/jenkins:lts
``` 

### Testing your local Shared Libraries

If you want to point to a local shared libraries and/or pipeline repo when developing you have to map those volumes into the container. 
The path var is then available through environment variables or you can change it on the jenkins UI. 

The `go.sh` script can help you here too.
Start by creating a file named *env.list* and copy the environment variables that you need into it.

```console
cat << EOF > env.list
SAMPLE_PIPELINE_REPO=/jenkins-pipelines
SAMPLE_PIPELINE_BRANCH=master

SHARED_LIBRARIES_REPO=/jenkins-shared-libraries
SHARED_LIBRARIES_BRANCH=master
EOF
```

Next start the script, but bind mount your sources.

```console
./go.sh \
  --env-file env.list \
  --bind-mount ~/jenkins-shared-libraries:/jenkins-shared-libraries \
  --bind-mount ~/jenkins-pipelines:/jenkins-pipelines
```

###### Caveats

As mentioned before, the script creates the named volume *jenkins_pipeline*
> -v jenkins_pipeline:/var/jenkins_home

And if you are testing new stuff, or for whatever reason, you wan't to change
this to something else, just set the environment variable `VOLUME_NAME` with a
different name.

```console
VOLUME_NAME=test ./go.sh --debug -n
docker run \
 -it \
 --rm \
 -v test:/var/jenkins_home \
 -v /home/user/.creds:/usr/share/jenkins/secrets/credentials \
 -p 8080:8080 \
 pentaho/jenkins:lts
```

---

#### What is going on behind the scenes 

If you are curious about what the script is doing, you can add the `--debug` parameter
to the script. This will instruct it to print out the commands it will call. This is useful
if you want to play around.  

```console
./go.sh --debug -c ~/.creds -n --env-file env.list --bind-mount ~/jenkins-shared-libraries:/jenkins-shared-libraries
docker run \
 --env-file /home/user/jenkins-pipelines/docker/jenkins/env.list \
 -it \
 --rm \
 -v /home/user/jenkins-shared-libraries:/jenkins-shared-libraries \
 -v jenkins_pipeline:/var/jenkins_home \
 -v /home/user/.creds:/usr/share/jenkins/secrets/credentials \
 -p 8080:8080 \
 pentaho/jenkins:lts
```


For more information, check the official jenkins docker image: [https://github.com/jenkinsci/docker](https://github.com/jenkinsci/docker#official-jenkins-docker-image)