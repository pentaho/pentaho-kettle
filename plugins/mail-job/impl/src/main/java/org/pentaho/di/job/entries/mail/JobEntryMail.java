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



package org.pentaho.di.job.entries.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.activation.URLDataSource;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.vfs2.FileObject;
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
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Describes a Mail Job Entry.
 *
 * @author Matt Created on 17-06-2003
 *
 */
@JobEntry( id = "MAIL", name = "JobEntry.Mail.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.mail",
        description = "JobEntry.Mail.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Mail",
        image = "ui/images/MAIL.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail" )
public class JobEntryMail extends JobEntryBase implements Cloneable, JobEntryInterface {

  public static final String TAG_FILETYPE = "filetype";
  public static final String TAG_IMAGE_NAME = "image_name";
  public static final String TAG_CONTENT_ID = "content_id";
  public static final String TAG_FILETYPES = "filetypes";
  public static final String TAG_EMBEDDED_IMAGES = "embeddedimages";
  public static final String TAG_EMBEDDED_IMAGE = "embeddedimage";
  private static Class<?> PKG = JobEntryMail.class; // for i18n purposes, needed by Translator2!!

  private static final String TAG_SERVER = "server";
  private static final String TAG_PORT = "port";
  private static final String TAG_DESTINATION = "destination";
  private static final String TAG_DESTINATION_CC = "destinationCc";
  private static final String TAG_DESTINATION_BCC = "destinationBCc";
  private static final String TAG_REPLYTO = "replyto";
  private static final String TAG_REPLYTONAME = "replytoname";
  private static final String TAG_SUBJECT = "subject";
  private static final String TAG_INCLUDE_DATE = "include_date";
  private static final String TAG_CONTACT_PERSON = "contact_person";
  private static final String TAG_CONTACT_PHONE = "contact_phone";
  private static final String TAG_COMMENT = "comment";
  private static final String TAG_INCLUDE_FILES = "include_files";
  private static final String TAG_ZIP_FILES = "zip_files";
  private static final String TAG_ZIP_NAME = "zip_name";
  private static final String TAG_USE_GRANT_TYPE = "use_grantType";
  private static final String TAG_USE_AUTH = "use_auth";
  private static final String TAG_USE_SECURE_AUTH = "use_secure_auth";
  private static final String TAG_AUTH_USER = "auth_user";
  private static final String TAG_AUTH_PASSWORD = "auth_password";
  private static final String TAG_AUTH_CLIENT_ID = "auth_clientId";
  private static final String TAG_AUTH_SECRET_KEY = "auth_secretKey";
  private static final String TAG_AUTH_SCOPE = "auth_scope";
  private static final String TAG_AUTH_TOKEN_URL = "auth_tokenUrl";
  private static final String TAG_AUTH_AUTHORIZATION_CODE = "auth_authorizationCode";
  private static final String TAG_REDIRECT_URI = "redirectURI";
  private static final String TAG_REFRESH_TOKEN = "refreshToken";
  private static final String TAG_ONLY_COMMENT = "only_comment";
  private static final String TAG_USE_HTML = "use_HTML";
  private static final String TAG_USE_PRIORITY = "use_Priority";
  private static final String TAG_ENCODING = "encoding";
  private static final String TAG_PRIORITY = "priority";
  private static final String TAG_IMPORTANCE = "importance";
  private static final String TAG_SENSITIVITY = "sensitivity";
  private static final String TAG_SECURE_CONNECTION_TYPE = "secureconnectiontype";
  private static final String TAG_REPLY_TO_ADDRESSES = "replyToAddresses";

  private static final String STR_10_SPACES = "          ";
  private static final String STR_8_SPACES = "        ";
  private static final String STR_6_SPACES = "      ";

  public static String AUTENTICATION_OAUTH = "OAUTH";

  public static String AUTENTICATION_BASIC = "Basic";

  public static String AUTENTICATION_NONE = "No Auth";

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

  private String server;

  private String destination;

  private String destinationCc;

  private String destinationBCc;

  /** Caution : It's sender address and NOT reply address **/
  private String replyAddress;

  /** Caution : It's sender name and NOT reply name **/
  private String replyName;

  private String subject;

  private boolean includeDate;

  private String contactPerson;

  private IEmailAuthenticationResponse token;

  private String contactPhone;

  private String comment;

  private boolean includingFiles;

  private int[] fileType;

  private boolean zipFiles;

  private String zipFilename;

  private String usingAuthentication;

  private String authenticationUser;

  private String authenticationPassword;

  private boolean onlySendComment;

  private boolean useHTML;

  private boolean usingSecureAuthentication;

  private String grant_type;

  private boolean usePriority;

  private String port;

  private String priority;

  private String importance;

  private String sensitivity;

  private String secureConnectionType;

  /** The encoding to use for reading: null or empty string means system default encoding */
  private String encoding;

  /** The reply to addresses */
  private String replyToAddresses;

  public String[] embeddedimages;

  public String[] contentids;

  public JobEntryMail( String n ) {
    super( n, "" );
    allocate( 0 );
  }

  public JobEntryMail() {
    this( "" );
    allocate( 0 );
  }

  public void allocate( int nrFileTypes ) {
    fileType = new int[nrFileTypes];
  }

  public void allocateImages( int nrImages ) {
    embeddedimages = new String[nrImages];
    contentids = new String[nrImages];
  }

  @Override
  public Object clone() {
    JobEntryMail je = (JobEntryMail) super.clone();
    if ( fileType != null ) {
      int nrFileTypes = fileType.length;
      je.allocate( nrFileTypes );
      System.arraycopy( fileType, 0, je.fileType, 0, nrFileTypes );
    }
    if ( embeddedimages != null ) {
      int nrImages = embeddedimages.length;
      je.allocateImages( nrImages );
      System.arraycopy( embeddedimages, 0, je.embeddedimages, 0, nrImages );
      System.arraycopy( contentids, 0, je.contentids, 0, nrImages );
    }
    return je;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 600 );

    retval.append( super.getXML() );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SERVER, server ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PORT, port ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DESTINATION, destination ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DESTINATION_CC, destinationCc ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DESTINATION_BCC, destinationBCc ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REPLYTO, replyAddress ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REPLYTONAME, replyName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SUBJECT, subject ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_DATE, includeDate ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_CONTACT_PERSON, contactPerson ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_CONTACT_PHONE, contactPhone ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_COMMENT, comment ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_FILES, includingFiles ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ZIP_FILES, zipFiles ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ZIP_NAME, zipFilename ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_GRANT_TYPE, grant_type) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_AUTH, usingAuthentication ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_SECURE_AUTH, usingSecureAuthentication ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_USER, authenticationUser ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_PASSWORD,
        Encr.encryptPasswordIfNotUsingVariables( authenticationPassword ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_CLIENT_ID, clientId ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_SECRET_KEY,  secretKey)  );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_SCOPE, scope ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_TOKEN_URL, tokenUrl ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_AUTHORIZATION_CODE, authorization_code ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REDIRECT_URI, redirectUri ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REFRESH_TOKEN, refresh_token) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ONLY_COMMENT, onlySendComment ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_HTML, useHTML ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_PRIORITY, usePriority ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ENCODING, encoding ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PRIORITY, priority ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_IMPORTANCE, importance ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SENSITIVITY, sensitivity ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SECURE_CONNECTION_TYPE, secureConnectionType ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REPLY_TO_ADDRESSES, replyToAddresses ) );

    retval.append( "      <filetypes>" );
    if ( fileType != null ) {
      for ( int j : fileType ) {
        retval.append( STR_8_SPACES ).append( XMLHandler.addTagValue( TAG_FILETYPE, ResultFile.getTypeCode( j ) ) );
      }
    }
    retval.append( "      </filetypes>" );

    retval.append( "      <embeddedimages>" ).append( Const.CR );
    if ( embeddedimages != null ) {
      for ( int i = 0; i < embeddedimages.length; i++ ) {
        retval.append( "        <embeddedimage>" ).append( Const.CR );
        retval.append( STR_10_SPACES ).append( XMLHandler.addTagValue( TAG_IMAGE_NAME, embeddedimages[i] ) );
        retval.append( STR_10_SPACES ).append( XMLHandler.addTagValue( TAG_CONTENT_ID, contentids[i] ) );
        retval.append( "        </embeddedimage>" ).append( Const.CR );
      }
    }
    retval.append( "      </embeddedimages>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      setServer( XMLHandler.getTagValue( entrynode, TAG_SERVER ) );
      setPort( XMLHandler.getTagValue( entrynode, TAG_PORT ) );
      setDestination( XMLHandler.getTagValue( entrynode, TAG_DESTINATION ) );
      setDestinationCc( XMLHandler.getTagValue( entrynode, TAG_DESTINATION_CC ) );
      setDestinationBCc( XMLHandler.getTagValue( entrynode, TAG_DESTINATION_BCC ) );
      setReplyAddress( XMLHandler.getTagValue( entrynode, TAG_REPLYTO ) );
      setReplyName( XMLHandler.getTagValue( entrynode, TAG_REPLYTONAME ) );
      setSubject( XMLHandler.getTagValue( entrynode, TAG_SUBJECT ) );
      setIncludeDate( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_INCLUDE_DATE ) ) );
      setContactPerson( XMLHandler.getTagValue( entrynode, TAG_CONTACT_PERSON ) );
      setContactPhone( XMLHandler.getTagValue( entrynode, TAG_CONTACT_PHONE ) );
      setComment( XMLHandler.getTagValue( entrynode, TAG_COMMENT ) );
      setIncludingFiles( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_INCLUDE_FILES ) ) );

      setUsingAuthentication( XMLHandler.getTagValue( entrynode, TAG_USE_AUTH ) );
      setUsingSecureAuthentication( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_USE_SECURE_AUTH ) ) );
      setAuthenticationUser( XMLHandler.getTagValue( entrynode, TAG_AUTH_USER ) );
      setAuthenticationPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(
              entrynode, TAG_AUTH_PASSWORD ) ) );
      setClientId( XMLHandler.getTagValue( entrynode, TAG_AUTH_CLIENT_ID ) );
      setSecretKey( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, TAG_AUTH_SECRET_KEY ) ) );
      setScope( XMLHandler.getTagValue( entrynode, TAG_AUTH_SCOPE ) );
      setTokenUrl( XMLHandler.getTagValue( entrynode, TAG_AUTH_TOKEN_URL ) );
      setAuthorization_code( XMLHandler.getTagValue( entrynode, TAG_AUTH_AUTHORIZATION_CODE ) );
      setRedirectUri( XMLHandler.getTagValue( entrynode, TAG_REDIRECT_URI ) );
      setRefresh_token( XMLHandler.getTagValue( entrynode, TAG_REFRESH_TOKEN ) );
      setGrant_type( XMLHandler.getTagValue( entrynode, TAG_USE_GRANT_TYPE ) );
      setOnlySendComment( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_ONLY_COMMENT ) ) );
      setUseHTML( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_USE_HTML ) ) );

      setUsePriority( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_USE_PRIORITY ) ) );

      setEncoding( XMLHandler.getTagValue( entrynode, TAG_ENCODING ) );
      setPriority( XMLHandler.getTagValue( entrynode, TAG_PRIORITY ) );
      setImportance( XMLHandler.getTagValue( entrynode, TAG_IMPORTANCE ) );
      setSensitivity( XMLHandler.getTagValue( entrynode, TAG_SENSITIVITY ) );
      setSecureConnectionType( XMLHandler.getTagValue( entrynode, TAG_SECURE_CONNECTION_TYPE ) );

      Node ftsnode = XMLHandler.getSubNode( entrynode, TAG_FILETYPES );
      int nrTypes = XMLHandler.countNodes( ftsnode, TAG_FILETYPE );
      allocate( nrTypes );
      for ( int i = 0; i < nrTypes; i++ ) {
        Node ftnode = XMLHandler.getSubNodeByNr( ftsnode, TAG_FILETYPE, i );
        fileType[i] = ResultFile.getType( XMLHandler.getNodeValue( ftnode ) );
      }

      setZipFiles( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_ZIP_FILES ) ) );
      setZipFilename( XMLHandler.getTagValue( entrynode, TAG_ZIP_NAME ) );
      setReplyToAddresses( XMLHandler.getTagValue( entrynode, TAG_REPLY_TO_ADDRESSES ) );

      Node images = XMLHandler.getSubNode( entrynode, TAG_EMBEDDED_IMAGES );

      // How many field embedded images ?
      int nrImages = XMLHandler.countNodes( images, TAG_EMBEDDED_IMAGE );
      allocateImages( nrImages );

      // Read them all...
      for ( int i = 0; i < nrImages; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( images, TAG_EMBEDDED_IMAGE, i );

        embeddedimages[i] = XMLHandler.getTagValue( fnode, TAG_IMAGE_NAME );
        contentids[i] = XMLHandler.getTagValue( fnode, TAG_CONTENT_ID );
      }
    } catch ( KettleException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'mail' from XML node", xe );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId idJobEntry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      // First load the common parts like name & description, then the attributes...
      //
      server = rep.getJobEntryAttributeString( idJobEntry, TAG_SERVER );
      port = rep.getJobEntryAttributeString( idJobEntry, TAG_PORT );
      destination = rep.getJobEntryAttributeString( idJobEntry, TAG_DESTINATION );
      destinationCc = rep.getJobEntryAttributeString( idJobEntry, TAG_DESTINATION_CC );
      destinationBCc = rep.getJobEntryAttributeString( idJobEntry, TAG_DESTINATION_BCC );
      replyAddress = rep.getJobEntryAttributeString( idJobEntry, TAG_REPLYTO );
      replyName = rep.getJobEntryAttributeString( idJobEntry, TAG_REPLYTONAME );
      subject = rep.getJobEntryAttributeString( idJobEntry, TAG_SUBJECT );
      includeDate = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_INCLUDE_DATE );
      contactPerson = rep.getJobEntryAttributeString( idJobEntry, TAG_CONTACT_PERSON );
      contactPhone = rep.getJobEntryAttributeString( idJobEntry, TAG_CONTACT_PHONE );
      comment = rep.getJobEntryAttributeString( idJobEntry, TAG_COMMENT );
      encoding = rep.getJobEntryAttributeString( idJobEntry, TAG_ENCODING );
      priority = rep.getJobEntryAttributeString( idJobEntry, TAG_PRIORITY );
      importance = rep.getJobEntryAttributeString( idJobEntry, TAG_IMPORTANCE );
      sensitivity = rep.getJobEntryAttributeString( idJobEntry, TAG_SENSITIVITY );
      includingFiles = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_INCLUDE_FILES );
      grant_type = rep.getJobEntryAttributeString( idJobEntry, TAG_USE_GRANT_TYPE );
      setUsingAuthentication( rep.getJobEntryAttributeString( idJobEntry, TAG_USE_AUTH ) );
      usingSecureAuthentication = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_USE_SECURE_AUTH );
      authenticationUser = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_USER );
      authenticationPassword =
              Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_PASSWORD ) );
      clientId = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_CLIENT_ID );
      secretKey =
              Encr.decryptPasswordOptionallyEncrypted(  rep.getJobEntryAttributeString( idJobEntry,
                TAG_AUTH_SECRET_KEY ) ) ;
      scope = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_SCOPE );
      tokenUrl = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_TOKEN_URL );
      authorization_code = rep.getJobEntryAttributeString( idJobEntry, TAG_AUTH_AUTHORIZATION_CODE );
      redirectUri= rep.getJobEntryAttributeString( idJobEntry, TAG_REDIRECT_URI );
      refresh_token = rep.getJobEntryAttributeString( idJobEntry, TAG_REFRESH_TOKEN );
      onlySendComment = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_ONLY_COMMENT );
      useHTML = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_USE_HTML );
      usePriority = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_USE_PRIORITY );
      secureConnectionType = rep.getJobEntryAttributeString( idJobEntry, TAG_SECURE_CONNECTION_TYPE );

      int nrTypes = rep.countNrJobEntryAttributes( idJobEntry, "file_type" );
      allocate( nrTypes );

      for ( int i = 0; i < nrTypes; i++ ) {
        String typeCode = rep.getJobEntryAttributeString( idJobEntry, i, "file_type" );
        fileType[i] = ResultFile.getType( typeCode );
      }

      zipFiles = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_ZIP_FILES );
      zipFilename = rep.getJobEntryAttributeString( idJobEntry, TAG_ZIP_NAME );
      replyToAddresses = rep.getJobEntryAttributeString( idJobEntry, TAG_REPLY_TO_ADDRESSES );

      // How many arguments?
      int imagesNr = rep.countNrJobEntryAttributes( idJobEntry, TAG_EMBEDDED_IMAGE );
      allocateImages( imagesNr );

      // Read them all...
      for ( int a = 0; a < imagesNr; a++ ) {
        embeddedimages[a] = rep.getJobEntryAttributeString( idJobEntry, a, TAG_EMBEDDED_IMAGE );
        contentids[a] = rep.getJobEntryAttributeString( idJobEntry, a, "contentid" );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'mail' from the repository with idJobEntry="
        + idJobEntry, dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SERVER, server );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_PORT, port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_DESTINATION, destination );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_DESTINATION_CC, destinationCc );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_DESTINATION_BCC, destinationBCc );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_REPLYTO, replyAddress );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_REPLYTONAME, replyName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SUBJECT, subject );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_INCLUDE_DATE, includeDate );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_CONTACT_PERSON, contactPerson );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_CONTACT_PHONE, contactPhone );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_COMMENT, comment );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_ENCODING, encoding );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_PRIORITY, priority );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_IMPORTANCE, importance );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SENSITIVITY, sensitivity );

      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_INCLUDE_FILES, includingFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_AUTH, usingAuthentication );
      rep.saveJobEntryAttribute(id_job,getObjectId(), TAG_USE_GRANT_TYPE,grant_type );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_SECURE_AUTH, usingSecureAuthentication );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_USER, authenticationUser );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_PASSWORD, Encr
              .encryptPasswordIfNotUsingVariables( authenticationPassword ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_CLIENT_ID, clientId );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_SECRET_KEY, Encr
              .encryptPasswordIfNotUsingVariables( secretKey ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_SCOPE, scope );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_TOKEN_URL, tokenUrl );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_AUTH_AUTHORIZATION_CODE, authorization_code );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_REDIRECT_URI,  redirectUri );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_REFRESH_TOKEN, refresh_token );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_ONLY_COMMENT, onlySendComment );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_HTML, useHTML );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_USE_PRIORITY, usePriority );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_SECURE_CONNECTION_TYPE, secureConnectionType );

      if ( fileType != null ) {
        for ( int i = 0; i < fileType.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "file_type", ResultFile.getTypeCode( fileType[i] ) );
        }
      }

      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_ZIP_FILES, zipFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_ZIP_NAME, zipFilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TAG_REPLY_TO_ADDRESSES, replyToAddresses );

      // save the arguments...
      if ( embeddedimages != null ) {
        for ( int i = 0; i < embeddedimages.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, TAG_EMBEDDED_IMAGE, embeddedimages[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "contentid", contentids[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'mail' to the repository for id_job=" + id_job, dbe );
    }
  }

  public void setServer( String s ) {
    server = s;
  }

  public String getServer() {
    return server;
  }

  public void setDestination( String dest ) {
    destination = dest;
  }

  public void setDestinationCc( String destCc ) {
    destinationCc = destCc;
  }

  public void setDestinationBCc( String destBCc ) {
    destinationBCc = destBCc;
  }

  public String getDestination() {
    return destination;
  }

  public String getDestinationCc() {
    return destinationCc;
  }

  public String getDestinationBCc() {
    return destinationBCc;
  }

  public void setReplyAddress( String reply ) {
    replyAddress = reply;
  }

  public String getReplyAddress() {
    return replyAddress;
  }

  public void setReplyName( String replyName ) {
    this.replyName = replyName;
  }

  public String getReplyName() {
    return replyName;
  }

  public void setSubject( String subj ) {
    subject = subj;
  }

  public String getSubject() {
    return subject;
  }

  public void setIncludeDate( boolean incl ) {
    includeDate = incl;
  }

  public boolean getIncludeDate() {
    return includeDate;
  }

  public void setContactPerson( String person ) {
    contactPerson = person;
  }

  public String getContactPerson() {
    return contactPerson;
  }

  public void setContactPhone( String phone ) {
    contactPhone = phone;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setComment( String comm ) {
    comment = comm;
  }

  public String getComment() {
    return comment;
  }
  public void setGrant_type( String grant_type ) {
    this.grant_type = grant_type;
  }

  public String getGrant_type() {
    return grant_type;
  }

  /**
   * @return the result file types to select for attachment </b>
   * @see ResultFile
   */
  public int[] getFileType() {
    return fileType;
  }

  /**
   * @param fileType
   *          the result file types to select for attachment
   * @see ResultFile
   */
  public void setFileType( int[] fileType ) {
    this.fileType = fileType;
  }

  public boolean isIncludingFiles() {
    return includingFiles;
  }

  public void setIncludingFiles( boolean includeFiles ) {
    this.includingFiles = includeFiles;
  }

  /**
   * @return Returns the zipFilename.
   */
  public String getZipFilename() {
    return zipFilename;
  }

  /**
   * @param zipFilename
   *          The zipFilename to set.
   */
  public void setZipFilename( String zipFilename ) {
    this.zipFilename = zipFilename;
  }

  /**
   * @return Returns the zipFiles.
   */
  public boolean isZipFiles() {
    return zipFiles;
  }

  /**
   * @param zipFiles
   *          The zipFiles to set.
   */
  public void setZipFiles( boolean zipFiles ) {
    this.zipFiles = zipFiles;
  }

  /**
   * @return Returns the authenticationPassword.
   */
  public String getAuthenticationPassword() {
    return authenticationPassword;
  }

  /**
   * @param authenticationPassword
   *          The authenticationPassword to set.
   */
  public void setAuthenticationPassword( String authenticationPassword ) {
    this.authenticationPassword = authenticationPassword;
  }

  /**
   * @return Returns the authenticationUser.
   */
  public String getAuthenticationUser() {
    return authenticationUser;
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

  /**
   * @param authenticationUser
   *          The authenticationUser to set.
   */
  public void setAuthenticationUser( String authenticationUser ) {
    this.authenticationUser = authenticationUser;
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
    } else if ( AUTENTICATION_BASIC.equalsIgnoreCase( usingAuthentication )
                  || "Y".equalsIgnoreCase( usingAuthentication ) ) {
      this.usingAuthentication = AUTENTICATION_BASIC;
    } else {
      // All other cases: "No Auth" (valid option), "N" (old option), null or unrecognized option
      this.usingAuthentication = AUTENTICATION_NONE;
    }
  }

  /**
   * @return the onlySendComment flag
   */
  public boolean isOnlySendComment() {
    return onlySendComment;
  }

  /**
   * @param onlySendComment
   *          the onlySendComment flag to set
   */
  public void setOnlySendComment( boolean onlySendComment ) {
    this.onlySendComment = onlySendComment;
  }

  /**
   * @return the useHTML flag
   */
  public boolean isUseHTML() {
    return useHTML;
  }

  /**
   * @param useHTML
   *          the useHTML to set
   */
  public void setUseHTML( boolean useHTML ) {
    this.useHTML = useHTML;
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @return the secure connection type
   */
  public String getSecureConnectionType() {
    return secureConnectionType;
  }

  /**
   * @param secureConnectionType
   *          the secure connection type to set
   */
  public void setSecureConnectionType( String secureConnectionType ) {
    this.secureConnectionType = secureConnectionType;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @param replyToAddresses
   *          the replayToAddresses to set
   */
  public void setReplyToAddresses( String replyToAddresses ) {
    this.replyToAddresses = replyToAddresses;
  }

  /**
   * @return replayToAddresses
   */
  public String getReplyToAddresses() {
    return this.replyToAddresses;
  }

  /**
   * @param usePriority
   *          the usePriority to set
   */
  public void setUsePriority( boolean usePriority ) {
    this.usePriority = usePriority;
  }

  /**
   * @return the usePriority flag
   */
  public boolean isUsePriority() {
    return usePriority;
  }

  /**
   * @return the priority
   */
  public String getPriority() {
    return priority;
  }

  /**
   * @param importance
   *          the importance to set
   */
  public void setImportance( String importance ) {
    this.importance = importance;
  }

  /**
   * @return the importance
   */
  public String getImportance() {
    return importance;
  }

  public String getSensitivity() {
    return sensitivity;
  }

  public void setSensitivity( String sensitivity ) {
    this.sensitivity = sensitivity;
  }

  /**
   * @param priority
   *          the priority to set
   */
  public void setPriority( String priority ) {
    this.priority = priority;
  }

  @Override
  public Result execute( Result result, int nr ) {
    File masterZipfile = null;

    // Send an e-mail...
    // create some properties and get the default Session
    Properties props = new Properties();
    if ( Utils.isEmpty( server ) ) {
      logError( BaseMessages.getString( PKG, "JobMail.Error.HostNotSpecified" ) );

      result.setNrErrors( 1L );
      result.setResult( false );
      return result;
    }

    String protocol = "smtp";
    if ( usingSecureAuthentication ) {
      if ( secureConnectionType.equals( "TLS" ) ) {
        // Allow TLS authentication
        props.put( "mail.smtp.starttls.enable", "true" );
      } else {
        protocol = "smtps";
        // required to get rid of an SSL exception :
        // nested exception is:
        // javax.net.ssl.SSLException: Unsupported record version Unknown
        props.put( "mail.smtps.quitwait", "false" );
      }
    }

    props.put( "mail." + protocol + ".host", environmentSubstitute( server ) );
    if ( !Utils.isEmpty( port ) ) {
      props.put( "mail." + protocol + ".port", environmentSubstitute( port ) );
    }

    if ( log.isDebug() ) {
      props.put( "mail.debug", "true" );
    }

    if ( usingAuthentication.equals( AUTENTICATION_BASIC ) ) {
      props.put( "mail." + protocol + ".auth", "true" );
    } else if ( usingAuthentication.equals( JobEntryMail.AUTENTICATION_OAUTH ) ) {
      token = getOauthToken( environmentSubstitute( tokenUrl ) );
      props.put( "mail." + protocol + ".auth.xoauth2.disable", "false" );
      props.put( "mail." + protocol + ".auth.mechanisms", "XOAUTH2" );
      props.put( "mail.transport.protocol", "smtp" );
      props.put( "mail." + protocol + ".auth.login.disable", "true" );
      props.put( "mail." + protocol + ".auth.plain.disable", "true" );
      props.setProperty( "mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
    }

    Session session = Session.getInstance( props );
    session.setDebug( log.isDebug() );

    try {
      // create a message
      Message msg = new MimeMessage( session );

      // set message priority
      if ( usePriority ) {
        String priorityAsInt = "1";
        if ( priority.equals( "low" ) ) {
          priorityAsInt = "3";
        } else if ( priority.equals( "normal" ) ) {
          priorityAsInt = "2";
        }

        msg.setHeader( "X-Priority", priorityAsInt ); // (String)int between 1= high and 3 = low.
        msg.setHeader( "Importance", importance );
        // seems to be needed for MS Outlook.
        // where it returns a string of high /normal /low.
        msg.setHeader( "Sensitivity", sensitivity );
        // Possible values are normal, personal, private, company-confidential
      }

      // Set Mail sender (From)
      String senderAddress = environmentSubstitute( replyAddress );
      if ( !Utils.isEmpty( senderAddress ) ) {
        String senderName = environmentSubstitute( replyName );
        if ( !Utils.isEmpty( senderName ) ) {
          senderAddress = senderName + '<' + senderAddress + '>';
        }
        msg.setFrom( new InternetAddress( senderAddress ) );
      } else {
        throw new MessagingException( BaseMessages.getString( PKG, "JobMail.Error.ReplyEmailNotFilled" ) );
      }

      // set Reply to addresses
      String reply_to_address = environmentSubstitute( replyToAddresses );
      if ( !Utils.isEmpty( reply_to_address ) ) {
        // Split the mail-address: space separated
        String[] reply_Address_List = environmentSubstitute( reply_to_address ).split( " " );
        InternetAddress[] address = new InternetAddress[reply_Address_List.length];
        for ( int i = 0; i < reply_Address_List.length; i++ ) {
          address[i] = new InternetAddress( reply_Address_List[i] );
        }
        msg.setReplyTo( address );
      }

      // Split the mail-address: space separated
      String[] destinations = environmentSubstitute( destination ).split( " " );
      InternetAddress[] address = new InternetAddress[destinations.length];
      for ( int i = 0; i < destinations.length; i++ ) {
        address[i] = new InternetAddress( destinations[i] );
      }
      msg.setRecipients( Message.RecipientType.TO, address );

      String realCC = environmentSubstitute( getDestinationCc() );
      if ( !Utils.isEmpty( realCC ) ) {
        // Split the mail-address Cc: space separated
        String[] destinationsCc = realCC.split( " " );
        InternetAddress[] addressCc = new InternetAddress[destinationsCc.length];
        for ( int i = 0; i < destinationsCc.length; i++ ) {
          addressCc[i] = new InternetAddress( destinationsCc[i] );
        }

        msg.setRecipients( Message.RecipientType.CC, addressCc );
      }

      String realBCc = environmentSubstitute( getDestinationBCc() );
      if ( !Utils.isEmpty( realBCc ) ) {
        // Split the mail-address BCc: space separated
        String[] destinationsBCc = realBCc.split( " " );
        InternetAddress[] addressBCc = new InternetAddress[destinationsBCc.length];
        for ( int i = 0; i < destinationsBCc.length; i++ ) {
          addressBCc[i] = new InternetAddress( destinationsBCc[i] );
        }

        msg.setRecipients( Message.RecipientType.BCC, addressBCc );
      }
      String realSubject = environmentSubstitute( subject );
      if ( !Utils.isEmpty( realSubject ) ) {
        msg.setSubject( realSubject );
      }

      msg.setSentDate( new Date() );
      StringBuilder messageText = new StringBuilder();
      String endRow = isUseHTML() ? "<br>" : Const.CR;
      String realComment = environmentSubstitute( comment );
      if ( !Utils.isEmpty( realComment ) ) {
        messageText.append( realComment ).append( Const.CR ).append( Const.CR );
      }
      if ( !onlySendComment ) {

        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Job" ) ).append( endRow )
          .append( "-----" ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobName" ) ).append( "    : " )
          .append( parentJob.getJobMeta().getName() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobDirectory" ) ).append( "  : " )
          .append( parentJob.getJobMeta().getRepositoryDirectory() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobEntry" ) ).append( "   : " )
          .append( getName() ).append( endRow );
        messageText.append( Const.CR );
      }

      if ( includeDate ) {
        messageText
          .append( endRow ).append( BaseMessages.getString( PKG, "JobMail.Log.Comment.MsgDate" ) ).append( ": " )
          .append( XMLHandler.date2string( new Date() ) ).append( endRow ).append( endRow );
      }
      if ( !onlySendComment && result != null ) {
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.PreviousResult" ) ).append( ":" )
          .append( endRow ).append( "-----------------" ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobEntryNr" ) ).append( "         : " )
          .append( result.getEntryNr() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Errors" ) ).append( "               : " )
          .append( result.getNrErrors() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesRead" ) ).append( "           : " )
          .append( result.getNrLinesRead() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesWritten" ) ).append( "        : " )
          .append( result.getNrLinesWritten() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesInput" ) ).append( "          : " )
          .append( result.getNrLinesInput() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesOutput" ) ).append( "         : " )
          .append( result.getNrLinesOutput() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesUpdated" ) ).append( "        : " )
          .append( result.getNrLinesUpdated() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesRejected" ) ).append( "       : " )
          .append( result.getNrLinesRejected() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Status" ) ).append( "  : " )
          .append( result.getExitStatus() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Result" ) ).append( "               : " )
          .append( result.getResult() ).append( endRow );
        messageText.append( endRow );
      }

      if ( !onlySendComment
        && ( !Utils.isEmpty( environmentSubstitute( contactPerson ) ) || !Utils
          .isEmpty( environmentSubstitute( contactPhone ) ) ) ) {
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.ContactInfo" ) ).append( " :" )
          .append( endRow ).append( "---------------------" ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.PersonToContact" ) ).append( " : " )
          .append( environmentSubstitute( contactPerson ) ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Tel" ) ).append( "  : " )
          .append( environmentSubstitute( contactPhone ) ).append( endRow );
        messageText.append( endRow );
      }

      // Include the path to this job entry...
      if ( !onlySendComment ) {
        JobTracker jobTracker = parentJob.getJobTracker();
        if ( jobTracker != null ) {
          messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.PathToJobentry" ) ).append( ':' )
            .append( endRow ).append( "------------------------" ).append( endRow );

          addBacktracking( jobTracker, messageText );
          if ( isUseHTML() ) {
            messageText.replace( 0, messageText.length(), messageText.toString().replace( Const.CR, endRow ) );
          }
        }
      }

      MimeMultipart parts = new MimeMultipart();
      MimeBodyPart part1 = new MimeBodyPart(); // put the text in the
      // Attached files counter
      int nrAttachedFiles = 0;

      // 1st part

      if ( useHTML ) {
        if ( !Utils.isEmpty( getEncoding() ) ) {
          part1.setContent( messageText.toString(), "text/html; " + "charset=" + getEncoding() );
        } else {
          part1.setContent( messageText.toString(), "text/html; " + "charset=ISO-8859-1" );
        }
      } else {
        part1.setText( messageText.toString() );
      }

      parts.addBodyPart( part1 );

      if ( includingFiles && result != null ) {
        List<ResultFile> resultFiles = result.getResultFilesList();
        if ( resultFiles != null && !resultFiles.isEmpty() ) {
          if ( !zipFiles ) {
            // Add all files to the message...
            //
            for ( ResultFile resultFile : resultFiles ) {
              FileObject file = resultFile.getFile();
              if ( file != null && file.exists() ) {
                for ( int j : fileType ) {
                  if ( j == resultFile.getType() ) {
                    // create a data source
                    MimeBodyPart files = new MimeBodyPart();
                    URLDataSource fds = new URLDataSource( file.getURL() );

                    // get a data Handler to manipulate this file type;
                    files.setDataHandler( new DataHandler( fds ) );
                    // include the file in the data source
                    files.setFileName( file.getName().getBaseName() );

                    // insist on base64 to preserve line endings
                    files.addHeader( "Content-Transfer-Encoding", "base64" );

                    // add the part with the file in the BodyPart();
                    parts.addBodyPart( files );
                    nrAttachedFiles++;
                    logBasic( "Added file '" + fds.getName() + "' to the mail message." );
                    break;
                  }
                }
              }
            }
          } else {
            // create a single ZIP archive of all files
            masterZipfile =
              new File( System.getProperty( "java.io.tmpdir" )
                + Const.FILE_SEPARATOR + environmentSubstitute( zipFilename ) );
            ZipOutputStream zipOutputStream = null;
            try {
              zipOutputStream = new ZipOutputStream( new FileOutputStream( masterZipfile ) );

              for ( ResultFile resultFile : resultFiles ) {
                for ( int j : fileType ) {
                  if ( j == resultFile.getType() ) {
                    FileObject file = resultFile.getFile();
                    ZipEntry zipEntry = new ZipEntry( file.getName().getBaseName() );
                    zipOutputStream.putNextEntry( zipEntry );

                    // Now put the content of this file into this archive...
                    try (
                      BufferedInputStream inputStream = new BufferedInputStream( KettleVFS.getInputStream( file ) ) ) {
                      int c;
                      while ( ( c = inputStream.read() ) >= 0 ) {
                        zipOutputStream.write( c );
                      }
                    }
                    zipOutputStream.closeEntry();
                    nrAttachedFiles++;
                    logBasic( "Added file '" + file.getName().getURI() + "' to the mail message in a zip archive." );
                    break;
                  }
                }
              }
            } catch ( Exception e ) {
              logError( "Error zipping attachment files into file ["
                + masterZipfile.getPath() + "] : " + e.toString() );
              logError( Const.getStackTracker( e ) );
              result.setNrErrors( 1 );
            } finally {
              if ( zipOutputStream != null ) {
                try {
                  zipOutputStream.finish();
                  zipOutputStream.close();
                } catch ( IOException e ) {
                  logError( "Unable to close attachment zip file archive : " + e.toString() );
                  logError( Const.getStackTracker( e ) );
                  result.setNrErrors( 1 );
                }
              }
            }

            // Now attach the master zip file to the message.
            if ( result.getNrErrors() == 0 ) {
              // create a data source
              MimeBodyPart files = new MimeBodyPart();
              FileDataSource fds = new FileDataSource( masterZipfile );
              // get a data Handler to manipulate this file type;
              files.setDataHandler( new DataHandler( fds ) );
              // include the file in the data source
              files.setFileName( fds.getName() );
              // add the part with the file in the BodyPart();
              parts.addBodyPart( files );
            }
          }
        }
      }

      int nrEmbeddedImages = 0;
      if ( embeddedimages != null && embeddedimages.length > 0 ) {
        FileObject imageFile = null;
        for ( int i = 0; i < embeddedimages.length; i++ ) {
          String realImageFile = environmentSubstitute( embeddedimages[i] );
          String realcontenID = environmentSubstitute( contentids[i] );
          if ( messageText.indexOf( "cid:" + realcontenID ) < 0 ) {
            if ( log.isDebug() ) {
              log.logDebug( "Image [" + realImageFile + "] is not used in message body!" );
            }
          } else {
            try {
              boolean found = false;
              imageFile = KettleVFS.getInstance( parentJobMeta.getBowl() ).getFileObject( realImageFile, this );
              if ( imageFile.exists() && imageFile.getType() == FileType.FILE ) {
                found = true;
              } else {
                log.logError( "We can not find [" + realImageFile + "] or it is not a file" );
              }
              if ( found ) {
                // Create part for the image
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                // Load the image
                URLDataSource fds = new URLDataSource( imageFile.getURL() );
                messageBodyPart.setDataHandler( new DataHandler( fds ) );
                // Setting the header
                messageBodyPart.setHeader( "Content-ID", "<" + realcontenID + ">" );
                // Add part to multi-part
                parts.addBodyPart( messageBodyPart );
                nrEmbeddedImages++;
                log.logBasic( "Image '" + fds.getName() + "' was embedded in message." );
              }
            } catch ( Exception e ) {
              log.logError( "Error embedding image [" + realImageFile + "] in message : " + e.toString() );
              log.logError( Const.getStackTracker( e ) );
              result.setNrErrors( 1 );
            } finally {
              if ( imageFile != null ) {
                try {
                  imageFile.close();
                } catch ( Exception e ) { /* Ignore */
                }
              }
            }
          }
        }
      }

      if ( nrEmbeddedImages > 0 && nrAttachedFiles == 0 ) {
        // If we need to embedd images...
        // We need to create a "multipart/related" message.
        // otherwise image will appear as attached file
        parts.setSubType( "related" );
      }
      // put all parts together
      msg.setContent( parts );

      try ( Transport transport = session.getTransport( protocol ) ) {
        if ( usingAuthentication.equals( AUTENTICATION_BASIC ) ) {
          String authPass = getPassword( authenticationPassword );

          if ( !Utils.isEmpty( port ) ) {
            transport.connect(
              environmentSubstitute( Const.NVL( server, "" ) ),
              Integer.parseInt( environmentSubstitute( Const.NVL( port, "" ) ) ),
              environmentSubstitute( Const.NVL( authenticationUser, "" ) ),
              authPass );
          } else {
            transport.connect(
              environmentSubstitute( Const.NVL( server, "" ) ),
              environmentSubstitute( Const.NVL( authenticationUser, "" ) ),
              authPass );
          }
        } else if ( usingAuthentication.equals( JobEntryMail.AUTENTICATION_OAUTH ) ) {
          if ( !Utils.isEmpty( port ) ) {
            transport.connect(
              environmentSubstitute( Const.NVL( server, "" ) ),
              Integer.parseInt( environmentSubstitute( Const.NVL( port, "" ) ) ),
              environmentSubstitute( Const.NVL( authenticationUser, "" ) ),
              this.token.getAccessToken() );
          } else {
            transport.connect(
              environmentSubstitute( Const.NVL( server, "" ) ),
              environmentSubstitute( Const.NVL( authenticationUser, "" ) ),
              this.token.getAccessToken() );
          }
        } else {
          transport.connect();
        }
        transport.sendMessage( msg, msg.getAllRecipients() );
      }
    } catch ( IOException e ) {
      logError( "Problem while sending message: " + e.toString() );
      result.setNrErrors( 1 );
    } catch ( MessagingException mex ) {
      logError( "Problem while sending message: " + mex.toString() );
      result.setNrErrors( 1 );

      Exception ex = mex;
      do {
        if ( ex instanceof SendFailedException ) {
          SendFailedException sfex = (SendFailedException) ex;

          Address[] invalid = sfex.getInvalidAddresses();
          if ( invalid != null ) {
            logError( "    ** Invalid Addresses" );
            for ( Address invalidAddress : invalid ) {
              logError( "         " + invalidAddress );
              result.setNrErrors( 1 );
            }
          }

          Address[] validUnsent = sfex.getValidUnsentAddresses();
          if ( validUnsent != null ) {
            logError( "    ** ValidUnsent Addresses" );
            for ( Address validUnsentAddress : validUnsent ) {
              logError( "         " + validUnsentAddress );
              result.setNrErrors( 1 );
            }
          }

          Address[] validSent = sfex.getValidSentAddresses();
          if ( validSent != null ) {
            // System.out.println("    ** ValidSent Addresses");
            for ( Address validSentAddress : validSent ) {
              logError( "         " + validSentAddress );
              result.setNrErrors( 1 );
            }
          }
        }
        if ( ex instanceof MessagingException ) {
          ex = ( (MessagingException) ex ).getNextException();
        } else {
          ex = null;
        }
      } while ( ex != null );
    } finally {
      if ( masterZipfile != null && masterZipfile.exists() ) {
        masterZipfile.delete();
      }
    }

    result.setResult( result.getNrErrors() <= 0 );

    return result;
  }

  private void addBacktracking( JobTracker jobTracker, StringBuilder messageText ) {
    addBacktracking( jobTracker, messageText, 0 );
  }

  private void addBacktracking( JobTracker jobTracker, StringBuilder messageText, int level ) {
    int nr = jobTracker.nrJobTrackers();

    messageText.append( Const.rightPad( " ", level * 2 ) );
    messageText.append( Const.NVL( jobTracker.getJobName(), "-" ) );
    JobEntryResult jer = jobTracker.getJobEntryResult();
    if ( jer != null ) {
      messageText.append( " : " );
      if ( jer.getJobEntryName() != null ) {
        messageText.append( " : " ).append( jer.getJobEntryName() );
      }
      if ( jer.getResult() != null ) {
        messageText.append( " : " );
        messageText.append( '[' ).append( jer.getResult().toString() ).append( ']' );
      }
      if ( jer.getReason() != null ) {
        messageText.append( " : " ).append( jer.getReason() );
      }
      if ( jer.getComment() != null ) {
        messageText.append( " : " ).append( jer.getComment() );
      }
      if ( jer.getLogDate() != null ) {
        messageText.append( " (" );
        messageText.append( XMLHandler.date2string( jer.getLogDate() ) );
        messageText.append( ')' );
      }
    }
    messageText.append( Const.CR );

    for ( int i = 0; i < nr; i++ ) {
      JobTracker jt = jobTracker.getJobTracker( i );
      addBacktracking( jt, messageText, level + 1 );
    }
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return true;
  }

  /**
   * @return the usingSecureAuthentication
   */
  public boolean isUsingSecureAuthentication() {
    return usingSecureAuthentication;
  }

  /**
   * @param usingSecureAuthentication
   *          the usingSecureAuthentication to set
   */
  public void setUsingSecureAuthentication( boolean usingSecureAuthentication ) {
    this.usingSecureAuthentication = usingSecureAuthentication;
  }

  /**
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( String port ) {
    this.port = port;
  }

  IEmailAuthenticationResponse getOauthToken( String tokenUrl ) {
    try ( CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient() ) {
      this.tokenUrl = environmentSubstitute( tokenUrl );
      HttpPost httpPost = new HttpPost( this.tokenUrl );
      List<NameValuePair> form = new ArrayList<>();
      form.add( new BasicNameValuePair( "scope", environmentSubstitute( scope ) ) );
      form.add( new BasicNameValuePair( "client_id", environmentSubstitute( clientId ) ) );
      form.add( new BasicNameValuePair( "client_secret", environmentSubstitute( secretKey ) ) );
      form.add( new BasicNameValuePair( "grant_type", environmentSubstitute( grant_type ) ) );
      if ( grant_type.equals( JobEntryMail.GRANTTYPE_REFRESH_TOKEN ) ) {
        form.add( new BasicNameValuePair( JobEntryMail.GRANTTYPE_REFRESH_TOKEN, environmentSubstitute( refresh_token ) ) );
      }
      if ( grant_type.equals( JobEntryMail.GRANTTYPE_AUTHORIZATION_CODE ) ) {
        form.add( new BasicNameValuePair( "code", environmentSubstitute( authorization_code ) ) );
        form.add( new BasicNameValuePair( "redirect_uri", environmentSubstitute( redirectUri ) ) );
      }
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity( form, Consts.UTF_8 );
      httpPost.setEntity( entity );
      try ( CloseableHttpResponse response = client.execute( httpPost ) ) {
        if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ) {
          throw new HttpException( "Unable to get authorization token " + response.getStatusLine().toString() );
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue( EntityUtils.toString( response.getEntity() ), EmailAuthenticationResponse.class );
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
    String realServername = jobMeta.environmentSubstitute( server );
    ResourceReference reference = new ResourceReference( this );
    reference.getEntries().add( new ResourceEntry( realServername, ResourceType.SERVER ) );
    references.add( reference );
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, TAG_SERVER, remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator()
      .validate( jobMeta.getBowl(), this, "replyAddress", remarks, AndValidator.putValidators(
          JobEntryValidatorUtils.notBlankValidator(), JobEntryValidatorUtils.emailValidator() ) );

    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, TAG_DESTINATION, remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );

    if ( usingAuthentication.equals( AUTENTICATION_BASIC ) ) {
      JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "authenticationUser", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
      JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "authenticationPassword", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
    }

    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, TAG_PORT, remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

  public String getPassword( String authPassword ) {
    return Encr.decryptPasswordOptionallyEncrypted(
        environmentSubstitute( Const.NVL( authPassword, "" ) ) );
  }
}
