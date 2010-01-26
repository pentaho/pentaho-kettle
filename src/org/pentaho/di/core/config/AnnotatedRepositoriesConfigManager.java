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
import org.pentaho.di.core.annotations.RepositoryPlugin;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.util.ResolverUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryPluginMeta;
import org.pentaho.di.trans.step.StepInterface;

/**
 * Registers classes annotated with @RepositoryPlugin as Kettle/PDI repository plugins, without the need for XML configurations.
 * 
 * Note: XML configurations will supersede and overwrite annotated definitions.
 * 
 * @param <T>
 */
public class AnnotatedRepositoriesConfigManager<T extends RepositoryPluginMeta> extends BasicConfigManager<T> 
{
	private static Class<?> PKG = StepInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	@Inject
	String packages;
	
	@SuppressWarnings("unchecked") //this is ok here because we defined T above.
	public Collection<T> load() throws KettleConfigException
	{
		ResolverUtil<RepositoryPluginMeta> resolver = new ResolverUtil<RepositoryPluginMeta>();
		
		// If there is a system wide property set with name KETTLE_PLUGIN_PACKAGES we search those packages as well
		//
		String allPackages = packages;
		String extraPackages = System.getProperty(Const.KETTLE_PLUGIN_PACKAGES);
		if (!Const.isEmpty(extraPackages)) {
			allPackages+=","+extraPackages;
		}
		
		resolver.find(new ResolverUtil.AnnotatedWith(RepositoryPlugin.class), allPackages != null ? allPackages.split(",") : new String[] {});
		
		Collection<RepositoryPluginMeta> steps = new LinkedHashSet<RepositoryPluginMeta>(resolver.size());
		for (Class<?> clazz : resolver.getClasses())
		{
			RepositoryPlugin repositoryPlugin = clazz.getAnnotation(RepositoryPlugin.class);
			
			String id = repositoryPlugin.id();
			
			// The package name to get the descriptions or tool tip from...
			//
			String packageName = repositoryPlugin.i18nPackageName();
			if (Const.isEmpty(packageName)) packageName = StepInterface.class.getPackage().getName();
			
			// The metadata and dialog class name
			//
			String metaClassName = repositoryPlugin.metaClass();
			String dialogClassName = repositoryPlugin.dialogClass();			
			String versionBrowserClassName = repositoryPlugin.versionBrowserClass();
			
			// Determine the i18n description of the step description (name)
			//
			String name = BaseMessages.getString(packageName, repositoryPlugin.name());
			if (name.startsWith("!") && name.endsWith("!")) {
				name=BaseMessages.getString(PKG, repositoryPlugin.name());
			}
			
			// Determine the i18n tool tip text for the step (the extended description)
			//
			String description = BaseMessages.getString(packageName, repositoryPlugin.description());
			if (description.startsWith("!") && description.endsWith("!")) {
				description=BaseMessages.getString(PKG, repositoryPlugin.description());
			}
			
			// Add the step to the list...
			//
			steps.add(new RepositoryPluginMeta(id, name, description, clazz.getName(), metaClassName, dialogClassName, versionBrowserClassName, null, null, null));
		}
		
		return (Collection<T>)steps;
	}

}
