package org.pentaho.di.core.config;

/**
 * A base class for <code>ConfigManager</code> to derive from.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */
public abstract class BasicConfigManager<T> implements ConfigManager<T>
{
	protected String id;

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.config.ConfigParameters#getId()
	 */
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
}
