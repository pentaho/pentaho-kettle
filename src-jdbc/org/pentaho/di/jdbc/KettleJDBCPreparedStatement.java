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
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KettleJDBCPreparedStatement extends KettleJDBCStatement implements
		PreparedStatement 
{
	ConnectionJDBC3 connection;
	String sql;
	String url;
	private transient final Log log = LogFactory
	.getLog(KettleJDBCPreparedStatement.class);
	
	
	public KettleJDBCPreparedStatement(ConnectionJDBC3 connection,
            String sql,String url)
	{
		super(connection,url);
		log.info("KettleJDBCPreparedStatement....");
		
		this.connection = connection;
		this.sql=sql;
		this.url= url;
	}
	
	public void addBatch() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void clearParameters() throws SQLException {
		// TODO Auto-generated method stub

	}

	public boolean execute() throws SQLException {
		
		return false;
	}

	public ResultSet executeQuery() throws SQLException {
		log.info("KettleJDBCPreparedStatement.executeQuery:"+sql);
		super.setUrl(url);
		super.setConnection(this.connection);
		super.execute(sql);
		
		return super.getResultSet();
	}

	public int executeUpdate() throws SQLException {
		
		return 0;
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		log.info("KettleJDBCPreparedStatement.getMetaData");
		KettleJDBCResultSetMetaData rsMeta = new KettleJDBCResultSetMetaData(rowAndDatas,columnStr);
		return rsMeta;
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setArray(int i, Array x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setBlob(int i, Blob x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setClob(int i, Clob x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setNull(int paramIndex, int sqlType, String typeName)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scale) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setRef(int i, Ref x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setString(int parameterIndex, String x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		throw new UnsupportedOperationException();

	}

	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

  public boolean isClosed() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isPoolable() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public void setPoolable(boolean arg0) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setBlob(int arg0, InputStream arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setClob(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setNClob(int arg0, NClob arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setNClob(int arg0, Reader arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setNString(int arg0, String arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setRowId(int arg0, RowId arg1) throws SQLException {
    // TODO Auto-generated method stub
  }

  public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
    // TODO Auto-generated method stub    
  }

}
