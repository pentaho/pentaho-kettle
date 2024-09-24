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

package org.pentaho.di.job.entries.mysqlbulkload;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.File;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a MySQL job entry.
 *
 * @author Samatar Hassan
 * @since Jan-2007
 */
public class JobEntryMysqlBulkLoad extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryMysqlBulkLoad.class; // for i18n purposes, needed by Translator2!!

  private String schemaname;
  private String tablename;
  private String filename;
  private String separator;
  private String enclosed;
  private String escaped;
  private String linestarted;
  private String lineterminated;
  private String ignorelines;
  private boolean replacedata;
  private String listattribut;
  private boolean localinfile;
  public int prorityvalue;
  private boolean addfiletoresult;

  private DatabaseMeta connection;

  public JobEntryMysqlBulkLoad( String n ) {
    super( n, "" );
    tablename = null;
    schemaname = null;
    filename = null;
    separator = null;
    enclosed = null;
    escaped = null;
    lineterminated = null;
    linestarted = null;
    replacedata = true;
    ignorelines = "0";
    listattribut = null;
    localinfile = true;
    connection = null;
    addfiletoresult = false;
  }

  public JobEntryMysqlBulkLoad() {
    this( "" );
  }

  public Object clone() {
    JobEntryMysqlBulkLoad je = (JobEntryMysqlBulkLoad) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "schemaname", schemaname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "tablename", tablename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "separator", separator ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "enclosed", enclosed ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "escaped", escaped ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "linestarted", linestarted ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "lineterminated", lineterminated ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "replacedata", replacedata ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "ignorelines", ignorelines ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "listattribut", listattribut ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "localinfile", localinfile ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "prorityvalue", prorityvalue ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "addfiletoresult", addfiletoresult ) );

    retval.append( "      " ).append(
      XMLHandler.addTagValue( "connection", connection == null ? null : connection.getName() ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      schemaname = XMLHandler.getTagValue( entrynode, "schemaname" );
      tablename = XMLHandler.getTagValue( entrynode, "tablename" );
      filename = XMLHandler.getTagValue( entrynode, "filename" );
      separator = XMLHandler.getTagValue( entrynode, "separator" );
      enclosed = XMLHandler.getTagValue( entrynode, "enclosed" );
      escaped = XMLHandler.getTagValue( entrynode, "escaped" );

      linestarted = XMLHandler.getTagValue( entrynode, "linestarted" );
      lineterminated = XMLHandler.getTagValue( entrynode, "lineterminated" );
      replacedata = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "replacedata" ) );
      ignorelines = XMLHandler.getTagValue( entrynode, "ignorelines" );
      listattribut = XMLHandler.getTagValue( entrynode, "listattribut" );
      localinfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "localinfile" ) );
      prorityvalue = Const.toInt( XMLHandler.getTagValue( entrynode, "prorityvalue" ), -1 );
      String dbname = XMLHandler.getTagValue( entrynode, "connection" );
      addfiletoresult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addfiletoresult" ) );
      connection = DatabaseMeta.findDatabase( databases, dbname );
    } catch ( KettleException e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'Mysql bulk load' from XML node", e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      schemaname = rep.getJobEntryAttributeString( id_jobentry, "schemaname" );
      tablename = rep.getJobEntryAttributeString( id_jobentry, "tablename" );
      filename = rep.getJobEntryAttributeString( id_jobentry, "filename" );
      separator = rep.getJobEntryAttributeString( id_jobentry, "separator" );
      enclosed = rep.getJobEntryAttributeString( id_jobentry, "enclosed" );
      escaped = rep.getJobEntryAttributeString( id_jobentry, "escaped" );
      linestarted = rep.getJobEntryAttributeString( id_jobentry, "linestarted" );
      lineterminated = rep.getJobEntryAttributeString( id_jobentry, "lineterminated" );
      replacedata = rep.getJobEntryAttributeBoolean( id_jobentry, "replacedata" );
      ignorelines = rep.getJobEntryAttributeString( id_jobentry, "ignorelines" );
      listattribut = rep.getJobEntryAttributeString( id_jobentry, "listattribut" );
      localinfile = rep.getJobEntryAttributeBoolean( id_jobentry, "localinfile" );
      prorityvalue = (int) rep.getJobEntryAttributeInteger( id_jobentry, "prorityvalue" );
      addfiletoresult = rep.getJobEntryAttributeBoolean( id_jobentry, "addfiletoresult" );

      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'Mysql bulk load' from the repository for id_jobentry=" + id_jobentry,
        dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "schemaname", schemaname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "tablename", tablename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filename", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "separator", separator );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "enclosed", enclosed );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "escaped", escaped );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "linestarted", linestarted );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "lineterminated", lineterminated );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "replacedata", replacedata );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ignorelines", ignorelines );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "listattribut", listattribut );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "localinfile", localinfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "prorityvalue", prorityvalue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "addfiletoresult", addfiletoresult );

      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", connection );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'Mysql Bulk Load' to the repository for id_job=" + id_job, dbe );
    }
  }

  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  public void setSchemaname( String schemaname ) {
    this.schemaname = schemaname;
  }

  public String getSchemaname() {
    return schemaname;
  }

  public String getTablename() {
    return tablename;
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
    return true;
  }

  public Result execute( Result previousResult, int nr ) {
    String ReplaceIgnore;
    String IgnoreNbrLignes = "";
    String ListOfColumn = "";
    String LocalExec = "";
    String PriorityText = "";
    String LineTerminatedby = "";
    String FieldTerminatedby = "";

    Result result = previousResult;
    result.setResult( false );

    String vfsFilename = environmentSubstitute( filename );

    // Let's check the filename ...
    if ( !Utils.isEmpty( vfsFilename ) ) {
      try {
        // User has specified a file, We can continue ...
        //

        // This is running over VFS but we need a normal file.
        // As such, we're going to verify that it's a local file...
        // We're also going to convert VFS FileObject to File
        //
        FileObject fileObject = KettleVFS.getFileObject( vfsFilename, this );
        if ( !( fileObject instanceof LocalFile ) ) {
          // MySQL LOAD DATA can only use local files, so that's what we limit ourselves to.
          //
          throw new KettleException( "Only local files are supported at this time, file ["
            + vfsFilename + "] is not a local file." );
        }

        // Convert it to a regular platform specific file name
        //
        String realFilename = KettleVFS.getFilename( fileObject );

        // Here we go... back to the regular scheduled program...
        //
        File file = new File( realFilename );
        if ( ( file.exists() && file.canRead() ) || isLocalInfile() == false ) {
          // User has specified an existing file, We can continue ...
          if ( log.isDetailed() ) {
            logDetailed( "File [" + realFilename + "] exists." );
          }

          if ( connection != null ) {
            // User has specified a connection, We can continue ...
            Database db = new Database( this, connection );
            db.shareVariablesWith( this );
            try {
              db.connect( parentJob.getTransactionId(), null );
              // Get schemaname
              String realSchemaname = environmentSubstitute( schemaname );
              // Get tablename
              String realTablename = environmentSubstitute( tablename );

              if ( db.checkTableExists( realTablename ) ) {
                // The table existe, We can continue ...
                if ( log.isDetailed() ) {
                  logDetailed( "Table [" + realTablename + "] exists." );
                }

                // Add schemaname (Most the time Schemaname.Tablename)
                if ( schemaname != null ) {
                  realTablename = realSchemaname + "." + realTablename;
                }

                // Set the REPLACE or IGNORE
                if ( isReplacedata() ) {
                  ReplaceIgnore = "REPLACE";
                } else {
                  ReplaceIgnore = "IGNORE";
                }

                // Set the IGNORE LINES
                if ( Const.toInt( getRealIgnorelines(), 0 ) > 0 ) {
                  IgnoreNbrLignes = "IGNORE " + getRealIgnorelines() + " LINES";
                }

                // Set list of Column
                if ( getRealListattribut() != null ) {
                  ListOfColumn = "(" + MysqlString( getRealListattribut() ) + ")";

                }

                // Local File execution
                if ( isLocalInfile() ) {
                  LocalExec = "LOCAL";
                }

                // Prority
                if ( prorityvalue == 1 ) {
                  // LOW
                  PriorityText = "LOW_PRIORITY";
                } else if ( prorityvalue == 2 ) {
                  // CONCURRENT
                  PriorityText = "CONCURRENT";
                }

                // Fields ....
                if ( getRealSeparator() != null || getRealEnclosed() != null || getRealEscaped() != null ) {
                  FieldTerminatedby = "FIELDS ";

                  if ( getRealSeparator() != null ) {
                    FieldTerminatedby =
                      FieldTerminatedby
                        + "TERMINATED BY '" + Const.replace( getRealSeparator(), "'", "''" ) + "'";
                  }
                  if ( getRealEnclosed() != null ) {
                    FieldTerminatedby =
                      FieldTerminatedby + " ENCLOSED BY '" + Const.replace( getRealEnclosed(), "'", "''" ) + "'";

                  }
                  if ( getRealEscaped() != null ) {

                    FieldTerminatedby =
                      FieldTerminatedby + " ESCAPED BY '" + Const.replace( getRealEscaped(), "'", "''" ) + "'";

                  }
                }

                // LINES ...
                if ( getRealLinestarted() != null || getRealLineterminated() != null ) {
                  LineTerminatedby = "LINES ";

                  // Line starting By
                  if ( getRealLinestarted() != null ) {
                    LineTerminatedby =
                      LineTerminatedby
                        + "STARTING BY '" + Const.replace( getRealLinestarted(), "'", "''" ) + "'";
                  }

                  // Line terminating By
                  if ( getRealLineterminated() != null ) {
                    LineTerminatedby =
                      LineTerminatedby
                        + " TERMINATED BY '" + Const.replace( getRealLineterminated(), "'", "''" ) + "'";
                  }
                }

                String SQLBULKLOAD =
                  "LOAD DATA "
                    + PriorityText + " " + LocalExec + " INFILE '" + realFilename.replace( '\\', '/' ) + "' "
                    + ReplaceIgnore + " INTO TABLE " + realTablename + " " + FieldTerminatedby + " "
                    + LineTerminatedby + " " + IgnoreNbrLignes + " " + ListOfColumn + ";";

                try {
                  // Run the SQL
                  db.execStatement( SQLBULKLOAD );

                  // Everything is OK...we can deconnect now
                  db.disconnect();

                  if ( isAddFileToResult() ) {
                    // Add zip filename to output files
                    ResultFile resultFile =
                      new ResultFile(
                        ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( realFilename, this ), parentJob
                          .getJobname(), toString() );
                    result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
                  }

                  result.setResult( true );
                } catch ( KettleDatabaseException je ) {
                  db.disconnect();
                  result.setNrErrors( 1 );
                  logError( "An error occurred executing this job entry : " + je.getMessage() );
                } catch ( KettleFileException e ) {
                  logError( "An error occurred executing this job entry : " + e.getMessage() );
                  result.setNrErrors( 1 );
                }
              } else {
                // Of course, the table should have been created already before the bulk load operation
                db.disconnect();
                result.setNrErrors( 1 );
                if ( log.isDetailed() ) {
                  logDetailed( "Table [" + realTablename + "] doesn't exist!" );
                }
              }
            } catch ( KettleDatabaseException dbe ) {
              db.disconnect();
              result.setNrErrors( 1 );
              logError( "An error occurred executing this entry: " + dbe.getMessage() );
            }
          } else {
            // No database connection is defined
            result.setNrErrors( 1 );
            logError( BaseMessages.getString( PKG, "JobMysqlBulkLoad.Nodatabase.Label" ) );
          }
        } else {
          // the file doesn't exist
          result.setNrErrors( 1 );
          logError( "File [" + realFilename + "] doesn't exist!" );
        }
      } catch ( Exception e ) {
        // An unexpected error occurred
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobMysqlBulkLoad.UnexpectedError.Label" ), e );
      }
    } else {
      // No file was specified
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobMysqlBulkLoad.Nofilename.Label" ) );
    }
    return result;
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] { connection, };
  }

  public boolean isReplacedata() {
    return replacedata;
  }

  public void setReplacedata( boolean replacedata ) {
    this.replacedata = replacedata;
  }

  public void setLocalInfile( boolean localinfile ) {
    this.localinfile = localinfile;
  }

  public boolean isLocalInfile() {
    return localinfile;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public void setSeparator( String separator ) {
    this.separator = separator;
  }

  public void setLineterminated( String lineterminated ) {
    this.lineterminated = lineterminated;
  }

  public void setLinestarted( String linestarted ) {
    this.linestarted = linestarted;
  }

  public String getEnclosed() {
    return enclosed;
  }

  public String getRealEnclosed() {
    return environmentSubstitute( getEnclosed() );
  }

  public void setEnclosed( String enclosed ) {
    this.enclosed = enclosed;
  }

  public String getEscaped() {
    return escaped;
  }

  public String getRealEscaped() {
    return environmentSubstitute( getEscaped() );
  }

  public void setEscaped( String escaped ) {
    this.escaped = escaped;
  }

  public String getSeparator() {
    return separator;
  }

  public String getLineterminated() {
    return lineterminated;
  }

  public String getLinestarted() {
    return linestarted;
  }

  public String getRealLinestarted() {
    return environmentSubstitute( getLinestarted() );
  }

  public String getRealLineterminated() {
    return environmentSubstitute( getLineterminated() );
  }

  public String getRealSeparator() {
    return environmentSubstitute( getSeparator() );
  }

  public void setIgnorelines( String ignorelines ) {
    this.ignorelines = ignorelines;
  }

  public String getIgnorelines() {
    return ignorelines;
  }

  public String getRealIgnorelines() {
    return environmentSubstitute( getIgnorelines() );
  }

  public void setListattribut( String listattribut ) {
    this.listattribut = listattribut;
  }

  public String getListattribut() {
    return listattribut;
  }

  public String getRealListattribut() {
    return environmentSubstitute( getListattribut() );
  }

  public void setAddFileToResult( boolean addfiletoresultin ) {
    this.addfiletoresult = addfiletoresultin;
  }

  public boolean isAddFileToResult() {
    return addfiletoresult;
  }

  private String MysqlString( String listcolumns ) {
    /*
     * Handle forbiden char like '
     */
    String returnString = "";
    String[] split = listcolumns.split( "," );

    for ( int i = 0; i < split.length; i++ ) {
      if ( returnString.equals( "" ) ) {
        returnString = "`" + Const.trim( split[i] ) + "`";
      } else {
        returnString = returnString + ", `" + Const.trim( split[i] ) + "`";
      }
    }

    return returnString;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    ResourceReference reference = null;
    if ( connection != null ) {
      reference = new ResourceReference( this );
      references.add( reference );
      reference.getEntries().add( new ResourceEntry( connection.getHostname(), ResourceType.SERVER ) );
      reference.getEntries().add( new ResourceEntry( connection.getDatabaseName(), ResourceType.DATABASENAME ) );
    }
    if ( filename != null ) {
      String realFilename = getRealFilename();
      if ( reference == null ) {
        reference = new ResourceReference( this );
        references.add( reference );
      }
      reference.getEntries().add( new ResourceEntry( realFilename, ResourceType.FILE ) );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notBlankValidator(), JobEntryValidatorUtils.fileExistsValidator() );
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks, ctx );

    JobEntryValidatorUtils.andValidator().validate( this, "tablename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

}
