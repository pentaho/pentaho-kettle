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

/* Eclipse Program Launcher
 *
 * This program performs the launching of the java VM used to
 * start the Eclipse or RCP java application.
 * As an implementation detail, this program serves two other
 * purposes: display a splash window and write to a segment
 * of shared memory.
 *
 * The java application receives the following arguments.
 * -launcher <launcher absolute name. e.g. d:\eclipse\eclipse.exe>
 * -name <application name. e.g. Eclipse>
 * If the splash window is to be displayed, the java application
 * will receive two extra arguments:
 *     -showsplash  <splash time out in seconds e.g. 600>
 *
 * When the Java program starts, it should determine the location of
 * the splash bitmap to be used. The Java program initiates the 
 * displaying of the splash window by executing the splash command 
 * as follows:
 *
 * Process splashProcess = Runtime.getRuntime().exec( array );
 * Where array stores String arguments in the following order:
 * 0. <launcher absolute name. e.g. d:\eclipse\eclipse.exe>
 * 1. -name
 * 2. <application name. e.g. Eclipse>
 * 3. -showsplash
 * 4. <splash time out in seconds e.g. 600>
 * 5. <absolute path of the splash screen e.g. d:\eclipse\splash.bmp>
 *
 * When the Java program initialization is complete, the splash window
 * is brought down by destroying the splash process as follows:
 *
 *     splashProcess.destroy();
 *
 * Therefore, when the splash window is visible, there are actually three
 * processes running: 
 * 1) the main launcher process 
 * 2) the Java VM process (Eclipse or RCP application) 
 * 3) the splash window process.
 *
 * The splash screen process can also show progress information. The
 * communication between the Java VM process and the splash screen
 * process is done through the standard input pipe. Messages sent to
 * the splash screen process have this format:
 *
 *    <key>=<value><LF>
 *
 * and the recognized messages are:
 *
 *    value=50\n (changes the current progress value)
 *    maximum=100\n (changes the maximum progress value. Default is 100)
 *    message=starting...\n (changes the displayed message. Any char except LF is allowed)
 *    foreground=RRGGBB\n  (changes the foreground color of the message, i.e. cyan=(0 << 16 | 255 << 8 | 255 << 0))
 *    messageRect=10,10,100,20\n (changes the rectangle(x,y,width,height) where the message is displayed)
 *    progressRect=10,30,100,15\n (changes the rectangle(x,y,width,height) where the progress is displayed)
 *
 * Similarly, the Java application will receive two other arguments:
 *    -exitdata <shared memory id>
 *
 * The exitdata command can be used by the Java application
 * to provide specific exit data to the main launcher process. The 
 * following causes another instance of the launcher to write to the 
 * segment of shared memory previously created by the
 * main launcher.
 *
 * Process exitDataProcess = Runtime.getRuntime().exec( array );
 * exitDataProcess.waitFor();
 * Where array stores String arguments in the following order:
 * 0. <launcher absolute name. e.g. d:\eclipse\eclipse.exe>
 * 1. -name
 * 2. <application name. e.g. Eclipse>
 * 3. -exitdata
 * 4. <shared memory id e.g. c60_7b4>
 * 5. <exit data that either contain a series of characters>
 *
 * The exit data size must not exceed MAX_SHARED_LENGTH which is
 * 16Kb. The exit data process will exit with an exit code
 * different than 0 if that happens. The interpretation of the
 * exit data is dependent on the exit value of the java application.
 *
 * The main launcher recognizes the following exit codes from the
 * Java application:
 *
 *    0
 *       - Exit normally.
 *    RESTART_LAST_EC = 23
 *       - restart the java VM again with the same arguments as the previous one.
 *    RESTART_NEW_EC  = 24
 *       - restart the java VM again with the arguments taken from the exit data.
 *       The exit data format is a list of arguments separated by '\n'. The Java
 *       application should build this list using the arguments passed to it on
 *       startup. See below.
 *
 * Additionally, if the Java application exits with an exit code other than the
 * ones above, the main launcher will display an error message with the contents
 * of the exit data. If the exit data is empty, a generic error message is
 * displayed. The generic error message shows the exit code and the arguments
 * passed to the Java application.
 *
 * The options that can be specified by the user to the launcher are:
 *  -vm <javaVM>               the Java VM to be used
 *  -os <opSys>                the operating system being run on
 *  -arch <osArch>             the hardware architecture of the OS: x86, sparc, hp9000
 *  -ws <gui>                  the window system to be used: win32, motif, gtk, ...
 *  -nosplash                  do not display the splash screen. The java application will
 *                             not receive the -showsplash command.
 *  -name <name>               application name displayed in error message dialogs and
 *                             splash screen window. Default value is computed from the
 *                             name of the executable - with the first letter capitalized
 *                             if possible. e.g. eclipse.exe defaults to the name Eclipse.
 *  -startup                   the startup jar to execute. The argument is first assumed to be
 *                             relative to the path of the launcher. If such a file does not
 *                             exist, the argument is then treated as an absolute path.
 *                             The default is to execute a jar called startup.jar in the folder
 *                             where the launcher is located.
 *                             The jar must be an executable jar.
 *                             e.g. -startup myfolder/myJar.jar will cause the launcher to start
 *                             the application: java -jar <launcher folder>/myfolder/myJar.jar
 *  <userArgs>                 arguments that are passed along to the Java application
 *                             (i.e, -data <path>, -debug, -console, -consoleLog, etc) 
 *  -vmargs <userVMargs> ...   a list of arguments for the VM itself
 *
 * The -vmargs option and all user specified VM arguments must appear
 * at the end of the command line, after all arguments that are
 * being passed to Java application. 
 *
 * The argument order for the new Java VM process is as follows:
 *
 * <javaVM> <all VM args>
 *     -os <user or default OS value>
 *     -ws <user or default WS value>
 *     -arch <user or default ARCH value>
 *     -launcher <absolute launcher name>
 *     -name <application name>
 *     [-showsplash <splash time out>]
 *     [-exitdata <shared memory id>]
 *     <userArgs>
 *     -vm <javaVM>
 *     -vmargs <all VM args>
 *
 * where:
 *   <all VM args> =
 *     [<defaultVMargs | <userVMargs>]
 *     -jar
 *     <startup jar full path>
 *	
 * The startup jar must be an executable jar.
 * 
 *
 * See "Main.java" for a simple implementation of the Java
 * application.
 *
 * Configuration file
 *   The launcher gets arguments from the command line and/or from a configuration file.
 * The configuration file must have the same name and location as the launcher executable
 * and the extension .ini. For example, the eclipse.ini configuration file must be
 * in the same folder as the eclipse.exe or eclipse executable.
 *   The format of the ini file matches that of the command line arguments - one
 * argument per line.
 *   In general, the settings of the config file are expected to be overriden by the
 * command line.
 *   - launcher arguments (-os, -arch...) set in the config file are overriden by the command line
 *   - the -vmargs from the command line replaces in its entirety the -vmargs from the config file.
 *   - user arguments from the config file are prepended to the user arguments defined in the
 *     config file. This is consistent with the java behaviour in the following case:
 *     java -Dtest="one" -Dtest="two" ...  : test is set to the value "two"
 */

#include "eclipseOS.h"
#include "eclipseShm.h"
#include "eclipseConfig.h"

#ifdef _WIN32
#include <direct.h>
#else
#include <unistd.h>
#include <strings.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>
#include <ctype.h>

#define MAX_PATH_LENGTH   2000
#define MAX_SHARED_LENGTH   (16 * 1024)

/* Global Variables */
_TCHAR* officialName = NULL;

/* Global Data */
static _TCHAR*  program     = NULL;       /* full pathname of the program */
static _TCHAR*  programDir  = NULL;       /* directory where program resides */
static _TCHAR*  javaVM      = NULL;       /* full pathname of the Java VM to run */
static _TCHAR*  jarFile     = NULL;		  /* full pathname of the startup jar file to run */
static _TCHAR*  sharedID    = NULL;       /* ID for the shared memory */

/* Define the special exit codes returned from Eclipse. */
#define RESTART_LAST_EC    23
#define RESTART_NEW_EC     24

/* Define the maximum time (in seconds) for the splash window to remain visible. */
static _TCHAR*  splashTimeout = _T_ECLIPSE("600");   /* 10 minutes */

/* Define error messages. (non-NLS) */
static _TCHAR* exitMsg = _T_ECLIPSE("JVM terminated. Exit code=%d\n%s");
static _TCHAR* goVMMsg = _T_ECLIPSE("Start VM: %s\n");
static _TCHAR* pathMsg = _T_ECLIPSE("%s\n'%s' in your current PATH");
static _TCHAR* showMsg = _T_ECLIPSE("Could not load splash bitmap:\n%s");
static _TCHAR* shareMsg = _T_ECLIPSE("No shared data available.");
static _TCHAR* noVMMsg =
_T_ECLIPSE("A Java Runtime Environment (JRE) or Java Development Kit (JDK)\n\
must be available in order to run %s. No Java virtual machine\n\
was found after searching the following locations:\n\
%s");

static _TCHAR* homeMsg =
_T_ECLIPSE("The %s executable launcher was unable to locate its \n\
companion startup.jar file (in the same directory as the executable).");

#define DEFAULT_STARTUP _T_ECLIPSE("startup.jar")

/* Define constants for the options recognized by the launcher. */
#define CONSOLE      _T_ECLIPSE("-console")
#define CONSOLELOG   _T_ECLIPSE("-consoleLog")
#define DEBUG        _T_ECLIPSE("-debug")
#define OS           _T_ECLIPSE("-os")
#define OSARCH       _T_ECLIPSE("-arch")
#define NOSPLASH     _T_ECLIPSE("-nosplash")
#define LAUNCHER     _T_ECLIPSE("-launcher")
#define SHOWSPLASH   _T_ECLIPSE("-showsplash")
#define EXITDATA     _T_ECLIPSE("-exitdata")
#define STARTUP      _T_ECLIPSE("-startup")
#define VM           _T_ECLIPSE("-vm")
#define WS           _T_ECLIPSE("-ws")
#define NAME         _T_ECLIPSE("-name")
#define VMARGS       _T_ECLIPSE("-vmargs")					/* special option processing required */

/* Define the variables to receive the option values. */
static int     needConsole   = 0;				/* True: user wants a console	*/
static int     debug         = 0;				/* True: output debugging info	*/
static int     noSplash      = 0;				/* True: do not show splash win	*/
static _TCHAR*  osArg         = _T_ECLIPSE(DEFAULT_OS);
static _TCHAR*  osArchArg     = _T_ECLIPSE(DEFAULT_OS_ARCH);
static _TCHAR*  showSplashArg = NULL;			/* showsplash data (main launcher window) */
static _TCHAR*  exitDataArg   = NULL;
static _TCHAR * startupArg    = DEFAULT_STARTUP; /* path of the startup.jar the user wants to run relative to the program path */
static _TCHAR*  vmName        = NULL;     		/* Java VM that the user wants to run */
static _TCHAR*  wsArg         = _T_ECLIPSE(DEFAULT_WS);	/* the SWT supported GUI to be used */
static _TCHAR*  name          = NULL;			/* program name */		
static _TCHAR** userVMarg     = NULL;     		/* user specific args for the Java VM  */

/* Define a table for processing command line options. */
typedef struct
{
	_TCHAR*  name;		/* the option recognized by the launcher */
	_TCHAR** value;		/* the variable where the option value is saved */
	int*   flag;		/* the variable that is set if the option is defined */
	int    remove;		/* the number of argments to remove from the list */
} Option;

static Option options[] = {
    { CONSOLE,		NULL,			&needConsole,	0 },
    { CONSOLELOG,	NULL,			&needConsole,	0 },
    { DEBUG,		NULL,			&debug,			0 },
    { NOSPLASH,     NULL,           &noSplash,		1 },
    { OS,			&osArg,			NULL,			2 },
    { OSARCH,		&osArchArg,		NULL,			2 },
    { SHOWSPLASH,   &showSplashArg,	NULL,			2 },
    { EXITDATA,		&exitDataArg,	NULL,			2 },
    { STARTUP,		&startupArg,	NULL,			2 },
    { VM,           &vmName,		NULL,			2 },
    { NAME,         &name,			NULL,			2 },
    { WS,			&wsArg,			NULL,			2 } };
static int optionsSize = (sizeof(options) / sizeof(options[0]));

static int configArgc = 0;
static _TCHAR** configArgv = NULL;

/* Define the required VM arguments (all platforms). */
static _TCHAR* jar = _T_ECLIPSE("-jar");
static _TCHAR**  reqVMarg[] = { &jar, &jarFile, NULL };

/* Local methods */
static int createUserArgs(int configArgc, _TCHAR **configArgv, int *argc, _TCHAR ***argv);
static void   parseArgs( int* argc, _TCHAR* argv[] );
static _TCHAR** parseArgList( _TCHAR *data );
static void   freeArgList( _TCHAR** data );
static _TCHAR** getVMCommand( int argc, _TCHAR* argv[] );
       _TCHAR*  findCommand( _TCHAR* command );
static _TCHAR*  formatVmCommandMsg( _TCHAR* args[] );
       _TCHAR*  getProgramDir();
static _TCHAR* getDefaultOfficialName();
static int isMainEclipse( int argc, _TCHAR **argv );

#ifdef _WIN32
#ifdef UNICODE
extern int main(int, char**);
int mainW(int, wchar_t**);
int wmain( int argc, wchar_t** argv ) {
	OSVERSIONINFOW info;
	info.dwOSVersionInfoSize = sizeof(OSVERSIONINFOW);
	/*
	* If the OS supports UNICODE functions, run the UNICODE version
	* of the main function. Otherwise, convert the arguments to
	* MBCS and run the ANSI version of the main function.
	*/
	if (!GetVersionExW (&info)) {
		int i, result;
		char **newArgv = malloc(argc * sizeof(char *));
		for (i=0; i<argc; i++) {
			wchar_t *oldArg = argv[i];
			int byteCount = WideCharToMultiByte (CP_ACP, 0, oldArg, -1, NULL, 0, NULL, NULL);
			char *newArg  = malloc(byteCount+1);
			newArg[byteCount] = 0;
			WideCharToMultiByte (CP_ACP, 0, oldArg, -1, newArg, byteCount, NULL, NULL);
			newArgv[i] = newArg;
		}
		result = main(argc, newArgv);
		for (i=0; i<argc; i++) {
			free(newArgv[i]);
		}
		free(newArgv);
		return result;
	}
	return mainW(argc, argv);
}
#define main mainW
#endif /* UNICODE */
#endif /* _WIN32 */

int main( int argc, _TCHAR* argv[] )
{
	_TCHAR*   splashBitmap;
    _TCHAR*   ch;
    _TCHAR*   data;
    _TCHAR*   shippedVM    = NULL;
    _TCHAR*   vmSearchPath = NULL;
    _TCHAR**  vmCommand = NULL;
    _TCHAR**  vmCommandList = NULL;
    _TCHAR**  vmCommandArgs = NULL;
    _TCHAR*   vmCommandMsg = NULL;
    _TCHAR*   errorMsg;
    int       exitCode;
    struct _stat stats;
    
	/* 
	 * Strip off any extroneous <CR> from the last argument. If a shell script
	 * on Linux is created in DOS format (lines end with <CR><LF>), the C-shell
	 * does not strip off the <CR> and hence the argument is bogus and may 
	 * not be recognized by the launcher or eclipse itself.
	 */
	 ch = _tcschr( argv[ argc - 1 ], _T_ECLIPSE('\r') );
	 if (ch != NULL)
	 {
	     *ch = _T_ECLIPSE('\0');
	 }
	 
	/* Determine the full pathname of this program. */
    program = findCommand( argv[0] );
    if (program == NULL)
    {
#ifdef _WIN32
    	program = malloc( MAX_PATH_LENGTH + 1 );
    	GetModuleFileName( NULL, program, MAX_PATH_LENGTH );
#else
    	program = malloc( strlen( argv[0] ) + 1 );
    	strcpy( program, argv[0] );
#endif
    }
	
	/* Parse configuration file arguments */
	if (isMainEclipse(argc, argv) && readConfigFile(program, argv[0], &configArgc, &configArgv) == 0)
	{
		parseArgs (&configArgc, configArgv);
	}
	
    /* Parse command line arguments (looking for the VM to use). */
    /* Override configuration file arguments */
    parseArgs( &argc, argv );

	/* Special case - user arguments specified in the config file
	 * are appended to the user arguments passed from the command line.
	 */
	if (configArgc > 1)
	{	
		createUserArgs(configArgc, configArgv, &argc, &argv);
	}

	/* Initialize official program name */
	officialName = name != NULL ? _tcsdup( name ) : getDefaultOfficialName();

    /* Initialize the window system. */
    initWindowSystem( &argc, argv, (showSplashArg != NULL) );

    /* Find the directory where the Eclipse program is installed. */
    programDir = getProgramDir();
    if (programDir == NULL)
    {
        errorMsg = malloc( (_tcslen(homeMsg) + _tcslen(officialName) + 10) * sizeof(_TCHAR) );
        _stprintf( errorMsg, homeMsg, officialName );
        displayMessage( errorMsg );
        free( errorMsg );
    	exit( 1 );
    }

    /* If the exit data option was given, set exit data */
    if (exitDataArg != NULL)
    {
       	/* If an extra argument was given, use it as the exit data, otherwise clear exit data */
       	data = argc > 1 ? argv[1] : NULL;
       	if (data != NULL && _tcslen( data ) > MAX_SHARED_LENGTH - 1)
       	{
       		exitCode = EINVAL;       		
       	}
       	else {
	        exitCode = setSharedData( exitDataArg, data );
    	}
   	    if (exitCode != 0 && debug) displayMessage( shareMsg );
		exit( exitCode );
    }

    /* If the showsplash option was given */
    if (showSplashArg != NULL && argc > 1)
    {
    	splashBitmap = argv[1];
    	exitCode = showSplash( showSplashArg, splashBitmap );
    	if (exitCode && debug)
    	{
        	errorMsg = malloc( (_tcslen(showMsg) + _tcslen(splashBitmap) + 10) * sizeof(_TCHAR) );
        	_stprintf( errorMsg, showMsg, splashBitmap );
        	displayMessage( errorMsg );
        	free( errorMsg );
     	}
    	exit( exitCode );
    }

    /* If the user did not specify a VM to be used */
    if (vmName == NULL)
    {
    	/* Determine which type of VM should be used. */
    	vmName = ((debug || needConsole) ? consoleVM : defaultVM);

        /* Try to find the VM shipped with eclipse. */
        shippedVM = malloc( (_tcslen( programDir ) + _tcslen( shippedVMDir ) + _tcslen( vmName ) + 10) * sizeof(_TCHAR) );
        _stprintf( shippedVM, _T_ECLIPSE("%s%s%s"), programDir, shippedVMDir, vmName );
        javaVM = findCommand( shippedVM );

        /* Format a message to indicate the default VM search path. */
        vmSearchPath = malloc( (_tcslen( pathMsg ) + _tcslen( shippedVM ) + _tcslen( vmName ) + 10) * sizeof(_TCHAR) );
        _stprintf( vmSearchPath, pathMsg, shippedVM, vmName );
        free( shippedVM );
        shippedVM = NULL;
	}

	/* If a Java VM has not been found yet */
	if (javaVM == NULL)
	{
		/* Either verify the VM specified by the user or
		   attempt to find the VM in the user's PATH. */
		javaVM = findCommand( vmName );

		/* If the VM was not found, display a message and exit. */
		if (javaVM == NULL)
		{
			if (vmSearchPath != NULL) vmName = vmSearchPath; /* used default VM searching */
        	errorMsg = malloc( (_tcslen(noVMMsg) + _tcslen(officialName) + _tcslen(vmName) + 10) * sizeof(_TCHAR) );
        	_stprintf( errorMsg, noVMMsg, officialName, vmName );
        	displayMessage( errorMsg );
        	free( errorMsg );
        	exit(1);
		}
	}

    if (createSharedData( &sharedID, MAX_SHARED_LENGTH )) {
        if (debug) {
   			if (debug) displayMessage( shareMsg );
        }
    }

	/* Construct the absolute name of the startup jar */
	jarFile = malloc( (_tcslen( programDir ) + _tcslen( startupArg ) + 1) * sizeof( _TCHAR ) );
	jarFile = _tcscpy( jarFile, programDir );
  	jarFile = _tcscat( jarFile, startupArg );

	/* If the file does not exist, treat the argument as an absolute path */
	if (_tstat( jarFile, &stats ) != 0)
	{
		free( jarFile );
		jarFile = malloc( (_tcslen( startupArg ) + 1) * sizeof( _TCHAR ) );
		jarFile = _tcscpy( jarFile, startupArg );
	}

    /* Get the command to start the Java VM. */
    vmCommandArgs = getVMCommand( argc, argv );

    /* While the Java VM should be restarted */
    vmCommand = vmCommandArgs;
    while (vmCommand != NULL)
    {
    	vmCommandMsg = formatVmCommandMsg( vmCommand );
    	if (debug) _tprintf( goVMMsg, vmCommandMsg );
    	exitCode = startJavaVM( vmCommand );
        switch( exitCode ) {
            case 0:
                vmCommand = NULL;
            	break;
            case RESTART_LAST_EC:
            	break;
            case RESTART_NEW_EC:
                if (getSharedData( sharedID, &data ) == 0) {
			    	if (vmCommandList != NULL) freeArgList( vmCommandList );
                    vmCommand = vmCommandList = parseArgList( data );
                } else {
                	vmCommand = NULL;
                    if (debug) displayMessage( shareMsg );
                }
                break;
			default:
                vmCommand = NULL;
                errorMsg = NULL;
                if (getSharedData( sharedID, &errorMsg ) == 0) {
                    if (_tcslen( errorMsg ) == 0) {
                	    free( errorMsg );
                	    errorMsg = NULL;
                    }
                } else {
                    if (debug) displayMessage( shareMsg );
                }
                if (errorMsg == NULL) {
	                errorMsg = malloc( (_tcslen(exitMsg) + _tcslen(vmCommandMsg) + 10) * sizeof(_TCHAR) );
	                _stprintf( errorMsg, exitMsg, exitCode, vmCommandMsg );
                }
	            displayMessage( errorMsg );
	            free( errorMsg );
                break;
        }
        free( vmCommandMsg );
    }

    /* Cleanup time. */
    free( jarFile );
    free( programDir );
    free( program );
    if ( vmSearchPath != NULL ) free( vmSearchPath );
    if ( vmCommandList != NULL ) freeArgList( vmCommandList );
    if ( configArgv != NULL ) freeConfig( configArgv );
    if (configArgc > 1) free( argv );
    free( officialName );
    if ( sharedID != NULL ) {
        if (destroySharedData( sharedID ) != 0) {
           if (debug) displayMessage( shareMsg );
        }
        free( sharedID );
    }

    return 0;
}

/* Return 1 if the current Eclipse is the process that starts the java IDE
 * Return 0 if it is an Eclipse used to display a splash screen or to write
 * data to a shared memory segment.
 * The main Eclipse is the only one that reads the eclipse.ini file.
 */
static int isMainEclipse( int argc, _TCHAR **argv )
{
	/* It is the main eclipse if the argument 3 is neither SHOWSPLASH nor EXITDATA */
	if (argc < 4) return 1;
	return (_tcsicmp( argv[3], SHOWSPLASH ) != 0 && _tcsicmp( argv[3], EXITDATA ) != 0);
}

/*
 * Parse arguments of the command.
 */
static void parseArgs( int* pArgc, _TCHAR* argv[] )
{
	Option* option;
    int     remArgs;
    int     index;
    int     i;

    /* Ensure the list of user argument is NULL terminated. */
    argv[ *pArgc ] = NULL;

	/* For each user defined argument (excluding the program) */
    for (index = 1; index < *pArgc; index++){
        remArgs = 0;

        /* Find the corresponding argument is a option supported by the launcher */
        option = NULL;
        for (i = 0; option == NULL && i < optionsSize; i++)
        {
        	if (_tcsicmp( argv[ index ], options[ i ].name ) == 0)
        	    option = &options[ i ];
       	}

       	/* If the option is recognized by the launcher */
       	if (option != NULL)
       	{
       		/* If the option requires a value and there is one, extract the value. */
       		if (option->value != NULL && (index+1) < *pArgc)
       			*option->value = argv[ index+1 ];

       		/* If the option requires a flag to be set, set it. */
       		if (option->flag != NULL)
       			*option->flag = 1;
       		remArgs = option->remove;
       	}

        /* All of the remaining arguments are user VM args. */
        else if (_tcsicmp( argv[ index ], VMARGS ) == 0)
        {
            userVMarg = &argv[ index+1 ];
            argv[ index ] = NULL;
            *pArgc = index;
        }

		/* Remove any matched arguments from the list. */
        if (remArgs > 0)
        {
            for (i = (index + remArgs); i <= *pArgc; i++)
            {
                argv[ i - remArgs ] = argv[ i ];
            }
            index--;
            *pArgc -= remArgs;
        }
    }
}

/*
 * Create a new array containing user arguments from the config file first and
 * from the command line second.
 * Allocate an array large enough to host all the strings passed in from
 * the argument configArgv and argv. That array is passed back to the
 * argv argument. That array must be freed with the regular free().
 * Note that both arg lists are expected to contain the argument 0 from the C
 * main method. That argument contains the path/executable name. It is
 * only copied once in the resulting list.
 *
 * Returns 0 if success.
 */
static int createUserArgs(int configArgc, _TCHAR **configArgv, int *argc, _TCHAR ***argv)
{
	 _TCHAR** newArray = (_TCHAR **)malloc((configArgc + *argc) * sizeof(_TCHAR *));

	memcpy(newArray, configArgv, configArgc * sizeof(_TCHAR *));	
	
	/* Skip the argument zero (program path and name) */
	memcpy(newArray + configArgc, *argv + 1, (*argc - 1) * sizeof(_TCHAR *));

	/* Null terminate the new list of arguments and return it. */	 
	*argv = newArray;
	*argc += configArgc - 1;
	(*argv)[*argc] = NULL;
	
	return 0;
}

/*
 * Free the memory allocated by parseArgList().
 */
static void freeArgList( _TCHAR** data ) {
	if (data == NULL) return;
	free( data [0] );
	free( data );
}

/*
 * Parse the data into a list of arguments separarted by \n.
 *
 * The list of strings returned by this function must be freed with
 * freeArgList().
 */
static _TCHAR** parseArgList( _TCHAR* data ) {
    int totalArgs = 0, dst = 0, length;
    _TCHAR *ch1, *ch2, **execArg;
    length = _tcslen( data );
    ch1 = ch2 = data;
    while ((ch2 = _tcschr( ch1, _T_ECLIPSE('\n') )) != NULL) {
    	totalArgs++;
    	ch1 = ch2 + 1;
    }
    if (ch1 != data + length) totalArgs++;
    execArg = malloc( (totalArgs + 1) * sizeof( _TCHAR* ) );
    ch1 = ch2 = data;
    while ((ch2 = _tcschr( ch1, _T_ECLIPSE('\n') )) != NULL) {
    	execArg[ dst++ ] = ch1;
    	ch2[ 0 ] = _T_ECLIPSE('\0');
    	ch1 = ch2 + 1;
    }
    if (ch1 != data + length) execArg[ dst++ ] = ch1;
    execArg[ dst++ ] = NULL;
    return execArg;
}

/*
 * Find the absolute pathname to where a command resides.
 *
 * The string returned by the function must be freed.
 */
#define EXTRA 20
_TCHAR* findCommand( _TCHAR* command )
{
    _TCHAR*  cmdPath;
    int    length;
    _TCHAR*  ch;
    _TCHAR*  dir;
    _TCHAR*  path;
    struct _stat stats;

    /* If the command was an abolute pathname, use it as is. */
    if (command[0] == dirSeparator ||
       (_tcslen( command ) > 2 && command[1] == _T_ECLIPSE(':')))
    {
        length = _tcslen( command );
        cmdPath = malloc( (length + EXTRA) * sizeof(_TCHAR) ); /* add extra space for a possible ".exe" extension */
        _tcscpy( cmdPath, command );
    }

    else
    {
        /* If the command string contains a path separator */
        if (_tcschr( command, dirSeparator ) != NULL)
        {
            /* It must be relative to the current directory. */
            length = MAX_PATH_LENGTH + EXTRA + _tcslen( command );
            cmdPath = malloc( length * sizeof (_TCHAR));
            _tgetcwd( cmdPath, length );
            if (cmdPath[ _tcslen( cmdPath ) - 1 ] != dirSeparator)
            {
                length = _tcslen( cmdPath );
                cmdPath[ length ] = dirSeparator;
                cmdPath[ length+1 ] = _T_ECLIPSE('\0');
            }
            _tcscat( cmdPath, command );
        }

        /* else the command must be in the PATH somewhere */
        else
        {
            /* Get the directory PATH where executables reside. */
            path = _tgetenv( _T_ECLIPSE("PATH") );
            if (!path)
            {
	            return NULL;
            }
            else
            {
	            length = _tcslen( path ) + _tcslen( command ) + MAX_PATH_LENGTH;
	            cmdPath = malloc( length * sizeof(_TCHAR));
	
	            /* Foreach directory in the PATH */
	            dir = path;
	            while (dir != NULL && *dir != _T_ECLIPSE('\0'))
	            {
	                ch = _tcschr( dir, pathSeparator );
	                if (ch == NULL)
	                {
	                    _tcscpy( cmdPath, dir );
	                }
	                else
	                {
	                    length = ch - dir;
	                    _tcsncpy( cmdPath, dir, length );
	                    cmdPath[ length ] = _T_ECLIPSE('\0');
	                    ch++;
	                }
	                dir = ch; /* advance for the next iteration */

#ifdef _WIN32
                    /* Remove quotes */
	                if (_tcschr( cmdPath, _T_ECLIPSE('"') ) != NULL)
	                {
	                    int i = 0, j = 0, c;
	                    length = _tcslen( cmdPath );
	                    while (i < length) {
	                        c = cmdPath[ i++ ];
	                        if (c == _T_ECLIPSE('"')) continue;
	                        cmdPath[ j++ ] = c;
	                    }
	                    cmdPath[ j ] = _T_ECLIPSE('\0');
	                }
#endif
	                /* Determine if the executable resides in this directory. */
	                if (cmdPath[0] == _T_ECLIPSE('.') &&
	                   (_tcslen(cmdPath) == 1 || (_tcslen(cmdPath) == 2 && cmdPath[1] == dirSeparator)))
	                {
	                	_tgetcwd( cmdPath, MAX_PATH_LENGTH );
	                }
	                if (cmdPath[ _tcslen( cmdPath ) - 1 ] != dirSeparator)
	                {
	                    length = _tcslen( cmdPath );
	                    cmdPath[ length ] = dirSeparator;
	                    cmdPath[ length+1 ] = _T_ECLIPSE('\0');
	                }
	                _tcscat( cmdPath, command );
	
	                /* If the file is not a directory and can be executed */
	                if (_tstat( cmdPath, &stats ) == 0 && (stats.st_mode & S_IFREG) != 0)
	                {
	                    /* Stop searching */
	                    dir = NULL;
	                }
	            }
	        }
        }
    }

#ifdef _WIN32
	/* If the command does not exist */
    if (_tstat( cmdPath, &stats ) != 0 || (stats.st_mode & S_IFREG) == 0)
    {
    	/* If the command does not end with .exe, append it an try again. */
    	length = _tcslen( cmdPath );
    	if (length > 4 && _tcsicmp( &cmdPath[ length - 4 ], _T_ECLIPSE(".exe") ) != 0)
    	    _tcscat( cmdPath, _T_ECLIPSE(".exe") );
    }
#endif

    /* Verify the resulting command actually exists. */
    if (_tstat( cmdPath, &stats ) != 0 || (stats.st_mode & S_IFREG) == 0)
    {
        free( cmdPath );
        cmdPath = NULL;
    }

    /* Return the absolute command pathname. */
    return cmdPath;
}

/*
 * Get the command and arguments to start the Java VM.
 *
 * Memory allocated by this function is assumed to be
 * deallocated when the program terminates.
 *
 * Some of the arguments returned by this function were
 * passed directly from the main( argv ) array so they
 * should not be deallocated.
 */
static _TCHAR** getVMCommand( int argc, _TCHAR* argv[] )
{
	_TCHAR** defVMarg;
    int     nDefVMarg = 0;
    int     nReqVMarg = 0;
    int     nUserVMarg = 0;
    int     totalArgs;
    _TCHAR** execArg;
    int     src;
    int     dst;

 	/* Calculate the number of user VM arguments. */
 	if (userVMarg != NULL)
 	{
	 	while (userVMarg[ nUserVMarg ] != NULL)
 			nUserVMarg++;
 	}

 	/* Calculate the number of default VM arguments. */
 	defVMarg = getArgVM( javaVM );
 	while (defVMarg[ nDefVMarg ] != NULL)
 		nDefVMarg++;

 	/* Calculate the number of required VM arguments. */
 	while (reqVMarg[ nReqVMarg ] != NULL)
 		nReqVMarg++;

    /* Allocate the arg list for the exec call.
     *  (VM + userVMargs + defaultVMargs + requiredVMargs + OS <os> + WS <ws> + ARCH <arch> + LAUNCHER <launcher> + NAME <officialName> +
     *      + SHOWSPLASH <cmd> + EXITDATA <cmd> + argv[] + VM + <vm> + VMARGS + userVMargs + defaultVMargs + requiredVMargs
     *      + NULL)
     */
    totalArgs  = 1 + nUserVMarg + nDefVMarg + nReqVMarg + 2 + 2 + 2 + 2 + 2 + 2 + 2 + argc + 2 + 1 + nUserVMarg + nDefVMarg + nReqVMarg + 1;
	execArg = malloc( totalArgs * sizeof( _TCHAR* ) );
    dst = 0;
    execArg[ dst++ ] = javaVM;

    /* If the user specified "-vmargs", add them instead of the default VM args. */
    if (userVMarg != NULL)
    {
    	for (src = 0; src < nUserVMarg; src++)
	    	execArg[ dst++ ] = userVMarg[ src ];
	}
	else
	{
    	for (src = 0; src < nDefVMarg; src++)
	    	execArg[ dst++ ] = defVMarg[ src ];
	}

    /* For each required VM arg */
	for (src = 0; src < nReqVMarg; src++)
    	execArg[ dst++ ] = *(reqVMarg[ src ]);

	/* Append the required options. */
    execArg[ dst++ ] = OS;
    execArg[ dst++ ] = osArg;
    execArg[ dst++ ] = WS;
    execArg[ dst++ ] = wsArg;
    execArg[ dst++ ] = OSARCH;
    execArg[ dst++ ] = osArchArg;

	/* Append the launcher command */
	execArg[ dst++ ] = LAUNCHER;
	execArg[ dst++ ] = program;

	/* Append the name command */
	execArg[ dst++ ] = NAME;
	execArg[ dst++ ] = 	officialName;
	
	/* Append the show splash window command, if defined. */
    if (!noSplash)
    {
        execArg[ dst++ ] = SHOWSPLASH;
        execArg[ dst++ ] = splashTimeout;
    }

	/* Append the exit data command. */
	if (sharedID) {
	    execArg[ dst++ ] = EXITDATA;
	    execArg[ dst++ ] = sharedID;
	}

	/* Append the remaining user defined arguments. */
    for (src = 1; src < argc; src++)
    {
        execArg[ dst++ ] = argv[ src ];
    }

    /* Append VM and VMARGS to be able to relaunch using exit data. */
	execArg[ dst++ ] = VM;
	execArg[ dst++ ] = javaVM;
    execArg[ dst++ ] = VMARGS;
    /* If the user specified "-vmargs", add them instead of the default VM args. */
    if (userVMarg != NULL)
    {
    	for (src = 0; src < nUserVMarg; src++)
	    	execArg[ dst++ ] = userVMarg[ src ];
	}
	else
	{
    	for (src = 0; src < nDefVMarg; src++)
	    	execArg[ dst++ ] = defVMarg[ src ];
	}
    /* For each required VM arg */
    for (src = 0; src < nReqVMarg; src++)
        execArg[ dst++ ] = *(reqVMarg[ src ]);

    execArg[ dst++ ] = NULL;

	return execArg;
 }

 /* Format the JVM start command for error messages
  *
  * This method formats a string with the JVM start command (and all arguments)
  * that can be used in displaying error messages. The string returned from this
  * method is probably not NLS compliant and must be deallocated by the caller.
  */
static _TCHAR* formatVmCommandMsg( _TCHAR* args[] )
{
	int   index;
    int   length;
    _TCHAR* ch;
    _TCHAR* message;

	/* Determine the length of the message buffer. */
	length = 0;
	for (index = 0; args[index] != NULL; index++)
	{
		length += _tcslen(args[index]) + 1;
	}
	message = malloc( (length + 5) * sizeof(_TCHAR) );
	
	/* Format the message such that options (args starting with '-') begin
	   on a new line. Otherwise, the Motif MessageBox does not automatically wrap
	   the messages and the message window can extend beyond both sides of the display. */
	ch = message;
	for (index = 0; args[index] != NULL; index++)
	{
		if (args[index][0] == _T_ECLIPSE('-') && *(ch-1) == _T_ECLIPSE(' '))
			*(ch-1) = _T_ECLIPSE('\n');
		_tcscpy( ch, args[index] );
		ch += _tcslen( args[index] );
		*ch++ = _T_ECLIPSE(' ');
	}
	*ch = _T_ECLIPSE('\0');

	return message;
}

/*
 * Determine the default official application name
 *
 * This function provides the default application name that appears in a variety of
 * places such as: title of message dialog, title of splash screen window
 * that shows up in Windows task bar.
 * It is computed from the name of the launcher executable and
 * by capitalizing the first letter. e.g. "c:/ide/eclipse.exe" provides
 * a default name of "Eclipse".
 */
static _TCHAR* getDefaultOfficialName()
{
	_TCHAR *ch = NULL;
	
	/* Skip the directory part */
	ch = _tcsrchr( program, dirSeparator );
	if (ch == NULL) ch = program;
	else ch++;
	
	ch = _tcsdup( ch );
#ifdef _WIN32
	{
		/* Search for the extension .exe and cut it */
		_TCHAR *extension = _tcsrchr(ch, _T_ECLIPSE('.'));
		if (extension != NULL) 
		{
			*extension = _T_ECLIPSE('\0');
		}
	}
#endif
	/* Upper case the first character */
#ifndef LINUX
	{
		*ch = _totupper(*ch);
	}
#else
	{
		if (*ch >= 'a' && *ch <= 'z')
		{
			*ch -= 32;
		}
	}
#endif
	return ch;
}

/* Determine the Program Directory
 *
 * This function takes the directory where program executable resides and
 * determines the installation directory.
 */
_TCHAR* getProgramDir( )
{
	_TCHAR*  ch;
	_TCHAR*  programDir;

    programDir = malloc( (_tcslen( program ) + 1) * sizeof(_TCHAR) );
    _tcscpy( programDir, program );
    ch = _tcsrchr( programDir, dirSeparator );
	if (ch != NULL)
    {
    	*(ch+1) = _T_ECLIPSE('\0');
   		return programDir;
    }

    free( programDir );
    return NULL;
}
