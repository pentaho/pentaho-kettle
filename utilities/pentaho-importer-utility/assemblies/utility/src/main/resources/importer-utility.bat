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
@ECHO OFF
set CP=lib/*
java -Xms1024m -Xmx2048m -cp "%CP%" org.hitachivantara.importer.utility.ImporterUtility
