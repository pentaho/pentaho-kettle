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
 

package be.ibridge.kettle.trans.step.accessoutput;

import java.util.List;

import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class AccessOutputData extends BaseStepData implements StepDataInterface
{
	public  Database db;
	public  Table    table;
    public List columns;
        
	public AccessOutputData()
	{
		super();
	}

}
