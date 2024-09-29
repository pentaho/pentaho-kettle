/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelwriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.core.annotations.PluginDialog;
@PluginDialog( id = "TypeExitExcelWriterStep", image = "XWS.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/microsoft-excel-writer" +
                "" )
public class ExcelWriterStepDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ExcelWriterStepMeta.class; // for i18n

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wFileTab, wContentTab;

  private FormData fdFileComp, fdContentComp;

  private Button wbFilename;
  private TextVar wFilename;

  private CCombo wExtension;

  private Button wCreateParentFolder;

  private Button wStreamData;

  private Button wAddStepnr;

  private Label wlAddDate;
  private Button wAddDate;

  private Label wlAddTime;
  private Button wAddTime;

  private Label wlProtectSheet;
  private Button wProtectSheet;
  private FormData fdlProtectSheet, fdProtectSheet;

  private Button wbShowFiles;
  private FormData fdbShowFiles;

  private Button wHeader;

  private Button wFooter;

  private Text wSplitEvery;

  private Label wlTemplate;
  private Button wTemplate;
  private FormData fdlTemplate, fdTemplate;

  private Label wlTemplateFilename;
  private Button wbTemplateFilename;
  private TextVar wTemplateFilename;
  private FormData fdlTemplateFilename, fdbTemplateFilename, fdTemplateFilename;

  private Label wlPassword;
  private TextVar wPassword;
  private FormData fdlPassword, fdPassword;

  private Label wlSheetname;
  private TextVar wSheetname;
  private FormData fdlSheetname, fdSheetname;

  private TableView wFields;
  private FormData fdFields;

  private ExcelWriterStepMeta input;

  private Button wMinWidth;
  private Listener lsMinWidth;

  private Label wlAddToResult;
  private Button wAddToResult;
  private FormData fdlAddToResult, fdAddToResult;

  private Label wlDoNotOpenNewFileInit;
  private Button wDoNotOpenNewFileInit;
  private FormData fdlDoNotOpenNewFileInit, fdDoNotOpenNewFileInit;

  private Label wlSpecifyFormat;
  private Button wSpecifyFormat;
  private FormData fdlSpecifyFormat, fdSpecifyFormat;

  private Label wlDateTimeFormat;
  private CCombo wDateTimeFormat;
  private FormData fdlDateTimeFormat, fdDateTimeFormat;

  private Button wAutoSize;

  private Button wRetainNullValues;

  private Group wTemplateGroup;
  private FormData fdTemplateGroup;

  private ColumnInfo[] colinf;

  private Map<String, Integer> inputFields;

  private Label wlIfFileExists;

  private FormData fdlIfFileExists;

  private CCombo wIfFileExists;

  private Label wlIfSheetExists;

  private CCombo wIfSheetExists;

  private Label wlTemplateSheetname;

  private TextVar wTemplateSheetname;

  private TextVar wStartingCell;

  private CCombo wRowWritingMethod;

  private Label wlTemplateSheet;

  private Button wTemplateSheet;

  private Label wlTemplateSheetHide;

  private Button wTemplateSheetHide;

  private Button wAppendLines;

  private Text wSkipRows;

  private Text wEmptyRows;

  private Button wOmitHeader;

  private TextVar wProtectedBy;

  private Button wMakeActiveSheet;
  private Button wForceFormulaRecalculation;
  private Button wLeaveExistingStylesUnchanged;
  private Button wExtendDataValidation;

  private int middle;

  public ExcelWriterStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (ExcelWriterStepMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    setShellImage( shell, input );

    SelectionAdapter lsSel = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    };
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
    shell.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.DialogTitle" ) );

    middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( middle, -margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    ScrolledComposite sc = new ScrolledComposite( shell, SWT.H_SCROLL | SWT.V_SCROLL );

    wTabFolder = new CTabFolder( sc, SWT.BORDER );

    // ////////////////////////
    // START OF FILE TAB///
    // /
    wFileTab = new CTabItem( wTabFolder, SWT.NONE );
    wFileTab.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.FileTab.TabTitle" ) );

    Composite wFileComp = new Composite( wTabFolder, SWT.NONE );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFileComp.setLayout( fileLayout );

    Group fileGroup = createFileGroup( lsSel, lsMod, margin, wFileComp );

    // END OF FILE GROUP

    Group sheetGroup = new Group( wFileComp, SWT.SHADOW_NONE );
    sheetGroup.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.sheetGroup.Label" ) );

    FormLayout sheetGroupLayout = new FormLayout();
    sheetGroupLayout.marginWidth = 10;
    sheetGroupLayout.marginHeight = 10;
    sheetGroup.setLayout( sheetGroupLayout );

    // Sheet name line
    wlSheetname = new Label( sheetGroup, SWT.RIGHT );
    wlSheetname.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.Sheetname.Label" ) );

    fdlSheetname = new FormData();
    fdlSheetname.left = new FormAttachment( 0, 0 );
    fdlSheetname.top = new FormAttachment( 0, margin );
    fdlSheetname.right = new FormAttachment( middle, -margin );
    wlSheetname.setLayoutData( fdlSheetname );
    wSheetname = new TextVar( transMeta, sheetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSheetname.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Sheetname.Tooltip" ) );
    wSheetname.addModifyListener( lsMod );
    fdSheetname = new FormData();
    fdSheetname.left = new FormAttachment( middle, 0 );
    fdSheetname.top = new FormAttachment( 0, margin );
    fdSheetname.right = new FormAttachment( 100, 0 );
    wSheetname.setLayoutData( fdSheetname );

    // Make sheet active Sheet Line
    Label wlMakeActiveSheet = new Label( sheetGroup, SWT.RIGHT );
    wlMakeActiveSheet.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.MakeActiveSheet.Label" ) );
    FormData fdlMakeActiveSheet = new FormData();
    fdlMakeActiveSheet.left = new FormAttachment( 0, 0 );
    fdlMakeActiveSheet.top = new FormAttachment( wSheetname, margin );
    fdlMakeActiveSheet.right = new FormAttachment( middle, -margin );
    wlMakeActiveSheet.setLayoutData( fdlMakeActiveSheet );
    wMakeActiveSheet = new Button( sheetGroup, SWT.CHECK );
    wMakeActiveSheet.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.MakeActiveSheet.Tooltip" ) );
    FormData fdMakeActiveSheet = new FormData();
    fdMakeActiveSheet.left = new FormAttachment( middle, 0 );
    fdMakeActiveSheet.top = new FormAttachment( wSheetname, margin );
    fdMakeActiveSheet.right = new FormAttachment( 100, 0 );
    wMakeActiveSheet.setLayoutData( fdMakeActiveSheet );
    wMakeActiveSheet.addSelectionListener( lsSel );

    // If output sheet exists line
    wlIfSheetExists = new Label( sheetGroup, SWT.RIGHT );
    wlIfSheetExists.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.IfSheetExists.Label" ) );
    FormData fdlIfSheetExists = new FormData();
    fdlIfSheetExists.left = new FormAttachment( 0, 0 );
    fdlIfSheetExists.top = new FormAttachment( wMakeActiveSheet, margin );
    fdlIfSheetExists.right = new FormAttachment( middle, -margin );
    wlIfSheetExists.setLayoutData( fdlIfSheetExists );
    wIfSheetExists = new CCombo( sheetGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY );

    String replaceSheetNewLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.IfSheetExists.CreateNew.Label" );
    String reuseSheetLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.IfSheetExists.Reuse.Label" );
    wIfSheetExists.setItems( new String[] { replaceSheetNewLabel, reuseSheetLabel } );
    wIfSheetExists.setData( replaceSheetNewLabel, ExcelWriterStepMeta.IF_SHEET_EXISTS_CREATE_NEW );
    wIfSheetExists.setData( reuseSheetLabel, ExcelWriterStepMeta.IF_SHEET_EXISTS_REUSE );

    wIfSheetExists.addModifyListener( lsMod );
    wIfSheetExists.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.IfSheetExists.Tooltip" ) );

    FormData fdIfSheetExists = new FormData();
    fdIfSheetExists.left = new FormAttachment( middle, 0 );
    fdIfSheetExists.top = new FormAttachment( wMakeActiveSheet, margin );
    fdIfSheetExists.right = new FormAttachment( 100, 0 );
    wIfSheetExists.setLayoutData( fdIfSheetExists );

    // Protect Sheet?
    wlProtectSheet = new Label( sheetGroup, SWT.RIGHT );
    wlProtectSheet.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.ProtectSheet.Label" ) );
    fdlProtectSheet = new FormData();
    fdlProtectSheet.left = new FormAttachment( 0, 0 );
    fdlProtectSheet.top = new FormAttachment( wIfSheetExists, margin );
    fdlProtectSheet.right = new FormAttachment( middle, -margin );
    wlProtectSheet.setLayoutData( fdlProtectSheet );
    wProtectSheet = new Button( sheetGroup, SWT.CHECK );
    wProtectSheet.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.ProtectSheet.Tooltip" ) );
    fdProtectSheet = new FormData();
    fdProtectSheet.left = new FormAttachment( middle, 0 );
    fdProtectSheet.top = new FormAttachment( wIfSheetExists, margin );
    fdProtectSheet.right = new FormAttachment( 100, 0 );
    wProtectSheet.setLayoutData( fdProtectSheet );
    wProtectSheet.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enablePassword();
      }
    } );

    // Protected by line
    Label wlProtectedBy = new Label( sheetGroup, SWT.RIGHT );
    wlProtectedBy.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.ProtectedBy.Label" ) );
    FormData fdlProtectedBy = new FormData();
    fdlProtectedBy.left = new FormAttachment( 0, 0 );
    fdlProtectedBy.top = new FormAttachment( wProtectSheet, margin );
    fdlProtectedBy.right = new FormAttachment( middle, -margin );
    wlProtectedBy.setLayoutData( fdlProtectedBy );
    wProtectedBy = new TextVar( transMeta, sheetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wProtectedBy.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.ProtectedBy.Tooltip" ) );

    wProtectedBy.addModifyListener( lsMod );
    FormData fdProtectedBy = new FormData();
    fdProtectedBy.left = new FormAttachment( middle, 0 );
    fdProtectedBy.top = new FormAttachment( wProtectSheet, margin );
    fdProtectedBy.right = new FormAttachment( 100, 0 );
    wProtectedBy.setLayoutData( fdProtectedBy );

    // Password line
    wlPassword = new Label( sheetGroup, SWT.RIGHT );
    wlPassword.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.Password.Label" ) );
    fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wProtectedBy, margin );
    fdlPassword.right = new FormAttachment( middle, -margin );
    wlPassword.setLayoutData( fdlPassword );
    wPassword = new PasswordTextVar( transMeta, sheetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wPassword.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Password.Tooltip" ) );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( middle, 0 );
    fdPassword.top = new FormAttachment( wProtectedBy, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    FormData fsSheetGroup = new FormData();
    fsSheetGroup.left = new FormAttachment( 0, margin );
    fsSheetGroup.top = new FormAttachment( fileGroup, margin );
    fsSheetGroup.right = new FormAttachment( 100, -margin );
    sheetGroup.setLayoutData( fsSheetGroup );

    // END OF SHEET GROUP

    // ///////////////////////////////
    // START OF Template Group GROUP //
    // ///////////////////////////////

    wTemplateGroup = new Group( wFileComp, SWT.SHADOW_NONE );
    wTemplateGroup.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.TemplateGroup.Label" ) );

    FormLayout TemplateGroupgroupLayout = new FormLayout();
    TemplateGroupgroupLayout.marginWidth = 10;
    TemplateGroupgroupLayout.marginHeight = 10;
    wTemplateGroup.setLayout( TemplateGroupgroupLayout );

    // Use template
    wlTemplate = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplate.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.Template.Label" ) );
    fdlTemplate = new FormData();
    fdlTemplate.left = new FormAttachment( 0, 0 );
    fdlTemplate.top = new FormAttachment( 0, margin );
    fdlTemplate.right = new FormAttachment( middle, -margin );
    wlTemplate.setLayoutData( fdlTemplate );
    wTemplate = new Button( wTemplateGroup, SWT.CHECK );
    fdTemplate = new FormData();
    fdTemplate.left = new FormAttachment( middle, 0 );
    fdTemplate.top = new FormAttachment( 0, margin );
    fdTemplate.right = new FormAttachment( 100, 0 );
    wTemplate.setLayoutData( fdTemplate );
    wTemplate.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableTemplate();
      }
    } );
    wTemplate.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Template.Tooltip" ) );

    // TemplateFilename line
    wlTemplateFilename = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplateFilename.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.TemplateFilename.Label" ) );
    fdlTemplateFilename = new FormData();
    fdlTemplateFilename.left = new FormAttachment( 0, 0 );
    fdlTemplateFilename.top = new FormAttachment( wTemplate, margin );
    fdlTemplateFilename.right = new FormAttachment( middle, -margin );
    wlTemplateFilename.setLayoutData( fdlTemplateFilename );

    wbTemplateFilename = new Button( wTemplateGroup, SWT.PUSH | SWT.CENTER );
    wbTemplateFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbTemplateFilename = new FormData();
    fdbTemplateFilename.right = new FormAttachment( 100, 0 );
    fdbTemplateFilename.top = new FormAttachment( wTemplate, 0 );
    wbTemplateFilename.setLayoutData( fdbTemplateFilename );

    wTemplateFilename = new TextVar( transMeta, wTemplateGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTemplateFilename.addModifyListener( lsMod );
    fdTemplateFilename = new FormData();
    fdTemplateFilename.left = new FormAttachment( middle, 0 );
    fdTemplateFilename.top = new FormAttachment( wTemplate, margin );
    fdTemplateFilename.right = new FormAttachment( wbTemplateFilename, -margin );
    wTemplateFilename.setLayoutData( fdTemplateFilename );

    // Use template sheet
    wlTemplateSheet = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplateSheet.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.TemplateSheet.Label" ) );
    FormData fdlTemplateSheet = new FormData();
    fdlTemplateSheet.left = new FormAttachment( 0, 0 );
    fdlTemplateSheet.top = new FormAttachment( wTemplateFilename, margin );
    fdlTemplateSheet.right = new FormAttachment( middle, -margin );
    wlTemplateSheet.setLayoutData( fdlTemplateSheet );
    wTemplateSheet = new Button( wTemplateGroup, SWT.CHECK );
    wTemplateSheet.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.TemplateSheet.Tooltip" ) );

    FormData fdTemplateSheet = new FormData();
    fdTemplateSheet.left = new FormAttachment( middle, 0 );
    fdTemplateSheet.top = new FormAttachment( wTemplateFilename, margin );
    fdTemplateSheet.right = new FormAttachment( 100, 0 );
    wTemplateSheet.setLayoutData( fdTemplateSheet );
    wTemplateSheet.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableTemplateSheet();
      }
    } );

    // TemplateSheetname line
    wlTemplateSheetname = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplateSheetname.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.TemplateSheetname.Label" ) );
    FormData fdlTemplateSheetname = new FormData();
    fdlTemplateSheetname.left = new FormAttachment( 0, 0 );
    fdlTemplateSheetname.top = new FormAttachment( wTemplateSheet, margin );
    fdlTemplateSheetname.right = new FormAttachment( middle, -margin );
    wlTemplateSheetname.setLayoutData( fdlTemplateSheetname );

    wTemplateSheetname = new TextVar( transMeta, wTemplateGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTemplateSheetname.addModifyListener( lsMod );
    FormData fdTemplateSheetname = new FormData();
    fdTemplateSheetname.left = new FormAttachment( middle, 0 );
    fdTemplateSheetname.top = new FormAttachment( wTemplateSheet, margin );
    fdTemplateSheetname.right = new FormAttachment( wbTemplateFilename, -margin );
    wTemplateSheetname.setLayoutData( fdTemplateSheetname );

    //Hide Template Sheet
    wlTemplateSheetHide = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplateSheetHide.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.TemplateSheetHide.Label" ) );
    FormData fdlTemplateSheetHide = new FormData();
    fdlTemplateSheetHide.left = new FormAttachment( 0, 0 );
    fdlTemplateSheetHide.top = new FormAttachment( wTemplateSheetname, margin );
    fdlTemplateSheetHide.right = new FormAttachment( middle, -margin );
    wlTemplateSheetHide.setLayoutData( fdlTemplateSheetHide );

    wTemplateSheetHide = new Button( wTemplateGroup, SWT.CHECK );
    wTemplateSheetHide.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.TemplateSheetHide.Tooltip" ) );
    FormData fdTemplateSheetHide = new FormData();
    fdTemplateSheetHide.left = new FormAttachment( middle, 0 );
    fdTemplateSheetHide.top = new FormAttachment( wTemplateSheetname, margin );
    fdTemplateSheetHide.right = new FormAttachment( 100, 0 );
    wTemplateSheetHide.setLayoutData( fdTemplateSheetHide );
    wTemplateSheetHide.addSelectionListener( lsSel );

    fdTemplateGroup = new FormData();
    fdTemplateGroup.left = new FormAttachment( 0, margin );
    fdTemplateGroup.top = new FormAttachment( sheetGroup, margin );
    fdTemplateGroup.right = new FormAttachment( 100, -margin );
    wTemplateGroup.setLayoutData( fdTemplateGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Write to existing Group GROUP
    // ///////////////////////////////////////////////////////////

    fdFileComp = new FormData();
    fdFileComp.left = new FormAttachment( 0, 0 );
    fdFileComp.top = new FormAttachment( 0, 0 );
    fdFileComp.right = new FormAttachment( 100, 0 );
    fdFileComp.bottom = new FormAttachment( 100, 0 );
    wFileComp.setLayoutData( fdFileComp );

    wFileComp.layout();
    wFileTab.setControl( wFileComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILE TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF CONTENT TAB///
    // /
    createContentTab( lsSel, lsMod, middle, margin, sc );
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, sc );

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
    wFilename.addSelectionListener( lsDef );
    wTemplateFilename.addSelectionListener( lsDef );

    // Whenever something changes, set the tooltip to the expanded version:
    wFilename.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wFilename.setToolTipText( transMeta.environmentSubstitute( wFilename.getText() )
          + "\n\n" + BaseMessages.getString( PKG, "ExcelWriterDialog.Filename.Tooltip" ) );
      }
    } );
    wTemplateFilename.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wTemplateFilename.setToolTipText( transMeta.environmentSubstitute( wTemplateFilename.getText() ) );
      }
    } );

    wSheetname.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wSheetname.setToolTipText( transMeta.environmentSubstitute( wSheetname.getText() )
          + "\n\n" + BaseMessages.getString( PKG, "ExcelWriterDialog.Sheetname.Tooltip" ) );
      }
    } );

    wTemplateSheetname.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wTemplateSheetname.setToolTipText( transMeta.environmentSubstitute( wTemplateSheetname.getText() ) );
      }
    } );

    wStartingCell.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wStartingCell.setToolTipText( transMeta.environmentSubstitute( wStartingCell.getText() )
          + "\n\n" + BaseMessages.getString( PKG, "ExcelWriterDialog.StartingCell.Tooltip" ) );
      }
    } );

    wPassword.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wPassword.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Password.Tooltip" ) );
      }
    } );

    wProtectedBy.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wProtectedBy.setToolTipText( transMeta.environmentSubstitute( wProtectedBy.getText() )
          + "\n\n" + BaseMessages.getString( PKG, "ExcelWriterDialog.ProtectedBy.Tooltip" ) );
      }
    } );

    wbFilename.addSelectionListener(
      new SelectionAdapterFileDialogTextVar(
        log,
        wFilename,
        transMeta,
        new SelectionAdapterOptions(
          SelectionOperation.SAVE_TO,
          new FilterType[] { FilterType.XLS, FilterType.XLSX, FilterType.ALL },
          FilterType.XLS,
          new ProviderFilterType[] {ProviderFilterType.DEFAULT}
        )
      )
    );

    wbTemplateFilename.addSelectionListener(
      new SelectionAdapterFileDialogTextVar(
        log,
        wTemplateFilename,
        transMeta,
        new SelectionAdapterOptions(
          SelectionOperation.FILE,
          new FilterType[] { FilterType.XLS, FilterType.XLSX, FilterType.ALL },
          FilterType.XLS,
          new ProviderFilterType[] {ProviderFilterType.DEFAULT}
        )
      )
    );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );
    // propsUI.setLook is recursive; only needs to be run once, at the end, in the parent container
    props.setLook( shell );

    getData();
    setDateTimeFormat();
    enableExtension();
    enableAppend();
    enableHeader();
    enableTemplateSheet();
    input.setChanged( changed );

    // artificially reduce table size
    for ( int t = 0; t < wFields.table.getColumnCount(); t++ ) {
      wFields.table.getColumn( t ).setWidth( 20 );
    }

    wFields.layout();
    wFields.pack();

    // determine scrollable area
    sc.setMinSize( wTabFolder.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    sc.setExpandHorizontal( true );
    sc.setExpandVertical( true );

    // set window size
    setSize( shell, 600, 600, true );

    // restore optimal column widths
    wFields.optWidth( true );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private Group createGroup( Composite parent, String labelKey ) {
    Group group = new Group( parent, SWT.SHADOW_NONE );
    group.setText( getMsg( labelKey ) );

    FormLayout groupLayout = new FormLayout();
    groupLayout.marginWidth = 10;
    groupLayout.marginHeight = 10;
    group.setLayout( groupLayout );

    return group;
  }

  private Group createFileGroup( SelectionAdapter lsSel, ModifyListener lsMod, int margin, Composite wFileComp ) {
    Group fileGroup = createGroup( wFileComp, "ExcelWriterDialog.fileGroup.Label" );

    // Filename line
    Label wlFilename = createLabel( fileGroup, "ExcelWriterDialog.Filename.Label" );
    wlFilename.setLayoutData( fd().right( middle, -margin ).top( 0, margin ).result() );

    wbFilename = new Button( fileGroup, SWT.PUSH | SWT.CENTER );
    wbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    FormData fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( 0, 0 );
    wbFilename.setLayoutData( fdbFilename );

    wFilename = new TextVar( transMeta, fileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wFilename.addModifyListener( lsMod );
    wFilename.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Filename.Tooltip" ) );
    FormData fdFilename = fd().left( middle, 0 ).top( 0, margin ).right(wbFilename, -margin).result();
    wFilename.setLayoutData( fdFilename );
    Control lastWidget = wFilename;
    // Extension line
    Label wlExtension = createLabel( fileGroup, "System.Label.Extension" );
    wExtension = new CCombo( fileGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY );
    String xlsLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.FormatXLS.Label" );
    String xlsxLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.FormatXLSX.Label" );
    wExtension.setItems( new String[] { xlsLabel, xlsxLabel } );
    wExtension.setData( xlsLabel, "xls" );
    wExtension.setData( xlsxLabel, "xlsx" );

    wExtension.addModifyListener( lsMod );

    wExtension.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableExtension();
      }
    } );
    wExtension.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Extension.Tooltip" ) );

    wlExtension.setLayoutData( fd().left().right( middle, -margin ).top( lastWidget, margin ).result() );
    wExtension.setLayoutData( fd().left( middle, 0 ).right( wbFilename, -margin ).top( lastWidget, margin ).result() );
    lastWidget = wExtension;

    Label wlCreateParentFolder = createLabel( fileGroup, "ExcelWriterDialog.CreateParentFolder.Label" );
    wCreateParentFolder = new Button( fileGroup, SWT.CHECK );
    wCreateParentFolder.addSelectionListener( lsSel );
    lastWidget = layoutLabelControlPair( wlCreateParentFolder, wCreateParentFolder, lastWidget );

    Label wlStreamData = createLabel( fileGroup, "ExcelWriterDialog.StreamData.Label" );
    wStreamData = new Button( fileGroup, SWT.CHECK );
    wStreamData.addSelectionListener( lsSel );
    lastWidget = layoutLabelControlPair( wlStreamData, wStreamData, lastWidget );

    // split every x rows
    Label wlSplitEvery = createLabel(fileGroup, "ExcelWriterDialog.SplitEvery.Label" ); new Label( fileGroup, SWT.RIGHT );
    wSplitEvery = new Text( fileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSplitEvery.addModifyListener( lsMod );
    wSplitEvery.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.SplitEvery.Tooltip" ) );
    lastWidget = layoutLabelControlPair( wlSplitEvery, wSplitEvery, lastWidget );

    // include step number
    Label wlAddStepnr = createLabel(fileGroup, "ExcelWriterDialog.AddStepnr.Label" );
    wAddStepnr = new Button( fileGroup, SWT.CHECK );
    wAddStepnr.addSelectionListener( lsSel );
    lastWidget = layoutLabelControlPair( wlAddStepnr, wAddStepnr, lastWidget );

    // include date
    wlAddDate = createLabel( fileGroup,  "ExcelWriterDialog.AddDate.Label" );
    wAddDate = new Button( fileGroup, SWT.CHECK );
    wAddDate.addSelectionListener( lsSel );
    lastWidget = layoutLabelControlPair( wlAddDate, wAddDate, lastWidget );

    // include time
    wlAddTime = createLabel( fileGroup, "ExcelWriterDialog.AddTime.Label" );
    wAddTime = new Button( fileGroup, SWT.CHECK );
    wAddTime.addSelectionListener( lsSel );
    lastWidget = layoutLabelControlPair( wlAddTime, wAddTime, lastWidget );

    // Specify date time format?
    wlSpecifyFormat = new Label( fileGroup, SWT.RIGHT );
    wlSpecifyFormat.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.SpecifyFormat.Label" ) );
    fdlSpecifyFormat = new FormData();
    fdlSpecifyFormat.left = new FormAttachment( 0, 0 );
    fdlSpecifyFormat.top = new FormAttachment( wAddTime, margin );
    fdlSpecifyFormat.right = new FormAttachment( middle, -margin );
    wlSpecifyFormat.setLayoutData( fdlSpecifyFormat );
    wSpecifyFormat = new Button( fileGroup, SWT.CHECK );
    wSpecifyFormat.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.SpecifyFormat.Tooltip" ) );
    fdSpecifyFormat = new FormData();
    fdSpecifyFormat.left = new FormAttachment( middle, 0 );
    fdSpecifyFormat.top = new FormAttachment( wAddTime, margin );
    fdSpecifyFormat.right = new FormAttachment( 100, 0 );
    wSpecifyFormat.setLayoutData( fdSpecifyFormat );
    wSpecifyFormat.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setDateTimeFormat();
      }
    } );

    // Prepare a list of possible DateTimeFormats...
    String[] dats = Const.getDateFormats();

    // DateTimeFormat
    wlDateTimeFormat = new Label( fileGroup, SWT.RIGHT );
    wlDateTimeFormat.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.DateTimeFormat.Label" ) );
    fdlDateTimeFormat = new FormData();
    fdlDateTimeFormat.left = new FormAttachment( 0, 0 );
    fdlDateTimeFormat.top = new FormAttachment( wSpecifyFormat, margin );
    fdlDateTimeFormat.right = new FormAttachment( middle, -margin );
    wlDateTimeFormat.setLayoutData( fdlDateTimeFormat );
    wDateTimeFormat = new CCombo( fileGroup, SWT.BORDER | SWT.READ_ONLY );
    wDateTimeFormat.setEditable( true );
    wDateTimeFormat.addModifyListener( lsMod );
    fdDateTimeFormat = new FormData();
    fdDateTimeFormat.left = new FormAttachment( middle, 0 );
    fdDateTimeFormat.top = new FormAttachment( wSpecifyFormat, margin );
    fdDateTimeFormat.right = new FormAttachment( 100, 0 );
    wDateTimeFormat.setLayoutData( fdDateTimeFormat );
    for ( int x = 0; x < dats.length; x++ ) {
      wDateTimeFormat.add( dats[x] );
    }

    wbShowFiles = new Button( fileGroup, SWT.PUSH | SWT.CENTER );
    wbShowFiles.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.ShowFiles.Button" ) );
    fdbShowFiles = new FormData();
    fdbShowFiles.left = new FormAttachment( middle, 0 );
    fdbShowFiles.top = new FormAttachment( wDateTimeFormat, margin * 3 );
    wbShowFiles.setLayoutData( fdbShowFiles );
    wbShowFiles.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        ExcelWriterStepMeta tfoi = new ExcelWriterStepMeta();
        getInfo( tfoi );
        String[] files = tfoi.getFiles( transMeta );
        if ( files != null && files.length > 0 ) {
          EnterSelectionDialog esd =
            new EnterSelectionDialog( shell, files,
              BaseMessages.getString( PKG, "ExcelWriterDialog.SelectOutputFiles.DialogTitle" ),
              BaseMessages.getString( PKG, "ExcelWriterDialog.SelectOutputFiles.DialogMessage" ) );
          esd.setViewOnly();
          esd.open();
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "ExcelWriterDialog.NoFilesFound.DialogMessage" ) );
          mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
          mb.open();
        }
      }
    } );

    // If output file exists line
    wlIfFileExists = new Label( fileGroup, SWT.RIGHT );
    wlIfFileExists.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.IfFileExists.Label" ) );
    fdlIfFileExists = new FormData();
    fdlIfFileExists.left = new FormAttachment( 0, 0 );
    fdlIfFileExists.top = new FormAttachment( wbShowFiles, 2 * margin, margin );
    fdlIfFileExists.right = new FormAttachment( middle, -margin );
    wlIfFileExists.setLayoutData( fdlIfFileExists );
    wIfFileExists = new CCombo( fileGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY );

    String createNewLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.IfFileExists.CreateNew.Label" );
    String reuseLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.IfFileExists.Reuse.Label" );
    wIfFileExists.setItems( new String[] { createNewLabel, reuseLabel } );
    wIfFileExists.setData( createNewLabel, ExcelWriterStepMeta.IF_FILE_EXISTS_CREATE_NEW );
    wIfFileExists.setData( reuseLabel, ExcelWriterStepMeta.IF_FILE_EXISTS_REUSE );

    wIfFileExists.addModifyListener( lsMod );
    wIfFileExists.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.IfFileExists.Tooltip" ) );

    FormData fdIfFileExists = new FormData();
    fdIfFileExists.left = new FormAttachment( middle, 0 );
    fdIfFileExists.top = new FormAttachment( wbShowFiles, 2 * margin, margin );
    fdIfFileExists.right = new FormAttachment( 100, 0 );
    wIfFileExists.setLayoutData( fdIfFileExists );

    // Open new File at Init
    wlDoNotOpenNewFileInit = new Label( fileGroup, SWT.RIGHT );
    wlDoNotOpenNewFileInit.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.DoNotOpenNewFileInit.Label" ) );
    fdlDoNotOpenNewFileInit = new FormData();
    fdlDoNotOpenNewFileInit.left = new FormAttachment( 0, 0 );
    fdlDoNotOpenNewFileInit.top = new FormAttachment( wIfFileExists, 2 * margin, margin );
    fdlDoNotOpenNewFileInit.right = new FormAttachment( middle, -margin );
    wlDoNotOpenNewFileInit.setLayoutData( fdlDoNotOpenNewFileInit );
    wDoNotOpenNewFileInit = new Button( fileGroup, SWT.CHECK );
    wDoNotOpenNewFileInit.setToolTipText( BaseMessages.getString(
      PKG, "ExcelWriterDialog.DoNotOpenNewFileInit.Tooltip" ) );
    fdDoNotOpenNewFileInit = new FormData();
    fdDoNotOpenNewFileInit.left = new FormAttachment( middle, 0 );
    fdDoNotOpenNewFileInit.top = new FormAttachment( wIfFileExists, 2 * margin, margin );
    fdDoNotOpenNewFileInit.right = new FormAttachment( 100, 0 );
    wDoNotOpenNewFileInit.setLayoutData( fdDoNotOpenNewFileInit );
    wDoNotOpenNewFileInit.addSelectionListener( lsSel );

    // Add File to the result files name
    wlAddToResult = new Label( fileGroup, SWT.RIGHT );
    wlAddToResult.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.AddFileToResult.Label" ) );
    fdlAddToResult = new FormData();
    fdlAddToResult.left = new FormAttachment( 0, 0 );
    fdlAddToResult.top = new FormAttachment( wDoNotOpenNewFileInit );
    fdlAddToResult.right = new FormAttachment( middle, -margin );
    wlAddToResult.setLayoutData( fdlAddToResult );
    wAddToResult = new Button( fileGroup, SWT.CHECK );
    wAddToResult.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.AddFileToResult.Tooltip" ) );
    fdAddToResult = new FormData();
    fdAddToResult.left = new FormAttachment( middle, 0 );
    fdAddToResult.top = new FormAttachment( wDoNotOpenNewFileInit );
    fdAddToResult.right = new FormAttachment( 100, 0 );
    wAddToResult.setLayoutData( fdAddToResult );
    wAddToResult.addSelectionListener( lsSel );

    FormData fsFileGroup = new FormData();
    fsFileGroup.left = new FormAttachment( 0, margin );
    fsFileGroup.top = new FormAttachment( 0, margin );
    fsFileGroup.right = new FormAttachment( 100, -margin );
    fileGroup.setLayoutData( fsFileGroup );
    return fileGroup;
  }

  private void createContentTab( SelectionAdapter lsSel, ModifyListener lsMod, int middle, int margin, ScrolledComposite sc ) {

    wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.ContentTab.TabTitle" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    Composite wContentComp = new Composite( wTabFolder, SWT.NONE );
    wContentComp.setLayout( contentLayout );

    // CONTENT GROUP
    Group wContentGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    wContentGroup.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.ContentGroup.Label" ) );

    FormLayout ContentGroupgroupLayout = new FormLayout();
    ContentGroupgroupLayout.marginWidth = 10;
    ContentGroupgroupLayout.marginHeight = 10;
    wContentGroup.setLayout( ContentGroupgroupLayout );

    createContentGroup( lsSel, lsMod, middle, margin, wContentGroup );
    FormData fdContentGroup = fd().left( 0, margin ).top( 0, margin ).right( 100, -margin ).result();
    wContentGroup.setLayoutData( fdContentGroup );

    // / END OF CONTENT GROUP

    Group writeToExistingGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    writeToExistingGroup.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.writeToExistingGroup.Label" ) );

    FormLayout writeToExistingGroupgroupLayout = new FormLayout();
    writeToExistingGroupgroupLayout.marginWidth = 10;
    writeToExistingGroupgroupLayout.marginHeight = 10;
    writeToExistingGroup.setLayout( writeToExistingGroupgroupLayout );

    // Use AppendLines
    Label wlAppendLines = new Label( writeToExistingGroup, SWT.RIGHT );
    wlAppendLines.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.AppendLines.Label" ) );
    FormData fdlAppendLines = new FormData();
    fdlAppendLines.left = new FormAttachment( 0, 0 );
    fdlAppendLines.top = new FormAttachment( 0, margin );
    fdlAppendLines.right = new FormAttachment( middle, -margin );
    wlAppendLines.setLayoutData( fdlAppendLines );
    wAppendLines = new Button( writeToExistingGroup, SWT.CHECK );
    wAppendLines.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.AppendLines.Tooltip" ) );
    FormData fdAppendLines = new FormData();
    fdAppendLines.left = new FormAttachment( middle, 0 );
    fdAppendLines.top = new FormAttachment( 0, margin );
    fdAppendLines.right = new FormAttachment( 100, 0 );
    wAppendLines.setLayoutData( fdAppendLines );
    // wAppendLines.addSelectionListener(lsMod);
    wAppendLines.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
        enableAppend();
      }
    } );

    // SkipRows line
    Label wlSkipRows = new Label( writeToExistingGroup, SWT.RIGHT );
    wlSkipRows.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.SkipRows.Label" ) );
    FormData fdlSkipRows = new FormData();
    fdlSkipRows.left = new FormAttachment( 0, 0 );
    fdlSkipRows.top = new FormAttachment( wAppendLines, margin );
    fdlSkipRows.right = new FormAttachment( middle, -margin );
    wlSkipRows.setLayoutData( fdlSkipRows );

    wSkipRows = new Text( writeToExistingGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSkipRows.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.SkipRows.Tooltip" ) );
    wSkipRows.addModifyListener( lsMod );
    FormData fdSkipRows = new FormData();
    fdSkipRows.left = new FormAttachment( middle, 0 );
    fdSkipRows.top = new FormAttachment( wAppendLines, margin );
    fdSkipRows.right = new FormAttachment( 100, 0 );
    wSkipRows.setLayoutData( fdSkipRows );

    // EmptyRows line
    Label wlEmptyRows = new Label( writeToExistingGroup, SWT.RIGHT );
    wlEmptyRows.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.EmptyRows.Label" ) );
    FormData fdlEmptyRows = new FormData();
    fdlEmptyRows.left = new FormAttachment( 0, 0 );
    fdlEmptyRows.top = new FormAttachment( wSkipRows, margin );
    fdlEmptyRows.right = new FormAttachment( middle, -margin );
    wlEmptyRows.setLayoutData( fdlEmptyRows );

    wEmptyRows = new Text( writeToExistingGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wEmptyRows.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.EmptyRows.Tooltip" ) );
    wEmptyRows.addModifyListener( lsMod );
    FormData fdEmptyRows = new FormData();
    fdEmptyRows.left = new FormAttachment( middle, 0 );
    fdEmptyRows.top = new FormAttachment( wSkipRows, margin );
    fdEmptyRows.right = new FormAttachment( 100, 0 );
    wEmptyRows.setLayoutData( fdEmptyRows );

    // Use AppendLines
    Label wlOmitHeader = new Label( writeToExistingGroup, SWT.RIGHT );
    wlOmitHeader.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.OmitHeader.Label" ) );
    FormData fdlOmitHeader = new FormData();
    fdlOmitHeader.left = new FormAttachment( 0, 0 );
    fdlOmitHeader.top = new FormAttachment( wEmptyRows, margin );
    fdlOmitHeader.right = new FormAttachment( middle, -margin );
    wlOmitHeader.setLayoutData( fdlOmitHeader );
    wOmitHeader = new Button( writeToExistingGroup, SWT.CHECK );
    wOmitHeader.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.OmitHeader.Tooltip" ) );
    FormData fdOmitHeader = new FormData();
    fdOmitHeader.left = new FormAttachment( middle, 0 );
    fdOmitHeader.top = new FormAttachment( wEmptyRows, margin );
    fdOmitHeader.right = new FormAttachment( 100, 0 );
    wOmitHeader.setLayoutData( fdOmitHeader );
    wOmitHeader.addSelectionListener( lsSel );

    FormData fdWriteToExistingGroup = new FormData();
    fdWriteToExistingGroup.left = new FormAttachment( 0, margin );
    fdWriteToExistingGroup.top = new FormAttachment( wContentGroup, margin );
    fdWriteToExistingGroup.right = new FormAttachment( 100, -margin );
    writeToExistingGroup.setLayoutData( fdWriteToExistingGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Write to existing Group GROUP
    // ///////////////////////////////////////////////////////////

    Group fieldGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    fieldGroup.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.fieldGroup.Label" ) );

    FormLayout fieldGroupgroupLayout = new FormLayout();
    fieldGroupgroupLayout.marginWidth = 10;
    fieldGroupgroupLayout.marginHeight = 10;
    fieldGroup.setLayout( fieldGroupgroupLayout );

    wGet = new Button( fieldGroup, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );
    wGet.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.GetFields" ) );

    wMinWidth = new Button( fieldGroup, SWT.PUSH );
    wMinWidth.setText( BaseMessages.getString( PKG, "ExcelWriterDialog.MinWidth.Button" ) );
    wMinWidth.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.MinWidth.Tooltip" ) );

    setButtonPositions( new Button[] { wGet, wMinWidth }, margin, null );

    final int FieldsRows = input.getOutputFields().length;

    // Prepare a list of possible formats, filtering reserved internal formats away
    String[] formats = BuiltinFormats.getAll();

    List<String> allFormats = Arrays.asList( BuiltinFormats.getAll() );
    List<String> nonReservedFormats = new ArrayList<String>( allFormats.size() );

    for ( String format : allFormats ) {
      if ( !format.startsWith( "reserved" ) ) {
        nonReservedFormats.add( format );
      }
    }

    Collections.sort( nonReservedFormats );
    formats = nonReservedFormats.toArray( new String[0] );

    colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.NameColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.TypeColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.FormatColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, formats ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.UseStyleCell.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.TitleColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.UseTitleStyleCell.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.FormulaField.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "N", "Y" }, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.HyperLinkField.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.CommentField.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelWriterDialog.CommentAuthor.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ) };

    wFields =
      new TableView(
        transMeta, fieldGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wGet, -margin );
    wFields.setLayoutData( fdFields );
    wFields.addModifyListener( lsMod );

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

    FormData fdFieldGroup = new FormData();
    fdFieldGroup.left = new FormAttachment( 0, margin );
    fdFieldGroup.top = new FormAttachment( writeToExistingGroup, margin );
    fdFieldGroup.bottom = new FormAttachment( 100, 0 );
    fdFieldGroup.right = new FormAttachment( 100, -margin );
    fieldGroup.setLayoutData( fdFieldGroup );

    fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment( 0, 0 );
    fdContentComp.top = new FormAttachment( 0, 0 );
    fdContentComp.right = new FormAttachment( 100, 0 );
    fdContentComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( fdContentComp );

    wContentComp.layout();
    wContentTab.setControl( wContentComp );

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( 0, 0 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, 0 );
    wTabFolder.setLayoutData( fdTabFolder );

    FormData fdSc = new FormData();
    fdSc.left = new FormAttachment( 0, 0 );
    fdSc.top = new FormAttachment( wStepname, margin );
    fdSc.right = new FormAttachment( 100, 0 );
    fdSc.bottom = new FormAttachment( 100, -50 );
    sc.setLayoutData( fdSc );

    sc.setContent( wTabFolder );
  }

  private void createContentGroup( SelectionAdapter lsSel, ModifyListener lsMod, int middle, int margin,
      Group wContentGroup ) {
    // starting cell
    Label wlStartingCell = createLabel( wContentGroup, "ExcelWriterDialog.StartingCell.Label" );
    wStartingCell = new TextVar( transMeta, wContentGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStartingCell.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.StartingCell.Tooltip" ) );
    wStartingCell.addModifyListener( lsMod );
    Control lastWidget = layoutLabelControlPair( wlStartingCell, wStartingCell );

    // row writing method line
    Label wlRowWritingMethod = createLabel( wContentGroup, "ExcelWriterDialog.RowWritingMethod.Label" );
    wRowWritingMethod = new CCombo( wContentGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY );
    lastWidget = layoutLabelControlPair( wlRowWritingMethod, wRowWritingMethod, lastWidget );

    String overwriteLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.RowWritingMethod.Overwrite.Label" );
    String pushDownLabel = BaseMessages.getString( PKG, "ExcelWriterDialog.RowWritingMethod.PushDown.Label" );
    wRowWritingMethod.setItems( new String[] { overwriteLabel, pushDownLabel } );
    wRowWritingMethod.setData( overwriteLabel, ExcelWriterStepMeta.ROW_WRITE_OVERWRITE );
    wRowWritingMethod.setData( pushDownLabel, ExcelWriterStepMeta.ROW_WRITE_PUSH_DOWN );
    wRowWritingMethod.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.RowWritingMethod.Tooltip" ) );
    wRowWritingMethod.addModifyListener( lsMod );

    Label wlHeader = createLabel( wContentGroup, "ExcelWriterDialog.Header.Label" );

    wHeader = new Button( wContentGroup, SWT.CHECK );
    wHeader.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Header.Tooltip" ) );
    wHeader.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableHeader();
      }
    } );
    lastWidget = layoutLabelControlPair( wlHeader, wHeader, lastWidget );

    Label wlFooter = createLabel( wContentGroup, "ExcelWriterDialog.Footer.Label" );
    wFooter = new Button( wContentGroup, SWT.CHECK );
    lastWidget = layoutLabelControlPair( wlFooter, wFooter, lastWidget );
    wFooter.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.Footer.Tooltip" ) );
    wFooter.addSelectionListener( lsSel );

    // auto size columns?
    Label wlAutoSize = createLabel( wContentGroup, "ExcelWriterDialog.AutoSize.Label", "ExcelWriterDialog.AutoSize.Tooltip" );
    wAutoSize = new Button( wContentGroup, SWT.CHECK );
    wAutoSize.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.AutoSize.Tooltip" ) );
    lastWidget = layoutLabelControlPair( wlAutoSize, wAutoSize, lastWidget );
    wAutoSize.addSelectionListener( lsSel );

    Label wlRetainNullValues = createLabel( wContentGroup, "ExcelWriterDialog.NullIsBlank.Label" );
    wRetainNullValues = new Button( wContentGroup, SWT.CHECK );
    wRetainNullValues.setToolTipText( BaseMessages.getString( PKG, "ExcelWriterDialog.NullIsBlank.Tooltip" ) );
    wRetainNullValues.addSelectionListener( lsSel );
    lastWidget = layoutLabelControlPair( wlRetainNullValues, wRetainNullValues, lastWidget );

    // force formula recalculation?
    Label wlForceFormulaRecalculation = createLabel(wContentGroup, "ExcelWriterDialog.ForceFormulaRecalculation.Label" );
    wForceFormulaRecalculation = new Button( wContentGroup, SWT.CHECK );
    wForceFormulaRecalculation.setToolTipText( BaseMessages.getString(
      PKG, "ExcelWriterDialog.ForceFormulaRecalculation.Tooltip" ) );
    lastWidget = layoutLabelControlPair(wlForceFormulaRecalculation, wForceFormulaRecalculation, lastWidget );
    wForceFormulaRecalculation.addSelectionListener( lsSel );

    // leave existing styles alone?
    Label wlLeaveExistingStylesUnchanged = createLabel( wContentGroup, "ExcelWriterDialog.LeaveExistingStylesUnchanged.Label", "ExcelWriterDialog.LeaveExistingStylesUnchanged.Tooltip" );
    wLeaveExistingStylesUnchanged = new Button( wContentGroup, SWT.CHECK );
    wLeaveExistingStylesUnchanged.setToolTipText( BaseMessages.getString(
      PKG, "ExcelWriterDialog.LeaveExistingStylesUnchanged.Tooltip" ) );
    lastWidget = layoutLabelControlPair( wlLeaveExistingStylesUnchanged, wLeaveExistingStylesUnchanged, lastWidget );
    wLeaveExistingStylesUnchanged.addSelectionListener( lsSel );

    // extend data validation
    Label lblExtendDataValidation = createLabel( wContentGroup, "ExcelWriterDialog.ExtendDataValidation.Label" );
    wExtendDataValidation = new Button( wContentGroup, SWT.CHECK );
    wExtendDataValidation.setToolTipText( getMsg( "ExcelWriterDialog.Injection.EXTEND_DATA_VALIDATION" ) );
    lastWidget = layoutLabelControlPair( lblExtendDataValidation, wExtendDataValidation, lastWidget );
    wExtendDataValidation.addSelectionListener( lsSel );

  }

  private String getMsg( String key ) {
    return BaseMessages.getString( PKG, key );
  }

  private void enableAppend() {
    wSplitEvery.setEnabled( !wAppendLines.getSelection() );
  }

  private void enableHeader() {
    wOmitHeader.setEnabled( wHeader.getSelection() );
  }

  private void setDateTimeFormat() {
    if ( wSpecifyFormat.getSelection() ) {
      wAddDate.setSelection( false );
      wAddTime.setSelection( false );
    }

    wDateTimeFormat.setEnabled( wSpecifyFormat.getSelection() );
    wlDateTimeFormat.setEnabled( wSpecifyFormat.getSelection() );
    wAddDate.setEnabled( !wSpecifyFormat.getSelection() );
    wlAddDate.setEnabled( !wSpecifyFormat.getSelection() );
    wAddTime.setEnabled( !wSpecifyFormat.getSelection() );
    wlAddTime.setEnabled( !wSpecifyFormat.getSelection() );

  }


  private Label createLabel( Composite parent, String labelKey ) {
    return createLabel( parent, labelKey, Optional.empty() );
  }

  private Label createLabel( Composite parent, String labelKey, String tooltipKey ) {
    return createLabel( parent, labelKey, Optional.of( tooltipKey ) );
  }

  private Label createLabel( Composite parent, String labelKey, Optional<String> tooltipKey ) {
    Label label = new Label(parent, SWT.RIGHT );
    label.setText( BaseMessages.getString( PKG, labelKey ) );
    tooltipKey.ifPresent( tooltip -> label.setToolTipText(BaseMessages.getString( PKG, tooltip ) ) );
    return label;
  }

  private Control layoutLabelControlPair( Label label, Control control, Control above ) {
    return layoutLabelControlPair( label, control, Optional.of( above ) );
  }

  private Control layoutLabelControlPair( Label label, Control control ) {
    return layoutLabelControlPair( label, control, Optional.empty() );
  }

  private Control layoutLabelControlPair( Label label, Control control, Optional<Control> above ) {
    int margin = Const.MARGIN;
    FormDataBuilder fdbLabel = fd().left().right( middle, -margin );
    setTop( fdbLabel, above, margin );
    label.setLayoutData( fdbLabel.result() );
    FormDataBuilder fdControl = fd().left( middle, 0 ).right();
    setTop( fdControl, above, margin );
    control.setLayoutData( fdControl.result() );
    return control;
  }

  private FormDataBuilder setTop( FormDataBuilder fdb, Optional<Control> above, int margin ) {
    return above.isPresent() ? fdb.top( above.get(), margin ) : fdb.top( 0, margin );
  }

  private FormDataBuilder fd() {
    return new FormDataBuilder();
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
    colinf[7].setComboValues( fieldNames );
    colinf[8].setComboValues( fieldNames );
    colinf[9].setComboValues( fieldNames );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getFileName() != null ) {
      wFilename.setText( input.getFileName() );
    }
    wDoNotOpenNewFileInit.setSelection( input.isDoNotOpenNewFileInit() );
    if ( input.getExtension() != null ) {

      if ( input.getExtension().equals( "xlsx" ) ) {
        wExtension.select( 1 );
      } else {
        wExtension.select( 0 );
      }

    }
    wCreateParentFolder.setSelection( input.isCreateParentFolders() );
    wStreamData.setSelection( input.isStreamingData() );
    wSplitEvery.setText( "" + input.getSplitEvery() );
    wEmptyRows.setText( "" + input.getAppendEmpty() );
    wSkipRows.setText( "" + input.getAppendOffset() );
    wAppendLines.setSelection( input.isAppendLines() );
    wHeader.setSelection( input.isHeaderEnabled() );
    wFooter.setSelection( input.isFooterEnabled() );
    wOmitHeader.setSelection( input.isAppendOmitHeader() );
    wForceFormulaRecalculation.setSelection( input.isForceFormulaRecalculation() );
    wLeaveExistingStylesUnchanged.setSelection( input.isLeaveExistingStylesUnchanged() );
    wExtendDataValidation.setSelection( input.isExtendDataValidationRanges() );

    if ( input.getStartingCell() != null ) {
      wStartingCell.setText( input.getStartingCell() );
    }

    wAddDate.setSelection( input.isDateInFilename() );
    wAddTime.setSelection( input.isTimeInFilename() );

    if ( input.getDateTimeFormat() != null ) {
      wDateTimeFormat.setText( input.getDateTimeFormat() );
    }
    wSpecifyFormat.setSelection( input.isSpecifyFormat() );

    wAddToResult.setSelection( input.isAddToResultFiles() );
    wAutoSize.setSelection( input.isAutoSizeColums() );
    wRetainNullValues.setSelection( input.isRetainNullValues() );
    wIfFileExists.select( ExcelWriterStepMeta.IF_FILE_EXISTS_REUSE.equals( input.getIfFileExists() ) ? 1 : 0 );
    wIfSheetExists.select( ExcelWriterStepMeta.IF_SHEET_EXISTS_REUSE.equals( input.getIfSheetExists() ) ? 1 : 0 );
    wRowWritingMethod.select( ExcelWriterStepMeta.ROW_WRITE_PUSH_DOWN.equals( input.getRowWritingMethod() )
      ? 1 : 0 );

    wAddStepnr.setSelection( input.isStepNrInFilename() );
    wMakeActiveSheet.setSelection( input.isMakeSheetActive() );
    wTemplate.setSelection( input.isTemplateEnabled() );
    wTemplateSheet.setSelection( input.isTemplateSheetEnabled() );

    if ( input.getTemplateFileName() != null ) {
      wTemplateFilename.setText( input.getTemplateFileName() );
    }

    if ( input.getTemplateSheetName() != null ) {
      wTemplateSheetname.setText( input.getTemplateSheetName() );
    }

    if ( input.getSheetname() != null ) {
      wSheetname.setText( input.getSheetname() );
    } else {
      wSheetname.setText( "Sheet1" );
    }
    wTemplateSheetHide.setSelection( input.isTemplateSheetHidden() );
    wProtectSheet.setSelection( input.isSheetProtected() );

    enablePassword();
    enableTemplate();

    if ( input.getPassword() != null ) {
      wPassword.setText( input.getPassword() );
    }
    if ( input.getProtectedBy() != null ) {
      wProtectedBy.setText( input.getProtectedBy() );
    }

    logDebug( "getting fields info..." );

    for ( int i = 0; i < input.getOutputFields().length; i++ ) {
      ExcelWriterStepField field = input.getOutputFields()[i];

      TableItem item = wFields.table.getItem( i );
      if ( field.getName() != null ) {
        item.setText( 1, field.getName() );
      }
      item.setText( 2, field.getTypeDesc() );

      if ( field.getFormat() != null ) {
        item.setText( 3, field.getFormat() );
      }
      if ( field.getStyleCell() != null ) {
        item.setText( 4, field.getStyleCell() );
      }
      if ( field.getTitle() != null ) {
        item.setText( 5, field.getTitle() );
      }
      if ( field.getTitleStyleCell() != null ) {
        item.setText( 6, field.getTitleStyleCell() );
      }
      if ( field.isFormula() ) {
        item.setText( 7, "Y" );
      } else {
        item.setText( 7, "N" );
      }

      if ( field.getHyperlinkField() != null ) {
        item.setText( 8, field.getHyperlinkField() );
      }
      if ( field.getCommentField() != null ) {
        item.setText( 9, field.getCommentField() );
      }
      if ( field.getCommentAuthorField() != null ) {
        item.setText( 10, field.getCommentAuthorField() );
      }

    }

    wFields.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;

    input.setChanged( backupChanged );

    dispose();
  }

  private void getInfo( ExcelWriterStepMeta tfoi ) {
    tfoi.setFileName( wFilename.getText() );
    tfoi.setCreateParentFolders( wCreateParentFolder.getSelection() );
    tfoi.setStreamingData( wStreamData.getSelection() );
    tfoi.setDoNotOpenNewFileInit( wDoNotOpenNewFileInit.getSelection() );
    tfoi.setAppendOmitHeader( wOmitHeader.getSelection() );
    tfoi.setExtension( (String) wExtension.getData( wExtension.getText() ) );
    tfoi.setSplitEvery( Const.toInt( wSplitEvery.getText(), 0 ) );
    tfoi.setAppendOffset( Const.toInt( wSkipRows.getText(), 0 ) );
    tfoi.setAppendEmpty( Const.toInt( wEmptyRows.getText(), 0 ) );
    tfoi.setAppendLines( wAppendLines.getSelection() );
    tfoi.setHeaderEnabled( wHeader.getSelection() );
    tfoi.setFooterEnabled( wFooter.getSelection() );
    tfoi.setStartingCell( wStartingCell.getText() );
    tfoi.setStepNrInFilename( wAddStepnr.getSelection() );
    tfoi.setDateInFilename( wAddDate.getSelection() );
    tfoi.setTimeInFilename( wAddTime.getSelection() );
    tfoi.setIfFileExists( (String) wIfFileExists.getData( wIfFileExists.getText() ) );
    tfoi.setIfSheetExists( (String) wIfSheetExists.getData( wIfSheetExists.getText() ) );
    tfoi.setRowWritingMethod( (String) wRowWritingMethod.getData( wRowWritingMethod.getText() ) );
    tfoi.setForceFormulaRecalculation( wForceFormulaRecalculation.getSelection() );
    tfoi.setLeaveExistingStylesUnchanged( wLeaveExistingStylesUnchanged.getSelection() );
    tfoi.setExtendDataValidationRanges( wExtendDataValidation.getSelection() );

    tfoi.setDateTimeFormat( wDateTimeFormat.getText() );
    tfoi.setSpecifyFormat( wSpecifyFormat.getSelection() );
    tfoi.setAutoSizeColums( wAutoSize.getSelection() );
    tfoi.setRetainNullValues( wRetainNullValues.getSelection() );

    tfoi.setAddToResultFiles( wAddToResult.getSelection() );

    tfoi.setMakeSheetActive( wMakeActiveSheet.getSelection() );
    tfoi.setProtectSheet( wProtectSheet.getSelection() );
    tfoi.setProtectedBy( wProtectedBy.getText() );
    tfoi.setPassword( wPassword.getText() );

    tfoi.setTemplateEnabled( wTemplate.getSelection() );
    tfoi.setTemplateSheetEnabled( wTemplateSheet.getSelection() );
    tfoi.setTemplateFileName( wTemplateFilename.getText() );
    tfoi.setTemplateSheetName( wTemplateSheetname.getText() );
    tfoi.setTemplateSheetHidden( wTemplateSheetHide.getSelection() );

    if ( wSheetname.getText() != null ) {
      tfoi.setSheetname( wSheetname.getText() );
    } else {
      tfoi.setSheetname( "Sheet 1" );
    }

    int nrfields = wFields.nrNonEmpty();

    tfoi.allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      ExcelWriterStepField field = new ExcelWriterStepField();

      TableItem item = wFields.getNonEmpty( i );
      field.setName( item.getText( 1 ) );
      field.setType( item.getText( 2 ) );
      field.setFormat( item.getText( 3 ) );
      field.setStyleCell( item.getText( 4 ) );
      field.setTitle( item.getText( 5 ) );
      field.setTitleStyleCell( item.getText( 6 ) );
      field.setFormula( item.getText( 7 ).equalsIgnoreCase( "Y" ) );
      field.setHyperlinkField( item.getText( 8 ) );
      field.setCommentField( item.getText( 9 ) );
      field.setCommentAuthorField( item.getText( 10 ) );

      //CHECKSTYLE:Indentation:OFF
      tfoi.getOutputFields()[i] = field;
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

  private void enablePassword() {
    wPassword.setEnabled( wProtectSheet.getSelection() );
    wProtectedBy.setEnabled( wProtectSheet.getSelection() );
  }

  private void enableTemplate() {
    wbTemplateFilename.setEnabled( wTemplate.getSelection() );
    wTemplateFilename.setEnabled( wTemplate.getSelection() );
  }

  private void enableTemplateSheet() {
    wTemplateSheetname.setEnabled( wTemplateSheet.getSelection() );
    wTemplateSheetHide.setEnabled( wTemplateSheet.getSelection() );
  }

  private void enableExtension() {
    wProtectSheet.setEnabled( wExtension.getSelectionIndex() == 0 );
    if ( wExtension.getSelectionIndex() == 0 ) {
      wPassword.setEnabled( wProtectSheet.getSelection() );
      wProtectedBy.setEnabled( wProtectSheet.getSelection() );
      wStreamData.setEnabled( false );
    } else {
      wPassword.setEnabled( false );
      wProtectedBy.setEnabled( false );
      wStreamData.setEnabled( true );
    }
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
        BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1, 5 }, new int[] { 2 }, 0, 0, listener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Title" ), BaseMessages
        .getString( PKG, "System.Dialog.GetFieldsFailed.Message" ), ke );
    }

  }

  /**
   * Sets the output width to minimal width...
   *
   */
  public void setMinimalWidth() {
    int nrNonEmptyFields = wFields.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );

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
    wFields.optWidth( true );
  }
}
