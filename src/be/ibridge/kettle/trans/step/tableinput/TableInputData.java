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
 

package be.ibridge.kettle.trans.step.tableinput;

import java.sql.ResultSet;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 20-jan-2005
 */
public class TableInputData extends BaseStepData implements StepDataInterface
{
	public Row 		nextrow;
	public Row 		thisrow;
	public Database db;
	public ResultSet rs;
	public String   lookupStep;
	
	public TableInputData()
	{
		super();
		
		db         = null;
		thisrow    = null;
		nextrow    = null;
		rs         = null;
		lookupStep = null;
	}


	
}
