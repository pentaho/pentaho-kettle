package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;

/**
 * @apiNote This class creates login dialog for repo login and performs login operation.
 * @since Pentaho 9.4
 */
public class RepositoryConnectionDialog extends Dialog {

  private LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryConnectionDialog.class );

  private static final Class<?> PKG = RepositoryConnectionDialog.class;


  private Text txt_username;
  private Text txt_passwd;
  private Shell shell;
  private Display display;
  private PropsUI props;
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final String LOGIN_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Login.Title" );
  private static final String HELP_URL = Const.getDocUrl( "Products/Use_a_Pentaho_Repository_in_PDI" );


  public RepositoryConnectionDialog( Shell shell ) {
    super( shell, SWT.NONE );
    this.props = PropsUI.getInstance();
  }


  public boolean createDialog( String str_repoName ) {
    Shell parent = getParent();
    display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    props.setLook( shell );
    shell.setLayout( new FormLayout() );
    shell.setText( LOGIN_TITLE );
    shell.setImage( LOGO );

    try {

      Label lblConnectTo = new Label( shell, SWT.NONE );
      props.setLook( lblConnectTo );
      lblConnectTo.setLayoutData( new FormDataBuilder().top( 10, 0 ).left(1,0).result() );
      lblConnectTo.setText( BaseMessages.getString( PKG, "repositories.connectto.label" ) );

      Label lblRepoName = new Label( shell, SWT.NONE );
      props.setLook( lblRepoName );
      lblRepoName.setLayoutData( new FormDataBuilder().top( lblConnectTo ).left( 5, 0 ).result() );
      lblRepoName.setText( str_repoName );

      Label lblUserName = new Label( shell, SWT.NONE );
      props.setLook( lblUserName );
      lblUserName.setLayoutData( new FormDataBuilder().top( lblRepoName, 10 ).left( 5, 0 ).result() );
      lblUserName.setText( BaseMessages.getString( PKG, "repositories.username.label" ));

      txt_username = new Text( shell, SWT.BORDER );
      props.setLook( txt_username );
      txt_username.setLayoutData( new FormDataBuilder().top( lblUserName ).left( 5, 0 ).right( 95, 0 ).result() );

      Label lblPassword = new Label( shell, SWT.NONE );
      props.setLook( lblPassword );
      lblPassword.setLayoutData( new FormDataBuilder().top( txt_username, 10 ).left( 5, 0 ).result() );
      lblPassword.setText( BaseMessages.getString( PKG, "repositories.password.label" ));

      txt_passwd = new Text( shell, SWT.BORDER|SWT.PASSWORD );
      props.setLook( txt_passwd );
      txt_passwd.setLayoutData( new FormDataBuilder().top( lblPassword ).left( 5, 0 ).right( 95, 0 ).result() );

      Button loginbtn = new Button( shell, SWT.NONE );
      props.setLook( loginbtn );
      loginbtn.setLayoutData( new FormDataBuilder().top( txt_passwd ).left(5,0).result() );
      loginbtn.setText(BaseMessages.getString( PKG, "repositories.login.label" ));

      //******************** HELP btn **********************************
      Button btnHelp = new Button( shell, SWT.ICON_INFORMATION );
      props.setLook( btnHelp );
      btnHelp.setLayoutData( new FormDataBuilder().bottom().right().result() );
      btnHelp.setText( BaseMessages.getString( PKG, "repositories.help.label" ) );

      //******************* HELP btn call implementation ***************
      btnHelp.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event event ) {
          Program.launch( HELP_URL );
        }
      } );


      //********* login btn call implementation ***********
      loginbtn.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event event ) {

          String str_username = txt_username.getText();
          String str_passwd = txt_passwd.getText();

          if ( str_username.isEmpty() ) {
            messageBoxService( " User name cannot be blank!" );
          }
          else if ( str_passwd.isEmpty() ) {
            messageBoxService( " Password cannot be blank!" );
          } else {
            callLoginEndPoint( str_repoName, str_username, str_passwd );
          }
        }
      } );
      shell.pack();
      shell.setMinimumSize( 500, 370 );
      shell.open();
      while ( !shell.isDisposed() ) {
        if ( !display.readAndDispatch() ) {
          display.sleep();
        }
      }
      return true;
    } catch ( Exception e ) {
      log.logError( "Error occurred creating dialog",e );
      return false;
    }
  }

  private boolean callLoginEndPoint( String str_repoName, String str_username, String str_passwd ) {
    try {
      if ( RepositoryConnectController.getInstance().isRelogin() == true ) {
        RepositoryConnectController.getInstance().reconnectToRepository( str_repoName, str_username, str_passwd );
      }
      else {
        RepositoryConnectController.getInstance().connectToRepository( str_repoName, str_username, str_passwd );
      }
      log.logBasic( "Connection successful to repository "+str_repoName );
      shell.close();
      return true;
    } catch ( Exception e ) {
      log.logError( "Error connecting to repository ",e );
    return false;
    }
  }

  private void messageBoxService(String msgText){
    MessageBox messageBox = new MessageBox( getParent().getShell(), SWT.OK |
      SWT.ICON_ERROR | SWT.CANCEL );
    messageBox.setMessage( msgText );
    messageBox.open();
  }
}
