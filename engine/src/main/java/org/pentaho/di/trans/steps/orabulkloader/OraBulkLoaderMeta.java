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

package org.pentaho.di.trans.steps.orabulkloader;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Created on 20-feb-2007
 *
 * @author Sven Boden
 */
@InjectionSupported( localizationPrefix = "OraBulkLoader.Injection.", groups = { "FIELDS", "DATABASE_FIELDS" } )
public class OraBulkLoaderMeta extends BaseStepMeta implements StepMetaInterface,
  ProvidesDatabaseConnectionInformation {
  private static Class<?> PKG = OraBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  private static int DEFAULT_COMMIT_SIZE = 100000; // The bigger the better for Oracle
  private static int DEFAULT_BIND_SIZE = 0;
  private static int DEFAULT_READ_SIZE = 0;
  private static int DEFAULT_MAX_ERRORS = 50;

  /** database connection */
  private DatabaseMeta databaseMeta;
  private List<? extends SharedObjectInterface> databases;

  /** what's the schema for the target? */
  @Injection( name = "SCHEMA_NAME", group = "FIELDS" )
  private String schemaName;

  /** what's the table for the target? */
  @Injection( name = "TABLE_NAME", group = "FIELDS" )
  private String tableName;

  /** Path to the sqlldr utility */
  @Injection( name = "SQLLDR_PATH", group = "FIELDS" )
  private String sqlldr;

  /** Path to the control file */
  @Injection( name = "CONTROL_FILE", group = "FIELDS" )
  private String controlFile;

  /** Path to the data file */
  @Injection( name = "DATA_FILE", group = "FIELDS" )
  private String dataFile;

  /** Path to the log file */
  @Injection( name = "LOG_FILE", group = "FIELDS" )
  private String logFile;

  /** Path to the bad file */
  @Injection( name = "BAD_FILE", group = "FIELDS" )
  private String badFile;

  /** Path to the discard file */
  @Injection( name = "DISCARD_FILE", group = "FIELDS" )
  private String discardFile;

  /** Field value to dateMask after lookup */
  @Injection( name = "FIELD_TABLE", group = "DATABASE_FIELDS" )
  private String[] fieldTable;

  /** Field name in the stream */
  @Injection( name = "FIELD_STREAM", group = "DATABASE_FIELDS" )
  private String[] fieldStream;

  /** boolean indicating if field needs to be updated */
  @Injection( name = "FIELD_DATEMASK", group = "DATABASE_FIELDS" )
  private String[] dateMask;

  /** Commit size (ROWS) */
  @Injection( name = "COMMIT_SIZE", group = "FIELDS" )
  private String commitSize;

  /** bindsize */
  @Injection( name = "BIND_SIZE", group = "FIELDS" )
  private String bindSize;

  /** readsize */
  @Injection( name = "READ_SIZE", group = "FIELDS" )
  private String readSize;

  /** maximum errors */
  @Injection( name = "MAX_ERRORS", group = "FIELDS" )
  private String maxErrors;

  /** Load method */
  @Injection( name = "LOAD_METHOD", group = "FIELDS" )
  private String loadMethod;

  /** Load action */
  @Injection( name = "LOAD_ACTION", group = "FIELDS" )
  private String loadAction;

  /** Encoding to use */
  @Injection( name = "ENCODING", group = "FIELDS" )
  private String encoding;

  /** Character set name used for Oracle */
  @Injection( name = "ORACLE_CHARSET_NAME", group = "FIELDS" )
  private String characterSetName;

  /** Direct Path? */
  @Injection( name = "DIRECT_PATH", group = "FIELDS" )
  private boolean directPath;

  /** Erase files after use */
  @Injection( name = "ERASE_FILES", group = "FIELDS" )
  private boolean eraseFiles;

  /** Database name override */
  @Injection( name = "DB_NAME_OVERRIDE", group = "FIELDS" )
  private String dbNameOverride;

  /** Fails when sqlldr returns a warning **/
  @Injection( name = "FAIL_ON_WARNING", group = "FIELDS" )
  private boolean failOnWarning;

  /** Fails when sqlldr returns anything else than a warning or OK **/
  @Injection( name = "FAIL_ON_ERROR", group = "FIELDS" )
  private boolean failOnError;

  /** allow Oracle to load data in parallel **/
  @Injection( name = "PARALLEL", group = "FIELDS" )
  private boolean parallel;

  /** If not empty, use this record terminator instead of default one **/
  @Injection( name = "RECORD_TERMINATOR", group = "FIELDS" )
  private String altRecordTerm;

  @Injection( name = "CONNECTION_NAME" )
  public void setConnection( String connectionName ) {
    databaseMeta = DatabaseMeta.findDatabase( databases, connectionName );
  }

  /*
   * Do not translate following values!!! They are will end up in the job export.
   */
  public static final String ACTION_APPEND = "APPEND";
  public static final String ACTION_INSERT = "INSERT";
  public static final String ACTION_REPLACE = "REPLACE";
  public static final String ACTION_TRUNCATE = "TRUNCATE";

  /*
   * Do not translate following values!!! They are will end up in the job export.
   */
  public static final String METHOD_AUTO_CONCURRENT = "AUTO_CONCURRENT";
  public static final String METHOD_AUTO_END = "AUTO_END";
  public static final String METHOD_MANUAL = "MANUAL";

  /*
   * Do not translate following values!!! They are will end up in the job export.
   */
  public static final String DATE_MASK_DATE = "DATE";
  public static final String DATE_MASK_DATETIME = "DATETIME";

  public OraBulkLoaderMeta() {
    super();
  }

  public int getCommitSizeAsInt( VariableSpace varSpace ) {
    try {
      return Integer.valueOf( varSpace.environmentSubstitute( getCommitSize() ) );
    } catch ( NumberFormatException ex ) {
      return DEFAULT_COMMIT_SIZE;
    }
  }

  /**
   * @return Returns the commitSize.
   */
  public String getCommitSize() {
    return commitSize;
  }

  /**
   * @param commitSize
   *          The commitSize to set.
   */
  public void setCommitSize( String commitSize ) {
    this.commitSize = commitSize;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database
   *          The database to set.
   */
  public void setDatabaseMeta( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  /**
   * @return Returns the tableName.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName
   *          The tableName to set.
   */
  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  public String getSqlldr() {
    return sqlldr;
  }

  public void setSqlldr( String sqlldr ) {
    this.sqlldr = sqlldr;
  }

  /**
   * @return Returns the fieldTable.
   */
  public String[] getFieldTable() {
    return fieldTable;
  }

  /**
   * @param fieldTable
   *          The fieldTable to set.
   */
  public void setFieldTable( String[] updateLookup ) {
    this.fieldTable = updateLookup;
  }

  /**
   * @return Returns the fieldStream.
   */
  public String[] getFieldStream() {
    return fieldStream;
  }

  /**
   * @param fieldStream
   *          The fieldStream to set.
   */
  public void setFieldStream( String[] updateStream ) {
    this.fieldStream = updateStream;
  }

  public String[] getDateMask() {
    return dateMask;
  }

  public void setDateMask( String[] dateMask ) {
    this.dateMask = dateMask;
  }

  public boolean isFailOnWarning() {
    return failOnWarning;
  }

  public void setFailOnWarning( boolean failOnWarning ) {
    this.failOnWarning = failOnWarning;
  }

  public boolean isFailOnError() {
    return failOnError;
  }

  public void setFailOnError( boolean failOnError ) {
    this.failOnError = failOnError;
  }

  public String getCharacterSetName() {
    return characterSetName;
  }

  public void setCharacterSetName( String characterSetName ) {
    this.characterSetName = characterSetName;
  }

  public String getAltRecordTerm() {
    return altRecordTerm;
  }

  public void setAltRecordTerm( String altRecordTerm ) {
    this.altRecordTerm = altRecordTerm;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrvalues ) {
    fieldTable = new String[nrvalues];
    fieldStream = new String[nrvalues];
    dateMask = new String[nrvalues];
  }

  public Object clone() {
    OraBulkLoaderMeta retval = (OraBulkLoaderMeta) super.clone();
    int nrvalues = fieldTable.length;

    retval.allocate( nrvalues );
    System.arraycopy( fieldTable, 0, retval.fieldTable, 0, nrvalues );
    System.arraycopy( fieldStream, 0, retval.fieldStream, 0, nrvalues );
    System.arraycopy( dateMask, 0, retval.dateMask, 0, nrvalues );
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      // String csize, bsize, rsize, serror;
      // int nrvalues;
      this.databases = databases;
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );

      commitSize = XMLHandler.getTagValue( stepnode, "commit" );
      if ( Utils.isEmpty( commitSize ) ) {
        commitSize = Integer.toString( DEFAULT_COMMIT_SIZE );
      }

      bindSize = XMLHandler.getTagValue( stepnode, "bind_size" );
      if ( Utils.isEmpty( bindSize ) ) {
        bindSize = Integer.toString( DEFAULT_BIND_SIZE );
      }

      readSize = XMLHandler.getTagValue( stepnode, "read_size" );
      if ( Utils.isEmpty( readSize ) ) {
        readSize = Integer.toString( DEFAULT_READ_SIZE );
      }

      maxErrors = XMLHandler.getTagValue( stepnode, "errors" );
      if ( Utils.isEmpty( maxErrors ) ) {
        maxErrors = Integer.toString( DEFAULT_MAX_ERRORS );
      }

      schemaName = XMLHandler.getTagValue( stepnode, "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "table" );

      loadMethod = XMLHandler.getTagValue( stepnode, "load_method" );
      loadAction = XMLHandler.getTagValue( stepnode, "load_action" );
      sqlldr = XMLHandler.getTagValue( stepnode, "sqlldr" );
      controlFile = XMLHandler.getTagValue( stepnode, "control_file" );
      dataFile = XMLHandler.getTagValue( stepnode, "data_file" );
      logFile = XMLHandler.getTagValue( stepnode, "log_file" );
      badFile = XMLHandler.getTagValue( stepnode, "bad_file" );
      discardFile = XMLHandler.getTagValue( stepnode, "discard_file" );
      directPath = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "direct_path" ) );
      eraseFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "erase_files" ) );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      dbNameOverride = XMLHandler.getTagValue( stepnode, "dbname_override" );

      characterSetName = XMLHandler.getTagValue( stepnode, "character_set" );
      failOnWarning = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "fail_on_warning" ) );
      failOnError = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "fail_on_error" ) );
      parallel = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "parallel" ) );
      altRecordTerm = XMLHandler.getTagValue( stepnode, "alt_rec_term" );

      int nrvalues = XMLHandler.countNodes( stepnode, "mapping" );
      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( stepnode, "mapping", i );

        fieldTable[i] = XMLHandler.getTagValue( vnode, "stream_name" );
        fieldStream[i] = XMLHandler.getTagValue( vnode, "field_name" );
        if ( fieldStream[i] == null ) {
          fieldStream[i] = fieldTable[i]; // default: the same name!
        }
        String locDateMask = XMLHandler.getTagValue( vnode, "date_mask" );
        if ( locDateMask == null ) {
          dateMask[i] = "";
        } else {
          if ( OraBulkLoaderMeta.DATE_MASK_DATE.equals( locDateMask )
            || OraBulkLoaderMeta.DATE_MASK_DATETIME.equals( locDateMask ) ) {
            dateMask[i] = locDateMask;
          } else {
            dateMask[i] = "";
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "OraBulkLoaderMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    fieldTable = null;
    databaseMeta = null;
    commitSize = Integer.toString( DEFAULT_COMMIT_SIZE );
    bindSize = Integer.toString( DEFAULT_BIND_SIZE ); // Use platform default
    readSize = Integer.toString( DEFAULT_READ_SIZE ); // Use platform default
    maxErrors = Integer.toString( DEFAULT_MAX_ERRORS );
    schemaName = "";
    tableName = BaseMessages.getString( PKG, "OraBulkLoaderMeta.DefaultTableName" );
    loadMethod = METHOD_AUTO_END;
    loadAction = ACTION_APPEND;
    sqlldr = "sqlldr";
    controlFile = "control${Internal.Step.CopyNr}.cfg";
    dataFile = "load${Internal.Step.CopyNr}.dat";
    logFile = "";
    badFile = "";
    discardFile = "";
    encoding = "";
    dbNameOverride = "";

    directPath = false;
    eraseFiles = true;

    characterSetName = "";
    failOnWarning = false;
    failOnError = false;
    parallel = false;
    altRecordTerm = "";

    int nrvalues = 0;
    allocate( nrvalues );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval
      .append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "commit", commitSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "bind_size", bindSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "read_size", readSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "errors", maxErrors ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "table", tableName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "load_method", loadMethod ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "load_action", loadAction ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sqlldr", sqlldr ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "control_file", controlFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "data_file", dataFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "log_file", logFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "bad_file", badFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "discard_file", discardFile ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "direct_path", directPath ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "erase_files", eraseFiles ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dbname_override", dbNameOverride ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "character_set", characterSetName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "fail_on_warning", failOnWarning ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "fail_on_error", failOnError ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "parallel", parallel ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "alt_rec_term", altRecordTerm ) );

    for ( int i = 0; i < fieldTable.length; i++ ) {
      retval.append( "      <mapping>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "stream_name", fieldTable[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field_name", fieldStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "date_mask", dateMask[i] ) );
      retval.append( "      </mapping>" ).append( Const.CR );
    }

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      this.databases = databases;
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      commitSize = rep.getStepAttributeString( id_step, "commit" );
      bindSize = rep.getStepAttributeString( id_step, "bind_size" );
      readSize = rep.getStepAttributeString( id_step, "read_size" );
      maxErrors = rep.getStepAttributeString( id_step, "errors" );
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );
      loadMethod = rep.getStepAttributeString( id_step, "load_method" );
      loadAction = rep.getStepAttributeString( id_step, "load_action" );
      sqlldr = rep.getStepAttributeString( id_step, "sqlldr" );
      controlFile = rep.getStepAttributeString( id_step, "control_file" );
      dataFile = rep.getStepAttributeString( id_step, "data_file" );
      logFile = rep.getStepAttributeString( id_step, "log_file" );
      badFile = rep.getStepAttributeString( id_step, "bad_file" );
      discardFile = rep.getStepAttributeString( id_step, "discard_file" );

      directPath = rep.getStepAttributeBoolean( id_step, "direct_path" );
      eraseFiles = rep.getStepAttributeBoolean( id_step, "erase_files" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      dbNameOverride = rep.getStepAttributeString( id_step, "dbname_override" );

      characterSetName = rep.getStepAttributeString( id_step, "character_set" );
      failOnWarning = rep.getStepAttributeBoolean( id_step, "fail_on_warning" );
      failOnError = rep.getStepAttributeBoolean( id_step, "fail_on_error" );
      parallel = rep.getStepAttributeBoolean( id_step, "parallel" );
      altRecordTerm = rep.getStepAttributeString( id_step, "alt_rec_term" );

      int nrvalues = rep.countNrStepAttributes( id_step, "stream_name" );

      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        fieldTable[i] = rep.getStepAttributeString( id_step, i, "stream_name" );
        fieldStream[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        dateMask[i] = rep.getStepAttributeString( id_step, i, "date_mask" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "OraBulkLoaderMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "commit", commitSize );
      rep.saveStepAttribute( id_transformation, id_step, "bind_size", bindSize );
      rep.saveStepAttribute( id_transformation, id_step, "read_size", readSize );
      rep.saveStepAttribute( id_transformation, id_step, "errors", maxErrors );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );

      rep.saveStepAttribute( id_transformation, id_step, "load_method", loadMethod );
      rep.saveStepAttribute( id_transformation, id_step, "load_action", loadAction );
      rep.saveStepAttribute( id_transformation, id_step, "sqlldr", sqlldr );
      rep.saveStepAttribute( id_transformation, id_step, "control_file", controlFile );
      rep.saveStepAttribute( id_transformation, id_step, "data_file", dataFile );
      rep.saveStepAttribute( id_transformation, id_step, "log_file", logFile );
      rep.saveStepAttribute( id_transformation, id_step, "bad_file", badFile );
      rep.saveStepAttribute( id_transformation, id_step, "discard_file", discardFile );

      rep.saveStepAttribute( id_transformation, id_step, "direct_path", directPath );
      rep.saveStepAttribute( id_transformation, id_step, "erase_files", eraseFiles );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "dbname_override", dbNameOverride );

      rep.saveStepAttribute( id_transformation, id_step, "character_set", characterSetName );
      rep.saveStepAttribute( id_transformation, id_step, "fail_on_warning", failOnWarning );
      rep.saveStepAttribute( id_transformation, id_step, "fail_on_error", failOnError );
      rep.saveStepAttribute( id_transformation, id_step, "parallel", parallel );
      rep.saveStepAttribute( id_transformation, id_step, "alt_rec_term", altRecordTerm );

      for ( int i = 0; i < fieldTable.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "stream_name", fieldTable[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "date_mask", dateMask[i] );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "OraBulkLoaderMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      db.shareVariablesWith( transMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( tableName ) ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "OraBulkLoaderMeta.CheckResult.TableNameOK" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          boolean error_found = false;
          error_message = "";

          // Check fields in table
          String schemaTable =
            databaseMeta.getQuotedSchemaTableCombination(
              transMeta.environmentSubstitute( schemaName ), transMeta.environmentSubstitute( tableName ) );
          RowMetaInterface r = db.getTableFields( schemaTable );
          if ( r != null ) {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "OraBulkLoaderMeta.CheckResult.TableExists" ), stepMeta );
            remarks.add( cr );

            // How about the fields to insert/dateMask in the table?
            first = true;
            error_found = false;
            error_message = "";

            for ( int i = 0; i < fieldTable.length; i++ ) {
              String field = fieldTable[i];

              ValueMetaInterface v = r.searchValueMeta( field );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString(
                      PKG, "OraBulkLoaderMeta.CheckResult.MissingFieldsToLoadInTargetTable" )
                      + Const.CR;
                }
                error_found = true;
                error_message += "\t\t" + field + Const.CR;
              }
            }
            if ( error_found ) {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            } else {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "OraBulkLoaderMeta.CheckResult.AllFieldsFoundInTargetTable" ), stepMeta );
            }
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "OraBulkLoaderMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "OraBulkLoaderMeta.CheckResult.StepReceivingDatas", prev.size() + "" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          error_message = "";
          boolean error_found = false;

          for ( int i = 0; i < fieldStream.length; i++ ) {
            ValueMetaInterface v = prev.searchValueMeta( fieldStream[i] );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "OraBulkLoaderMeta.CheckResult.MissingFieldsInInput" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + fieldStream[i] + Const.CR;
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "OraBulkLoaderMeta.CheckResult.AllFieldsFoundInInput" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message =
            BaseMessages.getString( PKG, "OraBulkLoaderMeta.CheckResult.MissingFieldsInInput3" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message =
          BaseMessages.getString( PKG, "OraBulkLoaderMeta.CheckResult.DatabaseErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "OraBulkLoaderMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "OraBulkLoaderMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "OraBulkLoaderMeta.CheckResult.NoInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
    Repository repository, IMetaStore metaStore ) throws KettleStepException {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        // Copy the row
        RowMetaInterface tableFields = new RowMeta();

        // Now change the field names
        for ( int i = 0; i < fieldTable.length; i++ ) {
          ValueMetaInterface v = prev.searchValueMeta( fieldStream[i] );
          if ( v != null ) {
            ValueMetaInterface tableField = v.clone();
            tableField.setName( fieldTable[i] );
            tableFields.addValueMeta( tableField );
          } else {
            throw new KettleStepException( "Unable to find field [" + fieldStream[i] + "] in the input rows" );
          }
        }

        if ( !Utils.isEmpty( tableName ) ) {
          Database db = new Database( loggingObject, databaseMeta );
          db.shareVariablesWith( transMeta );
          try {
            db.connect();

            String schemaTable =
              databaseMeta.getQuotedSchemaTableCombination(
                transMeta.environmentSubstitute( schemaName ), transMeta.environmentSubstitute( tableName ) );
            String sql = db.getDDL( schemaTable, tableFields, null, false, null, true );

            if ( sql.length() == 0 ) {
              retval.setSQL( null );
            } else {
              retval.setSQL( sql );
            }
          } catch ( KettleException e ) {
            retval.setError( BaseMessages.getString( PKG, "OraBulkLoaderMeta.GetSQL.ErrorOccurred" )
              + e.getMessage() );
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "OraBulkLoaderMeta.GetSQL.NoTableDefinedOnConnection" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "OraBulkLoaderMeta.GetSQL.NotReceivingAnyFields" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "OraBulkLoaderMeta.GetSQL.NoConnectionDefined" ) );
    }

    return retval;
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) throws KettleStepException {
    if ( prev != null ) {
      /* DEBUG CHECK THIS */
      // Insert dateMask fields : read/write
      for ( int i = 0; i < fieldTable.length; i++ ) {
        ValueMetaInterface v = prev.searchValueMeta( fieldStream[i] );

        DatabaseImpact ii =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
              .getDatabaseName(), transMeta.environmentSubstitute( tableName ), fieldTable[i],
            fieldStream[i], v != null ? v.getOrigin() : "?", "", "Type = " + v.toStringMeta() );
        impact.add( ii );
      }
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new OraBulkLoader( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new OraBulkLoaderData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return Do we want direct path loading.
   */
  public boolean isDirectPath() {
    return directPath;
  }

  /**
   * @param directPath
   *          do we want direct path
   */
  public void setDirectPath( boolean directPath ) {
    this.directPath = directPath;
  }

  public RowMetaInterface getRequiredFields( VariableSpace space ) throws KettleException {
    String realTableName = space.environmentSubstitute( tableName );
    String realSchemaName = space.environmentSubstitute( schemaName );

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( realTableName ) ) {
          String schemaTable = databaseMeta.getQuotedSchemaTableCombination( realSchemaName, realTableName );

          // Check if this table exists...
          if ( db.checkTableExists( schemaTable ) ) {
            return db.getTableFields( schemaTable );
          } else {
            throw new KettleException( BaseMessages.getString( PKG, "OraBulkLoaderMeta.Exception.TableNotFound" ) );
          }
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "OraBulkLoaderMeta.Exception.TableNotSpecified" ) );
        }
      } catch ( Exception e ) {
        throw new KettleException(
          BaseMessages.getString( PKG, "OraBulkLoaderMeta.Exception.ErrorGettingFields" ), e );
      } finally {
        db.disconnect();
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG, "OraBulkLoaderMeta.Exception.ConnectionNotDefined" ) );
    }

  }

  /**
   * @return the schemaName
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @param schemaName
   *          the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public String getBadFile() {
    return badFile;
  }

  public void setBadFile( String badFile ) {
    this.badFile = badFile;
  }

  public String getControlFile() {
    return controlFile;
  }

  public void setControlFile( String controlFile ) {
    this.controlFile = controlFile;
  }

  public String getDataFile() {
    return dataFile;
  }

  public void setDataFile( String dataFile ) {
    this.dataFile = dataFile;
  }

  public String getDiscardFile() {
    return discardFile;
  }

  public void setDiscardFile( String discardFile ) {
    this.discardFile = discardFile;
  }

  public String getLogFile() {
    return logFile;
  }

  public void setLogFile( String logFile ) {
    this.logFile = logFile;
  }

  public void setLoadAction( String action ) {
    this.loadAction = action;
  }

  public String getLoadAction() {
    return this.loadAction;
  }

  public void setLoadMethod( String method ) {
    this.loadMethod = method;
  }

  public String getLoadMethod() {
    return this.loadMethod;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  public String getDelimiter() {
    return ",";
  }

  public String getEnclosure() {
    return "\"";
  }

  public boolean isEraseFiles() {
    return eraseFiles;
  }

  public void setEraseFiles( boolean eraseFiles ) {
    this.eraseFiles = eraseFiles;
  }

  public int getBindSizeAsInt( VariableSpace varSpace ) {
    try {
      return Integer.valueOf( varSpace.environmentSubstitute( getBindSize() ) );
    } catch ( NumberFormatException ex ) {
      return DEFAULT_BIND_SIZE;
    }
  }

  public String getBindSize() {
    return bindSize;
  }

  public void setBindSize( String bindSize ) {
    this.bindSize = bindSize;
  }

  public int getMaxErrorsAsInt( VariableSpace varSpace ) {
    try {
      return Integer.valueOf( varSpace.environmentSubstitute( getMaxErrors() ) );
    } catch ( NumberFormatException ex ) {
      return DEFAULT_MAX_ERRORS;
    }
  }

  public String getMaxErrors() {
    return maxErrors;
  }

  public void setMaxErrors( String maxErrors ) {
    this.maxErrors = maxErrors;
  }

  public int getReadSizeAsInt( VariableSpace varSpace ) {
    try {
      return Integer.valueOf( varSpace.environmentSubstitute( getReadSize() ) );
    } catch ( NumberFormatException ex ) {
      return DEFAULT_READ_SIZE;
    }
  }

  public String getReadSize() {
    return readSize;
  }

  public void setReadSize( String readSize ) {
    this.readSize = readSize;
  }

  public String getDbNameOverride() {
    return dbNameOverride;
  }

  public void setDbNameOverride( String dbNameOverride ) {
    this.dbNameOverride = dbNameOverride;
  }

  /**
   * @return the parallel
   */
  public boolean isParallel() {
    return parallel;
  }

  /**
   * @param parallel
   *          the parallel to set
   */
  public void setParallel( boolean parallel ) {
    this.parallel = parallel;
  }

  @Override
  public String getMissingDatabaseConnectionInformationMessage() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    if ( fieldTable == null || fieldTable.length == 0 ) {
      return;
    }
    int nrFields = fieldTable.length;
    if ( fieldStream.length < nrFields ) {
      String[] newFieldStream = new String[ nrFields ];
      System.arraycopy( fieldStream, 0, newFieldStream, 0, fieldStream.length );
      fieldStream = newFieldStream;
    }
    for ( int i = 0; i < fieldStream.length; i++ ) {
      if ( fieldStream[ i ] == null ) {
        fieldStream[ i ] = StringUtils.EMPTY;
      }
    }
    //PDI-16472
    if ( dateMask.length < nrFields ) {
      String[] newDateMask = new String[ nrFields ];
      System.arraycopy( dateMask, 0, newDateMask, 0, dateMask.length );
      dateMask = newDateMask;
    }
  }
}
