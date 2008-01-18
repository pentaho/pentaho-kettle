package org.pentaho.di.core;

import org.eclipse.core.runtime.IProgressMonitor;

public class ProgressMonitorAdapter implements ProgressMonitorListener {
	private IProgressMonitor monitor;
	
	public ProgressMonitorAdapter(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	public void beginTask(String message, int nrWorks) {
		monitor.beginTask(message, nrWorks);
	}

	public void done() {
		monitor.done();
	}

	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	public void subTask(String message) {
		monitor.subTask(message);
	}

	public void worked(int nrWorks) {
		monitor.worked(nrWorks);
	}
	
	public void setTaskName(String taskName) {
		monitor.setTaskName(taskName);
	}

}
