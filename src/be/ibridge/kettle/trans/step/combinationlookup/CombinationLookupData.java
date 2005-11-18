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
