/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
