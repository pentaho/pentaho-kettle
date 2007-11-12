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
package org.pentaho.di.core.plugins;

import org.apache.commons.lang.ObjectUtils;
import org.pentaho.di.core.config.PropertySetter;
import org.pentaho.di.core.exception.KettleConfigException;

/**
 * Just a simple bean-style class to mediate plugin configuration.
 * 
 * @author Alex Silva
 *
 */
public class PluginLocation
{
	private String id;
	
	public static final String PDI_PLUGIN_CONFIG = "pdi.plugins.config"; 
	
	private String location;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	/**
	 * Returns where the plugin xml file is located.  This file specifies "n" locations from which plugins can be loaded.
	 * You can override what's returned by this method by supplying a system property called "pdi.plugins.config" that points
	 * to the location of the file.
	 * 
	 * @return
	 */
	public String getLocation()
	{
		return System.getProperty(PDI_PLUGIN_CONFIG,location);
	}

	public void setLocation(String resource)
	{
		this.location = resource;
	}
	
	public boolean equals(Object o)
	{
		if (o==this)
			return true;
		
		if (!(o instanceof PluginLocation))
			return false;
		
		PluginLocation that = (PluginLocation)o;
		
		return that.id.equals(id) && that.location.equals(location);
	}
	
	@Override
	public int hashCode()
	{
		return ObjectUtils.hashCode(id) + ObjectUtils.hashCode(location);
	}
	
	public void set(String property, String value) throws KettleConfigException
	{
		new PropertySetter().setProperty(this, property, value);
	}

}
