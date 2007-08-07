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

	private boolean initProps() { // verify that the property
		// file exists        

		if (!(exists(propFile)))
			return false; // load the values from the property file 
		return loadProps(propFile);
	}

	protected String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public static String getLAFProp(String key, String defaultValue) {
		return getInstance().getProperty(key, defaultValue);
	}

	public boolean exists(String filename) {
		try {
			boolean flag = new File(filename).exists();
			System.out.println("testing" + flag);
			return (new File(filename)).exists();
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
