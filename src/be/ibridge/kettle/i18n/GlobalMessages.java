package be.ibridge.kettle.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class GlobalMessages
{
    private static final ThreadLocal threadLocales         = new ThreadLocal();

    private static final LanguageChoice  langChoice        = LanguageChoice.getInstance();

    private static final String      SYSTEM_BUNDLE_PACKAGE = GlobalMessages.class.getPackage().getName();

    private static final String      BUNDLE_NAME           = "messages.messages";                                  //$NON-NLS-1$

    private static final Map         locales               = Collections.synchronizedMap(new HashMap());

    public static final String[] localeCodes = { "en_US", "nl_NL", "zh_CN", "es_ES", "fr_FR", "de_DE" };
    
    public static final String[] localeDescr = { "English (US)", "Nederlands", "Simplified Chinese", "Español", "Français", "Deutch" };
    
    protected static Map getLocales()
    {
        return locales;
    }

    public static Locale getLocale()
    {
        Locale rtn = (Locale) threadLocales.get();
        if (rtn != null) { return rtn; }

        setLocale(langChoice.getDefaultLocale());
        return langChoice.getDefaultLocale();
    }

    public static void setLocale(Locale newLocale)
    {
        threadLocales.set(newLocale);
    }

    private static String buildHashKey(Locale locale, String packageName)
    {
        String locString = locale.toString();
        if (locString.length()==5 && locString.charAt(2)=='_') // Force upper-lowercase format
        {
            locString=locString.substring(0,2).toLowerCase()+"_"+locString.substring(3).toUpperCase();
            // System.out.println("locString="+locString);
        }
        return packageName + "_" + locString;
    }

    private static String buildBundleName(String packageName)
    {
        return packageName + "." + BUNDLE_NAME;
    }

    private static ResourceBundle getBundle(Locale locale, String packageName) throws MissingResourceException
    {
        ResourceBundle bundle = (ResourceBundle) locales.get(buildHashKey(locale, packageName));
        if (bundle == null)
        {
            bundle = ResourceBundle.getBundle(buildHashKey(locale, packageName), locale);
            locales.put(buildHashKey(locale, packageName), bundle);
        }
        return bundle;
    }

    public static String getSystemString(String key)
    {
        try
        {
            ResourceBundle bundle = getBundle(langChoice.getDefaultLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE));
            return bundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            // OK, try to find the key in the alternate failover locale
            try
            {
                ResourceBundle bundle = getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE));
                return bundle.getString(key);
            }
            catch (MissingResourceException fe)
            {
                fe.printStackTrace();
                return '!' + key + '!';
            }
        }
    }

    public static String getSystemString(String key, String param1)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getDefaultLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1);
        }
        catch (MissingResourceException e)
        {
            try
            {
                return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1);
            }
            catch (MissingResourceException fe)
            {
                fe.printStackTrace();
                return '!' + key + '!';
            }
        }
    }

    public static String getSystemString(String key, String param1, String param2)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2);
        }
        catch (MissingResourceException e)
        {
            try
            {
                return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2);
            }
            catch (MissingResourceException fe)
            {
                fe.printStackTrace();
                return '!' + key + '!';
            }
        }
    }

    public static String getString(String packageName, String key)
    {
        try
        {
            return getBundle(langChoice.getDefaultLocale(), packageName + "." + BUNDLE_NAME).getString(key);
        }
        catch(MissingResourceException e)
        {
            return getBundle(langChoice.getFailoverLocale(), packageName + "." + BUNDLE_NAME).getString(key);
        }
    }

    public static String getString(String packageName, String key, String param1)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getDefaultLocale(), packageName + "." + BUNDLE_NAME), key, param1);
        }
        catch(MissingResourceException e)
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), packageName + "." + BUNDLE_NAME), key, param1);
        }
    }

    public static String getString(String packageName, String key, String param1, String param2)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getDefaultLocale(), packageName + "." + BUNDLE_NAME), key, param1, param2);
        }
        catch(MissingResourceException e)
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), packageName + "." + BUNDLE_NAME), key, param1, param2);
        }
    }

    public static String getString(String packageName, String key, String param1, String param2, String param3)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getDefaultLocale(), packageName + "." + BUNDLE_NAME), key, param1, param2, param3);
        }
        catch(MissingResourceException e)
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), packageName + "." + BUNDLE_NAME), key, param1, param2, param3);
        }
    }
    

/*
    public static String getErrorString(String packageName, String key)
    {
        return GlobalMessageUtil.formatErrorMessage(key, getString(packageName + "." + BUNDLE_NAME, key));
    }

    public static String getErrorString(String packageName, String key, String param1)
    {
        try
        {
            return GlobalMessageUtil.getErrorString(getBundle(langChoice.getDefaultLocale(), packageName + "." + BUNDLE_NAME), key, param1);
        }
        catch(MissingResourceException e)
        {
            return GlobalMessageUtil.getErrorString(getBundle(langChoice.getFailoverLocale(), packageName + "." + BUNDLE_NAME), key, param1);
        }
    }

    public static String getErrorString(String packageName, String key, String param1, String param2)
    {
        try
        {
            return GlobalMessageUtil.getErrorString(getBundle(langChoice.getDefaultLocale(), packageName + "." + BUNDLE_NAME), key, param1, param2);
        }
        catch(MissingResourceException e)
        {
            return GlobalMessageUtil.getErrorString(getBundle(langChoice.getFailoverLocale(), packageName), key, param1, param2);
        }
    }

    public static String getErrorString(String packageName, String key, String param1, String param2, String param3)
    {
        try
        {
            return GlobalMessageUtil.getErrorString(getBundle(langChoice.getDefaultLocale(), packageName), key, param1, param2, param3);
        }
        catch(MissingResourceException e)
        {
            return GlobalMessageUtil.getErrorString(getBundle(langChoice.getFailoverLocale(), packageName), key, param1, param2, param3);
        }
    }
*/
}
