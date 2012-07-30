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
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;

public class RowsResultSet implements ResultSet {

  private RowMetaInterface rowMeta;
  private List<Object[]> rows;
  
  private int currentIndex;
  private boolean lastNull;
  
  /**
   * @param rowMeta
   * @param rows
   */
  public RowsResultSet(RowMetaInterface rowMeta, List<Object[]> rows) {
    this.rowMeta = rowMeta;
    this.rows = rows;
    currentIndex=-1;
  }

  @Override
  public boolean absolute(int index) throws SQLException {
    currentIndex=index;
    return true;
  }

  @Override
  public void afterLast() throws SQLException {
    currentIndex=rows.size();
  }

  @Override
  public void beforeFirst() throws SQLException {
    currentIndex=-1;
  }

  @Override
  public void cancelRowUpdates() throws SQLException {
  }

  @Override
  public void clearWarnings() throws SQLException {
  }

  @Override
  public void close() throws SQLException {
  }

  @Override
  public void deleteRow() throws SQLException {
  }

  @Override
  public int findColumn(String name) throws SQLException {
    int index = rowMeta.indexOfValue(name)+1;
    System.out.println("findColumn("+name+") --> "+index);
    return index;
  }

  @Override
  public boolean first() throws SQLException {
    currentIndex=0;
    return true;
  }

  
  
  
  
  
  @Override
  public boolean wasNull() throws SQLException {
    return lastNull;
  }

  
  
  
  
  
  
  
  
  
  
  
  // Here are the getters...
  
  
  @Override
  public Date getDate(int index) throws SQLException {
    try {
      java.util.Date date = rowMeta.getDate(rows.get(currentIndex), index-1);
      if (date==null) {
        lastNull=true;
        return null;
      }
      lastNull=false;
      return new Date(date.getTime());
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public Date getDate(String columnName) throws SQLException {
    return getDate(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public Date getDate(int index, Calendar calendar) throws SQLException {
    return getDate(index);
  }

  @Override
  public Date getDate(String columnName, Calendar calendar) throws SQLException {
    return getDate(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public double getDouble(int index) throws SQLException {
    try {
      Double d = rowMeta.getNumber(rows.get(currentIndex), index-1);
      if (d==null) {
        lastNull=true;
      }
      lastNull=false;
      return d;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public double getDouble(String columnName) throws SQLException {
    return getDouble(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public Array getArray(int arg0) throws SQLException {
    throw new SQLException("Arrays are not supported");
  }

  @Override
  public Array getArray(String arg0) throws SQLException {
    throw new SQLException("Arrays are not supported");
  }

  @Override
  public InputStream getAsciiStream(int arg0) throws SQLException {
    throw new SQLException("ASCII streams are not supported");
  }

  @Override
  public InputStream getAsciiStream(String arg0) throws SQLException {
    throw new SQLException("ASCII streams are not supported");
  }

  @Override
  public BigDecimal getBigDecimal(int index) throws SQLException {
    try {
      BigDecimal d = rowMeta.getBigNumber(rows.get(currentIndex), index-1);
      if (d==null) {
        lastNull=true;
      }
      lastNull=false;
      return d;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public BigDecimal getBigDecimal(String columnName) throws SQLException {
    return getBigDecimal(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public BigDecimal getBigDecimal(int index, int arg1) throws SQLException {
    return getBigDecimal(index);
  }

  @Override
  public BigDecimal getBigDecimal(String columnName, int arg1) throws SQLException {
    return getBigDecimal(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public InputStream getBinaryStream(int arg0) throws SQLException {
    throw new SQLException("Binary streams are not supported");
  }

  @Override
  public InputStream getBinaryStream(String arg0) throws SQLException {
    throw new SQLException("Binary streams are not supported");
  }

  @Override
  public Blob getBlob(int index) throws SQLException {
    throw new SQLException("BLOBs are not supported");
  }

  @Override
  public Blob getBlob(String arg0) throws SQLException {
    throw new SQLException("BLOBs are not supported");
  }

  @Override
  public boolean getBoolean(int index) throws SQLException {
    try {
      Boolean b = rowMeta.getBoolean(rows.get(currentIndex), index-1);
      if (b==null) {
        lastNull=true;
      }
      lastNull=false;
      return b;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public boolean getBoolean(String columnName) throws SQLException {
    return getBoolean(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public byte getByte(int index) throws SQLException {
    long l = getLong(index);
    return (byte)l;
  }

  @Override
  public byte getByte(String columnName) throws SQLException {
    return getByte(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public byte[] getBytes(int index) throws SQLException {
    try {
      byte[] binary = rowMeta.getBinary(rows.get(currentIndex), index-1);
      if (binary==null) {
        lastNull=true;
        return null;
      }
      lastNull=false;
      return binary;
    } catch(Exception e) { 
      throw new SQLException(e);
    }  }

  @Override
  public byte[] getBytes(String columnName) throws SQLException {
    return getBytes(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public Reader getCharacterStream(int arg0) throws SQLException {
    throw new SQLException("Character streams are not supported");
  }

  @Override
  public Reader getCharacterStream(String arg0) throws SQLException {
    throw new SQLException("Character streams are not supported");
  }

  @Override
  public Clob getClob(int arg0) throws SQLException {
    throw new SQLException("CLOBs are not supported");
  }

  @Override
  public Clob getClob(String arg0) throws SQLException {
    throw new SQLException("CLOBs are not supported");
  }

  @Override
  public float getFloat(int index) throws SQLException {
    double d = getDouble(index);
    return (float)d;
  }

  @Override
  public float getFloat(String columnName) throws SQLException {
    double d = getDouble(columnName);
    return (float)d;
  }

  @Override
  public int getInt(int index) throws SQLException {
    long l = getLong(index);
    return (int)l;
  }

  @Override
  public int getInt(String columnName) throws SQLException {
    return getInt(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public long getLong(int index) throws SQLException {
    try {
      Long d = rowMeta.getInteger(rows.get(currentIndex), index-1);
      if (d==null) {
        lastNull=true;
      }
      lastNull=false;
      return d;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public long getLong(String columnName) throws SQLException {
    return getLong(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public Reader getNCharacterStream(int arg0) throws SQLException {
    throw new SQLException("NCharacter streams are not supported");
  }

  @Override
  public Reader getNCharacterStream(String arg0) throws SQLException {
    throw new SQLException("NCharacter streams are not supported");
  }

  @Override
  public NClob getNClob(int arg0) throws SQLException {
    throw new SQLException("NCLOBs are not supported");
  }

  @Override
  public NClob getNClob(String arg0) throws SQLException {
    throw new SQLException("NCLOBs are not supported");
  }

  @Override
  public String getNString(int arg0) throws SQLException {
    throw new SQLException("NStrings are not supported");
  }

  @Override
  public String getNString(String arg0) throws SQLException {
    throw new SQLException("NStrings are not supported");
  }

  @Override
  public Object getObject(int index) throws SQLException {
    return rows.get(currentIndex)[index];
  }

  @Override
  public Object getObject(String columnName) throws SQLException {
    return getObject(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public Object getObject(int index, Map<String, Class<?>> arg1) throws SQLException {
    return getObject(index);
  }

  @Override
  public Object getObject(String columnName, Map<String, Class<?>> arg1) throws SQLException {
    return getObject(columnName);
  }

  @Override
  public Ref getRef(int arg0) throws SQLException {
    throw new SQLException("Refs are not supported");
  }

  @Override
  public Ref getRef(String arg0) throws SQLException {
    throw new SQLException("Refs are not supported");
  }

  @Override
  public RowId getRowId(int arg0) throws SQLException {
    throw new SQLException("RowIDs are not supported");
  }

  @Override
  public RowId getRowId(String arg0) throws SQLException {
    throw new SQLException("RowIDs are not supported");
  }

  @Override
  public SQLXML getSQLXML(int arg0) throws SQLException {
    throw new SQLException("SQLXML is not supported");
  }

  @Override
  public SQLXML getSQLXML(String arg0) throws SQLException {
    throw new SQLException("SQLXML is not supported");
  }

  @Override
  public short getShort(int index) throws SQLException {
    long l = getLong(index);
    return (short)l;
  }

  @Override
  public short getShort(String columnName) throws SQLException {
    return getShort(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public String getString(int index) throws SQLException {
    try {
      String string = rowMeta.getString(rows.get(currentIndex), index-1);
      if (string==null) {
        lastNull=true;
      }
      lastNull=false;
      System.out.println("getString("+index+") --> "+string);
      return string;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public String getString(String columnName) throws SQLException {
    return getString(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public Time getTime(int arg0) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Time getTime(String arg0) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Time getTime(int arg0, Calendar arg1) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Time getTime(String arg0, Calendar arg1) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Timestamp getTimestamp(int index) throws SQLException {
    java.util.Date date = getDate(index);
    if (date==null) return null;
    return new Timestamp(date.getTime());
  }

  @Override
  public Timestamp getTimestamp(String columnName) throws SQLException {
    return getTimestamp(rowMeta.indexOfValue(columnName)+1);
  }

  @Override
  public Timestamp getTimestamp(int index, Calendar arg1) throws SQLException {
    return getTimestamp(index);
  }

  @Override
  public Timestamp getTimestamp(String columnName, Calendar arg1) throws SQLException {
    return getTimestamp(columnName);
  }

  @Override
  public URL getURL(int arg0) throws SQLException {
    throw new SQLException("URLs are not supported");
  }

  @Override
  public URL getURL(String arg0) throws SQLException {
    throw new SQLException("URLs are not supported");
  }

  @Override
  public InputStream getUnicodeStream(int arg0) throws SQLException {
    throw new SQLException("Unicode streams are not supported");
  }

  @Override
  public InputStream getUnicodeStream(String arg0) throws SQLException {
    throw new SQLException("Unicode streams are not supported");
  }


  
  
  
  
  
  
  
  
  
  // Update section below: all not supported...
  
  
  @Override
  public void updateArray(int arg0, Array arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateArray(String arg0, Array arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(int arg0, Blob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(String arg0, Blob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(int arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(String arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBoolean(int arg0, boolean arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBoolean(String arg0, boolean arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateByte(int arg0, byte arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateByte(String arg0, byte arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBytes(int arg0, byte[] arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBytes(String arg0, byte[] arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(int arg0, Clob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(String arg0, Clob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDate(int arg0, Date arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDate(String arg0, Date arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDouble(int arg0, double arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDouble(String arg0, double arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateFloat(int arg0, float arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateFloat(String arg0, float arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateInt(int arg0, int arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateInt(String arg0, int arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateLong(int arg0, long arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateLong(String arg0, long arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(int arg0, NClob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(String arg0, NClob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNString(int arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNString(String arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNull(int arg0) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNull(String arg0) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(int arg0, Object arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(String arg0, Object arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRef(int arg0, Ref arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRef(String arg0, Ref arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRow() throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRowId(int arg0, RowId arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRowId(String arg0, RowId arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateShort(int arg0, short arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateShort(String arg0, short arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateString(int arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateString(String arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTime(int arg0, Time arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTime(String arg0, Time arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return null;
  }

  @Override
  public int getConcurrency() throws SQLException {
    return ResultSet.CONCUR_READ_ONLY;
  }

  @Override
  public String getCursorName() throws SQLException {
    return "rows";
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return ResultSet.FETCH_FORWARD;
  }

  @Override
  public int getFetchSize() throws SQLException {
    return 1;
  }

  @Override
  public int getHoldability() throws SQLException {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return new ThinResultSetMetaData("rows", rowMeta);
  }

  @Override
  public int getRow() throws SQLException {
    return currentIndex;
  }

  @Override
  public Statement getStatement() throws SQLException {
    return null;
  }

  @Override
  public int getType() throws SQLException {
    return ResultSet.TYPE_FORWARD_ONLY;
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  @Override
  public void insertRow() throws SQLException {
  }

  @Override
  public boolean isAfterLast() throws SQLException {
    return currentIndex>=rows.size();
  }

  @Override
  public boolean isBeforeFirst() throws SQLException {
    return currentIndex<0;
  }

  @Override
  public boolean isClosed() throws SQLException {
    return false;
  }

  @Override
  public boolean isFirst() throws SQLException {
    return currentIndex==0;
  }

  @Override
  public boolean isLast() throws SQLException {
    return currentIndex==rows.size()-1;
  }

  @Override
  public boolean last() throws SQLException {
    currentIndex=rows.size()-1;
    return false;
  }

  @Override
  public void moveToCurrentRow() throws SQLException {
  }

  @Override
  public void moveToInsertRow() throws SQLException {
  }

  @Override
  public boolean next() throws SQLException {
    currentIndex++;
    boolean result = currentIndex<rows.size();
    System.out.println("next() --> "+result);
    return result;
  }

  @Override
  public boolean previous() throws SQLException {
    currentIndex--;
    return currentIndex>=0;
  }

  @Override
  public void refreshRow() throws SQLException {
  }

  @Override
  public boolean relative(int rows) throws SQLException {
    currentIndex+=rows;
    return true;
  }


  @Override
  public boolean rowDeleted() throws SQLException {
    return false;
  }

  @Override
  public boolean rowInserted() throws SQLException {
    return false;
  }

  @Override
  public boolean rowUpdated() throws SQLException {
    return false;
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
  }

}
