package org.pentaho.di.job;

import org.pentaho.di.core.Result;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;

public interface JobEntryListener {
	public void beforeExecution(Job job, JobEntryCopy jobEntryCopy, JobEntryInterface jobEntryInterface);
	public void afterExecution(Job job, JobEntryCopy jobEntryCopy, JobEntryInterface jobEntryInterface, Result result);
}
