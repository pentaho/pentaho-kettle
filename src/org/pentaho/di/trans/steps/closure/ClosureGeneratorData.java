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
 

package org.pentaho.di.trans.steps.closure;

import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 18-Sep-2007
 */
public class ClosureGeneratorData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	public int parentIndex;
	public int childIndex;
	public boolean reading;
	public ValueMetaInterface parentValueMeta;
	public ValueMetaInterface childValueMeta;
	public Map<Object, Object> map;
	public Map<Object, Long> parents;
	public Object topLevel;
	
	public ClosureGeneratorData()
	{
		super();
	}
}
