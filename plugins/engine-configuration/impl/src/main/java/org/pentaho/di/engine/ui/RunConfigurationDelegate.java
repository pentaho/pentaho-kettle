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


package org.pentaho.di.engine.ui;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.swt.SWT;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
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

  private RunConfigurationService configurationManager = RunConfigurationManager.getInstance();
  private static RunConfigurationDelegate instance;

  public static RunConfigurationDelegate getInstance() {
    if ( null == instance ) {
      instance = new RunConfigurationDelegate();
    }
    return instance;
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

  @VisibleForTesting
  void setRunConfigurationManager( RunConfigurationService runConfigurationManager ) {
    this.configurationManager = runConfigurationManager;
  }
}
