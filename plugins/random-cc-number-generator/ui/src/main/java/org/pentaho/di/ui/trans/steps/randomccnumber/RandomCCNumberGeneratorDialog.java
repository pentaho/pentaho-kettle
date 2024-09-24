/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.randomccnumber;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.randomccnumber.RandomCCNumberGeneratorMeta;
import org.pentaho.di.trans.steps.randomccnumber.RandomCreditCardNumberGenerator;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.annotations.PluginDialog;

@PluginDialog( id = "RandomCCNumberGenerator", image = "CCG.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Generate+random+credit+card+numbers" )

public class RandomCCNumberGeneratorDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = RandomCCNumberGeneratorMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlStepname;
  private Text wStepname;
  private FormData fdlStepname, fdStepname;

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Group wOutputFields;
  private Label wlCCNumberField;
  private FormData fdlCCNumberField;
  private Text wCCNumberField;
  private FormData fdCCNumberField;

  private Label wlCCLengthField;
  private FormData fdlCCLengthField;
  private Label wlCCTypeField;
  private FormData fdlCCTypeField;
  private Text wCCLengthField;
  private FormData fdCCLengthField;
  private Text wCCTypeField;
  private FormData fdCCTypeField;

  private RandomCCNumberGeneratorMeta input;

  public RandomCCNumberGeneratorDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (RandomCCNumberGeneratorMeta) in;
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
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.DialogTitle" ) );

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

    // ///////////////////////////////
    // START OF OutputFields GROUP //
    // ///////////////////////////////

    wOutputFields = new Group( shell, SWT.SHADOW_NONE );
    props.setLook( wOutputFields );
    wOutputFields.setText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.wOutputFields.Label" ) );

    FormLayout OutputFieldsgroupLayout = new FormLayout();
    OutputFieldsgroupLayout.marginWidth = 10;
    OutputFieldsgroupLayout.marginHeight = 10;
    wOutputFields.setLayout( OutputFieldsgroupLayout );

    // CCNumberField fieldname ...
    wlCCNumberField = new Label( wOutputFields, SWT.RIGHT );
    wlCCNumberField
      .setText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCNumberFieldName.Label" ) );
    props.setLook( wlCCNumberField );
    fdlCCNumberField = new FormData();
    fdlCCNumberField.left = new FormAttachment( 0, 0 );
    fdlCCNumberField.right = new FormAttachment( middle, -margin );
    fdlCCNumberField.top = new FormAttachment( wStepname, margin * 2 );
    wlCCNumberField.setLayoutData( fdlCCNumberField );
    wCCNumberField = new Text( wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wCCNumberField.setToolTipText( BaseMessages.getString(
      PKG, "RandomCCNumberGeneratorDialog.CCNumberFieldName.Tooltip" ) );
    props.setLook( wCCNumberField );
    wCCNumberField.addModifyListener( lsMod );
    fdCCNumberField = new FormData();
    fdCCNumberField.left = new FormAttachment( middle, 0 );
    fdCCNumberField.top = new FormAttachment( wStepname, margin * 2 );
    fdCCNumberField.right = new FormAttachment( 100, 0 );
    wCCNumberField.setLayoutData( fdCCNumberField );

    // CCTypeField fieldname ...
    wlCCTypeField = new Label( wOutputFields, SWT.RIGHT );
    wlCCTypeField.setText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCType.Label" ) );
    props.setLook( wlCCTypeField );
    fdlCCTypeField = new FormData();
    fdlCCTypeField.left = new FormAttachment( 0, 0 );
    fdlCCTypeField.right = new FormAttachment( middle, -margin );
    fdlCCTypeField.top = new FormAttachment( wCCNumberField, margin );
    wlCCTypeField.setLayoutData( fdlCCTypeField );
    wCCTypeField = new Text( wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wCCTypeField.setToolTipText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCType.Tooltip" ) );
    props.setLook( wCCTypeField );
    wCCTypeField.addModifyListener( lsMod );
    fdCCTypeField = new FormData();
    fdCCTypeField.left = new FormAttachment( middle, 0 );
    fdCCTypeField.top = new FormAttachment( wCCNumberField, margin );
    fdCCTypeField.right = new FormAttachment( 100, 0 );
    wCCTypeField.setLayoutData( fdCCTypeField );

    // CCLengthField fieldname ...
    wlCCLengthField = new Label( wOutputFields, SWT.RIGHT );
    wlCCLengthField.setText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCLength.Label" ) );
    props.setLook( wlCCLengthField );
    fdlCCLengthField = new FormData();
    fdlCCLengthField.left = new FormAttachment( 0, 0 );
    fdlCCLengthField.right = new FormAttachment( middle, -margin );
    fdlCCLengthField.top = new FormAttachment( wCCTypeField, margin );
    wlCCLengthField.setLayoutData( fdlCCLengthField );
    wCCLengthField = new Text( wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wCCLengthField
      .setToolTipText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCLength.Tooltip" ) );
    props.setLook( wCCLengthField );
    wCCLengthField.addModifyListener( lsMod );
    fdCCLengthField = new FormData();
    fdCCLengthField.left = new FormAttachment( middle, 0 );
    fdCCLengthField.top = new FormAttachment( wCCTypeField, margin );
    fdCCLengthField.right = new FormAttachment( 100, 0 );
    wCCLengthField.setLayoutData( fdCCLengthField );

    FormData fdOutputFields = new FormData();
    fdOutputFields.left = new FormAttachment( 0, margin );
    fdOutputFields.top = new FormAttachment( wStepname, 2 * margin );
    fdOutputFields.right = new FormAttachment( 100, -margin );
    wOutputFields.setLayoutData( fdOutputFields );

    // ///////////////////////////////////////////////////////////
    // / END OF OutputFields GROUP
    // ///////////////////////////////////////////////////////////

    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( wOutputFields, margin );
    wlFields.setLayoutData( fdlFields );

    final int FieldsCols = 3;
    final int FieldsRows = input.getFieldCCType().length;

    ColumnInfo[] colinf = new ColumnInfo[FieldsCols];
    colinf[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCTypeColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, RandomCreditCardNumberGenerator.cardTypes );
    colinf[0].setReadOnly( true );
    colinf[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCLengthColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[1].setUsingVariables( true );
    colinf[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.CCSizeColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[2].setUsingVariables( true );

    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.Button.PreviewRows" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wFields );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wPreview.addListener( SWT.Selection, lsPreview );
    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

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

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wStepname.setText( stepname );

    for ( int i = 0; i < input.getFieldCCType().length; i++ ) {
      TableItem item = wFields.table.getItem( i );
      String type = input.getFieldCCType()[i];
      String len = input.getFieldCCLength()[i];
      String size = input.getFieldCCSize()[i];

      if ( type != null ) {
        item.setText( 1, type );
      }
      if ( len != null ) {
        item.setText( 2, len );
      }
      if ( size != null ) {
        item.setText( 3, size );
      }
    }

    wFields.setRowNums();
    wFields.optWidth( true );

    if ( input.getCardNumberFieldName() != null ) {
      wCCNumberField.setText( input.getCardNumberFieldName() );
    }
    if ( input.getCardTypeFieldName() != null ) {
      wCCTypeField.setText( input.getCardTypeFieldName() );
    }
    if ( input.getCardLengthFieldName() != null ) {
      wCCLengthField.setText( input.getCardLengthFieldName() );
    }

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

    try {
      getInfo( input );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, "Error", "Error saving step informations", e );
    }
    dispose();
  }

  private void getInfo( RandomCCNumberGeneratorMeta in ) throws KettleException {

    stepname = wStepname.getText(); // return value
    int count = wFields.nrNonEmpty();
    in.allocate( count );

    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < count; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      in.getFieldCCType()[i] = item.getText( 1 );
      in.getFieldCCLength()[i] = item.getText( 2 );
      in.getFieldCCSize()[i] = item.getText( 3 );
    }
    in.setCardNumberFieldName( wCCNumberField.getText() );
    in.setCardTypeFieldName( wCCTypeField.getText() );
    in.setCardLengthFieldName( wCCLengthField.getText() );
  }

  // Preview the data
  private void preview() {
    try {
      // Create the RandomCCNumberGeneratorMeta input step
      RandomCCNumberGeneratorMeta oneMeta = new RandomCCNumberGeneratorMeta();
      getInfo( oneMeta );

      TransMeta previewMeta =
        TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );
      EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
        BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.NumberRows.DialogTitle" ),
        BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.NumberRows.DialogMessage" ) );

      int previewSize = numberDialog.open();
      if ( previewSize > 0 ) {
        TransPreviewProgressDialog progressDialog =
          new TransPreviewProgressDialog(
            shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
        progressDialog.open();

        if ( !progressDialog.isCancelled() ) {
          Trans trans = progressDialog.getTrans();
          String loggingText = progressDialog.getLoggingText();

          if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
            EnterTextDialog etd =
              new EnterTextDialog(
                shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
                  .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
            etd.setReadOnly();
            etd.open();
          }

          PreviewRowsDialog prd =
            new PreviewRowsDialog(
              shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
                .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
          prd.open();

        }
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.ErrorPreviewingData.DialogTitle" ),
        BaseMessages.getString( PKG, "RandomCCNumberGeneratorDialog.ErrorPreviewingData.DialogMessage" ), e );
    }
  }
}
