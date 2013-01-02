package org.pentaho.di.core.listeners;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public class SubComponentExecutionAdapter implements SubComponentExecutionListener {

  @Override
  public void beforeTransformationExecution(Trans trans) throws KettleException {
  }

  @Override
  public void afterTransformationExecution(Trans trans) throws KettleException {
  }

  @Override
  public void beforeJobExecution(Job job) throws KettleException {
  }

  @Override
  public void afterJobExecution(Job job) throws KettleException {
  }

}
