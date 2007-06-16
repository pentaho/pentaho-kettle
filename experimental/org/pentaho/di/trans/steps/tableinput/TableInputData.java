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
 

package org.pentaho.di.trans.steps.tableinput;

import java.sql.ResultSet;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 20-jan-2005
 */
public class TableInputData extends BaseStepData implements StepDataInterface
{
	public Object[]  nextrow;
	public Object[]  thisrow;
	public Database  db;
	public ResultSet rs;
	public String    lookupStep;
    public RowMetaInterface rowMeta;
	
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
