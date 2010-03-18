package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.w3c.dom.Node;

abstract class BaseLogTable {
	public static final String	XML_TAG	= "field";

	public static String	PROP_LOG_TABLE_CONNECTION_NAME	= "_LOG_TABLE_CONNECTION_NAME";
	public static String	PROP_LOG_TABLE_SCHEMA_NAME	= "_LOG_TABLE_SCHEMA_NAME";
	public static String	PROP_LOG_TABLE_TABLE_NAME	= "_LOG_TABLE_TABLE_NAME";

	public static String	PROP_LOG_TABLE_FIELD_ID	= "_LOG_TABLE_FIELD_ID";
	public static String	PROP_LOG_TABLE_FIELD_NAME	= "_LOG_TABLE_FIELD_NAME";
	public static String	PROP_LOG_TABLE_FIELD_ENABLED	= "_LOG_TABLE_FIELD_ENABLED";

	public static String	PROP_LOG_TABLE_INTERVAL	= "LOG_TABLE_INTERVAL";
	public static String	PROP_LOG_TABLE_SIZE_LIMIT	= "LOG_TABLE_SIZE_LIMIT";
	public static String	PROP_LOG_TABLE_TIMEOUT_DAYS	= "_LOG_TABLE_TIMEOUT_IN_DAYS";

	protected VariableSpace space;
	protected HasDatabasesInterface databasesInterface;;
	
	protected String connectionName;
	
	protected String schemaName;
	protected String tableName;
	protected String timeoutInDays;
	
	protected List<LogTableField> fields;

	public BaseLogTable(VariableSpace space, HasDatabasesInterface databasesInterface, String connectionName, String schemaName, String tableName) {
		this.space = space;
		this.databasesInterface = databasesInterface;
		this.connectionName = connectionName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.fields = new ArrayList<LogTableField>();
	}

	public String toString() {
		if (isDefined()) {
			return getDatabaseMeta().getName()+"-"+tableName;
		}
		return super.toString();
	}

	/**
	 * Save this core information of the log table to the repository using the specified attribute interface.
	 * 
	 * @param attributeInterface The attribute interface to use to set attributes
	 * @throws KettleException 
	 */
	public void saveToRepository(RepositoryAttributeInterface attributeInterface) throws KettleException {
		  
		attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_CONNECTION_NAME, getConnectionName());
		attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_SCHEMA_NAME, getSchemaName());
		attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_TABLE_NAME, getTableName());
		attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_TIMEOUT_DAYS, getTimeoutInDays());
		
		// Store the fields too...
		//
		for (int i=0;i<getFields().size();i++) {
			LogTableField field = getFields().get(i);
			attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_FIELD_ID+i, field.getId());
			attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_FIELD_NAME+i, field.getName());
			attributeInterface.setAttribute(getLogTableCode()+PROP_LOG_TABLE_FIELD_ENABLED+i, field.isEnabled());
		}
	}
	
	public void loadFromRepository(RepositoryAttributeInterface attributeInterface) throws KettleException {
		connectionName = attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_CONNECTION_NAME);
		schemaName = attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_SCHEMA_NAME);
		tableName = attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_TABLE_NAME);
		timeoutInDays = attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_TIMEOUT_DAYS);
		for (int i=0;i<getFields().size();i++) {
			String id = attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_FIELD_ID+i);
			LogTableField field = findField(id);
			field.setFieldName(attributeInterface.getAttributeString(getLogTableCode()+PROP_LOG_TABLE_FIELD_NAME+i));
			field.setEnabled(attributeInterface.getAttributeBoolean(getLogTableCode()+PROP_LOG_TABLE_FIELD_ENABLED+i));
		}
	}

	
	abstract String getLogTableCode();
	
	abstract String getConnectionNameVariable();
	
	abstract String getSchemaNameVariable();
	
	abstract String getTableNameVariable();
	
	/**
	 * @return the databaseMeta
	 */
	public DatabaseMeta getDatabaseMeta() {
		
		String name = space.environmentSubstitute(connectionName);
		if (Const.isEmpty(name)) {
			name = space.getVariable(getConnectionNameVariable());
		}
		if (Const.isEmpty(name)) return null;
		
		return databasesInterface.findDatabase(name);
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
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getQuotedSchemaTableCombination() {
		return getDatabaseMeta().getQuotedSchemaTableCombination(schemaName, tableName);
	}

	/**
	 * @return the fields
	 */
	public List<LogTableField> getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(List<LogTableField> fields) {
		this.fields = fields;
	}

	/**
	 * Find a log table field in this log table definition.
	 * Use the id of the field to do the lookup.
	 * @param id the id of the field to search for
	 * @return the log table field or null if nothing was found.
	 */
	public LogTableField findField(String id) {
		for (LogTableField field : fields) {
			if (field.getId().equals(id)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Get the subject of a field with the specified ID
	 * @param id
	 * @return the subject or null if no field could be find with the specified id
	 */
	public Object getSubject(String id) {
		LogTableField field = findField(id);
		if (field==null) return null;
		return field.getSubject();
	}

	/**
	 * Return the subject in the form of a string for the specified ID.
	 * @param id the id of the field to look for.
	 * @return the string of the subject (name of step) or null if nothing was found.
	 */
	public String getSubjectString(String id) {
		LogTableField field = findField(id);
		if (field==null) return null;
		if (field.getSubject()==null) return null;
		return field.getSubject().toString();
	}
	
	public boolean containsKeyField() {
		for (LogTableField field : fields) {
			if (field.isKey()) return true;
		}
		return false;
	}
	
	/**
	 * @return the field that represents the log date field or null if none was defined.
	 */
	public LogTableField getLogDateField() {
		for (LogTableField field : fields) {
			if (field.isLogDateField()) {
				return field;
			}
		}
		return null;
	}

	/**
	 * @return the field that represents the key to this logging table (batch id etc)
	 */
	public LogTableField getKeyField() {
		for (LogTableField field : fields) {
			if (field.isKey()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * @return the field that represents the logging text (or null if none is found)
	 */
	public LogTableField getLogField() {
		for (LogTableField field : fields) {
			if (field.isLogField()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * @return the field that represents the status (or null if none is found)
	 */
	public LogTableField getStatusField() {
		for (LogTableField field : fields) {
			if (field.isStatusField()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * @return the field that represents the number of errors (or null if none is found)
	 */
	public LogTableField getErrorsField() {
		for (LogTableField field : fields) {
			if (field.isErrorsField()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * @return the field that represents the name of the object that is being used (or null if none is found)
	 */
	public LogTableField getNameField() {
		for (LogTableField field : fields) {
			if (field.isNameField()) {
				return field;
			}
		}
		return null;
	}

	protected String getFieldsXML() {
		StringBuffer retval = new StringBuffer();
		
        for (LogTableField field : fields) {
            retval.append(XMLHandler.openTag(XML_TAG)); //$NON-NLS-1$
            
            retval.append(XMLHandler.addTagValue("id", field.getId(), false)); //$NON-NLS-1$
            retval.append(XMLHandler.addTagValue("enabled", field.isEnabled(), false)); //$NON-NLS-1$
            retval.append(XMLHandler.addTagValue("name", field.getFieldName(), false)); //$NON-NLS-1$
            if (field.isSubjectAllowed()) {
            	retval.append(XMLHandler.addTagValue("subject", field.getSubject()==null?null:field.getSubject().toString(), false)); //$NON-NLS-1$ 
            }
            
            retval.append(XMLHandler.closeTag(XML_TAG)); //$NON-NLS-1$ 
        }

		
		return retval.toString();
	}

	public void loadFieldsXML(Node node) {
		int nr = XMLHandler.countNodes(node, BaseLogTable.XML_TAG);
		for (int i=0;i<nr;i++) {
			Node fieldNode = XMLHandler.getSubNodeByNr(node, BaseLogTable.XML_TAG, i);
			String id = XMLHandler.getTagValue(fieldNode, "id") ;
			LogTableField field = findField(id);
			if (field==null) field = fields.get(i); // backward compatible until we go GA
			if (field!=null) {
				field.setFieldName( XMLHandler.getTagValue(fieldNode, "name") );
				field.setEnabled( "Y".equalsIgnoreCase(XMLHandler.getTagValue(fieldNode, "enabled")) );
			}
		}
	}
	
	public boolean isDefined() {
		return getDatabaseMeta()!=null && !Const.isEmpty(tableName);
	}

	/**
	 * @return the timeoutInDays
	 */
	public String getTimeoutInDays() {
		return timeoutInDays;
	}

	/**
	 * @param timeoutInDays the timeoutInDays to set
	 */
	public void setTimeoutInDays(String timeoutInDays) {
		this.timeoutInDays = timeoutInDays;
	}

	/**
	 * @return the connectionName
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * @param connectionName the connectionName to set
	 */
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

}
