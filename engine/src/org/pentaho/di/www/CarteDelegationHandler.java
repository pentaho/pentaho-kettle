package org.pentaho.di.www;

import org.pentaho.di.job.DelegationListener;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;

/**
 * A handler for registering sub-jobs and sub-transformations on the carte maps.
 * The trick here is that listeners are added recursively down as long as the listener methods are called.
 * 
 * @author matt
 *
 */
public class CarteDelegationHandler implements DelegationListener {
  
  protected TransformationMap transformationMap;
  protected JobMap jobMap;

  public CarteDelegationHandler(TransformationMap transformationMap, JobMap jobMap) {
    super();
    this.transformationMap = transformationMap;
    this.jobMap = jobMap;
  }

  @Override
  public synchronized void jobDelegationStarted(Job delegatedJob, JobExecutionConfiguration jobExecutionConfiguration) {
    synchronized(jobMap) {
      JobConfiguration jc = new JobConfiguration(delegatedJob.getJobMeta(), jobExecutionConfiguration);
      jobMap.registerJob(delegatedJob, jc);
      
      delegatedJob.addDelegationListener(this);
    }
  }
  
  @Override
  public synchronized void transformationDelegationStarted(Trans delegatedTrans, TransExecutionConfiguration transExecutionConfiguration) {
    synchronized(transformationMap) {
      TransConfiguration tc = new TransConfiguration(delegatedTrans.getTransMeta(), transExecutionConfiguration);
      transformationMap.registerTransformation(delegatedTrans, tc);

      delegatedTrans.addDelegationListener(this);
    }
  }

}
