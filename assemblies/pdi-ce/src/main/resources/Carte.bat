@echo off
setlocal
pushd %~dp0

SET OPT=%OPT% "-Dorg.mortbay.util.URI.charset=UTF-8"
SET STARTTITLE="Carte"
SET SPOON_CONSOLE=1
REM ***********************************************************************
REM ** Optionally set up the options for JAAS (uncomment to make active) **
REM ***********************************************************************

REM set OPT=%OPT% -Djava.security.auth.login.config=%JAAS_LOGIN_MODULE_CONFIG%
REM set OPT=%OPT% -Dloginmodulename=%JAAS_LOGIN_MODULE_NAME%
call Spoon.bat -main org.pentaho.di.www.Carte %*
popd
