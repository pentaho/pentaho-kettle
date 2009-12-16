package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

public class TeradataDatabaseDialect extends AbstractDatabaseDialect {

	public static final IDatabaseType	DBTYPE	= new DatabaseType("Teradata", "TERADATA", DatabaseAccessType.getList(DatabaseAccessType.NATIVE, DatabaseAccessType.ODBC, DatabaseAccessType.JNDI), -1, null // No
																																																				// known
																																																				// help
																																																				// page
																																																				// at
																																																				// this
																																																				// time
												);

	public IDatabaseType getDatabaseType() {
		return DBTYPE;
	}

	public String getNativeDriver() {
		return "com.teradata.jdbc.TeraDriver";
	}

	@Override
	protected String getNativeJdbcPre() {
		return "jdbc:teradata://";
	}

	public String getURL(IDatabaseConnection connection) {
		if (connection.getAccessType() == DatabaseAccessType.ODBC) {
			return "jdbc:odbc:" + connection.getDatabaseName();
		} else {
			if (Const.isEmpty(connection.getDatabasePort())) {
				return getNativeJdbcPre() + connection.getHostname() + "/" + connection.getDatabaseName();
			} else {
				return getNativeJdbcPre() + connection.getHostname() + ":" + connection.getDatabasePort() + "/" + connection.getDatabaseName();
			}
		}
	}

	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
	 */
	@Override
	public int getNotFoundTK(boolean use_autoinc) {
		if (supportsAutoInc() && use_autoinc) {
			return 1;
		}
		return super.getNotFoundTK(use_autoinc);
	}

	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC
	 * driver...
	 * 
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported() {
		return false;
	}

	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String,
	 *      java.lang.String)
	 */
	public String getSchemaTableCombination(String schema_name, String table_part) {
		return "\"" + schema_name + "\".\"" + table_part + "\"";
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex() {
		return false;
	}

	/**
	 * @return true if Kettle can create a repository on this type of database.
	 */
	public boolean supportsRepository() {
		return true;
	}

	@Override
	public String getSQLTableExists(String tablename) {
		return "show table " + tablename;
	}

	public String getSQLColumnExists(String columnname, String tablename) {
		return "SELECT * FROM DBC.columns WHERE tablename =" + tablename + " AND columnname =" + columnname;
	}

	/**
	 * @param tableName
	 *            The table to be truncated.
	 * @return The SQL statement to truncate a table: remove all rows from it
	 *         without a transaction
	 */
	public String getTruncateTableStatement(String tableName) {
		return "DELETE FROM " + tableName;
	}

	/**
	 * Generates the SQL statement to add a column to the specified table For
	 * this generic type, i set it to the most common possibility.
	 * 
	 * @param tablename
	 *            The table to add
	 * @param v
	 *            The column defined as a value
	 * @param tk
	 *            the name of the technical key field
	 * @param use_autoinc
	 *            whether or not this field uses auto increment
	 * @param pk
	 *            the name of the primary key field
	 * @param semicolon
	 *            whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
		return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	/**
	 * Generates the SQL statement to modify a column in the specified table
	 * 
	 * @param tablename
	 *            The table to add
	 * @param v
	 *            The column defined as a value
	 * @param tk
	 *            the name of the technical key field
	 * @param use_autoinc
	 *            whether or not this field uses auto increment
	 * @param pk
	 *            the name of the primary key field
	 * @param semicolon
	 *            whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to modify a column in the specified table
	 */
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
		return "ALTER TABLE " + tablename + " MODIFY " + getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr) {
		String retval = "";

		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();

		if (add_fieldname)
			retval += fieldname + " ";

		int type = v.getType();
		switch (type) {
		case ValueMetaInterface.TYPE_DATE:
			retval += "TIMESTAMP";
			break;
		case ValueMetaInterface.TYPE_BOOLEAN:
			retval += "CHAR(1)";
			break;
		case ValueMetaInterface.TYPE_NUMBER:
		case ValueMetaInterface.TYPE_INTEGER:
		case ValueMetaInterface.TYPE_BIGNUMBER:
			if (fieldname.equalsIgnoreCase(tk) || // Technical key
					fieldname.equalsIgnoreCase(pk) // Primary key
			) {
				retval += "INTEGER"; // TERADATA has no Auto-increment
										// functionality nor Sequences!
			} else {
				if (length > 0) {
					if (precision > 0 || length > 9) {
						retval += "DECIMAL(" + length + ", " + precision + ")";
					} else {
						if (length > 5) {
							retval += "INTEGER";
						} else {
							if (length < 3) {
								retval += "BYTEINT";
							} else {
								retval += "SMALLINT";
							}
						}
					}

				} else {
					retval += "DOUBLE PRECISION";
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length > 64000) {
				retval += "CLOB";
			} else {
				retval += "VARCHAR";
				if (length > 0) {
					retval += "(" + length + ")";
				} else {
					retval += "(64000)"; // Maybe use some default DB String
											// length?
				}
			}
			break;
		default:
			retval += " UNKNOWN";
			break;
		}

		if (add_cr)
			retval += Const.CR;

		return retval;
	}

	public String getExtraOptionSeparator() {
		return ",";
	}

	public String getExtraOptionIndicator() {
		return "/";
	}

	public String getExtraOptionsHelpText() {
		return "http://www.info.ncr.com/eTeradata-BrowseBy-Results.cfm?pl=&PID=&title=%25&release=&kword=CJDBC&sbrn=7&nm=Teradata+Tools+and+Utilities+-+Java+Database+Connectivity+(JDBC)";
	}

	public String[] getUsedLibraries() {
		return new String[] { "terajdbc4.jar", "tdgssjava.jar" };
	}

	public int getDefaultDatabasePort() {
		return 1025;
	}

	/*
	 * TODO : figure this out!
	 * 
	 * Overrides parent behavior to allow
	 * <code>getDatabasePortNumberString</code> value to override value of
	 * <code>DBS_PORT</code> extra option.
	 * 
	 * public Map<String, String> getExtraOptions() { Map<String,String> map =
	 * getExtraOptions();
	 * 
	 * if (!Const.isEmpty(getDatabaseP())) { map.put(getDatabaseTypeDesc() +
	 * ".DBS_PORT", getDatabasePortNumberString()); }
	 * 
	 * return map; }
	 */

}
