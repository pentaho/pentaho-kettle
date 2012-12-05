/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.w3c.dom.Node;

/**
 * This class describes a job logging table
 * 
 * @author matt
 *
 */
public class CheckpointLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

	private static final String NAMESPACE_PARAMETER = "namespace_parameter";

  private static final String SAVE_RESULT_FILES = "save_result_files";

  private static final String SAVE_RESULT_ROWS = "save_result_rows";

  private static final String SAVE_PARAMETERS = "save_parameters";

  private static final String RUN_RETRY_PERIOD = "run_retry_period";

  private static final String MAX_NR_RETRIES = "max_nr_retries";

  private static Class<?> PKG = CheckpointLogTable.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String	XML_TAG	= "checkpoint-log-table";
	
	public enum ID {
		
		ID_JOB_RUN("ID_JOB_RUN"),
    ID_JOB("ID_JOB"),
		JOBNAME("JOBNAME"),
    NAMESPACE("NAMESPACE"),
    CHECKPOINT_NAME("CHECKPOINT_NAME"),
    CHECKPOINT_COPYNR("CHECKPOINT_COPYNR"),
		ATTEMPT_NR("ATTEMPT_NR"),
    LOGDATE("LOGDATE"),
    JOB_RUN_START_DATE("JOB_RUN_START_DATE"),
    RESULT_XML("RESULT_XML"),
    PARAMETER_XML("PARAMETER_XML"),
    ;
		
		private String id;
		private ID(String id) {
			this.id = id;
		}

		public String toString() {
			return id;
		}
	}
	
	private String maxNrRetries;
	private String runRetryPeriod;
  private String namespaceParameter;
  private String saveParameters="Y";
  private String saveResultRows="Y";
  private String saveResultFiles="Y";
	
	private CheckpointLogTable(VariableSpace space, HasDatabasesInterface databasesInterface) {
		super(space, databasesInterface, null, null, null);
	}
	
	@Override
	public Object clone() {
		try {
			CheckpointLogTable table = (CheckpointLogTable) super.clone();
			table.fields = new ArrayList<LogTableField>();
			for (LogTableField field : this.fields) {
				table.fields.add((LogTableField) field.clone());
			}
			return table;
		}
		catch(CloneNotSupportedException e) {
			return null;
		}
	}
	
  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append(XMLHandler.openTag(XML_TAG));
    retval.append(XMLHandler.addTagValue("connection", connectionName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    retval.append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue("timeout_days", timeoutInDays)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue(MAX_NR_RETRIES, maxNrRetries)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue(RUN_RETRY_PERIOD, runRetryPeriod)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue(NAMESPACE_PARAMETER, namespaceParameter)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue(SAVE_PARAMETERS, saveParameters)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue(SAVE_RESULT_ROWS, saveResultRows)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(XMLHandler.addTagValue(SAVE_RESULT_FILES, saveResultFiles)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append(super.getFieldsXML());
    retval.append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);

    return retval.toString();
  }

	public void loadXML(Node node, List<DatabaseMeta> databases) {
		connectionName = XMLHandler.getTagValue(node, "connection");
		schemaName = XMLHandler.getTagValue(node, "schema");
		tableName = XMLHandler.getTagValue(node, "table");
		timeoutInDays = XMLHandler.getTagValue(node, "timeout_days");
    maxNrRetries = XMLHandler.getTagValue(node, MAX_NR_RETRIES);
    runRetryPeriod = XMLHandler.getTagValue(node, RUN_RETRY_PERIOD);
    namespaceParameter = XMLHandler.getTagValue(node, NAMESPACE_PARAMETER);
    saveParameters = XMLHandler.getTagValue(node, SAVE_PARAMETERS);
    saveResultRows = XMLHandler.getTagValue(node, SAVE_RESULT_ROWS);
    saveResultFiles = XMLHandler.getTagValue(node, SAVE_RESULT_FILES);
		super.loadFieldsXML(node);
	}
	
	public void saveToRepository(RepositoryAttributeInterface attributeInterface) throws KettleException {
		super.saveToRepository(attributeInterface);
		
		// Also save the log interval and log size limit
		//
		attributeInterface.setAttribute(getLogTableCode()+MAX_NR_RETRIES, maxNrRetries);
    attributeInterface.setAttribute(getLogTableCode()+RUN_RETRY_PERIOD, runRetryPeriod);
    attributeInterface.setAttribute(getLogTableCode()+NAMESPACE_PARAMETER, namespaceParameter);
    attributeInterface.setAttribute(getLogTableCode()+SAVE_PARAMETERS, saveParameters);
    attributeInterface.setAttribute(getLogTableCode()+SAVE_RESULT_ROWS, saveResultRows);
    attributeInterface.setAttribute(getLogTableCode()+SAVE_RESULT_FILES, saveResultFiles);
	}

	public void loadFromRepository(RepositoryAttributeInterface attributeInterface) throws KettleException {
		super.loadFromRepository(attributeInterface);
		
		maxNrRetries = attributeInterface.getAttributeString(getLogTableCode()+MAX_NR_RETRIES);
		runRetryPeriod = attributeInterface.getAttributeString(getLogTableCode()+RUN_RETRY_PERIOD);
    namespaceParameter = attributeInterface.getAttributeString(getLogTableCode()+NAMESPACE_PARAMETER);
		saveParameters = attributeInterface.getAttributeString(getLogTableCode()+SAVE_PARAMETERS);
		saveResultRows = attributeInterface.getAttributeString(getLogTableCode()+SAVE_RESULT_ROWS);
		saveResultFiles = attributeInterface.getAttributeString(getLogTableCode()+SAVE_RESULT_FILES);
	}

	public static CheckpointLogTable getDefault(VariableSpace space, HasDatabasesInterface databasesInterface) {
		CheckpointLogTable table = new CheckpointLogTable(space, databasesInterface);

    table.fields.add( new LogTableField(ID.ID_JOB_RUN.id, true, false, "ID_JOB_RUN", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.JobRunID"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.JobRunID"), ValueMetaInterface.TYPE_INTEGER, 8) );
		table.fields.add( new LogTableField(ID.ID_JOB.id, true, false, "ID_JOB", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.JobID"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.JobID"), ValueMetaInterface.TYPE_INTEGER, 8) );
		table.fields.add( new LogTableField(ID.JOBNAME.id, true, false, "JOBNAME", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.JobName"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.JobName"), ValueMetaInterface.TYPE_STRING, 255) );
    table.fields.add( new LogTableField(ID.NAMESPACE.id, true, false, "NAMESPACE", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.Namespace"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.Namespace"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.CHECKPOINT_NAME.id, true, false, "CHECKPOINT_NAME", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.CheckpointName"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.CheckpointName"), ValueMetaInterface.TYPE_STRING, 255) );
    table.fields.add( new LogTableField(ID.CHECKPOINT_COPYNR.id, true, false, "CHECKPOINT_COPYNR", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.CheckpointCopyNr"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.CheckpointCopyNr"), ValueMetaInterface.TYPE_INTEGER, 4) );
    table.fields.add( new LogTableField(ID.ATTEMPT_NR.id, true, false, "ATTEMPT_NR", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.AttemptNr"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.AttemptNr"), ValueMetaInterface.TYPE_INTEGER, 8) );
    table.fields.add( new LogTableField(ID.JOB_RUN_START_DATE.id, true, false, "JOB_RUN_START_DATE", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.JobRunStartDate"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.JobRunStartDate"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.LOGDATE.id, true, false, "LOGDATE", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.LogDate"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.LogDate"), ValueMetaInterface.TYPE_DATE, -1) );
    table.fields.add( new LogTableField(ID.RESULT_XML.id, true, false, "RESULT_XML", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.ResultXml"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.ResultXml"), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH) );
    table.fields.add( new LogTableField(ID.PARAMETER_XML.id, true, false, "PARAMETER_XML", BaseMessages.getString(PKG, "CheckpointLogTable.FieldName.ParameterXml"), BaseMessages.getString(PKG, "CheckpointLogTable.FieldDescription.ParameterXml"), ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH) );

		table.findField(ID.ID_JOB_RUN).setKey(true);
		table.findField(ID.LOGDATE).setLogDateField(true);
		table.findField(ID.JOBNAME).setNameField(true);

		return table;
	}
	
	public LogTableField findField(ID id) {
		return super.findField(id.id);
	}
	
	public LogTableField getNamespaceField() {
		return findField(ID.NAMESPACE);
	}

  public LogTableField getJobRunStartDateField() {
    return findField(ID.JOB_RUN_START_DATE);
  }

  public LogTableField getCheckpointNameField() {
    return findField(ID.CHECKPOINT_NAME);
  }

  public LogTableField getCheckpointCopyNrField() {
    return findField(ID.CHECKPOINT_COPYNR);
  }
  
  public LogTableField getAttemptNrField() {
    return findField(ID.ATTEMPT_NR);
  }

  public LogTableField getResultXmlField() {
    return findField(ID.RESULT_XML);
  }

  public LogTableField getParameterXmlField() {
    return findField(ID.PARAMETER_XML);
  }

	public void setLogFieldUsed(boolean use) {
	}

	public boolean isLogFieldUsed() {
		return false;
	}
	
	/**
	 * This method calculates all the values that are required
	 * @param id the id to use or -1 if no id is needed
	 * @param status the log status to use
	 * @throws KettleException in case there was an unknown parameter being used. 
	 */
	public RowMetaAndData getLogRecord(LogStatus status, Object subject, Object parent) throws KettleException {
		if ((parent==null || (parent instanceof Job)) && (subject==null || (subject instanceof Result))) {
			Job job = (Job) parent;
			Result result = (Result)subject;
			
			RowMetaAndData row = new RowMetaAndData();
			
			for (LogTableField field : fields) {
				if (field.isEnabled()) {
					Object value = null;
					if (job!=null) {
						
						switch(ID.valueOf(field.getId())) {
            case ID_JOB_RUN : value = job==null ? Long.valueOf(0) : new Long(job.getRunId()); break;
						case ID_JOB : value = job==null ? Long.valueOf(-1) : new Long(job.getBatchId()); break;
						case JOBNAME : value = job==null ? null : job.getJobname(); break;
            case NAMESPACE: 
              {
                try {
                  if (Const.isEmpty(namespaceParameter)) {
                    value = "-";
                  } else {
                    value = Const.NVL(job.getParameterValue(namespaceParameter), "-");
                  }
                } catch(UnknownParamException e) {
                  throw new KettleException(BaseMessages.getString(PKG, "CheckpointLogTable.Exception.ParameterNotFound"), e);
                }
                
              }
              break;
              
            case CHECKPOINT_NAME : value = job==null || job.getCheckpointJobEntry()==null ? null : job.getCheckpointJobEntry().getName(); break;
            case CHECKPOINT_COPYNR : value = job==null || job.getCheckpointJobEntry()==null ? null : Long.valueOf(job.getCheckpointJobEntry().getNr()); break;
            case ATTEMPT_NR: value = job==null ? Long.valueOf(0) : Long.valueOf( job.getRunAttemptNr() ); break;
						case LOGDATE: value = job==null ? null : job.getLogDate(); break;
            case JOB_RUN_START_DATE: value = job==null ? null : job.getRunStartDate(); break;
						case RESULT_XML: value = result!=null ? result.getXML() : null; break;
            case PARAMETER_XML: value = job==null ? null : getParametersXml(job); break;
						}
					}

					row.addValue(field.getFieldName(), field.getDataType(), value);
					row.getRowMeta().getValueMeta(row.size()-1).setLength(field.getLength());
				}
			}
				
			return row;
		}
		else {
			return null;
		}
	}

	private String getParametersXml(Job job) {
	  try {
  	  StringBuilder xml = new StringBuilder();
  	  xml.append(XMLHandler.openTag("pars"));
  	  for (String parameter: job.listParameters()) {
  	    String value = job.getParameterValue(parameter);
  	    xml.append(XMLHandler.openTag("par"));
        xml.append(XMLHandler.addTagValue("name", parameter, false));
  	    xml.append(XMLHandler.addTagValue("value", value, false));
  	    xml.append(XMLHandler.closeTag("par"));
  	  }
      xml.append(XMLHandler.closeTag("pars"));
      return xml.toString();
	  } catch(Exception e) {
	    throw new RuntimeException("Unexpected error encoding parameters to XML", e);
	  }
  }

  public String getLogTableCode() {
		return "CHECKPOINT";
	}

	public String getLogTableType() {
		return BaseMessages.getString(PKG, "CheckpointLogTable.Type.Description");
	}

	public String getConnectionNameVariable() {
		return Const.KETTLE_CHECKPOINT_LOG_DB;
	}

	public String getSchemaNameVariable() {
		return Const.KETTLE_CHECKPOINT_LOG_SCHEMA;
	}

	public String getTableNameVariable() {
		return Const.KETTLE_CHECKPOINT_LOG_TABLE;
	}

  public List<RowMetaInterface> getRecommendedIndexes() {
    List<RowMetaInterface> indexes = new ArrayList<RowMetaInterface>();

    // First index : ID_JOB_RUN
    //
    RowMetaInterface pkIndex = new RowMeta();
    LogTableField keyField = getKeyField();

    ValueMetaInterface keyMeta = new ValueMeta(keyField.getFieldName(), keyField.getDataType());
    keyMeta.setLength(keyField.getLength());
    pkIndex.addValueMeta(keyMeta);

    indexes.add(pkIndex);

    // The next index includes : JOBNAME, NAMESPACE:
    //
    RowMetaInterface lookupIndex = new RowMeta();
    LogTableField jobNameField = findField(ID.JOBNAME);
    if (jobNameField != null) {
      ValueMetaInterface valueMeta = new ValueMeta(jobNameField.getFieldName(), jobNameField.getDataType());
      valueMeta.setLength(jobNameField.getLength());
      lookupIndex.addValueMeta(valueMeta);
    }
    LogTableField namespaceField = findField(ID.NAMESPACE);
    if (namespaceField != null) {
      ValueMetaInterface valueMeta = new ValueMeta(namespaceField.getFieldName(), namespaceField.getDataType());
      valueMeta.setLength(namespaceField.getLength());
      lookupIndex.addValueMeta(valueMeta);
    }

    indexes.add(lookupIndex);

    return indexes;
  }

  /**
   * @return the maxNrRetries
   */
  public String getMaxNrRetries() {
    return maxNrRetries;
  }

  /**
   * @param maxNrRetries the maxNrRetries to set
   */
  public void setMaxNrRetries(String maxNrRetries) {
    this.maxNrRetries = maxNrRetries;
  }

  /**
   * @return the runRetryPeriod
   */
  public String getRunRetryPeriod() {
    return runRetryPeriod;
  }

  /**
   * @param runRetryPeriod the runRetryPeriod to set
   */
  public void setRunRetryPeriod(String runRetryPeriod) {
    this.runRetryPeriod = runRetryPeriod;
  }

  /**
   * @return the saveParameters
   */
  public String getSaveParameters() {
    return saveParameters;
  }

  /**
   * @param saveParameters the saveParameters to set
   */
  public void setSaveParameters(String saveParameters) {
    this.saveParameters = saveParameters;
  }

  /**
   * @return the saveResultRows
   */
  public String getSaveResultRows() {
    return saveResultRows;
  }

  /**
   * @param saveResultRows the saveResultRows to set
   */
  public void setSaveResultRows(String saveResultRows) {
    this.saveResultRows = saveResultRows;
  }

  /**
   * @return the saveResultFiles
   */
  public String getSaveResultFiles() {
    return saveResultFiles;
  }

  /**
   * @param saveResultFiles the saveResultFiles to set
   */
  public void setSaveResultFiles(String saveResultFiles) {
    this.saveResultFiles = saveResultFiles;
  }

  /**
   * @return the namespaceParameter
   */
  public String getNamespaceParameter() {
    return namespaceParameter;
  }

  /**
   * @param namespaceParameter the namespaceParameter to set
   */
  public void setNamespaceParameter(String namespaceParameter) {
    this.namespaceParameter = namespaceParameter;
  }
}
