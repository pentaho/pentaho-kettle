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


package org.pentaho.di.trans.steps.combinationlookup;

import java.sql.PreparedStatement;
import java.util.Map;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 */
public class CombinationLookupData extends BaseStepData implements StepDataInterface
{
	public Database db;
	public int keynrs[];      // nrs in row of the keys

	public Map<RowMetaAndData, Long> cache;
    
    public RowMetaInterface outputRowMeta;
    public RowMetaInterface lookupRowMeta;
    public RowMetaInterface insertRowMeta;
    public RowMetaInterface hashRowMeta;
    
    public boolean[] removeField;
    
    public String schemaTable;
    
    public PreparedStatement prepStatementLookup;
    public PreparedStatement prepStatementInsert;
    public long smallestCacheKey;

	/**
	 *  Default Constructor
	 */
	public CombinationLookupData()
	{
		super();
		db=null;
	}
}