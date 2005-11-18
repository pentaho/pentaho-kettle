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
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;


/*
 * Created on 28-jan-2004
 * 
 */
 
public class TableMeta implements Cloneable, XMLInterface
{
	private String       name;
	private String       dbname;
	private DatabaseMeta dbinfo;
	private ArrayList    fields;
	private ArrayList    conditions;
	public  String       description;
	private int          type;

	private Point        location;
	private int          width, height;
	private boolean      changed;
	private boolean      selected;
	private boolean      drawtable;
	
	private int          size;
	
	public static final int TYPE_TABLE_OTHER     = 0;
	public static final int TYPE_TABLE_DIMENSION = 1;
	public static final int TYPE_TABLE_FACT      = 2;
	
	public static final String typeTableDesc[] = { "Other", "Dimension", "Fact" };
	
	public TableMeta(String name, String dbname, DatabaseMeta dbinfo, ArrayList fields, ArrayList conditions)
	{
		this.name       = name;
		this.dbname     = dbname;
		this.dbinfo     = dbinfo;
		this.fields     = fields;
		this.conditions = conditions;

		this.type = TYPE_TABLE_OTHER;
		location = new Point(0,0);
		size = -1;
	}
	
	public TableMeta()
	{
		this(null, null, null, null, null);
	}

	public boolean loadXML(Node tablenode, ArrayList databases)
	{
		try
		{
			fields     = new ArrayList();
			conditions = new ArrayList();
			
			name           = XMLHandler.getTagValue(tablenode, "name");
			dbname         = XMLHandler.getTagValue(tablenode, "dbname");
			description    = XMLHandler.getTagValue(tablenode, "description");
			String conn    = XMLHandler.getTagValue(tablenode, "connection");
			dbinfo = findConnection(databases, conn);
			String stype   = XMLHandler.getTagValue(tablenode, "type");
			type = getType(stype);
			String ssize   = XMLHandler.getTagValue(tablenode, "size");
			size = Const.toInt(ssize, -1);
	
			//System.out.println("Table node #"+i+Const.CR+tablenode.toString()+Const.CR+Const.CR);
				
			// Read all fields in arraylist through Field class constructor!
			Node fieldsnode = XMLHandler.getSubNode(tablenode, "fields");
			int nrfields = XMLHandler.countNodes(fieldsnode, "field");
			
			//System.out.println("We have "+nrfields+" fields in table #"+i);
			for (int x=0;x<nrfields;x++)
			{
				Node fieldnode = XMLHandler.getSubNodeByNr(fieldsnode, "field", x);
				TableField f = new TableField();
				if (f.loadXML(fieldnode, this))
				{
					addField(f);
				}
				else
				{
					return false;
				}
			}
	
			// Read all conditions in arraylist through Condition class constructor!
			Node condsnode = XMLHandler.getSubNode(tablenode, "conditions");
			int nrconditions = XMLHandler.countNodes(condsnode, "condition");
			
			//System.out.println("We have "+nrfields+" fields in table #"+i);
			for (int x=0;x<nrconditions;x++)
			{
				Node condnode = XMLHandler.getSubNodeByNr(condsnode, "condition", x);
				
				WhereCondition cond = new WhereCondition();
				if (cond.loadXML(condnode, this, fields))
				{
					addCondition(cond);
				}
				else
				{
					return false;
				}
			}
	
			String sxloc   = XMLHandler.getTagValue(tablenode, "xloc");
			String syloc   = XMLHandler.getTagValue(tablenode, "yloc");
			String swidth  = XMLHandler.getTagValue(tablenode, "width");
			String sheight = XMLHandler.getTagValue(tablenode, "heigth");
			int x   = Const.toInt(sxloc, 0);
			int y   = Const.toInt(syloc, 0);
			location = new Point(x,y);
			width  = Const.toInt(swidth, 0);
			height = Const.toInt(sheight, 0);
			
			String sdrawn  = XMLHandler.getTagValue(tablenode, "drawtable");
			drawtable = "Y".equalsIgnoreCase(sdrawn);
			
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
		
		retval+="    <table>"+Const.CR;
		retval+="      "+XMLHandler.addTagValue("name",         name);
		retval+="      "+XMLHandler.addTagValue("dbname",       dbname);
		retval+="      "+XMLHandler.addTagValue("description",  description);
		retval+="      "+XMLHandler.addTagValue("connection",   dbinfo==null?"":dbinfo.getName());
		retval+="      "+XMLHandler.addTagValue("type",         getTypeDesc());
		retval+="      "+XMLHandler.addTagValue("size",         size);

		retval+="      "+XMLHandler.addTagValue("xloc",   location.x);
		retval+="      "+XMLHandler.addTagValue("yloc",   location.y);
		retval+="      "+XMLHandler.addTagValue("width",  width);
		retval+="      "+XMLHandler.addTagValue("heigth", height);

		retval+="      "+XMLHandler.addTagValue("drawtable", drawtable);

		retval+="      <fields>"+Const.CR;
		for (int i=0;i<nrFields();i++)
		{
			retval+=getField(i).getXML();
		}
		retval+="        </fields>"+Const.CR;

		retval+="      <conditions>"+Const.CR;
		for (int i=0;i<nrConditions();i++)
		{
			retval+=getCondition(i).getXML();
		}
		retval+="        </conditions>"+Const.CR;

		retval+="      </table>"+Const.CR;
		
		return retval;
	}

	private DatabaseMeta findConnection(ArrayList databases, String name)
	{
		for (int x=0;x<databases.size();x++)
		{
			DatabaseMeta dbinfo = (DatabaseMeta)databases.get(x);
			if (dbinfo.getName().equalsIgnoreCase(name)) return dbinfo;
		}
		return null;
	}

	

	public Object clone()
	{
		try
		{
			TableMeta retval   = (TableMeta)super.clone();
			
			if (getLocation()!=null)
			{
				retval.setLocation(getLocation().x, getLocation().y);
			}

			retval.fields = fields; // Do we need to copy this one by one?
									// Perhaps it's easier to change the fields in all tables with the same layout!
				
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public void setLocation(int x, int y)
	{
		int nx = (x>=0?x:0);
		int ny = (y>=0?y:0);
		
		Point loc = new Point(nx,ny);
		if (!loc.equals(location)) setChanged();
		location=loc;
	}
	
	public void setLocation(Point loc)
	{
		if (loc!=null && !loc.equals(location)) setChanged();
		location = loc;
	}
	
	public Point getLocation()
	{
		return location;
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
	
	public void setSelected(boolean sel)
	{
		selected=sel;
	}

	public void flipSelected()
	{
		selected=!selected;
	}

	public boolean isSelected()
	{
		return selected;
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
		return dbname;
	}

	public void setDBName(String dbname)
	{
		this.dbname = dbname;
	}
	
	public void setDatabase(DatabaseMeta dbinfo)
	{
		this.dbinfo = dbinfo;
	}
	
	public DatabaseMeta getDatabase()
	{
		return dbinfo;
	}
	
	public void setFields(ArrayList fields)
	{
		this.fields = fields;
	}
	
	public ArrayList getFields()
	{
		return fields;
	}
	
	public TableField getField(int i)
	{
		return (TableField)fields.get(i);
	}
	
	public void addField(TableField field)
	{
		fields.add(field);
	}

	public void addField(int i, TableField field)
	{
		fields.add(i, field);
	}

	public int findFieldnr(String fieldname)
	{
		for (int i=0;i<fields.size();i++)
		{
			if (getField(i).getName().equalsIgnoreCase(fieldname)) return i;
		}
		return -1;
	}
	
	public TableField findField(String fieldname)
	{
		int idx = findFieldnr(fieldname);
		if (idx>=0) 
		{
			// System.out.println("Found field #"+idx);
			return getField(idx);
		} 
		return null;
	}

	public int indexOfField(TableField f)
	{
		return fields.indexOf(f);
	}
	
	public void removeField(int i)
	{
		fields.remove(i);
	}
	
	public void removeAllFields()
	{
		fields.clear();
	}
	
	public int nrFields()
	{
		return fields.size();
	}

///////////////////////////////////////////////////////////////////////////

	public ArrayList getConditions()
	{
		return conditions;
	}
	
	public WhereCondition getCondition(int i)
	{
		return (WhereCondition)conditions.get(i);
	}
	
	public void addCondition(WhereCondition condition)
	{
		conditions.add(condition);
	}

	public void addCondition(int i, WhereCondition condition)
	{
		conditions.add(i, condition);
	}

	public int findConditionnr(String conditionname)
	{
		for (int i=0;i<conditions.size();i++)
		{
			if (getCondition(i).getName().equalsIgnoreCase(conditionname)) return i;
		}
		return -1;
	}
	
	public WhereCondition findCondition(String conditionname)
	{
		int idx = findConditionnr(conditionname);
		if (idx>=0) 
		{
			// System.out.println("Found field #"+idx);
			return getCondition(idx);
		} 
		return null;
	}

	public int indexOfCondition(WhereCondition c)
	{
		return conditions.indexOf(c);
	}
	
	public void removeCondition(int i)
	{
		conditions.remove(i);
	}
	
	public void removeAllConditions()
	{
		conditions.clear();
	}
	
	public int nrConditions()
	{
		return conditions.size();
	}






///////////////////////////////////////////////////////////////////////////////

	public boolean isDrawn()
	{
		return drawtable;
	}

	public void draw()
	{
		setDraw(true);
	}

	public void hide()
	{
		setDraw(false);
	}

	public void setDraw(boolean dr)
	{
		if (drawtable!=dr) setChanged();
		drawtable=dr;
	}
	
	public boolean equals(Object obj)
	{
		if (obj==null) return false;
		
		TableMeta inf = (TableMeta)obj;
		
		if (!getName().equalsIgnoreCase(inf.getName())) return false;

		return true; 
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
		return typeTableDesc[i];
	}

	public static final int getType(String typedesc)
	{
		for (int i=0;i<typeTableDesc.length;i++)
		{
			if (typeTableDesc[i].equalsIgnoreCase(typedesc)) return i;
		}
		return TYPE_TABLE_OTHER;
	}
	
	public boolean isDimension()
	{
		return type==TYPE_TABLE_DIMENSION;
	}

	public boolean isFact()
	{
		return type==TYPE_TABLE_FACT;
	}
	
	public void setSize(int size)
	{
		this.size=size;
	}
	
	public int getSize()
	{
		return size;
	}

	public String toString()
	{
		if (dbinfo!=null) return dbinfo.getName()+"-->"+getName();
		return getName();
	}
	
	public int hashCode()
	{
		return getName().hashCode();
	}

}
