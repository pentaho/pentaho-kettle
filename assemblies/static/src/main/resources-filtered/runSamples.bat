@echo off
REM ******************************************************************************
REM
REM Pentaho
REM
REM Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
REM
REM Use of this software is governed by the Business Source License included
REM in the LICENSE.TXT file.
REM
REM Change Date: 2030-06-15
REM ******************************************************************************
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
