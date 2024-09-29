@echo off
REM ******************************************************************************
REM
REM Pentaho
REM
REM Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
REM
REM Use of this software is governed by the Business Source License included
REM in the LICENSE.TXT file.
REM
REM Change Date: 2028-08-13
REM ******************************************************************************

SET CURRENTFOLDER=%~dp0
md %CURRENTFOLDER%\.uninstalled
set UNINSTALLEDFOLDER=%CURRENTFOLDER%\.uninstalled
md %UNINSTALLEDFOLDER%\classes
if exist %CURRENTFOLDER%\classes\kettle-lifecycle-listeners.xml move %CURRENTFOLDER%\classes\kettle-lifecycle-listeners.xml %UNINSTALLEDFOLDER%\classes\kettle-lifecycle-listeners.xml
if exist %CURRENTFOLDER%\classes\kettle-registry-extensions.xml move %CURRENTFOLDER%\classes\kettle-registry-extensions.xml %UNINSTALLEDFOLDER%\classes\kettle-registry-extensions.xml

if exist %CURRENTFOLDER%\drivers move %CURRENTFOLDER%\drivers %UNINSTALLEDFOLDER%\drivers
if exist %CURRENTFOLDER%\system move %CURRENTFOLDER%\system %UNINSTALLEDFOLDER%\system
md %UNINSTALLEDFOLDER%\samples\jobs
if exist %CURRENTFOLDER%\samples\jobs\hadoop move %CURRENTFOLDER%\samples\jobs\hadoop %UNINSTALLEDFOLDER%\samples\jobs\
md %UNINSTALLEDFOLDER%\plugins
if exist %CURRENTFOLDER%\plugins\pentaho-big-data-plugin move %CURRENTFOLDER%\plugins\pentaho-big-data-plugin %UNINSTALLEDFOLDER%\plugins\
md %UNINSTALLEDFOLDER%\lib
move "%CURRENTFOLDER%\lib\org.apache.karaf*.jar" %UNINSTALLEDFOLDER%\lib\
move "%CURRENTFOLDER%\lib\pdi-osgi-bridge-core*.jar" %UNINSTALLEDFOLDER%\lib\
move "%CURRENTFOLDER%\lib\pentaho-hadoop-shims-common-mapreduce*.jar" %UNINSTALLEDFOLDER%\lib\
move "%CURRENTFOLDER%\lib\pentaho-osgi-utils-api*.jar" %UNINSTALLEDFOLDER%\lib\
move "%CURRENTFOLDER%\lib\pentaho-service-coordinator*.jar" %UNINSTALLEDFOLDER%\lib
move "%CURRENTFOLDER%\lib\shim-api-core*.jar" %UNINSTALLEDFOLDER%\lib\

