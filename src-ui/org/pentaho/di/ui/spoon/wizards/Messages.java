package org.pentaho.di.ui.spoon.wizards;

import java.util.MissingResourceException;

public class Messages {
	public static final String packageName = Messages.class.getPackage().getName();
    
    
	public static String getString(String key) {
		try {
			return org.pentaho.di.i18n.BaseMessages.getString(packageName, key);
		} catch (MissingResourceException e) {
            return Messages.getString(key);
		}
	}
    
    public static String getString(String key, String param1) {
        try {
            return org.pentaho.di.i18n.BaseMessages.getString(packageName, key, param1);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1);
        }
    }

    public static String getString(String key, String param1, String param2) {
        try {
            return org.pentaho.di.i18n.BaseMessages.getString(packageName, key, param1, param2);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1, param2);
        }
    }

    public static String getString(String key, String param1, String param2, String param3) {
        try {
            return org.pentaho.di.i18n.BaseMessages.getString(packageName, key, param1, param2, param3);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1, param2);
        }
    }

}
