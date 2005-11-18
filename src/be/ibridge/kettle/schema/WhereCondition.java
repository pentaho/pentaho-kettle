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
 * Created on 28-jan-2004
 * 
 */

package be.ibridge.kettle.schema;
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;

public class WhereCondition implements Cloneable, XMLInterface
{
	private TableMeta table;
	private String name;
	private String code;
	private String description;
	private boolean changed;
	private TableField field;
	private String comparator;

	public static final String[] comparators = new String[] { "=", "<>", "<", "<=", ">", ">=", "IS NULL", "IS NOT NULL", "IN", "NOT IN" };
	
	
	public WhereCondition(TableMeta table, String name, String code)
	{
		this.table       = table;
		this.name        = name;
		this.code        = code;
		this.description = null;
		this.comparator  = null;
		this.field       = null;
	}
	
	public WhereCondition()
	{
		this(null, null, null);
	}


	public boolean loadXML(Node condnode, TableMeta tableinfo, ArrayList fields)
	{
		try
		{
			name        = XMLHandler.getTagValue(condnode, "name");
			code        = XMLHandler.getTagValue(condnode, "code");
			description = XMLHandler.getTagValue(condnode, "description");
			comparator  = XMLHandler.getTagValue(condnode, "comparator");
			String f    = XMLHandler.getTagValue(condnode, "field");
			field       = searchField(fields, f); 
			this.table  = tableinfo;
			
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public TableField searchField(ArrayList fields, String name)
	{
		for (int i=0;i<fields.size();i++)
		{
			TableField f = (TableField)fields.get(i);
			if (f.getName().equalsIgnoreCase(name)) return f;
		}
		return null;
	}

	public String getXML()
	{
		String retval="";
		
		retval+="      <condition>"+Const.CR;
		retval+="        "+XMLHandler.addTagValue("table",       table.getName());
		retval+="        "+XMLHandler.addTagValue("name",        name);
		retval+="        "+XMLHandler.addTagValue("code",        code);
		retval+="        "+XMLHandler.addTagValue("description", description);
		retval+="        "+XMLHandler.addTagValue("field",       field==null?null:field.getName());
		retval+="        "+XMLHandler.addTagValue("comparator",  comparator);
		retval+="        </condition>"+Const.CR;
		
		return retval;
	}
		
	public Object clone()
	{
		try
		{
			WhereCondition retval   = (WhereCondition)super.clone();
			
			retval.setTable((TableMeta)getTable().clone());
			
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

	public void setTable(TableMeta table)
	{
		this.table = table; 
	}
	
	public TableMeta getTable()
	{
		return table;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setComparator(String comparator)
	{
		this.comparator = comparator;
	}

	public String getComparator()
	{
		return comparator;
	}
	
	public void setField(TableField field)
	{
		this.field = field;
	}
	
	public TableField getField()
	{
		return field;
	}
	
	public void setCode(String code)
	{
		this.code = code;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public String getWhereClause()
	{
		String retval = "";
		if (field!=null)
		{
			if (getComparator()!=null && getComparator().length()>0)
			{
				retval = field.getAliasField() + " " + getComparator() + " " + getCode();
			}
			else
			{
				retval = field.getAliasField() + " " + getCode();
			}
		}
		else
		{
			retval = getCode();
		}
		return retval;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}


	public void setChanged()
	{
		setChanged(true);
	}
	
	public void setChanged(boolean ch)
	{
		changed = ch;		
	}
	
	public boolean hasChanged()
	{
		return changed;
	}
	
	public String toString()
	{
		return table.getName()+"::"+name+"::("+code+")";
	}
	
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	public boolean equals(Object obj)
	{
		WhereCondition rel = (WhereCondition)obj;
		
		return toString().equalsIgnoreCase(rel.toString());
	}
}
