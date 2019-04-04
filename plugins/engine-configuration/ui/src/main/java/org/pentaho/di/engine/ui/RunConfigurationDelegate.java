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

package org.pentaho.di.engine.ui;

import org.eclipse.swt.SWT;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.JobEntryRunConfigurableInterface;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 3/27/17.
 */
public class RunConfigurationDelegate {

  private static Class<?> PKG = RunConfigurationDelegate.class;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  private RunConfigurationService configurationManager;

  public RunConfigurationDelegate( RunConfigurationService configurationManager ) {
    this.configurationManager = configurationManager;
  }

  public void edit( RunConfiguration runConfiguration ) {
    final String key = runConfiguration.getName();
    RunConfigurationDialog dialog =
      new RunConfigurationDialog( spoonSupplier.get().getShell(), configurationManager,
        runConfiguration );
    RunConfiguration savedRunConfiguration = dialog.open();

    if ( savedRunConfiguration != null ) {
      configurationManager.delete( key );
      configurationManager.save( savedRunConfiguration );
      refreshTree();

      updateLoadedJobs( key, savedRunConfiguration );
    }
  }

  protected void updateLoadedJobs( String key, RunConfiguration runConfig ) {
    for ( JobMeta job : spoonSupplier.get().getLoadedJobs() ) {
      for ( int i = 0; i < job.nrJobEntries(); i++ ) {
        JobEntryInterface entry = job.getJobEntry( i ).getEntry();

        if ( entry instanceof JobEntryRunConfigurableInterface ) {
          JobEntryRunConfigurableInterface jet = (JobEntryRunConfigurableInterface) entry;
          if ( jet.getRunConfiguration() != null ) {
            if ( jet.getRunConfiguration().equals( key ) ) {
              try {
                ExtensionPointHandler.callExtensionPoint( job.getLogChannel(), KettleExtensionPoint.JobEntryTransSave.id,
                  new Object[] { job, runConfig.getName() } );
              } catch ( KettleException e ) {
                spoonSupplier.get().getLog().logBasic( "Unable to set run configuration in job " + job.getName() );
              }

              jet.setRunConfiguration( runConfig.getName() );
              if ( runConfig instanceof DefaultRunConfiguration ) {
                jet.setRemoteSlaveServerName( ( (DefaultRunConfiguration) runConfig ).getServer() );
                jet.setLoggingRemoteWork( ( (DefaultRunConfiguration) runConfig ).isLogRemoteExecutionLocally() );
              }
              jet.setChanged();
            }
          }
        }
      }
    }
  }

  public void delete( RunConfiguration runConfiguration ) {
    RunConfigurationDeleteDialog deleteDialog = new RunConfigurationDeleteDialog( spoonSupplier.get().getShell() );
    int response = deleteDialog.open();
    if ( response == SWT.YES ) {
      configurationManager.delete( runConfiguration.getName() );
      refreshTree();
    }
  }

  public void create() {
    List<String> names = configurationManager.getNames();
    String name = BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.Configuration.Default" ) + " ";
    int index = 1;
    while ( names.contains( name + String.valueOf( index ) ) ) {
      index++;
    }
    name = name + String.valueOf( index );
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( name );

    RunConfigurationDialog dialog =
      new RunConfigurationDialog( spoonSupplier.get().getShell(), configurationManager, defaultRunConfiguration );
    RunConfiguration savedRunConfiguration = dialog.open();

    if ( savedRunConfiguration != null ) {
      configurationManager.save( savedRunConfiguration );
      refreshTree();
    }
  }

  public List<RunConfiguration> load() {
    return configurationManager.load();
  }

  public RunConfiguration load( String name ) {
    return configurationManager.load( name );
  }

  private void refreshTree() {
    spoonSupplier.get().refreshTree( RunConfigurationFolderProvider.STRING_RUN_CONFIGURATIONS );
  }

}
