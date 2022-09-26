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
 ***************************************************************************** */

package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;

/**
 * @author amit kumar
 * @apiNote This class creates login dialog for repo login and performs login operation.
 * @since Pentaho 9.4
 */
public class RepositoryConnectionDialog extends Dialog {

  private LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryConnectionDialog.class );

  private static final Class<?> PKG = RepositoryConnectionDialog.class;

  private Shell shell;

  private PropsUI props;
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final String LOGIN_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Login.Title" );
  private static final String HELP_URL =
    Const.getDocUrl( BaseMessages.getString( PKG, "repositories.repohelpurl.label" ) );
  String flagMessageBlank = "                                                    ";


  public RepositoryConnectionDialog( Shell shell ) {
    super( shell, SWT.NONE );
    this.props = PropsUI.getInstance();
  }

  public boolean createDialog( String strRepoName ) {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
    props.setLook( shell );
    shell.setLayout( new FormLayout() );
    shell.setText( LOGIN_TITLE );
    shell.setImage( LOGO );

    try {
      Label lblConnectTo = new Label( shell, SWT.NONE );
      props.setLook( lblConnectTo );
      lblConnectTo.setLayoutData( new FormDataBuilder().top( 10, 0 ).left( 1, 0 ).result() );
      lblConnectTo.setText( BaseMessages.getString( PKG, "repositories.connectto.label" ) );

      Label lblRepoName = new Label( shell, SWT.NONE );
      props.setLook( lblRepoName );
      lblRepoName.setLayoutData( new FormDataBuilder().top( lblConnectTo ).left( 5, 0 ).result() );
      lblRepoName.setText( strRepoName );

      Label lblUserName = new Label( shell, SWT.NONE );
      props.setLook( lblUserName );
      lblUserName.setLayoutData( new FormDataBuilder().top( lblRepoName, 10 ).left( 5, 0 ).result() );
      lblUserName.setText( BaseMessages.getString( PKG, "repositories.username.label" ) );

      Text txtUserName = new Text( shell, SWT.BORDER );
      props.setLook( txtUserName );
      txtUserName.setLayoutData( new FormDataBuilder().top( lblUserName ).left( 5, 0 ).right( 95, 0 ).result() );

      Label lblPassword = new Label( shell, SWT.NONE );
      props.setLook( lblPassword );
      lblPassword.setLayoutData( new FormDataBuilder().top( txtUserName, 10 ).left( 5, 0 ).result() );
      lblPassword.setText( BaseMessages.getString( PKG, "repositories.password.label" ) );

      Text txtPasswd = new Text( shell, SWT.BORDER | SWT.PASSWORD );
      props.setLook( txtPasswd );
      txtPasswd.setLayoutData( new FormDataBuilder().top( lblPassword ).left( 5, 0 ).right( 95, 0 ).result() );

      Label lblFlag = new Label( shell, SWT.NONE );
      props.setLook( lblFlag );
      lblFlag.setLayoutData( new FormDataBuilder().top( txtPasswd, 10 ).left( 5, 0 ).result() );

      lblFlag.setText( flagMessageBlank );

      Button loginBtn = new Button( shell, SWT.NONE );
      props.setLook( loginBtn );
      loginBtn.setLayoutData( new FormDataBuilder().top( lblFlag ).left( 5, 0 ).result() );
      loginBtn.setText( BaseMessages.getString( PKG, "repositories.login.label" ) );

      //******************** HELP btn **********************************
      Button btnHelp = new Button( shell, SWT.ICON_INFORMATION );
      props.setLook( btnHelp );
      btnHelp.setLayoutData( new FormDataBuilder().bottom().right().result() );
      btnHelp.setText( BaseMessages.getString( PKG, "repositories.help.label" ) );

      //******************* HELP btn call implementation starts ***************
      btnHelp.addListener( SWT.Selection, event ->
        Program.launch( HELP_URL ) );
      //********* txt password enter login call implementation starts ***********

      txtPasswd.addKeyListener( new KeyAdapter() {
        @Override
        public void keyPressed( KeyEvent e ) {
          if ( e.keyCode == SWT.CR ) {
            {
              String strUserName = txtUserName.getText();
              String strPasswd = txtPasswd.getText();

              if ( Utils.isEmpty( strUserName ) ) {
                messageBoxService( " User name cannot be blank!" );
              } else if ( Utils.isEmpty( strPasswd ) ) {
                messageBoxService( " Password cannot be blank!" );
              } else {
                //set indicator flag
                lblFlag.setForeground( new Color( lblFlag.getDisplay(), 0, 153, 76 ) );
                lblFlag.setText( "Please wait while connecting . . ." );
                loginBtn.setEnabled( false );

                BusyIndicator.showWhile( loginBtn.getDisplay(), () ->
                  callLoginEndPoint( strRepoName, strUserName, strPasswd )
                );

                //set indicator flag
                lblFlag.setText( flagMessageBlank );
                loginBtn.setEnabled( true );

                //********* txt password enter login call implementation starts ***********
              }
            }
          }
        }
      } );


      //login btn call implementation starts ********************
      loginBtn.addListener( SWT.Selection, event ->
      {
        String strUserName = txtUserName.getText();
        String strPasswd = txtPasswd.getText();

        //login execution code starts here ****************************************
        if ( Utils.isEmpty( strUserName ) ) {
          messageBoxService( " User name cannot be blank!" );
        } else if ( Utils.isEmpty( strPasswd ) ) {
          messageBoxService( " Password cannot be blank!" );
        } else {
          //set indicator flag
          lblFlag.setForeground( new Color( lblFlag.getDisplay(), 0, 153, 76 ) );
          lblFlag.setText( "Please wait while connecting . . ." );
          loginBtn.setEnabled( false );

          BusyIndicator.showWhile( loginBtn.getDisplay(),
            () -> callLoginEndPoint( strRepoName, strUserName, strPasswd ) );

          //set indicator flag
          lblFlag.setText( flagMessageBlank );
          loginBtn.setEnabled( true );
          //********** login execution code ends here****************************************
        }
      } );

      shell.pack();
      shell.setMinimumSize( 500, 450 );

      //opening shell in center
      int width = display.getClientArea().width;
      int height = display.getClientArea().height;
      shell.setLocation( ( ( width - shell.getSize().x ) / 2 ) + display.getClientArea().x,
        ( ( height - shell.getSize().y ) / 2 ) + display.getClientArea().y );

      shell.open();
      while ( !shell.isDisposed() ) {
        if ( !display.readAndDispatch() ) {
          display.sleep();
        }
      }
      return true;
    } catch ( Exception e ) {
      log.logError( "Error occurred creating dialog", e );
      return false;
    }
  }

  public boolean callLoginEndPoint( String strRepoName, String strUserName, String strPasswd ) {
    try {
      if ( RepositoryConnectController.getInstance().isRelogin() ) {
        RepositoryConnectController.getInstance().reconnectToRepository( strRepoName, strUserName, strPasswd );
      } else {
        RepositoryConnectController.getInstance().connectToRepository( strRepoName, strUserName, strPasswd );
      }
      log.logBasic( "Connection successful to repository " + strRepoName );
      shell.close();
      return true;
    } catch ( Exception e ) {
      log.logError( "Error connecting to repository ", e );
      messageBoxService( "Repository connection unsuccessful. Please check logs." );
      return false;
    }
  }

  private void messageBoxService( String msgText ) {
    MessageBox messageBox = new MessageBox( getParent().getShell(), SWT.OK |
      SWT.ICON_ERROR );
    messageBox.setMessage( msgText );
    messageBox.open();
  }
}
