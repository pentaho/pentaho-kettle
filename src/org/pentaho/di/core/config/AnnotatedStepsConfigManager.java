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
package org.pentaho.di.core.config;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Inject;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.util.ResolverUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.StepPluginMeta;
import org.pentaho.di.trans.step.Messages;
import org.pentaho.di.trans.step.StepCategory;

/**
 * Registers classes annotated with @Step as Kettle/PDI steps, without the need for XML configurations.
 * 
 * Note: XML configurations will supersede and overwrite annotated definitions.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */
public class AnnotatedStepsConfigManager<T extends StepPluginMeta> extends BasicConfigManager<T> 
{
	@Inject
	String packages;
	
	@SuppressWarnings("unchecked") //this is ok here because we defined T above.
	public Collection<T> load() throws KettleConfigException
	{
		ResolverUtil<StepPluginMeta> resolver = new ResolverUtil<StepPluginMeta>();
		
		// If there is a system wide property set with name KETTLE_PLUGIN_PACKAGES we search those packages as well
		//
		String allPackages = packages;
		String extraPackages = System.getProperty(Const.KETTLE_PLUGIN_PACKAGES);
		if (!Const.isEmpty(extraPackages)) {
			allPackages+=","+extraPackages;
		}
		
		resolver.find(new ResolverUtil.AnnotatedWith(Step.class), allPackages != null ? allPackages.split(",") : new String[] {});
		
		Collection<StepPluginMeta> steps = new LinkedHashSet<StepPluginMeta>(resolver.size());
		for (Class<?> clazz : resolver.getClasses())
		{
			Step step = clazz.getAnnotation(Step.class);
			String[] stepName = step.name();
			if (stepName.length == 1 && stepName[0].equals("")) // default
				stepName = new String[] { clazz.getName() };
			
			// The package name to get the descriptions or tool tip from...
			//
			String packageName = step.i18nPackageName();
			if (Const.isEmpty(packageName)) packageName = org.pentaho.di.trans.step.Messages.class.getPackage().getName();
			
			// An alternative package to get the description or tool tip from...
			//
			String altPackageName = clazz.getPackage().getName();
			
			// Determine the i18n description of the step description (name)
			//
			String description = BaseMessages.getString(packageName, step.description());
			if (description.startsWith("!") && description.endsWith("!")) description=Messages.getString(step.description());
			if (description.startsWith("!") && description.endsWith("!")) description=BaseMessages.getString(altPackageName, step.description());
			
			// Determine the i18n tool tip text for the step (the extended description)
			//
			String tooltip = BaseMessages.getString(packageName, step.tooltip());
			if (tooltip.startsWith("!") && tooltip.endsWith("!")) tooltip=Messages.getString(step.tooltip());
			if (tooltip.startsWith("!") && tooltip.endsWith("!")) tooltip=BaseMessages.getString(altPackageName, step.tooltip());
			
			// If the step should have a separate category, this is the place to calculate that
			// This calculation is only used if the category is USER_DEFINED
			//
			String category;
			if (step.category()!=StepCategory.CATEGORY_USER_DEFINED) {
				category = StepCategory.STANDARD_CATEGORIES[step.category()].getName();
			}
			else {
				category = BaseMessages.getString(packageName, step.categoryDescription());
				if (category.startsWith("!") && category.endsWith("!")) category=Messages.getString(step.categoryDescription());
				if (category.startsWith("!") && category.endsWith("!")) category=BaseMessages.getString(altPackageName, step.categoryDescription());
			}
			
			steps.add(new StepPluginMeta(clazz, stepName, description, tooltip, step.image(), category));
		}
		
		return (Collection<T>)steps;
	}

}
