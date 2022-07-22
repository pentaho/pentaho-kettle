/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.pgbulkloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
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
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.pgbulkloader.PGBulkLoaderMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * Dialog class for the Greenplum bulk loader step. Created on 28mar2008, copied from Sven Boden's Oracle version
 *
 * @author Luke Lonergan
 */
public class PGBulkLoaderDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = PGBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  private CCombo wConnection;

  private Label wlSchema;
  private TextVar wSchema;
  private FormData fdlSchema, fdSchema;

  private Label wlTable;
  private Button wbTable;
  private TextVar wTable;
  private FormData fdlTable, fdbTable, fdTable;

  private Label wlLoadAction;
  private CCombo wLoadAction;
  private FormData fdlLoadAction, fdLoadAction;

  private Label wlReturn;
  private TableView wReturn;
  private FormData fdlReturn, fdReturn;

  private Label wlEnclosure;
  private TextVar wEnclosure;
  private FormData fdlEnclosure, fdEnclosure;

  private Label wlDelimiter;
  private TextVar wDelimiter;
  private FormData fdlDelimiter, fdDelimiter;

  private Label wlDbNameOverride;
  private TextVar wDbNameOverride;
  private FormData fdlDbNameOverride, fdDbNameOverride;

  private Button wGetLU;
  private FormData fdGetLU;
  private Listener lsGetLU;

  private Button wDoMapping;
  private FormData fdDoMapping;

  private Label wlStopOnError;
  private Button wStopOnError;
  private FormData fdlStopOnError, fdStopOnError;

  private PGBulkLoaderMeta input;

  private static final String[] ALL_FILETYPES = new String[] { BaseMessages.getString(
    PKG, "PGBulkLoaderDialog.Filetype.All" ) };

  private ColumnInfo[] ciReturn;

  private Map<String, Integer> inputFields;

  /**
   * List of ColumnInfo that should have the field names of the selected database table
   */
  private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();

  public PGBulkLoaderDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (PGBulkLoaderMeta) in;
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
    FocusListener lsFocusLost = new FocusAdapter() {
      public void focusLost( FocusEvent arg0 ) {
        setTableFieldCombo();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Stepname.Label" ) );
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
    if ( input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1 ) {
      wConnection.select( 0 );
    }
    wConnection.addModifyListener( lsMod );

    // Schema line...
    wlSchema = new Label( shell, SWT.RIGHT );
    wlSchema.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.TargetSchema.Label" ) );
    props.setLook( wlSchema );
    fdlSchema = new FormData();
    fdlSchema.left = new FormAttachment( 0, 0 );
    fdlSchema.right = new FormAttachment( middle, -margin );
    fdlSchema.top = new FormAttachment( wConnection, margin * 2 );
    wlSchema.setLayoutData( fdlSchema );

    wSchema = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSchema );
    wSchema.addModifyListener( lsMod );
    wSchema.addFocusListener( lsFocusLost );
    fdSchema = new FormData();
    fdSchema.left = new FormAttachment( middle, 0 );
    fdSchema.top = new FormAttachment( wConnection, margin * 2 );
    fdSchema.right = new FormAttachment( 100, 0 );
    wSchema.setLayoutData( fdSchema );

    // Table line...
    wlTable = new Label( shell, SWT.RIGHT );
    wlTable.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.TargetTable.Label" ) );
    props.setLook( wlTable );
    fdlTable = new FormData();
    fdlTable.left = new FormAttachment( 0, 0 );
    fdlTable.right = new FormAttachment( middle, -margin );
    fdlTable.top = new FormAttachment( wSchema, margin );
    wlTable.setLayoutData( fdlTable );

    wbTable = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTable );
    wbTable.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Browse.Button" ) );
    fdbTable = new FormData();
    fdbTable.right = new FormAttachment( 100, 0 );
    fdbTable.top = new FormAttachment( wSchema, margin );
    wbTable.setLayoutData( fdbTable );
    wTable = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTable );
    wTable.addModifyListener( lsMod );
    wTable.addFocusListener( lsFocusLost );
    fdTable = new FormData();
    fdTable.left = new FormAttachment( middle, 0 );
    fdTable.top = new FormAttachment( wSchema, margin );
    fdTable.right = new FormAttachment( wbTable, -margin );
    wTable.setLayoutData( fdTable );

    // Load Action line
    wlLoadAction = new Label( shell, SWT.RIGHT );
    wlLoadAction.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.LoadAction.Label" ) );
    props.setLook( wlLoadAction );
    fdlLoadAction = new FormData();
    fdlLoadAction.left = new FormAttachment( 0, 0 );
    fdlLoadAction.right = new FormAttachment( middle, -margin );
    fdlLoadAction.top = new FormAttachment( wTable, margin );
    wlLoadAction.setLayoutData( fdlLoadAction );
    wLoadAction = new CCombo( shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wLoadAction.add( BaseMessages.getString( PKG, "PGBulkLoaderDialog.InsertLoadAction.Label" ) );
    wLoadAction.add( BaseMessages.getString( PKG, "PGBulkLoaderDialog.TruncateLoadAction.Label" ) );

    wLoadAction.select( 0 ); // +1: starts at -1
    wLoadAction.addModifyListener( lsMod );

    props.setLook( wLoadAction );
    fdLoadAction = new FormData();
    fdLoadAction.left = new FormAttachment( middle, 0 );
    fdLoadAction.top = new FormAttachment( wTable, margin );
    fdLoadAction.right = new FormAttachment( 100, 0 );
    wLoadAction.setLayoutData( fdLoadAction );

    fdLoadAction = new FormData();
    fdLoadAction.left = new FormAttachment( middle, 0 );
    fdLoadAction.top = new FormAttachment( wTable, margin );
    fdLoadAction.right = new FormAttachment( 100, 0 );
    wLoadAction.setLayoutData( fdLoadAction );

    // Db Name Override line
    wlDbNameOverride = new Label( shell, SWT.RIGHT );
    wlDbNameOverride.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.DbNameOverride.Label" ) );
    props.setLook( wlDbNameOverride );
    fdlDbNameOverride = new FormData();
    fdlDbNameOverride.left = new FormAttachment( 0, 0 );
    fdlDbNameOverride.top = new FormAttachment( wLoadAction, margin );
    fdlDbNameOverride.right = new FormAttachment( middle, -margin );
    wlDbNameOverride.setLayoutData( fdlDbNameOverride );
    wDbNameOverride = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDbNameOverride );
    wDbNameOverride.addModifyListener( lsMod );
    fdDbNameOverride = new FormData();
    fdDbNameOverride.left = new FormAttachment( middle, 0 );
    fdDbNameOverride.top = new FormAttachment( wLoadAction, margin );
    fdDbNameOverride.right = new FormAttachment( 100, 0 );
    wDbNameOverride.setLayoutData( fdDbNameOverride );

    // Enclosure line
    wlEnclosure = new Label( shell, SWT.RIGHT );
    wlEnclosure.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Enclosure.Label" ) );
    props.setLook( wlEnclosure );
    fdlEnclosure = new FormData();
    fdlEnclosure.left = new FormAttachment( 0, 0 );
    fdlEnclosure.top = new FormAttachment( wDbNameOverride, margin );
    fdlEnclosure.right = new FormAttachment( middle, -margin );
    wlEnclosure.setLayoutData( fdlEnclosure );
    wEnclosure = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEnclosure );
    wEnclosure.addModifyListener( lsMod );
    fdEnclosure = new FormData();
    fdEnclosure.left = new FormAttachment( middle, 0 );
    fdEnclosure.top = new FormAttachment( wDbNameOverride, margin );
    fdEnclosure.right = new FormAttachment( 100, 0 );
    wEnclosure.setLayoutData( fdEnclosure );

    // Delimiter
    wlDelimiter = new Label( shell, SWT.RIGHT );
    wlDelimiter.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Delimiter.Label" ) );
    props.setLook( wlDelimiter );
    fdlDelimiter = new FormData();
    fdlDelimiter.left = new FormAttachment( 0, 0 );
    fdlDelimiter.top = new FormAttachment( wEnclosure, margin );
    fdlDelimiter.right = new FormAttachment( middle, -margin );
    wlDelimiter.setLayoutData( fdlDelimiter );
    wDelimiter = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDelimiter );
    wDelimiter.addModifyListener( lsMod );
    fdDelimiter = new FormData();
    fdDelimiter.left = new FormAttachment( middle, 0 );
    fdDelimiter.top = new FormAttachment( wEnclosure, margin );
    fdDelimiter.right = new FormAttachment( 100, 0 );
    wDelimiter.setLayoutData( fdDelimiter );

    // Stop on Error line
    wlStopOnError = new Label( shell, SWT.RIGHT );
    wlStopOnError.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.StopOnError.Label" ) );
    props.setLook( wlStopOnError );
    fdlStopOnError = new FormData();
    fdlStopOnError.left = new FormAttachment( 0, 0 );
    fdlStopOnError.top = new FormAttachment( wDelimiter, margin );
    fdlStopOnError.right = new FormAttachment( middle, -margin );
    wlStopOnError.setLayoutData( fdlStopOnError );
    wStopOnError = new Button( shell, SWT.CHECK );
    props.setLook( wStopOnError );
    fdStopOnError = new FormData();
    fdStopOnError.left = new FormAttachment( middle, 0 );
    fdStopOnError.top = new FormAttachment( wDelimiter, margin );
    fdStopOnError.right = new FormAttachment( 100, 0 );
    wStopOnError.setLayoutData( fdStopOnError );

    wStopOnError.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wSQL = new Button( shell, SWT.PUSH );
    wSQL.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.SQL.Button" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel, wSQL }, margin, null );

    // The field Table
    wlReturn = new Label( shell, SWT.NONE );
    wlReturn.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Fields.Label" ) );
    props.setLook( wlReturn );
    fdlReturn = new FormData();
    fdlReturn.left = new FormAttachment( 0, 0 );
    fdlReturn.top = new FormAttachment( wStopOnError, margin );
    wlReturn.setLayoutData( fdlReturn );

    int UpInsCols = 3;
    int UpInsRows = ( input.getFieldTable() != null ? input.getFieldTable().length : 1 );

    ciReturn = new ColumnInfo[UpInsCols];
    ciReturn[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.ColumnInfo.TableField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciReturn[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.ColumnInfo.StreamField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciReturn[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.ColumnInfo.DateMask" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
          "", BaseMessages.getString( PKG, "PGBulkLoaderDialog.PassThrough.Label" ),
          BaseMessages.getString( PKG, "PGBulkLoaderDialog.DateMask.Label" ),
          BaseMessages.getString( PKG, "PGBulkLoaderDialog.DateTimeMask.Label" ) }, true );
    tableFieldColumns.add( ciReturn[0] );
    wReturn =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciReturn,
        UpInsRows, lsMod, props );

    wGetLU = new Button( shell, SWT.PUSH );
    wGetLU.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.GetFields.Label" ) );
    fdGetLU = new FormData();
    fdGetLU.top = new FormAttachment( wlReturn, margin );
    fdGetLU.right = new FormAttachment( 100, 0 );
    wGetLU.setLayoutData( fdGetLU );

    wDoMapping = new Button( shell, SWT.PUSH );
    wDoMapping.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.EditMapping.Label" ) );
    fdDoMapping = new FormData();
    fdDoMapping.top = new FormAttachment( wGetLU, margin );
    fdDoMapping.right = new FormAttachment( 100, 0 );
    wDoMapping.setLayoutData( fdDoMapping );

    wDoMapping.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event arg0 ) {
        generateMappings();
      }
    } );

    fdReturn = new FormData();
    fdReturn.left = new FormAttachment( 0, 0 );
    fdReturn.top = new FormAttachment( wlReturn, margin );
    fdReturn.right = new FormAttachment( wDoMapping, -margin ); // fix margin error
    fdReturn.bottom = new FormAttachment( wOK, -2 * margin );
    wReturn.setLayoutData( fdReturn );

    //
    // Search the fields in the background
    //

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );

            // Remember these fields...
            for ( int i = 0; i < row.size(); i++ ) {
              inputFields.put( row.getValueMeta( i ).getName(), i );
            }

            setComboBoxes();
          } catch ( KettleException e ) {
            logError( BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
          }
        }
      }
    };
    new Thread( runnable ).start();

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsGetLU = new Listener() {
      public void handleEvent( Event e ) {
        getUpdate();
      }
    };
    lsSQL = new Listener() {
      public void handleEvent( Event e ) {
        create();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGetLU.addListener( SWT.Selection, lsGetLU );
    wSQL.addListener( SWT.Selection, lsSQL );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wSchema.addSelectionListener( lsDef );
    wTable.addSelectionListener( lsDef );
    wDbNameOverride.addSelectionListener( lsDef );
    wEnclosure.addSelectionListener( lsDef );
    wDelimiter.addSelectionListener( lsDef );
    wStopOnError.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wbTable.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        getTableName();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    setTableFieldCombo();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    logDebug( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Log.GettingKeyInfo" ) );

    if ( input.getFieldTable() != null ) {
      for ( int i = 0; i < input.getFieldTable().length; i++ ) {
        TableItem item = wReturn.table.getItem( i );
        if ( input.getFieldTable()[i] != null ) {
          item.setText( 1, input.getFieldTable()[i] );
        }
        if ( input.getFieldStream()[i] != null ) {
          item.setText( 2, input.getFieldStream()[i] );
        }
        String dateMask = input.getDateMask()[i];
        if ( dateMask != null ) {
          if ( PGBulkLoaderMeta.DATE_MASK_PASS_THROUGH.equals( dateMask ) ) {
            item.setText( 3, BaseMessages.getString( PKG, "PGBulkLoaderDialog.PassThrough.Label" ) );
          } else if ( PGBulkLoaderMeta.DATE_MASK_DATE.equals( dateMask ) ) {
            item.setText( 3, BaseMessages.getString( PKG, "PGBulkLoaderDialog.DateMask.Label" ) );
          } else if ( PGBulkLoaderMeta.DATE_MASK_DATETIME.equals( dateMask ) ) {
            item.setText( 3, BaseMessages.getString( PKG, "PGBulkLoaderDialog.DateTimeMask.Label" ) );
          } else {
            item.setText( 3, "" );
          }
        } else {
          item.setText( 3, "" );
        }
      }
    }

    if ( input.getDatabaseMeta() != null ) {
      wConnection.setText( input.getDatabaseMeta().getName() );
    } else {
      if ( transMeta.nrDatabases() == 1 ) {
        wConnection.setText( transMeta.getDatabase( 0 ).getName() );
      }
    }
    if ( input.getSchemaName() != null ) {
      wSchema.setText( input.getSchemaName() );
    }
    if ( input.getTableName() != null ) {
      wTable.setText( input.getTableName() );
    }
    if ( input.getDelimiter() != null ) {
      wDelimiter.setText( input.getDelimiter() );
    }
    if ( input.getEnclosure() != null ) {
      wEnclosure.setText( input.getEnclosure() );
    }
    wStopOnError.setSelection( input.isStopOnError() );
    if ( input.getDbNameOverride() != null ) {
      wDbNameOverride.setText( input.getDbNameOverride() );
    }

    String action = input.getLoadAction();
    if ( PGBulkLoaderMeta.ACTION_INSERT.equals( action ) ) {
      wLoadAction.select( 0 );
    } else if ( PGBulkLoaderMeta.ACTION_TRUNCATE.equals( action ) ) {
      wLoadAction.select( 1 );
    } else {
      logDebug( "Internal error: load_action set to default 'insert'" );
      wLoadAction.select( 0 );
    }

    wReturn.setRowNums();
    wReturn.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
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
    // return fields
    ciReturn[1].setComboValues( fieldNames );

  }

  /**
   * Reads in the fields from the previous steps and from the ONE next step and opens an EnterMappingDialog with this
   * information. After the user did the mapping, those information is put into the Select/Rename table.
   */
  private void generateMappings() {

    // Determine the source and target fields...
    //
    RowMetaInterface sourceFields;
    RowMetaInterface targetFields;

    try {
      sourceFields = transMeta.getPrevStepFields( stepMeta );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Title" ),
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Message" ), e );
      return;
    }
    // refresh data
    input.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    input.setTableName( transMeta.environmentSubstitute( wTable.getText() ) );
    StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
    try {
      targetFields = stepMetaInterface.getRequiredFields( transMeta );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Title" ),
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Message" ), e );
      return;
    }

    String[] inputNames = new String[sourceFields.size()];
    for ( int i = 0; i < sourceFields.size(); i++ ) {
      ValueMetaInterface value = sourceFields.getValueMeta( i );
      inputNames[i] = value.getName() + EnterMappingDialog.STRING_ORIGIN_SEPARATOR + value.getOrigin() + ")";
    }

    // Create the existing mapping list...
    //
    List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
    StringBuilder missingSourceFields = new StringBuilder();
    StringBuilder missingTargetFields = new StringBuilder();

    int nrFields = wReturn.nrNonEmpty();
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wReturn.getNonEmpty( i );
      String source = item.getText( 2 );
      String target = item.getText( 1 );

      int sourceIndex = sourceFields.indexOfValue( source );
      if ( sourceIndex < 0 ) {
        missingSourceFields.append( Const.CR ).append( "   " ).append( source ).append( " --> " ).append( target );
      }
      int targetIndex = targetFields.indexOfValue( target );
      if ( targetIndex < 0 ) {
        missingTargetFields.append( Const.CR ).append( "   " ).append( source ).append( " --> " ).append( target );
      }
      if ( sourceIndex < 0 || targetIndex < 0 ) {
        continue;
      }

      SourceToTargetMapping mapping = new SourceToTargetMapping( sourceIndex, targetIndex );
      mappings.add( mapping );
    }

    // show a confirm dialog if some missing field was found
    //
    if ( missingSourceFields.length() > 0 || missingTargetFields.length() > 0 ) {

      String message = "";
      if ( missingSourceFields.length() > 0 ) {
        message +=
          BaseMessages.getString(
            PKG, "PGBulkLoaderDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString() )
            + Const.CR;
      }
      if ( missingTargetFields.length() > 0 ) {
        message +=
          BaseMessages.getString(
            PKG, "PGBulkLoaderDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString() )
            + Const.CR;
      }
      message += Const.CR;
      message +=
        BaseMessages.getString( PKG, "PGBulkLoaderDialog.DoMapping.SomeFieldsNotFoundContinue" ) + Const.CR;
      MessageDialog.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      boolean goOn =
        MessageDialog.openConfirm( shell, BaseMessages.getString(
          PKG, "PGBulkLoaderDialog.DoMapping.SomeFieldsNotFoundTitle" ), message );
      if ( !goOn ) {
        return;
      }
    }
    EnterMappingDialog d =
      new EnterMappingDialog( PGBulkLoaderDialog.this.shell, sourceFields.getFieldNames(), targetFields
        .getFieldNames(), mappings );
    mappings = d.open();

    // mappings == null if the user pressed cancel
    //
    if ( mappings != null ) {
      // Clear and re-populate!
      //
      wReturn.table.removeAll();
      wReturn.table.setItemCount( mappings.size() );
      for ( int i = 0; i < mappings.size(); i++ ) {
        SourceToTargetMapping mapping = mappings.get( i );
        TableItem item = wReturn.table.getItem( i );
        item.setText( 2, sourceFields.getValueMeta( mapping.getSourcePosition() ).getName() );
        item.setText( 1, targetFields.getValueMeta( mapping.getTargetPosition() ).getName() );
      }
      wReturn.setRowNums();
      wReturn.optWidth( true );
    }
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void getInfo( PGBulkLoaderMeta inf ) {
    int nrfields = wReturn.nrNonEmpty();

    inf.allocate( nrfields );

    inf.setDbNameOverride( wDbNameOverride.getText() );

    logDebug( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Log.FoundFields", "" + nrfields ) );
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem item = wReturn.getNonEmpty( i );
      inf.getFieldTable()[i] = item.getText( 1 );
      inf.getFieldStream()[i] = item.getText( 2 );
      if ( BaseMessages.getString( PKG, "PGBulkLoaderDialog.PassThrough.Label" ).equals( item.getText( 3 ) ) ) {
        inf.getDateMask()[i] = PGBulkLoaderMeta.DATE_MASK_PASS_THROUGH;
      } else if ( BaseMessages.getString( PKG, "PGBulkLoaderDialog.DateMask.Label" ).equals( item.getText( 3 ) ) ) {
        inf.getDateMask()[i] = PGBulkLoaderMeta.DATE_MASK_DATE;
      } else if ( BaseMessages
        .getString( PKG, "PGBulkLoaderDialog.DateTimeMask.Label" ).equals( item.getText( 3 ) ) ) {
        inf.getDateMask()[i] = PGBulkLoaderMeta.DATE_MASK_DATETIME;
      } else {
        inf.getDateMask()[i] = "";
      }
    }

    inf.setSchemaName( wSchema.getText() );
    inf.setTableName( wTable.getText() );
    inf.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    inf.setDelimiter( wDelimiter.getText() );
    inf.setEnclosure( wEnclosure.getText() );
    inf.setStopOnError( wStopOnError.getSelection() );

    /*
     * /* Set the loadaction
     */
    String action = wLoadAction.getText();
    if ( BaseMessages.getString( PKG, "PGBulkLoaderDialog.InsertLoadAction.Label" ).equals( action ) ) {
      inf.setLoadAction( PGBulkLoaderMeta.ACTION_INSERT );
    } else if ( BaseMessages.getString( PKG, "PGBulkLoaderDialog.TruncateLoadAction.Label" ).equals( action ) ) {
      inf.setLoadAction( PGBulkLoaderMeta.ACTION_TRUNCATE );
    } else {
      logDebug( "Internal error: load_action set to default 'insert', value found '" + action + "'." );
      inf.setLoadAction( PGBulkLoaderMeta.ACTION_INSERT );
    }

    stepname = wStepname.getText(); // return value
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    // Get the information for the dialog into the input structure.
    getInfo( input );

    if ( input.getDatabaseMeta() == null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "PGBulkLoaderDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
    }

    dispose();
  }

  private void getTableName() {
    DatabaseMeta inf = null;
    // New class: SelectTableDialog
    int connr = wConnection.getSelectionIndex();
    if ( connr >= 0 ) {
      inf = transMeta.getDatabase( connr );
    }

    if ( inf != null ) {
      logDebug( BaseMessages.getString( PKG, "PGBulkLoaderDialog.Log.LookingAtConnection" ) + inf.toString() );

      DatabaseExplorerDialog std = new DatabaseExplorerDialog( shell, SWT.NONE, inf, transMeta.getDatabases() );
      std.setSelectedSchemaAndTable( wSchema.getText(), wTable.getText() );
      if ( std.open() ) {
        wSchema.setText( Const.NVL( std.getSchemaName(), "" ) );
        wTable.setText( Const.NVL( std.getTableName(), "" ) );
      }
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "PGBulkLoaderDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
    }
  }

  private void getUpdate() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        TableItemInsertListener listener = new TableItemInsertListener() {
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            if ( v.getType() == ValueMetaInterface.TYPE_DATE ) {
              // The default is date mask.
              tableItem.setText( 3, BaseMessages.getString( PKG, "PGBulkLoaderDialog.DateMask.Label" ) );
            } else {
              tableItem.setText( 3, "" );
            }
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious( r, wReturn, 1, new int[] { 1, 2 }, new int[] {}, -1, -1, listener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "PGBulkLoaderDialog.FailedToGetFields.DialogTitle" ), BaseMessages
          .getString( PKG, "PGBulkLoaderDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  // Generate code for create table...
  // Conversions done by Database
  private void create() {
    try {
      PGBulkLoaderMeta info = new PGBulkLoaderMeta();
      getInfo( info );

      String name = stepname; // new name might not yet be linked to other steps!
      StepMeta stepMeta =
        new StepMeta( BaseMessages.getString( PKG, "PGBulkLoaderDialog.StepMeta.Title" ), name, info );
      RowMetaInterface prev = transMeta.getPrevStepFields( stepname );

      SQLStatement sql = info.getSQLStatements( transMeta, stepMeta, prev, repository, metaStore );
      if ( !sql.hasError() ) {
        if ( sql.hasSQL() ) {
          SQLEditor sqledit =
            new SQLEditor( transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql
              .getSQL() );
          sqledit.open();
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
          mb.setMessage( BaseMessages.getString( PKG, "PGBulkLoaderDialog.NoSQLNeeds.DialogMessage" ) );
          mb.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.NoSQLNeeds.DialogTitle" ) );
          mb.open();
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( sql.getError() );
        mb.setText( BaseMessages.getString( PKG, "PGBulkLoaderDialog.SQLError.DialogTitle" ) );
        mb.open();
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "PGBulkLoaderDialog.CouldNotBuildSQL.DialogTitle" ), BaseMessages
          .getString( PKG, "PGBulkLoaderDialog.CouldNotBuildSQL.DialogMessage" ), ke );
    }

  }

  private void setTableFieldCombo() {
    Runnable fieldLoader = new Runnable() {
      public void run() {
        if ( !wTable.isDisposed() && !wConnection.isDisposed() && !wSchema.isDisposed() ) {
          final String tableName = wTable.getText(), connectionName = wConnection.getText(), schemaName =
            wSchema.getText();

          // clear
          for ( ColumnInfo colInfo : tableFieldColumns ) {
            colInfo.setComboValues( new String[] {} );
          }
          if ( !Utils.isEmpty( tableName ) ) {
            DatabaseMeta ci = transMeta.findDatabase( connectionName );
            if ( ci != null ) {
              Database db = new Database( loggingObject, ci );
              try {
                db.connect();

                String schemaTable =
                  ci.getQuotedSchemaTableCombination( transMeta.environmentSubstitute( schemaName ), transMeta
                    .environmentSubstitute( tableName ) );
                RowMetaInterface r = db.getTableFields( schemaTable );
                if ( null != r ) {
                  String[] fieldNames = r.getFieldNames();
                  if ( null != fieldNames ) {
                    for ( ColumnInfo colInfo : tableFieldColumns ) {
                      colInfo.setComboValues( fieldNames );
                    }
                  }
                }
              } catch ( Exception e ) {
                for ( ColumnInfo colInfo : tableFieldColumns ) {
                  colInfo.setComboValues( new String[] {} );
                }
                // ignore any errors here. drop downs will not be
                // filled, but no problem for the user
              } finally {
                try {
                  if ( db != null ) {
                    db.disconnect();
                  }
                } catch ( Exception ignored ) {
                  // ignore any errors here.
                  db = null;
                }
              }
            }
          }
        }
      }
    };
    shell.getDisplay().asyncExec( fieldLoader );
  }
}
