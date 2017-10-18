/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
