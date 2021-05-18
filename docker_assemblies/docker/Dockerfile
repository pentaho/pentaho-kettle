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
ARG DEPENDENCIES_DIR

RUN groupadd -r tomcat \
    && useradd -r --create-home -g tomcat tomcat \
    && chown -R tomcat:tomcat ${CATALINA_HOME}
USER tomcat

COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pdi-ce.zip pdi-ce-$dist.zip

COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/webspoon-$dist.war spoon.war

RUN unzip -q pdi-ce-$dist.zip && \
  mv data-integration/system ${CATALINA_HOME}/system && \
  mv data-integration/plugins ${CATALINA_HOME}/plugins && \
  mv data-integration/simple-jndi ${CATALINA_HOME}/simple-jndi && \
  mv data-integration/samples ${CATALINA_HOME}/samples && \
  mv data-integration/LICENSE.txt ${CATALINA_HOME}/webSpoon-LICENSE.txt && \
  rm pdi-ce-$dist.zip && \
  rm -rf data-integration

COPY --chown=tomcat:tomcat customFiles/server.xml /usr/local/tomcat/conf/server.xml

#Replacing plugins
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pdi-platform-utils-plugin-core-$dist.jar ${CATALINA_HOME}/plugins/platform-utils-plugin/pdi-platform-utils-plugin-core-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/hadoop-cluster-ui-$dist.jar ${CATALINA_HOME}/system/karaf/system/pentaho/hadoop-cluster-ui/$dist/hadoop-cluster-ui-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/repositories-plugin-core-$dist.jar ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/repositories-plugin-core/$dist/repositories-plugin-core-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pdi-engine-configuration-ui-$dist.jar ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/pdi-engine-configuration-ui/$dist/pdi-engine-configuration-ui-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/file-open-save-core-$dist.jar ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/file-open-save-core/$dist/file-open-save-core-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/file-open-save-new-core-$dist.jar ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/file-open-save-new-core/$dist/file-open-save-new-core-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/get-fields-core-$dist.jar ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/get-fields-core/$dist/get-fields-core-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/connections-ui-$dist.jar ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/connections-ui/$dist/connections-ui-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pdi-dataservice-server-plugin-$dist.jar ${CATALINA_HOME}/system/karaf/system/pentaho/pdi-dataservice-server-plugin/$dist/pdi-dataservice-server-plugin-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pentaho-marketplace-di-$dist.jar ${CATALINA_HOME}/system/karaf/system/org/pentaho/pentaho-marketplace-di/$dist/pentaho-marketplace-di-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pentaho-i18n-webservice-bundle-$dist.jar ${CATALINA_HOME}/system/karaf/system/pentaho/pentaho-i18n-webservice-bundle/$dist/pentaho-i18n-webservice-bundle-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pentaho-kettle-repository-locator-impl-spoon-$dist.jar ${CATALINA_HOME}/system/karaf/system/pentaho/pentaho-kettle-repository-locator-impl-spoon/$dist/pentaho-kettle-repository-locator-impl-spoon-$dist.jar
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/pentaho-pdi-platform-$dist.jar ${CATALINA_HOME}/system/karaf/system/pentaho/pentaho-pdi-platform/$dist/pentaho-pdi-platform-$dist.jar

#Configuring custom.properties
COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/etc/custom.properties ${CATALINA_HOME}/system/karaf/etc/custom.properties

#Configuring Carte
RUN mkdir ${CATALINA_HOME}/system/kettle
COPY --chown=tomcat:tomcat slave-server-config.xml ${CATALINA_HOME}/system/kettle/slave-server-config.xml

ARG CACHEBUST=1

RUN echo "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true" | tee -a conf/catalina.properties
COPY --chown=tomcat:tomcat install.sh /tmp/install.sh
RUN sh /tmp/install.sh

COPY --chown=tomcat:tomcat ${DEPENDENCIES_DIR}/webspoon-security-$dist.jar ${CATALINA_HOME}/lib/
RUN echo "CLASSPATH="$CATALINA_HOME"/lib/webspoon-security-$dist.jar" | tee ${CATALINA_HOME}/bin/setenv.sh
COPY --chown=tomcat:tomcat catalina.policy ${CATALINA_HOME}/conf/
COPY --chown=tomcat:tomcat customFiles/web.xml ${CATALINA_HOME}/webapps/spoon/WEB-INF/web.xml
RUN mkdir -p $HOME/.kettle/users && mkdir -p $HOME/.pentaho/users
RUN mkdir -p $HOME/.kettle/data && cp -r ${CATALINA_HOME}/samples $HOME/.kettle/data/samples
