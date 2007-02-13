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

package be.ibridge.kettle.trans.step;

import java.util.ArrayList;
import java.util.List;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.step.RowListener;

/**
 * Helper class for testcases. You can add an instance of this class to
 * a step to read all of the Rows the step read or wrote.
 *
 * @author Sven Boden
 */
public class RowStepCollector implements RowListener
{
	private List rowsRead;
	private List rowsWritten;
    private List rowsError;
	
	public RowStepCollector()
	{
		rowsRead = new ArrayList();
		rowsWritten = new ArrayList();
        rowsError = new ArrayList();
	}

	public void rowReadEvent(Row row) {
		rowsRead.add(row);
	}

	public void rowWrittenEvent(Row row) {
		rowsWritten.add(row);
	}
    
    public void errorRowWrittenEvent(Row row)
    {
        rowsError.add(row);
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
	
	public List getRowsRead()
	{
		return rowsRead;
	}
	
	public List getRowsWritten()
	{
		return rowsWritten;
	}	
    
    public List getRowsError()
    {
        return rowsError;
    }
}