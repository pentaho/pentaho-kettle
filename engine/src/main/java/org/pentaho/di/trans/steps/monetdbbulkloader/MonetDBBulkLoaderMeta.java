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

package org.pentaho.di.trans.steps.monetdbbulkloader;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleAttributeInterface;
import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MonetDBDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
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
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created on 20-feb-2007
 *
 * @author Sven Boden
 */
public class MonetDBBulkLoaderMeta extends BaseStepMeta implements StepMetaInjectionInterface, StepMetaInterface,
    ProvidesDatabaseConnectionInformation {
  private static Class<?> PKG = MonetDBBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  /**
   * The database connection name *
   */
  private String dbConnectionName;

  /**
   * what's the schema for the target?
   */
  private String schemaName;

  /**
   * what's the table for the target?
   */
  private String tableName;

  /**
   * Path to the log file
   */
  private String logFile;

  /**
   * database connection
   */
  private DatabaseMeta databaseMeta;

  /**
   * Field name of the target table
   */
  private String[] fieldTable;

  /**
   * Field name in the stream
   */
  private String[] fieldStream;

  /**
   * flag to indicate that the format is OK for MonetDB
   */
  private boolean[] fieldFormatOk;

  /**
   * Field separator character or string used to delimit fields
   */
  private String fieldSeparator;

  /**
   * Specifies which character surrounds each field's data. i.e. double quotes, single quotes or something else
   */
  private String fieldEnclosure;

  /**
   * How are NULLs represented as text to the MonetDB API or mclient i.e. can be an empty string or something else the
   * value is written out as text to the API and MonetDB is able to interpret it to the correct representation of NULL
   * in the database for the given column type.
   */
  private String NULLrepresentation;

  /**
   * Encoding to use
   */
  private String encoding;

  /**
   * Truncate table?
   */
  private boolean truncate = false;

  /**
   * Fully Quote SQL used in the step?
   */
  private boolean fullyQuoteSQL;

  /**
   * Auto adjust the table structure?
   */
  private boolean autoSchema = false;

  /**
   * Auto adjust strings that are too long?
   */
  private boolean autoStringWidths = false;

  public boolean isAutoStringWidths() {
    return autoStringWidths;
  }

  public void setAutoStringWidths( boolean autoStringWidths ) {
    this.autoStringWidths = autoStringWidths;
  }

  public boolean isTruncate() {
    return truncate;
  }

  public void setTruncate( boolean truncate ) {
    this.truncate = truncate;
  }

  public boolean isFullyQuoteSQL() {
    return fullyQuoteSQL;
  }

  public void setFullyQuoteSQL( boolean fullyQuoteSQLbool ) {
    this.fullyQuoteSQL = fullyQuoteSQLbool;
  }

  public boolean isAutoSchema() {
    return autoSchema;
  }

  public void setAutoSchema( boolean autoSchema ) {
    this.autoSchema = autoSchema;
  }

  /**
   * The number of rows to buffer before passing them over to MonetDB. This number should be non-zero since we need to
   * specify the number of rows we pass.
   */
  private String bufferSize;

  /**
   * The indicator defines that it is used the version of <i>MonetBD Jan2014-SP2</i> or later if it is <code>true</code>.
   * <code>False</code> indicates about using all MonetDb versions before this one.
   */
  private boolean compatibilityDbVersionMode = false;

  public MonetDBBulkLoaderMeta() {
    super();
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta( MonetDBBulkLoader loader ) {
    return databaseMeta;
  }

  /**
   * @param database The database to set.
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
   * @param tableName The tableName to set.
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
   * @param fieldTable The fieldTable to set.
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
   * @param fieldStream The fieldStream to set.
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
    fieldFormatOk = new boolean[nrvalues];
  }

  public Object clone() {
    MonetDBBulkLoaderMeta retval = (MonetDBBulkLoaderMeta) super.clone();
    int nrvalues = fieldTable.length;

    retval.allocate( nrvalues );

    System.arraycopy( fieldTable, 0, retval.fieldTable, 0, nrvalues );
    System.arraycopy( fieldStream, 0, retval.fieldStream, 0, nrvalues );
    System.arraycopy( fieldFormatOk, 0, retval.fieldFormatOk, 0, nrvalues );
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      dbConnectionName = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, dbConnectionName );

      schemaName = XMLHandler.getTagValue( stepnode, "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "table" );
      bufferSize = XMLHandler.getTagValue( stepnode, "buffer_size" );
      logFile = XMLHandler.getTagValue( stepnode, "log_file" );
      truncate = "Y".equals( XMLHandler.getTagValue( stepnode, "truncate" ) );

      // New in January 2013 Updates - For compatibility we set default values according to the old version of the step.
      //

      // This expression will only be true if a yes answer was previously recorded.
      fullyQuoteSQL = "Y".equals( XMLHandler.getTagValue( stepnode, "fully_quote_sql" ) );

      fieldSeparator = XMLHandler.getTagValue( stepnode, "field_separator" );
      if ( fieldSeparator == null ) {
        fieldSeparator = "|";
      }
      fieldEnclosure = XMLHandler.getTagValue( stepnode, "field_enclosure" );
      if ( fieldEnclosure == null ) {
        fieldEnclosure = "\"";
      }
      NULLrepresentation = XMLHandler.getTagValue( stepnode, "null_representation" );
      if ( NULLrepresentation == null ) {
        NULLrepresentation = "null";
      }
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      if ( encoding == null ) {
        encoding = "UTF-8";
      }

      // Old functionality. Always commented out. It may be safe to remove all of th
      // autoSchema = "Y".equals(XMLHandler.getTagValue(stepnode, "auto_schema"));
      // autoStringWidths = "Y".equals(XMLHandler.getTagValue(stepnode, "auto_string_widths"));

      int nrvalues = XMLHandler.countNodes( stepnode, "mapping" );
      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( stepnode, "mapping", i );

        fieldTable[i] = XMLHandler.getTagValue( vnode, "stream_name" );
        fieldStream[i] = XMLHandler.getTagValue( vnode, "field_name" );
        if ( fieldStream[i] == null ) {
          fieldStream[i] = fieldTable[i]; // default: the same name!
        }
        fieldFormatOk[i] = "Y".equalsIgnoreCase( XMLHandler.getTagValue( vnode, "field_format_ok" ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
          PKG, "MonetDBBulkLoaderMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    fieldTable = null;
    databaseMeta = null;
    schemaName = "";
    tableName = BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.DefaultTableName" );
    bufferSize = "100000";
    logFile = "";
    truncate = false;
    fullyQuoteSQL = true;

    // MonetDB safe defaults.
    fieldSeparator = "|";
    fieldEnclosure = "\"";
    NULLrepresentation = "";
    encoding = "UTF-8";
    allocate( 0 );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    // General Settings Tab
    retval.append( "    " ).append( XMLHandler.addTagValue( "connection", dbConnectionName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "buffer_size", bufferSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "table", tableName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "log_file", logFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "truncate", truncate ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "fully_quote_sql", fullyQuoteSQL ) );

    // MonetDB Settings Tab
    retval.append( "    " ).append( XMLHandler.addTagValue( "field_separator", fieldSeparator ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "field_enclosure", fieldEnclosure ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "null_representation", NULLrepresentation ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );

    // Output Fields Tab
    for ( int i = 0; i < fieldTable.length; i++ ) {
      retval.append( "      <mapping>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "stream_name", fieldTable[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field_name", fieldStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field_format_ok", fieldFormatOk[i] ) );
      retval.append( "      </mapping>" ).append( Const.CR );
    }

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      bufferSize = rep.getStepAttributeString( id_step, "buffer_size" );
      dbConnectionName = rep.getStepAttributeString( id_step, "db_connection_name" );
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );
      logFile = rep.getStepAttributeString( id_step, "log_file" );

      // The following default assignments are for backward compatibility with files saved under PDI version 4.4 files
      fieldSeparator = rep.getStepAttributeString( id_step, "field_separator" );
      if ( fieldSeparator == null ) {
        fieldEnclosure = "\"";
      }
      fieldEnclosure = rep.getStepAttributeString( id_step, "field_enclosure" );
      if ( fieldEnclosure == null ) {
        fieldEnclosure = "\"";
      }
      NULLrepresentation = rep.getStepAttributeString( id_step, "null_representation" );
      if ( NULLrepresentation == null ) {
        NULLrepresentation = "";
      }
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      if ( encoding == null ) {
        encoding = "UTF-8";
      }
      truncate = Boolean.parseBoolean( rep.getStepAttributeString( id_step, "truncate" ) );

      // This expression will only return true if a yes value was previously recorded; false otherwise.
      fullyQuoteSQL = Boolean.parseBoolean( rep.getStepAttributeString( id_step, "fully_quote_sql" ) );
      int nrvalues = rep.countNrStepAttributes( id_step, "stream_name" );

      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        fieldTable[i] = rep.getStepAttributeString( id_step, i, "stream_name" );
        fieldStream[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        if ( fieldStream[i] == null ) {
          fieldStream[i] = fieldTable[i];
        }
        fieldFormatOk[i] = rep.getStepAttributeBoolean( id_step, i, "field_format_ok" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
          PKG, "MonetDBBulkLoaderMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      // General Settings Tab
      rep.saveStepAttribute( id_transformation, id_step, "db_connection_name", dbConnectionName );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );
      rep.saveStepAttribute( id_transformation, id_step, "buffer_size", bufferSize );
      rep.saveStepAttribute( id_transformation, id_step, "log_file", logFile );
      rep.saveStepAttribute( id_transformation, id_step, "truncate", truncate );
      rep.saveStepAttribute( id_transformation, id_step, "fully_quote_sql", fullyQuoteSQL );

      // MonetDB Settings Tab
      rep.saveStepAttribute( id_transformation, id_step, "field_separator", fieldSeparator );
      rep.saveStepAttribute( id_transformation, id_step, "field_enclosure", fieldEnclosure );
      rep.saveStepAttribute( id_transformation, id_step, "null_representation", NULLrepresentation );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );

      // Output Fields Tab
      for ( int i = 0; i < fieldTable.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "stream_name", fieldTable[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format_ok", fieldFormatOk[i] );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
          PKG, "MonetDBBulkLoaderMeta.Exception.UnableToSaveStepInfoToRepository" )
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
                  PKG, "MonetDBBulkLoaderMeta.CheckResult.TableNameOK" ), stepMeta );
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
                    PKG, "MonetDBBulkLoaderMeta.CheckResult.TableExists" ), stepMeta );
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
                          PKG, "MonetDBBulkLoaderMeta.CheckResult.MissingFieldsToLoadInTargetTable" )
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
                      PKG, "MonetDBBulkLoaderMeta.CheckResult.AllFieldsFoundInTargetTable" ), stepMeta );
            }
            remarks.add( cr );
          } else {
            error_message =
                BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "MonetDBBulkLoaderMeta.CheckResult.StepReceivingDatas", prev.size() + "" ), stepMeta );
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
                    BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.CheckResult.MissingFieldsInInput" )
                        + Const.CR;
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
                    PKG, "MonetDBBulkLoaderMeta.CheckResult.AllFieldsFoundInInput" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message =
              BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.CheckResult.MissingFieldsInInput3" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message =
            BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.CheckResult.DatabaseErrorOccurred" )
                + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "MonetDBBulkLoaderMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "MonetDBBulkLoaderMeta.CheckResult.NoInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public SQLStatement getTableDdl( TransMeta transMeta, String stepname, boolean autoSchema,
      MonetDBBulkLoaderData data, boolean safeMode ) throws KettleException {

    String name = stepname; // new name might not yet be linked to other steps!
    StepMeta stepMeta =
        new StepMeta( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.StepMeta.Title" ), name, this );
    RowMetaInterface prev = transMeta.getPrevStepFields( stepname );

    SQLStatement sql = getSQLStatements( transMeta, stepMeta, prev, autoSchema, data, safeMode );
    return sql;
  }

  public RowMetaInterface updateFields( TransMeta transMeta, String stepname, MonetDBBulkLoaderData data ) throws KettleStepException {

    RowMetaInterface prev = transMeta.getPrevStepFields( stepname );
    return updateFields( prev, data );
  }

  public RowMetaInterface updateFields( RowMetaInterface prev, MonetDBBulkLoaderData data ) {
    // update the field table from the fields coming from the previous step
    RowMetaInterface tableFields = new RowMeta();
    List<ValueMetaInterface> fields = prev.getValueMetaList();
    fieldTable = new String[fields.size()];
    fieldStream = new String[fields.size()];
    fieldFormatOk = new boolean[fields.size()];
    int idx = 0;
    for ( ValueMetaInterface field : fields ) {
      ValueMetaInterface tableField = field.clone();
      tableFields.addValueMeta( tableField );
      fieldTable[idx] = field.getName();
      fieldStream[idx] = field.getName();
      fieldFormatOk[idx] = true;
      idx++;
    }

    data.keynrs = new int[getFieldStream().length];
    for ( int i = 0; i < data.keynrs.length; i++ ) {
      data.keynrs[i] = i;
    }
    return tableFields;
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      boolean autoSchema, MonetDBBulkLoaderData data, boolean safeMode ) throws KettleStepException {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        // Copy the row
        RowMetaInterface tableFields;

        if ( autoSchema ) {
          tableFields = updateFields( prev, data );
        } else {
          tableFields = new RowMeta();
          // Now change the field names
          for ( int i = 0; i < fieldTable.length; i++ ) {
            ValueMetaInterface v = prev.searchValueMeta( fieldStream[i] );
            if ( v != null ) {
              ValueMetaInterface tableField = v.clone();
              tableField.setName( fieldTable[i] );
              tableFields.addValueMeta( tableField );
            }
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
            MonetDBDatabaseMeta.safeModeLocal.set( safeMode );
            String cr_table = db.getDDL( schemaTable, tableFields, null, false, null, true );

            String sql = cr_table;
            if ( sql.length() == 0 ) {
              retval.setSQL( null );
            } else {
              retval.setSQL( sql );
            }
          } catch ( KettleException e ) {
            retval.setError( BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.GetSQL.ErrorOccurred" )
                + e.getMessage() );
          } finally {
            db.disconnect();
            MonetDBDatabaseMeta.safeModeLocal.remove();
          }
        } else {
          retval
              .setError( BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.GetSQL.NoTableDefinedOnConnection" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.GetSQL.NotReceivingAnyFields" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.GetSQL.NoConnectionDefined" ) );
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
    return new MonetDBBulkLoader( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new MonetDBBulkLoaderData();
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
            throw new KettleException( BaseMessages.getString(
                PKG, "MonetDBBulkLoaderMeta.Exception.TableNotFound" ) );
          }
        } else {
          throw new KettleException( BaseMessages.getString(
              PKG, "MonetDBBulkLoaderMeta.Exception.TableNotSpecified" ) );
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString(
            PKG, "MonetDBBulkLoaderMeta.Exception.ErrorGettingFields" ), e );
      } finally {
        db.disconnect();
      }
    } else {
      throw new KettleException( BaseMessages.getString(
          PKG, "MonetDBBulkLoaderMeta.Exception.ConnectionNotDefined" ) );
    }

  }

  /**
   * @return the schemaName
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @param schemaName the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public String getLogFile() {
    return logFile;
  }

  public void setLogFile( String logFile ) {
    this.logFile = logFile;
  }

  public String getFieldSeparator() {
    return fieldSeparator;
  }

  public void setFieldSeparator( String fieldSeparatorStr ) {
    this.fieldSeparator = fieldSeparatorStr;
  }

  public String getFieldEnclosure() {
    return fieldEnclosure;
  }

  public void setFieldEnclosure( String fieldEnclosureStr ) {
    this.fieldEnclosure = fieldEnclosureStr;
  }

  public String getNULLrepresentation() {
    return NULLrepresentation;
  }

  public void setNULLrepresentation( String NULLrepresentationStr ) {
    this.NULLrepresentation = NULLrepresentationStr;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @return the bufferSize
   */
  public String getBufferSize() {
    return bufferSize;
  }

  /**
   * @param bufferSize the bufferSize to set
   */
  public void setBufferSize( String bufferSize ) {
    this.bufferSize = bufferSize;
  }

  /**
   * @return the fieldFormatOk
   */
  public boolean[] getFieldFormatOk() {
    return fieldFormatOk;
  }

  /**
   * @param fieldFormatOk the fieldFormatOk to set
   */
  public void setFieldFormatOk( boolean[] fieldFormatOk ) {
    this.fieldFormatOk = fieldFormatOk;
  }

  @Override
  public String getMissingDatabaseConnectionInformationMessage() {
    // TODO
    return null;
  }

  /**
   * @param database connection name to set
   */
  public void setDbConnectionName( String dbConnectionName ) {
    this.dbConnectionName = dbConnectionName;
  }

  /**
   * @return the database connection name
   */
  public String getDbConnectionName() {
    return this.dbConnectionName;
  }

  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return this;
  }

  /**
   * Describe the metadata attributes that can be injected into this step metadata object.
   */
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() {
    return getStepInjectionMetadataEntries( PKG );
  }

  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> metadata ) {
    for ( StepInjectionMetaEntry entry : metadata ) {
      KettleAttributeInterface attr = findAttribute( entry.getKey() );

      // Set top level attributes...
      //
      if ( entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {

        if ( entry.getKey().equals( "SCHEMA" ) ) {
          schemaName = (String) entry.getValue();
        } else if ( entry.getKey().equals( "TABLE" ) ) {
          tableName = (String) entry.getValue();
        } else if ( entry.getKey().equals( "LOGFILE" ) ) {
          logFile = (String) entry.getValue();
        } else if ( entry.getKey().equals( "FIELD_SEPARATOR" ) ) {
          fieldSeparator = (String) entry.getValue();
        } else if ( entry.getKey().equals( "FIELD_ENCLOSURE" ) ) {
          fieldEnclosure = (String) entry.getValue();
        } else if ( entry.getKey().equals( "NULL_REPRESENTATION" ) ) {
          setNULLrepresentation( (String) entry.getValue() );
        } else if ( entry.getKey().equals( "ENCODING" ) ) {
          encoding = (String) entry.getValue();
        } else if ( entry.getKey().equals( "BUFFER_SIZE" ) ) {
          bufferSize = (String) entry.getValue();
        } else if ( entry.getKey().equals( "TRUNCATE" ) ) {
          truncate = (Boolean) entry.getValue();
        } else if ( entry.getKey().equals( "FULLY_QUOTE_SQL" ) ) {
          fullyQuoteSQL = (Boolean) entry.getValue();
        } else {
          throw new RuntimeException( "Unhandled metadata injection of attribute: "
              + attr.toString() + " - " + attr.getDescription() );
        }
      } else {
        // The data sets...
        //
        if ( attr.getKey().equals( "MAPPINGS" ) ) {
          List<StepInjectionMetaEntry> selectMappings = entry.getDetails();

          fieldTable = new String[selectMappings.size()];
          fieldStream = new String[selectMappings.size()];
          fieldFormatOk = new boolean[selectMappings.size()];

          for ( int row = 0; row < selectMappings.size(); row++ ) {
            StepInjectionMetaEntry selectField = selectMappings.get( row );

            List<StepInjectionMetaEntry> fieldAttributes = selectField.getDetails();
            //CHECKSTYLE:Indentation:OFF
            for ( int i = 0; i < fieldAttributes.size(); i++ ) {
              StepInjectionMetaEntry fieldAttribute = fieldAttributes.get( i );
              KettleAttributeInterface fieldAttr = findAttribute( fieldAttribute.getKey() );

              Object attributeValue = fieldAttribute.getValue();

              if ( attributeValue == null ) {
                continue;
              }

              if ( fieldAttr.getKey().equals( "STREAMNAME" ) ) {
                getFieldStream()[row] = (String) attributeValue;
              } else if ( fieldAttr.getKey().equals( "FIELDNAME" ) ) {
                getFieldTable()[row] = (String) attributeValue;
              } else if ( fieldAttr.getKey().equals( "FIELD_FORMAT_OK" ) ) {
                getFieldFormatOk()[row] = (Boolean) attributeValue;
              } else {
                throw new RuntimeException( "Unhandled metadata injection of attribute: "
                    + fieldAttr.toString() + " - " + fieldAttr.getDescription() );
              }
            }
          }
        }
        if ( !Utils.isEmpty( getFieldStream() ) ) {
          for ( int i = 0; i < getFieldStream().length; i++ ) {
            logDetailed( "row " + Integer.toString( i ) + ": stream=" + getFieldStream()[i]
                + " : table=" + getFieldTable()[i] );
          }
        }

      }
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  /**
   * Returns the version of MonetDB that is used.
   *
   * @return The version of MonetDB
   * @throws KettleException
   *           if an error occurs
   */
  private MonetDbVersion getMonetDBVersion() throws KettleException {
    Database db = null;

    db = new Database( loggingObject, databaseMeta );
    try {
      db.connect();
      return new MonetDbVersion( db.getDatabaseMetaData().getDatabaseProductVersion() );
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      if ( db != null ) {
        db.disconnect();
      }
    }
  }

  /**
   * Returns <code>true</code> if used the version of MonetBD Jan2014-SP2 or later, <code>false</code> otherwise.
   *
   * @return the compatibilityDbVersionMode
   */
  public boolean isCompatibilityDbVersionMode() {
    return compatibilityDbVersionMode;
  }

  /**
   * Defines and sets <code>true</code> if it is used the version of <i>MonetBD Jan2014-SP2</i> or later,
   * <code>false</code> otherwise. Sets also <code>false</code> if it's impossible to define which version of db is
   * used.
   */
  public void setCompatibilityDbVersionMode() {

    MonetDbVersion monetDBVersion;
    try {
      monetDBVersion = getMonetDBVersion();
      this.compatibilityDbVersionMode =
          monetDBVersion.compareTo( MonetDbVersion.JAN_2014_SP2_DB_VERSION ) < 0 ? false : true;
      if ( isDebug() && this.compatibilityDbVersionMode ) {
        logDebug( BaseMessages.getString( PKG, "MonetDBVersion.Info.UsingCompatibilityMode",
            MonetDbVersion.JAN_2014_SP2_DB_VERSION ) );
      }
    } catch ( KettleException e ) {
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "MonetDBBulkLoaderMeta.Exception.ErrorOnGettingDbVersion", e.getMessage() ) );
      }
    }
  }

}
