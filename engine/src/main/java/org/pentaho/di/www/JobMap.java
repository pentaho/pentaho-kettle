/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
  private final Map<CarteObjectEntry, Job> jobMap;
  private final Map<CarteObjectEntry, JobConfiguration> configurationMap;

  private SlaveServerConfig slaveServerConfig;

  public JobMap() {
    jobMap = new HashMap<>();
    configurationMap = new HashMap<>();
  }

  public synchronized void addJob( String jobName, String carteObjectId, Job job, JobConfiguration jobConfiguration ) {
    CarteObjectEntry entry = new CarteObjectEntry( jobName, carteObjectId );
    jobMap.put( entry, job );
    configurationMap.put( entry, jobConfiguration );
  }

  public synchronized void registerJob( Job job, JobConfiguration jobConfiguration ) {
    job.setContainerObjectId( UUID.randomUUID().toString() );
    CarteObjectEntry entry = new CarteObjectEntry( job.getJobMeta().getName(), job.getContainerObjectId() );
    jobMap.put( entry, job );
    configurationMap.put( entry, jobConfiguration );
  }

  public synchronized void replaceJob( CarteObjectEntry entry, Job job, JobConfiguration jobConfiguration ) {
    jobMap.put( entry, job );
    configurationMap.put( entry, jobConfiguration );
  }

  /**
   * Find the first job in the list that comes to mind!
   *
   * @param jobName
   * @return the first transformation with the specified name
   */
  public synchronized Job getJob( String jobName ) {
    for ( CarteObjectEntry entry : jobMap.keySet() ) {
      if ( entry.getName().equals( jobName ) ) {
        return getJob( entry );
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte job object
   * @return the job with the specified entry
   */
  public synchronized Job getJob( CarteObjectEntry entry ) {
    return jobMap.get( entry );
  }

  public synchronized JobConfiguration getConfiguration( String jobName ) {
    for ( CarteObjectEntry entry : configurationMap.keySet() ) {
      if ( entry.getName().equals( jobName ) ) {
        return getConfiguration( entry );
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte job object
   * @return the job configuration with the specified entry
   */
  public synchronized JobConfiguration getConfiguration( CarteObjectEntry entry ) {
    return configurationMap.get( entry );
  }

  public synchronized void removeJob( CarteObjectEntry entry ) {
    jobMap.remove( entry );
    configurationMap.remove( entry );
  }

  public synchronized List<CarteObjectEntry> getJobObjects() {
    return new ArrayList<>( jobMap.keySet() );
  }

  public synchronized CarteObjectEntry getFirstCarteObjectEntry( String jobName ) {
    for ( CarteObjectEntry key : jobMap.keySet() ) {
      if ( key.getName().equals( jobName ) ) {
        return key;
      }
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
  public void setSlaveServerConfig( SlaveServerConfig slaveServerConfig ) {
    this.slaveServerConfig = slaveServerConfig;
  }

  /**
   * Find a job using the container/carte object ID.
   *
   * @param id
   *          the container/carte object ID
   * @return The job if it's found, null if the ID couldn't be found in the job map.
   */
  public synchronized Job findJob( String id ) {
    for ( Job job : jobMap.values() ) {
      if ( job.getContainerObjectId().equals( id ) ) {
        return job;
      }
    }
    return null;
  }

}
