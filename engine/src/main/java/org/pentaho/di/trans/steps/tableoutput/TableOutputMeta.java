/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.tableoutput;

import com.google.common.base.Preconditions;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ProvidesModelerMeta;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Table Output meta data.
 *
 * @author Matt Casters
 * @since 2-jun-2003
 */
@InjectionSupported( localizationPrefix = "TableOutputMeta.Injection.", groups = { "DATABASE_FIELDS" } )
public class TableOutputMeta extends BaseDatabaseStepMeta implements StepMetaInterface, ProvidesModelerMeta {
  private static Class<?> PKG = TableOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private DatabaseMeta databaseMeta;

  @Injection( name = "TARGET_SCHEMA" )
  private String schemaName;

  @Injection( name = "TARGET_TABLE" )
  private String tableName;

  @Injection( name = "COMMIT_SIZE" )
  private String commitSize;

  @Injection( name = "TRUNCATE_TABLE" )
  public void metaSetTruncateTable( String value ) {
    setTruncateTable( "Y".equalsIgnoreCase( value ) );
  }
  private boolean truncateTable;

  /**
   * Do we explicitly select the fields to update in the database
   */
  @Injection( name = "SPECIFY_DATABASE_FIELDS" )
  public void metaSetSpecifyDatabaseFields( String value ) {
    setSpecifyFields( "Y".equalsIgnoreCase( value ) );
  }
  private boolean specifyFields;

  @Injection( name = "IGNORE_INSERT_ERRORS" )
  public void metaSetIgnoreInsertErrors( String value ) {
    setIgnoreErrors( "Y".equalsIgnoreCase( value ) );
  }
  private boolean ignoreErrors;

  @Injection( name = "USE_BATCH_UPDATE" )
  public void metaSetUseBatchUpdate( String value ) {
    setUseBatchUpdate( "Y".equalsIgnoreCase( value ) );
  }
  private boolean useBatchUpdate;


  @Injection( name = "PARTITION_OVER_TABLES" )
  public void metaSetPartitionOverTables( String value ) {
    setPartitioningEnabled( "Y".equalsIgnoreCase( value ) );
  }
  private boolean partitioningEnabled;

  @Injection( name = "PARTITIONING_FIELD" )
  private String partitioningField;

  @Injection( name = "PARTITION_DATA_PER" )
  private void metaSetPartitionPer( String value ) {
    setPartitioningDaily( "DAY".equalsIgnoreCase( value ) );
    setPartitioningMonthly( "MONTH".equalsIgnoreCase( value ) );
  }
  private boolean partitioningDaily;
  private boolean partitioningMonthly;

  @Injection( name = "TABLE_NAME_DEFINED_IN_FIELD" )
  public void metaSetTableNameDefinedInField( String value ) {
    setTableNameInField( "Y".equalsIgnoreCase( value ) );
  }
  private boolean tableNameInField;

  @Injection( name = "TABLE_NAME_FIELD" )
  private String tableNameField;

  private boolean tableNameInTable;

  @Injection( name = "RETURN_AUTO_GENERATED_KEY" )
  public void metaSetReturningGeneratedKeys( String value ) {
    setReturningGeneratedKeys( "Y".equalsIgnoreCase( value ) );
  }
  private boolean returningGeneratedKeys;

  @Injection( name = "AUTO_GENERATED_KEY_FIELD" )
  private String generatedKeyField;

  // This follows the naming convention on TableInputMeta see
  // @See https://github.com/pentaho/pentaho-kettle/blob/285c5962c8bc73c5bec30933fe0b8fee00426043/engine/src/main/java/org/pentaho/di/trans/steps/tableinput/TableInputMeta.java#L100
  @Injection( name = "CONNECTIONNAME" )
  public void metaSetConnection( String connectionName ) throws KettleException {
    setConnection( connectionName );
  }

  @Injection( name = "DATABASE_FIELD_NAME", group = "DATABASE_FIELDS" )
  private String[] fieldDatabase;

  /**
   * Fields in the table to insert
   */
  @Injection( name = "DATABASE_STREAM_NAME", group = "DATABASE_FIELDS" )
  private String[] fieldStream;


  /**
   * @return Returns the generatedKeyField.
   */
  public String getGeneratedKeyField() {
    return generatedKeyField;
  }

  /**
   * @param generatedKeyField The generatedKeyField to set.
   */
  public void setGeneratedKeyField( String generatedKeyField ) {
    this.generatedKeyField = generatedKeyField;
  }

  /**
   * @return Returns the returningGeneratedKeys.
   */
  public boolean isReturningGeneratedKeys() {
    if ( getDatabaseMeta() != null ) {
      return getDatabaseMeta().supportsAutoGeneratedKeys() && returningGeneratedKeys;
    }
    return false;
  }

  /**
   * @param returningGeneratedKeys The returningGeneratedKeys to set.
   */
  public void setReturningGeneratedKeys( boolean returningGeneratedKeys ) {
    this.returningGeneratedKeys = returningGeneratedKeys;
  }

  /**
   * @return Returns the tableNameInTable.
   */
  public boolean isTableNameInTable() {
    return tableNameInTable;
  }

  /**
   * @param tableNameInTable The tableNameInTable to set.
   */
  public void setTableNameInTable( boolean tableNameInTable ) {
    this.tableNameInTable = tableNameInTable;
  }

  /**
   * @return Returns the tableNameField.
   */
  public String getTableNameField() {
    return tableNameField;
  }

  /**
   * @param tableNameField The tableNameField to set.
   */
  public void setTableNameField( String tableNameField ) {
    this.tableNameField = tableNameField;
  }

  /**
   * @return Returns the tableNameInField.
   */
  public boolean isTableNameInField() {
    return tableNameInField;
  }

  /**
   * @param tableNameInField The tableNameInField to set.
   */
  public void setTableNameInField( boolean tableNameInField ) {
    this.tableNameInField = tableNameInField;
  }

  /**
   * @return Returns the partitioningDaily.
   */
  public boolean isPartitioningDaily() {
    return partitioningDaily;
  }

  /**
   * @param partitioningDaily The partitioningDaily to set.
   */
  public void setPartitioningDaily( boolean partitioningDaily ) {
    this.partitioningDaily = partitioningDaily;
  }

  /**
   * @return Returns the partitioningMontly.
   */
  public boolean isPartitioningMonthly() {
    return partitioningMonthly;
  }

  /**
   * @param partitioningMontly The partitioningMontly to set.
   */
  public void setPartitioningMonthly( boolean partitioningMontly ) {
    this.partitioningMonthly = partitioningMontly;
  }

  /**
   * @return Returns the partitioningEnabled.
   */
  public boolean isPartitioningEnabled() {
    return partitioningEnabled;
  }

  /**
   * @param partitioningEnabled The partitioningEnabled to set.
   */
  public void setPartitioningEnabled( boolean partitioningEnabled ) {
    this.partitioningEnabled = partitioningEnabled;
  }

  /**
   * @return Returns the partitioningField.
   */
  public String getPartitioningField() {
    return partitioningField;
  }

  /**
   * @param partitioningField The partitioningField to set.
   */
  public void setPartitioningField( String partitioningField ) {
    this.partitioningField = partitioningField;
  }

  public TableOutputMeta() {
    super(); // allocate BaseStepMeta
    useBatchUpdate = true;
    commitSize = "1000";

    fieldStream = new String[ 0 ];
    fieldDatabase = new String[ 0 ];
  }

  public void allocate( int nrRows ) {
    fieldStream = new String[ nrRows ];
    fieldDatabase = new String[ nrRows ];
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    Preconditions.checkState( fieldStream.length == fieldDatabase.length,
      "Table fields and stream fields are not of equal length." );
    TableOutputMeta retval = (TableOutputMeta) super.clone();
    int nrRows = fieldStream.length;
    retval.allocate( nrRows );
    System.arraycopy( fieldStream, 0, retval.fieldStream, 0, nrRows );
    System.arraycopy( fieldDatabase, 0, retval.fieldDatabase, 0, nrRows );

    return retval;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database The database to set.
   */
  public void setDatabaseMeta( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  /**
   * @return Returns the commitSize.
   */
  public String getCommitSize() {
    return commitSize;
  }

  /**
   * @param commitSizeInt The commitSize to set.
   */
  public void setCommitSize( int commitSizeInt ) {
    this.commitSize = Integer.toString( commitSizeInt );
  }

  /**
   * @param commitSize The commitSize to set.
   */
  public void setCommitSize( String commitSize ) {
    this.commitSize = commitSize;
  }

  /**
   * @return the table name
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Assign the table name to write to.
   *
   * @param tableName The table name to set
   */
  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  /**
   * @return Returns the tablename.
   * @deprecated Use {@link #getTableName()}
   */
  @Deprecated
  public String getTablename() {
    return getTableName();
  }

  /**
   * @param tablename The tablename to set.
   * @deprecated Use {@link #setTableName(String)}
   */
  @Deprecated
  public void setTablename( String tablename ) {
    setTableName( tablename );
  }

  /**
   * @return Returns the truncate table flag.
   */
  public boolean truncateTable() {
    return truncateTable;
  }

  /**
   * @param truncateTable The truncate table flag to set.
   */
  public void setTruncateTable( boolean truncateTable ) {
    this.truncateTable = truncateTable;
  }

  /**
   * @param ignoreErrors The ignore errors flag to set.
   */
  public void setIgnoreErrors( boolean ignoreErrors ) {
    this.ignoreErrors = ignoreErrors;
  }

  /**
   * @return Returns the ignore errors flag.
   */
  public boolean ignoreErrors() {
    return ignoreErrors;
  }

  /**
   * @param specifyFields The specify fields flag to set.
   */
  public void setSpecifyFields( boolean specifyFields ) {
    this.specifyFields = specifyFields;
  }

  /**
   * @return Returns the specify fields flag.
   */
  public boolean specifyFields() {
    return specifyFields;
  }

  /**
   * @param useBatchUpdate The useBatchUpdate flag to set.
   */
  public void setUseBatchUpdate( boolean useBatchUpdate ) {
    this.useBatchUpdate = useBatchUpdate;
  }

  /**
   * @return Returns the useBatchUpdate flag.
   */
  public boolean useBatchUpdate() {
    return useBatchUpdate;
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      schemaName = XMLHandler.getTagValue( stepnode, "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "table" );
      commitSize = XMLHandler.getTagValue( stepnode, "commit" );
      truncateTable = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "truncate" ) );
      ignoreErrors = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "ignore_errors" ) );
      useBatchUpdate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_batch" ) );

      // If not present it will be false to be compatible with pre-v3.2
      specifyFields = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "specify_fields" ) );

      partitioningEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "partitioning_enabled" ) );
      partitioningField = XMLHandler.getTagValue( stepnode, "partitioning_field" );
      partitioningDaily = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "partitioning_daily" ) );
      partitioningMonthly = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "partitioning_monthly" ) );

      tableNameInField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "tablename_in_field" ) );
      tableNameField = XMLHandler.getTagValue( stepnode, "tablename_field" );
      tableNameInTable = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "tablename_in_table" ) );

      returningGeneratedKeys = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "return_keys" ) );
      generatedKeyField = XMLHandler.getTagValue( stepnode, "return_field" );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrRows = XMLHandler.countNodes( fields, "field" );

      allocate( nrRows );

      for ( int i = 0; i < nrRows; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldDatabase[ i ] = XMLHandler.getTagValue( knode, "column_name" );
        fieldStream[ i ] = XMLHandler.getTagValue( knode, "stream_name" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    databaseMeta = null;
    tableName = "";
    commitSize = "1000";

    partitioningEnabled = false;
    partitioningMonthly = true;
    partitioningField = "";
    tableNameInTable = true;
    tableNameField = "";

    // To be compatible with pre-v3.2 (SB)
    specifyFields = false;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    "
      + XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "    " + XMLHandler.addTagValue( "table", tableName ) );
    retval.append( "    " + XMLHandler.addTagValue( "commit", commitSize ) );
    retval.append( "    " + XMLHandler.addTagValue( "truncate", truncateTable ) );
    retval.append( "    " + XMLHandler.addTagValue( "ignore_errors", ignoreErrors ) );
    retval.append( "    " + XMLHandler.addTagValue( "use_batch", useBatchUpdate ) );
    retval.append( "    " + XMLHandler.addTagValue( "specify_fields", specifyFields ) );

    retval.append( "    " + XMLHandler.addTagValue( "partitioning_enabled", partitioningEnabled ) );
    retval.append( "    " + XMLHandler.addTagValue( "partitioning_field", partitioningField ) );
    retval.append( "    " + XMLHandler.addTagValue( "partitioning_daily", partitioningDaily ) );
    retval.append( "    " + XMLHandler.addTagValue( "partitioning_monthly", partitioningMonthly ) );

    retval.append( "    " + XMLHandler.addTagValue( "tablename_in_field", tableNameInField ) );
    retval.append( "    " + XMLHandler.addTagValue( "tablename_field", tableNameField ) );
    retval.append( "    " + XMLHandler.addTagValue( "tablename_in_table", tableNameInTable ) );

    retval.append( "    " + XMLHandler.addTagValue( "return_keys", returningGeneratedKeys ) );
    retval.append( "    " + XMLHandler.addTagValue( "return_field", generatedKeyField ) );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldDatabase.length; i++ ) {
      retval.append( "        <field>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "column_name", fieldDatabase[ i ] ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "stream_name", fieldStream[ i ] ) );
      retval.append( "        </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );
      commitSize = rep.getStepAttributeString( id_step, "commit" );
      truncateTable = rep.getStepAttributeBoolean( id_step, "truncate" );
      ignoreErrors = rep.getStepAttributeBoolean( id_step, "ignore_errors" );
      useBatchUpdate = rep.getStepAttributeBoolean( id_step, "use_batch" );
      specifyFields = rep.getStepAttributeBoolean( id_step, "specify_fields" );

      partitioningEnabled = rep.getStepAttributeBoolean( id_step, "partitioning_enabled" );
      partitioningField = rep.getStepAttributeString( id_step, "partitioning_field" );
      partitioningDaily = rep.getStepAttributeBoolean( id_step, "partitioning_daily" );
      partitioningMonthly = rep.getStepAttributeBoolean( id_step, "partitioning_monthly" );

      tableNameInField = rep.getStepAttributeBoolean( id_step, "tablename_in_field" );
      tableNameField = rep.getStepAttributeString( id_step, "tablename_field" );
      tableNameInTable = rep.getStepAttributeBoolean( id_step, "tablename_in_table" );

      returningGeneratedKeys = rep.getStepAttributeBoolean( id_step, "return_keys" );
      generatedKeyField = rep.getStepAttributeString( id_step, "return_field" );

      int nrCols = rep.countNrStepAttributes( id_step, "column_name" );
      int nrStreams = rep.countNrStepAttributes( id_step, "stream_name" );

      int nrRows = ( nrCols < nrStreams ? nrStreams : nrCols );
      allocate( nrRows );

      for ( int idx = 0; idx < nrRows; idx++ ) {
        fieldDatabase[ idx ] = Const.NVL( rep.getStepAttributeString( id_step, idx, "column_name" ), "" );
        fieldStream[ idx ] = Const.NVL( rep.getStepAttributeString( id_step, idx, "stream_name" ), "" );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );
      rep.saveStepAttribute( id_transformation, id_step, "commit", commitSize );
      rep.saveStepAttribute( id_transformation, id_step, "truncate", truncateTable );
      rep.saveStepAttribute( id_transformation, id_step, "ignore_errors", ignoreErrors );
      rep.saveStepAttribute( id_transformation, id_step, "use_batch", useBatchUpdate );
      rep.saveStepAttribute( id_transformation, id_step, "specify_fields", specifyFields );

      rep.saveStepAttribute( id_transformation, id_step, "partitioning_enabled", partitioningEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "partitioning_field", partitioningField );
      rep.saveStepAttribute( id_transformation, id_step, "partitioning_daily", partitioningDaily );
      rep.saveStepAttribute( id_transformation, id_step, "partitioning_monthly", partitioningMonthly );

      rep.saveStepAttribute( id_transformation, id_step, "tablename_in_field", tableNameInField );
      rep.saveStepAttribute( id_transformation, id_step, "tablename_field", tableNameField );
      rep.saveStepAttribute( id_transformation, id_step, "tablename_in_table", tableNameInTable );

      rep.saveStepAttribute( id_transformation, id_step, "return_keys", returningGeneratedKeys );
      rep.saveStepAttribute( id_transformation, id_step, "return_field", generatedKeyField );

      int nrRows = ( fieldDatabase.length < fieldStream.length ? fieldStream.length : fieldDatabase.length );
      for ( int idx = 0; idx < nrRows; idx++ ) {
        String columnName = ( idx < fieldDatabase.length ? fieldDatabase[ idx ] : "" );
        String streamName = ( idx < fieldStream.length ? fieldStream[ idx ] : "" );
        rep.saveStepAttribute( id_transformation, id_step, idx, "column_name", columnName );
        rep.saveStepAttribute( id_transformation, id_step, idx, "stream_name", streamName );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Just add the returning key field...
    if ( returningGeneratedKeys && generatedKeyField != null && generatedKeyField.length() > 0 ) {
      ValueMetaInterface key =
        new ValueMetaInteger( space.environmentSubstitute( generatedKeyField ) );
      key.setOrigin( origin );
      row.addValueMeta( key );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
                     Repository repository, IMetaStore metaStore ) {
    if ( databaseMeta != null ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "TableOutputMeta.CheckResult.ConnectionExists" ), stepMeta );
      remarks.add( cr );

      Database db = new Database( loggingObject, databaseMeta );
      db.shareVariablesWith( transMeta );
      try {
        db.connect();

        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "TableOutputMeta.CheckResult.ConnectionOk" ), stepMeta );
        remarks.add( cr );

        if ( !Utils.isEmpty( tableName ) ) {
          String realSchemaName = db.environmentSubstitute( schemaName );
          String realTableName = db.environmentSubstitute( tableName );
          String schemaTable =
            databaseMeta.getQuotedSchemaTableCombination( realSchemaName, realTableName );
          // Check if this table exists...
          if ( db.checkTableExists( realSchemaName, realTableName ) ) {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "TableOutputMeta.CheckResult.TableAccessible", schemaTable ), stepMeta );
            remarks.add( cr );

            RowMetaInterface r = db.getTableFieldsMeta( realSchemaName, realTableName );
            if ( r != null ) {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "TableOutputMeta.CheckResult.TableOk", schemaTable ), stepMeta );
              remarks.add( cr );

              String error_message = "";
              boolean error_found = false;
              // OK, we have the table fields.
              // Now see what we can find as previous step...
              if ( prev != null && prev.size() > 0 ) {
                cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                    PKG, "TableOutputMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
                remarks.add( cr );

                if ( !specifyFields() ) {
                  // Starting from prev...
                  for ( int i = 0; i < prev.size(); i++ ) {
                    ValueMetaInterface pv = prev.getValueMeta( i );
                    int idx = r.indexOfValue( pv.getName() );
                    if ( idx < 0 ) {
                      error_message += "\t\t" + pv.getName() + " (" + pv.getTypeDesc() + ")" + Const.CR;
                      error_found = true;
                    }
                  }
                  if ( error_found ) {
                    error_message =
                      BaseMessages.getString(
                        PKG, "TableOutputMeta.CheckResult.FieldsNotFoundInOutput", error_message );

                    cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
                    remarks.add( cr );
                  } else {
                    cr =
                      new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                        PKG, "TableOutputMeta.CheckResult.AllFieldsFoundInOutput" ), stepMeta );
                    remarks.add( cr );
                  }
                } else {
                  // Specifying the column names explicitly
                  for ( int i = 0; i < getFieldDatabase().length; i++ ) {
                    int idx = r.indexOfValue( getFieldDatabase()[ i ] );
                    if ( idx < 0 ) {
                      error_message += "\t\t" + getFieldDatabase()[ i ] + Const.CR;
                      error_found = true;
                    }
                  }
                  if ( error_found ) {
                    error_message =
                      BaseMessages.getString(
                        PKG, "TableOutputMeta.CheckResult.FieldsSpecifiedNotInTable", error_message );

                    cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
                    remarks.add( cr );
                  } else {
                    cr =
                      new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                        PKG, "TableOutputMeta.CheckResult.AllFieldsFoundInOutput" ), stepMeta );
                    remarks.add( cr );
                  }
                }

                error_message = "";
                if ( !specifyFields() ) {
                  // Starting from table fields in r...
                  for ( int i = 0; i < getFieldDatabase().length; i++ ) {
                    ValueMetaInterface rv = r.getValueMeta( i );
                    int idx = prev.indexOfValue( rv.getName() );
                    if ( idx < 0 ) {
                      error_message += "\t\t" + rv.getName() + " (" + rv.getTypeDesc() + ")" + Const.CR;
                      error_found = true;
                    }
                  }
                  if ( error_found ) {
                    error_message =
                      BaseMessages.getString( PKG, "TableOutputMeta.CheckResult.FieldsNotFound", error_message );

                    cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, error_message, stepMeta );
                    remarks.add( cr );
                  } else {
                    cr =
                      new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                        PKG, "TableOutputMeta.CheckResult.AllFieldsFound" ), stepMeta );
                    remarks.add( cr );
                  }
                } else {
                  // Specifying the column names explicitly
                  for ( int i = 0; i < getFieldStream().length; i++ ) {
                    int idx = prev.indexOfValue( getFieldStream()[ i ] );
                    if ( idx < 0 ) {
                      error_message += "\t\t" + getFieldStream()[ i ] + Const.CR;
                      error_found = true;
                    }
                  }
                  if ( error_found ) {
                    error_message =
                      BaseMessages.getString(
                        PKG, "TableOutputMeta.CheckResult.FieldsSpecifiedNotFound", error_message );

                    cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
                    remarks.add( cr );
                  } else {
                    cr =
                      new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                        PKG, "TableOutputMeta.CheckResult.AllFieldsFound" ), stepMeta );
                    remarks.add( cr );
                  }
                }
              } else {
                cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
                    PKG, "TableOutputMeta.CheckResult.NoFields" ), stepMeta );
                remarks.add( cr );
              }
            } else {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
                  PKG, "TableOutputMeta.CheckResult.TableNotAccessible" ), stepMeta );
              remarks.add( cr );
            }
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
                PKG, "TableOutputMeta.CheckResult.TableError", schemaTable ), stepMeta );
            remarks.add( cr );
          }
        } else {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "TableOutputMeta.CheckResult.NoTableName" ), stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "TableOutputMeta.CheckResult.UndefinedError", e.getMessage() ), stepMeta );
        remarks.add( cr );
      } finally {
        db.close();
      }
    } else {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "TableOutputMeta.CheckResult.NoConnection" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "TableOutputMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "TableOutputMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
                                TransMeta transMeta, Trans trans ) {
    return new TableOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new TableOutputData();
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
                             RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info,
                             Repository repository,
                             IMetaStore metaStore ) {
    if ( truncateTable ) {
      DatabaseImpact ii =
        new DatabaseImpact(
          DatabaseImpact.TYPE_IMPACT_TRUNCATE, transMeta.getName(), stepMeta.getName(), databaseMeta
          .getDatabaseName(), tableName, "", "", "", "", "Truncate of table" );
      impact.add( ii );

    }
    // The values that are entering this step are in "prev":
    if ( prev != null ) {
      for ( int i = 0; i < prev.size(); i++ ) {
        ValueMetaInterface v = prev.getValueMeta( i );
        DatabaseImpact ii =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
            .getDatabaseName(), tableName, v.getName(), v.getName(), v != null ? v.getOrigin() : "?", "",
            "Type = " + v.toStringMeta() );
        impact.add( ii );
      }
    }
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                                        Repository repository, IMetaStore metaStore ) {
    return getSQLStatements( transMeta, stepMeta, prev, null, false, null );
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String tk,
                                        boolean use_autoinc, String pk ) {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        if ( !Utils.isEmpty( tableName ) ) {
          Database db = new Database( loggingObject, databaseMeta );
          db.shareVariablesWith( transMeta );
          try {
            db.connect();

            String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
            String cr_table = db.getDDL( schemaTable, prev, tk, use_autoinc, pk );

            // Empty string means: nothing to do: set it to null...
            if ( cr_table == null || cr_table.length() == 0 ) {
              cr_table = null;
            }

            retval.setSQL( cr_table );
          } catch ( KettleDatabaseException dbe ) {
            retval.setError( BaseMessages.getString( PKG, "TableOutputMeta.Error.ErrorConnecting", dbe
              .getMessage() ) );
          } finally {
            db.close();
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "TableOutputMeta.Error.NoTable" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "TableOutputMeta.Error.NoInput" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "TableOutputMeta.Error.NoConnection" ) );
    }

    return retval;
  }

  public RowMetaInterface getRequiredFields( VariableSpace space ) throws KettleException {
    String realTableName = space.environmentSubstitute( tableName );
    String realSchemaName = space.environmentSubstitute( schemaName );

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( realTableName ) ) {
          // Check if this table exists...
          if ( db.checkTableExists( realSchemaName, realTableName ) ) {
            return db.getTableFieldsMeta( realSchemaName, realTableName );
          } else {
            throw new KettleException( BaseMessages.getString( PKG, "TableOutputMeta.Exception.TableNotFound" ) );
          }
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "TableOutputMeta.Exception.TableNotSpecified" ) );
        }
      } catch ( Exception e ) {
        throw new KettleException(
          BaseMessages.getString( PKG, "TableOutputMeta.Exception.ErrorGettingFields" ), e );
      } finally {
        db.close();
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG, "TableOutputMeta.Exception.ConnectionNotDefined" ) );
    }

  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return Fields containing the values in the input stream to insert.
   */
  public String[] getFieldStream() {
    return fieldStream;
  }

  /**
   * @param fieldStream The fields containing the values in the input stream to insert in the table.
   */
  public void setFieldStream( String[] fieldStream ) {
    this.fieldStream = fieldStream;
  }

  @Override public RowMeta getRowMeta( StepDataInterface stepData ) {
    return (RowMeta) ( (TableOutputData) stepData ).insertRowMeta;
  }

  @Override public List<String> getDatabaseFields() {
    if ( specifyFields() ) {
      return Arrays.asList( getFieldDatabase() );
    }
    return Collections.emptyList();
  }

  @Override public List<String> getStreamFields() {
    if ( specifyFields() ) {
      return Arrays.asList( getFieldStream() );
    }
    return Collections.emptyList();

  }

  /**
   * @return Fields containing the fieldnames in the database insert.
   */
  public String[] getFieldDatabase() {
    return fieldDatabase;
  }

  /**
   * @param fieldDatabase The fields containing the names of the fields to insert.
   */
  public void setFieldDatabase( String[] fieldDatabase ) {
    this.fieldDatabase = fieldDatabase;
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

  public boolean supportsErrorHandling() {
    if ( databaseMeta != null ) {
      return databaseMeta.getDatabaseInterface().supportsErrorHandling();
    } else {
      return true;
    }
  }

  @Override
  public String getMissingDatabaseConnectionInformationMessage() {
    // Use default connection missing message
    return null;
  }

  public void setConnection( String connectionName ) throws KettleException {
    // Get the databases from the Database connection list this is required by
    // MDI injection of the connection name
    databaseMeta = DatabaseMeta.findDatabase( getParentStepMeta().getParentTransMeta().getDatabases(), connectionName );
    if ( databaseMeta == null ) {
      String errMsg = BaseMessages.getString( PKG, "TableOutputMeta.Exception.ConnectionUndefined", getParentStepMeta().getName(), connectionName );
      logError( errMsg );
      throw new KettleException( errMsg );
    }
  }

}
