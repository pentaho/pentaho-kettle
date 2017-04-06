@echo off
setlocal
SET initialDir=%cd%
pushd %~dp0
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.pdi.spark.driver.app.builder.SparkDriverAppBuilder %*
popd
