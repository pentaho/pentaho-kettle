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
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;


public class TableField implements XMLInterface
{
	public static final int TYPE_FIELD_NONE      = 0;
	public static final int TYPE_FIELD_DIMENSION = 1;
	public static final int TYPE_FIELD_FACT      = 2;
	public static final int TYPE_FIELD_KEY       = 3;
	
	public static final String typeFieldDesc[] = 
		{ 
			"", "Dimension", "Fact", "Key"
		};
	
	public static final int TYPE_AGGREGATION_NONE      = 0;
	public static final int TYPE_AGGREGATION_AVERAGE   = 1;
	public static final int TYPE_AGGREGATION_MINIMUM   = 2;
	public static final int TYPE_AGGREGATION_MAXIMUM   = 3;
	public static final int TYPE_AGGREGATION_COUNT     = 4;
	public static final int TYPE_AGGREGATION_SUM       = 5;
	
	public static final String typeAggregationDesc[] = 
		{ 
			"none", "average", "minimum", "maximum", "count", "sum" 
		};
	
	private int type;
	private String name;
	private String dbname;
	private String description;
	private int aggr_type;
	private boolean hidden;
	private boolean exact;
	private TableMeta tableinfo;
		
	public TableField(String name, String dbname, int type, int aggregation_type, TableMeta tableinfo)
	{
		this.name      = name;
		this.dbname    = dbname;
		this.type      = type;
		this.aggr_type = aggregation_type;
		this.hidden    = false; 
		this.tableinfo = tableinfo;
		this.exact     = false; 
	}
	
	public TableField()
	{
		this(null, null, TYPE_FIELD_NONE, TYPE_AGGREGATION_NONE, null);
	}
	
	public boolean loadXML(Node fieldnode, TableMeta tableinfo)
	{
		try
		{
			name        = XMLHandler.getTagValue(fieldnode, "name");
			dbname      = XMLHandler.getTagValue(fieldnode, "dbname");
			type        = getFieldType(XMLHandler.getTagValue(fieldnode, "field_type"));
			aggr_type   = getAggregationType(XMLHandler.getTagValue(fieldnode, "aggregation_type"));
			hidden      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(fieldnode, "hidden"));
			exact       = "Y".equalsIgnoreCase(XMLHandler.getTagValue(fieldnode, "exact"));
			description = XMLHandler.getTagValue(fieldnode, "description");
			this.tableinfo = tableinfo;
			
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
		
		retval+="        <field>"+Const.CR;
		retval+="        "+XMLHandler.addTagValue("name",             name);
		retval+="        "+XMLHandler.addTagValue("dbname",           dbname);
		retval+="        "+XMLHandler.addTagValue("description",      description);
		retval+="        "+XMLHandler.addTagValue("field_type",       getFieldTypeDesc());
		retval+="        "+XMLHandler.addTagValue("aggregation_type", getAggregationDesc());
		retval+="        "+XMLHandler.addTagValue("hidden",           hidden);
		retval+="        "+XMLHandler.addTagValue("table",            tableinfo.getName());
		retval+="        "+XMLHandler.addTagValue("exact",            exact);
		retval+="          </field>"+Const.CR;
		
		return retval;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getDBName()
	{
		//if (dbname==null || dbname.length()==0) return name;
		return dbname;
	}

	public void setDBName(String dbname)
	{
		this.dbname = dbname;
	}
	
	public int getType()
	{
		return type;
	}

	public String getFieldTypeDesc()
	{
		return getFieldTypeDesc(type);
	}
	
	public int getAggregationType()
	{
		return aggr_type;
	}

	public String getAggregationDesc()
	{
		return typeAggregationDesc[aggr_type];
	}

	public static final String getFieldTypeDesc(int i)
	{
		return typeFieldDesc[i];
	}
	
	public static final int getFieldType(String typedesc)
	{
		for (int i=0;i<typeFieldDesc.length;i++)
		{
			if (typeFieldDesc[i].equalsIgnoreCase(typedesc)) return i;
		}
		return TYPE_FIELD_NONE;
	}

	public static final String getAggregationTypeDesc(int i)
	{
		return typeAggregationDesc[i];
	}

	public static final int getAggregationType(String aggdesc)
	{
		for (int i=0;i<typeAggregationDesc.length;i++)
		{
			if (typeAggregationDesc[i].equalsIgnoreCase(aggdesc)) return i;
		}
		return TYPE_AGGREGATION_NONE;
	}
	
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}
	
	public void flipHidden()
	{
		hidden=!hidden;
	}
	
	public boolean isHidden()
	{
		return hidden;
	}
	
	public boolean isExact()
	{
		return exact;
	}

	public void setExact(boolean exact)
	{
		this.exact = exact;
	}
	
	public void flipExact()
	{
		exact=!exact;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}
	
	public void setTable(TableMeta tableinfo)
	{
		this.tableinfo = tableinfo; 
	}
	
	public TableMeta getTable()
	{
		return tableinfo;
	}
	
	public boolean isFact()
	{
		return getType()==TYPE_FIELD_FACT;
	}
	
	public boolean isDimension()
	{
		return getType()==TYPE_FIELD_DIMENSION;
	}
	
	public boolean hasAggregate()
	{
		return getAggregationType()!=TYPE_AGGREGATION_NONE && isFact();
	}
	
	public String getTableField()
	{
		String retval;
		
		if (getDBName()!=null && getDBName().length()>0)
		{
			
			retval=getDBName();
			if (retval==null||retval.length()==0) retval=getName();
		}
		else
		{
			TableMeta table = getTable();
			retval=table.getName()+"."+getName();
		}
		
		return retval;
	}
	
	public String getAliasField()
	{
		String retval;
		
		if (getTable()!=null && getDBName()!=null)
		{
			if (!isExact())
			{
				retval = getTable().getName()+"."+getDBName();
			}
			else
			{
				retval = getDBName();
			}
		}
		else
		{
			retval = "??";
		}
		
		return retval;
	}
	
	public String getSelectField(DatabaseMeta dbinfo, int fieldnr)
	{
		String retval="";
		
		if (hasAggregate() && !isExact())
		{
			retval+=getFunction(dbinfo)+"("+getAliasField()+")";
					
			// we need to rename the aggregated field
			// To recognize later, we just give it the name : F#+fieldnr
			retval+=" as F___"+fieldnr; 
		}
		else if (isExact())
		{
			retval+=getAliasField()+" as E___"+fieldnr;
		}
		else
		{
			retval+=getAliasField();
		}
	
		return retval;
	}

	public String getRenameAsField(DatabaseMeta dbinfo, int fieldnr)
	{
		String retval="";
		
		if (hasAggregate() && !isExact())
		{
			retval+="F___"+fieldnr; 
		}
		else
		if (isExact())
		{
			retval+="E___"+fieldnr;
		}
		else
		{
			retval+=getDBName();
		}
	
		return retval;
	}

	
	public String getFunction(DatabaseMeta dbinfo)
	{
		String fn="";
		
		switch(getAggregationType())
		{
			case TYPE_AGGREGATION_AVERAGE: fn=dbinfo.getFunctionAverage(); break;
			case TYPE_AGGREGATION_COUNT  : fn=dbinfo.getFunctionCount(); break;
			case TYPE_AGGREGATION_MAXIMUM: fn=dbinfo.getFunctionMaximum(); break;
			case TYPE_AGGREGATION_MINIMUM: fn=dbinfo.getFunctionMinimum(); break;
			case TYPE_AGGREGATION_SUM    : fn=dbinfo.getFunctionSum(); break;
			default: break;
		}
		
		return fn;
	}
		
	public boolean equals(Object obj)
	{
		TableField f = (TableField)obj;
		if (!getName().equalsIgnoreCase(f.getName())) return false;
		if (!getDBName().equalsIgnoreCase(f.getDBName())) return false;
		if (getAggregationType()!=f.getAggregationType()) return false;
		if (getType()!=f.getType()) return false;
		if (tableinfo!=null)
		{
			if (f.tableinfo!=null)
			{
				if (!tableinfo.getName().equalsIgnoreCase(f.tableinfo.getName())) return false;
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}
	
	public String toString()
	{
		//if (tableinfo!=null) return tableinfo.getName()+"-->"+getName();
		return getName()==null?"NULL":getName();
	}
}
