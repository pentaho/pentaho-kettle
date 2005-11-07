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

#ifndef ECLIPSE_UTIL_H
#define ECLIPSE_UTIL_H

#ifdef UNICODE
#define isJ9VM isJ9VMW
#endif

/* Eclipse Launcher Utility Methods */

/* Is the given Java VM J9 */
extern int isJ9VM( _TCHAR* vm );


#ifdef AIX 
/* Get the version of the VM */
extern char* getVMVersion( char* vm );

/* Compare JVM Versions */
extern int versionCmp( char* ver1, char* ver2 );
#endif

#endif /* ECLIPSE_UTIL_H */
