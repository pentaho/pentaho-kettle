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

REM ---------------------------------------------------------------------------
REM Finds a suitable Java
REM
REM Looks in well-known locations to find a suitable Java then sets two 
REM environment variables for use in other bat files. The two environment
REM variables are:
REM 
REM * _PENTAHO_JAVA_HOME - absolute path to Java home
REM * _PENTAHO_JAVA - absolute path to Java launcher (e.g. java.exe)
REM 
REM The order of the search is as follows:
REM 
REM 1. argument #1 - path to Java home
REM 2. environment variable PENTAHO_JAVA_HOME - path to Java home
REM 3. jre folder at current folder level
REM 4. java folder at current folder level
REM 5. jre folder one level up
REM 6 java folder one level up
REM 7. jre folder two levels up
REM 8. java folder two levels up
REM 9. environment variable JAVA_HOME - path to Java home
REM 10. environment variable JRE_HOME - path to Java home
REM 
REM If a suitable Java is found at one of these locations, then 
REM _PENTAHO_JAVA_HOME is set to that location and _PENTAHO_JAVA is set to the 
REM absolute path of the Java launcher at that location. If none of these 
REM locations are suitable, then _PENTAHO_JAVA_HOME is set to empty string and 
REM _PENTAHO_JAVA is set to java.exe.
REM 
REM Finally, there is one final optional environment variable: PENTAHO_JAVA.
REM If set, this value is used in the construction of _PENTAHO_JAVA. If not 
REM set, then the value java.exe is used. 
REM ---------------------------------------------------------------------------

if not "%PENTAHO_JAVA%" == "" goto gotPentahoJava
SET "__LAUNCHER=java.exe"
goto checkPentahoJavaHome

:gotPentahoJava
SET "__LAUNCHER=%PENTAHO_JAVA%"
goto checkPentahoJavaHome

:checkPentahoJavaHome
if exist "%~1\bin\%__LAUNCHER%" goto gotValueFromCaller
if not "%PENTAHO_JAVA_HOME%" == "" goto gotPentahoJavaHome
if exist "%~dp0jre\bin\%__LAUNCHER%" goto gotJreCurrentFolder
if exist "%~dp0java\bin\%__LAUNCHER%" goto gotJavaCurrentFolder
if exist "%~dp0..\jre\bin\%__LAUNCHER%" goto gotJreOneFolderUp
if exist "%~dp0..\java\bin\%__LAUNCHER%" goto gotJavaOneFolderUp
if exist "%~dp0..\..\jre\bin\%__LAUNCHER%" goto gotJreTwoFolderUp
if exist "%~dp0..\..\java\bin\%__LAUNCHER%" goto gotJavaTwoFolderUp
if not "%JAVA_HOME%" == "" goto gotJdkHome
if not "%JRE_HOME%" == "" goto gotJreHome
goto gotPath

:gotPentahoJavaHome
echo DEBUG: Using PENTAHO_JAVA_HOME
SET "_PENTAHO_JAVA_HOME=%PENTAHO_JAVA_HOME%"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreCurrentFolder
echo DEBUG: Found JRE at the current folder
SET "_PENTAHO_JAVA_HOME=%~dp0jre"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJavaCurrentFolder
echo DEBUG: Found JAVA at the current folder
SET "_PENTAHO_JAVA_HOME=%~dp0java"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreOneFolderUp
echo DEBUG: Found JRE one folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\jre"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJavaOneFolderUp
echo DEBUG: Found JAVA one folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\java"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreTwoFolderUp
echo DEBUG: Found JRE two folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\..\jre"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJavaTwoFolderUp
echo DEBUG: Found JAVA two folder up
SET "_PENTAHO_JAVA_HOME=%~dp0..\..\java"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJdkHome
echo DEBUG: Using JAVA_HOME
SET "_PENTAHO_JAVA_HOME=%JAVA_HOME%"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotJreHome
echo DEBUG: Using JRE_HOME
SET "_PENTAHO_JAVA_HOME=%JRE_HOME%"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotValueFromCaller
echo DEBUG: Using value (%~1) from calling script
SET "_PENTAHO_JAVA_HOME=%~1"
SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
goto end

:gotPath
echo WARNING: Using java from path

REM # Try to get java.home from java itself
FOR /F "tokens=2 delims==" %%a IN ('%__LAUNCHER% -XshowSettings:properties -version 2^>^&1^|findstr "java.home"') DO (SET _PENTAHO_JAVA_HOME=%%~a)
REM # Trim spaces
FOR /F "tokens=* delims= " %%a IN ("%_PENTAHO_JAVA_HOME%") DO (SET _PENTAHO_JAVA_HOME=%%~a)

if exist "%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%" (
	echo DEBUG: Getting java.home from java settings
	SET "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
) else (
	SET "_PENTAHO_JAVA_HOME="
	SET "_PENTAHO_JAVA=%__LAUNCHER%"
)

goto end

:end

echo DEBUG: _PENTAHO_JAVA_HOME=%_PENTAHO_JAVA_HOME%
echo DEBUG: _PENTAHO_JAVA=%_PENTAHO_JAVA%
