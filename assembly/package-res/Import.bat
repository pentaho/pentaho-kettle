@echo off
setlocal
pushd %~dp0

SET STARTTITLE="Import"
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.di.imp.Import %*
popd
