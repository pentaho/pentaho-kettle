# jenkins-pipelines
Jenkins Pipeline Build Files and YAML Build Data Files

### These piplelines are dependent on the installation of the [Jenkins Shared Libraries](https://github.com/pentaho/jenkins-shared-libraries) in your Jenkins configuration.

**How to do that:**
1. Click Jenkins/Manage Jenkins
2. Goto Global Pipeline Libraries
3. Set Library Name to 'jenkins-shared-libraries'
4. Set Default Version to 'master'
5. Set Load implicitly to 'false'
6. Set Allow default version to be overridden to 'true'
7. Set Include @Library changes in job recent changes to 'true'
8. Set Retrieval method to 'Modern SCM'
9. Goto Source Code Management section and ...
10. Set Project Repository to 'https://github.com/pentaho/jenkins-shared-libraries.git'
11. Set Credentials to a Jenkins credential with read access to that repository (possbily 'github-buildguy')
12. Click the Behviors Add button and add 'Discover branches'
13. Save the configuration changes.

### NOTE: The current defualt pipeline builds rely on a shared (maven) cache in the build workspace
This will probably end up causing problems with file locks and corrupt file issues in the maven cache because it is not designed for multithreaded to handle maven parallel builds (it's not so concurrent).

If you have this issue, you can install our build version of the [Takari maven local repository jar](http://nexus.pentaho.org/content/groups/omni/org/hitachi/aether/takari-local-repository/0.12.0/takari-local-repository-0.12.0.jar) in the Jenkins Maven lib/ext folder for the Maven installation used by your build. 

Here is an example sample path for that: 
`<JENKINS_HOME>/tools/hudson.tasks.Maven_MavenInstallation/<jenkins-maven-label>/lib/ext`

On my system the path is:
`/Users/buildguy/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven3-auto/lib/ext`



