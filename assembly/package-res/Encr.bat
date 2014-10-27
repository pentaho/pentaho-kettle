@echo off
setlocal
pushd %~dp0
SET STARTTITLE="Encr"
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.di.core.encryption.Encr %*
popd
