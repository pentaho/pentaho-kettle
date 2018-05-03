# https://github.com/jenkinsci/docker/blob/master/README.md
FROM jenkins/jenkins:lts

ENV CREDENTIALS_ID github-buildguy
ENV SAMPLE_PIPELINE_NAME sample-pipeline
ENV SAMPLE_PIPELINE_REPO https://github.com/pentaho/jenkins-pipelines.git
ENV SAMPLE_PIPELINE_BRANCH master
ENV SHARED_LIBRARIES_NAME jenkins-shared-libraries
ENV SHARED_LIBRARIES_REPO https://github.com/pentaho/jenkins-shared-libraries.git
ENV SHARED_LIBRARIES_BRANCH master

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY bootstrap /usr/share/jenkins/ref/init.groovy.d/

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl --fail http://localhost:8080 || exit 1