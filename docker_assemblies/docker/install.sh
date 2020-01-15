#!/bin/bash

if [ -z ${version} ]; then echo "version is unset" && exit 1; fi
if [ -z ${dist} ]; then echo "dist is unset" && exit 1; fi
if [ -z ${CATALINA_HOME} ]; then echo "CATALINA_HOME is unset" && exit 1; fi

echo 'Configuring org.pentaho.requirejs.cfg'
echo 'context.root=/spoon/osgi' | tee ${CATALINA_HOME}/system/karaf/etc/org.pentaho.requirejs.cfg

