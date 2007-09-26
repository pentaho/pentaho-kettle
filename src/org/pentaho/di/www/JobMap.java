package org.pentaho.di.www;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Appender;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;


/**
 * This is a map between the job name and the (running/waiting/finished) job.
 * 
 * @author Matt
 * @since  26-SEP-2007
 * @since  3.0.0
 *
 */
public class JobMap
{
    private Map<String, Job> jobMap;
    private Map<String, JobConfiguration> configurationMap;
    private Map<String, Appender> loggingMap;
        
    public JobMap()
    {
        jobMap = new Hashtable<String, Job>();
        configurationMap  = new Hashtable<String, JobConfiguration>();
        loggingMap        = new Hashtable<String, Appender>();
        
    }
    
    public synchronized void addJob(String jobName, Job job, JobConfiguration jobConfiguration)
    {
        jobMap.put(jobName, job);
        configurationMap.put(jobName, jobConfiguration);
    }
    
    public synchronized Job getJob(String jobName)
    {
        return jobMap.get(jobName);
    }
    
    public synchronized JobConfiguration getConfiguration(String jobName)
    {
        return configurationMap.get(jobName);
    }

    public synchronized void removeJob(String jobName)
    {
        jobMap.remove(jobName);
        configurationMap.remove(jobName);
    }
    
    public synchronized Appender getAppender(String jobName)
    {
        return loggingMap.get(jobName);
    }
    
    public synchronized void addAppender(String jobName, Appender appender)
    {
        loggingMap.put(jobName, appender);
    }

    public synchronized void removeAppender(String jobName)
    {
        loggingMap.remove(jobName);
    }
    
    public String[] getJobNames()
    {
        Set<String> keySet = jobMap.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    /**
     * @return the configurationMap
     */
    public Map<String, JobConfiguration> getConfigurationMap()
    {
        return configurationMap;
    }

    /**
     * @param configurationMap the configurationMap to set
     */
    public void setConfigurationMap(Map<String, JobConfiguration> configurationMap)
    {
        this.configurationMap = configurationMap;
    }
}
