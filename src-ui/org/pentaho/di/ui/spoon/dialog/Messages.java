package org.pentaho.di.ui.spoon.dialog;

import java.util.MissingResourceException;

public class Messages {
	public static final String packageName = Messages.class.getPackage().getName();
    
    
	public static String getString(String key) {
		try {
			return org.pentaho.di.i18n.GlobalMessages.getString(packageName, key);
		} catch (MissingResourceException e) {
            return Messages.getString(key);
		}
	}
    
    public static String getString(String key, String param1) {
        try {
            return org.pentaho.di.i18n.GlobalMessages.getString(packageName, key, param1);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1);
        }
    }

    public static String getString(String key, String param1, String param2) {
        try {
            return org.pentaho.di.i18n.GlobalMessages.getString(packageName, key, param1, param2);
        } catch (MissingResourceException e) {
            return Messages.getString(key, param1, param2);
        }
    }

}
