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
 

package org.pentaho.di.trans.steps.accessoutput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.healthmarketscience.jackcess.Column;
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
    public List<Column> columns;
    public List<Object[]> rows;
	public RowMetaInterface outputRowMeta;
        
	public AccessOutputData()
	{
		super();
        rows = new ArrayList<Object[]>();
	}

}
