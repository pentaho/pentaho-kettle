/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.tablecompare;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 19-11-2009
 *
 */
public class TableCompareData extends BaseStepData implements StepDataInterface
{

	/**
	 * 
	 */
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	
	public int refSchemaIndex;
	public int refTableIndex;
	public int cmpSchemaIndex;
	public int cmpTableIndex;
	public int keyFieldsIndex;
	public int excludeFieldsIndex;
	
	public Database	referenceDb;
	public Database	compareDb;
	public RowMetaInterface	errorRowMeta;
	
	public int	keyDescIndex;
	public int	valueReferenceIndex;
	public int	valueCompareIndex;
	
	public TableCompareData()
	{
		super();
	}

}
