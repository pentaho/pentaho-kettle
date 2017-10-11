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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains AS/400 specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */
public class AS400DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "com.ibm.as400.access.AS400JDBCDriver";
    }
  }

  /**
   * Get the maximum length of a text field for this database connection. This includes optional CLOB, Memo and Text
   * fields. (the maximum!)
   *
   * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
   */
  @Override
  public int getMaxTextFieldLength() {
    return 65536;
  }

  @Override
  public String getURL( String hostname, String port, String database ) {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + database;
    } else {
      return "jdbc:as400://" + hostname + "/" + database;
    }
  }

  /**
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "DELETE FROM " + tableName;
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
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
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
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE "
      + tablename + " ALTER COLUMN " + v.getName() + " SET "
      + getFieldDefinition( v, tk, pk, use_autoinc, false, false );
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
    boolean add_fieldname, boolean add_cr ) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( add_fieldname ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
      case ValueMetaInterface.TYPE_DATE:
        retval += "TIMESTAMP";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "CHAR(1)";
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( length <= 0 && precision <= 0 ) {
          retval += "DOUBLE";
        } else {
          retval += "DECIMAL";
          if ( length > 0 ) {
            retval += "(" + length;
            if ( precision > 0 ) {
              retval += ", " + precision;
            }
            retval += ")";
          }
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
      default:
        retval += " UNKNOWN";
        break;
    }

    if ( add_cr ) {
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
      // http://publib.boulder.ibm.com/infocenter/iseries/v5r4/index.jsp
      // This is the list of currently reserved DB2 UDB for iSeries words. Words may be added at any time.
      // For a list of additional words that may become reserved in the future, see the IBM SQL and
      // ANSI reserved words in the IBM SQL Reference Version 1 SC26-3255.
      "ACTIVATE", "ADD", "ALIAS", "ALL", "ALLOCATE", "ALLOW", "ALTER", "AND", "ANY", "AS", "ASENSITIVE", "AT",
      "ATTRIBUTES", "AUTHORIZATION", "BEGIN", "BETWEEN", "BINARY", "BY", "CACHE", "CALL", "CALLED",
      "CARDINALITY", "CASE", "CAST", "CCSID", "CHAR", "CHARACTER", "CHECK", "CLOSE", "COLLECTION", "COLUMN",
      "COMMENT", "COMMIT", "CONCAT", "CONDITION", "CONNECT", "CONNECTION", "CONSTRAINT", "CONTAINS", "CONTINUE",
      "COUNT", "COUNT_BIG", "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_SCHEMA",
      "CURRENT_SERVER", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TIMEZONE", "CURRENT_USER", "CURSOR",
      "CYCLE", "DATABASE", "DATAPARTITIONNAME", "DATAPARTITIONNUM", "DATE", "DAY", "DAYS", "DBINFO",
      "DBPARTITIONNAME", "DBPARTITIONNUM", "DB2GENERAL", "DB2GENRL", "DB2SQL", "DEALLOCATE", "DECLARE",
      "DEFAULT", "DEFAULTS", "DEFINITION", "DELETE", "DENSERANK", "DENSE_RANK", "DESCRIBE", "DESCRIPTOR",
      "DETERMINISTIC", "DIAGNOSTICS", "DISABLE", "DISALLOW", "DISCONNECT", "DISTINCT", "DO", "DOUBLE", "DROP",
      "DYNAMIC", "EACH", "ELSE", "ELSEIF", "ENABLE", "ENCRYPTION", "END", "ENDING", "END-EXEC", "ESCAPE",
      "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDING", "EXCLUSIVE", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL",
      "EXTRACT", "FENCED", "FETCH", "FILE", "FINAL", "FOR", "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION",
      "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GRAPHIC", "GROUP", "HANDLER", "HASH",
      "HASHED_VALUE", "HAVING", "HINT", "HOLD", "HOUR", "HOURS", "IDENTITY", "IF", "IMMEDIATE", "IN",
      "INCLUDING", "INCLUSIVE", "INCREMENT", "INDEX", "INDICATOR", "INHERIT", "INNER", "INOUT", "INSENSITIVE",
      "INSERT", "INTEGRITY", "INTERSECT", "INTO", "IS", "ISOLATION", "ITERATE", "JAVA", "JOIN", "KEY", "LABEL",
      "LANGUAGE", "LATERAL", "LEAVE", "LEFT", "LIKE", "LINKTYPE", "LOCAL", "LOCALDATE", "LOCALTIME",
      "LOCALTIMESTAMP", "LOCK", "LONG", "LOOP", "MAINTAINED", "MATERIALIZED", "MAXVALUE", "MICROSECOND",
      "MICROSECONDS", "MINUTE", "MINUTES", "MINVALUE", "MODE", "MODIFIES", "MONTH", "MONTHS", "NEW",
      "NEW_TABLE", "NEXTVAL", "NO", "NOCACHE", "NOCYCLE", "NODENAME", "NODENUMBER", "NOMAXVALUE", "NOMINVALUE",
      "NOORDER", "NORMALIZED", "NOT", "NULL", "OF", "OLD", "OLD_TABLE", "ON", "OPEN", "OPTIMIZE", "OPTION",
      "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERRIDING", "PACKAGE", "PAGESIZE", "PARAMETER", "PART",
      "PARTITION", "PARTITIONING", "PARTITIONS", "PASSWORD", "PATH", "POSITION", "PREPARE", "PREVVAL",
      "PRIMARY", "PRIVILEGES", "PROCEDURE", "PROGRAM", "QUERY", "RANGE", "RANK", "READ", "READS", "RECOVERY",
      "REFERENCES", "REFERENCING", "REFRESH", "RELEASE", "RENAME", "REPEAT", "RESET", "RESIGNAL", "RESTART",
      "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROUTINE", "ROW", "ROWNUMBER", "ROW_NUMBER",
      "ROWS", "RRN", "RUN", "SAVEPOINT", "SCHEMA", "SCRATCHPAD", "SCROLL", "SEARCH", "SECOND", "SECONDS",
      "SELECT", "SENSITIVE", "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SIGNAL", "SIMPLE", "SOME", "SOURCE",
      "SPECIFIC", "SQL", "SQLID", "STACKED", "START", "STARTING", "STATEMENT", "STATIC", "SUBSTRING", "SUMMARY",
      "SYNONYM", "SYSTEM_USER", "TABLE", "THEN", "TIME", "TIMESTAMP", "TO", "TRANSACTION", "TRIGGER", "TRIM",
      "TYPE", "UNDO", "UNION", "UNIQUE", "UNTIL", "UPDATE", "USAGE", "USER", "USING", "VALUE", "VALUES",
      "VARIABLE", "VARIANT", "VERSION", "VIEW", "VOLATILE", "WHEN", "WHERE", "WHILE", "WITH", "WITHOUT",
      "WRITE", "YEAR", "YEARS" };
  }

  /**
   * @return the required libraries (in lib) for this database connection.
   */
  @Override
  public String[] getUsedLibraries() {
    return new String[] { "jt400.jar" };
  }

  /**
   * Most databases round number(7,2) 17.29999999 to 17.30, but some don't.
   *
   * @return true if the database supports roundinf of floating point data on update/insert
   */
  @Override
  public boolean supportsFloatRoundingOnUpdate() {
    return false;
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
    return "SELECT * FROM SYSCAT.SEQUENCES WHERE SEQNAME = '" + sequenceName.toUpperCase() + "'";
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
   * @return true if the database supports the NOMAXVALUE sequence option. The default is false, AS/400 and DB2 support
   *         this.
   */
  @Override
  public boolean supportsSequenceNoMaxValueOption() {
    return true;
  }

}
