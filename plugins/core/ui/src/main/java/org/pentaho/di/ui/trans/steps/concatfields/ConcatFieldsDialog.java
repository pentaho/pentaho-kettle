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

package org.pentaho.di.ui.trans.steps.concatfields;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
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
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.concatfields.ConcatFieldsMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/*
 * ConcatFieldsDialog
 *
 * derived form TextFileOutputDialog
 *
 * @author jb
 * @since 2012-08-31
 *
 */
@PluginDialog( id = "ConcatFields", pluginType = PluginDialog.PluginType.STEP, image = "ConcatFields.svg",
  documentationUrl = "http://wiki.pentaho.com/display/EAI/Concat+Fields" )
public class ConcatFieldsDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ConcatFieldsDialog.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wAdvancedTab, wFieldsTab;

  private FormData fdAdvancedComp, fdFieldsComp;

  private Label wlTargetFieldName;
  private TextVar wTargetFieldName;
  private FormData fdlTargetFieldName, fdTargetFieldName;

  private Label wlTargetFieldLength;
  private Text wTargetFieldLength;
  private FormData fdlTargetFieldLength, fdTargetFieldLength;

  private Label wlRemoveSelectedFields;
  private Button wRemoveSelectedFields;
  private FormData fdlRemoveSelectedFields, fdRemoveSelectedFields;

  private Label wlSeparator;
  private Button wbSeparator;
  private TextVar wSeparator;
  private FormData fdlSeparator, fdbSeparator, fdSeparator;

  private Label wlEnclosure;
  private TextVar wEnclosure;
  private FormData fdlEnclosure, fdEnclosure;

  private Label wlEndedLine;
  private Text wEndedLine;
  private FormData fdlEndedLine, fdEndedLine;

  private Label wlEnclForced;
  private Button wEnclForced;
  private FormData fdlEnclForced, fdEnclForced;

  private Label wlDisableEnclosureFix;
  private Button wDisableEnclosureFix;
  private FormData fdlDisableEnclosureFix, fdDisableEnclosureFix;

  private Label wlHeader;
  private Button wHeader;
  private FormData fdlHeader, fdHeader;

  private Label wlFooter;
  private Button wFooter;
  private FormData fdlFooter, fdFooter;

  private Label wlEncoding;
  private CCombo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  private Label wlPad;
  private Button wPad;
  private FormData fdlPad, fdPad;

  private Label wlFastDump;
  private Button wFastDump;
  private FormData fdlFastDump, fdFastDump;

  private Label wlSplitEvery;
  private Text wSplitEvery;
  private FormData fdlSplitEvery, fdSplitEvery;

  private TableView wFields;
  private FormData fdFields;

  private ConcatFieldsMeta input;

  private Button wMinWidth;
  private Listener lsMinWidth;
  private boolean gotEncodings = false;

  private ColumnInfo[] colinf;

  private Map<String, Integer> inputFields;

  public ConcatFieldsDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (ConcatFieldsMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.DialogTitle" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( middle, -margin );
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

    // TargetFieldName line
    wlTargetFieldName = new Label( shell, SWT.RIGHT );
    wlTargetFieldName.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.TargetFieldName.Label" ) );
    wlTargetFieldName.setToolTipText( BaseMessages.getString( PKG, "ConcatFieldsDialog.TargetFieldName.Tooltip" ) );
    props.setLook( wlTargetFieldName );
    fdlTargetFieldName = new FormData();
    fdlTargetFieldName.left = new FormAttachment( 0, 0 );
    fdlTargetFieldName.top = new FormAttachment( wStepname, margin );
    fdlTargetFieldName.right = new FormAttachment( middle, -margin );
    wlTargetFieldName.setLayoutData( fdlTargetFieldName );
    wTargetFieldName = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTargetFieldName.setText( "" );
    props.setLook( wTargetFieldName );
    wTargetFieldName.addModifyListener( lsMod );
    fdTargetFieldName = new FormData();
    fdTargetFieldName.left = new FormAttachment( middle, 0 );
    fdTargetFieldName.top = new FormAttachment( wStepname, margin );
    fdTargetFieldName.right = new FormAttachment( 100, 0 );
    wTargetFieldName.setLayoutData( fdTargetFieldName );

    // TargetFieldLength line
    wlTargetFieldLength = new Label( shell, SWT.RIGHT );
    wlTargetFieldLength.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.TargetFieldLength.Label" ) );
    wlTargetFieldLength.setToolTipText( BaseMessages.getString(
      PKG, "ConcatFieldsDialog.TargetFieldLength.Tooltip" ) );
    props.setLook( wlTargetFieldLength );
    fdlTargetFieldLength = new FormData();
    fdlTargetFieldLength.left = new FormAttachment( 0, 0 );
    fdlTargetFieldLength.top = new FormAttachment( wTargetFieldName, margin );
    fdlTargetFieldLength.right = new FormAttachment( middle, -margin );
    wlTargetFieldLength.setLayoutData( fdlTargetFieldLength );
    wTargetFieldLength = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTargetFieldLength );
    wTargetFieldLength.addModifyListener( lsMod );
    fdTargetFieldLength = new FormData();
    fdTargetFieldLength.left = new FormAttachment( middle, 0 );
    fdTargetFieldLength.top = new FormAttachment( wTargetFieldName, margin );
    fdTargetFieldLength.right = new FormAttachment( 100, 0 );
    wTargetFieldLength.setLayoutData( fdTargetFieldLength );

    // Separator
    wlSeparator = new Label( shell, SWT.RIGHT );
    wlSeparator.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.Separator.Label" ) );
    props.setLook( wlSeparator );
    fdlSeparator = new FormData();
    fdlSeparator.left = new FormAttachment( 0, 0 );
    fdlSeparator.top = new FormAttachment( wTargetFieldLength, margin );
    fdlSeparator.right = new FormAttachment( middle, -margin );
    wlSeparator.setLayoutData( fdlSeparator );

    wbSeparator = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbSeparator );
    wbSeparator.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.Separator.Button" ) );
    fdbSeparator = new FormData();
    fdbSeparator.right = new FormAttachment( 100, 0 );
    fdbSeparator.top = new FormAttachment( wTargetFieldLength, margin );
    wbSeparator.setLayoutData( fdbSeparator );
    wbSeparator.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent se ) {
        // wSeparator.insert("\t");
        wSeparator.getTextWidget().insert( "\t" );
      }
    } );

    wSeparator = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSeparator );
    wSeparator.addModifyListener( lsMod );
    fdSeparator = new FormData();
    fdSeparator.left = new FormAttachment( middle, 0 );
    fdSeparator.top = new FormAttachment( wTargetFieldLength, margin );
    fdSeparator.right = new FormAttachment( wbSeparator, -margin );
    wSeparator.setLayoutData( fdSeparator );

    // Enclosure line...
    wlEnclosure = new Label( shell, SWT.RIGHT );
    wlEnclosure.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.Enclosure.Label" ) );
    props.setLook( wlEnclosure );
    fdlEnclosure = new FormData();
    fdlEnclosure.left = new FormAttachment( 0, 0 );
    fdlEnclosure.top = new FormAttachment( wSeparator, margin );
    fdlEnclosure.right = new FormAttachment( middle, -margin );
    wlEnclosure.setLayoutData( fdlEnclosure );
    wEnclosure = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEnclosure );
    wEnclosure.addModifyListener( lsMod );
    fdEnclosure = new FormData();
    fdEnclosure.left = new FormAttachment( middle, 0 );
    fdEnclosure.top = new FormAttachment( wSeparator, margin );
    fdEnclosure.right = new FormAttachment( 100, 0 );
    wEnclosure.setLayoutData( fdEnclosure );

    // ////////////////////////
    // START OF TABS
    // /

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );

    // Fields tab...
    //
    wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.FieldsTab.TabTitle" ) );

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = Const.FORM_MARGIN;
    fieldsLayout.marginHeight = Const.FORM_MARGIN;

    Composite wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    wFieldsComp.setLayout( fieldsLayout );
    props.setLook( wFieldsComp );

    wGet = new Button( wFieldsComp, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );
    wGet.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.GetFields" ) );

    wMinWidth = new Button( wFieldsComp, SWT.PUSH );
    wMinWidth.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.MinWidth.Button" ) );
    wMinWidth.setToolTipText( BaseMessages.getString( PKG, "ConcatFieldsDialog.MinWidth.Tooltip" ) );

    setButtonPositions( new Button[] { wGet, wMinWidth }, margin, null );

    final int FieldsCols = 10;
    final int FieldsRows = input.getOutputFields().length;

    // Prepare a list of possible formats...
    String[] dats = Const.getDateFormats();
    String[] nums = Const.getNumberFormats();
    int totsize = dats.length + nums.length;
    String[] formats = new String[ totsize ];
    for ( int x = 0; x < dats.length; x++ ) {
      formats[ x ] = dats[ x ];
    }
    for ( int x = 0; x < nums.length; x++ ) {
      formats[ dats.length + x ] = nums[ x ];
    }

    colinf = new ColumnInfo[ FieldsCols ];
    colinf[ 0 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.NameColumn.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        new String[] { "" }, false );
    colinf[ 1 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.TypeColumn.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        ValueMetaFactory.getValueMetaNames() );
    colinf[ 2 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.FormatColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, formats );
    colinf[ 3 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.LengthColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    colinf[ 4 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.PrecisionColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[ 5 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.CurrencyColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[ 6 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.DecimalColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    colinf[ 7 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.GroupColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    colinf[ 8 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.TrimTypeColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaString.trimTypeDesc, true );
    colinf[ 9 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConcatFieldsDialog.NullColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );

    wFields =
      new TableView(
        transMeta, wFieldsComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wGet, -margin );
    wFields.setLayoutData( fdFields );

    //
    // Search the fields in the background

    final Runnable runnable = new Runnable() {
      @Override
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

    fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment( 0, 0 );
    fdFieldsComp.top = new FormAttachment( 0, 0 );
    fdFieldsComp.right = new FormAttachment( 100, 0 );
    fdFieldsComp.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fdFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );

    // ////////////////////////
    // START OF ADVANCED TAB///
    // /
    wAdvancedTab = new CTabItem( wTabFolder, SWT.NONE );
    wAdvancedTab.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.AdvancedTab.TabTitle" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    Composite wContentComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // Remove selected fields?
    wlRemoveSelectedFields = new Label( wContentComp, SWT.RIGHT );
    wlRemoveSelectedFields
      .setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.RemoveSelectedFields.Label" ) );
    wlRemoveSelectedFields.setToolTipText( BaseMessages.getString(
      PKG, "ConcatFieldsDialog.RemoveSelectedFields.Tooltip" ) );
    props.setLook( wlRemoveSelectedFields );
    fdlRemoveSelectedFields = new FormData();
    fdlRemoveSelectedFields.left = new FormAttachment( 0, 0 );
    fdlRemoveSelectedFields.top = new FormAttachment( 0, 0 );
    fdlRemoveSelectedFields.right = new FormAttachment( middle, -margin );
    wlRemoveSelectedFields.setLayoutData( fdlRemoveSelectedFields );
    wRemoveSelectedFields = new Button( wContentComp, SWT.CHECK );
    props.setLook( wRemoveSelectedFields );
    fdRemoveSelectedFields = new FormData();
    fdRemoveSelectedFields.left = new FormAttachment( middle, 0 );
    fdRemoveSelectedFields.top = new FormAttachment( 0, 0 );
    fdRemoveSelectedFields.right = new FormAttachment( 100, 0 );
    wRemoveSelectedFields.setLayoutData( fdRemoveSelectedFields );
    wRemoveSelectedFields.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Enclosure forced line
    wlEnclForced = new Label( wContentComp, SWT.RIGHT );
    wlEnclForced.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.EnclForced.Label" ) );
    props.setLook( wlEnclForced );
    fdlEnclForced = new FormData();
    fdlEnclForced.left = new FormAttachment( 0, 0 );
    fdlEnclForced.top = new FormAttachment( wRemoveSelectedFields, margin );
    fdlEnclForced.right = new FormAttachment( middle, -margin );
    wlEnclForced.setLayoutData( fdlEnclForced );
    wEnclForced = new Button( wContentComp, SWT.CHECK );
    props.setLook( wEnclForced );
    fdEnclForced = new FormData();
    fdEnclForced.left = new FormAttachment( middle, 0 );
    fdEnclForced.top = new FormAttachment( wRemoveSelectedFields, margin );
    fdEnclForced.right = new FormAttachment( 100, 0 );
    wEnclForced.setLayoutData( fdEnclForced );
    wEnclForced.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Disable Enclosure Fix
    wlDisableEnclosureFix = new Label( wContentComp, SWT.RIGHT );
    wlDisableEnclosureFix.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.DisableEnclosureFix.Label" ) );
    props.setLook( wlDisableEnclosureFix );
    fdlDisableEnclosureFix = new FormData();
    fdlDisableEnclosureFix.left = new FormAttachment( 0, 0 );
    fdlDisableEnclosureFix.top = new FormAttachment( wEnclForced, margin );
    fdlDisableEnclosureFix.right = new FormAttachment( middle, -margin );
    wlDisableEnclosureFix.setLayoutData( fdlDisableEnclosureFix );
    wDisableEnclosureFix = new Button( wContentComp, SWT.CHECK );
    props.setLook( wDisableEnclosureFix );
    fdDisableEnclosureFix = new FormData();
    fdDisableEnclosureFix.left = new FormAttachment( middle, 0 );
    fdDisableEnclosureFix.top = new FormAttachment( wEnclForced, margin );
    fdDisableEnclosureFix.right = new FormAttachment( 100, 0 );
    wDisableEnclosureFix.setLayoutData( fdDisableEnclosureFix );
    wDisableEnclosureFix.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Header line
    wlHeader = new Label( wContentComp, SWT.RIGHT );
    wlHeader.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.Header.Label" ) );
    props.setLook( wlHeader );
    fdlHeader = new FormData();
    fdlHeader.left = new FormAttachment( 0, 0 );
    fdlHeader.top = new FormAttachment( wDisableEnclosureFix, margin );
    fdlHeader.right = new FormAttachment( middle, -margin );
    wlHeader.setLayoutData( fdlHeader );
    wHeader = new Button( wContentComp, SWT.CHECK );
    props.setLook( wHeader );
    fdHeader = new FormData();
    fdHeader.left = new FormAttachment( middle, 0 );
    fdHeader.top = new FormAttachment( wDisableEnclosureFix, margin );
    fdHeader.right = new FormAttachment( 100, 0 );
    wHeader.setLayoutData( fdHeader );
    wHeader.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Footer line
    wlFooter = new Label( wContentComp, SWT.RIGHT );
    wlFooter.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.Footer.Label" ) );
    props.setLook( wlFooter );
    fdlFooter = new FormData();
    fdlFooter.left = new FormAttachment( 0, 0 );
    fdlFooter.top = new FormAttachment( wHeader, margin );
    fdlFooter.right = new FormAttachment( middle, -margin );
    wlFooter.setLayoutData( fdlFooter );
    wFooter = new Button( wContentComp, SWT.CHECK );
    props.setLook( wFooter );
    fdFooter = new FormData();
    fdFooter.left = new FormAttachment( middle, 0 );
    fdFooter.top = new FormAttachment( wHeader, margin );
    fdFooter.right = new FormAttachment( 100, 0 );
    wFooter.setLayoutData( fdFooter );
    wFooter.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Encoding line
    wlEncoding = new Label( wContentComp, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.Encoding.Label" ) );
    props.setLook( wlEncoding );
    fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.top = new FormAttachment( wFooter, margin );
    fdlEncoding.right = new FormAttachment( middle, -margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new CCombo( wContentComp, SWT.BORDER | SWT.READ_ONLY );
    wEncoding.setEditable( true );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    fdEncoding = new FormData();
    fdEncoding.left = new FormAttachment( middle, 0 );
    fdEncoding.top = new FormAttachment( wFooter, margin );
    fdEncoding.right = new FormAttachment( 100, 0 );
    wEncoding.setLayoutData( fdEncoding );
    wEncoding.addFocusListener( new FocusListener() {
      @Override
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      @Override
      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setEncodings();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Pad line
    wlPad = new Label( wContentComp, SWT.RIGHT );
    wlPad.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.Pad.Label" ) );
    props.setLook( wlPad );
    fdlPad = new FormData();
    fdlPad.left = new FormAttachment( 0, 0 );
    fdlPad.top = new FormAttachment( wEncoding, margin );
    fdlPad.right = new FormAttachment( middle, -margin );
    wlPad.setLayoutData( fdlPad );
    wPad = new Button( wContentComp, SWT.CHECK );
    props.setLook( wPad );
    fdPad = new FormData();
    fdPad.left = new FormAttachment( middle, 0 );
    fdPad.top = new FormAttachment( wEncoding, margin );
    fdPad.right = new FormAttachment( 100, 0 );
    wPad.setLayoutData( fdPad );
    wPad.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Fast Dump line
    wlFastDump = new Label( wContentComp, SWT.RIGHT );
    wlFastDump.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.FastDump.Label" ) );
    props.setLook( wlFastDump );
    fdlFastDump = new FormData();
    fdlFastDump.left = new FormAttachment( 0, 0 );
    fdlFastDump.top = new FormAttachment( wPad, margin );
    fdlFastDump.right = new FormAttachment( middle, -margin );
    wlFastDump.setLayoutData( fdlFastDump );
    wFastDump = new Button( wContentComp, SWT.CHECK );
    props.setLook( wFastDump );
    fdFastDump = new FormData();
    fdFastDump.left = new FormAttachment( middle, 0 );
    fdFastDump.top = new FormAttachment( wPad, margin );
    fdFastDump.right = new FormAttachment( 100, 0 );
    wFastDump.setLayoutData( fdFastDump );
    wFastDump.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // Split Every line
    wlSplitEvery = new Label( wContentComp, SWT.RIGHT );
    wlSplitEvery.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.SplitEvery.Label" ) );
    props.setLook( wlSplitEvery );
    fdlSplitEvery = new FormData();
    fdlSplitEvery.left = new FormAttachment( 0, 0 );
    fdlSplitEvery.top = new FormAttachment( wFastDump, margin );
    fdlSplitEvery.right = new FormAttachment( middle, -margin );
    wlSplitEvery.setLayoutData( fdlSplitEvery );
    wSplitEvery = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSplitEvery );
    wSplitEvery.addModifyListener( lsMod );
    fdSplitEvery = new FormData();
    fdSplitEvery.left = new FormAttachment( middle, 0 );
    fdSplitEvery.top = new FormAttachment( wFastDump, margin );
    fdSplitEvery.right = new FormAttachment( 100, 0 );
    wSplitEvery.setLayoutData( fdSplitEvery );

    // Bruise:
    wlEndedLine = new Label( wContentComp, SWT.RIGHT );
    wlEndedLine.setText( BaseMessages.getString( PKG, "ConcatFieldsDialog.EndedLine.Label" ) );
    props.setLook( wlEndedLine );
    fdlEndedLine = new FormData();
    fdlEndedLine.left = new FormAttachment( 0, 0 );
    fdlEndedLine.top = new FormAttachment( wSplitEvery, margin );
    fdlEndedLine.right = new FormAttachment( middle, -margin );
    wlEndedLine.setLayoutData( fdlEndedLine );
    wEndedLine = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEndedLine );
    wEndedLine.addModifyListener( lsMod );
    fdEndedLine = new FormData();
    fdEndedLine.left = new FormAttachment( middle, 0 );
    fdEndedLine.top = new FormAttachment( wSplitEvery, margin );
    fdEndedLine.right = new FormAttachment( 100, 0 );
    wEndedLine.setLayoutData( fdEndedLine );

    fdAdvancedComp = new FormData();
    fdAdvancedComp.left = new FormAttachment( 0, 0 );
    fdAdvancedComp.top = new FormAttachment( 0, 0 );
    fdAdvancedComp.right = new FormAttachment( 100, 0 );
    fdAdvancedComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( fdAdvancedComp );

    wContentComp.layout();
    wAdvancedTab.setControl( wContentComp );

    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wEnclosure, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsGet = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsMinWidth = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        setMinimalWidth();
      }
    };
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wMinWidth.addListener( SWT.Selection, lsMinWidth );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wTargetFieldName.addSelectionListener( lsDef );
    wTargetFieldLength.addSelectionListener( lsDef );
    wSeparator.addSelectionListener( lsDef );

    // Whenever something changes, set the tooltip to the expanded version:
    wTargetFieldName.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wTargetFieldName.setToolTipText( transMeta.environmentSubstitute( wTargetFieldName.getText() ) );
      }
    } );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    lsResize = new Listener() {
      @Override
      public void handleEvent( Event event ) {
        Point size = shell.getSize();
        wFields.setSize( size.x - 10, size.y - 50 );
        wFields.table.setSize( size.x - 10, size.y - 50 );
        wFields.redraw();
      }
    };
    shell.addListener( SWT.Resize, lsResize );

    wTabFolder.setSelection( 0 );

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

    String[] fieldNames = entries.toArray( new String[ entries.size() ] );

    Const.sortStrings( fieldNames );
    colinf[ 0 ].setComboValues( fieldNames );
  }

  protected void setFlags() {
    // disable a couple of options when running in Fast Data Dump mode
    //
    boolean isNotFastDataDump = !wFastDump.getSelection();

    wDisableEnclosureFix.setEnabled( isNotFastDataDump );
    wlDisableEnclosureFix.setEnabled( isNotFastDataDump );

    wEncoding.setEnabled( isNotFastDataDump );
    wlEncoding.setEnabled( isNotFastDataDump );

    wPad.setEnabled( isNotFastDataDump );
    wlPad.setEnabled( isNotFastDataDump );
  }

  private void setEncodings() {
    // Encoding of the text file:
    if ( !gotEncodings ) {
      gotEncodings = true;

      wEncoding.removeAll();
      List<Charset> values = new ArrayList<Charset>( Charset.availableCharsets().values() );
      for ( int i = 0; i < values.size(); i++ ) {
        Charset charSet = values.get( i );
        wEncoding.add( charSet.displayName() );
      }

      // Now select the default!
      String defEncoding = Const.getEnvironmentVariable( "file.encoding", "UTF-8" );
      int idx = Const.indexOfString( defEncoding, wEncoding.getItems() );
      if ( idx >= 0 ) {
        wEncoding.select( idx );
      }
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    // New concat fields
    if ( input.getTargetFieldName() != null ) {
      wTargetFieldName.setText( input.getTargetFieldName() );
    }
    wTargetFieldLength.setText( "" + input.getTargetFieldLength() );
    wRemoveSelectedFields.setSelection( input.isRemoveSelectedFields() );
    // previous fields derived from TextFileOutputDialog
    wSeparator.setText( Const.NVL( input.getSeparator(), "" ) );
    wEnclosure.setText( Const.NVL( input.getEnclosure(), "" ) );

    if ( input.getEncoding() != null ) {
      wEncoding.setText( input.getEncoding() );
    }
    if ( input.getEndedLine() != null ) {
      wEndedLine.setText( input.getEndedLine() );
    }

    wSplitEvery.setText( "" + input.getSplitEvery() );

    wEnclForced.setSelection( input.isEnclosureForced() );
    wDisableEnclosureFix.setSelection( input.isEnclosureFixDisabled() );
    wHeader.setSelection( input.isHeaderEnabled() );
    wFooter.setSelection( input.isFooterEnabled() );
    wPad.setSelection( input.isPadded() );
    wFastDump.setSelection( input.isFastDump() );

    logDebug( "getting fields info..." );

    for ( int i = 0; i < input.getOutputFields().length; i++ ) {
      TextFileField field = input.getOutputFields()[ i ];

      TableItem item = wFields.table.getItem( i );
      if ( field.getName() != null ) {
        item.setText( 1, field.getName() );
      }
      item.setText( 2, field.getTypeDesc() );
      if ( field.getFormat() != null ) {
        item.setText( 3, field.getFormat() );
      }
      if ( field.getLength() >= 0 ) {
        item.setText( 4, "" + field.getLength() );
      }
      if ( field.getPrecision() >= 0 ) {
        item.setText( 5, "" + field.getPrecision() );
      }
      if ( field.getCurrencySymbol() != null ) {
        item.setText( 6, field.getCurrencySymbol() );
      }
      if ( field.getDecimalSymbol() != null ) {
        item.setText( 7, field.getDecimalSymbol() );
      }
      if ( field.getGroupingSymbol() != null ) {
        item.setText( 8, field.getGroupingSymbol() );
      }
      String trim = field.getTrimTypeDesc();
      if ( trim != null ) {
        item.setText( 9, trim );
      }
      if ( field.getNullString() != null ) {
        item.setText( 10, field.getNullString() );
      }
    }

    wFields.optWidth( true );
    setFlags();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;

    input.setChanged( backupChanged );

    dispose();
  }

  private void getInfo( ConcatFieldsMeta tfoi ) {
    // New concat fields
    tfoi.setTargetFieldName( wTargetFieldName.getText() );
    tfoi.setTargetFieldLength( Const.toInt( wTargetFieldLength.getText(), 0 ) );
    tfoi.setRemoveSelectedFields( wRemoveSelectedFields.getSelection() );
    // previous fields derived from TextFileOutputDialog
    tfoi.setEncoding( wEncoding.getText() );
    tfoi.setSeparator( wSeparator.getText() );
    tfoi.setEnclosure( wEnclosure.getText() );
    tfoi.setSplitEvery( Const.toInt( wSplitEvery.getText(), 0 ) );
    tfoi.setEndedLine( wEndedLine.getText() );

    tfoi.setEnclosureForced( wEnclForced.getSelection() );
    tfoi.setEnclosureFixDisabled( wDisableEnclosureFix.getSelection() );
    tfoi.setHeaderEnabled( wHeader.getSelection() );
    tfoi.setFooterEnabled( wFooter.getSelection() );
    tfoi.setPadded( wPad.getSelection() );
    tfoi.setFastDump( wFastDump.getSelection() );

    int i;
    // Table table = wFields.table;

    int nrfields = wFields.nrNonEmpty();

    tfoi.allocate( nrfields );

    for ( i = 0; i < nrfields; i++ ) {
      TextFileField field = new TextFileField();

      TableItem item = wFields.getNonEmpty( i );
      field.setName( item.getText( 1 ) );
      field.setType( item.getText( 2 ) );
      field.setFormat( item.getText( 3 ) );
      field.setLength( Const.toInt( item.getText( 4 ), -1 ) );
      field.setPrecision( Const.toInt( item.getText( 5 ), -1 ) );
      field.setCurrencySymbol( item.getText( 6 ) );
      field.setDecimalSymbol( item.getText( 7 ) );
      field.setGroupingSymbol( item.getText( 8 ) );
      field.setTrimType( ValueMetaString.getTrimTypeByDesc( item.getText( 9 ) ) );
      field.setNullString( item.getText( 10 ) );
      //CHECKSTYLE:Indentation:OFF
      tfoi.getOutputFields()[ i ] = field;
    }
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    getInfo( input );

    dispose();
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        TableItemInsertListener listener = new TableItemInsertListener() {
          @Override
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            if ( v.isNumber() ) {
              if ( v.getLength() > 0 ) {
                int le = v.getLength();
                int pr = v.getPrecision();

                if ( v.getPrecision() <= 0 ) {
                  pr = 0;
                }

                String mask = "";
                for ( int m = 0; m < le - pr; m++ ) {
                  mask += "0";
                }
                if ( pr > 0 ) {
                  mask += ".";
                }
                for ( int m = 0; m < pr; m++ ) {
                  mask += "0";
                }
                tableItem.setText( 3, mask );
              }
            }
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1 }, new int[] { 2 }, 4, 5, listener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Title" ), BaseMessages
        .getString( PKG, "System.Dialog.GetFieldsFailed.Message" ), ke );
    }

  }

  /**
   * Sets the output width to minimal width...
   */
  public void setMinimalWidth() {
    int nrNonEmptyFields = wFields.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );

      item.setText( 4, "" );
      item.setText( 5, "" );
      item.setText( 9, ValueMetaString.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_BOTH ) );

      int type = ValueMetaFactory.getIdForValueMeta( item.getText( 2 ) );
      switch ( type ) {
        case ValueMetaInterface.TYPE_STRING:
          item.setText( 3, "" );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          item.setText( 3, "0" );
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          item.setText( 3, "0.#####" );
          break;
        case ValueMetaInterface.TYPE_DATE:
          break;
        default:
          break;
      }
    }

    for ( int i = 0; i < input.getOutputFields().length; i++ ) {
      input.getOutputFields()[ i ].setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    }

    wFields.optWidth( true );
  }

}
