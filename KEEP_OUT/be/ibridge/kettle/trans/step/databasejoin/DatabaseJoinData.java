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
 

package be.ibridge.kettle.trans.step.databasejoin;

import java.sql.PreparedStatement;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DatabaseJoinData extends BaseStepData implements StepDataInterface
{
	public Database db;
	public PreparedStatement pstmt;

	public int    keynrs[]; // parameter value index in an input row...
	public Row    notfound; // Values in case nothing is found...

	/**
	 * 
	 */
	public DatabaseJoinData()
	{
		super();
		
		db=null;
		notfound=null;
	}

}
