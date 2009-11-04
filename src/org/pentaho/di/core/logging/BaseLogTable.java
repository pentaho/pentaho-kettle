package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.xml.XMLHandler;

abstract class BaseLogTable {
	public static final String	XML_TAG	= "field";
	protected DatabaseMeta databaseMeta;
	protected String schemaName;
	protected String tableName;
	
	protected List<LogTableField> fields;

	public BaseLogTable(DatabaseMeta databaseMeta, String schemaName, String tableName) {
		this.databaseMeta = databaseMeta;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.fields = new ArrayList<LogTableField>();
	}
	
	/**
	 * @return the databaseMeta
	 */
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}

	/**
	 * @param databaseMeta the databaseMeta to set
	 */
	public void setDatabaseMeta(DatabaseMeta databaseMeta) {
		this.databaseMeta = databaseMeta;
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
	
	protected String getFieldsXML() {
		StringBuffer retval = new StringBuffer();
		
        for (LogTableField field : fields) {
            retval.append(XMLHandler.openTag(XML_TAG)); //$NON-NLS-1$
            
            retval.append(XMLHandler.addTagValue("enabled", field.isEnabled(), false)); //$NON-NLS-1$
            retval.append(XMLHandler.addTagValue("name", field.getFieldName(), false)); //$NON-NLS-1$
            if (field.isSubjectAllowed()) {
            	retval.append(XMLHandler.addTagValue("subject", field.getSubject()==null?null:field.getSubject().toString(), false)); //$NON-NLS-1$ 
            }
            
            retval.append(XMLHandler.closeTag(XML_TAG)); //$NON-NLS-1$ 
        }

		
		return retval.toString();
	}
	
	public boolean isDefined() {
		return databaseMeta!=null && !Const.isEmpty(tableName);
	}
}
