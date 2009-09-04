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
 

package org.pentaho.di.trans.steps.addsequence;

import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class AddSequenceData extends BaseStepData implements StepDataInterface
{
	private Database db;
	private String lookup;
    public RowMetaInterface outputRowMeta;
	public Counter counter;

	// The runtime values, in which the environment variables are already resolved
	public long  start;
	public long  increment;
	public long  maximum;
	
	public String realSchemaName;
	public String realSequenceName;
	
	/**
	 * 
	 */
	public AddSequenceData()
	{
		super();

		db=null;
		realSchemaName=null;
		realSequenceName=null;
	}

	/**
	 * @return Returns the db.
	 */
	public Database getDb()
	{
		return db;
	}
	
	/**
	 * @param db The db to set.
	 */
	public void setDb(Database db)
	{
		this.db = db;
	}
	
	/**
	 * @return Returns the lookup string usually "@@"+the name of the sequence.
	 */
	public String getLookup()
	{
		return lookup;
	}
	
	/**
	 * @param lookup the lookup string usually "@@"+the name of the sequence.
	 */
	public void setLookup(String lookup)
	{
		this.lookup = lookup;
	}
}