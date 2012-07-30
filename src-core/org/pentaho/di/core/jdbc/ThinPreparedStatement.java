package org.pentaho.di.core.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ThinPreparedStatement extends ThinStatement implements PreparedStatement {
  
  private String sql; // contains ? placeholders

  protected List<Integer> placeholderIndexes;
  protected ValueMetaInterface[] paramMeta;
  protected Object[] paramData;

  public ThinPreparedStatement(ThinConnection connection, String sql) throws SQLException {
    super(connection);
    this.sql = sql;
    
    analyzeSql();
  }

  public void analyzeSql() throws SQLException {
    try {
      placeholderIndexes = new ArrayList<Integer>();
    
      int index=0;
      while (index<sql.length()) {
        index = ThinUtil.skipChars(sql, index, '\'', '"');
        if (index<sql.length()) {
          if (sql.charAt(index)=='?') {
            // placeholder found.
            placeholderIndexes.add(index);
          }
        }
        
        index++;
      }
      paramData = new Object[placeholderIndexes.size()];
      paramMeta = new ValueMetaInterface[placeholderIndexes.size()];
      // Null Strings is the default.
      for (int i=0;i<placeholderIndexes.size();i++) {
        paramMeta[i] = new ValueMeta("param-"+(i+1), ValueMetaInterface.TYPE_STRING);
      }
      
    } catch(Exception e) {
      throw new SQLException(e);
    }
  }
  
  public String replaceSql() throws SQLException {
    try {
      StringBuilder newSql = new StringBuilder(sql);
      
      for (int i=placeholderIndexes.size()-1;i>=0;i--) {
        int index = placeholderIndexes.get(i);
        ValueMetaInterface valueMeta = paramMeta[i];
        if (valueMeta==null) {
          throw new SQLException("Parameter "+(i+1)+" was not specified");
        }
        String replacement = null;
        if (valueMeta.isNull(paramData[i])) {
          replacement = "NULL";
        }
        switch(valueMeta.getType()) {
        case ValueMetaInterface.TYPE_STRING : 
          replacement = "'"+valueMeta.getString(paramData[i])+"'"; 
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          double d = valueMeta.getNumber(paramData[i]); 
          replacement = Double.toString(d);
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          long l = valueMeta.getInteger(paramData[i]); 
          replacement = Long.toString(l);
          break;
        case ValueMetaInterface.TYPE_DATE:
          java.util.Date date = valueMeta.getDate(paramData[i]); 
          replacement = new SimpleDateFormat("'['yyyy/MM/dd HH:mm:ss.SSS']'").format(date);
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          BigDecimal bd = valueMeta.getBigNumber(paramData[i]); 
          replacement = bd.toString();
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          boolean b = valueMeta.getBoolean(paramData[i]); 
          replacement = b ? "TRUE" : "FALSE";
          break;
        }
        if (replacement == null) {
          throw new KettleSQLException("Unhandled data type: "+valueMeta.getTypeDesc()+" replacing parameter "+(i+1));
        }
        
        // replace the ?
        //
        newSql.replace(index, index+1, replacement);
      }
      
      return newSql.toString();
    } catch(Exception e) {
      throw new SQLException("Unexpected enhancing SQL to include specified parameters", e);
    }
  }

  @Override
  public void addBatch() throws SQLException {
    throw new SQLException("Batch operations are not supported");
  }

  @Override
  public void clearParameters() throws SQLException {
    analyzeSql();
  }

  @Override
  public boolean execute() throws SQLException {
    return execute(replaceSql());
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    return executeQuery(replaceSql());
  }

  @Override
  public int executeUpdate() throws SQLException {
    throw new SQLException("Update operations are not supported");
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return resultSet.getMetaData();
  }

  @Override
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return new ThinParameterMetaData(this);
  }

  @Override
  public void setArray(int nr, Array value) throws SQLException {
    throw new SQLException("Arrays are not supported");
  }

  @Override
  public void setAsciiStream(int nr, InputStream value) throws SQLException {
    throw new SQLException("ASCII Streams are not supported");
  }

  @Override
  public void setAsciiStream(int nr, InputStream value, int arg2) throws SQLException {
    throw new SQLException("ASCII Streams are not supported");
  }

  @Override
  public void setAsciiStream(int nr, InputStream value, long arg2) throws SQLException {
    throw new SQLException("ASCII Streams are not supported");
  }

  @Override
  public void setBigDecimal(int nr, BigDecimal value) throws SQLException {
    paramData[nr-1] = value;
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_BIGNUMBER);
  }

  @Override
  public void setBinaryStream(int nr, InputStream value) throws SQLException {
    throw new SQLException("Binary Streams are not supported");
  }

  @Override
  public void setBinaryStream(int nr, InputStream value, int arg2) throws SQLException {
    throw new SQLException("Binary Streams are not supported");
  }

  @Override
  public void setBinaryStream(int nr, InputStream value, long arg2) throws SQLException {
    throw new SQLException("Binary Streams are not supported");
  }

  @Override
  public void setBlob(int nr, Blob value) throws SQLException {
    throw new SQLException("BLOB parameters are not supported");
  }

  @Override
  public void setBlob(int nr, InputStream value) throws SQLException {
    throw new SQLException("BLOB parameters are not supported");
  }

  @Override
  public void setBlob(int nr, InputStream value, long arg2) throws SQLException {
    throw new SQLException("BLOB parameters are not supported");
  }

  @Override
  public void setBoolean(int nr, boolean value) throws SQLException {
    paramData[nr-1] = Boolean.valueOf(value);
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_BOOLEAN);
  }

  @Override
  public void setByte(int nr, byte value) throws SQLException {
    paramData[nr-1] = Long.valueOf(value);
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_INTEGER);
  }

  @Override
  public void setBytes(int nr, byte[] value) throws SQLException {
    throw new SQLException("Binary parameters are not supported");
  }

  @Override
  public void setCharacterStream(int nr, Reader value) throws SQLException {
    throw new SQLException("Character stream parameters are not supported");
  }

  @Override
  public void setCharacterStream(int nr, Reader value, int arg2) throws SQLException {
    throw new SQLException("Character stream parameters are not supported");
  }

  @Override
  public void setCharacterStream(int nr, Reader value, long arg2) throws SQLException {
    throw new SQLException("Character stream parameters are not supported");
  }

  @Override
  public void setClob(int nr, Clob value) throws SQLException {
    throw new SQLException("CLOB parameters are not supported");
  }

  @Override
  public void setClob(int nr, Reader value) throws SQLException {
    throw new SQLException("CLOB parameters are not supported");
  }

  @Override
  public void setClob(int nr, Reader value, long arg2) throws SQLException {
    throw new SQLException("CLOB parameters are not supported");
  }

  @Override
  public void setDate(int nr, Date value) throws SQLException {
    paramData[nr-1] = new java.util.Date(value.getTime());
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_DATE);
  }

  @Override
  public void setDate(int nr, Date value, Calendar calendar) throws SQLException {
    // TODO: investigate the calendar parameter functionality with regards to time zones.
    //
    paramData[nr-1] = new java.util.Date(value.getTime());
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_DATE);
  }

  @Override
  public void setDouble(int nr, double value) throws SQLException {
    paramData[nr-1] = Double.valueOf(value);
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_NUMBER);
  }

  @Override
  public void setFloat(int nr, float value) throws SQLException {
    paramData[nr-1] = Double.valueOf(value);
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_NUMBER);
  }

  @Override
  public void setInt(int nr, int value) throws SQLException {
    paramData[nr-1] = Long.valueOf(value);
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_INTEGER);
  }

  @Override
  public void setLong(int nr, long value) throws SQLException {
    paramData[nr-1] = Long.valueOf(value);
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_INTEGER);
  }

  @Override
  public void setNCharacterStream(int nr, Reader value) throws SQLException {
    throw new SQLException("NCharacter stream parameters are not supported");
  }

  @Override
  public void setNCharacterStream(int nr, Reader value, long arg2) throws SQLException {
    throw new SQLException("NCharacter stream parameters are not supported");
  }

  @Override
  public void setNClob(int nr, NClob value) throws SQLException {
    throw new SQLException("NCLOB parameters are not supported");
  }

  @Override
  public void setNClob(int nr, Reader value) throws SQLException {
    throw new SQLException("NCLOB parameters are not supported");
  }

  @Override
  public void setNClob(int nr, Reader value, long arg2) throws SQLException {
    throw new SQLException("NCLOB parameters are not supported");
  }

  @Override
  public void setNString(int nr, String value) throws SQLException {
    throw new SQLException("NString parameters are not supported");
  }

  @Override
  public void setNull(int nr, int sqlType) throws SQLException {
    paramData[nr-1] = null;
    paramMeta[nr-1] = ThinUtil.getValueMeta("param-"+nr, sqlType);
  }

  @Override
  public void setNull(int nr, int value, String arg2) throws SQLException {
    setNull(nr, value);
  }

  @Override
  public void setObject(int nr, Object value) throws SQLException {
    if (value==null) {
      throw new SQLException("Null values are not supported for the setObject() method");
    }
    if (value instanceof String) {
      setString(nr, (String)value);
    } else if (value instanceof Long) {
      setLong(nr, (Long)value);
    } else if (value instanceof Integer) {
      setInt(nr, (Integer)value);
    } else if (value instanceof Byte) {
      setByte(nr, (Byte)value);
    } else if (value instanceof Date) {
      setDate(nr, (Date)value);
    } else if (value instanceof Boolean) {
      setBoolean(nr, (Boolean)value);
    } else if (value instanceof Double) {
      setDouble(nr, (Double)value);
    } else if (value instanceof Float) {
      setFloat(nr, (Float)value);
    } else if (value instanceof BigDecimal) {
      setBigDecimal(nr, (BigDecimal)value);
    } else {
      throw new SQLException("value of class "+value.getClass().getName());
    }
  }

  @Override
  public void setObject(int nr, Object value, int arg2) throws SQLException {
   setObject(nr, value);
  }

  @Override
  public void setObject(int nr, Object value, int arg2, int arg3) throws SQLException {
    setObject(nr, value);
  }

  @Override
  public void setRef(int nr, Ref value) throws SQLException {
    throw new SQLException("REF parameters are not supported");
  }

  @Override
  public void setRowId(int nr, RowId value) throws SQLException {
    throw new SQLException("ROWID parameters are not supported");
  }

  @Override
  public void setSQLXML(int nr, SQLXML value) throws SQLException {
    throw new SQLException("SQLXML parameters are not supported");
  }

  @Override
  public void setShort(int nr, short value) throws SQLException {
    setLong(nr, Long.valueOf(value));
  }

  @Override
  public void setString(int nr, String value) throws SQLException {
    paramData[nr-1] = value;
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_STRING);
  }

  @Override
  public void setTime(int nr, Time value) throws SQLException {
    paramData[nr-1] = new java.util.Date(value.getTime());
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_DATE);
  }

  @Override
  public void setTime(int nr, Time value, Calendar arg2) throws SQLException {
    setTime(nr, value);
  }

  @Override
  public void setTimestamp(int nr, Timestamp value) throws SQLException {
    paramData[nr-1] = new java.util.Date(value.getTime());
    paramMeta[nr-1] = new ValueMeta("param-"+nr, ValueMetaInterface.TYPE_DATE);
  }

  @Override
  public void setTimestamp(int nr, Timestamp value, Calendar arg2) throws SQLException {
    setTimestamp(nr, value);
  }

  @Override
  public void setURL(int nr, URL value) throws SQLException {
    throw new SQLException("URL parameters are not supported");
  }

  @Override
  public void setUnicodeStream(int nr, InputStream value, int arg2) throws SQLException {
    throw new SQLException("Unicode stream parameters are not supported");
  }

  /**
   * @return the sql
   */
  public String getSql() {
    return sql;
  }

  /**
   * @param sql the sql to set
   */
  public void setSql(String sql) {
    this.sql = sql;
  }

  /**
   * @return the paramMeta
   */
  public ValueMetaInterface[] getParamMeta() {
    return paramMeta;
  }

  /**
   * @return the paramData
   */
  public Object[] getParamData() {
    return paramData;
  }

}
