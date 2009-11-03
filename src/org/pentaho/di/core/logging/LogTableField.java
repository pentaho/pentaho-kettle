package org.pentaho.di.core.logging;


/**
 * This is a single log table field.<br>
 * The user can select this field or not, sees a field name, a description in the UI too.
 * The user can also specify a subject like a step name.
 * 
 * @author matt
 *
 */
public class LogTableField implements Cloneable {
	private String  id;
	private boolean enabled;
	private String  fieldName;
	private Object  subject;
	private String  description;
	private int     dataType;
	private int     length;
	private boolean subjectAllowed;
	private boolean key;

	/**
	 * @param id the ID to reference this field by in the log table
	 * @param enabled
	 * @param fieldName
	 * @param subject
	 * @param description
	 */
	public LogTableField(String id, boolean enabled, String fieldName, Object subject, String description) {
		this.id = id;
		this.enabled = enabled;
		this.fieldName = fieldName;
		this.subject = subject;
		this.description = description;
		this.subjectAllowed = true;
	}

	/**
	 * @param id
	 * @param enabled
	 * @param fieldName
	 * @param description
	 * @param dataType
	 * @param length
	 */
	public LogTableField(String id, boolean enabled, boolean subjectAllowed, String fieldName, String description, int dataType, int length) {
		this.id = id;
		this.enabled = enabled;
		this.subjectAllowed = subjectAllowed;
		this.fieldName = fieldName;
		this.description = description;
		this.dataType = dataType;
		this.length = length;
	}

	/**
	 * Create a new enabled log table field with the specified field name for the specified subject.
	 * 
	 * @param fieldname
	 * @param subject
	 */
	public LogTableField(String id, String fieldName, Object subject) {
		this.id = id;
		this.enabled = true;
		this.fieldName = fieldName;
		this.subject = subject;
		this.subjectAllowed = true;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return id;
	}
	
	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the subject
	 */
	public Object getSubject() {
		return subject;
	}
	
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Object subject) {
		this.subject = subject;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return the subjectAllowed
	 */
	public boolean isSubjectAllowed() {
		return subjectAllowed;
	}

	/**
	 * @param subjectAllowed the subjectAllowed to set
	 */
	public void setSubjectAllowed(boolean subjectAllowed) {
		this.subjectAllowed = subjectAllowed;
	}

	/**
	 * @return the key
	 */
	public boolean isKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(boolean key) {
		this.key = key;
	}	
}
