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

package org.pentaho.di.core.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;

/**
 * Responsible for tracking the execution of a job as a hierarchy.
 *
 * @author Matt
 * @since 30-mar-2006
 *
 */
public class JobTracker {
  /**
   * The trackers for each individual job entry.
   * Since we invoke LinkedList.removeFirst() there is no sense in lurking the field behind the interface
   */
  private LinkedList<JobTracker> jobTrackers;

  /** If the jobTrackers list is empty, then this is the result */
  private JobEntryResult result;

  /** The parent job tracker, null if this is the root */
  private JobTracker parentJobTracker;

  private String jobName;

  private String jobFilename;

  private int maxChildren;

  private final ReentrantReadWriteLock lock;

  /**
   * @param jobMeta
   *          the job metadata to keep track of (with maximum 5000 children)
   */
  public JobTracker( JobMeta jobMeta ) {
    this( jobMeta, Const.toInt( EnvUtil.getSystemProperty( Const.KETTLE_MAX_JOB_TRACKER_SIZE ), 5000 ) );
  }

  /**
   * @param jobMeta
   *          The job metadata to track
   * @param maxChildren
   *          The maximum number of children to keep track of (1000 is the default)
   */
  public JobTracker( JobMeta jobMeta, int maxChildren ) {
    if ( jobMeta != null ) {
      this.jobName = jobMeta.getName();
      this.jobFilename = jobMeta.getFilename();
    }

    this.jobTrackers = new LinkedList<JobTracker>();
    this.maxChildren = maxChildren;
    this.lock = new ReentrantReadWriteLock();
  }

  /**
   * Creates a jobtracker with a single result (maxChildren children are kept)
   *
   * @param jobMeta
   *          the job metadata to keep track of
   * @param result
   *          the job entry result to track.
   */
  public JobTracker( JobMeta jobMeta, JobEntryResult result ) {
    this( jobMeta );
    this.result = result;
  }

  /**
   * Creates a jobtracker with a single result
   *
   * @param jobMeta
   *          the job metadata to keep track of
   * @param maxChildren
   *          The maximum number of children to keep track of
   * @param result
   *          the job entry result to track.
   */
  public JobTracker( JobMeta jobMeta, int maxChildren, JobEntryResult result ) {
    this( jobMeta, maxChildren );
    this.result = result;
  }

  public void addJobTracker( JobTracker jobTracker ) {
    lock.writeLock().lock();
    try {
      jobTrackers.add( jobTracker );
      while ( jobTrackers.size() > maxChildren ) {
        // Use remove instead of subList
        jobTrackers.removeFirst();
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public JobTracker getJobTracker( int i ) {
    lock.readLock().lock();
    try {
      return jobTrackers.get( i );
    } finally {
      lock.readLock().unlock();
    }
  }

  public int nrJobTrackers() {
    lock.readLock().lock();
    try {
      return jobTrackers.size();
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Returns a list that contains all job trackers. The list is created as a defensive copy of internal trackers'
   * storage.
   * @return  list of job trackers
   */
  public List<JobTracker> getJobTrackers() {
    lock.readLock().lock();
    try {
      return new ArrayList<JobTracker>( jobTrackers );
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * @param jobTrackers
   *          The jobTrackers to set.
   */
  public void setJobTrackers( List<JobTracker> jobTrackers ) {
    lock.writeLock().lock();
    try {
      this.jobTrackers.clear();
      this.jobTrackers.addAll( jobTrackers );
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * @return Returns the result.
   */
  public JobEntryResult getJobEntryResult() {
    return result;
  }

  /**
   * @param result
   *          The result to set.
   */
  public void setJobEntryResult( JobEntryResult result ) {
    this.result = result;
  }

  public void clear() {
    lock.writeLock().lock();
    try {
      jobTrackers.clear();
      result = null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Finds the JobTracker for the job entry specified. Use this to
   *
   * @param jobEntryCopy
   *          The entry to search the job tracker for
   * @return The JobTracker of null if none could be found...
   */
  public JobTracker findJobTracker( JobEntryCopy jobEntryCopy ) {
    if ( jobEntryCopy.getName() == null ) {
      return null;
    }

    lock.readLock().lock();
    try {
      ListIterator<JobTracker> it = jobTrackers.listIterator( jobTrackers.size() );
      while ( it.hasPrevious() ) {
        JobTracker tracker = it.previous();
        JobEntryResult result = tracker.getJobEntryResult();
        if ( result != null ) {
          if ( jobEntryCopy.getName().equals( result.getJobEntryName() )
            && jobEntryCopy.getNr() == result.getJobEntryNr() ) {
            return tracker;
          }
        }
      }
    } finally {
      lock.readLock().unlock();
    }
    return null;
  }

  /**
   * @return Returns the parentJobTracker.
   */
  public JobTracker getParentJobTracker() {
    return parentJobTracker;
  }

  /**
   * @param parentJobTracker
   *          The parentJobTracker to set.
   */
  public void setParentJobTracker( JobTracker parentJobTracker ) {
    this.parentJobTracker = parentJobTracker;
  }

  public int getTotalNumberOfItems() {
    lock.readLock().lock();
    try {
      int total = 1; // 1 = this one

      for ( JobTracker jobTracker : jobTrackers ) {
        total += jobTracker.getTotalNumberOfItems();
      }

      return total;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * @return the jobFilename
   */
  public String getJobFilename() {
    return jobFilename;
  }

  /**
   * @param jobFilename
   *          the jobFilename to set
   */
  public void setJobFilename( String jobFilename ) {
    this.jobFilename = jobFilename;
  }

  /**
   * @return the jobName
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * @param jobName
   *          the jobName to set
   */
  public void setJobName( String jobName ) {
    this.jobName = jobName;
  }

  /**
   * @return the maxChildren
   */
  public int getMaxChildren() {
    return maxChildren;
  }

  /**
   * @param maxChildren
   *          the maxChildren to set
   */
  public void setMaxChildren( int maxChildren ) {
    this.maxChildren = maxChildren;
  }
}
