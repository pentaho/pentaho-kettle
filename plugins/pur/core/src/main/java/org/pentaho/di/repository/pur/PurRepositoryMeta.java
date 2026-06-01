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

package org.pentaho.di.repository.pur;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.w3c.dom.Node;

public class PurRepositoryMeta extends BaseRepositoryMeta implements RepositoryMeta, java.io.Serializable {

  private static final long serialVersionUID = -2456840196232185649L; /* EESOURCE: UPDATE SERIALVERUID */

  public static final String URL = "url";
  public static final String AUTH_METHOD = "authMethod";
  public static final String SSO_PROVIDER_NAME = "ssoProviderName";
  public static final String SSO_AUTHORIZATION_URI = "ssoAuthorizationUri";
  public static final String SSO_REGISTRATION_ID = "ssoRegistrationId";
  public static final String AUTH_METHOD_USERNAME_PASSWORD = "USERNAME_PASSWORD";

  /** The id as specified in the repository plugin meta, used for backward compatibility only */
  public static String REPOSITORY_TYPE_ID = "PentahoEnterpriseRepository";

  private PurRepositoryLocation repositoryLocation;

  private boolean versionCommentMandatory;
  
  private String authMethod;
  private String ssoProviderName;
  private String ssoAuthorizationUri;
  private String ssoRegistrationId;

  public PurRepositoryMeta() {
    super( REPOSITORY_TYPE_ID );
  }

  public PurRepositoryMeta( String id, String name, String description, PurRepositoryLocation repositoryLocation,
      boolean versionCommentMandatory ) {
    super( id, name, description );
    this.repositoryLocation = repositoryLocation;
    this.versionCommentMandatory = versionCommentMandatory;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 100 );

    retval.append( "  " ).append( XMLHandler.openTag( XML_TAG ) );
    retval.append( super.getXML() );
    retval.append( "    " ).append( XMLHandler.addTagValue( "repository_location_url", repositoryLocation.getUrl() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "version_comment_mandatory", versionCommentMandatory ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "auth_method", getAuthMethod() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sso_provider_name", getSsoProviderName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sso_authorization_uri", getSsoAuthorizationUri() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sso_registration_id", getSsoRegistrationId() ) );
    retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG ) );

    return retval.toString();
  }

  public void loadXML( Node repnode, List<DatabaseMeta> databases ) throws KettleException {
    super.loadXML( repnode, databases );
    try {
      String url = XMLHandler.getTagValue( repnode, "repository_location_url" );
      // remove trailing slash
      String urlTrim = url.endsWith( "/" ) ? url.substring( 0, url.length() - 1 ) : url;
      this.repositoryLocation = new PurRepositoryLocation( urlTrim );
      this.versionCommentMandatory =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( repnode, "version_comment_mandatory" ) );
      this.authMethod = XMLHandler.getTagValue( repnode, "auth_method" );
      // Normalize null or blank/whitespace auth method to default for backward compatibility
      if ( this.authMethod == null || this.authMethod.trim().isEmpty() ) {
        this.authMethod = AUTH_METHOD_USERNAME_PASSWORD;
      }
      setSsoProviderName( XMLHandler.getTagValue( repnode, "sso_provider_name" ) );
      setSsoAuthorizationUri( XMLHandler.getTagValue( repnode, "sso_authorization_uri" ) );
      setSsoRegistrationId( XMLHandler.getTagValue( repnode, "sso_registration_id" ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load Kettle database repository meta object", e );
    }
  }

  public RepositoryCapabilities getRepositoryCapabilities() {
    return new RepositoryCapabilities() {
      public boolean supportsUsers() {
        return true;
      }

      public boolean managesUsers() {
        return true;
      }

      public boolean isReadOnly() {
        return false;
      }

      public boolean supportsRevisions() {
        return true;
      }

      public boolean supportsMetadata() {
        return true;
      }

      public boolean supportsLocking() {
        return true;
      }

      public boolean hasVersionRegistry() {
        return true;
      }

      public boolean supportsAcls() {
        return true;
      }

      public boolean supportsReferences() {
        return true;
      }
    };
  }

  /**
   * @return the repositoryLocation
   */
  public PurRepositoryLocation getRepositoryLocation() {
    return repositoryLocation;
  }

  /**
   * @param repositoryLocation
   *          the repositoryLocation to set
   */
  public void setRepositoryLocation( PurRepositoryLocation repositoryLocation ) {
    this.repositoryLocation = repositoryLocation;
  }

  public boolean isVersionCommentMandatory() {
    return versionCommentMandatory;
  }

  public void setVersionCommentMandatory( boolean versionCommentMandatory ) {
    this.versionCommentMandatory = versionCommentMandatory;
  }
  
  public String getAuthMethod() {
    return authMethod != null ? authMethod : AUTH_METHOD_USERNAME_PASSWORD;
  }
  
  public void setAuthMethod( String authMethod ) {
    // Normalize null or blank/whitespace auth method to default
    if ( authMethod == null || authMethod.trim().isEmpty() ) {
      this.authMethod = AUTH_METHOD_USERNAME_PASSWORD;
    } else {
      this.authMethod = authMethod;
    }
  }

  public String getSsoProviderName() {
    return ssoProviderName;
  }

  public void setSsoProviderName( String ssoProviderName ) {
    this.ssoProviderName = normalizeOptionalValue( ssoProviderName );
  }

  public String getSsoAuthorizationUri() {
    return ssoAuthorizationUri;
  }

  public void setSsoAuthorizationUri( String ssoAuthorizationUri ) {
    this.ssoAuthorizationUri = normalizeOptionalValue( ssoAuthorizationUri );
  }

  public String getSsoRegistrationId() {
    return ssoRegistrationId;
  }

  public void setSsoRegistrationId( String ssoRegistrationId ) {
    this.ssoRegistrationId = normalizeOptionalValue( ssoRegistrationId );
  }

  public RepositoryMeta clone() {
    PurRepositoryMeta clone = new PurRepositoryMeta( REPOSITORY_TYPE_ID, getName(), getDescription(),
      getRepositoryLocation(), isVersionCommentMandatory() );
    clone.setAuthMethod( getAuthMethod() );
    clone.setSsoProviderName( getSsoProviderName() );
    clone.setSsoAuthorizationUri( getSsoAuthorizationUri() );
    clone.setSsoRegistrationId( getSsoRegistrationId() );
    return clone;
  }

  @Override public void populate( Map<String, Object> properties, RepositoriesMeta repositoriesMeta ) {
    super.populate( properties, repositoriesMeta );
    String url = (String) properties.get( URL );
    PurRepositoryLocation purRepositoryLocation = new PurRepositoryLocation( url );
    setRepositoryLocation( purRepositoryLocation );
    String authMethodValue = (String) properties.get( AUTH_METHOD );
    setAuthMethod( authMethodValue );
    setSsoProviderName( (String) properties.get( SSO_PROVIDER_NAME ) );
    setSsoAuthorizationUri( (String) properties.get( SSO_AUTHORIZATION_URI ) );
    setSsoRegistrationId( (String) properties.get( SSO_REGISTRATION_ID ) );
  }

  @SuppressWarnings( "unchecked" )
  @Override public JSONObject toJSONObject() {
    JSONObject object = super.toJSONObject();
    object.put( URL, getRepositoryLocation().getUrl() );
    object.put( AUTH_METHOD, getAuthMethod() );
    object.put( SSO_PROVIDER_NAME, getSsoProviderName() );
    object.put( SSO_AUTHORIZATION_URI, getSsoAuthorizationUri() );
    object.put( SSO_REGISTRATION_ID, getSsoRegistrationId() );
    return object;
  }

  private String normalizeOptionalValue( String value ) {
    return value == null || value.trim().isEmpty() ? null : value;
  }
}
