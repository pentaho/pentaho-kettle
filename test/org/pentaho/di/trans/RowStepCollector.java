/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;


/**
 * Helper class for testcases. You can add an instance of this class to
 * a step to read all of the Rows the step read or wrote.
 *
 * @author Sven Boden
 */
public class RowStepCollector implements RowListener
{
	private List<RowMetaAndData> rowsRead;
	private List<RowMetaAndData> rowsWritten;
    private List<RowMetaAndData> rowsError;

	public RowStepCollector()
	{
		rowsRead = new ArrayList<RowMetaAndData>();
		rowsWritten = new ArrayList<RowMetaAndData>();
        rowsError = new ArrayList<RowMetaAndData>();
	}

    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row)
    {
    	rowsRead.add(new RowMetaAndData(rowMeta, row));
    }

    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
    {
    	rowsWritten.add(new RowMetaAndData(rowMeta, row));
    }

    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
    {
    	rowsError.add(new RowMetaAndData(rowMeta, row));
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