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

package be.ibridge.kettle.trans.step.xsdvalidator;

import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 14-08-2007
 *
 */
public class XsdValidatorData extends BaseStepData implements StepDataInterface
{

	
	public int fields_used[];
	
	/**
	 * 
	 */
	public XsdValidatorData()
	{
		super();

		fields_used=null;
	}

}
