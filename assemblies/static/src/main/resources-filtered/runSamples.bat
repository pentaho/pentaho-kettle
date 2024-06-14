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
