/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class PostgreSQLDatabaseMetaTest {
  PostgreSQLDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new PostgreSQLDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertEquals( "&", nativeMeta.getExtraOptionSeparator() );
    assertEquals( "?", nativeMeta.getExtraOptionIndicator() );
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 5432, nativeMeta.getDefaultDatabasePort() );
    assertEquals( "org.postgresql.Driver", nativeMeta.getDriverClass() );

    assertEquals( "jdbc:postgresql://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );

    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertTrue( nativeMeta.supportsSequences() );
    assertTrue( nativeMeta.supportsSequenceNoMaxValueOption() );
    assertTrue( nativeMeta.supportsAutoInc() );
    assertEquals( " limit 5", nativeMeta.getLimitClause( 5 ) );
    assertFalse( nativeMeta.needsToLockAllTables() );
    assertArrayEquals( new String[] {
      // http://www.postgresql.org/docs/8.1/static/sql-keywords-appendix.html
      // added also non-reserved key words because there is progress from the Postgre developers to add them
      "A", "ABORT", "ABS", "ABSOLUTE", "ACCESS", "ACTION", "ADA", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL",
      "ALLOCATE", "ALSO", "ALTER", "ALWAYS", "ANALYSE", "ANALYZE", "AND", "ANY", "ARE", "ARRAY", "AS", "ASC",
      "ASENSITIVE", "ASSERTION", "ASSIGNMENT", "ASYMMETRIC", "AT", "ATOMIC", "ATTRIBUTE", "ATTRIBUTES",
      "AUTHORIZATION", "AVG", "BACKWARD", "BEFORE", "BEGIN", "BERNOULLI", "BETWEEN", "BIGINT", "BINARY", "BIT",
      "BITVAR", "BIT_LENGTH", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BY", "C", "CACHE", "CALL", "CALLED",
      "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CATALOG_NAME", "CEIL", "CEILING", "CHAIN",
      "CHAR", "CHARACTER", "CHARACTERISTICS", "CHARACTERS", "CHARACTER_LENGTH", "CHARACTER_SET_CATALOG",
      "CHARACTER_SET_NAME", "CHARACTER_SET_SCHEMA", "CHAR_LENGTH", "CHECK", "CHECKED", "CHECKPOINT", "CLASS",
      "CLASS_ORIGIN", "CLOB", "CLOSE", "CLUSTER", "COALESCE", "COBOL", "COLLATE", "COLLATION", "COLLATION_CATALOG",
      "COLLATION_NAME", "COLLATION_SCHEMA", "COLLECT", "COLUMN", "COLUMN_NAME", "COMMAND_FUNCTION",
      "COMMAND_FUNCTION_CODE", "COMMENT", "COMMIT", "COMMITTED", "COMPLETION", "CONDITION", "CONDITION_NUMBER",
      "CONNECT", "CONNECTION", "CONNECTION_NAME", "CONSTRAINT", "CONSTRAINTS", "CONSTRAINT_CATALOG", "CONSTRAINT_NAME",
      "CONSTRAINT_SCHEMA", "CONSTRUCTOR", "CONTAINS", "CONTINUE", "CONVERSION", "CONVERT", "COPY", "CORR",
      "CORRESPONDING", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE", "CREATEDB", "CREATEROLE", "CREATEUSER", "CROSS",
      "CSV", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH",
      "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER",
      "CURSOR", "CURSOR_NAME", "CYCLE", "DATA", "DATABASE", "DATE", "DATETIME_INTERVAL_CODE",
      "DATETIME_INTERVAL_PRECISION", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFAULTS",
      "DEFERRABLE", "DEFERRED", "DEFINED", "DEFINER", "DEGREE", "DELETE", "DELIMITER", "DELIMITERS", "DENSE_RANK",
      "DEPTH", "DEREF", "DERIVED", "DESC", "DESCRIBE", "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC",
      "DIAGNOSTICS", "DICTIONARY", "DISABLE", "DISCONNECT", "DISPATCH", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP",
      "DYNAMIC", "DYNAMIC_FUNCTION", "DYNAMIC_FUNCTION_CODE", "EACH", "ELEMENT", "ELSE", "ENABLE", "ENCODING",
      "ENCRYPTED", "END", "END-EXEC", "EQUALS", "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDE", "EXCLUDING",
      "EXCLUSIVE", "EXEC", "EXECUTE", "EXISTING", "EXISTS", "EXP", "EXPLAIN", "EXTERNAL", "EXTRACT", "FALSE", "FETCH",
      "FILTER", "FINAL", "FIRST", "FLOAT", "FLOOR", "FOLLOWING", "FOR", "FORCE", "FOREIGN", "FORTRAN", "FORWARD",
      "FOUND", "FREE", "FREEZE", "FROM", "FULL", "FUNCTION", "FUSION", "G", "GENERAL", "GENERATED", "GET", "GLOBAL",
      "GO", "GOTO", "GRANT", "GRANTED", "GREATEST", "GROUP", "GROUPING", "HANDLER", "HAVING", "HEADER", "HIERARCHY",
      "HOLD", "HOST", "HOUR", "IDENTITY", "IGNORE", "ILIKE", "IMMEDIATE", "IMMUTABLE", "IMPLEMENTATION", "IMPLICIT",
      "IN", "INCLUDING", "INCREMENT", "INDEX", "INDICATOR", "INFIX", "INHERIT", "INHERITS", "INITIALIZE", "INITIALLY",
      "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INSTANCE", "INSTANTIABLE", "INSTEAD", "INT", "INTEGER",
      "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "INVOKER", "IS", "ISNULL", "ISOLATION", "ITERATE", "JOIN", "K",
      "KEY", "KEY_MEMBER", "KEY_TYPE", "LANCOMPILER", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING", "LEAST",
      "LEFT", "LENGTH", "LESS", "LEVEL", "LIKE", "LIMIT", "LISTEN", "LN", "LOAD", "LOCAL", "LOCALTIME",
      "LOCALTIMESTAMP", "LOCATION", "LOCATOR", "LOCK", "LOGIN", "LOWER", "M", "MAP", "MATCH", "MATCHED", "MAX",
      "MAXVALUE", "MEMBER", "MERGE", "MESSAGE_LENGTH", "MESSAGE_OCTET_LENGTH", "MESSAGE_TEXT", "METHOD", "MIN",
      "MINUTE", "MINVALUE", "MOD", "MODE", "MODIFIES", "MODIFY", "MODULE", "MONTH", "MORE", "MOVE", "MULTISET",
      "MUMPS", "NAME", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NESTING", "NEW", "NEXT", "NO", "NOCREATEDB",
      "NOCREATEROLE", "NOCREATEUSER", "NOINHERIT", "NOLOGIN", "NONE", "NORMALIZE", "NORMALIZED", "NOSUPERUSER", "NOT",
      "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT", "NULL", "NULLABLE", "NULLIF", "NULLS", "NUMBER", "NUMERIC", "OBJECT",
      "OCTETS", "OCTET_LENGTH", "OF", "OFF", "OFFSET", "OIDS", "OLD", "ON", "ONLY", "OPEN", "OPERATION", "OPERATOR",
      "OPTION", "OPTIONS", "OR", "ORDER", "ORDERING", "ORDINALITY", "OTHERS", "OUT", "OUTER", "OUTPUT", "OVER",
      "OVERLAPS", "OVERLAY", "OVERRIDING", "OWNER", "PAD", "PARAMETER", "PARAMETERS", "PARAMETER_MODE",
      "PARAMETER_NAME", "PARAMETER_ORDINAL_POSITION", "PARAMETER_SPECIFIC_CATALOG", "PARAMETER_SPECIFIC_NAME",
      "PARAMETER_SPECIFIC_SCHEMA", "PARTIAL", "PARTITION", "PASCAL", "PASSWORD", "PATH", "PERCENTILE_CONT",
      "PERCENTILE_DISC", "PERCENT_RANK", "PLACING", "PLI", "POSITION", "POSTFIX", "POWER", "PRECEDING", "PRECISION",
      "PREFIX", "PREORDER", "PREPARE", "PREPARED", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURAL",
      "PROCEDURE", "PUBLIC", "QUOTE", "RANGE", "RANK", "READ", "READS", "REAL", "RECHECK", "RECURSIVE", "REF",
      "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE",
      "REGR_SXX", "REGR_SXY", "REGR_SYY", "REINDEX", "RELATIVE", "RELEASE", "RENAME", "REPEATABLE", "REPLACE", "RESET",
      "RESTART", "RESTRICT", "RESULT", "RETURN", "RETURNED_CARDINALITY", "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH",
      "RETURNED_SQLSTATE", "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROUTINE_CATALOG",
      "ROUTINE_NAME", "ROUTINE_SCHEMA", "ROW", "ROWS", "ROW_COUNT", "ROW_NUMBER", "RULE", "SAVEPOINT", "SCALE",
      "SCHEMA", "SCHEMA_NAME", "SCOPE", "SCOPE_CATALOG", "SCOPE_NAME", "SCOPE_SCHEMA", "SCROLL", "SEARCH", "SECOND",
      "SECTION", "SECURITY", "SELECT", "SELF", "SENSITIVE", "SEQUENCE", "SERIALIZABLE", "SERVER_NAME", "SESSION",
      "SESSION_USER", "SET", "SETOF", "SETS", "SHARE", "SHOW", "SIMILAR", "SIMPLE", "SIZE", "SMALLINT", "SOME",
      "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SPECIFIC_NAME", "SQL", "SQLCODE", "SQLERROR", "SQLEXCEPTION",
      "SQLSTATE", "SQLWARNING", "SQRT", "STABLE", "START", "STATE", "STATEMENT", "STATIC", "STATISTICS", "STDDEV_POP",
      "STDDEV_SAMP", "STDIN", "STDOUT", "STORAGE", "STRICT", "STRUCTURE", "STYLE", "SUBCLASS_ORIGIN", "SUBLIST",
      "SUBMULTISET", "SUBSTRING", "SUM", "SUPERUSER", "SYMMETRIC", "SYSID", "SYSTEM", "SYSTEM_USER", "TABLE",
      "TABLESAMPLE", "TABLESPACE", "TABLE_NAME", "TEMP", "TEMPLATE", "TEMPORARY", "TERMINATE", "THAN", "THEN", "TIES",
      "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TOAST", "TOP_LEVEL_COUNT", "TRAILING",
      "TRANSACTION", "TRANSACTIONS_COMMITTED", "TRANSACTIONS_ROLLED_BACK", "TRANSACTION_ACTIVE", "TRANSFORM",
      "TRANSFORMS", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", "TRIGGER_CATALOG", "TRIGGER_NAME",
      "TRIGGER_SCHEMA", "TRIM", "TRUE", "TRUNCATE", "TRUSTED", "TYPE", "UESCAPE", "UNBOUNDED", "UNCOMMITTED", "UNDER",
      "UNENCRYPTED", "UNION", "UNIQUE", "UNKNOWN", "UNLISTEN", "UNNAMED", "UNNEST", "UNTIL", "UPDATE", "UPPER",
      "USAGE", "USER", "USER_DEFINED_TYPE_CATALOG", "USER_DEFINED_TYPE_CODE", "USER_DEFINED_TYPE_NAME",
      "USER_DEFINED_TYPE_SCHEMA", "USING", "VACUUM", "VALID", "VALIDATOR", "VALUE", "VALUES", "VARCHAR", "VARIABLE",
      "VARYING", "VAR_POP", "VAR_SAMP", "VERBOSE", "VIEW", "VOLATILE", "WHEN", "WHENEVER", "WHERE", "WIDTH_BUCKET",
      "WINDOW", "WITH", "WITHIN", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE" }, nativeMeta.getReservedWords() );

    assertTrue( nativeMeta.supportsRepository() );
    assertFalse( nativeMeta.isDefaultingToUppercase() );
    assertEquals( "http://jdbc.postgresql.org/documentation/83/connect.html#connection-parameters", nativeMeta.getExtraOptionsHelpText() );
    assertArrayEquals( new String[] { "postgresql-8.2-506.jdbc3.jar" }, nativeMeta.getUsedLibraries() );
    assertFalse( nativeMeta.supportsErrorHandlingOnBatchUpdates() );
    assertTrue( nativeMeta.requiresCastToVariousForIsNull() );
    assertFalse( nativeMeta.supportsGetBlob() );
    assertTrue( nativeMeta.useSafePoints() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "SELECT * FROM FOO limit 1", nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( "SELECT * FROM FOO limit 1", nativeMeta.getSQLTableExists( "FOO" ) );
    assertEquals( "SELECT FOO FROM BAR limit 1", nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );
    assertEquals( "SELECT FOO FROM BAR limit 1", nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );
    assertEquals( "SELECT relname AS sequence_name FROM pg_catalog.pg_statio_all_sequences", nativeMeta.getSQLListOfSequences() );
    assertEquals( "SELECT nextval('FOO')", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    assertEquals( "SELECT currval('FOO')", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "SELECT relname AS sequence_name FROM pg_catalog.pg_statio_all_sequences WHERE relname = 'foo'",
        nativeMeta.getSQLSequenceExists( "FOO" ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR TIMESTAMP",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaDate( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR TIMESTAMP",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR CHAR(1)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBoolean( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR DOUBLE PRECISION",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 0, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR INTEGER",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 0 ), "", false, "", false ) );


    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR NUMERIC(13, 3)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR NUMERIC(13, 3)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR NUMERIC(25, 4)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 21, 4 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR TEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", nativeMeta.getMaxVARCHARLength() + 2, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, -7 ), "", false, "", false ) ); // Bug here - invalid SQL

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR NUMERIC(29, 7)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 22, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR DOUBLE PRECISION",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", -10, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR NUMERIC(12, 7)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR  UNKNOWN",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInternetAddress( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BIGSERIAL",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BIGSERIAL",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 26, 8 ), "BAR", true, "", false ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP COLUMN BAR",
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR_KTL VARCHAR(15);" + lineSep
                  + "UPDATE FOO SET BAR_KTL=BAR;" + lineSep + "ALTER TABLE FOO DROP COLUMN BAR;" + lineSep
                  + "ALTER TABLE FOO RENAME BAR_KTL TO BAR;" + lineSep,
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR_KTL TEXT;" + lineSep
        + "UPDATE FOO SET BAR_KTL=BAR;" + lineSep + "ALTER TABLE FOO DROP COLUMN BAR;" + lineSep
        + "ALTER TABLE FOO RENAME BAR_KTL TO BAR;" + lineSep,
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR" ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR SMALLINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 4, 0 ), "", true, "", false ) );

    assertEquals( "LOCK TABLE FOO , BAR IN ACCESS EXCLUSIVE MODE;" + lineSep,
        nativeMeta.getSQLLockTables( new String[] { "FOO", "BAR" } ) );

    assertNull( nativeMeta.getSQLUnlockTables(  new String[] { "FOO" } ) );
  }

}
