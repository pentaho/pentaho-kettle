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


package org.pentaho.di.ui.trans.steps.xbaseinput;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.xbaseinput.XBaseInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class XBaseInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = XBaseInputMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlFilename;
  private Button wbFilename;
  private TextVar wFilename;
  private FormData fdlFilename, fdbFilename, fdFilename;

  private Group gAccepting;
  private FormData fdAccepting;

  private Label wlAccFilenames;
  private Button wAccFilenames;
  private FormData fdlAccFilenames, fdAccFilenames;

  private Label wlAccField;
  private Text wAccField;
  private FormData fdlAccField, fdAccField;

  private Label wlCompression;
  private CCombo wCompression;
  private FormData fdlCompression, fdCompression;

  private Label wlAccStep;
  private CCombo wAccStep;
  private FormData fdlAccStep, fdAccStep;

  private Label wlLimit;
  private Text wLimit;
  private FormData fdlLimit, fdLimit;

  private Label wlAddRownr;
  private Button wAddRownr;
  private FormData fdlAddRownr, fdAddRownr;

  private Label wlFieldRownr;
  private Text wFieldRownr;
  private FormData fdlFieldRownr, fdFieldRownr;

  private Label wlInclFilename;
  private Button wInclFilename;
  private FormData fdlInclFilename, fdInclFilename;

  private Label wlInclFilenameField;
  private Text wInclFilenameField;
  private FormData fdlInclFilenameField, fdInclFilenameField;

  private Label wlCharactersetName;
  private Text wCharactersetName;
  private FormData fdlCharactersetName, fdCharactersetName;

  private XBaseInputMeta input;
  private boolean backupChanged, backupAddRownr;

  public XBaseInputDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (XBaseInputMeta) in;
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
    backupChanged = input.hasChanged();
    backupAddRownr = input.isRowNrAdded();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "XBaseInputDialog.Dialog.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
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

    // Filename line
    wlFilename = new Label( shell, SWT.RIGHT );
    wlFilename.setText( BaseMessages.getString( PKG, "System.Label.Filename" ) );
    props.setLook( wlFilename );
    fdlFilename = new FormData();
    fdlFilename.left = new FormAttachment( 0, 0 );
    fdlFilename.top = new FormAttachment( wStepname, margin );
    fdlFilename.right = new FormAttachment( middle, -margin );
    wlFilename.setLayoutData( fdlFilename );

    wbFilename = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbFilename );
    wbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( wStepname, margin );
    wbFilename.setLayoutData( fdbFilename );

    wFilename = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilename );
    wFilename.addModifyListener( lsMod );
    fdFilename = new FormData();
    fdFilename.left = new FormAttachment( middle, 0 );
    fdFilename.right = new FormAttachment( wbFilename, -margin );
    fdFilename.top = new FormAttachment( wStepname, margin );
    wFilename.setLayoutData( fdFilename );

    // Accepting filenames group
    //

    gAccepting = new Group( shell, SWT.SHADOW_ETCHED_IN );
    gAccepting.setText( BaseMessages.getString( PKG, "XBaseInputDialog.AcceptingGroup.Label" ) );
    FormLayout acceptingLayout = new FormLayout();
    acceptingLayout.marginWidth = 3;
    acceptingLayout.marginHeight = 3;
    gAccepting.setLayout( acceptingLayout );
    props.setLook( gAccepting );

    // Accept filenames from previous steps?
    //
    wlAccFilenames = new Label( gAccepting, SWT.RIGHT );
    wlAccFilenames.setText( BaseMessages.getString( PKG, "XBaseInputDialog.AcceptFilenames.Label" ) );
    props.setLook( wlAccFilenames );
    fdlAccFilenames = new FormData();
    fdlAccFilenames.top = new FormAttachment( 0, margin );
    fdlAccFilenames.left = new FormAttachment( 0, 0 );
    fdlAccFilenames.right = new FormAttachment( middle, -margin );
    wlAccFilenames.setLayoutData( fdlAccFilenames );
    wAccFilenames = new Button( gAccepting, SWT.CHECK );
    wAccFilenames.setToolTipText( BaseMessages.getString( PKG, "XBaseInputDialog.AcceptFilenames.Tooltip" ) );
    props.setLook( wAccFilenames );
    fdAccFilenames = new FormData();
    fdAccFilenames.top = new FormAttachment( 0, margin );
    fdAccFilenames.left = new FormAttachment( middle, 0 );
    fdAccFilenames.right = new FormAttachment( 100, 0 );
    wAccFilenames.setLayoutData( fdAccFilenames );
    wAccFilenames.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        setFlags();
        input.setChanged();
      }
    } );

    // Which step to read from?
    wlAccStep = new Label( gAccepting, SWT.RIGHT );
    wlAccStep.setText( BaseMessages.getString( PKG, "XBaseInputDialog.AcceptStep.Label" ) );
    props.setLook( wlAccStep );
    fdlAccStep = new FormData();
    fdlAccStep.top = new FormAttachment( wAccFilenames, margin );
    fdlAccStep.left = new FormAttachment( 0, 0 );
    fdlAccStep.right = new FormAttachment( middle, -margin );
    wlAccStep.setLayoutData( fdlAccStep );
    wAccStep = new CCombo( gAccepting, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wAccStep.setToolTipText( BaseMessages.getString( PKG, "XBaseInputDialog.AcceptStep.Tooltip" ) );
    props.setLook( wAccStep );
    fdAccStep = new FormData();
    fdAccStep.top = new FormAttachment( wAccFilenames, margin );
    fdAccStep.left = new FormAttachment( middle, 0 );
    fdAccStep.right = new FormAttachment( 100, 0 );
    wAccStep.setLayoutData( fdAccStep );

    // Which field?
    //
    wlAccField = new Label( gAccepting, SWT.RIGHT );
    wlAccField.setText( BaseMessages.getString( PKG, "XBaseInputDialog.AcceptField.Label" ) );
    props.setLook( wlAccField );
    fdlAccField = new FormData();
    fdlAccField.top = new FormAttachment( wAccStep, margin );
    fdlAccField.left = new FormAttachment( 0, 0 );
    fdlAccField.right = new FormAttachment( middle, -margin );
    wlAccField.setLayoutData( fdlAccField );
    wAccField = new Text( gAccepting, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wAccField.setToolTipText( BaseMessages.getString( PKG, "XBaseInputDialog.AcceptField.Tooltip" ) );
    props.setLook( wAccField );
    fdAccField = new FormData();
    fdAccField.top = new FormAttachment( wAccStep, margin );
    fdAccField.left = new FormAttachment( middle, 0 );
    fdAccField.right = new FormAttachment( 100, 0 );
    wAccField.setLayoutData( fdAccField );

    // Fill in the source steps...
    List<StepMeta> prevSteps = transMeta.findPreviousSteps( transMeta.findStep( stepname ) );
    for ( StepMeta prevStep : prevSteps ) {
      wAccStep.add( prevStep.getName() );
    }

    fdAccepting = new FormData();
    fdAccepting.left = new FormAttachment( middle, 0 );
    fdAccepting.right = new FormAttachment( 100, 0 );
    fdAccepting.top = new FormAttachment( wFilename, margin * 2 );
    // fdAccepting.bottom = new FormAttachment(wAccStep, margin);
    gAccepting.setLayoutData( fdAccepting );


    // Compression type (None, Zip or GZip
    wlCompression = new Label( shell, SWT.RIGHT );
    wlCompression.setText( BaseMessages.getString( PKG, "XBaseInputDialog.Compression.Label" ) );
    props.setLook( wlCompression );
    fdlCompression = new FormData();
    fdlCompression.left = new FormAttachment( 0, 0 );
    fdlCompression.top = new FormAttachment( gAccepting, margin );
    fdlCompression.right = new FormAttachment( middle, -margin );
    wlCompression.setLayoutData( fdlCompression );
    wCompression = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    wCompression.setText( BaseMessages.getString( PKG, "XBaseInputDialog.Compression.Label" ) );
    wCompression.setToolTipText( BaseMessages.getString( PKG, "XBaseInputDialog.Compression.Tooltip" ) );
    props.setLook( wCompression );
    wCompression.setItems( CompressionProviderFactory.getInstance().getCompressionProviderNames() );

    wCompression.addModifyListener( lsMod );
    fdCompression = new FormData();
    fdCompression.left = new FormAttachment( middle, 0 );
    fdCompression.top = new FormAttachment( gAccepting, margin );
    fdCompression.right = new FormAttachment( 100, 0 );
    wCompression.setLayoutData( fdCompression );

    // Limit input ...
    wlLimit = new Label( shell, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "XBaseInputDialog.LimitSize.Label" ) );
    props.setLook( wlLimit );
    fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.right = new FormAttachment( middle, -margin );
    fdlLimit.top = new FormAttachment( wlCompression, margin * 2 );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.top = new FormAttachment( wCompression, margin * 2 );
    fdLimit.right = new FormAttachment( 100, 0 );
    wLimit.setLayoutData( fdLimit );

    // Add rownr (1...)?
    wlAddRownr = new Label( shell, SWT.RIGHT );
    wlAddRownr.setText( BaseMessages.getString( PKG, "XBaseInputDialog.AddRowNr.Label" ) );
    props.setLook( wlAddRownr );
    fdlAddRownr = new FormData();
    fdlAddRownr.left = new FormAttachment( 0, 0 );
    fdlAddRownr.top = new FormAttachment( wLimit, margin );
    fdlAddRownr.right = new FormAttachment( middle, -margin );
    wlAddRownr.setLayoutData( fdlAddRownr );
    wAddRownr = new Button( shell, SWT.CHECK );
    props.setLook( wAddRownr );
    wAddRownr.setToolTipText( BaseMessages.getString( PKG, "XBaseInputDialog.AddRowNr.Tooltip" ) );
    fdAddRownr = new FormData();
    fdAddRownr.left = new FormAttachment( middle, 0 );
    fdAddRownr.top = new FormAttachment( wLimit, margin );
    wAddRownr.setLayoutData( fdAddRownr );
    wAddRownr.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    // FieldRownr input ...
    wlFieldRownr = new Label( shell, SWT.LEFT );
    wlFieldRownr.setText( BaseMessages.getString( PKG, "XBaseInputDialog.FieldnameOfRowNr.Label" ) );
    props.setLook( wlFieldRownr );
    fdlFieldRownr = new FormData();
    fdlFieldRownr.left = new FormAttachment( wAddRownr, margin );
    fdlFieldRownr.top = new FormAttachment( wLimit, margin );
    wlFieldRownr.setLayoutData( fdlFieldRownr );
    wFieldRownr = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFieldRownr );
    wFieldRownr.addModifyListener( lsMod );
    fdFieldRownr = new FormData();
    fdFieldRownr.left = new FormAttachment( wlFieldRownr, margin );
    fdFieldRownr.top = new FormAttachment( wLimit, margin );
    fdFieldRownr.right = new FormAttachment( 100, 0 );
    wFieldRownr.setLayoutData( fdFieldRownr );

    wlInclFilename = new Label( shell, SWT.RIGHT );
    wlInclFilename.setText( BaseMessages.getString( PKG, "XBaseInputDialog.InclFilename.Label" ) );
    props.setLook( wlInclFilename );
    fdlInclFilename = new FormData();
    fdlInclFilename.left = new FormAttachment( 0, 0 );
    fdlInclFilename.top = new FormAttachment( wFieldRownr, margin );
    fdlInclFilename.right = new FormAttachment( middle, -margin );
    wlInclFilename.setLayoutData( fdlInclFilename );
    wInclFilename = new Button( shell, SWT.CHECK );
    props.setLook( wInclFilename );
    wInclFilename.setToolTipText( BaseMessages.getString( PKG, "XBaseInputDialog.InclFilename.Tooltip" ) );
    fdInclFilename = new FormData();
    fdInclFilename.left = new FormAttachment( middle, 0 );
    fdInclFilename.top = new FormAttachment( wFieldRownr, margin );
    wInclFilename.setLayoutData( fdInclFilename );
    wInclFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
        setFlags();
      }
    } );

    wlInclFilenameField = new Label( shell, SWT.LEFT );
    wlInclFilenameField.setText( BaseMessages.getString( PKG, "XBaseInputDialog.InclFilenameField.Label" ) );
    props.setLook( wlInclFilenameField );
    fdlInclFilenameField = new FormData();
    fdlInclFilenameField.left = new FormAttachment( wInclFilename, margin );
    fdlInclFilenameField.top = new FormAttachment( wFieldRownr, margin );
    wlInclFilenameField.setLayoutData( fdlInclFilenameField );
    wInclFilenameField = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclFilenameField );
    wInclFilenameField.addModifyListener( lsMod );
    fdInclFilenameField = new FormData();
    fdInclFilenameField.left = new FormAttachment( wlInclFilenameField, margin );
    fdInclFilenameField.top = new FormAttachment( wFieldRownr, margin );
    fdInclFilenameField.right = new FormAttachment( 100, 0 );
    wInclFilenameField.setLayoutData( fdInclFilenameField );

    // #CRQ-6087
    //
    wlCharactersetName = new Label( shell, SWT.RIGHT );
    wlCharactersetName.setText( BaseMessages.getString( PKG, "XBaseInputDialog.CharactersetName.Label" ) );
    props.setLook( wlCharactersetName );
    fdlCharactersetName = new FormData();
    fdlCharactersetName.left = new FormAttachment( 0, 0 );
    fdlCharactersetName.right = new FormAttachment( middle, -margin );
    fdlCharactersetName.top = new FormAttachment( wInclFilename, margin );
    wlCharactersetName.setLayoutData( fdlCharactersetName );
    wCharactersetName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wCharactersetName.setToolTipText( BaseMessages.getString( PKG, "XBaseInputDialog.CharactersetName.Tooltip" ) );
    props.setLook( wCharactersetName );
    wCharactersetName.addModifyListener( lsMod );
    fdCharactersetName = new FormData();
    fdCharactersetName.left = new FormAttachment( middle, 0 );
    fdCharactersetName.top = new FormAttachment( wInclFilename, margin );
    fdCharactersetName.right = new FormAttachment( 100, 0 );
    wCharactersetName.setLayoutData( fdCharactersetName );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Preview" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wCharactersetName );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wPreview.addListener( SWT.Selection, lsPreview );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wLimit.addSelectionListener( lsDef );
    wFieldRownr.addSelectionListener( lsDef );
    wAccField.addSelectionListener( lsDef );

    wFilename.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        wFilename.setToolTipText( transMeta.environmentSubstitute( wFilename.getText() ) );
      }
    } );

    wbFilename.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wFilename, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE,
        new FilterType[] { FilterType.DBF, FilterType.ALL }, FilterType.DBF  ) ) );


    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    input.setChanged( changed );

    // Set the shell size, based upon previous time...
    setSize();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void setFlags() {
    wlFieldRownr.setEnabled( wAddRownr.getSelection() );
    wFieldRownr.setEnabled( wAddRownr.getSelection() );

    wlInclFilenameField.setEnabled( wInclFilename.getSelection() );
    wInclFilenameField.setEnabled( wInclFilename.getSelection() );

    wlFilename.setEnabled( !wAccFilenames.getSelection() );
    wFilename.setEnabled( !wAccFilenames.getSelection() );
    wbFilename.setEnabled( !wAccFilenames.getSelection() );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getDbfFileName() != null ) {
      wFilename.setText( input.getDbfFileName() );
      wFilename.setToolTipText( transMeta.environmentSubstitute( input.getDbfFileName() ) );
    }
    wFieldRownr.setText( Const.NVL( input.getRowNrField(), "" ) );

    wInclFilename.setSelection( input.includeFilename() );
    wInclFilenameField.setText( Const.NVL( input.getFilenameField(), "" ) );
    wCompression.setText( Const.NVL( input.getFileCompression(), "" ) );

    wAccFilenames.setSelection( input.isAcceptingFilenames() );
    wAccField.setText( Const.NVL( input.getAcceptingField(), "" ) );
    wAccStep.setText( Const.NVL( input.getAcceptingStepName(), "" ) );
    wCharactersetName.setText( Const.NVL( input.getCharactersetName(), "" ) );

    setFlags();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setRowNrAdded( backupAddRownr );
    input.setChanged( backupChanged );
    dispose();
  }

  public void getInfo( XBaseInputMeta meta ) throws KettleStepException {
    // copy info to Meta class (input)
    meta.setDbfFileName( wFilename.getText() );
    meta.setRowLimit( Const.toInt( wLimit.getText(), 0 ) );
    meta.setRowNrAdded( wAddRownr.getSelection() );
    meta.setRowNrField( wFieldRownr.getText() );

    meta.setIncludeFilename( wInclFilename.getSelection() );
    meta.setFilenameField( wInclFilenameField.getText() );
    meta.setFileCompression( wCompression.getText() );

    meta.setAcceptingFilenames( wAccFilenames.getSelection() );
    meta.setAcceptingField( wAccField.getText() );
    meta.setAcceptingStep( transMeta.findStep( wAccStep.getText() ) );
    meta.setCharactersetName( wCharactersetName.getText() );

    if ( Utils.isEmpty( meta.getDbfFileName() ) && !meta.isAcceptingFilenames() ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "XBaseInputDialog.Exception.SpecifyAFileToUse" ) );
    }
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    try {
      stepname = wStepname.getText(); // return value
      getInfo( input );
    } catch ( KettleStepException e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( e.toString() );
      mb.setText( BaseMessages.getString( PKG, "System.Warning" ) );
      mb.open();
    }
    dispose();
  }

  // Preview the data
  private void preview() {
    // Create the XML input step
    try {
      XBaseInputMeta oneMeta = new XBaseInputMeta();
      getInfo( oneMeta );

      if ( oneMeta.isAcceptingFilenames() ) {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
        mb.setMessage( BaseMessages.getString( PKG, "XBaseInputDialog.Dialog.SpecifyASampleFile.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "XBaseInputDialog.Dialog.SpecifyASampleFile.Title" ) );
        mb.open();
        return;
      }

      TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(
        transMeta,
        oneMeta,
        wStepname.getText() );

      EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
        BaseMessages.getString( PKG, "XBaseInputDialog.PreviewSize.DialogTitle" ),
        BaseMessages.getString( PKG, "XBaseInputDialog.PreviewSize.DialogMessage" ) );
      int previewSize = numberDialog.open();
      if ( previewSize > 0 ) {
        TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(
          shell,
          previewMeta,
          new String[] { wStepname.getText() },
          new int[] { previewSize } );
        progressDialog.open();

        Trans trans = progressDialog.getTrans();
        String loggingText = progressDialog.getLoggingText();

        if ( !progressDialog.isCancelled() ) {
          if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
            EnterTextDialog etd =
              new EnterTextDialog(
                shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
                  .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
            etd.setReadOnly();
            etd.open();
          }
        }

        PreviewRowsDialog prd =
          new PreviewRowsDialog(
            shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
              .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
        prd.open();
      }
    } catch ( Exception e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
        .getString( PKG, "System.Dialog.PreviewError.Message" ), e );
    }
  }
}
