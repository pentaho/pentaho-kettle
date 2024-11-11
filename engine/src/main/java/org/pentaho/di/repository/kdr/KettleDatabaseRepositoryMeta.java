/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.repository.kdr;

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

/*
 * Created on 31-mar-2004
 */
public class KettleDatabaseRepositoryMeta extends BaseRepositoryMeta implements RepositoryMeta {

  public static final String ID = "id";
  public static final String DATABASE_CONNECTION = "databaseConnection";

  /** The id as specified in the repository plugin meta, used for backward compatibility only */
  public static String REPOSITORY_TYPE_ID = "KettleDatabaseRepository";

  private DatabaseMeta databaseMeta;

  public KettleDatabaseRepositoryMeta() {
    super( REPOSITORY_TYPE_ID );
  }

  public KettleDatabaseRepositoryMeta( String id, String name, String description, DatabaseMeta connection ) {
    super( id, name, description );
    this.databaseMeta = connection;
  }

  public KettleDatabaseRepositoryMeta( String id ) {
    super( id, "", "" );
    this.databaseMeta = null;
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
        return false;
      }

      public boolean supportsMetadata() {
        return true;
      }

      public boolean supportsLocking() {
        return true;
      }

      public boolean hasVersionRegistry() {
        return false;
      }

      public boolean supportsAcls() {
        return false;
      }

      public boolean supportsReferences() {
        return true;
      }
    };
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public Boolean isDefault() {
    return isDefault;
  }

  public void setDefault( Boolean isDefault ) {
    this.isDefault = isDefault;
  }

  public void setConnection( DatabaseMeta connection ) {
    this.databaseMeta = connection;
  }

  public DatabaseMeta getConnection() {
    return databaseMeta;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( "  " ).append( XMLHandler.openTag( XML_TAG ) );
    retval.append( super.getXML() );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "connection", databaseMeta != null ? databaseMeta.getName() : null ) );
    retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG ) );

    return retval.toString();
  }

  public void loadXML( Node repnode, List<DatabaseMeta> databases ) throws KettleException {
    super.loadXML( repnode, databases );
    try {
      String conn = XMLHandler.getTagValue( repnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, conn );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load Kettle database repository meta object", e );
    }
  }

  public RepositoryMeta clone() {
    return new KettleDatabaseRepositoryMeta( REPOSITORY_TYPE_ID, getName(), getDescription(), getConnection() );
  }

  @Override public void populate( Map<String, Object> properties, RepositoriesMeta repositoriesMeta ) {
    super.populate( properties, repositoriesMeta );
    String databaseConnection = (String) properties.get( DATABASE_CONNECTION );

    DatabaseMeta databaseMeta = repositoriesMeta.searchDatabase( databaseConnection );
    if ( databaseMeta != null ) {
      setConnection( databaseMeta );
    }
  }

  @SuppressWarnings( "unchecked" )
  @Override public JSONObject toJSONObject() {
    JSONObject object = super.toJSONObject();
    object.put( DATABASE_CONNECTION, databaseMeta != null ? databaseMeta.getName() : "" );
    return object;
  }

}
