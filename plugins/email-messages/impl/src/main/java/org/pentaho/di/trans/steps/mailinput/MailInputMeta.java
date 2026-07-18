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



package org.pentaho.di.trans.steps.mailinput;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.getpop.EmailAuthenticationResponse;
import org.pentaho.di.job.entries.getpop.IEmailAuthenticationResponse;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Step( id = "MailInput", name = "BaseStep.TypeLongDesc.MailInput",
        i18nPackageName = "org.pentaho.di.trans.step.mailinput",
        description = "BaseStep.TypeTooltipDesc.MailInput",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Input",
        image = "ui/images/GETPOP.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Email+Messages+Input" )
public class MailInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MailInputMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String TAG_SERVERNAME = "servername";
  private static final String TAG_USE_BATCH = "useBatch";
  private static final String TAG_BATCH_SIZE = "batchSize";
  private static final String TAG_START_MSG = "startMsg";
  private static final String TAG_END_MSG = "endMsg";
  private static final String TAG_STOP_ON_ERROR = "stopOnError";
  private static final String TAG_USERNAME = "username";
  private static final String TAG_USE_AUTH = "use_auth";
  private static final String TAG_AUTH_CLIENT_ID = "auth_clientId";
  private static final String TAG_AUTH_SECRET_KEY = "auth_secretKey";
  private static final String TAG_AUTH_SCOPE = "auth_scope";
  private static final String TAG_AUTH_TOKEN_URL = "auth_tokenUrl";
  private static final String TAG_AUTH_AUTHORIZATION_CODE = "auth_authorizationCode";
  private static final String TAG_REDIRECT_URI = "redirectURI";
  private static final String TAG_REFRESH_TOKEN = "refreshToken";
  private static final String TAG_USE_GRANT_TYPE = "use_grantType";
  private static final String TAG_PASSWORD = "password";
  private static final String TAG_USE_SSL = "usessl";
  private static final String TAG_SSL_PORT = "sslport";
  private static final String TAG_RETRIEVE_MAILS = "retrievemails";
  private static final String TAG_FIRST_MAILS = "firstmails";
  private static final String TAG_DELETE = "delete";
  private static final String TAG_PROTOCOL = "protocol";
  private static final String TAG_VALUE_IMAP_LIST = "valueimaplist";
  private static final String TAG_IMAP_FIRST_MAILS = "imapfirstmails";
  private static final String TAG_IMAP_FOLDER = "imapfolder";
  private static final String TAG_SENDER_SEARCH = "sendersearch";
  private static final String TAG_NOT_TERM_SENDER_SEARCH = "nottermsendersearch";
  private static final String TAG_RECIPIENT_SEARCH = "recipientsearch";
  private static final String TAG_NOT_TERM_RECIPIENT_SEARCH = "notTermRecipientSearch";
  private static final String TAG_SUBJECT_SEARCH = "subjectsearch";
  private static final String TAG_NOT_TERM_SUBJECT_SEARCH = "nottermsubjectsearch";
  private static final String TAG_CONDITION_RECEIVED_DATE = "conditionreceiveddate";
  private static final String TAG_NOT_TERM_RECEIVED_DATE_SEARCH = "nottermreceiveddatesearch";
  private static final String TAG_RECEIVED_DATE_1 = "receivedDate1";
  private static final String TAG_RECEIVED_DATE_2 = "receivedDate2";
  private static final String TAG_INCLUDE_SUBFOLDERS = "includesubfolders";
  private static final String TAG_USE_DYNAMIC_FOLDER = "usedynamicfolder";
  private static final String TAG_FOLDER_FIELD = "folderfield";
  private static final String TAG_PROXY_USERNAME = "proxyusername";
  private static final String TAG_USE_PROXY = "useproxy";
  private static final String TAG_ROW_LIMIT = "rowlimit";
  private static final String TAG_FIELDS = "fields";
  private static final String TAG_FIELD = "field";
  private static final String TAG_NAME = "name";
  private static final String TAG_COLUMN = "column";

  private static final String STR_8_SPACES = "        ";
  private static final String STR_6_SPACES = "      ";
  private static final String STR_Y = "Y";

  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static int DEFAULT_BATCH_SIZE = 500;

  public int conditionReceivedDate;

  public int valueimaplist;

  private String serverName;
  private String userName;
  private String password;
  protected VariableSpace variables = new Variables();
  public static String AUTENTICATION_OAUTH = "OAuth";

  public static String AUTENTICATION_BASIC = "Basic";

  public static String GRANTTYPE_CLIENTCREDENTIALS = "client_credentials";

  public static String GRANTTYPE_AUTHORIZATION_CODE = "authorization_code";

  public static String GRANTTYPE_REFRESH_TOKEN = "refresh_token";

  private String clientId;

  private String secretKey;

  private String scope;

  private String tokenUrl;

  private String authorization_code;

  private String redirectUri;

  private String refresh_token;

  private String grant_type;

  private String usingAuthentication;
  private boolean useSsl;
  private String sslPort;
  private String firstMails;
  public int retrievemails;
  private boolean delete;
  private String protocol;
  private String imapFirstMails;
  private String imapFolder;
  // search term
  private String senderSearch;
  private boolean notTermSenderSearch;
  private String recipientSearch;
  private String subjectSearch;
  private String receivedDate1;
  private String receivedDate2;
  private boolean notTermSubjectSearch;
  private boolean notTermRecipientSearch;
  private boolean notTermReceivedDateSearch;
  private boolean includeSubFolders;
  private boolean useProxy;
  private String proxyUserName;
  private String folderField;
  private boolean useDynamicFolder;
  private String rowLimit;

  /** The fields ... */
  private MailInputField[] inputFields;

  private boolean useBatch;
  private String start;
  private String end;

  private Integer batchSize = DEFAULT_BATCH_SIZE;

  private boolean stopOnError;

  public MailInputMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrFields ) {
    inputFields = new MailInputField[ nrFields ];
  }

  @Override
  public Object clone() {
    MailInputMeta retval = (MailInputMeta) super.clone();
    int nrFields = inputFields.length;
    retval.allocate( nrFields );
    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[i] != null ) {
        retval.inputFields[i] = (MailInputField) inputFields[i].clone();
      }
    }

    return retval;
  }

  private void readData( Node stepnode ) {
    serverName = XMLHandler.getTagValue( stepnode, TAG_SERVERNAME );
    userName = XMLHandler.getTagValue( stepnode, TAG_USERNAME );
    setUsingAuthentication( XMLHandler.getTagValue( stepnode, TAG_USE_AUTH ) );
    clientId = XMLHandler.getTagValue( stepnode, TAG_AUTH_CLIENT_ID );
    secretKey = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, TAG_AUTH_SECRET_KEY ) );
    scope = XMLHandler.getTagValue( stepnode, TAG_AUTH_SCOPE );
    tokenUrl = XMLHandler.getTagValue( stepnode, TAG_AUTH_TOKEN_URL );
    authorization_code = XMLHandler.getTagValue( stepnode, TAG_AUTH_AUTHORIZATION_CODE );
    redirectUri = XMLHandler.getTagValue( stepnode, TAG_REDIRECT_URI );
    refresh_token = XMLHandler.getTagValue( stepnode, TAG_REFRESH_TOKEN );
    grant_type = XMLHandler.getTagValue( stepnode, TAG_USE_GRANT_TYPE );
    password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, TAG_PASSWORD ) );
    useSsl = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_SSL ) );
    sslPort = XMLHandler.getTagValue( stepnode, TAG_SSL_PORT );
    retrievemails = Const.toInt( XMLHandler.getTagValue( stepnode, TAG_RETRIEVE_MAILS ), -1 );
    firstMails = XMLHandler.getTagValue( stepnode, TAG_FIRST_MAILS );
    delete = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_DELETE ) );

    protocol = Const.NVL( XMLHandler.getTagValue( stepnode, TAG_PROTOCOL ), MailConnectionMeta.PROTOCOL_STRING_POP3 );
    valueimaplist = MailConnectionMeta.getValueImapListByCode( Const.NVL(
        XMLHandler.getTagValue( stepnode, TAG_VALUE_IMAP_LIST ), "" ) );
    imapFirstMails = XMLHandler.getTagValue( stepnode, TAG_IMAP_FIRST_MAILS );
    imapFolder = XMLHandler.getTagValue( stepnode, TAG_IMAP_FOLDER );
    // search term
    senderSearch = XMLHandler.getTagValue( stepnode, TAG_SENDER_SEARCH );
    notTermSenderSearch = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_NOT_TERM_SENDER_SEARCH ) );
    recipientSearch = XMLHandler.getTagValue( stepnode, TAG_RECIPIENT_SEARCH );
    notTermRecipientSearch = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_NOT_TERM_RECIPIENT_SEARCH ) );
    subjectSearch = XMLHandler.getTagValue( stepnode, TAG_SUBJECT_SEARCH );
    notTermSubjectSearch = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_NOT_TERM_SUBJECT_SEARCH ) );
    conditionReceivedDate = MailConnectionMeta.getConditionByCode( Const.NVL(
        XMLHandler.getTagValue( stepnode, TAG_CONDITION_RECEIVED_DATE ), "" ) );
    notTermReceivedDateSearch =
      STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_NOT_TERM_RECEIVED_DATE_SEARCH ) );
    receivedDate1 = XMLHandler.getTagValue( stepnode, TAG_RECEIVED_DATE_1 );
    receivedDate2 = XMLHandler.getTagValue( stepnode, TAG_RECEIVED_DATE_2 );
    includeSubFolders = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_INCLUDE_SUBFOLDERS ) );
    useDynamicFolder = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_DYNAMIC_FOLDER ) );
    folderField = XMLHandler.getTagValue( stepnode, TAG_FOLDER_FIELD );
    proxyUserName = XMLHandler.getTagValue( stepnode, TAG_PROXY_USERNAME );
    useProxy = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_PROXY ) );
    useBatch = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_BATCH ) );
    try {
      batchSize = Integer.parseInt( XMLHandler.getTagValue( stepnode, TAG_BATCH_SIZE ) );
    } catch ( NumberFormatException e ) {
      batchSize = DEFAULT_BATCH_SIZE;
    }
    start = XMLHandler.getTagValue( stepnode, TAG_START_MSG );
    end = XMLHandler.getTagValue( stepnode, TAG_END_MSG );
    stopOnError = STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_STOP_ON_ERROR ) );

    rowLimit = XMLHandler.getTagValue( stepnode, TAG_ROW_LIMIT );
    Node fields = XMLHandler.getSubNode( stepnode, TAG_FIELDS );
    int nrFields = XMLHandler.countNodes( fields, TAG_FIELD );

    allocate( nrFields );
    for ( int i = 0; i < nrFields; i++ ) {
      Node fnode = XMLHandler.getSubNodeByNr( fields, TAG_FIELD, i );
      inputFields[i] = new MailInputField();
      inputFields[i].setName( XMLHandler.getTagValue( fnode, TAG_NAME ) );
      inputFields[i].setColumn( MailInputField.getColumnByCode( XMLHandler.getTagValue( fnode, TAG_COLUMN ) ) );
    }
  }

  @Override
  public void setDefault() {
    serverName = null;
    userName = null;
    password = null;
    useSsl = false;
    sslPort = null;
    retrievemails = 0;
    firstMails = null;
    delete = false;
    protocol = MailConnectionMeta.PROTOCOL_STRING_POP3;
    imapFirstMails = "0";
    valueimaplist = MailConnectionMeta.VALUE_IMAP_LIST_ALL;
    imapFolder = null;
    // search term
    senderSearch = null;
    notTermSenderSearch = false;
    notTermRecipientSearch = false;
    notTermSubjectSearch = false;
    receivedDate1 = null;
    receivedDate2 = null;
    notTermReceivedDateSearch = false;
    recipientSearch = null;
    subjectSearch = null;
    includeSubFolders = false;
    useProxy = false;
    proxyUserName = null;
    folderField = null;
    useDynamicFolder = false;
    rowLimit = "0";

    batchSize = DEFAULT_BATCH_SIZE;
    useBatch = false;
    start = null;
    end = null;
    stopOnError = true;

    allocate( 0 );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      serverName = rep.getStepAttributeString( id_step, TAG_SERVERNAME );
      userName = rep.getStepAttributeString( id_step, TAG_USERNAME );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, TAG_PASSWORD ) );
      usingAuthentication = Const.NVL( rep.getStepAttributeString( id_step, TAG_USE_AUTH ), AUTENTICATION_BASIC );
      grant_type = rep.getStepAttributeString( id_step, TAG_USE_GRANT_TYPE );
      clientId = rep.getStepAttributeString( id_step, TAG_AUTH_CLIENT_ID );
      secretKey =
              Encr.decryptPasswordOptionallyEncrypted(  rep.getStepAttributeString( id_step, TAG_AUTH_SECRET_KEY ) ) ;
      scope = rep.getStepAttributeString( id_step, TAG_AUTH_SCOPE );
      tokenUrl = rep.getStepAttributeString( id_step, TAG_AUTH_TOKEN_URL );
      authorization_code = rep.getStepAttributeString( id_step, TAG_AUTH_AUTHORIZATION_CODE );
      redirectUri= rep.getStepAttributeString( id_step, TAG_REDIRECT_URI );
      refresh_token = rep.getStepAttributeString( id_step, TAG_REFRESH_TOKEN );
      useSsl = rep.getStepAttributeBoolean( id_step, TAG_USE_SSL );
      int intSSLPort = (int) rep.getStepAttributeInteger( id_step, TAG_SSL_PORT );
      sslPort = rep.getStepAttributeString( id_step, TAG_SSL_PORT ); // backward compatible.
      if ( intSSLPort > 0 && Utils.isEmpty( sslPort ) ) {
        sslPort = Integer.toString( intSSLPort );
      }

      retrievemails = (int) rep.getStepAttributeInteger( id_step, TAG_RETRIEVE_MAILS );
      firstMails = rep.getStepAttributeString( id_step, TAG_FIRST_MAILS );
      delete = rep.getStepAttributeBoolean( id_step, TAG_DELETE );

      protocol =
        Const.NVL( rep.getStepAttributeString( id_step, TAG_PROTOCOL ), MailConnectionMeta.PROTOCOL_STRING_POP3 );

      valueimaplist = MailConnectionMeta.getValueListImapListByCode( Const.NVL(
          rep.getStepAttributeString( id_step, TAG_VALUE_IMAP_LIST ), "" ) );
      imapFirstMails = rep.getStepAttributeString( id_step, TAG_IMAP_FIRST_MAILS );
      imapFolder = rep.getStepAttributeString( id_step, TAG_IMAP_FOLDER );
      // search term
      senderSearch = rep.getStepAttributeString( id_step, TAG_SENDER_SEARCH );
      notTermSenderSearch = rep.getStepAttributeBoolean( id_step, TAG_NOT_TERM_SENDER_SEARCH );
      recipientSearch = rep.getStepAttributeString( id_step, TAG_RECIPIENT_SEARCH );
      notTermRecipientSearch = rep.getStepAttributeBoolean( id_step, TAG_NOT_TERM_RECIPIENT_SEARCH );
      subjectSearch = rep.getStepAttributeString( id_step, TAG_SUBJECT_SEARCH );
      notTermSubjectSearch = rep.getStepAttributeBoolean( id_step, TAG_NOT_TERM_SUBJECT_SEARCH );
      conditionReceivedDate = MailConnectionMeta.getConditionByCode( Const.NVL(
          rep.getStepAttributeString( id_step, TAG_CONDITION_RECEIVED_DATE ), "" ) );
      notTermReceivedDateSearch = rep.getStepAttributeBoolean( id_step, TAG_NOT_TERM_RECEIVED_DATE_SEARCH );
      receivedDate1 = rep.getStepAttributeString( id_step, "receiveddate1" );
      receivedDate2 = rep.getStepAttributeString( id_step, "receiveddate2" );
      includeSubFolders = rep.getStepAttributeBoolean( id_step, TAG_INCLUDE_SUBFOLDERS );
      useProxy = rep.getStepAttributeBoolean( id_step, TAG_USE_PROXY );
      proxyUserName = rep.getStepAttributeString( id_step, TAG_PROXY_USERNAME );
      useDynamicFolder = rep.getStepAttributeBoolean( id_step, TAG_USE_DYNAMIC_FOLDER );
      folderField = rep.getStepAttributeString( id_step, TAG_FOLDER_FIELD );
      rowLimit = rep.getStepAttributeString( id_step, TAG_ROW_LIMIT );
      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );

      useBatch = rep.getStepAttributeBoolean( id_step, TAG_USE_BATCH );
      try {
        batchSize = (int) rep.getStepAttributeInteger( id_step, TAG_BATCH_SIZE );
      } catch ( Exception e ) {
        batchSize = DEFAULT_BATCH_SIZE;
      }
      start = rep.getStepAttributeString( id_step, TAG_START_MSG );
      end = rep.getStepAttributeString( id_step, TAG_END_MSG );
      stopOnError = rep.getStepAttributeBoolean( id_step, TAG_STOP_ON_ERROR );

      allocate( nrFields );
      for ( int i = 0; i < nrFields; i++ ) {
        MailInputField field = new MailInputField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field
          .setColumn( MailInputField.getColumnByCode( rep.getStepAttributeString( id_step, i, "field_column" ) ) );

        inputFields[i] = field;
      }
    } catch ( Exception e ) {
      throw new KettleException( "Erreur inattendue", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, TAG_SERVERNAME, serverName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USERNAME, userName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_PASSWORD, Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_CLIENT_ID, clientId );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_SECRET_KEY, Encr
              .encryptPasswordIfNotUsingVariables( secretKey ) );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_SCOPE, scope );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_TOKEN_URL, tokenUrl );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_AUTHORIZATION_CODE, authorization_code );
      rep.saveStepAttribute( id_transformation, id_step, TAG_REDIRECT_URI, redirectUri );
      rep.saveStepAttribute( id_transformation, id_step, TAG_REFRESH_TOKEN, refresh_token );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_GRANT_TYPE, grant_type );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_AUTH, usingAuthentication );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_SSL, useSsl );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SSL_PORT, sslPort );
      rep.saveStepAttribute( id_transformation, id_step, TAG_RETRIEVE_MAILS, retrievemails );
      rep.saveStepAttribute( id_transformation, id_step, TAG_FIRST_MAILS, firstMails );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DELETE, delete );

      rep.saveStepAttribute( id_transformation, id_step, TAG_PROTOCOL, protocol );

      rep.saveStepAttribute( id_transformation, id_step, TAG_VALUE_IMAP_LIST, MailConnectionMeta
        .getValueImapListCode( valueimaplist ) );
      rep.saveStepAttribute( id_transformation, id_step, TAG_IMAP_FIRST_MAILS, imapFirstMails );
      rep.saveStepAttribute( id_transformation, id_step, TAG_IMAP_FOLDER, imapFolder );
      // search term
      rep.saveStepAttribute( id_transformation, id_step, TAG_SENDER_SEARCH, senderSearch );
      rep.saveStepAttribute( id_transformation, id_step, TAG_NOT_TERM_SENDER_SEARCH, notTermSenderSearch );
      rep.saveStepAttribute( id_transformation, id_step, TAG_RECIPIENT_SEARCH, recipientSearch );
      rep.saveStepAttribute( id_transformation, id_step, TAG_NOT_TERM_RECIPIENT_SEARCH, notTermRecipientSearch );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SUBJECT_SEARCH, subjectSearch );
      rep.saveStepAttribute( id_transformation, id_step, TAG_NOT_TERM_SUBJECT_SEARCH, notTermSubjectSearch );
      rep.saveStepAttribute( id_transformation, id_step, TAG_CONDITION_RECEIVED_DATE, MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) );
      rep.saveStepAttribute( id_transformation, id_step, TAG_NOT_TERM_RECEIVED_DATE_SEARCH, notTermReceivedDateSearch );
      rep.saveStepAttribute( id_transformation, id_step, "receiveddate1", receivedDate1 );
      rep.saveStepAttribute( id_transformation, id_step, "receiveddate2", receivedDate2 );
      rep.saveStepAttribute( id_transformation, id_step, TAG_INCLUDE_SUBFOLDERS, includeSubFolders );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_PROXY, useProxy );
      rep.saveStepAttribute( id_transformation, id_step, TAG_PROXY_USERNAME, proxyUserName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_DYNAMIC_FOLDER, useDynamicFolder );
      rep.saveStepAttribute( id_transformation, id_step, TAG_FOLDER_FIELD, folderField );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ROW_LIMIT, rowLimit );

      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_BATCH, useBatch );
      rep.saveStepAttribute( id_transformation, id_step, TAG_BATCH_SIZE, batchSize );
      rep.saveStepAttribute( id_transformation, id_step, TAG_START_MSG, start );
      rep.saveStepAttribute( id_transformation, id_step, TAG_END_MSG, end );
      rep.saveStepAttribute( id_transformation, id_step, TAG_STOP_ON_ERROR, stopOnError );

      for ( int i = 0; i < inputFields.length; i++ ) {
        MailInputField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_column", field.getColumnCode() );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save step of type 'get pop' to the repository for id_step=" + id_step, dbe );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SERVERNAME, serverName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USERNAME, userName ) );
    retval.append( STR_6_SPACES ).append(
      XMLHandler.addTagValue( TAG_PASSWORD, Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_AUTH, usingAuthentication ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_GRANT_TYPE, grant_type) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_CLIENT_ID, clientId ) );
    retval.append( STR_6_SPACES ).append(
            XMLHandler.addTagValue( TAG_AUTH_SECRET_KEY,  Encr.encryptPasswordIfNotUsingVariables( secretKey) )  );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_SCOPE, scope ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_TOKEN_URL, tokenUrl ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_AUTHORIZATION_CODE, authorization_code ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REDIRECT_URI, redirectUri ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REFRESH_TOKEN, refresh_token) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_SSL, useSsl ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SSL_PORT, sslPort ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_RETRIEVE_MAILS, retrievemails ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_FIRST_MAILS, firstMails ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DELETE, delete ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PROTOCOL, protocol ) );
    retval.append( STR_6_SPACES ).append(
      XMLHandler.addTagValue( TAG_VALUE_IMAP_LIST, MailConnectionMeta.getValueImapListCode( valueimaplist ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_IMAP_FIRST_MAILS, imapFirstMails ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_IMAP_FOLDER, imapFolder ) );
    // search term
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SENDER_SEARCH, senderSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_SENDER_SEARCH, notTermSenderSearch ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_RECIPIENT_SEARCH, recipientSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_RECIPIENT_SEARCH, notTermRecipientSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SUBJECT_SEARCH, subjectSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NOT_TERM_SUBJECT_SEARCH, notTermSubjectSearch ) );
    retval.append( STR_6_SPACES ).append(
      XMLHandler.addTagValue( TAG_CONDITION_RECEIVED_DATE, MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) ) );
    retval.append( STR_6_SPACES ).append(
      XMLHandler.addTagValue( TAG_NOT_TERM_RECEIVED_DATE_SEARCH, notTermReceivedDateSearch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( "receiveddate1", receivedDate1 ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( "receiveddate2", receivedDate2 ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_SUBFOLDERS, includeSubFolders ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_PROXY, useProxy ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PROXY_USERNAME, proxyUserName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_DYNAMIC_FOLDER, useDynamicFolder ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_FOLDER_FIELD, folderField ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ROW_LIMIT, rowLimit ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_BATCH, useBatch ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_BATCH_SIZE, batchSize ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_START_MSG, start ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_END_MSG, end ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_STOP_ON_ERROR, stopOnError ) );

    /*
     * Describe the fields to read
     */
    retval.append( "    <fields>" ).append( Const.CR );
    for ( MailInputField inputField : inputFields ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( STR_8_SPACES ).append( XMLHandler.addTagValue( TAG_NAME, inputField.getName() ) );
      retval.append( STR_8_SPACES ).append( XMLHandler.addTagValue( TAG_COLUMN, inputField.getColumnCode() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );
    return retval.toString();
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailInputMeta.CheckResult.NoInputExpected" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailInputMeta.CheckResult.NoInput" ), stepMeta );
      remarks.add( cr );
    }
  }

  public String getPort() {
    return sslPort;
  }

  public void setPort( String sslport ) {
    this.sslPort = sslport;
  }

  public void setFirstMails( String firstmails ) {
    this.firstMails = firstmails;
  }

  public String getFirstMails() {
    return firstMails;
  }

  public boolean isIncludeSubFolders() {
    return includeSubFolders;
  }

  public void setIncludeSubFolders( boolean includesubfolders ) {
    this.includeSubFolders = includesubfolders;
  }

  /**
   * @return Returns the useproxy.
   */
  public boolean isUseProxy() {
    return this.useProxy;
  }

  public void setUseProxy( boolean useprox ) {
    this.useProxy = useprox;
  }

  public void setProxyUsername( String username ) {
    this.proxyUserName = username;
  }

  public String getProxyUsername() {
    return this.proxyUserName;
  }

  /**
   * @return Returns the usedynamicfolder.
   */
  public boolean isDynamicFolder() {
    return this.useDynamicFolder;
  }

  public void setDynamicFolder( boolean usedynamicfolder ) {
    this.useDynamicFolder = usedynamicfolder;
  }

  public void setRowLimit( String rowlimit ) {
    this.rowLimit = rowlimit;
  }

  public String getRowLimit() {
    return this.rowLimit;
  }

  public void setFolderField( String folderfield ) {
    this.folderField = folderfield;
  }

  public String getFolderField() {
    return this.folderField;
  }

  public void setFirstIMAPMails( String firstmails ) {
    this.imapFirstMails = firstmails;
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

  public boolean isNotTermSubjectSearch() {
    return this.notTermSubjectSearch;
  }

  public void setNotTermReceivedDateSearch( boolean notTermReceivedDateSearch ) {
    this.notTermReceivedDateSearch = notTermReceivedDateSearch;
  }

  public boolean isNotTermReceivedDateSearch() {
    return this.notTermReceivedDateSearch;
  }

  public void setNotTermRecipientSearch( boolean notTermRecipientSearch ) {
    this.notTermRecipientSearch = notTermRecipientSearch;
  }

  public boolean isNotTermRecipientSearch() {
    return this.notTermRecipientSearch;
  }

  public void setRecipientSearch( String recipientSearch ) {
    this.recipientSearch = recipientSearch;
  }

  public String getRecipientSearch() {
    return this.recipientSearch;
  }

  public void setSubjectSearch( String subjectSearch ) {
    this.subjectSearch = subjectSearch;
  }

  public String getSubjectSearch() {
    return this.subjectSearch;
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

  public void setConditionOnReceivedDate( int conditionReceivedDate ) {
    this.conditionReceivedDate = conditionReceivedDate;
  }

  public int getConditionOnReceivedDate() {
    return this.conditionReceivedDate;
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

  /**
   * <li>0 = retrieve all <li>2 = retrieve unread
   *
   * @param nr
   * @see {@link #setValueImapList(int)}
   */
  public void setRetrievemails( int nr ) {
    retrievemails = nr;
  }

  public int getRetrievemails() {
    return this.retrievemails;
  }

  public int getValueImapList() {
    return valueimaplist;
  }

  public void setValueImapList( int value ) {
    this.valueimaplist = value;
  }

  /**
   * @return Returns the input fields.
   */
  public MailInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( MailInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
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

  /**
   * @param usessl
   *          The usessl to set.
   */
  public void setUseSSL( boolean usessl ) {
    this.useSsl = usessl;
  }

  /**
   * @return Returns the usessl.
   */
  public boolean isUseSSL() {
    return useSsl;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new MailInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new MailInputData();
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    int i;
    variables = space;
    for ( i = 0; i < inputFields.length; i++ ) {
      MailInputField field = inputFields[i];
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( field.getName() ) );
      switch ( field.getColumn() ) {
        case MailInputField.COLUMN_MESSAGE_NR:
        case MailInputField.COLUMN_SIZE:
        case MailInputField.COLUMN_ATTACHED_FILES_COUNT:
          v = new ValueMetaInteger( space.environmentSubstitute( field.getName() ) );
          v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
          break;
        case MailInputField.COLUMN_RECEIVED_DATE:
        case MailInputField.COLUMN_SENT_DATE:
          v = new ValueMetaDate( space.environmentSubstitute( field.getName() ) );
          break;
        case MailInputField.COLUMN_FLAG_DELETED:
        case MailInputField.COLUMN_FLAG_DRAFT:
        case MailInputField.COLUMN_FLAG_FLAGGED:
        case MailInputField.COLUMN_FLAG_NEW:
        case MailInputField.COLUMN_FLAG_READ:
          v = new ValueMetaBoolean( space.environmentSubstitute( field.getName() ) );
          break;
        default:
          // STRING
          v.setLength( 250 );
          v.setPrecision( -1 );
          break;
      }
      v.setOrigin( name );
      r.addValueMeta( v );
    }

  }

  public boolean useBatch() {
    return useBatch;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public boolean isStopOnError() {
    return stopOnError;
  }

  public void setStopOnError( boolean breakOnError ) {
    this.stopOnError = breakOnError;
  }

  public boolean isUseBatch() {
    return useBatch;
  }

  public void setUseBatch( boolean useBatch ) {
    this.useBatch = useBatch;
  }

  public String getStart() {
    return start;
  }

  public void setStart( String start ) {
    this.start = start;
  }

  public String getEnd() {
    return end;
  }

  public void setEnd( String end ) {
    this.end = end;
  }

  public void setBatchSize( Integer batchSize ) {
    this.batchSize = batchSize;
  }
  public String isUsingAuthentication() {
    return this.usingAuthentication;
  }

  public void setUsingAuthentication( String usingAuthentication ) {
    if ( AUTENTICATION_OAUTH.equalsIgnoreCase( usingAuthentication ) ) {
      this.usingAuthentication = AUTENTICATION_OAUTH;
    } else {
      // All other cases: "Basic" (valid option), null/empty (old option) or unrecognized option
      this.usingAuthentication = AUTENTICATION_BASIC;
    }
  }

  public void setGrant_type( String grant_type ) {
    this.grant_type = grant_type;
  }

  public String getGrant_type() {
    return grant_type;
  }

  public void setClientId( String clientId ) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setSecretKey( String secretKey ) {
    this.secretKey = secretKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setScope(String scope)
  {
    this.scope=scope;
  }

  public String getScope()
  {
    return scope;
  }

  public void setGrantType(String grant_type)
  {
    this.grant_type=grant_type;
  }
  public String getGrantType()
  {
    return grant_type;
  }

  public void setTokenUrl(String tokenUrl)
  {
    this.tokenUrl=tokenUrl;
  }

  public String getTokenUrl()
  {
    return tokenUrl;
  }

  public void setAuthorization_code(String authorization_code)
  {
    this.authorization_code=authorization_code;
  }

  public String getAuthorization_code()
  {
    return authorization_code;
  }

  public void setRedirectUri(String redirectUri)
  {
    this.redirectUri=redirectUri;
  }

  public String getRedirectUri()
  {
    return redirectUri;
  }

  public void setRefresh_token(String refresh_token)
  {
    this.refresh_token=refresh_token;
  }

  public String getRefresh_token()
  {
    return refresh_token;
  }

  public IEmailAuthenticationResponse getOauthToken(String tokenUrl, String scope, String clientId, String secretKey,
                                                    String grantType, String refreshToken, String authorizationCode, String redirectUri) {
    try (CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient()) {
      HttpPost httpPost = new HttpPost( variables.environmentSubstitute( tokenUrl ) );
      List<NameValuePair> form = new ArrayList<>();
      form.add(new BasicNameValuePair("scope", variables.environmentSubstitute( scope ) ) );
      form.add(new BasicNameValuePair("client_id", variables.environmentSubstitute( clientId ) ));
      form.add(new BasicNameValuePair("client_secret", variables.environmentSubstitute( secretKey ) ) );
      form.add(new BasicNameValuePair("grant_type", grantType));
      if (grantType.equals(GRANTTYPE_REFRESH_TOKEN)) {
        form.add(new BasicNameValuePair(GRANTTYPE_REFRESH_TOKEN, variables.environmentSubstitute( refreshToken ) ) );
      }
      if (grantType.equals(GRANTTYPE_AUTHORIZATION_CODE)) {
        form.add(new BasicNameValuePair("code", variables.environmentSubstitute( authorizationCode ) ) );
        form.add(new BasicNameValuePair("redirect_uri", variables.environmentSubstitute( redirectUri ) ) );
      }
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
      httpPost.setEntity(entity);
      try (CloseableHttpResponse response = client.execute(httpPost)) {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          throw new HttpException("Unable to get authorization token " + response.getStatusLine().toString());
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(EntityUtils.toString(response.getEntity()), EmailAuthenticationResponse.class);
      } catch ( HttpException | IOException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
