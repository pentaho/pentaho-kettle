# jenkins-pipelines
Jenkins Pipeline and YAML Build Data Files

This repository is used to quickly build source repositories checked into a Git repository. The source for this repository is built on top of [Jenkins Declarative Pipeline](https://jenkins.io/doc/book/pipeline/syntax/#declarative-pipeline), a DSL with a relatively simple syntax.

The [Jenkins Shared Libraries](https://github.com/pentaho/jenkins-shared-libraries) repository is a required companion to this repository. The libraries contain a significant amount of custom code to make it easy to construct complex builds using YAML Build Data Files. The build can that may use single or multiple build technologies (Maven, Gradle, and Ant).

## Usage

There are two approaches to enable the usage of Jenkins pipelines:

- Use Docker to create a Jenkins master
- Manually setup a Jenkins master

For most, Docker is the best approach as you can quickly spin up a Jenkins master with all of the required tools and plugins properly configured in about 10 minutes.

### Docker Jenkins master

Before starting, you must create a [GitHub API token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) that has access to all source repositories to be built. This token is used by Jenkins to view and checkout private repositories.

Once you have a token, clone this repository to the machine that has Docker installed. Within the [docker/jenkins](https://github.com/pentaho/jenkins-pipelines/blob/master/docker/jenkins) folder, there is a [bash script](https://github.com/pentaho/jenkins-pipelines/blob/master/docker/jenkins/go.sh) that simplifies the creation of the Docker Jenkins container. The container is set up with everything Jenkins needs to build the pipeline definitions in this repository. On Mac and Linux, only Docker is required to run the script. On Windows however, the script requires Git BASH found in the [Git for Windows](https://gitforwindows.org/) download. (Also note that on Windows there are some compatibility issues running Docker and Virtual Box at the same time.)

A video tutorial (~30 mins) on how to setup and use the script is available [here](https://pentaho.app.box.com/file/306724225188).

The reference page for the script can be found [here](https://github.com/pentaho/jenkins-pipelines/blob/master/docker/jenkins/README.md).

### Manually setup a Jenkins master

If you have the hardware, knowledge, and skill to setup a Jenkins server, below are the steps required to get the

1. Pipelines are dependent on the installation of the [Jenkins Shared Libraries](https://github.com/pentaho/jenkins-shared-libraries) in your Jenkins configuration. Follow the directions in the [readme](https://github.com/pentaho/jenkins-shared-libraries/README.md) to set up the libraries.
2. Goto *Jenkins > Manage Jenkins > Manage Plugins*
3. Install the following Jenkins plugins: Git, Github, Blue Ocean, Build Timeout, Folder Properties, Gradle, Maven Integration, Pipeline Maven Integration, and all other default Pipeline plugins.
4. Goto *Jenkins > Manage Jenkins > Global Tool Configuration*
5. Under the JDK section, setup a Java 8 JDK named *Java8_auto*.
6. Under the Maven section, setup a Maven configuration named *maven3-auto*. The minimum recommended Maven version is Maven 3.3.9.
7. Save the configuration changes.
8. Goto *Jenkins > Credentials > (global) Domain > Add Credentials*
9. Create a credential with the ID and Description of *github-buildguy* and set a username and password for GitHub credentials. The password must be a [GitHub API token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) that has access to the repositories being built.
10. Create a fork of this project and a branch to store your new build control YAML data file.
11. Set up a new single branch pipeline build in your Jenkins and define the jenkins-pipeline fork URL and branch you want to build.
12. Run your new build and shake out any issues. Recommend edits to this readme, the default Jenkinsfile, and the jenkins-shared-libraries with BACKLOG tickets.
13. Set Retrieval method to 'Modern SCM'
14. Make this thing rock and have some fun!


### NOTE: The current default pipeline builds rely on a shared (maven) cache in the build workspace
This will probably end up causing problems with file locks and corrupt file issues in the maven cache because it is not designed for multithreaded to handle maven parallel builds.

If you are on a Pentaho build server or using the Docker Jenkins master, this is already handled, but if you are creating a new local Jenkins you will likely need to do this:

*Install our build version of the [Takari maven local repository jar](http://nexus.pentaho.org/content/groups/omni/org/hitachi/aether/takari-local-repository/0.12.0/takari-local-repository-0.12.0.jar) in the Jenkins Maven lib/ext folder for the Maven installation used by your build.*

*Here is an example sample path for that*
`<JENKINS_HOME>/tools/hudson.tasks.Maven_MavenInstallation/<jenkins-maven-label>/lib/ext`

*On my mac system the path is*
`/Users/buildguy/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven3-auto/lib/ext`

## Creating Pipelines  

Once Jenkins is setup, it is now time to create the build data for the pipelines. In the [resources/builders](https://github.com/pentaho/jenkins-pipelines/tree/master/resources/builders) folder there are a number of example YAML files to get you started. Build data files currently contain two sections:

- buildProperties - Top-level build properties that are unique to this build
- jobGroups - A structured list of jobs to build as steps in the pipelines.

There is a video tutorial (~15 mins) on how to [Build a Feature Branch Using Jenkins Pipelines](https://pentaho.box.com/s/6qcysk2yccxvh4fpj5a8qkzlys4ipr7a). The tutorial goes into detail on how easy it is to tailor an existing build data file for your own purposes.
