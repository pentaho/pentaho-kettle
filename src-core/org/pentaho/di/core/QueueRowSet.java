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
package org.pentaho.di.core;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * A simplified rowset for steps for single threaded execution.  This row set has no limited size.
 *  
 * @author matt
 */
public class QueueRowSet extends BaseRowSet implements Comparable<RowSet>, RowSet {

	private LinkedList<Object[]> buffer;
	
	public QueueRowSet() {
      buffer = new LinkedList<Object[]>();
    }

	public Object[] getRow() {
		Object[] retRow = buffer.pollFirst();
		return retRow;
	}

	public Object[] getRowImmediate() {
		return getRow();
	}

	public Object[] getRowWait(long timeout, TimeUnit tu) {
		return getRow();
	}

	public boolean putRow(RowMetaInterface rowMeta, Object[] rowData) {
		this.rowMeta = rowMeta;
		buffer.add(rowData);
		return true;
	}

	public boolean putRowWait(RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu) {
		return putRow(rowMeta, rowData);
	}

	public int size() {
		return buffer.size();
	}
}
