#!/bin/bash

if [ -z ${version} ]; then echo "version is unset" && exit 1; fi
if [ -z ${dist} ]; then echo "dist is unset" && exit 1; fi
if [ -z ${CATALINA_HOME} ]; then echo "CATALINA_HOME is unset" && exit 1; fi

echo 'Downloading and extracting spoon.war'
wget -q https://github.com/HiromuHota/pentaho-kettle/releases/download/webspoon%2F$version/spoon.war || exit $?
mkdir ${CATALINA_HOME}/webapps/spoon
unzip -q spoon.war -d ${CATALINA_HOME}/webapps/spoon
rm spoon.war

echo 'Downloading and replacing plugins'
wget -q https://github.com/HiromuHota/pdi-platform-utils-plugin/releases/download/webspoon%2F$version/pdi-platform-utils-plugin-core-$dist.jar -O ${CATALINA_HOME}/plugins/platform-utils-plugin/pdi-platform-utils-plugin-core-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/big-data-plugin/releases/download/webspoon%2F$version/hadoop-cluster-ui-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/pentaho/hadoop-cluster-ui/$dist/hadoop-cluster-ui-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-kettle/releases/download/webspoon%2F$version/repositories-plugin-core-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/repositories-plugin-core/$dist/repositories-plugin-core-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-kettle/releases/download/webspoon%2F$version/pdi-engine-configuration-ui-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/pdi-engine-configuration-ui/$dist/pdi-engine-configuration-ui-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-kettle/releases/download/webspoon%2F$version/file-open-save-core-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/file-open-save-core/$dist/file-open-save-core-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-kettle/releases/download/webspoon%2F$version/file-open-save-new-core-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/file-open-save-new-core/$dist/file-open-save-new-core-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-kettle/releases/download/webspoon%2F$version/get-fields-core-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/get-fields-core/$dist/get-fields-core-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-kettle/releases/download/webspoon%2F$version/connections-ui-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/org/pentaho/di/plugins/connections-ui/$dist/connections-ui-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pdi-dataservice-server-plugin/releases/download/webspoon%2F$version/pdi-dataservice-server-plugin-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/pentaho/pdi-dataservice-server-plugin/$dist/pdi-dataservice-server-plugin-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/marketplace/releases/download/webspoon%2F$version/pentaho-marketplace-di-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/org/pentaho/pentaho-marketplace-di/$dist/pentaho-marketplace-di-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-osgi-bundles/releases/download/webspoon%2F$version/pentaho-i18n-webservice-bundle-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/pentaho/pentaho-i18n-webservice-bundle/$dist/pentaho-i18n-webservice-bundle-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-osgi-bundles/releases/download/webspoon%2F$version/pentaho-kettle-repository-locator-impl-spoon-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/pentaho/pentaho-kettle-repository-locator-impl-spoon/$dist/pentaho-kettle-repository-locator-impl-spoon-$dist.jar || exit $?
wget -q https://github.com/HiromuHota/pentaho-osgi-bundles/releases/download/webspoon%2F$version/pentaho-pdi-platform-$dist.jar -O ${CATALINA_HOME}/system/karaf/system/pentaho/pentaho-pdi-platform/$dist/pentaho-pdi-platform-$dist.jar || exit $?

echo 'Configuring org.pentaho.requirejs.cfg'
echo 'context.root=/spoon/osgi' | tee ${CATALINA_HOME}/system/karaf/etc/org.pentaho.requirejs.cfg

echo 'Configuring custom.properties'
wget -q https://raw.githubusercontent.com/HiromuHota/pentaho-karaf-assembly/webspoon%2F$version/assemblies/common-resources/src/main/resources-filtered/etc/custom.properties -O ${CATALINA_HOME}/system/karaf/etc/custom.properties || exit $?

echo 'Configuring Carte'
mkdir ${CATALINA_HOME}/system/kettle
wget -q https://raw.githubusercontent.com/HiromuHota/pentaho-kettle/webspoon%2F$version/docker/slave-server-config.xml -O ${CATALINA_HOME}/system/kettle/slave-server-config.xml || exit $?

echo 'Removing Karaf cache'
rm -rf ${CATALINA_HOME}/system/karaf/caches/webspoonservletcontextlistener || true
