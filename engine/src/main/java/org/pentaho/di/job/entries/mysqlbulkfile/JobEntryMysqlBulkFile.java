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

package org.pentaho.di.job.entries.mysqlbulkfile;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
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
 * This defines an MYSQL Bulk file job entry.
 *
 * @author Samatar
 * @since 05-03-2006
 *
 */
public class JobEntryMysqlBulkFile extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryMysqlBulkFile.class; // for i18n purposes, needed by Translator2!!

  private String tablename;
  private String schemaname;
  private String filename;
  private String separator;
  private String enclosed;
  private String lineterminated;
  private String limitlines;
  private String listcolumn;
  private boolean highpriority;
  private boolean optionenclosed;
  public int outdumpvalue;
  public int iffileexists;
  private boolean addfiletoresult;

  private DatabaseMeta connection;

  public JobEntryMysqlBulkFile( String n ) {
    super( n, "" );
    tablename = null;
    schemaname = null;
    filename = null;
    separator = null;
    enclosed = null;
    limitlines = "0";
    listcolumn = null;
    lineterminated = null;
    highpriority = true;
    optionenclosed = false;
    iffileexists = 2;
    connection = null;
    addfiletoresult = false;
  }

  public JobEntryMysqlBulkFile() {
    this( "" );
  }

  public Object clone() {
    JobEntryMysqlBulkFile je = (JobEntryMysqlBulkFile) super.clone();
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
    retval.append( "      " ).append( XMLHandler.addTagValue( "optionenclosed", optionenclosed ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "lineterminated", lineterminated ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "limitlines", limitlines ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "listcolumn", listcolumn ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "highpriority", highpriority ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "outdumpvalue", outdumpvalue ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "iffileexists", iffileexists ) );
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
      lineterminated = XMLHandler.getTagValue( entrynode, "lineterminated" );
      limitlines = XMLHandler.getTagValue( entrynode, "limitlines" );
      listcolumn = XMLHandler.getTagValue( entrynode, "listcolumn" );
      highpriority = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "highpriority" ) );
      optionenclosed = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "optionenclosed" ) );
      outdumpvalue = Const.toInt( XMLHandler.getTagValue( entrynode, "outdumpvalue" ), -1 );
      iffileexists = Const.toInt( XMLHandler.getTagValue( entrynode, "iffileexists" ), -1 );
      String dbname = XMLHandler.getTagValue( entrynode, "connection" );
      connection = DatabaseMeta.findDatabase( databases, dbname );
      addfiletoresult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addfiletoresult" ) );
    } catch ( KettleException e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'table exists' from XML node", e );
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
      lineterminated = rep.getJobEntryAttributeString( id_jobentry, "lineterminated" );
      limitlines = rep.getJobEntryAttributeString( id_jobentry, "limitlines" );
      listcolumn = rep.getJobEntryAttributeString( id_jobentry, "listcolumn" );
      highpriority = rep.getJobEntryAttributeBoolean( id_jobentry, "highpriority" );
      optionenclosed = rep.getJobEntryAttributeBoolean( id_jobentry, "optionenclosed" );

      outdumpvalue = (int) rep.getJobEntryAttributeInteger( id_jobentry, "outdumpvalue" );

      iffileexists = (int) rep.getJobEntryAttributeInteger( id_jobentry, "iffileexists" );

      addfiletoresult = rep.getJobEntryAttributeBoolean( id_jobentry, "addfiletoresult" );

      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'table exists' from the repository for id_jobentry=" + id_jobentry,
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
      rep.saveJobEntryAttribute( id_job, getObjectId(), "lineterminated", lineterminated );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "limitlines", limitlines );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "listcolumn", listcolumn );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "highpriority", highpriority );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "optionenclosed", optionenclosed );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "outdumpvalue", outdumpvalue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "iffileexists", iffileexists );

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

  public String getTablename() {
    return tablename;
  }

  public String getSchemaname() {
    return schemaname;
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

    String LimitNbrLignes = "";
    String ListOfColumn = "*";
    String strHighPriority = "";
    String OutDumpText = "";
    String OptionEnclosed = "";
    String FieldSeparator = "";
    String LinesTerminated = "";

    Result result = previousResult;
    result.setResult( false );

    // Let's check the filename ...
    if ( filename != null ) {
      // User has specified a file, We can continue ...
      String realFilename = getRealFilename();
      File file = new File( realFilename );

      if ( file.exists() && iffileexists == 2 ) {
        // the file exists and user want to Fail
        result.setResult( false );
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobMysqlBulkFile.FileExists1.Label" )
          + realFilename + BaseMessages.getString( PKG, "JobMysqlBulkFile.FileExists2.Label" ) );

      } else if ( file.exists() && iffileexists == 1 ) {
        // the file exists and user want to do nothing
        result.setResult( true );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobMysqlBulkFile.FileExists1.Label" )
            + realFilename + BaseMessages.getString( PKG, "JobMysqlBulkFile.FileExists2.Label" ) );
        }

      } else {

        if ( file.exists() && iffileexists == 0 ) {
          // File exists and user want to renamme it with unique name

          // Format Date

          // Try to clean filename (without wildcard)
          String wildcard = realFilename.substring( realFilename.length() - 4, realFilename.length() );
          if ( wildcard.substring( 0, 1 ).equals( "." ) ) {
            // Find wildcard
            realFilename =
              realFilename.substring( 0, realFilename.length() - 4 )
                + "_" + StringUtil.getFormattedDateTimeNow( true ) + wildcard;
          } else {
            // did not find wildcard
            realFilename = realFilename + "_" + StringUtil.getFormattedDateTimeNow( true );
          }

          logDebug( BaseMessages.getString( PKG, "JobMysqlBulkFile.FileNameChange1.Label" )
            + realFilename + BaseMessages.getString( PKG, "JobMysqlBulkFile.FileNameChange1.Label" ) );

        }

        // User has specified an existing file, We can continue ...
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobMysqlBulkFile.FileExists1.Label" )
            + realFilename + BaseMessages.getString( PKG, "JobMysqlBulkFile.FileExists2.Label" ) );
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
                logDetailed( BaseMessages.getString( PKG, "JobMysqlBulkFile.TableExists1.Label" )
                  + realTablename + BaseMessages.getString( PKG, "JobMysqlBulkFile.TableExists2.Label" ) );
              }

              // Add schemaname (Most the time Schemaname.Tablename)
              if ( schemaname != null ) {
                realTablename = realSchemaname + "." + realTablename;
              }

              // Set the Limit lines
              if ( Const.toInt( getRealLimitlines(), 0 ) > 0 ) {
                LimitNbrLignes = "LIMIT " + getRealLimitlines();
              }

              // Set list of Column, if null get all columns (*)
              if ( getRealListColumn() != null ) {
                ListOfColumn = MysqlString( getRealListColumn() );
              }

              // Fields separator
              if ( getRealSeparator() != null && outdumpvalue == 0 ) {
                FieldSeparator = "FIELDS TERMINATED BY '" + Const.replace( getRealSeparator(), "'", "''" ) + "'";

              }

              // Lines Terminated by
              if ( getRealLineterminated() != null && outdumpvalue == 0 ) {
                LinesTerminated =
                  "LINES TERMINATED BY '" + Const.replace( getRealLineterminated(), "'", "''" ) + "'";

              }

              // High Priority ?
              if ( isHighPriority() ) {
                strHighPriority = "HIGH_PRIORITY";
              }

              if ( getRealEnclosed() != null && outdumpvalue == 0 ) {
                if ( isOptionEnclosed() ) {
                  OptionEnclosed = "OPTIONALLY ";
                }
                OptionEnclosed =
                  OptionEnclosed + "ENCLOSED BY '" + Const.replace( getRealEnclosed(), "'", "''" ) + "'";

              }

              // OutFile or Dumpfile
              if ( outdumpvalue == 0 ) {
                OutDumpText = "INTO OUTFILE";
              } else {
                OutDumpText = "INTO DUMPFILE";
              }

              String FILEBulkFile =
                "SELECT "
                  + strHighPriority + " " + ListOfColumn + " " + OutDumpText + " '" + realFilename + "' "
                  + FieldSeparator + " " + OptionEnclosed + " " + LinesTerminated + " FROM " + realTablename
                  + " " + LimitNbrLignes + " LOCK IN SHARE MODE";

              try {
                if ( log.isDetailed() ) {
                  logDetailed( FILEBulkFile );
                }
                // Run the SQL
                PreparedStatement ps = db.prepareSQL( FILEBulkFile );
                ps.execute();

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

              } catch ( SQLException je ) {
                db.disconnect();
                result.setNrErrors( 1 );
                logError( BaseMessages.getString( PKG, "JobMysqlBulkFile.Error.Label" ) + " " + je.getMessage() );
              } catch ( KettleFileException e ) {
                logError( BaseMessages.getString( PKG, "JobMysqlBulkFile.Error.Label" ) + e.getMessage() );
                result.setNrErrors( 1 );
              }

            } else {
              // Of course, the table should have been created already before the bulk load operation
              db.disconnect();
              result.setNrErrors( 1 );
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobMysqlBulkFile.TableNotExists1.Label" )
                  + realTablename + BaseMessages.getString( PKG, "JobMysqlBulkFile.TableNotExists2.Label" ) );
              }
            }

          } catch ( KettleDatabaseException dbe ) {
            db.disconnect();
            result.setNrErrors( 1 );
            logError( BaseMessages.getString( PKG, "JobMysqlBulkFile.Error.Label" ) + " " + dbe.getMessage() );
          }

        } else {
          // No database connection is defined
          result.setNrErrors( 1 );
          logError( BaseMessages.getString( PKG, "JobMysqlBulkFile.Nodatabase.Label" ) );
        }

      }

    } else {
      // No file was specified
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobMysqlBulkFile.Nofilename.Label" ) );
    }

    return result;

  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] { connection, };
  }

  public void setHighPriority( boolean highpriority ) {
    this.highpriority = highpriority;
  }

  public void setOptionEnclosed( boolean optionenclosed ) {
    this.optionenclosed = optionenclosed;
  }

  public boolean isHighPriority() {
    return highpriority;
  }

  public boolean isOptionEnclosed() {
    return optionenclosed;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public String getRealFilename() {
    String RealFile = environmentSubstitute( getFilename() );
    return RealFile.replace( '\\', '/' );
  }

  public void setSeparator( String separator ) {
    this.separator = separator;
  }

  public void setEnclosed( String enclosed ) {
    this.enclosed = enclosed;
  }

  public void setLineterminated( String lineterminated ) {
    this.lineterminated = lineterminated;
  }

  public String getLineterminated() {
    return lineterminated;
  }

  public String getRealLineterminated() {
    return environmentSubstitute( getLineterminated() );
  }

  public String getSeparator() {
    return separator;
  }

  public String getEnclosed() {
    return enclosed;
  }

  public String getRealSeparator() {
    return environmentSubstitute( getSeparator() );
  }

  public String getRealEnclosed() {
    return environmentSubstitute( getEnclosed() );
  }

  public void setLimitlines( String limitlines ) {
    this.limitlines = limitlines;
  }

  public String getLimitlines() {
    return limitlines;
  }

  public String getRealLimitlines() {
    return environmentSubstitute( getLimitlines() );
  }

  public void setListColumn( String listcolumn ) {
    this.listcolumn = listcolumn;
  }

  public String getListColumn() {
    return listcolumn;
  }

  public String getRealListColumn() {
    return environmentSubstitute( getListColumn() );
  }

  public void setAddFileToResult( boolean addfiletoresultin ) {
    this.addfiletoresult = addfiletoresultin;
  }

  public boolean isAddFileToResult() {
    return addfiletoresult;
  }

  private String MysqlString( String listcolumns ) {
    /*
     * handle forbiden char like '
     */
    String ReturnString = "";
    String[] split = listcolumns.split( "," );

    for ( int i = 0; i < split.length; i++ ) {
      if ( ReturnString.equals( "" ) ) {
        ReturnString = "`" + Const.trim( split[i] ) + "`";
      } else {
        ReturnString = ReturnString + ", `" + Const.trim( split[i] ) + "`";
      }

    }

    return ReturnString;

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
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "tablename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

}
