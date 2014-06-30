@echo off
echo SpoonDebug is to support you in finding unusual errors and start problems.
echo -

set SPOON_CONSOLE=1

echo This starts Spoon with a console output with the following options:
echo -
echo Pause after the termination?
echo (helps in finding start problems and unusual crashes of the JVM)
choice /C NYC /N /M "Pause? (Y=Yes, N=No, C=Cancel)"
if errorlevel == 3 exit
if errorlevel == 2 set SPOON_PAUSE=1

echo -
echo Set logging level to Debug? (default: Basic logging)
choice /C NYC /N /M "Debug? (Y=Yes, N=No, C=Cancel)"
if errorlevel == 3 exit
if errorlevel == 2 set SPOON_OPTIONS=/level:Debug

echo -
echo Redirect console output to SpoonDebug.txt in the actual Spoon directory?
choice /C NYC /N /M "Redirect to SpoonDebug.txt? (Y=Yes, N=No, C=Cancel)"
if errorlevel == 3 exit
if errorlevel == 2 set SPOON_REDIRECT=1
REM We need to disable the pause in this case otherwise the user does not see the pause message
if errorlevel == 2 set SPOON_PAUSE=

echo -
echo Launching Spoon: "%~dp0spoon.bat" %SPOON_OPTIONS%
if not "%SPOON_REDIRECT%"=="1" "%~dp0spoon.bat" %SPOON_OPTIONS%
if "%SPOON_REDIRECT%"=="1" echo Console output gets redirected to "%~dp0SpoonDebug.txt"
if "%SPOON_REDIRECT%"=="1" "%~dp0spoon.bat" %SPOON_OPTIONS% >>"%~dp0SpoonDebug.txt" 2>&1

