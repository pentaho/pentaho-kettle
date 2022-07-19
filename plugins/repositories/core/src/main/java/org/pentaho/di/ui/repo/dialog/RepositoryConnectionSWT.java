package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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
  //private RepositoryConnectController controller;
  private Shell shell;
  private Display display;
  private PropsUI props;
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  //private static final String MANAGER_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Manager.Title" );
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

    try {
      System.out.println( "create dialog swt try block" );

      System.out.println( "in conn manager rcvd selected repos: " + str_repoName );

//      Label lblRepositoryConnection = new Label( shell, SWT.CENTER );
//      lblRepositoryConnection.setText( "Repository Connection" );
//      lblRepositoryConnection.setLayoutData( new FormDataBuilder().top().left( 50, 0 ).right( 50, 0 ).result() );

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

      txt_passwd = new Text( shell, SWT.BORDER );
      props.setLook( txt_passwd );
      txt_passwd.setLayoutData( new FormDataBuilder().top( lblPassword ).left( 5, 0 ).right( 95, 0 ).result() );

      Button btnConnect_1 = new Button( shell, SWT.NONE );
      props.setLook( btnConnect_1 );
      btnConnect_1.setLayoutData( new FormDataBuilder().top( txt_passwd ).result() );
      btnConnect_1.setText( "login" );

      //******************** HELP **********************************
      Button btnHelp = new Button( shell, SWT.ICON_INFORMATION );
      props.setLook( btnHelp );
      btnHelp.setLayoutData( new FormDataBuilder().bottom().result() );
      btnHelp.setText( "help" );
      btnHelp.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event event ) {
          System.out.println( "help button clicked" );
          System.out.println( "help url :" + HELP_URL );
          Program.launch( HELP_URL );

        }
      } );


      createContents();

      btnConnect_1.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event event ) {
          System.out.println( "button pressed" );

          str_username = txt_username.getText();
          str_passwd = txt_passwd.getText();
          //System.out.println("processed_repo_name :"+repo_name);
          //str_repoURL = repo_name;

          System.out.println( "rcvd url, id and password" );

          System.out.println( "rcvd reponame :" + str_repoName );
          System.out.println( "rcvd username :" + str_username );
          System.out.println( "rcvd password :" + str_passwd );

          if ( str_repoName.isEmpty() ) {
            System.out.println( "blank ip reponame" );

          }
          if ( str_username.isEmpty() ) {
            System.out.println( "blank ip username" );

          }
          if ( str_passwd.isEmpty() ) {
            System.out.println( "blank password" );

          } else {
            System.out.println( "not blank ip username and password" );
            callLoginEndPoint( str_repoName, str_username, str_passwd );

          }
        }
      } );
      shell.pack();
      shell.setMinimumSize( 500, 250 );
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

  /**
   * Create the shell.
   *
   * @param display
   */


  /**
   * Create contents of the shell.
   */
  protected void createContents() {
    setText( LOGIN_TITLE );
//    setSize( 739, 707 );
//    setImage( LOGO );

  }

  void callLoginEndPoint( String str_repoName, String str_username, String str_passwd ) {

    System.out.println( "login end points called:" );
    System.out.println( "repo name : " + str_repoName );
    System.out.println( "username : " + str_username );
    System.out.println( "password : " + str_passwd );
    try {
      System.out.println( "try block controller connect to repo" );
      //            RepositoryConnectController newcontroller = new RepositoryConnectController();

      //below logic should be replacing active logic //do not delete
            /*if ( controller.isRelogin() ) {
                System.out.println("here in relogin");
                controller
                        .reconnectToRepository( repositoryName, repositoryName, password );
            } else {
                System.out.println("here in login");
                controller
                        .connectToRepository(repositoryName, repositoryName, password );
            }*/

      RepositoryConnectController.getInstance().connectToRepository( str_repoName, str_username, str_passwd );

      System.out.println( "repo connection successful" );
      shell.close();
    } catch ( Exception e ) {
      System.out.println( e );
      System.out.println( "catch block of repoendpoints" );
    }
    System.out.println( "login end points calls ended" );
  }

  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }
}
