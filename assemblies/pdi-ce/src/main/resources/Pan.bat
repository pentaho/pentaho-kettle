@echo off
setlocal
SET initialDir=%cd%
pushd %~dp0
SET STARTTITLE="Pan"
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.di.pan.Pan -initialDir "%initialDir%"\ %*
popd
