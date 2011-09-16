/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 */

package org.pentaho.di.jdbc;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class JDBCKettleMetaData implements java.sql.DatabaseMetaData {
	// private String[] columns;
	// private Object[] values;
  
	private transient final Log log = LogFactory
	.getLog(JDBCKettleMetaData.class);
	ConnectionJDBC3 connectionJDBC3;
	String url;
	Map<String, String[]> stepsMap = null;
	boolean isDir = false;
	KettleHelper helper = null;
	

	public JDBCKettleMetaData(ConnectionJDBC3 connectionJDBC3, String url) {
		this.connectionJDBC3 = connectionJDBC3;
		this.url = url;
		String kettleurl = url.substring(url
				.indexOf(JDBCKettleDriver.driverPrefix)
				+ JDBCKettleDriver.driverPrefix.length());
		URLParser p = new URLParser();
		p.parse(kettleurl);
		helper = new KettleHelper();
		String fileUrl = p.getKettleUrl();
		if (fileUrl.indexOf("file://") != -1) {
			fileUrl = fileUrl.substring(fileUrl.indexOf("file://") + 7);
		} else if (fileUrl.indexOf("file:///") != -1) {
			fileUrl = fileUrl.substring(fileUrl.indexOf("file://") + 8);
		}
		File f = new File(fileUrl);

		if (f.isDirectory()) {
			stepsMap = helper.visitDirectory(f);
			isDir = true;
		} else {
			stepsMap = helper.getSteps(f);
		}
	}

	public boolean allProceduresAreCallable() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allTablesAreSelectable() throws SQLException {

		return false;
	}

	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean deletesAreDetected(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet getAttributes(String catalog, String schemaPattern,
			String typeNamePattern, String attributeNamePattern)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getBestRowIdentifier(String catalog, String schema,
			String table, int scope, boolean nullable) throws SQLException {
		log.debug("getBestRowIdentifier.....");
		return null;
	}

	public String getCatalogSeparator() throws SQLException {

		return ".";
	}

	public String getCatalogTerm() throws SQLException {
		return "database";
	}

	public ResultSet getCatalogs() throws SQLException {
		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();
		// if USE_TRANSNAME_AS_SCHEMA is true, then we use the filename or
		// transformation name as the schema
		if (!isDir) {

			RowMetaAndData rd = new RowMetaAndData();
			rd.addValue("TABLE_CAT", ValueMetaInterface.TYPE_STRING,
					Constants.TABLE_TYPE_TABLE);
			rowAndDatas.add(rd);
			KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas,
					"*");
			return rs;
		}

		Set<String> set = this.stepsMap.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			RowMetaAndData rd = new RowMetaAndData();
			rd.addValue("TABLE_CAT", ValueMetaInterface.TYPE_STRING, name);
			rowAndDatas.add(rd);
		}
		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;
	}

	public ResultSet getColumnPrivileges(String catalog, String schema,
			String table, String columnNamePattern) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws SQLException {

		log.debug("catalog:" + catalog + " , schemaPattern:"
				+ schemaPattern + " , tableNamePattern:" + tableNamePattern
				+ " ,columnNamePattern:" + columnNamePattern);

		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();
		// if USE_TRANSNAME_AS_SCHEMA is true, then we use the filename or
		// transformation name as the schema
		if (!isDir) {

			log.debug(helper.getRowMeta(tableNamePattern));
			RowMeta rm = helper.getRowMeta(tableNamePattern);
			String[] columns = rm.getFieldNames();
			for (int i = 0; columns != null && i < columns.length; i++) {

				RowMetaAndData rd = new RowMetaAndData();
				rd.addValue("TABLE_CAT", ValueMetaInterface.TYPE_STRING,
						catalog);
				rd.addValue("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING,
						schemaPattern);
				rd.addValue("TABLE_NAME", ValueMetaInterface.TYPE_STRING,
						tableNamePattern);
				rd.addValue("COLUMN_NAME", ValueMetaInterface.TYPE_STRING,
						columns[i]);
				rd.addValue("DATA_TYPE", ValueMetaInterface.TYPE_INTEGER, "4");
				rd.addValue("TYPE_NAME", ValueMetaInterface.TYPE_STRING, "");
				rd.addValue("COLUMN_SIZE", ValueMetaInterface.TYPE_INTEGER,
						columns.length);
				rd.addValue("BUFFER_LENGTH", ValueMetaInterface.TYPE_INTEGER,
						"20");

				rd.addValue("DECIMAL_DIGITS", ValueMetaInterface.TYPE_INTEGER,
						"20");
				rd.addValue("NUM_PREC_RADIX", ValueMetaInterface.TYPE_INTEGER,
						"20");
				rd.addValue("NULLABLE", ValueMetaInterface.TYPE_INTEGER, "20");
				rd.addValue("REMARKS", ValueMetaInterface.TYPE_STRING, "");
				rd.addValue("COLUMN_DEF", ValueMetaInterface.TYPE_STRING, "");
				rd.addValue("SQL_DATA_TYPE", ValueMetaInterface.TYPE_INTEGER,
						"20");
				rd.addValue("SQL_DATETIME_SUB",
						ValueMetaInterface.TYPE_INTEGER, "20");
				rd.addValue("CHAR_OCTET_LENGTH",
						ValueMetaInterface.TYPE_INTEGER, "1");
				rd.addValue("ORDINAL_POSITION",
						ValueMetaInterface.TYPE_INTEGER, "20");
				rd.addValue("IS_NULLABLE", ValueMetaInterface.TYPE_STRING, "0");
				rd.addValue("SCOPE_CATALOG", ValueMetaInterface.TYPE_STRING,
						"0");
				rd
						.addValue("SCOPE_SCHEMA",
								ValueMetaInterface.TYPE_STRING, "0");
				rd.addValue("SCOPE_TABLE", ValueMetaInterface.TYPE_STRING, "0");
				rd.addValue("SOURCE_DATA_TYPE",
						ValueMetaInterface.TYPE_INTEGER, "1");
				rowAndDatas.add(rd);
			}
			KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas,
					"*");
			return rs;
		}

	
//		log.debug("getRowMeta:" + helper.getRowMeta(tableNamePattern));
		RowMeta rm = helper.getRowMeta(tableNamePattern);
		String[] columns = rm.getFieldNames();
		for (int i = 0; columns != null && i < columns.length; i++) {
			String name = columns[i];
			RowMetaAndData rd = new RowMetaAndData();
			rd.addValue("TABLE_CAT", ValueMetaInterface.TYPE_STRING, catalog);
			rd.addValue("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING,
					schemaPattern);
			rd.addValue("TABLE_NAME", ValueMetaInterface.TYPE_STRING, tableNamePattern);
			rd.addValue("COLUMN_NAME", ValueMetaInterface.TYPE_STRING, name);
			rd.addValue("DATA_TYPE", ValueMetaInterface.TYPE_INTEGER, "4");
			rd.addValue("TYPE_NAME", ValueMetaInterface.TYPE_STRING, "");
			rd.addValue("COLUMN_SIZE", ValueMetaInterface.TYPE_INTEGER,
					columns.length);
			rd.addValue("BUFFER_LENGTH", ValueMetaInterface.TYPE_INTEGER, name);
			rd
					.addValue("DECIMAL_DIGITS",
							ValueMetaInterface.TYPE_INTEGER, "20");
			rd
					.addValue("NUM_PREC_RADIX",
							ValueMetaInterface.TYPE_INTEGER, "20");
			rd.addValue("NULLABLE", ValueMetaInterface.TYPE_INTEGER, "20");
			rd.addValue("REMARKS", ValueMetaInterface.TYPE_STRING, name);
			rd.addValue("COLUMN_DEF", ValueMetaInterface.TYPE_STRING, name);
			rd.addValue("SQL_DATA_TYPE", ValueMetaInterface.TYPE_INTEGER, "20");
			rd.addValue("SQL_DATETIME_SUB", ValueMetaInterface.TYPE_INTEGER,
					"20");
			rd.addValue("CHAR_OCTET_LENGTH", ValueMetaInterface.TYPE_INTEGER,
					"1");
			rd.addValue("ORDINAL_POSITION", ValueMetaInterface.TYPE_INTEGER,
					"20");
			rd.addValue("IS_NULLABLE", ValueMetaInterface.TYPE_STRING, "0");
			rd.addValue("SCOPE_CATALOG", ValueMetaInterface.TYPE_STRING, "0");
			rd.addValue("SCOPE_SCHEMA", ValueMetaInterface.TYPE_STRING, "0");
			rd.addValue("SCOPE_TABLE", ValueMetaInterface.TYPE_STRING, "0");
			rd.addValue("SOURCE_DATA_TYPE", ValueMetaInterface.TYPE_INTEGER,
					"1");
			rowAndDatas.add(rd);

		}

		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;

	}

	public Connection getConnection() throws SQLException {

		return this.connectionJDBC3;
	}

	public ResultSet getCrossReference(String primaryCatalog,
			String primarySchema, String primaryTable, String foreignCatalog,
			String foreignSchema, String foreignTable) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDatabaseMajorVersion() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDatabaseMinorVersion() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getDatabaseProductName() throws SQLException {

		return "bayontechnologies.com";
	}

	public String getDatabaseProductVersion() throws SQLException {

		return this.connectionJDBC3.getDatabaseProductVersion();
	}

	public int getDefaultTransactionIsolation() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDriverMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDriverMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getDriverName() throws SQLException {

		return "jdbc for kettle";
	}

	public String getDriverVersion() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getExportedKeys(String catalog, String schema, String table)
			throws SQLException {
		log.debug("getExportedKeys.........");
		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();

		RowMetaAndData rd = new RowMetaAndData();
		rd.addValue("PKTABLE_CAT", ValueMetaInterface.TYPE_STRING, catalog);
		rd.addValue("PKTABLE_SCHEM", ValueMetaInterface.TYPE_STRING, schema);
		rd.addValue("PKTABLE_NAME", ValueMetaInterface.TYPE_STRING, table);
		rd.addValue("PKCOLUMN_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("FKTABLE_CAT", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("FKTABLE_SCHEM", ValueMetaInterface.TYPE_STRING, "");

		rd.addValue("FKTABLE_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("FKCOLUMN_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("KEY_SEQ", ValueMetaInterface.TYPE_INTEGER, "1");
		rd.addValue("UPDATE_RULE", ValueMetaInterface.TYPE_INTEGER, "1");
		rd.addValue("DELETE_RULE", ValueMetaInterface.TYPE_INTEGER, "1");
		rd.addValue("FK_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("PK_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("DEFERRABILITY", ValueMetaInterface.TYPE_STRING, "");
		rowAndDatas.add(rd);
		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;
	}

	public String getExtraNameCharacters() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIdentifierQuoteString() throws SQLException {

		return "\"";
	}

	public ResultSet getImportedKeys(String catalog, String schema, String table)
			throws SQLException {
		log.debug("getImportedKeys.........");
		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();

		RowMetaAndData rd = new RowMetaAndData();
		rd.addValue("PKTABLE_CAT", ValueMetaInterface.TYPE_STRING, catalog);
		rd.addValue("PKTABLE_SCHEM", ValueMetaInterface.TYPE_STRING, schema);
		rd.addValue("PKTABLE_NAME", ValueMetaInterface.TYPE_STRING, table);
		rd.addValue("PKCOLUMN_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("FKTABLE_CAT", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("FKTABLE_SCHEM", ValueMetaInterface.TYPE_STRING, "");

		rd.addValue("FKTABLE_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("FKCOLUMN_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("KEY_SEQ", ValueMetaInterface.TYPE_INTEGER, "1");
		rd.addValue("UPDATE_RULE", ValueMetaInterface.TYPE_INTEGER, "1");
		rd.addValue("DELETE_RULE", ValueMetaInterface.TYPE_INTEGER, "1");
		rd.addValue("FK_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("PK_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("DEFERRABILITY", ValueMetaInterface.TYPE_STRING, "");
		rowAndDatas.add(rd);
		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;
	}

	public ResultSet getIndexInfo(String catalog, String schema, String table,
			boolean unique, boolean approximate) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getJDBCMajorVersion() throws SQLException {

		return 1;
	}

	public int getJDBCMinorVersion() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxBinaryLiteralLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxCatalogNameLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxCharLiteralLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxColumnNameLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxColumnsInGroupBy() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxColumnsInIndex() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxColumnsInOrderBy() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxColumnsInSelect() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxColumnsInTable() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxConnections() throws SQLException {
		
		return 0;
	}

	public int getMaxCursorNameLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxIndexLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxProcedureNameLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxRowSize() throws SQLException {
		
		return 0;
	}

	public int getMaxSchemaNameLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxStatementLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxStatements() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxTableNameLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxTablesInSelect() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxUserNameLength() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getNumericFunctions() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getPrimaryKeys(String catalog, String schema, String table)
			throws SQLException {
		log.debug("getPrimaryKeys........");

		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();

		RowMetaAndData rd = new RowMetaAndData();
		rd.addValue("TABLE_CAT", ValueMetaInterface.TYPE_STRING, catalog);
		rd.addValue("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING, schema);
		rd.addValue("TABLE_NAME", ValueMetaInterface.TYPE_STRING, table);
		rd.addValue("COLUMN_NAME", ValueMetaInterface.TYPE_STRING, "");
		rd.addValue("KEY_SEQ", ValueMetaInterface.TYPE_INTEGER, "1");
		rd.addValue("PK_NAME", ValueMetaInterface.TYPE_STRING, "");
		rowAndDatas.add(rd);
		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;

	}

	public ResultSet getProcedureColumns(String catalog, String schemaPattern,
			String procedureNamePattern, String columnNamePattern)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProcedureTerm() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getProcedures(String catalog, String schemaPattern,
			String procedureNamePattern) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getResultSetHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSQLKeywords() throws SQLException {

		return "";
	}

	public int getSQLStateType() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSchemaTerm() throws SQLException {

		return "owner";
	}

	public ResultSet getSchemas() throws SQLException {
		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();
		// if USE_TRANSNAME_AS_SCHEMA is true, then we use the filename or
		// transformation name as the schema


		Set<String> set = this.stepsMap.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			RowMetaAndData rd = new RowMetaAndData();
			rd.addValue("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING, name);
			rowAndDatas.add(rd);
		}
		
		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;
	}

	public String getSearchStringEscape() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStringFunctions() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getSuperTables(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getSuperTypes(String catalog, String schemaPattern,
			String typeNamePattern) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSystemFunctions() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getTablePrivileges(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getTableTypes() throws SQLException {
		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();
		RowMetaAndData rd = new RowMetaAndData();
		rd.addValue("TABLE_TYPE", ValueMetaInterface.TYPE_STRING,
				Constants.TABLE_TYPE_TABLE);
		rowAndDatas.add(rd);
		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;
	}

	public ResultSet getTables(String catalog, String schemaPattern,
			String tableNamePattern, String[] types) throws SQLException {
		List<RowMetaAndData> rowAndDatas = new ArrayList<RowMetaAndData>();
		// get the steps from *.ktr or *.job
		log.debug("catalog:" + catalog + " , schemaPattern:"
				+ schemaPattern + " , tableNamePattern:" + tableNamePattern);
		if (!isDir) {
			Set<Map.Entry<String, String[]>> tables = this.stepsMap.entrySet();
			log.debug("tables:"+tables);
			for (Iterator<Map.Entry<String, String[]>> iterator = tables.iterator(); iterator.hasNext();) {
				Map.Entry<String, String[]> o = iterator.next();
				String[] values = (String[]) (o.getValue());
				for (int i = 0; i < values.length; i++) {
			RowMetaAndData rd = new RowMetaAndData();
			rd.addValue("TABLE_CAT", ValueMetaInterface.TYPE_STRING,
					"jdbckettle");
			rd.addValue("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING,
					"jdbckettle");
			rd.addValue("TABLE_NAME", ValueMetaInterface.TYPE_STRING,
					values[i]);

			rd.addValue("TABLE_TYPE", ValueMetaInterface.TYPE_STRING,
					Constants.TABLE_TYPE_TABLE);
			rd.addValue("REMARKS", ValueMetaInterface.TYPE_STRING, "");
			rd.addValue("TYPE_CAT", ValueMetaInterface.TYPE_STRING,
					Constants.TABLE_TYPE_TABLE);
			rd.addValue("TYPE_SCHEM", ValueMetaInterface.TYPE_STRING,
					Constants.TABLE_TYPE_TABLE);
			rd.addValue("TYPE_NAME", ValueMetaInterface.TYPE_STRING,
					Constants.TABLE_TYPE_TABLE);
			rd.addValue("SELF_REFERENCING_COL_NAME",
					ValueMetaInterface.TYPE_STRING, "");
			rd.addValue("REF_GENERATION", ValueMetaInterface.TYPE_STRING, "");
			rowAndDatas.add(rd);
				}
			}
		} else {
			Set<Map.Entry<String, String[]>> tables = this.stepsMap.entrySet();
			//for BIRT special schema
			
			boolean isBirtSchema = this.stepsMap.keySet().contains(schemaPattern);
			for (Iterator<Map.Entry<String, String[]>> iterator = tables.iterator(); iterator.hasNext();) {
				Map.Entry<String, String[]> o = iterator.next();
				String schema = o.getKey();
				
				if ((!schema.equals(schemaPattern))&&isBirtSchema) {
					continue;
				}
				String[] values = (String[]) (o.getValue());
//				log.debug("getTables:"
//						+ java.util.Arrays.toString(values));
				
				for (int i = 0; i < values.length; i++) {
					RowMetaAndData rd = new RowMetaAndData();
					rd.addValue("TABLE_CAT", ValueMetaInterface.TYPE_STRING,
							"jdbckettle");
					rd.addValue("TABLE_SCHEM", ValueMetaInterface.TYPE_STRING,
							"jdbckettle");
					rd.addValue("TABLE_NAME", ValueMetaInterface.TYPE_STRING,
							values[i]);

					rd.addValue("TABLE_TYPE", ValueMetaInterface.TYPE_STRING,
							Constants.TABLE_TYPE_TABLE);
					rd.addValue("REMARKS", ValueMetaInterface.TYPE_STRING, "");
					rd.addValue("TYPE_CAT", ValueMetaInterface.TYPE_STRING,
							Constants.TABLE_TYPE_TABLE);
					rd.addValue("TYPE_SCHEM", ValueMetaInterface.TYPE_STRING,
							Constants.TABLE_TYPE_TABLE);
					rd.addValue("TYPE_NAME", ValueMetaInterface.TYPE_STRING,
							Constants.TABLE_TYPE_TABLE);
					rd.addValue("SELF_REFERENCING_COL_NAME",
							ValueMetaInterface.TYPE_STRING, "");
					rd.addValue("REF_GENERATION",
							ValueMetaInterface.TYPE_STRING, "");
					rowAndDatas.add(rd);
				}

			}
		}
		KettleJDBCResultSet rs = new KettleJDBCResultSet(null, rowAndDatas, "*");
		return rs;
	}

	public String getTimeDateFunctions() throws SQLException {

		return null;
	}

	public ResultSet getTypeInfo() throws SQLException {

		return null;
	}

	public ResultSet getUDTs(String catalog, String schemaPattern,
			String typeNamePattern, int[] types) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getURL() throws SQLException {

		return connectionJDBC3.getURL();
	}

	public String getUserName() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getVersionColumns(String catalog, String schema,
			String table) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean insertsAreDetected(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCatalogAtStart() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReadOnly() throws SQLException {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean locatorsUpdateCopy() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean nullPlusNonNullIsNull() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean nullsAreSortedAtEnd() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean nullsAreSortedAtStart() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean nullsAreSortedHigh() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean nullsAreSortedLow() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean othersDeletesAreVisible(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean othersInsertsAreVisible(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ownDeletesAreVisible(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ownInsertsAreVisible(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean storesLowerCaseIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean storesMixedCaseIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean storesUpperCaseIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsANSI92FullSQL() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsBatchUpdates() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsColumnAliasing() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsConvert() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsConvert(int fromType, int toType)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsCoreSQLGrammar() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsCorrelatedSubqueries() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsDataDefinitionAndDataManipulationTransactions()
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsDataManipulationTransactionsOnly()
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsExpressionsInOrderBy() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsExtendedSQLGrammar() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsFullOuterJoins() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsGetGeneratedKeys() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsGroupBy() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsGroupByBeyondSelect() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsGroupByUnrelated() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsLikeEscapeClause() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsLimitedOuterJoins() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMinimumSQLGrammar() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMultipleOpenResults() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMultipleResultSets() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMultipleTransactions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsNamedParameters() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsNonNullableColumns() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsOrderByUnrelated() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsOuterJoins() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsPositionedDelete() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsPositionedUpdate() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsResultSetConcurrency(int type, int concurrency)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsResultSetHoldability(int holdability)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsResultSetType(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSavepoints() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSchemasInDataManipulation() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSelectForUpdate() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsStatementPooling() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsStoredProcedures() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSubqueriesInComparisons() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSubqueriesInExists() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSubqueriesInIns() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsTableCorrelationNames() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsTransactionIsolationLevel(int level)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsTransactions() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsUnion() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsUnionAll() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updatesAreDetected(int type) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean usesLocalFilePerTable() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean usesLocalFiles() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	String[] generateColumns() {
		return new String[] { "Year", "PresentsNickReceived",
				"PresentsRequested" };
	}

	Object[] generateValues() {
		String[] r1 = new String[] { "2003", "7", "4" };
		String[] r2 = new String[] { "2004", "9", "8" };
		return new Object[] { r1, r2 };
	}

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    return null;
  }

  public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
    return false;
  }

  public ResultSet getClientInfoProperties() throws SQLException {
    return null;
  }

  public ResultSet getFunctionColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
    return null;
  }

  public ResultSet getFunctions(String arg0, String arg1, String arg2) throws SQLException {
    return null;
  }

  public RowIdLifetime getRowIdLifetime() throws SQLException {
    return null;
  }

  public ResultSet getSchemas(String arg0, String arg1) throws SQLException {
    return null;
  }

  public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
    return false;
  }

}
