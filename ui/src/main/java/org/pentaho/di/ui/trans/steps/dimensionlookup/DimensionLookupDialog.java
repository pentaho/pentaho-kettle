/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.dimensionlookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dimensionlookup.DimensionLookupMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.ui.util.HelpUtils;

/**
 * Dialog for the Dimension Lookup/Update step.
 */
public class DimensionLookupDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = DimensionLookupMeta.class; // for
  // i18n
  // purposes,
  // needed
  // by
  // Translator2!!

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wKeyTab, wFieldsTab;

  private FormData fdKeyComp, fdFieldsComp;

  private CCombo wConnection;

  private Label wlSchema;
  private TextVar wSchema;
  private Button wbSchema;
  private FormData fdbSchema;

  private Label wlTable;
  private Button wbTable;
  private TextVar wTable;

  private Label wlCommit;
  private Text wCommit;

  private Label wlUseCache;
  private Button wUseCache;

  private Label wlPreloadCache;
  private Button wPreloadCache;

  private Label wlCacheSize;
  private Text wCacheSize;

  private Label wlTk;
  private CCombo wTk;

  private Label wlTkRename;
  private Text wTkRename;

  private Group gTechGroup;

  private Label wlAutoinc;
  private Button wAutoinc;

  private Label wlTableMax;
  private Button wTableMax;

  private Label wlSeqButton;
  private Button wSeqButton;
  private Text wSeq;

  private Label wlVersion;
  private CCombo wVersion;

  private Label wlDatefield;
  private CCombo wDatefield;

  private Label wlFromdate;
  private CCombo wFromdate;

  private Label wlUseAltStartDate;
  private Button wUseAltStartDate;
  private CCombo wAltStartDate;
  private CCombo wAltStartDateField;

  private Label wlMinyear;
  private Text wMinyear;

  private Label wlTodate;
  private CCombo wTodate;

  private Label wlMaxyear;
  private Text wMaxyear;

  private Label wlUpdate;
  private Button wUpdate;

  private Label wlKey;
  private TableView wKey;

  private Label wlUpIns;
  private TableView wUpIns;

  private Button wGet, wCreate;
  private Listener lsGet, lsCreate;

  private DimensionLookupMeta input;
  private boolean backupUpdate, backupAutoInc;

  private DatabaseMeta ci;

  private ColumnInfo[] ciUpIns;

  private ColumnInfo[] ciKey;

  private Map<String, Integer> inputFields;

  private boolean gotPreviousFields = false;

  private boolean gotTableFields = false;

  /**
   * List of ColumnInfo that should have the field names of the selected database table
   */
  private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();

  private ScrolledComposite sComp;

  private Composite helpComp;

  private Composite comp;

  public DimensionLookupDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (DimensionLookupMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };

    FocusListener lsConnectionFocus = new FocusAdapter() {

      public void focusLost( FocusEvent event ) {
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

    backupChanged = input.hasChanged();
    backupUpdate = input.isUpdate();
    backupAutoInc = input.isAutoIncrement();
    ci = input.getDatabaseMeta();

    GridLayout shellLayout = new GridLayout();
    shellLayout.numColumns = 1;
    shell.setLayout( shellLayout );
    shell.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    Composite sCompParent = new Composite( shell, SWT.NONE );
    sCompParent.setLayout( new FillLayout( SWT.VERTICAL ) );
    GridData sCompGridData = new GridData( GridData.FILL_BOTH );
    sCompGridData.grabExcessHorizontalSpace = true;
    sCompGridData.grabExcessVerticalSpace = true;
    sCompParent.setLayoutData( sCompGridData );

    sComp = new ScrolledComposite( sCompParent, SWT.V_SCROLL | SWT.H_SCROLL );
    sComp.setLayout( new FormLayout() );
    sComp.setExpandHorizontal( true );
    sComp.setExpandVertical( true );

    helpComp = new Composite( shell, SWT.NONE );
    helpComp.setLayout( new FormLayout() );
    GridData helpCompData = new GridData();
    helpCompData.grabExcessHorizontalSpace = true;
    helpCompData.grabExcessVerticalSpace = false;
    helpComp.setLayoutData( helpCompData );
    setShellImage( shell, input );

    comp = new Composite( sComp, SWT.NONE );
    props.setLook( comp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    comp.setLayout( fileLayout );

    // Stepname line
    wlStepname = new Label( comp, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Update the dimension?
    wlUpdate = new Label( comp, SWT.RIGHT );
    wlUpdate.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Update.Label" ) );
    props.setLook( wlUpdate );
    FormData fdlUpdate = new FormData();
    fdlUpdate.left = new FormAttachment( 0, 0 );
    fdlUpdate.right = new FormAttachment( middle, -margin );
    fdlUpdate.top = new FormAttachment( wStepname, margin );
    wlUpdate.setLayoutData( fdlUpdate );
    wUpdate = new Button( comp, SWT.CHECK );
    props.setLook( wUpdate );
    FormData fdUpdate = new FormData();
    fdUpdate.left = new FormAttachment( middle, 0 );
    fdUpdate.top = new FormAttachment( wStepname, margin );
    fdUpdate.right = new FormAttachment( 100, 0 );
    wUpdate.setLayoutData( fdUpdate );

    // Clicking on update changes the options in the update combo boxes!
    wUpdate.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setUpdate( !input.isUpdate() );
        input.setChanged();

        setFlags();
      }
    } );

    // Connection line
    wConnection = addConnectionLine( comp, wUpdate, middle, margin );
    if ( input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1 ) {
      wConnection.select( 0 );
    }
    // wConnection.addModifyListener(lsConnectionMod);
    // wConnection.addSelectionListener(lsSelection);
    wConnection.addFocusListener( lsConnectionFocus );
    wConnection.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        // We have new content: change ci connection:
        ci = transMeta.findDatabase( wConnection.getText() );
        setFlags();
      }
    } );

    // Schema line...
    wlSchema = new Label( comp, SWT.RIGHT );
    wlSchema.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.TargetSchema.Label" ) );
    props.setLook( wlSchema );
    FormData fdlSchema = new FormData();
    fdlSchema.left = new FormAttachment( 0, 0 );
    fdlSchema.right = new FormAttachment( middle, -margin );
    fdlSchema.top = new FormAttachment( wConnection, margin );
    wlSchema.setLayoutData( fdlSchema );

    wbSchema = new Button( comp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbSchema );
    wbSchema.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbSchema = new FormData();
    fdbSchema.top = new FormAttachment( wConnection, margin );
    fdbSchema.right = new FormAttachment( 100, 0 );
    wbSchema.setLayoutData( fdbSchema );

    wSchema = new TextVar( transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSchema );
    wSchema.addModifyListener( lsTableMod );
    FormData fdSchema = new FormData();
    fdSchema.left = new FormAttachment( middle, 0 );
    fdSchema.top = new FormAttachment( wConnection, margin );
    fdSchema.right = new FormAttachment( wbSchema, -margin );
    wSchema.setLayoutData( fdSchema );

    // Table line...
    wlTable = new Label( comp, SWT.RIGHT );
    wlTable.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.TargeTable.Label" ) );
    props.setLook( wlTable );
    FormData fdlTable = new FormData();
    fdlTable.left = new FormAttachment( 0, 0 );
    fdlTable.right = new FormAttachment( middle, -margin );
    fdlTable.top = new FormAttachment( wbSchema, margin );
    wlTable.setLayoutData( fdlTable );

    wbTable = new Button( comp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTable );
    wbTable.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Browse.Button" ) );
    FormData fdbTable = new FormData();
    fdbTable.right = new FormAttachment( 100, 0 );
    fdbTable.top = new FormAttachment( wbSchema, margin );
    wbTable.setLayoutData( fdbTable );

    wTable = new TextVar( transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTable );
    wTable.addModifyListener( lsTableMod );
    FormData fdTable = new FormData();
    fdTable.left = new FormAttachment( middle, 0 );
    fdTable.top = new FormAttachment( wbSchema, margin );
    fdTable.right = new FormAttachment( wbTable, 0 );
    wTable.setLayoutData( fdTable );

    // Commit size ...
    wlCommit = new Label( comp, SWT.RIGHT );
    wlCommit.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Commit.Label" ) );
    props.setLook( wlCommit );
    FormData fdlCommit = new FormData();
    fdlCommit.left = new FormAttachment( 0, 0 );
    fdlCommit.right = new FormAttachment( middle, -margin );
    fdlCommit.top = new FormAttachment( wTable, margin );
    wlCommit.setLayoutData( fdlCommit );
    wCommit = new Text( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wCommit );
    wCommit.addModifyListener( lsMod );
    FormData fdCommit = new FormData();
    fdCommit.left = new FormAttachment( middle, 0 );
    fdCommit.top = new FormAttachment( wTable, margin );
    fdCommit.right = new FormAttachment( 100, 0 );
    wCommit.setLayoutData( fdCommit );

    // Use Cache?
    wlUseCache = new Label( comp, SWT.RIGHT );
    wlUseCache.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.UseCache.Label" ) );
    props.setLook( wlUseCache );
    FormData fdlUseCache = new FormData();
    fdlUseCache.left = new FormAttachment( 0, 0 );
    fdlUseCache.right = new FormAttachment( middle, -margin );
    fdlUseCache.top = new FormAttachment( wCommit, margin );
    wlUseCache.setLayoutData( fdlUseCache );
    wUseCache = new Button( comp, SWT.CHECK );
    props.setLook( wUseCache );
    wUseCache.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        setFlags();
        input.setChanged();
      }
    } );
    FormData fdUseCache = new FormData();
    fdUseCache.left = new FormAttachment( middle, 0 );
    fdUseCache.top = new FormAttachment( wCommit, margin );
    fdUseCache.right = new FormAttachment( 100, 0 );
    wUseCache.setLayoutData( fdUseCache );

    // Preload cache?
    wlPreloadCache = new Label( comp, SWT.RIGHT );
    wlPreloadCache.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.PreloadCache.Label" ) );
    props.setLook( wlPreloadCache );
    FormData fdlPreloadCache = new FormData();
    fdlPreloadCache.left = new FormAttachment( 0, 0 );
    fdlPreloadCache.right = new FormAttachment( middle, -margin );
    fdlPreloadCache.top = new FormAttachment( wUseCache, margin );
    wlPreloadCache.setLayoutData( fdlPreloadCache );
    wPreloadCache = new Button( comp, SWT.CHECK );
    props.setLook( wPreloadCache );
    wPreloadCache.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        setFlags();
        input.setChanged();
      }
    } );
    FormData fdPreloadCache = new FormData();
    fdPreloadCache.left = new FormAttachment( middle, 0 );
    fdPreloadCache.top = new FormAttachment( wUseCache, margin );
    fdPreloadCache.right = new FormAttachment( 100, 0 );
    wPreloadCache.setLayoutData( fdPreloadCache );

    // Cache size ...
    wlCacheSize = new Label( comp, SWT.RIGHT );
    wlCacheSize.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.CacheSize.Label" ) );
    props.setLook( wlCacheSize );
    FormData fdlCacheSize = new FormData();
    fdlCacheSize.left = new FormAttachment( 0, 0 );
    fdlCacheSize.right = new FormAttachment( middle, -margin );
    fdlCacheSize.top = new FormAttachment( wPreloadCache, margin );
    wlCacheSize.setLayoutData( fdlCacheSize );
    wCacheSize = new Text( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wCacheSize );
    wCacheSize.addModifyListener( lsMod );
    FormData fdCacheSize = new FormData();
    fdCacheSize.left = new FormAttachment( middle, 0 );
    fdCacheSize.top = new FormAttachment( wPreloadCache, margin );
    fdCacheSize.right = new FormAttachment( 100, 0 );
    wCacheSize.setLayoutData( fdCacheSize );

    wlTkRename = new Label( comp, SWT.RIGHT );

    wTabFolder = new CTabFolder( comp, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF KEY TAB ///
    // /
    wKeyTab = new CTabItem( wTabFolder, SWT.NONE );
    wKeyTab.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.KeyTab.CTabItem" ) );

    FormLayout keyLayout = new FormLayout();
    keyLayout.marginWidth = 3;
    keyLayout.marginHeight = 3;

    Composite wKeyComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wKeyComp );
    wKeyComp.setLayout( keyLayout );

    //
    // The Lookup fields: usually the key
    //
    wlKey = new Label( wKeyComp, SWT.NONE );
    wlKey.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.KeyFields.Label" ) );
    props.setLook( wlKey );
    FormData fdlKey = new FormData();
    fdlKey.left = new FormAttachment( 0, 0 );
    fdlKey.top = new FormAttachment( 0, margin );
    fdlKey.right = new FormAttachment( 100, 0 );
    wlKey.setLayoutData( fdlKey );

    int nrKeyCols = 2;
    int nrKeyRows = ( input.getKeyStream() != null ? input.getKeyStream().length : 1 );

    ciKey = new ColumnInfo[nrKeyCols];
    ciKey[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DimensionLookupDialog.ColumnInfo.DimensionField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciKey[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DimensionLookupDialog.ColumnInfo.FieldInStream" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    tableFieldColumns.add( ciKey[0] );
    wKey =
      new TableView( transMeta, wKeyComp, SWT.BORDER
        | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKey, nrKeyRows, lsMod, props );

    FormData fdKey = new FormData();
    fdKey.left = new FormAttachment( 0, 0 );
    fdKey.top = new FormAttachment( wlKey, margin );
    fdKey.right = new FormAttachment( 100, 0 );
    fdKey.bottom = new FormAttachment( 100, 0 );
    wKey.setLayoutData( fdKey );

    fdKeyComp = new FormData();
    fdKeyComp.left = new FormAttachment( 0, 0 );
    fdKeyComp.top = new FormAttachment( 0, 0 );
    fdKeyComp.right = new FormAttachment( 100, 0 );
    fdKeyComp.bottom = new FormAttachment( 100, 0 );
    wKeyComp.setLayoutData( fdKeyComp );

    wKeyComp.layout();
    wKeyTab.setControl( wKeyComp );

    // ///////////////////////////////////////////////////////////
    // / END OF KEY TAB
    // ///////////////////////////////////////////////////////////

    // Fields tab...
    //
    wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.FieldsTab.CTabItem.Title" ) );

    Composite wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFieldsComp );

    FormLayout fieldsCompLayout = new FormLayout();
    fieldsCompLayout.marginWidth = Const.FORM_MARGIN;
    fieldsCompLayout.marginHeight = Const.FORM_MARGIN;
    wFieldsComp.setLayout( fieldsCompLayout );

    // THE UPDATE/INSERT TABLE
    wlUpIns = new Label( wFieldsComp, SWT.NONE );
    wlUpIns.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.UpdateOrInsertFields.Label" ) );
    props.setLook( wlUpIns );
    FormData fdlUpIns = new FormData();
    fdlUpIns.left = new FormAttachment( 0, 0 );
    fdlUpIns.top = new FormAttachment( 0, margin );
    wlUpIns.setLayoutData( fdlUpIns );

    int UpInsCols = 3;
    int UpInsRows = ( input.getFieldStream() != null ? input.getFieldStream().length : 1 );

    ciUpIns = new ColumnInfo[UpInsCols];
    ciUpIns[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DimensionLookupDialog.ColumnInfo.DimensionField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciUpIns[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DimensionLookupDialog.ColumnInfo.StreamField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciUpIns[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DimensionLookupDialog.ColumnInfo.TypeOfDimensionUpdate" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, input.isUpdate()
          ? DimensionLookupMeta.typeDesc : DimensionLookupMeta.typeDescLookup );
    tableFieldColumns.add( ciUpIns[0] );
    wUpIns =
      new TableView( transMeta, wFieldsComp, SWT.BORDER
        | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciUpIns, UpInsRows, lsMod, props );

    FormData fdUpIns = new FormData();
    fdUpIns.left = new FormAttachment( 0, 0 );
    fdUpIns.top = new FormAttachment( wlUpIns, margin );
    fdUpIns.right = new FormAttachment( 100, 0 );
    fdUpIns.bottom = new FormAttachment( 100, 0 );
    wUpIns.setLayoutData( fdUpIns );

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

    fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment( 0, 0 );
    fdFieldsComp.top = new FormAttachment( 0, 0 );
    fdFieldsComp.right = new FormAttachment( 100, 0 );
    fdFieldsComp.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fdFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wCacheSize, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.height = 200;
    wTabFolder.setLayoutData( fdTabFolder );

    // Technical key field:
    wlTk = new Label( comp, SWT.RIGHT );
    wlTk.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.TechnicalKeyField.Label" ) );
    props.setLook( wlTk );
    FormData fdlTk = new FormData();
    fdlTk.left = new FormAttachment( 0, 0 );
    fdlTk.right = new FormAttachment( middle, -margin );
    fdlTk.top = new FormAttachment( wTabFolder, 2 * margin );

    wlTk.setLayoutData( fdlTk );
    wTk = new CCombo( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTk );
    wTk.addModifyListener( lsMod );
    FormData fdTk = new FormData();
    fdTk.left = new FormAttachment( middle, 0 );
    fdTk.top = new FormAttachment( wTabFolder, 2 * margin );
    fdTk.right = new FormAttachment( 50 + middle / 2, 0 );
    wTk.setLayoutData( fdTk );
    wTk.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFieldsFromTable();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    wlTkRename.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.NewName.Label" ) );
    props.setLook( wlTkRename );
    FormData fdlTkRename = new FormData();
    fdlTkRename.left = new FormAttachment( 50 + middle / 2, margin );
    fdlTkRename.top = new FormAttachment( wTabFolder, 2 * margin );
    wlTkRename.setLayoutData( fdlTkRename );
    wTkRename = new Text( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTkRename );
    wTkRename.addModifyListener( lsMod );
    FormData fdTkRename = new FormData();
    fdTkRename.left = new FormAttachment( wlTkRename, margin );
    fdTkRename.top = new FormAttachment( wTabFolder, 2 * margin );
    fdTkRename.right = new FormAttachment( 100, 0 );
    wTkRename.setLayoutData( fdTkRename );

    // //////////////////////////////////////////////////
    // The key creation box
    // //////////////////////////////////////////////////

    gTechGroup = new Group( comp, SWT.SHADOW_ETCHED_IN );
    gTechGroup.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.TechGroup.Label" ) );
    GridLayout gridLayout = new GridLayout( 3, false );
    gTechGroup.setLayout( gridLayout );
    FormData fdTechGroup = new FormData();
    fdTechGroup.left = new FormAttachment( middle, 0 );
    fdTechGroup.top = new FormAttachment( wTkRename, 2 * margin );
    fdTechGroup.right = new FormAttachment( 100, 0 );
    gTechGroup.setBackground( shell.getBackground() ); // the default looks ugly
    gTechGroup.setLayoutData( fdTechGroup );

    // Use maximum of table + 1
    wTableMax = new Button( gTechGroup, SWT.RADIO );
    props.setLook( wTableMax );
    wTableMax.setSelection( false );
    GridData gdTableMax = new GridData();
    wTableMax.setLayoutData( gdTableMax );
    wTableMax
      .setToolTipText( BaseMessages.getString( PKG, "DimensionLookupDialog.TableMaximum.Tooltip", Const.CR ) );
    wlTableMax = new Label( gTechGroup, SWT.LEFT );
    wlTableMax.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.TableMaximum.Label" ) );
    props.setLook( wlTableMax );
    GridData gdlTableMax = new GridData( GridData.FILL_BOTH );
    gdlTableMax.horizontalSpan = 2;
    gdlTableMax.verticalSpan = 1;
    wlTableMax.setLayoutData( gdlTableMax );

    // Sequence Check Button
    wSeqButton = new Button( gTechGroup, SWT.RADIO );
    props.setLook( wSeqButton );
    wSeqButton.setSelection( false );
    GridData gdSeqButton = new GridData();
    wSeqButton.setLayoutData( gdSeqButton );
    wSeqButton.setToolTipText( BaseMessages.getString( PKG, "DimensionLookupDialog.Sequence.Tooltip", Const.CR ) );
    wlSeqButton = new Label( gTechGroup, SWT.LEFT );
    wlSeqButton.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Sequence.Label" ) );
    props.setLook( wlSeqButton );
    GridData gdlSeqButton = new GridData();
    wlSeqButton.setLayoutData( gdlSeqButton );

    wSeq = new Text( gTechGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSeq );
    wSeq.addModifyListener( lsMod );
    GridData gdSeq = new GridData( GridData.FILL_HORIZONTAL );
    wSeq.setLayoutData( gdSeq );
    wSeq.addFocusListener( new FocusListener() {
      public void focusGained( FocusEvent arg0 ) {
        input.setTechKeyCreation( DimensionLookupMeta.CREATION_METHOD_SEQUENCE );
        wSeqButton.setSelection( true );
        wAutoinc.setSelection( false );
        wTableMax.setSelection( false );
      }

      public void focusLost( FocusEvent arg0 ) {
      }
    } );

    // Use an autoincrement field?
    wAutoinc = new Button( gTechGroup, SWT.RADIO );
    props.setLook( wAutoinc );
    wAutoinc.setSelection( false );
    GridData gdAutoinc = new GridData();
    wAutoinc.setLayoutData( gdAutoinc );
    wAutoinc
      .setToolTipText( BaseMessages.getString( PKG, "DimensionLookupDialog.AutoincButton.Tooltip", Const.CR ) );
    wlAutoinc = new Label( gTechGroup, SWT.LEFT );
    wlAutoinc.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Autoincrement.Label" ) );
    props.setLook( wlAutoinc );
    GridData gdlAutoinc = new GridData();
    wlAutoinc.setLayoutData( gdlAutoinc );

    // //////////////////////////////////////////////////
    // The key creation box END
    // //////////////////////////////////////////////////

    // Version key field:
    wlVersion = new Label( comp, SWT.RIGHT );
    wlVersion.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Version.Label" ) );
    props.setLook( wlVersion );
    FormData fdlVersion = new FormData();
    fdlVersion.left = new FormAttachment( 0, 0 );
    fdlVersion.right = new FormAttachment( middle, -margin );
    fdlVersion.top = new FormAttachment( gTechGroup, 2 * margin );
    wlVersion.setLayoutData( fdlVersion );
    wVersion = new CCombo( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wVersion );
    wVersion.addModifyListener( lsMod );
    FormData fdVersion = new FormData();
    fdVersion.left = new FormAttachment( middle, 0 );
    fdVersion.top = new FormAttachment( gTechGroup, 2 * margin );
    fdVersion.right = new FormAttachment( 100, 0 );
    wVersion.setLayoutData( fdVersion );
    wVersion.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFieldsFromTable();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Datefield line
    wlDatefield = new Label( comp, SWT.RIGHT );
    wlDatefield.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Datefield.Label" ) );
    props.setLook( wlDatefield );
    FormData fdlDatefield = new FormData();
    fdlDatefield.left = new FormAttachment( 0, 0 );
    fdlDatefield.right = new FormAttachment( middle, -margin );
    fdlDatefield.top = new FormAttachment( wVersion, 2 * margin );
    wlDatefield.setLayoutData( fdlDatefield );
    wDatefield = new CCombo( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDatefield );
    wDatefield.addModifyListener( lsMod );
    FormData fdDatefield = new FormData();
    fdDatefield.left = new FormAttachment( middle, 0 );
    fdDatefield.top = new FormAttachment( wVersion, 2 * margin );
    fdDatefield.right = new FormAttachment( 100, 0 );
    wDatefield.setLayoutData( fdDatefield );
    wDatefield.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Fromdate line
    //
    // 0 [wlFromdate] middle [wFromdate] (100-middle)/3 [wlMinyear]
    // 2*(100-middle)/3 [wMinyear] 100%
    //
    wlFromdate = new Label( comp, SWT.RIGHT );
    wlFromdate.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Fromdate.Label" ) );
    props.setLook( wlFromdate );
    FormData fdlFromdate = new FormData();
    fdlFromdate.left = new FormAttachment( 0, 0 );
    fdlFromdate.right = new FormAttachment( middle, -margin );
    fdlFromdate.top = new FormAttachment( wDatefield, 2 * margin );
    wlFromdate.setLayoutData( fdlFromdate );
    wFromdate = new CCombo( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFromdate );
    wFromdate.addModifyListener( lsMod );
    FormData fdFromdate = new FormData();
    fdFromdate.left = new FormAttachment( middle, 0 );
    fdFromdate.right = new FormAttachment( middle + ( 100 - middle ) / 3, -margin );
    fdFromdate.top = new FormAttachment( wDatefield, 2 * margin );
    wFromdate.setLayoutData( fdFromdate );
    wFromdate.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFieldsFromTable();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Minyear line
    wlMinyear = new Label( comp, SWT.RIGHT );
    wlMinyear.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Minyear.Label" ) );
    props.setLook( wlMinyear );
    FormData fdlMinyear = new FormData();
    fdlMinyear.left = new FormAttachment( wFromdate, margin );
    fdlMinyear.right = new FormAttachment( middle + 2 * ( 100 - middle ) / 3, -margin );
    fdlMinyear.top = new FormAttachment( wDatefield, 2 * margin );
    wlMinyear.setLayoutData( fdlMinyear );
    wMinyear = new Text( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMinyear );
    wMinyear.addModifyListener( lsMod );
    FormData fdMinyear = new FormData();
    fdMinyear.left = new FormAttachment( wlMinyear, margin );
    fdMinyear.right = new FormAttachment( 100, 0 );
    fdMinyear.top = new FormAttachment( wDatefield, 2 * margin );
    wMinyear.setLayoutData( fdMinyear );
    wMinyear.setToolTipText( BaseMessages.getString( PKG, "DimensionLookupDialog.Minyear.ToolTip" ) );

    // Add a line with an option to specify an alternative start date...
    //
    wlUseAltStartDate = new Label( comp, SWT.RIGHT );
    wlUseAltStartDate
      .setText( BaseMessages.getString( PKG, "DimensionLookupDialog.UseAlternativeStartDate.Label" ) );
    props.setLook( wlUseAltStartDate );
    FormData fdlUseAltStartDate = new FormData();
    fdlUseAltStartDate.left = new FormAttachment( 0, 0 );
    fdlUseAltStartDate.right = new FormAttachment( middle, -margin );
    fdlUseAltStartDate.top = new FormAttachment( wFromdate, margin );
    wlUseAltStartDate.setLayoutData( fdlUseAltStartDate );
    wUseAltStartDate = new Button( comp, SWT.CHECK );
    props.setLook( wUseAltStartDate );
    wUseAltStartDate.setToolTipText( BaseMessages.getString(
      PKG, "DimensionLookupDialog.UseAlternativeStartDate.Tooltip", Const.CR ) );
    FormData fdUseAltStartDate = new FormData();
    fdUseAltStartDate.left = new FormAttachment( middle, 0 );
    fdUseAltStartDate.top = new FormAttachment( wFromdate, margin );
    wUseAltStartDate.setLayoutData( fdUseAltStartDate );
    wUseAltStartDate.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setFlags();
        input.setChanged();
      }
    } );

    // The choice...
    //
    wAltStartDate = new CCombo( comp, SWT.BORDER );
    props.setLook( wAltStartDate );
    // All options except for "No alternative"...
    wAltStartDate.removeAll();
    for ( int i = 1; i < DimensionLookupMeta.getStartDateAlternativeDescriptions().length; i++ ) {
      wAltStartDate.add( DimensionLookupMeta.getStartDateAlternativeDescriptions()[i] );
    }
    wAltStartDate.setText( BaseMessages.getString(
      PKG, "DimensionLookupDialog.AlternativeStartDate.SelectItemDefault" ) );
    wAltStartDate.setToolTipText( BaseMessages.getString(
      PKG, "DimensionLookupDialog.AlternativeStartDate.Tooltip", Const.CR ) );
    FormData fdAltStartDate = new FormData();
    fdAltStartDate.left = new FormAttachment( wUseAltStartDate, 2 * margin );
    fdAltStartDate.right = new FormAttachment( wUseAltStartDate, 200 );
    fdAltStartDate.top = new FormAttachment( wFromdate, margin );
    wAltStartDate.setLayoutData( fdAltStartDate );
    wAltStartDate.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        setFlags();
        input.setChanged();
      }
    } );
    wAltStartDateField = new CCombo( comp, SWT.SINGLE | SWT.BORDER );
    props.setLook( wAltStartDateField );
    wAltStartDateField.setToolTipText( BaseMessages.getString(
      PKG, "DimensionLookupDialog.AlternativeStartDateField.Tooltip", Const.CR ) );
    FormData fdAltStartDateField = new FormData();
    fdAltStartDateField.left = new FormAttachment( wAltStartDate, 2 * margin );
    fdAltStartDateField.right = new FormAttachment( 100, 0 );
    fdAltStartDateField.top = new FormAttachment( wFromdate, margin );
    wAltStartDateField.setLayoutData( fdAltStartDateField );
    wAltStartDateField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFieldsFromTable();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Todate line
    wlTodate = new Label( comp, SWT.RIGHT );
    wlTodate.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Todate.Label" ) );
    props.setLook( wlTodate );
    FormData fdlTodate = new FormData();
    fdlTodate.left = new FormAttachment( 0, 0 );
    fdlTodate.right = new FormAttachment( middle, -margin );
    fdlTodate.top = new FormAttachment( wAltStartDate, margin );
    wlTodate.setLayoutData( fdlTodate );
    wTodate = new CCombo( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTodate );
    wTodate.addModifyListener( lsMod );
    FormData fdTodate = new FormData();
    fdTodate.left = new FormAttachment( middle, 0 );
    fdTodate.right = new FormAttachment( middle + ( 100 - middle ) / 3, -margin );
    fdTodate.top = new FormAttachment( wAltStartDate, margin );
    wTodate.setLayoutData( fdTodate );
    wTodate.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFieldsFromTable();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Maxyear line
    wlMaxyear = new Label( comp, SWT.RIGHT );
    wlMaxyear.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.Maxyear.Label" ) );
    props.setLook( wlMaxyear );
    FormData fdlMaxyear = new FormData();
    fdlMaxyear.left = new FormAttachment( wTodate, margin );
    fdlMaxyear.right = new FormAttachment( middle + 2 * ( 100 - middle ) / 3, -margin );
    fdlMaxyear.top = new FormAttachment( wAltStartDate, margin );
    wlMaxyear.setLayoutData( fdlMaxyear );
    wMaxyear = new Text( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMaxyear );
    wMaxyear.addModifyListener( lsMod );
    FormData fdMaxyear = new FormData();
    fdMaxyear.left = new FormAttachment( wlMaxyear, margin );
    fdMaxyear.right = new FormAttachment( 100, 0 );
    fdMaxyear.top = new FormAttachment( wAltStartDate, margin );
    wMaxyear.setLayoutData( fdMaxyear );
    wMaxyear.setToolTipText( BaseMessages.getString( PKG, "DimensionLookupDialog.Maxyear.ToolTip" ) );

    // THE BOTTOM BUTTONS
    wOK = new Button( comp, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wGet = new Button( comp, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.GetFields.Button" ) );
    wCreate = new Button( comp, SWT.PUSH );
    wCreate.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.SQL.Button" ) );
    wCancel = new Button( comp, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel, wGet, wCreate }, margin, wMaxyear );

    FormData fdComp = new FormData();
    fdComp.left = new FormAttachment( 0, 0 );
    fdComp.top = new FormAttachment( 0, 0 );
    fdComp.right = new FormAttachment( 100, 0 );
    fdComp.bottom = new FormAttachment( 100, 0 );
    comp.setLayoutData( fdComp );

    comp.pack();
    Rectangle bounds = comp.getBounds();

    sComp.setContent( comp );
    sComp.setExpandHorizontal( true );
    sComp.setExpandVertical( true );
    sComp.setMinWidth( bounds.width );
    sComp.setMinHeight( bounds.height );

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
    lsCreate = new Listener() {
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
    wGet.addListener( SWT.Selection, lsGet );
    wCreate.addListener( SWT.Selection, lsCreate );
    wCancel.addListener( SWT.Selection, lsCancel );

    setTableMax();
    setSequence();
    setAutoincUse();

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wSchema.addSelectionListener( lsDef );
    wTable.addSelectionListener( lsDef );
    wCommit.addSelectionListener( lsDef );
    wCacheSize.addSelectionListener( lsDef );
    wTk.addSelectionListener( lsDef );
    wTkRename.addSelectionListener( lsDef );
    wSeq.addSelectionListener( lsDef );
    wVersion.addSelectionListener( lsDef );
    wDatefield.addSelectionListener( lsDef );
    wFromdate.addSelectionListener( lsDef );
    wMinyear.addSelectionListener( lsDef );
    wTodate.addSelectionListener( lsDef );
    wMaxyear.addSelectionListener( lsDef );

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

    wTabFolder.setSelection( 0 );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    setTableFieldCombo();
    input.setChanged( backupChanged );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  public void setFlags() {
    ColumnInfo colinf =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DimensionLookupDialog.ColumnInfo.Type" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        input.isUpdate() ? DimensionLookupMeta.typeDesc : DimensionLookupMeta.typeDescLookup );
    wUpIns.setColumnInfo( 2, colinf );

    if ( input.isUpdate() ) {
      wUpIns.setColumnText( 2, BaseMessages.getString(
        PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnText.SteamFieldToCompare" ) );
      wUpIns.setColumnText( 3, BaseMessages.getString(
        PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnTextTypeOfDimensionUpdate" ) );
      wUpIns.setColumnToolTip( 2, BaseMessages.getString(
        PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnToolTip" )
        + Const.CR + "Punch Through: Kimball Type I" + Const.CR + "Update: Correct error in last version" );
    } else {
      wUpIns.setColumnText( 2, BaseMessages.getString(
        PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnText.NewNameOfOutputField" ) );
      wUpIns.setColumnText( 3, BaseMessages.getString(
        PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnText.TypeOfReturnField" ) );
      wUpIns.setColumnToolTip( 2, BaseMessages.getString(
        PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnToolTip2" ) );
    }
    wUpIns.optWidth( true );

    // In case of lookup: disable commitsize, etc.
    boolean update = wUpdate.getSelection();
    wlCommit.setEnabled( update );
    wCommit.setEnabled( update );
    wlMinyear.setEnabled( update );
    wMinyear.setEnabled( update );
    wlMaxyear.setEnabled( update );
    wMaxyear.setEnabled( update );
    wlMinyear.setEnabled( update );
    wMinyear.setEnabled( update );
    wlVersion.setEnabled( update );
    wVersion.setEnabled( update );
    wlTkRename.setEnabled( !update );
    wTkRename.setEnabled( !update );

    wCreate.setEnabled( update );

    // Set the technical creation key fields correct... then disable
    // depending on update or not. Then reset if we're updating. It makes
    // sure that the disabled options because of database restrictions
    // will always be properly grayed out.
    setAutoincUse();
    setSequence();
    setTableMax();

    // Surpisingly we can't disable these fields as they influence the
    // calculation of the "Unknown" key
    // If we have a MySQL database with Auto-increment for example, the
    // "unknown" is 1.
    // If we have a MySQL database with Table-max the "unknown" is 0.
    //

    // gTechGroup.setEnabled( update );
    // wlAutoinc.setEnabled( update );
    // wAutoinc.setEnabled( update );
    // wlTableMax.setEnabled( update );
    // wTableMax.setEnabled( update );
    // wlSeqButton.setEnabled( update );
    // wSeqButton.setEnabled( update );
    // wSeq.setEnabled( update );

    if ( update ) {
      setAutoincUse();
      setSequence();
      setTableMax();
    }

    // The alternative start date
    //
    wAltStartDate.setEnabled( wUseAltStartDate.getSelection() );
    int alternative = DimensionLookupMeta.getStartDateAlternative( wAltStartDate.getText() );
    wAltStartDateField.setEnabled( alternative == DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE );

    // Caching...
    //
    wlPreloadCache.setEnabled( wUseCache.getSelection() && !wUpdate.getSelection() );
    wPreloadCache.setEnabled( wUseCache.getSelection() && !wUpdate.getSelection() );

    wlCacheSize.setEnabled( wUseCache.getSelection() && !wPreloadCache.getSelection() );
    wCacheSize.setEnabled( wUseCache.getSelection() && !wPreloadCache.getSelection() );

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
    ciKey[1].setComboValues( fieldNames );
    ciUpIns[1].setComboValues( fieldNames );
  }

  public void setAutoincUse() {
    boolean enable = ( ci == null ) || ( ci.supportsAutoinc() && ci.supportsAutoGeneratedKeys() );

    wlAutoinc.setEnabled( enable );
    wAutoinc.setEnabled( enable );
    if ( !enable && wAutoinc.getSelection() ) {
      wAutoinc.setSelection( false );
      wSeqButton.setSelection( false );
      wTableMax.setSelection( true );
    }
  }

  public void setTableMax() {
    wlTableMax.setEnabled( true );
    wTableMax.setEnabled( true );
  }

  public void setSequence() {
    boolean seq = ( ci == null ) || ci.supportsSequences();
    wSeq.setEnabled( seq );
    wlSeqButton.setEnabled( seq );
    wSeqButton.setEnabled( seq );
    if ( !seq && wSeqButton.getSelection() ) {
      wAutoinc.setSelection( false );
      wSeqButton.setSelection( false );
      wTableMax.setSelection( true );
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "DimensionLookupDialog.Log.GettingKeyInfo" ) );
    }

    if ( input.getKeyStream() != null ) {
      for ( int i = 0; i < input.getKeyStream().length; i++ ) {
        TableItem item = wKey.table.getItem( i );
        if ( input.getKeyLookup()[i] != null ) {
          item.setText( 1, input.getKeyLookup()[i] );
        }
        if ( input.getKeyStream()[i] != null ) {
          item.setText( 2, input.getKeyStream()[i] );
        }
      }
    }

    if ( input.getFieldStream() != null ) {
      for ( int i = 0; i < input.getFieldStream().length; i++ ) {
        TableItem item = wUpIns.table.getItem( i );
        if ( input.getFieldLookup()[i] != null ) {
          item.setText( 1, input.getFieldLookup()[i] );
        }
        if ( input.getFieldStream()[i] != null ) {
          item.setText( 2, input.getFieldStream()[i] );
        }
        item.setText( 3, DimensionLookupMeta.getUpdateType( input.isUpdate(), input.getFieldUpdate()[i] ) );
      }
    }

    wUpdate.setSelection( input.isUpdate() );

    if ( input.getSchemaName() != null ) {
      wSchema.setText( input.getSchemaName() );
    }
    if ( input.getTableName() != null ) {
      wTable.setText( input.getTableName() );
    }
    if ( input.getKeyField() != null ) {
      wTk.setText( input.getKeyField() );
    }
    if ( input.getKeyRename() != null ) {
      wTkRename.setText( input.getKeyRename() );
    }

    wAutoinc.setSelection( input.isAutoIncrement() );

    if ( input.getVersionField() != null ) {
      wVersion.setText( input.getVersionField() );
    }
    if ( input.getSequenceName() != null ) {
      wSeq.setText( input.getSequenceName() );
    }
    if ( input.getDatabaseMeta() != null ) {
      wConnection.setText( input.getDatabaseMeta().getName() );
    } else if ( transMeta.nrDatabases() == 1 ) {
      wConnection.setText( transMeta.getDatabase( 0 ).getName() );
    }
    if ( input.getDateField() != null ) {
      wDatefield.setText( input.getDateField() );
    }
    if ( input.getDateFrom() != null ) {
      wFromdate.setText( input.getDateFrom() );
    }
    if ( input.getDateTo() != null ) {
      wTodate.setText( input.getDateTo() );
    }

    String techKeyCreation = input.getTechKeyCreation();
    if ( techKeyCreation == null ) {
      // Determine the creation of the technical key for
      // backwards compatibility. Can probably be removed at
      // version 3.x or so (Sven Boden).
      DatabaseMeta database = input.getDatabaseMeta();
      if ( database == null || !database.supportsAutoinc() ) {
        input.setAutoIncrement( false );
      }
      wAutoinc.setSelection( input.isAutoIncrement() );

      wSeqButton.setSelection( input.getSequenceName() != null && input.getSequenceName().length() > 0 );
      if ( !input.isAutoIncrement() && ( input.getSequenceName() == null || input.getSequenceName().length() <= 0 ) ) {
        wTableMax.setSelection( true );
      }

      if ( database != null && database.supportsSequences() && input.getSequenceName() != null ) {
        wSeq.setText( input.getSequenceName() );
        input.setAutoIncrement( false );
        wTableMax.setSelection( false );
      }
    } else {
      // KETTLE post 2.2 version:
      // The "creation" field now determines the behaviour of the
      // key creation.
      if ( DimensionLookupMeta.CREATION_METHOD_AUTOINC.equals( techKeyCreation ) ) {
        wAutoinc.setSelection( true );
        wSeqButton.setSelection( false );
        wTableMax.setSelection( false );
      } else if ( ( DimensionLookupMeta.CREATION_METHOD_SEQUENCE.equals( techKeyCreation ) ) ) {
        wSeqButton.setSelection( true );
        wAutoinc.setSelection( false );
        wTableMax.setSelection( false );
      } else { // the rest
        wTableMax.setSelection( true );
        wAutoinc.setSelection( false );
        wSeqButton.setSelection( false );
        input.setTechKeyCreation( DimensionLookupMeta.CREATION_METHOD_TABLEMAX );
      }
      if ( input.getSequenceName() != null ) {
        wSeq.setText( input.getSequenceName() );
      }
    }

    wCommit.setText( "" + input.getCommitSize() );

    wUseCache.setSelection( input.getCacheSize() >= 0 );
    wPreloadCache.setSelection( input.isPreloadingCache() );
    if ( input.getCacheSize() >= 0 ) {
      wCacheSize.setText( "" + input.getCacheSize() );
    }

    wMinyear.setText( "" + input.getMinYear() );
    wMaxyear.setText( "" + input.getMaxYear() );

    wUpIns.removeEmptyRows();
    wUpIns.setRowNums();
    wUpIns.optWidth( true );
    wKey.removeEmptyRows();
    wKey.setRowNums();
    wKey.optWidth( true );

    ci = transMeta.findDatabase( wConnection.getText() );

    // The alternative start date...
    //
    wUseAltStartDate.setSelection( input.isUsingStartDateAlternative() );
    if ( input.isUsingStartDateAlternative() ) {
      wAltStartDate.setText( DimensionLookupMeta.getStartDateAlternativeDesc( input.getStartDateAlternative() ) );
    }
    wAltStartDateField.setText( Const.NVL( input.getStartDateFieldName(), "" ) );

    setFlags();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( backupChanged );
    input.setUpdate( backupUpdate );
    input.setAutoIncrement( backupAutoInc );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    getInfo( input );

    stepname = wStepname.getText(); // return value

    if ( input.getDatabaseMeta() == null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "DimensionLookupDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
      return;
    }

    dispose();
  }

  private void getInfo( DimensionLookupMeta in ) {
    in.setUpdate( wUpdate.getSelection() );

    // Table ktable = wKey.table;
    int nrkeys = wKey.nrNonEmpty();
    int nrfields = wUpIns.nrNonEmpty();

    in.allocate( nrkeys, nrfields );

    logDebug( BaseMessages.getString( PKG, "DimensionLookupDialog.Log.FoundKeys", String.valueOf( nrkeys ) ) );
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrkeys; i++ ) {
      TableItem item = wKey.getNonEmpty( i );
      in.getKeyLookup()[i] = item.getText( 1 );
      in.getKeyStream()[i] = item.getText( 2 );
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "DimensionLookupDialog.Log.FoundFields", String.valueOf( nrfields ) ) );
    }
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem item = wUpIns.getNonEmpty( i );
      in.getFieldLookup()[i] = item.getText( 1 );
      in.getFieldStream()[i] = item.getText( 2 );
      in.getFieldUpdate()[i] = DimensionLookupMeta.getUpdateType( in.isUpdate(), item.getText( 3 ) );
    }

    in.setSchemaName( wSchema.getText() );
    in.setTableName( wTable.getText() );
    in.setKeyField( wTk.getText() );
    in.setKeyRename( wTkRename.getText() );
    if ( wAutoinc.getSelection() ) {
      in.setTechKeyCreation( DimensionLookupMeta.CREATION_METHOD_AUTOINC );
      in.setAutoIncrement( true ); // for downwards compatibility
      in.setSequenceName( null );
    } else if ( wSeqButton.getSelection() ) {
      in.setTechKeyCreation( DimensionLookupMeta.CREATION_METHOD_SEQUENCE );
      in.setAutoIncrement( false );
      in.setSequenceName( wSeq.getText() );
    } else { // all the rest
      in.setTechKeyCreation( DimensionLookupMeta.CREATION_METHOD_TABLEMAX );
      in.setAutoIncrement( false );
      in.setSequenceName( null );
    }

    in.setAutoIncrement( wAutoinc.getSelection() );

    if ( in.getKeyRename() != null && in.getKeyRename().equalsIgnoreCase( in.getKeyField() ) ) {
      in.setKeyRename( null ); // Don't waste space&time if it's the same
    }

    in.setVersionField( wVersion.getText() );
    in.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    in.setDateField( wDatefield.getText() );
    in.setDateFrom( wFromdate.getText() );
    in.setDateTo( wTodate.getText() );

    in.setCommitSize( Const.toInt( wCommit.getText(), 0 ) );

    if ( wUseCache.getSelection() ) {
      in.setCacheSize( Const.toInt( wCacheSize.getText(), -1 ) );
    } else {
      in.setCacheSize( -1 );
    }
    in.setPreloadingCache( wPreloadCache.getSelection() );
    if ( wPreloadCache.getSelection() ) {
      in.setCacheSize( 0 );
    }

    in.setMinYear( Const.toInt( wMinyear.getText(), Const.MIN_YEAR ) );
    in.setMaxYear( Const.toInt( wMaxyear.getText(), Const.MAX_YEAR ) );

    in.setUsingStartDateAlternative( wUseAltStartDate.getSelection() );
    in.setStartDateAlternative( DimensionLookupMeta.getStartDateAlternative( wAltStartDate.getText() ) );
    in.setStartDateFieldName( wAltStartDateField.getText() );
  }

  private void getTableName() {
    int connr = wConnection.getSelectionIndex();
    if ( connr < 0 ) {
      return;
    }
    DatabaseMeta inf = transMeta.getDatabase( connr );

    logDebug( BaseMessages.getString( PKG, "DimensionLookupDialog.Log.LookingAtConnection" ) + inf.toString() );

    DatabaseExplorerDialog std = new DatabaseExplorerDialog( shell, SWT.NONE, inf, transMeta.getDatabases() );
    std.setSelectedSchemaAndTable( wSchema.getText(), wTable.getText() );
    if ( std.open() ) {
      wSchema.setText( Const.NVL( std.getSchemaName(), "" ) );
      wTable.setText( Const.NVL( std.getTableName(), "" ) );
      setTableFieldCombo();
    }
  }

  private void get() {
    if ( wTabFolder.getSelection() == wFieldsTab ) {
      if ( input.isUpdate() ) {
        getUpdate();
      } else {
        getLookup();
      }
    } else {
      getKeys();
    }
  }

  /**
   * Get the fields from the previous step and use them as "update fields". Only get the the fields which are not yet in
   * use as key, or in the field table. Also ignore technical key, version, fromdate, todate.
   */
  private void getUpdate() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog.getFieldsFromPrevious(
          r, wUpIns, 2, new int[] { 1, 2 }, new int[] {}, -1, -1, new TableItemInsertListener() {
            public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
              tableItem
                .setText( 3, BaseMessages.getString( PKG, "DimensionLookupDialog.TableItem.Insert.Label" ) );

              int idx = wKey.indexOfString( v.getName(), 2 );
              return idx < 0
                && !v.getName().equalsIgnoreCase( wTk.getText() )
                && !v.getName().equalsIgnoreCase( wVersion.getText() )
                && !v.getName().equalsIgnoreCase( wFromdate.getText() )
                && !v.getName().equalsIgnoreCase( wTodate.getText() );
            }
          } );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "DimensionLookupDialog.FailedToGetFields.DialogTitle" ),
        BaseMessages.getString( PKG, "DimensionLookupDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  // Set table "dimension field" and "technical key" drop downs
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
          // Ensure other table field dropdowns are refreshed fields when they
          // next get focus
          gotTableFields = false;
          if ( !Utils.isEmpty( tableName ) ) {
            DatabaseMeta ci = transMeta.findDatabase( connectionName );
            if ( ci != null ) {
              Database db = new Database( loggingObject, ci );
              try {
                db.connect();

                RowMetaInterface r =
                  db.getTableFieldsMeta(
                    transMeta.environmentSubstitute( schemaName ),
                    transMeta.environmentSubstitute( tableName ) );
                if ( null != r ) {
                  String[] fieldNames = r.getFieldNames();
                  if ( null != fieldNames ) {
                    for ( ColumnInfo colInfo : tableFieldColumns ) {
                      colInfo.setComboValues( fieldNames );
                    }
                    wTk.setItems( fieldNames );
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

  /**
   * Get the fields from the table in the database and use them as lookup keys. Only get the the fields which are not
   * yet in use as key, or in the field table. Also ignore technical key, version, fromdate, todate.
   */
  private void getLookup() {
    DatabaseMeta databaseMeta = transMeta.findDatabase( wConnection.getText() );
    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      db.shareVariablesWith( transMeta );
      try {
        db.connect();
        RowMetaInterface r = db.getTableFieldsMeta( wSchema.getText(), wTable.getText() );
        if ( r != null && !r.isEmpty() ) {
          BaseStepDialog.getFieldsFromPrevious(
            r, wUpIns, 2, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, new TableItemInsertListener() {
              public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
                int idx = wKey.indexOfString( v.getName(), 2 );
                return idx < 0
                  && !v.getName().equalsIgnoreCase( wTk.getText() )
                  && !v.getName().equalsIgnoreCase( wVersion.getText() )
                  && !v.getName().equalsIgnoreCase( wFromdate.getText() )
                  && !v.getName().equalsIgnoreCase( wTodate.getText() );
              }
            } );
        }
      } catch ( KettleException e ) {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.ErrorOccurred.DialogTitle" ) );
        mb.setMessage( BaseMessages.getString( PKG, "DimensionLookupDialog.ErrorOccurred.DialogMessage" )
          + Const.CR + e.getMessage() );
        mb.open();
      } finally {
        db.disconnect();
      }
    }
  }

  private void getFields() {
    if ( !gotPreviousFields ) {
      try {
        String field = wDatefield.getText();
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          wDatefield.setItems( r.getFieldNames() );

        }
        if ( field != null ) {
          wDatefield.setText( field );
        }
      } catch ( KettleException ke ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "DimensionLookupDialog.ErrorGettingFields.Title" ), BaseMessages
            .getString( PKG, "DimensionLookupDialog.ErrorGettingFields.Message" ), ke );
      }
      gotPreviousFields = true;
    }
  }

  private void getFieldsFromTable() {
    if ( !gotTableFields ) {
      if ( !Utils.isEmpty( wTable.getText() ) ) {
        DatabaseMeta ci = transMeta.findDatabase( wConnection.getText() );
        if ( ci != null ) {
          Database db = new Database( loggingObject, ci );
          try {
            db.connect();
            RowMetaInterface r =
              db.getTableFieldsMeta(
                transMeta.environmentSubstitute( wSchema.getText() ),
                transMeta.environmentSubstitute( wTable.getText() ) );
            if ( null != r ) {
              String[] fieldNames = r.getFieldNames();
              if ( null != fieldNames ) {
                // Version
                String version = wVersion.getText();
                wVersion.setItems( fieldNames );
                if ( version != null ) {
                  wVersion.setText( version );
                }
                // from date
                String fromdate = wFromdate.getText();
                wFromdate.setItems( fieldNames );
                if ( fromdate != null ) {
                  wFromdate.setText( fromdate );
                }
                // to date
                String todate = wTodate.getText();
                wTodate.setItems( fieldNames );
                if ( todate != null ) {
                  wTodate.setText( todate );
                }
                // tk
                String tk = wTk.getText();
                wTk.setItems( fieldNames );
                if ( tk != null ) {
                  wTk.setText( tk );
                }
                // AltStartDateField
                String sd = wAltStartDateField.getText();
                wAltStartDateField.setItems( fieldNames );
                if ( sd != null ) {
                  wAltStartDateField.setText( sd );
                }
              }
            }
          } catch ( Exception e ) {

            // ignore any errors here. drop downs will not be
            // filled, but no problem for the user
          }
        }
      }
      gotTableFields = true;
    }
  }

  /**
   * Get the fields from the previous step and use them as "keys". Only get the the fields which are not yet in use as
   * key, or in the field table. Also ignore technical key, version, fromdate, todate.
   */
  private void getKeys() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog.getFieldsFromPrevious(
          r, wKey, 2, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, new TableItemInsertListener() {
            public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
              int idx = wKey.indexOfString( v.getName(), 2 );
              return idx < 0
                && !v.getName().equalsIgnoreCase( wTk.getText() )
                && !v.getName().equalsIgnoreCase( wVersion.getText() )
                && !v.getName().equalsIgnoreCase( wFromdate.getText() )
                && !v.getName().equalsIgnoreCase( wTodate.getText() );
            }
          } );

        Table table = wKey.table;
        for ( int i = 0; i < r.size(); i++ ) {
          ValueMetaInterface v = r.getValueMeta( i );
          int idx = wKey.indexOfString( v.getName(), 2 );
          int idy = wUpIns.indexOfString( v.getName(), 2 );
          if ( idx < 0
            && idy < 0 && !v.getName().equalsIgnoreCase( wTk.getText() )
            && !v.getName().equalsIgnoreCase( wVersion.getText() )
            && !v.getName().equalsIgnoreCase( wFromdate.getText() )
            && !v.getName().equalsIgnoreCase( wTodate.getText() ) ) {
            TableItem ti = new TableItem( table, SWT.NONE );
            ti.setText( 1, v.getName() );
            ti.setText( 2, v.getName() );
            ti.setText( 3, v.getTypeDesc() );
          }
        }
        wKey.removeEmptyRows();
        wKey.setRowNums();
        wKey.optWidth( true );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "DimensionLookupDialog.FailedToGetFields.DialogTitle" ),
        BaseMessages.getString( PKG, "DimensionLookupDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  @Override
  protected Button createHelpButton( Shell shell, StepMeta stepMeta, PluginInterface plugin ) {
    return HelpUtils.createHelpButton( helpComp, HelpUtils.getHelpDialogTitle( plugin ), plugin );
  }

  // Generate code for create table...
  // Conversions done by Database
  // For Sybase ASE: don't keep everything in lowercase!
  private void create() {
    try {
      DimensionLookupMeta info = new DimensionLookupMeta();
      getInfo( info );

      String name = stepname; // new name might not yet be linked to other
      // steps!
      StepMeta stepinfo =
        new StepMeta( BaseMessages.getString( PKG, "DimensionLookupDialog.Stepinfo.Title" ), name, info );
      RowMetaInterface prev = transMeta.getPrevStepFields( stepname );

      String message = null;
      if ( Utils.isEmpty( info.getKeyField() ) ) {
        message = BaseMessages.getString( PKG, "DimensionLookupDialog.Error.NoTechnicalKeySpecified" );
      }
      if ( Utils.isEmpty( info.getTableName() ) ) {
        message = BaseMessages.getString( PKG, "DimensionLookupDialog.Error.NoTableNameSpecified" );
      }

      if ( message == null ) {
        SQLStatement sql = info.getSQLStatements( transMeta, stepinfo, prev, repository, metaStore );
        if ( !sql.hasError() ) {
          if ( sql.hasSQL() ) {
            SQLEditor sqledit =
              new SQLEditor( transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql
                .getSQL() );
            sqledit.open();
          } else {
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
            mb.setMessage( BaseMessages.getString( PKG, "DimensionLookupDialog.NoSQLNeeds.DialogMessage" ) );
            mb.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.NoSQLNeeds.DialogTitle" ) );
            mb.open();
          }
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( sql.getError() );
          mb.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.SQLError.DialogTitle" ) );
          mb.open();
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( message );
        mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
        mb.open();
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString(
        PKG, "DimensionLookupDialog.UnableToBuildSQLError.DialogMessage" ), BaseMessages.getString(
        PKG, "DimensionLookupDialog.UnableToBuildSQLError.DialogTitle" ), ke );
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
              PKG, "DimensionLookupDialog.AvailableSchemas.Title", wConnection.getText() ), BaseMessages
              .getString( PKG, "DimensionLookupDialog.AvailableSchemas.Message", wConnection.getText() ) );
          String d = dialog.open();
          if ( d != null ) {
            wSchema.setText( Const.NVL( d, "" ) );
            setTableFieldCombo();
          }

        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "DimensionLookupDialog.NoSchema.Error" ) );
          mb.setText( BaseMessages.getString( PKG, "DimensionLookupDialog.GetSchemas.Error" ) );
          mb.open();
        }
      } catch ( Exception e ) {
        new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages
          .getString( PKG, "DimensionLookupDialog.ErrorGettingSchemas" ), e );
      } finally {
        database.disconnect();
      }
    }
  }
}
