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
 

package be.ibridge.kettle.trans.step.tableoutput;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Map;

import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class TableOutputData extends BaseStepData implements StepDataInterface
{
	public  Database db;
	public  int      warnings;
    
    /**
     * Mapping between the SQL and the actual prepared statement.
     * Normally this is only one, but in case we have more then one, it's convenient to have this.
     */
    public  Map      preparedStatements;
    
    public  int      indexOfPartitioningField;
    
    /** Cache of the data formatter object */
    public SimpleDateFormat dateFormater;

    /** Use batch mode or not? */
    public boolean batchMode;
    public int indexOfTableNameField;
    
	public TableOutputData()
	{
		super();
		
		db=null;
		warnings=0;
        
        preparedStatements = new Hashtable(); 
        
        indexOfPartitioningField = -1;
        indexOfTableNameField = -1;
	}

}
