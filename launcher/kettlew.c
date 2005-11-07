
#include <unistd.h>
#include <windows.h>
#include <strings.h>

#define JAVA_CMD "javaw.exe"

int WINAPI WinMain (HINSTANCE hInstance, 
		HINSTANCE hPrevInstance, 
		LPSTR cmdLine, 
		int iCmdShow) 
{
	////////////////////////////////////////////////
	// DEBUGING CODE
	// char sCmd[4096];
	// sprintf(sCmd, "%s %s\n", JAVA_CMD, cmdLine);
	// sprintf(sCmd, "calc.exe");
	// MessageBox (NULL, sCmd, "command:", MB_OK);
	// system(sCmd);

	ShellExecute(0,                           
			   "open",                      // Operation to perform
			   JAVA_CMD,  			// Application name
			   cmdLine,		        // Additional parameters
			   0,                           // Default directory
			   SW_SHOW);

	return (0);
}

