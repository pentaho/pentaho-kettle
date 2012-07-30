package org.pentaho.di.core.jdbc;

import java.sql.SQLException;

import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ThinUtil {
  
  public static String stripNewlines(String sql) {
    if (sql==null) return null;
    
    StringBuffer sbsql = new StringBuffer(sql);
    
    for (int i=sbsql.length()-1;i>=0;i--)
    {
      if (sbsql.charAt(i)=='\n' || sbsql.charAt(i)=='\r') sbsql.setCharAt(i, ' ');
    }
    return sbsql.toString();
  }
  
  
  public static int getSqlType(ValueMetaInterface valueMeta) {
    switch(valueMeta.getType()) {
    case ValueMetaInterface.TYPE_STRING: return java.sql.Types.VARCHAR;
    case ValueMetaInterface.TYPE_DATE: return java.sql.Types.TIMESTAMP;
    case ValueMetaInterface.TYPE_INTEGER: return java.sql.Types.BIGINT; // TODO: for metadata we don't want a long?
    case ValueMetaInterface.TYPE_BIGNUMBER: return java.sql.Types.DECIMAL;
    case ValueMetaInterface.TYPE_NUMBER: return java.sql.Types.DOUBLE;
    case ValueMetaInterface.TYPE_BOOLEAN: return java.sql.Types.BOOLEAN;
    case ValueMetaInterface.TYPE_TIMESTAMP: return java.sql.Types.TIMESTAMP;
    case ValueMetaInterface.TYPE_BINARY: return java.sql.Types.BLOB;
    }
    return java.sql.Types.VARCHAR;
  }

  public static String getSqlTypeDesc(ValueMetaInterface valueMeta) {
    switch(valueMeta.getType()) {
    case ValueMetaInterface.TYPE_STRING: return "VARCHAR";
    case ValueMetaInterface.TYPE_DATE: return "TIMESTAMP";
    case ValueMetaInterface.TYPE_INTEGER: return "BIGINT"; // TODO: for metadata we don't want a long?
    case ValueMetaInterface.TYPE_NUMBER: return "DOUBLE";
    case ValueMetaInterface.TYPE_BIGNUMBER: return "DECIMAL";
    case ValueMetaInterface.TYPE_BOOLEAN: return "BOOLEAN";
    case ValueMetaInterface.TYPE_TIMESTAMP: return "TIMESTAMP";
    case ValueMetaInterface.TYPE_BINARY: return "BLOB";
    }
    return null;
  }
  
  public static ValueMetaInterface getValueMeta(String valueName, int sqlType) throws SQLException {
    switch(sqlType) {
    case java.sql.Types.BIGINT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_INTEGER);
    case java.sql.Types.INTEGER: return new ValueMeta(valueName, ValueMetaInterface.TYPE_INTEGER);
    case java.sql.Types.SMALLINT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_INTEGER);
    
    case java.sql.Types.CHAR: return new ValueMeta(valueName, ValueMetaInterface.TYPE_STRING);
    case java.sql.Types.VARCHAR: return new ValueMeta(valueName, ValueMetaInterface.TYPE_STRING);
    case java.sql.Types.CLOB: return new ValueMeta(valueName, ValueMetaInterface.TYPE_STRING);
    
    case java.sql.Types.DATE: return new ValueMeta(valueName, ValueMetaInterface.TYPE_DATE);
    case java.sql.Types.TIMESTAMP: return new ValueMeta(valueName, ValueMetaInterface.TYPE_DATE);
    case java.sql.Types.TIME: return new ValueMeta(valueName, ValueMetaInterface.TYPE_DATE);

    case java.sql.Types.DECIMAL: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BIGNUMBER);
    
    case java.sql.Types.DOUBLE: return new ValueMeta(valueName, ValueMetaInterface.TYPE_NUMBER);
    case java.sql.Types.FLOAT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_NUMBER);
    
    case java.sql.Types.BOOLEAN: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BOOLEAN);
    case java.sql.Types.BIT: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BOOLEAN);
    
    case java.sql.Types.BINARY: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BINARY);
    case java.sql.Types.BLOB: return new ValueMeta(valueName, ValueMetaInterface.TYPE_BINARY);
    
    default:
      throw new SQLException("Don't know how to handle SQL Type: "+sqlType+", with name: "+valueName);
    }
  }
}
