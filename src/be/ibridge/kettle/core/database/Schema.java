/*
 *
 *
 */

package be.ibridge.kettle.core.database;

/**
 * Contains the information that's stored in a single schema.
 * 
 * @author Matt
 * @since  7-apr-2005
 */
public class Schema
{
	private String   schemaName;
	private String[] items;
	
	public Schema(String catalogName, String[] items)
	{
		this.schemaName = catalogName;
		this.items = items;
	}
	
	/**
	 * @return Returns the catalogName.
	 */
	public String getSchemaName()
	{
		return schemaName;
	}
	
	/**
	 * @param catalogName The catalogName to set.
	 */
	public void setSchemaName(String catalogName)
	{
		this.schemaName = catalogName;
	}
	
	/**
	 * @return Returns the items.
	 */
	public String[] getItems()
	{
		return items;
	}
	
	/**
	 * @param items The items to set.
	 */
	public void setItems(String[] items)
	{
		this.items = items;
	}
};
