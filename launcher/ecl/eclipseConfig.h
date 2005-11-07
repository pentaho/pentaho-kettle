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

#ifndef ECLIPSE_CONFIG_H
#define ECLIPSE_CONFIG_H

#ifdef UNICODE
#define readConfigFile readConfigFileW
#define freeConfig freeConfigW
#endif

/* Configuration file reading utilities */

/**
 * Reads a configuration file for the corresponding
 * program argument.
 * e.g if the program argument contains "c:/folder/eclipse.exe"
 * then the config file "c:/folder/eclipse.ini" will be parsed.
 * On a Unix like platform, for a program argument "/usr/eclipse/eclipse"
 * should correspond a configuration file "/usr/eclipse/eclipse.ini"
 *
 * The argument arg0 corresponds to the first argument received by
 * the main function. It will be duplicated and set in the first
 * position of the resulting list of arguments.
 *
 * The argument args refers to a newly allocated array of strings.
 * The first entry is the program name to mimic the expectations
 * from a typical argv list.
 * The last entry of that array is NULL. 
 * Each non NULL entry in that array must be freed by the caller 
 * as well as the array itself, using freeConfig().
 * The argument nArgs contains the number of string allocated.
 *
 * Returns 0 if success.
 */
extern int readConfigFile(_TCHAR* program, _TCHAR* arg0, int *nArgs, _TCHAR ***args);

/**
 * Free the memory allocated by readConfigFile().
 */
extern void freeConfig(_TCHAR **args);

#endif /* ECLIPSE_CONFIG_H */