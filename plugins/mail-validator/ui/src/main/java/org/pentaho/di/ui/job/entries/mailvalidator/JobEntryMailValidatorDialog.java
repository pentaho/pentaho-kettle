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

package org.pentaho.di.ui.job.entries.mailvalidator;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.mailvalidator.JobEntryMailValidator;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.annotations.PluginDialog;

/**
 * This dialog allows you to edit a JobEntryMailValidator
 *
 * @author Samatar
 * @since 23-06-2008
 */
@PluginDialog( id = "MAIL_VALIDATOR,JobCategory.Category.Mail_VALIDATOR", image = "MAV.svg", pluginType = PluginDialog.PluginType.JOBENTRY,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail+Validator" )
public class JobEntryMailValidatorDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryMailValidator.class; // for i18n purposes, needed by Translator2!!

  private Label wlName;

  private Text wName;

  private FormData fdlName, fdName;

  private Button wOK, wCancel;

  private Listener lsOK, lsCancel;

  private JobEntryMailValidator jobEntry;

  private Shell shell;

  private SelectionAdapter lsDef;

  private boolean changed;

  private LabelTextVar wMailAddress;

  private FormData fdMailAddress;

  private Display display;

  private Group wSettingsGroup;
  private FormData fdSettingsGroup;

  private Label wleMailSender;
  private TextVar weMailSender;
  private FormData fdleMailSender, fdeMailSender;

  private Label wlTimeOut;
  private TextVar wTimeOut;
  private FormData fdlTimeOut, fdTimeOut;

  private Label wlDefaultSMTP;
  private TextVar wDefaultSMTP;
  private FormData fdlDefaultSMTP, fdDefaultSMTP;

  private Label wlSMTPCheck;
  private FormData fdlSMTPCheck;
  private Button wSMTPCheck;
  private FormData fdSMTPCheck;

  public JobEntryMailValidatorDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryMailValidator) jobEntryInt;

    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.Name.Default" ) );
    }
  }

  public JobEntryInterface open() {
    Shell parent = getParent();
    display = parent.getDisplay();

    shell = new Shell( parent, props.getJobsDialogStyle() );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        jobEntry.setChanged();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Filename line
    wlName = new Label( shell, SWT.RIGHT );
    wlName.setText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.Label" ) );
    props.setLook( wlName );
    fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, 0 );
    fdlName.top = new FormAttachment( 0, margin );
    wlName.setLayoutData( fdlName );
    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    fdName = new FormData();
    fdName.left = new FormAttachment( middle, margin );
    fdName.top = new FormAttachment( 0, margin );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    // eMail address
    wMailAddress = new LabelTextVar( jobMeta, shell,
      BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.MailAddress.Label" ),
      BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.MailAddress.Tooltip" ) );
    wMailAddress.addModifyListener( lsMod );
    fdMailAddress = new FormData();
    fdMailAddress.left = new FormAttachment( 0, 0 );
    fdMailAddress.top = new FormAttachment( wName, margin );
    fdMailAddress.right = new FormAttachment( 100, 0 );
    wMailAddress.setLayoutData( fdMailAddress );

    // ////////////////////////
    // START OF Settings GROUP
    // ////////////////////////

    wSettingsGroup = new Group( shell, SWT.SHADOW_NONE );
    props.setLook( wSettingsGroup );
    wSettingsGroup.setText( BaseMessages
      .getString( PKG, "JobEntryMailValidatorDialog.Group.SettingsAddress.Label" ) );

    FormLayout SettingsgroupLayout = new FormLayout();
    SettingsgroupLayout.marginWidth = 10;
    SettingsgroupLayout.marginHeight = 10;
    wSettingsGroup.setLayout( SettingsgroupLayout );

    // perform SMTP check?
    wlSMTPCheck = new Label( wSettingsGroup, SWT.RIGHT );
    wlSMTPCheck.setText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.SMTPCheck.Label" ) );
    props.setLook( wlSMTPCheck );
    fdlSMTPCheck = new FormData();
    fdlSMTPCheck.left = new FormAttachment( 0, 0 );
    fdlSMTPCheck.top = new FormAttachment( wMailAddress, margin );
    fdlSMTPCheck.right = new FormAttachment( middle, -2 * margin );
    wlSMTPCheck.setLayoutData( fdlSMTPCheck );
    wSMTPCheck = new Button( wSettingsGroup, SWT.CHECK );
    props.setLook( wSMTPCheck );
    wSMTPCheck.setToolTipText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.SMTPCheck.Tooltip" ) );
    fdSMTPCheck = new FormData();
    fdSMTPCheck.left = new FormAttachment( middle, -margin );
    fdSMTPCheck.top = new FormAttachment( wMailAddress, margin );
    wSMTPCheck.setLayoutData( fdSMTPCheck );
    wSMTPCheck.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeSMTPCheck();
      }
    } );

    // TimeOut fieldname ...
    wlTimeOut = new Label( wSettingsGroup, SWT.RIGHT );
    wlTimeOut.setText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.TimeOutField.Label" ) );
    props.setLook( wlTimeOut );
    fdlTimeOut = new FormData();
    fdlTimeOut.left = new FormAttachment( 0, 0 );
    fdlTimeOut.right = new FormAttachment( middle, -2 * margin );
    fdlTimeOut.top = new FormAttachment( wSMTPCheck, margin );
    wlTimeOut.setLayoutData( fdlTimeOut );

    wTimeOut = new TextVar( jobMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wTimeOut.setToolTipText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.TimeOutField.Tooltip" ) );
    props.setLook( wTimeOut );
    wTimeOut.addModifyListener( lsMod );
    fdTimeOut = new FormData();
    fdTimeOut.left = new FormAttachment( middle, -margin );
    fdTimeOut.top = new FormAttachment( wSMTPCheck, margin );
    fdTimeOut.right = new FormAttachment( 100, 0 );
    wTimeOut.setLayoutData( fdTimeOut );

    // eMailSender fieldname ...
    wleMailSender = new Label( wSettingsGroup, SWT.RIGHT );
    wleMailSender.setText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.eMailSenderField.Label" ) );
    props.setLook( wleMailSender );
    fdleMailSender = new FormData();
    fdleMailSender.left = new FormAttachment( 0, 0 );
    fdleMailSender.right = new FormAttachment( middle, -2 * margin );
    fdleMailSender.top = new FormAttachment( wTimeOut, margin );
    wleMailSender.setLayoutData( fdleMailSender );

    weMailSender = new TextVar( jobMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    weMailSender.setToolTipText( BaseMessages.getString(
      PKG, "JobEntryMailValidatorDialog.eMailSenderField.Tooltip" ) );
    props.setLook( weMailSender );
    weMailSender.addModifyListener( lsMod );
    fdeMailSender = new FormData();
    fdeMailSender.left = new FormAttachment( middle, -margin );
    fdeMailSender.top = new FormAttachment( wTimeOut, margin );
    fdeMailSender.right = new FormAttachment( 100, 0 );
    weMailSender.setLayoutData( fdeMailSender );

    // DefaultSMTP fieldname ...
    wlDefaultSMTP = new Label( wSettingsGroup, SWT.RIGHT );
    wlDefaultSMTP.setText( BaseMessages.getString( PKG, "JobEntryMailValidatorDialog.DefaultSMTPField.Label" ) );
    props.setLook( wlDefaultSMTP );
    fdlDefaultSMTP = new FormData();
    fdlDefaultSMTP.left = new FormAttachment( 0, 0 );
    fdlDefaultSMTP.right = new FormAttachment( middle, -2 * margin );
    fdlDefaultSMTP.top = new FormAttachment( weMailSender, margin );
    wlDefaultSMTP.setLayoutData( fdlDefaultSMTP );

    wDefaultSMTP = new TextVar( jobMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wDefaultSMTP.setToolTipText( BaseMessages.getString(
      PKG, "JobEntryMailValidatorDialog.DefaultSMTPField.Tooltip" ) );
    props.setLook( wDefaultSMTP );
    wDefaultSMTP.addModifyListener( lsMod );
    fdDefaultSMTP = new FormData();
    fdDefaultSMTP.left = new FormAttachment( middle, -margin );
    fdDefaultSMTP.top = new FormAttachment( weMailSender, margin );
    fdDefaultSMTP.right = new FormAttachment( 100, 0 );
    wDefaultSMTP.setLayoutData( fdDefaultSMTP );

    fdSettingsGroup = new FormData();
    fdSettingsGroup.left = new FormAttachment( 0, margin );
    fdSettingsGroup.top = new FormAttachment( wMailAddress, margin );
    fdSettingsGroup.right = new FormAttachment( 100, -margin );
    wSettingsGroup.setLayoutData( fdSettingsGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Settings GROUP
    // ///////////////////////////////////////////////////////////

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    // at the bottom
    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wSettingsGroup );

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

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wName.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    activeSMTPCheck();
    BaseStepDialog.setSize( shell );

    shell.open();
    props.setDialogSize( shell, "JobSuccessDialogSize" );
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  public void dispose() {
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  private void activeSMTPCheck() {
    wlTimeOut.setEnabled( wSMTPCheck.getSelection() );
    wTimeOut.setEnabled( wSMTPCheck.getSelection() );
    wlDefaultSMTP.setEnabled( wSMTPCheck.getSelection() );
    wDefaultSMTP.setEnabled( wSMTPCheck.getSelection() );
    wleMailSender.setEnabled( wSMTPCheck.getSelection() );
    weMailSender.setEnabled( wSMTPCheck.getSelection() );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wName.setText( Const.NVL( jobEntry.getName(), "" ) );
    wMailAddress.setText( Const.NVL( jobEntry.getEmailAddress(), "" ) );
    wTimeOut.setText( Const.NVL( jobEntry.getTimeOut(), "0" ) );
    wSMTPCheck.setSelection( jobEntry.isSMTPCheck() );
    wDefaultSMTP.setText( Const.NVL( jobEntry.getDefaultSMTP(), "" ) );
    weMailSender.setText( Const.NVL( jobEntry.geteMailSender(), "" ) );

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    jobEntry.setChanged( changed );
    jobEntry = null;
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wName.getText() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
      mb.open();
      return;
    }
    jobEntry.setName( wName.getText() );
    jobEntry.setEmailAddress( wMailAddress.getText() );
    jobEntry.setTimeOut( wTimeOut.getText() );
    jobEntry.setDefaultSMTP( wDefaultSMTP.getText() );
    jobEntry.seteMailSender( weMailSender.getText() );
    jobEntry.setSMTPCheck( wSMTPCheck.getSelection() );
    dispose();
  }
}
