#!/bin/bash

if [ -z ${version} ]; then echo "version is unset" && exit 1; fi
if [ -z ${dist} ]; then echo "dist is unset" && exit 1; fi
if [ -z ${CATALINA_HOME} ]; then echo "CATALINA_HOME is unset" && exit 1; fi

echo 'Extracting spoon.war'
mkdir ${CATALINA_HOME}/webapps/spoon
unzip -q spoon.war -d ${CATALINA_HOME}/webapps/spoon
rm spoon.war

echo 'Configuring org.pentaho.requirejs.cfg'
echo 'context.root=/spoon/osgi' | tee ${CATALINA_HOME}/system/karaf/etc/org.pentaho.requirejs.cfg

echo 'Removing Karaf cache'
rm -rf ${CATALINA_HOME}/system/karaf/caches/webspoonservletcontextlistener || true
