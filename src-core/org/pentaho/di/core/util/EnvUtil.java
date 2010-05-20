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
package org.pentaho.di.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.version.BuildVersion;

public class EnvUtil
{
	private static Properties env = null;

	/**
	 * Returns the properties from the users kettle home directory.
	 * 
	 * @param fileName
	 *            the relative name of the properties file in the users kettle
	 *            directory.
	 * @return the map of properties.
	 */
	public static Properties readProperties(String fileName) throws KettleException
	{
		Properties props = new Properties();
		String kettlePropsFilename = Const.getKettleDirectory() + Const.FILE_SEPARATOR + fileName;
		InputStream is = null;
		try
		{
			is = new FileInputStream(kettlePropsFilename);
			props.load(is);
		}
		catch (IOException ioe)
		{
			throw new KettleException("Unable to read file '"+kettlePropsFilename+"'", ioe);
		}
		finally
		{
			if (is != null)
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					// ignore
				}
		}
		return props;
	}

	/**
	 * Adds the kettle properties the the global system properties.
	 * @throws KettleException in case the properties file can't be read.
	 */
	@SuppressWarnings({"unchecked"})
    public static void environmentInit() throws KettleException
	{
        // Workaround for a Mac OS/X Leopard issue where getContextClassLoader() is returning 
        // null when run from the eclipse IDE
	    // http://lists.apple.com/archives/java-dev/2007/Nov/msg00385.html - DM
	    // Moving this hack to the first place where the NPE is triggered so all entrypoints can be debugged in Mac Eclipse
        if ( Thread.currentThread().getContextClassLoader() == null ) 
        {
          Thread.currentThread ().setContextClassLoader(ClassLoader.getSystemClassLoader());
        }

		Map kettleProperties = EnvUtil.readProperties(Const.KETTLE_PROPERTIES);
        System.getProperties().putAll(kettleProperties);
        
        // Also put some default values for obscure environment variables in there...
        // Place-holders if you will.
        // 
        System.getProperties().put(Const.INTERNAL_VARIABLE_CLUSTER_SIZE, "1");
        System.getProperties().put(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER, "0");
        System.getProperties().put(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NAME, "slave-trans-name");

        System.getProperties().put(Const.INTERNAL_VARIABLE_STEP_COPYNR, "0");
        System.getProperties().put(Const.INTERNAL_VARIABLE_STEP_NAME, "step-name");
        System.getProperties().put(Const.INTERNAL_VARIABLE_STEP_PARTITION_ID, "partition-id");
        System.getProperties().put(Const.INTERNAL_VARIABLE_STEP_PARTITION_NR, "0");
        System.getProperties().put(Const.INTERNAL_VARIABLE_STEP_UNIQUE_COUNT, "1");
        System.getProperties().put(Const.INTERNAL_VARIABLE_STEP_UNIQUE_NUMBER, "0");
	}

    /**
     * Add a number of internal variables to the Kettle Variables at the root.
     * @param variables
     */
    public static void addInternalVariables(Properties prop)
    {
        // Add a bunch of internal variables
        
        // The Kettle version
        prop.put(Const.INTERNAL_VARIABLE_KETTLE_VERSION, Const.VERSION);

        // The Kettle build version
        prop.put(Const.INTERNAL_VARIABLE_KETTLE_BUILD_VERSION, BuildVersion.getInstance().getVersion());

        // The Kettle build date
        prop.put(Const.INTERNAL_VARIABLE_KETTLE_BUILD_DATE, BuildVersion.getInstance().getBuildDate() );
    }

    /**
	 * Get System.getenv() in a reflection kind of way. The problem is
	 * that System.getenv() was deprecated in Java 1.4 while reinstated in 1.5
	 * This method will get at getenv() using reflection and will return
	 * empty properties when used in 1.4
	 * 
	 * @return Properties containing the environment. You're not meant
	 *         to change any value in the returned Properties!
	 * 
	 */
    @SuppressWarnings({"unchecked"})
    private static final Properties getEnv()
	{		
		 Class<?> system = System.class;
		 if ( env == null )
		 {
			 Map<String,String> returnMap = null;
			 try  {
			     Method method = system.getMethod("getenv");
			     
			     returnMap = (Map<String,String>) method.invoke(system);
			 }
   	         catch ( Exception ex )  {
   	        	 returnMap = null;
   	         }
   	         
   	         env = new Properties();
   	         if ( returnMap != null )
   	         {
   	             // We're on a VM with getenv() defined.
   	             ArrayList<String> list = new ArrayList<String>(returnMap.keySet());
   	             for (int i=0;i<list.size();i++)
   	             {
   	                 String var = list.get(i);
   	                 String val = returnMap.get(var);
   	        	 
   	        	     env.setProperty(var, val);   	          
   	             }
   	         }
		 }
		 return env;
	}
	
    /**
     * @return an array of strings, made up of all the environment variables available in the VM, format var=value.
     * To be used for Runtime.exec(cmd, envp)
     */
    @SuppressWarnings({"unchecked"})
    public static final String[] getEnvironmentVariablesForRuntimeExec()
    {
        Properties sysprops = new Properties();
        sysprops.putAll( getEnv() );
        sysprops.putAll( System.getProperties() );
        addInternalVariables(sysprops);
        
        String[] envp = new String[sysprops.size()];
        List<String> list = new ArrayList(sysprops.keySet());
        for (int i=0;i<list.size();i++)
        {
            String var = list.get(i);
            String val = sysprops.getProperty(var);
            
            envp[i] = var+"="+val;
        }

        return envp;
    }

    /**
     * This method is written especially for weird JVM's like IBM's on AIX and OS/400.
     * On these platforms, we notice that environment variables have an extra double quote around it...
     * This is messing up the ability to specify things.
     *  
     * @param key The key, the name of the environment variable to return
     * @param def The default value to return in case the key can't be found
     * @return The value of a System environment variable in the java virtual machine.  
     *         If the key is not present, the variable is not defined and the default value is returned.
     */
    public static final String getSystemPropertyStripQuotes(String key, String def)
    {
        String value = System.getProperty(key, def);
        if (value.startsWith("\"") && value.endsWith("\"") && value.length()>1)
        {
            return value.substring(1,value.length()-2);
        }
        return value;
    }

    /**
     * This method is written especially for weird JVM's like 
     * @param key The key, the name of the environment variable to return
     * @param def The default value to return in case the key can't be found
     * @return The value of a System environment variable in the java virtual machine.  
     *         If the key is not present, the variable is not defined and the default value is returned.
     */
    public static final String getSystemProperty(String key, String def)
    {
        String value = System.getProperty(key, def);
        return value;
    }
    
    /**
     * @param key The key, the name of the environment variable to return
     * @return The value of a System environment variable in the java virtual machine.  
     *         If the key is not present, the variable is not defined and null returned.
     */
    public static final String getSystemProperty(String key)
    {
        return getSystemProperty(key, null);
    }
    

    /**
     * Returns an available java.util.Locale object for the given localeCode.
     * 
     * The localeCode code can be case insensitive, if it is available
     * the method will find it and return it.
     * 
     * Returns null if an invalid or unavailable localeCode is provided.
     * 
     * @param localeCode
     * @return java.util.Locale.
     */
    public static Locale createLocale(String localeCode) {

      Locale resultLocale = null;
      if (localeCode != null) {
        // Creates a new java.util.Locale regardless of the given code.
        Locale validatingLocale = null;
        StringTokenizer parser = new StringTokenizer(localeCode, "_"); //$NON-NLS-1$
        if (parser.countTokens() == 2) {
          validatingLocale = new Locale(parser.nextToken(), parser.nextToken());
        } else {
          validatingLocale = new Locale(localeCode);
        }

        //Validates that the new java.util.Locale is available.
        for (Locale currentLocale : Locale.getAvailableLocales()) {
          if (validatingLocale.equals(currentLocale)) {
            resultLocale = currentLocale;
            break;
          }
        }
      }
      return resultLocale;
    }
}
