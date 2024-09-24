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

package org.pentaho.di.ui.trans.steps.selectvalues;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog for the Select Values step.
 */
public class SelectValuesDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = SelectValuesMeta.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wSelectTab, wRemoveTab, wMetaTab;

  private Composite wSelectComp, wRemoveComp, wMetaComp;
  private FormData fdSelectComp, fdRemoveComp, fdMetaComp;

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Label wlUnspecified;
  private Button wUnspecified;
  private FormData fdlUnspecified, fdUnspecified;

  private Label wlRemove;
  private TableView wRemove;
  private FormData fdlRemove, fdRemove;

  private Label wlMeta;
  private TableView wMeta;
  private FormData fdlMeta, fdMeta;

  private Button wGetSelect, wGetRemove, wGetMeta, wDoMapping;
  private FormData fdGetSelect, fdGetRemove, fdGetMeta;

  private SelectValuesMeta input;

  private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();

  private String[] charsets = null;

  /**
   * Fields from previous step
   */
  private RowMetaInterface prevFields;

  /**
   * Previous fields are read asynchonous because this might take some time and the user is able to do other things,
   * where he will not need the previous fields
   */
  private boolean bPreviousFieldsLoaded = false;

  private Map<String, Integer> inputFields;

  public SelectValuesDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (SelectValuesMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    SelectionListener lsSel = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
      }
    };

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        get();
      }
    };

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Shell.Label" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Stepname.Label" ) );
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

    // The folders!
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF SELECT TAB ///
    // ////////////////////////

    wSelectTab = new CTabItem( wTabFolder, SWT.NONE );
    wSelectTab.setText( BaseMessages.getString( PKG, "SelectValuesDialog.SelectTab.TabItem" ) );

    wSelectComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSelectComp );

    FormLayout selectLayout = new FormLayout();
    selectLayout.marginWidth = margin;
    selectLayout.marginHeight = margin;
    wSelectComp.setLayout( selectLayout );

    wlUnspecified = new Label( wSelectComp, SWT.RIGHT );
    wlUnspecified.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Unspecified.Label" ) );
    props.setLook( wlUnspecified );
    fdlUnspecified = new FormData();
    fdlUnspecified.left = new FormAttachment( 0, 0 );
    fdlUnspecified.right = new FormAttachment( middle, 0 );
    fdlUnspecified.bottom = new FormAttachment( 100, 0 );
    wlUnspecified.setLayoutData( fdlUnspecified );

    wUnspecified = new Button( wSelectComp, SWT.CHECK );
    props.setLook( wUnspecified );
    fdUnspecified = new FormData();
    fdUnspecified.left = new FormAttachment( middle, margin );
    fdUnspecified.right = new FormAttachment( 100, 0 );
    fdUnspecified.bottom = new FormAttachment( 100, 0 );
    wUnspecified.setLayoutData( fdUnspecified );
    wUnspecified.addSelectionListener( lsSel );

    wlFields = new Label( wSelectComp, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( 0, 0 );
    wlFields.setLayoutData( fdlFields );

    final int fieldsCols = 4;
    final int fieldsRows = input.getSelectFields().length;

    ColumnInfo[] colinf = new ColumnInfo[fieldsCols];
    colinf[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Fieldname" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(
          PKG, "SelectValuesDialog.ColumnInfo.Loading" ) }, false );
    colinf[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.RenameTo" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    colinf[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Length" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    colinf[3] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Precision" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );

    fieldColumns.add( colinf[0] );
    wFields =
      new TableView(
        transMeta, wSelectComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldsRows, lsMod, props );

    wGetSelect = new Button( wSelectComp, SWT.PUSH );
    wGetSelect.setText( BaseMessages.getString( PKG, "SelectValuesDialog.GetSelect.Button" ) );
    wGetSelect.addListener( SWT.Selection, lsGet );
    fdGetSelect = new FormData();
    fdGetSelect.right = new FormAttachment( 100, 0 );
    fdGetSelect.top = new FormAttachment( wlFields, margin );
    wGetSelect.setLayoutData( fdGetSelect );

    wDoMapping = new Button( wSelectComp, SWT.PUSH );
    wDoMapping.setText( BaseMessages.getString( PKG, "SelectValuesDialog.DoMapping.Button" ) );

    wDoMapping.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event arg0 ) {
        generateMappings();
      }
    } );

    fdGetSelect = new FormData();
    fdGetSelect.right = new FormAttachment( 100, 0 );
    fdGetSelect.top = new FormAttachment( wGetSelect, 0 );
    wDoMapping.setLayoutData( fdGetSelect );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( wGetSelect, -margin );
    fdFields.bottom = new FormAttachment( wUnspecified, -margin );
    wFields.setLayoutData( fdFields );

    fdSelectComp = new FormData();
    fdSelectComp.left = new FormAttachment( 0, 0 );
    fdSelectComp.top = new FormAttachment( 0, 0 );
    fdSelectComp.right = new FormAttachment( 100, 0 );
    fdSelectComp.bottom = new FormAttachment( 100, 0 );
    wSelectComp.setLayoutData( fdSelectComp );

    wSelectComp.layout();
    wSelectTab.setControl( wSelectComp );

    // ///////////////////////////////////////////////////////////
    // / END OF SELECT TAB
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////
    // START OF REMOVE TAB
    // ///////////////////////////////////////////////////////////
    wRemoveTab = new CTabItem( wTabFolder, SWT.NONE );
    wRemoveTab.setText( BaseMessages.getString( PKG, "SelectValuesDialog.RemoveTab.TabItem" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = margin;
    contentLayout.marginHeight = margin;

    wRemoveComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wRemoveComp );
    wRemoveComp.setLayout( contentLayout );

    wlRemove = new Label( wRemoveComp, SWT.NONE );
    wlRemove.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Remove.Label" ) );
    props.setLook( wlRemove );
    fdlRemove = new FormData();
    fdlRemove.left = new FormAttachment( 0, 0 );
    fdlRemove.top = new FormAttachment( 0, 0 );
    wlRemove.setLayoutData( fdlRemove );

    final int RemoveCols = 1;
    final int RemoveRows = input.getDeleteName().length;

    ColumnInfo[] colrem = new ColumnInfo[RemoveCols];
    colrem[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Fieldname" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(
          PKG, "SelectValuesDialog.ColumnInfo.Loading" ) }, false );
    fieldColumns.add( colrem[0] );
    wRemove =
      new TableView(
        transMeta, wRemoveComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colrem, RemoveRows, lsMod, props );

    wGetRemove = new Button( wRemoveComp, SWT.PUSH );
    wGetRemove.setText( BaseMessages.getString( PKG, "SelectValuesDialog.GetRemove.Button" ) );
    wGetRemove.addListener( SWT.Selection, lsGet );
    fdGetRemove = new FormData();
    fdGetRemove.right = new FormAttachment( 100, 0 );
    fdGetRemove.top = new FormAttachment( 50, 0 );
    wGetRemove.setLayoutData( fdGetRemove );

    fdRemove = new FormData();
    fdRemove.left = new FormAttachment( 0, 0 );
    fdRemove.top = new FormAttachment( wlRemove, margin );
    fdRemove.right = new FormAttachment( wGetRemove, -margin );
    fdRemove.bottom = new FormAttachment( 100, 0 );
    wRemove.setLayoutData( fdRemove );

    fdRemoveComp = new FormData();
    fdRemoveComp.left = new FormAttachment( 0, 0 );
    fdRemoveComp.top = new FormAttachment( 0, 0 );
    fdRemoveComp.right = new FormAttachment( 100, 0 );
    fdRemoveComp.bottom = new FormAttachment( 100, 0 );
    wRemoveComp.setLayoutData( fdRemoveComp );

    wRemoveComp.layout();
    wRemoveTab.setControl( wRemoveComp );

    // ///////////////////////////////////////////////////////////
    // / END OF REMOVE TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF META TAB ///
    // ////////////////////////

    wMetaTab = new CTabItem( wTabFolder, SWT.NONE );
    wMetaTab.setText( BaseMessages.getString( PKG, "SelectValuesDialog.MetaTab.TabItem" ) );

    wMetaComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wMetaComp );

    FormLayout metaLayout = new FormLayout();
    metaLayout.marginWidth = margin;
    metaLayout.marginHeight = margin;
    wMetaComp.setLayout( metaLayout );

    wlMeta = new Label( wMetaComp, SWT.NONE );
    wlMeta.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Meta.Label" ) );
    props.setLook( wlMeta );
    fdlMeta = new FormData();
    fdlMeta.left = new FormAttachment( 0, 0 );
    fdlMeta.top = new FormAttachment( 0, 0 );
    wlMeta.setLayoutData( fdlMeta );

    final int MetaRows = input.getMeta().length;

    ColumnInfo[] colmeta =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Fieldname" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(
            PKG, "SelectValuesDialog.ColumnInfo.Loading" ) }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Renameto" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Type" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getAllValueMetaNames(), false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Length" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Precision" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Storage.Label" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          new String[] {
            BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ), } ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Format" ),
          ColumnInfo.COLUMN_TYPE_FORMAT, 3 ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.DateLenient" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          new String[] {
            BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ), } ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.DateFormatLocale" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, EnvUtil.getLocaleList() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.DateFormatTimeZone" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, EnvUtil.getTimeZones() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.LenientStringToNumber" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
            BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ), } ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Encoding" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, getCharsets(), false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Decimal" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Grouping" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Currency" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ), };
    colmeta[5].setToolTip( BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Storage.Tooltip" ) );
    fieldColumns.add( colmeta[0] );
    wMeta =
      new TableView(
        transMeta, wMetaComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colmeta, MetaRows, lsMod, props );

    wGetMeta = new Button( wMetaComp, SWT.PUSH );
    wGetMeta.setText( BaseMessages.getString( PKG, "SelectValuesDialog.GetMeta.Button" ) );
    wGetMeta.addListener( SWT.Selection, lsGet );
    fdGetMeta = new FormData();
    fdGetMeta.right = new FormAttachment( 100, 0 );
    fdGetMeta.top = new FormAttachment( 50, 0 );
    wGetMeta.setLayoutData( fdGetMeta );

    fdMeta = new FormData();
    fdMeta.left = new FormAttachment( 0, 0 );
    fdMeta.top = new FormAttachment( wlMeta, margin );
    fdMeta.right = new FormAttachment( wGetMeta, -margin );
    fdMeta.bottom = new FormAttachment( 100, 0 );
    wMeta.setLayoutData( fdMeta );

    fdMetaComp = new FormData();
    fdMetaComp.left = new FormAttachment( 0, 0 );
    fdMetaComp.top = new FormAttachment( 0, 0 );
    fdMetaComp.right = new FormAttachment( 100, 0 );
    fdMetaComp.bottom = new FormAttachment( 100, 0 );
    wMetaComp.setLayoutData( fdMetaComp );

    wMetaComp.layout();
    wMetaTab.setControl( wMetaComp );

    // ///////////////////////////////////////////////////////////
    // / END OF META TAB
    // ///////////////////////////////////////////////////////////

    fdTabFolder = new FormData();
    fdTabFolder.width = 680;
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // ///////////////////////////////////////////////////////////
    // / END OF TAB FOLDER
    // ///////////////////////////////////////////////////////////

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
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

    //
    // Search the fields in the background
    //

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );
            prevFields = row;
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

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( changed );

    setComboValues();

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
          String msg = BaseMessages.getString( PKG, "SelectValuesDialog.DoMapping.UnableToFindInput" );
          logError( msg );
        }
        String[] prevStepFieldNames = prevFields != null ? prevFields.getFieldNames() : new String[0];
        Arrays.sort( prevStepFieldNames );
        bPreviousFieldsLoaded = true;
        for ( int i = 0; i < fieldColumns.size(); i++ ) {
          ColumnInfo colInfo = fieldColumns.get( i );
          colInfo.setComboValues( prevStepFieldNames );
        }
      }
    };
    shell.getDisplay().asyncExec( fieldLoader );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wTabFolder.setSelection( 0 ); // Default

    /*
     * Select fields
     */
    if ( input.getSelectFields() != null && input.getSelectFields().length > 0 ) {
      for ( int i = 0; i < input.getSelectFields().length; i++ ) {
        TableItem item = wFields.table.getItem( i );
        if ( input.getSelectFields()[i].getName() != null ) {
          item.setText( 1, input.getSelectFields()[i].getName() );
        }
        if ( input.getSelectFields()[i].getRename() != null && !input.getSelectFields()[i].getRename().equals( input
            .getSelectFields()[i].getName() ) ) {
          item.setText( 2, input.getSelectFields()[i].getRename() );
        }
        item.setText( 3, input.getSelectFields()[i].getLength() < 0 ? "" : "" + input.getSelectFields()[i]
            .getLength() );
        item.setText( 4, input.getSelectFields()[i].getPrecision() < 0 ? "" : "" + input.getSelectFields()[i]
            .getPrecision() );
      }
      wFields.setRowNums();
      wFields.optWidth( true );
      wTabFolder.setSelection( 0 );
    }
    wUnspecified.setSelection( input.isSelectingAndSortingUnspecifiedFields() );

    /*
     * Remove certain fields...
     */
    if ( input.getDeleteName() != null && input.getDeleteName().length > 0 ) {
      for ( int i = 0; i < input.getDeleteName().length; i++ ) {
        TableItem item = wRemove.table.getItem( i );
        if ( input.getDeleteName()[i] != null ) {
          item.setText( 1, input.getDeleteName()[i] );
        }
      }
      wRemove.setRowNums();
      wRemove.optWidth( true );
      wTabFolder.setSelection( 1 );
    }

    /*
     * Change the meta-data of certain fields
     */
    if ( !Utils.isEmpty( input.getMeta() ) ) {
      for ( int i = 0; i < input.getMeta().length; i++ ) {
        SelectMetadataChange change = input.getMeta()[i];

        TableItem item = wMeta.table.getItem( i );
        int index = 1;
        item.setText( index++, Const.NVL( change.getName(), "" ) );
        if ( change.getRename() != null && !change.getRename().equals( change.getName() ) ) {
          item.setText( index++, change.getRename() );
        } else {
          index++;
        }
        item.setText( index++, ValueMetaFactory.getValueMetaName( change.getType() ) );
        item.setText( index++, change.getLength() < 0 ? "" : "" + change.getLength() );
        item.setText( index++, change.getPrecision() < 0 ? "" : "" + change.getPrecision() );
        item.setText( index++, change.getStorageType() == ValueMetaInterface.STORAGE_TYPE_NORMAL ? BaseMessages
          .getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString( PKG, "System.Combo.No" ) );
        item.setText( index++, Const.NVL( change.getConversionMask(), "" ) );
        item
          .setText( index++, change.isDateFormatLenient()
            ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString(
              PKG, "System.Combo.No" ) );
        item
          .setText( index++, change.getDateFormatLocale() == null ? "" : change.getDateFormatLocale().toString() );
        item.setText( index++, change.getDateFormatTimeZone() == null ? "" : change
          .getDateFormatTimeZone().toString() );
        item
          .setText( index++, change.isLenientStringToNumber()
            ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString(
              PKG, "System.Combo.No" ) );
        item.setText( index++, Const.NVL( change.getEncoding(), "" ) );
        item.setText( index++, Const.NVL( change.getDecimalSymbol(), "" ) );
        item.setText( index++, Const.NVL( change.getGroupingSymbol(), "" ) );
        item.setText( index++, Const.NVL( change.getCurrencySymbol(), "" ) );
      }
      wMeta.setRowNums();
      wMeta.optWidth( true );
      wTabFolder.setSelection( 2 );
    }

    wStepname.setFocus();
    wStepname.selectAll();
  }

  private String[] getCharsets() {
    if ( charsets == null ) {
      Collection<Charset> charsetCol = Charset.availableCharsets().values();
      charsets = new String[charsetCol.size()];
      int i = 0;
      for ( Charset charset : charsetCol ) {
        charsets[i++] = charset.displayName();
      }
    }
    return charsets;
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

    stepname = wStepname.getText(); // return value

    // copy info to meta class (input)

    int nrfields = wFields.nrNonEmpty();
    int nrremove = wRemove.nrNonEmpty();
    int nrmeta = wMeta.nrNonEmpty();

    input.allocate( nrfields, nrremove, nrmeta );

    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      input.getSelectFields()[i].setName( item.getText( 1 ) );
      input.getSelectFields()[i].setRename( item.getText( 2 ) );
      if ( input.getSelectFields()[i].getRename() == null || input.getSelectFields()[i].getName().length() == 0 ) {
        input.getSelectFields()[i].setRename( input.getSelectFields()[i].getName() );
      }
      input.getSelectFields()[i].setLength( Const.toInt( item.getText( 3 ), -2 ) );
      input.getSelectFields()[i].setPrecision( Const.toInt( item.getText( 4 ), -2 ) );

      if ( input.getSelectFields()[i].getLength() < -2 ) {
        input.getSelectFields()[i].setLength( -2 );
      }
      if ( input.getSelectFields()[i].getPrecision() < -2 ) {
        input.getSelectFields()[i].setPrecision( -2 );
      }
    }
    input.setSelectingAndSortingUnspecifiedFields( wUnspecified.getSelection() );

    for ( int i = 0; i < nrremove; i++ ) {
      TableItem item = wRemove.getNonEmpty( i );
      input.getDeleteName()[i] = item.getText( 1 );
    }

    for ( int i = 0; i < nrmeta; i++ ) {
      SelectMetadataChange change = new SelectMetadataChange( input );
      input.getMeta()[i] = change;

      TableItem item = wMeta.getNonEmpty( i );

      int index = 1;
      change.setName( item.getText( index++ ) );
      change.setRename( item.getText( index++ ) );
      if ( Utils.isEmpty( change.getRename() ) ) {
        change.setRename( change.getName() );
      }
      change.setType( ValueMetaFactory.getIdForValueMeta( item.getText( index++ ) ) );

      change.setLength( Const.toInt( item.getText( index++ ), -2 ) );
      change.setPrecision( Const.toInt( item.getText( index++ ), -2 ) );

      if ( change.getLength() < -2 ) {
        change.setLength( -2 );
      }
      if ( change.getPrecision() < -2 ) {
        change.setPrecision( -2 );
      }
      if ( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( index++ ) ) ) {
        change.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
      }

      change.setConversionMask( item.getText( index++ ) );
      // If DateFormatLenient is anything but Yes (including blank) then it is false
      change.setDateFormatLenient( item.getText( index++ ).equalsIgnoreCase(
        BaseMessages.getString( PKG, "System.Combo.Yes" ) ) ? true : false );
      change.setDateFormatLocale( item.getText( index++ ) );
      change.setDateFormatTimeZone( item.getText( index++ ) );
      change.setLenientStringToNumber( item.getText( index++ ).equalsIgnoreCase(
        BaseMessages.getString( PKG, "System.Combo.Yes" ) ) ? true : false );
      change.setEncoding( item.getText( index++ ) );
      change.setDecimalSymbol( item.getText( index++ ) );
      change.setGroupingSymbol( item.getText( index++ ) );
      change.setCurrencySymbol( item.getText( index++ ) );
    }
    dispose();
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        switch ( wTabFolder.getSelectionIndex() ) {
          case 0:
            BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, null );
            break;
          case 1:
            BaseStepDialog.getFieldsFromPrevious( r, wRemove, 1, new int[] { 1 }, new int[] {}, -1, -1, null );
            break;
          case 2:
            BaseStepDialog.getFieldsFromPrevious( r, wMeta, 1, new int[] { 1 }, new int[] {}, 4, 5, null );
            break;
          default:
            break;
        }
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "SelectValuesDialog.FailedToGetFields.DialogTitle" ), BaseMessages
          .getString( PKG, "SelectValuesDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  /**
   * Reads in the fields from the previous steps and from the ONE next step and opens an EnterMappingDialog with this
   * information. After the user did the mapping, those information is put into the Select/Rename table.
   */
  private void generateMappings() {
    if ( !bPreviousFieldsLoaded ) {
      MessageDialog.openError(
        shell, BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Loading" ), BaseMessages.getString(
          PKG, "SelectValuesDialog.ColumnInfo.Loading" ) );
      return;
    }
    if ( ( wRemove.getItemCount() > 0 ) || ( wMeta.getItemCount() > 0 ) ) {
      for ( int i = 0; i < wRemove.getItemCount(); i++ ) {
        String[] columns = wRemove.getItem( i );
        for ( int a = 0; a < columns.length; a++ ) {
          if ( columns[a].length() > 0 ) {
            MessageDialog.openError( shell, BaseMessages.getString(
              PKG, "SelectValuesDialog.DoMapping.NoDeletOrMetaTitle" ), BaseMessages.getString(
              PKG, "SelectValuesDialog.DoMapping.NoDeletOrMeta" ) );
            return;
          }
        }
      }
      for ( int i = 0; i < wMeta.getItemCount(); i++ ) {
        String[] columns = wMeta.getItem( i );
        for ( int a = 0; a < columns.length; a++ ) {
          String col = columns[a];
          if ( col.length() > 0 ) {
            MessageDialog.openError( shell, BaseMessages.getString(
              PKG, "SelectValuesDialog.DoMapping.NoDeletOrMetaTitle" ), BaseMessages.getString(
              PKG, "SelectValuesDialog.DoMapping.NoDeletOrMeta" ) );
            return;
          }
        }
      }
    }

    RowMetaInterface nextStepRequiredFields = null;

    StepMeta stepMeta = new StepMeta( stepname, input );
    List<StepMeta> nextSteps = transMeta.findNextSteps( stepMeta );
    if ( nextSteps.size() == 0 || nextSteps.size() > 1 ) {
      MessageDialog.openError(
        shell, BaseMessages.getString( PKG, "SelectValuesDialog.DoMapping.NoNextStepTitle" ), BaseMessages
          .getString( PKG, "SelectValuesDialog.DoMapping.NoNextStep" ) );
      return;
    }
    StepMeta outputStepMeta = nextSteps.get( 0 );
    StepMetaInterface stepMetaInterface = outputStepMeta.getStepMetaInterface();
    try {
      nextStepRequiredFields = stepMetaInterface.getRequiredFields( transMeta );
    } catch ( KettleException e ) {
      logError( BaseMessages.getString( PKG, "SelectValuesDialog.DoMapping.UnableToFindOutput" ) );
      nextStepRequiredFields = new RowMeta();
    }

    String[] inputNames = new String[prevFields.size()];
    for ( int i = 0; i < prevFields.size(); i++ ) {
      ValueMetaInterface value = prevFields.getValueMeta( i );
      inputNames[i] = value.getName() + EnterMappingDialog.STRING_ORIGIN_SEPARATOR + value.getOrigin() + ")";
    }

    String[] outputNames = new String[nextStepRequiredFields.size()];
    for ( int i = 0; i < nextStepRequiredFields.size(); i++ ) {
      outputNames[i] = nextStepRequiredFields.getValueMeta( i ).getName();
    }

    String[] selectName = new String[wFields.getItemCount()];
    String[] selectRename = new String[wFields.getItemCount()];
    for ( int i = 0; i < wFields.getItemCount(); i++ ) {
      selectName[i] = wFields.getItem( i, 1 );
      selectRename[i] = wFields.getItem( i, 2 );
    }

    List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
    StringBuilder missingFields = new StringBuilder();
    for ( int i = 0; i < selectName.length; i++ ) {
      String valueName = selectName[i];
      String valueRename = selectRename[i];
      int inIndex = prevFields.indexOfValue( valueName );
      if ( inIndex < 0 ) {
        missingFields.append( Const.CR + "   " + valueName + " --> " + valueRename );
        continue;
      }
      if ( null == valueRename || valueRename.equals( "" ) ) {
        valueRename = valueName;
      }
      int outIndex = nextStepRequiredFields.indexOfValue( valueRename );
      if ( outIndex < 0 ) {
        missingFields.append( Const.CR + "   " + valueName + " --> " + valueRename );
        continue;
      }
      SourceToTargetMapping mapping = new SourceToTargetMapping( inIndex, outIndex );
      mappings.add( mapping );
    }
    // show a confirm dialog if some misconfiguration was found
    if ( missingFields.length() > 0 ) {
      MessageDialog.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      boolean goOn =
        MessageDialog.openConfirm( shell,
          BaseMessages.getString( PKG, "SelectValuesDialog.DoMapping.SomeFieldsNotFoundTitle" ),
          BaseMessages.getString( PKG, "SelectValuesDialog.DoMapping.SomeFieldsNotFound", missingFields.toString() ) );
      if ( !goOn ) {
        return;
      }
    }
    EnterMappingDialog d =
      new EnterMappingDialog( SelectValuesDialog.this.shell, inputNames, outputNames, mappings );
    mappings = d.open();

    // mappings == null if the user pressed cancel
    //
    if ( mappings != null ) {
      wFields.table.removeAll();
      wFields.table.setItemCount( mappings.size() );
      for ( int i = 0; i < mappings.size(); i++ ) {
        SourceToTargetMapping mapping = mappings.get( i );
        TableItem item = wFields.table.getItem( i );
        item.setText( 1, prevFields.getValueMeta( mapping.getSourcePosition() ).getName() );
        item.setText( 2, outputNames[mapping.getTargetPosition()] );
      }
      wFields.setRowNums();
      wFields.optWidth( true );
      wTabFolder.setSelection( 0 );
    }
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

    bPreviousFieldsLoaded = true;
    for ( int i = 0; i < fieldColumns.size(); i++ ) {
      ColumnInfo colInfo = fieldColumns.get( i );
      colInfo.setComboValues( fieldNames );
    }
  }
}
