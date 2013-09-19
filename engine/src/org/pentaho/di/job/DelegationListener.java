package org.pentaho.di.job;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;

public interface DelegationListener {
  public void jobDelegationStarted(Job delegatedJob, JobExecutionConfiguration jobExecutionConfiguration);
  public void transformationDelegationStarted(Trans delegatedTrans, TransExecutionConfiguration transExecutionConfiguration);
}
