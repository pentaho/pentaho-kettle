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
 

package be.ibridge.kettle.trans.step.databaselookup;

import java.util.Hashtable;

import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DatabaseLookupData extends BaseStepData implements StepDataInterface
{
	public Hashtable look;       // to store values in used to look up things...
	public Database db;

	public Value  nullif[];         // Not found: default values...
	public int    keynrs[];         // nr of keylookup -value in row...
	public int    keynrs2[];        // nr of keylookup2-value in row...
	public int    keytypes[];       // Types of the desired database values

	/**
	 * 
	 */
	public DatabaseLookupData()
	{
		super();
		
		db=null;
	}

}
