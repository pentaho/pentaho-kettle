@echo off
setlocal
pushd %~dp0
SET STARTTITLE="Kitchen"
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.di.kitchen.Kitchen %*
popd
