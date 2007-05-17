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
 

package org.pentaho.di.trans.steps.databaselookup;

import java.util.Hashtable;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;




/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DatabaseLookupData extends BaseStepData implements StepDataInterface
{
	public Hashtable look;       // to store values in used to look up things...
	public Database db;

	public Object nullif[];         // Not found: default values...
	public int    keynrs[];         // nr of keylookup -value in row...
	public int    keynrs2[];        // nr of keylookup2-value in row...
	public int    keytypes[];       // Types of the desired database values
    
    public RowMetaInterface outputRowMeta;
    public RowMetaInterface lookupMeta;
    public RowMetaInterface returnMeta;


	/**
	 * 
	 */
	public DatabaseLookupData()
	{
		super();
		
		db=null;
	}

}
