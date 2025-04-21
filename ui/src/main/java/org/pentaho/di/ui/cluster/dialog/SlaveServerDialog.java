/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.cluster.dialog;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.DialogUtils;
import org.pentaho.di.www.RegisterTransServlet;

/**
 *
 * Dialog that allows you to edit the settings of the security service connection
 *
 * @see SlaveServer
 * @author Matt
 * @since 31-10-2006
 *
 */

public class SlaveServerDialog extends Dialog {
  private static Class<?> PKG = SlaveServerDialog.class; // for i18n purposes, needed by Translator2!!

  private SlaveServer slaveServer;

  private Collection<SlaveServer> existingServers;

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wServiceTab, wProxyTab;

  private Composite wServiceComp, wProxyComp;
  private FormData fdServiceComp, fdProxyComp;

  private Shell shell;

  // Service
  private Text wName;
  private TextVar wHostname, wPort, wWebAppName, wUsername, wPassword;
  private Button wMaster;
  private Button wSSL;

  // Proxy
  private TextVar wProxyHost, wProxyPort, wNonProxyHosts;

  private Button wOK, wCancel;

  private ModifyListener lsMod;

  private PropsUI props;

  private int middle;
  private int margin;

  private SlaveServer originalServer;
  private boolean ok;

  public SlaveServerDialog( Shell par, SlaveServer slaveServer, Collection<SlaveServer> existingServers ) {
    super( par, SWT.NONE );
    this.slaveServer = (SlaveServer) slaveServer.clone();
    this.slaveServer.shareVariablesWith( slaveServer );
    this.originalServer = slaveServer;
    this.existingServers = existingServers;
    props = PropsUI.getInstance();
    ok = false;
  }

  public SlaveServerDialog( Shell par, SlaveServer slaveServer ) {
    this( par, slaveServer, Collections.<SlaveServer>emptyList() );
  }

  public boolean open() {
    Shell parent = getParent();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSlave() );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        slaveServer.setChanged();
      }
    };

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( BaseMessages.getString( PKG, "SlaveServerDialog.Shell.Title" ) );
    shell.setLayout( formLayout );

    // First, add the buttons...

    // Buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    Button[] buttons = new Button[] { wOK, wCancel };
    BaseStepDialog.positionBottomButtons( shell, buttons, margin, null );

    // The rest stays above the buttons...

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    addServiceTab();
    addProxyTab();

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( 0, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( wOK, -margin );
    wTabFolder.setLayoutData( fdTabFolder );

    // Add listeners
    wOK.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    } );
    wCancel.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    } );

    SelectionAdapter selAdapter = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wUsername.addSelectionListener( selAdapter );
    wPassword.addSelectionListener( selAdapter );
    wHostname.addSelectionListener( selAdapter );
    wPort.addSelectionListener( selAdapter );
    wWebAppName.addSelectionListener( selAdapter );
    wProxyHost.addSelectionListener( selAdapter );
    wProxyPort.addSelectionListener( selAdapter );
    wNonProxyHosts.addSelectionListener( selAdapter );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );

    getData();

    BaseStepDialog.setSize( shell );

    shell.open();
    Display display = parent.getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return ok;
  }

  private void addServiceTab() {
    // ////////////////////////
    // START OF DB TAB ///
    // ////////////////////////
    wServiceTab = new CTabItem( wTabFolder, SWT.NONE );
    wServiceTab.setText( BaseMessages.getString( PKG, "SlaveServerDialog.USER_TAB_SERVICE" ) );

    wServiceComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wServiceComp );

    FormLayout GenLayout = new FormLayout();
    GenLayout.marginWidth = Const.FORM_MARGIN;
    GenLayout.marginHeight = Const.FORM_MARGIN;
    wServiceComp.setLayout( GenLayout );

    // What's the name
    Label wlName = new Label( wServiceComp, SWT.RIGHT );
    props.setLook( wlName );
    wlName.setText( BaseMessages.getString( PKG, "SlaveServerDialog.ServerName.Label" ) );
    FormData fdlName = new FormData();
    fdlName.top = new FormAttachment( 0, 0 );
    fdlName.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlName.right = new FormAttachment( middle, -margin );
    wlName.setLayoutData( fdlName );

    wName = new Text( wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    FormData fdName = new FormData();
    fdName.top = new FormAttachment( 0, 0 );
    fdName.left = new FormAttachment( middle, 0 ); // To the right of the label
    fdName.right = new FormAttachment( 95, 0 );
    wName.setLayoutData( fdName );

    // What's the hostname
    Label wlHostname = new Label( wServiceComp, SWT.RIGHT );
    props.setLook( wlHostname );
    wlHostname.setText( BaseMessages.getString( PKG, "SlaveServerDialog.HostIP.Label" ) );
    FormData fdlHostname = new FormData();
    fdlHostname.top = new FormAttachment( wName, margin * 2 );
    fdlHostname.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlHostname.right = new FormAttachment( middle, -margin );
    wlHostname.setLayoutData( fdlHostname );

    wHostname = new TextVar( slaveServer, wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wHostname );
    wHostname.addModifyListener( lsMod );
    FormData fdHostname = new FormData();
    fdHostname.top = new FormAttachment( wName, margin * 2 );
    fdHostname.left = new FormAttachment( middle, 0 ); // To the right of the label
    fdHostname.right = new FormAttachment( 95, 0 );
    wHostname.setLayoutData( fdHostname );

    // What's the service URL?
    Label wlPort = new Label( wServiceComp, SWT.RIGHT );
    props.setLook( wlPort );
    wlPort.setText( BaseMessages.getString( PKG, "SlaveServerDialog.Port.Label" ) );
    FormData fdlPort = new FormData();
    fdlPort.top = new FormAttachment( wHostname, margin );
    fdlPort.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlPort.right = new FormAttachment( middle, -margin );
    wlPort.setLayoutData( fdlPort );

    wPort = new TextVar( slaveServer, wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPort );
    wPort.addModifyListener( lsMod );
    FormData fdPort = new FormData();
    fdPort.top = new FormAttachment( wHostname, margin );
    fdPort.left = new FormAttachment( middle, 0 ); // To the right of the label
    fdPort.right = new FormAttachment( 95, 0 );
    wPort.setLayoutData( fdPort );

    // webapp name (optional)
    Label wlWebAppName = new Label( wServiceComp, SWT.RIGHT );
    wlWebAppName.setText( BaseMessages.getString( PKG, "SlaveServerDialog.WebAppName.Label" ) );
    props.setLook( wlWebAppName );
    FormData fdlWebAppName = new FormData();
    fdlWebAppName.top = new FormAttachment( wPort, margin );
    fdlWebAppName.left = new FormAttachment( 0, 0 );
    fdlWebAppName.right = new FormAttachment( middle, -margin );
    wlWebAppName.setLayoutData( fdlWebAppName );

    wWebAppName = new TextVar( slaveServer, wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wWebAppName );
    wWebAppName.addModifyListener( lsMod );
    FormData fdWebAppName = new FormData();
    fdWebAppName.top = new FormAttachment( wPort, margin );
    fdWebAppName.left = new FormAttachment( middle, 0 );
    fdWebAppName.right = new FormAttachment( 95, 0 );
    wWebAppName.setLayoutData( fdWebAppName );

    // Username
    Label wlUsername = new Label( wServiceComp, SWT.RIGHT );
    wlUsername.setText( BaseMessages.getString( PKG, "SlaveServerDialog.UserName.Label" ) );
    props.setLook( wlUsername );
    FormData fdlUsername = new FormData();
    fdlUsername.top = new FormAttachment( wWebAppName, margin );
    fdlUsername.left = new FormAttachment( 0, 0 );
    fdlUsername.right = new FormAttachment( middle, -margin );
    wlUsername.setLayoutData( fdlUsername );

    wUsername = new TextVar( slaveServer, wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUsername );
    wUsername.addModifyListener( lsMod );
    FormData fdUsername = new FormData();
    fdUsername.top = new FormAttachment( wWebAppName, margin );
    fdUsername.left = new FormAttachment( middle, 0 );
    fdUsername.right = new FormAttachment( 95, 0 );
    wUsername.setLayoutData( fdUsername );

    // Password
    Label wlPassword = new Label( wServiceComp, SWT.RIGHT );
    wlPassword.setText( BaseMessages.getString( PKG, "SlaveServerDialog.Password.Label" ) );
    props.setLook( wlPassword );
    FormData fdlPassword = new FormData();
    fdlPassword.top = new FormAttachment( wUsername, margin );
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.right = new FormAttachment( middle, -margin );
    wlPassword.setLayoutData( fdlPassword );

    wPassword = new PasswordTextVar( slaveServer, wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.top = new FormAttachment( wUsername, margin );
    fdPassword.left = new FormAttachment( middle, 0 );
    fdPassword.right = new FormAttachment( 95, 0 );
    wPassword.setLayoutData( fdPassword );

    // Master
    Label wlMaster = new Label( wServiceComp, SWT.RIGHT );
    wlMaster.setText( BaseMessages.getString( PKG, "SlaveServerDialog.IsTheMaster.Label" ) );
    props.setLook( wlMaster );
    FormData fdlMaster = new FormData();
    fdlMaster.top = new FormAttachment( wPassword, margin );
    fdlMaster.left = new FormAttachment( 0, 0 );
    fdlMaster.right = new FormAttachment( middle, -margin );
    wlMaster.setLayoutData( fdlMaster );

    wMaster = new Button( wServiceComp, SWT.CHECK );
    props.setLook( wMaster );
    FormData fdMaster = new FormData();
    fdMaster.top = new FormAttachment( wPassword, margin );
    fdMaster.left = new FormAttachment( middle, 0 );
    fdMaster.right = new FormAttachment( 95, 0 );
    wMaster.setLayoutData( fdMaster );

    // Https
    Control lastControl = wMaster;
    Label wlSSL = new Label( wServiceComp, SWT.RIGHT );
    wlSSL.setText( BaseMessages.getString( PKG, "SlaveServerDialog.UseSsl.Label" ) );
    props.setLook( wlSSL );
    FormData fd = new FormData();
    fd.top = new FormAttachment( lastControl, margin );
    fd.left = new FormAttachment( 0, 0 );
    fd.right = new FormAttachment( middle, -margin );
    wlSSL.setLayoutData( fd );
    wlSSL.setVisible( true );

    wSSL = new Button( wServiceComp, SWT.CHECK );
    props.setLook( wSSL );
    FormData bfd = new FormData();
    bfd.top = new FormAttachment( lastControl, margin );
    bfd.left = new FormAttachment( middle, 0 );
    bfd.right = new FormAttachment( 95, 0 );
    wSSL.setLayoutData( bfd );
    wSSL.setVisible( true );

    fdServiceComp = new FormData();
    fdServiceComp.left = new FormAttachment( 0, 0 );
    fdServiceComp.top = new FormAttachment( 0, 0 );
    fdServiceComp.right = new FormAttachment( 100, 0 );
    fdServiceComp.bottom = new FormAttachment( 100, 0 );
    wServiceComp.setLayoutData( fdServiceComp );

    wServiceComp.layout();
    wServiceTab.setControl( wServiceComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GEN TAB
    // ///////////////////////////////////////////////////////////
  }

  private void addProxyTab() {
    // ////////////////////////
    // START OF POOL TAB///
    // /
    wProxyTab = new CTabItem( wTabFolder, SWT.NONE );
    wProxyTab.setText( BaseMessages.getString( PKG, "SlaveServerDialog.USER_TAB_PROXY" ) );

    FormLayout poolLayout = new FormLayout();
    poolLayout.marginWidth = Const.FORM_MARGIN;
    poolLayout.marginHeight = Const.FORM_MARGIN;

    wProxyComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wProxyComp );
    wProxyComp.setLayout( poolLayout );

    // What's the data tablespace name?
    Label wlProxyHost = new Label( wProxyComp, SWT.RIGHT );
    props.setLook( wlProxyHost );
    wlProxyHost.setText( BaseMessages.getString( PKG, "SlaveServerDialog.ProxyServerName.Label" ) );
    FormData fdlProxyHost = new FormData();
    fdlProxyHost.top = new FormAttachment( 0, 0 );
    fdlProxyHost.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlProxyHost.right = new FormAttachment( middle, -margin );
    wlProxyHost.setLayoutData( fdlProxyHost );

    wProxyHost = new TextVar( slaveServer, wProxyComp, SWT.BORDER | SWT.LEFT | SWT.SINGLE );
    props.setLook( wProxyHost );
    wProxyHost.addModifyListener( lsMod );
    FormData fdProxyHost = new FormData();
    fdProxyHost.top = new FormAttachment( 0, 0 );
    fdProxyHost.left = new FormAttachment( middle, 0 ); // To the right of the label
    fdProxyHost.right = new FormAttachment( 95, 0 );
    wProxyHost.setLayoutData( fdProxyHost );

    // What's the initial pool size
    Label wlProxyPort = new Label( wProxyComp, SWT.RIGHT );
    props.setLook( wlProxyPort );
    wlProxyPort.setText( BaseMessages.getString( PKG, "SlaveServerDialog.ProxyServerPort.Label" ) );
    FormData fdlProxyPort = new FormData();
    fdlProxyPort.top = new FormAttachment( wProxyHost, margin );
    fdlProxyPort.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlProxyPort.right = new FormAttachment( middle, -margin );
    wlProxyPort.setLayoutData( fdlProxyPort );

    wProxyPort = new TextVar( slaveServer, wProxyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wProxyPort );
    wProxyPort.addModifyListener( lsMod );
    FormData fdProxyPort = new FormData();
    fdProxyPort.top = new FormAttachment( wProxyHost, margin );
    fdProxyPort.left = new FormAttachment( middle, 0 ); // To the right of the label
    fdProxyPort.right = new FormAttachment( 95, 0 );
    wProxyPort.setLayoutData( fdProxyPort );

    // What's the maximum pool size
    Label wlNonProxyHosts = new Label( wProxyComp, SWT.RIGHT );
    props.setLook( wlNonProxyHosts );
    wlNonProxyHosts.setText( BaseMessages.getString( PKG, "SlaveServerDialog.IgnoreProxyForHosts.Label" ) );
    FormData fdlNonProxyHosts = new FormData();
    fdlNonProxyHosts.top = new FormAttachment( wProxyPort, margin );
    fdlNonProxyHosts.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlNonProxyHosts.right = new FormAttachment( middle, -margin );
    wlNonProxyHosts.setLayoutData( fdlNonProxyHosts );

    wNonProxyHosts = new TextVar( slaveServer, wProxyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wNonProxyHosts );
    wNonProxyHosts.addModifyListener( lsMod );
    FormData fdNonProxyHosts = new FormData();
    fdNonProxyHosts.top = new FormAttachment( wProxyPort, margin );
    fdNonProxyHosts.left = new FormAttachment( middle, 0 ); // To the right of the label
    fdNonProxyHosts.right = new FormAttachment( 95, 0 );
    wNonProxyHosts.setLayoutData( fdNonProxyHosts );

    fdProxyComp = new FormData();
    fdProxyComp.left = new FormAttachment( 0, 0 );
    fdProxyComp.top = new FormAttachment( 0, 0 );
    fdProxyComp.right = new FormAttachment( 100, 0 );
    fdProxyComp.bottom = new FormAttachment( 100, 0 );
    wProxyComp.setLayoutData( fdProxyComp );

    wProxyComp.layout();
    wProxyTab.setControl( wProxyComp );
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void getData() {
    wName.setText( Const.NVL( slaveServer.getName(), "" ) );
    wHostname.setText( Const.NVL( slaveServer.getHostname(), "" ) );
    wPort.setText( Const.NVL( slaveServer.getPort(), "" ) );
    wWebAppName.setText( Const.NVL( slaveServer.getWebAppName(), "" ) );
    wUsername.setText( Const.NVL( slaveServer.getUsername(), "" ) );
    wPassword.setText( Const.NVL( slaveServer.getPassword(), "" ) );

    wProxyHost.setText( Const.NVL( slaveServer.getProxyHostname(), "" ) );
    wProxyPort.setText( Const.NVL( slaveServer.getProxyPort(), "" ) );
    wNonProxyHosts.setText( Const.NVL( slaveServer.getNonProxyHosts(), "" ) );

    wMaster.setSelection( slaveServer.isMaster() );

    wSSL.setSelection( slaveServer.isSslMode() );

    wName.setFocus();
  }

  private void cancel() {
    originalServer = null;
    dispose();
  }

  private String trim( String orig ) {
    if ( orig == null ) {
      return orig;
    }
    return orig.trim();
  }

  public void ok() {
    getInfo();

    if ( slaveServer.getName().isEmpty() ) {
      showMessage( BaseMessages.getString( PKG, "SlaveServerDialog.SlaveServerNoName" ) );
      return;
    }
    String newName = trim( slaveServer.getName() );
    slaveServer.setName( newName );
    String origName = trim( originalServer.getName() );
    if ( !newName.equals( origName ) ) {
      DialogUtils.removeMatchingObject( origName, existingServers );
      if ( DialogUtils.objectWithTheSameNameExists( slaveServer, existingServers ) ) {
        String title = BaseMessages.getString( PKG, "SlaveServerDialog.SlaveServerNameExists.Title" );
        String message =
            BaseMessages.getString( PKG, "SlaveServerDialog.SlaveServerNameExists", slaveServer.getName() );
        String okButton = BaseMessages.getString( PKG, "System.Button.OK" );
        MessageDialog dialog =
            new MessageDialog( shell, title, null, message, MessageDialog.ERROR, new String[] { okButton }, 0 );

        dialog.open();
        return;
      }
    }

    originalServer.setName( newName );
    originalServer.setHostname( slaveServer.getHostname() );
    originalServer.setPort( slaveServer.getPort() );
    originalServer.setWebAppName( slaveServer.getWebAppName() );
    originalServer.setUsername( slaveServer.getUsername() );
    originalServer.setPassword( slaveServer.getPassword() );

    originalServer.setProxyHostname( slaveServer.getProxyHostname() );
    originalServer.setProxyPort( slaveServer.getProxyPort() );
    originalServer.setNonProxyHosts( slaveServer.getNonProxyHosts() );

    originalServer.setMaster( slaveServer.isMaster() );

    originalServer.setSslMode( slaveServer.isSslMode() );

    originalServer.setChanged();

    ok = true;

    dispose();
  }

  // Get dialog info in securityService
  private void getInfo() {
    slaveServer.setName( wName.getText().trim() );
    slaveServer.setHostname( wHostname.getText() );
    slaveServer.setPort( wPort.getText() );
    slaveServer.setWebAppName( wWebAppName.getText() );
    slaveServer.setUsername( wUsername.getText() );
    slaveServer.setPassword( wPassword.getText() );

    slaveServer.setProxyHostname( wProxyHost.getText() );
    slaveServer.setProxyPort( wProxyPort.getText() );
    slaveServer.setNonProxyHosts( wNonProxyHosts.getText() );

    slaveServer.setMaster( wMaster.getSelection() );

    slaveServer.setSslMode( wSSL.getSelection() );
  }

  public void test() {
    try {
      getInfo();

      String xml = "<sample/>";

      String reply = slaveServer.sendXML( xml, RegisterTransServlet.CONTEXT_PATH );

      String message =
        BaseMessages.getString( PKG, "SlaveServer.Replay.Info1" )
          + slaveServer.constructUrl( RegisterTransServlet.CONTEXT_PATH ) + Const.CR
          + BaseMessages.getString( PKG, "SlaveServer.Replay.Info2" ) + Const.CR + Const.CR;
      message += xml;
      message += Const.CR + Const.CR;
      message += "Reply was:" + Const.CR + Const.CR;
      message += reply + Const.CR;

      EnterTextDialog dialog =
        new EnterTextDialog(
          shell, "XML", BaseMessages.getString( PKG, "SlaveServer.RetournedXMLInfo" ), message );
      dialog.open();
    } catch ( Exception e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "SlaveServer.ExceptionError" ), BaseMessages.getString(
        PKG, "SlaveServer.ExceptionUnableGetReplay.Error1" )
        + slaveServer.getHostname()
        + BaseMessages.getString( PKG, "SlaveServer.ExceptionUnableGetReplay.Error2" ), e );
    }
  }

  private void showMessage( String message ) {
    MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
    mb.setMessage( message );
    mb.open();
  }
}
