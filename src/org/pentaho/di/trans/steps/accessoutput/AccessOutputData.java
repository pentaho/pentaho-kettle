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
 

package org.pentaho.di.trans.steps.accessoutput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class AccessOutputData extends BaseStepData implements StepDataInterface
{
    public  Database db;
	public  Table    table;
    public List<Column> columns;
    public List<Object[]> rows;
	public RowMetaInterface outputRowMeta;
	public boolean oneFileOpened;
        
	public AccessOutputData()
	{
		super();
        rows = new ArrayList<Object[]>();
        oneFileOpened=false;
	}

}
