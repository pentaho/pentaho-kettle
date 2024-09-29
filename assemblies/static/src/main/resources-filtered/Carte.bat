@echo off
REM ******************************************************************************
REM
REM Pentaho
REM
REM Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
REM
REM Use of this software is governed by the Business Source License included
REM in the LICENSE.TXT file.
REM
REM Change Date: 2028-08-13
REM ******************************************************************************

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
