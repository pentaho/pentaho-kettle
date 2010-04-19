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
