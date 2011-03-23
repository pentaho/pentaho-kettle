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
/**
 * 
 */
package org.pentaho.di.core.gui;

import java.util.ArrayList;
import java.util.List;

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
  /** The trackers for each individual job entry */
  private List<JobTracker> jobTrackers;

  /** If the jobTrackers list is empty, then this is the result */
  private JobEntryResult   result;

  /** The parent job tracker, null if this is the root */
  private JobTracker       parentJobTracker;

  private String           jobName;

  private String           jobFilename;

  private int              maxChildren;

  /**
  * @param jobMeta the job metadata to keep track of (with maximum 1000 children)
  */
  public JobTracker(JobMeta jobMeta) {
    if (jobMeta != null) {
      this.jobName = jobMeta.getName();
      this.jobFilename = jobMeta.getFilename();
    }

    jobTrackers = new ArrayList<JobTracker>();
    maxChildren = Const.toInt(EnvUtil.getSystemProperty(Const.KETTLE_MAX_JOB_TRACKER_SIZE), 1000);
  }

  /**
   * @param jobMeta The job metadata to track
   * @param maxChildren The maximum number of children to keep track of (1000 is the default)
   */
  public JobTracker(JobMeta jobMeta, int maxChildren) {
    if (jobMeta != null) {
      this.jobName = jobMeta.getName();
      this.jobFilename = jobMeta.getFilename();
    }

    jobTrackers = new ArrayList<JobTracker>();
    this.maxChildren = maxChildren;
  }

  /**
   * Creates a jobtracker with a single result (max 1000 children are kept)
   * 
   * @param jobMeta the job metadata to keep track of
   * @param result the job entry result to track.
   */
  public JobTracker(JobMeta jobMeta, JobEntryResult result) {
    this(jobMeta);
    this.result = result;
  }

  /**
   * Creates a jobtracker with a single result
   * 
   * @param jobMeta the job metadata to keep track of
   * @param maxChildren The maximum number of children to keep track of
   * @param result the job entry result to track.
   */
  public JobTracker(JobMeta jobMeta, int maxChildren, JobEntryResult result) {
    this(jobMeta, maxChildren);
    this.result = result;
  }

  public void addJobTracker(JobTracker jobTracker) {
    jobTrackers.add(jobTracker);
    if (jobTrackers.size()>maxChildren) jobTrackers.remove(0);
  }

  public JobTracker getJobTracker(int i) {
    return jobTrackers.get(i);
  }

  public int nrJobTrackers() {
    return jobTrackers.size();
  }

  /**
   * @return Returns the jobTrackers.
   */
  public List<JobTracker> getJobTrackers() {
    return jobTrackers;
  }

  /**
   * @param jobTrackers
   *          The jobTrackers to set.
   */
  public void setJobTrackers(List<JobTracker> jobTrackers) {
    this.jobTrackers = jobTrackers;
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
  public void setJobEntryResult(JobEntryResult result) {
    this.result = result;
  }

  public void clear() {
    jobTrackers.clear();
    result = null;
  }

  /**
   * Finds the JobTracker for the job entry specified. Use this to
   * 
   * @param jobEntryCopy
   *          The entry to search the job tracker for
   * @return The JobTracker of null if none could be found...
   */
  public JobTracker findJobTracker(JobEntryCopy jobEntryCopy) {
    for (int i = jobTrackers.size() - 1; i >= 0; i--) {
      JobTracker tracker = getJobTracker(i);
      JobEntryResult result = tracker.getJobEntryResult();
      if (result != null) {
        if (jobEntryCopy.getName() != null && jobEntryCopy.getName().equals(result.getJobEntryName()) && jobEntryCopy.getNr() == result.getJobEntryNr()) {
          return tracker;
        }
      }
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
  public void setParentJobTracker(JobTracker parentJobTracker) {
    this.parentJobTracker = parentJobTracker;
  }

  public int getTotalNumberOfItems() {
    int total = 1; // 1 = this one

    for (int i = 0; i < nrJobTrackers(); i++) {
      total += getJobTracker(i).getTotalNumberOfItems();
    }

    return total;
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
  public void setJobFilename(String jobFilename) {
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
  public void setJobName(String jobName) {
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
  public void setMaxChildren(int maxChildren) {
    this.maxChildren = maxChildren;
  }
}
