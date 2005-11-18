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
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;

/*
 * Created on 28-jan-2004
 * 
 */
 
public class RelationshipMeta implements Cloneable, XMLInterface
{
	private TableMeta table_from, table_to;
	private int fieldnr_from, fieldnr_to;
	private int type;
	private boolean changed;
	private boolean complex;
	private String complex_join;
	
	public final static int TYPE_RELATIONSHIP_UNDEFINED = 0;
	public final static int TYPE_RELATIONSHIP_1_N       = 1;
	public final static int TYPE_RELATIONSHIP_N_1       = 2;
	public final static int TYPE_RELATIONSHIP_1_1       = 3;
	public final static int TYPE_RELATIONSHIP_0_N       = 4;
	public final static int TYPE_RELATIONSHIP_N_0       = 5;
	public final static int TYPE_RELATIONSHIP_0_1       = 6;
	public final static int TYPE_RELATIONSHIP_1_0       = 7;
	public final static int TYPE_RELATIONSHIP_N_N       = 8;
	
	public final static String typeRelationshipDesc[] = 
		{
			"undefined", "1:N", "N:1", "1:1", "0:N", "N:0", "0:1", "1:0", "N:N"
		};
	
	public RelationshipMeta(TableMeta table_from, TableMeta table_to, int fieldnr_from, int fieldnr_to)
	{
		this.table_from   = table_from;
		this.table_to     = table_to;
		this.fieldnr_from = fieldnr_from;
		this.fieldnr_to   = fieldnr_to;
		type = TYPE_RELATIONSHIP_UNDEFINED;
		complex = false;
		complex_join = "";
	}
	
	public RelationshipMeta()
	{
		this(null, null, 0, 0);
	}

	public RelationshipMeta(TableMeta table_from, TableMeta table_to, String complex_join)
	{
		this.table_from   = table_from;
		this.table_to     = table_to;
		this.fieldnr_from = -1;
		this.fieldnr_to   = -1;
		this.type         = TYPE_RELATIONSHIP_UNDEFINED;
		this.complex      = true;
		this.complex_join = complex_join;
	}

	public boolean loadXML(Node relnode, ArrayList tables)
	{
		try
		{
			String from = XMLHandler.getTagValue(relnode, "table_from");
			table_from  = findTable(tables, from);
			String to   = XMLHandler.getTagValue(relnode, "table_to");
			table_to = findTable(tables, to);
					
			fieldnr_from = Const.toInt(XMLHandler.getTagValue(relnode, "fieldnr_from"), -1);
			fieldnr_to   = Const.toInt(XMLHandler.getTagValue(relnode, "fieldnr_to"), -1);
			type         = getType(XMLHandler.getTagValue(relnode, "type"));
			complex      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(relnode, "complex"));
			complex_join = XMLHandler.getTagValue(relnode, "complex_join");
			
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public String getXML()
	{
		String retval="";
		
		retval+="      <relationship>"+Const.CR;
		retval+="        "+XMLHandler.addTagValue("table_from",   table_from.getName());
		retval+="        "+XMLHandler.addTagValue("table_to",     table_to.getName());
		retval+="        "+XMLHandler.addTagValue("fieldnr_from", fieldnr_from);
		retval+="        "+XMLHandler.addTagValue("fieldnr_to",   fieldnr_to);
		retval+="        "+XMLHandler.addTagValue("type",         getTypeDesc());
		retval+="        "+XMLHandler.addTagValue("complex",      complex);
		retval+="        "+XMLHandler.addTagValue("complex_join", complex_join);
		retval+="        </relationship>"+Const.CR;
		
		return retval;
	}
	
	private TableMeta findTable(ArrayList tables, String name)
	{
		for (int x=0;x<tables.size();x++)
		{
			TableMeta tableinfo = (TableMeta)tables.get(x);
			if (tableinfo.getName().equalsIgnoreCase(name)) return tableinfo;
		}
		return null;
	}

	
	public Object clone()
	{
		try
		{
			RelationshipMeta retval   = (RelationshipMeta)super.clone();
			
			retval.setTableFrom((TableMeta)getTableFrom().clone());
			retval.setTableTo  ((TableMeta)getTableTo().clone());
			
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

	public void setTableFrom(TableMeta table_from)
	{
		this.table_from = table_from; 
	}
	public TableMeta getTableFrom()
	{
		return table_from;
	}

	public void setTableTo(TableMeta table_to)
	{
		this.table_to = table_to; 
	}
	public TableMeta getTableTo()
	{
		return table_to;
	}
	
	public void setFieldnrFrom(int fieldnr_from)
	{
		this.fieldnr_from = fieldnr_from;
	}
	public void setFieldnrTo(int fieldnr_to)
	{
		this.fieldnr_to = fieldnr_to;
	}

	public int getFieldnrFrom()
	{
		return fieldnr_from;
	}
	public int getFieldnrTo()
	{
		return fieldnr_to;
	}

	public TableField getFieldFrom()
	{
		return table_from.getField(fieldnr_from);
	}
	public TableField getFieldTo()
	{
		return table_to.getField(fieldnr_to);
	}
	
	public boolean isComplex()
	{
		return complex;
	}
	
	public boolean isRegular()
	{
		return !complex;
	}
	
	public void setComplex()
	{
		setComplex(true);
	}

	public void setRegular()
	{
		setComplex(false);
	}
	
	public void flipComplex()
	{
		setComplex(!isComplex());
	}

	public void setComplex(boolean c)
	{
		complex = c;
	}
	
	public String getComplexJoin()
	{
		return complex_join;
	}
	
	public void setComplexJoin(String cj)
	{
		complex_join = cj;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type=type;
	}
	
	public void setType(String tdesc)
	{
		this.type = getType(tdesc);
	}

	public String getTypeDesc()
	{
		return getType(type);
	}
	
	public static final String getType(int i)
	{
		return typeRelationshipDesc[i];
	}

	public static final int getType(String typedesc)
	{
		for (int i=0;i<typeRelationshipDesc.length;i++)
		{
			if (typeRelationshipDesc[i].equalsIgnoreCase(typedesc)) return i;
		}
		return TYPE_RELATIONSHIP_UNDEFINED;
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
	
	public boolean isUsingTable(TableMeta table)
	{
		if (table==null) return false;
		return (table.equals(table_from) || table.equals(table_to));
	}
	
	// Swap from and to...
	public void flip()
	{
		TableMeta dummy = table_from;
		table_from = table_to;
		table_to = dummy;
		
		int dum = fieldnr_from;
		fieldnr_from = fieldnr_to;
		fieldnr_to = dum;
		
		switch(type)
		{
			case TYPE_RELATIONSHIP_UNDEFINED : break;
			case TYPE_RELATIONSHIP_1_N       : type = TYPE_RELATIONSHIP_N_1; break;
			case TYPE_RELATIONSHIP_N_1       : type = TYPE_RELATIONSHIP_1_N; break;
			case TYPE_RELATIONSHIP_1_1       : break;
			case TYPE_RELATIONSHIP_0_N       : type = TYPE_RELATIONSHIP_N_0; break;
			case TYPE_RELATIONSHIP_N_0       : type = TYPE_RELATIONSHIP_0_N; break;
			case TYPE_RELATIONSHIP_0_1       : type = TYPE_RELATIONSHIP_1_0; break;
			case TYPE_RELATIONSHIP_1_0       : type = TYPE_RELATIONSHIP_0_1; break;		
		}
	}
	
	public String toString()
	{
		if (fieldnr_from>=0 && fieldnr_to>=0)
		{
			return table_from.getName()+"."+table_from.getField(fieldnr_from).getName()+ 
                   " - "+
				   table_to.getName()+"."+table_to.getField(fieldnr_to).getName();
		}
		else
		{
			return table_from.getName()+" - "+table_to.getName();
		}
	}
	
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	public boolean equals(Object obj)
	{
		RelationshipMeta rel = (RelationshipMeta)obj;
		
		return toString().equalsIgnoreCase(rel.toString());
	}
	
	public String getJoin()
	{
		String join="";
		
		if (isComplex())
		{
			join = complex_join;
		}
		else
		if (table_from!=null && table_to!=null && fieldnr_from>=0 && fieldnr_to>=0)
		{
			TableField frf = getFieldFrom();
			TableField tof = getFieldTo();
			join=frf.getAliasField()+" = "+tof.getAliasField();
		}
		
		return join;
	}
}
