@echo off
setlocal enabledelayedexpansion
pushd %~dp0
ECHO ****************************************************
ECHO * Starting executing transformations - !time! *
ECHO ****************************************************
for /r samples\transformations %%x in (*.ktr) DO (
  echo EXECUTING TRANSFORMATION ["%%x"]
  call pan.bat -file:"%%x" -level:Minimal
)
ECHO *************************************************
ECHO Finished executing transformation - !time! *
ECHO *************************************************
popd