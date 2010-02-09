/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

public class GlobalMessages extends AbstractMessageHandler
{
	private static String packageNM = GlobalMessages.class.getPackage().getName();
	
	protected static final ThreadLocal<Locale> threadLocales         = new ThreadLocal<Locale>();

    protected static final LanguageChoice  langChoice        = LanguageChoice.getInstance();

    protected static final String      SYSTEM_BUNDLE_PACKAGE = GlobalMessages.class.getPackage().getName();

    protected static final String      BUNDLE_NAME           = "messages.messages";                                  //$NON-NLS-1$

    protected static final Map<String,ResourceBundle>         locales               = Collections.synchronizedMap(new HashMap<String,ResourceBundle>());

    protected static final LogChannelInterface log = new LogChannel("i18n");
    
    public static final String[] localeCodes = { 
    	"en_US", 
    	"nl_NL", 
    	"zh_CN", 
    	"es_ES", 
    	"fr_FR", 
    	"de_DE", 
    	"pt_BR", 
    	"pt_PT", 
    	"es_AR", 
    	"no_NO",
    	"it_IT",
    	"ja_JP",
    	"ko_KR"
    	};
    
    public static final String[] localeDescr = { 
    	"English (US)", 
    	"Nederlands", 
    	"Simplified Chinese", 
    	"Espa\u00F1ol (Spain)", 
    	"Fran\u00E7ais", 
    	"Deutsch", 
    	"Portuguese (Brazil)", 
    	"Portuguese (Portugal)", 
    	"Espa\u00F1ol (Argentina)", 
    	"Norwegian (Norway)", 
    	"Italian (Italy)",
    	"Japanese (Japan)",
    	"Korean (Korea)",
    	};
 
    protected static GlobalMessages GMinstance = null;
    
    /**
     * TODO: extend from abstract class to ensure singleton status and migrate
     * instantiation to class controlled private
     */
    public GlobalMessages() {
    }
    
    public synchronized static MessageHandler getInstance() {
    	if (GMinstance == null) {
    		GMinstance = new GlobalMessages();
    	}
    	return (MessageHandler)GMinstance;
    }
    
    protected static Map<String, ResourceBundle> getLocales()
    {
        return locales;
    }

    public synchronized static Locale getLocale()
    {
        Locale rtn = threadLocales.get();
        if (rtn != null) { return rtn; }

        setLocale(langChoice.getDefaultLocale());
        return langChoice.getDefaultLocale();
    }

    public synchronized static void setLocale(Locale newLocale)
    {
        threadLocales.set(newLocale);
    }
    
    protected static String getLocaleString(Locale locale)
    {
        String locString = locale.toString();
        if (locString.length()==5 && locString.charAt(2)=='_') // Force upper-lowercase format
        {
            locString=locString.substring(0,2).toLowerCase()+"_"+locString.substring(3).toUpperCase();
            // System.out.println("locString="+locString);
        }
        return locString;
    }

    protected static String buildHashKey(Locale locale, String packageName)
    {
        return packageName + "_" + getLocaleString(locale);
    }

    protected static String buildBundleName(String packageName)
    {
        return packageName + "." + BUNDLE_NAME;
    }
    
    /**
     * Retrieve a resource bundle of the default or fail-over locale.
     * @param packageName The package to search in
     * @return The resource bundle
     * @throws MissingResourceException in case both resource bundles couldn't be found.
     */
    public static ResourceBundle getBundle(String packageName) throws MissingResourceException
    {
    	ResourceBundle bundle ;
    	try {
    		// First try to load the bundle in the default locale
    		//
    		bundle = getBundle(LanguageChoice.getInstance().getDefaultLocale(), packageName);
    		return bundle;
    	} catch(MissingResourceException e) {
    		try {
    			// Now retry the fail-over locale (en_US etc)
    			//
    			bundle = getBundle(LanguageChoice.getInstance().getFailoverLocale(), packageName);
    			return bundle;
    		} catch(MissingResourceException e2) {
    			// If nothing usable could be found throw an exception...
    			//
    			throw new MissingResourceException(
    					"Unable to find properties file in the default '"+LanguageChoice.getInstance().getDefaultLocale()+
    					"' nor the failore locale '"+LanguageChoice.getInstance().getFailoverLocale()+"'", 
    					packageName, packageName);
    		}
    	}
    }

    public static ResourceBundle getBundle(Locale locale, String packageName) throws MissingResourceException
    {
    	String filename = buildHashKey(locale, packageName);
    	filename = "/"+filename.replace('.', '/') + ".properties";
    	
    	try
    	{
    	    ResourceBundle bundle = locales.get(filename);
            if (bundle == null)
            {
                InputStream inputStream = LanguageChoice.getInstance().getClass().getResourceAsStream(filename);
                //
                //  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //  !!!!!!!!! TODO: ENABLE THIS PART AGAIN AFTER REFACTORING !!!!!!!!
                //  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //
                // Try to ask the step loader instance to find the input stream for the specified filename
                // This works for messages files stored in step plugin jars
                //
                if (inputStream==null) // Try in the step plugin list: look in the jars over there
                {
                	try {
	                	Class<?> stepLoaderClass = Class.forName("org.pentaho.di.trans.StepLoader");
	                	Method getInstanceMethod = stepLoaderClass.getMethod("getInstance", new Class[0]);
	                	LoaderInputStreamProvider inputStreamProvider = (LoaderInputStreamProvider)getInstanceMethod.invoke(null, new Object[0]);
	                    inputStream = inputStreamProvider.getInputStreamForFile(filename);
                	}
                	catch(Exception e) {
                		// Eat exception, if we can grab a StepLoader instance, we're dealing with a core object instance not inside of Kettle.
                	}
                }
                // Try to ask the job entry loader instance to find the input stream for the specified filename
                // This works for messages files stored in job entry plugin jars
                //
                if (inputStream==null) // Try in the step plugin list: look in the jars over there
                {
                	try {
	                	Class<?> stepLoaderClass = Class.forName("org.pentaho.di.job.JobEntryLoader");
	                	Method getInstanceMethod = stepLoaderClass.getMethod("getInstance", new Class[0]);
	                	LoaderInputStreamProvider inputStreamProvider = (LoaderInputStreamProvider)getInstanceMethod.invoke(null, new Object[0]);
	                    inputStream = inputStreamProvider.getInputStreamForFile(filename);
                	}
                	catch(Exception e) {
                		// Eat exception, if we can grab a JobEntryLoader instance, we're dealing with a core object instance not inside of Kettle or in Pan.
                	}
                }

                // Now get the bundle from the messages files input stream
                //
            	if (inputStream!=null)
            	{
            		bundle = new PropertyResourceBundle(inputStream);
            		locales.put(filename, bundle);
            	}
            	else
            	{
            		throw new MissingResourceException("Unable to find properties file ["+filename+"]", locale.toString(), packageName);
            	}
            }
            return bundle;
    	}
    	catch(IOException e)
    	{
    		throw new MissingResourceException("Unable to find properties file ["+filename+"] : "+e.toString(), locale.toString(), packageName);
    	}
    }
    
    /**
     * @deprecated  As of build 4512, replaced by {@link #getInstance() and #getString(String)}
     */
    @Deprecated
    public static String getSystemString(String key)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getDefaultLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key);
        }
        catch (MissingResourceException e)
        {
            try
            {
                return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key);
            }
            catch (MissingResourceException fe)
            {
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }

        /*
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
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }
        */
    }
    
    /**
     * @deprecated  As of build 4512, replaced by {@link #getInstance() and #getString(String, String)}
     */
    @Deprecated
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
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }
    }

    /**
     * @deprecated  As of build 4512, replaced by {@link #getInstance() and #getString(String, String, String)}
     */
    @Deprecated
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
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }
    }
    
    /**
     * @deprecated  As of build 4512, replaced by {@link #getInstance() and #getString(String, String, String, String)}
     */
    @Deprecated
    public static String getSystemString(String key, String param1, String param2, String param3)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3);
        }
        catch (MissingResourceException e)
        {
            try
            {
                return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3);
            }
            catch (MissingResourceException fe)
            {
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }
    }

    /**
     * @deprecated  As of build 4512, replaced by {@link #getInstance() and #getString(String, String, String, String, String)}
     */
    @Deprecated
    public static String getSystemString(String key, String param1, String param2, String param3, String param4)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3, param4);
        }
        catch (MissingResourceException e)
        {
            try
            {
                return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3, param4);
            }
            catch (MissingResourceException fe)
            {
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }
    }

    /**
     * @deprecated  As of build 4512, replaced by {@link #getInstance() and #getString(String, String, String, String, String, String)}
     */
    @Deprecated
    public static String getSystemString(String key, String param1, String param2, String param3, String param4, String param5)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3, param4, param5);
        }
        catch (MissingResourceException e)
        {
            try
            {
                return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3, param4, param5);
            }
            catch (MissingResourceException fe)
            {
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }
    }

    /**
     * @deprecated  As of build 4512, replaced by {@link #getInstance() and #getString(String, String, String, String, String, String, String)}
     */
    @Deprecated
    public static String getSystemString(String key, String param1, String param2, String param3, String param4, String param5, String param6)
    {
        try
        {
            return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3, param4, param5, param6);
        }
        catch (MissingResourceException e)
        {
            try
            {
                return GlobalMessageUtil.getString(getBundle(langChoice.getFailoverLocale(), buildBundleName(SYSTEM_BUNDLE_PACKAGE)), key, param1, param2, param3, param4, param5, param6);
            }
            catch (MissingResourceException fe)
            {
                log.logError("Internationalisation/Translation error", Const.getStackTracker(e));
                return '!' + key + '!';
            }
        }
    }
    
    protected String findString(String packageName, Locale locale, String key, Object[] parameters) throws MissingResourceException
    {
        try
        {
            ResourceBundle bundle = getBundle(locale, packageName + "." + BUNDLE_NAME);
            String unformattedString = bundle.getString(key);
            String string = MessageFormat.format(unformattedString, parameters);
            return string;
        }
        catch(IllegalArgumentException e)
        {
            String message = "Format problem with key=["+key+"], locale=["+locale+"], package="+packageName+" : "+e.toString();
            log.logError("i18n", message);
            log.logError("i18n", Const.getStackTracker(e));
            throw new MissingResourceException(message, packageName, key);
        }
    }
    
    protected String calculateString(String packageName, String key, Object[] parameters)
    {
        String string=null;
        
        // First try the standard locale, in the local package
        try { string = findString(packageName, langChoice.getDefaultLocale(), key, parameters); } catch(MissingResourceException e) {};
        if (string!=null) return string;
        
        // Then try to find it in the i18n package, in the system messages of the preferred language.
        try { string = findString(SYSTEM_BUNDLE_PACKAGE, langChoice.getDefaultLocale(), key, parameters); } catch(MissingResourceException e) {};
        if (string!=null) return string;
        
        // Then try the failover locale, in the local package
        try { string = findString(packageName, langChoice.getFailoverLocale(), key, parameters); } catch(MissingResourceException e) {};
        if (string!=null) return string;
        
        // Then try to find it in the i18n package, in the system messages of the failover language.
        try { string = findString(SYSTEM_BUNDLE_PACKAGE, langChoice.getFailoverLocale(), key, parameters); } catch(MissingResourceException e) {};
        if (string!=null) return string;
        
        string = "!"+key+"!";
        String message = "Message not found in the preferred and failover locale: key=["+key+"], package="+packageName;
        log.logDetailed("i18n", Const.getStackTracker(new KettleException(message)));

        return string;
    }
    
    public String getString(String key) 
    {
        Object[] parameters = null;
        return calculateString(packageNM, key, parameters);
    }
    
    public String getString(String packageName, String key) 
    {
        Object[] parameters = new Object[] {};
        return calculateString(packageName, key, parameters);
    }
    
    public String getString(String packageName, String key, String...parameters)
    {
        return calculateString(packageName, key, parameters);
    }

    @Deprecated
    public String getString(String packageName, String key, String param1)
    {
        Object[] parameters = new Object[] { param1 };
        return calculateString(packageName, key, parameters);
    }

    @Deprecated
    public String getString(String packageName, String key, String param1, String param2)
    {
        Object[] parameters = new Object[] { param1, param2 };
        return calculateString(packageName, key, parameters);
    }

    @Deprecated
    public String getString(String packageName, String key, String param1, String param2, String param3)
    {
        Object[] parameters = new Object[] { param1, param2, param3 };
        return calculateString(packageName, key, parameters);
    }
    
    @Deprecated
    public String getString(String packageName, String key, String param1, String param2, String param3,String param4)
    {
        Object[] parameters = new Object[] { param1, param2, param3, param4 };
        return calculateString(packageName, key, parameters);
    }
    
    @Deprecated
    public String getString(String packageName, String key, String param1, String param2, String param3, String param4, String param5)
    {
        Object[] parameters = new Object[] { param1, param2, param3, param4, param5 };
        return calculateString(packageName, key, parameters);
    }
    
    @Deprecated
    public String getString(String packageName, String key, String param1, String param2, String param3,String param4,String param5,String param6)
    {
        Object[] parameters = new Object[] { param1, param2, param3, param4, param5, param6 };
        return calculateString(packageName, key, parameters);
    }
}
