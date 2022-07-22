package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.function.Supplier;


public class RepositoryConnectionSWT extends Dialog {

  private LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryConnectionSWT.class );

  private static Class<?> PKG = RepositoryConnectionSWT.class;


  private Text txt_username;
  private Text txt_passwd;
  private String str_repoName;
  private String str_username;
  private String str_passwd;
  private Shell shell;
  private Display display;
  private PropsUI props;
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final String LOGIN_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Login.Title" );
  private Supplier<Spoon> spoonSupplier;
  public static final String HELP_URL =
    Const.getDocUrl( BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Help" ) );
  //public static final String HELP_URL ="Products/Use_a_Pentaho_Repository_in_PDI";

  public RepositoryConnectionSWT( Shell shell ) {
    super( shell, SWT.NONE );
    this.props = PropsUI.getInstance();
  }


  public void createDialog( String str_repoName ) {
    this.str_repoName = str_repoName;
    Shell parent = getParent();
    display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    props.setLook( shell );
    shell.setLayout( new FormLayout() );
    shell.setText( "Repository Connection" );
    shell.setImage( LOGO );

    try {

      Label lblConnectTo = new Label( shell, SWT.NONE );
      props.setLook( lblConnectTo );
      lblConnectTo.setLayoutData( new FormDataBuilder().top( 10, 0 ).left().result() );
      lblConnectTo.setText( "Connect to :" );

      Label lblRepoName = new Label( shell, SWT.NONE );
      props.setLook( lblRepoName );
      lblRepoName.setLayoutData( new FormDataBuilder().top( lblConnectTo ).left( 5, 0 ).result() );
      lblRepoName.setText( str_repoName );

      Label lblUserName = new Label( shell, SWT.NONE );
      props.setLook( lblUserName );
      lblUserName.setLayoutData( new FormDataBuilder().top( lblRepoName, 10 ).left( 5, 0 ).result() );
      lblUserName.setText( "User name:" );

      txt_username = new Text( shell, SWT.BORDER );
      props.setLook( txt_username );
      txt_username.setLayoutData( new FormDataBuilder().top( lblUserName ).left( 5, 0 ).right( 95, 0 ).result() );

      Label lblPassword = new Label( shell, SWT.NONE );
      props.setLook( lblPassword );
      lblPassword.setLayoutData( new FormDataBuilder().top( txt_username, 10 ).left( 5, 0 ).result() );
      lblPassword.setText( "Password:" );

      txt_passwd = new Text( shell, SWT.PASSWORD );
      props.setLook( txt_passwd );
      txt_passwd.setLayoutData( new FormDataBuilder().top( lblPassword ).left( 5, 0 ).right( 95, 0 ).result() );

      Button loginbtn = new Button( shell, SWT.NONE );
      props.setLook( loginbtn );
      loginbtn.setLayoutData( new FormDataBuilder().top( txt_passwd ).result() );
      loginbtn.setText( "login" );

      //******************** HELP btn **********************************
      Button btnHelp = new Button( shell, SWT.ICON_INFORMATION );
      props.setLook( btnHelp );
      btnHelp.setLayoutData( new FormDataBuilder().bottom().result() );
      btnHelp.setText( "help" );
      btnHelp.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event event ) {
          Program.launch( HELP_URL );
        }
      } );


      //********* login btn call implementation ***********
      loginbtn.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event event ) {

          str_username = txt_username.getText();
          str_passwd = txt_passwd.getText();

          if ( str_username.isEmpty() ) {
            MessageBox messageBox = new MessageBox( getParent().getShell(), SWT.OK |
              SWT.ICON_ERROR | SWT.CANCEL );
            messageBox.setMessage( "user name can not be blank" );
            messageBox.open();
          }
          if ( str_passwd.isEmpty() ) {
            MessageBox messageBox = new MessageBox( getParent().getShell(), SWT.OK |
              SWT.ICON_ERROR | SWT.CANCEL );
            messageBox.setMessage( "password can not be blank" );
            messageBox.open();
          } else {
            callLoginEndPoint( str_repoName, str_username, str_passwd );
          }
        }
      } );
      shell.pack();
      shell.setMinimumSize( 500, 300 );
      shell.open();
      while ( !shell.isDisposed() ) {
        if ( !display.readAndDispatch() ) {
          display.sleep();
        }
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  void callLoginEndPoint( String str_repoName, String str_username, String str_passwd ) {

    try {
      if ( RepositoryConnectController.getInstance().isRelogin() == true ) {
        RepositoryConnectController.getInstance().reconnectToRepository( str_repoName, str_username, str_passwd );
      } else {
        RepositoryConnectController.getInstance().connectToRepository( str_repoName, str_username, str_passwd );
      }
      shell.close();
    } catch ( Exception e ) {
      System.out.println( e );
    }
  }

  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }
}
