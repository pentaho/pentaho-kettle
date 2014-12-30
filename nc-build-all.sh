#!/bin/bash

export GIT_DIR=/c/git
export PDI_EE_DIR=/c/pentaho/pdi-ee-client-TRUNK-SNAPSHOT

BUILD_KETTLE=true
BUILD_PDI_EE_PLUGIN=false
BUILD_SHIMS=false
BUILD_BIG_DATA_PLUGIN=false

if [ "$BUILD_KETTLE" == "true" ]; then
	### build kettle core, engine, ui, copy to client + server
	echo "Building kettle/core..."
	cd core
	ant clean-all resolve dist publish-local &> build.out
	cp dist/kettle-core-TRUNK-SNAPSHOT.jar $PDI_EE_DIR/data-integration/lib
	cp dist/kettle-core-TRUNK-SNAPSHOT.jar $PDI_EE_DIR/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib
	echo "Building kettle/engine..."
	cd ../engine
	ant clean-all resolve dist publish-local &> build.out
	cp dist/kettle-engine-TRUNK-SNAPSHOT.jar $PDI_EE_DIR/data-integration/lib
	cp dist/kettle-engine-TRUNK-SNAPSHOT.jar $PDI_EE_DIR/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib
	echo "Building kettle/ui..."
	cd ../ui
	ant clean-all resolve dist publish-local &> build.out
	cp dist/kettle-ui-swt-TRUNK-SNAPSHOT.jar $PDI_EE_DIR/data-integration/lib
	cp dist/kettle-ui-swt-TRUNK-SNAPSHOT.jar $PDI_EE_DIR/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib
	cd ..
	rm -Rf /c/pentaho/pdi-ee/data-integration/ui
	cp -Rf assembly/package-res/ui $PDI_EE_DIR/data-integration
fi

if [ "$BUILD_PDI_EE_PLUGIN" == "true" ]; then
	### handle pdi-ee-plugin + platform/repository
	echo "Building platform/repository..."
	cd ../pentaho-platform/repository
	ant clean-all resolve dist publish-local &> build.out
	cp dist/pentaho-platform-repository-TRUNK-SNAPSHOT.jar $PDI_EE_DIR/data-integration-server/tomcat/webapps/pentaho-di/WEB-INF/lib
	echo "Building pdi-ee-plugin (PurRepository)..."
	cd ../../pdi-ee-plugin
	ant clean-all resolve dist &> build.out
	rm -Rf $PDI_EE_DIR/data-integration/plugins/repositories/pur-repository-plugin
	rm -Rf $PDI_EE_DIR/data-integration-server/pentaho-solutions/system/kettle/plugins/repositories/pur-repository-plugin
	rm -Rf $PDI_EE_DIR/data-integration-server/pentaho-solutions/system/pur-repository-plugin
	cp dist/pur-repository-plugin-package-TRUNK-SNAPSHOT.zip $PDI_EE_DIR/data-integration/plugins/repositories
	cp dist/pur-repository-plugin-package-TRUNK-SNAPSHOT.zip $PDI_EE_DIR/data-integration-server/pentaho-solutions/system/kettle/plugins/repositories
	cp dist/pur-repository-plugin-package-TRUNK-SNAPSHOT.zip $PDI_EE_DIR/data-integration-server/pentaho-solutions/system/
	cd $PDI_EE_DIR/data-integration/plugins/repositories
	unzip pur-repository-plugin-package-TRUNK-SNAPSHOT.zip &> build.out
	rm pur-repository-plugin-package-TRUNK-SNAPSHOT.zip
	cd $PDI_EE_DIR/data-integration-server/pentaho-solutions/system/kettle/plugins/repositories
	unzip pur-repository-plugin-package-TRUNK-SNAPSHOT.zip &> build.out
	rm pur-repository-plugin-package-TRUNK-SNAPSHOT.zip
	cd $PDI_EE_DIR/data-integration-server/pentaho-solutions/system
	unzip pur-repository-plugin-package-TRUNK-SNAPSHOT.zip &> build.out
	rm pur-repository-plugin-package-TRUNK-SNAPSHOT.zip
fi

if [ "$BUILD_SHIMS" == "true" ]; then
	### build shims + big-data-plugin 
	echo "Building shims..."
	cd $GIT_DIR/pentaho-hadoop-shims
	ant clean-all resolve dist >& build.out
fi

if [ "$BUILD_BIG_DATA_PLUGIN" == "true" ]; then	
	echo "Building big-data-plugin..."
	cd ../big-data-plugin
	ant clean-all resolve dist >& build.out
	cp dist/pentaho-big-data-plugin-TRUNK-SNAPSHOT.zip $PDI_EE_DIR/data-integration/plugins/
	cp dist/pentaho-big-data-plugin-TRUNK-SNAPSHOT.zip $PDI_EE_DIR/data-integration-server/pentaho-solutions/system/kettle/plugins/
	cd $PDI_EE_DIR/data-integration/plugins
	rm -Rf pentaho-big-data-plugin
	unzip pentaho-big-data-plugin-TRUNK-SNAPSHOT.zip &> build.out
	rm pentaho-big-data-plugin-TRUNK-SNAPSHOT.zip
	cd $PDI_EE_DIR/data-integration-server/pentaho-solutions/system/kettle/plugins/
	rm -Rf pentaho-big-data-plugin
	unzip pentaho-big-data-plugin-TRUNK-SNAPSHOT.zip &> build.out
	rm pentaho-big-data-plugin-TRUNK-SNAPSHOT.zip
fi
	
echo ""
echo "All projects built and deployed.  Restart di-server and use spoon."
