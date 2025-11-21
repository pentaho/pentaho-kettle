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
import org.pentaho.di.core.util.Utils;
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

  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static int DEFAULT_BATCH_SIZE = 500;

  public int conditionReceivedDate;

  public int valueimaplist;

  private String servername;
  private String username;
  private String password;
  protected static VariableSpace variables = new Variables();
  public static String AUTENTICATION_OAUTH = "OAuth";

  public static  String AUTENTICATION_BASIC = "Basic";

  public static String AUTENTICATION_NONE= "No Auth";

  public static String GRANTTYPE_CLIENTCREDENTIALS="client_credentials";

  public static String GRANTTYPE_AUTHORIZATION_CODE="authorization_code";

  public static String GRANTTYPE_REFRESH_TOKEN="refresh_token";

  private String clientId;

  private String secretKey;

  private String scope;

  private String tokenUrl;

  private String authorization_code;

  private String redirectUri;

  private String refresh_token;

  private String grant_type;

  private String usingAuthentication;
  private boolean usessl;
  private String sslport;
  private String firstmails;
  public int retrievemails;
  private boolean delete;
  private String protocol;
  private String imapfirstmails;
  private String imapfolder;
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
  private boolean includesubfolders;
  private boolean useproxy;
  private String proxyusername;
  private String folderfield;
  private boolean usedynamicfolder;
  private String rowlimit;

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

  public void allocate( int nrfields ) {
    inputFields = new MailInputField[nrfields];
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
    servername = XMLHandler.getTagValue( stepnode, "servername" );
    username = XMLHandler.getTagValue( stepnode, "username" );
    usingAuthentication = XMLHandler.getTagValue( stepnode, "use_auth" ) ;
    clientId = XMLHandler.getTagValue( stepnode, "auth_clientId" );
    secretKey = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "auth_secretKey" ) );
    scope = XMLHandler.getTagValue( stepnode, "auth_scope" );
    tokenUrl = XMLHandler.getTagValue( stepnode, "auth_tokenUrl" );
    authorization_code = XMLHandler.getTagValue( stepnode, "auth_authorizationCode" );
    redirectUri = XMLHandler.getTagValue( stepnode, "redirectURI" );
    refresh_token = XMLHandler.getTagValue( stepnode, "refreshToken" );
    grant_type = XMLHandler.getTagValue( stepnode, "use_grantType" );
    password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "password" ) );
    usessl = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usessl" ) );
    sslport = XMLHandler.getTagValue( stepnode, "sslport" );
    retrievemails = Const.toInt( XMLHandler.getTagValue( stepnode, "retrievemails" ), -1 );
    firstmails = XMLHandler.getTagValue( stepnode, "firstmails" );
    delete = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "delete" ) );

    protocol = Const.NVL( XMLHandler.getTagValue( stepnode, "protocol" ), MailConnectionMeta.PROTOCOL_STRING_POP3 );
    valueimaplist =
      MailConnectionMeta.getValueImapListByCode( Const.NVL(
        XMLHandler.getTagValue( stepnode, "valueimaplist" ), "" ) );
    imapfirstmails = XMLHandler.getTagValue( stepnode, "imapfirstmails" );
    imapfolder = XMLHandler.getTagValue( stepnode, "imapfolder" );
    // search term
    senderSearch = XMLHandler.getTagValue( stepnode, "sendersearch" );
    notTermSenderSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "nottermsendersearch" ) );
    recipientSearch = XMLHandler.getTagValue( stepnode, "recipientsearch" );
    notTermRecipientSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "notTermRecipientSearch" ) );
    subjectSearch = XMLHandler.getTagValue( stepnode, "subjectsearch" );
    notTermSubjectSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "nottermsubjectsearch" ) );
    conditionReceivedDate =
      MailConnectionMeta.getConditionByCode( Const.NVL( XMLHandler.getTagValue(
        stepnode, "conditionreceiveddate" ), "" ) );
    notTermReceivedDateSearch =
      "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "nottermreceiveddatesearch" ) );
    receivedDate1 = XMLHandler.getTagValue( stepnode, "receivedDate1" );
    receivedDate2 = XMLHandler.getTagValue( stepnode, "receivedDate2" );
    includesubfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "includesubfolders" ) );
    usedynamicfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usedynamicfolder" ) );
    folderfield = XMLHandler.getTagValue( stepnode, "folderfield" );
    proxyusername = XMLHandler.getTagValue( stepnode, "proxyusername" );
    useproxy = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "useproxy" ) );
    useBatch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, Tags.USE_BATCH ) );
    try {
      batchSize = Integer.parseInt( XMLHandler.getTagValue( stepnode, Tags.BATCH_SIZE ) );
    } catch ( NumberFormatException e ) {
      batchSize = DEFAULT_BATCH_SIZE;
    }
    start = XMLHandler.getTagValue( stepnode, Tags.START_MSG );
    end = XMLHandler.getTagValue( stepnode, Tags.END_MSG );
    stopOnError = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, Tags.STOP_ON_ERROR ) );

    rowlimit = XMLHandler.getTagValue( stepnode, "rowlimit" );
    Node fields = XMLHandler.getSubNode( stepnode, "fields" );
    int nrFields = XMLHandler.countNodes( fields, "field" );

    allocate( nrFields );
    for ( int i = 0; i < nrFields; i++ ) {
      Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
      inputFields[i] = new MailInputField();
      inputFields[i].setName( XMLHandler.getTagValue( fnode, "name" ) );
      inputFields[i].setColumn( MailInputField.getColumnByCode( XMLHandler.getTagValue( fnode, "column" ) ) );
    }
  }

  @Override
  public void setDefault() {
    servername = null;
    username = null;
    password = null;
    usessl = false;
    sslport = null;
    retrievemails = 0;
    firstmails = null;
    delete = false;
    protocol = MailConnectionMeta.PROTOCOL_STRING_POP3;
    imapfirstmails = "0";
    valueimaplist = MailConnectionMeta.VALUE_IMAP_LIST_ALL;
    imapfolder = null;
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
    includesubfolders = false;
    useproxy = false;
    proxyusername = null;
    folderfield = null;
    usedynamicfolder = false;
    rowlimit = "0";

    batchSize = DEFAULT_BATCH_SIZE;
    useBatch = false;
    start = null;
    end = null;
    stopOnError = true;

    int nrFields = 0;
    allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      inputFields[i] = new MailInputField( "field" + ( i + 1 ) );
    }

  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      servername = rep.getStepAttributeString( id_step, "servername" );
      username = rep.getStepAttributeString( id_step, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "password" ) );
      usingAuthentication = rep.getStepAttributeString( id_step, "use_auth" );
      grant_type = rep.getStepAttributeString( id_step, "use_grantType" );
      clientId = rep.getStepAttributeString( id_step, "auth_clientId" );
      secretKey =
              Encr.decryptPasswordOptionallyEncrypted(  rep.getStepAttributeString( id_step, "auth_secretKey" ) ) ;
      scope = rep.getStepAttributeString( id_step, "auth_scope" );
      tokenUrl = rep.getStepAttributeString( id_step, "auth_tokenUrl" );
      authorization_code = rep.getStepAttributeString( id_step, "auth_authorizationCode" );
      redirectUri= rep.getStepAttributeString( id_step, "redirectURI" );
      refresh_token = rep.getStepAttributeString( id_step, "refreshToken" );
      usessl = rep.getStepAttributeBoolean( id_step, "usessl" );
      int intSSLPort = (int) rep.getStepAttributeInteger( id_step, "sslport" );
      sslport = rep.getStepAttributeString( id_step, "sslport" ); // backward compatible.
      if ( intSSLPort > 0 && Utils.isEmpty( sslport ) ) {
        sslport = Integer.toString( intSSLPort );
      }

      retrievemails = (int) rep.getStepAttributeInteger( id_step, "retrievemails" );
      firstmails = rep.getStepAttributeString( id_step, "firstmails" );
      delete = rep.getStepAttributeBoolean( id_step, "delete" );

      protocol =
        Const.NVL( rep.getStepAttributeString( id_step, "protocol" ), MailConnectionMeta.PROTOCOL_STRING_POP3 );

      valueimaplist =
        MailConnectionMeta.getValueListImapListByCode( Const.NVL( rep.getStepAttributeString(
          id_step, "valueimaplist" ), "" ) );
      imapfirstmails = rep.getStepAttributeString( id_step, "imapfirstmails" );
      imapfolder = rep.getStepAttributeString( id_step, "imapfolder" );
      // search term
      senderSearch = rep.getStepAttributeString( id_step, "sendersearch" );
      notTermSenderSearch = rep.getStepAttributeBoolean( id_step, "nottermsendersearch" );
      recipientSearch = rep.getStepAttributeString( id_step, "recipientsearch" );
      notTermRecipientSearch = rep.getStepAttributeBoolean( id_step, "notTermRecipientSearch" );
      subjectSearch = rep.getStepAttributeString( id_step, "subjectsearch" );
      notTermSubjectSearch = rep.getStepAttributeBoolean( id_step, "nottermsubjectsearch" );
      conditionReceivedDate =
        MailConnectionMeta.getConditionByCode( Const.NVL( rep.getStepAttributeString(
          id_step, "conditionreceiveddate" ), "" ) );
      notTermReceivedDateSearch = rep.getStepAttributeBoolean( id_step, "nottermreceiveddatesearch" );
      receivedDate1 = rep.getStepAttributeString( id_step, "receiveddate1" );
      receivedDate2 = rep.getStepAttributeString( id_step, "receiveddate2" );
      includesubfolders = rep.getStepAttributeBoolean( id_step, "includesubfolders" );
      useproxy = rep.getStepAttributeBoolean( id_step, "useproxy" );
      proxyusername = rep.getStepAttributeString( id_step, "proxyusername" );
      usedynamicfolder = rep.getStepAttributeBoolean( id_step, "usedynamicfolder" );
      folderfield = rep.getStepAttributeString( id_step, "folderfield" );
      rowlimit = rep.getStepAttributeString( id_step, "rowlimit" );
      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );

      useBatch = rep.getStepAttributeBoolean( id_step, Tags.USE_BATCH );
      try {
        batchSize = (int) rep.getStepAttributeInteger( id_step, Tags.BATCH_SIZE );
      } catch ( Exception e ) {
        batchSize = DEFAULT_BATCH_SIZE;
      }
      start = rep.getStepAttributeString( id_step, Tags.START_MSG );
      end = rep.getStepAttributeString( id_step, Tags.END_MSG );
      stopOnError = rep.getStepAttributeBoolean( id_step, Tags.STOP_ON_ERROR );

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

      rep.saveStepAttribute( id_transformation, id_step, "servername", servername );
      rep.saveStepAttribute( id_transformation, id_step, "username", username );
      rep.saveStepAttribute( id_transformation, id_step, "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveStepAttribute( id_transformation, id_step, "auth_clientId", clientId );
      rep.saveStepAttribute( id_transformation, id_step, "auth_secretKey", Encr
              .encryptPasswordIfNotUsingVariables( secretKey ) );
      rep.saveStepAttribute( id_transformation, id_step, "auth_scope", scope );
      rep.saveStepAttribute( id_transformation, id_step, "auth_tokenUrl", tokenUrl );
      rep.saveStepAttribute( id_transformation, id_step, "auth_authorizationCode", authorization_code );
      rep.saveStepAttribute( id_transformation, id_step, "redirectURI", redirectUri );
      rep.saveStepAttribute( id_transformation, id_step, "refreshToken", refresh_token );
      rep.saveStepAttribute( id_transformation, id_step, "use_grantType", grant_type );
      rep.saveStepAttribute( id_transformation, id_step, "use_auth", usingAuthentication );
      rep.saveStepAttribute( id_transformation, id_step, "usessl", usessl );
      rep.saveStepAttribute( id_transformation, id_step, "sslport", sslport );
      rep.saveStepAttribute( id_transformation, id_step, "retrievemails", retrievemails );
      rep.saveStepAttribute( id_transformation, id_step, "firstmails", firstmails );
      rep.saveStepAttribute( id_transformation, id_step, "delete", delete );

      rep.saveStepAttribute( id_transformation, id_step, "protocol", protocol );

      rep.saveStepAttribute( id_transformation, id_step, "valueimaplist", MailConnectionMeta
        .getValueImapListCode( valueimaplist ) );
      rep.saveStepAttribute( id_transformation, id_step, "imapfirstmails", imapfirstmails );
      rep.saveStepAttribute( id_transformation, id_step, "imapfolder", imapfolder );
      // search term
      rep.saveStepAttribute( id_transformation, id_step, "sendersearch", senderSearch );
      rep.saveStepAttribute( id_transformation, id_step, "nottermsendersearch", notTermSenderSearch );
      rep.saveStepAttribute( id_transformation, id_step, "recipientsearch", recipientSearch );
      rep.saveStepAttribute( id_transformation, id_step, "notTermRecipientSearch", notTermRecipientSearch );
      rep.saveStepAttribute( id_transformation, id_step, "subjectsearch", subjectSearch );
      rep.saveStepAttribute( id_transformation, id_step, "nottermsubjectsearch", notTermSubjectSearch );
      rep.saveStepAttribute( id_transformation, id_step, "conditionreceiveddate", MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) );
      rep.saveStepAttribute( id_transformation, id_step, "nottermreceiveddatesearch", notTermReceivedDateSearch );
      rep.saveStepAttribute( id_transformation, id_step, "receiveddate1", receivedDate1 );
      rep.saveStepAttribute( id_transformation, id_step, "receiveddate2", receivedDate2 );
      rep.saveStepAttribute( id_transformation, id_step, "includesubfolders", includesubfolders );
      rep.saveStepAttribute( id_transformation, id_step, "useproxy", useproxy );
      rep.saveStepAttribute( id_transformation, id_step, "proxyusername", proxyusername );
      rep.saveStepAttribute( id_transformation, id_step, "usedynamicfolder", usedynamicfolder );
      rep.saveStepAttribute( id_transformation, id_step, "folderfield", folderfield );
      rep.saveStepAttribute( id_transformation, id_step, "rowlimit", rowlimit );

      rep.saveStepAttribute( id_transformation, id_step, Tags.USE_BATCH, useBatch );
      rep.saveStepAttribute( id_transformation, id_step, Tags.BATCH_SIZE, batchSize );
      rep.saveStepAttribute( id_transformation, id_step, Tags.START_MSG, start );
      rep.saveStepAttribute( id_transformation, id_step, Tags.END_MSG, end );
      rep.saveStepAttribute( id_transformation, id_step, Tags.STOP_ON_ERROR, stopOnError );

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

  private static final class Tags {
    static final String USE_BATCH = "useBatch";
    static final String BATCH_SIZE = "batchSize";
    static final String START_MSG = "startMsg";
    static final String END_MSG = "endMsg";
    static final String STOP_ON_ERROR = "stopOnError";
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    String tab = "      ";
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", servername ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", username ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "use_auth", usingAuthentication ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "use_grantType", grant_type) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "auth_clientId", clientId ) );
    retval.append( "      " ).append(
            XMLHandler.addTagValue( "auth_secretKey",  Encr.encryptPasswordIfNotUsingVariables( secretKey) )  );
    retval.append( "      " ).append( XMLHandler.addTagValue( "auth_scope", scope ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "auth_tokenUrl", tokenUrl ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "auth_authorizationCode", authorization_code ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "redirectURI", redirectUri ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "refreshToken", refresh_token) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usessl", usessl ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sslport", sslport ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "retrievemails", retrievemails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "firstmails", firstmails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "delete", delete ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "protocol", protocol ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "valueimaplist", MailConnectionMeta.getValueImapListCode( valueimaplist ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "imapfirstmails", imapfirstmails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "imapfolder", imapfolder ) );
    // search term
    retval.append( "      " ).append( XMLHandler.addTagValue( "sendersearch", senderSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nottermsendersearch", notTermSenderSearch ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "recipientsearch", recipientSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "notTermRecipientSearch", notTermRecipientSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "subjectsearch", subjectSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nottermsubjectsearch", notTermSubjectSearch ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "conditionreceiveddate", MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "nottermreceiveddatesearch", notTermReceivedDateSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "receiveddate1", receivedDate1 ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "receiveddate2", receivedDate2 ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "includesubfolders", includesubfolders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "useproxy", useproxy ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyusername", proxyusername ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usedynamicfolder", usedynamicfolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "folderfield", folderfield ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "rowlimit", rowlimit ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.USE_BATCH, useBatch ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.BATCH_SIZE, batchSize ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.START_MSG, start ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.END_MSG, end ) );
    retval.append( tab ).append( XMLHandler.addTagValue( Tags.STOP_ON_ERROR, stopOnError ) );

    /*
     * Describe the fields to read
     */
    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", inputFields[i].getName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "column", inputFields[i].getColumnCode() ) );
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
    return sslport;
  }

  public void setPort( String sslport ) {
    this.sslport = sslport;
  }

  public void setFirstMails( String firstmails ) {
    this.firstmails = firstmails;
  }

  public String getFirstMails() {
    return firstmails;
  }

  public boolean isIncludeSubFolders() {
    return includesubfolders;
  }

  public void setIncludeSubFolders( boolean includesubfolders ) {
    this.includesubfolders = includesubfolders;
  }

  /**
   * @return Returns the useproxy.
   */
  public boolean isUseProxy() {
    return this.useproxy;
  }

  public void setUseProxy( boolean useprox ) {
    this.useproxy = useprox;
  }

  public void setProxyUsername( String username ) {
    this.proxyusername = username;
  }

  public String getProxyUsername() {
    return this.proxyusername;
  }

  /**
   * @return Returns the usedynamicfolder.
   */
  public boolean isDynamicFolder() {
    return this.usedynamicfolder;
  }

  public void setDynamicFolder( boolean usedynamicfolder ) {
    this.usedynamicfolder = usedynamicfolder;
  }

  public void setRowLimit( String rowlimit ) {
    this.rowlimit = rowlimit;
  }

  public String getRowLimit() {
    return this.rowlimit;
  }

  public void setFolderField( String folderfield ) {
    this.folderfield = folderfield;
  }

  public String getFolderField() {
    return this.folderfield;
  }

  public void setFirstIMAPMails( String firstmails ) {
    this.imapfirstmails = firstmails;
  }

  public String getFirstIMAPMails() {
    return imapfirstmails;
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
    this.servername = servername;
  }

  public String getServerName() {
    return servername;
  }

  public void setUserName( String username ) {
    this.username = username;
  }

  public String getUserName() {
    return username;
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
    return imapfolder;
  }

  public void setIMAPFolder( String folder ) {
    this.imapfolder = folder;
  }

  /**
   * @param usessl
   *          The usessl to set.
   */
  public void setUseSSL( boolean usessl ) {
    this.usessl = usessl;
  }

  /**
   * @return Returns the usessl.
   */
  public boolean isUseSSL() {
    return usessl;
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
    this.usingAuthentication = usingAuthentication;
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
      HttpPost httpPost = new HttpPost( variables.environmentSubstitute(  tokenUrl ) );
      List<NameValuePair> form = new ArrayList<>();
      form.add(new BasicNameValuePair("scope", variables.environmentSubstitute( scope ) ) );
      form.add(new BasicNameValuePair("client_id", variables.environmentSubstitute( clientId ) ));
      form.add(new BasicNameValuePair("client_secret", variables.environmentSubstitute(  secretKey ) ) );
      form.add(new BasicNameValuePair("grant_type", grantType));
      if (grantType.equals(GRANTTYPE_REFRESH_TOKEN)) {
        form.add(new BasicNameValuePair(GRANTTYPE_REFRESH_TOKEN, variables.environmentSubstitute( refreshToken ) ) );
      }
      if (grantType.equals(GRANTTYPE_AUTHORIZATION_CODE)) {
        form.add(new BasicNameValuePair("code", variables.environmentSubstitute( authorizationCode ) ) );
        form.add(new BasicNameValuePair("redirect_uri", variables.environmentSubstitute(  redirectUri ) ) );
      }
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
      httpPost.setEntity(entity);
      try (CloseableHttpResponse response = client.execute(httpPost)) {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          throw new HttpException("Unable to get authorization token " + response.getStatusLine().toString());
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(EntityUtils.toString(response.getEntity()), EmailAuthenticationResponse.class);
      } catch (HttpException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
