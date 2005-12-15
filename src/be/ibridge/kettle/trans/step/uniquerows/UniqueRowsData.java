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

package be.ibridge.kettle.trans.step.uniquerows;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 22-jan-2005
 */
public class UniqueRowsData extends BaseStepData implements StepDataInterface
{
	public long	counter;
	public Row	previous;
	public int  fieldnrs[];
	public boolean ascending[];
    public boolean[] caseInsensitive;
	
	/**
	 * 
	 */
	public UniqueRowsData()
	{
		super();

		previous=null;
		counter=0;
	}

}
