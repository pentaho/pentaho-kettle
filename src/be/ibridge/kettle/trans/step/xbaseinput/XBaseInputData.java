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
 
package be.ibridge.kettle.trans.step.xbaseinput;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XBase;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * Provides data for the XBaseInput step.
 * 
 * @author Matt
 * @since 20-jan-2005
 */
public class XBaseInputData extends BaseStepData implements StepDataInterface
{
	public XBase xbi;
	public Row fields;

	public XBaseInputData()
	{
		super();

		xbi=null;
		fields=null;
	}

}
