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
 * Copyright Kettle Project.
 */

package org.pentaho.di.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;


/**
 * Helper class for collecting the datas. 
 *
 * @author tomqin
 */
public class RowStepListener implements RowListener
{
	private transient final Log log = LogFactory.getLog(RowStepListener.class);
	private List<RowMetaAndData> rowsRead;
	private List<RowMetaAndData> rowsWritten;
    private List<RowMetaAndData> rowsError;

	public RowStepListener()
	{
		rowsRead = new ArrayList<RowMetaAndData>();
		rowsWritten = new ArrayList<RowMetaAndData>();
        rowsError = new ArrayList<RowMetaAndData>();
	}

    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row)
    {
//    	log.debug("add the row....");
//    	rowsRead.add(new RowMetaAndData(rowMeta, row));
    }

    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
    {
    	log.debug("write the row....");
    	rowsWritten.add(new RowMetaAndData(rowMeta, row));
    }

    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
    {
//    	rowsError.add(new RowMetaAndData(rowMeta, row));
    }

	/**
	 * Clear the rows read and rows written.
	 */
	public void clear()
	{
		rowsRead.clear();
		rowsWritten.clear();
        rowsError.clear();
	}

	public List<RowMetaAndData> getRowsRead()
	{
		return rowsRead;
	}

	public List<RowMetaAndData> getRowsWritten()
	{
		return rowsWritten;
	}

    public List<RowMetaAndData> getRowsError()
    {
        return rowsError;
    }
}