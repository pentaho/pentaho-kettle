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

package org.pentaho.di.ui.trans.steps.databaselookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.databaselookup.DatabaseLookupMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class DatabaseLookupDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = DatabaseLookupMeta.class; // for i18n purposes, needed by Translator2!!

  private CCombo wConnection;

  private Label wlCache;
  private Button wCache;
  private FormData fdlCache, fdCache;

  private Label wlCacheLoadAll;
  private Button wCacheLoadAll;
  private FormData fdlCacheLoadAll, fdCacheLoadAll;

  private Label wlCachesize;
  private Text wCachesize;
  private FormData fdlCachesize, fdCachesize;

  private Label wlKey;
  private TableView wKey;
  private FormData fdlKey, fdKey;

  private Label wlSchema;
  private TextVar wSchema;
  private FormData fdlSchema, fdSchema;
  private Button wbSchema;
  private FormData fdbSchema;

  private Label wlTable;
  private Button wbTable;
  private TextVar wTable;
  private FormData fdlTable, fdbTable, fdTable;

  private Label wlReturn;
  private TableView wReturn;
  private FormData fdlReturn, fdReturn;

  private Label wlOrderBy;
  private Text wOrderBy;
  private FormData fdlOrderBy, fdOrderBy;

  private Label wlFailMultiple;
  private Button wFailMultiple;
  private FormData fdlFailMultiple, fdFailMultiple;

  private Label wlEatRows;
  private Button wEatRows;
  private FormData fdlEatRows, fdEatRows;

  private Button wGet, wGetLU;
  private Listener lsGet, lsGetLU;

  private DatabaseLookupMeta input;

  /**
   * List of ColumnInfo that should have the field names of the selected database table
   */
  private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();

  /**
   * List of ColumnInfo that should have the previous fields combo box
   */
  private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();

  /**
   * all fields from the previous steps
   */
  private RowMetaInterface prevFields = null;

  public DatabaseLookupDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (DatabaseLookupMeta) in;
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

    ModifyListener lsConnectionMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
        setTableFieldCombo();
      }
    };

    ModifyListener lsTableMod = new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        input.setChanged();
        setTableFieldCombo();
      }
    };
    SelectionListener lsSelection = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setTableFieldCombo();
      }
    };
    backupChanged = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Stepname.Label" ) );
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
    wConnection.addModifyListener( lsConnectionMod );
    wConnection.addSelectionListener( lsSelection );

    // Schema line...
    wlSchema = new Label( shell, SWT.RIGHT );
    wlSchema.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.TargetSchema.Label" ) );
    props.setLook( wlSchema );
    fdlSchema = new FormData();
    fdlSchema.left = new FormAttachment( 0, 0 );
    fdlSchema.right = new FormAttachment( middle, -margin );
    fdlSchema.top = new FormAttachment( wConnection, margin * 2 );
    wlSchema.setLayoutData( fdlSchema );

    wbSchema = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbSchema );
    wbSchema.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbSchema = new FormData();
    fdbSchema.top = new FormAttachment( wConnection, 2 * margin );
    fdbSchema.right = new FormAttachment( 100, 0 );
    wbSchema.setLayoutData( fdbSchema );

    wSchema = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSchema );
    wSchema.addModifyListener( lsTableMod );
    fdSchema = new FormData();
    fdSchema.left = new FormAttachment( middle, 0 );
    fdSchema.top = new FormAttachment( wConnection, margin * 2 );
    fdSchema.right = new FormAttachment( wbSchema, -margin );
    wSchema.setLayoutData( fdSchema );

    // Table line...
    wlTable = new Label( shell, SWT.RIGHT );
    wlTable.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Lookuptable.Label" ) );
    props.setLook( wlTable );
    fdlTable = new FormData();
    fdlTable.left = new FormAttachment( 0, 0 );
    fdlTable.right = new FormAttachment( middle, -margin );
    fdlTable.top = new FormAttachment( wbSchema, margin );
    wlTable.setLayoutData( fdlTable );

    wbTable = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTable );
    wbTable.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Browse.Button" ) );
    fdbTable = new FormData();
    fdbTable.right = new FormAttachment( 100, 0 );
    fdbTable.top = new FormAttachment( wbSchema, margin );
    wbTable.setLayoutData( fdbTable );

    wTable = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTable );
    wTable.addModifyListener( lsTableMod );
    fdTable = new FormData();
    fdTable.left = new FormAttachment( middle, 0 );
    fdTable.top = new FormAttachment( wbSchema, margin );
    fdTable.right = new FormAttachment( wbTable, -margin );
    wTable.setLayoutData( fdTable );

    // Cache?
    wlCache = new Label( shell, SWT.RIGHT );
    wlCache.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Cache.Label" ) );
    props.setLook( wlCache );
    fdlCache = new FormData();
    fdlCache.left = new FormAttachment( 0, 0 );
    fdlCache.right = new FormAttachment( middle, -margin );
    fdlCache.top = new FormAttachment( wTable, margin );
    wlCache.setLayoutData( fdlCache );
    wCache = new Button( shell, SWT.CHECK );
    props.setLook( wCache );
    fdCache = new FormData();
    fdCache.left = new FormAttachment( middle, 0 );
    fdCache.top = new FormAttachment( wTable, margin );
    wCache.setLayoutData( fdCache );
    wCache.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableFields();
      }
    } );

    // Cache size line
    wlCachesize = new Label( shell, SWT.RIGHT );
    wlCachesize.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Cachesize.Label" ) );
    props.setLook( wlCachesize );
    wlCachesize.setEnabled( input.isCached() );
    fdlCachesize = new FormData();
    fdlCachesize.left = new FormAttachment( 0, 0 );
    fdlCachesize.right = new FormAttachment( middle, -margin );
    fdlCachesize.top = new FormAttachment( wCache, margin );
    wlCachesize.setLayoutData( fdlCachesize );
    wCachesize = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wCachesize );
    wCachesize.setEnabled( input.isCached() );
    wCachesize.addModifyListener( lsMod );
    fdCachesize = new FormData();
    fdCachesize.left = new FormAttachment( middle, 0 );
    fdCachesize.right = new FormAttachment( 100, 0 );
    fdCachesize.top = new FormAttachment( wCache, margin );
    wCachesize.setLayoutData( fdCachesize );

    // Cache : Load all?
    wlCacheLoadAll = new Label( shell, SWT.RIGHT );
    wlCacheLoadAll.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.CacheLoadAll.Label" ) );
    props.setLook( wlCacheLoadAll );
    fdlCacheLoadAll = new FormData();
    fdlCacheLoadAll.left = new FormAttachment( 0, 0 );
    fdlCacheLoadAll.right = new FormAttachment( middle, -margin );
    fdlCacheLoadAll.top = new FormAttachment( wCachesize, margin );
    wlCacheLoadAll.setLayoutData( fdlCacheLoadAll );
    wCacheLoadAll = new Button( shell, SWT.CHECK );
    props.setLook( wCacheLoadAll );
    fdCacheLoadAll = new FormData();
    fdCacheLoadAll.left = new FormAttachment( middle, 0 );
    fdCacheLoadAll.top = new FormAttachment( wCachesize, margin );
    wCacheLoadAll.setLayoutData( fdCacheLoadAll );
    wCacheLoadAll.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableFields();
      }
    } );

    wlKey = new Label( shell, SWT.NONE );
    wlKey.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Keys.Label" ) );
    props.setLook( wlKey );
    fdlKey = new FormData();
    fdlKey.left = new FormAttachment( 0, 0 );
    fdlKey.top = new FormAttachment( wCacheLoadAll, margin );
    wlKey.setLayoutData( fdlKey );

    int nrKeyCols = 4;
    int nrKeyRows = ( input.getStreamKeyField1() != null ? input.getStreamKeyField1().length : 1 );

    ColumnInfo[] ciKey = new ColumnInfo[nrKeyCols];
    ciKey[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Tablefield" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciKey[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Comparator" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, DatabaseLookupMeta.conditionStrings );
    ciKey[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Field1" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciKey[3] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Field2" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    tableFieldColumns.add( ciKey[0] );
    fieldColumns.add( ciKey[2] );
    fieldColumns.add( ciKey[3] );
    wKey =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKey,
        nrKeyRows, lsMod, props );

    fdKey = new FormData();
    fdKey.left = new FormAttachment( 0, 0 );
    fdKey.top = new FormAttachment( wlKey, margin );
    fdKey.right = new FormAttachment( 100, 0 );
    fdKey.bottom = new FormAttachment( wlKey, 190 );
    wKey.setLayoutData( fdKey );

    // THE UPDATE/INSERT TABLE
    wlReturn = new Label( shell, SWT.NONE );
    wlReturn.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Return.Label" ) );
    props.setLook( wlReturn );
    fdlReturn = new FormData();
    fdlReturn.left = new FormAttachment( 0, 0 );
    fdlReturn.top = new FormAttachment( wKey, margin );
    wlReturn.setLayoutData( fdlReturn );

    int UpInsCols = 4;
    int UpInsRows = ( input.getReturnValueField() != null ? input.getReturnValueField().length : 1 );

    ColumnInfo[] ciReturn = new ColumnInfo[UpInsCols];
    ciReturn[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Field" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        new String[] {}, false );
    ciReturn[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Newname" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    ciReturn[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Default" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    ciReturn[3] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DatabaseLookupDialog.ColumnInfo.Type" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        ValueMetaFactory.getValueMetaNames() );
    tableFieldColumns.add( ciReturn[0] );

    wReturn =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciReturn,
        UpInsRows, lsMod, props );

    fdReturn = new FormData();
    fdReturn.left = new FormAttachment( 0, 0 );
    fdReturn.top = new FormAttachment( wlReturn, margin );
    fdReturn.right = new FormAttachment( 100, 0 );
    fdReturn.bottom = new FormAttachment( wlReturn, 190 );
    wReturn.setLayoutData( fdReturn );

    // EatRows?
    wlEatRows = new Label( shell, SWT.RIGHT );
    wlEatRows.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.EatRows.Label" ) );
    props.setLook( wlEatRows );
    fdlEatRows = new FormData();
    fdlEatRows.left = new FormAttachment( 0, 0 );
    fdlEatRows.top = new FormAttachment( wReturn, margin );
    fdlEatRows.right = new FormAttachment( middle, -margin );
    wlEatRows.setLayoutData( fdlEatRows );
    wEatRows = new Button( shell, SWT.CHECK );
    props.setLook( wEatRows );
    fdEatRows = new FormData();
    fdEatRows.left = new FormAttachment( middle, 0 );
    fdEatRows.top = new FormAttachment( wReturn, margin );
    wEatRows.setLayoutData( fdEatRows );
    wEatRows.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableFields();
      }
    } );

    // FailMultiple?
    wlFailMultiple = new Label( shell, SWT.RIGHT );
    wlFailMultiple.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.FailMultiple.Label" ) );
    props.setLook( wlFailMultiple );
    fdlFailMultiple = new FormData();
    fdlFailMultiple.left = new FormAttachment( 0, 0 );
    fdlFailMultiple.top = new FormAttachment( wEatRows, margin );
    fdlFailMultiple.right = new FormAttachment( middle, -margin );
    wlFailMultiple.setLayoutData( fdlFailMultiple );
    wFailMultiple = new Button( shell, SWT.CHECK );
    props.setLook( wFailMultiple );
    fdFailMultiple = new FormData();
    fdFailMultiple.left = new FormAttachment( middle, 0 );
    fdFailMultiple.top = new FormAttachment( wEatRows, margin );
    wFailMultiple.setLayoutData( fdFailMultiple );
    wFailMultiple.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableFields();
      }
    } );

    // OderBy line
    wlOrderBy = new Label( shell, SWT.RIGHT );
    wlOrderBy.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.Orderby.Label" ) );
    props.setLook( wlOrderBy );
    fdlOrderBy = new FormData();
    fdlOrderBy.left = new FormAttachment( 0, 0 );
    fdlOrderBy.top = new FormAttachment( wFailMultiple, margin );
    fdlOrderBy.right = new FormAttachment( middle, -margin );
    wlOrderBy.setLayoutData( fdlOrderBy );
    wOrderBy = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wOrderBy );
    fdOrderBy = new FormData();
    fdOrderBy.left = new FormAttachment( middle, 0 );
    fdOrderBy.top = new FormAttachment( wFailMultiple, margin );
    fdOrderBy.right = new FormAttachment( 100, 0 );
    wOrderBy.setLayoutData( fdOrderBy );
    wOrderBy.addModifyListener( lsMod );

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.GetFields.Button" ) );
    wGetLU = new Button( shell, SWT.PUSH );
    wGetLU.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.GetLookupFields.Button" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel, wGet, wGetLU }, margin, wOrderBy );

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
    lsGetLU = new Listener() {
      public void handleEvent( Event e ) {
        getlookup();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wGetLU.addListener( SWT.Selection, lsGetLU );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wTable.addSelectionListener( lsDef );
    wOrderBy.addSelectionListener( lsDef );
    wCachesize.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );
    wbSchema.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        getSchemaNames();
      }
    } );
    wbTable.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        getTableName();
      }
    } );

    shell.pack();
    shell.setMinimumSize( shell.getSize() );

    getData();
    input.setChanged( backupChanged );

    setComboValues();
    setTableFieldCombo();
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void setComboValues() {
    Runnable fieldLoader = new Runnable() {
      public void run() {
        try {
          prevFields = transMeta.getPrevStepFields( stepname );
        } catch ( KettleException e ) {
          prevFields = new RowMeta();
          String msg = BaseMessages.getString( PKG, "DatabaseLookupDialog.DoMapping.UnableToFindInput" );
          logError( msg );
        }
        String[] prevStepFieldNames = prevFields.getFieldNames();
        Arrays.sort( prevStepFieldNames );
        for ( ColumnInfo colInfo : fieldColumns ) {
          colInfo.setComboValues( prevStepFieldNames );
        }
      }
    };
    new Thread( fieldLoader ).start();
  }

  private void setTableFieldCombo() {
    Runnable fieldLoader = new Runnable() {
      public void run() {
        if ( !wTable.isDisposed() && !wConnection.isDisposed() && !wSchema.isDisposed() ) {
          final String tableName = wTable.getText(), connectionName = wConnection.getText(), schemaName =
            wSchema.getText();
          if ( !Utils.isEmpty( tableName ) ) {
            DatabaseMeta ci = transMeta.findDatabase( connectionName );
            if ( ci != null ) {
              Database db = new Database( loggingObject, ci );
              db.shareVariablesWith( transMeta );
              try {
                db.connect();

                //RowMetaInterface r = db.getTableFieldsMeta( schemaName, tableName );
                String schemaTable = ci.getQuotedSchemaTableCombination( schemaName, tableName );
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

  private void enableFields() {
    wlOrderBy.setEnabled( !wFailMultiple.getSelection() );
    wOrderBy.setEnabled( !wFailMultiple.getSelection() );

    wCachesize.setEnabled( wCache.getSelection() && !wCacheLoadAll.getSelection() );
    wlCachesize.setEnabled( wCache.getSelection() && !wCacheLoadAll.getSelection() );
    wCacheLoadAll.setEnabled( wCache.getSelection() );
    wlCacheLoadAll.setEnabled( wCache.getSelection() );
    wFailMultiple.setEnabled( !wCache.getSelection() );
    wlFailMultiple.setEnabled( !wCache.getSelection() );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    logDebug( BaseMessages.getString( PKG, "DatabaseLookupDialog.Log.GettingKeyInfo" ) );

    wCache.setSelection( input.isCached() );
    wCachesize.setText( "" + input.getCacheSize() );
    wCacheLoadAll.setSelection( input.isLoadingAllDataInCache() );

    if ( input.getStreamKeyField1() != null ) {
      for ( int i = 0; i < input.getStreamKeyField1().length; i++ ) {
        TableItem item = wKey.table.getItem( i );
        if ( input.getTableKeyField()[i] != null ) {
          item.setText( 1, input.getTableKeyField()[i] );
        }
        if ( input.getKeyCondition()[i] != null ) {
          item.setText( 2, input.getKeyCondition()[i] );
        }
        if ( input.getStreamKeyField1()[i] != null ) {
          item.setText( 3, input.getStreamKeyField1()[i] );
        }
        if ( input.getStreamKeyField2()[i] != null ) {
          item.setText( 4, input.getStreamKeyField2()[i] );
        }
      }
    }

    if ( input.getReturnValueField() != null ) {
      for ( int i = 0; i < input.getReturnValueField().length; i++ ) {
        TableItem item = wReturn.table.getItem( i );
        if ( input.getReturnValueField()[i] != null ) {
          item.setText( 1, input.getReturnValueField()[i] );
        }
        if ( input.getReturnValueNewName()[i] != null
          && !input.getReturnValueNewName()[i].equals( input.getReturnValueField()[i] ) ) {
          item.setText( 2, input.getReturnValueNewName()[i] );
        }

        if ( input.getReturnValueDefault()[i] != null ) {
          item.setText( 3, input.getReturnValueDefault()[i] );
        }
        item.setText( 4, ValueMetaFactory.getValueMetaName( input.getReturnValueDefaultType()[i] ) );
      }
    }

    if ( input.getSchemaName() != null ) {
      wSchema.setText( input.getSchemaName() );
    }
    if ( input.getTablename() != null ) {
      wTable.setText( input.getTablename() );
    }
    if ( input.getDatabaseMeta() != null ) {
      wConnection.setText( input.getDatabaseMeta().getName() );
    } else if ( transMeta.nrDatabases() == 1 ) {
      wConnection.setText( transMeta.getDatabase( 0 ).getName() );
    }
    if ( input.getOrderByClause() != null ) {
      wOrderBy.setText( input.getOrderByClause() );
    }
    wFailMultiple.setSelection( input.isFailingOnMultipleResults() );
    wEatRows.setSelection( input.isEatingRowOnLookupFailure() );

    wKey.setRowNums();
    wKey.optWidth( true );
    wReturn.setRowNums();
    wReturn.optWidth( true );

    enableFields();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( backupChanged );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    int nrkeys = wKey.nrNonEmpty();
    int nrfields = wReturn.nrNonEmpty();

    input.allocate( nrkeys, nrfields );

    input.setCached( wCache.getSelection() );
    input.setCacheSize( Const.toInt( wCachesize.getText(), 0 ) );
    input.setLoadingAllDataInCache( wCacheLoadAll.getSelection() );

    logDebug( BaseMessages.getString( PKG, "DatabaseLookupDialog.Log.FoundKeys", String.valueOf( nrkeys ) ) );
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrkeys; i++ ) {
      TableItem item = wKey.getNonEmpty( i );
      input.getTableKeyField()[i] = item.getText( 1 );
      input.getKeyCondition()[i] = item.getText( 2 );
      input.getStreamKeyField1()[i] = item.getText( 3 );
      input.getStreamKeyField2()[i] = item.getText( 4 );
    }

    logDebug( BaseMessages.getString( PKG, "DatabaseLookupDialog.Log.FoundFields", String.valueOf( nrfields ) ) );
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem item = wReturn.getNonEmpty( i );
      input.getReturnValueField()[i] = item.getText( 1 );
      input.getReturnValueNewName()[i] = item.getText( 2 );
      if ( input.getReturnValueNewName()[i] == null || input.getReturnValueNewName()[i].length() == 0 ) {
        input.getReturnValueNewName()[i] = input.getReturnValueField()[i];
      }

      input.getReturnValueDefault()[i] = item.getText( 3 );
      input.getReturnValueDefaultType()[i] = ValueMetaFactory.getIdForValueMeta( item.getText( 4 ) );

      if ( input.getReturnValueDefaultType()[i] < 0 ) {
        input.getReturnValueDefaultType()[i] = ValueMetaInterface.TYPE_STRING;
      }
    }

    input.setSchemaName( wSchema.getText() );
    input.setTablename( wTable.getText() );
    input.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    input.setOrderByClause( wOrderBy.getText() );
    input.setFailingOnMultipleResults( wFailMultiple.getSelection() );
    input.setEatingRowOnLookupFailure( wEatRows.getSelection() );

    stepname = wStepname.getText(); // return value

    if ( transMeta.findDatabase( wConnection.getText() ) == null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "DatabaseLookupDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.InvalidConnection.DialogTitle" ) );
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
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "DatabaseLookupDialog.Log.LookingAtConnection" ) + inf.toString() );
      }

      DatabaseExplorerDialog std = new DatabaseExplorerDialog( shell, SWT.NONE, inf, transMeta.getDatabases() );
      std.setSelectedSchemaAndTable( wSchema.getText(), wTable.getText() );
      if ( std.open() ) {
        wSchema.setText( Const.NVL( std.getSchemaName(), "" ) );
        wTable.setText( Const.NVL( std.getTableName(), "" ) );
        setTableFieldCombo();
      }
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "DatabaseLookupDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
    }
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        TableItemInsertListener listener = new TableItemInsertListener() {
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            tableItem.setText( 2, "=" );
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious( r, wKey, 1, new int[] { 1, 3 }, new int[] {}, -1, -1, listener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "DatabaseLookupDialog.GetFieldsFailed.DialogTitle" ), BaseMessages
          .getString( PKG, "DatabaseLookupDialog.GetFieldsFailed.DialogMessage" ), ke );
    }

  }

  private void getlookup() {
    DatabaseMeta ci = transMeta.findDatabase( wConnection.getText() );
    if ( ci != null ) {
      Database db = new Database( loggingObject, ci );
      db.shareVariablesWith( transMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( wTable.getText() ) ) {
          String schemaTable =
            ci.getQuotedSchemaTableCombination( db.environmentSubstitute( wSchema.getText() ), db
              .environmentSubstitute( wTable.getText() ) );
          RowMetaInterface r = db.getTableFields( schemaTable );

          if ( r != null && !r.isEmpty() ) {
            logDebug( BaseMessages.getString( PKG, "DatabaseLookupDialog.Log.FoundTableFields" )
              + schemaTable + " --> " + r.toStringMeta() );
            BaseStepDialog
              .getFieldsFromPrevious( r, wReturn, 1, new int[] { 1, 2 }, new int[] { 4 }, -1, -1, null );
          } else {
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage( BaseMessages
              .getString( PKG, "DatabaseLookupDialog.CouldNotReadTableInfo.DialogMessage" ) );
            mb.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.CouldNotReadTableInfo.DialogTitle" ) );
            mb.open();
          }
        }
      } catch ( KettleException e ) {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( BaseMessages.getString( PKG, "DatabaseLookupDialog.ErrorOccurred.DialogMessage" )
          + Const.CR + e.getMessage() );
        mb.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.ErrorOccurred.DialogTitle" ) );
        mb.open();
      }
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "DatabaseLookupDialog.InvalidConnectionName.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.InvalidConnectionName.DialogTitle" ) );
      mb.open();
    }
  }

  private void getSchemaNames() {
    DatabaseMeta databaseMeta = transMeta.findDatabase( wConnection.getText() );
    if ( databaseMeta != null ) {
      Database database = new Database( loggingObject, databaseMeta );
      try {
        database.connect();
        String[] schemas = database.getSchemas();

        if ( null != schemas && schemas.length > 0 ) {
          schemas = Const.sortStrings( schemas );
          EnterSelectionDialog dialog =
            new EnterSelectionDialog( shell, schemas, BaseMessages.getString(
              PKG, "DatabaseLookupDialog.AvailableSchemas.Title", wConnection.getText() ), BaseMessages
              .getString( PKG, "DatabaseLookupDialog.AvailableSchemas.Message", wConnection.getText() ) );
          String d = dialog.open();
          if ( d != null ) {
            wSchema.setText( Const.NVL( d, "" ) );
            setTableFieldCombo();
          }

        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "DatabaseLookupDialog.NoSchema.Error" ) );
          mb.setText( BaseMessages.getString( PKG, "DatabaseLookupDialog.GetSchemas.Error" ) );
          mb.open();
        }
      } catch ( Exception e ) {
        new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages
          .getString( PKG, "DatabaseLookupDialog.ErrorGettingSchemas" ), e );
      } finally {
        database.disconnect();
      }
    }
  }
}
