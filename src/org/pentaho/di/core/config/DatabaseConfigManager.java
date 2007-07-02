package org.pentaho.di.core.config;

import java.util.Collection;

import org.pentaho.di.core.annotations.Inject;
import org.pentaho.di.core.exception.KettleConfigException;

/**
 * A <code>ConfigManager</code> implementation that caters to loading configuration parameters from a database table.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */
public class DatabaseConfigManager<T> extends BasicConfigManager<T>
{
	private String connectionURL;

	private String table;

	public String getConnectionURL()
	{
		return connectionURL;
	}

	@Inject
	public void setConnectionURL(String connectionURL)
	{
		this.connectionURL = connectionURL;
	}

	public String getTable()
	{
		return table;
	}

	@Inject
	public void setTable(String table)
	{
		this.table = table;
	}
	
	public Collection<T> load() throws KettleConfigException
	{
		//here we establish conn to the database and read config from table
		//but since we don't have any configurations coming from any table in the database, we'll implement this later.
		
		return null;
	}
	
	public <E> Collection<E> loadAs(Class<? extends E> type) throws KettleConfigException
	{
		return null;
	}
}
