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
 

package be.ibridge.kettle.trans.step.rowsfromresult;

import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class RowsFromResultData extends BaseStepData implements StepDataInterface
{
	public int pointer;

	/**
	 * 
	 */
	public RowsFromResultData()
	{
		super();
	}

}
