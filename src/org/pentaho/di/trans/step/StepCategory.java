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
 * List of step categories, put in order
 */

public class StepCategory
{
	private static Class<?> PKG = StepCategory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public static final String[] CATEGORIES_IN_ORDER = new String[] {
		BaseMessages.getString(PKG, "BaseStep.Category.Input"),
		BaseMessages.getString(PKG, "BaseStep.Category.Output"),
		BaseMessages.getString(PKG, "BaseStep.Category.Transform"),
		BaseMessages.getString(PKG, "BaseStep.Category.Utility"),
		BaseMessages.getString(PKG, "BaseStep.Category.Flow"),
		BaseMessages.getString(PKG, "BaseStep.Category.Scripting"),
		BaseMessages.getString(PKG, "BaseStep.Category.Lookup"),
		BaseMessages.getString(PKG, "BaseStep.Category.Joins"),
		BaseMessages.getString(PKG, "BaseStep.Category.DataWarehouse"),
		BaseMessages.getString(PKG, "BaseStep.Category.Validation"),
		BaseMessages.getString(PKG, "BaseStep.Category.Statistics"),
		BaseMessages.getString(PKG, "BaseStep.Category.Job"),
		BaseMessages.getString(PKG, "BaseStep.Category.Mapping"),
		BaseMessages.getString(PKG, "BaseStep.Category.Inline"),
		BaseMessages.getString(PKG, "BaseStep.Category.Experimental"),
		BaseMessages.getString(PKG, "BaseStep.Category.Deprecated"),
		BaseMessages.getString(PKG, "BaseStep.Category.Bulk"),
		};
	
}
