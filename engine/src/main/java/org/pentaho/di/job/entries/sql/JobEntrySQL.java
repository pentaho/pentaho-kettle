/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
  public static final String USE_VARIABLE_SUBSTITUTION_TAG = "useVariableSubstitution";
  public static final String SQLFROMFILE_TAG = "sqlfromfile";
  public static final String SQLFILENAME_TAG = "sqlfilename";
  public static final String SEND_ONE_STATEMENT_TAG = "sendOneStatement";
  public static final String CONNECTION_TAG = "connection";
  public static final String INDENT = "      ";
  private static final Class<?> PKG = JobEntrySQL.class; // for i18n purposes, needed by Translator2!!
  public static final String SQL_TAG = "sql";
  public static final String ID_DATABASE = "id_database";

  private String sql;
  private DatabaseMeta databaseMeta;
  private boolean useVariableSubstitution = false;
  private boolean sqlFromFile = false;
  private String sqlFilename;
  private boolean sendOneStatement = false;

  public JobEntrySQL( String n ) {
    super( n, "" );
    sql = null;
    databaseMeta = null;
  }

  public JobEntrySQL() {
    this( "" );
  }

  @Override
  public Object clone() {
    JobEntrySQL je = (JobEntrySQL) super.clone();
    return je;
  }

  public String getXML() {
    @SuppressWarnings( "StringBufferReplaceableByString" )
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );

    retval.append( INDENT ).append( XMLHandler.addTagValue( "sql", sql ) );
    retval.append( INDENT ).append(
      XMLHandler.addTagValue( USE_VARIABLE_SUBSTITUTION_TAG, useVariableSubstitution ? "T" : "F" ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( SQLFROMFILE_TAG, sqlFromFile ? "T" : "F" ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( SQLFILENAME_TAG, sqlFilename ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( SEND_ONE_STATEMENT_TAG, sendOneStatement ? "T" : "F" ) );

    retval.append( INDENT ).append(
      XMLHandler.addTagValue( CONNECTION_TAG, databaseMeta == null ? null : databaseMeta.getName() ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      sql = XMLHandler.getTagValue( entrynode, "sql" );
      String dbname = XMLHandler.getTagValue( entrynode, CONNECTION_TAG );
      String sSubs = XMLHandler.getTagValue( entrynode, USE_VARIABLE_SUBSTITUTION_TAG );

      if ( sSubs != null && sSubs.equalsIgnoreCase( "T" ) ) {
        useVariableSubstitution = true;
      }
      databaseMeta = DatabaseMeta.findDatabase( databases, dbname );

      String ssql = XMLHandler.getTagValue( entrynode, SQLFROMFILE_TAG );
      if ( ssql != null && ssql.equalsIgnoreCase( "T" ) ) {
        sqlFromFile = true;
      }

      sqlFilename = XMLHandler.getTagValue( entrynode, SQLFILENAME_TAG );

      String sOneStatement = XMLHandler.getTagValue( entrynode, SEND_ONE_STATEMENT_TAG );
      if ( sOneStatement != null && sOneStatement.equalsIgnoreCase( "T" ) ) {
        sendOneStatement = true;
      }

    } catch ( KettleException e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'sql' from XML node", e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId idJobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      sql = rep.getJobEntryAttributeString( idJobentry, SQL_TAG );
      String sSubs = rep.getJobEntryAttributeString( idJobentry, USE_VARIABLE_SUBSTITUTION_TAG );
      if ( sSubs != null && sSubs.equalsIgnoreCase( "T" ) ) {
        useVariableSubstitution = true;
      }

      String ssql = rep.getJobEntryAttributeString( idJobentry, SQLFROMFILE_TAG );
      if ( ssql != null && ssql.equalsIgnoreCase( "T" ) ) {
        sqlFromFile = true;
      }

      String ssendOneStatement = rep.getJobEntryAttributeString( idJobentry, SEND_ONE_STATEMENT_TAG );
      if ( ssendOneStatement != null && ssendOneStatement.equalsIgnoreCase( "T" ) ) {
        sendOneStatement = true;
      }

      sqlFilename = rep.getJobEntryAttributeString( idJobentry, SQLFILENAME_TAG );

      databaseMeta = rep.loadDatabaseMetaFromJobEntryAttribute( idJobentry, CONNECTION_TAG, ID_DATABASE, databases );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'sql' from the repository with idJobentry="
        + idJobentry, dbe );
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idJob ) throws KettleException {
    try {
      rep.saveDatabaseMetaJobEntryAttribute( idJob, getObjectId(), CONNECTION_TAG, ID_DATABASE, databaseMeta );

      rep.saveJobEntryAttribute( idJob, getObjectId(), SQL_TAG, sql );
      rep.saveJobEntryAttribute( idJob, getObjectId(), USE_VARIABLE_SUBSTITUTION_TAG, useVariableSubstitution
        ? "T" : "F" );
      rep.saveJobEntryAttribute( idJob, getObjectId(), SQLFROMFILE_TAG, sqlFromFile ? "T" : "F" );
      rep.saveJobEntryAttribute( idJob, getObjectId(), SQLFILENAME_TAG, sqlFilename );
      rep.saveJobEntryAttribute( idJob, getObjectId(), SEND_ONE_STATEMENT_TAG, sendOneStatement ? "T" : "F" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'sql' to the repository for idJob=" + idJob, dbe );
    }
  }

  public void setSQL( String sql ) {
    this.sql = sql;
  }

  public String getSQL() {
    return sql;
  }

  public String getSQLFilename() {
    return sqlFilename;
  }

  public void setSQLFilename( String sqlFilename ) {
    this.sqlFilename = sqlFilename;
  }

  public boolean getUseVariableSubstitution() {
    return useVariableSubstitution;
  }

  public void setUseVariableSubstitution( boolean subs ) {
    useVariableSubstitution = subs;
  }

  public void setSQLFromFile( boolean sqlFromFileIn ) {
    sqlFromFile = sqlFromFileIn;
  }

  public boolean getSQLFromFile() {
    return sqlFromFile;
  }

  public boolean isSendOneStatement() {
    return sendOneStatement;
  }

  public void setSendOneStatement( boolean sendOneStatementIn ) {
    sendOneStatement = sendOneStatementIn;
  }

  public void setDatabase( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  public DatabaseMeta getDatabase() {
    return databaseMeta;
  }

  public Result execute( Result result, int nr ) {

    if ( databaseMeta != null ) {
      try ( Database db = new Database( this, databaseMeta ) ) {
        String theSql = sqlFromFile ? buildSqlFromFile() : sql;
        if ( Utils.isEmpty( theSql ) ) {
          return result;
        }
        db.shareVariablesWith( this );
        db.connect( parentJob.getTransactionId(), null );
        // let it run
        if ( useVariableSubstitution ) {
          theSql = environmentSubstitute( theSql );
        }
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobSQL.Log.SQlStatement", theSql ) );
        }
        if ( sendOneStatement ) {
          db.execStatement( theSql );
        } else {
          db.execStatements( theSql );
        }
      } catch ( KettleDatabaseException je ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobSQL.ErrorRunJobEntry", je.getMessage() ) );
      }
    } else {
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobSQL.NoDatabaseConnection" ) );
    }

    result.setResult( result.getNrErrors() == 0 );
    return result;
  }

  public String buildSqlFromFile() throws KettleDatabaseException {
    if ( sqlFilename == null ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "JobSQL.NoSQLFileSpecified" ) );
    }

    String realFilename = environmentSubstitute( sqlFilename );
    try ( FileObject sqlFile = KettleVFS.getFileObject( realFilename, this ) ) {
      if ( !sqlFile.exists() ) {
        logError( BaseMessages.getString( PKG, "JobSQL.SQLFileNotExist", realFilename ) );
        throw new KettleDatabaseException( BaseMessages.getString(
          PKG, "JobSQL.SQLFileNotExist", realFilename ) );
      }
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobSQL.SQLFileExists", realFilename ) );
      }

      try ( InputStream inputStream = KettleVFS.getInputStream( sqlFile ) ) {
        InputStreamReader bufferedStream = new InputStreamReader( new BufferedInputStream( inputStream, 500 ) );

        BufferedReader buff = new BufferedReader( bufferedStream );
        String sLine;
        StringBuilder sqlBuilder = new StringBuilder( Const.CR );

        while ( ( sLine = buff.readLine() ) != null ) {
          if ( Utils.isEmpty( sLine ) ) {
            sqlBuilder.append( Const.CR );
          } else {
            sqlBuilder.append( Const.CR ).append( sLine );
          }
        }
        return sqlBuilder.toString();
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "JobSQL.ErrorRunningSQLfromFile" ), e );
    }
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return true;
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] { databaseMeta, };
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( databaseMeta != null ) {
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( databaseMeta.getHostname(), ResourceType.SERVER ) );
      reference.getEntries().add( new ResourceEntry( databaseMeta.getDatabaseName(), ResourceType.DATABASENAME ) );
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
