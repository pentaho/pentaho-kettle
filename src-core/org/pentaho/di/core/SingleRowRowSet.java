package org.pentaho.di.core;

import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * A simplified rowset for steps that always only need to only have a single row on input...
 *  
 * @author matt
 */
public class SingleRowRowSet extends BaseRowSet implements Comparable<RowSet>, RowSet {

	private Object[] row;

	public Object[] getRow() {
		Object[] retRow = row;
		row = null;
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
		this.row = rowData;
		return true;
	}

	@Override
	public boolean putRowWait(RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu) {
		return putRow(rowMeta, rowData);
	}

	@Override
	public int size() {
		return row==null ? 0 : 1;
	}
}
