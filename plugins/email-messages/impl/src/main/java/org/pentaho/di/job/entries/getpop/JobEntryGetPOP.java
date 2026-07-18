/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.job.entries.getpop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Flags.Flag;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.http.Consts;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This defines a get pop job entry.
 *
 * @author Samatar
 * @since 01-03-2007
 *
 */
@JobEntry( id = "GET_POP", name = "JobEntry.GetPOP.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.getpop",
        description = "JobEntry.GetPOP.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Mail",
        image = "ui/images/GETPOP.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Get+Mails+from+POP" )
public class JobEntryGetPOP extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryGetPOP.class; // for i18n purposes, needed by Translator2!!

  private static final String TAG_SERVERNAME = "servername";
  private static final String TAG_USERNAME = "username";
  private static final String TAG_PASSWORD = "password";
  private static final String TAG_USE_SSL = "usessl";
  private static final String TAG_SSL_PORT = "sslport";
  private static final String TAG_OUTPUT_DIRECTORY = "outputdirectory";
  private static final String TAG_FILENAME_PATTERN = "filenamepattern";
  private static final String TAG_RETRIEVE_MAILS = "retrievemails";
  private static final String TAG_FIRST_MAILS = "firstmails";
  private static final String TAG_DELETE = "delete";
  private static final String TAG_SAVE_MESSAGE = "savemessage";
  private static final String TAG_SAVE_ATTACHMENT = "saveattachment";
  private static final String TAG_USE_DIFFERENT_FOLDER_FOR_ATTACHMENT = "usedifferentfolderforattachment";
  private static final String TAG_PROTOCOL = "protocol";
  private static final String TAG_ATTACHMENT_FOLDER = "attachmentfolder";
  private static final String TAG_ATTACHMENT_WILDCARD = "attachmentwildcard";
  private static final String TAG_VALUE_IMAP_LIST = "valueimaplist";
  private static final String TAG_IMAP_FIRST_MAILS = "imapfirstmails";
  private static final String TAG_IMAP_FOLDER = "imapfolder";
  private static final String TAG_SENDER_SEARCH = "sendersearch";
  private static final String TAG_NOT_TERM_SENDER_SEARCH = "nottermsendersearch";
  private static final String TAG_RECIPIENT_SEARCH = "receipientsearch";
  private static final String TAG_NOT_TERM_RECIPIENT_SEARCH = "nottermreceipientsearch";
  private static final String TAG_SUBJECT_SEARCH = "subjectsearch";
  private static final String TAG_NOT_TERM_SUBJECT_SEARCH = "nottermsubjectsearch";
  private static final String TAG_BODY_SEARCH = "bodysearch";
  private static final String TAG_NOT_TERM_BODY_SEARCH = "nottermbodysearch";
  private static final String TAG_CONDITION_RECEIVED_DATE = "conditionreceiveddate";
  private static final String TAG_NOT_TERM_RECEIVED_DATE_SEARCH = "nottermreceiveddatesearch";
  private static final String TAG_RECEIVED_DATE_1 = "receiveddate1";
  private static final String TAG_RECEIVED_DATE_2 = "receiveddate2";
  private static final String TAG_ACTION_TYPE = "actiontype";
  private static final String TAG_MOVE_TO_IMAP_FOLDER = "movetoimapfolder";
  private static final String TAG_CREATE_MOVE_TO_FOLDER = "createmovetofolder";
  private static final String TAG_CREATE_LOCAL_FOLDER = "createlocalfolder";
  private static final String TAG_AFTER_GET_IMAP = "aftergetimap";
  private static final String TAG_INCLUDE_SUBFOLDERS = "includesubfolders";
  private static final String TAG_USE_PROXY = "useproxy";
  private static final String TAG_PROXY_USER_NAME = "proxyusername";
  private static final String TAG_USE_AUTH = "use_auth";
  private static final String TAG_USE_GRANT_TYPE = "use_grantType";
  private static final String TAG_AUTH_CLIENT_ID = "auth_clientId";
  private static final String TAG_AUTH_SECRET_KEY = "auth_secretKey";
  private static final String TAG_AUTH_SCOPE = "auth_scope";
  private static final String TAG_AUTH_TOKEN_URL = "auth_tokenUrl";
  private static final String TAG_AUTH_AUTHORIZATION_CODE = "auth_authorizationCode";
  private static final String TAG_REDIRECT_URI = "redirectURI";
  private static final String TAG_REFRESH_TOKEN = "refreshToken";

  private static final String STR_6_SPACES = "      ";
  private static final String STR_Y = "Y";

  static final int FOLDER_OUTPUT = 0;
  static final int FOLDER_ATTACHMENTS = 1;

  public int actiontype;

  public int conditionReceivedDate;

  public int valueimaplist;

  public int aftergetimap;

  private String serverName;
  private String userName;
  private String password;
  public static String AUTENTICATION_OAUTH = "OAUTH";
  public static String AUTENTICATION_BASIC = "Basic";
  public static String GRANTTYPE_CLIENTCREDENTIALS = "client_credentials";
  public static String GRANTTYPE_AUTHORIZATION_CODE = "authorization_code";
  public static String GRANTTYPE_REFRESH_TOKEN = "refresh_token";
  private String clientId;
  private String secretKey;
  private String scope;
  private String tokenUrl;
  private String authorizationCode;
  private String redirectUri;
  private String refreshToken;
  private String grantType;
  private String usingAuthentication;
  private boolean useSsl;
  private String sslPort;
  private boolean useProxy;
  private String proxyUserName;
  private String outputDirectory;
  private String filenamePattern;
  private String firstMails;
  public int retrieveMails;
  private boolean delete;
  private String protocol;
  private boolean saveAttachment;
  private boolean saveMessage;
  private boolean useDifferentFolderForAttachment;
  private String attachmentFolder;
  private String attachmentWildcard;
  private String imapFirstMails;
  private String imapFolder;
  // search term
  private String senderSearch;
  private boolean notTermSenderSearch;
  private String recipientSearch;
  private String subjectSearch;
  private String bodySearch;
  private boolean notTermBodySearch;
  private String receivedDate1;
  private String receivedDate2;
  private boolean notTermSubjectSearch;
  private boolean notTermRecipientSearch;
  private boolean notTermReceivedDateSearch;
  private boolean includeSubfolders;
  private String moveToIMAPFolder;
  private boolean createMoveToFolder;
  private boolean createLocalFolder;

  private static final String DEFAULT_FILE_NAME_PATTERN = "name_{SYS|hhmmss_MMddyyyy|}_#IdFile#.mail";

  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static final String FILENAME_ID_PATTERN = "#IdFile#";
  private static final String FILENAME_SYS_DATE_OPEN = "{SYS|";
  private static final String FILENAME_SYS_DATE_CLOSE = "|}";

  private Pattern attachementPattern;

  public JobEntryGetPOP( String n ) {
    super( n, "" );
    serverName = null;
    userName = null;
    password = null;
    useSsl = false;
    sslPort = null;
    useProxy = false;
    proxyUserName = null;
    outputDirectory = null;
    filenamePattern = DEFAULT_FILE_NAME_PATTERN;
    retrieveMails = 0;
    firstMails = null;
    delete = false;
    protocol = MailConnectionMeta.PROTOCOL_STRING_POP3;
    saveAttachment = true;
    saveMessage = true;
    useDifferentFolderForAttachment = false;
    attachmentFolder = null;
    attachmentWildcard = null;
    imapFirstMails = "0";
    valueimaplist = MailConnectionMeta.VALUE_IMAP_LIST_ALL;
    imapFolder = null;
    // search term
    senderSearch = null;
    notTermSenderSearch = false;
    notTermRecipientSearch = false;
    notTermSubjectSearch = false;
    bodySearch = null;
    notTermBodySearch = false;
    receivedDate1 = null;
    receivedDate2 = null;
    notTermReceivedDateSearch = false;
    recipientSearch = null;
    subjectSearch = null;
    actiontype = MailConnectionMeta.ACTION_TYPE_GET;
    moveToIMAPFolder = null;
    createMoveToFolder = false;
    createLocalFolder = false;
    aftergetimap = MailConnectionMeta.AFTER_GET_IMAP_NOTHING;
    includeSubfolders = false;
  }

  public JobEntryGetPOP() {
    this( "" );
  }

  @Override
  public Object clone() {
    return super.clone();
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 550 );

    retval.append( super.getXML() );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SERVERNAME, serverName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USERNAME, userName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PASSWORD,
        Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_SSL, useSsl ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SSL_PORT, sslPort ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_OUTPUT_DIRECTORY, outputDirectory ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_FILENAME_PATTERN, filenamePattern ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_RETRIEVE_MAILS, retrieveMails ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_FIRST_MAILS, firstMails ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DELETE, delete ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SAVE_MESSAGE, saveMessage ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SAVE_ATTACHMENT, saveAttachment ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_DIFFERENT_FOLDER_FOR_ATTACHMENT,
        useDifferentFolderForAttachment ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PROTOCOL, protocol ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ATTACHMENT_FOLDER, attachmentFolder ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ATTACHMENT_WILDCARD, attachmentWildcard ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_VALUE_IMAP_LIST,
        MailConnectionMeta.getValueImapListCode( valueimaplist ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_IMAP_FIRST_MAILS, imapFirstMails ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_IMAP_FOLDER, imapFolder ) );
    // search term
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SENDER_SEARCH, senderSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_SENDER_SEARCH, notTermSenderSearch ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_RECIPIENT_SEARCH, recipientSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_RECIPIENT_SEARCH,
          notTermRecipientSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SUBJECT_SEARCH, subjectSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_SUBJECT_SEARCH, notTermSubjectSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_BODY_SEARCH, bodySearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_BODY_SEARCH, notTermBodySearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_CONDITION_RECEIVED_DATE,
        MailConnectionMeta.getConditionDateCode( conditionReceivedDate ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_RECEIVED_DATE_SEARCH,
        notTermReceivedDateSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_RECEIVED_DATE_1, receivedDate1 ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_RECEIVED_DATE_2, receivedDate2 ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ACTION_TYPE,
        MailConnectionMeta.getActionTypeCode( actiontype ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_MOVE_TO_IMAP_FOLDER, moveToIMAPFolder ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_CREATE_MOVE_TO_FOLDER, createMoveToFolder ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_CREATE_LOCAL_FOLDER, createLocalFolder ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AFTER_GET_IMAP,
        MailConnectionMeta.getAfterGetIMAPCode( aftergetimap ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_SUBFOLDERS, includeSubfolders ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_PROXY, useProxy ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PROXY_USER_NAME, proxyUserName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_AUTH, usingAuthentication ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_GRANT_TYPE, grantType ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_CLIENT_ID, clientId ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_SECRET_KEY, secretKey)  );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_SCOPE, scope ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_TOKEN_URL, tokenUrl ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_AUTHORIZATION_CODE, authorizationCode ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REDIRECT_URI, redirectUri ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REFRESH_TOKEN, refreshToken ) );
    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      serverName = XMLHandler.getTagValue( entrynode, TAG_SERVERNAME );
      userName = XMLHandler.getTagValue( entrynode, TAG_USERNAME );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, TAG_PASSWORD ) );
      useSsl = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_USE_SSL ) );
      sslPort = XMLHandler.getTagValue( entrynode, TAG_SSL_PORT );
      outputDirectory = XMLHandler.getTagValue( entrynode, TAG_OUTPUT_DIRECTORY );
      filenamePattern = XMLHandler.getTagValue( entrynode, TAG_FILENAME_PATTERN );
      if ( Utils.isEmpty( filenamePattern ) ) {
        filenamePattern = DEFAULT_FILE_NAME_PATTERN;
      }
      retrieveMails = Const.toInt( XMLHandler.getTagValue( entrynode, TAG_RETRIEVE_MAILS ), -1 );
      firstMails = XMLHandler.getTagValue( entrynode, TAG_FIRST_MAILS );
      delete = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_DELETE ) );

      protocol =
        Const.NVL( XMLHandler.getTagValue( entrynode, TAG_PROTOCOL ), MailConnectionMeta.PROTOCOL_STRING_POP3 );

      String sm = XMLHandler.getTagValue( entrynode, TAG_SAVE_MESSAGE );
      if ( Utils.isEmpty( sm ) ) {
        saveMessage = true;
      } else {
        saveMessage = STR_Y.equalsIgnoreCase( sm );
      }

      String sa = XMLHandler.getTagValue( entrynode, TAG_SAVE_ATTACHMENT );
      if ( Utils.isEmpty( sa ) ) {
        saveAttachment = true;
      } else {
        saveAttachment = STR_Y.equalsIgnoreCase( sa );
      }

      useDifferentFolderForAttachment =
        STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_USE_DIFFERENT_FOLDER_FOR_ATTACHMENT ) );
      attachmentFolder = XMLHandler.getTagValue( entrynode, TAG_ATTACHMENT_FOLDER );
      attachmentWildcard = XMLHandler.getTagValue( entrynode, TAG_ATTACHMENT_WILDCARD );
      valueimaplist =
        MailConnectionMeta.getValueImapListByCode( Const.NVL( XMLHandler
          .getTagValue( entrynode, TAG_VALUE_IMAP_LIST ), "" ) );
      imapFirstMails = XMLHandler.getTagValue( entrynode, TAG_IMAP_FIRST_MAILS );
      imapFolder = XMLHandler.getTagValue( entrynode, TAG_IMAP_FOLDER );
      // search term
      senderSearch = XMLHandler.getTagValue( entrynode, TAG_SENDER_SEARCH );
      notTermSenderSearch = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_NOT_TERM_SENDER_SEARCH ) );
      recipientSearch = XMLHandler.getTagValue( entrynode, TAG_RECIPIENT_SEARCH );
      notTermRecipientSearch =
        STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_NOT_TERM_RECIPIENT_SEARCH ) );
      subjectSearch = XMLHandler.getTagValue( entrynode, TAG_SUBJECT_SEARCH );
      notTermSubjectSearch = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_NOT_TERM_SUBJECT_SEARCH ) );
      bodySearch = XMLHandler.getTagValue( entrynode, TAG_BODY_SEARCH );
      notTermBodySearch = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_NOT_TERM_BODY_SEARCH ) );
      conditionReceivedDate =
        MailConnectionMeta.getConditionByCode( Const.NVL( XMLHandler.getTagValue(
          entrynode, TAG_CONDITION_RECEIVED_DATE ), "" ) );
      notTermReceivedDateSearch =
        STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_NOT_TERM_RECEIVED_DATE_SEARCH ) );
      receivedDate1 = XMLHandler.getTagValue( entrynode, "receivedDate1" );
      receivedDate2 = XMLHandler.getTagValue( entrynode, "receivedDate2" );
      actiontype =
        MailConnectionMeta.getActionTypeByCode( Const
          .NVL( XMLHandler.getTagValue( entrynode, TAG_ACTION_TYPE ), "" ) );
      moveToIMAPFolder = XMLHandler.getTagValue( entrynode, TAG_MOVE_TO_IMAP_FOLDER );
      createMoveToFolder = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_CREATE_MOVE_TO_FOLDER ) );
      createLocalFolder = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_CREATE_LOCAL_FOLDER ) );
      aftergetimap =
        MailConnectionMeta.getAfterGetIMAPByCode( Const.NVL(
          XMLHandler.getTagValue( entrynode, TAG_AFTER_GET_IMAP ), "" ) );
      includeSubfolders = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_INCLUDE_SUBFOLDERS ) );
      useProxy = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_USE_PROXY ) );
      proxyUserName = XMLHandler.getTagValue( entrynode, TAG_PROXY_USER_NAME );
      setUsingAuthentication( XMLHandler.getTagValue( entrynode, TAG_USE_AUTH ) );
      setClientId( XMLHandler.getTagValue( entrynode, TAG_AUTH_CLIENT_ID ) );
      setSecretKey( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, TAG_AUTH_SECRET_KEY ) ) );
      setScope( XMLHandler.getTagValue( entrynode, TAG_AUTH_SCOPE ) );
      setTokenUrl( XMLHandler.getTagValue( entrynode, TAG_AUTH_TOKEN_URL ) );
      setAuthorization_code( XMLHandler.getTagValue( entrynode, TAG_AUTH_AUTHORIZATION_CODE ) );
      setRedirectUri( XMLHandler.getTagValue( entrynode, TAG_REDIRECT_URI ) );
      setRefresh_token( XMLHandler.getTagValue( entrynode, TAG_REFRESH_TOKEN ) );
      setGrant_type( XMLHandler.getTagValue( entrynode, TAG_USE_GRANT_TYPE ) );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'get pop' from XML node", xe );
    }
  }

  public int getValueImapList() {
    return valueimaplist;
  }

  public void setValueImapList( int value ) {
    this.valueimaplist = value;
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId idJobEntry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      serverName = rep.getJobEntryAttributeString( idJobEntry, TAG_SERVERNAME );
      userName = rep.getJobEntryAttributeString( idJobEntry, TAG_USERNAME );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( idJobEntry, TAG_PASSWORD ) );
      grantType = rep.getJobEntryAttributeString( idJobEntry, TAG_USE_GRANT_TYPE );
      usingAuthentication = rep.getJobEntryAttributeString( idJobEntry, TAG_USE_AUTH );
      clientId = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_CLIENT_ID );
      secretKey = Encr.decryptPasswordOptionallyEncrypted(  rep.getJobEntryAttributeString( idJobEntry,
          TAG_AUTH_SECRET_KEY ) ) ;
      scope = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_SCOPE );
      tokenUrl = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_TOKEN_URL );
      authorizationCode = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_AUTHORIZATION_CODE );
      redirectUri= rep.getJobEntryAttributeString( idJobEntry, TAG_REDIRECT_URI );
      refreshToken = rep.getJobEntryAttributeString( idJobEntry, TAG_REFRESH_TOKEN );
      useSsl = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_USE_SSL );
      sslPort = rep.getJobEntryAttributeString( idJobEntry, TAG_SSL_PORT ); // backward compatible.
      outputDirectory = rep.getJobEntryAttributeString( idJobEntry, TAG_OUTPUT_DIRECTORY );
      filenamePattern = rep.getJobEntryAttributeString( idJobEntry, TAG_FILENAME_PATTERN );
      if ( Utils.isEmpty( filenamePattern ) ) {
        filenamePattern = DEFAULT_FILE_NAME_PATTERN;
      }
      retrieveMails = (int) rep.getJobEntryAttributeInteger( idJobEntry, TAG_RETRIEVE_MAILS );
      firstMails = rep.getJobEntryAttributeString( idJobEntry, TAG_FIRST_MAILS );
      delete = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_DELETE );

      protocol =
        Const.NVL(
          rep.getJobEntryAttributeString( idJobEntry, TAG_PROTOCOL ), MailConnectionMeta.PROTOCOL_STRING_POP3 );

      String sv = rep.getJobEntryAttributeString( idJobEntry, TAG_SAVE_MESSAGE );
      if ( Utils.isEmpty( sv ) ) {
        saveMessage = true;
      } else {
        saveMessage = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_SAVE_MESSAGE );
      }

      String sa = rep.getJobEntryAttributeString( idJobEntry, TAG_SAVE_ATTACHMENT );
      if ( Utils.isEmpty( sa ) ) {
        saveAttachment = true;
      } else {
        saveAttachment = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_SAVE_ATTACHMENT );
      }

      useDifferentFolderForAttachment = rep.getJobEntryAttributeBoolean( idJobEntry,
          TAG_USE_DIFFERENT_FOLDER_FOR_ATTACHMENT );
      attachmentFolder = rep.getJobEntryAttributeString( idJobEntry, TAG_ATTACHMENT_FOLDER );
      attachmentWildcard = rep.getJobEntryAttributeString( idJobEntry, TAG_ATTACHMENT_WILDCARD );
      valueimaplist = MailConnectionMeta.getValueListImapListByCode( Const.NVL( rep.getJobEntryAttributeString(
          idJobEntry, TAG_VALUE_IMAP_LIST ), "" ) );
      imapFirstMails = rep.getJobEntryAttributeString( idJobEntry, TAG_IMAP_FIRST_MAILS );
      imapFolder = rep.getJobEntryAttributeString( idJobEntry, TAG_IMAP_FOLDER );
      // search term
      senderSearch = rep.getJobEntryAttributeString( idJobEntry, TAG_SENDER_SEARCH );
      notTermSenderSearch = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_NOT_TERM_SENDER_SEARCH );
      recipientSearch = rep.getJobEntryAttributeString( idJobEntry, TAG_RECIPIENT_SEARCH );
      notTermRecipientSearch = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_NOT_TERM_RECIPIENT_SEARCH );
      subjectSearch = rep.getJobEntryAttributeString( idJobEntry, TAG_SUBJECT_SEARCH );
      notTermSubjectSearch = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_NOT_TERM_SUBJECT_SEARCH );
      bodySearch = rep.getJobEntryAttributeString( idJobEntry, TAG_BODY_SEARCH );
      notTermBodySearch = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_NOT_TERM_BODY_SEARCH );
      conditionReceivedDate = MailConnectionMeta.getConditionByCode( Const.NVL( rep.getJobEntryAttributeString(
          idJobEntry, TAG_CONDITION_RECEIVED_DATE ), "" ) );
      notTermReceivedDateSearch = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_NOT_TERM_RECEIVED_DATE_SEARCH );
      receivedDate1 = rep.getJobEntryAttributeString( idJobEntry, TAG_RECEIVED_DATE_1 );
      receivedDate2 = rep.getJobEntryAttributeString( idJobEntry, TAG_RECEIVED_DATE_2 );
      actiontype = MailConnectionMeta.getActionTypeByCode( Const.NVL( rep.getJobEntryAttributeString(
          idJobEntry, TAG_ACTION_TYPE ), "" ) );
      moveToIMAPFolder = rep.getJobEntryAttributeString( idJobEntry, TAG_MOVE_TO_IMAP_FOLDER );
      createMoveToFolder = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_CREATE_MOVE_TO_FOLDER );
      createLocalFolder = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_CREATE_LOCAL_FOLDER );
      aftergetimap = MailConnectionMeta.getAfterGetIMAPByCode( Const.NVL( rep.getJobEntryAttributeString(
          idJobEntry, TAG_AFTER_GET_IMAP ), "" ) );
      includeSubfolders = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_INCLUDE_SUBFOLDERS );
      useProxy = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_USE_PROXY );
      proxyUserName = rep.getJobEntryAttributeString( idJobEntry, TAG_PROXY_USER_NAME );
    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'get pop' exists from the repository for idJobEntry=" + idJobEntry,
        dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SERVERNAME, serverName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USERNAME, userName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_PASSWORD,
        Encr.encryptPasswordIfNotUsingVariables( password ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_SSL, useSsl );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SSL_PORT, sslPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_OUTPUT_DIRECTORY, outputDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_FILENAME_PATTERN, filenamePattern );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_RETRIEVE_MAILS, retrieveMails );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_FIRST_MAILS, firstMails );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_DELETE, delete );

      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_PROTOCOL, protocol );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_AUTH, usingAuthentication );
      rep.saveJobEntryAttribute( id_job,getObjectId(), TAG_USE_GRANT_TYPE, grantType );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_CLIENT_ID, clientId );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_SECRET_KEY,
        Encr.encryptPasswordIfNotUsingVariables( secretKey ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_SCOPE, scope );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_TOKEN_URL, tokenUrl );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_AUTHORIZATION_CODE, authorizationCode );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_REDIRECT_URI,  redirectUri );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_REFRESH_TOKEN, refreshToken );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SAVE_MESSAGE, saveMessage );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SAVE_ATTACHMENT, saveAttachment );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_DIFFERENT_FOLDER_FOR_ATTACHMENT,
        useDifferentFolderForAttachment );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_ATTACHMENT_FOLDER, attachmentFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_ATTACHMENT_WILDCARD, attachmentWildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_VALUE_IMAP_LIST, MailConnectionMeta
        .getValueImapListCode( valueimaplist ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_IMAP_FIRST_MAILS, imapFirstMails );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_IMAP_FOLDER, imapFolder );
      // search term
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SENDER_SEARCH, senderSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_NOT_TERM_SENDER_SEARCH, notTermSenderSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_RECIPIENT_SEARCH, recipientSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_NOT_TERM_RECIPIENT_SEARCH, notTermRecipientSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SUBJECT_SEARCH, subjectSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_NOT_TERM_SUBJECT_SEARCH, notTermSubjectSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_BODY_SEARCH, bodySearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_NOT_TERM_BODY_SEARCH, notTermBodySearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_CONDITION_RECEIVED_DATE, MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_NOT_TERM_RECEIVED_DATE_SEARCH, notTermReceivedDateSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_RECEIVED_DATE_1, receivedDate1 );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_RECEIVED_DATE_2, receivedDate2 );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_ACTION_TYPE, MailConnectionMeta
        .getActionTypeCode( actiontype ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_MOVE_TO_IMAP_FOLDER, moveToIMAPFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_CREATE_MOVE_TO_FOLDER, createMoveToFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_CREATE_LOCAL_FOLDER, createLocalFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AFTER_GET_IMAP, MailConnectionMeta
        .getAfterGetIMAPCode( aftergetimap ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_INCLUDE_SUBFOLDERS, includeSubfolders );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_PROXY, useProxy );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_PROXY_USER_NAME, proxyUserName );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'get pop' to the repository for id_job="
        + id_job, dbe );
    }
  }

  public String getPort() {
    return sslPort;
  }

  /**
   * @return Returns the usingAuthentication.
   */
  public String isUsingAuthentication() {
    return usingAuthentication;
  }

  /**
   * @param usingAuthentication
   *          The usingAuthentication to set.
   */
  public void setUsingAuthentication( String usingAuthentication ) {
    if ( AUTENTICATION_OAUTH.equalsIgnoreCase( usingAuthentication ) ) {
      this.usingAuthentication = AUTENTICATION_OAUTH;
    } else {
      // All other cases: "Basic" (valid option), null/empty (old option) or unrecognized option
      this.usingAuthentication = AUTENTICATION_BASIC;
    }
  }

  public void setClientId( String clientId ) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return environmentSubstitute( clientId );
  }

  public void setSecretKey( String secretKey ) {
    this.secretKey = secretKey;
  }

  public String getSecretKey() {
    return environmentSubstitute( secretKey );
  }

  public void setScope(String scope)
  {
    this.scope=scope;
  }

  public String getScope()
  {
    return environmentSubstitute( scope );
  }

  public void setTokenUrl(String tokenUrl)
  {
    this.tokenUrl=tokenUrl;
  }

  public String getTokenUrl()
  {
    return environmentSubstitute( tokenUrl );
  }

  public void setAuthorization_code(String authorizationCode )
  {
    this.authorizationCode = authorizationCode;
  }

  public String getAuthorization_code()
  {
    return environmentSubstitute( authorizationCode );
  }

  public void setRedirectUri(String redirectUri)
  {
    this.redirectUri=redirectUri;
  }

  public String getRedirectUri()
  {
    return environmentSubstitute( redirectUri );
  }

  public void setRefresh_token(String refreshToken )
  {
    this.refreshToken = refreshToken;
  }

  public String getRefresh_token()
  {
    return environmentSubstitute( refreshToken );
  }

  public void setGrant_type( String grantType ) {
    this.grantType = grantType;
  }

  public String getGrant_type() {
    return environmentSubstitute( grantType );
  }

  public String getRealPort() {
    return environmentSubstitute( getPort() );
  }

  public void setPort( String sslPort ) {
    this.sslPort = sslPort;
  }

  public void setFirstMails( String firstMails ) {
    this.firstMails = firstMails;
  }

  public String getFirstMails() {
    return firstMails;
  }

  public boolean isIncludeSubFolders() {
    return includeSubfolders;
  }

  public void setIncludeSubFolders( boolean includeSubfolders ) {
    this.includeSubfolders = includeSubfolders;
  }

  public void setFirstIMAPMails( String firstMails ) {
    this.imapFirstMails = firstMails;
  }

  public String getFirstIMAPMails() {
    return imapFirstMails;
  }

  public void setSenderSearchTerm( String senderSearch ) {
    this.senderSearch = senderSearch;
  }

  public String getSenderSearchTerm() {
    return this.senderSearch;
  }

  public void setNotTermSenderSearch( boolean notTermSenderSearch ) {
    this.notTermSenderSearch = notTermSenderSearch;
  }

  public boolean isNotTermSenderSearch() {
    return this.notTermSenderSearch;
  }

  public void setNotTermSubjectSearch( boolean notTermSubjectSearch ) {
    this.notTermSubjectSearch = notTermSubjectSearch;
  }

  public void setNotTermBodySearch( boolean notTermBodySearch ) {
    this.notTermBodySearch = notTermBodySearch;
  }

  public boolean isNotTermSubjectSearch() {
    return this.notTermSubjectSearch;
  }

  public boolean isNotTermBodySearch() {
    return this.notTermBodySearch;
  }

  public void setNotTermReceivedDateSearch( boolean notTermReceivedDateSearch ) {
    this.notTermReceivedDateSearch = notTermReceivedDateSearch;
  }

  public boolean isNotTermReceivedDateSearch() {
    return this.notTermReceivedDateSearch;
  }

  public void setNotTermReceipientSearch( boolean notTermRecipientSearch ) {
    this.notTermRecipientSearch = notTermRecipientSearch;
  }

  public boolean isNotTermReceipientSearch() {
    return this.notTermRecipientSearch;
  }

  public void setCreateMoveToFolder( boolean createFolder ) {
    this.createMoveToFolder = createFolder;
  }

  public boolean isCreateMoveToFolder() {
    return this.createMoveToFolder;
  }

  public void setReceipientSearch( String recipientSearch ) {
    this.recipientSearch = recipientSearch;
  }

  public String getReceipientSearch() {
    return this.recipientSearch;
  }

  public void setSubjectSearch( String subjectSearch ) {
    this.subjectSearch = subjectSearch;
  }

  public String getSubjectSearch() {
    return this.subjectSearch;
  }

  public void setBodySearch( String bodySearch ) {
    this.bodySearch = bodySearch;
  }

  public String getBodySearch() {
    return this.bodySearch;
  }

  public String getReceivedDate1() {
    return this.receivedDate1;
  }

  public void setReceivedDate1( String inputDate ) {
    this.receivedDate1 = inputDate;
  }

  public String getReceivedDate2() {
    return this.receivedDate2;
  }

  public void setReceivedDate2( String inputDate ) {
    this.receivedDate2 = inputDate;
  }

  public void setMoveToIMAPFolder( String folderName ) {
    this.moveToIMAPFolder = folderName;
  }

  public String getMoveToIMAPFolder() {
    return this.moveToIMAPFolder;
  }

  public void setCreateLocalFolder( boolean createFolder ) {
    this.createLocalFolder = createFolder;
  }

  public boolean isCreateLocalFolder() {
    return this.createLocalFolder;
  }

  public void setConditionOnReceivedDate( int conditionReceivedDate ) {
    this.conditionReceivedDate = conditionReceivedDate;
  }

  public int getConditionOnReceivedDate() {
    return this.conditionReceivedDate;
  }

  public void setActionType( int actionType ) {
    this.actiontype = actionType;
  }

  public int getActionType() {
    return this.actiontype;
  }

  public void setAfterGetIMAP( int afterGet ) {
    this.aftergetimap = afterGet;
  }

  public int getAfterGetIMAP() {
    return this.aftergetimap;
  }

  public String getRealFirstMails() {
    return environmentSubstitute( getFirstMails() );
  }

  public void setServerName( String servername ) {
    this.serverName = servername;
  }

  public String getServerName() {
    return serverName;
  }

  public void setUserName( String username ) {
    this.userName = username;
  }

  public String getUserName() {
    return userName;
  }

  public void setOutputDirectory( String outputDirectory ) {
    this.outputDirectory = outputDirectory;
  }

  public void setFilenamePattern( String fileNamePattern ) {
    this.filenamePattern = fileNamePattern;
  }

  /**
   * <li>0 = retrieve all <li>2 = retrieve unread
   *
   * @param nr
   * @see {@link #setValueImapList(int)}
   */
  public void setRetrievemails( int nr ) {
    retrieveMails = nr;
  }

  public int getRetrievemails() {
    return this.retrieveMails;
  }

  public String getFilenamePattern() {
    return filenamePattern;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public String getRealOutputDirectory() {
    return environmentSubstitute( getOutputDirectory() );
  }

  public String getRealFilenamePattern() {
    return environmentSubstitute( getFilenamePattern() );
  }

  public String getRealUsername() {
    return environmentSubstitute( getUserName() );
  }

  public String getRealServername() {
    return environmentSubstitute( getServerName() );
  }

  public String getRealProxyUsername() {
    return environmentSubstitute( geProxyUsername() );
  }

  public String geProxyUsername() {
    return this.proxyUserName;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password string for resolving
   * @return Returns resolved decrypted password or null
   * in case of param returns null.
   */
  public String getRealPassword( String password ) {
    return Utils.resolvePassword( variables, password );
  }

  public String getAttachmentFolder() {
    return attachmentFolder;
  }

  public String getRealAttachmentFolder() {
    return environmentSubstitute( getAttachmentFolder() );
  }

  public void setAttachmentFolder( String folderName ) {
    this.attachmentFolder = folderName;
  }

  /**
   * @param delete
   *          The delete to set.
   */
  public void setDelete( boolean delete ) {
    this.delete = delete;
  }

  /**
   * @return Returns the delete.
   */
  public boolean getDelete() {
    return delete;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol( String protocol ) {
    this.protocol = protocol;
  }

  public String getIMAPFolder() {
    return imapFolder;
  }

  public void setIMAPFolder( String folder ) {
    this.imapFolder = folder;
  }

  public void setAttachmentWildcard( String wildcard ) {
    attachmentWildcard = wildcard;
  }

  public String getAttachmentWildcard() {
    return attachmentWildcard;
  }

  /**
   * @param useSsl
   *          The useSsl to set.
   */
  public void setUseSSL( boolean useSsl ) {
    this.useSsl = useSsl;
  }

  /**
   * @return Returns the useSsl.
   */
  public boolean isUseSSL() {
    return this.useSsl;
  }

  /**
   * @return Returns the useProxy.
   */
  public boolean isUseProxy() {
    return this.useProxy;
  }

  public void setUseProxy( boolean useProxy ) {
    this.useProxy = useProxy;
  }

  public boolean isSaveAttachment() {
    return saveAttachment;
  }

  public void setProxyUsername( String username ) {
    this.proxyUserName = username;
  }

  public String getProxyUsername() {
    return this.proxyUserName;
  }

  public void setSaveAttachment( boolean saveAttachment ) {
    this.saveAttachment = saveAttachment;
  }

  public boolean isSaveMessage() {
    return saveMessage;
  }

  public void setSaveMessage( boolean saveMessage ) {
    this.saveMessage = saveMessage;
  }

  public void setDifferentFolderForAttachment( boolean useDifferentFolder ) {
    this.useDifferentFolderForAttachment = useDifferentFolder;
  }

  public boolean isDifferentFolderForAttachment() {
    return this.useDifferentFolderForAttachment;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  @Override
  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    result.setResult( false );

    MailConnection mailConn = null;
    Date beginDate = null;
    Date endDate = null;

    SimpleDateFormat df = new SimpleDateFormat( DATE_PATTERN );

    try {
      boolean usePOP3 = getProtocol().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 );
      boolean moveAfter = false;
      int nbrMailToRetrieve =
        usePOP3 ? ( getRetrievemails() == 2 ? Const.toInt( getFirstMails(), 0 ) : 0 ) : Const.toInt(
          getFirstIMAPMails(), 0 );

      String realOutputFolder = createOutputDirectory( JobEntryGetPOP.FOLDER_OUTPUT );
      String targetAttachmentFolder = createOutputDirectory( JobEntryGetPOP.FOLDER_ATTACHMENTS );

      // Check destination folder
      String realMoveToIMAPFolder = environmentSubstitute( getMoveToIMAPFolder() );
      if ( getProtocol().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP )
        && ( getActionType() == MailConnectionMeta.ACTION_TYPE_MOVE )
        || ( getActionType() == MailConnectionMeta.ACTION_TYPE_GET
        && getAfterGetIMAP() == MailConnectionMeta.AFTER_GET_IMAP_MOVE ) ) {
        if ( Utils.isEmpty( realMoveToIMAPFolder ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.MoveToIMAPFolderEmpty" ) );
        }
        moveAfter = true;
      }
      // check search terms
      // Received Date
      String realBeginDate;
      switch ( getConditionOnReceivedDate() ) {
        case MailConnectionMeta.CONDITION_DATE_EQUAL:
        case MailConnectionMeta.CONDITION_DATE_GREATER:
        case MailConnectionMeta.CONDITION_DATE_SMALLER:
          realBeginDate = environmentSubstitute( getReceivedDate1() );
          if ( Utils.isEmpty( realBeginDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.ReceivedDateSearchTermEmpty" ) );
          }
          beginDate = df.parse( realBeginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_BETWEEN:
          realBeginDate = environmentSubstitute( getReceivedDate1() );
          if ( Utils.isEmpty( realBeginDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.ReceivedDatesSearchTermEmpty" ) );
          }
          beginDate = df.parse( realBeginDate );
          String realEndDate = environmentSubstitute( getReceivedDate2() );
          if ( Utils.isEmpty( realEndDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.ReceivedDatesSearchTermEmpty" ) );
          }
          endDate = df.parse( realEndDate );
          break;
        default:
          break;
      }

      String realServer = getRealServername();
      String realUsername = getRealUsername();
      String realPassword = getRealPassword( getPassword() );
      String realFilenamePattern = getRealFilenamePattern();
      int realPort = Const.toInt( environmentSubstitute( sslPort ), -1 );
      String realIMAPFolder = environmentSubstitute( getIMAPFolder() );
      String realProxyUsername = getRealProxyUsername();

      initVariables();
      // create a mail connection object

     if( JobEntryGetPOP.AUTENTICATION_OAUTH.equals( usingAuthentication ) ) {
        realPassword = "Bearer " + getOauthToken(environmentSubstitute( getTokenUrl() ) ).getAccessToken();
      }

      mailConn =
        new MailConnection(
          parentJobMeta.getBowl(), log, MailConnectionMeta.getProtocolFromString( getProtocol(),
          MailConnectionMeta.PROTOCOL_IMAP ), realServer, realPort, realUsername, realPassword, isUseSSL(),
          isUseProxy(), realProxyUsername );
      // connect
      mailConn.connect();

      if ( moveAfter ) {
        // Set destination folder
        // Check if folder exists
        mailConn.setDestinationFolder( realMoveToIMAPFolder, isCreateMoveToFolder() );
      }

      // apply search term?
      String realSearchSender = environmentSubstitute( getSenderSearchTerm() );
      if ( !Utils.isEmpty( realSearchSender ) ) {
        // apply FROM
        mailConn.setSenderTerm( realSearchSender, isNotTermSenderSearch() );
      }
      String realSearchRecipient = environmentSubstitute( getReceipientSearch() );
      if ( !Utils.isEmpty( realSearchRecipient ) ) {
        // apply TO
        mailConn.setReceipientTerm( realSearchRecipient );
      }
      String realSearchSubject = environmentSubstitute( getSubjectSearch() );
      if ( !Utils.isEmpty( realSearchSubject ) ) {
        // apply Subject
        mailConn.setSubjectTerm( realSearchSubject, isNotTermSubjectSearch() );
      }
      String realSearchBody = environmentSubstitute( getBodySearch() );
      if ( !Utils.isEmpty( realSearchBody ) ) {
        // apply body
        mailConn.setBodyTerm( realSearchBody, isNotTermBodySearch() );
      }
      // Received Date
      switch ( getConditionOnReceivedDate() ) {
        case MailConnectionMeta.CONDITION_DATE_EQUAL:
          mailConn.setReceivedDateTermEQ( beginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_GREATER:
          mailConn.setReceivedDateTermGT( beginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_SMALLER:
          mailConn.setReceivedDateTermLT( beginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_BETWEEN:
          mailConn.setReceivedDateTermBetween( beginDate, endDate );
          break;
        default:
          break;
      }
      // set FlagTerm?
      if ( usePOP3 ) {
        // retrieve messages
        if ( getRetrievemails() == 1 ) {
          // New messages
          // POP doesn't support the concept of "new" messages!
          mailConn.setFlagTermUnread();
        }
      } else {
        switch ( getValueImapList() ) {
          case MailConnectionMeta.VALUE_IMAP_LIST_NEW:
            mailConn.setFlagTermNew();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_OLD:
            mailConn.setFlagTermOld();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_READ:
            mailConn.setFlagTermRead();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_UNREAD:
            mailConn.setFlagTermUnread();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_FLAGGED:
            mailConn.setFlagTermFlagged();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_NOT_FLAGGED:
            mailConn.setFlagTermNotFlagged();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_DRAFT:
            mailConn.setFlagTermDraft();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_NOT_DRAFT:
            mailConn.setFlagTermNotDraft();
            break;
          default:
            break;
        }
      }
      // open folder and retrieve messages
      fetchOneFolder( mailConn, usePOP3, realIMAPFolder, realOutputFolder, targetAttachmentFolder, realMoveToIMAPFolder,
        realFilenamePattern, nbrMailToRetrieve, df );

      if ( isIncludeSubFolders() ) {
        // Fetch also sub folders?
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobGetPOP.FetchingSubFolders" ) );
        }
        String[] subfolders = mailConn.returnAllFolders();
        if ( subfolders.length == 0 ) {
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "JobGetPOP.NoSubFolders" ) );
          }
        } else {
          for ( String subfolder : subfolders ) {
            fetchOneFolder( mailConn, usePOP3, subfolder, realOutputFolder, targetAttachmentFolder,
              realMoveToIMAPFolder, realFilenamePattern, nbrMailToRetrieve, df );
          }
        }
      }

      result.setResult( true );
      result.setNrFilesRetrieved( mailConn.getSavedAttachedFilesCounter() );
      result.setNrLinesWritten( mailConn.getSavedMessagesCounter() );
      result.setNrLinesDeleted( mailConn.getDeletedMessagesCounter() );
      result.setNrLinesUpdated( mailConn.getMovedMessagesCounter() );

      if ( isDetailed() ) {
        logDetailed( "=======================================" );
        logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.SavedMessages", ""
          + mailConn.getSavedMessagesCounter() ) );
        logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.DeletedMessages", ""
          + mailConn.getDeletedMessagesCounter() ) );
        logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.MovedMessages", ""
          + mailConn.getMovedMessagesCounter() ) );
        if ( getActionType() == MailConnectionMeta.ACTION_TYPE_GET && isSaveAttachment() ) {
          logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.AttachedMessagesSuccess", ""
            + mailConn.getSavedAttachedFilesCounter() ) );
        }
        logDetailed( "=======================================" );
      }
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      logError( "Unexpected error: " + e.getMessage() );
      logError( Const.getStackTracker( e ) );
    } finally {
      try {
        if ( mailConn != null ) {
          mailConn.disconnect();
          mailConn = null;
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }

    return result;
  }

  void fetchOneFolder( MailConnection mailConn, boolean usePOP3, String realIMAPFolder,
    String realOutputFolder, String targetAttachmentFolder, String realMoveToIMAPFolder,
    String realFilenamePattern, int nbrMailToRetrieve, SimpleDateFormat df ) throws KettleException {
    try {
      // if it is not pop3 and we have non-default imap folder...
      if ( !usePOP3 && !Utils.isEmpty( realIMAPFolder ) ) {
        mailConn.openFolder( realIMAPFolder, true );
      } else {
        mailConn.openFolder( true );
      }

      mailConn.retrieveMessages();

      int messagesCount = mailConn.getMessagesCount();

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobGetMailsFromPOP.TotalMessagesFolder.Label", ""
          + messagesCount, Const.NVL( mailConn.getFolderName(), MailConnectionMeta.INBOX_FOLDER ) ) );
      }

      messagesCount =
        nbrMailToRetrieve > 0
          ? ( nbrMailToRetrieve > messagesCount ? messagesCount : nbrMailToRetrieve ) : messagesCount;

      if ( messagesCount > 0 ) {
        switch ( getActionType() ) {
          case MailConnectionMeta.ACTION_TYPE_DELETE:
            if ( nbrMailToRetrieve > 0 ) {
              // We need to fetch all messages in order to retrieve
              // only the first nbrMailToRetrieve ...
              for ( int i = 0; i < messagesCount && !parentJob.isStopped(); i++ ) {
                // Get next message
                mailConn.fetchNext();
                // Delete this message
                mailConn.deleteMessage();
                if ( isDebug() ) {
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageDeleted", "" + i ) );
                }
              }
            } else {
              // Delete messages
              mailConn.deleteMessages( true );
              if ( isDebug() ) {
                logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessagesDeleted", "" + messagesCount ) );
              }
            }
            break;
          case MailConnectionMeta.ACTION_TYPE_MOVE:
            if ( nbrMailToRetrieve > 0 ) {
              // We need to fetch all messages in order to retrieve
              // only the first nbrMailToRetrieve ...
              for ( int i = 0; i < messagesCount && !parentJob.isStopped(); i++ ) {
                // Get next message
                mailConn.fetchNext();
                // Move this message
                mailConn.moveMessage();
                if ( isDebug() ) {
                  logDebug( BaseMessages.getString(
                    PKG, "JobGetMailsFromPOP.MessageMoved", "" + i, realMoveToIMAPFolder ) );
                }
              }
            } else {
              // Move all messages
              mailConn.moveMessages();
              if ( isDebug() ) {
                logDebug( BaseMessages.getString(
                  PKG, "JobGetMailsFromPOP.MessagesMoved", "" + messagesCount, realMoveToIMAPFolder ) );
              }
            }
            break;
          default:
            // Get messages and save it in a local file
            // also save attached files if needed
            for ( int i = 0; i < messagesCount && !parentJob.isStopped(); i++ ) {
              // Get next message
              mailConn.fetchNext();
              int messageNumber = mailConn.getMessage().getMessageNumber();
              boolean okPOP3 = usePOP3;
              boolean okIMAP = !usePOP3;

              if ( okPOP3 || okIMAP ) {
                // display some infos on the current message
                //
                if ( isDebug() && mailConn.getMessage() != null ) {
                  logDebug( "--------------------------------------------------" );
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageNumber.Label", ""
                    + messageNumber ) );
                  if ( mailConn.getMessage().getReceivedDate() != null ) {
                    logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.ReceivedDate.Label", df
                      .format( mailConn.getMessage().getReceivedDate() ) ) );
                  }
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.ContentType.Label", mailConn
                    .getMessage().getContentType() ) );
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.EmailFrom.Label", Const.NVL( mailConn
                    .getMessage().getFrom()[0].toString(), "" ) ) );
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.EmailSubject.Label", Const.NVL(
                    mailConn.getMessage().getSubject(), "" ) ) );
                }
                if ( isSaveMessage() ) {
                  // get local message filename
                  String localFilenameMessage = replaceTokens( realFilenamePattern, i );

                  if ( isDebug() ) {
                    logDebug( BaseMessages.getString(
                      PKG, "JobGetMailsFromPOP.LocalFilename.Label", localFilenameMessage ) );
                  }

                  // save message content in the file
                  mailConn.saveMessageContentToFile( localFilenameMessage, realOutputFolder );
                  // PDI-10942 explicitly set message as read
                  mailConn.getMessage().setFlag( Flag.SEEN, true );

                  if ( isDetailed() ) {
                    logDetailed( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageSaved.Label", ""
                      + messageNumber, localFilenameMessage, realOutputFolder ) );
                  }
                }

                // Do we need to save attached file?
                if ( isSaveAttachment() ) {
                  mailConn.saveAttachedFiles( targetAttachmentFolder, attachementPattern );
                }
                // We successfully retrieved message
                // do we need to make another action (delete, move)?
                if ( usePOP3 ) {
                  if ( getDelete() ) {
                    mailConn.deleteMessage();
                    if ( isDebug() ) {
                      logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageDeleted", ""
                        + messageNumber ) );
                    }
                  }
                } else {
                  switch ( getAfterGetIMAP() ) {
                    case MailConnectionMeta.AFTER_GET_IMAP_DELETE:
                      // Delete messages
                      mailConn.deleteMessage();
                      if ( isDebug() ) {
                        logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageDeleted", ""
                          + messageNumber ) );
                      }
                      break;
                    case MailConnectionMeta.AFTER_GET_IMAP_MOVE:
                      // Move messages
                      mailConn.moveMessage();
                      if ( isDebug() ) {
                        logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageMoved", ""
                          + messageNumber, realMoveToIMAPFolder ) );
                      }
                      break;
                    default:
                  }
                }
              }
            }
            break;
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  private String replaceTokens( String aString, int idFile ) {
    String localFilenameMessage = aString.replace( FILENAME_ID_PATTERN, "" + ( idFile + 1 ) );
    localFilenameMessage =
      substituteDate( localFilenameMessage, FILENAME_SYS_DATE_OPEN, FILENAME_SYS_DATE_CLOSE, new Date() );

    return localFilenameMessage;
  }

  private String substituteDate( String aString, String open, String close, Date datetime ) {
    if ( aString == null ) {
      return null;
    }
    StringBuilder buffer = new StringBuilder();
    String rest = aString;

    // search for closing string
    int i = rest.indexOf( open );
    while ( i > -1 ) {
      int j = rest.indexOf( close, i + open.length() );
      // search for closing string
      if ( j > -1 ) {
        String varName = rest.substring( i + open.length(), j );
        DateFormat dateFormat = new SimpleDateFormat( varName );
        Object Value = dateFormat.format( datetime );

        buffer.append( rest.substring( 0, i ) );
        buffer.append( Value );
        rest = rest.substring( j + close.length() );
      } else {
        // no closing tag found; end the search
        buffer.append( rest );
        rest = "";
      }
      // keep searching
      i = rest.indexOf( close );
    }
    buffer.append( rest );
    return buffer.toString();
  }

  private void initVariables() {
    // Attachment wildcard
    attachementPattern = null;
    String realAttachmentWildcard = environmentSubstitute( getAttachmentWildcard() );
    if ( !Utils.isEmpty( realAttachmentWildcard ) ) {
      attachementPattern = Pattern.compile( realAttachmentWildcard );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "serverName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "userName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, TAG_PASSWORD, remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notBlankValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "outputDirectory", remarks, ctx );

    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "SSLPort", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

  public IEmailAuthenticationResponse getOauthToken( String tokenUrl ) {
    try ( CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient() ) {
      this.tokenUrl= parentJobMeta.environmentSubstitute(  tokenUrl );
      HttpPost httpPost = new HttpPost( parentJobMeta.environmentSubstitute( tokenUrl ) );
      List<NameValuePair> form = new ArrayList<>();
      form.add( new BasicNameValuePair( "scope", parentJobMeta.environmentSubstitute( getScope() ) ) );
      form.add( new BasicNameValuePair( "client_id", parentJobMeta.environmentSubstitute( getClientId() ) ));
      form.add( new BasicNameValuePair( "client_secret", parentJobMeta.environmentSubstitute( getSecretKey() ) ));
      String realGrantType = parentJobMeta.environmentSubstitute( getGrant_type() );
      form.add( new BasicNameValuePair( "grant_type", realGrantType ) );
      if ( realGrantType.equals( JobEntryGetPOP.GRANTTYPE_REFRESH_TOKEN ) ) {
        form.add( new BasicNameValuePair( JobEntryGetPOP.GRANTTYPE_REFRESH_TOKEN, parentJobMeta.environmentSubstitute( getRefresh_token() ) ) );
      }
      if ( realGrantType.equals( JobEntryGetPOP.GRANTTYPE_AUTHORIZATION_CODE ) ) {
        form.add( new BasicNameValuePair( "code", parentJobMeta.environmentSubstitute( getAuthorization_code() ) ) );
        form.add( new BasicNameValuePair( "redirect_uri", parentJobMeta.environmentSubstitute( getRedirectUri() ) ) );
      }
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity( form, Consts.UTF_8 );
      httpPost.setEntity( entity );
      try ( CloseableHttpResponse response = client.execute( httpPost ) ) {
        if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ) {
          throw new HttpException( "Unable to get authorization token " + response.getStatusLine().toString() );
        }
        String responseBody = EntityUtils.toString( response.getEntity() );
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue( responseBody, EmailAuthenticationResponse.class );
      } catch ( HttpException | IOException e ) {
        throw new RuntimeException( e );
      }
    } catch ( IOException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( serverName ) ) {
      String realServername = jobMeta.environmentSubstitute( serverName );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realServername, ResourceType.SERVER ) );
      references.add( reference );
    }
    return references;
  }

  String createOutputDirectory( int folderType ) throws KettleException, FileSystemException, IllegalArgumentException {
    if ( ( folderType != JobEntryGetPOP.FOLDER_OUTPUT ) && ( folderType != JobEntryGetPOP.FOLDER_ATTACHMENTS ) ) {
      throw new IllegalArgumentException( "Invalid folderType argument" );
    }
    String folderName = "";
    switch ( folderType ) {
      case JobEntryGetPOP.FOLDER_OUTPUT:
        folderName = getRealOutputDirectory();
        break;
      case JobEntryGetPOP.FOLDER_ATTACHMENTS:
        if ( isSaveAttachment() && isDifferentFolderForAttachment() ) {
          folderName = getRealAttachmentFolder();
        } else {
          folderName = getRealOutputDirectory();
        }
        break;
    }
    if ( Utils.isEmpty( folderName ) ) {
      switch ( folderType ) {
        case JobEntryGetPOP.FOLDER_OUTPUT:
          throw new KettleException( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.OutputFolderEmpty" ) );
        case JobEntryGetPOP.FOLDER_ATTACHMENTS:
          throw new KettleException( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.AttachmentFolderEmpty" ) );
      }
    }
    FileObject folder = KettleVFS.getInstance( parentJobMeta.getBowl() ).getFileObject( folderName, this );
    if ( folder.exists() ) {
      if ( folder.getType() != FileType.FOLDER ) {
        switch ( folderType ) {
          case JobEntryGetPOP.FOLDER_OUTPUT:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.NotAFolderNot", folderName ) );
          case JobEntryGetPOP.FOLDER_ATTACHMENTS:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.AttachmentFolderNotAFolder", folderName ) );
        }
      }
      if ( isDebug() ) {
        switch ( folderType ) {
          case JobEntryGetPOP.FOLDER_OUTPUT:
            logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Log.OutputFolderExists", folderName ) );
            break;
          case JobEntryGetPOP.FOLDER_ATTACHMENTS:
            logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Log.AttachmentFolderExists", folderName ) );
            break;
        }
      }
    } else {
      if ( isCreateLocalFolder() ) {
        folder.createFolder();
      } else {
        switch ( folderType ) {
          case JobEntryGetPOP.FOLDER_OUTPUT:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.OutputFolderNotExist", folderName ) );
          case JobEntryGetPOP.FOLDER_ATTACHMENTS:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.AttachmentFolderNotExist", folderName ) );
        }
      }
    }

    String returnValue = KettleVFS.getFilename( folder );
    try {
      folder.close();
    } catch ( IOException ignore ) {
      //Ignore error, as the folder was created successfully
    }
    return returnValue;
  }
}
