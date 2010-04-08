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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;

/**
 * This is a map between the job name and the (running/waiting/finished) job.
 * 
 * @author Matt
 * @since 26-SEP-2007
 * @since 3.0.0
 * 
 */
public class JobMap {
  private Map<CarteObjectEntry, Job>              jobMap;
  private Map<CarteObjectEntry, JobConfiguration> configurationMap;

  private SlaveServerConfig                       slaveServerConfig;

  public JobMap() {
    jobMap = new Hashtable<CarteObjectEntry, Job>();
    configurationMap = new Hashtable<CarteObjectEntry, JobConfiguration>();
  }

  public synchronized void addJob(String jobName, String carteObjectId, Job job, JobConfiguration jobConfiguration) {
    CarteObjectEntry entry = new CarteObjectEntry(jobName, carteObjectId);
    jobMap.put(entry, job);
    configurationMap.put(entry, jobConfiguration);
  }

  public void replaceJob(CarteObjectEntry entry, Job job, JobConfiguration jobConfiguration) {
    jobMap.put(entry, job);
    configurationMap.put(entry, jobConfiguration);
  }

  /**
   * Find the first job in the list that comes to mind!
   * 
   * @param jobName
   * @return the first transformation with the specified name
   */
  public synchronized Job getJob(String jobName) {
    for (CarteObjectEntry entry : jobMap.keySet()) {
      if (entry.getName().equals(jobName)) {
        return jobMap.get(entry);
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte job object
   * @return the job with the specified entry
   */
  public synchronized Job getJob(CarteObjectEntry entry) {
    return jobMap.get(entry);
  }

  public synchronized JobConfiguration getConfiguration(String jobName) {
    for (CarteObjectEntry entry : configurationMap.keySet()) {
      if (entry.getName().equals(jobName)) {
        return configurationMap.get(entry);
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte job object
   * @return the job configuration with the specified entry
   */
  public synchronized JobConfiguration getConfiguration(CarteObjectEntry entry) {
    return configurationMap.get(entry);
  }

  public synchronized void removeJob(CarteObjectEntry entry) {
    jobMap.remove(entry);
    configurationMap.remove(entry);
  }

  public List<CarteObjectEntry> getJobObjects() {
    return new ArrayList<CarteObjectEntry>(jobMap.keySet());
  }

  /**
   * @return the configurationMap
   */
  public Map<CarteObjectEntry, JobConfiguration> getConfigurationMap() {
    return configurationMap;
  }

  /**
   * @param configurationMap
   *          the configurationMap to set
   */
  public void setConfigurationMap(Map<CarteObjectEntry, JobConfiguration> configurationMap) {
    this.configurationMap = configurationMap;
  }

  public CarteObjectEntry getFirstCarteObjectEntry(String jobName) {
    for (CarteObjectEntry key : jobMap.keySet()) {
      if (key.getName().equals(jobName))
        return key;
    }
    return null;
  }

  /**
   * @return the slaveServerConfig
   */
  public SlaveServerConfig getSlaveServerConfig() {
    return slaveServerConfig;
  }

  /**
   * @param slaveServerConfig
   *          the slaveServerConfig to set
   */
  public void setSlaveServerConfig(SlaveServerConfig slaveServerConfig) {
    this.slaveServerConfig = slaveServerConfig;
  }

}
