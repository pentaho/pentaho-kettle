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
package org.pentaho.di.trans.steps.ifnull;

import java.util.HashMap;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class IfNullData extends BaseStepData implements StepDataInterface
{

	/**
	 * 
	 */
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	
	public int     fieldnrs[]; 
	public int     fieldnr;
	public String realReplaceByValue;
	public String realconversionMask;
	public boolean realSetEmptyString;
	
	
	public HashMap<String, Integer> ListTypes;
	public String defaultValues[];
	public String defaultMasks[];
	public boolean setEmptyString[];
	
	public IfNullData()
	{
		super();
		ListTypes = new HashMap<String, Integer>();
	}

}
