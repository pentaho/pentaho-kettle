package org.pentaho.di.core;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.repository.RepositoryLoader;
import org.pentaho.di.trans.StepLoader;

public class KettleEnvironment {
	
	private static Boolean initialized;
	
	public static void init() throws KettleException {
		if (initialized==null) {
			
			EnvUtil.environmentInit();
			
			JndiUtil.initJNDI();
			
			StepLoader.init();
			
			JobEntryLoader.init();
			
			RepositoryLoader.init();
			
			initialized = true;
		}
	}
	
	public static boolean isInitialized() {
		if (initialized==null) return false; else return true;
	}
}
