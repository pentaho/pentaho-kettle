@echo off
setlocal
cd /D %~dp0
call "%~dp0set-pentaho-env.bat"

"%_PENTAHO_JAVA%" -Xmx2048m -XX:MaxPermSize=256m -classpath "%~dp0plugins\pdi-pur-plugin\*;%~dp0lib\*" com.pentaho.di.purge.RepositoryCleanupUtil %*
