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

package org.pentaho.di.ui.trans.steps.execprocess;

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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.execprocess.ExecProcessMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ExecProcessDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ExecProcessMeta.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;
  private CTabItem wGeneralTab, wOutputTab;
  private Composite wGeneralComp, wOutputComp;

  private Label wlProcess;
  private CCombo wProcess;
  private FormData fdlProcess, fdProcess;

  private Label wlArgumentsInFields;
  private Button wArgumentsInFields;
  private FormData fdlArgumentsInFields, fdArgumentsInFields;

  private Label wlArgumentFields;
  private TableView wArgumentFields;
  private FormData fdlArgumentFields, fdArgumentFields;

  private Label wlFailWhenNotSuccess;
  private Button wFailWhenNotSuccess;
  private FormData fdlFailWhenNotSuccess, fdFailWhenNotSuccess;

  private LabelTextVar wOutputDelim, wResult, wExitValue, wError;
  private FormData fdOutputDelim, fdResult, fdExitValue, fdError;

  private ExecProcessMeta input;
  private boolean gotPreviousFields = false;

  public ExecProcessDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (ExecProcessMeta) in;
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
    shell.setText( BaseMessages.getString( PKG, "ExecProcessDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "ExecProcessDialog.Stepname.Label" ) );
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

    // The Tab Folders
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook(  wTabFolder, Props.WIDGET_STYLE_TAB );

    // ///////////////////////
    // START OF GENERAL TAB //
    // ///////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "ExecProcessDialog.GeneralTab.TabItem" ) );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = margin;
    generalLayout.marginHeight = margin;
    wGeneralComp.setLayout( generalLayout );

    // filename field
    wlProcess = new Label( wGeneralComp, SWT.RIGHT );
    wlProcess.setText( BaseMessages.getString( PKG, "ExecProcessDialog.Process.Label" ) );
    props.setLook( wlProcess );
    fdlProcess = new FormData();
    fdlProcess.left = new FormAttachment( 0, 0 );
    fdlProcess.right = new FormAttachment( middle, -margin );
    fdlProcess.top = new FormAttachment( wStepname, margin );
    wlProcess.setLayoutData( fdlProcess );

    wProcess = new CCombo( wGeneralComp, SWT.BORDER | SWT.READ_ONLY );
    wProcess.setEditable( true );
    props.setLook( wProcess );
    wProcess.addModifyListener( lsMod );
    fdProcess = new FormData();
    fdProcess.left = new FormAttachment( middle, 0 );
    fdProcess.top = new FormAttachment( wStepname, margin );
    fdProcess.right = new FormAttachment( 100, -margin );
    wProcess.setLayoutData( fdProcess );
    wProcess.addFocusListener( new FocusListener() {
      @Override
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      @Override
      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        get();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Command Arguments are in separate fields
    wlArgumentsInFields = new Label( wGeneralComp, SWT.RIGHT );
    wlArgumentsInFields.setText( BaseMessages.getString( PKG, "ExecProcessDialog.ArgumentInFields.Label" ) );
    props.setLook( wlArgumentsInFields );
    fdlArgumentsInFields = new FormData();
    fdlArgumentsInFields.left = new FormAttachment( 0, 0 );
    fdlArgumentsInFields.top = new FormAttachment( wProcess, margin );
    fdlArgumentsInFields.right = new FormAttachment( middle, -margin );
    wlArgumentsInFields.setLayoutData( fdlArgumentsInFields );
    wArgumentsInFields = new Button( wGeneralComp, SWT.CHECK );
    wArgumentsInFields.setToolTipText( BaseMessages.getString( PKG, "ExecProcessDialog.ArgumentInFields.Tooltip" ) );
    props.setLook( wArgumentsInFields );
    fdArgumentsInFields = new FormData();
    fdArgumentsInFields.left = new FormAttachment( middle, 0 );
    fdArgumentsInFields.top = new FormAttachment( wProcess, margin );
    fdArgumentsInFields.right = new FormAttachment( 100, 0 );
    wArgumentsInFields.setLayoutData( fdArgumentsInFields );
    wArgumentsInFields.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        enableFields();
        input.setChanged();
      }
    } );

    // Fail when status is different than 0
    wlFailWhenNotSuccess = new Label( wGeneralComp, SWT.RIGHT );
    wlFailWhenNotSuccess.setText( BaseMessages.getString( PKG, "ExecProcessDialog.FailWhenNotSuccess.Label" ) );
    props.setLook( wlFailWhenNotSuccess );
    fdlFailWhenNotSuccess = new FormData();
    fdlFailWhenNotSuccess.left = new FormAttachment( 0, 0 );
    fdlFailWhenNotSuccess.top = new FormAttachment( wArgumentsInFields, margin );
    fdlFailWhenNotSuccess.right = new FormAttachment( middle, -margin );
    wlFailWhenNotSuccess.setLayoutData( fdlFailWhenNotSuccess );
    wFailWhenNotSuccess = new Button( wGeneralComp, SWT.CHECK );
    wFailWhenNotSuccess.setToolTipText( BaseMessages.getString(
      PKG, "ExecProcessDialog.FailWhenNotSuccess.Tooltip" ) );
    props.setLook( wFailWhenNotSuccess );
    fdFailWhenNotSuccess = new FormData();
    fdFailWhenNotSuccess.left = new FormAttachment( middle, 0 );
    fdFailWhenNotSuccess.top = new FormAttachment( wArgumentsInFields, margin );
    fdFailWhenNotSuccess.right = new FormAttachment( 100, 0 );
    wFailWhenNotSuccess.setLayoutData( fdFailWhenNotSuccess );
    wFailWhenNotSuccess.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // List of Argument Fields when ArgumentsInFields is enabled
    wlArgumentFields = new Label( wGeneralComp, SWT.LEFT );
    wlArgumentFields.setText( BaseMessages.getString( PKG, "ExecProcessDialog.ArgumentFields.Label" ) );
    props.setLook( wlArgumentFields );
    fdlArgumentFields = new FormData();
    fdlArgumentFields.left = new FormAttachment( 0, 0 );
    fdlArgumentFields.top = new FormAttachment( wFailWhenNotSuccess, margin );
    fdlArgumentFields.right = new FormAttachment( middle, -margin );
    wlArgumentFields.setLayoutData( fdlArgumentFields );
    ColumnInfo[] colinf = new ColumnInfo[1];
    colinf[0] = new ColumnInfo(
      BaseMessages.getString( PKG, "ExecProcessDialog.ArgumentField.Label" ),
      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    colinf[0].setToolTip( BaseMessages.getString( PKG, "ExecProcessDialog.ArgumentField.Tooltip" ) );
    wArgumentFields =
      new TableView(
        null, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, lsMod, props );
    fdArgumentFields = new FormData();
    fdArgumentFields.left = new FormAttachment( 0, 0 );
    fdArgumentFields.top = new FormAttachment( wlArgumentFields, margin );
    fdArgumentFields.right = new FormAttachment( 100, 0 );
    fdArgumentFields.bottom = new FormAttachment( 100, -margin );
    wArgumentFields.setLayoutData( fdArgumentFields );

    FormData fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );

    // /////////////////////
    // END OF GENERAL TAB //
    // /////////////////////

    // //////////////////////
    // START OF OUTPUT TAB //
    // //////////////////////

    wOutputTab = new CTabItem( wTabFolder, SWT.NONE );
    wOutputTab.setText( BaseMessages.getString( PKG, "ExecProcessDialog.Output.TabItem" ) );

    wOutputComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOutputComp );

    FormLayout fdOutputCompLayout = new FormLayout();
    fdOutputCompLayout.marginWidth = margin;
    fdOutputCompLayout.marginHeight = margin;
    wOutputComp.setLayout( fdOutputCompLayout );

    // Output Line Delimiter
    wOutputDelim = new LabelTextVar( transMeta, wOutputComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER,
      BaseMessages.getString( PKG, "ExecProcessDialog.OutputDelimiterField.Label" ),
      BaseMessages.getString( PKG, "ExecProcessDialog.OutputDelimiterField.Tooltip" ) );
    wOutputDelim.addModifyListener( lsMod );
    fdOutputDelim = new FormData();
    fdOutputDelim.left = new FormAttachment( 0, 0 );
    fdOutputDelim.top = new FormAttachment( 0, margin );
    fdOutputDelim.right = new FormAttachment( 100, 0 );
    wOutputDelim.setLayoutData( fdOutputDelim );

    // Result fieldname ...
    wResult = new LabelTextVar( transMeta, wOutputComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER,
      BaseMessages.getString( PKG, "ExecProcessDialog.ResultField.Label" ),
      BaseMessages.getString( PKG, "ExecProcessDialog.ResultField.Tooltip" ) );
    wResult.addModifyListener( lsMod );
    fdResult = new FormData();
    fdResult.left = new FormAttachment( 0, 0 );
    fdResult.top = new FormAttachment( wOutputDelim, margin );
    fdResult.right = new FormAttachment( 100, 0 );
    wResult.setLayoutData( fdResult );

    // Error fieldname ...
    wError = new LabelTextVar( transMeta, wOutputComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER,
      BaseMessages.getString( PKG, "ExecProcessDialog.ErrorField.Label" ),
      BaseMessages.getString( PKG, "ExecProcessDialog.ErrorField.Tooltip" ) );
    wError.addModifyListener( lsMod );
    fdError = new FormData();
    fdError.left = new FormAttachment( 0, 0 );
    fdError.top = new FormAttachment( wResult, margin );
    fdError.right = new FormAttachment( 100, 0 );
    wError.setLayoutData( fdError );

    // ExitValue fieldname ...
    wExitValue = new LabelTextVar( transMeta, wOutputComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER,
      BaseMessages.getString( PKG, "ExecProcessDialog.ExitValueField.Label" ),
      BaseMessages.getString( PKG, "ExecProcessDialog.ExitValueField.Tooltip" ) );
    wExitValue.addModifyListener( lsMod );
    fdExitValue = new FormData();
    fdExitValue.left = new FormAttachment( 0, 0 );
    fdExitValue.top = new FormAttachment( wError, margin );
    fdExitValue.right = new FormAttachment( 100, 0 );
    wExitValue.setLayoutData( fdExitValue );

    FormData fdOutputComp = new FormData();
    fdOutputComp.left = new FormAttachment( 0, 0 );
    fdOutputComp.top = new FormAttachment( 0, 0 );
    fdOutputComp.right = new FormAttachment( 100, 0 );
    fdOutputComp.bottom = new FormAttachment( 100, 0 );
    wOutputComp.setLayoutData( fdOutputComp );

    wOutputComp.layout();
    wOutputTab.setControl( wOutputComp );

    // ////////////////////
    // END OF OUTPUT TAB //
    // ////////////////////

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    wTabFolder.setSelection( 0 );

    // ////////////////////
    // END OF TAB FOLDER //
    // ////////////////////

    // THE BUTTONS
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

    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    RowMetaInterface r = null;
    try {
      r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        wArgumentFields.getColumns()[0].setComboValues( r.getFieldNames() );
      }
    } catch ( KettleStepException ignore ) {
      // Do nothing
    }


    getData();
    enableFields();
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
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "ExecProcessDialog.Log.GettingKeyInfo" ) );
    }

    if ( input.getProcessField() != null ) {
      wProcess.setText( input.getProcessField() );
    }
    if ( input.getResultFieldName() != null ) {
      wResult.setText( input.getResultFieldName() );
    }
    if ( input.getErrorFieldName() != null ) {
      wError.setText( input.getErrorFieldName() );
    }
    if ( input.getExitValueFieldName() != null ) {
      wExitValue.setText( input.getExitValueFieldName() );
    }
    if ( input.getOutputLineDelimiter() != null ) {
      wOutputDelim.setText( input.getOutputLineDelimiter() );
    }
    wFailWhenNotSuccess.setSelection( input.isFailWhenNotSuccess() );
    wArgumentsInFields.setSelection( input.isArgumentsInFields() );
    int nrRows = input.getArgumentFieldNames().length;
    if ( nrRows <= 0 ) {
      wArgumentFields.getTable().setItemCount( 1 );
    } else {
      wArgumentFields.getTable().setItemCount( nrRows );
      for ( int i = 0; i < input.getArgumentFieldNames().length; i++ ) {
        TableItem item = wArgumentFields.getTable().getItem( i );
        item.setText( 1, input.getArgumentFieldNames()[i] );
      }
    }
    wArgumentFields.setRowNums();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void enableFields() {
    wArgumentFields.setEnabled( wArgumentsInFields.getSelection() );
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
    input.setProcessField( wProcess.getText() );
    input.setResultFieldName( wResult.getText() );
    input.setErrorFieldName( wError.getText() );
    input.setExitValueFieldName( wExitValue.getText() );
    input.setFailWhenNotSuccess( wFailWhenNotSuccess.getSelection() );
    input.setOutputLineDelimiter( wOutputDelim.getText() );
    input.setArgumentsInFields( wArgumentsInFields.getSelection() );
    String[] argumentFields = null;
    if ( wArgumentsInFields.getSelection() ) {
      argumentFields = new String[wArgumentFields.nrNonEmpty()];
    } else {
      argumentFields = new String[0];
    }
    for ( int i = 0; i < argumentFields.length; i++ ) {
      argumentFields[i] = wArgumentFields.getNonEmpty( i ).getText( 1 );
    }
    input.setArgumentFieldNames( argumentFields );
    stepname = wStepname.getText(); // return value

    dispose();
  }

  private void get() {
    if ( !gotPreviousFields ) {
      try {
        String fieldvalue = wProcess.getText();
        wProcess.removeAll();
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          wProcess.setItems( r.getFieldNames() );
        }
        if ( fieldvalue != null ) {
          wProcess.setText( fieldvalue );
        }
        gotPreviousFields = true;
      } catch ( KettleException ke ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "ExecProcessDialog.FailedToGetFields.DialogTitle" ), BaseMessages
            .getString( PKG, "ExecProcessDialog.FailedToGetFields.DialogMessage" ), ke );
      }
    }
  }
}
