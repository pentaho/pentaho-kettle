package org.pentaho.di.core;

public interface ProgressMonitorListener {
	public void beginTask(String message, int nrWorks);
	public void subTask(String message);
	public boolean isCanceled();
	public void worked(int nrWorks);
	public void done();
	public void setTaskName(String taskName);
}
