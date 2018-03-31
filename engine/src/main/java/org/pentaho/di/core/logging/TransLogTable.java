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
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/**
 * This class describes a transformation logging table
 *
 * @author matt
 *
 */
public class TransLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

  private static Class<?> PKG = TransLogTable.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "trans-log-table";

  /** The client executing the transformation */
  // private String client;

  public enum ID {

    ID_BATCH( "ID_BATCH" ), CHANNEL_ID( "CHANNEL_ID" ), TRANSNAME( "TRANSNAME" ), STATUS( "STATUS" ), LINES_READ(
      "LINES_READ" ), LINES_WRITTEN( "LINES_WRITTEN" ), LINES_UPDATED( "LINES_UPDATED" ), LINES_INPUT(
      "LINES_INPUT" ), LINES_OUTPUT( "LINES_OUTPUT" ), LINES_REJECTED( "LINES_REJECTED" ), ERRORS( "ERRORS" ),
      STARTDATE( "STARTDATE" ), ENDDATE( "ENDDATE" ), LOGDATE( "LOGDATE" ), DEPDATE( "DEPDATE" ), REPLAYDATE(
        "REPLAYDATE" ), LOG_FIELD( "LOG_FIELD" ), EXECUTING_SERVER( "EXECUTING_SERVER" ), EXECUTING_USER(
        "EXECUTING_USER" ), CLIENT( "CLIENT" );

    private String id;

    private ID( String id ) {
      this.id = id;
    }

    public String toString() {
      return id;
    }
  }

  private String logInterval;

  private String logSizeLimit;

  private List<StepMeta> steps;

  public TransLogTable( VariableSpace space, HasDatabasesInterface databasesInterface, List<StepMeta> steps ) {
    super( space, databasesInterface, null, null, null );
    this.steps = steps;
  }

  @Override
  public Object clone() {
    try {
      TransLogTable table = (TransLogTable) super.clone();
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
    retval.append( "        " ).append( XMLHandler.addTagValue( "size_limit_lines", logSizeLimit ) );
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
    logSizeLimit = XMLHandler.getTagValue( node, "size_limit_lines" );
    logInterval = XMLHandler.getTagValue( node, "interval" );
    timeoutInDays = XMLHandler.getTagValue( node, "timeout_days" );

    int nr = XMLHandler.countNodes( node, BaseLogTable.XML_TAG );
    for ( int i = 0; i < nr; i++ ) {
      Node fieldNode = XMLHandler.getSubNodeByNr( node, BaseLogTable.XML_TAG, i );
      String id = XMLHandler.getTagValue( fieldNode, "id" );
      LogTableField field = findField( id );
      if ( field == null ) {
        field = fields.get( i );
      }
      if ( field != null ) {
        field.setFieldName( XMLHandler.getTagValue( fieldNode, "name" ) );
        field.setEnabled( "Y".equalsIgnoreCase( XMLHandler.getTagValue( fieldNode, "enabled" ) ) );
        field.setSubject( StepMeta.findStep( steps, XMLHandler.getTagValue( fieldNode, "subject" ) ) );
      }
    }
  }

  public void saveToRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException {
    super.saveToRepository( attributeInterface );

    // Also save the log interval and log size limit
    //
    attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_INTERVAL, logInterval );
    attributeInterface.setAttribute( getLogTableCode() + PROP_LOG_TABLE_SIZE_LIMIT, logSizeLimit );
  }

  public void loadFromRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException {
    super.loadFromRepository( attributeInterface );

    logInterval = attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_INTERVAL );
    logSizeLimit = attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_SIZE_LIMIT );

    for ( int i = 0; i < getFields().size(); i++ ) {
      String id = attributeInterface.getAttributeString( getLogTableCode() + PROP_LOG_TABLE_FIELD_ID + i );
      // Only read further if the ID is available.
      // For backward compatibility, this might not be provided yet!
      //
      if ( id != null ) {
        LogTableField field = findField( id );
        if ( field.isSubjectAllowed() ) {

          // BaseLogTable.loadFromRepository sets subject as a String
          //
          String stepname = (String) field.getSubject();
          if ( !Utils.isEmpty( stepname ) ) {
            field.setSubject( StepMeta.findStep( steps, stepname ) );
          } else {
            field.setSubject( null );
          }
        }
      }
    }
  }

  @Override
  public void replaceMeta( LogTableCoreInterface logTableInterface ) {
    if ( !( logTableInterface instanceof TransLogTable ) ) {
      return;
    }

    TransLogTable logTable = (TransLogTable) logTableInterface;
    super.replaceMeta( logTable );
  }

  //CHECKSTYLE:LineLength:OFF
  public static TransLogTable getDefault( VariableSpace space, HasDatabasesInterface databasesInterface,
    List<StepMeta> steps ) {
    TransLogTable table = new TransLogTable( space, databasesInterface, steps );

    table.fields.add( new LogTableField( ID.ID_BATCH.id, true, false, "ID_BATCH", BaseMessages.getString( PKG, "TransLogTable.FieldName.BatchID" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.BatchID" ), ValueMetaInterface.TYPE_INTEGER, 8 ) );
    table.fields.add( new LogTableField( ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString( PKG, "TransLogTable.FieldName.ChannelID" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.ChannelID" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.TRANSNAME.id, true, false, "TRANSNAME", BaseMessages.getString( PKG, "TransLogTable.FieldName.TransName" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.TransName" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.STATUS.id, true, false, "STATUS", BaseMessages.getString( PKG, "TransLogTable.FieldName.Status" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.Status" ), ValueMetaInterface.TYPE_STRING, 15 ) );
    table.fields.add( new LogTableField( ID.LINES_READ.id, true, true, "LINES_READ", BaseMessages.getString( PKG, "TransLogTable.FieldName.LinesRead" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LinesRead" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_WRITTEN.id, true, true, "LINES_WRITTEN", BaseMessages.getString( PKG, "TransLogTable.FieldName.LinesWritten" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LinesWritten" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_UPDATED.id, true, true, "LINES_UPDATED", BaseMessages.getString( PKG, "TransLogTable.FieldName.LinesUpdated" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LinesUpdated" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_INPUT.id, true, true, "LINES_INPUT", BaseMessages.getString( PKG, "TransLogTable.FieldName.LinesInput" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LinesInput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_OUTPUT.id, true, true, "LINES_OUTPUT", BaseMessages.getString( PKG, "TransLogTable.FieldName.LinesOutput" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LinesOutput" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.LINES_REJECTED.id, true, true, "LINES_REJECTED", BaseMessages.getString( PKG, "TransLogTable.FieldName.LinesRejected" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LinesRejected" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.ERRORS.id, true, false, "ERRORS", BaseMessages.getString( PKG, "TransLogTable.FieldName.Errors" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.Errors" ), ValueMetaInterface.TYPE_INTEGER, 18 ) );
    table.fields.add( new LogTableField( ID.STARTDATE.id, true, false, "STARTDATE", BaseMessages.getString( PKG, "TransLogTable.FieldName.StartDateRange" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.StartDateRange" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.ENDDATE.id, true, false, "ENDDATE", BaseMessages.getString( PKG, "TransLogTable.FieldName.EndDateRange" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.EndDateRange" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.LOGDATE.id, true, false, "LOGDATE", BaseMessages.getString( PKG, "TransLogTable.FieldName.LogDate" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LogDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.DEPDATE.id, true, false, "DEPDATE", BaseMessages.getString( PKG, "TransLogTable.FieldName.DepDate" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.DepDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.REPLAYDATE.id, true, false, "REPLAYDATE", BaseMessages.getString( PKG, "TransLogTable.FieldName.ReplayDate" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.ReplayDate" ), ValueMetaInterface.TYPE_DATE, -1 ) );
    table.fields.add( new LogTableField( ID.LOG_FIELD.id, true, false, "LOG_FIELD", BaseMessages.getString( PKG, "TransLogTable.FieldName.LogField" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.LogField" ), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH ) );
    table.fields.add( new LogTableField( ID.EXECUTING_SERVER.id, false, false, "EXECUTING_SERVER", BaseMessages.getString( PKG, "TransLogTable.FieldName.ExecutingServer" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.ExecutingServer" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.EXECUTING_USER.id, false, false, "EXECUTING_USER", BaseMessages.getString( PKG, "TransLogTable.FieldName.ExecutingUser" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.ExecutingUser" ), ValueMetaInterface.TYPE_STRING, 255 ) );
    table.fields.add( new LogTableField( ID.CLIENT.id, false, false, "CLIENT", BaseMessages.getString( PKG, "TransLogTable.FieldName.Client" ), BaseMessages.getString( PKG, "TransLogTable.FieldDescription.Client" ), ValueMetaInterface.TYPE_STRING, 255 ) );

    table.findField( ID.ID_BATCH ).setKey( true );
    table.findField( ID.LOGDATE ).setLogDateField( true );
    table.findField( ID.LOG_FIELD ).setLogField( true );
    table.findField( ID.CHANNEL_ID ).setVisible( false );
    table.findField( ID.TRANSNAME ).setVisible( false );
    table.findField( ID.STATUS ).setStatusField( true );
    table.findField( ID.ERRORS ).setErrorsField( true );
    table.findField( ID.TRANSNAME ).setNameField( true );

    return table;
  }

  public LogTableField findField( ID id ) {
    return super.findField( id.id );
  }

  public Object getSubject( ID id ) {
    return super.getSubject( id.id );
  }

  public String getSubjectString( ID id ) {
    return super.getSubjectString( id.id );
  }

  public void setBatchIdUsed( boolean use ) {
    findField( ID.ID_BATCH ).setEnabled( use );
  }

  public boolean isBatchIdUsed() {
    return findField( ID.ID_BATCH ).isEnabled();
  }

  public void setLogFieldUsed( boolean use ) {
    findField( ID.LOG_FIELD ).setEnabled( use );
  }

  public boolean isLogFieldUsed() {
    return findField( ID.LOG_FIELD ).isEnabled();
  }

  public String getStepnameRead() {
    return getSubjectString( ID.LINES_READ );
  }

  public void setStepRead( StepMeta read ) {
    findField( ID.LINES_READ ).setSubject( read );
  }

  public String getStepnameWritten() {
    return getSubjectString( ID.LINES_WRITTEN );
  }

  public void setStepWritten( StepMeta written ) {
    findField( ID.LINES_WRITTEN ).setSubject( written );
  }

  public String getStepnameInput() {
    return getSubjectString( ID.LINES_INPUT );
  }

  public void setStepInput( StepMeta input ) {
    findField( ID.LINES_INPUT ).setSubject( input );
  }

  public String getStepnameOutput() {
    return getSubjectString( ID.LINES_OUTPUT );
  }

  public void setStepOutput( StepMeta output ) {
    findField( ID.LINES_OUTPUT ).setSubject( output );
  }

  public String getStepnameUpdated() {
    return getSubjectString( ID.LINES_UPDATED );
  }

  public void setStepUpdate( StepMeta update ) {
    findField( ID.LINES_UPDATED ).setSubject( update );
  }

  public String getStepnameRejected() {
    return getSubjectString( ID.LINES_REJECTED );
  }

  public void setStepRejected( StepMeta rejected ) {
    findField( ID.LINES_REJECTED ).setSubject( rejected );
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
   * @return the logSizeLimit
   */
  public String getLogSizeLimit() {
    return logSizeLimit;
  }

  /**
   * @param logSizeLimit
   *          the logSizeLimit to set
   */
  public void setLogSizeLimit( String logSizeLimit ) {
    this.logSizeLimit = logSizeLimit;
  }

  /**
   * This method calculates all the values that are required
   *
   * @param id
   *          the id to use or -1 if no id is needed
   * @param status
   *          the log status to use
   * @param subject
   *          the subject to query, in this case a Trans object
   */
  public RowMetaAndData getLogRecord( LogStatus status, Object subject, Object parent ) {
    if ( subject == null || subject instanceof Trans ) {
      Trans trans = (Trans) subject;
      Result result = null;
      if ( trans != null ) {
        result = trans.getResult();
      }

      RowMetaAndData row = new RowMetaAndData();

      for ( LogTableField field : fields ) {
        if ( field.isEnabled() ) {
          Object value = null;
          if ( trans != null ) {

            switch ( ID.valueOf( field.getId() ) ) {
              case ID_BATCH:
                value = new Long( trans.getBatchId() );
                break;
              case CHANNEL_ID:
                value = trans.getLogChannelId();
                break;
              case TRANSNAME:
                value = trans.getName();
                break;
              case STATUS:
                value = status.getStatus();
                break;
              case LINES_READ:
                value = new Long( result.getNrLinesRead() );
                break;
              case LINES_WRITTEN:
                value = new Long( result.getNrLinesWritten() );
                break;
              case LINES_INPUT:
                value = new Long( result.getNrLinesInput() );
                break;
              case LINES_OUTPUT:
                value = new Long( result.getNrLinesOutput() );
                break;
              case LINES_UPDATED:
                value = new Long( result.getNrLinesUpdated() );
                break;
              case LINES_REJECTED:
                value = new Long( result.getNrLinesRejected() );
                break;
              case ERRORS:
                value = new Long( result.getNrErrors() );
                break;
              case STARTDATE:
                value = trans.getStartDate();
                break;
              case LOGDATE:
                value = trans.getLogDate();
                break;
              case ENDDATE:
                value = trans.getEndDate();
                break;
              case DEPDATE:
                value = trans.getDepDate();
                break;
              case REPLAYDATE:
                value = trans.getCurrentDate();
                break;
              case LOG_FIELD:
                value = getLogBuffer( trans, trans.getLogChannelId(), status, logSizeLimit );
                break;
              case EXECUTING_SERVER:
                value = trans.getExecutingServer();
                break;
              case EXECUTING_USER:
                value = trans.getExecutingUser();
                break;
              case CLIENT:
                value =
                  KettleClientEnvironment.getInstance().getClient() != null ? KettleClientEnvironment
                    .getInstance().getClient().toString() : "unknown";
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
    return "TRANS";
  }

  public String getLogTableType() {
    return BaseMessages.getString( PKG, "TransLogTable.Type.Description" );
  }

  public String getConnectionNameVariable() {
    return Const.KETTLE_TRANS_LOG_DB;
  }

  public String getSchemaNameVariable() {
    return Const.KETTLE_TRANS_LOG_SCHEMA;
  }

  public String getTableNameVariable() {
    return Const.KETTLE_TRANS_LOG_TABLE;
  }

  public List<RowMetaInterface> getRecommendedIndexes() {
    List<RowMetaInterface> indexes = new ArrayList<RowMetaInterface>();

    // First index : ID_BATCH if any is used.
    //
    if ( isBatchIdUsed() ) {
      RowMetaInterface batchIndex = new RowMeta();
      LogTableField keyField = getKeyField();

      ValueMetaInterface keyMeta = new ValueMetaBase( keyField.getFieldName(), keyField.getDataType() );
      keyMeta.setLength( keyField.getLength() );
      batchIndex.addValueMeta( keyMeta );

      indexes.add( batchIndex );
    }

    // The next index includes : ERRORS, STATUS, TRANSNAME:

    RowMetaInterface lookupIndex = new RowMeta();
    LogTableField errorsField = findField( ID.ERRORS );
    if ( errorsField != null ) {
      ValueMetaInterface valueMeta = new ValueMetaBase( errorsField.getFieldName(), errorsField.getDataType() );
      valueMeta.setLength( errorsField.getLength() );
      lookupIndex.addValueMeta( valueMeta );
    }
    LogTableField statusField = findField( ID.STATUS );
    if ( statusField != null ) {
      ValueMetaInterface valueMeta = new ValueMetaBase( statusField.getFieldName(), statusField.getDataType() );
      valueMeta.setLength( statusField.getLength() );
      lookupIndex.addValueMeta( valueMeta );
    }
    LogTableField transNameField = findField( ID.TRANSNAME );
    if ( transNameField != null ) {
      ValueMetaInterface valueMeta = new ValueMetaBase( transNameField.getFieldName(), transNameField.getDataType() );
      valueMeta.setLength( transNameField.getLength() );
      lookupIndex.addValueMeta( valueMeta );
    }

    indexes.add( lookupIndex );

    return indexes;
  }

  @Override
  public void setAllGlobalParametersToNull()  {
    boolean clearGlobalVariables = Boolean.valueOf( System.getProperties().getProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "false" ) );
    if ( clearGlobalVariables ) {
      super.setAllGlobalParametersToNull();

      logInterval = isGlobalParameter( logInterval ) ? null : logInterval;
      logSizeLimit = isGlobalParameter( logSizeLimit ) ? null : logSizeLimit;
    }
  }
}
