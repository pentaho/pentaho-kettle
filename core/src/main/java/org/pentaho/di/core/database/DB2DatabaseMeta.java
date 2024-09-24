/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

/**
 * Contains DB2 specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class DB2DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 50000;
    }
    return -1;
  }

  @Override
  public boolean supportsSetCharacterStream() {
    return false;
  }

  @Override
  public String getDriverClass() {
    return "com.ibm.db2.jcc.DB2Driver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    return "jdbc:db2://" + hostname + ":" + port + "/" + databaseName;
  }

  /**
   * @return true if the database supports schemas, DB2 supports it (v7 and v8 for sure).
   */
  @Override
  public boolean supportsSchemas() {
    return true;
  }

  /**
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "ALTER TABLE " + tableName + " ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE";
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " ADD COLUMN " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " DROP COLUMN " + v.getName() + Const.CR;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    String retval = "";
    retval += "ALTER TABLE " + tablename + " DROP COLUMN " + v.getName() + Const.CR + ";" + Const.CR;
    retval +=
      "ALTER TABLE " + tablename + " ADD COLUMN " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
    return retval;
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( addFieldName ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
      case ValueMetaInterface.TYPE_DATE:
        retval += "TIMESTAMP";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "CHARACTER(1)";
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( fieldname.equalsIgnoreCase( tk ) && useAutoinc ) { // Technical key: auto increment field!
          retval += "BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)";
        } else {
          if ( length > 0 ) {
            retval += "DECIMAL(" + length;
            if ( precision > 0 ) {
              retval += ", " + precision;
            }
            retval += ")";
          } else {
            retval += "FLOAT";
          }
        }
        break;
      case ValueMetaInterface.TYPE_INTEGER:
        if ( fieldname.equalsIgnoreCase( tk ) && useAutoinc ) { // Technical key: auto increment field!
          retval += "INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)";
        } else {
          retval += "INTEGER";
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length > getMaxVARCHARLength() || length >= DatabaseMeta.CLOB_LENGTH ) {
          retval += "CLOB";
        } else {
          retval += "VARCHAR";
          if ( length > 0 ) {
            retval += "(" + length;
          } else {
            retval += "("; // Maybe use some default DB String length?
          }
          retval += ")";

        }
        break;
      case ValueMetaInterface.TYPE_BINARY:
        if ( length > getMaxVARCHARLength() || length >= DatabaseMeta.CLOB_LENGTH ) {
          retval += "BLOB(" + length + ")";
        } else {
          if ( length > 0 ) {
            retval += "CHAR(" + length + ") FOR BIT DATA";
          } else {
            retval += "BLOB"; // not going to work, but very close
          }
        }
        break;
      default:
        retval += " UNKNOWN";
        break;
    }

    if ( addCr ) {
      retval += Const.CR;
    }

    return retval;
  }

  /*
   * (non-Javadoc)
   *
   * @see DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    return new String[] {
      // http://publib.boulder.ibm.com/infocenter/db2luw/v8/index.jsp?topic=/com.ibm.db2.udb.doc/admin/r0001095.htm
      // For portability across the DB2 Universal Database products, the following should be considered reserved
      // words.
      // The following list also contains the ISO/ANSI SQL99 reserved words for future compatibility.
      "ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALLOW", "ALTER",
      "AND", "ANY", "APPLICATION", "ARE", "ARRAY", "AS", "ASC", "ASSERTION", "ASSOCIATE", "ASUTIME", "AT",
      "AUDIT", "AUTHORIZATION", "AUX", "AUXILIARY", "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BIT", "BLOB",
      "BOOLEAN", "BOTH", "BREADTH", "BUFFERPOOL", "BY", "CACHE", "CALL", "CALLED", "CAPTURE", "CARDINALITY",
      "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CCSID", "CHAR", "CHARACTER", "CHECK", "CLASS", "CLOB",
      "CLOSE", "CLUSTER", "COLLATE", "COLLATION", "COLLECTION", "COLLID", "COLUMN", "COMMENT", "COMMIT",
      "COMPLETION", "CONCAT", "CONDITION", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR",
      "CONTAINS", "CONTINUE", "CORRESPONDING", "COUNT", "COUNT_BIG", "CREATE", "CROSS", "CUBE", "CURRENT",
      "CURRENT_DATE", "CURRENT_LC_CTYPE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_SERVER", "CURRENT_TIME",
      "CURRENT_TIMESTAMP", "CURRENT_TIMEZONE", "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATABASE", "DATE",
      "DAY", "DAYS", "DB2GENERAL", "DB2GENRL", "DB2SQL", "DBINFO", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE",
      "DEFAULT", "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEFINITION", "DELETE", "DEPTH", "DEREF", "DESC",
      "DESCRIBE", "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY",
      "DISALLOW", "DISCONNECT", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP", "DSNHATTR", "DSSIZE", "DYNAMIC",
      "EACH", "EDITPROC", "ELSE", "ELSEIF", "ENCODING", "END", "END-EXEC", "END-EXEC1", "EQUALS", "ERASE",
      "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDING", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL",
      "FALSE", "FENCED", "FETCH", "FIELDPROC", "FILE", "FINAL", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND",
      "FREE", "FROM", "FULL", "FUNCTION", "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT",
      "GRAPHIC", "GROUP", "GROUPING", "HANDLER", "HAVING", "HOLD", "HOST", "HOUR", "HOURS", "IDENTITY", "IF",
      "IGNORE", "IMMEDIATE", "IN", "INCLUDING", "INCREMENT", "INDEX", "INDICATOR", "INHERIT", "INITIALIZE",
      "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTEGRITY",
      "INTERSECT", "INTERVAL", "INTO", "IS", "ISOBID", "ISOLATION", "ITERATE", "JAR", "JAVA", "JOIN", "KEY",
      "LABEL", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LC_CTYPE", "LEADING", "LEAVE", "LEFT", "LESS", "LEVEL",
      "LIKE", "LIMIT", "LINKTYPE", "LOCAL", "LOCALE", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOCATORS",
      "LOCK", "LOCKMAX", "LOCKSIZE", "LONG", "LOOP", "MAP", "MATCH", "MAXVALUE", "MICROSECOND", "MICROSECONDS",
      "MINUTE", "MINUTES", "MINVALUE", "MODE", "MODIFIES", "MODIFY", "MODULE", "MONTH", "MONTHS", "NAMES",
      "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEW_TABLE", "NEXT", "NO", "NOCACHE", "NOCYCLE",
      "NODENAME", "NODENUMBER", "NOMAXVALUE", "NOMINVALUE", "NONE", "NOORDER", "NOT", "NULL", "NULLS",
      "NUMERIC", "NUMPARTS", "OBID", "OBJECT", "OF", "OFF", "OLD", "OLD_TABLE", "ON", "ONLY", "OPEN",
      "OPERATION", "OPTIMIZATION", "OPTIMIZE", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT",
      "OVERRIDING", "PACKAGE", "PAD", "PARAMETER", "PARAMETERS", "PART", "PARTIAL", "PARTITION", "PATH",
      "PIECESIZE", "PLAN", "POSITION", "POSTFIX", "PRECISION", "PREFIX", "PREORDER", "PREPARE", "PRESERVE",
      "PRIMARY", "PRIOR", "PRIQTY", "PRIVILEGES", "PROCEDURE", "PROGRAM", "PSID", "PUBLIC", "QUERYNO", "READ",
      "READS", "REAL", "RECOVERY", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "RELATIVE", "RELEASE",
      "RENAME", "REPEAT", "RESET", "RESIGNAL", "RESTART", "RESTRICT", "RESULT", "RESULT_SET_LOCATOR", "RETURN",
      "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWS", "RRN", "RUN",
      "SAVEPOINT", "SCHEMA", "SCOPE", "SCRATCHPAD", "SCROLL", "SEARCH", "SECOND", "SECONDS", "SECQTY",
      "SECTION", "SECURITY", "SELECT", "SENSITIVE", "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SETS",
      "SIGNAL", "SIMPLE", "SIZE", "SMALLINT", "SOME", "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL",
      "SQLEXCEPTION", "SQLID", "SQLSTATE", "SQLWARNING", "STANDARD", "START", "STATE", "STATEMENT", "STATIC",
      "STAY", "STOGROUP", "STORES", "STRUCTURE", "STYLE", "SUBPAGES", "SUBSTRING", "SYNONYM", "SYSFUN",
      "SYSIBM", "SYSPROC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESPACE", "TEMPORARY", "TERMINATE", "THAN",
      "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION",
      "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TYPE", "UNDER", "UNDO", "UNION", "UNIQUE", "UNKNOWN",
      "UNNEST", "UNTIL", "UPDATE", "USAGE", "USER", "USING", "VALIDPROC", "VALUE", "VALUES", "VARCHAR",
      "VARIABLE", "VARIANT", "VARYING", "VCAT", "VIEW", "VOLUMES", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH",
      "WITHOUT", "WLM", "WORK", "WRITE", "YEAR", "YEARS", "ZONE" };
  }

  @Override
  public String getSQLLockTables( String[] tableNames ) {
    String sql = "";
    for ( int i = 0; i < tableNames.length; i++ ) {
      sql += "LOCK TABLE " + tableNames[i] + " IN SHARE MODE;" + Const.CR;
    }
    return sql;
  }

  @Override
  public String getSQLUnlockTables( String[] tableName ) {
    return null; // lock release on commit point.
  }

  /**
   * Get the maximum length of a text field (VARCHAR) for this database connection. If this size is exceeded use a CLOB.
   *
   * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() -
   *         CLOB_LENGTH)
   */
  @Override
  public int getMaxVARCHARLength() {
    return 32672;
  }

  @Override
  public boolean supportsBatchUpdates() {
    return true;
  }

  /**
   * @return false because the DB2 JDBC driver doesn't support getBlob on the resultset. We must use getBytes() to get
   *         the data.
   */
  @Override
  public boolean supportsGetBlob() {
    return false;
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "db2jcc.jar", "db2jcc_license_cu.jar" };
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return true;
  }

  @Override
  public String getSQLListOfSequences() {
    return "SELECT SEQNAME FROM SYSCAT.SEQUENCES";
  }

  /**
   * Check if a sequence exists.
   *
   * @param sequenceName
   *          The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  @Override
  public String getSQLSequenceExists( String sequenceName ) {
    if ( sequenceName.contains( "." ) ) {
      return "SELECT * FROM SYSCAT.SEQUENCES WHERE SEQSCHEMA = '"
        + sequenceName.substring( 0, sequenceName.indexOf( '.' ) ).toUpperCase() + "' AND SEQNAME = '"
        + sequenceName.substring( sequenceName.indexOf( '.' ) + 1, sequenceName.length() ).toUpperCase() + "'";
    } else {
      return "SELECT * FROM SYSCAT.SEQUENCES WHERE SEQNAME = '" + sequenceName.toUpperCase() + "'";
    }
  }

  /**
   * Get the current value of a database sequence
   *
   * @param sequenceName
   *          The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return "SELECT PREVIOUS VALUE FOR " + sequenceName + " FROM SYSIBM.SYSDUMMY1";
  }

  /**
   * Get the SQL to get the next value of a sequence. (Oracle only)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle only)
   */
  @Override
  public String getSQLNextSequenceValue( String sequenceName ) {
    return "SELECT NEXT VALUE FOR " + sequenceName + " FROM SYSIBM.SYSDUMMY1";
  }

  /**
   * @return This indicator separates the normal URL from the options. DB2 is special in the sense that it requires a :
   *         instead of the usual ;.
   */
  @Override
  public String getExtraOptionIndicator() {
    return ":";
  }

  /**
   * @return true if the database supports the NOMAXVALUE sequence option. The default is false, AS/400 and DB2 support
   *         this.
   */
  @Override
  public boolean supportsSequenceNoMaxValueOption() {
    return false;
  }

  /**
   * @return true if the database requires you to cast a parameter to varchar before comparing to null. Only required
   *         for DB2 and Vertica
   *
   */
  @Override
  public boolean requiresCastToVariousForIsNull() {
    return true;
  }

  /**
   * @return Handles the special case of DB2 where the display size returned is twice the precision. In that case, the
   *         length is the precision.
   *
   */
  @Override
  public boolean isDisplaySizeTwiceThePrecision() {
    return true;
  }

  /**
   * @return true if the database supports newlines in a SQL statements.
   */
  @Override
  public boolean supportsNewLinesInSQL() {
    return false;
  }

  /**
   * Get the SQL to insert a new empty unknown record in a dimension.
   *
   * @param schemaTable
   *          the schema-table name to insert into
   * @param keyField
   *          The key field
   * @param versionField
   *          the version field
   * @return the SQL to insert the unknown record into the SCD.
   */
  @Override
  public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField, String versionField ) {
    return "insert into " + schemaTable + "(" + versionField + ") values (1)";
  }

  @Override
  public boolean supportsOptionsInURL() {
    return false;
  }
}
