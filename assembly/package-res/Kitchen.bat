@echo off
setlocal
SET initialDir=%cd%
pushd %~dp0
SET STARTTITLE="Kitchen"
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.di.kitchen.Kitchen -initialDir "%initialDir%"\ %*
popd
