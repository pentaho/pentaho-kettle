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
 

package be.ibridge.kettle.trans.step.combinationlookup;

import java.util.Hashtable;

import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class CombinationLookupData extends BaseStepData implements StepDataInterface
{
	public Database db;
	public int keynrs[];      // nrs in row of the keys
	
	// TODO: limit the database cache, we store 1000's of rows here + the corresponding technical key
	public Hashtable cache;  

	/**
	 * 
	 */
	public CombinationLookupData()
	{
		super();
		
		db=null;
	}

}
