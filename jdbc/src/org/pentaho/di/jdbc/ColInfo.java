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
 * Copyright (C) 2004 The jTDS Project
 */
package org.pentaho.di.jdbc;

/**
 * 
 * @author TOMQIN
 *
 */
public class ColInfo {
  
    /** JDBC type constant from java.sql.Types */
    int jdbcType;
    public int getJdbcType() {
		return jdbcType;
	}
	public void setJdbcType(int jdbcType) {
		this.jdbcType = jdbcType;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getCatalog() {
		return catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public int getNullable() {
		return nullable;
	}
	public void setNullable(int nullable) {
		this.nullable = nullable;
	}
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
	public void setCaseSensitive(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
	}
	public boolean isWriteable() {
		return isWriteable;
	}
	public void setWriteable(boolean isWriteable) {
		this.isWriteable = isWriteable;
	}
	public boolean isIdentity() {
		return isIdentity;
	}
	public void setIdentity(boolean isIdentity) {
		this.isIdentity = isIdentity;
	}
	public boolean isKey() {
		return isKey;
	}
	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}
	public boolean isHidden() {
		return isHidden;
	}
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
	public int getUserType() {
		return userType;
	}
	public void setUserType(int userType) {
		this.userType = userType;
	}
	public CharsetInfo getCharsetInfo() {
		return charsetInfo;
	}
	public void setCharsetInfo(CharsetInfo charsetInfo) {
		this.charsetInfo = charsetInfo;
	}
	public int getDisplaySize() {
		return displaySize;
	}
	public void setDisplaySize(int displaySize) {
		this.displaySize = displaySize;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	public int getPrecision() {
		return precision;
	}
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
	public String getSqlType() {
		return sqlType;
	}
	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}
	
	public String toString()
	{
		return "table:"+this.tableName+" , column name:"+this.realName+",nick name:"+this.name;
	}
	/** Column actual table name */
    String realName;
    /** Column label / name */
    String name;
    /** Table name owning this column */
    String tableName;
    /** Database owning this column */
    String catalog;
    /** User owning this column */
    String schema;
    /** Column data type supports SQL NULL */
    int nullable;
    /** Column name is case sensitive */
    boolean isCaseSensitive;
    /** Column may be updated */
    boolean isWriteable;
    /** Column is an indentity column */
    boolean isIdentity;
    /** Column may be used as a key */
    boolean isKey;
    /** Column should be hidden */
    boolean isHidden;
    /** Database ID for UDT */
    int userType;
  
    /** Character set descriptor (if different from default) */
    CharsetInfo charsetInfo;
    /** Column display size */
    int displaySize;
    /** Column buffer (max) size */
    int bufferSize;
    /** Column decimal precision */
    int precision;
    /** Column decimal scale */
    int scale;
    /** The SQL type name for this column. */
    String sqlType;
}