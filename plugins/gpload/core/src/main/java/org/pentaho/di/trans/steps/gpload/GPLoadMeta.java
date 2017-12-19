/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.di.trans.steps.gpload;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.annotations.Step;
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
 * GPLoad Bulk Loader Step Meta
 *
 * @author Matt Casters, Sean Flatley
 */
@Step( id = "GPLoad", image = "BLKGP.svg", i18nPackageName = "org.pentaho.di.trans.steps.gpload",
    name = "GPLoad.TypeLongDesc", description = "GPLoad.TypeLongDesc",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Bulk",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/Greenplum+Load" )
@InjectionSupported( localizationPrefix = "GPLoad.Injection.", groups = { "FIELDS", "LOCALHOSTS", "GP_CONFIG" } )
public class GPLoadMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = GPLoadMeta.class; // for i18n purposes, needed by Translator2!!

  /** Collection of Local hosts **/
  @Injection( name = "LOCALHOST_NAME", group = "LOCALHOSTS" )
  private String[] localHosts;

  /** LocalHostPort **/
  @Injection( name = "PORT", group = "LOCALHOSTS" )
  private String localhostPort;

  /** what's the schema for the target? */
  @Injection( name = "SCHEMA_NAME" )
  private String schemaName;

  /** what's the table for the target? */
  @Injection( name = "TABLE_NAME" )
  private String tableName;

  /** what's the target of the error table? */
  @Injection( name = "ERROR_TABLE", group = "GP_CONFIG" )
  private String errorTableName;

  /** Path to the gpload utility */
  @Injection( name = "GPLOAD_PATH", group = "GP_CONFIG" )
  private String gploadPath;

  /** Path to the control file */
  @Injection( name = "CONTROL_FILE", group = "GP_CONFIG" )
  private String controlFile;

  /** Path to the data file */
  @Injection( name = "DATA_FILE", group = "GP_CONFIG" )
  private String dataFile;

  /** Path to the log file */
  @Injection( name = "LOG_FILE", group = "GP_CONFIG" )
  private String logFile;

  /** NULL_AS parameter for gpload - gpload treats values matching this string as null */
  @Injection( name = "NULL_AS", group = "GP_CONFIG" )
  private String nullAs;

  /** database connection */
  private DatabaseMeta databaseMeta;

  /** Specified database field */
  @Injection( name = "FIELD_TABLE", group = "FIELDS" )
  private String[] fieldTable;

  /** Field name in the stream */
  @Injection( name = "FIELD_STREAM", group = "FIELDS" )
  private String[] fieldStream;

  /** Database column to match on for an update or merge operation */
  @Injection( name = "FIELD_MATCH", group = "FIELDS" )
  private boolean[] matchColumn;

  /** Database columns to update */
  @Injection( name = "FIELD_UPDATE", group = "FIELDS" )
  private boolean[] updateColumn;

  /** the date mask to use if the value is a date */
  @Injection( name = "FIELD_DATEMASK", group = "FIELDS" )
  private String[] dateMask;

  /** maximum errors */
  @Injection( name = "MAX_ERRORS", group = "GP_CONFIG" )
  private String maxErrors;

  /** Load method */
  @Injection( name = "LOAD_METHOD" )
  private String loadMethod;

  /** Load action */
  @Injection( name = "LOAD_ACTION", group = "FIELDS" )
  private String loadAction;

  /** Encoding to use */
  @Injection( name = "ENCODING", group = "GP_CONFIG" )
  private String encoding;

  /** Erase files after use */
  @Injection( name = "ERASE_FILE" )
  private boolean eraseFiles;

  /** Boolean to indicate that numbers are to be enclosed */
  @Injection( name = "ENCLOSURE_NUMBERS" )
  private boolean encloseNumbers;

  /** Data file delimiter */
  @Injection( name = "DELIMITER", group = "GP_CONFIG" )
  private String delimiter;

  /** Default number of maximum errors allowed on a load */
  public static String MAX_ERRORS_DEFAULT = "50";

  /** Update condition **/
  @Injection( name = "UPDATE_CONDITIONS", group = "FIELDS" )
  private String updateCondition;

  /*
   * Encodings supported by GPLoad. This list was obtained from the GPAAdminGuide.
   */
  public static final String[] SUPPORTED_ENCODINGS = { "", "BIG5", "EUC_CN", "EUC_JP", "EUC_KR", "EUC_TW", "GB18030",
    "GBK", "ISO-8859-1", "ISO_8859_5", "ISO_8859_6", "ISO_8859_7", "ISO_8859_8", "JOHAB", "KOI8", "LATIN1", "LATIN2",
    "LATIN3", "LATIN4", "LATIN5", "LATIN6", "LATIN7", "LATIN8", "LATIN9", "LATIN10", "MULE_INTERNAL", "SJIS",
    "SQL_ASCII", "UHC", "UTF8", "WIN866", "WIN874", "WIN1250", "WIN1251", "WIN1252", "WIN1253", "WIN1254", "WIN1255",
    "WIN1256", "WIN1257", "WIN1258" };

  /*
   * Do not translate following values!!! They are will end up in the job export.
   */
  public static final String ACTION_INSERT = "insert";
  public static final String ACTION_UPDATE = "update";
  public static final String ACTION_MERGE = "merge";

  /*
   * Do not translate following values!!! They are will end up in the job export.
   */
  // final static public String METHOD_AUTO_CONCURRENT = "AUTO_CONCURRENT";
  public static final String METHOD_AUTO_END = "AUTO_END";
  public static final String METHOD_MANUAL = "MANUAL";

  /*
   * Do not translate following values!!! They are will end up in the job export.
   */
  public static final String DATE_MASK_DATE = "DATE";
  public static final String DATE_MASK_DATETIME = "DATETIME";

  public GPLoadMeta() {
    super();
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

  /**
   * @return Returns the errorTableName.
   */
  public String getErrorTableName() {
    return errorTableName;
  }

  /**
   * @param errorTableName
   *          The error table name to set.
   */
  public void setErrorTableName( String errorTableName ) {
    this.errorTableName = errorTableName;
  }

  public String getGploadPath() {
    return gploadPath;
  }

  public void setGploadPath( String gploadPath ) {
    this.gploadPath = gploadPath;
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
  public void setFieldTable( String[] fieldTable ) {
    this.fieldTable = fieldTable;
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
  public void setFieldStream( String[] fieldStream ) {
    this.fieldStream = fieldStream;
  }

  public String[] getDateMask() {
    return dateMask;
  }

  public void setDateMask( String[] dateMask ) {
    this.dateMask = dateMask;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrvalues ) {
    fieldTable = new String[nrvalues];
    fieldStream = new String[nrvalues];
    dateMask = new String[nrvalues];
    matchColumn = new boolean[nrvalues];
    updateColumn = new boolean[nrvalues];

  }

  public void allocateLocalHosts( int numberOfLocalHosts ) {
    this.localHosts = new String[numberOfLocalHosts];
  }

  public Object clone() {
    GPLoadMeta retval = (GPLoadMeta) super.clone();
    int nrvalues = fieldTable.length;

    retval.allocate( nrvalues );

    for ( int i = 0; i < nrvalues; i++ ) {
      retval.fieldTable[i] = fieldTable[i];
      retval.fieldStream[i] = fieldStream[i];
      retval.dateMask[i] = dateMask[i];
      retval.matchColumn[i] = matchColumn[i];
      retval.updateColumn[i] = updateColumn[i];
    }
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      maxErrors = XMLHandler.getTagValue( stepnode, "errors" );
      schemaName = XMLHandler.getTagValue( stepnode, "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "table" );
      errorTableName = XMLHandler.getTagValue( stepnode, "error_table" );
      loadMethod = XMLHandler.getTagValue( stepnode, "load_method" );
      loadAction = XMLHandler.getTagValue( stepnode, "load_action" );
      gploadPath = XMLHandler.getTagValue( stepnode, "gpload_path" );
      controlFile = XMLHandler.getTagValue( stepnode, "control_file" );
      dataFile = XMLHandler.getTagValue( stepnode, "data_file" );
      delimiter = XMLHandler.getTagValue( stepnode, "delimiter" );
      logFile = XMLHandler.getTagValue( stepnode, "log_file" );
      nullAs = XMLHandler.getTagValue( stepnode, "null_as" );
      eraseFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "erase_files" ) );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      updateCondition = XMLHandler.getTagValue( stepnode, "update_condition" );

      Node localHostsNode = XMLHandler.getSubNode( stepnode, "local_hosts" );
      int nLocalHosts = XMLHandler.countNodes( localHostsNode, "local_host" );
      allocateLocalHosts( nLocalHosts );
      for ( int i = 0; i < nLocalHosts; i++ ) {
        Node localHostNode = XMLHandler.getSubNodeByNr( localHostsNode, "local_host", i );
        localHosts[i] = XMLHandler.getNodeValue( localHostNode );
      }
      localhostPort = XMLHandler.getTagValue( stepnode, "localhost_port" );

      encloseNumbers = XMLHandler.getTagValue( stepnode, "enclose_numbers" ).equalsIgnoreCase( "Y" );

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
          if ( GPLoadMeta.DATE_MASK_DATE.equals( locDateMask ) || GPLoadMeta.DATE_MASK_DATETIME.equals( locDateMask ) ) {
            dateMask[i] = locDateMask;
          } else {
            dateMask[i] = "";
          }
        }

        matchColumn[i] = ( "Y".equalsIgnoreCase( XMLHandler.getTagValue( vnode, "match_column" ) ) );
        updateColumn[i] = ( "Y".equalsIgnoreCase( XMLHandler.getTagValue( vnode, "update_column" ) ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "GPLoadMeta.Exception.UnableToReadStepInfoFromXML" ),
          e );
    }
  }

  public void setDefault() {

    // TODO: Make non empty defaults public static Strings

    fieldTable = null;
    databaseMeta = null;
    maxErrors = GPLoadMeta.MAX_ERRORS_DEFAULT;
    schemaName = "";
    localhostPort = "";
    tableName = BaseMessages.getString( PKG, "GPLoadMeta.DefaultTableName" );
    errorTableName = ""; // BaseMessages.getString(PKG, "GPLocal.ErrorTable.Prefix")+tableName;
    loadMethod = METHOD_AUTO_END;
    loadAction = ACTION_INSERT;
    gploadPath = "/usr/local/greenplum-db/bin/gpload";
    controlFile = "control${Internal.Step.CopyNr}.cfg";
    dataFile = "load${Internal.Step.CopyNr}.dat";
    logFile = "";
    nullAs = "";
    encoding = "";
    delimiter = ",";
    encloseNumbers = false;
    eraseFiles = true;
    updateCondition = "";

    allocate( 0 );
    allocateLocalHosts( 0 );
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 300 );

    retval.append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "errors", maxErrors ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "table", tableName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_table", errorTableName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "load_method", loadMethod ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "load_action", loadAction ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "gpload_path", gploadPath ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "control_file", controlFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "data_file", dataFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "delimiter", delimiter ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "log_file", logFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "null_as", nullAs ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "erase_files", eraseFiles ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "enclose_numbers", ( encloseNumbers ? "Y" : "N" ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "localhost_port", localhostPort ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "update_condition", updateCondition ) );

    for ( int i = 0; i < fieldTable.length; i++ ) {
      retval.append( "      <mapping>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "stream_name", fieldTable[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field_name", fieldStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "date_mask", dateMask[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "match_column", ( matchColumn[i] ? "Y" : "N" ) ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "update_column", ( updateColumn[i] ? "Y" : "N" ) ) );
      retval.append( "      </mapping>" ).append( Const.CR );
    }

    retval.append( "      <local_hosts>" ).append( Const.CR );
    for ( String localHost : localHosts ) {
      retval.append( "        " ).append( XMLHandler.addTagValue( "local_host", localHost ) );
    }
    retval.append( "      </local_hosts>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      maxErrors = rep.getStepAttributeString( id_step, "errors" );
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );
      errorTableName = rep.getStepAttributeString( id_step, "error_table" );
      loadMethod = rep.getStepAttributeString( id_step, "load_method" );
      loadAction = rep.getStepAttributeString( id_step, "load_action" );
      gploadPath = rep.getStepAttributeString( id_step, "gpload_path" );
      controlFile = rep.getStepAttributeString( id_step, "control_file" );
      dataFile = rep.getStepAttributeString( id_step, "data_file" );
      delimiter = rep.getStepAttributeString( id_step, "delimiter" );
      logFile = rep.getStepAttributeString( id_step, "log_file" );
      nullAs = rep.getStepAttributeString( id_step, "null_as" );
      eraseFiles = rep.getStepAttributeBoolean( id_step, "erase_files" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      localhostPort = rep.getStepAttributeString( id_step, "localhost_port" );
      encloseNumbers =
          ( rep.getStepAttributeString( id_step, "enclose_numbers" ).equalsIgnoreCase( "Y" ) ? true : false );
      updateCondition = rep.getStepAttributeString( id_step, "update_condition" );

      int numberOfLocalHosts = rep.countNrStepAttributes( id_step, "local_host" );
      allocateLocalHosts( numberOfLocalHosts );
      for ( int i = 0; i < numberOfLocalHosts; i++ ) {
        localHosts[i] = rep.getStepAttributeString( id_step, i, "local_host" );
      }

      int nrvalues = rep.countNrStepAttributes( id_step, "stream_name" );
      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        fieldTable[i] = rep.getStepAttributeString( id_step, i, "stream_name" );
        fieldStream[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        dateMask[i] = rep.getStepAttributeString( id_step, i, "date_mask" );
        matchColumn[i] = rep.getStepAttributeBoolean( id_step, i, "match_column" );
        updateColumn[i] = rep.getStepAttributeBoolean( id_step, i, "update_column" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "GPLoadMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "errors", maxErrors );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );
      rep.saveStepAttribute( id_transformation, id_step, "error_table", errorTableName );
      rep.saveStepAttribute( id_transformation, id_step, "load_method", loadMethod );
      rep.saveStepAttribute( id_transformation, id_step, "load_action", loadAction );
      rep.saveStepAttribute( id_transformation, id_step, "gpload_path", gploadPath );
      rep.saveStepAttribute( id_transformation, id_step, "control_file", controlFile );
      rep.saveStepAttribute( id_transformation, id_step, "data_file", dataFile );
      rep.saveStepAttribute( id_transformation, id_step, "delimiter", delimiter );
      rep.saveStepAttribute( id_transformation, id_step, "log_file", logFile );
      rep.saveStepAttribute( id_transformation, id_step, "null_as", nullAs );
      rep.saveStepAttribute( id_transformation, id_step, "erase_files", eraseFiles );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "enclose_numbers", ( encloseNumbers ? "Y" : "N" ) );
      rep.saveStepAttribute( id_transformation, id_step, "localhost_port", localhostPort );
      rep.saveStepAttribute( id_transformation, id_step, "update_condition", updateCondition );

      for ( int i = 0; i < localHosts.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "local_host", localHosts[i] );
      }

      for ( int i = 0; i < fieldTable.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "stream_name", fieldTable[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "date_mask", dateMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "match_column", matchColumn[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "update_column", updateColumn[i] );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GPLoadMeta.Exception.UnableToSaveStepInfoToRepository" )
          + id_step, e );
    }
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      db.shareVariablesWith( transMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( tableName ) ) {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "GPLoadMeta.CheckResult.TableNameOK" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          boolean error_found = false;
          error_message = "";

          // Check fields in table
          String schemaTable =
              databaseMeta.getQuotedSchemaTableCombination( transMeta.environmentSubstitute( schemaName ), transMeta
                  .environmentSubstitute( tableName ) );
          RowMetaInterface r = db.getTableFields( schemaTable );
          if ( r != null ) {
            cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                    "GPLoadMeta.CheckResult.TableExists" ), stepMeta );
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
                      BaseMessages.getString( PKG, "GPLoadMeta.CheckResult.MissingFieldsToLoadInTargetTable" )
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
                  new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                      "GPLoadMeta.CheckResult.AllFieldsFoundInTargetTable" ), stepMeta );
            }
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "GPLoadMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "GPLoadMeta.CheckResult.StepReceivingDatas", prev.size() + "" ), stepMeta );
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
                    BaseMessages.getString( PKG, "GPLoadMeta.CheckResult.MissingFieldsInInput" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + fieldStream[i] + Const.CR;
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                    "GPLoadMeta.CheckResult.AllFieldsFoundInInput" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message = BaseMessages.getString( PKG, "GPLoadMeta.CheckResult.MissingFieldsInInput3" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message = BaseMessages.getString( PKG, "GPLoadMeta.CheckResult.DatabaseErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "GPLoadMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "GPLoadMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "GPLoadMeta.CheckResult.NoInputError" ), stepMeta );
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
                databaseMeta.getQuotedSchemaTableCombination( transMeta.environmentSubstitute( schemaName ), transMeta
                    .environmentSubstitute( tableName ) );
            String sql = db.getDDL( schemaTable, tableFields, null, false, null, true );

            if ( sql.length() == 0 ) {
              retval.setSQL( null );
            } else {
              retval.setSQL( sql );
            }
          } catch ( KettleException e ) {
            retval.setError( BaseMessages.getString( PKG, "GPLoadMeta.GetSQL.ErrorOccurred" ) + e.getMessage() );
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "GPLoadMeta.GetSQL.NoTableDefinedOnConnection" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "GPLoadMeta.GetSQL.NotReceivingAnyFields" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "GPLoadMeta.GetSQL.NoConnectionDefined" ) );
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
            new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(),
                databaseMeta.getDatabaseName(), transMeta.environmentSubstitute( tableName ), fieldTable[i],
                fieldStream[i], v != null ? v.getOrigin() : "?", "", "Type = " + v.toStringMeta() );
        impact.add( ii );
      }
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new GPLoad( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new GPLoadData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
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
            throw new KettleException( BaseMessages.getString( PKG, "GPLoadMeta.Exception.TableNotFound" ) );
          }
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "GPLoadMeta.Exception.TableNotSpecified" ) );
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( PKG, "GPLoadMeta.Exception.ErrorGettingFields" ), e );
      } finally {
        db.disconnect();
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG, "GPLoadMeta.Exception.ConnectionNotDefined" ) );
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

  public String getLogFile() {
    return logFile;
  }

  public void setLogFile( String logFile ) {
    this.logFile = logFile;
  }

  public String getNullAs() {
    return nullAs;
  }

  public void setNullAs( String nullAs ) {
    this.nullAs = nullAs;
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

  public void setDelimiter( String delimiter ) {
    this.delimiter = delimiter;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public String getEnclosure() {
    return "";
  }

  public boolean isEraseFiles() {
    return eraseFiles;
  }

  public void setEraseFiles( boolean eraseFiles ) {
    this.eraseFiles = eraseFiles;
  }

  public String getMaxErrors() {
    return maxErrors;
  }

  public void setMaxErrors( String maxErrors ) {
    this.maxErrors = maxErrors;
  }

  public void setEncloseNumbers( boolean encloseNumbers ) {
    this.encloseNumbers = encloseNumbers;
  }

  public boolean getEncloseNumbers() {
    return this.encloseNumbers;
  }

  public void setLocalHosts( String[] localHosts ) {
    this.localHosts = localHosts;
  }

  public String[] getLocalHosts() {
    return localHosts;
  }

  public void setLocalhostPort( String localhostPort ) {
    this.localhostPort = localhostPort;
  }

  public String getLocalhostPort() {
    return localhostPort;
  }

  public void setMatchColumns( boolean[] matchColumn ) {
    this.matchColumn = matchColumn;
  }

  public boolean[] getMatchColumn() {
    return matchColumn;
  }

  public void setUpdateColumn( boolean[] updateColumn ) {
    this.updateColumn = updateColumn;
  }

  public boolean[] getUpdateColumn() {
    return updateColumn;
  }

  public boolean hasMatchColumn() {

    for ( boolean matchColumn : this.matchColumn ) {
      if ( matchColumn ) {
        return true;
      }
    }
    return false;
  }

  public boolean hasUpdateColumn() {

    for ( boolean updateColumn : this.updateColumn ) {
      if ( updateColumn ) {
        return true;
      }
    }

    return false;
  }

  public void setUpdateCondition( String updateCondition ) {
    this.updateCondition = updateCondition;
  }

  public String getUpdateCondition() {
    return updateCondition;
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( fieldTable == null ) ? -1 : fieldTable.length;
    if ( nrFields <= 0 ) {
      return;
    }
    String[][] rtnStringArray = Utils.normalizeArrays( nrFields, fieldStream, dateMask );
    fieldStream = rtnStringArray[ 0 ];
    dateMask = rtnStringArray[ 1 ];

    boolean[][] rtnBoolArray = Utils.normalizeArrays( nrFields, matchColumn, updateColumn );
    matchColumn = rtnBoolArray[ 0 ];
    updateColumn = rtnBoolArray[ 1 ];

  }
}
