/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 */

package org.pentaho.di.jdbc;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.i18n.BaseMessages;

public class KettleJDBCResultSet implements ResultSet {
  private static Class<?> PKG = KettleDriver.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private List<RowMetaAndData> rowAndDatas;
	/** Number of visible columns in row. */
    protected int columnCount;
    /** Index of current row in rowData. */
    protected int rowPtr=0;
    /**     current row      */
	private Object[] currentRow;
	protected ColInfo[] columns;
	// private boolean wasNull;
	String columnStr;
	private HashMap<String,Integer> columnMap;
	private Statement statement;
	private transient final Log log = LogFactory
	.getLog(KettleJDBCResultSet.class);
	// private int pos;
	 protected static final int POS_BEFORE_FIRST = 0;
	    protected static final int POS_AFTER_LAST = -1;
	
	public KettleJDBCResultSet()
	{
		
	}
	
	public KettleJDBCResultSet(Statement statement,List<RowMetaAndData> rowAndDatas,String columnStr)
	{
		log.debug("KettleJDBCResultSet:"+rowAndDatas);
		this.statement = statement;
		this.rowAndDatas = rowAndDatas;
		this.columnStr = columnStr;
		if(this.rowAndDatas!=null&&this.rowAndDatas.size()>0)
			this.columnCount = (this.rowAndDatas.get(0)).getRowMeta().size();
		int counter=0;
		if(this.rowAndDatas!=null)
		{
			counter=this.rowAndDatas.size();
		}
		log.debug("row counter="+counter);
	}
	public boolean absolute(int row) throws SQLException {
		this.rowPtr = row;
//		if(currentRow==null)
		
		currentRow = this.rowAndDatas.get(rowPtr-1).getData();
		
		
		return true;
	}

	public void afterLast() throws SQLException {
		
		this.rowPtr=this.rowAndDatas.size()+1;
	}

	public void beforeFirst() throws SQLException {
		
		this.rowPtr = 0;
	}

	public void cancelRowUpdates() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void close() throws SQLException {
		

	}

	public void deleteRow() throws SQLException {
		// TODO Auto-generated method stub

	}

	public int findColumn(String columnName) throws SQLException {
		
		KettleJDBCResultSetMetaData rm = (KettleJDBCResultSetMetaData)getMetaData();
		columns=rm.getColumns();
		
		 if (columnMap == null) {
	            columnMap = new HashMap<String,Integer>(columnCount);
	        } else {
	            Object pos = columnMap.get(columnName);
	            if (pos != null) {
	                return ((Integer) pos).intValue();
	            }
	        }

	        
	        for (int i = 0; i < columnCount; i++) {
	            if (columns[i].realName.equalsIgnoreCase(columnName)) {
	                columnMap.put(columnName, new Integer(i + 1));

	                return i + 1;
	            }
	        }

	        throw new SQLException(BaseMessages.getString(PKG, "error.resultset.colname", columnName), "07009");
	
	}

	public boolean first() throws SQLException {
//		rowPtr=1;
		return false;
	}

	public Array getArray(int i) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Array getArray(String colName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return (BigDecimal) Support.convert(this, getColumn(columnIndex), java.sql.Types.DECIMAL, null);
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		 return getBigDecimal(findColumn(columnName));
	}

	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public BigDecimal getBigDecimal(String columnName, int scale)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Blob getBlob(int i) throws SQLException {
		return (Blob) Support.convert(this, getColumn(i), java.sql.Types.BLOB, null);
	}

	public Blob getBlob(String colName) throws SQLException {
		 return getBlob(findColumn(colName));
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getBoolean(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public byte getByte(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte getByte(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getBytes(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Clob getClob(int i) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Clob getClob(String colName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getConcurrency() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCursorName() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDate(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDate(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public double getDouble(int columnIndex) throws SQLException {
		return ((Double) Support.convert(this, getColumn(columnIndex), java.sql.Types.DOUBLE, null)).doubleValue();
	}

	public double getDouble(String columnName) throws SQLException {
		 return getDouble(findColumn(columnName));
	}

	public int getFetchDirection() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getFetchSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getFloat(int columnIndex) throws SQLException {
		 return ((Float) Support.convert(this, getColumn(columnIndex), java.sql.Types.REAL, null)).floatValue();
	}

	public float getFloat(String columnName) throws SQLException {
		 return getFloat(findColumn(columnName));
	}

	public int getInt(int columnIndex) throws SQLException {
		 return ((Integer) Support.convert(this, getColumn(columnIndex), java.sql.Types.INTEGER, null)).intValue();
	}

	public int getInt(String columnName) throws SQLException {
		 return getInt(findColumn(columnName));
	}

	public long getLong(int columnIndex) throws SQLException {
		return ((Long) Support.convert(this, getColumn(columnIndex), java.sql.Types.BIGINT, null)).longValue();
	}

	public long getLong(String columnName) throws SQLException {
		return getLong(findColumn(columnName));
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		log.debug("getMetaData");
		KettleJDBCResultSetMetaData rsMeta = new KettleJDBCResultSetMetaData(rowAndDatas,columnStr);
		return rsMeta;
	}

	public Object getObject(int columnIndex) throws SQLException {
		 Object value = getColumn(columnIndex);

	       
	        if (value instanceof UniqueIdentifier) {
	            return value.toString();
	        }
	       
	        if (value instanceof DateTime) {
	            return ((DateTime) value).toObject();
	        }
	       

	        return value;
	}
	
	 /**
     * Get the specified column's data item.
     *
     * @param index the column index in the row
     * @return the column value as an <code>Object</code>
     * @throws SQLException if the connection is closed;
     *         if <code>index</code> is less than <code>1</code>;
     *         if <code>index</code> is greater that the number of columns;
     *         if there is no current row
     */
    protected Object getColumn(int index) throws SQLException {
        

        if (index < 1 || index > columnCount) {
            throw new SQLException(BaseMessages.getString(PKG, "error.resultset.colindex",
                                                      Integer.toString(index)),
                                                       "07009");
        }

        if (currentRow == null) {
            throw new SQLException(BaseMessages.getString(PKG, "error.resultset.norow"), "24000");
        }

        Object data = currentRow[index-1];

        // wasNull = data == null;

        return data;
    }

	public Object getObject(String columnName) throws SQLException {
		
		 return getObject(findColumn(columnName));
	}

	public Object getObject(int i, Map<String, Class<?>> map)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getObject(String colName, Map<String, Class<?>> map)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Ref getRef(int i) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Ref getRef(String colName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRow() throws SQLException {
		
		return rowPtr > 0 ? rowPtr : 0;
	}

	public short getShort(int columnIndex) throws SQLException {
		 return ((Integer) Support.convert(this, getColumn(columnIndex), java.sql.Types.SMALLINT, null)).shortValue();
	}

	public short getShort(String columnName) throws SQLException {
		return getShort(findColumn(columnName));
	}

	public Statement getStatement() throws SQLException {
		return this.statement;
	}

	public String getString(int columnIndex) throws SQLException {
		 Object tmp = getColumn(columnIndex);

	        if (tmp instanceof String) {
	            return (String) tmp;
	        }
//	        return (String) Support.convert(this, tmp, java.sql.Types.VARCHAR, getConnection().getCharset());
	        return (String) Support.convert(this, tmp, java.sql.Types.VARCHAR, "ISO-8859-1");
	        //"ISO-8859-1"
	}

	public String getString(String columnName) throws SQLException {
		return getString(findColumn(columnName));
	}

	public Time getTime(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getTime(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Timestamp getTimestamp(String columnName, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType() throws SQLException {
		//not Scrollable resultset 
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	public URL getURL(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getURL(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public InputStream getUnicodeStream(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void insertRow() throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean isAfterLast() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBeforeFirst() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isFirst() throws SQLException {
		 return rowPtr == 1;
	}

	public boolean isLast() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean last() throws SQLException {
		rowPtr=this.rowAndDatas.size();
		return true;
	}

	public void moveToCurrentRow() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void moveToInsertRow() throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean next() throws SQLException {
		log.debug("next.....");
		if(rowPtr<this.rowAndDatas.size())
		{
			this.currentRow = this.rowAndDatas.get(rowPtr).getData();
			rowPtr++;
			return true;
		}
		
		return false;
	}

	public boolean previous() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void refreshRow() throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean relative(int rows) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean rowDeleted() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean rowInserted() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean rowUpdated() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFetchDirection(int direction) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void setFetchSize(int rows) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateArray(String columnName, Array x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateAsciiStream(String columnName, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBigDecimal(String columnName, BigDecimal x)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBinaryStream(String columnName, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateByte(String columnName, byte x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateCharacterStream(String columnName, Reader reader,
			int length) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateDate(String columnName, Date x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateDouble(String columnName, double x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateFloat(String columnName, float x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateInt(String columnName, int x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateLong(String columnName, long x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateNull(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateNull(String columnName) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateObject(String columnName, Object x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateObject(int columnIndex, Object x, int scale)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateObject(String columnName, Object x, int scale)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateRow() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateShort(String columnName, short x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateString(int columnIndex, String x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateString(String columnName, String x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateTime(String columnName, Time x) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public void updateTimestamp(String columnName, Timestamp x)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean wasNull() throws SQLException {
		
		return false;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public int getRowCount() throws SQLException
	{
		int counter = -1;
		if(this.rowAndDatas!=null)
		{
			counter=this.rowAndDatas.size();
		}
		return counter;
	}

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public int getHoldability() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  public Reader getNCharacterStream(int arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader getNCharacterStream(String arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public NClob getNClob(int arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public NClob getNClob(String arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNString(int arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNString(String arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public RowId getRowId(int arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public RowId getRowId(String arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public SQLXML getSQLXML(int arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public SQLXML getSQLXML(String arg0) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isClosed() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBlob(int arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBlob(String arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateClob(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateClob(String arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNClob(int arg0, NClob arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNClob(String arg0, NClob arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNClob(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNClob(String arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNString(int arg0, String arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateNString(String arg0, String arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateRowId(int arg0, RowId arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateRowId(String arg0, RowId arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

}
