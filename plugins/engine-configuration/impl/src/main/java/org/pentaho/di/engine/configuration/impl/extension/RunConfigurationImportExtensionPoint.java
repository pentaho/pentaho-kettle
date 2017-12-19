/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.impl.EmbeddedRunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by bmorrise on 5/15/17.
 */
@ExtensionPoint( id = "RunConfigurationImportExtensionPoint", extensionPointId = "JobAfterOpen",
  description = "" )
public class RunConfigurationImportExtensionPoint implements ExtensionPointInterface {

  private RunConfigurationManager runConfigurationManager;

  public RunConfigurationImportExtensionPoint( RunConfigurationManager runConfigurationManager ) {
    this.runConfigurationManager = runConfigurationManager;
  }

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    AbstractMeta abstractMeta = (AbstractMeta) o;

    final EmbeddedMetaStore embeddedMetaStore = abstractMeta.getEmbeddedMetaStore();

    RunConfigurationManager embeddedRunConfigurationManager =
      EmbeddedRunConfigurationManager.build( embeddedMetaStore );

    List<RunConfiguration> runConfigurationList = embeddedRunConfigurationManager.load();
    List<String> runConfigurationNames = runConfigurationList.stream().map( RunConfiguration::getName ).collect( Collectors.toList() );
    runConfigurationNames.addAll( runConfigurationManager.getNames() );
    runConfigurationList.addAll( createSlaveServerRunConfigurations( runConfigurationNames, abstractMeta ) );

    for ( RunConfiguration runConfiguration : runConfigurationList ) {
      if ( !runConfiguration.getName().equals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
        runConfigurationManager.save( runConfiguration );
      }
    }
  }

  private List<RunConfiguration> createSlaveServerRunConfigurations( List<String> existingConfigurationNames, AbstractMeta abstractMeta ) {
    List<RunConfiguration> runConfigurations = new ArrayList<>();
    if ( abstractMeta instanceof JobMeta ) {
      JobMeta jobMeta = (JobMeta) abstractMeta;

      Map<String, List<JobEntryTrans>> slaveServerGroups = jobMeta.getJobCopies().stream()
        .map( JobEntryCopy::getEntry )
        .filter( entry -> entry instanceof JobEntryTrans )
        .map( entry -> (JobEntryTrans) entry )
        .filter( entry -> Utils.isEmpty( entry.getRunConfiguration() ) )
        .filter( entry -> !Utils.isEmpty( entry.getRemoteSlaveServerName() ) )
        .collect( Collectors.groupingBy( JobEntryTrans::getRemoteSlaveServerName ) );

      slaveServerGroups.forEach( (remoteServerName, entries ) -> {
        String runConfigurationName = createRunConfigurationName( existingConfigurationNames, remoteServerName );
        DefaultRunConfiguration runConfiguration = createRunConfiguration( runConfigurationName, remoteServerName );
        runConfigurations.add( runConfiguration );
        entries.forEach( e -> e.setRunConfiguration( runConfiguration.getName() ) );
      } );
    }
    return runConfigurations;
  }

  private DefaultRunConfiguration createRunConfiguration( String configurationName, String slaveServerName ) {
    DefaultRunConfiguration runConfiguration = new DefaultRunConfiguration();
    runConfiguration.setName( configurationName );
    runConfiguration.setServer( slaveServerName );
    runConfiguration.setLocal( false );
    runConfiguration.setRemote( true );
    return runConfiguration;
  }

  @VisibleForTesting String createRunConfigurationName( List<String> runConfigurations, String slaveServerName ) {
    String defaultName = String.format( "pentaho_auto_%s_config", slaveServerName );
    long count = runConfigurations.stream().filter( s -> s.matches( defaultName + "(_\\d)*" ) ).count();
    if ( count == 0 ) {
      return defaultName;
    }

    Optional<Integer> index = runConfigurations.stream()
      .filter( s -> s.matches( defaultName + "_\\d+" ) )
      .map( s -> s.substring( defaultName.length() + 1 ) )
      .filter( s -> s.matches( "\\d+" ) )
      .map( Integer::valueOf )
      .sorted( Comparator.reverseOrder() )
      .findFirst();

    return String.format( "%s_%d", defaultName, index.orElse( 0 ) + 1 );
  }
}
