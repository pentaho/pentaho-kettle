/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entry;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizard;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.provider.DBConnectionFolderProvider;
import org.pentaho.di.ui.util.DialogUtils;
import org.pentaho.metastore.api.IMetaStore;

import java.util.function.Supplier;

/**
 * The JobEntryDialog class is responsible for constructing and opening the settings dialog for the job entry. Whenever
 * the user opens the job entry settings in Spoon, it will instantiate the dialog class passing in the JobEntryInterface
 * object and call the
 *
 * <pre>
 * open()
 * </pre>
 * <p>
 * method on the dialog. SWT is the native windowing environment of Spoon, and it is typically the framework used for
 * implementing job entry dialogs.
 */
public class JobEntryDialog extends Dialog {

  /**
   * The package name, used for internationalization.
   */
  private static final Class<?> PKG = StepInterface.class; // for i18n purposes, needed by Translator2!!

  /**
   * The loggingObject for the dialog
   */
  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "Job entry dialog", LoggingObjectType.JOBENTRYDIALOG, null );

  /**
   * A reference to the job entry interface
   */
  protected JobEntryInterface jobEntryInt;

  /**
   * The repository
   */
  protected Repository rep;

  /**
   * the MetaStore
   */
  protected IMetaStore metaStore;

  /**
   * The job metadata object.
   */
  protected JobMeta jobMeta;

  /**
   * A reference to the shell object
   */
  protected Shell shell;

  /**
   * A reference to the properties user interface
   */
  protected PropsUI props;

  /**
   * A reference to the parent shell
   */
  protected Shell parent;

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  /**
   * A reference to a database dialog
   */
  protected DatabaseDialog databaseDialog;

  /**
   * Instantiates a new job entry dialog.
   *
   * @param parent   the parent shell
   * @param jobEntry the job entry interface
   * @param rep      the repository
   * @param jobMeta  the job metadata object
   */
  public JobEntryDialog( Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta ) {
    super( parent, SWT.NONE );
    props = PropsUI.getInstance();

    this.jobEntryInt = jobEntry;
    this.rep = rep;
    this.jobMeta = jobMeta;
    this.shell = parent;
  }

  /**
   * Gets the database dialog.
   *
   * @return the database dialog
   */
  private DatabaseDialog getDatabaseDialog() {
    if ( databaseDialog != null ) {
      return databaseDialog;
    }
    databaseDialog = new DatabaseDialog( shell );
    return databaseDialog;
  }

  /**
   * Adds the connection line for the given parent and previous control, and returns a combo box UI component
   *
   * @param parent   the parent composite object
   * @param previous the previous control
   * @param middle   the middle
   * @param margin   the margin
   * @return the combo box UI component
   */
  public CCombo addConnectionLine( Composite parent, Control previous, int middle, int margin ) {
    return addConnectionLine( parent, previous, middle, margin, new Label( parent, SWT.RIGHT ), new Button(
      parent, SWT.PUSH ), new Button( parent, SWT.PUSH ), new Button( parent, SWT.PUSH ) );
  }

  /**
   * Adds the connection line for the given parent and previous control, and returns a combo box UI component
   *
   * @param parent        the parent composite object
   * @param previous      the previous control
   * @param middle        the middle
   * @param margin        the margin
   * @param wlConnection  the connection label
   * @param wbnConnection the "new connection" button
   * @param wbeConnection the "edit connection" button
   * @return the combo box UI component
   */
  public CCombo addConnectionLine( Composite parent, Control previous, int middle, int margin, final Label wlConnection,
                                   final Button wbwConnection, final Button wbnConnection,
                                   final Button wbeConnection ) {
    final CCombo wConnection;
    final FormData fdlConnection, fdbConnection, fdeConnection, fdConnection, fdbwConnection;

    wConnection = new CCombo( parent, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wConnection );

    addDatabases( wConnection );

    wlConnection.setText( BaseMessages.getString( PKG, "BaseStepDialog.Connection.Label" ) );
    props.setLook( wlConnection );
    fdlConnection = new FormData();
    fdlConnection.left = new FormAttachment( 0, 0 );
    fdlConnection.right = new FormAttachment( middle, -margin );
    if ( previous != null ) {
      fdlConnection.top = new FormAttachment( previous, margin );
    } else {
      fdlConnection.top = new FormAttachment( 0, 0 );
    }
    wlConnection.setLayoutData( fdlConnection );

    //
    // Wizard button
    //
    wbwConnection.setText( BaseMessages.getString( PKG, "BaseStepDialog.WizardConnectionButton.Label" ) );
    wbwConnection.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent e ) {
        CreateDatabaseWizard cdw = new CreateDatabaseWizard();
        DatabaseMeta newDBInfo = cdw.createAndRunDatabaseWizard( shell, props, jobMeta.getDatabases() );
        if ( newDBInfo != null ) {
          jobMeta.addDatabase( newDBInfo );
          reinitConnectionDropDown( wConnection, newDBInfo.getName() );
        }
      }
    } );
    fdbwConnection = new FormData();
    fdbwConnection.right = new FormAttachment( 100, 0 );
    if ( previous != null ) {
      fdbwConnection.top = new FormAttachment( previous, margin );
    } else {
      fdbwConnection.top = new FormAttachment( 0, 0 );
    }
    wbwConnection.setLayoutData( fdbwConnection );

    //
    // NEW button
    //
    wbnConnection.setText( BaseMessages.getString( PKG, "BaseStepDialog.NewConnectionButton.Label" ) );
    wbnConnection.addSelectionListener( new AddConnectionListener( wConnection ) );
    fdbConnection = new FormData();
    fdbConnection.right = new FormAttachment( wbwConnection, -margin );
    if ( previous != null ) {
      fdbConnection.top = new FormAttachment( previous, margin );
    } else {
      fdbConnection.top = new FormAttachment( 0, 0 );
    }
    wbnConnection.setLayoutData( fdbConnection );

    //
    // Edit button
    //
    wbeConnection.setText( BaseMessages.getString( PKG, "BaseStepDialog.EditConnectionButton.Label" ) );
    wbeConnection.addSelectionListener( new EditConnectionListener( wConnection ) );
    fdeConnection = new FormData();
    fdeConnection.right = new FormAttachment( wbnConnection, -margin );
    if ( previous != null ) {
      fdeConnection.top = new FormAttachment( previous, margin );
    } else {
      fdeConnection.top = new FormAttachment( 0, 0 );
    }
    wbeConnection.setLayoutData( fdeConnection );

    //
    // what's left of the line: combo box
    //
    fdConnection = new FormData();
    fdConnection.left = new FormAttachment( middle, 0 );
    if ( previous != null ) {
      fdConnection.top = new FormAttachment( previous, margin );
    } else {
      fdConnection.top = new FormAttachment( 0, 0 );
    }
    fdConnection.right = new FormAttachment( wbeConnection, -margin );
    wConnection.setLayoutData( fdConnection );

    return wConnection;
  }

  @VisibleForTesting
  String showDbDialogUnlessCancelledOrValid( DatabaseMeta changing, DatabaseMeta origin ) {
    changing.shareVariablesWith( jobMeta );
    DatabaseDialog cid = getDatabaseDialog();
    cid.setDatabaseMeta( changing );
    cid.setModalDialog( true );

    String name = null;
    boolean repeat = true;
    while ( repeat ) {
      name = cid.open();
      if ( name == null ) {
        // Cancel was pressed
        repeat = false;
      } else {
        name = name.trim();
        DatabaseMeta same = jobMeta.findDatabase( name );
        if ( same == null || same == origin ) {
          // OK was pressed and input is valid
          repeat = false;
        } else {
          showDbExistsDialog( changing );
        }
      }
    }
    return name;
  }

  @VisibleForTesting
  void showDbExistsDialog( DatabaseMeta changing ) {
    DatabaseDialog.showDatabaseExistsDialog( shell, changing );
  }

  private void reinitConnectionDropDown( CCombo dropDown, String selected ) {
    dropDown.removeAll();
    addDatabases( dropDown );
    selectDatabase( dropDown, selected );
  }

  /**
   * Adds the databases from the job metadata to the combo box.
   *
   * @param wConnection the w connection
   */
  public void addDatabases( CCombo wConnection ) {
    for ( int i = 0; i < jobMeta.nrDatabases(); i++ ) {
      DatabaseMeta ci = jobMeta.getDatabase( i );
      wConnection.add( ci.getName() );
    }
  }

  /**
   * Selects a database from the combo box
   *
   * @param wConnection the combo box list of connections
   * @param name        the name
   */
  public void selectDatabase( CCombo wConnection, String name ) {
    int idx = wConnection.indexOf( name );
    if ( idx >= 0 ) {
      wConnection.select( idx );
    }
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  protected String getPathOf( RepositoryElementMetaInterface object ) {
    return DialogUtils.getPathOf( object );
  }

  @VisibleForTesting
  class AddConnectionListener extends SelectionAdapter {

    private final CCombo wConnection;

    public AddConnectionListener( CCombo wConnection ) {
      this.wConnection = wConnection;
    }

    @Override
    public void widgetSelected( SelectionEvent e ) {
      DatabaseMeta databaseMeta = new DatabaseMeta();
      String connectionName = showDbDialogUnlessCancelledOrValid( databaseMeta, null );
      if ( connectionName != null ) {
        jobMeta.addDatabase( databaseMeta );
        reinitConnectionDropDown( wConnection, databaseMeta.getName() );
        spoonSupplier.get().refreshTree( DBConnectionFolderProvider.STRING_CONNECTIONS );
      }
    }
  }

  @VisibleForTesting
  class EditConnectionListener extends SelectionAdapter {

    private final CCombo wConnection;

    public EditConnectionListener( CCombo wConnection ) {
      this.wConnection = wConnection;
    }

    @Override public void widgetSelected( SelectionEvent e ) {
      DatabaseMeta databaseMeta = jobMeta.findDatabase( wConnection.getText() );
      if ( databaseMeta != null ) {
        // cloning to avoid spoiling data on cancel or incorrect input
        DatabaseMeta clone = (DatabaseMeta) databaseMeta.clone();
        String connectionName = showDbDialogUnlessCancelledOrValid( clone, databaseMeta );
        if ( connectionName != null ) {
          // need to replace the old connection with a new one
          jobMeta.removeDatabase( jobMeta.indexOfDatabase( databaseMeta ) );
          jobMeta.addDatabase( clone );
          reinitConnectionDropDown( wConnection, connectionName );
        }
      }
    }
  }
}
