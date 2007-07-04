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
 
package org.pentaho.di.trans.steps.append;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Sven Boden
 * @since 3-june-2007
 */
public class AppendData extends BaseStepData implements StepDataInterface
{
    public boolean processHead;
    public boolean processTail;
	public RowSet headRowSet;
	public RowSet tailRowSet;
	public RowMetaInterface outputRowMeta;   
    
	/**
	 * Default constructor.
	 */
	public AppendData()
	{
		super();
	}
}