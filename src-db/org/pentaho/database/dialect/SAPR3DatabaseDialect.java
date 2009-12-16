package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class SAPR3DatabaseDialect extends AbstractDatabaseDialect {

	public static final String			ATTRIBUTE_SAP_LANGUAGE		= "SAPLanguage";
	public static final String			ATTRIBUTE_SAP_SYSTEM_NUMBER	= "SAPSystemNumber";
	public static final String			ATTRIBUTE_SAP_CLIENT		= "SAPClient";

	public static final IDatabaseType DBTYPE = new DatabaseType(
			"SAP R/3 System", 
			"SAPR3", 
			DatabaseAccessType.getList(DatabaseAccessType.PLUGIN), 
			-1, 
			null
		);

	public IDatabaseType getDatabaseType() {
		return DBTYPE;
	}

	/**
	 * @return Whether or not the database can use auto increment type of fields
	 *         (pk)
	 */
	public boolean supportsAutoInc() {
		return false;
	}

	public String getDriverClass() {
		return null;
	}

	public String getURL(String hostname, String port, String databaseName) {
		return null;
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex() {
		return false;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms() {
		return false;
	}

	/**
	 * Generates the SQL statement to add a column to the specified table
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
		return null;
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
		return null;
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr) {
		return null;
	}

	public String[] getReservedWords() {
		return null;
	}

	public String[] getUsedLibraries() {
		return new String[] {};
	}

	public String getNativeDriver() {
		return null;
	}

	protected String getNativeJdbcPre() {
		return null;
	}

	public String getURL(IDatabaseConnection connection) throws KettleDatabaseException {
		return null;
	}
}
