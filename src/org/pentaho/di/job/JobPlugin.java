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

package org.pentaho.di.job;

import org.pentaho.di.core.plugins.Plugin;

/**
 * Contains the description of a job-entry of a job-entry plugin, what jars to
 * load, the icon, etc.
 * 
 * @since 2005-may-09
 * @author Matt
 * 
 */
public class JobPlugin extends Plugin<String>
{
	private JobEntryType jobType;

	public JobPlugin(int type, String id, JobEntryType jobType, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname)
	{

		super(type, id, jobType.getDescription(), tooltip, directory, jarfiles, icon_filename, classname);
		this.jobType = jobType;
	}

	public JobPlugin(int type, String id, String description, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname)
	{
		super(type, id, description, tooltip, directory, jarfiles, icon_filename, classname);
	}

	public JobEntryType getJobType()
	{
		return jobType;
	}

	public int hashCode()
	{
		return getID().hashCode();
	}

	public boolean equals(Object obj)
	{
		return getID().equals(((JobPlugin) obj).getID());
	}

	public String toString()
	{
		return getClass().getName() + ": " + getID() + "(" + (getType() == TYPE_NATIVE ? "NATIVE" : "PLUGIN")
				+ ")";
	}
}