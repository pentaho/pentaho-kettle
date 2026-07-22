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


package org.pentaho.di.ui.job.entries.mail;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.mail.JobEntryMail;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

/**
 * Dialog that allows you to edit a JobEntryMail object.
 *
 * @author Matt
 * @since 19-06-2003
 */
@PluginDialog( id = "MAIL", image = "MAIL.svg", pluginType = PluginDialog.PluginType.JOBENTRY,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail" )
public class JobEntryMailDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryMail.class; // for i18n purposes, needed by Translator2!!

  private static final String[] IMAGES_FILE_TYPES =
    new String[] {
      BaseMessages.getString( PKG, "JobMail.Filetype.Png" ),
      BaseMessages.getString( PKG, "JobMail.Filetype.Jpeg" ),
      BaseMessages.getString( PKG, "JobMail.Filetype.Gif" ),
      BaseMessages.getString( PKG, "JobMail.Filetype.All" ) };

  private LabelText wName;

  private LabelTextVar wDestination;

  private LabelTextVar wDestinationCc;

  private LabelTextVar wDestinationBCc;

  private LabelTextVar wServer;

  private LabelTextVar wPort;

  private Combo wUseAuth;

  private Label wlGrantType;

  private Combo grantType;

  private Label wlUseSecAuth;

  private Button wUseSecAuth;

  private LabelTextVar wAuthUser;

  private LabelTextVar wAuthPass;

  private LabelTextVar wAuthClientId;

  private LabelTextVar wAuthSecretKey;

  private LabelTextVar wAuthScope;

  private LabelTextVar wAuthTokenUrl;

  private LabelTextVar wAuthorizationCode;

  private LabelTextVar wRedirectUri;

  private LabelTextVar wAuthRefreshToken;

  private LabelTextVar wReply;
  private LabelTextVar wReplyName;

  private LabelTextVar wSubject;

  private Button wAddDate;

  private Button wIncludeFiles;

  private Label wlTypes;

  private List wTypes;

  private Label wlZipFiles;

  private Button wZipFiles;

  private LabelTextVar wZipFilename;

  private LabelTextVar wPerson;

  private LabelTextVar wPhone;

  private TextVar wComment;

  private Button wOnlyComment;
  private Button wUseHTML;
  private Button wUsePriority;

  private Label wlEncoding;
  private CCombo wEncoding;

  private Label wlSecureConnectionType;
  private CCombo wSecureConnectionType;

  private Label wlPriority;
  private CCombo wPriority;

  private Label wlImportance;
  private CCombo wImportance;

  private Label wlSensitivity;
  private CCombo wSensitivity;

  private Shell shell;

  private JobEntryMail jobEntry;

  private boolean backupDate;
  private boolean backupChanged;

  private boolean gotEncodings = false;

  private LabelTextVar wReplyToAddress;

  private Label wlImageFilename;
  private Label wlContentID;
  private Label wlFields;
  private Button wbImageFilename;
  private Button wbAddImageFilename;
  private Button wbDeleteImageFilename;
  private Button wbEditImageFilename;
  private TextVar wImageFilename;
  private TextVar wContentID;
  private TableView wFields;
  private final Random random = new SecureRandom();

  public JobEntryMailDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryMail) jobEntryInt;
  }

  @Override
  public JobEntryInterface open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, props.getJobsDialogStyle() );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    ModifyListener lsMod = e -> jobEntry.setChanged();
    backupChanged = jobEntry.hasChanged();
    backupDate = jobEntry.getIncludeDate();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobMail.Header" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Name line
    wName =
      new LabelText( shell, BaseMessages.getString( PKG, "JobMail.NameOfEntry.Label" ), BaseMessages.getString(
        PKG, "JobMail.NameOfEntry.Tooltip" ) );
    wName.addModifyListener( lsMod );
    FormData fdName = new FormData();
    fdName.top = new FormAttachment( 0, 0 );
    fdName.left = new FormAttachment( 0, 0 );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    CTabFolder wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    CTabItem wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "JobMail.Tab.General.Label" ) );

    Composite wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF Destination Settings GROUP
    // ////////////////////////

    Group wDestinationGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wDestinationGroup );
    wDestinationGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.DestinationAddress.Label" ) );

    FormLayout destinationGroupLayout = new FormLayout();
    destinationGroupLayout.marginWidth = 10;
    destinationGroupLayout.marginHeight = 10;
    wDestinationGroup.setLayout( destinationGroupLayout );

    // Destination line
    wDestination = new LabelTextVar( jobMeta, wDestinationGroup,
      BaseMessages.getString( PKG, "JobMail.DestinationAddress.Label" ),
      BaseMessages.getString( PKG, "JobMail.DestinationAddress.Tooltip" ) );
    wDestination.addModifyListener( lsMod );
    FormData fdDestination = new FormData();
    fdDestination.left = new FormAttachment( 0, 0 );
    fdDestination.top = new FormAttachment( wName, margin );
    fdDestination.right = new FormAttachment( 100, 0 );
    wDestination.setLayoutData( fdDestination );

    // Destination Cc
    wDestinationCc = new LabelTextVar( jobMeta, wDestinationGroup,
      BaseMessages.getString( PKG, "JobMail.DestinationAddressCc.Label" ),
      BaseMessages.getString( PKG, "JobMail.DestinationAddressCc.Tooltip" ) );
    wDestinationCc.addModifyListener( lsMod );
    FormData fdDestinationCc = new FormData();
    fdDestinationCc.left = new FormAttachment( 0, 0 );
    fdDestinationCc.top = new FormAttachment( wDestination, margin );
    fdDestinationCc.right = new FormAttachment( 100, 0 );
    wDestinationCc.setLayoutData( fdDestinationCc );

    // Destination BCc
    wDestinationBCc =
      new LabelTextVar( jobMeta, wDestinationGroup,
        BaseMessages.getString( PKG, "JobMail.DestinationAddressBCc.Label" ),
        BaseMessages.getString( PKG, "JobMail.DestinationAddressBCc.Tooltip" ) );
    wDestinationBCc.addModifyListener( lsMod );
    FormData fdDestinationBCc = new FormData();
    fdDestinationBCc.left = new FormAttachment( 0, 0 );
    fdDestinationBCc.top = new FormAttachment( wDestinationCc, margin );
    fdDestinationBCc.right = new FormAttachment( 100, 0 );
    wDestinationBCc.setLayoutData( fdDestinationBCc );

    FormData fdDestinationGroup = new FormData();
    fdDestinationGroup.left = new FormAttachment( 0, margin );
    fdDestinationGroup.top = new FormAttachment( wName, margin );
    fdDestinationGroup.right = new FormAttachment( 100, -margin );
    wDestinationGroup.setLayoutData( fdDestinationGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF DESTINATION ADDRESS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Reply Settings GROUP
    // ////////////////////////

    Group wReplyGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wReplyGroup );
    wReplyGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.Reply.Label" ) );

    FormLayout replyGroupLayout = new FormLayout();
    replyGroupLayout.marginWidth = 10;
    replyGroupLayout.marginHeight = 10;
    wReplyGroup.setLayout( replyGroupLayout );

    // Reply name
    wReplyName = new LabelTextVar( jobMeta, wReplyGroup, BaseMessages.getString( PKG, "JobMail.ReplyName.Label" ),
      BaseMessages.getString( PKG, "JobMail.ReplyName.Tooltip" ) );
    wReplyName.addModifyListener( lsMod );
    FormData fdReplyName = new FormData();
    fdReplyName.left = new FormAttachment( 0, 0 );
    fdReplyName.top = new FormAttachment( wDestinationGroup, 2 * margin );
    fdReplyName.right = new FormAttachment( 100, 0 );
    wReplyName.setLayoutData( fdReplyName );

    // Reply line
    wReply = new LabelTextVar( jobMeta, wReplyGroup, BaseMessages.getString( PKG, "JobMail.ReplyAddress.Label" ),
      BaseMessages.getString( PKG, "JobMail.ReplyAddress.Tooltip" ) );
    wReply.addModifyListener( lsMod );
    FormData fdReply = new FormData();
    fdReply.left = new FormAttachment( 0, 0 );
    fdReply.top = new FormAttachment( wReplyName, margin );
    fdReply.right = new FormAttachment( 100, 0 );
    wReply.setLayoutData( fdReply );

    FormData fdReplyGroup = new FormData();
    fdReplyGroup.left = new FormAttachment( 0, margin );
    fdReplyGroup.top = new FormAttachment( wDestinationGroup, margin );
    fdReplyGroup.right = new FormAttachment( 100, -margin );
    wReplyGroup.setLayoutData( fdReplyGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Replay GROUP
    // ///////////////////////////////////////////////////////////

    // Reply to
    wReplyToAddress = new LabelTextVar( jobMeta, wGeneralComp,
      BaseMessages.getString( PKG, "JobMail.ReplyToAddress.Label" ),
      BaseMessages.getString( PKG, "JobMail.ReplyToAddress.Tooltip" ) );
    wReplyToAddress.addModifyListener( lsMod );
    FormData fdReplyToAddress = new FormData();
    fdReplyToAddress.left = new FormAttachment( 0, 0 );
    fdReplyToAddress.top = new FormAttachment( wReplyGroup, 2 * margin );
    fdReplyToAddress.right = new FormAttachment( 100, 0 );
    wReplyToAddress.setLayoutData( fdReplyToAddress );

    // Contact line
    wPerson = new LabelTextVar( jobMeta, wGeneralComp, BaseMessages.getString( PKG, "JobMail.ContactPerson.Label" ),
      BaseMessages.getString( PKG, "JobMail.ContactPerson.Tooltip" ) );
    wPerson.addModifyListener( lsMod );
    FormData fdPerson = new FormData();
    fdPerson.left = new FormAttachment( 0, 0 );
    fdPerson.top = new FormAttachment( wReplyToAddress, 2 * margin );
    fdPerson.right = new FormAttachment( 100, 0 );
    wPerson.setLayoutData( fdPerson );

    // Phone line
    wPhone = new LabelTextVar( jobMeta, wGeneralComp, BaseMessages.getString( PKG, "JobMail.ContactPhone.Label" ),
      BaseMessages.getString( PKG, "JobMail.ContactPhone.Tooltip" ) );
    wPhone.addModifyListener( lsMod );
    FormData fdPhone = new FormData();
    fdPhone.left = new FormAttachment( 0, 0 );
    fdPhone.top = new FormAttachment( wPerson, margin );
    fdPhone.right = new FormAttachment( 100, 0 );
    wPhone.setLayoutData( fdPhone );

    FormData fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 500, -margin );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
    props.setLook( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////////
    // START OF SERVER TAB ///
    // ///////////////////////////////////

    CTabItem wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "JobMailDialog.Server.Label" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    Composite wContentComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // ////////////////////////
    // START OF SERVER GROUP
    // /////////////////////////

    Group wServerGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wServerGroup );
    wServerGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.SMTPServer.Label" ) );

    FormLayout serverGroupLayout = new FormLayout();
    serverGroupLayout.marginWidth = 10;
    serverGroupLayout.marginHeight = 10;
    wServerGroup.setLayout( serverGroupLayout );

    // Server line
    wServer = new LabelTextVar( jobMeta, wServerGroup, BaseMessages.getString( PKG, "JobMail.SMTPServer.Label" ),
      BaseMessages.getString( PKG, "JobMail.SMTPServer.Tooltip" ) );
    wServer.addModifyListener( lsMod );
    FormData fdServer = new FormData();
    fdServer.left = new FormAttachment( 0, 0 );
    fdServer.top = new FormAttachment( 0, margin );
    fdServer.right = new FormAttachment( 100, 0 );
    wServer.setLayoutData( fdServer );

    // Port line
    wPort = new LabelTextVar( jobMeta, wServerGroup, BaseMessages.getString( PKG, "JobMail.Port.Label" ), BaseMessages
      .getString( PKG, "JobMail.Port.Tooltip" ) );
    wPort.addModifyListener( lsMod );
    FormData fdPort = new FormData();
    fdPort.left = new FormAttachment( 0, 0 );
    fdPort.top = new FormAttachment( wServer, margin );
    fdPort.right = new FormAttachment( 100, 0 );
    wPort.setLayoutData( fdPort );

    FormData fdServerGroup = new FormData();
    fdServerGroup.left = new FormAttachment( 0, margin );
    fdServerGroup.top = new FormAttachment( wName, margin );
    fdServerGroup.right = new FormAttachment( 100, -margin );
    wServerGroup.setLayoutData( fdServerGroup );

    // //////////////////////////////////////
    // / END OF SERVER ADDRESS GROUP
    // ///////////////////////////////////////

    // ////////////////////////////////////
    // START OF AUTHENTIFICATION GROUP
    // ////////////////////////////////////

    Group wAuthentificationGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAuthentificationGroup );
    wAuthentificationGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.Authentification.Label" ) );

    FormLayout authentificationGroupLayout = new FormLayout();
    authentificationGroupLayout.marginWidth = 10;
    authentificationGroupLayout.marginHeight = 10;
    wAuthentificationGroup.setLayout( authentificationGroupLayout );

    // Authentication?
    Label wlUseAuth = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseAuth.setText( BaseMessages.getString( PKG, "JobMail.UseAuthentication.Label" ) );
    props.setLook( wlUseAuth );
    FormData fdlUseAuth = new FormData();
    fdlUseAuth.left = new FormAttachment( 0, 0 );
    fdlUseAuth.top = new FormAttachment( wServerGroup, 2 * margin );
    fdlUseAuth.right = new FormAttachment( middle, -margin );
    wlUseAuth.setLayoutData( fdlUseAuth );
    wUseAuth = new Combo( wAuthentificationGroup, SWT.DROP_DOWN | SWT.READ_ONLY );
    wUseAuth.add( JobEntryMail.AUTENTICATION_NONE );
    wUseAuth.add( JobEntryMail.AUTENTICATION_BASIC );
    wUseAuth.add( JobEntryMail.AUTENTICATION_OAUTH );
    wUseAuth.select( wUseAuth.indexOf( JobEntryMail.AUTENTICATION_NONE ) );
    props.setLook( wUseAuth );
    wUseAuth.addModifyListener( lsMod );
    FormData fdUseAuth = new FormData();
    fdUseAuth.left = new FormAttachment( middle, margin );
    fdUseAuth.top = new FormAttachment( wServerGroup, 2 * margin );
    fdUseAuth.right = new FormAttachment( 100, 0 );
    wUseAuth.setLayoutData( fdUseAuth );
    wUseAuth.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setUseAuth();
      }
    } );

    // AuthSecretKey line
    wAuthSecretKey = new LabelTextVar( jobMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "JobMail.AuthenticationSecretKey.Label" ),
            BaseMessages.getString( PKG, "JobMail.AuthenticationSecretKey.Tooltip" ), true );
    wAuthSecretKey.addModifyListener( lsMod );
    FormData fdAuthSecretKey = new FormData();
    fdAuthSecretKey.left = new FormAttachment( 0, 0 );
    fdAuthSecretKey.top = new FormAttachment( wUseAuth, margin );
    fdAuthSecretKey.right = new FormAttachment( 100, 0 );
    wAuthSecretKey.setLayoutData( fdAuthSecretKey );

    // AuthClientId line
    wAuthClientId = new LabelTextVar( jobMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "JobMail.AuthenticationClientId.Label" ),
            BaseMessages.getString( PKG, "JobMail.AuthenticationClientId.Tooltip" ));
    wAuthClientId.addModifyListener( lsMod );
    FormData fdAuthClientId = new FormData();
    fdAuthClientId.left = new FormAttachment( 0, 0 );
    fdAuthClientId.top = new FormAttachment( wAuthSecretKey, margin );
    fdAuthClientId.right = new FormAttachment( 100, 0 );
    wAuthClientId.setLayoutData( fdAuthClientId );

    // AuthUser line
    wAuthUser = new LabelTextVar( jobMeta, wAuthentificationGroup,
      BaseMessages.getString( PKG, "JobMail.AuthenticationUser.Label" ),
      BaseMessages.getString( PKG, "JobMail.AuthenticationUser.Tooltip" ) );
    wAuthUser.addModifyListener( lsMod );
    FormData fdAuthUser = new FormData();
    fdAuthUser.left = new FormAttachment( 0, 0 );
    fdAuthUser.top = new FormAttachment( wAuthClientId, margin );
    fdAuthUser.right = new FormAttachment( 100, 0 );
    wAuthUser.setLayoutData( fdAuthUser );

    // AuthPass line
    wAuthPass = new LabelTextVar( jobMeta, wAuthentificationGroup,
      BaseMessages.getString( PKG, "JobMail.AuthenticationPassword.Label" ),
      BaseMessages.getString( PKG, "JobMail.AuthenticationPassword.Tooltip" ), true );
    wAuthPass.addModifyListener( lsMod );
    FormData fdAuthPass = new FormData();
    fdAuthPass.left = new FormAttachment( 0, 0 );
    fdAuthPass.top = new FormAttachment( wAuthUser, margin );
    fdAuthPass.right = new FormAttachment( 100, 0 );
    wAuthPass.setLayoutData( fdAuthPass );

    //Scope line
    wAuthScope = new LabelTextVar( jobMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "JobMail.AuthenticationScope.Label" ),
            BaseMessages.getString( PKG, "JobMail.AuthenticationScope.Tooltip" ));
    wAuthScope.addModifyListener( lsMod );
    FormData fdAuthScope = new FormData();
    fdAuthScope.left = new FormAttachment( 0, 0 );
    fdAuthScope.top = new FormAttachment( wAuthPass, margin );
    fdAuthScope.right = new FormAttachment( 100, 0 );
    wAuthScope.setLayoutData( fdAuthScope );

    // Grant Type
    wlGrantType = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlGrantType.setText(BaseMessages.getString( PKG, "JobMail.GrantType.Label" ) );
    props.setLook( wlGrantType );
    FormData fdlGrantType = new FormData();
    fdlGrantType.left = new FormAttachment( 0, 0 );
    fdlGrantType.top = new FormAttachment( wAuthScope, 2*margin );
    fdlGrantType.right = new FormAttachment( middle, -margin );
    wlGrantType.setLayoutData( fdlGrantType );
    grantType = new Combo( wAuthentificationGroup, SWT.DROP_DOWN );
    grantType.add( JobEntryMail.GRANTTYPE_CLIENTCREDENTIALS );
    grantType.add( JobEntryMail.GRANTTYPE_AUTHORIZATION_CODE );
    grantType.add( JobEntryMail.GRANTTYPE_REFRESH_TOKEN );
    props.setLook( grantType );
    grantType.addModifyListener( lsMod );
    FormData fdGrantType = new FormData();
    fdGrantType.left = new FormAttachment( middle, margin );
    fdGrantType.top = new FormAttachment( wAuthScope, 2*margin );
    fdGrantType.right = new FormAttachment( 100, 0 );
    grantType.setLayoutData( fdGrantType );
    grantType.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setUseGrantType();
      }
    } );

    //Token Url
    wAuthTokenUrl = new LabelTextVar( jobMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "JobMail.AuthenticationTokenUrl.Label" ),
            BaseMessages.getString( PKG, "JobMail.AuthenticationTokenUrl.Tooltip" ));
    wAuthTokenUrl.addModifyListener( lsMod );
    FormData fdAuthTokenUrl = new FormData();
    fdAuthTokenUrl.left = new FormAttachment( 0, 0 );
    fdAuthTokenUrl.top = new FormAttachment( grantType, margin );
    fdAuthTokenUrl.right = new FormAttachment( 100, 0 );
    wAuthTokenUrl.setLayoutData( fdAuthTokenUrl );
    //AuthorizationCode
    wAuthorizationCode= new LabelTextVar( jobMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "JobMail.AuthorizationCode.Label" ),
            BaseMessages.getString( PKG, "JobMail.AuthorizationCode.Tooltip" ));
    wAuthorizationCode.addModifyListener( lsMod );
    FormData fdAuthorizationCode = new FormData();
    fdAuthorizationCode.left = new FormAttachment( 0, 0 );
    fdAuthorizationCode.top = new FormAttachment( wAuthTokenUrl, margin );
    fdAuthorizationCode.right = new FormAttachment( 100, 0 );
    wAuthorizationCode.setLayoutData( fdAuthorizationCode );
    //Redirect Uri
    wRedirectUri= new LabelTextVar( jobMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "JobMail.RedirectURI.Label" ),
            BaseMessages.getString( PKG, "JobMail.RedirectURI.Tooltip" ));
    wRedirectUri.addModifyListener( lsMod );
    FormData fdRedirectUri = new FormData();
    fdRedirectUri.left = new FormAttachment( 0, 0 );
    fdRedirectUri.top = new FormAttachment(wAuthorizationCode, margin );
    fdRedirectUri.right = new FormAttachment( 100, 0 );
    wRedirectUri.setLayoutData( fdRedirectUri );
    //Refresh Token
    wAuthRefreshToken= new LabelTextVar( jobMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "JobMail.RefreshToken.Label" ),
            BaseMessages.getString( PKG, "JobMail.RefreshToken.Tooltip" ));
    wAuthRefreshToken.addModifyListener( lsMod );
    FormData fdAuthRefreshToken = new FormData();
    fdAuthRefreshToken.left = new FormAttachment( 0, 0 );
    fdAuthRefreshToken.top = new FormAttachment(wRedirectUri, margin );
    fdAuthRefreshToken.right = new FormAttachment( 100, 0 );
    wAuthRefreshToken.setLayoutData( fdAuthRefreshToken );

    // Use secure authentication?
    wlUseSecAuth = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseSecAuth.setText( BaseMessages.getString( PKG, "JobMail.UseSecAuthentication.Label" ) );
    props.setLook( wlUseSecAuth );
    FormData fdlUseSecAuth = new FormData();
    fdlUseSecAuth.left = new FormAttachment( 0, 0 );
    fdlUseSecAuth.top = new FormAttachment( wAuthRefreshToken, 2 * margin );
    fdlUseSecAuth.right = new FormAttachment( middle, -margin );
    wlUseSecAuth.setLayoutData( fdlUseSecAuth );
    wUseSecAuth = new Button( wAuthentificationGroup, SWT.CHECK );
    props.setLook( wUseSecAuth );
    FormData fdUseSecAuth = new FormData();
    fdUseSecAuth.left = new FormAttachment( middle, margin );
    fdUseSecAuth.top = new FormAttachment( wAuthRefreshToken, 2 * margin );
    fdUseSecAuth.right = new FormAttachment( 100, 0 );
    wUseSecAuth.setLayoutData( fdUseSecAuth );
    wUseSecAuth.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setSecureConnectiontype();
        jobEntry.setChanged();
      }
    } );

    // SecureConnectionType
    wlSecureConnectionType = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlSecureConnectionType.setText( BaseMessages.getString( PKG, "JobMail.SecureConnectionType.Label" ) );
    props.setLook( wlSecureConnectionType );
    FormData fdlSecureConnectionType = new FormData();
    fdlSecureConnectionType.left = new FormAttachment( 0, 0 );
    fdlSecureConnectionType.top = new FormAttachment( wUseSecAuth, margin );
    fdlSecureConnectionType.right = new FormAttachment( middle, -margin );
    wlSecureConnectionType.setLayoutData( fdlSecureConnectionType );
    wSecureConnectionType = new CCombo( wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY );
    wSecureConnectionType.setEditable( true );
    props.setLook( wSecureConnectionType );
    wSecureConnectionType.addModifyListener( lsMod );
    FormData fdSecureConnectionType = new FormData();
    fdSecureConnectionType.left = new FormAttachment( middle, margin );
    fdSecureConnectionType.top = new FormAttachment( wUseSecAuth, margin );
    fdSecureConnectionType.right = new FormAttachment( 100, 0 );
    wSecureConnectionType.setLayoutData( fdSecureConnectionType );
    wSecureConnectionType.add( "SSL" );
    wSecureConnectionType.add( "TLS" );
    wSecureConnectionType.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setSecureConnectiontype();
        jobEntry.setChanged();
      }
    } );

    FormData fdAuthentificationGroup = new FormData();
    fdAuthentificationGroup.left = new FormAttachment( 0, margin );
    fdAuthentificationGroup.top = new FormAttachment( wServerGroup, margin );
    fdAuthentificationGroup.right = new FormAttachment( 100, -margin );
    fdAuthentificationGroup.bottom = new FormAttachment( 100, -margin );
    wAuthentificationGroup.setLayoutData( fdAuthentificationGroup );

    // //////////////////////////////////////
    // / END OF AUTHENTIFICATION GROUP
    // ///////////////////////////////////////

    FormData fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment( 0, 0 );
    fdContentComp.top = new FormAttachment( 0, 0 );
    fdContentComp.right = new FormAttachment( 100, 0 );
    fdContentComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( wContentComp );

    wContentComp.layout();
    wContentTab.setControl( wContentComp );

    // ///////////////////////////////////////////////////////////
    // / END OF SERVER TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////////
    // START OF MESSAGE TAB ///
    // ///////////////////////////////////

    CTabItem wMessageTab = new CTabItem( wTabFolder, SWT.NONE );
    wMessageTab.setText( BaseMessages.getString( PKG, "JobMail.Tab.Message.Label" ) );

    FormLayout messageLayout = new FormLayout();
    messageLayout.marginWidth = 3;
    messageLayout.marginHeight = 3;

    Composite wMessageComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wMessageComp );
    wMessageComp.setLayout( contentLayout );

    // ////////////////////////////////////
    // START OF MESSAGE SETTINGS GROUP
    // ////////////////////////////////////

    Group wMessageSettingsGroup = new Group( wMessageComp, SWT.SHADOW_NONE );
    props.setLook( wMessageSettingsGroup );
    wMessageSettingsGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.MessageSettings.Label" ) );

    FormLayout messageSettingsGroupLayout = new FormLayout();
    messageSettingsGroupLayout.marginWidth = 10;
    messageSettingsGroupLayout.marginHeight = 10;
    wMessageSettingsGroup.setLayout( messageSettingsGroupLayout );

    // Add date to logfile name?
    Label wlAddDate = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlAddDate.setText( BaseMessages.getString( PKG, "JobMail.IncludeDate.Label" ) );
    props.setLook( wlAddDate );
    FormData fdlAddDate = new FormData();
    fdlAddDate.left = new FormAttachment( 0, 0 );
    fdlAddDate.top = new FormAttachment( 0, margin );
    fdlAddDate.right = new FormAttachment( middle, -margin );
    wlAddDate.setLayoutData( fdlAddDate );
    wAddDate = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wAddDate );
    FormData fdAddDate = new FormData();
    fdAddDate.left = new FormAttachment( middle, margin );
    fdAddDate.top = new FormAttachment( 0, margin );
    fdAddDate.right = new FormAttachment( 100, 0 );
    wAddDate.setLayoutData( fdAddDate );
    wAddDate.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // Only send the comment in the mail body
    Label wlOnlyComment = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlOnlyComment.setText( BaseMessages.getString( PKG, "JobMail.OnlyCommentInBody.Label" ) );
    props.setLook( wlOnlyComment );
    FormData fdlOnlyComment = new FormData();
    fdlOnlyComment.left = new FormAttachment( 0, 0 );
    fdlOnlyComment.top = new FormAttachment( wAddDate, margin );
    fdlOnlyComment.right = new FormAttachment( middle, -margin );
    wlOnlyComment.setLayoutData( fdlOnlyComment );
    wOnlyComment = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wOnlyComment );
    FormData fdOnlyComment = new FormData();
    fdOnlyComment.left = new FormAttachment( middle, margin );
    fdOnlyComment.top = new FormAttachment( wAddDate, margin );
    fdOnlyComment.right = new FormAttachment( 100, 0 );
    wOnlyComment.setLayoutData( fdOnlyComment );
    wOnlyComment.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // HTML format ?
    Label wlUseHTML = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlUseHTML.setText( BaseMessages.getString( PKG, "JobMail.UseHTMLInBody.Label" ) );
    props.setLook( wlUseHTML );
    FormData fdlUseHTML = new FormData();
    fdlUseHTML.left = new FormAttachment( 0, 0 );
    fdlUseHTML.top = new FormAttachment( wOnlyComment, margin );
    fdlUseHTML.right = new FormAttachment( middle, -margin );
    wlUseHTML.setLayoutData( fdlUseHTML );
    wUseHTML = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wUseHTML );
    FormData fdUseHTML = new FormData();
    fdUseHTML.left = new FormAttachment( middle, margin );
    fdUseHTML.top = new FormAttachment( wOnlyComment, margin );
    fdUseHTML.right = new FormAttachment( 100, 0 );
    wUseHTML.setLayoutData( fdUseHTML );
    wUseHTML.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        SetEnabledEncoding();
        jobEntry.setChanged();
      }
    } );

    // Encoding
    wlEncoding = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "JobMail.Encoding.Label" ) );
    props.setLook( wlEncoding );
    FormData fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.top = new FormAttachment( wUseHTML, margin );
    fdlEncoding.right = new FormAttachment( middle, -margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new CCombo( wMessageSettingsGroup, SWT.BORDER | SWT.READ_ONLY );
    wEncoding.setEditable( true );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    FormData fdEncoding = new FormData();
    fdEncoding.left = new FormAttachment( middle, margin );
    fdEncoding.top = new FormAttachment( wUseHTML, margin );
    fdEncoding.right = new FormAttachment( 100, 0 );
    wEncoding.setLayoutData( fdEncoding );
    wEncoding.addFocusListener( new FocusListener() {
      @Override
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      @Override
      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setEncodings();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Use Priority ?
    Label wlUsePriority = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlUsePriority.setText( BaseMessages.getString( PKG, "JobMail.UsePriority.Label" ) );
    props.setLook( wlUsePriority );
    FormData fdlPriority = new FormData();
    fdlPriority.left = new FormAttachment( 0, 0 );
    fdlPriority.top = new FormAttachment( wEncoding, margin );
    fdlPriority.right = new FormAttachment( middle, -margin );
    wlUsePriority.setLayoutData( fdlPriority );
    wUsePriority = new Button( wMessageSettingsGroup, SWT.CHECK );
    wUsePriority.setToolTipText( BaseMessages.getString( PKG, "JobMail.UsePriority.Tooltip" ) );
    props.setLook( wUsePriority );
    FormData fdUsePriority = new FormData();
    fdUsePriority.left = new FormAttachment( middle, margin );
    fdUsePriority.top = new FormAttachment( wEncoding, margin );
    fdUsePriority.right = new FormAttachment( 100, 0 );
    wUsePriority.setLayoutData( fdUsePriority );
    wUsePriority.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        activeUsePriority();
        jobEntry.setChanged();
      }
    } );

    // Priority
    wlPriority = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlPriority.setText( BaseMessages.getString( PKG, "JobMail.Priority.Label" ) );
    props.setLook( wlPriority );
    fdlPriority = new FormData();
    fdlPriority.left = new FormAttachment( 0, 0 );
    fdlPriority.right = new FormAttachment( middle, -margin );
    fdlPriority.top = new FormAttachment( wUsePriority, margin );
    wlPriority.setLayoutData( fdlPriority );
    wPriority = new CCombo( wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wPriority.add( BaseMessages.getString( PKG, "JobMail.Priority.Low.Label" ) );
    wPriority.add( BaseMessages.getString( PKG, "JobMail.Priority.Normal.Label" ) );
    wPriority.add( BaseMessages.getString( PKG, "JobMail.Priority.High.Label" ) );
    wPriority.select( 1 ); // +1: starts at -1
    props.setLook( wPriority );
    FormData fdPriority = new FormData();
    fdPriority.left = new FormAttachment( middle, 0 );
    fdPriority.top = new FormAttachment( wUsePriority, margin );
    fdPriority.right = new FormAttachment( 100, 0 );
    wPriority.setLayoutData( fdPriority );

    // Importance
    wlImportance = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlImportance.setText( BaseMessages.getString( PKG, "JobMail.Importance.Label" ) );
    props.setLook( wlImportance );
    FormData fdlImportance = new FormData();
    fdlImportance.left = new FormAttachment( 0, 0 );
    fdlImportance.right = new FormAttachment( middle, -margin );
    fdlImportance.top = new FormAttachment( wPriority, margin );
    wlImportance.setLayoutData( fdlImportance );
    wImportance = new CCombo( wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wImportance.add( BaseMessages.getString( PKG, "JobMail.Priority.Low.Label" ) );
    wImportance.add( BaseMessages.getString( PKG, "JobMail.Priority.Normal.Label" ) );
    wImportance.add( BaseMessages.getString( PKG, "JobMail.Priority.High.Label" ) );

    wImportance.select( 1 ); // +1: starts at -1

    props.setLook( wImportance );
    FormData fdImportance = new FormData();
    fdImportance.left = new FormAttachment( middle, 0 );
    fdImportance.top = new FormAttachment( wPriority, margin );
    fdImportance.right = new FormAttachment( 100, 0 );
    wImportance.setLayoutData( fdImportance );

    // Sensitivity
    wlSensitivity = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlSensitivity.setText( BaseMessages.getString( PKG, "JobMail.Sensitivity.Label" ) );
    props.setLook( wlSensitivity );
    FormData fdlSensitivity = new FormData();
    fdlSensitivity.left = new FormAttachment( 0, 0 );
    fdlSensitivity.right = new FormAttachment( middle, -margin );
    fdlSensitivity.top = new FormAttachment( wImportance, margin );
    wlSensitivity.setLayoutData( fdlSensitivity );
    wSensitivity = new CCombo( wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wSensitivity.add( BaseMessages.getString( PKG, "JobMail.Sensitivity.normal.Label" ) );
    wSensitivity.add( BaseMessages.getString( PKG, "JobMail.Sensitivity.personal.Label" ) );
    wSensitivity.add( BaseMessages.getString( PKG, "JobMail.Sensitivity.private.Label" ) );
    wSensitivity.add( BaseMessages.getString( PKG, "JobMail.Sensitivity.confidential.Label" ) );
    wSensitivity.select( 0 );

    props.setLook( wSensitivity );
    FormData fdSensitivity = new FormData();
    fdSensitivity.left = new FormAttachment( middle, 0 );
    fdSensitivity.top = new FormAttachment( wImportance, margin );
    fdSensitivity.right = new FormAttachment( 100, 0 );
    wSensitivity.setLayoutData( fdSensitivity );

    FormData fdMessageSettingsGroup = new FormData();
    fdMessageSettingsGroup.left = new FormAttachment( 0, margin );
    fdMessageSettingsGroup.top = new FormAttachment( wName, margin );
    fdMessageSettingsGroup.right = new FormAttachment( 100, -margin );
    wMessageSettingsGroup.setLayoutData( fdMessageSettingsGroup );

    // //////////////////////////////////////
    // / END OF MESSAGE SETTINGS GROUP
    // ///////////////////////////////////////

    // ////////////////////////////////////
    // START OF MESSAGE GROUP
    // ////////////////////////////////////

    Group wMessageGroup = new Group( wMessageComp, SWT.SHADOW_NONE );
    props.setLook( wMessageGroup );
    wMessageGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.Message.Label" ) );

    FormLayout messageGroupLayout = new FormLayout();
    messageGroupLayout.marginWidth = 10;
    messageGroupLayout.marginHeight = 10;
    wMessageGroup.setLayout( messageGroupLayout );

    // Subject line
    wSubject =
      new LabelTextVar(
        jobMeta, wMessageGroup, BaseMessages.getString( PKG, "JobMail.Subject.Label" ), BaseMessages
          .getString( PKG, "JobMail.Subject.Tooltip" ) );
    wSubject.addModifyListener( lsMod );
    FormData fdSubject = new FormData();
    fdSubject.left = new FormAttachment( 0, 0 );
    fdSubject.top = new FormAttachment( wMessageSettingsGroup, margin );
    fdSubject.right = new FormAttachment( 100, 0 );
    wSubject.setLayoutData( fdSubject );

    // Comment line
    Label wlComment = new Label( wMessageGroup, SWT.RIGHT );
    wlComment.setText( BaseMessages.getString( PKG, "JobMail.Comment.Label" ) );
    props.setLook( wlComment );
    FormData fdlComment = new FormData();
    fdlComment.left = new FormAttachment( 0, 0 );
    fdlComment.top = new FormAttachment( wSubject, 2 * margin );
    fdlComment.right = new FormAttachment( middle, margin );
    wlComment.setLayoutData( fdlComment );

    wComment = new TextVar( jobMeta, wMessageGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    props.setLook( wComment );
    wComment.addModifyListener( lsMod );
    FormData fdComment = new FormData();
    fdComment.left = new FormAttachment( middle, margin );
    fdComment.top = new FormAttachment( wSubject, 2 * margin );
    fdComment.right = new FormAttachment( 100, 0 );
    fdComment.bottom = new FormAttachment( 100, -margin );
    wComment.setLayoutData( fdComment );

    FormData fdMessageGroup = new FormData();
    fdMessageGroup.left = new FormAttachment( 0, margin );
    fdMessageGroup.top = new FormAttachment( wMessageSettingsGroup, margin );
    fdMessageGroup.bottom = new FormAttachment( 100, -margin );
    fdMessageGroup.right = new FormAttachment( 100, -margin );
    wMessageGroup.setLayoutData( fdMessageGroup );

    // //////////////////////////////////////
    // / END OF MESSAGE GROUP
    // ///////////////////////////////////////

    FormData fdMessageComp = new FormData();
    fdMessageComp.left = new FormAttachment( 0, 0 );
    fdMessageComp.top = new FormAttachment( 0, 0 );
    fdMessageComp.right = new FormAttachment( 100, 0 );
    fdMessageComp.bottom = new FormAttachment( 100, 0 );
    wMessageComp.setLayoutData( wMessageComp );

    wMessageComp.layout();
    wMessageTab.setControl( wMessageComp );

    // ///////////////////////////////////////////////////////////
    // / END OF MESSAGE TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////////
    // START OF ATTACHED FILES TAB ///
    // ///////////////////////////////////

    CTabItem wAttachedTab = new CTabItem( wTabFolder, SWT.NONE );
    wAttachedTab.setText( BaseMessages.getString( PKG, "JobMail.Tab.AttachedFiles.Label" ) );

    FormLayout attachedLayout = new FormLayout();
    attachedLayout.marginWidth = 3;
    attachedLayout.marginHeight = 3;

    Composite wAttachedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAttachedComp );
    wAttachedComp.setLayout( attachedLayout );

    // ////////////////////////////////////
    // START OF Result File GROUP
    // ////////////////////////////////////

    Group wResultFilesGroup = new Group( wAttachedComp, SWT.SHADOW_NONE );
    props.setLook( wResultFilesGroup );
    wResultFilesGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.AddPreviousFiles.Label" ) );

    FormLayout resultFilesGroupLayout = new FormLayout();
    resultFilesGroupLayout.marginWidth = 10;
    resultFilesGroupLayout.marginHeight = 10;
    wResultFilesGroup.setLayout( resultFilesGroupLayout );

    // Include Files?
    Label wlIncludeFiles = new Label( wResultFilesGroup, SWT.RIGHT );
    wlIncludeFiles.setText( BaseMessages.getString( PKG, "JobMail.AttachFiles.Label" ) );
    props.setLook( wlIncludeFiles );
    FormData fdlIncludeFiles = new FormData();
    fdlIncludeFiles.left = new FormAttachment( 0, 0 );
    fdlIncludeFiles.top = new FormAttachment( 0, margin );
    fdlIncludeFiles.right = new FormAttachment( middle, -margin );
    wlIncludeFiles.setLayoutData( fdlIncludeFiles );
    wIncludeFiles = new Button( wResultFilesGroup, SWT.CHECK );
    props.setLook( wIncludeFiles );
    FormData fdIncludeFiles = new FormData();
    fdIncludeFiles.left = new FormAttachment( middle, margin );
    fdIncludeFiles.top = new FormAttachment( 0, margin );
    fdIncludeFiles.right = new FormAttachment( 100, 0 );
    wIncludeFiles.setLayoutData( fdIncludeFiles );
    wIncludeFiles.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
        setFlags();
      }
    } );

    // Include Files?
    wlTypes = new Label( wResultFilesGroup, SWT.RIGHT );
    wlTypes.setText( BaseMessages.getString( PKG, "JobMail.SelectFileTypes.Label" ) );
    props.setLook( wlTypes );
    FormData fdlTypes = new FormData();
    fdlTypes.left = new FormAttachment( 0, 0 );
    fdlTypes.top = new FormAttachment( wIncludeFiles, margin );
    fdlTypes.right = new FormAttachment( middle, -margin );
    wlTypes.setLayoutData( fdlTypes );
    wTypes = new List( wResultFilesGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    props.setLook( wTypes );
    FormData fdTypes = new FormData();
    fdTypes.left = new FormAttachment( middle, margin );
    fdTypes.top = new FormAttachment( wIncludeFiles, margin );
    fdTypes.bottom = new FormAttachment( wIncludeFiles, margin + 150 );
    fdTypes.right = new FormAttachment( 100, 0 );
    wTypes.setLayoutData( fdTypes );
    for ( int i = 0; i < ResultFile.getAllTypeDesc().length; i++ ) {
      wTypes.add( ResultFile.getAllTypeDesc()[i] );
    }

    // Zip Files?
    wlZipFiles = new Label( wResultFilesGroup, SWT.RIGHT );
    wlZipFiles.setText( BaseMessages.getString( PKG, "JobMail.ZipFiles.Label" ) );
    props.setLook( wlZipFiles );
    FormData fdlZipFiles = new FormData();
    fdlZipFiles.left = new FormAttachment( 0, 0 );
    fdlZipFiles.top = new FormAttachment( wTypes, margin );
    fdlZipFiles.right = new FormAttachment( middle, -margin );
    wlZipFiles.setLayoutData( fdlZipFiles );
    wZipFiles = new Button( wResultFilesGroup, SWT.CHECK );
    props.setLook( wZipFiles );
    FormData fdZipFiles = new FormData();
    fdZipFiles.left = new FormAttachment( middle, margin );
    fdZipFiles.top = new FormAttachment( wTypes, margin );
    fdZipFiles.right = new FormAttachment( 100, 0 );
    wZipFiles.setLayoutData( fdZipFiles );
    wZipFiles.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
        setFlags();
      }
    } );

    // ZipFilename line
    wZipFilename =
      new LabelTextVar(
        jobMeta, wResultFilesGroup, BaseMessages.getString( PKG, "JobMail.ZipFilename.Label" ), BaseMessages
          .getString( PKG, "JobMail.ZipFilename.Tooltip" ) );
    wZipFilename.addModifyListener( lsMod );
    FormData fdZipFilename = new FormData();
    fdZipFilename.left = new FormAttachment( 0, 0 );
    fdZipFilename.top = new FormAttachment( wZipFiles, margin );
    fdZipFilename.right = new FormAttachment( 100, 0 );
    wZipFilename.setLayoutData( fdZipFilename );

    FormData fdResultFilesGroup = new FormData();
    fdResultFilesGroup.left = new FormAttachment( 0, margin );
    fdResultFilesGroup.top = new FormAttachment( 0, margin );
    // fdResultFilesGroup.bottom = new FormAttachment(100, -margin);
    fdResultFilesGroup.right = new FormAttachment( 100, -margin );
    wResultFilesGroup.setLayoutData( fdResultFilesGroup );

    // //////////////////////////////////////
    // / END OF RESULT FILES GROUP
    // ///////////////////////////////////////

    // ////////////////////////////////////
    // START OF Embedded Images GROUP
    // ////////////////////////////////////

    Group wEmbeddedImagesGroup = new Group( wAttachedComp, SWT.SHADOW_NONE );
    props.setLook( wEmbeddedImagesGroup );
    wEmbeddedImagesGroup.setText( BaseMessages.getString( PKG, "JobMail.Group.EmbeddedImages.Label" ) );

    FormLayout attachedImagesGroupLayout = new FormLayout();
    attachedImagesGroupLayout.marginWidth = 10;
    attachedImagesGroupLayout.marginHeight = 10;
    wEmbeddedImagesGroup.setLayout( attachedImagesGroupLayout );

    // ImageFilename line
    wlImageFilename = new Label( wEmbeddedImagesGroup, SWT.RIGHT );
    wlImageFilename.setText( BaseMessages.getString( PKG, "JobMail.ImageFilename.Label" ) );
    props.setLook( wlImageFilename );
    FormData fdlImageFilename = new FormData();
    fdlImageFilename.left = new FormAttachment( 0, 0 );
    fdlImageFilename.top = new FormAttachment( wResultFilesGroup, margin );
    fdlImageFilename.right = new FormAttachment( middle, -margin );
    wlImageFilename.setLayoutData( fdlImageFilename );

    wbImageFilename = new Button( wEmbeddedImagesGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbImageFilename );
    wbImageFilename.setText( BaseMessages.getString( PKG, "JobMail.BrowseFiles.Label" ) );
    FormData fdbImageFilename = new FormData();
    fdbImageFilename.right = new FormAttachment( 100, 0 );
    fdbImageFilename.top = new FormAttachment( wResultFilesGroup, margin );
    fdbImageFilename.right = new FormAttachment( 100, -margin );
    wbImageFilename.setLayoutData( fdbImageFilename );

    wbAddImageFilename = new Button( wEmbeddedImagesGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbAddImageFilename );
    wbAddImageFilename.setText( BaseMessages.getString( PKG, "JobMail.ImageFilenameAdd.Button" ) );
    FormData fdbAddImageFilename = new FormData();
    fdbAddImageFilename.right = new FormAttachment( wbImageFilename, -margin );
    fdbAddImageFilename.top = new FormAttachment( wResultFilesGroup, margin );
    wbAddImageFilename.setLayoutData( fdbAddImageFilename );

    wImageFilename = new TextVar( jobMeta, wEmbeddedImagesGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wImageFilename );
    wImageFilename.addModifyListener( lsMod );
    FormData fdImageFilename = new FormData();
    fdImageFilename.left = new FormAttachment( middle, 0 );
    fdImageFilename.top = new FormAttachment( wResultFilesGroup, margin );
    fdImageFilename.right = new FormAttachment( wbImageFilename, -40 );
    wImageFilename.setLayoutData( fdImageFilename );

    // Whenever something changes, set the tooltip to the expanded version:
    wImageFilename.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        wImageFilename.setToolTipText( jobMeta.environmentSubstitute( wImageFilename.getText() ) );
      }
    } );

    wbImageFilename.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN );
        dialog.setFilterExtensions( new String[] { "*png;*PNG", "*jpeg;*jpg;*JPEG;*JPG", "*gif;*GIF", "*" } );
        if ( wImageFilename.getText() != null ) {
          dialog.setFileName( jobMeta.environmentSubstitute( wImageFilename.getText() ) );
        }
        dialog.setFilterNames( IMAGES_FILE_TYPES );
        if ( dialog.open() != null ) {
          wImageFilename.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
          wContentID.setText( Long.toString( Math.abs( random.nextLong() ), 32 ) );
        }
      }
    } );

    // ContentID
    wlContentID = new Label( wEmbeddedImagesGroup, SWT.RIGHT );
    wlContentID.setText( BaseMessages.getString( PKG, "JobMail.ContentID.Label" ) );
    props.setLook( wlContentID );
    FormData fdlContentID = new FormData();
    fdlContentID.left = new FormAttachment( 0, 0 );
    fdlContentID.top = new FormAttachment( wImageFilename, margin );
    fdlContentID.right = new FormAttachment( middle, -margin );
    wlContentID.setLayoutData( fdlContentID );
    wContentID =
      new TextVar( jobMeta, wEmbeddedImagesGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
        PKG, "JobMail.ContentID.Tooltip" ) );
    props.setLook( wContentID );
    wContentID.addModifyListener( lsMod );
    FormData fdContentID = new FormData();
    fdContentID.left = new FormAttachment( middle, 0 );
    fdContentID.top = new FormAttachment( wImageFilename, margin );
    fdContentID.right = new FormAttachment( wbImageFilename, -40 );
    wContentID.setLayoutData( fdContentID );

    // Buttons to the right of the screen...
    wbDeleteImageFilename = new Button( wEmbeddedImagesGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDeleteImageFilename );
    wbDeleteImageFilename.setText( BaseMessages.getString( PKG, "JobMail.ImageFilenameDelete.Button" ) );
    wbDeleteImageFilename.setToolTipText( BaseMessages.getString( PKG, "JobMail.ImageFilenameDelete.Tooltip" ) );
    FormData fdbDeleteImageFilename = new FormData();
    fdbDeleteImageFilename.right = new FormAttachment( 100, 0 );
    fdbDeleteImageFilename.top = new FormAttachment( wContentID, 40 );
    wbDeleteImageFilename.setLayoutData( fdbDeleteImageFilename );

    wbEditImageFilename = new Button( wEmbeddedImagesGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbEditImageFilename );
    wbEditImageFilename.setText( BaseMessages.getString( PKG, "JobMail.ImageFilenameEdit.Button" ) );
    wbEditImageFilename.setToolTipText( BaseMessages.getString( PKG, "JobMail.ImageFilenameEdit.Tooltip" ) );
    FormData fdbEditImageFilename = new FormData();
    fdbEditImageFilename.right = new FormAttachment( 100, 0 );
    fdbEditImageFilename.left = new FormAttachment( wbDeleteImageFilename, 0, SWT.LEFT );
    fdbEditImageFilename.top = new FormAttachment( wbDeleteImageFilename, margin );
    wbEditImageFilename.setLayoutData( fdbEditImageFilename );

    wlFields = new Label( wEmbeddedImagesGroup, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "JobMail.Fields.Label" ) );
    props.setLook( wlFields );
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.right = new FormAttachment( middle, -margin );
    fdlFields.top = new FormAttachment( wContentID, margin );
    wlFields.setLayoutData( fdlFields );

    final int fieldsRows =
      jobEntry.embeddedimages == null ? 1 : ( jobEntry.embeddedimages.length == 0
        ? 0 : jobEntry.embeddedimages.length );

    ColumnInfo[] colInf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobMail.Fields.Image.Label" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobMail.Fields.ContentID.Label" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    colInf[ 0 ].setUsingVariables( true );
    colInf[ 0 ].setToolTip( BaseMessages.getString( PKG, "JobMail.Fields.Image.Tooltip" ) );
    colInf[ 1 ].setUsingVariables( true );
    colInf[ 1 ].setToolTip( BaseMessages.getString( PKG, "JobMail.Fields.ContentID.Tooltip" ) );

    wFields =
      new TableView(
        jobMeta, wEmbeddedImagesGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colInf, fieldsRows, lsMod,
        props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( wbEditImageFilename, -margin );
    fdFields.bottom = new FormAttachment( 100, -margin );
    wFields.setLayoutData( fdFields );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        wFields.add( wImageFilename.getText(), wContentID.getText() );
        wImageFilename.setText( "" );
        wContentID.setText( "" );
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth( true );
      }
    };
    wbAddImageFilename.addSelectionListener( selA );
    wImageFilename.addSelectionListener( selA );

    // Delete files from the list of files...
    wbDeleteImageFilename.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFields.getSelectionIndices();
        wFields.remove( idx );
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbEditImageFilename.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        int idx = wFields.getSelectionIndex();
        if ( idx >= 0 ) {
          String[] string = wFields.getItem( idx );
          wImageFilename.setText( string[0] );
          wContentID.setText( string[1] );
          wFields.remove( idx );
        }
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    FormData fdEmbeddedImagesGroup = new FormData();
    fdEmbeddedImagesGroup.left = new FormAttachment( 0, margin );
    fdEmbeddedImagesGroup.top = new FormAttachment( wResultFilesGroup, margin );
    fdEmbeddedImagesGroup.bottom = new FormAttachment( 100, -margin );
    fdEmbeddedImagesGroup.right = new FormAttachment( 100, -margin );
    wEmbeddedImagesGroup.setLayoutData( fdEmbeddedImagesGroup );

    // //////////////////////////////////////
    // / END OF Embedded Images GROUP
    // ///////////////////////////////////////

    FormData fdAttachedComp = new FormData();
    fdAttachedComp.left = new FormAttachment( 0, 0 );
    fdAttachedComp.top = new FormAttachment( 0, 0 );
    fdAttachedComp.right = new FormAttachment( 100, 0 );
    fdAttachedComp.bottom = new FormAttachment( 100, 0 );
    wAttachedComp.setLayoutData( wAttachedComp );

    wAttachedComp.layout();
    wAttachedTab.setControl( wAttachedComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILES TAB
    // ///////////////////////////////////////////////////////////

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wName, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // Some buttons
    Button wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    Button wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners
    wOK.addListener( SWT.Selection, e -> ok() );
    wCancel.addListener( SWT.Selection, e -> cancel() );

    SelectionAdapter lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wName.addSelectionListener( lsDef );
    wServer.addSelectionListener( lsDef );
    wSubject.addSelectionListener( lsDef );
    wDestination.addSelectionListener( lsDef );
    wDestinationCc.addSelectionListener( lsDef );
    wDestinationBCc.addSelectionListener( lsDef );
    wReply.addSelectionListener( lsDef );
    wPerson.addSelectionListener( lsDef );
    wPhone.addSelectionListener( lsDef );
    wZipFilename.addSelectionListener( lsDef );

    // Detect [X] or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    SetEnabledEncoding();
    activeUsePriority();
    setFlags();
    setUseAuth();
    setUseGrantType();
    BaseStepDialog.setSize( shell );

    shell.open();
    props.setDialogSize( shell, "JobMailDialogSize" );
    wTabFolder.setSelection( 0 );
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  private void activeUsePriority() {
    wlPriority.setEnabled( wUsePriority.getSelection() );
    wPriority.setEnabled( wUsePriority.getSelection() );
    wlImportance.setEnabled( wUsePriority.getSelection() );
    wImportance.setEnabled( wUsePriority.getSelection() );
    wlSensitivity.setEnabled( wUsePriority.getSelection() );
    wSensitivity.setEnabled( wUsePriority.getSelection() );
  }

  private void SetEnabledEncoding() {
    wEncoding.setEnabled( wUseHTML.getSelection() );
    wlEncoding.setEnabled( wUseHTML.getSelection() );
    wlImageFilename.setEnabled( wUseHTML.getSelection() );
    wbImageFilename.setEnabled( wUseHTML.getSelection() );
    wbAddImageFilename.setEnabled( wUseHTML.getSelection() );
    wImageFilename.setEnabled( wUseHTML.getSelection() );
    wlContentID.setEnabled( wUseHTML.getSelection() );
    wContentID.setEnabled( wUseHTML.getSelection() );
    wbDeleteImageFilename.setEnabled( wUseHTML.getSelection() );
    wbEditImageFilename.setEnabled( wUseHTML.getSelection() );
    wlFields.setEnabled( wUseHTML.getSelection() );
    wFields.setEnabled( wUseHTML.getSelection() );
  }

  protected void setSecureConnectiontype() {
    wSecureConnectionType.setEnabled( wUseSecAuth.getSelection() );
    wlSecureConnectionType.setEnabled( wUseSecAuth.getSelection() );
  }

  protected void setFlags() {
    wlTypes.setEnabled( wIncludeFiles.getSelection() );
    wTypes.setEnabled( wIncludeFiles.getSelection() );
    wlZipFiles.setEnabled( wIncludeFiles.getSelection() );
    wZipFiles.setEnabled( wIncludeFiles.getSelection() );
    wZipFilename.setEnabled( wIncludeFiles.getSelection() && wZipFiles.getSelection() );
  }

  protected void setUseAuth() {
    String selectedAuth = wUseAuth.getText();

    if ( JobEntryMail.AUTENTICATION_NONE.equals( selectedAuth ) ) {
      wAuthClientId.setEnabled( false );
      wAuthSecretKey.setEnabled( false );
      wAuthUser.setEnabled( false );
      wAuthPass.setEnabled( false );
      wAuthScope.setEnabled( false );
      wlGrantType.setEnabled( false );
      grantType.setEnabled( false );
      wAuthTokenUrl.setEnabled( false );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
      wlUseSecAuth.setEnabled( false );
      wUseSecAuth.setEnabled( false );
      wlSecureConnectionType.setEnabled( false );
      wSecureConnectionType.setEnabled( false );
    } else if ( JobEntryMail.AUTENTICATION_BASIC.equals( selectedAuth ) ) {
      wAuthClientId.setEnabled( false );
      wAuthSecretKey.setEnabled( false );
      wAuthUser.setEnabled( true );
      wAuthPass.setEnabled( true );
      wAuthScope.setEnabled( false );
      wlGrantType.setEnabled( false );
      grantType.setEnabled( false );
      wAuthTokenUrl.setEnabled( false );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
      wlUseSecAuth.setEnabled( true );
      wUseSecAuth.setEnabled( true );
      setSecureConnectiontype();
    } else if ( JobEntryMail.AUTENTICATION_OAUTH.equals( selectedAuth ) ) {
      wAuthClientId.setEnabled( true );
      wAuthSecretKey.setEnabled( true );
      wAuthUser.setEnabled( true );
      wAuthPass.setEnabled( false );
      wAuthScope.setEnabled( true );
      wlGrantType.setEnabled( true );
      grantType.setEnabled( true );
      wlUseSecAuth.setEnabled( true );
      wUseSecAuth.setEnabled( true );
      setSecureConnectiontype();
    } else {
      setSecureConnectiontype();
    }
  }

  protected void setUseGrantType() {
    String selectedAuth = grantType.getText();

    if ( selectedAuth.equals( JobEntryMail.GRANTTYPE_CLIENTCREDENTIALS ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
    } else if ( selectedAuth.equals( JobEntryMail.GRANTTYPE_REFRESH_TOKEN ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( true );
    } else if ( selectedAuth.equals( JobEntryMail.GRANTTYPE_AUTHORIZATION_CODE ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( true );
      wRedirectUri.setEnabled( true );
      wAuthRefreshToken.setEnabled( false );
    } else {
      setSecureConnectiontype();
    }
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void getData() {
    wName.setText( Const.nullToEmpty( jobEntry.getName() ) );
    wDestination.setText( Const.nullToEmpty( jobEntry.getDestination() ) );
    wDestinationCc.setText( Const.nullToEmpty( jobEntry.getDestinationCc() ) );
    wDestinationBCc.setText( Const.nullToEmpty( jobEntry.getDestinationBCc() ) );
    wServer.setText( Const.nullToEmpty( jobEntry.getServer() ) );
    wPort.setText( Const.nullToEmpty( jobEntry.getPort() ) );
    wReply.setText( Const.nullToEmpty( jobEntry.getReplyAddress() ) );
    wReplyName.setText( Const.nullToEmpty( jobEntry.getReplyName() ) );
    wSubject.setText( Const.nullToEmpty( jobEntry.getSubject() ) );
    wPerson.setText( Const.nullToEmpty( jobEntry.getContactPerson() ) );
    wPhone.setText( Const.nullToEmpty( jobEntry.getContactPhone() ) );
    wComment.setText( Const.nullToEmpty( jobEntry.getComment() ) );

    wAddDate.setSelection( jobEntry.getIncludeDate() );
    wIncludeFiles.setSelection( jobEntry.isIncludingFiles() );

    if ( jobEntry.getFileType() != null ) {
      int[] types = jobEntry.getFileType();
      wTypes.setSelection( types );
    }

    wZipFiles.setSelection( jobEntry.isZipFiles() );
    wZipFilename.setText( Const.nullToEmpty( jobEntry.getZipFilename() ) );

    wUseSecAuth.setSelection( jobEntry.isUsingSecureAuthentication() );
    wAuthUser.setText( Const.nullToEmpty( jobEntry.getAuthenticationUser() ) );
    wAuthPass.setText( Const.nullToEmpty( jobEntry.getAuthenticationPassword() ) );
    wAuthClientId.setText( Const.nullToEmpty( jobEntry.getClientId() ) );
    wAuthSecretKey.setText( Const.nullToEmpty( jobEntry.getSecretKey() ) );
    wAuthScope.setText( Const.nullToEmpty( jobEntry.getScope() ) );
    wAuthTokenUrl.setText( Const.nullToEmpty( jobEntry.getTokenUrl() ) );
    wAuthorizationCode.setText( Const.nullToEmpty( jobEntry.getAuthorization_code() ) );
    wRedirectUri.setText( Const.nullToEmpty( jobEntry.getRedirectUri() ) );
    wAuthRefreshToken.setText( Const.nullToEmpty( jobEntry.getRefresh_token() ) );
    wOnlyComment.setSelection( jobEntry.isOnlySendComment() );
    grantType.setText( Const.nullToEmpty( jobEntry.getGrant_type() ) );
    wUseAuth.setText( Const.nullToEmpty( jobEntry.isUsingAuthentication() ) );

    wUseHTML.setSelection( jobEntry.isUseHTML() );

    if ( jobEntry.getEncoding() != null ) {
      wEncoding.setText( jobEntry.getEncoding() );
    } else {
      wEncoding.setText( "UTF-8" );
    }

    // Secure connection type
    if ( jobEntry.getSecureConnectionType() != null ) {
      wSecureConnectionType.setText( jobEntry.getSecureConnectionType() );
    } else {
      wSecureConnectionType.setText( "SSL" );
    }
    wUsePriority.setSelection( jobEntry.isUsePriority() );

    // Priority

    if ( jobEntry.getPriority() != null ) {
      if ( jobEntry.getPriority().equals( "low" ) ) {
        wPriority.select( 0 ); // Low
      } else if ( jobEntry.getPriority().equals( "normal" ) ) {
        wPriority.select( 1 ); // Normal
      } else {
        wPriority.select( 2 ); // Default High
      }
    } else {
      wPriority.select( 3 ); // Default High
    }

    // Importance
    if ( jobEntry.getImportance() != null ) {
      if ( jobEntry.getImportance().equals( "low" ) ) {
        wImportance.select( 0 ); // Low
      } else if ( jobEntry.getImportance().equals( "normal" ) ) {
        wImportance.select( 1 ); // Normal
      } else {
        wImportance.select( 2 ); // Default High
      }
    } else {
      wImportance.select( 3 ); // Default High
    }

    if ( jobEntry.getReplyToAddresses() != null ) {
      wReplyToAddress.setText( jobEntry.getReplyToAddresses() );
    }

    // Sensitivity
    if ( jobEntry.getSensitivity() != null ) {
      if ( jobEntry.getSensitivity().equals( "personal" ) ) {
        wSensitivity.select( 1 );
      } else if ( jobEntry.getSensitivity().equals( "private" ) ) {
        wSensitivity.select( 2 );
      } else if ( jobEntry.getSensitivity().equals( "company-confidential" ) ) {
        wSensitivity.select( 3 );
      } else {
        wSensitivity.select( 0 );
      }
    } else {
      wSensitivity.select( 0 ); // Default normal
    }

    if ( jobEntry.embeddedimages != null ) {
      for ( int i = 0; i < jobEntry.embeddedimages.length; i++ ) {
        TableItem ti = wFields.table.getItem( i );
        if ( jobEntry.embeddedimages[i] != null ) {
          ti.setText( 1, jobEntry.embeddedimages[i] );
        }
        if ( jobEntry.contentids[i] != null ) {
          ti.setText( 2, jobEntry.contentids[i] );
        }
      }
      wFields.setRowNums();
      wFields.optWidth( true );
    }

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    jobEntry.setChanged( backupChanged );
    jobEntry.setIncludeDate( backupDate );

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
    jobEntry.setDestination( wDestination.getText() );
    jobEntry.setDestinationCc( wDestinationCc.getText() );
    jobEntry.setDestinationBCc( wDestinationBCc.getText() );
    jobEntry.setServer( wServer.getText() );
    jobEntry.setPort( wPort.getText() );
    jobEntry.setReplyAddress( wReply.getText() );
    jobEntry.setReplyName( wReplyName.getText() );
    jobEntry.setSubject( wSubject.getText() );
    jobEntry.setContactPerson( wPerson.getText() );
    jobEntry.setContactPhone( wPhone.getText() );
    jobEntry.setClientId( wAuthClientId.getText() );
    jobEntry.setSecretKey( wAuthSecretKey.getText() );
    jobEntry.setScope( wAuthScope.getText() );
    jobEntry.setTokenUrl( wAuthTokenUrl.getText() );
    jobEntry.setAuthorization_code( wAuthorizationCode.getText() );
    jobEntry.setRedirectUri( wRedirectUri.getText() );
    jobEntry.setRefresh_token( wAuthRefreshToken.getText() );
    jobEntry.setComment( wComment.getText() );

    jobEntry.setIncludeDate( wAddDate.getSelection() );
    jobEntry.setIncludingFiles( wIncludeFiles.getSelection() );
    jobEntry.setFileType( wTypes.getSelectionIndices() );
    jobEntry.setZipFilename( wZipFilename.getText() );
    jobEntry.setZipFiles( wZipFiles.getSelection() );
    jobEntry.setAuthenticationUser( wAuthUser.getText() );
    jobEntry.setAuthenticationPassword( wAuthPass.getText() );
    jobEntry.setUsingAuthentication( wUseAuth.getText() );
    jobEntry.setGrant_type( grantType.getText() );
    jobEntry.setUsingSecureAuthentication( wUseSecAuth.getSelection() );
    jobEntry.setOnlySendComment( wOnlyComment.getSelection() );
    jobEntry.setUseHTML( wUseHTML.getSelection() );
    jobEntry.setUsePriority( wUsePriority.getSelection() );

    jobEntry.setEncoding( wEncoding.getText() );
    jobEntry.setPriority( wPriority.getText() );

    // Priority
    if ( wPriority.getSelectionIndex() == 0 ) {
      jobEntry.setPriority( "low" );
    } else if ( wPriority.getSelectionIndex() == 1 ) {
      jobEntry.setPriority( "normal" );
    } else {
      jobEntry.setPriority( "high" );
    }

    // Importance
    if ( wImportance.getSelectionIndex() == 0 ) {
      jobEntry.setImportance( "low" );
    } else if ( wImportance.getSelectionIndex() == 1 ) {
      jobEntry.setImportance( "normal" );
    } else {
      jobEntry.setImportance( "high" );
    }

    // Sensitivity
    if ( wSensitivity.getSelectionIndex() == 1 ) {
      jobEntry.setSensitivity( "personal" );
    } else if ( wSensitivity.getSelectionIndex() == 2 ) {
      jobEntry.setSensitivity( "private" );
    } else if ( wSensitivity.getSelectionIndex() == 3 ) {
      jobEntry.setSensitivity( "company-confidential" );
    } else {
      jobEntry.setSensitivity( "normal" ); // default is normal
    }

    // Secure Connection type
    jobEntry.setSecureConnectionType( wSecureConnectionType.getText() );

    jobEntry.setReplyToAddresses( wReplyToAddress.getText() );

    int nrItems = wFields.nrNonEmpty();
    int nr = 0;
    for ( int i = 0; i < nrItems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      if ( arg != null && !arg.isEmpty() ) {
        nr++;
      }
    }
    jobEntry.embeddedimages = new String[nr];
    jobEntry.contentids = new String[nr];
    nr = 0;
    for ( int i = 0; i < nrItems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      String wild = wFields.getNonEmpty( i ).getText( 2 );
      if ( arg != null && !arg.isEmpty() ) {
        jobEntry.embeddedimages[nr] = arg;
        jobEntry.contentids[nr] = wild;
        nr++;
      }
    }

    dispose();
  }

  private void setEncodings() {
    // Encoding of the text file:
    if ( !gotEncodings ) {
      gotEncodings = true;

      wEncoding.removeAll();
      java.util.List<Charset> values = new ArrayList<>( Charset.availableCharsets().values() );
      for ( Charset charSet : values ) {
        wEncoding.add( charSet.displayName() );
      }

      // Now select the default!
      String defEncoding = Const.getEnvironmentVariable( "file.encoding", "UTF-8" );
      int idx = Const.indexOfString( defEncoding, wEncoding.getItems() );
      if ( idx >= 0 ) {
        wEncoding.select( idx );
      }
    }
  }
}
