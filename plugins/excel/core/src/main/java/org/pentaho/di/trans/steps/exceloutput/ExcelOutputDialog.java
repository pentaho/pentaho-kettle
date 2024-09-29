/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.exceloutput;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
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
import org.pentaho.di.ui.util.DialogHelper;

@PluginDialog( id = "ExcelOutput", image = "ui/images/deprecated.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/microsoft-excel-output" )
public class ExcelOutputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ExcelOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private Label wlTempDirectory;
  private TextVar wTempDirectory;
  private FormData fdlTempDirectory, fdTempDirectory;

  private Button wbTempDir;
  private FormData fdbTempDir;

  private Group wFontHeaderGroup;
  private FormData fdFontHeaderGroup;

  private Label wlHeaderFontName;
  private CCombo wHeaderFontName;
  private FormData fdlHeaderFontName, fdHeaderFontName;

  private Label wlHeaderFontSize;
  private TextVar wHeaderFontSize;
  private FormData fdlHeaderFontSize, fdHeaderFontSize;

  private Label wlRowFontSize;
  private TextVar wRowFontSize;
  private FormData fdlRowFontSize, fdRowFontSize;

  private CCombo wRowFontColor;
  private FormData fdRowFontColor;
  private Label wlRowFontColor;
  private FormData fdlRowFontColor;

  private Label wlImage;
  private Button wbImage;
  private TextVar wImage;
  private FormData fdlImage, fdbImage, fdImage;

  private Label wlHeaderRowHeight;
  private TextVar wHeaderRowHeight;
  private FormData fdlHeaderRowHeight, fdHeaderRowHeight;

  private Label wlRowFontName;
  private CCombo wRowFontName;
  private FormData fdlRowFontName, fdRowFontName;

  private Label wlHeaderFontUnderline;
  private CCombo wHeaderFontUnderline;
  private FormData fdlHeaderFontUnderline, fdHeaderFontUnderline;

  private Label wlHeaderFontOrientation;
  private CCombo wHeaderFontOrientation;
  private FormData fdlHeaderFontOrientation, fdHeaderFontOrientation;

  private Label wlHeaderAlignment;
  private CCombo wHeaderAlignment;
  private FormData fdlHeaderAlignment, fdHeaderAlignment;

  private Group wFontRowGroup;

  private CTabItem wFileTab, wContentTab, wCustomTab, wFieldsTab;

  private FormData fdFileComp, fdContentComp, fdFieldsComp, fdCustomComp;

  private Label wlFilename;
  private Button wbFilename;
  private TextVar wFilename;
  private FormData fdlFilename, fdbFilename, fdFilename;

  private Label wlExtension;
  private TextVar wExtension;
  private FormData fdlExtension, fdExtension;

  private Label wlAddStepnr;
  private Button wAddStepnr;
  private FormData fdlAddStepnr, fdAddStepnr;

  private Label wlAddDate;
  private Button wAddDate;
  private FormData fdlAddDate, fdAddDate;

  private Label wlHeaderFontBold;
  private Button wHeaderFontBold;
  private FormData fdlHeaderFontBold, fdHeaderFontBold;

  private Label wlHeaderFontItalic;
  private Button wHeaderFontItalic;
  private FormData fdlHeaderFontItalic, fdHeaderFontItalic;

  private CCombo wHeaderFontColor;
  private FormData fdHeaderFontColor;
  private Label wlHeaderFontColor;
  private FormData fdlHeaderFontColor;

  private CCombo wHeaderBackGroundColor;
  private FormData fdHeaderBackGroundColor;
  private Label wlHeaderBackGroundColor;
  private FormData fdlHeaderBackGroundColor;

  private CCombo wRowBackGroundColor;
  private FormData fdFontRowGroup;
  private FormData fdRowBackGroundColor;
  private Label wlRowBackGroundColor;
  private FormData fdlRowBackGroundColor;

  private Label wlAddTime;
  private Button wAddTime;
  private FormData fdlAddTime, fdAddTime;

  private Label wlProtectSheet;
  private Button wProtectSheet;
  private FormData fdlProtectSheet, fdProtectSheet;

  private Button wbShowFiles;
  private FormData fdbShowFiles;

  private Label wlHeader;
  private Button wHeader;
  private FormData fdlHeader, fdHeader;

  private Label wlFooter;
  private Button wFooter;
  private FormData fdlFooter, fdFooter;

  private Label wlEncoding;
  private CCombo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  private Label wlSplitEvery;
  private Text wSplitEvery;
  private FormData fdlSplitEvery, fdSplitEvery;

  private Label wlTemplate;
  private Button wTemplate;
  private FormData fdlTemplate, fdTemplate;

  private Label wlTemplateAppend;
  private Button wTemplateAppend;
  private FormData fdlTemplateAppend, fdTemplateAppend;

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

  private ExcelOutputMeta input;

  private Button wMinWidth;
  private Listener lsMinWidth;
  private boolean gotEncodings = false;

  private Label wlAddToResult;
  private Button wAddToResult;
  private FormData fdlAddToResult, fdAddToResult;

  private Label wlAppend;
  private Button wAppend;
  private FormData fdlAppend, fdAppend;

  private Label wlDoNotOpenNewFileInit;
  private Button wDoNotOpenNewFileInit;
  private FormData fdlDoNotOpenNewFileInit, fdDoNotOpenNewFileInit;

  private Label wlSpecifyFormat;
  private Button wSpecifyFormat;
  private FormData fdlSpecifyFormat, fdSpecifyFormat;

  private Label wlDateTimeFormat;
  private CCombo wDateTimeFormat;
  private FormData fdlDateTimeFormat, fdDateTimeFormat;

  private Label wlAutoSize;
  private Button wAutoSize;
  private FormData fdlAutoSize, fdAutoSize;

  private Label wlNullIsBlank;
  private Button wNullIsBlank;
  private FormData fdlNullIsBlank, fdNullIsBlank;

  private Group wTemplateGroup;
  private FormData fdTemplateGroup;

  private Label wluseTempFiles;
  private Button wuseTempFiles;
  private FormData fdluseTempFiles, fduseTempFiles;

  private Label wlCreateParentFolder;
  private Button wCreateParentFolder;
  private FormData fdlCreateParentFolder, fdCreateParentFolder;

  private ColumnInfo[] colinf;

  private Map<String, Integer> inputFields;

  public ExcelOutputDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (ExcelOutputMeta) in;
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
    shell.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.DialogTitle" ) );

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

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF FILE TAB///
    // /
    wFileTab = new CTabItem( wTabFolder, SWT.NONE );
    wFileTab.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.FileTab.TabTitle" ) );

    Composite wFileComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFileComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFileComp.setLayout( fileLayout );

    // Filename line
    wlFilename = new Label( wFileComp, SWT.RIGHT );
    wlFilename.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Filename.Label" ) );
    props.setLook( wlFilename );
    fdlFilename = new FormData();
    fdlFilename.left = new FormAttachment( 0, 0 );
    fdlFilename.top = new FormAttachment( 0, margin );
    fdlFilename.right = new FormAttachment( middle, -margin );
    wlFilename.setLayoutData( fdlFilename );

    wbFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbFilename );
    wbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( 0, 0 );
    wbFilename.setLayoutData( fdbFilename );

    wFilename = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilename );
    wFilename.addModifyListener( lsMod );
    fdFilename = new FormData();
    fdFilename.left = new FormAttachment( middle, 0 );
    fdFilename.top = new FormAttachment( 0, margin );
    fdFilename.right = new FormAttachment( wbFilename, -margin );
    wFilename.setLayoutData( fdFilename );

    // Create Parent Folder
    wlCreateParentFolder = new Label( wFileComp, SWT.RIGHT );
    wlCreateParentFolder.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.CreateParentFolder.Label" ) );
    props.setLook( wlCreateParentFolder );
    fdlCreateParentFolder = new FormData();
    fdlCreateParentFolder.left = new FormAttachment( 0, 0 );
    fdlCreateParentFolder.top = new FormAttachment( wFilename, margin );
    fdlCreateParentFolder.right = new FormAttachment( middle, -margin );
    wlCreateParentFolder.setLayoutData( fdlCreateParentFolder );
    wCreateParentFolder = new Button( wFileComp, SWT.CHECK );
    wCreateParentFolder.setToolTipText( BaseMessages.getString(
      PKG, "ExcelOutputDialog.CreateParentFolder.Tooltip" ) );
    props.setLook( wCreateParentFolder );
    fdCreateParentFolder = new FormData();
    fdCreateParentFolder.left = new FormAttachment( middle, 0 );
    fdCreateParentFolder.top = new FormAttachment( wFilename, margin );
    fdCreateParentFolder.right = new FormAttachment( 100, 0 );
    wCreateParentFolder.setLayoutData( fdCreateParentFolder );
    wCreateParentFolder.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Open new File at Init
    wlDoNotOpenNewFileInit = new Label( wFileComp, SWT.RIGHT );
    wlDoNotOpenNewFileInit.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.DoNotOpenNewFileInit.Label" ) );
    props.setLook( wlDoNotOpenNewFileInit );
    fdlDoNotOpenNewFileInit = new FormData();
    fdlDoNotOpenNewFileInit.left = new FormAttachment( 0, 0 );
    fdlDoNotOpenNewFileInit.top = new FormAttachment( wCreateParentFolder, margin );
    fdlDoNotOpenNewFileInit.right = new FormAttachment( middle, -margin );
    wlDoNotOpenNewFileInit.setLayoutData( fdlDoNotOpenNewFileInit );
    wDoNotOpenNewFileInit = new Button( wFileComp, SWT.CHECK );
    wDoNotOpenNewFileInit.setToolTipText( BaseMessages.getString(
      PKG, "ExcelOutputDialog.DoNotOpenNewFileInit.Tooltip" ) );
    props.setLook( wDoNotOpenNewFileInit );
    fdDoNotOpenNewFileInit = new FormData();
    fdDoNotOpenNewFileInit.left = new FormAttachment( middle, 0 );
    fdDoNotOpenNewFileInit.top = new FormAttachment( wCreateParentFolder, margin );
    fdDoNotOpenNewFileInit.right = new FormAttachment( 100, 0 );
    wDoNotOpenNewFileInit.setLayoutData( fdDoNotOpenNewFileInit );
    wDoNotOpenNewFileInit.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Extension line
    wlExtension = new Label( wFileComp, SWT.RIGHT );
    wlExtension.setText( BaseMessages.getString( PKG, "System.Label.Extension" ) );
    props.setLook( wlExtension );
    fdlExtension = new FormData();
    fdlExtension.left = new FormAttachment( 0, 0 );
    fdlExtension.top = new FormAttachment( wDoNotOpenNewFileInit, margin );
    fdlExtension.right = new FormAttachment( middle, -margin );
    wlExtension.setLayoutData( fdlExtension );
    wExtension = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wExtension.setText( "" );
    props.setLook( wExtension );
    wExtension.addModifyListener( lsMod );
    fdExtension = new FormData();
    fdExtension.left = new FormAttachment( middle, 0 );
    fdExtension.top = new FormAttachment( wDoNotOpenNewFileInit, margin );
    fdExtension.right = new FormAttachment( wbFilename, -margin );
    wExtension.setLayoutData( fdExtension );

    // Create multi-part file?
    wlAddStepnr = new Label( wFileComp, SWT.RIGHT );
    wlAddStepnr.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.AddStepnr.Label" ) );
    props.setLook( wlAddStepnr );
    fdlAddStepnr = new FormData();
    fdlAddStepnr.left = new FormAttachment( 0, 0 );
    fdlAddStepnr.top = new FormAttachment( wExtension, margin );
    fdlAddStepnr.right = new FormAttachment( middle, -margin );
    wlAddStepnr.setLayoutData( fdlAddStepnr );
    wAddStepnr = new Button( wFileComp, SWT.CHECK );
    props.setLook( wAddStepnr );
    fdAddStepnr = new FormData();
    fdAddStepnr.left = new FormAttachment( middle, 0 );
    fdAddStepnr.top = new FormAttachment( wExtension, margin );
    fdAddStepnr.right = new FormAttachment( 100, 0 );
    wAddStepnr.setLayoutData( fdAddStepnr );
    wAddStepnr.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Create multi-part file?
    wlAddDate = new Label( wFileComp, SWT.RIGHT );
    wlAddDate.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.AddDate.Label" ) );
    props.setLook( wlAddDate );
    fdlAddDate = new FormData();
    fdlAddDate.left = new FormAttachment( 0, 0 );
    fdlAddDate.top = new FormAttachment( wAddStepnr, margin );
    fdlAddDate.right = new FormAttachment( middle, -margin );
    wlAddDate.setLayoutData( fdlAddDate );
    wAddDate = new Button( wFileComp, SWT.CHECK );
    props.setLook( wAddDate );
    fdAddDate = new FormData();
    fdAddDate.left = new FormAttachment( middle, 0 );
    fdAddDate.top = new FormAttachment( wAddStepnr, margin );
    fdAddDate.right = new FormAttachment( 100, 0 );
    wAddDate.setLayoutData( fdAddDate );
    wAddDate.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        // System.out.println("wAddDate.getSelection()="+wAddDate.getSelection());
      }
    } );
    // Create multi-part file?
    wlAddTime = new Label( wFileComp, SWT.RIGHT );
    wlAddTime.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.AddTime.Label" ) );
    props.setLook( wlAddTime );
    fdlAddTime = new FormData();
    fdlAddTime.left = new FormAttachment( 0, 0 );
    fdlAddTime.top = new FormAttachment( wAddDate, margin );
    fdlAddTime.right = new FormAttachment( middle, -margin );
    wlAddTime.setLayoutData( fdlAddTime );
    wAddTime = new Button( wFileComp, SWT.CHECK );
    props.setLook( wAddTime );
    fdAddTime = new FormData();
    fdAddTime.left = new FormAttachment( middle, 0 );
    fdAddTime.top = new FormAttachment( wAddDate, margin );
    fdAddTime.right = new FormAttachment( 100, 0 );
    wAddTime.setLayoutData( fdAddTime );
    wAddTime.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );
    // Specify date time format?
    wlSpecifyFormat = new Label( wFileComp, SWT.RIGHT );
    wlSpecifyFormat.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.SpecifyFormat.Label" ) );
    props.setLook( wlSpecifyFormat );
    fdlSpecifyFormat = new FormData();
    fdlSpecifyFormat.left = new FormAttachment( 0, 0 );
    fdlSpecifyFormat.top = new FormAttachment( wAddTime, margin );
    fdlSpecifyFormat.right = new FormAttachment( middle, -margin );
    wlSpecifyFormat.setLayoutData( fdlSpecifyFormat );
    wSpecifyFormat = new Button( wFileComp, SWT.CHECK );
    props.setLook( wSpecifyFormat );
    wSpecifyFormat.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.SpecifyFormat.Tooltip" ) );
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
    wlDateTimeFormat = new Label( wFileComp, SWT.RIGHT );
    wlDateTimeFormat.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.DateTimeFormat.Label" ) );
    props.setLook( wlDateTimeFormat );
    fdlDateTimeFormat = new FormData();
    fdlDateTimeFormat.left = new FormAttachment( 0, 0 );
    fdlDateTimeFormat.top = new FormAttachment( wSpecifyFormat, margin );
    fdlDateTimeFormat.right = new FormAttachment( middle, -margin );
    wlDateTimeFormat.setLayoutData( fdlDateTimeFormat );
    wDateTimeFormat = new CCombo( wFileComp, SWT.BORDER | SWT.READ_ONLY );
    wDateTimeFormat.setEditable( true );
    props.setLook( wDateTimeFormat );
    wDateTimeFormat.addModifyListener( lsMod );
    fdDateTimeFormat = new FormData();
    fdDateTimeFormat.left = new FormAttachment( middle, 0 );
    fdDateTimeFormat.top = new FormAttachment( wSpecifyFormat, margin );
    fdDateTimeFormat.right = new FormAttachment( 100, 0 );
    wDateTimeFormat.setLayoutData( fdDateTimeFormat );
    for ( int x = 0; x < dats.length; x++ ) {
      wDateTimeFormat.add( dats[x] );
    }

    wbShowFiles = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbShowFiles );
    wbShowFiles.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.ShowFiles.Button" ) );
    fdbShowFiles = new FormData();
    fdbShowFiles.left = new FormAttachment( middle, 0 );
    fdbShowFiles.top = new FormAttachment( wDateTimeFormat, margin * 3 );
    wbShowFiles.setLayoutData( fdbShowFiles );
    wbShowFiles.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        ExcelOutputMeta tfoi = new ExcelOutputMeta();
        getInfo( tfoi );
        String[] files = tfoi.getFiles( transMeta );
        if ( files != null && files.length > 0 ) {
          EnterSelectionDialog esd =
            new EnterSelectionDialog( shell, files,
              BaseMessages.getString( PKG, "ExcelOutputDialog.SelectOutputFiles.DialogTitle" ),
              BaseMessages.getString( PKG, "ExcelOutputDialog.SelectOutputFiles.DialogMessage" ) );
          esd.setViewOnly();
          esd.open();
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "ExcelOutputDialog.NoFilesFound.DialogMessage" ) );
          mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
          mb.open();
        }
      }
    } );

    // Add File to the result files name
    wlAddToResult = new Label( wFileComp, SWT.RIGHT );
    wlAddToResult.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.AddFileToResult.Label" ) );
    props.setLook( wlAddToResult );
    fdlAddToResult = new FormData();
    fdlAddToResult.left = new FormAttachment( 0, 0 );
    fdlAddToResult.top = new FormAttachment( wbShowFiles, 2 * margin );
    fdlAddToResult.right = new FormAttachment( middle, -margin );
    wlAddToResult.setLayoutData( fdlAddToResult );
    wAddToResult = new Button( wFileComp, SWT.CHECK );
    wAddToResult.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.AddFileToResult.Tooltip" ) );
    props.setLook( wAddToResult );
    fdAddToResult = new FormData();
    fdAddToResult.left = new FormAttachment( middle, 0 );
    fdAddToResult.top = new FormAttachment( wbShowFiles, 2 * margin );
    fdAddToResult.right = new FormAttachment( 100, 0 );
    wAddToResult.setLayoutData( fdAddToResult );
    SelectionAdapter lsSelR = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
      }
    };
    wAddToResult.addSelectionListener( lsSelR );

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
    wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.ContentTab.TabTitle" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    Composite wContentComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // Append checkbox
    wlAppend = new Label( wContentComp, SWT.RIGHT );
    wlAppend.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Append.Label" ) );
    props.setLook( wlAppend );
    fdlAppend = new FormData();
    fdlAppend.left = new FormAttachment( 0, 0 );
    fdlAppend.top = new FormAttachment( 0, 0 );
    fdlAppend.right = new FormAttachment( middle, -margin );
    wlAppend.setLayoutData( fdlAppend );
    wAppend = new Button( wContentComp, SWT.CHECK );
    props.setLook( wAppend );
    wAppend.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.Append.Tooltip" ) );
    fdAppend = new FormData();
    fdAppend.left = new FormAttachment( middle, 0 );
    fdAppend.top = new FormAttachment( 0, 0 );
    fdAppend.right = new FormAttachment( 100, 0 );
    wAppend.setLayoutData( fdAppend );
    wAppend.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
      }
    } );

    wlHeader = new Label( wContentComp, SWT.RIGHT );
    wlHeader.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Header.Label" ) );
    props.setLook( wlHeader );
    fdlHeader = new FormData();
    fdlHeader.left = new FormAttachment( 0, 0 );
    fdlHeader.top = new FormAttachment( wAppend, margin );
    fdlHeader.right = new FormAttachment( middle, -margin );
    wlHeader.setLayoutData( fdlHeader );
    wHeader = new Button( wContentComp, SWT.CHECK );
    props.setLook( wHeader );
    fdHeader = new FormData();
    fdHeader.left = new FormAttachment( middle, 0 );
    fdHeader.top = new FormAttachment( wAppend, margin );
    fdHeader.right = new FormAttachment( 100, 0 );
    wHeader.setLayoutData( fdHeader );
    wHeader.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    wlFooter = new Label( wContentComp, SWT.RIGHT );
    wlFooter.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Footer.Label" ) );
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

    wlEncoding = new Label( wContentComp, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Encoding.Label" ) );
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

    wlSplitEvery = new Label( wContentComp, SWT.RIGHT );
    wlSplitEvery.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.SplitEvery.Label" ) );
    props.setLook( wlSplitEvery );
    fdlSplitEvery = new FormData();
    fdlSplitEvery.left = new FormAttachment( 0, 0 );
    fdlSplitEvery.top = new FormAttachment( wEncoding, margin );
    fdlSplitEvery.right = new FormAttachment( middle, -margin );
    wlSplitEvery.setLayoutData( fdlSplitEvery );
    wSplitEvery = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSplitEvery );
    wSplitEvery.addModifyListener( lsMod );
    fdSplitEvery = new FormData();
    fdSplitEvery.left = new FormAttachment( middle, 0 );
    fdSplitEvery.top = new FormAttachment( wEncoding, margin );
    fdSplitEvery.right = new FormAttachment( 100, 0 );
    wSplitEvery.setLayoutData( fdSplitEvery );

    // Sheet name line
    wlSheetname = new Label( wContentComp, SWT.RIGHT );
    wlSheetname.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Sheetname.Label" ) );
    props.setLook( wlSheetname );
    fdlSheetname = new FormData();
    fdlSheetname.left = new FormAttachment( 0, 0 );
    fdlSheetname.top = new FormAttachment( wSplitEvery, margin );
    fdlSheetname.right = new FormAttachment( middle, -margin );
    wlSheetname.setLayoutData( fdlSheetname );
    wSheetname = new TextVar( transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSheetname.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.Sheetname.Tooltip" ) );
    props.setLook( wSheetname );
    wSheetname.addModifyListener( lsMod );
    fdSheetname = new FormData();
    fdSheetname.left = new FormAttachment( middle, 0 );
    fdSheetname.top = new FormAttachment( wSplitEvery, margin );
    fdSheetname.right = new FormAttachment( 100, 0 );
    wSheetname.setLayoutData( fdSheetname );

    // Protect Sheet?
    wlProtectSheet = new Label( wContentComp, SWT.RIGHT );
    wlProtectSheet.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.ProtectSheet.Label" ) );
    props.setLook( wlProtectSheet );
    fdlProtectSheet = new FormData();
    fdlProtectSheet.left = new FormAttachment( 0, 0 );
    fdlProtectSheet.top = new FormAttachment( wSheetname, margin );
    fdlProtectSheet.right = new FormAttachment( middle, -margin );
    wlProtectSheet.setLayoutData( fdlProtectSheet );
    wProtectSheet = new Button( wContentComp, SWT.CHECK );
    props.setLook( wProtectSheet );
    fdProtectSheet = new FormData();
    fdProtectSheet.left = new FormAttachment( middle, 0 );
    fdProtectSheet.top = new FormAttachment( wSheetname, margin );
    fdProtectSheet.right = new FormAttachment( 100, 0 );
    wProtectSheet.setLayoutData( fdProtectSheet );
    wProtectSheet.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {

        EnablePassword();
      }
    } );

    // Password line
    wlPassword = new Label( wContentComp, SWT.RIGHT );
    wlPassword.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Password.Label" ) );
    props.setLook( wlPassword );
    fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wProtectSheet, margin );
    fdlPassword.right = new FormAttachment( middle, -margin );
    wlPassword.setLayoutData( fdlPassword );
    wPassword = new PasswordTextVar( transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wPassword.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.Password.Tooltip" ) );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( middle, 0 );
    fdPassword.top = new FormAttachment( wProtectSheet, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    // auto size columns?
    wlAutoSize = new Label( wContentComp, SWT.RIGHT );
    wlAutoSize.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.AutoSize.Label" ) );
    props.setLook( wlAutoSize );
    fdlAutoSize = new FormData();
    fdlAutoSize.left = new FormAttachment( 0, 0 );
    fdlAutoSize.top = new FormAttachment( wPassword, margin );
    fdlAutoSize.right = new FormAttachment( middle, -margin );
    wlAutoSize.setLayoutData( fdlAutoSize );
    wAutoSize = new Button( wContentComp, SWT.CHECK );
    props.setLook( wAutoSize );
    wAutoSize.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.AutoSize.Tooltip" ) );
    fdAutoSize = new FormData();
    fdAutoSize.left = new FormAttachment( middle, 0 );
    fdAutoSize.top = new FormAttachment( wPassword, margin );
    fdAutoSize.right = new FormAttachment( 100, 0 );
    wAutoSize.setLayoutData( fdAutoSize );
    wAutoSize.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        EnableAutoSize();
      }
    } );

    // write null values as blank cells ?
    wlNullIsBlank = new Label( wContentComp, SWT.RIGHT );
    wlNullIsBlank.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.NullIsBlank.Label" ) );
    props.setLook( wlNullIsBlank );
    fdlNullIsBlank = new FormData();
    fdlNullIsBlank.left = new FormAttachment( 0, 0 );
    fdlNullIsBlank.top = new FormAttachment( wAutoSize, margin );
    fdlNullIsBlank.right = new FormAttachment( middle, -margin );
    wlNullIsBlank.setLayoutData( fdlNullIsBlank );
    wNullIsBlank = new Button( wContentComp, SWT.CHECK );
    props.setLook( wNullIsBlank );
    wNullIsBlank.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.NullIsBlank.Tooltip" ) );
    fdNullIsBlank = new FormData();
    fdNullIsBlank.left = new FormAttachment( middle, 0 );
    fdNullIsBlank.top = new FormAttachment( wAutoSize, margin );
    fdNullIsBlank.right = new FormAttachment( 100, 0 );
    wNullIsBlank.setLayoutData( fdNullIsBlank );

    // use temporary files?
    wluseTempFiles = new Label( wContentComp, SWT.RIGHT );
    wluseTempFiles.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.useTempFile.Label" ) );
    props.setLook( wluseTempFiles );
    fdluseTempFiles = new FormData();
    fdluseTempFiles.left = new FormAttachment( 0, 0 );
    fdluseTempFiles.top = new FormAttachment( wNullIsBlank, margin );
    fdluseTempFiles.right = new FormAttachment( middle, -margin );
    wluseTempFiles.setLayoutData( fdluseTempFiles );
    wuseTempFiles = new Button( wContentComp, SWT.CHECK );
    props.setLook( wuseTempFiles );
    wuseTempFiles.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.useTempFile.Tooltip" ) );
    fduseTempFiles = new FormData();
    fduseTempFiles.left = new FormAttachment( middle, 0 );
    fduseTempFiles.top = new FormAttachment( wNullIsBlank, margin );
    fduseTempFiles.right = new FormAttachment( 100, 0 );
    wuseTempFiles.setLayoutData( fduseTempFiles );
    wuseTempFiles.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        useTempFile();
      }
    } );

    // TempDirectory line
    wlTempDirectory = new Label( wContentComp, SWT.RIGHT );
    wlTempDirectory.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.TempDirectory.Label" ) );
    props.setLook( wlTempDirectory );
    fdlTempDirectory = new FormData();
    fdlTempDirectory.left = new FormAttachment( 0, 0 );
    fdlTempDirectory.top = new FormAttachment( wuseTempFiles, margin );
    fdlTempDirectory.right = new FormAttachment( middle, -margin );
    wlTempDirectory.setLayoutData( fdlTempDirectory );

    // Select TempDir
    wbTempDir = new Button( wContentComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTempDir );
    wbTempDir.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbTempDir = new FormData();
    fdbTempDir.right = new FormAttachment( 100, -margin );
    fdbTempDir.top = new FormAttachment( wuseTempFiles, margin );
    wbTempDir.setLayoutData( fdbTempDir );

    wTempDirectory = new TextVar( transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTempDirectory.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.TempDirectory.Tooltip" ) );
    props.setLook( wTempDirectory );
    wTempDirectory.addModifyListener( lsMod );
    fdTempDirectory = new FormData();
    fdTempDirectory.left = new FormAttachment( middle, 0 );
    fdTempDirectory.top = new FormAttachment( wuseTempFiles, margin );
    fdTempDirectory.right = new FormAttachment( wbTempDir, -margin );
    wTempDirectory.setLayoutData( fdTempDirectory );
    wTempDirectory.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    } );

    // ///////////////////////////////
    // START OF Template Group GROUP //
    // ///////////////////////////////

    wTemplateGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wTemplateGroup );
    wTemplateGroup.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.TemplateGroup.Label" ) );

    FormLayout TemplateGroupgroupLayout = new FormLayout();
    TemplateGroupgroupLayout.marginWidth = 10;
    TemplateGroupgroupLayout.marginHeight = 10;
    wTemplateGroup.setLayout( TemplateGroupgroupLayout );

    // Use template
    wlTemplate = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplate.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Template.Label" ) );
    props.setLook( wlTemplate );
    fdlTemplate = new FormData();
    fdlTemplate.left = new FormAttachment( 0, 0 );
    fdlTemplate.top = new FormAttachment( wTempDirectory, margin );
    fdlTemplate.right = new FormAttachment( middle, -margin );
    wlTemplate.setLayoutData( fdlTemplate );
    wTemplate = new Button( wTemplateGroup, SWT.CHECK );
    props.setLook( wTemplate );
    fdTemplate = new FormData();
    fdTemplate.left = new FormAttachment( middle, 0 );
    fdTemplate.top = new FormAttachment( wTempDirectory, margin );
    fdTemplate.right = new FormAttachment( 100, 0 );
    wTemplate.setLayoutData( fdTemplate );
    wTemplate.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        EnableTemplate();
      }
    } );

    // TemplateFilename line
    wlTemplateFilename = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplateFilename.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.TemplateFilename.Label" ) );
    props.setLook( wlTemplateFilename );
    fdlTemplateFilename = new FormData();
    fdlTemplateFilename.left = new FormAttachment( 0, 0 );
    fdlTemplateFilename.top = new FormAttachment( wTemplate, margin );
    fdlTemplateFilename.right = new FormAttachment( middle, -margin );
    wlTemplateFilename.setLayoutData( fdlTemplateFilename );

    wbTemplateFilename = new Button( wTemplateGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTemplateFilename );
    wbTemplateFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbTemplateFilename = new FormData();
    fdbTemplateFilename.right = new FormAttachment( 100, 0 );
    fdbTemplateFilename.top = new FormAttachment( wTemplate, 0 );
    wbTemplateFilename.setLayoutData( fdbTemplateFilename );

    wTemplateFilename = new TextVar( transMeta, wTemplateGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTemplateFilename );
    wTemplateFilename.addModifyListener( lsMod );
    fdTemplateFilename = new FormData();
    fdTemplateFilename.left = new FormAttachment( middle, 0 );
    fdTemplateFilename.top = new FormAttachment( wTemplate, margin );
    fdTemplateFilename.right = new FormAttachment( wbTemplateFilename, -margin );
    wTemplateFilename.setLayoutData( fdTemplateFilename );

    // Template Append
    wlTemplateAppend = new Label( wTemplateGroup, SWT.RIGHT );
    wlTemplateAppend.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.TemplateAppend.Label" ) );
    props.setLook( wlTemplateAppend );
    fdlTemplateAppend = new FormData();
    fdlTemplateAppend.left = new FormAttachment( 0, 0 );
    fdlTemplateAppend.top = new FormAttachment( wTemplateFilename, margin );
    fdlTemplateAppend.right = new FormAttachment( middle, -margin );
    wlTemplateAppend.setLayoutData( fdlTemplateAppend );
    wTemplateAppend = new Button( wTemplateGroup, SWT.CHECK );
    props.setLook( wTemplateAppend );
    fdTemplateAppend = new FormData();
    fdTemplateAppend.left = new FormAttachment( middle, 0 );
    fdTemplateAppend.top = new FormAttachment( wTemplateFilename, margin );
    fdTemplateAppend.right = new FormAttachment( 100, 0 );
    wTemplateAppend.setLayoutData( fdTemplateAppend );
    wTemplateAppend.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    fdTemplateGroup = new FormData();
    fdTemplateGroup.left = new FormAttachment( 0, margin );
    fdTemplateGroup.top = new FormAttachment( wTempDirectory, margin );
    fdTemplateGroup.right = new FormAttachment( 100, -margin );
    wTemplateGroup.setLayoutData( fdTemplateGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Template Group GROUP
    // ///////////////////////////////////////////////////////////

    fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment( 0, 0 );
    fdContentComp.top = new FormAttachment( 0, 0 );
    fdContentComp.right = new FormAttachment( 100, 0 );
    fdContentComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( fdContentComp );

    wContentComp.layout();
    wContentTab.setControl( wContentComp );

    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////

    // Custom tab...
    //
    wCustomTab = new CTabItem( wTabFolder, SWT.NONE );
    wCustomTab.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.CustomTab.TabTitle" ) );

    FormLayout CustomLayout = new FormLayout();
    CustomLayout.marginWidth = Const.FORM_MARGIN;
    CustomLayout.marginHeight = Const.FORM_MARGIN;

    Composite wCustomComp = new Composite( wTabFolder, SWT.NONE );
    wCustomComp.setLayout( CustomLayout );
    props.setLook( wCustomComp );

    // ///////////////////////////////
    // START OF Header Font GROUP //
    // ///////////////////////////////

    wFontHeaderGroup = new Group( wCustomComp, SWT.SHADOW_NONE );
    props.setLook( wFontHeaderGroup );
    wFontHeaderGroup.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.FontHeaderGroup.Label" ) );

    FormLayout FontHeadergroupLayout = new FormLayout();
    FontHeadergroupLayout.marginWidth = 10;
    FontHeadergroupLayout.marginHeight = 10;
    wFontHeaderGroup.setLayout( FontHeadergroupLayout );

    // Header font name
    wlHeaderFontName = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderFontName.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontName.Label" ) );
    props.setLook( wlHeaderFontName );
    fdlHeaderFontName = new FormData();
    fdlHeaderFontName.left = new FormAttachment( 0, 0 );
    fdlHeaderFontName.top = new FormAttachment( 0, margin );
    fdlHeaderFontName.right = new FormAttachment( middle, -margin );
    wlHeaderFontName.setLayoutData( fdlHeaderFontName );
    wHeaderFontName = new CCombo( wFontHeaderGroup, SWT.BORDER | SWT.READ_ONLY );
    wHeaderFontName.setItems( ExcelOutputMeta.font_name_desc );
    props.setLook( wHeaderFontName );
    wHeaderFontName.addModifyListener( lsMod );
    fdHeaderFontName = new FormData();
    fdHeaderFontName.left = new FormAttachment( middle, 0 );
    fdHeaderFontName.top = new FormAttachment( 0, margin );
    fdHeaderFontName.right = new FormAttachment( 100, 0 );
    wHeaderFontName.setLayoutData( fdHeaderFontName );

    // Header font size
    wlHeaderFontSize = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderFontSize.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontSize.Label" ) );
    props.setLook( wlHeaderFontSize );
    fdlHeaderFontSize = new FormData();
    fdlHeaderFontSize.left = new FormAttachment( 0, 0 );
    fdlHeaderFontSize.top = new FormAttachment( wHeaderFontName, margin );
    fdlHeaderFontSize.right = new FormAttachment( middle, -margin );
    wlHeaderFontSize.setLayoutData( fdlHeaderFontSize );
    wHeaderFontSize = new TextVar( transMeta, wFontHeaderGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wHeaderFontSize.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontSize.Tooltip" ) );
    props.setLook( wHeaderFontSize );
    wHeaderFontSize.addModifyListener( lsMod );
    fdHeaderFontSize = new FormData();
    fdHeaderFontSize.left = new FormAttachment( middle, 0 );
    fdHeaderFontSize.top = new FormAttachment( wHeaderFontName, margin );
    fdHeaderFontSize.right = new FormAttachment( 100, 0 );
    wHeaderFontSize.setLayoutData( fdHeaderFontSize );

    // Header font bold?
    wlHeaderFontBold = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderFontBold.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontBold.Label" ) );
    props.setLook( wlHeaderFontBold );
    fdlHeaderFontBold = new FormData();
    fdlHeaderFontBold.left = new FormAttachment( 0, 0 );
    fdlHeaderFontBold.top = new FormAttachment( wHeaderFontSize, margin );
    fdlHeaderFontBold.right = new FormAttachment( middle, -margin );
    wlHeaderFontBold.setLayoutData( fdlHeaderFontBold );
    wHeaderFontBold = new Button( wFontHeaderGroup, SWT.CHECK );
    props.setLook( wHeaderFontBold );
    fdHeaderFontBold = new FormData();
    fdHeaderFontBold.left = new FormAttachment( middle, 0 );
    fdHeaderFontBold.top = new FormAttachment( wHeaderFontSize, margin );
    fdHeaderFontBold.right = new FormAttachment( 100, 0 );
    wHeaderFontBold.setLayoutData( fdHeaderFontBold );
    wHeaderFontBold.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Header font bold?
    wlHeaderFontItalic = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderFontItalic.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontItalic.Label" ) );
    props.setLook( wlHeaderFontItalic );
    fdlHeaderFontItalic = new FormData();
    fdlHeaderFontItalic.left = new FormAttachment( 0, 0 );
    fdlHeaderFontItalic.top = new FormAttachment( wHeaderFontBold, margin );
    fdlHeaderFontItalic.right = new FormAttachment( middle, -margin );
    wlHeaderFontItalic.setLayoutData( fdlHeaderFontItalic );
    wHeaderFontItalic = new Button( wFontHeaderGroup, SWT.CHECK );
    props.setLook( wHeaderFontItalic );
    fdHeaderFontItalic = new FormData();
    fdHeaderFontItalic.left = new FormAttachment( middle, 0 );
    fdHeaderFontItalic.top = new FormAttachment( wHeaderFontBold, margin );
    fdHeaderFontItalic.right = new FormAttachment( 100, 0 );
    wHeaderFontItalic.setLayoutData( fdHeaderFontItalic );
    wHeaderFontItalic.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Font header uderline?
    wlHeaderFontUnderline = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderFontUnderline.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontUnderline.Label" ) );
    props.setLook( wlHeaderFontUnderline );
    fdlHeaderFontUnderline = new FormData();
    fdlHeaderFontUnderline.left = new FormAttachment( 0, 0 );
    fdlHeaderFontUnderline.top = new FormAttachment( wHeaderFontItalic, margin );
    fdlHeaderFontUnderline.right = new FormAttachment( middle, -margin );
    wlHeaderFontUnderline.setLayoutData( fdlHeaderFontUnderline );
    wHeaderFontUnderline = new CCombo( wFontHeaderGroup, SWT.BORDER | SWT.READ_ONLY );
    wHeaderFontUnderline.setItems( ExcelOutputMeta.font_underline_desc );
    props.setLook( wHeaderFontUnderline );
    wHeaderFontUnderline.addModifyListener( lsMod );
    fdHeaderFontUnderline = new FormData();
    fdHeaderFontUnderline.left = new FormAttachment( middle, 0 );
    fdHeaderFontUnderline.top = new FormAttachment( wHeaderFontItalic, margin );
    fdHeaderFontUnderline.right = new FormAttachment( 100, 0 );
    wHeaderFontUnderline.setLayoutData( fdHeaderFontUnderline );

    // Font header orientation
    wlHeaderFontOrientation = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderFontOrientation
      .setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontOrientation.Label" ) );
    props.setLook( wlHeaderFontOrientation );
    fdlHeaderFontOrientation = new FormData();
    fdlHeaderFontOrientation.left = new FormAttachment( 0, 0 );
    fdlHeaderFontOrientation.top = new FormAttachment( wHeaderFontUnderline, margin );
    fdlHeaderFontOrientation.right = new FormAttachment( middle, -margin );
    wlHeaderFontOrientation.setLayoutData( fdlHeaderFontOrientation );
    wHeaderFontOrientation = new CCombo( wFontHeaderGroup, SWT.BORDER | SWT.READ_ONLY );
    wHeaderFontOrientation.setItems( ExcelOutputMeta.font_orientation_desc );
    props.setLook( wHeaderFontOrientation );
    wHeaderFontOrientation.addModifyListener( lsMod );
    fdHeaderFontOrientation = new FormData();
    fdHeaderFontOrientation.left = new FormAttachment( middle, 0 );
    fdHeaderFontOrientation.top = new FormAttachment( wHeaderFontUnderline, margin );
    fdHeaderFontOrientation.right = new FormAttachment( 100, 0 );
    wHeaderFontOrientation.setLayoutData( fdHeaderFontOrientation );

    // Font header color
    wlHeaderFontColor = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderFontColor.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderFontColor.Label" ) );
    props.setLook( wlHeaderFontColor );
    fdlHeaderFontColor = new FormData();
    fdlHeaderFontColor.left = new FormAttachment( 0, 0 );
    fdlHeaderFontColor.top = new FormAttachment( wHeaderFontOrientation, margin );
    fdlHeaderFontColor.right = new FormAttachment( middle, -margin );
    wlHeaderFontColor.setLayoutData( fdlHeaderFontColor );

    wHeaderFontColor = new CCombo( wFontHeaderGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wHeaderFontColor );
    fdHeaderFontColor = new FormData();
    fdHeaderFontColor.left = new FormAttachment( middle, 0 );
    fdHeaderFontColor.top = new FormAttachment( wHeaderFontOrientation, margin );
    fdHeaderFontColor.right = new FormAttachment( 100, 0 );
    wHeaderFontColor.setLayoutData( fdHeaderFontColor );
    wHeaderFontColor.setItems( ExcelOutputMeta.font_color_desc );

    // Font header background color
    wlHeaderBackGroundColor = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderBackGroundColor
      .setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderBackGroundColor.Label" ) );
    props.setLook( wlHeaderBackGroundColor );
    fdlHeaderBackGroundColor = new FormData();
    fdlHeaderBackGroundColor.left = new FormAttachment( 0, 0 );
    fdlHeaderBackGroundColor.top = new FormAttachment( wHeaderFontColor, margin );
    fdlHeaderBackGroundColor.right = new FormAttachment( middle, -margin );
    wlHeaderBackGroundColor.setLayoutData( fdlHeaderBackGroundColor );

    wHeaderBackGroundColor = new CCombo( wFontHeaderGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wHeaderBackGroundColor );
    fdHeaderBackGroundColor = new FormData();
    fdHeaderBackGroundColor.left = new FormAttachment( middle, 0 );
    fdHeaderBackGroundColor.top = new FormAttachment( wHeaderFontColor, margin );
    fdHeaderBackGroundColor.right = new FormAttachment( 100, 0 );
    wHeaderBackGroundColor.setLayoutData( fdHeaderBackGroundColor );
    wHeaderBackGroundColor.setItems( ExcelOutputMeta.font_color_desc );

    // Header font size
    wlHeaderRowHeight = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderRowHeight.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderRowHeight.Label" ) );
    props.setLook( wlHeaderRowHeight );
    fdlHeaderRowHeight = new FormData();
    fdlHeaderRowHeight.left = new FormAttachment( 0, 0 );
    fdlHeaderRowHeight.top = new FormAttachment( wHeaderBackGroundColor, margin );
    fdlHeaderRowHeight.right = new FormAttachment( middle, -margin );
    wlHeaderRowHeight.setLayoutData( fdlHeaderRowHeight );
    wHeaderRowHeight = new TextVar( transMeta, wFontHeaderGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wHeaderRowHeight.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderRowHeight.Tooltip" ) );
    props.setLook( wHeaderRowHeight );
    wHeaderRowHeight.addModifyListener( lsMod );
    fdHeaderRowHeight = new FormData();
    fdHeaderRowHeight.left = new FormAttachment( middle, 0 );
    fdHeaderRowHeight.top = new FormAttachment( wHeaderBackGroundColor, margin );
    fdHeaderRowHeight.right = new FormAttachment( 100, 0 );
    wHeaderRowHeight.setLayoutData( fdHeaderRowHeight );

    // Header Alignment
    wlHeaderAlignment = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlHeaderAlignment.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.HeaderAlignment.Label" ) );
    props.setLook( wlHeaderAlignment );
    fdlHeaderAlignment = new FormData();
    fdlHeaderAlignment.left = new FormAttachment( 0, 0 );
    fdlHeaderAlignment.top = new FormAttachment( wHeaderRowHeight, margin );
    fdlHeaderAlignment.right = new FormAttachment( middle, -margin );
    wlHeaderAlignment.setLayoutData( fdlHeaderAlignment );
    wHeaderAlignment = new CCombo( wFontHeaderGroup, SWT.BORDER | SWT.READ_ONLY );
    wHeaderAlignment.setItems( ExcelOutputMeta.font_alignment_desc );
    props.setLook( wHeaderAlignment );
    wHeaderAlignment.addModifyListener( lsMod );
    fdHeaderAlignment = new FormData();
    fdHeaderAlignment.left = new FormAttachment( middle, 0 );
    fdHeaderAlignment.top = new FormAttachment( wHeaderRowHeight, margin );
    fdHeaderAlignment.right = new FormAttachment( 100, 0 );
    wHeaderAlignment.setLayoutData( fdHeaderAlignment );

    // Select Image
    wbImage = new Button( wFontHeaderGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbImage );
    wbImage.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.AddImage" ) );
    fdbImage = new FormData();
    fdbImage.right = new FormAttachment( 100, 0 );
    fdbImage.top = new FormAttachment( wHeaderAlignment, margin );
    wbImage.setLayoutData( fdbImage );

    // Image line
    wlImage = new Label( wFontHeaderGroup, SWT.RIGHT );
    wlImage.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.Image.Label" ) );
    props.setLook( wlImage );
    fdlImage = new FormData();
    fdlImage.left = new FormAttachment( 0, 0 );
    fdlImage.top = new FormAttachment( wHeaderAlignment, margin );
    fdlImage.right = new FormAttachment( middle, -margin );
    wlImage.setLayoutData( fdlImage );

    wImage = new TextVar( transMeta, wFontHeaderGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wImage );
    wImage.addModifyListener( lsMod );
    fdImage = new FormData();
    fdImage.left = new FormAttachment( middle, 0 );
    fdImage.top = new FormAttachment( wHeaderAlignment, margin );
    fdImage.right = new FormAttachment( wbImage, -margin );
    wImage.setLayoutData( fdImage );

    wbImage.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wImage, transMeta,
            new SelectionAdapterOptions( SelectionOperation.FILE,
                    new FilterType[] { FilterType.PNG, FilterType.ALL }, FilterType.PNG  ) ) );

    fdFontHeaderGroup = new FormData();
    fdFontHeaderGroup.left = new FormAttachment( 0, margin );
    fdFontHeaderGroup.top = new FormAttachment( 0, margin );
    fdFontHeaderGroup.right = new FormAttachment( 100, -margin );
    wFontHeaderGroup.setLayoutData( fdFontHeaderGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Font Group GROUP
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////
    // START OF Row Font GROUP //
    // ///////////////////////////////

    wFontRowGroup = new Group( wCustomComp, SWT.SHADOW_NONE );
    props.setLook( wFontRowGroup );
    wFontRowGroup.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.FontRowGroup.Label" ) );
    FormLayout FontRowGroupLayout = new FormLayout();
    FontRowGroupLayout.marginWidth = 10;
    FontRowGroupLayout.marginHeight = 10;
    wFontRowGroup.setLayout( FontRowGroupLayout );

    // Font Row name
    wlRowFontName = new Label( wFontRowGroup, SWT.RIGHT );
    wlRowFontName.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.RowFontName.Label" ) );
    props.setLook( wlRowFontName );
    fdlRowFontName = new FormData();
    fdlRowFontName.left = new FormAttachment( 0, 0 );
    fdlRowFontName.top = new FormAttachment( 0, margin );
    fdlRowFontName.right = new FormAttachment( middle, -margin );
    wlRowFontName.setLayoutData( fdlRowFontName );
    wRowFontName = new CCombo( wFontRowGroup, SWT.BORDER | SWT.READ_ONLY );
    wRowFontName.setItems( ExcelOutputMeta.font_name_desc );
    props.setLook( wRowFontName );
    wRowFontName.addModifyListener( lsMod );
    fdRowFontName = new FormData();
    fdRowFontName.left = new FormAttachment( middle, 0 );
    fdRowFontName.top = new FormAttachment( 0, margin );
    fdRowFontName.right = new FormAttachment( 100, 0 );
    wRowFontName.setLayoutData( fdRowFontName );

    // Row font size
    wlRowFontSize = new Label( wFontRowGroup, SWT.RIGHT );
    wlRowFontSize.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.RowFontSize.Label" ) );
    props.setLook( wlRowFontSize );
    fdlRowFontSize = new FormData();
    fdlRowFontSize.left = new FormAttachment( 0, 0 );
    fdlRowFontSize.top = new FormAttachment( wRowFontName, margin );
    fdlRowFontSize.right = new FormAttachment( middle, -margin );
    wlRowFontSize.setLayoutData( fdlRowFontSize );
    wRowFontSize = new TextVar( transMeta, wFontRowGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wRowFontSize.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.RowFontSize.Tooltip" ) );
    props.setLook( wRowFontSize );
    wRowFontSize.addModifyListener( lsMod );
    fdRowFontSize = new FormData();
    fdRowFontSize.left = new FormAttachment( middle, 0 );
    fdRowFontSize.top = new FormAttachment( wRowFontName, margin );
    fdRowFontSize.right = new FormAttachment( 100, 0 );
    wRowFontSize.setLayoutData( fdRowFontSize );

    // Font Row color
    wlRowFontColor = new Label( wFontRowGroup, SWT.RIGHT );
    wlRowFontColor.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.RowFontColor.Label" ) );
    props.setLook( wlRowFontColor );
    fdlRowFontColor = new FormData();
    fdlRowFontColor.left = new FormAttachment( 0, 0 );
    fdlRowFontColor.top = new FormAttachment( wRowFontSize, margin );
    fdlRowFontColor.right = new FormAttachment( middle, -margin );
    wlRowFontColor.setLayoutData( fdlRowFontColor );
    wRowFontColor = new CCombo( wFontRowGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wRowFontColor );
    fdRowFontColor = new FormData();
    fdRowFontColor.left = new FormAttachment( middle, 0 );
    fdRowFontColor.top = new FormAttachment( wRowFontSize, margin );
    fdRowFontColor.right = new FormAttachment( 100, 0 );
    wRowFontColor.setLayoutData( fdRowFontColor );
    wRowFontColor.setItems( ExcelOutputMeta.font_color_desc );

    // Font Row background color
    wlRowBackGroundColor = new Label( wFontRowGroup, SWT.RIGHT );
    wlRowBackGroundColor.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.RowBackGroundColor.Label" ) );
    props.setLook( wlRowBackGroundColor );
    fdlRowBackGroundColor = new FormData();
    fdlRowBackGroundColor.left = new FormAttachment( 0, 0 );
    fdlRowBackGroundColor.top = new FormAttachment( wRowFontColor, margin );
    fdlRowBackGroundColor.right = new FormAttachment( middle, -margin );
    wlRowBackGroundColor.setLayoutData( fdlRowBackGroundColor );

    wRowBackGroundColor = new CCombo( wFontRowGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wRowBackGroundColor );
    fdRowBackGroundColor = new FormData();
    fdRowBackGroundColor.left = new FormAttachment( middle, 0 );
    fdRowBackGroundColor.top = new FormAttachment( wRowFontColor, margin );
    fdRowBackGroundColor.right = new FormAttachment( 100, 0 );
    wRowBackGroundColor.setLayoutData( fdRowBackGroundColor );
    wRowBackGroundColor.setItems( ExcelOutputMeta.font_color_desc );

    fdFontRowGroup = new FormData();
    fdFontRowGroup.left = new FormAttachment( 0, margin );
    fdFontRowGroup.top = new FormAttachment( wFontHeaderGroup, margin );
    fdFontRowGroup.right = new FormAttachment( 100, -margin );
    wFontRowGroup.setLayoutData( fdFontRowGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Row Font Group
    // ///////////////////////////////////////////////////////////

    fdCustomComp = new FormData();
    fdCustomComp.left = new FormAttachment( 0, 0 );
    fdCustomComp.top = new FormAttachment( 0, 0 );
    fdCustomComp.right = new FormAttachment( 100, 0 );
    fdCustomComp.bottom = new FormAttachment( 100, 0 );
    wCustomComp.setLayoutData( fdCustomComp );

    wCustomComp.layout();
    wCustomTab.setControl( wCustomComp );
    // ///////////////////////////////////////////////////////////
    // / END OF customer TAB
    // ///////////////////////////////////////////////////////////

    // Fields tab...
    //
    wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.FieldsTab.TabTitle" ) );

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
    wMinWidth.setText( BaseMessages.getString( PKG, "ExcelOutputDialog.MinWidth.Button" ) );
    wMinWidth.setToolTipText( BaseMessages.getString( PKG, "ExcelOutputDialog.MinWidth.Tooltip" ) );

    setButtonPositions( new Button[] { wGet, wMinWidth }, margin, null );

    final int FieldsRows = input.getOutputFields().length;

    // Prepare a list of possible formats...
    String[] formats =
      new String[] {
        // Numbers
        "#", "0", "0.00", "#,##0", "#,##0.00", "$#,##0;($#,##0)", "$#,##0;($#,##0)", "$#,##0;($#,##0)",
        "$#,##0;($#,##0)", "0%", "0.00%", "0.00E00", "#,##0;(#,##0)", "#,##0;(#,##0)", "#,##0.00;(#,##0.00)",
        "#,##0.00;(#,##0.00)", "#,##0;(#,##0)", "#,##0;(#,##0)", "#,##0.00;(#,##0.00)", "#,##0.00;(#,##0.00)",
        "#,##0.00;(#,##0.00)", "##0.0E0",

        // Forces text
        "@",

        // Dates
        "M/d/yy", "d-MMM-yy", "d-MMM", "MMM-yy", "h:mm a", "h:mm:ss a", "H:mm", "H:mm:ss", "M/d/yy H:mm",
        "mm:ss", "H:mm:ss", "H:mm:ss", };

    colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelOutputDialog.NameColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelOutputDialog.TypeColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ExcelOutputDialog.FormatColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, formats ), };

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

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
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
    wFilename.addSelectionListener( lsDef );
    wTemplateFilename.addSelectionListener( lsDef );

    // Whenever something changes, set the tooltip to the expanded version:
    wFilename.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wFilename.setToolTipText( transMeta.environmentSubstitute( wFilename.getText() ) );
      }
    } );
    wTemplateFilename.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wTemplateFilename.setToolTipText( transMeta.environmentSubstitute( wTemplateFilename.getText() ) );
      }
    } );

    wbFilename.addSelectionListener( DialogHelper.constructSelectionAdapterFileDialogTextVarForUserFile( log, wFilename,
      transMeta, SelectionOperation.SAVE_TO_FILE_FOLDER, new FilterType[] { FilterType.XLS, FilterType.XLSX, FilterType.ALL }, FilterType.XLS ) );
    wbTemplateFilename.addSelectionListener( DialogHelper.constructSelectionAdapterFileDialogTextVarForUserFile( log, wTemplateFilename,
      transMeta, SelectionOperation.SAVE_TO_FILE_FOLDER, new FilterType[] { FilterType.XLS, FilterType.XLSX, FilterType.ALL }, FilterType.XLS ) );
    wbTempDir.addSelectionListener( DialogHelper.constructSelectionAdapterFileDialogTextVarForUserFile( log, wTempDirectory,
      transMeta, SelectionOperation.FOLDER, new FilterType[] { FilterType.ALL }, FilterType.ALL ) );
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
    setDateTimeFormat();
    EnableAutoSize();
    useTempFile();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void EnableAutoSize() {
    wMinWidth.setEnabled( !wAutoSize.getSelection() );
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
    if ( input.getFileName() != null ) {
      wFilename.setText( input.getFileName() );
    }
    wDoNotOpenNewFileInit.setSelection( input.isDoNotOpenNewFileInit() );
    if ( input.getExtension() != null ) {
      wExtension.setText( input.getExtension() );
    }
    if ( input.getEncoding() != null ) {
      wEncoding.setText( input.getEncoding() );
    }
    if ( input.getTemplateFileName() != null ) {
      wTemplateFilename.setText( input.getTemplateFileName() );
    }
    wuseTempFiles.setSelection( input.isUseTempFiles() );
    if ( input.getTempDirectory() != null ) {
      wTempDirectory.setText( input.getTempDirectory() );
    }
    wSplitEvery.setText( "" + input.getSplitEvery() );
    wAppend.setSelection( input.isAppend() );
    wHeader.setSelection( input.isHeaderEnabled() );
    wFooter.setSelection( input.isFooterEnabled() );
    wAddDate.setSelection( input.isDateInFilename() );
    wAddTime.setSelection( input.isTimeInFilename() );

    if ( input.getDateTimeFormat() != null ) {
      wDateTimeFormat.setText( input.getDateTimeFormat() );
    }
    wSpecifyFormat.setSelection( input.isSpecifyFormat() );

    wCreateParentFolder.setSelection( input.isCreateParentFolder() );
    wAddToResult.setSelection( input.isAddToResultFiles() );
    wAutoSize.setSelection( input.isAutoSizeColums() );
    wNullIsBlank.setSelection( input.isNullBlank() );

    wAddStepnr.setSelection( input.isStepNrInFilename() );
    wTemplate.setSelection( input.isTemplateEnabled() );
    wTemplateAppend.setSelection( input.isTemplateAppend() );
    if ( input.getSheetname() != null ) {
      wSheetname.setText( input.getSheetname() );
    } else {
      wSheetname.setText( "Sheet1" );
    }
    wProtectSheet.setSelection( input.isSheetProtected() );

    EnablePassword();
    EnableTemplate();

    if ( input.getPassword() != null ) {
      wPassword.setText( input.getPassword() );
    }
    if ( isDebug() ) {
      logDebug( "getting fields info..." );
    }

    for ( int i = 0; i < input.getOutputFields().length; i++ ) {
      ExcelField field = input.getOutputFields()[i];

      TableItem item = wFields.table.getItem( i );
      if ( field.getName() != null ) {
        item.setText( 1, field.getName() );
      }
      item.setText( 2, field.getTypeDesc() );
      if ( field.getFormat() != null ) {
        item.setText( 3, field.getFormat() );
      }
    }

    wFields.optWidth( true );

    // Header Font settings
    wHeaderFontName.setText( ExcelOutputMeta.getFontNameDesc( input.getHeaderFontName() ) );
    wHeaderFontSize.setText( input.getHeaderFontSize() );
    wHeaderFontBold.setSelection( input.isHeaderFontBold() );
    wHeaderFontItalic.setSelection( input.isHeaderFontItalic() );
    wHeaderFontUnderline.setText( ExcelOutputMeta.getFontUnderlineDesc( input.getHeaderFontUnderline() ) );
    wHeaderFontOrientation.setText( ExcelOutputMeta.getFontOrientationDesc( input.getHeaderFontOrientation() ) );
    wHeaderFontColor.setText( ExcelOutputMeta.getFontColorDesc( input.getHeaderFontColor() ) );
    wHeaderBackGroundColor.setText( ExcelOutputMeta.getFontColorDesc( input.getHeaderBackGroundColor() ) );
    wHeaderRowHeight.setText( Const.NVL( input.getHeaderRowHeight(), "" + ExcelOutputMeta.DEFAULT_ROW_HEIGHT ) );
    wHeaderAlignment.setText( ExcelOutputMeta.getFontAlignmentDesc( input.getHeaderAlignment() ) );
    if ( input.getHeaderImage() != null ) {
      wImage.setText( input.getHeaderImage() );
    }

    // Row font settings
    wRowFontName.setText( ExcelOutputMeta.getFontNameDesc( input.getRowFontName() ) );
    wRowFontSize.setText( input.getRowFontSize() );
    wRowFontColor.setText( ExcelOutputMeta.getFontColorDesc( input.getRowFontColor() ) );
    wRowBackGroundColor.setText( ExcelOutputMeta.getFontColorDesc( input.getRowBackGroundColor() ) );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;

    input.setChanged( backupChanged );

    dispose();
  }

  private void getInfo( ExcelOutputMeta tfoi ) {
    tfoi.setFileName( wFilename.getText() );
    tfoi.setEncoding( wEncoding.getText() );
    tfoi.setDoNotOpenNewFileInit( wDoNotOpenNewFileInit.getSelection() );
    tfoi.setExtension( wExtension.getText() );
    tfoi.setTemplateFileName( wTemplateFilename.getText() );
    tfoi.setSplitEvery( Const.toInt( wSplitEvery.getText(), 0 ) );
    tfoi.setAppend( wAppend.getSelection() );
    tfoi.setHeaderEnabled( wHeader.getSelection() );
    tfoi.setFooterEnabled( wFooter.getSelection() );
    tfoi.setStepNrInFilename( wAddStepnr.getSelection() );
    tfoi.setDateInFilename( wAddDate.getSelection() );
    tfoi.setTimeInFilename( wAddTime.getSelection() );
    tfoi.setUseTempFiles( wuseTempFiles.getSelection() );
    tfoi.setTempDirectory( wTempDirectory.getText() );
    tfoi.setDateTimeFormat( wDateTimeFormat.getText() );
    tfoi.setSpecifyFormat( wSpecifyFormat.getSelection() );
    tfoi.setAutoSizeColums( wAutoSize.getSelection() );
    tfoi.setNullIsBlank( wNullIsBlank.getSelection() );

    tfoi.setAddToResultFiles( wAddToResult.getSelection() );
    tfoi.setCreateParentFolder( wCreateParentFolder.getSelection() );
    tfoi.setProtectSheet( wProtectSheet.getSelection() );
    tfoi.setPassword( wPassword.getText() );
    tfoi.setTemplateEnabled( wTemplate.getSelection() );
    tfoi.setTemplateAppend( wTemplateAppend.getSelection() );
    if ( wSheetname.getText() != null ) {
      tfoi.setSheetname( wSheetname.getText() );
    } else {
      tfoi.setSheetname( "Sheet 1" );
    }

    int i;
    // Table table = wFields.table;

    int nrfields = wFields.nrNonEmpty();

    tfoi.allocate( nrfields );

    for ( i = 0; i < nrfields; i++ ) {
      ExcelField field = new ExcelField();

      TableItem item = wFields.getNonEmpty( i );
      field.setName( item.getText( 1 ) );
      field.setType( item.getText( 2 ) );
      field.setFormat( item.getText( 3 ) );

      //CHECKSTYLE:Indentation:OFF
      tfoi.getOutputFields()[i] = field;
    }
    // Header font
    tfoi.setHeaderFontName( ExcelOutputMeta.getFontNameByDesc( wHeaderFontName.getText() ) );
    tfoi.setHeaderFontSize( wHeaderFontSize.getText() );
    tfoi.setHeaderFontBold( wHeaderFontBold.getSelection() );
    tfoi.setHeaderFontItalic( wHeaderFontItalic.getSelection() );
    tfoi.setHeaderFontUnderline( ExcelOutputMeta.getFontUnderlineByDesc( wHeaderFontUnderline.getText() ) );
    tfoi.setHeaderFontOrientation( ExcelOutputMeta.getFontOrientationByDesc( wHeaderFontOrientation.getText() ) );
    tfoi.setHeaderFontColor( ExcelOutputMeta.getFontColorByDesc( wHeaderFontColor.getText() ) );
    tfoi.setHeaderBackGroundColor( ExcelOutputMeta.getFontColorByDesc( wHeaderBackGroundColor.getText() ) );
    tfoi.setHeaderRowHeight( wHeaderRowHeight.getText() );
    tfoi.setHeaderAlignment( ExcelOutputMeta.getFontAlignmentByDesc( wHeaderAlignment.getText() ) );
    tfoi.setHeaderImage( wImage.getText() );

    // Row font
    tfoi.setRowFontName( ExcelOutputMeta.getFontNameByDesc( wRowFontName.getText() ) );
    tfoi.setRowFontSize( wRowFontSize.getText() );
    tfoi.setRowFontColor( ExcelOutputMeta.getFontColorByDesc( wRowFontColor.getText() ) );
    tfoi.setRowBackGroundColor( ExcelOutputMeta.getFontColorByDesc( wRowBackGroundColor.getText() ) );
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    getInfo( input );

    dispose();
  }

  private void EnablePassword() {
    input.setChanged();

    wPassword.setEnabled( wProtectSheet.getSelection() );
    wlPassword.setEnabled( wProtectSheet.getSelection() );

  }

  private void EnableTemplate() {
    input.setChanged();

    wlTemplateFilename.setEnabled( wTemplate.getSelection() );
    wTemplateFilename.setEnabled( wTemplate.getSelection() );
    wbTemplateFilename.setEnabled( wTemplate.getSelection() );
    wlTemplateAppend.setEnabled( wTemplate.getSelection() );
    wTemplateAppend.setEnabled( wTemplate.getSelection() );

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
   *
   */
  public void setMinimalWidth() {
    int nrNonEmptyFields = wFields.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );

      item.setText( 4, "" );
      item.setText( 5, "" );

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

  private void useTempFile() {
    wTempDirectory.setEnabled( wuseTempFiles.getSelection() );
    wlTempDirectory.setEnabled( wuseTempFiles.getSelection() );
    wbTempDir.setEnabled( wuseTempFiles.getSelection() );
  }
}
