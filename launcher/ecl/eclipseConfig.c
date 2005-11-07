/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

#include "eclipseOS.h"
#include "eclipseConfig.h"

#ifdef _WIN32

#include <stdio.h>

#ifdef __MINGW32__
#include <stdlib.h>
#endif

#else /* Unix like platforms */

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

#endif

int readConfigFile(_TCHAR* program, _TCHAR* arg0, int *argc, _TCHAR ***argv) 
{
	_TCHAR* config_file = NULL;
	_TCHAR buffer[1024];
	_TCHAR argument[1024];
	FILE *file = NULL;
	int maxArgs = 128;
	int index;
	
	if (program == NULL || argc == NULL || argv == NULL) return -1;

	/* Get a copy */
	config_file = _tcsdup(program);
	
#ifdef _WIN32
	{
		/* Search for the extension .exe and replace it with .ini */
		_TCHAR *extension = _tcsrchr(config_file, _T_ECLIPSE('.'));
		if (extension == NULL || _tcslen(extension) < 4)
		{
			free(config_file);
			return -2;
		}
		_tcscpy(extension, _T_ECLIPSE(".ini"));
	}
#else
	/* Append the extension */
	config_file = (char*)realloc(config_file, strlen(config_file) + 5);
	strcat(config_file, ".ini");
#endif
	
	/* Open the config file as a text file 
	 * Note that carriage return-linefeed combination \r\n are automatically
	 * translated into single linefeeds on input in the t (translated) mode.
	 */	
	file = _tfopen(config_file, _T_ECLIPSE("rt"));	
	if (file == NULL) return -3;

	*argv = (_TCHAR **)malloc(1 + maxArgs * sizeof(_TCHAR*));
	
	/* Make it look like a regular list of arguments that starts with the executable location and name */
	(*argv)[0] = _tcsdup(arg0);
	index = 1;
	
	/* Parse every line */	
	while (_fgetts(buffer, 1024, file) != NULL)
	{
		/* Extract the string prior to the first newline character.
		 * We don't have to worry about \r\n combinations since the file
		 * is opened in translated mode.
		 */
		if (_stscanf(buffer, _T_ECLIPSE("%[^\n]"), argument) == 1)
		{
			(*argv)[index] = _tcsdup(argument);
			index++;
			
			/* Grow the array of TCHAR*. Ensure one more entry is
			 * available for the final NULL entry
			 */
			if (index == maxArgs - 1)
			{
				maxArgs += 128;
				*argv = (_TCHAR **)realloc(*argv, maxArgs * sizeof(_TCHAR*));
			}
		}
	}
	(*argv)[index] = NULL;
	*argc = index;
	
	fclose(file);
	free(config_file);
	return 0;
}

void freeConfig(_TCHAR **argv) 
{
	int index = 0;
	if (argv == NULL) return;
	while (argv[index] != NULL)
	{
		free(argv[index]);
		index++;
	}
	free(argv);
}