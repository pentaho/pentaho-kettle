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


package org.pentaho.amazon.s3;

import com.amazonaws.regions.Regions;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.connections.annotations.Encrypted;
import org.pentaho.di.connections.vfs.BaseVFSConnectionDetails;
import org.pentaho.di.connections.vfs.VFSDetailsComposite;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.pentaho.s3.vfs.S3FileProvider;
import org.pentaho.s3common.S3CommonFileSystemConfigBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@MetaStoreElementType( name = "Amazon S3 Connection", description = "Defines the connection details for an Amazon S3 connection" )
public class S3Details extends BaseVFSConnectionDetails {
  VFSDetailsComposite vfsDetailsComposite;
  VariableSpace space;

  public static final String CONNECTION_TYPE_AWS = "0";
  public static final String CONNECTION_TYPE_MINIO = "1";

  @MetaStoreAttribute private String name;

  @MetaStoreAttribute private String description;

  @MetaStoreAttribute @Encrypted private String accessKey;

  @MetaStoreAttribute @Encrypted private String secretKey;

  @MetaStoreAttribute @Encrypted private String sessionToken;

  @MetaStoreAttribute private String credentialsFilePath;

  @MetaStoreAttribute @Encrypted private String credentialsFile;

  @MetaStoreAttribute private String authType;

  @MetaStoreAttribute private String region;

  @MetaStoreAttribute private String profileName;

  @MetaStoreAttribute private String endpoint;

  @MetaStoreAttribute private String pathStyleAccess;
  @MetaStoreAttribute private String pathStyleAccessVariable;

  @MetaStoreAttribute private String signatureVersion;

  @MetaStoreAttribute private String defaultS3Config;
  @MetaStoreAttribute private String defaultS3ConfigVariable;

  @MetaStoreAttribute private String connectionType;

  @Override public String getName() {
    return name;
  }

  @Override public void setName( String name ) {
    this.name = name;
  }

  @Override public String getType() {
    return S3FileProvider.SCHEME;
  }

  @Override public String getDescription() {
    return description;
  }

  @Override
  public void setDescription( String description ) {
    this.description = description;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey( String accessKey ) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey( String secretKey ) {
    this.secretKey = secretKey;
  }

  public String getSessionToken() {
    return sessionToken;
  }

  public void setSessionToken( String sessionToken ) {
    this.sessionToken = sessionToken;
  }

  public String getCredentialsFilePath() {
    return credentialsFilePath;
  }

  public void setCredentialsFilePath( String credentialsFilePath ) {
    this.credentialsFilePath = credentialsFilePath;
  }

  public String getCredentialsFile() {
    return credentialsFile;
  }

  public void setCredentialsFile( String credentialsFile ) {
    this.credentialsFile = credentialsFile;
  }

  public String getAuthType() {
    return authType;
  }

  public void setAuthType( String authType ) {
    this.authType = authType;
  }

  public List<String> getRegions() {
    List<String> names = new ArrayList<>();
    for ( Regions reg : Regions.values() ) {
      names.add( reg.getName() );
    }
    return names;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion( String region ) {
    this.region = region;
  }

  public String getProfileName() {
    return profileName;
  }

  public void setProfileName( String profileName ) {
    this.profileName = profileName;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint( String endpoint ) {
    this.endpoint = endpoint;
  }

  public String getSignatureVersion() {
    return signatureVersion;
  }

  public void setSignatureVersion( String signatureVersion ) {
    this.signatureVersion = signatureVersion;
  }

  public String getPathStyleAccess() {
    return pathStyleAccess;
  }

  public void setPathStyleAccess( String pathStyleAccess ) {
    this.pathStyleAccess = pathStyleAccess;
  }

  public String getDefaultS3Config() {
    return defaultS3Config;
  }

  public void setDefaultS3Config( String defaultS3Config ) {
    this.defaultS3Config = defaultS3Config;
  }

  public String getConnectionType() {
    return connectionType;
  }

  public void setConnectionType( String connectionType ) {
    this.connectionType = connectionType;
  }

  public String getPathStyleAccessVariable() {
    return pathStyleAccessVariable;
  }

  public void setPathStyleAccessVariable( String pathStyleAccessVariable ) {
    this.pathStyleAccessVariable = pathStyleAccessVariable;
  }

  public String getDefaultS3ConfigVariable() {
    return defaultS3ConfigVariable;
  }

  public void setDefaultS3ConfigVariable( String defaultS3ConfigVariable ) {
    this.defaultS3ConfigVariable = defaultS3ConfigVariable;
  }

  @Override protected void fillProperties( Map<String, String> props ) {
    props.put( "name", getName() );
    props.put( "description", getDescription() );
    props.put( "accessKey", getAccessKey() );
    props.put( "secretKey", getSecretKey() );
    props.put( "sessionToken", getSessionToken() );
    props.put( "credentialsFilePath", getCredentialsFilePath() );
    props.put( "credentialsFile", getCredentialsFile() );
    props.put( "authType", getAuthType() );
    props.put( "region", getRegion() );
    props.put( "profileName", getProfileName() );
    props.put( "endpoint", getEndpoint() );
    props.put( "signatureVersion", getSignatureVersion() );
    props.put( S3CommonFileSystemConfigBuilder.PATHSTYLE_ACCESS, getPathStyleAccess() );
    props.put( "defaultS3Config", getDefaultS3Config() );
    props.put( "connectionType", getConnectionType() );
    super.fillProperties( props );
  }

  @Override
  public Object openDialog( Bowl bowl, Object wCompositeWrapper, Object props ) {
    if ( wCompositeWrapper instanceof Composite && props instanceof PropsUI ) {
      vfsDetailsComposite = new S3DetailComposite( bowl, (Composite) wCompositeWrapper, this, (PropsUI) props );
      vfsDetailsComposite.open();
      return vfsDetailsComposite;
    }
    return null;
  }

  @Override
  public void closeDialog() {
    if ( vfsDetailsComposite != null ) {
      vfsDetailsComposite.close();
      vfsDetailsComposite = null;
    }
  }

  @Override public VariableSpace getSpace() {
    return space;
  }

  @Override public void setSpace( VariableSpace space ) {
    this.space = space;
  }
}
