package be.ibridge.kettle.core;

import java.util.Map;
import java.util.Properties;

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
    
    private Thread localThread;
    private Thread parentThread;
    
    public KettleVariables(Thread localThread, Thread parentThread)
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
        KettleVariables kettleVariables = LocalVariables.getKettleVariables(Thread.currentThread());
        if (kettleVariables==null)
        {
            System.out.println("---> Unable to find Kettle Variables for thread ["+Thread.currentThread()+"]");
        }
        return kettleVariables;
    }
    
    /**
     * Create the KettleVariables object and uses the argument as starting point.
     * @param properties The properties to add to an empty set of Kettle Variables.
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
        return properties.getProperty(variableName, defaultValue);
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
     * @return Returns the localThread.
     */
    public Thread getLocalThread()
    {
        return localThread;
    }

    /**
     * @param localThread The localThread to set.
     */
    public void setLocalThread(Thread localThread)
    {
        this.localThread = localThread;
    }

    /**
     * @return Returns the parentThread.
     */
    public Thread getParentThread()
    {
        return parentThread;
    }

    /**
     * @param parentThread The parentThread to set.
     */
    public void setParentThread(Thread parentThread)
    {
        this.parentThread = parentThread;
    }
}
