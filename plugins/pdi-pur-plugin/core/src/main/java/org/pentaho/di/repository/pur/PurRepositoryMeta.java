/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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

  /** The id as specified in the repository plugin meta, used for backward compatibility only */
  public static String REPOSITORY_TYPE_ID = "PentahoEnterpriseRepository";

  private PurRepositoryLocation repositoryLocation;

  private boolean versionCommentMandatory;

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

  public RepositoryMeta clone() {
    return new PurRepositoryMeta( REPOSITORY_TYPE_ID, getName(), getDescription(), getRepositoryLocation(),
        isVersionCommentMandatory() );
  }

  @Override public void populate( Map<String, Object> properties, RepositoriesMeta repositoriesMeta ) {
    super.populate( properties, repositoriesMeta );
    String url = (String) properties.get( URL );
    PurRepositoryLocation purRepositoryLocation = new PurRepositoryLocation( url );
    setRepositoryLocation( purRepositoryLocation );
  }

  @SuppressWarnings( "unchecked" )
  @Override public JSONObject toJSONObject() {
    JSONObject object = super.toJSONObject();
    object.put( URL, getRepositoryLocation().getUrl() );
    return object;
  }
}
