rem ---------------------------------------------------------------------------
rem Set environment variables for pentaho
rem
rem Calls set-pentaho-java to find PENTAHO_JAVA and PENTAHO_JAVA_HOME
rem 
rem Then searches to find the pentaho installed licence file
rem ---------------------------------------------------------------------------

if exist %1 goto callWithParam
call "%~dp0set-pentaho-java.bat"
goto checkLicense

:callWithParam
call "%~dp0set-pentaho-java.bat" %1
goto checkLicense


:checkLicense
if "%PENTAHO_INSTALLED_LICENSE_PATH%" == "" goto findLicensePath
goto end

:findLicensePath
if exist "%~dp0.installedLicenses.xml" goto foundLicenseCurrentFolder
if exist "%~dp0..\.installedLicenses.xml" goto foundLicenseOneFolderUp
if exist "%~dp0..\..\.installedLicenses.xml" goto foundLicenseTwoFoldersUp
goto end
:foundLicenseCurrentFolder:
echo DEBUG: Found Pentaho License at the current folder
set PENTAHO_INSTALLED_LICENSE_PATH=%~dp0.installedLicenses.xml
goto end
:foundLicenseOneFolderUp:
echo DEBUG: Found Pentaho License one folder up
set PENTAHO_INSTALLED_LICENSE_PATH=%~dp0..\.installedLicenses.xml
goto end
:foundLicenseTwoFoldersUp:
echo DEBUG: Found Pentaho License two folders up
set PENTAHO_INSTALLED_LICENSE_PATH=%~dp0..\..\.installedLicenses.xml
goto end
:end
echo DEBUG: PENTAHO_INSTALLED_LICENSE_PATH=%PENTAHO_INSTALLED_LICENSE_PATH%
