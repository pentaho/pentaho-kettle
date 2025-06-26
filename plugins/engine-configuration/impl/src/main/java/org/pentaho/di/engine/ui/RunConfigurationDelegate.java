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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.JobEntryRunConfigurableInterface;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 3/27/17.
 */
public class RunConfigurationDelegate {

  private static Class<?> PKG = RunConfigurationDelegate.class;
  private static Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  private RunConfigurationService configurationManager;

  private RunConfigurationDelegate( CheckedMetaStoreSupplier supplier ) {
    configurationManager = RunConfigurationManager.getInstance( supplier );
  }

  public static RunConfigurationDelegate getInstance( CheckedMetaStoreSupplier supplier ) {
    return new RunConfigurationDelegate( supplier );
  }

  public void edit( RunConfiguration runConfiguration ) {
    final String key = runConfiguration.getName();
    List<String> names = configurationManager.getNames();
    RunConfigurationDialog dialog =
      new RunConfigurationDialog( spoonSupplier.get().getShell(), configurationManager,
        runConfiguration, names );
    RunConfiguration savedRunConfiguration = dialog.open();
    if ( savedRunConfiguration == null ) {
      //When user click on cancel.
      return;
    }

    configurationManager.delete( key );
    configurationManager.save( savedRunConfiguration );
    refreshTree();

    updateLoadedJobs( key, savedRunConfiguration );
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
                ExtensionPointHandler.callExtensionPoint( job.getLogChannel(),
                  KettleExtensionPoint.JobEntryTransSave.id,
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
      new RunConfigurationDialog( spoonSupplier.get().getShell(), configurationManager, defaultRunConfiguration, names );
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

  public void duplicate( RunConfiguration runConfiguration ) {
    Set<String> existingNames = new HashSet<>( configurationManager.getNames() );
    String newName = TreeUtil.findUniqueSuffix( runConfiguration.getName(), existingNames );
    runConfiguration.setName( newName );
    List<String> names = configurationManager.getNames();
    RunConfigurationDialog dialog =
      new RunConfigurationDialog( spoonSupplier.get().getShell(), configurationManager, runConfiguration, names );
    RunConfiguration duplicateRunConfiguration = dialog.open();
    if ( duplicateRunConfiguration == null ) {
      //When user click on cancel.
      return;
    }
    if ( !existingNames.contains( duplicateRunConfiguration.getName() ) ) {
      configurationManager.save( duplicateRunConfiguration );
      refreshTree();
    } else {
      showRunConfigurationExistsDialog( spoonSupplier.get().getShell(), runConfiguration );
    }
  }

  private void showRunConfigurationExistsDialog( Shell parent, RunConfiguration runConfiguration ) {
    String title = BaseMessages.getString( PKG, "RunConfigurationDialog.RunConfigurationNameExists.Title" );
    String message = BaseMessages.getString( PKG, "RunConfigurationDialog.RunConfigurationNameExists", runConfiguration.getName() );
    String okButton = BaseMessages.getString( PKG, "System.Button.OK" );
    MessageDialog dialog =
      new MessageDialog( parent, title, null, message, MessageDialog.ERROR, new String[] { okButton }, 0 );
    dialog.open();
  }

  public void copyToGlobal( RunConfigurationManager manager, RunConfiguration runConfiguration ) {
    moveCopy( manager, runConfiguration, spoonSupplier.get().getGlobalManagementBowl(), false );
  }

  public void copyToProject( RunConfigurationManager manager, RunConfiguration runConfiguration ) {
    moveCopy( manager, runConfiguration, spoonSupplier.get().getManagementBowl(), false );
  }

  public void moveToGlobal( RunConfigurationManager manager, RunConfiguration runConfiguration ) {
    moveCopy( manager, runConfiguration, spoonSupplier.get().getGlobalManagementBowl(), true );
  }

  public void moveToProject( RunConfigurationManager manager, RunConfiguration runConfiguration ) {
    moveCopy( manager, runConfiguration, Spoon.getInstance().getManagementBowl(), true );
  }

  private void moveCopy( RunConfigurationManager srcManager, RunConfiguration runConfiguration, Bowl targetBowl,
                         boolean deleteSource ) {
    CheckedMetaStoreSupplier ms = () -> targetBowl.getMetastore();
    RunConfigurationManager targetManager = RunConfigurationManager.getInstance( ms );
    if ( targetManager.getNames().stream().anyMatch( element -> element.equalsIgnoreCase( runConfiguration.getName() ) ) ) {
      if ( !shouldOverwrite( BaseMessages.getString( PKG, "RunConfigurationDialog.OverwriteRunConfigurationYN",
        runConfiguration.getName() ) ) ) {
        return;
      } else {
        targetManager.delete( runConfiguration.getName() );
      }
    }
    targetManager.save( runConfiguration );
    if ( deleteSource ) {
      srcManager.delete( runConfiguration.getName() );
    }
    refreshTree();
  }

  protected boolean shouldOverwrite( String message ) {
    MessageBox mb = new MessageBox( spoonSupplier.get().getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    mb.setMessage( message );
    mb.setText( BaseMessages.getString( PKG, "RunConfigurationDialog.dialog.PromptOverwrite.Title" ) );
    int response = mb.open();

    if ( response != SWT.YES ) {
      return false;
    }
    return true;
  }
}
