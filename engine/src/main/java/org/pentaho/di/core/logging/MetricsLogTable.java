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
import org.pentaho.di.core.metrics.MetricsSnapshotInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/**
 * This class describes a logging channel logging table
 *
 * @author matt
 *
 */
public class MetricsLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

  private static Class<?> PKG = MetricsLogTable.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "metrics-log-table";

  public enum ID {

    ID_BATCH( "ID_BATCH" ), CHANNEL_ID( "CHANNEL_ID" ), LOG_DATE( "LOG_DATE" ), // The date this record got logged
    METRICS_DATE( "METRICS_DATE" ), // For snapshot: the date/time of snapshot
      METRICS_CODE( "METRICS_CODE" ), // The unique code of the metric
      METRICS_DESCRIPTION( "METRICS_DESCRIPTION" ), // The description of the metric
      METRICS_SUBJECT( "METRICS_SUBJECT" ), // The subject of the metric
      METRICS_TYPE( "METRICS_TYPE" ), // For snapshots: START or STOP, for metrics: MAX, MIN, SUM
      METRICS_VALUE( "METRICS_VALUE" ); // For metrics: the value measured (max, min, sum)

    private String id;

    private ID( String id ) {
      this.id = id;
    }

    public String toString() {
      return id;
    }
  }

  private MetricsLogTable( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    super( space, databasesInterface, null, null, null );
  }

  @Override
  public Object clone() {
    try {
      MetricsLogTable table = (MetricsLogTable) super.clone();
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
    if ( !( logTableInterface instanceof MetricsLogTable ) ) {
      return;
    }

    MetricsLogTable logTable = (MetricsLogTable) logTableInterface;
    super.replaceMeta( logTable );
  }

  public static MetricsLogTable getDefault( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    MetricsLogTable table = new MetricsLogTable( space, databasesInterface );

    //CHECKSTYLE:LineLength:OFF
    table.fields.add( new LogTableField( ID.ID_BATCH.id, true, false, "ID_BATCH", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.IdBatch" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.IdBatch" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.ChannelId" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.ChannelId" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.LOG_DATE.id, true, false, "LOG_DATE", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.LogDate" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.LogDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.METRICS_DATE.id, true, false, "METRICS_DATE", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.MetricsDate" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.MetricsDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.METRICS_CODE.id, true, false, "METRICS_CODE", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.MetricsDescription" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.MetricsCode" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.METRICS_DESCRIPTION.id, true, false, "METRICS_DESCRIPTION", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.MetricsDescription" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.MetricsDescription" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.METRICS_SUBJECT.id, true, false, "METRICS_SUBJECT", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.MetricsSubject" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.MetricsSubject" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.METRICS_TYPE.id, true, false, "METRICS_TYPE", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.MetricsType" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.MetricsType" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.METRICS_VALUE.id, true, false, "METRICS_VALUE", BaseMessages.getString( PKG, "MetricsLogTable.FieldName.MetricsValue" ), BaseMessages.getString( PKG, "MetricsLogTable.FieldDescription.MetricsValue" ), ValueMetaInterface.TYPE_INTEGER, 12 ) );

    table.findField( ID.LOG_DATE.id ).setLogDateField( true );
    table.findField( ID.ID_BATCH.id ).setKey( true );

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
    if ( subject == null || subject instanceof LoggingMetric ) {

      LoggingMetric loggingMetric = (LoggingMetric) subject;
      MetricsSnapshotInterface snapshot = null;
      if ( subject != null ) {
        snapshot = loggingMetric.getSnapshot();
      }

      RowMetaAndData row = new RowMetaAndData();

      for ( LogTableField field : fields ) {
        if ( field.isEnabled() ) {
          Object value = null;
          if ( subject != null ) {
            switch ( ID.valueOf( field.getId() ) ) {
              case ID_BATCH:
                value = new Long( loggingMetric.getBatchId() );
                break;
              case CHANNEL_ID:
                value = snapshot.getLogChannelId();
                break;
              case LOG_DATE:
                value = new Date();
                break;
              case METRICS_DATE:
                value = snapshot.getDate();
                break;
              case METRICS_CODE:
                value = snapshot.getMetric().getCode();
                break;
              case METRICS_DESCRIPTION:
                value = snapshot.getMetric().getDescription();
                break;
              case METRICS_SUBJECT:
                value = snapshot.getSubject();
                break;
              case METRICS_TYPE:
                value = snapshot.getMetric().getType().name();
                break;
              case METRICS_VALUE:
                value = snapshot.getValue();
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
    return "METRICS";
  }

  public String getLogTableType() {
    return BaseMessages.getString( PKG, "MetricsLogTable.Type.Description" );
  }

  public String getConnectionNameVariable() {
    return Const.KETTLE_METRICS_LOG_DB;
  }

  public String getSchemaNameVariable() {
    return Const.KETTLE_METRICS_LOG_SCHEMA;
  }

  public String getTableNameVariable() {
    return Const.KETTLE_METRICS_LOG_TABLE;
  }

  public List<RowMetaInterface> getRecommendedIndexes() {
    List<RowMetaInterface> indexes = new ArrayList<RowMetaInterface>();
    return indexes;
  }

}
