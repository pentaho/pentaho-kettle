package org.pentaho.di.www;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Appender;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;


/**
 * This is a map between the transformation name and the (running/waiting/finished) transformation.
 * 
 * @author Matt
 *
 */
public class TransformationMap
{
    private Map<String, Trans> transformationMap;
    private Map<String, TransConfiguration> configurationMap;
    private Map<String, Appender> loggingMap;
    
    private String parentThreadName;
    
    public TransformationMap(String parentThreadName)
    {
        this.parentThreadName = parentThreadName;
        
        transformationMap = new Hashtable<String, Trans>();
        configurationMap  = new Hashtable<String, TransConfiguration>();
        loggingMap        = new Hashtable<String, Appender>();
    }
    
    public synchronized void addTransformation(String transformationName, Trans trans, TransConfiguration transConfiguration)
    {
        transformationMap.put(transformationName, trans);
        configurationMap.put(transformationName, transConfiguration);
    }
    
    public synchronized Trans getTransformation(String transformationName)
    {
        return transformationMap.get(transformationName);
    }
    
    public synchronized TransConfiguration getConfiguration(String transformationName)
    {
        return configurationMap.get(transformationName);
    }

    public synchronized void removeTransformation(String transformationName)
    {
        transformationMap.remove(transformationName);
        configurationMap.remove(transformationName);
    }
    
    public synchronized Appender getAppender(String transformationName)
    {
        return loggingMap.get(transformationName);
    }
    
    public synchronized void addAppender(String transformationName, Appender appender)
    {
        loggingMap.put(transformationName, appender);
    }

    public synchronized void removeAppender(String transformationName)
    {
        loggingMap.remove(transformationName);
    }
    
    public String[] getTransformationNames()
    {
        Set<String> keySet = transformationMap.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    /**
     * @return the parentThreadName
     */
    public String getParentThreadName()
    {
        return parentThreadName;
    }

    /**
     * @return the configurationMap
     */
    public Map<String, TransConfiguration> getConfigurationMap()
    {
        return configurationMap;
    }

    /**
     * @param configurationMap the configurationMap to set
     */
    public void setConfigurationMap(Map<String, TransConfiguration> configurationMap)
    {
        this.configurationMap = configurationMap;
    }
}
