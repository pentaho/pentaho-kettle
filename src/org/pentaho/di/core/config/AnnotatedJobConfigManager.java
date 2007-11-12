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

import org.pentaho.di.core.annotations.Inject;
import org.pentaho.di.core.annotations.Job;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.util.ResolverUtil;
import org.pentaho.di.job.JobPluginMeta;
import org.pentaho.di.job.entry.Messages;


/**
 * Registers classes annotated with @Job as Kettle/PDI jobs, without the need for XML configurations.
 * 
 * Note: XML configurations will superseed and overwrite annotated definitions.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */public class AnnotatedJobConfigManager<T extends JobPluginMeta> extends BasicConfigManager<T> 
{
	@Inject
	String packages;
	
	@SuppressWarnings("unchecked") //this is ok here because we defined T above.
	public Collection<T> load() throws KettleConfigException
	{
		ResolverUtil<JobPluginMeta> resolver = new ResolverUtil<JobPluginMeta>();
		resolver.find(new ResolverUtil.AnnotatedWith(Job.class), packages != null ? packages.split(",")
				: new String[] {});
		Collection<JobPluginMeta> jobs = new LinkedHashSet<JobPluginMeta>(resolver.size());
		for (Class<?> clazz : resolver.getClasses())
		{
			Job job = clazz.getAnnotation(Job.class);
			String jobId  = job.id();
			if (jobId.equals("")) // default
				jobId =  clazz.getName();

			jobs.add(new JobPluginMeta(clazz, jobId, job.type(),Messages.getString(job.tooltip()),job.image()));
		}
		
		
		return (Collection<T>)jobs;
	}

}
