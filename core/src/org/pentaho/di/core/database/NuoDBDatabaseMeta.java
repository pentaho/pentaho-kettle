/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains NuoDB specific information through static final members.
 *
 * @author Robert Buck
 * @since 2014-03-15
 */
public class NuoDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

  @Override
  public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr) {
    String retval = "";

    int length = v.getLength();
    int precision = v.getPrecision();

    String fieldname = v.getName();
    if (add_fieldname) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch (type) {
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if (fieldname.equalsIgnoreCase(tk)) {
          retval += "BIGINT";
          if (use_autoinc) {
            retval += " GENERATED ALWAYS AS IDENTITY";
          }
          if (fieldname.equalsIgnoreCase(pk)) {
            retval += " PRIMARY KEY";
          }
        } else {
          if (precision == 0) {
            if (length > 18) {
              retval += "DECIMAL(" + length + ",0)";
            } else {
              if (length > 9) {
                retval += "BIGINT";
              } else {
                retval += "INTEGER";
              }
              if (fieldname.equalsIgnoreCase(pk)) {
                retval += " PRIMARY KEY";
              }
            }
          } else {
            if (length > 15) {
              retval += "DECIMAL(" + length;
              if (precision > 0) {
                retval += ", " + precision;
              }
              retval += ")";
            } else {
              retval += "DOUBLE";
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        retval += "STRING";
        break;
      case ValueMetaInterface.TYPE_DATE:
        retval += "DATE";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "BOOLEAN";
        break;
      case ValueMetaInterface.TYPE_BINARY:
        retval += "BLOB";
        break;
      case ValueMetaInterface.TYPE_TIMESTAMP:
        retval += "TIMESTAMP";
        break;
      default:
        retval += "UNKNOWN";
        break;
    }

    if (add_cr) {
      retval += Const.CR;
    }

    return retval;
  }

  @Override
  public int[] getAccessTypeList() {
    return new int[]{DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI};
  }

  @Override
  public String getDriverClass() {
    if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "com.nuodb.jdbc.Driver";
    }
  }

  @Override
  public int getDefaultDatabasePort() {
    if (getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE) {
      return 48004;
    }
    return -1;
  }

  @Override
  public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException {
    if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
      return "jdbc:odbc:" + databaseName;
    } else {
      if (Const.isEmpty(port)) {
        return "jdbc:com.nuodb://" + hostname + "/" + databaseName;
      } else {
        return "jdbc:com.nuodb://" + hostname + ":" + port + "/" + databaseName;
      }
    }
  }

  @Override
  public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
    return "ALTER TABLE " + tablename + " ADD COLUMN " + getFieldDefinition(v, tk, pk, use_autoinc, true, false);
  }

  @Override
  public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
    return "ALTER TABLE " + tablename + " MODIFY COLUMN " + getFieldDefinition(v, tk, pk, use_autoinc, true, false);
  }

  @Override
  public String getLimitClause(int nrRows) {
    return " LIMIT " + nrRows;
  }

  @Override
  public int getNotFoundTK(boolean use_autoinc) {
    if (supportsAutoInc() && use_autoinc) {
      return 1;
    }
    return super.getNotFoundTK(use_autoinc);
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[]{"nuodb-jdbc.jar"};
  }

  @Override
  public boolean supportsSequences() {
    return true;
  }

  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  @Override
  public String[] getReservedWords() {
    // as of 11/21/2013
    return new String[]{"ABS", "ACOS", "ASIN", "ATAN2", "ATAN", "AVG", "BITS",
        "BIT_LENGTH", "BREAK", "CASCADE", "CATCH", "CEILING", "CHARACTER_LENGTH",
        "COALESCE", "CONCAT", "CONTAINING", "CONVERT_TZ", "COS", "COT", "COUNT",
        "CURRENT_SCHEMA", "DAYOFWEEK", "DAYOFYEAR", "DEGREES", "END_FOR",
        "END_IF", "END_PROCEDURE", "END_TRIGGER", "END_TRY", "END_WHILE", "ENUM",
        "EXTRACT", "FLOOR", "GENERATED", "IF", "IFNULL", "KEY", "LIMIT", "LOCATE",
        "LOWER", "LTRIM", "MAX", "MIN", "MOD", "MSLEEP", "NEXT", "NEXT_VALUE",
        "NOW", "NTEXT", "NULLIF", "NVARCHAR", "OCTETS", "OCTET_LENGTH", "OFF",
        "OFFSET", "PI", "POSITION", "POWER", "RADIANS", "RAND", "RECORD_BATCHING",
        "REGEXP", "RESTART", "RESTRICT", "REVERSE", "ROUND", "RTRIM", "SHOW",
        "SIN", "SMALLDATETIME", "SQRT", "STARTING", "STRING_TYPE", "SUBSTRING_INDEX",
        "SUBSTR", "SUM", "TAN", "THROW", "TINYBLOB", "TINYINT", "TRIM", "TRY",
        "VAR", "VER", "WHILE"};
  }

  @Override
  public boolean supportsViews() {
    return true;
  }

  @Override
  public String getSQLQueryFields(String tablename) {
    return "SELECT * FROM " + tablename + " LIMIT 0";
  }

  @Override
  public String getExtraOptionSeparator() {
    return "&";
  }

  @Override
  public String getExtraOptionIndicator() {
    return "?";
  }

  @Override
  public String getExtraOptionsHelpText() {
    return "http://doc.nuodb.com/display/doc/JDBC+Connection+Properties";
  }

  @Override
  public String getSQLTableExists(String tablename) {
    return getSQLQueryFields(tablename);
  }

  @Override
  public String getSQLColumnExists(String columnname, String tablename) {
    return "SELECT " + columnname + " FROM " + tablename + " LIMIT 0";
  }

  @Override
  public boolean supportsErrorHandlingOnBatchUpdates() {
    return false;
  }

  @Override
  public String getSQLInsertAutoIncUnknownDimensionRow(String schemaTable, String keyField, String versionField) {
    return "INSERT INTO " + schemaTable + "(" + versionField + ") VALUES (1)";
  }

  @Override
  public boolean supportsCatalogs() {
    return false;
  }

  @Override
  public boolean supportsSetCharacterStream() {
    return false;
  }
}
