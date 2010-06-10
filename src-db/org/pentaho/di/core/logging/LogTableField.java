/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
	private String  name;
	private String  description;
	private int     dataType;
	private int     length;
	private boolean subjectAllowed;
	
	// Field indicators...
	//
	
	private boolean key;
	private boolean logDateField;
	private boolean logField;
	private boolean visible;
	private boolean statusField;
	private boolean errorsField;
	private boolean nameField;

	/**
	 * @param id the ID to reference this field by in the log table
	 * @param enabled
	 * @param fieldName
	 * @param subject
	 * @param description
	 */
	public LogTableField(String id, boolean enabled, String fieldName, Object subject, String name, String description) {
		this.id = id;
		this.enabled = enabled;
		this.fieldName = fieldName;
		this.subject = subject;
		this.name = name;
		this.description = description;
		this.subjectAllowed = true;
		this.visible=true;
	}

	/**
	 * @param id
	 * @param enabled
	 * @param fieldName
	 * @param description
	 * @param dataType
	 * @param length
	 */
	public LogTableField(String id, boolean enabled, boolean subjectAllowed, String fieldName, String name, String description, int dataType, int length) {
		this.id = id;
		this.enabled = enabled;
		this.subjectAllowed = subjectAllowed;
		this.fieldName = fieldName;
		this.name = name;
		this.description = description;
		this.dataType = dataType;
		this.length = length;
		this.visible=true;
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
		this.visible=true;
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

	/**
	 * @return the logDateField
	 */
	public boolean isLogDateField() {
		return logDateField;
	}

	/**
	 * @param logDateField the logDateField to set
	 */
	public void setLogDateField(boolean logDateField) {
		this.logDateField = logDateField;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the logField
	 */
	public boolean isLogField() {
		return logField;
	}

	/**
	 * @param logField the logField to set
	 */
	public void setLogField(boolean logField) {
		this.logField = logField;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the statusField
	 */
	public boolean isStatusField() {
		return statusField;
	}

	/**
	 * @param statusField the statusField to set
	 */
	public void setStatusField(boolean statusField) {
		this.statusField = statusField;
	}

	/**
	 * @return the errorsField
	 */
	public boolean isErrorsField() {
		return errorsField;
	}

	/**
	 * @param errorsField the errorsField to set
	 */
	public void setErrorsField(boolean errorsField) {
		this.errorsField = errorsField;
	}

	/**
	 * @return the nameField
	 */
	public boolean isNameField() {
		return nameField;
	}

	/**
	 * @param nameField the nameField to set
	 */
	public void setNameField(boolean nameField) {
		this.nameField = nameField;
	}	
}
