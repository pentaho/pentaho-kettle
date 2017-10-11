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

package org.pentaho.di.trans.steps.mysqlbulkloader;

import java.util.List;

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
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.injection.InjectionTypeConverter;
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
 * Here are the steps that we need to take to make streaming loading possible for MySQL:<br>
 * <br>
 * The following steps are carried out by the step at runtime:<br>
 * <br>
 * - create a unique FIFO file (using mkfifo, LINUX ONLY FOLKS!)<br>
 * - Create a target table using standard Kettle SQL generation<br>
 * - Execute the LOAD DATA SQL Command to bulk load in a separate SQL thread in the background:<br>
 * - Write to the FIFO file<br>
 * - At the end, close the output stream to the FIFO file<br>
 * * At the end, remove the FIFO file <br>
 *
 *
 * Created on 24-oct-2007<br>
 *
 * @author Matt Casters<br>
 */
@InjectionSupported( localizationPrefix = "MySQLBulkLoader.Injection.", groups = { "FIELDS" } )
public class MySQLBulkLoaderMeta extends BaseStepMeta implements StepMetaInterface,
    ProvidesDatabaseConnectionInformation {
  private static Class<?> PKG = MySQLBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int FIELD_FORMAT_TYPE_OK = 0;
  public static final int FIELD_FORMAT_TYPE_DATE = 1;
  public static final int FIELD_FORMAT_TYPE_TIMESTAMP = 2;
  public static final int FIELD_FORMAT_TYPE_NUMBER = 3;
  public static final int FIELD_FORMAT_TYPE_STRING_ESCAPE = 4;

  private static final String[] fieldFormatTypeCodes = { "OK", "DATE", "TIMESTAMP", "NUMBER", "STRING_ESC" };
  private static final String[] fieldFormatTypeDescriptions = {
    BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.FieldFormatType.OK.Description" ),
    BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.FieldFormatType.Date.Description" ),
    BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.FieldFormatType.Timestamp.Description" ),
    BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.FieldFormatType.Number.Description" ),
    BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.FieldFormatType.StringEscape.Description" ), };

  /** what's the schema for the target? */
  @Injection( name = "SCHEMA_NAME" )
  private String schemaName;

  /** what's the table for the target? */
  @Injection( name = "TABLE_NAME" )
  private String tableName;

  /** The name of the FIFO file to create */
  @Injection( name = "FIFO_FILE" )
  private String fifoFileName;

  /** database connection */
  private DatabaseMeta databaseMeta;

  /** Field name of the target table */
  @Injection( name = "FIELD_TABLE", group = "FIELDS" )
  private String[] fieldTable;

  /** Field name in the stream */
  @Injection( name = "FIELD_STREAM", group = "FIELDS" )
  private String[] fieldStream;

  /** flag to indicate what to do with the formatting */
  @Injection( name = "FIELD_FORMAT", group = "FIELDS", converter = FieldFormatTypeConverter.class )
  private int[] fieldFormatType;

  /** Encoding to use */
  @Injection( name = "ENCODING" )
  private String encoding;

  /** REPLACE clause flag */
  @Injection( name = "USE_REPLACE_CLAUSE" )
  private boolean replacingData;

  /** IGNORE clause flag */
  @Injection( name = "USE_IGNORE_CLAUSE" )
  private boolean ignoringErrors;

  /** allows specification of the LOCAL clause */
  @Injection( name = "LOCAL_FILE" )
  private boolean localFile;

  /** The delimiter to use */
  @Injection( name = "DELIMITER" )
  private String delimiter;

  /** The enclosure to use */
  @Injection( name = "ENCLOSURE" )
  private String enclosure;

  /** The escape character */
  @Injection( name = "ESCAPE_CHAR" )
  private String escapeChar;

  /** The number of rows to load per bulk statement */
  @Injection( name = "BULK_SIZE" )
  private String bulkSize;

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

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrvalues ) {
    fieldTable = new String[nrvalues];
    fieldStream = new String[nrvalues];
    fieldFormatType = new int[nrvalues];
  }

  public Object clone() {
    MySQLBulkLoaderMeta retval = (MySQLBulkLoaderMeta) super.clone();
    int nrvalues = fieldTable.length;

    retval.allocate( nrvalues );
    System.arraycopy( fieldTable, 0, retval.fieldTable, 0, nrvalues );
    System.arraycopy( fieldStream, 0, retval.fieldStream, 0, nrvalues );
    System.arraycopy( fieldFormatType, 0, retval.fieldFormatType, 0, nrvalues );

    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );

      schemaName = XMLHandler.getTagValue( stepnode, "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "table" );

      fifoFileName = XMLHandler.getTagValue( stepnode, "fifo_file_name" );

      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      enclosure = XMLHandler.getTagValue( stepnode, "enclosure" );
      delimiter = XMLHandler.getTagValue( stepnode, "delimiter" );
      escapeChar = XMLHandler.getTagValue( stepnode, "escape_char" );

      bulkSize = XMLHandler.getTagValue( stepnode, "bulk_size" );

      replacingData = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "replace" ) );
      ignoringErrors = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "ignore" ) );
      localFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "local" ) );

      int nrvalues = XMLHandler.countNodes( stepnode, "mapping" );
      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( stepnode, "mapping", i );

        fieldTable[i] = XMLHandler.getTagValue( vnode, "stream_name" );
        fieldStream[i] = XMLHandler.getTagValue( vnode, "field_name" );
        if ( fieldStream[i] == null ) {
          fieldStream[i] = fieldTable[i]; // default: the same name!
        }
        fieldFormatType[i] = getFieldFormatType( XMLHandler.getTagValue( vnode, "field_format_ok" ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG,
          "MySQLBulkLoaderMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    fieldTable = null;
    databaseMeta = null;
    schemaName = "";
    tableName = BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.DefaultTableName" );
    encoding = "";
    fifoFileName = "/tmp/fifo";
    delimiter = "\t";
    enclosure = "\"";
    escapeChar = "\\";
    replacingData = false;
    ignoringErrors = false;
    localFile = true;
    bulkSize = null;

    allocate( 0 );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "table", tableName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "delimiter", delimiter ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "enclosure", enclosure ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "escape_char", escapeChar ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "replace", replacingData ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "ignore", ignoringErrors ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "local", localFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "fifo_file_name", fifoFileName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "bulk_size", bulkSize ) );

    for ( int i = 0; i < fieldTable.length; i++ ) {
      retval.append( "      <mapping>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "stream_name", fieldTable[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field_name", fieldStream[i] ) );
      retval.append( "        " ).append(
          XMLHandler.addTagValue( "field_format_ok", getFieldFormatTypeCode( fieldFormatType[i] ) ) );
      retval.append( "      </mapping>" ).append( Const.CR );
    }

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      enclosure = rep.getStepAttributeString( id_step, "enclosure" );
      delimiter = rep.getStepAttributeString( id_step, "delimiter" );
      escapeChar = rep.getStepAttributeString( id_step, "escape_char" );
      fifoFileName = rep.getStepAttributeString( id_step, "fifo_file_name" );
      replacingData = rep.getStepAttributeBoolean( id_step, "replace" );
      ignoringErrors = rep.getStepAttributeBoolean( id_step, "ignore" );
      localFile = rep.getStepAttributeBoolean( id_step, "local" );
      bulkSize = rep.getStepAttributeString( id_step, "bulk_size" );

      int nrvalues = rep.countNrStepAttributes( id_step, "stream_name" );

      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        fieldTable[i] = rep.getStepAttributeString( id_step, i, "stream_name" );
        fieldStream[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        if ( fieldStream[i] == null ) {
          fieldStream[i] = fieldTable[i];
        }
        fieldFormatType[i] = getFieldFormatType( rep.getStepAttributeString( id_step, i, "field_format_ok" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "MySQLBulkLoaderMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "enclosure", enclosure );
      rep.saveStepAttribute( id_transformation, id_step, "delimiter", delimiter );
      rep.saveStepAttribute( id_transformation, id_step, "escape_char", escapeChar );
      rep.saveStepAttribute( id_transformation, id_step, "fifo_file_name", fifoFileName );
      rep.saveStepAttribute( id_transformation, id_step, "replace", replacingData );
      rep.saveStepAttribute( id_transformation, id_step, "ignore", ignoringErrors );
      rep.saveStepAttribute( id_transformation, id_step, "local", localFile );
      rep.saveStepAttribute( id_transformation, id_step, "bulk_size", bulkSize );

      for ( int i = 0; i < fieldTable.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "stream_name", fieldTable[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format_ok",
            getFieldFormatTypeCode( fieldFormatType[i] ) );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "MySQLBulkLoaderMeta.Exception.UnableToSaveStepInfoToRepository" )
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
                  "MySQLBulkLoaderMeta.CheckResult.TableNameOK" ), stepMeta );
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
                    "MySQLBulkLoaderMeta.CheckResult.TableExists" ), stepMeta );
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
                      BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.CheckResult.MissingFieldsToLoadInTargetTable" )
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
                      "MySQLBulkLoaderMeta.CheckResult.AllFieldsFoundInTargetTable" ), stepMeta );
            }
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "MySQLBulkLoaderMeta.CheckResult.StepReceivingDatas", prev.size() + "" ), stepMeta );
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
                    BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.CheckResult.MissingFieldsInInput" ) + Const.CR;
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
                    "MySQLBulkLoaderMeta.CheckResult.AllFieldsFoundInInput" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message =
              BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.CheckResult.MissingFieldsInInput3" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message =
            BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.CheckResult.DatabaseErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "MySQLBulkLoaderMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "MySQLBulkLoaderMeta.CheckResult.NoInputError" ), stepMeta );
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
            String cr_table = db.getDDL( schemaTable, tableFields, null, false, null, true );

            String sql = cr_table;
            if ( sql.length() == 0 ) {
              retval.setSQL( null );
            } else {
              retval.setSQL( sql );
            }
          } catch ( KettleException e ) {
            retval
                .setError( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.GetSQL.ErrorOccurred" ) + e.getMessage() );
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.GetSQL.NoTableDefinedOnConnection" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.GetSQL.NotReceivingAnyFields" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.GetSQL.NoConnectionDefined" ) );
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
    return new MySQLBulkLoader( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new MySQLBulkLoaderData();
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
            throw new KettleException( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.Exception.TableNotFound" ) );
          }
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.Exception.TableNotSpecified" ) );
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.Exception.ErrorGettingFields" ), e );
      } finally {
        db.disconnect();
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG, "MySQLBulkLoaderMeta.Exception.ConnectionNotDefined" ) );
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

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter( String delimiter ) {
    this.delimiter = delimiter;
  }

  public String getEnclosure() {
    return enclosure;
  }

  public void setEnclosure( String enclosure ) {
    this.enclosure = enclosure;
  }

  /**
   * @return the fifoFileName
   */
  public String getFifoFileName() {
    return fifoFileName;
  }

  /**
   * @param fifoFileName
   *          the fifoFileName to set
   */
  public void setFifoFileName( String fifoFileName ) {
    this.fifoFileName = fifoFileName;
  }

  /**
   * @return the replacingData
   */
  public boolean isReplacingData() {
    return replacingData;
  }

  /**
   * @param replacingData
   *          the replacingData to set
   */
  public void setReplacingData( boolean replacingData ) {
    this.replacingData = replacingData;
  }

  public int[] getFieldFormatType() {
    return fieldFormatType;
  }

  public void setFieldFormatType( int[] fieldFormatType ) {
    this.fieldFormatType = fieldFormatType;
  }

  public static String[] getFieldFormatTypeCodes() {
    return fieldFormatTypeCodes;
  }

  public static String[] getFieldFormatTypeDescriptions() {
    return fieldFormatTypeDescriptions;
  }

  public static String getFieldFormatTypeCode( int type ) {
    return fieldFormatTypeCodes[type];
  }

  public static String getFieldFormatTypeDescription( int type ) {
    return fieldFormatTypeDescriptions[type];
  }

  public static int getFieldFormatType( String codeOrDescription ) {
    for ( int i = 0; i < fieldFormatTypeCodes.length; i++ ) {
      if ( fieldFormatTypeCodes[i].equalsIgnoreCase( codeOrDescription ) ) {
        return i;
      }
    }
    for ( int i = 0; i < fieldFormatTypeDescriptions.length; i++ ) {
      if ( fieldFormatTypeDescriptions[i].equalsIgnoreCase( codeOrDescription ) ) {
        return i;
      }
    }
    return FIELD_FORMAT_TYPE_OK;
  }

  /**
   * @return the escapeChar
   */
  public String getEscapeChar() {
    return escapeChar;
  }

  /**
   * @param escapeChar
   *          the escapeChar to set
   */
  public void setEscapeChar( String escapeChar ) {
    this.escapeChar = escapeChar;
  }

  /**
   * @return the ignoringErrors
   */
  public boolean isIgnoringErrors() {
    return ignoringErrors;
  }

  /**
   * @param ignoringErrors
   *          the ignoringErrors to set
   */
  public void setIgnoringErrors( boolean ignoringErrors ) {
    this.ignoringErrors = ignoringErrors;
  }

  /**
   * @return the bulkSize
   */
  public String getBulkSize() {
    return bulkSize;
  }

  /**
   * @param bulkSize
   *          the bulkSize to set
   */
  public void setBulkSize( String bulkSize ) {
    this.bulkSize = bulkSize;
  }

  /**
   * @return the localFile
   */
  public boolean isLocalFile() {
    return localFile;
  }

  /**
   * @param localFile
   *          the localFile to set
   */
  public void setLocalFile( boolean localFile ) {
    this.localFile = localFile;
  }

  @Override
  public String getMissingDatabaseConnectionInformationMessage() {
    // TODO Auto-generated method stub
    return null;
  }

  public static class FieldFormatTypeConverter extends InjectionTypeConverter {
    @Override
    public int string2intPrimitive( String v ) throws KettleValueException {
      for ( int i = 0; i < fieldFormatTypeCodes.length; i++ ) {
        if ( fieldFormatTypeCodes[i].equalsIgnoreCase( v ) ) {
          return i;
        }
      }
      return FIELD_FORMAT_TYPE_OK;
    }
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
    String[][] rtnStrings = Utils.normalizeArrays( nrFields, fieldStream );
    fieldStream = rtnStrings[ 0 ];

    int[][] rtnInts = Utils.normalizeArrays( nrFields, fieldFormatType );
    fieldFormatType = rtnInts[ 0 ];
  }

}
