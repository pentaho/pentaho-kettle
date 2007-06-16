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

package be.ibridge.kettle.trans.step.dimensionlookup;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.hash.ByteArrayHashIndex;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DimensionLookupData extends BaseStepData implements StepDataInterface
{
	public Value   valueDateNow;
	
	public Database db;
	
	public Value min_date;
	public Value max_date;

	public int     keynrs[];      // nrs in row of the keys
	public int     fieldnrs[];    // nrs in row of the fields
	public int     datefieldnr;   // Nr of datefield field in row

    public Row keyMeta;

    public ByteArrayHashIndex cache;

    public Row valueMeta;

    public long smallestCacheKey;

    public Value notFoundTk;
    
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
