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

package org.pentaho.di.job.entries.mssqlbulkload;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.pentaho.di.core.database.MSSQLServerDatabaseMeta;
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
 * This defines a MSSQL Bulk job entry.
 *
 * @author Samatar Hassan
 * @since Jan-2007
 */
public class JobEntryMssqlBulkLoad extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryMssqlBulkLoad.class; // for i18n purposes, needed by Translator2!!

  private String schemaname;
  private String tablename;
  private String filename;
  private String datafiletype;
  private String fieldterminator;
  private String lineterminated;
  private String codepage;
  private String specificcodepage;
  private int startfile;
  private int endfile;
  private String orderby;
  private boolean addfiletoresult;
  private String formatfilename;
  private boolean firetriggers;
  private boolean checkconstraints;
  private boolean keepnulls;
  private boolean tablock;
  private String errorfilename;
  private boolean adddatetime;
  private String orderdirection;
  private int maxerrors;
  private int batchsize;
  private int rowsperbatch;
  private boolean keepidentity;
  private boolean truncate;

  private DatabaseMeta connection;

  public JobEntryMssqlBulkLoad( String n ) {
    super( n, "" );
    tablename = null;
    schemaname = null;
    filename = null;
    datafiletype = "char";
    fieldterminator = null;
    lineterminated = null;
    codepage = "OEM";
    specificcodepage = null;
    checkconstraints = false;
    keepnulls = false;
    tablock = false;
    startfile = 0;
    endfile = 0;
    orderby = null;

    errorfilename = null;
    adddatetime = false;
    orderdirection = "Asc";
    maxerrors = 0;
    batchsize = 0;
    rowsperbatch = 0;

    connection = null;
    addfiletoresult = false;
    formatfilename = null;
    firetriggers = false;
    keepidentity = false;
    truncate = false;
  }

  public JobEntryMssqlBulkLoad() {
    this( "" );
  }

  public Object clone() {
    JobEntryMssqlBulkLoad je = (JobEntryMssqlBulkLoad) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "schemaname", schemaname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "tablename", tablename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "datafiletype", datafiletype ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fieldterminator", fieldterminator ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "lineterminated", lineterminated ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "codepage", codepage ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "specificcodepage", specificcodepage ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "formatfilename", formatfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "firetriggers", firetriggers ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "checkconstraints", checkconstraints ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "keepnulls", keepnulls ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "keepidentity", keepidentity ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "tablock", tablock ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "startfile", startfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "endfile", endfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "orderby", orderby ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "orderdirection", orderdirection ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "maxerrors", maxerrors ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "batchsize", batchsize ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "rowsperbatch", rowsperbatch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "errorfilename", errorfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "adddatetime", adddatetime ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addfiletoresult", addfiletoresult ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "truncate", truncate ) );

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
      datafiletype = XMLHandler.getTagValue( entrynode, "datafiletype" );
      fieldterminator = XMLHandler.getTagValue( entrynode, "fieldterminator" );

      lineterminated = XMLHandler.getTagValue( entrynode, "lineterminated" );
      codepage = XMLHandler.getTagValue( entrynode, "codepage" );
      specificcodepage = XMLHandler.getTagValue( entrynode, "specificcodepage" );
      formatfilename = XMLHandler.getTagValue( entrynode, "formatfilename" );

      firetriggers = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "firetriggers" ) );
      checkconstraints = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "checkconstraints" ) );
      keepnulls = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "keepnulls" ) );
      keepidentity = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "keepidentity" ) );

      tablock = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "tablock" ) );
      startfile = Const.toInt( XMLHandler.getTagValue( entrynode, "startfile" ), 0 );
      endfile = Const.toInt( XMLHandler.getTagValue( entrynode, "endfile" ), 0 );

      orderby = XMLHandler.getTagValue( entrynode, "orderby" );
      orderdirection = XMLHandler.getTagValue( entrynode, "orderdirection" );

      errorfilename = XMLHandler.getTagValue( entrynode, "errorfilename" );

      maxerrors = Const.toInt( XMLHandler.getTagValue( entrynode, "maxerrors" ), 0 );
      batchsize = Const.toInt( XMLHandler.getTagValue( entrynode, "batchsize" ), 0 );
      rowsperbatch = Const.toInt( XMLHandler.getTagValue( entrynode, "rowsperbatch" ), 0 );
      adddatetime = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "adddatetime" ) );
      String dbname = XMLHandler.getTagValue( entrynode, "connection" );
      addfiletoresult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addfiletoresult" ) );
      truncate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "truncate" ) );

      connection = DatabaseMeta.findDatabase( databases, dbname );

    } catch ( KettleException e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'MSsql bulk load' from XML node", e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      schemaname = rep.getJobEntryAttributeString( id_jobentry, "schemaname" );
      tablename = rep.getJobEntryAttributeString( id_jobentry, "tablename" );
      filename = rep.getJobEntryAttributeString( id_jobentry, "filename" );

      datafiletype = rep.getJobEntryAttributeString( id_jobentry, "datafiletype" );
      fieldterminator = rep.getJobEntryAttributeString( id_jobentry, "fieldterminator" );

      lineterminated = rep.getJobEntryAttributeString( id_jobentry, "lineterminated" );
      codepage = rep.getJobEntryAttributeString( id_jobentry, "codepage" );
      specificcodepage = rep.getJobEntryAttributeString( id_jobentry, "specificcodepage" );
      formatfilename = rep.getJobEntryAttributeString( id_jobentry, "formatfilename" );
      firetriggers = rep.getJobEntryAttributeBoolean( id_jobentry, "firetriggers" );
      checkconstraints = rep.getJobEntryAttributeBoolean( id_jobentry, "checkconstraints" );
      keepnulls = rep.getJobEntryAttributeBoolean( id_jobentry, "keepnulls" );
      keepidentity = rep.getJobEntryAttributeBoolean( id_jobentry, "keepidentity" );

      tablock = rep.getJobEntryAttributeBoolean( id_jobentry, "tablock" );

      startfile = (int) rep.getJobEntryAttributeInteger( id_jobentry, "startfile" );
      endfile = (int) rep.getJobEntryAttributeInteger( id_jobentry, "endfile" );

      orderby = rep.getJobEntryAttributeString( id_jobentry, "orderby" );
      orderdirection = rep.getJobEntryAttributeString( id_jobentry, "orderdirection" );

      errorfilename = rep.getJobEntryAttributeString( id_jobentry, "errorfilename" );
      maxerrors = (int) rep.getJobEntryAttributeInteger( id_jobentry, "maxerrors" );
      batchsize = (int) rep.getJobEntryAttributeInteger( id_jobentry, "batchsize" );
      rowsperbatch = (int) rep.getJobEntryAttributeInteger( id_jobentry, "rowsperbatch" );
      adddatetime = rep.getJobEntryAttributeBoolean( id_jobentry, "adddatetime" );

      addfiletoresult = rep.getJobEntryAttributeBoolean( id_jobentry, "addfiletoresult" );
      truncate = rep.getJobEntryAttributeBoolean( id_jobentry, "truncate" );

      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'MSsql bulk load' from the repository for id_jobentry=" + id_jobentry,
        dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "schemaname", schemaname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "tablename", tablename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filename", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "datafiletype", datafiletype );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "fieldterminator", fieldterminator );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "lineterminated", lineterminated );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "codepage", codepage );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "specificcodepage", specificcodepage );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "formatfilename", formatfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "firetriggers", firetriggers );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "checkconstraints", checkconstraints );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "keepnulls", keepnulls );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "keepidentity", keepidentity );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "tablock", tablock );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "startfile", startfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "endfile", endfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "orderby", orderby );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "orderdirection", orderdirection );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "errorfilename", errorfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "maxerrors", maxerrors );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "batchsize", batchsize );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "rowsperbatch", rowsperbatch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "adddatetime", adddatetime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "addfiletoresult", addfiletoresult );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "truncate", truncate );

      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", connection );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'MSsql Bulk Load' to the repository for id_job=" + id_job, dbe );
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

  public void setMaxErrors( int maxerrors ) {
    this.maxerrors = maxerrors;
  }

  public int getMaxErrors() {
    return maxerrors;
  }

  public int getBatchSize() {
    return batchsize;
  }

  public void setBatchSize( int batchsize ) {
    this.batchsize = batchsize;
  }

  public int getRowsPerBatch() {
    return rowsperbatch;
  }

  public void setRowsPerBatch( int rowsperbatch ) {
    this.rowsperbatch = rowsperbatch;
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
    String TakeFirstNbrLines = "";
    String LineTerminatedby = "";
    String FieldTerminatedby = "";
    boolean useFieldSeparator = false;
    String UseCodepage = "";
    String ErrorfileName = "";

    Result result = previousResult;
    result.setResult( false );

    String vfsFilename = environmentSubstitute( filename );
    FileObject fileObject = null;
    // Let's check the filename ...
    if ( !Utils.isEmpty( vfsFilename ) ) {
      try {
        // User has specified a file, We can continue ...
        //
        // This is running over VFS but we need a normal file.
        // As such, we're going to verify that it's a local file...
        // We're also going to convert VFS FileObject to File
        //
        fileObject = KettleVFS.getFileObject( vfsFilename, this );
        if ( !( fileObject instanceof LocalFile ) ) {
          // MSSQL BUKL INSERT can only use local files, so that's what we limit ourselves to.
          //
          throw new KettleException( BaseMessages.getString(
            PKG, "JobMssqlBulkLoad.Error.OnlyLocalFileSupported", vfsFilename ) );
        }

        // Convert it to a regular platform specific file name
        //
        String realFilename = KettleVFS.getFilename( fileObject );

        // Here we go... back to the regular scheduled program...
        //
        File file = new File( realFilename );
        if ( file.exists() && file.canRead() ) {
          // User has specified an existing file, We can continue ...
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobMssqlBulkLoad.FileExists.Label", realFilename ) );
          }

          if ( connection != null ) {
            // User has specified a connection, We can continue ...
            Database db = new Database( this, connection );

            if ( !( db.getDatabaseMeta().getDatabaseInterface() instanceof MSSQLServerDatabaseMeta ) ) {
              logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Error.DbNotMSSQL", connection
                .getDatabaseName() ) );
              return result;
            }
            db.shareVariablesWith( this );
            try {
              db.connect( parentJob.getTransactionId(), null );
              // Get schemaname
              String realSchemaname = environmentSubstitute( schemaname );
              // Get tablename
              String realTablename = environmentSubstitute( tablename );

              // Add schemaname (Most the time Schemaname.Tablename)
              if ( schemaname != null ) {
                realTablename = realSchemaname + "." + realTablename;
              }

              if ( db.checkTableExists( realTablename ) ) {
                // The table existe, We can continue ...
                if ( log.isDetailed() ) {
                  logDetailed( BaseMessages.getString( PKG, "JobMssqlBulkLoad.TableExists.Label", realTablename ) );
                }

                // FIELDTERMINATOR
                String Fieldterminator = getRealFieldTerminator();
                if ( Utils.isEmpty( Fieldterminator )
                  && ( datafiletype.equals( "char" ) || datafiletype.equals( "widechar" ) ) ) {
                  logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Error.FieldTerminatorMissing" ) );
                  return result;
                } else {
                  if ( datafiletype.equals( "char" ) || datafiletype.equals( "widechar" ) ) {
                    useFieldSeparator = true;
                    FieldTerminatedby = "FIELDTERMINATOR='" + Fieldterminator + "'";
                  }
                }
                // Check Specific Code page
                if ( codepage.equals( "Specific" ) ) {
                  String realCodePage = environmentSubstitute( codepage );
                  if ( specificcodepage.length() < 0 ) {
                    logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Error.SpecificCodePageMissing" ) );
                    return result;

                  } else {
                    UseCodepage = "CODEPAGE = '" + realCodePage + "'";
                  }
                } else {
                  UseCodepage = "CODEPAGE = '" + codepage + "'";
                }

                // Check Error file
                String realErrorFile = environmentSubstitute( errorfilename );
                if ( realErrorFile != null ) {
                  File errorfile = new File( realErrorFile );
                  if ( errorfile.exists() && !adddatetime ) {
                    // The error file is created when the command is executed. An error occurs if the file already
                    // exists.
                    logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Error.ErrorFileExists" ) );
                    return result;
                  }
                  if ( adddatetime ) {
                    // Add date time to filename...
                    SimpleDateFormat daf = new SimpleDateFormat();
                    Date now = new Date();
                    daf.applyPattern( "yyyMMdd_HHmmss" );
                    String d = daf.format( now );

                    ErrorfileName = "ERRORFILE ='" + realErrorFile + "_" + d + "'";
                  } else {
                    ErrorfileName = "ERRORFILE ='" + realErrorFile + "'";
                  }
                }

                // ROWTERMINATOR
                String Rowterminator = getRealLineterminated();
                if ( !Utils.isEmpty( Rowterminator ) ) {
                  LineTerminatedby = "ROWTERMINATOR='" + Rowterminator + "'";
                }

                // Start file at
                if ( startfile > 0 ) {
                  TakeFirstNbrLines = "FIRSTROW=" + startfile;
                }

                // End file at
                if ( endfile > 0 ) {
                  TakeFirstNbrLines = "LASTROW=" + endfile;
                }

                // Truncate table?
                String SQLBULKLOAD = "";
                if ( truncate ) {
                  SQLBULKLOAD = "TRUNCATE TABLE " + realTablename + ";";
                }

                // Build BULK Command
                SQLBULKLOAD =
                  SQLBULKLOAD
                    + "BULK INSERT " + realTablename + " FROM " + "'" + realFilename.replace( '\\', '/' )
                    + "'";
                SQLBULKLOAD = SQLBULKLOAD + " WITH (";
                if ( useFieldSeparator ) {
                  SQLBULKLOAD = SQLBULKLOAD + FieldTerminatedby;
                } else {
                  SQLBULKLOAD = SQLBULKLOAD + "DATAFILETYPE ='" + datafiletype + "'";
                }

                if ( LineTerminatedby.length() > 0 ) {
                  SQLBULKLOAD = SQLBULKLOAD + "," + LineTerminatedby;
                }
                if ( TakeFirstNbrLines.length() > 0 ) {
                  SQLBULKLOAD = SQLBULKLOAD + "," + TakeFirstNbrLines;
                }
                if ( UseCodepage.length() > 0 ) {
                  SQLBULKLOAD = SQLBULKLOAD + "," + UseCodepage;
                }
                String realFormatFile = environmentSubstitute( formatfilename );
                if ( realFormatFile != null ) {
                  SQLBULKLOAD = SQLBULKLOAD + ", FORMATFILE='" + realFormatFile + "'";
                }
                if ( firetriggers ) {
                  SQLBULKLOAD = SQLBULKLOAD + ",FIRE_TRIGGERS";
                }
                if ( keepnulls ) {
                  SQLBULKLOAD = SQLBULKLOAD + ",KEEPNULLS";
                }
                if ( keepidentity ) {
                  SQLBULKLOAD = SQLBULKLOAD + ",KEEPIDENTITY";
                }
                if ( checkconstraints ) {
                  SQLBULKLOAD = SQLBULKLOAD + ",CHECK_CONSTRAINTS";
                }
                if ( tablock ) {
                  SQLBULKLOAD = SQLBULKLOAD + ",TABLOCK";
                }
                if ( orderby != null ) {
                  SQLBULKLOAD = SQLBULKLOAD + ",ORDER ( " + orderby + " " + orderdirection + ")";
                }
                if ( ErrorfileName.length() > 0 ) {
                  SQLBULKLOAD = SQLBULKLOAD + ", " + ErrorfileName;
                }
                if ( maxerrors > 0 ) {
                  SQLBULKLOAD = SQLBULKLOAD + ", MAXERRORS=" + maxerrors;
                }
                if ( batchsize > 0 ) {
                  SQLBULKLOAD = SQLBULKLOAD + ", BATCHSIZE=" + batchsize;
                }
                if ( rowsperbatch > 0 ) {
                  SQLBULKLOAD = SQLBULKLOAD + ", ROWS_PER_BATCH=" + rowsperbatch;
                }
                // End of Bulk command
                SQLBULKLOAD = SQLBULKLOAD + ")";

                try {
                  // Run the SQL
                  db.execStatement( SQLBULKLOAD );

                  // Everything is OK...we can disconnect now
                  db.disconnect();

                  if ( isAddFileToResult() ) {
                    // Add filename to output files
                    ResultFile resultFile =
                      new ResultFile(
                        ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( realFilename, this ), parentJob
                          .getJobname(), toString() );
                    result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
                  }

                  result.setResult( true );
                } catch ( KettleDatabaseException je ) {
                  result.setNrErrors( 1 );
                  logError( "An error occurred executing this job entry : " + je.getMessage(), je );
                } catch ( KettleFileException e ) {
                  logError( "An error occurred executing this job entry : " + e.getMessage(), e );
                  result.setNrErrors( 1 );
                } finally {
                  if ( db != null ) {
                    db.disconnect();
                    db = null;
                  }
                }
              } else {
                // Of course, the table should have been created already before the bulk load operation
                db.disconnect();
                result.setNrErrors( 1 );
                logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Error.TableNotExists", realTablename ) );
              }
            } catch ( KettleDatabaseException dbe ) {
              db.disconnect();
              result.setNrErrors( 1 );
              logError( "An error occurred executing this entry: " + dbe.getMessage() );
            }
          } else {
            // No database connection is defined
            result.setNrErrors( 1 );
            logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Nodatabase.Label" ) );
          }
        } else {
          // the file doesn't exist
          result.setNrErrors( 1 );
          logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Error.FileNotExists", realFilename ) );
        }
      } catch ( Exception e ) {
        // An unexpected error occurred
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.UnexpectedError.Label" ), e );
      } finally {
        try {
          if ( fileObject != null ) {
            fileObject.close();
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
      }
    } else {
      // No file was specified
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobMssqlBulkLoad.Nofilename.Label" ) );
    }
    return result;
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] { connection, };
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public void setFieldTerminator( String fieldterminator ) {
    this.fieldterminator = fieldterminator;
  }

  public void setLineterminated( String lineterminated ) {
    this.lineterminated = lineterminated;
  }

  public void setCodePage( String codepage ) {
    this.codepage = codepage;
  }

  public String getCodePage() {
    return codepage;
  }

  public void setSpecificCodePage( String specificcodepage ) {
    this.specificcodepage = specificcodepage;
  }

  public String getSpecificCodePage() {
    return specificcodepage;
  }

  public void setFormatFilename( String formatfilename ) {
    this.formatfilename = formatfilename;
  }

  public String getFormatFilename() {
    return formatfilename;
  }

  public String getFieldTerminator() {
    return fieldterminator;
  }

  public String getLineterminated() {
    return lineterminated;
  }

  public String getDataFileType() {
    return datafiletype;
  }

  public void setDataFileType( String datafiletype ) {
    this.datafiletype = datafiletype;
  }

  public String getRealLineterminated() {
    return environmentSubstitute( getLineterminated() );
  }

  public String getRealFieldTerminator() {
    return environmentSubstitute( getFieldTerminator() );
  }

  public void setStartFile( int startfile ) {
    this.startfile = startfile;
  }

  public int getStartFile() {
    return startfile;
  }

  public void setEndFile( int endfile ) {
    this.endfile = endfile;
  }

  public int getEndFile() {
    return endfile;
  }

  public void setOrderBy( String orderby ) {
    this.orderby = orderby;
  }

  public String getOrderBy() {
    return orderby;
  }

  public String getOrderDirection() {
    return orderdirection;
  }

  public void setOrderDirection( String orderdirection ) {
    this.orderdirection = orderdirection;
  }

  public void setErrorFilename( String errorfilename ) {
    this.errorfilename = errorfilename;
  }

  public String getErrorFilename() {
    return errorfilename;
  }

  public String getRealOrderBy() {
    return environmentSubstitute( getOrderBy() );
  }

  public void setAddFileToResult( boolean addfiletoresultin ) {
    this.addfiletoresult = addfiletoresultin;
  }

  public boolean isAddFileToResult() {
    return addfiletoresult;
  }

  public void setTruncate( boolean truncate ) {
    this.truncate = truncate;
  }

  public boolean isTruncate() {
    return truncate;
  }

  public void setAddDatetime( boolean adddatetime ) {
    this.adddatetime = adddatetime;
  }

  public boolean isAddDatetime() {
    return adddatetime;
  }

  public void setFireTriggers( boolean firetriggers ) {
    this.firetriggers = firetriggers;
  }

  public boolean isFireTriggers() {
    return firetriggers;
  }

  public void setCheckConstraints( boolean checkconstraints ) {
    this.checkconstraints = checkconstraints;
  }

  public boolean isCheckConstraints() {
    return checkconstraints;
  }

  public void setKeepNulls( boolean keepnulls ) {
    this.keepnulls = keepnulls;
  }

  public boolean isKeepNulls() {
    return keepnulls;
  }

  public void setKeepIdentity( boolean keepidentity ) {
    this.keepidentity = keepidentity;
  }

  public boolean isKeepIdentity() {
    return keepidentity;
  }

  public void setTablock( boolean tablock ) {
    this.tablock = tablock;
  }

  public boolean isTablock() {
    return tablock;
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
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notBlankValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks, ctx );

    JobEntryValidatorUtils.andValidator().validate( this, "tablename", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

}
