@echo off & setlocal

set startTime=%time%
echo --------------- START: MAVEN BUILD ---------------------------
REM set MAVEN_COMMAND=mvn clean install -DskipTests -o
set MAVEN_COMMAND=mvn -T 2C clean install -Dmaven.test.skip -DskipTests -Dmaven.javadoc.skip=true -o
call %MAVEN_COMMAND%
echo --------------- END: MAVEN BUILD ---------------------------

echo --------------- START: COPY PLUGIN ---------------------------

REM PDI Version
set PDI_VERSION=10.1.0.0-SNAPSHOT
set KETTLE_DIR=C:\ASK\penWorkspace\pentaho-kettle\assemblies\client\target\pdi-ce-%PDI_VERSION%
set KETTLE_PLUGIN_DIR=%KETTLE_DIR%\data-integration\plugins\

echo KETTLE_PLUGIN_DIR: %KETTLE_PLUGIN_DIR%

if "%CURRENT_DIR%"=="" set CURRENT_DIR=%~dp0
echo CURRENT_DIR: %CURRENT_DIR%

REM set current directory name to variable
for /f %%A in ('cd') do set CURRENT_DIR_NAME=%%~nA
REM echo CURRENT_DIR_NAME: %CURRENT_DIR_NAME%

REM Plugin Name
set PLUGIN_DIR_NAME=%CURRENT_DIR_NAME%-plugin
echo PLUGIN_DIR_NAME: %PLUGIN_DIR_NAME%

REM go to the plugin zip folder
cd %CURRENT_DIR%\assemblies\plugin\target\
tar -xf %CURRENT_DIR_NAME%-plugin-%PDI_VERSION%.zip

set CURRENT_PLUGIN_DIRECTORY=%CURRENT_DIR%assemblies\plugin\target\%PLUGIN_DIR_NAME%
set destination=C:\ASK\penWorkspace\pentaho-kettle\assemblies\client\target\pdi-ce-%PDI_VERSION%\data-integration\plugins\kettle-language-translator-plugin\
xcopy %CURRENT_PLUGIN_DIRECTORY% %KETTLE_PLUGIN_DIR%\%PLUGIN_DIR_NAME% /e /c /i /h /r /y

echo --------------- END: COPY PLUGIN -----------------------------
echo Start Time: %startTime%
echo Finish Time: %time%
echo ---------------------------------------------------------------