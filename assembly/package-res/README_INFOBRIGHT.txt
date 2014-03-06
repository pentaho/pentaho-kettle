Dear InfoBright user,

If you want to use the InfoBright bulk loader step under Windows, make sure to copy one of either file to your Windows system path (for example %WINDIR%/System32/): 

	libswt/win32/infobright_jni_64bit.dll (Windows 64-bit)
	libswt/win32/infobright_jni.dll       (Windows 32-bit)

Rename the file to:

	infobright_jni.dll

Background: When using Spoon, the DLLs are picked up in libswt/win32. When using without the user interface (Pan, Kitchen etc.) the DLLs must be in the Windows system path.

The Pentaho team.
