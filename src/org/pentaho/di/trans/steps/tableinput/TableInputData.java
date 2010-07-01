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
 

package org.pentaho.di.trans.steps.tableinput;

import java.sql.ResultSet;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

/**
 * @author Matt
 * @since 20-jan-2005
 */
public class TableInputData extends BaseStepData implements StepDataInterface
{
	public Object[]  nextrow;
	public Object[]  thisrow;
	public Database  db;
	public ResultSet rs;
	public String    lookupStep;
    public RowMetaInterface rowMeta;
	public RowSet rowSet;
	public boolean isCanceled;
    public StreamInterface infoStream;
	
	public TableInputData()
	{
		super();
		
		db         = null;
		thisrow    = null;
		nextrow    = null;
		rs         = null;
		lookupStep = null;
	}


	
}
