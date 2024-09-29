@ECHO OFF
set CP=lib/*
java -Xms1024m -Xmx2048m -cp "%CP%" org.hitachivantara.importer.utility.ImporterUtility
