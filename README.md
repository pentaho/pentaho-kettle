# jenkins-pipelines
Jenkins Pipeline Build Files and YAML Build Data Files

### These piplelines are dependent on the installation of the [Jenkins Shared Libraries](https://github.com/pentaho/jenkins-shared-libraries) in your Jenkins configuration.

**How to set up Jenkins to run a new data-driven pipeline**
1. Follow the directions in the shared libraries project readme to set up the libraries
2. Click Jenkins/Manage Jenkins
3. Goto Plugin Manager and make sure you have installed: Git, Github, Blue Ocean, Build Timeout, and all Pipeline plugins. (This is probably not an exhaustive list and will need refinement.)
4. Goto Global Tool Configuration / JDK and setup a Java 8 JDK named "Java8_auto". Keep in mind that this label is NOT set in stone, you can configure any named JDK you like and use the Jenkinsfile JENKINS_JDK_FOR_BUILDS parameter to control it.
5. Goto Global Tool Configuration / Maven and setup a Maven configuration using at least version 3.3.9 named "maven3-auto". Keep in mind that this label is NOT set in stone, you can configure any named Maven like and use the Jenkinsfile JENKINS_MAVEN_FOR_BUILDS parameter to control it.
6. Save the configuration changes.
7. Create a fork of this project and a branch to store your new build control YAML data file. 
8. Set up a new single branch pipeline build in your Jenkins and define the jenkins-pipeline fork URL and branch you want to build.
9. Run your new build and shake out any issues. Recommend edits to this readme, the default Jenkinsfile, and the jenkins-shared-libraries with BACKLOG tickets.
10. Set Retrieval method to 'Modern SCM'
11. Make this thing rock and have some fun!


### NOTE: The current defualt pipeline builds rely on a shared (maven) cache in the build workspace
This will probably end up causing problems with file locks and corrupt file issues in the maven cache because it is not designed for multithreaded to handle maven parallel builds.

If you are on a Pentaho build server using one of our pre-configured maven installations this is probably already handled, but if you are creating a new (possible local) Jenkins you will likely need to do this:

*Install our build version of the [Takari maven local repository jar](http://nexus.pentaho.org/content/groups/omni/org/hitachi/aether/takari-local-repository/0.12.0/takari-local-repository-0.12.0.jar) in the Jenkins Maven lib/ext folder for the Maven installation used by your build.* 

*Here is an example sample path for that* 
`<JENKINS_HOME>/tools/hudson.tasks.Maven_MavenInstallation/<jenkins-maven-label>/lib/ext`

*On my mac system the path is*
`/Users/buildguy/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven3-auto/lib/ext`
