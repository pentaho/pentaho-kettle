/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.configuration.impl.extension;

import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.engine.configuration.impl.EmbeddedRunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Function;

/**
 * Created by bmorrise on 3/16/17.
 */
@ExtensionPoint( id = "RunConfigurationRunExtensionPoint", extensionPointId = "TransBeforeStart",
  description = "" )
public class RunConfigurationRunExtensionPoint implements ExtensionPointInterface {

  private static Class<?> PKG = RunConfigurationRunExtensionPoint.class;
  // basically exists for testing.
  private Function<Bowl, RunConfigurationManager> rcmProvider = bowl ->
    RunConfigurationManager.getInstance( () -> bowl != null ? bowl.getMetastore() :
                                         DefaultBowl.getInstance().getMetastore() );

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    ExecutionConfiguration executionConfiguration = (ExecutionConfiguration) ( (Object[]) o )[ 0 ];
    AbstractMeta meta = (AbstractMeta) ( (Object[]) o )[ 1 ];
    VariableSpace variableSpace = (VariableSpace) ( (Object[]) o )[ 2 ];
    Repository repository = (Repository) ( (Object[]) o )[ 3 ];
    EmbeddedMetaStore embeddedMetaStore = meta.getEmbeddedMetaStore();

    RunConfigurationManager runConfigurationManager = rcmProvider.apply( meta.getBowl() );
    RunConfiguration runConfiguration =
      runConfigurationManager.load( executionConfiguration.getRunConfiguration() );

    if ( runConfiguration == null ) {
      RunConfigurationManager embeddedRunConfigurationManager =
        EmbeddedRunConfigurationManager.build( embeddedMetaStore );
      runConfiguration = embeddedRunConfigurationManager.load( executionConfiguration.getRunConfiguration() );
    }

    if ( runConfiguration != null ) {
      RunConfigurationExecutor runConfigurationExecutor =
        runConfigurationManager.getExecutor( runConfiguration.getType() );

      if ( runConfigurationExecutor != null ) {
        runConfigurationExecutor.execute( runConfiguration, executionConfiguration, meta, variableSpace, repository );
      }
    } else {
      String name = "";
      if ( variableSpace instanceof TransMeta ) {
        name = ( (TransMeta) variableSpace ).getFilename();
      }
      throw new KettleException( BaseMessages
        .getString( PKG, "RunConfigurationRunExtensionPoint.ConfigNotFound.Error", name,
          executionConfiguration.getRunConfiguration(), "{0}" ) );
    }
  }

  @VisibleForTesting
  void setRunConfigurationManagerProvider ( Function<Bowl, RunConfigurationManager> provider ) {
    this.rcmProvider = provider;
  }
}
