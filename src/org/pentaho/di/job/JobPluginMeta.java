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
package org.pentaho.di.job;

import org.pentaho.di.core.config.PropertySetter;
import org.pentaho.di.core.exception.KettleConfigException;

public class JobPluginMeta
{

	protected Class<?> className;

	protected String id;

	protected JobEntryType type;

	protected String tooltipDesc;

	protected String imageFileName;

	protected final PropertySetter psetter = new PropertySetter();

	public JobPluginMeta()
	{
		// for "outside" configurations
	}


	public JobPluginMeta(Class<?> className, String id, JobEntryType type, String tooltipDesc,
			String imageFileName)
	{
		this.className = className;
		this.id = id;
		this.type = type;
		this.tooltipDesc = tooltipDesc;
		this.imageFileName = imageFileName;
	}

	public Class<?> getClassName()
	{
		return className;
	}

	public void setClassName(Class<?> className)
	{
		this.className = className;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getImageFileName()
	{
		return imageFileName;
	}

	public void setImageFileName(String imageFileName)
	{
		this.imageFileName = imageFileName;
	}

	public JobEntryType getType()
	{
		return type;
	}

	public void setType(JobEntryType type)
	{
		this.type = type;
	}

	public String getTooltipDesc()
	{
		return tooltipDesc;
	}

	public void setTooltipDesc(String tooltipDesc)
	{
		this.tooltipDesc = tooltipDesc;
	}

	public void set(String property, String value) throws KettleConfigException
	{
		psetter.setProperty(this, property, value);
	}

}
