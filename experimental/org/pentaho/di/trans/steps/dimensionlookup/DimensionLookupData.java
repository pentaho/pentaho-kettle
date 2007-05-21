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

package org.pentaho.di.trans.steps.dimensionlookup;

import java.sql.PreparedStatement;
import java.util.Date;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.hash.ByteArrayHashIndex;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DimensionLookupData extends BaseStepData implements StepDataInterface
{
	public Date   valueDateNow;
	
	public Database db;
	
	public Date  min_date;
	public Date   max_date;

	public int     keynrs[];      // nrs in row of the keys
	public int     fieldnrs[];    // nrs in row of the fields
	public int     datefieldnr;   // Nr of datefield field in row

    public ByteArrayHashIndex cache;

    public long smallestCacheKey;

    public Long notFoundTk;

    public RowMetaInterface outputRowMeta;

    public RowMetaInterface lookupRowMeta;
    public RowMetaInterface returnRowMeta;

    public PreparedStatement prepStatementLookup;
    public PreparedStatement prepStatementInsert;
    public PreparedStatement prepStatementUpdate;
    public PreparedStatement prepStatementDimensionUpdate;
    public PreparedStatement prepStatementPunchThrough;

    public RowMetaInterface insertRowMeta;
    public RowMetaInterface updateRowMeta;
    public RowMetaInterface dimensionUpdateRowMeta;
    public RowMetaInterface punchThroughRowMeta;

    public RowMetaInterface cacheKeyRowMeta;  
    public RowMetaInterface cacheValueRowMeta;

    public String schemaTable;

	/**
	 * 
	 */
	public DimensionLookupData()
	{
		super();

		db=null;
		valueDateNow=null;
        smallestCacheKey=-1;
	}

}
