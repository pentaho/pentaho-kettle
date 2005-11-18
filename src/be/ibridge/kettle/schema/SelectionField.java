 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.schema;
/*
 * Created on 6-feb-04
 *
 */

public class SelectionField
{
	private String name; 
	private TableField field;
	private SelectionGroup group;
	
	public SelectionField(String name, TableField field, SelectionGroup group)
	{
		this.name = name;
		this.field = field;
		this.group = group;
	}

	public SelectionField(String name, TableField field)
	{
		this(name, field, null);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setField(TableField field)
	{
		this.field = field;
	}
	
	public TableField getField()
	{
		return field;
	}
	
	public void setGroup(SelectionGroup group)
	{
		this.group = group;
	}
	
	public SelectionGroup getGroup()
	{
		return group;
	}
}
