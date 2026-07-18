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
@ECHO OFF
set CP=lib/*
java -Xms1024m -Xmx2048m -cp "%CP%" org.hitachivantara.importer.utility.ImporterUtility
