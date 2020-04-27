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

package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.performance.StepPerformanceSnapShot;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/**
 * This class describes a step performance logging table
 *
 * @author matt
 *
 */
public class PerformanceLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

  private static Class<?> PKG = PerformanceLogTable.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "perf-log-table";

  public enum ID {

    ID_BATCH( "ID_BATCH" ), SEQ_NR( "SEQ_NR" ), LOGDATE( "LOGDATE" ), TRANSNAME( "TRANSNAME" ), STEPNAME(
      "STEPNAME" ), STEP_COPY( "STEP_COPY" ), LINES_READ( "LINES_READ" ), LINES_WRITTEN( "LINES_WRITTEN" ),
      LINES_UPDATED( "LINES_UPDATED" ), LINES_INPUT( "LINES_INPUT" ), LINES_OUTPUT( "LINES_OUTPUT" ),
      LINES_REJECTED( "LINES_REJECTED" ), ERRORS( "ERRORS" ), INPUT_BUFFER_ROWS( "INPUT_BUFFER_ROWS" ),
      OUTPUT_BUFFER_ROWS( "OUTPUT_BUFFER_ROWS" );

    private String id;

    private ID( String id ) {
      this.id = id;
    }

    public String toString() {
      return id;
    }
  }

  private String logInterval;

  private PerformanceLogTable( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    super( space, databasesInterface, null, null, null );
  }

  @Override
  public Object clone() {
    try {
      PerformanceLogTable table = (PerformanceLogTable) super.clone();
      table.fields = new ArrayList<LogTableField>();
      for ( LogTableField field : this.fields ) {
        table.fields.add( (LogTableField) field.clone() );
      }
      return table;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "      " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    retval.append( "        " ).append( XMLHandler.addTagValue( "connection", connectionName ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "table", tableName ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "interval", logInterval ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "timeout_days", timeoutInDays ) );
    retval.append( super.getFieldsXML() );
    retval.append( "      " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node node, List<DatabaseMeta> databases, List<StepMeta> steps ) {
    connectionName = XMLHandler.getTagValue( node, "connection" );
    schemaName = XMLHandler.getTagValue( node, "schema" );
    tableName = XMLHandler.getTagValue( node, "table" );
    logInterval = XMLHandler.getTagValue( node, "interval" );
    timeoutInDays = XMLHandler.getTagValue( node, "timeout_days" );

    super.loadFieldsXML( node );
  }

  public void saveToRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException {
    super.saveToRepository( attributeInterface );

    // Also save the log interval and log size limit
    //
    attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_INTERVAL, logInterval );
  }

  public void loadFromRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException {
    super.loadFromRepository( attributeInterface );

    logInterval = attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_INTERVAL );
  }

  @Override
  public void replaceMeta( LogTableCoreInterface logTableInterface ) {
    if ( !( logTableInterface instanceof PerformanceLogTable ) ) {
      return;
    }

    PerformanceLogTable logTable = (PerformanceLogTable) logTableInterface;
    super.replaceMeta( logTable );
  }

  public static PerformanceLogTable getDefault( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    PerformanceLogTable table = new PerformanceLogTable( space, databasesInterface );

    //CHECKSTYLE:LineLength:OFF
    table.fields.add( new LogTableField( ID.ID_BATCH.id, true, false, "ID_BATCH", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.BatchID" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.BatchID" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.SEQ_NR.id, true, false, "SEQ_NR", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.SeqNr" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.SeqNr" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.LOGDATE.id, true, false, "LOGDATE", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.LogDate" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.LogDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.TRANSNAME.id, true, false, "TRANSNAME", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.TransName" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.TransName" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.STEPNAME.id, true, false, "STEPNAME", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.StepName" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.StepName" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.STEP_COPY.id, true, false, "STEP_COPY", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.StepCopy" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.StepCopy" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.LINES_READ.id, true, false, "LINES_READ", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.LinesRead" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.LinesRead" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_WRITTEN.id, true, false, "LINES_WRITTEN", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.LinesWritten" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.LinesWritten" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_UPDATED.id, true, false, "LINES_UPDATED", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.LinesUpdated" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.LinesUpdated" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_INPUT.id, true, false, "LINES_INPUT", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.LinesInput" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.LinesInput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_OUTPUT.id, true, false, "LINES_OUTPUT", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.LinesOutput" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.LinesOutput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_REJECTED.id, true, false, "LINES_REJECTED", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.LinesRejected" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.LinesRejected" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.ERRORS.id, true, false, "ERRORS", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.Errors" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.Errors" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.INPUT_BUFFER_ROWS.id, true, false, "INPUT_BUFFER_ROWS", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.InputBufferRows" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.InputBufferRows" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.OUTPUT_BUFFER_ROWS.id, true, false, "OUTPUT_BUFFER_ROWS", BaseMessages.getString( PKG, "PerformanceLogTable.FieldName.OutputBufferRows" ), BaseMessages.getString( PKG, "PerformanceLogTable.FieldDescription.OutputBufferRows" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );

    table.findField( ID.ID_BATCH.id ).setKey( true );
    table.findField( ID.LOGDATE.id ).setLogDateField( true );
    table.findField( ID.TRANSNAME.id ).setNameField( true );

    return table;
  }

  /**
   * Sets the logging interval in seconds. Disabled if the logging interval is <=0.
   *
   * @param logInterval
   *          The log interval value. A value higher than 0 means that the log table is updated every 'logInterval'
   *          seconds.
   */
  public void setLogInterval( String logInterval ) {
    this.logInterval = logInterval;
  }

  /**
   * Get the logging interval in seconds. Disabled if the logging interval is <=0. A value higher than 0 means that the
   * log table is updated every 'logInterval' seconds.
   *
   * @param logInterval
   *          The log interval,
   */
  public String getLogInterval() {
    return logInterval;
  }

  /**
   * This method calculates all the values that are required
   *
   * @param id
   *          the id to use or -1 if no id is needed
   * @param status
   *          the log status to use
   */
  public RowMetaAndData getLogRecord( LogStatus status, Object subject, Object parent ) {
    if ( subject == null || subject instanceof StepPerformanceSnapShot ) {
      StepPerformanceSnapShot snapShot = (StepPerformanceSnapShot) subject;

      RowMetaAndData row = new RowMetaAndData();

      for ( LogTableField field : fields ) {
        if ( field.isEnabled() ) {
          Object value = null;
          if ( subject != null ) {
            switch ( ID.valueOf( field.getId() ) ) {

              case ID_BATCH:
                value = new Long( snapShot.getBatchId() );
                break;
              case SEQ_NR:
                value = new Long( snapShot.getSeqNr() );
                break;
              case LOGDATE:
                value = snapShot.getDate();
                break;
              case TRANSNAME:
                value = snapShot.getTransName();
                break;
              case STEPNAME:
                value = snapShot.getStepName();
                break;
              case STEP_COPY:
                value = new Long( snapShot.getStepCopy() );
                break;
              case LINES_READ:
                value = new Long( snapShot.getLinesRead() );
                break;
              case LINES_WRITTEN:
                value = new Long( snapShot.getLinesWritten() );
                break;
              case LINES_INPUT:
                value = new Long( snapShot.getLinesInput() );
                break;
              case LINES_OUTPUT:
                value = new Long( snapShot.getLinesOutput() );
                break;
              case LINES_UPDATED:
                value = new Long( snapShot.getLinesUpdated() );
                break;
              case LINES_REJECTED:
                value = new Long( snapShot.getLinesRejected() );
                break;
              case ERRORS:
                value = new Long( snapShot.getErrors() );
                break;
              case INPUT_BUFFER_ROWS:
                value = new Long( snapShot.getInputBufferSize() );
                break;
              case OUTPUT_BUFFER_ROWS:
                value = new Long( snapShot.getOutputBufferSize() );
                break;
              default:
                break;
            }
          }

          row.addValue( field.getFieldName(), field.getDataType(), value );
          row.getRowMeta().getValueMeta( row.size() - 1 ).setLength( field.getLength() );
        }
      }

      return row;
    } else {
      return null;
    }
  }

  public String getLogTableCode() {
    return "PERFORMANCE";
  }

  public String getLogTableType() {
    return BaseMessages.getString( PKG, "PerformanceLogTable.Type.Description" );
  }

  public String getConnectionNameVariable() {
    return Const.KETTLE_TRANS_PERFORMANCE_LOG_DB;
  }

  public String getSchemaNameVariable() {
    return Const.KETTLE_TRANS_PERFORMANCE_LOG_SCHEMA;
  }

  public String getTableNameVariable() {
    return Const.KETTLE_TRANS_PERFORMANCE_LOG_TABLE;
  }

  public List<RowMetaInterface> getRecommendedIndexes() {
    List<RowMetaInterface> indexes = new ArrayList<RowMetaInterface>();
    return indexes;
  }

  @Override
  public void setAllGlobalParametersToNull()  {
    boolean clearGlobalVariables = Boolean.valueOf( System.getProperties().getProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "false" ) );
    if ( clearGlobalVariables ) {
      super.setAllGlobalParametersToNull();

      logInterval = isGlobalParameter( logInterval ) ? null : logInterval;
    }
  }
}
