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
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/**
 * This class describes a job entry logging table
 *
 * @author matt
 *
 */
public class JobEntryLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

  private static Class<?> PKG = JobEntryLogTable.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "jobentry-log-table";

  public enum ID {

    ID_BATCH( "ID_BATCH" ), CHANNEL_ID( "CHANNEL_ID" ), LOG_DATE( "LOG_DATE" ), JOBNAME( "JOBNAME" ),
      JOBENTRYNAME( "JOBENTRYNAME" ), LINES_READ( "LINES_READ" ), LINES_WRITTEN( "LINES_WRITTEN" ), LINES_UPDATED(
        "LINES_UPDATED" ), LINES_INPUT( "LINES_INPUT" ), LINES_OUTPUT( "LINES_OUTPUT" ), LINES_REJECTED(
        "LINES_REJECTED" ), ERRORS( "ERRORS" ), RESULT( "RESULT" ), NR_RESULT_ROWS( "NR_RESULT_ROWS" ),
      NR_RESULT_FILES( "NR_RESULT_FILES" ), LOG_FIELD( "LOG_FIELD" ), COPY_NR( "COPY_NR" );

    private String id;

    private ID( String id ) {
      this.id = id;
    }

    public String toString() {
      return id;
    }
  }

  private JobEntryLogTable( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    super( space, databasesInterface, null, null, null );
  }

  @Override
  public Object clone() {
    try {
      JobEntryLogTable table = (JobEntryLogTable) super.clone();
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

  @Override
  public void replaceMeta( LogTableCoreInterface logTableInterface ) {
    if ( !( logTableInterface instanceof JobEntryLogTable ) ) {
      return;
    }

    JobEntryLogTable logTable = (JobEntryLogTable) logTableInterface;
    super.replaceMeta( logTable );
  }

  public void loadXML( Node jobnode, List<DatabaseMeta> databases, List<StepMeta> steps ) {
    Node node = XMLHandler.getSubNode( jobnode, XML_TAG );
    if ( node == null ) {
      return;
    }

    connectionName = XMLHandler.getTagValue( node, "connection" );
    schemaName = XMLHandler.getTagValue( node, "schema" );
    tableName = XMLHandler.getTagValue( node, "table" );
    timeoutInDays = XMLHandler.getTagValue( node, "timeout_days" );

    super.loadFieldsXML( node );
  }

  //CHECKSTYLE:LineLength:OFF
  public static JobEntryLogTable getDefault( VariableSpace space, HasDatabasesInterface databasesInterface ) {
    JobEntryLogTable table = new JobEntryLogTable( space, databasesInterface );

    table.fields.add( new LogTableField( ID.ID_BATCH.id, true, false, "ID_BATCH", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.IdBatch" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.IdBatch" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.ChannelId" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.ChannelId" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.LOG_DATE.id, true, false, "LOG_DATE", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LogDate" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LogDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.JOBNAME.id, true, false, "TRANSNAME", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.JobName" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.JobName" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.JOBENTRYNAME.id, true, false, "STEPNAME", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.JobEntryName" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.JobEntryName" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.LINES_READ.id, true, false, "LINES_READ", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LinesRead" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LinesRead" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_WRITTEN.id, true, false, "LINES_WRITTEN", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LinesWritten" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LinesWritten" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_UPDATED.id, true, false, "LINES_UPDATED", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LinesUpdated" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LinesUpdated" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_INPUT.id, true, false, "LINES_INPUT", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LinesInput" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LinesInput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_OUTPUT.id, true, false, "LINES_OUTPUT", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LinesOutput" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LinesOutput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_REJECTED.id, true, false, "LINES_REJECTED", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LinesRejected" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LinesRejected" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.ERRORS.id, true, false, "ERRORS", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.Errors" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.Errors" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.RESULT.id, true, false, "RESULT", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.Result" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.Result" ), ValueMetaInterface.TYPE_BOOLEAN, -1 ) );
    table.fields.add( new LogTableField( ID.NR_RESULT_ROWS.id, true, false, "NR_RESULT_ROWS", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.NrResultRows" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.NrResultRows" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.NR_RESULT_FILES.id, true, false, "NR_RESULT_FILES", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.NrResultFiles" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.NrResultFiles" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LOG_FIELD.id, false, false, "LOG_FIELD", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.LogField" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.LogField" ), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH ) );
    table.fields.add( new LogTableField( ID.COPY_NR.id, false, false, "COPY_NR", BaseMessages.getString( PKG, "JobEntryLogTable.FieldName.CopyNr" ), BaseMessages.getString( PKG, "JobEntryLogTable.FieldDescription.CopyNr" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );

    table.findField( ID.JOBNAME.id ).setNameField( true );
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
   * @param subject
   *          the object to log
   * @param parent
   *          the parent to which the object belongs
   */
  public RowMetaAndData getLogRecord( LogStatus status, Object subject, Object parent ) {
    if ( subject == null || subject instanceof JobEntryCopy ) {

      JobEntryCopy jobEntryCopy = (JobEntryCopy) subject;
      Job parentJob = (Job) parent;

      RowMetaAndData row = new RowMetaAndData();

      for ( LogTableField field : fields ) {
        if ( field.isEnabled() ) {
          Object value = null;
          if ( subject != null ) {

            JobEntryInterface jobEntry = jobEntryCopy.getEntry();
            JobTracker jobTracker = parentJob.getJobTracker();
            JobTracker entryTracker = jobTracker.findJobTracker( jobEntryCopy );
            JobEntryResult jobEntryResult = null;
            if ( entryTracker != null ) {
              jobEntryResult = entryTracker.getJobEntryResult();
            }
            Result result = null;
            if ( jobEntryResult != null ) {
              result = jobEntryResult.getResult();
            }

            switch ( ID.valueOf( field.getId() ) ) {

              case ID_BATCH:
                value = new Long( parentJob.getBatchId() );
                break;
              case CHANNEL_ID:
                value = jobEntry.getLogChannel().getLogChannelId();
                break;
              case LOG_DATE:
                value = new Date();
                break;
              case JOBNAME:
                value = parentJob.getJobname();
                break;
              case JOBENTRYNAME:
                value = jobEntry.getName();
                break;
              case LINES_READ:
                value = new Long( result != null ? result.getNrLinesRead() : 0 );
                break;
              case LINES_WRITTEN:
                value = new Long( result != null ? result.getNrLinesWritten() : 0 );
                break;
              case LINES_UPDATED:
                value = new Long( result != null ? result.getNrLinesUpdated() : 0 );
                break;
              case LINES_INPUT:
                value = new Long( result != null ? result.getNrLinesInput() : 0 );
                break;
              case LINES_OUTPUT:
                value = new Long( result != null ? result.getNrLinesOutput() : 0 );
                break;
              case LINES_REJECTED:
                value = new Long( result != null ? result.getNrLinesRejected() : 0 );
                break;
              case ERRORS:
                value = new Long( result != null ? result.getNrErrors() : 0 );
                break;
              case RESULT:
                value = new Boolean( result != null ? result.getResult() : false );
                break;
              case NR_RESULT_FILES:
                value =
                  new Long( result != null && result.getResultFiles() != null
                    ? result.getResultFiles().size() : 0 );
                break;
              case NR_RESULT_ROWS:
                value = new Long( result != null && result.getRows() != null ? result.getRows().size() : 0 );
                break;
              case LOG_FIELD:
                if ( result != null ) {
                  value = result.getLogText();
                }
                break;
              case COPY_NR:
                value = new Long( jobEntryCopy.getNr() );
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
    return "JOB_ENTRY";
  }

  public String getLogTableType() {
    return BaseMessages.getString( PKG, "JobEntryLogTable.Type.Description" );
  }

  public String getConnectionNameVariable() {
    return Const.KETTLE_JOBENTRY_LOG_DB;
  }

  public String getSchemaNameVariable() {
    return Const.KETTLE_JOBENTRY_LOG_SCHEMA;
  }

  public String getTableNameVariable() {
    return Const.KETTLE_JOBENTRY_LOG_TABLE;
  }

  public List<RowMetaInterface> getRecommendedIndexes() {
    List<RowMetaInterface> indexes = new ArrayList<RowMetaInterface>();
    LogTableField keyField = getKeyField();

    if ( keyField.isEnabled() ) {
      RowMetaInterface batchIndex = new RowMeta();

      ValueMetaInterface keyMeta = new ValueMetaBase( keyField.getFieldName(), keyField.getDataType() );
      keyMeta.setLength( keyField.getLength() );
      batchIndex.addValueMeta( keyMeta );

      indexes.add( batchIndex );
    }

    return indexes;
  }
}
