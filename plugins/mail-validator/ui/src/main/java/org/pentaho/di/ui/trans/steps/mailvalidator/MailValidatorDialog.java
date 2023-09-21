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

package org.pentaho.di.ui.trans.steps.mailvalidator;
import org.pentaho.di.core.annotations.PluginDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mailvalidator.MailValidatorMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

@PluginDialog( id = "MailValidator", image = "MAV.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail+Validator" )
public class MailValidatorDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MailValidatorMeta.class; // for i18n purposes, needed by Translator2!!

  private boolean gotPreviousFields = false;

  private Label wlemailFieldName;
  private CCombo wemailFieldName;
  private FormData fdlemailFieldName, fdemailFieldName;

  private Label wldefaultSMTPField;
  private CCombo wdefaultSMTPField;
  private FormData fdldefaultSMTPField, fddefaultSMTPField;

  private Label wlResult;
  private TextVar wResult;
  private FormData fdlResult, fdResult;

  private Label wleMailSender;
  private TextVar weMailSender;
  private FormData fdleMailSender, fdeMailSender;

  private Label wlTimeOut;
  private TextVar wTimeOut;
  private FormData fdlTimeOut, fdTimeOut;

  private Label wlDefaultSMTP;
  private TextVar wDefaultSMTP;
  private FormData fdlDefaultSMTP, fdDefaultSMTP;

  private Label wldynamicDefaultSMTP;
  private Button wdynamicDefaultSMTP;
  private FormData fdldynamicDefaultSMTP;
  private FormData fddynamicDefaultSMTP;

  private Group wResultGroup;
  private FormData fdResultGroup;

  private Group wSettingsGroup;
  private FormData fdSettingsGroup;

  private Label wlResultAsString;
  private FormData fdlResultAsString;
  private Button wResultAsString;
  private FormData fdResultAsString;

  private Label wlSMTPCheck;
  private FormData fdlSMTPCheck;
  private Button wSMTPCheck;
  private FormData fdSMTPCheck;

  private Label wlResultStringFalse;
  private FormData fdlResultStringFalse;
  private Label wlResultStringTrue;
  private FormData fdlResultStringTrue;
  private FormData fdResultStringTrue;
  private FormData fdResultStringFalse;
  private TextVar wResultStringFalse;
  private TextVar wResultStringTrue;

  private TextVar wErrorMsg;
  private Label wlErrorMsg;
  private FormData fdlErrorMsg;
  private FormData fdErrorMsg;

  private MailValidatorMeta input;

  public MailValidatorDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (MailValidatorMeta) in;
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
    shell.setText( BaseMessages.getString( PKG, "MailValidatorDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MailValidatorDialog.Stepname.Label" ) );
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

    // emailFieldName field
    wlemailFieldName = new Label( shell, SWT.RIGHT );
    wlemailFieldName.setText( BaseMessages.getString( PKG, "MailValidatorDialog.emailFieldName.Label" ) );
    props.setLook( wlemailFieldName );
    fdlemailFieldName = new FormData();
    fdlemailFieldName.left = new FormAttachment( 0, 0 );
    fdlemailFieldName.right = new FormAttachment( middle, -margin );
    fdlemailFieldName.top = new FormAttachment( wStepname, margin );
    wlemailFieldName.setLayoutData( fdlemailFieldName );

    wemailFieldName = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wemailFieldName );
    wemailFieldName.addModifyListener( lsMod );
    fdemailFieldName = new FormData();
    fdemailFieldName.left = new FormAttachment( middle, 0 );
    fdemailFieldName.top = new FormAttachment( wStepname, margin );
    fdemailFieldName.right = new FormAttachment( 100, -margin );
    wemailFieldName.setLayoutData( fdemailFieldName );
    wemailFieldName.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        get();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // ////////////////////////
    // START OF Settings GROUP
    //

    wSettingsGroup = new Group( shell, SWT.SHADOW_NONE );
    props.setLook( wSettingsGroup );
    wSettingsGroup.setText( BaseMessages.getString( PKG, "MailValidatorDialog.SettingsGroup.Label" ) );

    FormLayout groupSettings = new FormLayout();
    groupSettings.marginWidth = 10;
    groupSettings.marginHeight = 10;
    wSettingsGroup.setLayout( groupSettings );

    // perform SMTP check?
    wlSMTPCheck = new Label( wSettingsGroup, SWT.RIGHT );
    wlSMTPCheck.setText( BaseMessages.getString( PKG, "MailValidatorDialog.SMTPCheck.Label" ) );
    props.setLook( wlSMTPCheck );
    fdlSMTPCheck = new FormData();
    fdlSMTPCheck.left = new FormAttachment( 0, 0 );
    fdlSMTPCheck.top = new FormAttachment( wResult, margin );
    fdlSMTPCheck.right = new FormAttachment( middle, -2 * margin );
    wlSMTPCheck.setLayoutData( fdlSMTPCheck );
    wSMTPCheck = new Button( wSettingsGroup, SWT.CHECK );
    props.setLook( wSMTPCheck );
    wSMTPCheck.setToolTipText( BaseMessages.getString( PKG, "MailValidatorDialog.SMTPCheck.Tooltip" ) );
    fdSMTPCheck = new FormData();
    fdSMTPCheck.left = new FormAttachment( middle, -margin );
    fdSMTPCheck.top = new FormAttachment( wemailFieldName, margin );
    wSMTPCheck.setLayoutData( fdSMTPCheck );
    wSMTPCheck.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeSMTPCheck();
        input.setChanged();
      }
    } );

    // TimeOut fieldname ...
    wlTimeOut = new Label( wSettingsGroup, SWT.RIGHT );
    wlTimeOut.setText( BaseMessages.getString( PKG, "MailValidatorDialog.TimeOutField.Label" ) );
    props.setLook( wlTimeOut );
    fdlTimeOut = new FormData();
    fdlTimeOut.left = new FormAttachment( 0, 0 );
    fdlTimeOut.right = new FormAttachment( middle, -2 * margin );
    fdlTimeOut.top = new FormAttachment( wSMTPCheck, margin );
    wlTimeOut.setLayoutData( fdlTimeOut );

    wTimeOut = new TextVar( transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTimeOut.setToolTipText( BaseMessages.getString( PKG, "MailValidatorDialog.TimeOutField.Tooltip" ) );
    props.setLook( wTimeOut );
    wTimeOut.addModifyListener( lsMod );
    fdTimeOut = new FormData();
    fdTimeOut.left = new FormAttachment( middle, -margin );
    fdTimeOut.top = new FormAttachment( wSMTPCheck, margin );
    fdTimeOut.right = new FormAttachment( 100, 0 );
    wTimeOut.setLayoutData( fdTimeOut );

    // eMailSender fieldname ...
    wleMailSender = new Label( wSettingsGroup, SWT.RIGHT );
    wleMailSender.setText( BaseMessages.getString( PKG, "MailValidatorDialog.eMailSenderField.Label" ) );
    props.setLook( wleMailSender );
    fdleMailSender = new FormData();
    fdleMailSender.left = new FormAttachment( 0, 0 );
    fdleMailSender.right = new FormAttachment( middle, -2 * margin );
    fdleMailSender.top = new FormAttachment( wTimeOut, margin );
    wleMailSender.setLayoutData( fdleMailSender );

    weMailSender = new TextVar( transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    weMailSender.setToolTipText( BaseMessages.getString( PKG, "MailValidatorDialog.eMailSenderField.Tooltip" ) );
    props.setLook( weMailSender );
    weMailSender.addModifyListener( lsMod );
    fdeMailSender = new FormData();
    fdeMailSender.left = new FormAttachment( middle, -margin );
    fdeMailSender.top = new FormAttachment( wTimeOut, margin );
    fdeMailSender.right = new FormAttachment( 100, 0 );
    weMailSender.setLayoutData( fdeMailSender );

    // DefaultSMTP fieldname ...
    wlDefaultSMTP = new Label( wSettingsGroup, SWT.RIGHT );
    wlDefaultSMTP.setText( BaseMessages.getString( PKG, "MailValidatorDialog.DefaultSMTPField.Label" ) );
    props.setLook( wlDefaultSMTP );
    fdlDefaultSMTP = new FormData();
    fdlDefaultSMTP.left = new FormAttachment( 0, 0 );
    fdlDefaultSMTP.right = new FormAttachment( middle, -2 * margin );
    fdlDefaultSMTP.top = new FormAttachment( weMailSender, margin );
    wlDefaultSMTP.setLayoutData( fdlDefaultSMTP );

    wDefaultSMTP = new TextVar( transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wDefaultSMTP.setToolTipText( BaseMessages.getString( PKG, "MailValidatorDialog.DefaultSMTPField.Tooltip" ) );
    props.setLook( wDefaultSMTP );
    wDefaultSMTP.addModifyListener( lsMod );
    fdDefaultSMTP = new FormData();
    fdDefaultSMTP.left = new FormAttachment( middle, -margin );
    fdDefaultSMTP.top = new FormAttachment( weMailSender, margin );
    fdDefaultSMTP.right = new FormAttachment( 100, 0 );
    wDefaultSMTP.setLayoutData( fdDefaultSMTP );

    // dynamic SMTP server?
    wldynamicDefaultSMTP = new Label( wSettingsGroup, SWT.RIGHT );
    wldynamicDefaultSMTP.setText( BaseMessages.getString( PKG, "MailValidatorDialog.dynamicDefaultSMTP.Label" ) );
    props.setLook( wldynamicDefaultSMTP );
    fdldynamicDefaultSMTP = new FormData();
    fdldynamicDefaultSMTP.left = new FormAttachment( 0, 0 );
    fdldynamicDefaultSMTP.top = new FormAttachment( wDefaultSMTP, margin );
    fdldynamicDefaultSMTP.right = new FormAttachment( middle, -2 * margin );
    wldynamicDefaultSMTP.setLayoutData( fdldynamicDefaultSMTP );
    wdynamicDefaultSMTP = new Button( wSettingsGroup, SWT.CHECK );
    props.setLook( wdynamicDefaultSMTP );
    wdynamicDefaultSMTP.setToolTipText( BaseMessages.getString(
      PKG, "MailValidatorDialog.dynamicDefaultSMTP.Tooltip" ) );
    fddynamicDefaultSMTP = new FormData();
    fddynamicDefaultSMTP.left = new FormAttachment( middle, -margin );
    fddynamicDefaultSMTP.top = new FormAttachment( wDefaultSMTP, margin );
    wdynamicDefaultSMTP.setLayoutData( fddynamicDefaultSMTP );
    wdynamicDefaultSMTP.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activedynamicDefaultSMTP();
        input.setChanged();
      }
    } );

    // defaultSMTPField field
    wldefaultSMTPField = new Label( wSettingsGroup, SWT.RIGHT );
    wldefaultSMTPField.setText( BaseMessages.getString( PKG, "MailValidatorDialog.defaultSMTPField.Label" ) );
    props.setLook( wldefaultSMTPField );
    fdldefaultSMTPField = new FormData();
    fdldefaultSMTPField.left = new FormAttachment( 0, 0 );
    fdldefaultSMTPField.right = new FormAttachment( middle, -2 * margin );
    fdldefaultSMTPField.top = new FormAttachment( wdynamicDefaultSMTP, margin );
    wldefaultSMTPField.setLayoutData( fdldefaultSMTPField );

    wdefaultSMTPField = new CCombo( wSettingsGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wdefaultSMTPField );
    wdefaultSMTPField.addModifyListener( lsMod );
    fddefaultSMTPField = new FormData();
    fddefaultSMTPField.left = new FormAttachment( middle, -margin );
    fddefaultSMTPField.top = new FormAttachment( wdynamicDefaultSMTP, margin );
    fddefaultSMTPField.right = new FormAttachment( 100, -margin );
    wdefaultSMTPField.setLayoutData( fddefaultSMTPField );
    wdefaultSMTPField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        get();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    fdSettingsGroup = new FormData();
    fdSettingsGroup.left = new FormAttachment( 0, margin );
    fdSettingsGroup.top = new FormAttachment( wemailFieldName, margin );
    fdSettingsGroup.right = new FormAttachment( 100, -margin );
    wSettingsGroup.setLayoutData( fdSettingsGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Settings GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Result GROUP
    //

    wResultGroup = new Group( shell, SWT.SHADOW_NONE );
    props.setLook( wResultGroup );
    wResultGroup.setText( BaseMessages.getString( PKG, "MailValidatorDialog.ResultGroup.label" ) );

    FormLayout groupResult = new FormLayout();
    groupResult.marginWidth = 10;
    groupResult.marginHeight = 10;
    wResultGroup.setLayout( groupResult );

    // Result fieldname ...
    wlResult = new Label( wResultGroup, SWT.RIGHT );
    wlResult.setText( BaseMessages.getString( PKG, "MailValidatorDialog.ResultField.Label" ) );
    props.setLook( wlResult );
    fdlResult = new FormData();
    fdlResult.left = new FormAttachment( 0, 0 );
    fdlResult.right = new FormAttachment( middle, -2 * margin );
    fdlResult.top = new FormAttachment( wSettingsGroup, margin * 2 );
    wlResult.setLayoutData( fdlResult );

    wResult = new TextVar( transMeta, wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wResult.setToolTipText( BaseMessages.getString( PKG, "MailValidatorDialog.ResultField.Tooltip" ) );
    props.setLook( wResult );
    wResult.addModifyListener( lsMod );
    fdResult = new FormData();
    fdResult.left = new FormAttachment( middle, -margin );
    fdResult.top = new FormAttachment( wSettingsGroup, margin * 2 );
    fdResult.right = new FormAttachment( 100, 0 );
    wResult.setLayoutData( fdResult );

    // is Result as String
    wlResultAsString = new Label( wResultGroup, SWT.RIGHT );
    wlResultAsString.setText( BaseMessages.getString( PKG, "MailValidatorDialog.ResultAsString.Label" ) );
    props.setLook( wlResultAsString );
    fdlResultAsString = new FormData();
    fdlResultAsString.left = new FormAttachment( 0, 0 );
    fdlResultAsString.top = new FormAttachment( wResult, margin );
    fdlResultAsString.right = new FormAttachment( middle, -2 * margin );
    wlResultAsString.setLayoutData( fdlResultAsString );
    wResultAsString = new Button( wResultGroup, SWT.CHECK );
    props.setLook( wResultAsString );
    wResultAsString.setToolTipText( BaseMessages.getString( PKG, "MailValidatorDialog.ResultAsString.Tooltip" ) );
    fdResultAsString = new FormData();
    fdResultAsString.left = new FormAttachment( middle, -margin );
    fdResultAsString.top = new FormAttachment( wResult, margin );
    wResultAsString.setLayoutData( fdResultAsString );
    wResultAsString.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeResultAsString();
        input.setChanged();
      }
    } );

    // ResultStringTrue fieldname ...
    wlResultStringTrue = new Label( wResultGroup, SWT.RIGHT );
    wlResultStringTrue.setText( BaseMessages.getString( PKG, "MailValidatorDialog.ResultStringTrueField.Label" ) );
    props.setLook( wlResultStringTrue );
    fdlResultStringTrue = new FormData();
    fdlResultStringTrue.left = new FormAttachment( 0, 0 );
    fdlResultStringTrue.right = new FormAttachment( middle, -2 * margin );
    fdlResultStringTrue.top = new FormAttachment( wResultAsString, margin );
    wlResultStringTrue.setLayoutData( fdlResultStringTrue );

    wResultStringTrue = new TextVar( transMeta, wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wResultStringTrue.setToolTipText( BaseMessages.getString(
      PKG, "MailValidatorDialog.ResultStringTrueField.Tooltip" ) );
    props.setLook( wResultStringTrue );
    wResultStringTrue.addModifyListener( lsMod );
    fdResultStringTrue = new FormData();
    fdResultStringTrue.left = new FormAttachment( middle, -margin );
    fdResultStringTrue.top = new FormAttachment( wResultAsString, margin );
    fdResultStringTrue.right = new FormAttachment( 100, 0 );
    wResultStringTrue.setLayoutData( fdResultStringTrue );

    // ResultStringFalse fieldname ...
    wlResultStringFalse = new Label( wResultGroup, SWT.RIGHT );
    wlResultStringFalse
      .setText( BaseMessages.getString( PKG, "MailValidatorDialog.ResultStringFalseField.Label" ) );
    props.setLook( wlResultStringFalse );
    fdlResultStringFalse = new FormData();
    fdlResultStringFalse.left = new FormAttachment( 0, 0 );
    fdlResultStringFalse.right = new FormAttachment( middle, -2 * margin );
    fdlResultStringFalse.top = new FormAttachment( wResultStringTrue, margin );
    wlResultStringFalse.setLayoutData( fdlResultStringFalse );

    wResultStringFalse = new TextVar( transMeta, wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wResultStringFalse.setToolTipText( BaseMessages.getString(
      PKG, "MailValidatorDialog.ResultStringFalseField.Tooltip" ) );
    props.setLook( wResultStringFalse );
    wResultStringFalse.addModifyListener( lsMod );
    fdResultStringFalse = new FormData();
    fdResultStringFalse.left = new FormAttachment( middle, -margin );
    fdResultStringFalse.top = new FormAttachment( wResultStringTrue, margin );
    fdResultStringFalse.right = new FormAttachment( 100, 0 );
    wResultStringFalse.setLayoutData( fdResultStringFalse );

    // ErrorMsg fieldname ...
    wlErrorMsg = new Label( wResultGroup, SWT.RIGHT );
    wlErrorMsg.setText( BaseMessages.getString( PKG, "MailValidatorDialog.ErrorMsgField.Label" ) );
    props.setLook( wlErrorMsg );
    fdlErrorMsg = new FormData();
    fdlErrorMsg.left = new FormAttachment( 0, 0 );
    fdlErrorMsg.right = new FormAttachment( middle, -2 * margin );
    fdlErrorMsg.top = new FormAttachment( wResultStringFalse, margin );
    wlErrorMsg.setLayoutData( fdlErrorMsg );

    wErrorMsg = new TextVar( transMeta, wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wErrorMsg.setToolTipText( BaseMessages.getString( PKG, "MailValidatorDialog.ErrorMsgField.Tooltip" ) );
    props.setLook( wErrorMsg );
    wErrorMsg.addModifyListener( lsMod );
    fdErrorMsg = new FormData();
    fdErrorMsg.left = new FormAttachment( middle, -margin );
    fdErrorMsg.top = new FormAttachment( wResultStringFalse, margin );
    fdErrorMsg.right = new FormAttachment( 100, 0 );
    wErrorMsg.setLayoutData( fdErrorMsg );

    fdResultGroup = new FormData();
    fdResultGroup.left = new FormAttachment( 0, margin );
    fdResultGroup.top = new FormAttachment( wSettingsGroup, 2 * margin );
    fdResultGroup.right = new FormAttachment( 100, -margin );
    wResultGroup.setLayoutData( fdResultGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Result GROUP
    // ///////////////////////////////////////////////////////////

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wResultGroup );

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

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    activeSMTPCheck();
    activeResultAsString();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void activedynamicDefaultSMTP() {
    wldefaultSMTPField.setEnabled( wSMTPCheck.getSelection() && wdynamicDefaultSMTP.getSelection() );
    wdefaultSMTPField.setEnabled( wSMTPCheck.getSelection() && wdynamicDefaultSMTP.getSelection() );
  }

  private void activeSMTPCheck() {
    wlTimeOut.setEnabled( wSMTPCheck.getSelection() );
    wTimeOut.setEnabled( wSMTPCheck.getSelection() );
    wlDefaultSMTP.setEnabled( wSMTPCheck.getSelection() );
    wDefaultSMTP.setEnabled( wSMTPCheck.getSelection() );
    wleMailSender.setEnabled( wSMTPCheck.getSelection() );
    weMailSender.setEnabled( wSMTPCheck.getSelection() );
    wdynamicDefaultSMTP.setEnabled( wSMTPCheck.getSelection() );
    wldynamicDefaultSMTP.setEnabled( wSMTPCheck.getSelection() );
    activedynamicDefaultSMTP();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getEmailField() != null ) {
      wemailFieldName.setText( input.getEmailField() );
    }
    if ( input.getResultFieldName() != null ) {
      wResult.setText( input.getResultFieldName() );
    }

    wResultAsString.setSelection( input.isResultAsString() );
    if ( input.getEMailValideMsg() != null ) {
      wResultStringTrue.setText( input.getEMailValideMsg() );
    }
    if ( input.getEMailNotValideMsg() != null ) {
      wResultStringFalse.setText( input.getEMailNotValideMsg() );
    }
    if ( input.getErrorsField() != null ) {
      wErrorMsg.setText( input.getErrorsField() );
    }
    int timeout = Const.toInt( input.getTimeOut(), 0 );
    wTimeOut.setText( String.valueOf( timeout ) );
    wSMTPCheck.setSelection( input.isSMTPCheck() );
    if ( input.getDefaultSMTP() != null ) {
      wDefaultSMTP.setText( input.getDefaultSMTP() );
    }
    if ( input.geteMailSender() != null ) {
      weMailSender.setText( input.geteMailSender() );
    }
    wdynamicDefaultSMTP.setSelection( input.isdynamicDefaultSMTP() );
    if ( input.getDefaultSMTPField() != null ) {
      wdefaultSMTPField.setText( input.getDefaultSMTPField() );
    }

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void activeResultAsString() {
    wlResultStringFalse.setEnabled( wResultAsString.getSelection() );
    wResultStringFalse.setEnabled( wResultAsString.getSelection() );
    wlResultStringTrue.setEnabled( wResultAsString.getSelection() );
    wResultStringTrue.setEnabled( wResultAsString.getSelection() );
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
    input.setEmailfield( wemailFieldName.getText() );
    input.setResultFieldName( wResult.getText() );
    stepname = wStepname.getText(); // return value

    input.setResultAsString( wResultAsString.getSelection() );
    input.setEmailValideMsg( wResultStringTrue.getText() );
    input.setEmailNotValideMsg( wResultStringFalse.getText() );
    input.setErrorsField( wErrorMsg.getText() );
    input.setTimeOut( wTimeOut.getText() );
    input.setDefaultSMTP( wDefaultSMTP.getText() );
    input.seteMailSender( weMailSender.getText() );
    input.setSMTPCheck( wSMTPCheck.getSelection() );
    input.setdynamicDefaultSMTP( wdynamicDefaultSMTP.getSelection() );
    input.setDefaultSMTPField( wdefaultSMTPField.getText() );

    dispose();
  }

  private void get() {
    if ( !gotPreviousFields ) {
      try {
        String emailField = null;
        String smtpdefaultField = null;
        if ( wemailFieldName.getText() != null ) {
          emailField = wemailFieldName.getText();
        }
        if ( wdefaultSMTPField.getText() != null ) {
          smtpdefaultField = wdefaultSMTPField.getText();
        }

        wemailFieldName.removeAll();
        wdefaultSMTPField.removeAll();

        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          wemailFieldName.setItems( r.getFieldNames() );
          wdefaultSMTPField.setItems( r.getFieldNames() );
        }
        if ( emailField != null ) {
          wemailFieldName.setText( emailField );
        }
        if ( smtpdefaultField != null ) {
          wdefaultSMTPField.setText( smtpdefaultField );
        }
        gotPreviousFields = true;
      } catch ( KettleException ke ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "MailValidatorDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "MailValidatorDialog.FailedToGetFields.DialogMessage" ), ke );
      }
    }
  }
}
