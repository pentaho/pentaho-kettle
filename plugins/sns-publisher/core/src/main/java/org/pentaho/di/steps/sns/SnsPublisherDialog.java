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

package org.pentaho.di.steps.sns;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI.
 *
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *
 * This class is the implementation of StepDialogInterface.
 * Classes implementing this interface need to:
 *
 * - build and open a SWT dialog displaying the step's settings (stored in the step's meta object)
 * - write back any changes the user makes to the step's meta object
 * - report whether the user changed any settings when confirming the dialog
 *
 */
public class SnsPublisherDialog extends BaseStepDialog implements StepDialogInterface {

  /**
   *  The PKG member is used when looking up internationalized strings.
   *  The properties file with localized keys is expected to reside in
   *  {the package of the class specified}/messages/messages_{locale}.properties
   */
  private static Class<?> PKG = SnsPublisherMeta.class; // for i18n purposes

  private CTabFolder wTabFolder;

  private CTabItem wGeneralTab, wAwsConfigurationTab;

  private Composite wGeneralComp, wAwsConfigurationComp;

  private Group wMessageGroup, wCredentialsGroup;

  private Label wlTopicARN, wlProtocol, wlMessageBody, wlSubject;
  private TextVar wTopicARN;
  private CCombo  wProtocol, wMessageBody, wSubject;
  private FormData fdlTopicARN, fdTopicARN, fdlProtocol, fdProtocol;
  private FormData fdlMessageBody, fdMessageBody, fdlSubject, fdSubject;

  private FormData fdGeneralComp, fdAwsConfigurationComp, fdMessageGroup, fdCredentialsGroup;

  private FormData fdlAccessKey, fdAccessKey, fdlSecretKey, fdSecretKey;
  private FormData fdTabFolder;
  private Label wlAccessKey, wlSecretKey;
  private TextVar wAccessKey, wSecretKey;


  // this is the object the stores the step's settings
  // the dialog reads the settings from it when opening
  // the dialog writes the settings to it when confirmed
  private SnsPublisherMeta input;

  // text field holding the name of the field to add to the row stream
  private LabelText wHelloFieldName;

  private boolean gotPreviousFields = false;

  /**
   * The constructor should simply invoke super() and save the incoming meta
   * object to a local variable, so it can conveniently read and write settings
   * from/to it.
   *
   * @param parent   the SWT shell to open the dialog in
   * @param in    the meta object holding the step's settings
   * @param transMeta  transformation description
   * @param sname    the step name
   */
  public SnsPublisherDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (SnsPublisherMeta) in;
  }

  /**
   * This method is called by Spoon when the user opens the settings dialog of the step.
   * It should open the dialog and return only once the dialog has been closed by the user.
   *
   * If the user confirms the dialog, the meta object (passed in the constructor) must
   * be updated to reflect the new step settings. The changed flag of the meta object must
   * reflect whether the step configuration was changed by the dialog.
   *
   * If the user cancels the dialog, the meta object must not be updated, and its changed flag
   * must remain unaltered.
   *
   * The open() method must return the name of the step after the user has confirmed the dialog,
   * or null if the user cancelled the dialog.
   */
  public String open() {
    // store some convenient SWT variables
    Shell parent = getParent();
    Display display = parent.getDisplay();

    // SWT code for preparing the dialog
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, input );

    // Save the value of the changed flag on the meta object. If the user cancels
    // the dialog, it will be restored to this saved value.
    // The "changed" variable is inherited from BaseStepDialog
    changed = input.hasChanged();

    // The ModifyListener used on all controls. It will update the meta object to
    // indicate that changes are being made.
    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };

    // ------------------------------------------------------- //
    // SWT code for building the actual settings dialog        //
    // ------------------------------------------------------- //
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "Sns.Shell.Title" ) );

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

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, PropsUI.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "SnsStep.Tab.General.Label" ) );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF Message GROUP
    // ////////////////////////

    //Group label
    wMessageGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wMessageGroup );
    wMessageGroup.setText( BaseMessages.getString( PKG, "SnsStep.Group.Message.Label" ) );

    FormLayout destinationGroupLayout = new FormLayout();
    destinationGroupLayout.marginWidth = 10;
    destinationGroupLayout.marginHeight = 10;
    wMessageGroup.setLayout( destinationGroupLayout );

    // Topic ARN Label
    wlTopicARN = new Label( wMessageGroup, SWT.RIGHT );
    wlTopicARN.setText( BaseMessages.getString( PKG, "SnsStep.Topic.ARN.Label" ) );
    props.setLook( wlTopicARN );
    fdlTopicARN = new FormData();
    fdlTopicARN.left = new FormAttachment( 0, 0 );
    fdlTopicARN.top = new FormAttachment( wStepname, margin );
    fdlTopicARN.right = new FormAttachment( middle, -margin );
    wlTopicARN.setLayoutData( fdlTopicARN );
    // Destination Data
    wTopicARN = new TextVar( transMeta, wMessageGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTopicARN );
    wTopicARN.addModifyListener( lsMod );
    fdTopicARN = new FormData();
    fdTopicARN.left = new FormAttachment( middle, 0 );
    fdTopicARN.top = new FormAttachment( wStepname, margin );
    fdTopicARN.right = new FormAttachment( 100, -margin );
    wTopicARN.setLayoutData( fdTopicARN );

    // Protocol Label
    wlProtocol = new Label( wMessageGroup, SWT.RIGHT );
    wlProtocol.setText( BaseMessages.getString( PKG, "SnsStep.Protocol.Label" ) );
    props.setLook( wlProtocol );
    fdlProtocol = new FormData();
    fdlProtocol.left = new FormAttachment( 0, 0 );
    fdlProtocol.right = new FormAttachment( middle, -margin );
    fdlProtocol.top = new FormAttachment( wTopicARN, margin );
    wlProtocol.setLayoutData( fdlProtocol );
    //Data
    wProtocol = new CCombo( wMessageGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wProtocol.add( input.PROTOCOL_EMAIL );
    wProtocol.select( 0 ); // +1: starts at -1
    props.setLook( wProtocol );
    fdProtocol = new FormData();
    fdProtocol.left = new FormAttachment( middle, 0 );
    fdProtocol.top = new FormAttachment( wTopicARN, margin );
    fdProtocol.right = new FormAttachment( 100, -margin );
    wProtocol.setLayoutData( fdProtocol );


    // Subject Label
    wlSubject = new Label( wMessageGroup, SWT.RIGHT );
    wlSubject.setText( BaseMessages.getString( PKG, "SnsStep.Subject.Label" ) );
    props.setLook( wlSubject );
    fdlSubject = new FormData();
    fdlSubject.left = new FormAttachment( 0, 0 );
    fdlSubject.right = new FormAttachment( middle, -margin );
    fdlSubject.top = new FormAttachment( wProtocol, margin );
    wlSubject.setLayoutData( fdlSubject );
    // Subject Data
    wSubject = new CCombo( wMessageGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wSubject );
    wSubject.setEditable( true );
    wSubject.addModifyListener( lsMod );
    fdSubject = new FormData();
    fdSubject.left = new FormAttachment( middle, 0 );
    fdSubject.top = new FormAttachment( wProtocol, margin );
    fdSubject.right = new FormAttachment( 100, -margin );
    wSubject.setLayoutData( fdSubject );
    wSubject.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }
      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Message Body Label
    wlMessageBody = new Label( wMessageGroup, SWT.RIGHT );
    wlMessageBody.setText( BaseMessages.getString( PKG, "SnsStep.Payload.Label" ) );
    props.setLook( wlMessageBody );
    fdlMessageBody = new FormData();
    fdlMessageBody.left = new FormAttachment( 0, 0 );
    fdlMessageBody.right = new FormAttachment( middle, -margin );
    fdlMessageBody.top = new FormAttachment( wSubject, margin );
    wlMessageBody.setLayoutData( fdlMessageBody );
    // Message Body Data
    wMessageBody = new CCombo( wMessageGroup, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wMessageBody );
    wMessageBody.setEditable( true );
    wMessageBody.addModifyListener( lsMod );
    fdMessageBody = new FormData();
    fdMessageBody.left = new FormAttachment( middle, 0 );
    fdMessageBody.top = new FormAttachment( wSubject, margin );
    fdMessageBody.right = new FormAttachment( 100, -margin );
    wMessageBody.setLayoutData( fdMessageBody );
    wMessageBody.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }
      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    fdMessageGroup = new FormData();
    fdMessageGroup.left = new FormAttachment( 0, margin );
    fdMessageGroup.top = new FormAttachment( wStepname, margin );
    fdMessageGroup.right = new FormAttachment( 100, -margin );
    wMessageGroup.setLayoutData( fdMessageGroup );
    // ///////////////////////////////////////////////////////////
    // / END OF MESSAGE GROUP
    // ///////////////////////////////////////////////////////////

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
    props.setLook( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////


    // ///////////////////////////////////////////////////////////
    // / Start OF AWS CONFIGURATION TAB
    // ///////////////////////////////////////////////////////////

    wAwsConfigurationTab = new CTabItem( wTabFolder, SWT.NONE );
    wAwsConfigurationTab.setText( BaseMessages.getString( PKG, "SnsStep.AWS.Configuration.Title" ) );

    wAwsConfigurationComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAwsConfigurationComp );

    FormLayout alayout = new FormLayout();
    alayout.marginWidth = 3;
    alayout.marginHeight = 3;
    wAwsConfigurationComp.setLayout( alayout );

    // /////////////////////////////
    // START AWS CREDENTIALS GROUP
    // /////////////////////////////

    wCredentialsGroup = new Group( wAwsConfigurationComp, SWT.SHADOW_ETCHED_IN );
    props.setLook( wCredentialsGroup );
    wCredentialsGroup.setText( BaseMessages.getString( PKG, "SnsStep.AWS.Configuration.Label" ) );
    FormLayout credentialsLayout = new FormLayout();
    credentialsLayout.marginWidth = 10;
    credentialsLayout.marginHeight = 10;
    wCredentialsGroup.setLayout( credentialsLayout );

    // Access Key
    wlAccessKey = new Label( wCredentialsGroup, SWT.RIGHT );
    wlAccessKey.setText( BaseMessages.getString( PKG, "SnsStep.AWS.AccessKey.Label" ) );
    props.setLook( wlAccessKey );
    fdlAccessKey = new FormData();
    fdlAccessKey.top = new FormAttachment( wStepname, margin );
    fdlAccessKey.left = new FormAttachment( 0, 0 );
    fdlAccessKey.right = new FormAttachment( middle, -margin );
    wlAccessKey.setLayoutData( fdlAccessKey );
    //Access Key Data
    wAccessKey = new TextVar( transMeta, wCredentialsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wAccessKey.addModifyListener( lsMod );
    wAccessKey.setToolTipText( BaseMessages.getString( PKG, "SnsStep.AWS.AccessKey.Tooltip" ) );
    props.setLook( wAccessKey );
    fdAccessKey = new FormData();
    fdAccessKey.top = new FormAttachment( wStepname, margin );
    fdAccessKey.left = new FormAttachment( middle, 0 );
    fdAccessKey.right = new FormAttachment( 100, 0 );
    wAccessKey.setLayoutData( fdAccessKey );

    // Secret Key
    wlSecretKey = new Label( wCredentialsGroup, SWT.RIGHT );
    wlSecretKey.setText( BaseMessages.getString( PKG, "SnsStep.AWS.SecretKey.Label" ) );
    props.setLook( wlSecretKey );
    fdlSecretKey = new FormData();
    fdlSecretKey.left = new FormAttachment( 0, 0 );
    fdlSecretKey.top = new FormAttachment( wAccessKey, margin );
    fdlSecretKey.right = new FormAttachment( middle, -margin );
    wlSecretKey.setLayoutData( fdlSecretKey );
    //Access Key Data
    wSecretKey = new TextVar( transMeta, wCredentialsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSecretKey.addModifyListener( lsMod );
    wSecretKey.setToolTipText( BaseMessages.getString( PKG, "SnsStep.SecretKey.Tooltip" ) );
    props.setLook( wSecretKey );
    fdSecretKey = new FormData();
    fdSecretKey.top = new FormAttachment( wAccessKey, margin );
    fdSecretKey.left = new FormAttachment( middle, 0 );
    fdSecretKey.right = new FormAttachment( 100, 0 );
    wSecretKey.setLayoutData( fdSecretKey );

    fdCredentialsGroup = new FormData();
    fdCredentialsGroup.left = new FormAttachment( 0, margin );
    fdCredentialsGroup.right = new FormAttachment( 100, -margin );
    fdCredentialsGroup.top = new FormAttachment( wStepname, margin );
    wCredentialsGroup.setLayoutData( fdCredentialsGroup );

    // ///////////////////////////
    // / END OF Credentials GROUP
    // ///////////////////////////

    fdAwsConfigurationComp = new FormData();
    fdAwsConfigurationComp.left = new FormAttachment( 0, 0 );
    fdAwsConfigurationComp.top = new FormAttachment( 0, 0 );
    fdAwsConfigurationComp.right = new FormAttachment( 100, 0 );
    fdAwsConfigurationComp.bottom = new FormAttachment( 100, 0 );
    wAwsConfigurationComp.setLayoutData( fdAwsConfigurationComp );

    wAwsConfigurationComp.layout();
    wAwsConfigurationTab.setControl( wAwsConfigurationComp );
    props.setLook( wAwsConfigurationComp );

    // ///////////////////////////////////////////////////////////
    // / END OF AWS CONFIGURATION TAB
    // ///////////////////////////////////////////////////////////


    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // OK and cancel buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners for cancel and OK
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

    // default listener (for hitting "enter")
    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wStepname.addSelectionListener( lsDef );
    //wHelloFieldName.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
/*    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );*/

    wTabFolder.setSelection( 0 );
    // Set/Restore the dialog size based on last position on screen
    // The setSize() method is inherited from BaseStepDialog
    setSize();
    getPreviousFields();

    // populate the dialog with the values from the meta object
    populateDialog();

    // restore the changed flag to original value, as the modify listeners fire during dialog population
    input.setChanged( changed );

    // open dialog and enter event loop
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    // at this point the dialog has closed, so either ok() or cancel() have been executed
    // The "stepname" variable is inherited from BaseStepDialog
    return stepname;
  }

  /**
   * This helper method puts the step configuration stored in the meta object
   * and puts it into the dialog controls.
   */
  private void populateDialog() {
    wStepname.selectAll();
    //wHelloFieldName.setText( input.getOutputField() );
  }

  /**
   * Called when the user cancels the dialog.
   */
  private void cancel() {
    // The "stepname" variable will be the return value for the open() method.
    // Setting to null to indicate that dialog was cancelled.
    stepname = null;
    // Restoring original "changed" flag on the met aobject
    input.setChanged( changed );
    // close the SWT dialog window
    dispose();
  }

  /**
   * Called when the user confirms the dialog
   */
  private void ok() {
    // The "stepname" variable will be the return value for the open() method.
    // Setting to step name from the dialog control
    stepname = wStepname.getText();
    // Setting the  settings to the meta object
    input.setTopicARN( wTopicARN.getText() );
    input.setProtocol( wProtocol.getText() );
    input.setSubject( wSubject.getText() );
    input.setMessage( wMessageBody.getText() );
    input.setAccessKey( wAccessKey.getText() );
    input.setSecretKey( wSecretKey.getText() );
    // close the SWT dialog window
    dispose();
  }

  private void getPreviousFields() {
    if ( !gotPreviousFields ) {
      try {
        String subject = wSubject.getText();
        String message = wMessageBody.getText();
        String topicArn = wTopicARN.getText();
        String accessKey = wAccessKey.getText();
        String secretKey = wSecretKey.getText();
        wSubject.removeAll();
        wMessageBody.removeAll();
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          String[] fields = r.getFieldNames();
          wSubject.setItems( fields );
          wMessageBody.setItems( fields );
        }
        if ( subject != null ) {
          wSubject.setText( subject );
        }
        if ( message != null ) {
          wMessageBody.setText( message );
        }
        if ( topicArn != null ) {
          wTopicARN.setText( topicArn );
        }
        if ( accessKey != null ) {
          wAccessKey.setText( accessKey );
        }
        if ( secretKey != null ) {
          wSecretKey.setText( secretKey );
        }
        if ( input.getSubject() != null ) {
          wSubject.setText( input.getSubject() );
        }
        if ( input.getMessage() != null ) {
          wMessageBody.setText( input.getMessage() );
        }
        if ( input.getTopicARN() != null ) {
          wTopicARN.setText( input.getTopicARN() );
        }
        if ( input.getAccessKey() != null ) {
          wAccessKey.setText( input.getAccessKey() );
        }
        if ( input.getSecretKey() != null ) {
          wSecretKey.setText( input.getSecretKey() );
        }
        gotPreviousFields = true;
      } catch ( KettleException ke ) {
        new ErrorDialog( shell, BaseMessages.getString( PKG, "LanguageTranslatorDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "LanguageTranslatorDialog.FailedToGetFields.DialogMessage" ), ke );
      }
    }
  }
}
