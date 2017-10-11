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

package org.pentaho.di.job;

import java.util.Comparator;
import java.util.Date;

import org.pentaho.di.core.Result;
import org.pentaho.di.job.entry.JobEntryCopy;

/**
 * This class holds the result of a job entry after it was executed. Things we want to keep track of are:
 * <p>
 * --> result of the execution (Result)
 * <p>
 * --> ...
 * <p>
 *
 * @author Matt
 * @since 16-mrt-2005
 */
public class JobEntryResult implements Cloneable, Comparator<JobEntryResult>, Comparable<JobEntryResult> {
  private Result result;
  private String jobEntryName;
  private int jobEntryNr;

  private String comment;
  private String reason;

  private Date logDate;
  private String jobEntryFilename;
  private String logChannelId;

  private boolean checkpoint;

  /**
   * Creates a new empty job entry result...
   */
  public JobEntryResult() {
    logDate = new Date();
  }

  /**
   * Creates a new job entry result...
   *
   * @param result
   *          the result of the job entry
   * @param comment
   *          an optional comment
   * @param jobEntry
   *          the job entry for which this is the result.
   */
  public JobEntryResult( Result result, String logChannelId, String comment, String reason, String jobEntryName,
    int jobEntryNr, String jobEntryFilename ) {
    this();
    if ( result != null ) {
      // lightClone doesn't bother cloning all the rows.
      this.result = result.lightClone();
      this.result.setLogText( null );
      // this.result.setRows(null);
    } else {
      this.result = null;
    }
    this.logChannelId = logChannelId;
    this.comment = comment;
    this.reason = reason;
    this.jobEntryName = jobEntryName;
    this.jobEntryNr = jobEntryNr;
    this.jobEntryFilename = jobEntryFilename;
  }

  /**
   * @deprecated use {@link #JobEntryResult(Result, String, String, String, String, int, String)}
   *
   * @param result
   * @param comment
   * @param reason
   * @param copy
   */
  @Deprecated
  public JobEntryResult( Result result, String comment, String reason, JobEntryCopy copy ) {

    this( result, copy.getEntry().getLogChannel().getLogChannelId(), comment, reason, copy != null ? copy
      .getName() : null, copy != null ? copy.getNr() : 0, copy == null ? null : ( copy.getEntry() != null ? copy
      .getEntry().getFilename() : null ) );
  }

  @Override
  public Object clone() {
    try {
      JobEntryResult jobEntryResult = (JobEntryResult) super.clone();

      if ( getResult() != null ) {
        jobEntryResult.setResult( getResult().clone() );
      }

      return jobEntryResult;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  /**
   * @param result
   *          The result to set.
   */
  public void setResult( Result result ) {
    this.result = result;
  }

  /**
   * @return Returns the result.
   */
  public Result getResult() {
    return result;
  }

  /**
   * @return Returns the comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * @param comment
   *          The comment to set.
   */
  public void setComment( String comment ) {
    this.comment = comment;
  }

  /**
   * @return Returns the reason.
   */
  public String getReason() {
    return reason;
  }

  /**
   * @param reason
   *          The reason to set.
   */
  public void setReason( String reason ) {
    this.reason = reason;
  }

  /**
   * @return Returns the logDate.
   */
  public Date getLogDate() {
    return logDate;
  }

  /**
   * @param logDate
   *          The logDate to set.
   */
  public void setLogDate( Date logDate ) {
    this.logDate = logDate;
  }

  /**
   * @return the jobEntryName
   */
  public String getJobEntryName() {
    return jobEntryName;
  }

  /**
   * @param jobEntryName
   *          the jobEntryName to set
   */
  public void setJobEntryName( String jobEntryName ) {
    this.jobEntryName = jobEntryName;
  }

  /**
   * @return the jobEntryFilename
   */
  public String getJobEntryFilename() {
    return jobEntryFilename;
  }

  /**
   * @param jobEntryFilename
   *          the jobEntryFilename to set
   */
  public void setJobEntryFilename( String jobEntryFilename ) {
    this.jobEntryFilename = jobEntryFilename;
  }

  /**
   * @return the jobEntryNr
   */
  public int getJobEntryNr() {
    return jobEntryNr;
  }

  /**
   * @param jobEntryNr
   *          the jobEntryNr to set
   */
  public void setJobEntryNr( int jobEntryNr ) {
    this.jobEntryNr = jobEntryNr;
  }

  @Override
  public int compare( JobEntryResult one, JobEntryResult two ) {
    if ( one == null && two != null ) {
      return -1;
    }
    if ( one != null && two == null ) {
      return 1;
    }
    if ( one == null && two == null ) {
      return 0;
    }
    if ( one.getJobEntryName() == null && two.getJobEntryName() != null ) {
      return -1;
    }
    if ( one.getJobEntryName() != null && two.getJobEntryName() == null ) {
      return 1;
    }
    if ( one.getJobEntryName() == null && two.getJobEntryName() == null ) {
      return 0;
    }

    int cmp = one.getJobEntryName().compareTo( two.getJobEntryName() );
    if ( cmp != 0 ) {
      return cmp;
    }
    return Integer.valueOf( one.getJobEntryNr() ).compareTo( Integer.valueOf( two.getJobEntryNr() ) );
  }

  @Override
  public int compareTo( JobEntryResult two ) {
    return compare( this, two );
  }

  public String getLogChannelId() {
    return logChannelId;
  }

  /**
   * @return the checkpoint
   */
  public boolean isCheckpoint() {
    return checkpoint;
  }

  /**
   * @param checkpoint
   *          the checkpoint to set
   */
  public void setCheckpoint( boolean checkpoint ) {
    this.checkpoint = checkpoint;
  }
}
