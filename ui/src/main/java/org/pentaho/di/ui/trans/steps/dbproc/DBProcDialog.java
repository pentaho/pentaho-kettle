/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.dbproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dbproc.DBProcMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class DBProcDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = DBProcMeta.class; // for i18n purposes, needed by Translator2!!

  private CCombo wConnection;

  private Button wbProcName;
  private Label wlProcName;
  private TextVar wProcName;
  private FormData fdlProcName, fdbProcName, fdProcName;

  private Label wlAutoCommit;
  private Button wAutoCommit;
  private FormData fdlAutoCommit, fdAutoCommit;

  private Label wlResult;
  private Text wResult;
  private FormData fdlResult, fdResult;

  private Label wlResultType;
  private CCombo wResultType;
  private FormData fdlResultType, fdResultType;

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Button wGet;
  private Listener lsGet;

  private DBProcMeta input;

  private ColumnInfo[] colinf;

  private Map<String, Integer> inputFields;

  public DBProcDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (DBProcMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };

    SelectionAdapter lsSelMod = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
      }
    };

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "DBProcDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "DBProcDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Connection line
    wConnection = addConnectionLine( shell, wStepname, middle, margin );
    if ( input.getDatabase() == null && transMeta.nrDatabases() == 1 ) {
      wConnection.select( 0 );
    }
    wConnection.addModifyListener( lsMod );

    // ProcName line...
    // add button to get list of procedures on selected connection...
    wbProcName = new Button( shell, SWT.PUSH );
    wbProcName.setText( BaseMessages.getString( PKG, "DBProcDialog.Finding.Button" ) );
    fdbProcName = new FormData();
    fdbProcName.right = new FormAttachment( 100, 0 );
    fdbProcName.top = new FormAttachment( wConnection, margin * 2 );
    wbProcName.setLayoutData( fdbProcName );
    wbProcName.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        DatabaseMeta dbInfo = transMeta.findDatabase( wConnection.getText() );
        if ( dbInfo != null ) {
          Database db = new Database( loggingObject, dbInfo );
          try {
            db.connect();
            String[] procs = db.getProcedures();
            if ( procs != null && procs.length > 0 ) {
              EnterSelectionDialog esd =
                new EnterSelectionDialog( shell, procs,
                  BaseMessages.getString( PKG, "DBProcDialog.EnterSelection.DialogTitle" ),
                  BaseMessages.getString( PKG, "DBProcDialog.EnterSelection.DialogMessage" ) );
              String proc = esd.open();
              if ( proc != null ) {
                wProcName.setText( proc );
              }
            } else {
              MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
              mb.setMessage( BaseMessages.getString( PKG, "DBProcDialog.NoProceduresFound.DialogMessage" ) );
              mb.setText( BaseMessages.getString( PKG, "DBProcDialog.NoProceduresFound.DialogTitle" ) );
              mb.open();
            }
          } catch ( KettleDatabaseException dbe ) {
            new ErrorDialog( shell,
              BaseMessages.getString( PKG, "DBProcDialog.ErrorGettingProceduresList.DialogTitle" ),
              BaseMessages.getString( PKG, "DBProcDialog.ErrorGettingProceduresList.DialogMessage" ), dbe );
          } finally {
            db.disconnect();
          }
        }
      }
    } );

    wlProcName = new Label( shell, SWT.RIGHT );
    wlProcName.setText( BaseMessages.getString( PKG, "DBProcDialog.ProcedureName.Label" ) );
    props.setLook( wlProcName );
    fdlProcName = new FormData();
    fdlProcName.left = new FormAttachment( 0, 0 );
    fdlProcName.right = new FormAttachment( middle, -margin );
    fdlProcName.top = new FormAttachment( wConnection, margin * 2 );
    wlProcName.setLayoutData( fdlProcName );

    wProcName = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wProcName );
    wProcName.addModifyListener( lsMod );
    fdProcName = new FormData();
    fdProcName.left = new FormAttachment( middle, 0 );
    fdProcName.top = new FormAttachment( wConnection, margin * 2 );
    fdProcName.right = new FormAttachment( wbProcName, -margin );
    wProcName.setLayoutData( fdProcName );

    // AutoCommit line
    wlAutoCommit = new Label( shell, SWT.RIGHT );
    wlAutoCommit.setText( BaseMessages.getString( PKG, "DBProcDialog.AutoCommit.Label" ) );
    wlAutoCommit.setToolTipText( BaseMessages.getString( PKG, "DBProcDialog.AutoCommit.Tooltip" ) );
    props.setLook( wlAutoCommit );
    fdlAutoCommit = new FormData();
    fdlAutoCommit.left = new FormAttachment( 0, 0 );
    fdlAutoCommit.top = new FormAttachment( wProcName, margin );
    fdlAutoCommit.right = new FormAttachment( middle, -margin );
    wlAutoCommit.setLayoutData( fdlAutoCommit );
    wAutoCommit = new Button( shell, SWT.CHECK );
    wAutoCommit.setToolTipText( BaseMessages.getString( PKG, "DBProcDialog.AutoCommit.Tooltip" ) );
    props.setLook( wAutoCommit );
    fdAutoCommit = new FormData();
    fdAutoCommit.left = new FormAttachment( middle, 0 );
    fdAutoCommit.top = new FormAttachment( wProcName, margin );
    fdAutoCommit.right = new FormAttachment( 100, 0 );
    wAutoCommit.setLayoutData( fdAutoCommit );
    wAutoCommit.addSelectionListener( lsSelMod );

    // Result line...
    wlResult = new Label( shell, SWT.RIGHT );
    wlResult.setText( BaseMessages.getString( PKG, "DBProcDialog.Result.Label" ) );
    props.setLook( wlResult );
    fdlResult = new FormData();
    fdlResult.left = new FormAttachment( 0, 0 );
    fdlResult.right = new FormAttachment( middle, -margin );
    fdlResult.top = new FormAttachment( wAutoCommit, margin * 2 );
    wlResult.setLayoutData( fdlResult );
    wResult = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResult );
    wResult.addModifyListener( lsMod );
    fdResult = new FormData();
    fdResult.left = new FormAttachment( middle, 0 );
    fdResult.top = new FormAttachment( wAutoCommit, margin * 2 );
    fdResult.right = new FormAttachment( 100, 0 );
    wResult.setLayoutData( fdResult );

    // ResultType line
    wlResultType = new Label( shell, SWT.RIGHT );
    wlResultType.setText( BaseMessages.getString( PKG, "DBProcDialog.ResultType.Label" ) );
    props.setLook( wlResultType );
    fdlResultType = new FormData();
    fdlResultType.left = new FormAttachment( 0, 0 );
    fdlResultType.right = new FormAttachment( middle, -margin );
    fdlResultType.top = new FormAttachment( wResult, margin );
    wlResultType.setLayoutData( fdlResultType );
    wResultType = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wResultType );
    String[] types = ValueMetaFactory.getValueMetaNames();
    for ( int x = 0; x < types.length; x++ ) {
      wResultType.add( types[x] );
    }
    wResultType.select( 0 );
    wResultType.addModifyListener( lsMod );
    fdResultType = new FormData();
    fdResultType.left = new FormAttachment( middle, 0 );
    fdResultType.top = new FormAttachment( wResult, margin );
    fdResultType.right = new FormAttachment( 100, 0 );
    wResultType.setLayoutData( fdResultType );

    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "DBProcDialog.Parameters.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( wResultType, margin );
    wlFields.setLayoutData( fdlFields );

    final int FieldsCols = 3;
    final int FieldsRows = input.getArgument().length;

    colinf = new ColumnInfo[FieldsCols];
    colinf[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DBProcDialog.ColumnInfo.Name" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        new String[] { "" }, false );
    colinf[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DBProcDialog.ColumnInfo.Direction" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        new String[] { "IN", "OUT", "INOUT" } );
    colinf[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DBProcDialog.ColumnInfo.Type" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        ValueMetaFactory.getValueMetaNames() );

    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    //
    // Search the fields in the background

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );

            // Remember these fields...
            for ( int i = 0; i < row.size(); i++ ) {
              inputFields.put( row.getValueMeta( i ).getName(), Integer.valueOf( i ) );
            }
            setComboBoxes();
          } catch ( KettleException e ) {
            logError( BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
          }
        }
      }
    };
    new Thread( runnable ).start();

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "DBProcDialog.GetFields.Button" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel, wGet }, margin, wFields );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    lsResize = new Listener() {
      public void handleEvent( Event event ) {
        Point size = shell.getSize();
        wFields.setSize( size.x - 10, size.y - 50 );
        wFields.table.setSize( size.x - 10, size.y - 50 );
        wFields.redraw();
      }
    };
    if ( !Const.isRunningOnWebspoonMode() ) {
      shell.addListener( SWT.Resize, lsResize );
    }

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<String, Integer>();

    // Add the currentMeta fields...
    fields.putAll( inputFields );

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<String>( keySet );

    String[] fieldNames = entries.toArray( new String[entries.size()] );

    Const.sortStrings( fieldNames );
    colinf[0].setComboValues( fieldNames );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    int i;
    logDebug( BaseMessages.getString( PKG, "DBProcDialog.Log.GettingKeyInfo" ) );

    if ( input.getArgument() != null ) {
      for ( i = 0; i < input.getArgument().length; i++ ) {
        TableItem item = wFields.table.getItem( i );
        if ( input.getArgument()[i] != null ) {
          item.setText( 1, input.getArgument()[i] );
        }
        if ( input.getArgumentDirection()[i] != null ) {
          item.setText( 2, input.getArgumentDirection()[i] );
        }
        item.setText( 3, ValueMetaFactory.getValueMetaName( input.getArgumentType()[i] ) );
      }
    }

    if ( input.getDatabase() != null ) {
      wConnection.setText( input.getDatabase().getName() );
    } else if ( transMeta.nrDatabases() == 1 ) {
      wConnection.setText( transMeta.getDatabase( 0 ).getName() );
    }
    if ( input.getProcedure() != null ) {
      wProcName.setText( input.getProcedure() );
    }
    if ( input.getResultName() != null ) {
      wResult.setText( input.getResultName() );
    }
    wResultType.setText( ValueMetaFactory.getValueMetaName( input.getResultType() ) );

    wAutoCommit.setSelection( input.isAutoCommit() );

    wFields.setRowNums();
    wFields.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    int i;

    int nrargs = wFields.nrNonEmpty();

    input.allocate( nrargs );

    logDebug( BaseMessages.getString( PKG, "DBProcDialog.Log.FoundArguments", String.valueOf( nrargs ) ) );
    //CHECKSTYLE:Indentation:OFF
    for ( i = 0; i < nrargs; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      input.getArgument()[i] = item.getText( 1 );
      input.getArgumentDirection()[i] = item.getText( 2 );
      input.getArgumentType()[i] = ValueMetaFactory.getIdForValueMeta( item.getText( 3 ) );
    }

    input.setDatabase( transMeta.findDatabase( wConnection.getText() ) );
    input.setProcedure( wProcName.getText() );
    input.setResultName( wResult.getText() );
    input.setResultType( ValueMetaFactory.getIdForValueMeta( wResultType.getText() ) );
    input.setAutoCommit( wAutoCommit.getSelection() );

    stepname = wStepname.getText(); // return value

    if ( input.getDatabase() == null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "DBProcDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "DBProcDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
    }

    dispose();
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        TableItemInsertListener listener = new TableItemInsertListener() {
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            tableItem.setText( 2, "IN" );
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1 }, new int[] { 3 }, -1, -1, listener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "DBProcDialog.FailedToGetFields.DialogTitle" ), BaseMessages
          .getString( PKG, "DBProcDialog.FailedToGetFields.DialogMessage" ), ke );
    }

  }
}
