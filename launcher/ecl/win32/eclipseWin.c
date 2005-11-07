/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kevin Cornell (Rational Software Corporation)
 *******************************************************************************/

#include "eclipseOS.h"

#include <windows.h>
#include <commctrl.h>
#include <process.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#ifdef __MINGW32__
#include <stdlib.h>
#endif

#define ECLIPSE_ICON  401

/* Global Variables */
_TCHAR   dirSeparator  = _T('\\');
_TCHAR   pathSeparator = _T(';');
_TCHAR*  consoleVM     = _T("java.exe");
_TCHAR*  defaultVM     = _T("javaw.exe");
_TCHAR*  shippedVMDir  = _T("jre\\bin\\");

/* Define the window system arguments for the Java VM. */
static _TCHAR*  argVM[] = { NULL };

/* Define local variables for the main window. */
static HWND    topWindow  = 0;
static WNDPROC oldProc;

/* Define local variables for running the JVM and detecting its exit. */
static int     jvmProcess     = 0;
static int     jvmExitCode    = 0;
static int     jvmExitTimeout = 100;
static int     jvmExitTimerId = 99;

/* Define local variables for handling the splash window and its image. */
static int      splashTimerId = 88, inputTimerId = 89;

static HWND label = NULL, progress = NULL;
static COLORREF foreground = 0;
static RECT progressRect = {0, 0, 0, 0}, messageRect = {0, 0, 0, 0};
static int value = 0, maximum = 100;

/* Local functions */
static void CALLBACK  detectJvmExit( HWND hwnd, UINT uMsg, UINT id, DWORD dwTime );
static HBITMAP        loadSplashImage(_TCHAR *baseDir, _TCHAR *fileName);
static void CALLBACK  splashTimeout( HWND hwnd, UINT uMsg, UINT id, DWORD dwTime );
static LRESULT WINAPI WndProc (HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);


/* Display a Message */
void displayMessage( _TCHAR* message )
{
	MessageBox( topWindow, message, officialName, MB_OK );
}

/* Initialize Window System
 *
 * Create a pop window to display the bitmap image.
 *
 * Return the window handle as the data for the splash command.
 *
 */
void initWindowSystem( int* pArgc, _TCHAR* argv[], int showSplash )
{
    /* Create a window that has no decorations. */
	InitCommonControls();
    topWindow = CreateWindowEx (0,
		_T("STATIC"),
		officialName,
		SS_BITMAP | WS_POPUP,
		0,
		0,
		0,
		0,
		NULL,
		NULL,
		GetModuleHandle (NULL),
		NULL);
	SetClassLong(topWindow, GCL_HICON, (LONG)LoadIcon(GetModuleHandle(NULL), MAKEINTRESOURCE(ECLIPSE_ICON)));
    oldProc = (WNDPROC) GetWindowLong (topWindow, GWL_WNDPROC);
    SetWindowLong (topWindow, GWL_WNDPROC, (LONG) WndProc);
}


static void readRect(_TCHAR *str, RECT *rect) {
	int x, y, width, height;
	_TCHAR *temp = str, *comma;
	comma = _tcschr(temp, _T(','));
	if (comma == NULL) return;
	comma[0] = 0;
	x = _ttoi(temp);
	temp = comma + 1;
	comma = _tcschr(temp, _T(','));
	if (comma == NULL) return;
	comma[0] = 0;
	y = _ttoi(temp);
	temp = comma + 1;
	comma = _tcschr(temp, _T(','));
	if (comma == NULL) return;
	comma[0] = 0;
	width = _ttoi(temp);
	temp = comma + 1;
	height = _ttoi(temp);
	rect->left = x;
	rect->top = y;
	rect->right = x + width;
	rect->bottom = y + height;
}

static void readColor(_TCHAR *str, COLORREF *color) {
	int value = _ttoi(str);
	*color = ((value & 0xFF0000) >> 16) | (value & 0xFF00) | ((value & 0xFF) << 16);
}

static void readInput() {
	int available;
	FILE *fd = stdin;
#ifdef _UNICODE
	WCHAR *buffer1 = NULL;
#endif
	char *buffer = NULL;
	_TCHAR *equals = NULL, *end, *line;
	HANDLE hStdin = GetStdHandle(STD_INPUT_HANDLE);
	available = GetFileSize (hStdin, NULL) - SetFilePointer (hStdin, 0, NULL, FILE_CURRENT);
	if (available <= 0) return;
	buffer = malloc(available + 1);
	if (!ReadFile(hStdin, buffer, available, &available, NULL)) {
		return;
	}
	if (available <= 0) {
		free(buffer);
		return;
	}
	buffer[available] = 0;
#ifdef _UNICODE
	{
	buffer1 = malloc((available + 1) * sizeof(TCHAR));
	available = MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, (LPCSTR)buffer, available, (LPWSTR)buffer1, available);
	buffer1[available] = 0;
	line = buffer1;
	}
#else
	line = buffer;
#endif
	while (line != NULL) {
		end = _tcschr(line, _T('\n'));
		equals = _tcschr(line, _T('='));
		if (end != NULL) end[0] = 0;
		if (equals != NULL) {
			_TCHAR *str = (_TCHAR *)equals + 1;
			equals[0] = 0;
			if (_tcscmp(line, _T("maximum")) == 0) {
				maximum = _ttoi(str);
				if (progress) {
					SendMessage (progress, PBM_SETRANGE32, 0, maximum);
				}
			} else if (_tcscmp(line, _T("value")) == 0) {
				value = _ttoi(str);
				if (progress) {
					SendMessage (progress, PBM_SETPOS, value, 0);
				}
			} else if (_tcscmp(line, _T("progressRect")) == 0) {
				readRect(str, &progressRect);
				if (progress) {
					int flags = SWP_NOZORDER | SWP_DRAWFRAME | SWP_NOACTIVATE;
					SetWindowPos (progress, 0, progressRect.left, progressRect.top, progressRect.right - progressRect.left, progressRect.bottom - progressRect.top, flags);
				}
			} else if (_tcscmp(line, _T("messageRect")) == 0) {
				readRect(str, &messageRect);
				if (label) {
					int flags = SWP_NOZORDER | SWP_DRAWFRAME | SWP_NOACTIVATE;
					SetWindowPos (label, 0, messageRect.left, messageRect.top, messageRect.right - messageRect.left, messageRect.bottom - messageRect.top, flags);
				}
			} else if (_tcscmp(line, _T("foreground")) == 0) {
				readColor(str, &foreground);
				if (label) {
					RECT rect;
					GetWindowRect (label, &rect);
					MapWindowPoints (0, topWindow, (POINT *)&rect, 2);
					InvalidateRect (topWindow, &rect, 1);
				}
			} else if (_tcscmp(line, _T("message")) == 0) {
				if (label) {
					RECT rect;
					SetWindowText (label, str);
					GetWindowRect (label, &rect);
					MapWindowPoints (0, topWindow, (POINT *)&rect, 2);
					InvalidateRect (topWindow, &rect, 1);
				}
			}
			
		}
		if (end != NULL) line = end + 1;
		else line = NULL;
	}
	free(buffer);
#ifdef _UNICODE
	if (buffer1 != NULL) free(buffer1);
#endif
}

static void CALLBACK timerProc( HWND hwnd, UINT uMsg, UINT id, DWORD dwTime ) {
	readInput();
}

/* Show the Splash Window
 *
 * Open the bitmap, insert into the splash window and display it.
 *
 */
int showSplash( _TCHAR* timeoutString, _TCHAR* featureImage )
{
	int     timeout = 0;
    RECT    rect;
    HBITMAP hBitmap = 0;
    HDC     hDC;
    int     depth;
    int     x, y;
    int     width, height;
    MSG     msg;

	/* Determine the splash timeout value (in seconds). */
	if (timeoutString != NULL && _tcslen( timeoutString ) > 0)
	{
	    _stscanf( timeoutString, _T("%d"), &timeout );
	}

    /* Load the bitmap for the feature. */
    hDC = GetDC( NULL);
    depth = GetDeviceCaps( hDC, BITSPIXEL ) * GetDeviceCaps( hDC, PLANES);
    ReleaseDC(NULL, hDC);
    if (featureImage != NULL)
    	hBitmap = LoadImage(NULL, featureImage, IMAGE_BITMAP, 0, 0, LR_LOADFROMFILE);

    /* If the bitmap could not be found, return an error. */
    if (hBitmap == 0)
    	return ERROR_FILE_NOT_FOUND;

	/* Load the bitmap into the splash popup window. */
    SendMessage( topWindow, STM_SETIMAGE, IMAGE_BITMAP, (LPARAM) hBitmap );

	progress = CreateWindowEx (0, _T("msctls_progress32"),
		_T(""),
		WS_CHILD | WS_VISIBLE | WS_CLIPSIBLINGS,
		0,
		0,
		0,
		0,
		topWindow,
		NULL,
		GetModuleHandle (NULL),
		NULL);
	label = CreateWindowEx (0, _T("STATIC"),
		_T(""),
		WS_CHILD | WS_VISIBLE | WS_CLIPSIBLINGS,
		0,
		0,
		0,
		0,
		topWindow,
		NULL,
		GetModuleHandle (NULL),
		NULL);		
 	SendMessage (label, WM_SETFONT, (WPARAM)GetStockObject (DEFAULT_GUI_FONT), (LPARAM)1);		
		
	readInput();
    SetTimer( topWindow, inputTimerId, 50, timerProc );

    /* Centre the splash window and display it. */
    GetWindowRect (topWindow, &rect);
    width = GetSystemMetrics (SM_CXSCREEN);
    height = GetSystemMetrics (SM_CYSCREEN);
    x = (width - (rect.right - rect.left)) / 2;
    y = (height - (rect.bottom - rect.top)) / 2;
    SetWindowPos (topWindow, 0, x, y, 0, 0, SWP_NOZORDER | SWP_NOSIZE | SWP_NOACTIVATE);
    ShowWindow( topWindow, SW_SHOW );
    BringWindowToTop( topWindow );

	/* If a timeout for the splash window was given */
	if (timeout != 0)
	{
		/* Add a timeout (in milliseconds) to bring down the splash screen. */
        SetTimer( topWindow, splashTimerId, (timeout * 1000), splashTimeout );
	}

    /* Process messages until the splash window is closed or process is terminated. */
   	while (GetMessage( &msg, NULL, 0, 0 ))
   	{
		TranslateMessage( &msg );
		DispatchMessage( &msg );
	}

	return 0;
}


/* Get the window system specific VM args */
_TCHAR** getArgVM( _TCHAR *vm )
{
	return argVM;
}


/* Start the Java VM
 *
 * This method is called to start the Java virtual machine and to wait until it
 * terminates. The function returns the exit code from the JVM.
 */
int startJavaVM( _TCHAR* args[] )
{
    MSG   msg;
	int   index, length;
	_TCHAR *commandLine, *ch, *space;

	/*
	* Build the command line. Any argument with spaces must be in
	* double quotes in the command line. 
	*/
	length = 0;
	for (index = 0; args[index] != NULL; index++)
	{
		/* String length plus space character */
		length += _tcslen( args[ index ] ) + 1;
		/* Quotes */
		if (_tcschr( args[ index ], _T(' ') ) != NULL) length += 2;
	}
	commandLine = ch = malloc ( (length + 1) * sizeof(_TCHAR) );
	for (index = 0; args[index] != NULL; index++)
	{
		space = _tcschr( args[ index ], _T(' '));
		if (space != NULL) *ch++ = _T('\"');
		_tcscpy( ch, args[index] );
		ch += _tcslen( args[index] );
		if (space != NULL) *ch++ = _T('\"');
		*ch++ = _T(' ');
	}
	*ch = _T('\0');

	/*
	* Start the Java virtual machine. Use CreateProcess() instead of spawnv()
	* otherwise the arguments cannot be freed since spawnv() segments fault.
	*/
	{
	STARTUPINFO    si;
    PROCESS_INFORMATION  pi;
    GetStartupInfo(&si);
    if (CreateProcess(NULL, commandLine, NULL, NULL, TRUE, 0, NULL, NULL, &si, &pi)) {
    	CloseHandle( pi.hThread );
    	jvmProcess = (int)pi.hProcess;
    }    
	}

	free( commandLine );

	/* If the child process (JVM) would not start */
	if (jvmProcess == -1)
	{
		/* Return the error number. */
		jvmExitCode = errno;
		jvmProcess  = 0;
	}

	/* else */
	else
	{
        /* Set a timer to detect JVM process termination. */
        SetTimer( topWindow, jvmExitTimerId, jvmExitTimeout, detectJvmExit );

    	/* Process messages until the JVM terminates.
    	   This launcher process must continue to process events until the JVM exits
    	   or else Windows 2K will hang if the desktop properties (e.g., background) are
    	   changed by the user. Windows does a SendMessage() to every top level window
    	   process, which blocks the caller until the process responds. */
   		while (jvmProcess != 0)
   		{
   			GetMessage( &msg, NULL, 0, 0 );
			TranslateMessage( &msg );
			DispatchMessage( &msg );
		}

		/* Kill the timer. */
        KillTimer( topWindow, jvmExitTimerId );
	}

	/* Return the exit code from the JVM. */
	return jvmExitCode;
}

/* Local functions */

/* Detect JVM Process Termination */
static void CALLBACK detectJvmExit( HWND hwnd, UINT uMsg, UINT id, DWORD dwTime )
{
    DWORD   exitCode;

    /* If the JVM process has terminated */
    if (!GetExitCodeProcess( (HANDLE)jvmProcess, &exitCode ) ||
    		 exitCode != STILL_ACTIVE)
    {
    	/* Save the JVM exit code. This should cause the loop in startJavaVM() to exit. */
        jvmExitCode = exitCode;
        jvmProcess = 0;
    }
}

/* Splash Timeout */
static void CALLBACK splashTimeout( HWND hwnd, UINT uMsg, UINT id, DWORD dwTime )
{
	/* Kill the timer. */
    KillTimer( topWindow, id );
	PostMessage( topWindow, WM_QUIT, 0, 0 );
}

/* Window Procedure for the Spash window.
 *
 * A special WndProc is needed to return the proper vlaue for WM_NCHITTEST.
 * It must also detect the message from the splash window process.
 */
static LRESULT WINAPI WndProc (HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	switch (uMsg)
	{
		case WM_NCHITTEST: return HTCLIENT;
		case WM_CLOSE:
	    	PostQuitMessage(  0 );
	    	break;
		case WM_CTLCOLORSTATIC:
			if ((HWND)lParam == label) {
				SetTextColor((HDC)wParam, foreground);
				SetBkMode((HDC)wParam, TRANSPARENT);
				return (LRESULT)GetStockObject (NULL_BRUSH);
			}
			break;
	}
	return CallWindowProc (oldProc, hwnd, uMsg, wParam, lParam);
}
