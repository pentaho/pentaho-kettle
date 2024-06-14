
REM *****************************************************************************
REM
REM Pentaho Data Integration
REM
REM Copyright (C) 2010 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
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

rem ---------------------------------------------------------------------------
rem Finds a suitable Java
rem
rem Looks in well-known locations to find a suitable Java then sets two 
rem environment variables for use in other bat files. The two environment
rem variables are:
rem 
rem * _PENTAHO_JAVA_HOME - absolute path to Java home
rem * _PENTAHO_JAVA - absolute path to Java launcher (e.g. java.exe)
rem 
rem The order of the search is as follows:
rem 
rem 1. argument #1 - path to Java home
rem 2. environment variable PENTAHO_JAVA_HOME - path to Java home
rem 3. jre folder at current folder level
rem 4. java folder at current folder level
rem 5. jre folder one level up
rem 6 java folder one level up
rem 7. jre folder two levels up
rem 8. java folder two levels up
rem 9. environment variable JAVA_HOME - path to Java home
rem 10. environment variable JRE_HOME - path to Java home
rem 
rem If a suitable Java is found at one of these locations, then 
rem _PENTAHO_JAVA_HOME is set to that location and _PENTAHO_JAVA is set to the 
rem absolute path of the Java launcher at that location. If none of these 
rem locations are suitable, then _PENTAHO_JAVA_HOME is set to empty string and 
rem _PENTAHO_JAVA is set to java.exe.
rem 
rem Finally, there is one final optional environment variable: PENTAHO_JAVA.
rem If set, this value is used in the construction of _PENTAHO_JAVA. If not 
rem set, then the value java.exe is used. 
rem ---------------------------------------------------------------------------

if not "%PENTAHO_JAVA%" == "" goto gotPentahoJava
set __LAUNCHER=java.exe
goto checkPentahoJavaHome

:gotPentahoJava
set __LAUNCHER=%PENTAHO_JAVA%
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
set _PENTAHO_JAVA_HOME=%PENTAHO_JAVA_HOME%
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJreCurrentFolder
echo DEBUG: Found JRE at the current folder
set _PENTAHO_JAVA_HOME=%~dp0jre
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJavaCurrentFolder
echo DEBUG: Found JAVA at the current folder
set _PENTAHO_JAVA_HOME=%~dp0java
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJreOneFolderUp
echo DEBUG: Found JRE one folder up
set _PENTAHO_JAVA_HOME=%~dp0..\jre
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJavaOneFolderUp
echo DEBUG: Found JAVA one folder up
set _PENTAHO_JAVA_HOME=%~dp0..\java
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJreTwoFolderUp
echo DEBUG: Found JRE two folder up
set _PENTAHO_JAVA_HOME=%~dp0..\..\jre
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJavaTwoFolderUp
echo DEBUG: Found JAVA two folder up
set _PENTAHO_JAVA_HOME=%~dp0..\..\java
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJdkHome
echo DEBUG: Using JAVA_HOME
set _PENTAHO_JAVA_HOME=%JAVA_HOME%
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotJreHome
echo DEBUG: Using JRE_HOME
set _PENTAHO_JAVA_HOME=%JRE_HOME%
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotValueFromCaller
echo DEBUG: Using value (%~1) from calling script
set _PENTAHO_JAVA_HOME=%~1
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%
goto end

:gotPath
echo WARNING: Using java from path

REM # Try to get java.home from java itself
FOR /F "tokens=2 delims==" %%a IN ('%__LAUNCHER% -XshowSettings:properties -version 2^>^&1^|findstr "java.home"') DO (SET _PENTAHO_JAVA_HOME=%%~a)
REM # Trim spaces
FOR /F "tokens=* delims= " %%a IN ("%_PENTAHO_JAVA_HOME%") DO (SET _PENTAHO_JAVA_HOME=%%~a)

if exist "%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%" (
	echo DEBUG: Getting java.home from java settings
	set "_PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\%__LAUNCHER%"
) else (
	set _PENTAHO_JAVA_HOME=
	set _PENTAHO_JAVA=%__LAUNCHER%
)

goto end

:end

echo DEBUG: _PENTAHO_JAVA_HOME=%_PENTAHO_JAVA_HOME%
echo DEBUG: _PENTAHO_JAVA=%_PENTAHO_JAVA%
