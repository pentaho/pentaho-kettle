 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 


package org.pentaho.di.trans.steps.gettablenames;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class GetTableNamesData extends BaseStepData implements StepDataInterface
{
	public Database db;
	public String realTableNameFieldName;
	public String realObjectTypeFieldName;
	public String realIsSystemObjectFieldName;
	public String realSQLCreationFieldName;
	public String realSchemaName;
	
	public RowMetaInterface outputRowMeta;
    public long                rownr;
    public RowMetaInterface inputRowMeta;
    public int totalpreviousfields;
    public int indexOfSchemaField;
    
    public Object[] readrow;
    
	/**
	 * 
	 */
	public GetTableNamesData()
	{
		super();
		db=null;
		realTableNameFieldName=null;
		realObjectTypeFieldName=null;
		realIsSystemObjectFieldName=null;
		realSQLCreationFieldName=null;
		rownr=0;
		realSchemaName=null;
		totalpreviousfields=0;
		readrow=null;
		indexOfSchemaField=-1;
	}

}
