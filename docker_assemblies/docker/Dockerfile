FROM tomcat:jdk8
MAINTAINER Hiromu Hota <hiromu.hota@hal.hitachi.com>
ENV JAVA_OPTS="-Xms1024m -Xmx2048m"
RUN echo "root:password" | chpasswd
RUN apt-get update && apt-get install -y vim
RUN apt-get update && apt-get install -y sudo

RUN rm -rf ${CATALINA_HOME}/webapps/* \
    && mkdir ${CATALINA_HOME}/webapps/ROOT \
    && echo "<% response.sendRedirect(\"spoon/\"); %>" > ${CATALINA_HOME}/webapps/ROOT/index.jsp


ARG base=9.0
ARG patch=22
ARG version=0.$base.$patch
ARG dist

RUN groupadd -r tomcat \
    && useradd -r --create-home -g tomcat tomcat \
    && chown -R tomcat:tomcat ${CATALINA_HOME}
USER tomcat

ARG CACHEBUST=1

COPY --chown=tomcat:tomcat target/tomcat ${CATALINA_HOME}

RUN echo "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true" | tee -a conf/catalina.properties
COPY --chown=tomcat:tomcat install.sh /tmp/install.sh
RUN sh /tmp/install.sh

RUN echo "CLASSPATH="$CATALINA_HOME"/lib/webspoon-security-$dist.jar" | tee ${CATALINA_HOME}/bin/setenv.sh
RUN mkdir -p $HOME/.kettle/users && mkdir -p $HOME/.pentaho/users
RUN mkdir -p $HOME/.kettle/data && cp -r ${CATALINA_HOME}/samples $HOME/.kettle/data/samples
