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
 

/*
 * Created on 6-feb-04
 *
 */

package be.ibridge.kettle.schema;
import java.util.ArrayList;

public class SelectionGroup
{
	private String         name; 
	private SelectionGroup parent;
	private String         description;
	private ArrayList      selectionGroups;
	private ArrayList      selectionFields;
	
	public SelectionGroup(String name, SelectionGroup parent)
	{
		clear();
		
		this.name  = name;
		this.parent = parent;		
	}

	public SelectionGroup(String name)
	{
		this(name, null);
	}
	
	public void clear()
	{
		name = "";
		parent = null;
		selectionGroups = new ArrayList();
		selectionFields = new ArrayList();
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String desc)
	{
		this.description = desc;
	}
	
	public void setParent(SelectionGroup parent)
	{
		this.parent = parent;
	}
	
	public SelectionGroup getParent()
	{
		return parent;
	}

    public ArrayList getSelectionFields()
    {
        return selectionFields;
    }
    
    public ArrayList getSelectionGroups()
    {
        return selectionGroups;
    }
}
