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

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;

/**
 * Contains SAP ERP system specific information through static final members 
 * 
 * @author Matt
 * @since  03-07-2005
 */

public class SAPR3DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
    public static final String ATTRIBUTE_SAP_LANGUAGE = "SAPLanguage";
    public static final String ATTRIBUTE_SAP_SYSTEM_NUMBER = "SAPSystemNumber";
    public static final String ATTRIBUTE_SAP_CLIENT = "SAPClient";
    
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN };
	}
	
	public int getDefaultDatabasePort()
	{
		return -1;
	}

	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	public String getDriverClass()
	{
			return null;
	}

    public String getURL(String hostname, String port, String databaseName)
    {
		return null;
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return false;
	}

	/**
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return null;
	}

	/**
	 * Generates the SQL statement to modify a column in the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to modify a column in the specified table
	 */
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return null;
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
	    return null;
	}
	
	public String [] getReservedWords()
	{
		return null;
	}
    
    public String[] getUsedLibraries()
    {
        return new String[] { };
    }
    
	public String getDatabaseFactoryName() {
		return EnvUtil.getSystemProperty(Const.KETTLE_SAP_CONNECTION_FACTORY, Const.KETTLE_SAP_CONNECTION_FACTORY_DEFAULT_NAME);
	}

	/**
	 * @return true if this is a relational database you can explore.
	 * Return false for SAP, PALO, etc.
	 */
	public boolean isExplorable() {
		return false;
	}

}
