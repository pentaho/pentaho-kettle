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
 

package be.ibridge.kettle.trans.step.sql;

import java.util.List;

import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 20-jan-2005
 */
public class ExecSQLData extends BaseStepData implements StepDataInterface
{
	public Database db;
    public Result   result;
    public int[]    argumentIndexes;
    public List     markerPositions;
	
	public ExecSQLData()
	{
		super();
		
		db              = null;
        result          = null;
        argumentIndexes = null;
        markerPositions = null;
	}


	
}
