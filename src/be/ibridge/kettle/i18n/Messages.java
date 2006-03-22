
package be.ibridge.kettle.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final ThreadLocal threadLocales = new ThreadLocal();
	private static Locale defaultLocale;	
	private static final String SYSTEM_BUNDLE_NAME = "be.ibridge.kettle.i18n.messages";//$NON-NLS-1$
	private static final String BUNDLE_NAME = "messages";//$NON-NLS-1$
	
	private static final Map locales = Collections.synchronizedMap( new HashMap() );
	
	  protected static Map getLocales() {
	  	return locales;
	  }
	  
	public static Locale getLocale() {
		Locale rtn = (Locale) threadLocales.get();
    if (rtn != null) {
      return rtn;
    }
    defaultLocale = Locale.US;
    setLocale( defaultLocale );
    return defaultLocale;
	}
	public static void setLocale( Locale newLocale ) {
		threadLocales.set( newLocale );
	}	
		  
	  private static ResourceBundle getBundle(String packageName) {
	  	//Locale locale = PentahoSystem.getLocale();
	  	Locale locale = getLocale();
	  	ResourceBundle bundle = (ResourceBundle) locales.get( locale );
	  	if( bundle == null ) {
	  	    try
            {
	  	        bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale );
            }
            catch(MissingResourceException e)
            {
                // Sorry, no translation found for you, let's default to US for now...
                // Backward compatibility!
                // 
                bundle = ResourceBundle.getBundle( BUNDLE_NAME, defaultLocale);
            }
	  		locales.put( locale, bundle );
	  	}
	  	return bundle;
	  }
      
      public static String getString(String key) {
        try {
          return getBundle(SYSTEM_BUNDLE_NAME).getString(key);
        } catch (MissingResourceException e) {
          System.out.println(e.getMessage());   
          return '!' + key + '!';
        }
      }

	  
	  public static String getString(String packageName, String key) {
	    try {
	      return getBundle(packageName).getString(key);
	    } catch (MissingResourceException e) {
	      System.out.println(e.getMessage());	
	      return '!' + key + '!';
	    }
	  }

	  public static String getString(String packageName, String key, String param1) {
	    return MessageUtil.getString(getBundle(packageName), key, param1);
	  }

	  public static String getString(String packageName, String key, String param1, String param2) {
	    return MessageUtil.getString(getBundle(packageName), key, param1, param2);
	  }

	  public static String getString(String packageName, String key, String param1, String param2, String param3) {
	    return MessageUtil.getString(getBundle(packageName), key, param1, param2, param3);
	  }

	  public static String getErrorString(String packageName, String key) {
	    return MessageUtil.formatErrorMessage(key, getString(packageName, key));
	  }
	  
	  public static String getErrorString(String packageName, String key, String param1) {
	    return MessageUtil.getErrorString(getBundle(packageName), key, param1);
	  }

	  public static String getErrorString(String packageName, String key, String param1, String param2) {
	    return MessageUtil.getErrorString(getBundle(packageName), key, param1, param2);
	  }

	  public static String getErrorString(String packageName, String key, String param1, String param2, String param3) {
	    return MessageUtil.getErrorString(getBundle(packageName), key, param1, param2, param3);
	  }

}