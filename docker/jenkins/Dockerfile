# https://github.com/jenkinsci/docker/blob/master/README.md
FROM jenkins/jenkins:lts

ENV SCM_CREDENTIALS_ID github-buildguy
ENV DEPLOY_CREDENTIALS_ID nexus-buildguy
ENV DEFAULT_JOB_NAME pipeline
ENV DEFAULT_JOB_REPO https://github.com/pentaho/jenkins-pipelines.git
ENV DEFAULT_JOB_BRANCH master
ENV SHARED_LIBRARIES_NAME jenkins-shared-libraries
ENV SHARED_LIBRARIES_REPO https://github.com/pentaho/jenkins-shared-libraries.git
ENV SHARED_LIBRARIES_BRANCH master
ENV ROOT_URL http://0.0.0.0:8080

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

COPY install-custom-plugins.sh /usr/local/bin/install-custom-plugins.sh
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-custom-plugins.sh

COPY bootstrap /usr/share/jenkins/ref/init.groovy.d/

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl --fail $ROOT_URL || exit 1