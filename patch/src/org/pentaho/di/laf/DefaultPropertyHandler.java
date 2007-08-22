package org.pentaho.di.laf; 

import java.io.File;
import java.net.URL;
import java.util.Properties;
import org.pentaho.di.core.Const;

public class DefaultPropertyHandler implements PropertyHandler {
	protected static String propFile = null;
	protected static String altFile = null;
	
	static protected Properties properties;

	private static PropertyHandler instance = null;
	
	static {
		propFile = "ui/laf.properties"; // default location for kettle prop file
	}

	public DefaultPropertyHandler() {
		initProps();
	}

	public static PropertyHandler getInstance() {
		if (instance == null) {
			instance = new DefaultPropertyHandler();
		}
		return instance;
	}

	public static void setAltLAF(String pf) {
		altFile = pf;
	}

	public static String getAltLAF() {
		return altFile;
	}
	
	protected boolean loadAltProps() {
		// check the -D switch... something like -Dorg.pentaho.di.laf.alt="somefile.properties"
		altFile = Const.getEnvironmentVariable("org.pentaho.di.laf.alt",null);
		if (altFile!=null) {
			return loadProps(altFile);
		}
		return false;
	}

	private boolean initProps() { 
		boolean flag = true;
		flag = loadProps(propFile);
		return (loadAltProps()||flag);
	}

	public String getProperty(String key) {
		//System.out.println(properties.getProperty(key));
		return properties.getProperty(key);
	}

	public static String getLAFProp(String key) {
		return getInstance().getProperty(key);
	}

	public boolean loadProps(String filename) {
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			URL fileURL = classLoader.getResource(filename);
			if (fileURL==null) {
				fileURL = new File(filename).toURL();
			}
			if (properties == null) properties = new Properties();
			properties.load(fileURL.openStream());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		    return false;
		}
		return true;
	}

	public String getProperty(String key, String defValue) {
		String s = getProperty(key);
		if (s!=null) {
			return s;
		}
		return defValue;
	}

	public boolean exists(String filename) {
		ClassLoader classLoader = getClass().getClassLoader();
		URL fileURL = classLoader.getResource(filename);
		return (fileURL != null);
	}
}
