/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.repository.filerep;

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

public class KettleFileRepositoryMeta extends BaseRepositoryMeta implements RepositoryMeta {

  public static final String SHOW_HIDDEN_FOLDERS = "showHiddenFolders";
  public static final String LOCATION = "location";
  public static final String DO_NOT_MODIFY = "doNotModify";
  public static String REPOSITORY_TYPE_ID = "KettleFileRepository";

  private String baseDirectory;
  private boolean readOnly;
  private boolean hidingHiddenFiles;

  public KettleFileRepositoryMeta() {
    super( REPOSITORY_TYPE_ID );
  }

  public KettleFileRepositoryMeta( String id, String name, String description, String baseDirectory ) {
    super( id, name, description );
    this.baseDirectory = baseDirectory;
  }

  public RepositoryCapabilities getRepositoryCapabilities() {
    return new RepositoryCapabilities() {
      public boolean supportsUsers() {
        return false;
      }

      public boolean managesUsers() {
        return false;
      }

      public boolean isReadOnly() {
        return readOnly;
      }

      public boolean supportsRevisions() {
        return false;
      }

      public boolean supportsMetadata() {
        return false;
      }

      public boolean supportsLocking() {
        return false;
      }

      public boolean hasVersionRegistry() {
        return false;
      }

      public boolean supportsAcls() {
        return false;
      }

      public boolean supportsReferences() {
        return false;
      }
    };
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( "  " ).append( XMLHandler.openTag( XML_TAG ) );
    retval.append( super.getXML() );
    retval.append( "    " ).append( XMLHandler.addTagValue( "base_directory", baseDirectory ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "read_only", readOnly ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "hides_hidden_files", hidingHiddenFiles ) );
    retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG ) );

    return retval.toString();
  }

  public void loadXML( Node repnode, List<DatabaseMeta> databases ) throws KettleException {
    super.loadXML( repnode, databases );
    try {
      baseDirectory = XMLHandler.getTagValue( repnode, "base_directory" );
      readOnly = "Y".equalsIgnoreCase( XMLHandler.getTagValue( repnode, "read_only" ) );
      hidingHiddenFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( repnode, "hides_hidden_files" ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load Kettle file repository meta object", e );
    }
  }

  /**
   * @return the baseDirectory
   */
  public String getBaseDirectory() {
    return baseDirectory;
  }

  /**
   * @param baseDirectory
   *          the baseDirectory to set
   */
  public void setBaseDirectory( String baseDirectory ) {
    this.baseDirectory = baseDirectory;
  }

  /**
   * @return the readOnly
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * @param readOnly
   *          the readOnly to set
   */
  public void setReadOnly( boolean readOnly ) {
    this.readOnly = readOnly;
  }

  public RepositoryMeta clone() {
    return new KettleFileRepositoryMeta( REPOSITORY_TYPE_ID, getName(), getDescription(), getBaseDirectory() );
  }

  @Override public void populate( Map<String, Object> properties, RepositoriesMeta repositoriesMeta ) {
    super.populate( properties, repositoriesMeta );
    Boolean showHiddenFolders = (Boolean) properties.get( SHOW_HIDDEN_FOLDERS );
    String location = (String) properties.get( LOCATION );
    Boolean doNotModify = (Boolean) properties.get( DO_NOT_MODIFY );

    setHidingHiddenFiles( showHiddenFolders );
    setBaseDirectory( location );
    setReadOnly( doNotModify );
  }

  @SuppressWarnings( "unchecked" )
  @Override public JSONObject toJSONObject() {
    JSONObject object = super.toJSONObject();
    object.put( SHOW_HIDDEN_FOLDERS, isHidingHiddenFiles() );
    object.put( LOCATION, getBaseDirectory() );
    object.put( DO_NOT_MODIFY, isReadOnly() );
    return object;
  }

  /**
   * @return the hidingHiddenFiles
   */
  public boolean isHidingHiddenFiles() {
    return hidingHiddenFiles;
  }

  /**
   * @param hidingHiddenFiles
   *          the hidingHiddenFiles to set
   */
  public void setHidingHiddenFiles( boolean hidingHiddenFiles ) {
    this.hidingHiddenFiles = hidingHiddenFiles;
  }
}
