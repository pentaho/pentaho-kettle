package org.pentaho.di.core.jdbc;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ThinResultSetMetaData implements ResultSetMetaData {

  private String serviceName;
  private RowMetaInterface rowMeta;

  public ThinResultSetMetaData(String serviceName, RowMetaInterface rowMeta) {
    this.serviceName = serviceName;
    this.rowMeta = rowMeta;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new SQLException("Wrapping is not supported");
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException("Wrapping is not supported");
  }

  @Override
  public String getCatalogName(int column) throws SQLException {
    return null;
  }

  @Override
  public String getColumnClassName(int column) throws SQLException {
    switch(rowMeta.getValueMeta(column-1).getType()) {
    case ValueMetaInterface.TYPE_STRING: return java.lang.String.class.getName();
    case ValueMetaInterface.TYPE_NUMBER: return java.lang.Double.class.getName();
    case ValueMetaInterface.TYPE_DATE: return java.util.Date.class.getName();
    case ValueMetaInterface.TYPE_BIGNUMBER: return BigDecimal.class.getName();
    case ValueMetaInterface.TYPE_INTEGER: return java.lang.Long.class.getName();
    case ValueMetaInterface.TYPE_BOOLEAN: return java.lang.Boolean.class.getName();
    case ValueMetaInterface.TYPE_BINARY: return (new byte[0]).getClass().getName();
    default:
      throw new SQLException("Unknown data type for column "+column);
    }
  }

  @Override
  public int getColumnCount() throws SQLException {
    return rowMeta.size();
  }

  @Override
  public int getColumnDisplaySize(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).getLength();
  }

  @Override
  public String getColumnLabel(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).getName();
  }

  @Override
  public String getColumnName(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).getName();
  }

  @Override
  public int getColumnType(int column) throws SQLException {
    switch(rowMeta.getValueMeta(column-1).getType()) {
    case ValueMetaInterface.TYPE_STRING: return java.sql.Types.VARCHAR;
    case ValueMetaInterface.TYPE_NUMBER: return java.sql.Types.DOUBLE;
    case ValueMetaInterface.TYPE_DATE: return java.sql.Types.TIMESTAMP;
    case ValueMetaInterface.TYPE_BIGNUMBER: return java.sql.Types.DECIMAL;
    case ValueMetaInterface.TYPE_INTEGER: return java.sql.Types.BIGINT;
    case ValueMetaInterface.TYPE_BOOLEAN: return java.sql.Types.BOOLEAN;
    case ValueMetaInterface.TYPE_BINARY: return java.sql.Types.BLOB;
    default:
      throw new SQLException("Unknown data type for column "+column);
    }
  }

  @Override
  public String getColumnTypeName(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).getTypeDesc();
  }

  @Override
  public int getPrecision(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).getLength();
  }

  @Override
  public int getScale(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).getPrecision();
  }

  @Override
  public String getSchemaName(int column) throws SQLException {
    return null;
  }

  @Override
  public String getTableName(int column) throws SQLException {
    return serviceName;
  }

  @Override
  public boolean isAutoIncrement(int column) throws SQLException {
    return false;
  }

  @Override
  public boolean isCaseSensitive(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).isCaseInsensitive();
  }

  @Override
  public boolean isCurrency(int column) throws SQLException {
    return false;
  }

  @Override
  public boolean isDefinitelyWritable(int column) throws SQLException {
    return false;
  }

  @Override
  public int isNullable(int column) throws SQLException {
    return 0;
  }

  @Override
  public boolean isReadOnly(int column) throws SQLException {
    return true;
  }

  @Override
  public boolean isSearchable(int column) throws SQLException {
    return false;
  }

  @Override
  public boolean isSigned(int column) throws SQLException {
    return rowMeta.getValueMeta(column-1).isNumeric();
  }

  @Override
  public boolean isWritable(int column) throws SQLException {
    return false;
  }
}
