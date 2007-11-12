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
