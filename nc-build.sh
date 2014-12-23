#!/bin/bash

cd core
ant clean-all resolve dist publish-local
cp dist/kettle-core-TRUNK-SNAPSHOT.jar ~/Downloads/pdi-ee/data-integration/lib
cp dist/kettle-core-TRUNK-SNAPSHOT.jar ~/Downloads/pdi-ee/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib

cd ../engine
ant clean-all resolve dist publish-local
cp dist/kettle-engine-TRUNK-SNAPSHOT.jar ~/Downloads/pdi-ee/data-integration/lib
cp dist/kettle-engine-TRUNK-SNAPSHOT.jar ~/Downloads/pdi-ee/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib

cd ../ui
ant clean-all resolve dist publish-local
cp dist/kettle-ui-swt-TRUNK-SNAPSHOT.jar ~/Downloads/pdi-ee/data-integration/lib
cp dist/kettle-ui-swt-TRUNK-SNAPSHOT.jar ~/Downloads/pdi-ee/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib

