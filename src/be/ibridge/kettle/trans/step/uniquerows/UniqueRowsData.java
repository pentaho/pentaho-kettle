 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
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
