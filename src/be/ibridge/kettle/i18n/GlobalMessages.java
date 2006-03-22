
package be.ibridge.kettle.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class GlobalMessages {
	private static final ThreadLocal threadLocales = new ThreadLocal();
	private static Locale defaultLocale = Locale.getDefault();	
    private static Locale failoverLocale = Locale.US;
	private static final String SYSTEM_BUNDLE_PACKAGE = GlobalMessages.class.getPackage().getName();
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
    
    setLocale( defaultLocale );
    return defaultLocale;
	}
	public static void setLocale( Locale newLocale ) {
		threadLocales.set( newLocale );
	}	
		  
    private static String buildHashKey(Locale locale, String packageName)
    {
        return packageName+"_"+locale.toString();
    }
    
    private static String buildBundleName(String packageName)
    {
        return packageName+"."+BUNDLE_NAME;
    }
    
	  private static ResourceBundle getBundle(String packageName) {
	  	//Locale locale = PentahoSystem.getLocale();
	  	Locale locale = getLocale();
	  	ResourceBundle bundle = (ResourceBundle) locales.get( buildHashKey(locale, packageName) );
	  	if( bundle == null ) {
	  	    try
            {
	  	        bundle = ResourceBundle.getBundle( buildBundleName(packageName), locale );
            }
            catch(MissingResourceException e)
            {
                // Sorry, no translation found for you, let's try the failover locale
                // Backward compatibility!
                // 
                bundle = ResourceBundle.getBundle( buildBundleName(packageName), failoverLocale);
            }
	  		locales.put( buildHashKey(locale, packageName), bundle );
	  	}
	  	return bundle;
	  }
      
      public static String getSystemString(String key) {
        try {
          ResourceBundle bundle = getBundle(SYSTEM_BUNDLE_PACKAGE);
          return bundle.getString(key);
        } catch (MissingResourceException e) {
          // System.out.println(e.getMessage()); 
          e.printStackTrace();
          return '!' + key + '!';
        }
      }

      public static String getSystemString(String key, String param1) {
          try {
            return GlobalMessageUtil.getString(getBundle(SYSTEM_BUNDLE_PACKAGE), key, param1);
          } catch (MissingResourceException e) {
            // System.out.println(e.getMessage());   
            e.printStackTrace();
            return '!' + key + '!';
          }
        }

      public static String getSystemString(String key, String param1, String param2) {
          try {
            return GlobalMessageUtil.getString(getBundle(SYSTEM_BUNDLE_PACKAGE), key, param1, param2);
          } catch (MissingResourceException e) {
            // System.out.println(e.getMessage());   
            e.printStackTrace();
            return '!' + key + '!';
          }
        }
	  
	  public static String getString(String packageName, String key) {
            ResourceBundle bundle = getBundle(packageName+"."+BUNDLE_NAME);
            return bundle.getString(key);
	  }

	  public static String getString(String packageName, String key, String param1) {
	    return GlobalMessageUtil.getString(getBundle(packageName), key, param1);
	  }

	  public static String getString(String packageName, String key, String param1, String param2) {
	    return GlobalMessageUtil.getString(getBundle(packageName), key, param1, param2);
	  }

	  public static String getString(String packageName, String key, String param1, String param2, String param3) {
	    return GlobalMessageUtil.getString(getBundle(packageName), key, param1, param2, param3);
	  }

	  public static String getErrorString(String packageName, String key) {
	    return GlobalMessageUtil.formatErrorMessage(key, getString(packageName, key));
	  }
	  
	  public static String getErrorString(String packageName, String key, String param1) {
	    return GlobalMessageUtil.getErrorString(getBundle(packageName), key, param1);
	  }

	  public static String getErrorString(String packageName, String key, String param1, String param2) {
	    return GlobalMessageUtil.getErrorString(getBundle(packageName), key, param1, param2);
	  }

	  public static String getErrorString(String packageName, String key, String param1, String param2, String param3) {
	    return GlobalMessageUtil.getErrorString(getBundle(packageName), key, param1, param2, param3);
	  }

}