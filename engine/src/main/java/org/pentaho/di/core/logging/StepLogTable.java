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
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.w3c.dom.Node;

/**
 * This class describes a step logging table
 *
 * @author matt
 *
 */
public class StepLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

  private static Class<?> PKG = StepLogTable.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "step-log-table";

  public enum ID {

    ID_BATCH( "ID_BATCH" ), CHANNEL_ID( "CHANNEL_ID" ), LOG_DATE( "LOG_DATE" ), TRANSNAME( "TRANSNAME" ),
      STEPNAME( "STEPNAME" ), STEP_COPY( "STEP_COPY" ), LINES_READ( "LINES_READ" ),
      LINES_WRITTEN( "LINES_WRITTEN" ), LINES_UPDATED( "LINES_UPDATED" ), LINES_INPUT( "LINES_INPUT" ),
      LINES_OUTPUT( "LINES_OUTPUT" ), LINES_REJECTED( "LINES_REJECTED" ), ERRORS( "ERRORS" ),
      LOG_FIELD( "LOG_FIELD" );

    private String id;

    private ID( String id ) {
      this.id = id;
    }

    public String toString() {
      return id;
    }
  }

  private StepLogTable( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    super( space, databasesInterface, null, null, null );
  }

  @Override
  public Object clone() {
    try {
      StepLogTable table = (StepLogTable) super.clone();
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
    retval.append( "        " ).append( XMLHandler.addTagValue( "timeout_days", timeoutInDays ) );
    retval.append( super.getFieldsXML() );
    retval.append( "      " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node node, List<DatabaseMeta> databases, List<StepMeta> steps ) {
    connectionName = XMLHandler.getTagValue( node, "connection" );
    schemaName = XMLHandler.getTagValue( node, "schema" );
    tableName = XMLHandler.getTagValue( node, "table" );
    timeoutInDays = XMLHandler.getTagValue( node, "timeout_days" );

    super.loadFieldsXML( node );
  }

  @Override
  public void replaceMeta( LogTableCoreInterface logTableInterface ) {
    if ( !( logTableInterface instanceof StepLogTable ) ) {
      return;
    }

    StepLogTable logTable = (StepLogTable) logTableInterface;
    super.replaceMeta( logTable );
  }

  //CHECKSTYLE:LineLength:OFF
  public static StepLogTable getDefault( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    StepLogTable table = new StepLogTable( space, databasesInterface );

    table.fields.add( new LogTableField( ID.ID_BATCH.id, true, false, "ID_BATCH", BaseMessages.getString( PKG, "StepLogTable.FieldName.IdBatch" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.IdBatch" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString( PKG, "StepLogTable.FieldName.ChannelId" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.ChannelId" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.LOG_DATE.id, true, false, "LOG_DATE", BaseMessages.getString( PKG, "StepLogTable.FieldName.LogDate" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LogDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.TRANSNAME.id, true, false, "TRANSNAME", BaseMessages.getString( PKG, "StepLogTable.FieldName.TransName" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.TransName" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.STEPNAME.id, true, false, "STEPNAME", BaseMessages.getString( PKG, "StepLogTable.FieldName.StepName" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.StepName" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.STEP_COPY.id, true, false, "STEP_COPY", BaseMessages.getString( PKG, "StepLogTable.FieldName.StepCopy" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.StepCopy" ), ValueMetaInterface.TYPE_INTEGER, 3 ) );
    table.fields.add( new LogTableField( ID.LINES_READ.id, true, false, "LINES_READ", BaseMessages.getString( PKG, "StepLogTable.FieldName.LinesRead" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LinesRead" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_WRITTEN.id, true, false, "LINES_WRITTEN", BaseMessages.getString( PKG, "StepLogTable.FieldName.LinesWritten" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LinesWritten" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_UPDATED.id, true, false, "LINES_UPDATED", BaseMessages.getString( PKG, "StepLogTable.FieldName.LinesUpdated" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LinesUpdated" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_INPUT.id, true, false, "LINES_INPUT", BaseMessages.getString( PKG, "StepLogTable.FieldName.LinesInput" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LinesInput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_OUTPUT.id, true, false, "LINES_OUTPUT", BaseMessages.getString( PKG, "StepLogTable.FieldName.LinesOutput" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LinesOutput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_REJECTED.id, true, false, "LINES_REJECTED", BaseMessages.getString( PKG, "StepLogTable.FieldName.LinesRejected" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LinesRejected" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.ERRORS.id, true, false, "ERRORS", BaseMessages.getString( PKG, "StepLogTable.FieldName.Errors" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.Errors" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LOG_FIELD.id, false, false, "LOG_FIELD", BaseMessages.getString( PKG, "StepLogTable.FieldName.LogField" ), BaseMessages.getString( PKG, "StepLogTable.FieldDescription.LogField" ), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH ) );

    table.findField( ID.TRANSNAME.id ).setNameField( true );
    table.findField( ID.LOG_DATE.id ).setLogDateField( true );
    table.findField( ID.ID_BATCH.id ).setKey( true );
    table.findField( ID.CHANNEL_ID.id ).setVisible( false );
    table.findField( ID.LOG_FIELD.id ).setLogField( true );
    table.findField( ID.ERRORS.id ).setErrorsField( true );

    return table;
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
    if ( subject == null || subject instanceof StepMetaDataCombi ) {

      StepMetaDataCombi combi = (StepMetaDataCombi) subject;

      RowMetaAndData row = new RowMetaAndData();

      for ( LogTableField field : fields ) {
        if ( field.isEnabled() ) {
          Object value = null;
          if ( subject != null ) {
            switch ( ID.valueOf( field.getId() ) ) {

              case ID_BATCH:
                value = new Long( combi.step.getTrans().getBatchId() );
                break;
              case CHANNEL_ID:
                value = combi.step.getLogChannel().getLogChannelId();
                break;
              case LOG_DATE:
                value = new Date();
                break;
              case TRANSNAME:
                value = combi.step.getTrans().getName();
                break;
              case STEPNAME:
                value = combi.stepname;
                break;
              case STEP_COPY:
                value = new Long( combi.copy );
                break;
              case LINES_READ:
                value = new Long( combi.step.getLinesRead() );
                break;
              case LINES_WRITTEN:
                value = new Long( combi.step.getLinesWritten() );
                break;
              case LINES_UPDATED:
                value = new Long( combi.step.getLinesUpdated() );
                break;
              case LINES_INPUT:
                value = new Long( combi.step.getLinesInput() );
                break;
              case LINES_OUTPUT:
                value = new Long( combi.step.getLinesOutput() );
                break;
              case LINES_REJECTED:
                value = new Long( combi.step.getLinesRejected() );
                break;
              case ERRORS:
                value = new Long( combi.step.getErrors() );
                break;
              case LOG_FIELD:
                value = getLogBuffer( combi.step, combi.step.getLogChannel().getLogChannelId(), status, null );
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
    return "STEP";
  }

  public String getLogTableType() {
    return BaseMessages.getString( PKG, "StepLogTable.Type.Description" );
  }

  public String getConnectionNameVariable() {
    return Const.KETTLE_STEP_LOG_DB;
  }

  public String getSchemaNameVariable() {
    return Const.KETTLE_STEP_LOG_SCHEMA;
  }

  public String getTableNameVariable() {
    return Const.KETTLE_STEP_LOG_TABLE;
  }

  public List<RowMetaInterface> getRecommendedIndexes() {
    List<RowMetaInterface> indexes = new ArrayList<RowMetaInterface>();
    return indexes;
  }
}
