/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.www;

import org.pentaho.di.job.DelegationListener;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;

/**
 * A handler for registering sub-jobs and sub-transformations on the carte maps. The trick here is that listeners are
 * added recursively down as long as the listener methods are called.
 *
 * @author matt
 *
 */
public class CarteDelegationHandler implements DelegationListener {

  protected TransformationMap transformationMap;
  protected JobMap jobMap;

  public CarteDelegationHandler( TransformationMap transformationMap, JobMap jobMap ) {
    super();
    this.transformationMap = transformationMap;
    this.jobMap = jobMap;
  }

  @Override
  public synchronized void jobDelegationStarted( Job delegatedJob,
                                                 JobExecutionConfiguration jobExecutionConfiguration ) {

    JobConfiguration jc = new JobConfiguration( delegatedJob.getJobMeta(), jobExecutionConfiguration );
    jobMap.registerJob( delegatedJob, jc );

    delegatedJob.addDelegationListener( this );
  }

  @Override
  public synchronized void transformationDelegationStarted( Trans delegatedTrans,
    TransExecutionConfiguration transExecutionConfiguration ) {
    TransConfiguration tc = new TransConfiguration( delegatedTrans.getTransMeta(), transExecutionConfiguration );
    transformationMap.registerTransformation( delegatedTrans, tc );
    delegatedTrans.addDelegationListener( this );

  }

}
