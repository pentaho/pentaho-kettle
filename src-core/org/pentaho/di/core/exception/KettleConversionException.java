/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.exception;

import java.util.List;

import org.pentaho.di.core.row.ValueMetaInterface;

public class KettleConversionException extends KettleException {

	private List<Exception> causes;
	private List<ValueMetaInterface>	fields;
	private Object[] rowData;
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1697154653111622296L;

	/** 
	 * Constructs a new throwable with null as its detail message.
	 */
	public KettleConversionException()
	{
		super();
	}

	/**
	 * Constructs a new throwable with the specified detail message and cause.
	 * @param message the detail message (which is saved for later retrieval by the getMessage() method).
	 * @param causes the causes of the conversion errors
	 * @param fields the failing fields
	 * @param rowData the row with the failed fields set to null.
	 */
	public KettleConversionException(String message, List<Exception> causes, List<ValueMetaInterface> fields, Object[] rowData)
	{
		super(message);
		this.causes = causes;
		this.fields = fields;
		this.rowData = rowData;
	}

	/**
	 * @return the causes
	 */
	public List<Exception> getCauses() {
		return causes;
	}

	/**
	 * @param causes the causes to set
	 */
	public void setCauses(List<Exception> causes) {
		this.causes = causes;
	}

	/**
	 * @return the fields
	 */
	public List<ValueMetaInterface> getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(List<ValueMetaInterface> fields) {
		this.fields = fields;
	}

	/**
	 * @return the rowData
	 */
	public Object[] getRowData() {
		return rowData;
	}

	/**
	 * @param rowData the rowData to set
	 */
	public void setRowData(Object[] rowData) {
		this.rowData = rowData;
	}
}
