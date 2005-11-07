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
 

package be.ibridge.kettle.trans.step.insertupdate;

import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class InsertUpdateData extends BaseStepData implements StepDataInterface
{
	public Database dblup, dbins, dbupd;

	public int    keynrs[];         // nr of keylookup -value in row...
	public int    keynrs2[];        // nr of keylookup2-value in row...
	public int    valuenrs[];       // Stream valuename nrs to prevent searches.
	
	/**
	 * 
	 */
	public InsertUpdateData()
	{
		super();

		dblup=null;
		dbins=null;
		dbupd=null;
	}

}
