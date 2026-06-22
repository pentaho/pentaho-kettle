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


package org.pentaho.di.trans.steps.mail;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
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

import java.util.List;

/**
 * Send mail step. based on Mail job entry
 *
 * @author Samatar
 * @since 28-07-2008
 */

@Step( id = "Mail", name = "BaseStep.TypeLongDesc.Mail",
        i18nPackageName = "org.pentaho.di.trans.step.mail",
        description = "BaseStep.TypeTooltipDesc.Mail",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Utility",
        image = "ui/images/MAIL.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail+%28step%29" )
public class MailMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MailMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String TAG_SERVER = "server";
  private static final String TAG_PORT = "port";
  private static final String TAG_DESTINATION = "destination";
  private static final String TAG_DESTINATION_CC = "destinationCc";
  private static final String TAG_DESTINATION_BCC = "destinationBCc";
  private static final String TAG_REPLYTO_ADDRESSES = "replyToAddresses";
  private static final String TAG_REPLYTO = "replyto";
  private static final String TAG_REPLYTO_NAME = "replytoname";
  private static final String TAG_SUBJECT = "subject";
  private static final String TAG_INCLUDE_DATE = "include_date";
  private static final String TAG_INCLUDE_SUBFOLDERS = "include_subfolders";
  private static final String TAG_ZIP_FILENAME_DYNAMIC = "zipFilenameDynamic";
  private static final String TAG_IS_FILENAME_DYNAMIC = "isFilenameDynamic";
  private static final String TAG_DYNAMIC_FIELDNAME = "dynamicFieldname";
  private static final String TAG_DYNAMIC_WILDCARD = "dynamicWildcard";
  private static final String TAG_ATTACH_CONTENT_FROM_FIELD = "attachContentFromField";
  private static final String TAG_ATTACH_CONTENT_FIELD = "attachContentField";
  private static final String TAG_ATTACH_CONTENT_FILE_NAME_FIELD = "attachContentFileNameField";
  private static final String TAG_DYNAMIC_ZIP_FILENAME = "dynamicZipFilename";
  private static final String TAG_SOURCE_FILE_FOLDER_NAME = "sourcefilefoldername";
  private static final String TAG_SOURCE_WILDCARD = "sourcewildcard";
  private static final String TAG_CONTACT_PERSON = "contact_person";
  private static final String TAG_CONTACT_PHONE = "contact_phone";
  private static final String TAG_COMMENT = "comment";
  private static final String TAG_INCLUDE_FILES = "include_files";
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
  private static final String TAG_USE_GRANT_TYPE = "use_grantType";
  private static final String TAG_ONLY_COMMENT = "only_comment";
  private static final String TAG_USE_HTML = "use_HTML";
  private static final String TAG_USE_PRIORITY = "use_Priority";
  private static final String TAG_ENCODING = "encoding";
  private static final String TAG_PRIORITY = "priority";
  private static final String TAG_IMPORTANCE = "importance";
  private static final String TAG_SENSITIVITY = "sensitivity";
  private static final String TAG_SECURE_CONNECTION_TYPE = "secureconnectiontype";
  private static final String TAG_ZIP_FILES = "zip_files";
  private static final String TAG_ZIP_NAME = "zip_name";
  private static final String TAG_ZIP_LIMIT_SIZE = "zip_limit_size";
  private static final String TAG_EMBEDDED_IMAGES = "embeddedimages";
  private static final String TAG_EMBEDDED_IMAGE = "embeddedimage";
  private static final String TAG_IMAGE_NAME = "image_name";
  private static final String TAG_CONTENT_ID = "content_id";

  private static final String STR_10_SPACES = "          ";
  private static final String STR_8_SPACES = "        ";
  private static final String STR_6_SPACES = "      ";
  private static final String STR_4_SPACES = "    ";
  private static final String STR_Y = "Y";

  private String server;

  private String destination;

  private String destinationCc;

  private String destinationBCc;

  /** Caution : this is not the reply to addresses but the mail sender name */
  private String replyAddress;

  /** Caution : this is not the reply to addresses but the mail sender */
  private String replyName;

  private String subject;

  public static String AUTENTICATION_OAUTH = "OAuth";

  public static String AUTENTICATION_BASIC = "Basic";

  public static String AUTENTICATION_NONE = "No Auth";

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

  private boolean includeDate;

  private boolean includeSubFolders;

  private boolean zipFilenameDynamic;

  private boolean isFilenameDynamic;

  private String dynamicFieldName;

  private String dynamicWildcard;

  private String dynamicZipFilename;

  private String sourceFileFolderName;

  private String sourceWildcard;

  private String contactPerson;

  private String contactPhone;

  private String comment;

  private boolean includingFiles;

  private boolean zipFiles;

  private String zipFilename;

  private String zipLimitSize;

  private String usingAuthentication;

  private String authenticationUser;

  private String authenticationPassword;

  private boolean onlySendComment;

  private boolean useHTML;

  private boolean usingSecureAuthentication;

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

  private String[] embeddedImages;

  private String[] contentIds;

  /** Flag : attach file from content defined in a field **/
  private boolean attachContentFromField;

  /** file content field name **/
  private String attachContentField;

  /** filename content field **/
  private String attachContentFileNameField;

  public MailMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    return super.clone();
  }

  public void allocate( int value ) {
    this.embeddedImages = new String[value];
    this.contentIds = new String[value];
  }

  private void readData( Node stepnode ) {
    setServer( XMLHandler.getTagValue( stepnode, TAG_SERVER ) );
    setPort( XMLHandler.getTagValue( stepnode, TAG_PORT ) );
    setDestination( XMLHandler.getTagValue( stepnode, TAG_DESTINATION ) );
    setDestinationCc( XMLHandler.getTagValue( stepnode, TAG_DESTINATION_CC ) );
    setDestinationBCc( XMLHandler.getTagValue( stepnode, TAG_DESTINATION_BCC ) );
    setReplyToAddresses( XMLHandler.getTagValue( stepnode, TAG_REPLYTO_ADDRESSES ) );
    setReplyAddress( XMLHandler.getTagValue( stepnode, TAG_REPLYTO ) );
    setReplyName( XMLHandler.getTagValue( stepnode, TAG_REPLYTO_NAME ) );
    setSubject( XMLHandler.getTagValue( stepnode, TAG_SUBJECT ) );
    setIncludeDate( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_INCLUDE_DATE ) ) );
    setIncludeSubFolders( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_INCLUDE_SUBFOLDERS ) ) );
    setZipFilenameDynamic( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_ZIP_FILENAME_DYNAMIC ) ) );
    setisDynamicFilename( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_IS_FILENAME_DYNAMIC ) ) );
    setDynamicFieldname( XMLHandler.getTagValue( stepnode, TAG_DYNAMIC_FIELDNAME ) );
    setDynamicWildcard( XMLHandler.getTagValue( stepnode, TAG_DYNAMIC_WILDCARD ) );
    setAttachContentFromField( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_ATTACH_CONTENT_FROM_FIELD ) ) );
    setAttachContentField( XMLHandler.getTagValue( stepnode, TAG_ATTACH_CONTENT_FIELD ) );
    setAttachContentFileNameField( XMLHandler.getTagValue( stepnode, TAG_ATTACH_CONTENT_FILE_NAME_FIELD ) );

    setDynamicZipFilenameField( XMLHandler.getTagValue( stepnode, TAG_DYNAMIC_ZIP_FILENAME ) );
    setSourceFileFoldername( XMLHandler.getTagValue( stepnode, TAG_SOURCE_FILE_FOLDER_NAME ) );
    setSourceWildcard( XMLHandler.getTagValue( stepnode, TAG_SOURCE_WILDCARD ) );
    setContactPerson( XMLHandler.getTagValue( stepnode, TAG_CONTACT_PERSON ) );
    setContactPhone( XMLHandler.getTagValue( stepnode, TAG_CONTACT_PHONE ) );
    setComment( XMLHandler.getTagValue( stepnode, TAG_COMMENT ) );
    setIncludingFiles( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_INCLUDE_FILES ) ) );
    setUsingAuthentication( XMLHandler.getTagValue( stepnode, TAG_USE_AUTH ) );
    setUsingSecureAuthentication( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_SECURE_AUTH ) ) );
    setAuthenticationUser( XMLHandler.getTagValue( stepnode, TAG_AUTH_USER ) );
    setAuthenticationPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(
      stepnode, TAG_AUTH_PASSWORD ) ) );
    setClientId( XMLHandler.getTagValue( stepnode, TAG_AUTH_CLIENT_ID ) );
    setSecretKey( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, TAG_AUTH_SECRET_KEY ) ) );
    setScope( XMLHandler.getTagValue( stepnode, TAG_AUTH_SCOPE ) );
    setTokenUrl( XMLHandler.getTagValue( stepnode, TAG_AUTH_TOKEN_URL ) );
    setAuthorization_code( XMLHandler.getTagValue( stepnode, TAG_AUTH_AUTHORIZATION_CODE ) );
    setRedirectUri( XMLHandler.getTagValue( stepnode, TAG_REDIRECT_URI ) );
    setRefresh_token( XMLHandler.getTagValue( stepnode, TAG_REFRESH_TOKEN ) );
    setGrant_type( XMLHandler.getTagValue( stepnode, TAG_USE_GRANT_TYPE ) );
    setOnlySendComment( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_ONLY_COMMENT ) ) );
    setUseHTML( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_HTML ) ) );
    setUsePriority( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_PRIORITY ) ) );
    setEncoding( XMLHandler.getTagValue( stepnode, TAG_ENCODING ) );
    setPriority( XMLHandler.getTagValue( stepnode, TAG_PRIORITY ) );
    setImportance( XMLHandler.getTagValue( stepnode, TAG_IMPORTANCE ) );
    setSensitivity( XMLHandler.getTagValue( stepnode, TAG_SENSITIVITY ) );
    setSecureConnectionType( XMLHandler.getTagValue( stepnode, TAG_SECURE_CONNECTION_TYPE ) );
    setZipFiles( STR_Y.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_ZIP_FILES ) ) );
    setZipFilename( XMLHandler.getTagValue( stepnode, TAG_ZIP_NAME ) );
    setZipLimitSize( XMLHandler.getTagValue( stepnode, TAG_ZIP_LIMIT_SIZE ) );

    Node images = XMLHandler.getSubNode( stepnode, TAG_EMBEDDED_IMAGES );
    // How many field embedded images ?
    int nrImages = XMLHandler.countNodes( images, TAG_EMBEDDED_IMAGE );

    allocate( nrImages );

    // Read them all...
    for ( int i = 0; i < nrImages; i++ ) {
      Node fnode = XMLHandler.getSubNodeByNr( images, TAG_EMBEDDED_IMAGE, i );

      embeddedImages[i] = XMLHandler.getTagValue( fnode, TAG_IMAGE_NAME );
      contentIds[i] = XMLHandler.getTagValue( fnode, TAG_CONTENT_ID );
    }
  }

  public void setEmbeddedImage( int i, String value ) {
    embeddedImages[i] = value;
  }

  public void setEmbeddedImages( String[] value ) {
    this.embeddedImages = value;
  }

  public void setContentIds( int i, String value ) {
    contentIds[i] = value;
  }

  public void setContentIds( String[] value ) {
    this.contentIds = value;
  }

  @Override
  public void setDefault() {
  }

  @Override
  public String getXML() throws KettleException {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SERVER, this.server ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PORT, this.port ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DESTINATION, this.destination ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DESTINATION_CC, this.destinationCc ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DESTINATION_BCC, this.destinationBCc ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REPLYTO_ADDRESSES, this.replyToAddresses ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REPLYTO, this.replyAddress ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REPLYTO_NAME, this.replyName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SUBJECT, this.subject ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_DATE, this.includeDate ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_SUBFOLDERS, this.includeSubFolders ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ZIP_FILENAME_DYNAMIC, this.zipFilenameDynamic ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_IS_FILENAME_DYNAMIC, this.isFilenameDynamic ) );
    retval.append( STR_6_SPACES ).append(
      XMLHandler.addTagValue( TAG_ATTACH_CONTENT_FROM_FIELD, this.attachContentFromField ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ATTACH_CONTENT_FIELD, this.attachContentField ) );
    retval.append( STR_6_SPACES ).append(
      XMLHandler.addTagValue( TAG_ATTACH_CONTENT_FILE_NAME_FIELD, this.attachContentFileNameField ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DYNAMIC_FIELDNAME, this.dynamicFieldName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DYNAMIC_WILDCARD, this.dynamicWildcard ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DYNAMIC_ZIP_FILENAME, this.dynamicZipFilename ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SOURCE_FILE_FOLDER_NAME, this.sourceFileFolderName ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_SOURCE_WILDCARD, this.sourceWildcard ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_CONTACT_PERSON, this.contactPerson ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_CONTACT_PHONE, this.contactPhone ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_COMMENT, this.comment ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_FILES, this.includingFiles ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ZIP_FILES, this.zipFiles ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ZIP_NAME, this.zipFilename ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ZIP_LIMIT_SIZE, this.zipLimitSize ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_AUTH, this.usingAuthentication ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_GRANT_TYPE, grantType ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_SECURE_AUTH, this.usingSecureAuthentication ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_USER, this.authenticationUser ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_PASSWORD,
        Encr.encryptPasswordIfNotUsingVariables( this.authenticationPassword ) ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_CLIENT_ID, clientId ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_SECRET_KEY,
        Encr.encryptPasswordIfNotUsingVariables( secretKey) )  );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_SCOPE, scope ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_TOKEN_URL, tokenUrl ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_AUTH_AUTHORIZATION_CODE, authorizationCode ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REDIRECT_URI, redirectUri ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_REFRESH_TOKEN, refreshToken ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ONLY_COMMENT, this.onlySendComment ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_HTML, this.useHTML ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USE_PRIORITY, this.usePriority ) );
    retval.append( STR_4_SPACES ).append( XMLHandler.addTagValue( TAG_ENCODING, this.encoding ) );
    retval.append( STR_4_SPACES ).append( XMLHandler.addTagValue( TAG_PRIORITY, this.priority ) );
    retval.append( STR_4_SPACES ).append( XMLHandler.addTagValue( TAG_IMPORTANCE, this.importance ) );
    retval.append( STR_4_SPACES ).append( XMLHandler.addTagValue( TAG_SENSITIVITY, this.sensitivity ) );
    retval.append( STR_4_SPACES ).append( XMLHandler.addTagValue( TAG_SECURE_CONNECTION_TYPE, this.secureConnectionType ) );

    retval.append( STR_6_SPACES ).append( '<' ).append( TAG_EMBEDDED_IMAGES ).append( '>' ).append( Const.CR );
    if ( embeddedImages != null ) {
      for ( int i = 0; i < embeddedImages.length; i++ ) {
        retval.append( STR_8_SPACES ).append( '<' ).append( TAG_EMBEDDED_IMAGE ).append( '>' ).append( Const.CR );
        retval.append( STR_10_SPACES ).append( XMLHandler.addTagValue( TAG_IMAGE_NAME, embeddedImages[ i ] ) );
        retval.append( STR_10_SPACES ).append( XMLHandler.addTagValue( TAG_CONTENT_ID, contentIds[ i ] ) );
        retval.append( STR_8_SPACES ).append( "</" ).append( TAG_EMBEDDED_IMAGE ).append( '>' ).append( Const.CR );
      }
    }
    retval.append( STR_6_SPACES ).append( "</" ).append( TAG_EMBEDDED_IMAGES ).append( '>' ).append( Const.CR );

    return retval.toString();
  }

  public void setServer( String s ) {
    this.server = s;
  }

  public String getServer() {
    return this.server;
  }

  public void setDestination( String dest ) {
    this.destination = dest;
  }

  public void setDestinationCc( String destCc ) {
    this.destinationCc = destCc;
  }

  public void setDestinationBCc( String destBCc ) {
    this.destinationBCc = destBCc;
  }

  public String getDestination() {
    return this.destination;
  }

  public String getDestinationCc() {
    return this.destinationCc;
  }

  public String getDestinationBCc() {

    return this.destinationBCc;
  }

  public void setReplyAddress( String reply ) {
    this.replyAddress = reply;
  }

  public String getReplyAddress() {
    return this.replyAddress;
  }

  public void setReplyName( String replyName ) {
    this.replyName = replyName;
  }

  public String getReplyName() {
    return this.replyName;
  }

  public void setSubject( String subj ) {
    this.subject = subj;
  }

  public String getSubject() {
    return this.subject;
  }

  public void setIncludeDate( boolean incl ) {
    this.includeDate = incl;
  }

  public void setIncludeSubFolders( boolean incl ) {
    this.includeSubFolders = incl;
  }

  public boolean isIncludeSubFolders() {
    return this.includeSubFolders;
  }

  public String[] getEmbeddedImages() {
    return embeddedImages;
  }

  public String[] getContentIds() {
    return contentIds;
  }

  public boolean isZipFilenameDynamic() {
    return this.zipFilenameDynamic;
  }

  public void setZipFilenameDynamic( boolean isdynamic ) {
    this.zipFilenameDynamic = isdynamic;
  }

  public void setisDynamicFilename( boolean isdynamic ) {
    this.isFilenameDynamic = isdynamic;
  }

  public void setAttachContentFromField( boolean attachContentFromField ) {
    this.attachContentFromField = attachContentFromField;
  }

  public void setAttachContentField( String attachContentField ) {
    this.attachContentField = attachContentField;
  }

  public void setAttachContentFileNameField( String attachContentFileNameField ) {
    this.attachContentFileNameField = attachContentFileNameField;
  }

  public void setDynamicWildcard( String dynamicwildcard ) {
    this.dynamicWildcard = dynamicwildcard;
  }

  public void setDynamicZipFilenameField( String dynamiczipfilename ) {
    this.dynamicZipFilename = dynamiczipfilename;
  }

  public String getDynamicZipFilenameField() {
    return this.dynamicZipFilename;
  }

  public String getDynamicWildcard() {
    return this.dynamicWildcard;
  }

  public void setSourceFileFoldername( String sourcefile ) {
    this.sourceFileFolderName = sourcefile;
  }

  public String getSourceFileFoldername() {
    return this.sourceFileFolderName;
  }

  public void setSourceWildcard( String wildcard ) {
    this.sourceWildcard = wildcard;
  }

  public String getSourceWildcard() {
    return this.sourceWildcard;
  }

  public void setDynamicFieldname( String dynamicfield ) {
    this.dynamicFieldName = dynamicfield;
  }

  public String getDynamicFieldname() {
    return this.dynamicFieldName;
  }

  public boolean getIncludeDate() {
    return this.includeDate;
  }

  public boolean isDynamicFilename() {
    return this.isFilenameDynamic;
  }

  public boolean isAttachContentFromField() {
    return this.attachContentFromField;
  }

  public String getAttachContentField() {
    return this.attachContentField;
  }

  public String getAttachContentFileNameField() {
    return this.attachContentFileNameField;
  }

  public void setContactPerson( String person ) {
    this.contactPerson = person;
  }

  public String getContactPerson() {
    return this.contactPerson;
  }

  public void setContactPhone( String phone ) {
    this.contactPhone = phone;
  }

  public String getContactPhone() {
    return this.contactPhone;
  }

  public void setComment( String comm ) {
    this.comment = comm;
  }

  public String getComment() {
    return this.comment;
  }

  public boolean isIncludingFiles() {
    return this.includingFiles;
  }

  public void setIncludingFiles( boolean includeFiles ) {
    this.includingFiles = includeFiles;
  }

  /**
   * @return Returns the zipFilename.
   */
  public String getZipFilename() {
    return this.zipFilename;
  }

  /**
   * @return Returns the ziplimitsize.
   */
  public String getZipLimitSize() {
    return this.zipLimitSize;
  }

  /**
   * @param ziplimitsize
   *          The ziplimitsize to set.
   */
  public void setZipLimitSize( String ziplimitsize ) {
    this.zipLimitSize = ziplimitsize;
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
    return this.authenticationPassword;
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
    return this.authenticationUser;
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
    return this.usingAuthentication;
  }

  /**
   * @param usingAuthentication
   *          The usingAuthentication to set.
   */
  public void setUsingAuthentication( String usingAuthentication ) {
    if ( AUTENTICATION_OAUTH.equalsIgnoreCase( usingAuthentication ) ) {
      this.usingAuthentication = AUTENTICATION_OAUTH;
    } else if ( AUTENTICATION_BASIC.equalsIgnoreCase( usingAuthentication )
                  || STR_Y.equalsIgnoreCase( usingAuthentication ) ) {
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
    return this.onlySendComment;
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
    return this.useHTML;
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
    return this.encoding;
  }

  /**
   * @return the secure connection type
   */
  public String getSecureConnectionType() {
    return this.secureConnectionType;
  }

  /**
   * @param secureConnectionType
   *          the secure connection type to set
   */
  public void setSecureConnectionType( String secureConnectionType ) {
    this.secureConnectionType = secureConnectionType;
  }

  /**
   * @param replyToAddresses
   *          the replyToAddresses to set
   */
  public void setReplyToAddresses( String replyToAddresses ) {
    this.replyToAddresses = replyToAddresses;
  }

  /**
   * @return the secure replyToAddresses
   */
  public String getReplyToAddresses() {
    return this.replyToAddresses;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @return the usingSecureAuthentication
   */
  public boolean isUsingSecureAuthentication() {
    return this.usingSecureAuthentication;
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
    return this.port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( String port ) {
    this.port = port;
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
    return this.usePriority;
  }

  /**
   * @return the priority
   */
  public String getPriority() {
    return this.priority;
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
    return this.importance;
  }

  public void setGrant_type( String grant_type ) {
    this.grantType = grant_type;
  }

  public String getGrant_type() {
    return grantType;
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
    this.grantType =grant_type;
  }
  public String getGrantType()
  {
    return grantType;
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
    this.authorizationCode =authorization_code;
  }

  public String getAuthorization_code()
  {
    return authorizationCode;
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
    this.refreshToken =refresh_token;
  }

  public String getRefresh_token()
  {
    return refreshToken;
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
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {

      // First load the common parts like name & description, then the attributes...
      //

      this.server = rep.getStepAttributeString( id_step, TAG_SERVER );
      this.port = rep.getStepAttributeString( id_step, TAG_PORT );
      this.destination = rep.getStepAttributeString( id_step, TAG_DESTINATION );
      this.destinationCc = rep.getStepAttributeString( id_step, TAG_DESTINATION_CC );
      this.destinationBCc = rep.getStepAttributeString( id_step, TAG_DESTINATION_BCC );
      this.replyToAddresses = rep.getStepAttributeString( id_step, TAG_REPLYTO_ADDRESSES );
      this.replyAddress = rep.getStepAttributeString( id_step, TAG_REPLYTO );
      this.replyName = rep.getStepAttributeString( id_step, TAG_REPLYTO_NAME );

      this.subject = rep.getStepAttributeString( id_step, TAG_SUBJECT );
      this.includeDate = rep.getStepAttributeBoolean( id_step, TAG_INCLUDE_DATE );
      this.includeSubFolders = rep.getStepAttributeBoolean( id_step, TAG_INCLUDE_SUBFOLDERS );
      this.zipFilenameDynamic = rep.getStepAttributeBoolean( id_step, TAG_ZIP_FILENAME_DYNAMIC );

      this.attachContentFromField = rep.getStepAttributeBoolean( id_step, TAG_ATTACH_CONTENT_FROM_FIELD );
      this.attachContentField = rep.getStepAttributeString( id_step, TAG_ATTACH_CONTENT_FIELD );
      this.attachContentFileNameField = rep.getStepAttributeString( id_step, TAG_ATTACH_CONTENT_FILE_NAME_FIELD );

      this.isFilenameDynamic = rep.getStepAttributeBoolean( id_step, TAG_IS_FILENAME_DYNAMIC );
      this.dynamicFieldName = rep.getStepAttributeString( id_step, TAG_DYNAMIC_FIELDNAME );
      this.dynamicWildcard = rep.getStepAttributeString( id_step, TAG_DYNAMIC_WILDCARD );
      this.dynamicZipFilename = rep.getStepAttributeString( id_step, TAG_DYNAMIC_ZIP_FILENAME );

      this.sourceFileFolderName = rep.getStepAttributeString( id_step, TAG_SOURCE_FILE_FOLDER_NAME );
      this.sourceWildcard = rep.getStepAttributeString( id_step, TAG_SOURCE_WILDCARD );

      this.contactPerson = rep.getStepAttributeString( id_step, TAG_CONTACT_PERSON );
      this.contactPhone = rep.getStepAttributeString( id_step, TAG_CONTACT_PHONE );
      this.comment = rep.getStepAttributeString( id_step, TAG_COMMENT );
      this.encoding = rep.getStepAttributeString( id_step, TAG_ENCODING );
      this.priority = rep.getStepAttributeString( id_step, TAG_PRIORITY );
      this.importance = rep.getStepAttributeString( id_step, TAG_IMPORTANCE );
      this.sensitivity = rep.getStepAttributeString( id_step, TAG_SENSITIVITY );

      this.includingFiles = rep.getStepAttributeBoolean( id_step, TAG_INCLUDE_FILES );

      setUsingAuthentication( rep.getStepAttributeString( id_step, TAG_USE_AUTH ) );
      this.grantType = rep.getStepAttributeString( id_step, TAG_USE_GRANT_TYPE );
      this.usingSecureAuthentication = rep.getStepAttributeBoolean( id_step, TAG_USE_SECURE_AUTH );
      this.authenticationUser = rep.getStepAttributeString( id_step, TAG_AUTH_USER );
      this.authenticationPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, TAG_AUTH_PASSWORD ) );

      this.clientId = rep.getStepAttributeString( id_step, TAG_AUTH_CLIENT_ID );
      this.secretKey =
              Encr.decryptPasswordOptionallyEncrypted(  rep.getStepAttributeString( id_step, TAG_AUTH_SECRET_KEY ) ) ;
      this.scope = rep.getStepAttributeString( id_step, TAG_AUTH_SCOPE );
      this.tokenUrl = rep.getStepAttributeString( id_step, TAG_AUTH_TOKEN_URL );
      this.authorizationCode = rep.getStepAttributeString( id_step, TAG_AUTH_AUTHORIZATION_CODE );
      this.redirectUri= rep.getStepAttributeString( id_step, TAG_REDIRECT_URI );
      this.refreshToken = rep.getStepAttributeString( id_step, TAG_REFRESH_TOKEN );
      this.onlySendComment = rep.getStepAttributeBoolean( id_step, TAG_ONLY_COMMENT );
      this.useHTML = rep.getStepAttributeBoolean( id_step, TAG_USE_HTML );
      this.usePriority = rep.getStepAttributeBoolean( id_step, TAG_USE_PRIORITY );
      this.secureConnectionType = rep.getStepAttributeString( id_step, TAG_SECURE_CONNECTION_TYPE );

      this.zipFiles = rep.getStepAttributeBoolean( id_step, TAG_ZIP_FILES );
      this.zipFilename = rep.getStepAttributeString( id_step, TAG_ZIP_NAME );
      this.zipLimitSize = rep.getStepAttributeString( id_step, TAG_ZIP_LIMIT_SIZE );

      // How many arguments?
      int imagesNr = rep.countNrStepAttributes( id_step, TAG_EMBEDDED_IMAGE );

      allocate( imagesNr );

      // Read them all...
      for ( int a = 0; a < imagesNr; a++ ) {
        embeddedImages[a] = rep.getStepAttributeString( id_step, a, TAG_EMBEDDED_IMAGE );
        contentIds[a] = rep.getStepAttributeString( id_step, a, "contentid" );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load step type 'mail' from the repository with id_step=" + id_step, dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, TAG_SERVER, this.server );
      rep.saveStepAttribute( id_transformation, id_step, TAG_PORT, this.port );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DESTINATION, this.destination );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DESTINATION_CC, this.destinationCc );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DESTINATION_BCC, this.destinationBCc );
      rep.saveStepAttribute( id_transformation, id_step, TAG_REPLYTO_ADDRESSES, this.replyToAddresses );
      rep.saveStepAttribute( id_transformation, id_step, TAG_REPLYTO, this.replyAddress );
      rep.saveStepAttribute( id_transformation, id_step, TAG_REPLYTO_NAME, this.replyName );

      rep.saveStepAttribute( id_transformation, id_step, TAG_SUBJECT, this.subject );
      rep.saveStepAttribute( id_transformation, id_step, TAG_INCLUDE_DATE, this.includeDate );
      rep.saveStepAttribute( id_transformation, id_step, TAG_INCLUDE_SUBFOLDERS, this.includeSubFolders );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ZIP_FILENAME_DYNAMIC, this.zipFilenameDynamic );

      rep.saveStepAttribute( id_transformation, id_step, TAG_ATTACH_CONTENT_FROM_FIELD, attachContentFromField );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ATTACH_CONTENT_FIELD, this.attachContentField );
      rep.saveStepAttribute(
        id_transformation, id_step, TAG_ATTACH_CONTENT_FILE_NAME_FIELD, this.attachContentFileNameField );

      rep.saveStepAttribute( id_transformation, id_step, TAG_IS_FILENAME_DYNAMIC, isFilenameDynamic );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DYNAMIC_FIELDNAME, dynamicFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DYNAMIC_WILDCARD, dynamicWildcard );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DYNAMIC_ZIP_FILENAME, dynamicZipFilename );

      rep.saveStepAttribute( id_transformation, id_step, TAG_SOURCE_FILE_FOLDER_NAME, sourceFileFolderName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SOURCE_WILDCARD, sourceWildcard );

      rep.saveStepAttribute( id_transformation, id_step, TAG_CONTACT_PERSON, contactPerson );
      rep.saveStepAttribute( id_transformation, id_step, TAG_CONTACT_PHONE, contactPhone );
      rep.saveStepAttribute( id_transformation, id_step, TAG_COMMENT, comment );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ENCODING, encoding );
      rep.saveStepAttribute( id_transformation, id_step, TAG_PRIORITY, priority );
      rep.saveStepAttribute( id_transformation, id_step, TAG_IMPORTANCE, importance );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SENSITIVITY, sensitivity );

      rep.saveStepAttribute( id_transformation, id_step, TAG_INCLUDE_FILES, includingFiles );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_AUTH, usingAuthentication );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_SECURE_AUTH, usingSecureAuthentication );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_USER, authenticationUser );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_PASSWORD, Encr
        .encryptPasswordIfNotUsingVariables( authenticationPassword ) );

      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_CLIENT_ID, clientId );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_SECRET_KEY, Encr
              .encryptPasswordIfNotUsingVariables( secretKey ) );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_SCOPE, scope );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_TOKEN_URL, tokenUrl );
      rep.saveStepAttribute( id_transformation, id_step, TAG_AUTH_AUTHORIZATION_CODE, authorizationCode );
      rep.saveStepAttribute( id_transformation, id_step, TAG_REDIRECT_URI, redirectUri );
      rep.saveStepAttribute( id_transformation, id_step, TAG_REFRESH_TOKEN, refreshToken );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_GRANT_TYPE, grantType );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ONLY_COMMENT, onlySendComment );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_HTML, useHTML );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_PRIORITY, usePriority );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SECURE_CONNECTION_TYPE, secureConnectionType );

      rep.saveStepAttribute( id_transformation, id_step, TAG_ZIP_FILES, zipFiles );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ZIP_NAME, zipFilename );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ZIP_LIMIT_SIZE, zipLimitSize );

      // save the arguments...
      if ( embeddedImages != null ) {
        for ( int i = 0; i < embeddedImages.length; i++ ) {
          rep.saveStepAttribute( id_transformation, id_step, i, TAG_EMBEDDED_IMAGE, embeddedImages[i] );
          rep.saveStepAttribute( id_transformation, id_step, i, "contentid", contentIds[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save step type 'mail' to the repository for id_step=" + id_step, dbe );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.isEmpty() ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.NotReceivingFields" ), stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
    }
    remarks.add( cr );

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.StepRecevingData2" ), stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
    }
    remarks.add( cr );

    // Servername
    if ( Utils.isEmpty( server ) ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.ServerEmpty" ), stepMeta );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.ServerOk" ), stepMeta );
      remarks.add( cr );
      // is the field exists?
      if ( (prev != null) && prev.indexOfValue( transMeta.environmentSubstitute( server ) ) < 0 ) {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG, "MailMeta.CheckResult.ServerFieldNotFound", server ), stepMeta );
      }
      remarks.add( cr );
    }

    // port number
    if ( Utils.isEmpty( port ) ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.PortEmpty" ), stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailMeta.CheckResult.PortOk" ), stepMeta );
    }
    remarks.add( cr );

    // reply address
    if ( Utils.isEmpty( replyAddress ) ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.ReplayAddressEmpty" ), stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.ReplayAddressOk" ), stepMeta );
    }
    remarks.add( cr );

    // Destination
    if ( Utils.isEmpty( destination ) ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.DestinationEmpty" ), stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.DestinationOk" ), stepMeta );
    }
    remarks.add( cr );

    // Subject
    if ( Utils.isEmpty( subject ) ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.SubjectEmpty" ), stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.SubjectOk" ), stepMeta );
    }
    remarks.add( cr );

    // Comment
    if ( Utils.isEmpty( comment ) ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.CommentEmpty" ), stepMeta );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailMeta.CheckResult.CommentEmpty" ), stepMeta );
    }
    remarks.add( cr );

    if ( isFilenameDynamic ) {
      // Dynamic Filename field
      if ( Utils.isEmpty( dynamicFieldName ) ) {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MailMeta.CheckResult.DynamicFilenameFieldEmpty" ), stepMeta );
      } else {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailMeta.CheckResult.DynamicFilenameFieldOk" ), stepMeta );
      }
      remarks.add( cr );
    } else {
      // static filename
      if ( Utils.isEmpty( sourceFileFolderName ) ) {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MailMeta.CheckResult.SourceFilenameEmpty" ), stepMeta );
      } else {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailMeta.CheckResult.SourceFilenameOk" ), stepMeta );
      }
      remarks.add( cr );
    }

    if ( isZipFiles() ) {
      if ( isFilenameDynamic ) {
        // dynamic zipfilename
        if ( Utils.isEmpty( getDynamicZipFilenameField() ) ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "MailMeta.CheckResult.DynamicZipfilenameEmpty" ), stepMeta );
        } else {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "MailMeta.CheckResult.DynamicZipfilenameOK" ), stepMeta );
        }
        remarks.add( cr );
      } else {
        // static zipfilename
        if ( Utils.isEmpty( zipFilename ) ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "MailMeta.CheckResult.ZipfilenameEmpty" ), stepMeta );
        } else {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "MailMeta.CheckResult.ZipfilenameOk" ), stepMeta );
        }
        remarks.add( cr );
      }
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new Mail( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new MailData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }
}
