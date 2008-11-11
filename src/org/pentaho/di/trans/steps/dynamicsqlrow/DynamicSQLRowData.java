/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.trans.steps.dynamicsqlrow;

import java.util.ArrayList;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DynamicSQLRowData extends BaseStepData implements StepDataInterface
{
	RowMetaInterface outputRowMeta;
	RowMetaInterface lookupRowMeta;
	
	public Database db;

	public Object[]    notfound; // Values in case nothing is found...

	public int indexOfSQLField;
	
	public boolean skipPreviousRow;
	
	public String previousSQL;
	
	public ArrayList<Object[]> previousrowbuffer;
	
	public boolean isCanceled;
	
	/**
	 * 
	 */
	public DynamicSQLRowData()
	{
		super();
		
		db=null;
		notfound=null;
		indexOfSQLField=-1;
		skipPreviousRow=false;
		previousSQL=null;
		previousrowbuffer=new ArrayList<Object[]>();
	}
}
