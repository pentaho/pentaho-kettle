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

package org.pentaho.di.job.entries.sql;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines an SQL job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntrySQL extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntrySQL.class; // for i18n purposes, needed by Translator2!!

  private String sql;
  private DatabaseMeta connection;
  private boolean useVariableSubstitution = false;
  private boolean sqlfromfile = false;
  private String sqlfilename;
  private boolean sendOneStatement = false;

  public JobEntrySQL( String n ) {
    super( n, "" );
    sql = null;
    connection = null;
  }

  public JobEntrySQL() {
    this( "" );
  }

  public Object clone() {
    JobEntrySQL je = (JobEntrySQL) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "sql", sql ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "useVariableSubstitution", useVariableSubstitution ? "T" : "F" ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sqlfromfile", sqlfromfile ? "T" : "F" ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sqlfilename", sqlfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sendOneStatement", sendOneStatement ? "T" : "F" ) );

    retval.append( "      " ).append(
      XMLHandler.addTagValue( "connection", connection == null ? null : connection.getName() ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      sql = XMLHandler.getTagValue( entrynode, "sql" );
      String dbname = XMLHandler.getTagValue( entrynode, "connection" );
      String sSubs = XMLHandler.getTagValue( entrynode, "useVariableSubstitution" );

      if ( sSubs != null && sSubs.equalsIgnoreCase( "T" ) ) {
        useVariableSubstitution = true;
      }
      connection = DatabaseMeta.findDatabase( databases, dbname );

      String ssql = XMLHandler.getTagValue( entrynode, "sqlfromfile" );
      if ( ssql != null && ssql.equalsIgnoreCase( "T" ) ) {
        sqlfromfile = true;
      }

      sqlfilename = XMLHandler.getTagValue( entrynode, "sqlfilename" );

      String sOneStatement = XMLHandler.getTagValue( entrynode, "sendOneStatement" );
      if ( sOneStatement != null && sOneStatement.equalsIgnoreCase( "T" ) ) {
        sendOneStatement = true;
      }

    } catch ( KettleException e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'sql' from XML node", e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      sql = rep.getJobEntryAttributeString( id_jobentry, "sql" );
      String sSubs = rep.getJobEntryAttributeString( id_jobentry, "useVariableSubstitution" );
      if ( sSubs != null && sSubs.equalsIgnoreCase( "T" ) ) {
        useVariableSubstitution = true;
      }

      String ssql = rep.getJobEntryAttributeString( id_jobentry, "sqlfromfile" );
      if ( ssql != null && ssql.equalsIgnoreCase( "T" ) ) {
        sqlfromfile = true;
      }

      String ssendOneStatement = rep.getJobEntryAttributeString( id_jobentry, "sendOneStatement" );
      if ( ssendOneStatement != null && ssendOneStatement.equalsIgnoreCase( "T" ) ) {
        sendOneStatement = true;
      }

      sqlfilename = rep.getJobEntryAttributeString( id_jobentry, "sqlfilename" );

      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'sql' from the repository with id_jobentry="
        + id_jobentry, dbe );
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", connection );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "sql", sql );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "useVariableSubstitution", useVariableSubstitution
        ? "T" : "F" );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "sqlfromfile", sqlfromfile ? "T" : "F" );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "sqlfilename", sqlfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "sendOneStatement", sendOneStatement ? "T" : "F" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'sql' to the repository for id_job=" + id_job, dbe );
    }
  }

  public void setSQL( String sql ) {
    this.sql = sql;
  }

  public String getSQL() {
    return sql;
  }

  public String getSQLFilename() {
    return sqlfilename;
  }

  public void setSQLFilename( String sqlfilename ) {
    this.sqlfilename = sqlfilename;
  }

  public boolean getUseVariableSubstitution() {
    return useVariableSubstitution;
  }

  public void setUseVariableSubstitution( boolean subs ) {
    useVariableSubstitution = subs;
  }

  public void setSQLFromFile( boolean sqlfromfilein ) {
    sqlfromfile = sqlfromfilein;
  }

  public boolean getSQLFromFile() {
    return sqlfromfile;
  }

  public boolean isSendOneStatement() {
    return sendOneStatement;
  }

  public void setSendOneStatement( boolean sendOneStatementin ) {
    sendOneStatement = sendOneStatementin;
  }

  public void setDatabase( DatabaseMeta database ) {
    this.connection = database;
  }

  public DatabaseMeta getDatabase() {
    return connection;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;

    if ( connection != null ) {
      Database db = new Database( this, connection );
      FileObject SQLfile = null;
      db.shareVariablesWith( this );
      try {
        String theSQL = null;
        db.connect( parentJob.getTransactionId(), null );

        if ( sqlfromfile ) {
          if ( sqlfilename == null ) {
            throw new KettleDatabaseException( BaseMessages.getString( PKG, "JobSQL.NoSQLFileSpecified" ) );
          }

          try {
            String realfilename = environmentSubstitute( sqlfilename );
            SQLfile = KettleVFS.getFileObject( realfilename, this );
            if ( !SQLfile.exists() ) {
              logError( BaseMessages.getString( PKG, "JobSQL.SQLFileNotExist", realfilename ) );
              throw new KettleDatabaseException( BaseMessages.getString(
                PKG, "JobSQL.SQLFileNotExist", realfilename ) );
            }
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobSQL.SQLFileExists", realfilename ) );
            }

            InputStream IS = KettleVFS.getInputStream( SQLfile );
            try {
              InputStreamReader BIS = new InputStreamReader( new BufferedInputStream( IS, 500 ) );
              StringBuilder lineSB = new StringBuilder( 256 );
              lineSB.setLength( 0 );

              BufferedReader buff = new BufferedReader( BIS );
              String sLine = null;
              theSQL = Const.CR;

              while ( ( sLine = buff.readLine() ) != null ) {
                if ( Utils.isEmpty( sLine ) ) {
                  theSQL = theSQL + Const.CR;
                } else {
                  theSQL = theSQL + Const.CR + sLine;
                }
              }
            } finally {
              IS.close();
            }
          } catch ( Exception e ) {
            throw new KettleDatabaseException( BaseMessages.getString( PKG, "JobSQL.ErrorRunningSQLfromFile" ), e );
          }

        } else {
          theSQL = sql;
        }
        if ( !Utils.isEmpty( theSQL ) ) {
          // let it run
          if ( useVariableSubstitution ) {
            theSQL = environmentSubstitute( theSQL );
          }
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobSQL.Log.SQlStatement", theSQL ) );
          }
          if ( sendOneStatement ) {
            db.execStatement( theSQL );
          } else {
            db.execStatements( theSQL );
          }
        }
      } catch ( KettleDatabaseException je ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobSQL.ErrorRunJobEntry", je.getMessage() ) );
      } finally {
        db.disconnect();
        if ( SQLfile != null ) {
          try {
            SQLfile.close();
          } catch ( Exception e ) {
            // Ignore errors
          }
        }
      }
    } else {
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobSQL.NoDatabaseConnection" ) );
    }

    if ( result.getNrErrors() == 0 ) {
      result.setResult( true );
    } else {
      result.setResult( false );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return true;
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] { connection, };
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( connection != null ) {
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( connection.getHostname(), ResourceType.SERVER ) );
      reference.getEntries().add( new ResourceEntry( connection.getDatabaseName(), ResourceType.DATABASENAME ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "SQL", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

}
