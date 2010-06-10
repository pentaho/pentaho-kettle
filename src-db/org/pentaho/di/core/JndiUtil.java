/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core;

import java.io.File;

import org.pentaho.di.core.exception.KettleException;

public class JndiUtil {
	
	public static void initJNDI() throws KettleException {
		String path = Const.JNDI_DIRECTORY;
		
		if(path == null || path.equals("")) { //$NON-NLS-1$
  		try {
  			File file = new File("simple-jndi"); //$NON-NLS-1$
  			path = file.getCanonicalPath();
  		} catch (Exception e) {
  			throw new KettleException("Error initializing JNDI", e);
  		}
  		Const.JNDI_DIRECTORY = path;
		}

		System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
	  System.setProperty("org.osjava.sj.root", path); //$NON-NLS-1$
		System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
