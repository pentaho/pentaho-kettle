/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRepositoryNotSupportedException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/*
 * Created on 31-mrt-2004
 *
 * This class contains information regarding the defined Kettle repositories
 */

public class RepositoriesMeta {
  private static Class<?> PKG = RepositoriesMeta.class; // for i18n purposes, needed by Translator2!!

  private List<DatabaseMeta> databases; // Repository connections
  private List<RepositoryMeta> repositories; // List of repositories
  private LogChannel log;
  private String errorMessage;

  public RepositoriesMeta() {
    clear();
  }

  public void clear() {
    errorMessage = null;
    databases = new ArrayList<DatabaseMeta>();
    repositories = new ArrayList<RepositoryMeta>();
    LogLevel level = null;
    if ( log != null ) {
      level = log.getLogLevel();
    }
    setLog( newLogChannel() );
    if ( level != null ) {
      log.setLogLevel( level );
    }
  }

  LogChannel newLogChannel() {
    return new LogChannel( "RepositoriesMeta" );
  }

  public void addDatabase( DatabaseMeta ci ) {
    databases.add( ci );
  }

  public void addRepository( RepositoryMeta ri ) {
    repositories.add( ri );
  }

  void setLog( LogChannel log ) {
    this.log = log;
  }

  public void addDatabase( int p, DatabaseMeta ci ) {
    databases.add( p, ci );
  }

  public void addRepository( int p, RepositoryMeta ri ) {
    repositories.add( p, ri );
  }

  public DatabaseMeta getDatabase( int i ) {
    return databases.get( i );
  }

  public RepositoryMeta getRepository( int i ) {
    return repositories.get( i );
  }

  public void removeDatabase( int i ) {
    if ( i < 0 || i >= databases.size() ) {
      return;
    }
    databases.remove( i );
  }

  public void removeRepository( int i ) {
    if ( i < 0 || i >= repositories.size() ) {
      return;
    }
    repositories.remove( i );
  }

  public int nrDatabases() {
    return databases.size();
  }

  public int nrRepositories() {
    return repositories.size();
  }

  public DatabaseMeta searchDatabase( String name ) {
    for ( int i = 0; i < nrDatabases(); i++ ) {
      if ( getDatabase( i ).getName().equalsIgnoreCase( name ) ) {
        return getDatabase( i );
      }
    }
    return null;
  }

  public RepositoryMeta searchRepository( String name ) {
    for ( int i = 0; i < nrRepositories(); i++ ) {
      String repName = getRepository( i ).getName();
      if ( repName != null && repName.equalsIgnoreCase( name ) ) {
        return getRepository( i );
      }
    }
    return null;
  }

  public int indexOfDatabase( DatabaseMeta di ) {
    return databases.indexOf( di );
  }

  public int indexOfRepository( RepositoryMeta ri ) {
    return repositories.indexOf( ri );
  }

  public RepositoryMeta findRepository( String name ) {
    for ( int i = 0; i < nrRepositories(); i++ ) {
      RepositoryMeta ri = getRepository( i );
      if ( ri.getName().equalsIgnoreCase( name ) ) {
        return ri;
      }
    }
    return null;
  }

  public RepositoryMeta findRepositoryById( String id ) {
    for ( int i = 0; i < nrRepositories(); i++ ) {
      RepositoryMeta ri = getRepository( i );
      if ( ri.getId().equalsIgnoreCase( id ) ) {
        return ri;
      }
    }
    return null;
  }

  // We read the repositories from the file:
  //
  public boolean readData() throws KettleException {
    // Clear the information
    //
    clear();

    File file = new File( getKettleLocalRepositoriesFile() );
    if ( !file.exists() || !file.isFile() ) {
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "RepositoryMeta.Log.NoRepositoryFileInLocalDirectory", file.getAbsolutePath() ) );
      }
      file = new File( getKettleUserRepositoriesFile() );
      if ( !file.exists() || !file.isFile() ) {
        return true; // nothing to read!
      }
    }

    if ( log.isBasic() ) {
      log.logBasic( BaseMessages.getString( PKG, "RepositoryMeta.Log.ReadingXMLFile", file.getAbsoluteFile() ) );
    }

    try {
      // Check and open XML document
      DocumentBuilderFactory dbf = XMLParserFactoryProducer.createSecureDocBuilderFactory();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc;
      try {
        doc = db.parse( file );
      } catch ( FileNotFoundException ef ) {
        try ( InputStream is = getClass().getResourceAsStream( "/org/pentaho/di/repository/repositories.xml" ) ) {
          if ( is != null ) {
            doc = db.parse( is );
          } else {
            throw new KettleException( BaseMessages.getString( PKG, "RepositoryMeta.Error.OpeningFile", file.getAbsoluteFile() ), ef );
          }
        }
      }
      parseRepositoriesDoc( doc );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "RepositoryMeta.Error.ReadingInfo" ), e );
    }

    return true;
  }

  String getKettleUserRepositoriesFile() {
    return Const.getKettleUserRepositoriesFile();
  }

  String getKettleLocalRepositoriesFile() {
    return Const.getKettleLocalRepositoriesFile();
  }

  public void readDataFromInputStream( InputStream is ) throws KettleException {
    // Clear the information
    //
    clear();

    if ( log.isBasic() ) {
      log.logBasic( BaseMessages.getString( PKG, "RepositoryMeta.Log.ReadingXMLFile", "FromInputStream" ) );
    }

    try {
      // Check and open XML document
      DocumentBuilderFactory dbf = XMLParserFactoryProducer.createSecureDocBuilderFactory();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse( is );
      parseRepositoriesDoc( doc );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "RepositoryMeta.Error.ReadingInfo" ), e );
    }
  }

  protected void parseRepositoriesDoc( Document doc ) throws Exception {
    // Get the <repositories> node:
    Node repsnode = XMLHandler.getSubNode( doc, "repositories" );

    // Handle connections
    int nrconn = XMLHandler.countNodes( repsnode, "connection" );
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "RepositoryMeta.Log.ConnectionNumber", nrconn ) );
    }

    for ( int i = 0; i < nrconn; i++ ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "RepositoryMeta.Log.LookingConnection", i ) );
      }

      Node dbnode = XMLHandler.getSubNodeByNr( repsnode, "connection", i );

      DatabaseMeta dbcon = null;
      try {
        dbcon = new DatabaseMeta( dbnode );
        addDatabase( dbcon );
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "RepositoryMeta.Log.ReadConnection", dbcon.getName() ) );
        }
      } catch ( Exception kpe ) {

        log.logError( BaseMessages.getString( PKG, "RepositoryMeta.Error.CreatingDatabaseMeta", dbcon.getName() ) );

      }
    }

    // Handle repositories...
    int nrreps = XMLHandler.countNodes( repsnode, RepositoryMeta.XML_TAG );
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "RepositoryMeta.Log.RepositoryNumber", nrreps ) );
    }
    StringBuilder unableToReadIds = new StringBuilder();
    KettleException kettleException = null;
    for ( int i = 0; i < nrreps; i++ ) {
      Node repnode = XMLHandler.getSubNodeByNr( repsnode, RepositoryMeta.XML_TAG, i );
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "RepositoryMeta.Log.LookingRepository", i ) );
      }
      String id = XMLHandler.getTagValue( repnode, "id" );
      if ( Utils.isEmpty( id ) ) {
        // Backward compatibility : if the id is not defined, it's the database repository!
        //
        id = KettleDatabaseRepositoryMeta.REPOSITORY_TYPE_ID;
      }
      try {
        RepositoryMeta repositoryMeta =
          PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, id, RepositoryMeta.class );
        if ( repositoryMeta != null ) {
          repositoryMeta.loadXML( repnode, databases );
          // Backward compatibility. Description is now required as it will be what gets displayed on the
          // repositories dialog
          if ( repositoryMeta.getDescription() == null || repositoryMeta.getDescription().equals( "" ) ) {
            repositoryMeta.setDescription( repositoryMeta.getName() );
          }
          addRepository( repositoryMeta );
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages
              .getString( PKG, "RepositoryMeta.Log.ReadRepository", repositoryMeta.getName() ) );
          }
        } else {
          unableToReadIds.append( id );
          unableToReadIds.append( "," );
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "RepositoryMeta.Error.ReadRepositoryId", id ) );
          }
        }
      } catch ( KettleException ex ) {
        // Get to the root cause
        Throwable cause = ex;
        kettleException = ex;
        while ( cause.getCause() != null ) {
          cause = cause.getCause();
        }

        if ( cause instanceof KettleRepositoryNotSupportedException ) {
          // If the root cause is a KettleRepositoryNotSupportedException, do not fail
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "RepositoryMeta.Error.UnrecognizedRepositoryType", id ) );
          }
        }
      }
    }
    if ( unableToReadIds != null && unableToReadIds.length() > 0 ) {
      errorMessage =
        BaseMessages.getString( PKG, "RepositoryMeta.Error.ReadRepositoryIdNotAvailable", unableToReadIds
          .substring( 0, unableToReadIds.lastIndexOf( "," ) ) );
    }
    if ( kettleException != null ) {
      throw kettleException;
    }
  }

  public String getXML() {
    String retval = "";

    retval += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR;
    retval += "<repositories>" + Const.CR;

    for ( int i = 0; i < nrDatabases(); i++ ) {
      DatabaseMeta conn = getDatabase( i );
      retval += conn.getXML();
    }

    for ( int i = 0; i < nrRepositories(); i++ ) {
      RepositoryMeta ri = getRepository( i );
      retval += ri.getXML();
    }

    retval += "  </repositories>" + Const.CR;
    return retval;
  }

  public void writeData() throws KettleException {
    try {
      FileOutputStream fos = new FileOutputStream( new File( getKettleUserRepositoriesFile() ) );
      fos.write( getXML().getBytes() );
      fos.close();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "RepositoryMeta.Error.WritingMetadata" ), e );
    }
  }

  public String toString() {
    return getClass().getSimpleName();
  }

  public RepositoriesMeta clone() {
    RepositoriesMeta meta = new RepositoriesMeta();
    meta.clear();
    for ( DatabaseMeta dbMeta : databases ) {
      meta.addDatabase( dbMeta );
    }
    for ( RepositoryMeta repMeta : repositories ) {
      meta.addRepository( repMeta.clone() );
    }
    return meta;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public LogChannelInterface getLog() {
    return log;
  }
}
