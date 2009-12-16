package org.pentaho.di.job;

import org.pentaho.di.core.exception.KettleException;

public interface JobListener {
	public void jobFinished(Job job) throws KettleException;
}
