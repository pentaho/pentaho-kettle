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

/* Eclipse Launcher Utility Methods */

#include "eclipseOS.h"
#include "eclipseUtil.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#ifndef _WIN32
#include <strings.h>
#endif

#define MAX_LINE_LENGTH 256

/* Is the given VM J9 */
int isJ9VM( _TCHAR* vm )
{
	_TCHAR *ch = _tcsrchr( vm, dirSeparator );
	if (ch == NULL)
	    ch = vm;
	else
	    ch++;
	return (_tcsicmp( ch, _T_ECLIPSE("j9") ) == 0);
}


#ifdef AIX

#include <sys/types.h>
#include <time.h>

/* Return the JVM version in the format x.x.x 
 */
char* getVMVersion( char *vmPath )
{
    char   cmd[MAX_LINE_LENGTH];
    char   lineString[MAX_LINE_LENGTH];
    char*  firstChar;
    char   fileName[MAX_LINE_LENGTH];
    time_t curTime;
    FILE*  fp;
    int    numChars = 0;
    char*  version  = NULL;

	/* Define a unique filename for the java output. */
    (void) time(&curTime);
    (void) sprintf(fileName, "/tmp/tmp%ld.txt", curTime);

    /* Write java -version output to a temp file */
    (void) sprintf(cmd,"%s -version 2> %s", vmPath, fileName);
    (void) system(cmd); 

    fp = fopen(fileName, "r");
    if (fp != NULL)
    {
    	/* Read java -version output from a temp file */
    	if (fgets(lineString, MAX_LINE_LENGTH, fp) == NULL)
    		lineString[0] = '\0';
    	fclose(fp);
    	unlink(fileName);

    	/* Extract version number */
    	firstChar = (char *) (strchr(lineString, '"') + 1);
    	if (firstChar != NULL)
    		numChars = (int)  (strrchr(lineString, '"') - firstChar);
    	
    	/* Allocate a buffer and copy the version string into it. */
    	if (numChars > 0)
    	{
    		version = malloc( numChars + 1 );
    		strncpy(version, firstChar, numChars);
			version[numChars] = '\0';
		}
	}  

    return version;
}

/* Compare JVM Versions of the form "x.x.x..."
 *     
 *    Returns -1 if ver1 < ver2
 *    Returns  0 if ver1 = ver2 
 *    Returns  1 if ver1 > ver2
 */     
int versionCmp(char *ver1, char *ver2)
{
    char*  dot1;
    char*  dot2;
    int    num1;
    int    num2;

    dot1 = strchr(ver1, '.');
    dot2 = strchr(ver2, '.');

    num1 = atoi(ver1);
    num2 = atoi(ver2);

    if (num1 > num2)
    	return 1;
    	
	if (num1 < num2)
		return -1;
	
	if (dot1 && !dot2)   /* x.y > x */
        return 1;

    if (!dot1 && dot2)   /* x < x.y */
        return -1;
    
    if (!dot1 && !dot2)  /* x == x */
        return 0;

    return versionCmp((char*)(dot1 + 1), (char*)(dot2 + 1) );
}
#endif /* AIX */
