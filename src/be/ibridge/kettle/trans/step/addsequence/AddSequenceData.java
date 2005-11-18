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
 

package be.ibridge.kettle.trans.step.addsequence;

import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class AddSequenceData extends BaseStepData implements StepDataInterface
{
	private Database  db;
	private String lookup;
	
	/**
	 * 
	 */
	public AddSequenceData()
	{
		super();

		db=null;
	}

	/**
	 * @return Returns the db.
	 */
	public Database getDb()
	{
		return db;
	}
	
	/**
	 * @param db The db to set.
	 */
	public void setDb(Database db)
	{
		this.db = db;
	}
	
	/**
	 * @return Returns the lookup string usually "@@"+the name of the sequence.
	 */
	public String getLookup()
	{
		return lookup;
	}
	
	/**
	 * @param lookup the lookup string usually "@@"+the name of the sequence.
	 */
	public void setLookup(String lookup)
	{
		this.lookup = lookup;
	}
}
