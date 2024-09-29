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


package org.pentaho.di.ui.spoon.delegates;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.InstanceCreationException;
import org.pentaho.di.ui.spoon.Spoon;

public class SpoonDelegates {
  public SpoonJobDelegate jobs;

  public SpoonTabsDelegate tabs;

  public SpoonTransformationDelegate trans;

  public SpoonSlaveDelegate slaves;

  public SpoonTreeDelegate tree;

  public SpoonStepsDelegate steps;

  public SpoonDBDelegate db;

  public SpoonClustersDelegate clusters;

  public SpoonPartitionsDelegate partitions;

  public SpoonDelegates( Spoon spoon ) {
    tabs = new SpoonTabsDelegate( spoon );
    tree = new SpoonTreeDelegate( spoon );
    slaves = new SpoonSlaveDelegate( spoon );
    steps = new SpoonStepsDelegate( spoon );
    db = new SpoonDBDelegate( spoon );
    clusters = new SpoonClustersDelegate( spoon );
    partitions = new SpoonPartitionsDelegate( spoon );
    update( spoon );
  }

  public void update( Spoon spoon ) {
    SpoonJobDelegate origJobs = jobs;
    try {
      jobs = (SpoonJobDelegate) SpoonDelegateRegistry.getInstance().constructSpoonJobDelegate( spoon );
    } catch ( InstanceCreationException e ) {
      jobs = new SpoonJobDelegate( spoon );
    }
    if ( origJobs != null ) {
      // preserve open jobs
      for ( JobMeta jobMeta : origJobs.getLoadedJobs() ) {
        jobs.addJob( jobMeta );
      }
    }
    SpoonTransformationDelegate origTrans = trans;
    try {
      trans =
        (SpoonTransformationDelegate) SpoonDelegateRegistry.getInstance().constructSpoonTransDelegate( spoon );
    } catch ( InstanceCreationException e ) {
      trans = new SpoonTransformationDelegate( spoon );
    }
    if ( origTrans != null ) {
      // preseve open trans
      for ( TransMeta transMeta : origTrans.getLoadedTransformations() ) {
        trans.addTransformation( transMeta );
      }
    }
  }
}
