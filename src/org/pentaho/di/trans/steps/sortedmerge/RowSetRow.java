/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.sortedmerge;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;

public class RowSetRow {
	private RowSet rowSet;
	private RowMetaInterface rowMeta;
	private Object[] rowData;
	
	/**
	 * @param rowSet
	 * @param rowData
	 */
	public RowSetRow(RowSet rowSet, RowMetaInterface rowMeta, Object[] rowData) {
		super();
		this.rowSet = rowSet;
		this.rowMeta = rowMeta;
		this.rowData = rowData;
	}
	
	/**
	 * @return the rowSet
	 */
	public RowSet getRowSet() {
		return rowSet;
	}
	
	/**
	 * @param rowSet the rowSet to set
	 */
	public void setRowSet(RowSet rowSet) {
		this.rowSet = rowSet;
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

	/**
	 * @return the rowMeta
	 */
	public RowMetaInterface getRowMeta() {
		return rowMeta;
	}

	/**
	 * @param rowMeta the rowMeta to set
	 */
	public void setRowMeta(RowMetaInterface rowMeta) {
		this.rowMeta = rowMeta;
	}
}
