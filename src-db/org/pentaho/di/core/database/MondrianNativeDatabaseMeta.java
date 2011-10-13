/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.core.database;

import org.pentaho.di.core.row.ValueMetaInterface;

public class MondrianNativeDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

	public String[] getUsedLibraries() {
		//return new String[] { "mysql-connector-java-3.1.14-bin.jar" };
		return null;
	}

	public String getDriverClass() {
		return "mondrian.olap4j.MondrianOlap4jDriver";
	}

	public int[] getAccessTypeList() {
		return new int[] { DatabaseMeta.TYPE_ACCESS_JNDI };
	}

	public String getURL(String hostname, String port, String databaseName) {
		//jdbc:mondrian:Datasource=jdbc/SampleData;Catalog=./foodmart/FoodMart.xml;
		return "jdbc:mondrian:Datasource=jdbc/" + databaseName + ";Catalog=" + hostname;
	}

	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
		return null;
	}

	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
		return null;
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr) {
		return null;
	}

}
