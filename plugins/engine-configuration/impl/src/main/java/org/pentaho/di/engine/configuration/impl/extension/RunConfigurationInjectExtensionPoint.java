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

package org.pentaho.di.engine.configuration.impl.extension;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.impl.EmbeddedRunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;

@ExtensionPoint(
    id = "RunConfigurationInjectExtensionPoint",
    description = "Inject run cofiguration before job entry start",
    extensionPointId = "JobBeforeJobEntryExecution"
  )
public class RunConfigurationInjectExtensionPoint implements ExtensionPointInterface {

  private RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance();

  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    if ( !( object instanceof JobExecutionExtension ) ) {
      return;
    }

    JobExecutionExtension extension = (JobExecutionExtension) object;

    Job job = extension.job;
    JobMeta jobMeta = job.getJobMeta();

    final EmbeddedMetaStore embeddedMetaStore = jobMeta.getEmbeddedMetaStore();

    RunConfigurationManager embeddedRunConfigurationManager =  EmbeddedRunConfigurationManager.build( embeddedMetaStore );

    //will load and save to meta all run configurations
    for ( JobEntryTrans trans : job.getActiveJobEntryTransformations().values() ) {
      RunConfiguration loadedRunConfiguration = runConfigurationManager.load( jobMeta.environmentSubstitute( trans.getRunConfiguration() ) );
      embeddedRunConfigurationManager.save( loadedRunConfiguration );
    }

    for ( JobEntryJob subJob : job.getActiveJobEntryJobs().values() ) {
      RunConfiguration loadedRunConfiguration = runConfigurationManager.load( jobMeta.environmentSubstitute( subJob.getRunConfiguration() ) );
      embeddedRunConfigurationManager.save( loadedRunConfiguration );
    }
  }

  @VisibleForTesting
  void setRunConfigurationManager( RunConfigurationManager runConfigurationManager ) {
    this.runConfigurationManager = runConfigurationManager;
  }
}
