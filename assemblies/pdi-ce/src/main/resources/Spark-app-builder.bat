@echo off
setlocal
SET initialDir=%cd%
pushd %~dp0
SET SPOON_CONSOLE=1

if "%PENTAHO_DI_JAVA_OPTIONS%"=="" set "PENTAHO_DI_JAVA_OPTIONS=-Xms1024m -Xmx3072m -XX:MaxPermSize=256m"

call Spoon.bat -main org.pentaho.pdi.spark.driver.app.builder.SparkDriverAppBuilder %*
popd
