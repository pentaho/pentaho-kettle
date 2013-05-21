package org.pentaho.di.job;

import org.pentaho.di.core.Result;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobExecutionExtension {
  
  public Job job;
  public Result result;
  public JobEntryCopy jobEntryCopy;
  public boolean executeEntry;
  
  public JobExecutionExtension(Job job, Result result, JobEntryCopy jobEntryCopy, boolean executeEntry) {
    super();
    this.job = job;
    this.result = result;
    this.jobEntryCopy = jobEntryCopy;
    this.executeEntry = executeEntry;
  }
}
