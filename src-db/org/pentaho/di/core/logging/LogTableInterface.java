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

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryAttributeInterface;

public interface LogTableInterface {

	/**
	 * Saves the log table to a repository.
	 * @param attributeInterface The attribute interface used to store the attributes
	 */
	public void saveToRepository(RepositoryAttributeInterface attributeInterface) throws KettleException;

	/**
	 * Loads details of the log table from a repository.
	 * @param attributeInterface The attribute interface used to load the attributes
	 */
	public void loadFromRepository(RepositoryAttributeInterface attributeInterface) throws KettleException;

	public String getConnectionName();
	public void setConnectionName(String connectionName);
	
	public DatabaseMeta getDatabaseMeta();
	public List<LogTableField> getFields();
	public String getSchemaName();
	public String getTableName();
	
	/**
	 * Assemble the log record from the logging subject.
	 * 
	 * @param status The status to log
	 * @param subject The subject object to log
	 * @param parent The parent object to log
	 * @return The log record to write
	 */
	public RowMetaAndData getLogRecord(LogStatus status, Object subject, Object parent);
	
	public String getLogTableType();
	
	public String getConnectionNameVariable();
	
	public String getSchemaNameVariable();
	
	public String getTableNameVariable();
	
	public boolean isDefined();
	
	/**
	 * @return The string that describes the timeout in days (variable supported) as a floating point number
	 */
	public String getTimeoutInDays();
	
	/**
	 * @return the field that represents the log date field or null if none was defined.
	 */
	public LogTableField getLogDateField();
	
	/**
	 * @return the field that represents the key to this logging table (batch id etc)
	 */
	public LogTableField getKeyField();
	
	/**
	 * @return the appropriately quoted (by the database metadata) schema/table combination
	 */
	public String getQuotedSchemaTableCombination();
	
	/**
	 * @return the field that represents the logging text (or null if none is found)
	 */
	public LogTableField getLogField();
	
	/**
	 * @return the field that represents the status (or null if none is found)
	 */
	public LogTableField getStatusField();
	
	/**
	 * @return the field that represents the number of errors (or null if none is found)
	 */
	public LogTableField getErrorsField();

	/**
	 * @return the field that represents the name of the object that is being used (or null if none is found)
	 */
	public LogTableField getNameField();
}
