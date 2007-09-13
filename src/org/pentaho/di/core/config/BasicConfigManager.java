package org.pentaho.di.core.config;

import java.util.Collection;

import org.pentaho.di.core.exception.KettleConfigException;

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
	
	@SuppressWarnings("unchecked")
	public <E> Collection<E> loadAs(Class<? extends E> type) throws KettleConfigException
	{
		Collection<T> coll = load();
		
		if (coll.isEmpty())
			return (Collection<E>)coll;
		
		for (T obj:coll)
		{
			if (obj.getClass().isAssignableFrom(type))
				return (Collection<E>)coll;
			
			break;
		}
		
		throw new KettleConfigException(type + " is not a valid class type for the configurations elements loaded!");
	}

	
}
