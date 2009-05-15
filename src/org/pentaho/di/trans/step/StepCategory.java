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

import org.pentaho.di.i18n.BaseMessages;

/**
 * Different types of steps; right now used by the Step annotation
 * @author Alex Silva
 *
 */

public class StepCategory
{
	private static Class<?> PKG = StepCategory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	// Modify also: BaseStep.category_order to fix the order of appearance in Spoon 
	@Deprecated
	public static final StepCategory INPUT          = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Input"));
	@Deprecated
	public static final StepCategory OUTPUT         = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Output"));
	@Deprecated
	public static final StepCategory TRANSFORM      = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Transform"));
	@Deprecated
	public static final StepCategory SCRIPTING      = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Scripting"));
	@Deprecated
	public static final StepCategory LOOKUP         = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Lookup"));
	@Deprecated
	public static final StepCategory JOINS          = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Joins"));
	@Deprecated
	public static final StepCategory DATA_WAREHOUSE = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.DataWarehouse"));
	@Deprecated
	public static final StepCategory JOB            = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Job"));
	@Deprecated
	public static final StepCategory MAPPING        = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Mapping"));
	@Deprecated
	public static final StepCategory INLINE         = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Inline"));
	@Deprecated
	public static final StepCategory EXPERIMENTAL   = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Experimental"));
	@Deprecated
	public static final StepCategory DEPRECATED     = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Deprecated"));
	@Deprecated
	public static final StepCategory BULK           = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Bulk"));
	@Deprecated
	public static final StepCategory VALIDATION     = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Validation"));
	@Deprecated
	public static final StepCategory STATISTICS     = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Statistics"));
	@Deprecated
	public static final StepCategory UTILITY        = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Utility"));
	@Deprecated
	public static final StepCategory FLOW           = new StepCategory(BaseMessages.getString(PKG, "BaseStep.Category.Flow"));
	@Deprecated
	public static final int CATEGORY_USER_DEFINED   = -1;
	@Deprecated
	public static final int CATEGORY_INPUT          =  0;
	@Deprecated
	public static final int CATEGORY_OUTPUT         =  1;
	@Deprecated
	public static final int CATEGORY_TRANSFORM      =  2;
	@Deprecated
	public static final int CATEGORY_SCRIPTING      =  3;
	@Deprecated
	public static final int CATEGORY_LOOKUP         =  4;
	@Deprecated
	public static final int CATEGORY_JOINS          =  5;
	@Deprecated
	public static final int CATEGORY_DATA_WAREHOUSE =  6;
	@Deprecated
	public static final int CATEGORY_JOB            =  7;
	@Deprecated
	public static final int CATEGORY_MAPPING        =  8;
	@Deprecated
	public static final int CATEGORY_INLINE         =  9;
	@Deprecated
	public static final int CATEGORY_EXPERIMENTAL   = 10;
	@Deprecated
	public static final int CATEGORY_DEPRECATED     = 11;
	@Deprecated
	public static final int CATEGORY_BULK		    = 12;
	@Deprecated
	public static final int CATEGORY_VALIDATION		= 13;
	@Deprecated
	public static final int CATEGORY_STATISTICS		= 14;
	@Deprecated
	public static final int CATEGORY_UTILITY		= 15;
	@Deprecated
	public static final int CATEGORY_FLOW    		= 16;
	
    public static final StepCategory[] BRIDGE_ANNOTATION_CATEGORY_NUMBERS = new StepCategory[] { INPUT, OUTPUT, TRANSFORM, SCRIPTING, 
        LOOKUP, JOINS, DATA_WAREHOUSE, JOB, MAPPING, INLINE, EXPERIMENTAL, DEPRECATED, BULK, VALIDATION, STATISTICS, UTILITY, FLOW,};

	public static final StepCategory[] STANDARD_CATEGORIES = new StepCategory[] { INPUT, OUTPUT, TRANSFORM, UTILITY, FLOW, SCRIPTING, 
		LOOKUP, JOINS, DATA_WAREHOUSE, VALIDATION, STATISTICS, JOB, MAPPING, INLINE, EXPERIMENTAL, DEPRECATED, BULK,};

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