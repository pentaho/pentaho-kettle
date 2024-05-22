/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.impl.EmbeddedRunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryRunConfigurableInterface;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.List;

/**
 * Created by bmorrise on 5/3/17.
 */
@ExtensionPoint( id = "RunConfigurationSaveExtensionPoint", extensionPointId = "JobEntryTransSave",
  description = "" )
public class RunConfigurationSaveExtensionPoint implements ExtensionPointInterface {

  private Function<JobMeta, RunConfigurationManager> rcmProvider =
    jm -> RunConfigurationManager.getInstance( () -> jm.getBowl().getMetastore() );

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    JobMeta jobMeta = (JobMeta) ( (Object[]) o )[ 0 ];
    final EmbeddedMetaStore embeddedMetaStore = jobMeta.getEmbeddedMetaStore();

    RunConfigurationManager embeddedRunConfigurationManager =
      EmbeddedRunConfigurationManager.build( embeddedMetaStore );
    embeddedRunConfigurationManager.deleteAll();

    List<String> runConfigurationNames = new ArrayList<>();
    boolean embedAll = false;
    for ( JobEntryCopy jobEntryCopy : jobMeta.getJobCopies() ) {
      if ( jobEntryCopy.getEntry() instanceof JobEntryRunConfigurableInterface ) {
        String usedConfiguration = ( (JobEntryRunConfigurableInterface) jobEntryCopy.getEntry() ).getRunConfiguration();
        embedAll = embedAll || StringUtil.isVariable( usedConfiguration );
        if ( !Utils.isEmpty( usedConfiguration ) && !runConfigurationNames.contains( usedConfiguration ) ) {
          runConfigurationNames.add( usedConfiguration );
        }
      }
    }

    if ( embedAll ) {
      embedAllRunConfigurations( jobMeta, embeddedRunConfigurationManager );
    } else {
      embedRunConfigurations( jobMeta, embeddedRunConfigurationManager, runConfigurationNames );
    }
  }

  private void embedAllRunConfigurations( JobMeta jobMeta, RunConfigurationManager embeddedRunConfigurationManager ) {
    List<RunConfiguration> runConfigurations = getRunConfigurationManager( jobMeta ).load();
    for ( RunConfiguration loadedRunConfiguration : runConfigurations ) {
      if ( !loadedRunConfiguration.isReadOnly() ) {
        embeddedRunConfigurationManager.save( loadedRunConfiguration );
      }
    }
  }

  private void embedRunConfigurations( JobMeta jobMeta, RunConfigurationManager embeddedRunConfigurationManager,
                                       List<String> runConfigurationNames ) {
    for ( String runConfigurationName : runConfigurationNames ) {
      if ( !runConfigurationName.equals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
        RunConfiguration loadedRunConfiguration = getRunConfigurationManager( jobMeta ).load( runConfigurationName );
        embeddedRunConfigurationManager.save( loadedRunConfiguration );
      }
    }
  }

  @VisibleForTesting
  void setRunConfigurationManager( RunConfigurationManager runConfigurationManager ) {
    this.rcmProvider = x -> runConfigurationManager;
  }

  private RunConfigurationManager getRunConfigurationManager( JobMeta meta) {
    return rcmProvider.apply( meta );
  }
}
