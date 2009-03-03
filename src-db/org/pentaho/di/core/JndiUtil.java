package org.pentaho.di.core;

import java.io.File;

import org.pentaho.di.core.logging.LogWriter;

public class JndiUtil {
	
	public static void initJNDI() {
		String path = ""; //$NON-NLS-1$
		try {
			File file = new File("simple-jndi"); //$NON-NLS-1$
			path = file.getCanonicalPath();
		} catch (Exception e) {
			LogWriter.getInstance().logError("JNDI", "Error initializing JNDI", e);
		}

		System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.osjava.sj.root", path); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
