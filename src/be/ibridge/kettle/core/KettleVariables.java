package be.ibridge.kettle.core;

import java.util.Map;
import java.util.Properties;

import be.ibridge.kettle.core.util.EnvUtil;

/**
 * This class is a container for "Local" enrvironment variables.
 * This is a singleton.  We are going to launch jobs using a customer classloader.
 * This will make the variables inside it local.
 * 
 * @author Matt
 */
public class KettleVariables
{
    private Properties properties;
    
    private String localThread;
    private String parentThread;
    
    public KettleVariables(String localThread, String parentThread)
    {
        properties = new Properties();
        this.localThread = localThread;
        this.parentThread = parentThread;
    }
    
    /**
     * @return the Kettle Variables for the current thread
     */
    public static final KettleVariables getInstance()
    {
        Thread thread = Thread.currentThread();
        KettleVariables kettleVariables = LocalVariables.getKettleVariables(thread.getName());
        if (kettleVariables==null)
        {
            /*
            System.out.println("What's in LOCAL variables?");
            System.out.println("-----------------------------");
            
            Map map = LocalVariables.getInstance().getMap();
            ArrayList keys = new ArrayList(map.keySet());
            for (int i=0;i<keys.size();i++)
            {
                String key = (String)keys.get(i);
                KettleVariables v = (KettleVariables) map.get(key);
                System.out.println("Kettle variables #"+i+", key ["+key+"] --+> local thread ["+v.getLocalThread()+"], parent ["+v.getParentThread()+"]");
            }
            */
            
            // New functionality: if we arrive here, it means that somehow we are disconnected from the root 
            // Probably this is because of some threading issue beyond the control of Kettle.
            // The safe bet here would be to return the root if that one is created and otherwise throw an exception.
            //
            kettleVariables = LocalVariables.getRoot();
            if (kettleVariables==null)
            {
                throw new RuntimeException("Unable to find Kettle Variables for thread ["+thread.getName()+"]");
            }
        }
        // Add the internal variables, just to make sure that they are always present 
        // if people skip EnvUtil.environmentInit(), like the pentaho framework...
        //
        EnvUtil.addInternalVariables(kettleVariables);
        return kettleVariables;
    }
    
    /**
     * Create the KettleVariables object and uses the argument as starting point.
     * @param map The values to add to an empty set of Kettle Variables.
     * If properties is null, nothing gets added.
     */
    public void putAll(Map map)
    {
        if (properties!=null && map!=null) 
        {
            properties.putAll(map);
        }
    }
    
    /**
     * Sets a variable in the Kettle Variables list.
     * 
     * @param variableName The name of the variable to set
     * @param variableValue The value of the variable to set.  If the variableValue is null, the variable is cleared from the list. 
     */
    public void setVariable(String variableName, String variableValue)
    {
        if (variableValue!=null)
        {
            properties.setProperty(variableName, variableValue);
        }
        else
        {
            properties.remove(variableName);
        }
    }
    
    /**
     * Get the value of a variable with a default in case the variable is not found.
     * @param variableName The name of the variable
     * @param defaultValue The default value in case the variable could not be found
     * @return the String value of a variable
     */
    public String getVariable(String variableName, String defaultValue)
    {
        String var = properties.getProperty(variableName, defaultValue);
        return var;
    }
    
    /**
     * Get the value of a variable
     * @param variableName The name of the variable
     * @return the String value of a variable or null in case the variable could not be found.
     */
    public String getVariable(String variableName)
    {
        return properties.getProperty(variableName);
    }

    /**
     * @return Returns the properties.
     */
    public Properties getProperties()
    {
        return properties;
    }
    
    /**
     * Set the properties of this KettleVariables object.
     * 
     * @param properties
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * @return Returns the localThread.
     */
    public String getLocalThread()
    {
        return localThread;
    }

    /**
     * @param localThread The localThread to set.
     */
    public void setLocalThread(String localThread)
    {
        this.localThread = localThread;
    }

    /**
     * @return Returns the parentThread.
     */
    public String getParentThread()
    {
        return parentThread;
    }

    /**
     * @param parentThread The parentThread to set.
     */
    public void setParentThread(String parentThread)
    {
        this.parentThread = parentThread;
    }
}
