# jenkins-pipelines
Jenkins Pipeline and YAML Build Data Files

This repository is used to quickly build source repositories checked into a Git repository. The source for this repository is built on top of [Jenkins Declarative Pipeline](https://jenkins.io/doc/book/pipeline/syntax/#declarative-pipeline), a DSL with a relatively simple syntax. For a detailed reference, see the [Wiki](../../wiki).

The [Jenkins Shared Libraries](https://github.com/pentaho/jenkins-shared-libraries) repository is a required companion to this repository. The libraries contain a significant amount of custom code to make it easy to construct complex builds using YAML Build Data Files. The build can that may use single or multiple build technologies (Maven, Gradle, and Ant).

## Setting up Jenkins Pipelines

In order to use this repository, you must have a recent version of Jenkins installed. See [Setting Up Jenkins](https://github.com/pentaho/jenkins-pipelines/wiki/1.-Setting-Up-Jenkins) in the [wiki](https://github.com/pentaho/jenkins-pipelines/wiki) for more details. Using Docker, you can have a Jenkins pipeline building in no time at all.

## Configuring Pipelines  

Once Jenkins is setup, it is now time to create the build data for the pipelines. In the [resources/builders](https://github.com/pentaho/jenkins-pipelines/tree/master/resources/builders) folder of this repository, there are a number of example YAML files to get you started. For a more detailed dive, see the [Configuration](https://github.com/pentaho/jenkins-pipelines/wiki/2.-Configuration) section in the [wiki](https://github.com/pentaho/jenkins-pipelines/wiki).

## Video Tutorials

[Video tutorials](https://github.com/pentaho/jenkins-pipelines/wiki/3.-Video-Tutorials) to assist in the setup and creation of pipelines are in the [wiki](https://github.com/pentaho/jenkins-pipelines/wiki).
