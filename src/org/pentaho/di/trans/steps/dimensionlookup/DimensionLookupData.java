 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.dimensionlookup;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.hash.ByteArrayHashMap;
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

    public ByteArrayHashMap cache;

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
    
    public String realTableName;
    public String realSchemaName;

	public int startDateChoice;

	public int startDateFieldIndex;

	public int[]	preloadKeyIndexes;

	public int	preloadFromDateIndex;
	public int	preloadToDateIndex;

	public DimensionCache	preloadCache;

	public List<Integer>	preloadIndexes;

  public List<Integer> lazyList;

  /**
   * The input row metadata, but converted to normal storage type
   */
  public RowMetaInterface inputRowMeta;

	/**
	 * 
	 */
	public DimensionLookupData()
	{
		super();

		db=null;
		valueDateNow=null;
        smallestCacheKey=-1;
        realTableName=null;
        realSchemaName=null;
	}

}
