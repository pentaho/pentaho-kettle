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
package org.pentaho.di.trans.steps.s3csvinput;

import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jets3t.service.model.S3Bucket;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.textfileinput.TextFileCSVImportProgressDialog;

public class S3CsvInputDialog extends BaseStepDialog implements StepDialogInterface {
  private S3CsvInputMeta inputMeta;

  private TextVar      wBucket;
  private Button       wbBucket; // browse for a bucket.
  private TextVar      wFilename;
  private CCombo       wFilenameField;
  private Button       wbbFilename; // Browse for a file
  private Button       wIncludeFilename;
  private TextVar      wRowNumField;
  private Button       wbDelimiter;
  private TextVar      wDelimiter;
  private TextVar      wEnclosure;
  private TextVar      wMaxLineSize;
  private Button       wLazyConversion;
  private Button       wHeaderPresent;

  private TableView    wFields;

  private boolean isReceivingInput;
  private Button wRunningInParallel;

  public S3CsvInputDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    inputMeta = (S3CsvInputMeta) in;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, inputMeta );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        inputMeta.setChanged();
      }
    };
    changed = inputMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth  = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( Messages.getString( "S3CsvInputDialog.Shell.Title" ) ); //$NON-NLS-1$

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Step name line
    //
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( Messages.getString( "S3CsvInputDialog.Stepname.Label" ) ); //$NON-NLS-1$
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );
    Control lastControl = wStepname;

    // Bucket name
    Label wlBucket = new Label( shell, SWT.RIGHT );
    wlBucket.setText( Messages.getString( "S3CsvInputDialog.Bucket.Label" ) ); //$NON-NLS-1$
    props.setLook( wlBucket );
    FormData fdlBucket = new FormData();
    fdlBucket.top = new FormAttachment( lastControl, margin );
    fdlBucket.left = new FormAttachment( 0, 0 );
    fdlBucket.right = new FormAttachment( middle, -margin );
    wlBucket.setLayoutData( fdlBucket );
    wbBucket = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbBucket );
    wbBucket.setText( Messages.getString( "S3CsvInputDialog.Bucket.Button" ) );
    FormData fdbBucket = new FormData();
    fdbBucket.top = new FormAttachment( lastControl, margin );
    fdbBucket.right = new FormAttachment( 100, 0 );
    wbBucket.setLayoutData( fdbBucket );
    wBucket = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBucket );
    wBucket.addModifyListener( lsMod );
    FormData fdBucket = new FormData();
    fdBucket.top = new FormAttachment( lastControl, margin );
    fdBucket.left = new FormAttachment( middle, 0 );
    fdBucket.right = new FormAttachment( wbBucket, -margin );
    wBucket.setLayoutData( fdBucket );
    lastControl = wBucket;

    // See if the step receives input.  If so, we don't ask for the filename, but for the filename field.
    //
    isReceivingInput = transMeta.findNrPrevSteps( stepMeta ) > 0;
    if ( isReceivingInput ) {

      RowMetaInterface previousFields;
      try {
        previousFields = transMeta.getPrevStepFields( stepMeta );
      } catch ( KettleStepException e ) {
        new ErrorDialog( shell, Messages.getString( "S3CsvInputDialog.ErrorDialog.UnableToGetInputFields.Title" ), Messages.getString( "S3CsvInputDialog.ErrorDialog.UnableToGetInputFields.Message" ), e );
        previousFields = new RowMeta();
      }

      // The filename field ...
      //
      Label wlFilename = new Label( shell, SWT.RIGHT );
      wlFilename.setText( Messages.getString( "S3CsvInputDialog.FilenameField.Label" ) ); //$NON-NLS-1$
      props.setLook( wlFilename );
      FormData fdlFilename = new FormData();
      fdlFilename.top = new FormAttachment( lastControl, margin );
      fdlFilename.left = new FormAttachment( 0, 0 );
      fdlFilename.right = new FormAttachment( middle, -margin );
      wlFilename.setLayoutData( fdlFilename );
      wFilenameField = new CCombo( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      wFilenameField.setItems( previousFields.getFieldNames() );
      props.setLook( wFilenameField );
      wFilenameField.addModifyListener( lsMod );
      FormData fdFilename = new FormData();
      fdFilename.top = new FormAttachment( lastControl, margin );
      fdFilename.left = new FormAttachment( middle, 0 );
      fdFilename.right = new FormAttachment( 100, 0 );
      wFilenameField.setLayoutData( fdFilename );
      lastControl = wFilenameField;

      // Checkbox to include the filename in the output...
      //
      Label wlIncludeFilename = new Label( shell, SWT.RIGHT );
      wlIncludeFilename.setText( Messages.getString( "S3CsvInputDialog.IncludeFilenameField.Label" ) ); //$NON-NLS-1$
      props.setLook( wlIncludeFilename );
      FormData fdlIncludeFilename = new FormData();
      fdlIncludeFilename.top = new FormAttachment( lastControl, margin );
      fdlIncludeFilename.left = new FormAttachment( 0, 0 );
      fdlIncludeFilename.right = new FormAttachment( middle, -margin );
      wlIncludeFilename.setLayoutData( fdlIncludeFilename );
      wIncludeFilename = new Button( shell, SWT.CHECK );
      props.setLook( wIncludeFilename );
      wFilenameField.addModifyListener( lsMod );
      FormData fdIncludeFilename = new FormData();
      fdIncludeFilename.top = new FormAttachment( lastControl, margin );
      fdIncludeFilename.left = new FormAttachment( middle, 0 );
      fdIncludeFilename.right = new FormAttachment( 100, 0 );
      wIncludeFilename.setLayoutData( fdIncludeFilename );
      lastControl = wIncludeFilename;
    } else {

      // Filename...
      //
      // The filename browse button
      //
      wbbFilename = new Button( shell, SWT.PUSH | SWT.CENTER );
      props.setLook( wbbFilename );
      wbbFilename.setText( Messages.getString( "System.Button.Browse" ) );
      wbbFilename.setToolTipText( Messages.getString( "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
      FormData fdbFilename = new FormData();
      fdbFilename.top = new FormAttachment( lastControl, margin );
      fdbFilename.right = new FormAttachment( 100, 0 );
      wbbFilename.setLayoutData( fdbFilename );

      // The field itself...
      //
      Label wlFilename = new Label( shell, SWT.RIGHT );
      wlFilename.setText( Messages.getString( "S3CsvInputDialog.Filename.Label" ) ); //$NON-NLS-1$
      props.setLook( wlFilename );
      FormData fdlFilename = new FormData();
      fdlFilename.top = new FormAttachment( lastControl, margin );
      fdlFilename.left = new FormAttachment( 0, 0 );
      fdlFilename.right = new FormAttachment( middle, -margin );
      wlFilename.setLayoutData( fdlFilename );
      wFilename = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      props.setLook( wFilename );
      wFilename.addModifyListener( lsMod );
      FormData fdFilename = new FormData();
      fdFilename.top = new FormAttachment( lastControl, margin );
      fdFilename.left = new FormAttachment( middle, 0 );
      fdFilename.right = new FormAttachment( wbbFilename, -margin );
      wFilename.setLayoutData( fdFilename );
      lastControl = wFilename;
    }

    // delimiter
    Label wlDelimiter = new Label( shell, SWT.RIGHT );
    wlDelimiter.setText( Messages.getString( "S3CsvInputDialog.Delimiter.Label" ) ); //$NON-NLS-1$
    props.setLook( wlDelimiter );
    FormData fdlDelimiter = new FormData();
    fdlDelimiter.top = new FormAttachment( lastControl, margin );
    fdlDelimiter.left = new FormAttachment( 0, 0 );
    fdlDelimiter.right = new FormAttachment( middle, -margin );
    wlDelimiter.setLayoutData( fdlDelimiter );
    wbDelimiter = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDelimiter );
    wbDelimiter.setText( Messages.getString( "S3CsvInputDialog.Delimiter.Button" ) );
    FormData fdbDelimiter = new FormData();
    fdbDelimiter.top = new FormAttachment( lastControl, margin );
    fdbDelimiter.right = new FormAttachment( 100, 0 );
    wbDelimiter.setLayoutData( fdbDelimiter );
    wDelimiter = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDelimiter );
    wDelimiter.addModifyListener( lsMod );
    FormData fdDelimiter = new FormData();
    fdDelimiter.top = new FormAttachment( lastControl, margin );
    fdDelimiter.left = new FormAttachment( middle, 0 );
    fdDelimiter.right = new FormAttachment( wbDelimiter, -margin );
    wDelimiter.setLayoutData( fdDelimiter );
    lastControl = wDelimiter;

    // enclosure
    Label wlEnclosure = new Label( shell, SWT.RIGHT );
    wlEnclosure.setText( Messages.getString( "S3CsvInputDialog.Enclosure.Label" ) ); //$NON-NLS-1$
    props.setLook( wlEnclosure );
    FormData fdlEnclosure = new FormData();
    fdlEnclosure.top = new FormAttachment( lastControl, margin );
    fdlEnclosure.left = new FormAttachment( 0, 0 );
    fdlEnclosure.right = new FormAttachment( middle, -margin );
    wlEnclosure.setLayoutData( fdlEnclosure );
    wEnclosure = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEnclosure );
    wEnclosure.addModifyListener( lsMod );
    FormData fdEnclosure = new FormData();
    fdEnclosure.top = new FormAttachment( lastControl, margin );
    fdEnclosure.left = new FormAttachment( middle, 0 );
    fdEnclosure.right = new FormAttachment( 100, 0 );
    wEnclosure.setLayoutData( fdEnclosure );
    lastControl = wEnclosure;

    // Max line size
    //
    Label wlMaxLineSize = new Label( shell, SWT.RIGHT );
    wlMaxLineSize.setText( Messages.getString( "S3CsvInputDialog.MaxLineSize.Label" ) ); //$NON-NLS-1$
    props.setLook( wlMaxLineSize );
    FormData fdlMaxLineSize = new FormData();
    fdlMaxLineSize.top = new FormAttachment( lastControl, margin );
    fdlMaxLineSize.left = new FormAttachment( 0, 0 );
    fdlMaxLineSize.right = new FormAttachment( middle, -margin );
    wlMaxLineSize.setLayoutData( fdlMaxLineSize );
    wMaxLineSize = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMaxLineSize );
    wMaxLineSize.addModifyListener( lsMod );
    FormData fdMaxLineSize = new FormData();
    fdMaxLineSize.top = new FormAttachment( lastControl, margin );
    fdMaxLineSize.left = new FormAttachment( middle, 0 );
    fdMaxLineSize.right = new FormAttachment( 100, 0 );
    wMaxLineSize.setLayoutData( fdMaxLineSize );
    lastControl = wMaxLineSize;

    // performingLazyConversion?
    //
    Label wlLazyConversion = new Label( shell, SWT.RIGHT );
    wlLazyConversion.setText( Messages.getString( "S3CsvInputDialog.LazyConversion.Label" ) ); //$NON-NLS-1$
    props.setLook( wlLazyConversion );
    FormData fdlLazyConversion = new FormData();
    fdlLazyConversion.top = new FormAttachment( lastControl, margin );
    fdlLazyConversion.left = new FormAttachment( 0, 0 );
    fdlLazyConversion.right = new FormAttachment( middle, -margin );
    wlLazyConversion.setLayoutData( fdlLazyConversion );
    wLazyConversion = new Button( shell, SWT.CHECK );
    props.setLook( wLazyConversion );
    FormData fdLazyConversion = new FormData();
    fdLazyConversion.top = new FormAttachment( lastControl, margin );
    fdLazyConversion.left = new FormAttachment( middle, 0 );
    fdLazyConversion.right = new FormAttachment( 100, 0 );
    wLazyConversion.setLayoutData( fdLazyConversion );
    lastControl = wLazyConversion;

    // header row?
    //
    Label wlHeaderPresent = new Label( shell, SWT.RIGHT );
    wlHeaderPresent.setText( Messages.getString( "S3CsvInputDialog.HeaderPresent.Label" ) ); //$NON-NLS-1$
    props.setLook( wlHeaderPresent );
    FormData fdlHeaderPresent = new FormData();
    fdlHeaderPresent.top = new FormAttachment( lastControl, margin );
    fdlHeaderPresent.left = new FormAttachment( 0, 0 );
    fdlHeaderPresent.right = new FormAttachment( middle, -margin );
    wlHeaderPresent.setLayoutData( fdlHeaderPresent );
    wHeaderPresent = new Button( shell, SWT.CHECK );
    props.setLook( wHeaderPresent );
    FormData fdHeaderPresent = new FormData();
    fdHeaderPresent.top = new FormAttachment( lastControl, margin );
    fdHeaderPresent.left = new FormAttachment( middle, 0 );
    fdHeaderPresent.right = new FormAttachment( 100, 0 );
    wHeaderPresent.setLayoutData( fdHeaderPresent );
    lastControl = wHeaderPresent;

        // The field itself...
        //
    Label wlRowNumField = new Label( shell, SWT.RIGHT );
    wlRowNumField.setText( Messages.getString( "S3CsvInputDialog.RowNumField.Label" ) ); //$NON-NLS-1$
    props.setLook( wlRowNumField );
    FormData fdlRowNumField = new FormData();
    fdlRowNumField.top = new FormAttachment( lastControl, margin );
    fdlRowNumField.left = new FormAttachment( 0, 0 );
    fdlRowNumField.right = new FormAttachment( middle, -margin );
    wlRowNumField.setLayoutData( fdlRowNumField );
    wRowNumField = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wRowNumField );
    wRowNumField.addModifyListener( lsMod );
    FormData fdRowNumField = new FormData();
    fdRowNumField.top = new FormAttachment( lastControl, margin );
    fdRowNumField.left = new FormAttachment( middle, 0 );
    fdRowNumField.right = new FormAttachment( 100, 0 );
    wRowNumField.setLayoutData( fdRowNumField );
    lastControl = wRowNumField;

    // running in parallel?
    //
    Label wlRunningInParallel = new Label( shell, SWT.RIGHT );
    wlRunningInParallel.setText( Messages.getString( "S3CsvInputDialog.RunningInParallel.Label" ) ); //$NON-NLS-1$
    props.setLook( wlRunningInParallel );
    FormData fdlRunningInParallel = new FormData();
    fdlRunningInParallel.top = new FormAttachment( lastControl, margin );
    fdlRunningInParallel.left = new FormAttachment( 0, 0 );
    fdlRunningInParallel.right = new FormAttachment( middle, -margin );
    wlRunningInParallel.setLayoutData( fdlRunningInParallel );
    wRunningInParallel = new Button( shell, SWT.CHECK );
    props.setLook( wRunningInParallel );
    FormData fdRunningInParallel = new FormData();
    fdRunningInParallel.top = new FormAttachment( lastControl, margin );
    fdRunningInParallel.left = new FormAttachment( middle, 0 );
    wRunningInParallel.setLayoutData( fdRunningInParallel );
    lastControl = wRunningInParallel;

    // Some buttons first, so that the dialog scales nicely...
    //
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( Messages.getString( "System.Button.OK" ) ); //$NON-NLS-1$
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( Messages.getString( "System.Button.Cancel" ) ); //$NON-NLS-1$
    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( Messages.getString( "System.Button.Preview" ) ); //$NON-NLS-1$
    wPreview.setEnabled( !isReceivingInput );
    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( Messages.getString( "System.Button.GetFields" ) ); //$NON-NLS-1$
    wGet.setEnabled( !isReceivingInput );

    setButtonPositions( new Button[] { wOK, wCancel, wPreview, wGet, }, margin, null );


    // Fields
    ColumnInfo[] colinf = new ColumnInfo[] {
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.NameColumn.Column" ),       ColumnInfo.COLUMN_TYPE_TEXT,    false ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.TypeColumn.Column" ),       ColumnInfo.COLUMN_TYPE_CCOMBO,  ValueMeta.getTypes(), true ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.FormatColumn.Column" ),     ColumnInfo.COLUMN_TYPE_CCOMBO,  Const.getConversionFormats() ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.LengthColumn.Column" ),     ColumnInfo.COLUMN_TYPE_TEXT,    false ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.PrecisionColumn.Column" ),  ColumnInfo.COLUMN_TYPE_TEXT,    false ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.CurrencyColumn.Column" ),   ColumnInfo.COLUMN_TYPE_TEXT,    false ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.DecimalColumn.Column" ),    ColumnInfo.COLUMN_TYPE_TEXT,    false ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.GroupColumn.Column" ),      ColumnInfo.COLUMN_TYPE_TEXT,    false ),
      new ColumnInfo( Messages.getString( "S3CsvInputDialog.TrimTypeColumn.Column" ),   ColumnInfo.COLUMN_TYPE_CCOMBO,  ValueMeta.trimTypeDesc ),
    };

    colinf[2].setComboValuesSelectionListener( new ComboValuesSelectionListener() {
      @Override
      public String[] getComboValues( TableItem tableItem, int rowNr, int colNr ) {
        String[] comboValues = new String[] { };
        int type = ValueMeta.getType( tableItem.getText( colNr - 1 ) );
        switch ( type ) {
          case ValueMetaInterface.TYPE_DATE:
            comboValues = Const.getDateFormats();
            break;
          case ValueMetaInterface.TYPE_INTEGER:
          case ValueMetaInterface.TYPE_BIGNUMBER:
          case ValueMetaInterface.TYPE_NUMBER:
            comboValues = Const.getNumberFormats();
            break;
          default:
            break;
        }
        return comboValues;
      }

    } );


    wFields = new TableView( transMeta, shell, SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.top = new FormAttachment( lastControl, margin * 2 );
    fdFields.bottom = new FormAttachment( wOK, -margin * 2 );
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    wFields.setLayoutData( fdFields );

    // Add listeners
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsPreview = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsGet = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        getCSV();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    wPreview.addListener( SWT.Selection, lsPreview );
    wGet.addListener( SWT.Selection, lsGet );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    if ( wFilename != null ) {
      wFilename.addSelectionListener( lsDef );
    }
    if ( wFilenameField != null ) {
      wFilenameField.addSelectionListener( lsDef );
    }
    wDelimiter.addSelectionListener( lsDef );
    wEnclosure.addSelectionListener( lsDef );
    wMaxLineSize.addSelectionListener( lsDef );
    wRowNumField.addSelectionListener( lsDef );

    // Allow the insertion of tabs as separator...
    wbDelimiter.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent se ) {
        Text t = wDelimiter.getTextWidget();
        if ( t != null ) {
          t.insert( "\t" );
        }
      }
    } );

    wbBucket.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        // List the buckets...
        //
        try {
          S3CsvInputMeta meta = new S3CsvInputMeta();
          getInfo( meta );
          S3ObjectsProvider s3ObjProvider = new S3ObjectsProvider( meta.getS3Service( transMeta ) );

          EnterSelectionDialog dialog = new EnterSelectionDialog( shell, s3ObjProvider.getBucketsNames(),
              Messages.getString( "S3CsvInputDialog.Exception.SelectBucket.Title" ),
              Messages.getString( "S3CsvInputDialog.Exception.SelectBucket.Message" ) );
          dialog.setMulti( false );
          String bucketname = dialog.open();
          if ( bucketname != null ) {
            wBucket.setText( bucketname );
          }
        } catch ( Exception e ) {
          new ErrorDialog( shell,
              Messages.getString( "S3CsvInputDialog.Exception.UnableToGetBuckets.Title" ),
              Messages.getString( "S3CsvInputDialog.Exception.UnableToGetBuckets.Message" ), e );
        }
      }
    } );


    if ( wbbFilename != null ) {
      // Listen to the browse button next to the file name
      wbbFilename.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent event ) {
          try {
            S3CsvInputMeta meta = new S3CsvInputMeta();
            getInfo( meta );

            S3ObjectsProvider s3ObjProvider = new S3ObjectsProvider( meta.getS3Service( transMeta ) );
            String[] objectnames = s3ObjProvider.getS3ObjectsNames( meta.getBucket() );

            EnterSelectionDialog dialog = new EnterSelectionDialog( shell, objectnames,
              Messages.getString( "S3CsvInputDialog.Exception.SelectObject.Title" ),
              Messages.getString( "S3CsvInputDialog.Exception.SelectObject.Message" ) );
            dialog.setMulti( false );
            if ( !Utils.isEmpty( wFilename.getText() ) ) {
              int index = Const.indexOfString( wFilename.getText(), objectnames );
              if ( index >= 0 ) {
                dialog.setSelectedNrs( new int[] { index, } );
              }
            }
            String objectname = dialog.open();
            if ( objectname != null ) {
              wFilename.setText( objectname );
            }
          } catch ( Exception e ) {
            new ErrorDialog( shell,
              Messages.getString( "S3CsvInputDialog.Exception.UnableToGetFiles.Title" ),
              Messages.getString( "S3CsvInputDialog.Exception.UnableToGetFiles.Message" ), e );
          }
        }
      } );
    }


    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );


    // Set the shell size, based upon previous time...
    setSize();

    getData();
    inputMeta.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  public void getData() {
    getData( inputMeta );
  }
  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData( S3CsvInputMeta inputMeta ) {
    wStepname.setText( stepname );
    wBucket.setText( Const.NVL( inputMeta.getBucket(), "" ) );

    if ( isReceivingInput ) {
      wFilenameField.setText( Const.NVL( inputMeta.getFilenameField(), "" ) );
      wIncludeFilename.setSelection( inputMeta.isIncludingFilename() );
    } else {
      wFilename.setText( Const.NVL( inputMeta.getFilename(), "" ) );
    }
    wDelimiter.setText( Const.NVL( inputMeta.getDelimiter(), "" ) );
    wEnclosure.setText( Const.NVL( inputMeta.getEnclosure(), "" ) );
    wMaxLineSize.setText( Const.NVL( inputMeta.getMaxLineSize(), "" ) );
    wLazyConversion.setSelection( inputMeta.isLazyConversionActive() );
    wHeaderPresent.setSelection( inputMeta.isHeaderPresent() );
    wRunningInParallel.setSelection( inputMeta.isRunningInParallel() );
    wRowNumField.setText( Const.NVL( inputMeta.getRowNumField(), "" ) );

    for ( int i = 0; i < inputMeta.getInputFields().length; i++ ) {
      TextFileInputField field = inputMeta.getInputFields()[i];

      TableItem item = new TableItem( wFields.table, SWT.NONE );
      int colnr = 1;
      item.setText( colnr++, Const.NVL( field.getName(), "" ) );
      item.setText( colnr++, ValueMeta.getTypeDesc( field.getType() ) );
      item.setText( colnr++, Const.NVL( field.getFormat(), "" ) );
      item.setText( colnr++, field.getLength() >= 0 ? Integer.toString( field.getLength() ) : "" );
      item.setText( colnr++, field.getPrecision() >= 0 ? Integer.toString( field.getPrecision() ) : "" );
      item.setText( colnr++, Const.NVL( field.getCurrencySymbol(), "" ) );
      item.setText( colnr++, Const.NVL( field.getDecimalSymbol(), "" ) );
      item.setText( colnr++, Const.NVL( field.getGroupSymbol(), "" ) );
      item.setText( colnr++, Const.NVL( field.getTrimTypeDesc(), "" ) );
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    wStepname.selectAll();
  }

  private void cancel() {
    stepname = null;
    inputMeta.setChanged( changed );
    dispose();
  }

  private void getInfo( S3CsvInputMeta inputMeta ) {

    inputMeta.setBucket( wBucket.getText() );

    if ( isReceivingInput ) {
      inputMeta.setFilenameField( wFilenameField.getText() );
      inputMeta.setIncludingFilename( wIncludeFilename.getSelection() );
    } else {
      inputMeta.setFilename( wFilename.getText() );
    }

    inputMeta.setDelimiter( wDelimiter.getText() );
    inputMeta.setEnclosure( wEnclosure.getText() );
    inputMeta.setMaxLineSize( wMaxLineSize.getText() );
    inputMeta.setLazyConversionActive( wLazyConversion.getSelection() );
    inputMeta.setHeaderPresent( wHeaderPresent.getSelection() );
    inputMeta.setRowNumField( wRowNumField.getText() );
    inputMeta.setRunningInParallel( wRunningInParallel.getSelection() );

    int nrNonEmptyFields = wFields.nrNonEmpty();
    inputMeta.allocate( nrNonEmptyFields );

    for ( int i = 0; i < nrNonEmptyFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      inputMeta.getInputFields()[i] = new TextFileInputField();

      int colnr = 1;
      inputMeta.getInputFields()[i].setName( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setType( ValueMeta.getType( item.getText( colnr++ ) ) );
      inputMeta.getInputFields()[i].setFormat( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setLength( Const.toInt( item.getText( colnr++ ), -1 ) );
      inputMeta.getInputFields()[i].setPrecision( Const.toInt( item.getText( colnr++ ), -1 ) );
      inputMeta.getInputFields()[i].setCurrencySymbol( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setDecimalSymbol( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setGroupSymbol( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setTrimType( ValueMeta.getTrimTypeByDesc( item.getText( colnr++ ) ) );
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    inputMeta.setChanged();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    getInfo( inputMeta );
    stepname = wStepname.getText();
    dispose();
  }

  // Get the data layout
  private void getCSV() {
    InputStream inputStream = null;
    try {
      S3CsvInputMeta meta = new S3CsvInputMeta();
      getInfo( meta );

      String filename = transMeta.environmentSubstitute( meta.getFilename() );
      String bucketname = transMeta.environmentSubstitute( meta.getBucket() );
      int maxLineSize = Const.toInt( transMeta.environmentSubstitute( meta.getMaxLineSize() ), 2000 );

      wFields.table.removeAll();

      S3ObjectsProvider s3ObjProvider = new S3ObjectsProvider( meta.getS3Service( transMeta ) );
      S3Bucket s3bucket = s3ObjProvider.getBucket( bucketname );

      if ( s3bucket == null ) {
        throw new Exception( Messages.getString( "S3DefaultService.Exception.UnableToFindBucket.Message", bucketname ) );
      }

      // Now we can continue reading the rows of data and we can guess the
      // Sample a few lines to determine the correct type of the fields...
      //
      String shellText = Messages.getString( "S3CsvInputDialog.LinesToSample.DialogTitle" );
      String lineText = Messages.getString( "S3CsvInputDialog.LinesToSample.DialogMessage" );
      EnterNumberDialog end = new EnterNumberDialog( shell, 100, shellText, lineText );
      int samples = end.open();
      if ( samples < 0 ) {
        return;
      }

      // Only get the first lines, not the complete file
      // And grab an input stream to the data...
      inputStream = s3ObjProvider.getS3Object( s3bucket, filename, 0L, (long) samples * (long) maxLineSize ).getDataInputStream();

      InputStreamReader reader = new InputStreamReader( inputStream );

      // Read a line of data to determine the number of rows...
      //
      String line = TextFileInput.getLine( log, reader, TextFileInputMeta.FILE_FORMAT_MIXED, new StringBuilder( 1000 ) );

      // Split the string, header or data into parts...
      //
      String[] fieldNames = Const.splitString( line, meta.getDelimiter() );

      if ( !meta.isHeaderPresent() ) {
        // Don't use field names from the header...
        // Generate field names F1 ... F10
        //
        DecimalFormat df = new DecimalFormat( "000" ); // $NON-NLS-1$
        for ( int i = 0; i < fieldNames.length; i++ ) {
          fieldNames[i] = "Field_" + df.format( i ); // $NON-NLS-1$
        }
      } else {
        if ( !Utils.isEmpty( meta.getEnclosure() ) ) {
          for ( int i = 0; i < fieldNames.length; i++ ) {
            if ( fieldNames[i].startsWith( meta.getEnclosure() ) && fieldNames[i].endsWith( meta.getEnclosure() ) && fieldNames[i].length() > 1 ) {
              fieldNames[i] = fieldNames[i].substring( 1, fieldNames[i].length() - 1 );
            }
          }
        }
      }

      // Trim the names to make sure...
      //
      for ( int i = 0; i < fieldNames.length; i++ ) {
        fieldNames[i] = Const.trim( fieldNames[i] );
      }

      // Update the GUI
      //
      for ( int i = 0; i < fieldNames.length; i++ ) {
        TableItem item = new TableItem( wFields.table, SWT.NONE );
        item.setText( 1, fieldNames[i] );
        item.setText( 2, ValueMeta.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
      }
      wFields.removeEmptyRows();
      wFields.setRowNums();
      wFields.optWidth( true );

      getInfo( meta );

      TextFileCSVImportProgressDialog pd = new TextFileCSVImportProgressDialog( shell, meta, transMeta, reader, samples, true );
      String message = pd.open();
      if ( message != null ) {
        wFields.removeAll();

        // OK, what's the result of our search?
        getData( meta );
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth( true );

        EnterTextDialog etd = new EnterTextDialog( shell, Messages.getString( "S3CsvInputDialog.ScanResults.DialogTitle" ), Messages.getString( "S3CsvInputDialog.ScanResults.DialogMessage" ), message, true );
        etd.setReadOnly();
        etd.open();
      }
    } catch ( IOException e ) {
      new ErrorDialog( shell, Messages.getString( "S3CsvInputDialog.IOError.DialogTitle" ), Messages.getString( "S3CsvInputDialog.IOError.DialogMessage" ), e );
    } catch ( Exception e ) {
      new ErrorDialog( shell, Messages.getString( "System.Dialog.Error.Title" ), Messages.getString( "S3CsvInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    } finally {
      try {
        if ( inputStream != null ) {
          inputStream.close();
        }
      } catch ( Exception e ) {
        log.logError( stepname, "Error closing s3 data input stream", e );
      }
    }
  }

  // Preview the data
  private void preview() {
    // Create the XML input step
    S3CsvInputMeta oneMeta = new S3CsvInputMeta();
    getInfo( oneMeta );

    TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

    EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(), Messages.getString( "S3CsvInputDialog.PreviewSize.DialogTitle" ), Messages.getString( "S3CsvInputDialog.PreviewSize.DialogMessage" ) );
    int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog( shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() ) {
        if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd = new EnterTextDialog( shell, Messages.getString( "System.Dialog.PreviewError.Title" ),
            Messages.getString( "System.Dialog.PreviewError.Message" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd = new PreviewRowsDialog( shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname.getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
      prd.open();
    }
  }
}
