package be.ibridge.kettle.spoon;

import java.util.MissingResourceException;

public class Messages {
	public static final String packageName = Spoon.class.getPackage().toString();
    
    
	public static String getString(String key) {
		try {
			return be.ibridge.kettle.i18n.Messages.getString(packageName, key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
    
    public static String getString(String key, String param1) {
        try {
            return be.ibridge.kettle.i18n.Messages.getString(packageName, key, param1);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getString(String key, String param1, String param2) {
        try {
            return be.ibridge.kettle.i18n.Messages.getString(packageName, key, param1, param2);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

}
