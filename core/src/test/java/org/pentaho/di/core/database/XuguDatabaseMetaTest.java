/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class XuguDatabaseMetaTest {
	private XuguDatabaseMeta nativeMeta, odbcMeta, jndiMeta;

	@Before
	public void setupOnce() throws Exception {
		nativeMeta = new XuguDatabaseMeta();
		odbcMeta = new XuguDatabaseMeta();
		jndiMeta = new XuguDatabaseMeta();
		nativeMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
		odbcMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_ODBC);
		jndiMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
		KettleClientEnvironment.init();
	}

	@Test
	public void testOverriddenSettings() throws Exception {
		// Tests the settings of the Oracle Database Meta
		// according to the features of the DB as we know them

		assertEquals(5138, nativeMeta.getDefaultDatabasePort());
		assertEquals(-1, odbcMeta.getDefaultDatabasePort());
		assertTrue(nativeMeta.supportsAutoInc());
		assertEquals("com.xugu.cloudjdbc.Driver", nativeMeta.getDriverClass());
		assertEquals("sun.jdbc.odbc.JdbcOdbcDriver", odbcMeta.getDriverClass());
		assertEquals("jdbc:odbc:xugu", odbcMeta.getURL(null, null, "xugu"));
		assertEquals("jdbc:xugu://localhost:5138/xugudb", nativeMeta.getURL("localhost", "5138", "xugudb"));
		assertEquals("jdbc:xugu://localhost:888/xugudb", nativeMeta.getURL("localhost", "888", "xugudb"));
		assertEquals("jdbc:xugu://localhost:65534/xugudb", nativeMeta.getURL("localhost", "65534", "xugudb"));
		assertEquals("jdbc:xugu://:5138/xugudb", nativeMeta.getURL("", "", "xugudb"));
		assertEquals("jdbc:xugu://:5138/", nativeMeta.getURL("", "", ""));

		assertFalse(nativeMeta.supportsSequences());
		assertEquals("&", nativeMeta.getExtraOptionSeparator());
		assertEquals("?", nativeMeta.getExtraOptionIndicator());
		assertTrue(nativeMeta.supportsTransactions());
		assertTrue(nativeMeta.supportsBitmapIndex());
		assertTrue(nativeMeta.supportsSynonyms());
		String[] reservedWords = new String[] { "ABORT", "ABOVE", "ABSOLUTE", "ACCESS", "ACCOUNT", "ACTION", "ADD",
				"AFTER", "AGGREGATE", "ALL", "ALTER", "ANALYSE", "ANALYZE", "AND", "ANY", "AOVERLAPS", "APPEND",
				"ARCHIVELOG", "ARE", "ARRAY", "AS", "ASC", "AT", "AUDIT", "AUDITOR", "AUTHID", "AUTHORIZATION", "AUTO",
				"BACKUP", "BACKWARD", "BADFILE", "BCONTAINS", "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BINTERSECTS",
				"BIT", "BLOCK", "BLOCKS", "BODY", "BOTH", "BOUND", "BOVERLAPS", "BREAK", "BUFFER_POOL", "BUILD", "BULK",
				"BWITHIN", "BY", "CACHE", "CALL", "CASCADE", "CASE", "CAST", "CATCH", "CATEGORY", "CHAIN", "CHAR",
				"CHARACTER", "CHARACTERISTICS", "CHECK", "CHECKPOINT", "CHUNK", "CLOSE", "CLUSTER", "COALESCE",
				"COLLATE", "COLLECT", "COLUMN", "COMMENT", "COMMIT", "COMMITTED", "COMPLETE", "COMPRESS", "COMPUTE",
				"CONNECT", "CONSTANT", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTEXT", "CONTINUE",
				"COPY", "CORRESPONDING", "CREATE", "CREATEDB", "CREATEUSER", "CROSS", "CROSSES", "CUBE", "CURRENT",
				"CURSOR", "CYCLE", "DATABASE", "DATAFILE", "DATE", "DATETIME", "DAY", "DBA", "DEALLOCATE", "DEC",
				"DECIMAL", "DECLARE", "DECODE", "DECRYPT", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DELIMITED",
				"DELIMITERS", "DEMAND", "DESC", "DESCRIBE", "DETERMINISTIC", "DIR", "DISABLE", "DISASSEMBLE",
				"DISCORDFILE", "DISJOINT", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DRIVEN", "DROP", "EACH", "ELEMENT",
				"ELSE", "ELSEIF", "ELSIF", "ENABLE", "ENCODING", "ENCRYPT", "ENCRYPTOR", "END", "ENDCASE", "ENDFOR",
				"ENDIF", "ENDLOOP", "EQUALS", "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCEPTIONS", "EXCLUSIVE",
				"EXEC", "EXECUTE", "EXISTS", "EXIT", "EXPIRE", "EXPLAIN", "EXPORT", "EXTEND", "EXTERNAL", "EXTRACT",
				"FALSE", "FAST", "FETCH", "FIELD", "FIELDS", "FILTER", "FINAL", "FINALLY", "FIRST", "FLOAT",
				"FOLLOWING", "FOR", "FORALL", "FORCE", "FOREIGN", "FORWARD", "FOUND", "FREELIST", "FREELISTS", "FROM",
				"FULL", "FUNCTION", "GENERATED", "GET", "GLOBAL", "GOTO", "GRANT", "GREATEST", "GROUP", "GROUPING",
				"GROUPS", "HANDLER", "HASH", "HAVING", "HEAP", "HIDE", "HOTSPOT", "HOUR", "IDENTIFIED", "IDENTIFIER",
				"IDENTITY", "IF", "ILIKE", "IMMEDIATE", "IMPORT", "IN", "INCLUDE", "INCREMENT", "INDEX", "INDEXTYPE",
				"INDICATOR", "INDICES", "INHERITS", "INIT", "INITIAL", "INITIALLY", "INITRANS", "INNER", "INOUT",
				"INSENSITIVE", "INSERT", "INSTANTIABLE", "INSTEAD", "INTERSECT", "INTERSECTS", "INTERVAL", "INTO", "IO",
				"IS", "ISNULL", "ISOLATION", "ISOPEN", "JOB", "JOIN", "K", "KEEP", "KEY", "KEYSET", "LABEL", "LANGUAGE",
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
		assertArrayEquals(reservedWords, nativeMeta.getReservedWords());
		assertArrayEquals(new String[] { "cloudjdbc.jar" }, nativeMeta.getUsedLibraries());
		assertTrue(nativeMeta.supportsPreparedStatementMetadataRetrieval());
		String quoteTest1 = "FOO 'BAR' \r TEST \n";
		String quoteTest2 = "FOO 'BAR' \\r TEST \\n";
		assertEquals("'FOO ''BAR'' \\r TEST \\n'", nativeMeta.quoteSQLString(quoteTest1));
		assertEquals("'FOO ''BAR'' \\r TEST \\n'", nativeMeta.quoteSQLString(quoteTest2));
		assertTrue(nativeMeta.isFetchSizeSupported());
		assertTrue(nativeMeta.releaseSavepoint());
		assertTrue(nativeMeta.supportsErrorHandlingOnBatchUpdates());
		assertFalse(nativeMeta.isDefaultingToUppercase());
	}

	@Test
	public void testGetExtraOptions() {
		XuguDatabaseMeta xgdbm = new XuguDatabaseMeta();
		xgdbm.setXuguExtraOptions();
		Map<String, String> map = xgdbm.getExtraOptions();
		assertEquals("2", map.get(xgdbm.getPluginId() + "." + "recv_mode"));
		assertFalse(Boolean.valueOf(map.get(xgdbm.getPluginId() + "." + "return_rowid")));
	}

	@Test
	public void testOverriddenSQLStatements() throws Exception {
		assertEquals(" WHERE ROWNUM <= 5", nativeMeta.getLimitClause(5));
		String reusedFieldsQuery = "SELECT * FROM FOO WHERE 1=0";
		assertEquals(reusedFieldsQuery, nativeMeta.getSQLQueryFields("FOO"));
		assertEquals(reusedFieldsQuery, nativeMeta.getSQLTableExists("FOO"));
		String reusedColumnsQuery = "SELECT FOO FROM BAR WHERE 1=0";
		assertEquals(reusedColumnsQuery, nativeMeta.getSQLQueryColumnFields("FOO", "BAR"));
		assertEquals(reusedColumnsQuery, nativeMeta.getSQLColumnExists("FOO", "BAR"));
		assertEquals("ALTER TABLE FOO ADD FOO DATETIME",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaTimestamp("FOO"), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO DATE",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaDate("FOO"), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO VARCHAR(15)",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaString("FOO", 15, 0), "", false, "", false));

		assertEquals("ALTER TABLE FOO ADD FOO SMALLINT",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaInteger("FOO", 3, 0), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO INTEGER",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaInteger("FOO", 7, 0), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO BIGINT",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaInteger("FOO", 15, 0), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO NUMERIC()",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaInteger("FOO", 20, 0), "", false, "", false));

		assertEquals("ALTER TABLE FOO ADD FOO FLOAT",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaBigNumber("FOO", 6, 10), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO DOUBLE",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaBigNumber("FOO", 15, 10), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO NUMERIC(17, 10)",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaBigNumber("FOO", 17, 10), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO NUMERIC(17, 10)",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaNumber("FOO", 17, 10), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO BLOB",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaBinary("FOO", 2048, 0), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO BOOLEAN",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaBoolean("FOO"), "", false, "", false));
		assertEquals("ALTER TABLE FOO ADD FOO  UNKNOWN",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaInternetAddress("FOO"), "", false, "", false));

		String lineSep = System.getProperty("line.separator");
		assertEquals("ALTER TABLE FOO DROP COLUMN BAR" + lineSep,
				nativeMeta.getDropColumnStatement("FOO", new ValueMetaString("BAR"), "", false, "", false));
		assertEquals("BEGIN EXECUTE IMMEDIATE 'DROP TABLE FOO'; END;", nativeMeta.getDropTableIfExistsStatement("FOO"));

	}

	@Test
	public void testGetFieldDefinition() {
		assertEquals("FOO DATE", nativeMeta.getFieldDefinition(new ValueMetaDate("FOO"), "", "", false, true, false));
		assertEquals("DATETIME",
				nativeMeta.getFieldDefinition(new ValueMetaTimestamp("FOO"), "", "", false, false, false));

		assertEquals("BOOLEAN",
				nativeMeta.getFieldDefinition(new ValueMetaBoolean("FOO"), "", "", false, false, false));

		assertEquals("FLOAT",
				nativeMeta.getFieldDefinition(new ValueMetaNumber("FOO", 5, 3), "", "", false, false, false));
		assertEquals("INTEGER",
				nativeMeta.getFieldDefinition(new ValueMetaBigNumber("FOO", 5, 0), "", "", false, false, false));
		assertEquals("BIGINT",
				nativeMeta.getFieldDefinition(new ValueMetaInteger("FOO", 17, 0), "", "", false, false, false));

		assertEquals("CLOB", nativeMeta.getFieldDefinition(new ValueMetaString("FOO", DatabaseMeta.CLOB_LENGTH, 0), "",
				"", false, false, false));
		assertEquals("VARCHAR(1)",
				nativeMeta.getFieldDefinition(new ValueMetaString("FOO", 1, 0), "", "", false, false, false));
		assertEquals("VARCHAR(15)",
				nativeMeta.getFieldDefinition(new ValueMetaString("FOO", 15, 0), "", "", false, false, false));
		assertEquals("VARCHAR", nativeMeta.getFieldDefinition(new ValueMetaString("FOO"), "", "", false, false, false));
		assertEquals("CLOB", nativeMeta.getFieldDefinition(
				new ValueMetaString("FOO", nativeMeta.getMaxVARCHARLength(), 0), "", "", false, false, false));
		assertEquals("CLOB", nativeMeta.getFieldDefinition(
				new ValueMetaString("FOO", nativeMeta.getMaxVARCHARLength() + 1, 0), "", "", false, false, false));

		assertEquals("BLOB",
				nativeMeta.getFieldDefinition(new ValueMetaBinary("FOO", 45, 0), "", "", false, false, false));

		assertEquals(" UNKNOWN",
				nativeMeta.getFieldDefinition(new ValueMetaInternetAddress("FOO"), "", "", false, false, false));

		assertEquals(" UNKNOWN" + System.getProperty("line.separator"),
				nativeMeta.getFieldDefinition(new ValueMetaInternetAddress("FOO"), "", "", false, false, true));

	}

	@Test
	public void testSupportsTimestampDataTypeIsTrue() throws Exception {
		nativeMeta.setSupportsTimestampDataType(true);
		assertEquals("DATETIME",
				nativeMeta.getFieldDefinition(new ValueMetaTimestamp("FOO"), "", "", false, false, false));
		assertEquals("ALTER TABLE FOO ADD FOO DATETIME",
				nativeMeta.getAddColumnStatement("FOO", new ValueMetaTimestamp("FOO"), "", false, "", false));
	}

}
