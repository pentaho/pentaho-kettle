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

package org.pentaho.di.job.entries.tableexists;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a table exists job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntryTableExists extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryTableExists.class; // for i18n purposes, needed by Translator2!!

  private String tablename;
  private String schemaname;
  private DatabaseMeta connection;

  public JobEntryTableExists( String n ) {
    super( n, "" );
    schemaname = null;
    tablename = null;
    connection = null;
  }

  public JobEntryTableExists() {
    this( "" );
  }

  public Object clone() {
    JobEntryTableExists je = (JobEntryTableExists) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "tablename", tablename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "schemaname", schemaname ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "connection", connection == null ? null : connection.getName() ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      tablename = XMLHandler.getTagValue( entrynode, "tablename" );
      schemaname = XMLHandler.getTagValue( entrynode, "schemaname" );
      String dbname = XMLHandler.getTagValue( entrynode, "connection" );
      connection = DatabaseMeta.findDatabase( databases, dbname );
    } catch ( KettleException e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "TableExists.Meta.UnableLoadXml" ), e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      tablename = rep.getJobEntryAttributeString( id_jobentry, "tablename" );
      schemaname = rep.getJobEntryAttributeString( id_jobentry, "schemaname" );

      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "TableExists.Meta.UnableLoadRep", "" + id_jobentry ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "tablename", tablename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "schemaname", schemaname );

      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", connection );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "TableExists.Meta.UnableSaveRep", "" + id_job ), dbe );
    }
  }

  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  public String getTablename() {
    return tablename;
  }

  public String getSchemaname() {
    return schemaname;
  }

  public void setSchemaname( String schemaname ) {
    this.schemaname = schemaname;
  }

  public void setDatabase( DatabaseMeta database ) {
    this.connection = database;
  }

  public DatabaseMeta getDatabase() {
    return connection;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    if ( connection != null ) {
      Database db = new Database( this, connection );
      db.shareVariablesWith( this );
      try {
        db.connect( parentJob.getTransactionId(), null );
        String realTablename = environmentSubstitute( tablename );
        String realSchemaname = environmentSubstitute( schemaname );

        if ( db.checkTableExists( realSchemaname, realTablename ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "TableExists.Log.TableExists", realTablename ) );
          }
          result.setResult( true );
        } else {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "TableExists.Log.TableNotExists", realTablename ) );
          }
        }
      } catch ( KettleDatabaseException dbe ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "TableExists.Error.RunningJobEntry", dbe.getMessage() ) );
      } finally {
        if ( db != null ) {
          try {
            db.disconnect();
          } catch ( Exception e ) { /* Ignore */
          }
        }
      }
    } else {
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "TableExists.Error.NoConnectionDefined" ) );
    }

    return result;
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
    JobEntryValidatorUtils.andValidator().validate( this, "tablename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

}
