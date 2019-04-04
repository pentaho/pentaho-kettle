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

package org.pentaho.di.job.entries.columnsexist;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.List;

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
 * This defines a column exists job entry.
 *
 * @author Samatar
 * @since 16-06-2008
 *
 */

public class JobEntryColumnsExist extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryColumnsExist.class; // for i18n purposes, needed by Translator2!!
  private String schemaname;
  private String tablename;
  private DatabaseMeta connection;
  private String[] arguments;

  public JobEntryColumnsExist( String n ) {
    super( n, "" );
    schemaname = null;
    tablename = null;
    connection = null;
  }

  public JobEntryColumnsExist() {
    this( "" );
  }

  public void allocate( int nrFields ) {
    arguments = new String[nrFields];
  }

  public Object clone() {
    JobEntryColumnsExist je = (JobEntryColumnsExist) super.clone();
    if ( arguments != null ) {
      int nrFields = arguments.length;
      je.allocate( nrFields );
      System.arraycopy( arguments, 0, je.arguments, 0, nrFields );
    }
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "tablename", tablename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "schemaname", schemaname ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "connection", connection == null ? null : connection.getName() ) );

    retval.append( "      <fields>" ).append( Const.CR );
    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "name", arguments[i] ) );
        retval.append( "        </field>" ).append( Const.CR );
      }
    }
    retval.append( "      </fields>" ).append( Const.CR );

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

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      // How many field arguments?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      allocate( nrFields );

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        arguments[i] = XMLHandler.getTagValue( fnode, "name" );
      }

    } catch ( KettleException e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryColumnsExist.Meta.UnableLoadXml" ), e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      tablename = rep.getJobEntryAttributeString( id_jobentry, "tablename" );
      schemaname = rep.getJobEntryAttributeString( id_jobentry, "schemaname" );

      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "name" );
      arguments = new String[argnr];

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        arguments[a] = rep.getJobEntryAttributeString( id_jobentry, a, "name" );
      }

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryColumnsExist.Meta.UnableLoadRep", ""
        + id_jobentry ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "tablename", tablename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "schemaname", schemaname );

      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", connection );

      // save the arguments...
      if ( arguments != null ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "name", arguments[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryColumnsExist.Meta.UnableSaveRep", ""
        + id_job ), dbe );
    }
  }

  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  public String getTablename() {
    return tablename;
  }

  public void setSchemaname( String schemaname ) {
    this.schemaname = schemaname;
  }

  public String getSchemaname() {
    return schemaname;
  }

  public String[] getArguments() {
    return arguments;
  }

  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
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
    result.setNrErrors( 1 );

    int nrexistcolums = 0;
    int nrnotexistcolums = 0;

    if ( Utils.isEmpty( tablename ) ) {
      logError( BaseMessages.getString( PKG, "JobEntryColumnsExist.Error.TablenameEmpty" ) );
      return result;
    }
    if ( arguments == null ) {
      logError( BaseMessages.getString( PKG, "JobEntryColumnsExist.Error.ColumnameEmpty" ) );
      return result;
    }
    if ( connection != null ) {
      Database db = getNewDatabaseFromMeta();
      db.shareVariablesWith( this );
      try {
        String realSchemaname = environmentSubstitute( schemaname );
        String realTablename = environmentSubstitute( tablename );

        db.connect( parentJob.getTransactionId(), null );

        if ( db.checkTableExists( realSchemaname, realTablename ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryColumnsExist.Log.TableExists", realTablename ) );
          }

          for ( int i = 0; i < arguments.length && !parentJob.isStopped(); i++ ) {
            String realColumnname = environmentSubstitute( arguments[i] );

            if ( db.checkColumnExists( realSchemaname, realTablename, realColumnname ) ) {
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString(
                  PKG, "JobEntryColumnsExist.Log.ColumnExists", realColumnname, realTablename ) );
              }
              nrexistcolums++;
            } else {
              logError( BaseMessages.getString(
                PKG, "JobEntryColumnsExist.Log.ColumnNotExists", realColumnname, realTablename ) );
              nrnotexistcolums++;
            }
          }
        } else {
          logError( BaseMessages.getString( PKG, "JobEntryColumnsExist.Log.TableNotExists", realTablename ) );
        }
      } catch ( KettleDatabaseException dbe ) {
        logError( BaseMessages.getString( PKG, "JobEntryColumnsExist.Error.UnexpectedError", dbe.getMessage() ) );
      } finally {
        if ( db != null ) {
          try {
            db.disconnect();
          } catch ( Exception e ) { /* Ignore */
          }
        }
      }
    } else {
      logError( BaseMessages.getString( PKG, "JobEntryColumnsExist.Error.NoDbConnection" ) );
    }

    result.setEntryNr( nrnotexistcolums );
    result.setNrLinesWritten( nrexistcolums );
    // result is true only if all columns found (PDI-15801)
    if (  nrexistcolums == arguments.length ) {
      result.setNrErrors( 0 );
      result.setResult( true );
    }
    return result;
  }

  Database getNewDatabaseFromMeta() {
    return new Database( this, connection );
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
    JobEntryValidatorUtils.andValidator().validate( this, "tablename", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "columnname", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

}
