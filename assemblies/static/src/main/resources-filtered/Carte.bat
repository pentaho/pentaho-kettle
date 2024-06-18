@echo off

REM *****************************************************************************
REM
REM Pentaho Data Integration
REM
REM Copyright (C) 2006 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
REM
REM *****************************************************************************
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with
REM the License. You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM
REM *****************************************************************************

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
