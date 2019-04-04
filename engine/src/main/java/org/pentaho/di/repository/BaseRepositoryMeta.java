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

package org.pentaho.di.repository;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class BaseRepositoryMeta {

  public static final String ID = "id";
  public static final String DISPLAY_NAME = "displayName";
  public static final String DESCRIPTION = "description";
  public static final String IS_DEFAULT = "isDefault";

  protected String id;
  protected String name;
  protected String description;
  protected Boolean isDefault = false;

  public BaseRepositoryMeta( String id ) {
    this.id = id;
  }

  /**
   * This returns the expected name for the dialog that edits this repository metadata object The expected name is in
   * the org.pentaho.di.ui tree and has a class name that is the name of the job entry with 'Dialog' added to the end.
   *
   * e.g. if the repository meta class is org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta the dialog would be
   * org.pentaho.di.ui.repository.kdr.KettleDatabaseRepositoryDialog
   *
   * If the dialog class does not match this pattern, the RepositoryMeta class should override this method and return
   * the appropriate class name
   *
   * @return full class name of the dialog
   */
  public String getDialogClassName() {
    String className = getClass().getCanonicalName();
    className = className.replaceFirst( "\\.di\\.", ".di.ui." );
    if ( className.endsWith( "Meta" ) ) {
      className = className.substring( 0, className.length() - 4 );
    }
    className += "Dialog";
    return className;
  }

  /**
   * This returns the expected name for the dialog that edits this repository metadata object The expected name is in
   * the org.pentaho.di.ui tree and has a class name that is the name of the job entry with 'Dialog' added to the end.
   *
   * e.g. if the repository meta class is org.pentaho.di.pur.PurRepositoryMeta the dialog would be
   * org.pentaho.di.ui.repository.pur.PurRepositoryRevisionBrowserDialog
   *
   * If the dialog class does not match this pattern, the RepositoryMeta class should override this method and return
   * the appropriate class name
   *
   * @return full class name of the dialog
   */
  public String getRevisionBrowserDialogClassName() {
    String className = getClass().getCanonicalName();
    className = className.replaceFirst( "\\.di\\.", ".di.ui." );
    if ( className.endsWith( "Meta" ) ) {
      className = className.substring( 0, className.length() - 4 );
    }
    className += "RevisionBrowserDialog";
    return className;
  }

  /**
   * @param id
   * @param name
   * @param description
   */
  public BaseRepositoryMeta( String id, String name, String description ) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /**
   * @param id
   * @param name
   * @param description
   * @param isDefault
   */
  public BaseRepositoryMeta( String id, String name, String description, boolean isDefault ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.isDefault = isDefault;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#loadXML(org.w3c.dom.Node, java.util.List)
   */
  public void loadXML( Node repnode, List<DatabaseMeta> databases ) throws KettleException {
    try {
      // Fix for PDI-2508: migrating from 3.2 to 4.0 causes NPE on startup.
      id = Const.NVL( XMLHandler.getTagValue( repnode, "id" ), id );
      name = XMLHandler.getTagValue( repnode, "name" );
      description = XMLHandler.getTagValue( repnode, "description" );
      isDefault = Boolean.valueOf( XMLHandler.getTagValue( repnode, "is_default" ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load repository meta object", e );
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#getXML()
   */
  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "id", id ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "name", name ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "description", description ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "is_default", isDefault.toString() ) );

    return retval.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#getId()
   */
  public String getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#setId(java.lang.String)
   */
  public void setId( String id ) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#getName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#setName(java.lang.String)
   */
  public void setName( String name ) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#getDescription()
   */
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#setDescription(java.lang.String)
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#isDefault()
   */
  public Boolean isDefault() {
    return isDefault;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryMeta#setDefault(java.lang.Boolean)
   */
  public void setDefault( Boolean isDefault ) {
    this.isDefault = isDefault;
  }

  @SuppressWarnings( "unchecked" )
  public JSONObject toJSONObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( ID, getId() );
    jsonObject.put( DISPLAY_NAME, getName() );
    jsonObject.put( DESCRIPTION, getDescription() );
    jsonObject.put( IS_DEFAULT, isDefault() );
    return jsonObject;
  }

  public void populate( Map<String, Object> properties, RepositoriesMeta repositoriesMeta ) {
    String displayName = (String) properties.get( DISPLAY_NAME );
    String description = (String) properties.get( DESCRIPTION );
    Boolean isDefault = (Boolean) properties.get( IS_DEFAULT );
    setName( displayName );
    setDescription( description );
    setDefault( isDefault );
  }


}
