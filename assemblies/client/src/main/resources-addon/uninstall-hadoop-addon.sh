#!/bin/bash
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2028-08-13
# ******************************************************************************

mkdir -p -- "$PWD/.uninstalled"
INSTALLEDDIR=$PWD/.uninstalled
mkdir -p -- $INSTALLEDDIR/classes

if [ -e $PWD/classes/kettle-lifecycle-listeners.xml ];then mv $PWD/classes/kettle-lifecycle-listeners.xml $INSTALLEDDIR/classes/kettle-lifecycle-listeners.xml; fi
if [ -e $PWD/classes/kettle-registry-extensions.xml ];then mv $PWD/classes/kettle-registry-extensions.xml $INSTALLEDDIR/classes/kettle-registry-extensions.xml; fi

if [ -e $PWD/drivers ];then mv $PWD/drivers $INSTALLEDDIR/drivers ; fi  
if [ -e $PWD/system ];then mv $PWD/system $INSTALLEDDIR/system ; fi
mkdir -p -- $INSTALLEDDIR/samples/jobs
if [ -e $PWD/samples/jobs/hadoop ];then mv $PWD/samples/jobs/hadoop $INSTALLEDDIR/samples/jobs/ ; fi  
mkdir -p -- $INSTALLEDDIR/plugins
if [ -e $PWD/plugins/pentaho-big-data-plugin ];then mv $PWD/plugins/pentaho-big-data-plugin $INSTALLEDDIR/plugins/ ; fi
mkdir -p -- $INSTALLEDDIR/lib
mv "$PWD"/lib/org.apache.karaf*.jar $INSTALLEDDIR/lib/
mv "$PWD"/lib/pdi-osgi-bridge-core*.jar $INSTALLEDDIR/lib/
mv "$PWD"/lib/pentaho-hadoop-shims-common-mapreduce*.jar $INSTALLEDDIR/lib/
mv "$PWD"/lib/pentaho-osgi-utils-api*.jar $INSTALLEDDIR/lib/
mv "$PWD"/lib/pentaho-service-coordinator*.jar $INSTALLEDDIR/lib/
mv "$PWD"/lib/shim-api-core*.jar $INSTALLEDDIR/lib/