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
REM Change Date: 2029-07-20
REM ******************************************************************************

setlocal
SET initialDir=%cd%
pushd %~dp0
SET STARTTITLE="Pan"
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.di.pan.Pan -initialDir "%initialDir%"\ %*
popd
