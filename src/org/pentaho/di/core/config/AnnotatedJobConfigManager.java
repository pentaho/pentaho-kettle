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
import org.pentaho.di.core.annotations.Job;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.ResolverUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobPluginMeta;
import org.pentaho.di.job.entry.JobEntryBase;

/**
 * Registers classes annotated with @Job as Kettle/PDI jobs, without the need for XML configurations.
 * 
 * Note: XML configurations will superseed and overwrite annotated definitions.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */
public class AnnotatedJobConfigManager<T extends JobPluginMeta> extends BasicConfigManager<T> 
{
	private static Class<?> PKG = JobEntryBase.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	@Inject
	String packages;
	
	@SuppressWarnings("unchecked") //this is ok here because we defined T above.
	public Collection<T> load() throws KettleConfigException
	{
		// saving old value for change the loglevel to BASIC to avoid i18n messages, see below
		int oldLogLevel=LogWriter.getInstance().getLogLevel(); 
		
		ResolverUtil<JobPluginMeta> resolver = new ResolverUtil<JobPluginMeta>();
		
		// If there is a system wide property set with name KETTLE_PLUGIN_PACKAGES we search those packages as well
		//
		String allPackages = packages;
		String extraPackages = System.getProperty(Const.KETTLE_PLUGIN_PACKAGES);
		if (!Const.isEmpty(extraPackages)) {
			allPackages+=","+extraPackages;
		}
		
		resolver.find(new ResolverUtil.AnnotatedWith(Job.class), allPackages != null ? allPackages.split(",") : new String[] {});
		
		Collection<JobPluginMeta> jobs = new LinkedHashSet<JobPluginMeta>(resolver.size());
		for (Class<?> clazz : resolver.getClasses())
		{
			Job job = clazz.getAnnotation(Job.class);
			
			String jobId  = job.id();
			if (jobId.equals("")) // default
				jobId =  clazz.getName();

			// The package name to get the descriptions or tool tip from...
			//
			String packageName = job.i18nPackageName();
			if (Const.isEmpty(packageName)) packageName = PKG.getPackage().getName();

			// An alternative package to get the description or tool tip from...
			//
			String altPackageName = clazz.getPackage().getName();

			// Determine the i18n description of the step description (name)
			//
			LogWriter.getInstance().setLogLevel(LogWriter.LOG_LEVEL_BASIC); // avoid i18n messages for missing locale
			String name = BaseMessages.getString(packageName, job.name());
			if (name.startsWith("!") && name.endsWith("!")) name=BaseMessages.getString(PKG, job.name());
			LogWriter.getInstance().setLogLevel(oldLogLevel); // restore loglevel, when the last alternative fails, log it when loglevel is detailed
			if (!Const.isEmpty(job.name()) && name.startsWith("!") && name.endsWith("!")) name=BaseMessages.getString(altPackageName, job.name());
			if (name.startsWith("!") && name.endsWith("!")) name=job.name(); // Nothing to translate, keep it like it is. 
			
			// Determine the i18n tool tip text for the step (the extended description)
			//
			LogWriter.getInstance().setLogLevel(LogWriter.LOG_LEVEL_BASIC); // avoid i18n messages for missing locale
			String tooltip = BaseMessages.getString(packageName, job.tooltip());
			if (tooltip.startsWith("!") && tooltip.endsWith("!")) tooltip=BaseMessages.getString(PKG, job.tooltip());
			LogWriter.getInstance().setLogLevel(oldLogLevel); // restore loglevel, when the last alternative fails, log it when loglevel is detailed
			if (tooltip.startsWith("!") && tooltip.endsWith("!")) tooltip=BaseMessages.getString(altPackageName, job.tooltip());
			if (tooltip.startsWith("!") && tooltip.endsWith("!")) tooltip=job.tooltip(); // Nothing to translate, keep it like it is. 
			
			if (Const.isEmpty(name)) name=tooltip;
			
			// If the step should have a separate category, this is the place to calculate that
			// This calculation is only used if the category is USER_DEFINED
			//
			LogWriter.getInstance().setLogLevel(LogWriter.LOG_LEVEL_BASIC); // avoid i18n messages for missing locale
			String category = BaseMessages.getString(packageName, job.categoryDescription());
			if (category.startsWith("!") && category.endsWith("!")) category=BaseMessages.getString(PKG, job.categoryDescription());
			LogWriter.getInstance().setLogLevel(oldLogLevel); // restore loglevel, when the last alternative fails, log it when loglevel is detailed
			if (category.startsWith("!") && category.endsWith("!")) category=BaseMessages.getString(altPackageName, job.categoryDescription());
			
			jobs.add(new JobPluginMeta(clazz, jobId, name, tooltip, job.image(), category));
		}
		
		
		return (Collection<T>)jobs;
	}

}
