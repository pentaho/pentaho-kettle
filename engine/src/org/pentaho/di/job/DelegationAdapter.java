package org.pentaho.di.job;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;

public class DelegationAdapter implements DelegationListener {

  @Override
  public void jobDelegationStarted(Job delegatedJob, JobExecutionConfiguration jobExecutionConfiguration) {
  }

  @Override
  public void transformationDelegationStarted(Trans delegatedTrans, TransExecutionConfiguration transExecutionConfiguration) {
  }
  
}
