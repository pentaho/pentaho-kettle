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


package org.pentaho.s3common;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

import org.pentaho.s3n.vfs.S3NFileSystem;


/**
 * Configuration Builder for S3 File System
 */
public class S3CommonFileSystemConfigBuilder extends FileSystemConfigBuilder {

  public static final String NAME = "name";
  public static final String ACCESS_KEY = "accessKey";
  public static final String SECRET_KEY = "secretKey";
  public static final String SESSION_TOKEN = "sessionToken";
  public static final String REGION = "region";
  public static final String CREDENTIALS_FILE = "credentialsFile";
  public static final String PROFILE_NAME = "profileName";
  public static final String ENDPOINT = "endpoint";
  public static final String SIGNATURE_VERSION = "signature_version";
  public static final String PATHSTYLE_ACCESS = "pathStyleAccess";
  public static final String DEFAULT_S3_CONFIG = "defaultS3Config";
  public static final String CONNECTION_TYPE = "connectionType";
  public static final String TRUST_STORE_FILE_PATH = "trustStoreFilePath";
  public static final String TRUST_STORE_PASSWORD = "trustStorePassword";
  public static final String TRUST_ALL = "trustAll";
  public static final String KEY_STORE_FILE_PATH = "keyStoreFilePath";
  public static final String KEY_STORE_PASSWORD = "keyStorePassword";
  private static final String USE_DEFAULTS = "useDefaults";

  private FileSystemOptions fileSystemOptions;

  public S3CommonFileSystemConfigBuilder( FileSystemOptions fileSystemOptions ) {
    this.fileSystemOptions = fileSystemOptions;
  }

  public FileSystemOptions getFileSystemOptions() {
    return fileSystemOptions;
  }

  public void setFileSystemOptions( FileSystemOptions fileSystemOptions ) {
    this.fileSystemOptions = fileSystemOptions;
  }

  public void setName( String name ) {
    this.setParam( getFileSystemOptions(), NAME, name );
  }

  public String getName() {
    return (String) this.getParam( getFileSystemOptions(), NAME );
  }

  public void setAccessKey( String accessKey ) {
    this.setParam( getFileSystemOptions(), ACCESS_KEY, accessKey );
  }

  public String getAccessKey() {
    return (String) this.getParam( getFileSystemOptions(), ACCESS_KEY );
  }

  public void setSecretKey( String secretKey ) {
    this.setParam( getFileSystemOptions(), SECRET_KEY, secretKey );
  }

  public String getSecretKey() {
    return (String) this.getParam( getFileSystemOptions(), SECRET_KEY );
  }

  public void setSessionToken( String sessionToken ) {
    this.setParam( getFileSystemOptions(), SESSION_TOKEN, sessionToken );
  }

  public String getSessionToken() {
    return (String) this.getParam( getFileSystemOptions(), SESSION_TOKEN );
  }

  public void setRegion( String region ) {
    this.setParam( getFileSystemOptions(), REGION, region );
  }

  public String getRegion() {
    return (String) this.getParam( getFileSystemOptions(), REGION );
  }

  public void setCredentialsFile( String credentialsFile ) {
    this.setParam( getFileSystemOptions(), CREDENTIALS_FILE, credentialsFile );
  }

  public String getCredentialsFile() {
    return (String) this.getParam( getFileSystemOptions(), CREDENTIALS_FILE );
  }

  public String getProfileName() {
    return (String) this.getParam( getFileSystemOptions(), PROFILE_NAME );
  }

  public void setProfileName( String profileName ) {
    this.setParam( getFileSystemOptions(), PROFILE_NAME, profileName );
  }

  public void setEndpoint( String endpoint ) {
    this.setParam( getFileSystemOptions(), ENDPOINT, endpoint );
  }

  public String getEndpoint() {
    return (String) this.getParam( getFileSystemOptions(), ENDPOINT );
  }


  public void setSignatureVersion( String signatureVersion ) {
    this.setParam( getFileSystemOptions(), SIGNATURE_VERSION, signatureVersion );
  }

  public String getSignatureVersion() {
    return (String) this.getParam( getFileSystemOptions(), SIGNATURE_VERSION );
  }

  public void setPathStyleAccess( String pathStyleAccess ) {
    this.setParam( getFileSystemOptions(), PATHSTYLE_ACCESS, pathStyleAccess );
  }

  public String getPathStyleAccess() {
    return (String) this.getParam( getFileSystemOptions(), PATHSTYLE_ACCESS );
  }

  public void setDefaultS3Config( String defaultS3Config ) {
    this.setParam( getFileSystemOptions(), DEFAULT_S3_CONFIG, defaultS3Config );
  }

  public String getDefaultS3Config() {
    return (String) this.getParam( getFileSystemOptions(), DEFAULT_S3_CONFIG );
  }

  public void setConnectionType( String connectionType ) {
    this.setParam( getFileSystemOptions(), CONNECTION_TYPE, connectionType );
  }

  public String getConnectionType() {
    return (String) this.getParam( getFileSystemOptions(), CONNECTION_TYPE );
  }

  public void setTrustStoreFilePath( String trustStoreFilePath ) {
    this.setParam( getFileSystemOptions(), TRUST_STORE_FILE_PATH, trustStoreFilePath );
  }

  public String getTrustStoreFilePath() {
    return (String) this.getParam( getFileSystemOptions(), TRUST_STORE_FILE_PATH );
  }

  public void setTrustStorePassword( String trustStorePassword ) {
    this.setParam( getFileSystemOptions(), TRUST_STORE_PASSWORD, trustStorePassword );
  }

  public String getTrustStorePassword() {
    return (String) this.getParam( getFileSystemOptions(), TRUST_STORE_PASSWORD );
  }

  public void setTrustAll( String trustAll ) {
    this.setParam( getFileSystemOptions(), TRUST_ALL, trustAll );
  }

  public String getTrustAll() {
    return (String) this.getParam( getFileSystemOptions(), TRUST_ALL );
  }

  public void setKeyStoreFilePath( String keyStoreFilePath ) {
    this.setParam( getFileSystemOptions(), KEY_STORE_FILE_PATH, keyStoreFilePath );
  }

  public String getKeyStoreFilePath() {
    return (String) this.getParam( getFileSystemOptions(), KEY_STORE_FILE_PATH );
  }

  public void setKeyStorePassword( String keyStorePassword ) {
    this.setParam( getFileSystemOptions(), KEY_STORE_PASSWORD, keyStorePassword );
  }

  public String getKeyStorePassword() {
    return (String) this.getParam( getFileSystemOptions(), KEY_STORE_PASSWORD );
  }

  public boolean useDefaults() {
    return this.getBoolean( getFileSystemOptions(), USE_DEFAULTS, Boolean.TRUE );
  }

  public void setUseDefaults( boolean useDefaults ) {
    this.setParam( getFileSystemOptions(), USE_DEFAULTS, useDefaults );
  }

  @Override protected Class<? extends FileSystem> getConfigClass() {
    return S3NFileSystem.class;
  }
}
