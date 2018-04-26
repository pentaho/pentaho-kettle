# https://github.com/jenkinsci/docker/blob/master/README.md
FROM jenkins/jenkins:lts

ENV CREDENTIALS_ID github-buildguy

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY bootstrap /usr/share/jenkins/ref/init.groovy.d/

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl --fail http://localhost:8080 || exit 1