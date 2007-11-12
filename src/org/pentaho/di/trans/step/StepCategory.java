/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.step;

/**
 * Different types of steps; right now used by the Step annotation
 * @author Alex Silva
 *
 */
public class StepCategory
{
	public static final StepCategory INPUT          = new StepCategory(Messages.getString("BaseStep.Category.Input"));
	public static final StepCategory OUTPUT         = new StepCategory(Messages.getString("BaseStep.Category.Output"));
	public static final StepCategory TRANSFORM      = new StepCategory(Messages.getString("BaseStep.Category.Transform"));
	public static final StepCategory SCRIPTING      = new StepCategory(Messages.getString("BaseStep.Category.Scripting"));
	public static final StepCategory LOOKUP         = new StepCategory(Messages.getString("BaseStep.Category.Lookup"));
	public static final StepCategory JOINS          = new StepCategory(Messages.getString("BaseStep.Category.Joins"));
	public static final StepCategory DATA_WAREHOUSE = new StepCategory(Messages.getString("BaseStep.Category.DataWarehouse"));
	public static final StepCategory JOB            = new StepCategory(Messages.getString("BaseStep.Category.Job"));
	public static final StepCategory MAPPING        = new StepCategory(Messages.getString("BaseStep.Category.Mapping"));
	public static final StepCategory INLINE         = new StepCategory(Messages.getString("BaseStep.Category.Inline"));
	public static final StepCategory EXPERIMENTAL   = new StepCategory(Messages.getString("BaseStep.Category.Experimental"));
	public static final StepCategory DEPRECATED     = new StepCategory(Messages.getString("BaseStep.Category.Deprecated"));
	
	public static final int CATEGORY_USER_DEFINED   = -1;
	public static final int CATEGORY_INPUT          =  0;
	public static final int CATEGORY_OUTPUT         =  1;
	public static final int CATEGORY_TRANSFORM      =  2;
	public static final int CATEGORY_SCRIPTING      =  3;
	public static final int CATEGORY_LOOKUP         =  4;
	public static final int CATEGORY_JOINS          =  5;
	public static final int CATEGORY_DATA_WAREHOUSE =  6;
	public static final int CATEGORY_JOB            =  7;
	public static final int CATEGORY_MAPPING        =  8;
	public static final int CATEGORY_INLINE         =  9;
	public static final int CATEGORY_EXPERIMENTAL   = 10;
	public static final int CATEGORY_DEPRECATED     = 11;
	
	public static final StepCategory[] STANDARD_CATEGORIES = new StepCategory[] { INPUT, OUTPUT, TRANSFORM, SCRIPTING, 
		LOOKUP, JOINS, DATA_WAREHOUSE, JOB, MAPPING, INLINE, EXPERIMENTAL, DEPRECATED, };

	private String name;
	
	public StepCategory(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
}