package org.pentaho.di.laf; 

import java.io.File;
import java.net.URL;
import java.util.Properties;

public class LAFManager {
	private static String propFile = null;

	static protected Properties properties;

	private static LAFManager instance = null;
	static {
		propFile = "ui/laf.properties"; // default location for
		// kettle prop file    
	}

	private LAFManager() {
		initProps();
	}

	public static LAFManager getInstance() {
		if (instance == null) {
			instance = new LAFManager();
		}

		return instance;
	}

	public static void setLAF(String pf) {
		propFile = pf;
	}

	public static String getLAF() {
		return propFile;
	}

	private boolean initProps() { 
		
		// check the -D switch... something like -Dorg.pentaho.di.laf.alt="somefile.properties"
		// 
		String altprop = System.getProperty("org.pentaho.di.laf.alt");
		if (altprop!=null) {
			return loadProps(altprop);
		}
		else {
			// verify that the property file exists        
			//
			if (!(exists(propFile))) {
				return false; 
			}
			// load the values from the property file
			//
			return loadProps(propFile);
		}
	}

	protected String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static String getLAFProp(String key) {
		return getInstance().getProperty(key);
	}

	public boolean exists(String filename) {
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			URL fileURL = classLoader.getResource(filename);
			if (fileURL==null) {
				return new File(filename).exists();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean loadProps(String filename) {
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			URL fileURL = classLoader.getResource(filename);
			if (fileURL==null) {
				fileURL = new File(filename).toURL();
			}
			properties = new Properties();
			properties.load(fileURL.openStream());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return false;
		}

		return true;
	}
}
