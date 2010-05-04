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
package org.pentaho.di.core.row;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of data rows as well as the RowMetaInterface to describe it.
 * 
 * @author matt
 *
 */
public class RowBuffer {
	private RowMetaInterface rowMeta;
	private List<Object[]> buffer;
	/**
	 * @param rowMeta
	 * @param buffer
	 */
	public RowBuffer(RowMetaInterface rowMeta, List<Object[]> buffer) {
		this.rowMeta = rowMeta;
		this.buffer = buffer;
	}
	
	/**
	 * @param rowMeta
	 */
	public RowBuffer(RowMetaInterface rowMeta) {
		this(rowMeta, new ArrayList<Object[]>());
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

	/**
	 * @return the buffer
	 */
	public List<Object[]> getBuffer() {
		return buffer;
	}

	/**
	 * @param buffer the buffer to set
	 */
	public void setBuffer(List<Object[]> buffer) {
		this.buffer = buffer;
	}
}
