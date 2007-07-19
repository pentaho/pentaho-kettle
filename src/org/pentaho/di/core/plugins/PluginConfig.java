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
public class PluginConfig
{
	private String id;

	private String location;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String resource)
	{
		this.location = resource;
	}
	
	public boolean equals(Object o)
	{
		if (o==this)
			return true;
		
		if (!(o instanceof PluginConfig))
			return false;
		
		PluginConfig that = (PluginConfig)o;
		
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
