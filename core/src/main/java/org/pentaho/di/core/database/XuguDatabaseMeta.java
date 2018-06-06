/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2010-2018 by Pentaho : http://www.pentaho.com
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;

/**
 * Contains xugu specific information through static final members
 * 
 * @author ouguan
 * @since 04-June-2018
 */

public class XuguDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
	@Override
	public int[] getAccessTypeList() {
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC,
				DatabaseMeta.TYPE_ACCESS_JNDI };
	}

	@Override
	public int getDefaultDatabasePort() {
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE)
			return 5138;
		return -1;
	}

	@Override
	public String getDriverClass() {
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		} else {
			return "com.xugu.cloudjdbc.Driver";
		}
	}

	/**
	 * get the extra properties when if it has. Returns the connection URL.
	 */
	@Override
	public String getURL(String hostname, String port, String databaseName) {
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
			return "jdbc:odbc:" + databaseName;
		} else if (getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE) {
			setXuguExtraOptions();
			if (Utils.isEmpty(port)) {
				return "jdbc:xugu://" + hostname + ":" + "5138" + "/" + databaseName;
			} else {
				return "jdbc:xugu://" + hostname + ":" + port + "/" + databaseName;
			}
		} else {
			return "jdbc:xugu://" + hostname + ":" + port + "/" + databaseName;
		}
	}

	public void setXuguExtraOptions() {

		try {

			String fileName = "launcher" + Const.FILE_SEPARATOR + getPluginName() + ".properties";
			File xuguOptionFile = new File(Const.FILE_SEPARATOR + fileName);
			
			if (!xuguOptionFile.exists()) {
				xuguOptionFile = new File(fileName);
			}

			if (xuguOptionFile.exists()) {

				Properties extraOption = new Properties();
				FileInputStream inputFile = new FileInputStream(fileName);
				extraOption.load(inputFile);
				Enumeration<?> enumVal = extraOption.propertyNames();
				while (enumVal.hasMoreElements()) {
					String strKey = (String) enumVal.nextElement();
					String strValue = extraOption.getProperty(strKey);
					addExtraOption(getPluginId(), strKey, strValue);
				}
				inputFile.close();

			} else {

				addExtraOption(getPluginId(), "recv_mode", "2");
				addExtraOption(getPluginId(), "return_rowid", String.valueOf(false));
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
	 */
	@Override
	public String getLimitClause(int nrRows) {
		return " WHERE ROWNUM <= " + nrRows;
	}

	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the
	 * resultset for a given database table
	 * 
	 * @param tableName
	 *            The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	@Override
	public String getSQLQueryFields(String tableName) {
		return "SELECT * FROM " + tableName + " WHERE 1=0";
	}

	@Override
	public String getSQLTableExists(String tablename) {
		return getSQLQueryFields(tablename);
	}

	@Override
	public String getSQLColumnExists(String columnname, String tablename) {
		return getSQLQueryColumnFields(columnname, tablename);
	}

	public String getSQLQueryColumnFields(String columnname, String tableName) {
		return "SELECT " + columnname + " FROM " + tableName + " WHERE 1=0";
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
	@Override
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
			String pk, boolean semicolon) {
		return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	/**
	 * Generates the SQL statement to drop a column from the specified table
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
	 * @return the SQL statement to drop a column from the specified table
	 */
	@Override
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
			String pk, boolean semicolon) {
		return "ALTER TABLE " + tablename + " DROP COLUMN " + v.getName() + Const.CR;
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
	@Override
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
			String pk, boolean semicolon) {
		return "ALTER TABLE " + tablename + " ALTER " + getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	/**
	 * 
	 * @param tablename
	 * @param v
	 * @param tk
	 * @param use_autoinc
	 * @param pk
	 * @param semicolon
	 * @return
	 */
	public String getRenameColumnStatement(String oldTablename, ValueMetaInterface v, String tk, boolean use_autoinc,
			String pk, boolean semicolon) {
		return "ALTER TABLE " + oldTablename + " RENAME TO" + v.getName() + Const.CR;
	}

	/**
	 * xugu does not support a construct like 'drop table if exists', which is
	 * apparently legal syntax in many other RDBMSs. So we need to implement the
	 * same behavior and avoid throwing 'table does not exist' exception.
	 *
	 * @param tableName
	 *            Name of the table to drop
	 * @return 'drop table if exists'-like statement for xugu
	 */
	@Override
	public String getDropTableIfExistsStatement(String tableName) {
		return "BEGIN EXECUTE IMMEDIATE 'DROP TABLE " + tableName + "'; END;";
	}

	@Override
	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
			boolean add_fieldname, boolean add_cr) {
		StringBuffer retval = new StringBuffer(128);

		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();

		if (add_fieldname)
			retval.append(fieldname + " ");

		int type = v.getType();
		switch (type) {
		case ValueMetaInterface.TYPE_DATE:
			retval.append("DATE");
			break;
		case ValueMetaInterface.TYPE_TIMESTAMP:
			retval.append("DATETIME");
			break;
		case ValueMetaInterface.TYPE_BOOLEAN:
			retval.append("BOOLEAN");
			break;
		case ValueMetaInterface.TYPE_NUMBER:
		case ValueMetaInterface.TYPE_INTEGER:
		case ValueMetaInterface.TYPE_BIGNUMBER:
			if (precision == 0) {
				if (length < 4) {
					// from -127-128
					retval.append("SMALLINT");
				} else if (length > 9) {

					if (length < 19) {
						// can hold signed values between -9223372036854775808
						// and 9223372036854775807
						// 18 significant digits
						retval.append("BIGINT");
					} else {
						retval.append("NUMERIC()");
					}
				} else {
					retval.append("INTEGER");
				}
			}
			// Floating point values...
			else {
				if (length < 7) {
					retval.append("FLOAT");
				} else if (length > 15) {
					retval.append("NUMERIC(" + length);
					if (precision > 0)
						retval.append(", " + precision);
					retval.append(")");
				} else {
					retval.append("DOUBLE");
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length >= DatabaseMeta.CLOB_LENGTH) {
				retval.append("CLOB");
			} else if (length > 0 && length < 2000) {
				retval.append("VARCHAR(" + length + ")");
			} else {
				if (length <= 0) {
					retval.append("VARCHAR"); // We don't know, so we just use
												// the variable char...
				} else {
					retval.append("CLOB");
				}
			}
			break;
		case ValueMetaInterface.TYPE_BINARY:
			retval.append("BLOB");
			break;
		default:
			retval.append(" UNKNOWN");
			break;
		}

		if (add_cr)
			retval.append(Const.CR);

		return retval.toString();
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
	 * @return The extra option separator in database URL for this platform (usually
	 *         this is semicolon ; )
	 */
	@Override
	public String getExtraOptionSeparator() {
		return "&";
	}

	/**
	 * @return This indicator separates the normal URL from the options
	 */
	@Override
	public String getExtraOptionIndicator() {
		return "?";
	}

	/**
	 * @return true if the database supports transactions.
	 */
	@Override
	public boolean supportsTransactions() {
		return true;
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	@Override
	public boolean supportsBitmapIndex() {
		return true;
	}

	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 *
	 * @return true is setFetchSize() is supported!
	 */
	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	@Override
	public boolean supportsSynonyms() {
		return true;
	}

	@Override
	public boolean supportsGetBlob() {
		return true;
	}
	
	/**
	 * Most databases allow you to retrieve result metadata by preparing a SELECT
	 * statement.
	 * 
	 * @return true if the database supports retrieval of query metadata from a
	 *         prepared statement. False if the query needs to be executed first.
	 */
	@Override
	public boolean supportsPreparedStatementMetadataRetrieval() {
		return true;
	}

	/**
	 * Returns a false as Oracle does not allow for the releasing of savepoints.
	 */
	@Override
	public boolean releaseSavepoint() {
		return true;
	}

	@Override
	public boolean supportsErrorHandlingOnBatchUpdates() {
		return true;
	}
	
	/**
	 * @return true if the database defaults to naming tables and fields in uppercase. True for most databases except for
	 *         stuborn stuff like Postgres ;-)
	 */
	@Override
	public boolean isDefaultingToUppercase() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
	 */
	@Override
	public String[] getReservedWords() {
		return new String[] { "ABORT", "ABOVE", "ABSOLUTE", "ACCESS", "ACCOUNT", "ACTION", "ADD", "AFTER", "AGGREGATE",
				"ALL", "ALTER", "ANALYSE", "ANALYZE", "AND", "ANY", "AOVERLAPS", "APPEND", "ARCHIVELOG", "ARE", "ARRAY",
				"AS", "ASC", "AT", "AUDIT", "AUDITOR", "AUTHID", "AUTHORIZATION", "AUTO", "BACKUP", "BACKWARD",
				"BADFILE", "BCONTAINS", "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BINTERSECTS", "BIT", "BLOCK", "BLOCKS",
				"BODY", "BOTH", "BOUND", "BOVERLAPS", "BREAK", "BUFFER_POOL", "BUILD", "BULK", "BWITHIN", "BY", "CACHE",
				"CALL", "CASCADE", "CASE", "CAST", "CATCH", "CATEGORY", "CHAIN", "CHAR", "CHARACTER", "CHARACTERISTICS",
				"CHECK", "CHECKPOINT", "CHUNK", "CLOSE", "CLUSTER", "COALESCE", "COLLATE", "COLLECT", "COLUMN",
				"COMMENT", "COMMIT", "COMMITTED", "COMPLETE", "COMPRESS", "COMPUTE", "CONNECT", "CONSTANT",
				"CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTEXT", "CONTINUE", "COPY", "CORRESPONDING",
				"CREATE", "CREATEDB", "CREATEUSER", "CROSS", "CROSSES", "CUBE", "CURRENT", "CURSOR", "CYCLE",
				"DATABASE", "DATAFILE", "DATE", "DATETIME", "DAY", "DBA", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE",
				"DECODE", "DECRYPT", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DELIMITED", "DELIMITERS", "DEMAND",
				"DESC", "DESCRIBE", "DETERMINISTIC", "DIR", "DISABLE", "DISASSEMBLE", "DISCORDFILE", "DISJOINT",
				"DISTINCT", "DO", "DOMAIN", "DOUBLE", "DRIVEN", "DROP", "EACH", "ELEMENT", "ELSE", "ELSEIF", "ELSIF",
				"ENABLE", "ENCODING", "ENCRYPT", "ENCRYPTOR", "END", "ENDCASE", "ENDFOR", "ENDIF", "ENDLOOP", "EQUALS",
				"ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCEPTIONS", "EXCLUSIVE", "EXEC", "EXECUTE", "EXISTS",
				"EXIT", "EXPIRE", "EXPLAIN", "EXPORT", "EXTEND", "EXTERNAL", "EXTRACT", "FALSE", "FAST", "FETCH",
				"FIELD", "FIELDS", "FILTER", "FINAL", "FINALLY", "FIRST", "FLOAT", "FOLLOWING", "FOR", "FORALL",
				"FORCE", "FOREIGN", "FORWARD", "FOUND", "FREELIST", "FREELISTS", "FROM", "FULL", "FUNCTION",
				"GENERATED", "GET", "GLOBAL", "GOTO", "GRANT", "GREATEST", "GROUP", "GROUPING", "GROUPS", "HANDLER",
				"HASH", "HAVING", "HEAP", "HIDE", "HOTSPOT", "HOUR", "IDENTIFIED", "IDENTIFIER", "IDENTITY", "IF",
				"ILIKE", "IMMEDIATE", "IMPORT", "IN", "INCLUDE", "INCREMENT", "INDEX", "INDEXTYPE", "INDICATOR",
				"INDICES", "INHERITS", "INIT", "INITIAL", "INITIALLY", "INITRANS", "INNER", "INOUT", "INSENSITIVE",
				"INSERT", "INSTANTIABLE", "INSTEAD", "INTERSECT", "INTERSECTS", "INTERVAL", "INTO", "IO", "IS",
				"ISNULL", "ISOLATION", "ISOPEN", "JOB", "JOIN", "K", "KEEP", "KEY", "KEYSET", "LABEL", "LANGUAGE",
				"LAST", "LEADING", "LEAST", "LEAVE", "LEFT", "LEFTOF", "LENGTH", "LESS", "LEVEL", "LEVELS", "LEXER",
				"LIBRARY", "LIKE", "LIMIT", "LINK", "LIST", "LISTEN", "LOAD", "LOB", "LOCAL", "LOCATION", "LOCATOR",
				"LOCK", "LOGFILE", "LOGGING", "LOGIN", "LOGOUT", "LOOP", "LOVERLAPS", "M", "MATCH", "MATERIALIZED",
				"MAX", "MAXEXTENTS", "MAXSIZE", "MAXTRANS", "MAXVALUE", "MAXVALUES", "MEMBER", "MEMORY", "MERGE",
				"MINEXTENTS", "MINUS", "MINUTE", "MINVALUE", "MISSING", "MODE", "MODIFY", "MONTH", "MOVEMENT", "NAME",
				"NAMES", "NATIONAL", "NATURAL", "NCHAR", "NESTED", "NEW", "NEWLINE", "NEXT", "NO", "NOARCHIVELOG",
				"NOAUDIT", "NOCACHE", "NOCOMPRESS", "NOCREATEDB", "NOCREATEUSER", "NOCYCLE", "NODE", "NOFORCE",
				"NOFOUND", "NOLOGGING", "NONE", "NOORDER", "NOPARALLEL", "NOT", "NOTFOUND", "NOTHING", "NOTIFY",
				"NOTNULL", "NOVALIDATE", "NOWAIT", "NULL", "NULLIF", "NULLS", "NUMBER", "NUMERIC", "NVARCHAR",
				"NVARCHAR2", "NVL", "NVL2", "OBJECT", "OF", "OFF", "OFFLINE", "OFFSET", "OIDINDEX", "OIDS", "OLD", "ON",
				"ONLINE", "ONLY", "OPEN", "OPERATOR", "OPTION", "OR", "ORDER", "ORGANIZATION", "OTHERVALUES", "OUT",
				"OUTER", "OVER", "OVERLAPS", "OWNER", "PACKAGE", "PARALLEL", "PARAMETERS", "PARTIAL", "PARTITION",
				"PARTITIONS", "PASSWORD", "PCTFREE", "PCTINCREASE", "PCTUSED", "PCTVERSION", "PERIOD", "POLICY",
				"PRAGMA", "PREBUILT", "PRECEDING", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIORITY",
				"PRIVILEGES", "PROCEDURAL", "PROCEDURE", "PROTECTED", "PUBLIC", "QUERY", "QUOTA", "RAISE", "RANGE",
				"RAW", "READ", "READS", "REBUILD", "RECOMPILE", "RECORD", "RECORDS", "RECYCLE", "REDUCED", "REF",
				"REFERENCES", "REFERENCING", "REFRESH", "REINDEX", "RELATIVE", "RENAME", "REPEATABLE", "REPLACE",
				"REPLICATION", "RESOURCE", "RESTART", "RESTORE", "RESTRICT", "RESULT", "RETURN", "RETURNING", "REVERSE",
				"REVOKE", "REWRITE", "RIGHT", "RIGHTOF", "ROLE", "ROLLBACK", "ROLLUP", "ROVERLAPS", "ROW", "ROWCOUNT",
				"ROWID", "ROWS", "ROWTYPE", "RULE", "RUN", "SAVEPOINT", "SCHEMA", "SCROLL", "SECOND", "SEGMENT",
				"SELECT", "SELF", "SEQUENCE", "SERIALIZABLE", "SESSION", "SET", "SETOF", "SETS", "SHARE", "SHOW",
				"SHUTDOWN", "SIBLINGS", "SIZE", "SLOW", "SNAPSHOT", "SOME", "SPATIAL", "SPLIT", "SSO", "STANDBY",
				"START", "STATEMENT", "STATIC", "STATISTICS", "STEP", "STOP", "STORAGE", "STORE", "STREAM",
				"SUBPARTITION", "SUBPARTITIONS", "SUBTYPE", "SUCCESSFUL", "SYNONYM", "SYSTEM", "TABLE", "TABLESPACE",
				"TEMP", "TEMPLATE", "TEMPORARY", "TERMINATED", "THAN", "THEN", "THROW", "TIME", "TIMESTAMP", "TO",
				"TOP", "TOPOVERLAPS", "TOUCHES", "TRACE", "TRAILING", "TRAN", "TRANSACTION", "TRIGGER", "TRUE",
				"TRUNCATE", "TRUSTED", "TRY", "TYPE", "UNBOUNDED", "UNDER", "UNDO", "UNIFORM", "UNION", "UNIQUE",
				"UNLIMITED", "UNLISTEN", "UNLOCK", "UNPROTECTED", "UNTIL", "UOVERLAPS", "UPDATE", "USE", "USER",
				"USING", "VACUUM", "VALID", "VALIDATE", "VALUE", "VALUES", "VARCHAR", "VARCHAR2", "VARRAY", "VARYING",
				"VERBOSE", "VERSION", "VIEW", "VOCABLE", "WAIT", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH", "WITHIN",
				"WITHOUT", "WORK", "WRITE", "XML", "YEAR", "ZONE" };
	}

	@Override
	public String[] getUsedLibraries() {
		return new String[] { "cloudjdbc.jar" };
	}

	/**
	 * @param string
	 * @return A string that is properly quoted for use in a SQL statement (insert,
	 *         update, delete, etc)
	 */
	@Override
	public String quoteSQLString(String string) {
		string = string.replaceAll("'", "''");
		string = string.replaceAll("\\n", "\\\\n");
		string = string.replaceAll("\\r", "\\\\r");
		return "'" + string + "'";
	}

}
