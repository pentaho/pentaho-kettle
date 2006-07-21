package be.ibridge.kettle.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;

public class EnvUtil
{
	private static LogWriter log = LogWriter.getInstance();
	private static Properties env = null;

	/**
	 * Returns the properties from the users ketle home directory.
	 * 
	 * @param fileName
	 *            the relative name of the properties file in the users kettle
	 *            directory.
	 * @return the map of properties.
	 */
	public static Map readProperties(String fileName)
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
			log.logDetailed("Kettle Environment", "Unable to read \"<home>/.kettle/kettle.properties\" file: " + ioe.getMessage());
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
	 */
	public static void environmentInit()
	{
		Map kettleProperties = EnvUtil.readProperties(Const.KETTLE_PROPERTIES);
        System.getProperties().putAll(kettleProperties);
        
        // OK, initialize the KettleVariables as well...
        LocalVariables local = LocalVariables.getInstance();
        local.createKettleVariables(Thread.currentThread().getName(), null, false);
	}
    
	/**
	 * Get System.getenv() in a reflection kind of way. The problem is
	 * that System.getenv() was deprecated in Java 1.4 while reinstated in 1.5
	 * This method will get at getenv() using reflection and will return
	 * empty properties when used in 1.4
	 * 
	 * @return Properties containing the environment. You're not meant
	 *         to change any value in the returned Properties!
	 */
	private static final Properties getEnv()
	{		
		 Class system = System.class;
		 if ( env == null )
		 {
			 Map returnMap = null;
			 try  {
			     Method method = system.getMethod("getenv", (Class[])null);
			     returnMap = (Map)method.invoke(system, (Object[])null);
			 }
   	         catch ( Exception ex )  {
   	        	 returnMap = null;
   	         }
   	         
   	         env = new Properties();
   	         if ( returnMap != null )
   	         {
   	             // We're on a VM with getenv() defined.
   	             ArrayList list = new ArrayList(returnMap.keySet());
   	             for (int i=0;i<list.size();i++)
   	             {
   	                 String var = (String)list.get(i);
   	                 String val = (String)returnMap.get(var);
   	        	 
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
    public static final String[] getEnvironmentVariablesForRuntimeExec()
    {
        KettleVariables vars = KettleVariables.getInstance();
        
        Properties sysprops = new Properties();
        sysprops.putAll( getEnv() );
        sysprops.putAll( System.getProperties() );
        sysprops.putAll( vars.getProperties() );
        
        String[] envp = new String[sysprops.size()];
        ArrayList list = new ArrayList(sysprops.keySet());
        for (int i=0;i<list.size();i++)
        {
            String var = (String)list.get(i);
            String val = sysprops.getProperty(var);
            
            envp[i] = var+"="+val;
        }

        return envp;

    }
}
