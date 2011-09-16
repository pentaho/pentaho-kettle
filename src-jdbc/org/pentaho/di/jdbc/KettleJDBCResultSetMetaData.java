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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;

public class KettleJDBCResultSetMetaData implements ResultSetMetaData {
	// private List<RowMetaAndData> rowAndDatas;
	private ColInfo[] columns;
	private transient final Log log = LogFactory
			.getLog(KettleJDBCResultSetMetaData.class);

	public ColInfo[] getColumns() {
		return columns;
	}

	public void setColumns(ColInfo[] columns) {
		this.columns = columns;
	}

	private int columnCount = 0;
	private boolean useLOBs = false;

	public KettleJDBCResultSetMetaData() {

	}

	public KettleJDBCResultSetMetaData(ColInfo[] columns, int columnCount,
			boolean useLOBs) {
		this.columns = columns;
		this.columnCount = columnCount;
		this.useLOBs = useLOBs;
	}

	public KettleJDBCResultSetMetaData(List<RowMetaAndData> rowAndDatas, String columnStr) {
		// this.rowAndDatas = rowAndDatas;
		if (rowAndDatas != null && rowAndDatas.size() > 0) {
			RowMetaAndData row = rowAndDatas.get(0);
			RowMeta rm = (RowMeta) row.getRowMeta();
			if (columnStr.indexOf("*") != -1) {

				this.columns = KettleHelper.convert(rm);
//				this.columnCount = row.getRowMeta().size();
			} else {
				this.columns = KettleHelper.convert(rm,columnStr);
				

			}
			this.columnCount = this.columns.length;
		}
		log.debug("KettleJDBCResultSetMetaData:" + rowAndDatas);
	}

	public String getCatalogName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnClassName(int column) throws SQLException {
		String c = Support.getClassName(getColumnType(column));

		if (!useLOBs) {
			if ("java.sql.Clob".equals(c)) {
				return "java.lang.String";
			}

			if ("java.sql.Blob".equals(c)) {
				return "[B";
			}
		}

		return c;
	}

	public int getColumnCount() throws SQLException {
		log.debug("columnCount=" + columnCount);
		return this.columnCount;
	}

	public int getColumnDisplaySize(int column) throws SQLException {
		log.debug("getColumnDisplaySize");
		return 0;
	}

	public String getColumnLabel(int column) throws SQLException {
		log.debug("getColumnLabel");
		return getColumn(column).name;
	}

	public String getColumnName(int column) throws SQLException {
		return getColumn(column).realName;
	}

	public int getColumnType(int column) throws SQLException {
		if (useLOBs) {
			return getColumn(column).jdbcType;
		} else {
			return Support.convertLOBType(getColumn(column).jdbcType);
		}
	}

	public String getColumnTypeName(int column) throws SQLException {
		return getColumn(column).sqlType;
	}

	public int getPrecision(int column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getScale(int column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSchemaName(int column) throws SQLException {
		log.debug("getSchemaName");
		return null;
	}

	public String getTableName(int column) throws SQLException {
		ColInfo col = getColumn(column);

		return (col.tableName == null) ? "" : col.tableName;
	}

	public boolean isAutoIncrement(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	ColInfo getColumn(int column) throws SQLException {
		if (column < 1 || column > columnCount) {
			throw new SQLException(Messages.get("error.resultset.colindex",
					Integer.toString(column)), "07009");
		}
		log.debug("getColumn");
		return columns[column - 1];
	}

	public boolean isCaseSensitive(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCurrency(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDefinitelyWritable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public int isNullable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isReadOnly(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSearchable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSigned(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isWritable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }	
}
