/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.extension;

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

  private RunConfigurationManager runConfigurationManager;

  public RunConfigurationInjectExtensionPoint( RunConfigurationManager runConfigurationManager ) {
    this.runConfigurationManager = runConfigurationManager;
  }

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

}
