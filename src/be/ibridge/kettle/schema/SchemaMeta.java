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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;

 
public class SchemaMeta implements XMLInterface
{
	private   String         name;
	public    String         filename;
	private   LogWriter      log;
	public    ArrayList      databases;
	private   ArrayList      tables;
	private   ArrayList      relationships;
	private   ArrayList      notes;
	
	private   ArrayList      selfields;
	private   ArrayList      selconditions;
	private   String         selcubefile;
	
	private boolean   changed, changed_connections, changed_tables, changed_relationships, changed_notes;
	private boolean   changed_selfields, changed_selconditions;

	public  Props props;
	private ArrayList undo;
	private int max_undo;
	private int undo_position;

	public  DBCache dbcache;
	
	public static final int TYPE_UNDO_CHANGE   = 1;
	public static final int TYPE_UNDO_NEW      = 2;
	public static final int TYPE_UNDO_DELETE   = 3;
	public static final int TYPE_UNDO_POSITION = 4;

	// Remember the size and position of the different windows...
	public  static final int NR_WINDOWS = 3;
	public  boolean    max[]  = new boolean[NR_WINDOWS];
	public  Rectangle  size[] = new Rectangle[NR_WINDOWS];
	
	public SchemaMeta(LogWriter l)
	{
		log = l;
		clear();
	}
	
	public void clear()
	{
		databases=new ArrayList();
		tables = new ArrayList();
		relationships = new ArrayList();
		notes=new ArrayList();
		selfields=new ArrayList();
		selconditions=new ArrayList();
		selcubefile=null;

		dbcache = DBCache.getInstance();
	
		clearUndo();
	}

	public void clearUndo()
	{
		undo = new ArrayList();
		undo_position=-1;
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String n)
	{
		name = n;
	}
	
	public void setCubeFile(String cf)
	{
		selcubefile = cf;
	}
	
	public String getCubeFile()
	{
		return selcubefile;
	}
		
	public void addConnection(DatabaseMeta ci)
	{
		databases.add(ci);
		changed_connections = true;
	}
	public void addTable(TableMeta ti)
	{
		tables.add(ti);
		changed_tables = true;
	}
	public void addRelationship(RelationshipMeta ri)
	{
		relationships.add(ri);
		changed_relationships = true;
	}
	public void addNote(NotePadMeta ni)
	{
		notes.add(ni);
		changed_notes = true;
	}
	public void addSelField(TableField f)
	{
		selfields.add(f);
		changed_selfields = true;
	}
	public void addSelCondition(WhereCondition c)
	{
		selconditions.add(c);
		changed_selconditions = true;
	}

	public void addConnection(int p, DatabaseMeta ci)
	{
		databases.add(p, ci);
		changed_connections = true;
	}
	public void addTable(int p, TableMeta ti)
	{
		tables.add(p, ti);
		changed_tables = true;
	}
	public void addRelationship(int p, RelationshipMeta ri)
	{
		relationships.add(p, ri);
		changed_relationships = true;
	}
	public void addNote(int p, NotePadMeta ni)
	{
		notes.add(p, ni);
		changed_notes = true;
	}
	public void addSelField(int p, TableField f)
	{
		selfields.add(p, f);
		changed_selfields = true;
	}
	public void addSelCondition(int p, WhereCondition c)
	{
		selconditions.add(p, c);
		changed_selconditions = true;
	}

	public void setChanged()
	{
		setChanged(true);
	}

	public void setChanged(boolean ch)
	{
		changed=ch;
	}

	public DatabaseMeta getConnection(int i)
	{
		return (DatabaseMeta)databases.get(i);
	}
	public TableMeta getTable(int i)
	{
		return (TableMeta)tables.get(i);
	}
	public RelationshipMeta getRelationship(int i)
	{
		return (RelationshipMeta)relationships.get(i);
	}
	public NotePadMeta getNote(int i)
	{
		return (NotePadMeta)notes.get(i);
	}
	public TableField getSelField(int i)
	{
		return (TableField)selfields.get(i);
	}
	public WhereCondition getSelCondition(int i)
	{
		return (WhereCondition)selconditions.get(i);
	}
	
	public void removeConnection(int i)
	{
		if (i<0 || i>=databases.size()) return;
		databases.remove(i);
		changed_connections = true;
	}
	public void removeTable(int i)
	{
		if (i<0 || i>=tables.size()) return;

		tables.remove(i);
		changed_tables = true;
	}
	public void removeRelationship(int i)
	{
		if (i<0 || i>=relationships.size()) return;

		relationships.remove(i);
		changed_relationships = true;
	}
	public void removeNote(int i)
	{
		if (i<0 || i>=notes.size()) return;
		notes.remove(i);
		changed_notes = true;
	}
	public void removeSelField(int i)
	{
		if (i<0 || i>=selfields.size()) return;
		selfields.remove(i);
		changed_selfields = true;
	}
	public void removeSelCondition(int i)
	{
		if (i<0 || i>=selconditions.size()) return;
		selconditions.remove(i);
		changed_selconditions = true;
	}

	public void removeAllSelFields()
	{
		selfields.clear();
		changed_selfields = true;
	}
	public void removeAllSelConditions()
	{
		selconditions.clear();
		changed_selconditions = true;
	}

	public int nrConnections()   { return databases.size();   }
	public int nrTables()        { return tables.size();        }
	public int nrRelationships() { return relationships.size(); }
	public int nrNotes()         { return notes.size();         }
	public int nrSelFields()     { return selfields.size();     }
	public int nrSelConditions() { return selconditions.size();     }

	public void clearChanged()
	{
		changed               = false;
		changed_connections   = false;
		changed_tables        = false;
		changed_relationships = false;
		changed_selfields     = false;
		changed_selconditions = false;
		
		for (int i=0;i<nrConnections();i++)
		{
			getConnection(i).setChanged(false);
		}
		for (int i=0;i<nrTables();i++)
		{
			getTable(i).setChanged(false);
		}
		for (int i=0;i<nrRelationships();i++)
		{
			getRelationship(i).setChanged(false);
		}
	}

	public boolean haveConnectionsChanged()
	{
		if (changed_connections) return true;
		
		for (int i=0;i<nrConnections();i++)
		{
			DatabaseMeta ci = getConnection(i);
			if (ci.hasChanged()) return true;
		}
		return false;
	}

	public boolean haveTablesChanged()
	{
		if (changed_tables) return true;
		
		for (int i=0;i<nrTables();i++)
		{
			TableMeta ti = getTable(i);
			if (ti.hasChanged()) return true;
		}
		return false;
	}

	public boolean haveRelationsipsChanged()
	{
		if (changed_relationships) return true;
		
		for (int i=0;i<nrRelationships();i++)
		{
			RelationshipMeta ri = getRelationship(i);
			if (ri.hasChanged()) return true;
		}
		return false;
	}

    public boolean haveNotesChanged()
    {
        if (changed_notes) return true;
        
        for (int i=0;i<nrNotes();i++)
        {
            NotePadMeta ni = getNote(i);
            if (ni.hasChanged()) return true;
        }
        return false;
    }

	public boolean haveSelFieldsChanged()
	{
		return changed_selfields;
	}

	public boolean haveSelConditionsChanged()
	{
		return changed_selconditions;
	}

	public boolean hasChanged()
	{
		if (changed) return true;
		
		if (haveConnectionsChanged())   return true;
		if (haveTablesChanged())        return true;
		if (haveRelationsipsChanged())  return true;
		if (haveSelFieldsChanged())     return true;
		if (haveSelConditionsChanged()) return true;

		return false;
	}

	public DatabaseMeta findConnection(String name)
	{
		int i;
		for (i=0;i<nrConnections();i++)
		{
			DatabaseMeta ci = getConnection(i); 
			if (ci.getName().equalsIgnoreCase(name))
			{
				return ci; 
			}
		}
		return null;
	}

	public TableMeta findTable(String name)
	{
		return findTable(name, null);
	}
	
	public TableMeta findTable(String name, TableMeta exclude)
	{
		int i;
		int excl = -1;
		if (exclude!=null) excl=indexOfTable(exclude);

		// This is slow!		
		for (i=0;i<nrTables();i++)
		{
			TableMeta ti = getTable(i); 
			if (i!=excl && ti.getName().equalsIgnoreCase(name))
			{
				return ti; 
			}
		}
		return null;
	}
	
	public int indexOfConnection(Object ci)
	{
		return databases.indexOf(ci);
	}
	public int indexOfTable(Object ti)
	{
		return tables.indexOf(ti);
	}
	public int indexOfRelationship(Object ri)
	{
		return relationships.indexOf(ri);
	}
	public int indexOfNote(Object ni)
	{
		return notes.indexOf(ni);
	}
	
	
			
	

	public int getMaxUndo()
	{
		return max_undo;
	}
	
	public void setMaxUndo(int mu)
	{
		max_undo=mu;
		while (undo.size()>mu && undo.size()>0) undo.remove(0);
	}

	public void addUndo(Object from[], Object to[], int pos[], Point prev[], Point curr[], int type_of_change)
	{
		// First clean up after the current position.
		// Example: position at 3, size=5
		// 012345
		//    ^
		// remove 34
		// Add 4
		// 01234
		
		if (from!=null && from.length==0) return;
		
		while (undo.size()>undo_position+1 && undo.size()>0)
		{
			int last = undo.size()-1;
			undo.remove(last);
		}
	
		TransAction ta = new TransAction();
		switch(type_of_change)
		{
		case TYPE_UNDO_CHANGE   : ta.setChanged(from, to, pos); break;
		case TYPE_UNDO_DELETE   : ta.setDelete(from, pos); break;
		case TYPE_UNDO_NEW      : ta.setNew(from, pos); break;
		case TYPE_UNDO_POSITION : ta.setPosition(from, pos, prev, curr); break;
		}
		undo.add(ta);
		undo_position++;
		
		if (undo.size()>max_undo)
		{
			undo.remove(0);
			undo_position--;
		}
	}
	
	// get previous undo, change position
	public TransAction previousUndo()
	{
		if (undo.size()==0 || undo_position<0) return null;  // No undo left!
		
		TransAction retval = (TransAction)undo.get(undo_position);

		undo_position--;
		
		return retval;
	}

	// View previous undo, don't change position
	public TransAction viewPreviousUndo()
	{
		if (undo.size()==0 || undo_position<0) return null;  // No undo left!
		
		TransAction retval = (TransAction)undo.get(undo_position);
		
		return retval;
	}

	public TransAction nextUndo()
	{
		int size=undo.size();
		if (size==0 || undo_position>=size-1) return null; // no redo left...
		
		undo_position++;
				
		TransAction retval = (TransAction)undo.get(undo_position);
	
		return retval;
	}

	public TransAction viewNextUndo()
	{
		int size=undo.size();
		if (size==0 || undo_position>=size-1) return null; // no redo left...
		
		TransAction retval = (TransAction)undo.get(undo_position+1);
	
		return retval;
	}

	public void selectAll()
	{
		int i;
		for (i=0;i<nrTables();i++)
		{
			TableMeta ti = getTable(i);
			ti.setSelected(true);
		}
	}

	public void unselectAll()
	{
		int i;
		for (i=0;i<nrTables();i++)
		{
			TableMeta ti = getTable(i);
			ti.setSelected(false);
		}
	}

	public int nrSelected()
	{
		int i, count;
		count=0;
		for (i=0;i<nrTables();i++)
		{
			if (getTable(i).isSelected()) count++;
		}
		return count;
	}

	public TableMeta getSelected(int nr)
	{
		int i, count;
		count=0;
		for (i=0;i<nrTables();i++)
		{
			if (getTable(i).isSelected())
			{
				if (nr==count) return getTable(i);
				count++; 
			} 
		}
		return null;
	}

	public String getSelectedName(int nr)
	{
		TableMeta ti = getSelected(nr);
		if (ti!=null) return ti.getName();
		return null;
	}
	
	public void selectInRect(Rectangle rect)
	{
		int i;
		for (i=0;i<nrTables();i++)
		{
			TableMeta ti = getTable(i);
			Point p = ti.getLocation();
			if (rect.contains(p)) ti.setSelected(true);
		}
	}
	
	public Point[] getSelectedLocations()
	{
		int sels = nrSelected();
		Point retval[] = new Point[sels];
		for (int i=0;i<sels;i++)
		{
			TableMeta ti = getSelected(i);
			Point p = ti.getLocation();
			retval[i] = new Point(p.x, p.y); // explicit copy of location
		}
		return retval;
	}

	public TableMeta[] getSelectedTables()
	{
		int sels = nrSelected();
		if (sels==0) return null;
		
		TableMeta retval[] = new TableMeta[sels];
		for (int i=0;i<sels;i++)
		{
			TableMeta si = getSelected(i);
			retval[i] = si;
		}
		return retval;
	}

	public int[] getTableIndexes(TableMeta tables[])
	{
		int retval[] = new int[tables.length];
		
		for (int i=0;i<tables.length;i++) retval[i]=indexOfTable(tables[i]);
		
		return retval;
	}

	public TableMeta getTable(int x, int y, int iconsize)
	{
		int i, s;
		s = nrTables();
		for (i=s-1;i>=0;i--)  // Back to front because drawing goes from start to end
		{
			TableMeta ti = (TableMeta)tables.get(i);
			if (ti.isDrawn()) // Only consider steps from active or inactive hops!
			{
				Point p = ti.getLocation();
				if (p!=null)
				{
					if (   x >= p.x && x <= p.x+iconsize
						&& y >= p.y && y <= p.y+iconsize           
					   )
					{
						return ti;
					}
				}
			}
		}
		return null;
	}

	public NotePadMeta getNote(int x, int y)
	{
		int i, s;
		s = notes.size();
		for (i=s-1;i>=0;i--)  // Back to front because drawing goes from start to end
		{
			NotePadMeta ni = (NotePadMeta )notes.get(i);
			Point loc = ni.getLocation();
			Point p = new Point(loc.x, loc.y);
			if (   x >= p.x && x <= p.x+ni.width+2*Const.NOTE_MARGIN
				&& y >= p.y && y <= p.y+ni.height+2*Const.NOTE_MARGIN
			   )
			{
				return ni;
			}
		}
		return null;
	}


	public RelationshipMeta findRelationship(String name)
	{
		int i;
		for (i=0;i<nrRelationships();i++)
		{
			RelationshipMeta ri = getRelationship(i);
			if (ri.toString().equalsIgnoreCase(name))
			{
				return ri; 
			}
		}
		return null;
	}

	public RelationshipMeta findRelationship(String from, String to)
	{
		int i;
		for (i=0;i<nrRelationships();i++)
		{
			RelationshipMeta ri = getRelationship(i);
			if (ri.getTableFrom().getName().equalsIgnoreCase(from) && 
			    ri.getTableTo().getName().equalsIgnoreCase(to))
			{
				return ri; 
			}
		}
		return null;
	}

	public RelationshipMeta findRelationshipFrom(String name)
	{
		int i;
		for (i=0;i<nrRelationships();i++)
		{
			RelationshipMeta ri = getRelationship(i); 
			if (ri.getTableFrom().getName().equalsIgnoreCase(name)) // return the first
			{
				return ri; 
			}
		}
		return null;
	}

	public RelationshipMeta findRelationshipTo(String name)
	{
		int i;
		for (i=0;i<nrRelationships();i++)
		{
			RelationshipMeta hi = getRelationship(i); 
			if (hi.getTableTo().getName().equalsIgnoreCase(name)) // Return the first!
			{
				return hi; 
			}
		}
		return null;
	}

	public boolean isTableUsedInRelationships(String name)
	{
		RelationshipMeta fr = findRelationshipFrom(name);
		RelationshipMeta to = findRelationshipTo(name);
		if (fr!=null || to!=null) return true;
		return false;
	}

	public ArrayList getRelationshipTables()
	{
		ArrayList st = new ArrayList();
		int idx;
		TableMeta si;
		RelationshipMeta hi;
		
		for (int x=0;x<nrRelationships();x++)
		{
			hi = getRelationship(x);
			si = hi.getTableFrom(); // FROM
			idx = st.indexOf(si);
			if (idx<0) st.add(si);
			
			si = hi.getTableTo();  // TO
			idx = st.indexOf(si);
			if (idx<0) st.add(si);
		}
		
		// Also, add the steps that need to be painted, but are not part of a hop
		for (int x=0;x<nrTables();x++)
		{
			si = getTable(x);
			if (si.isDrawn() && !isTableUsedInRelationships(si.getName()))
			{
				st.add(si);
			}
		}
		
		return st;
	}	

	public Point getMaximum()
	{
		int maxx = 0, maxy = 0;
		for (int i = 0; i < nrTables(); i++)
		{
			TableMeta ti = getTable(i);
			Point loc = ti.getLocation();
			if (loc.x > maxx)
				maxx = loc.x;
			if (loc.y > maxy)
				maxy = loc.y;
		}
		for (int i = 0; i < nrNotes(); i++)
		{
			NotePadMeta ni = getNote(i);
			Point loc = ni.getLocation();
			if (loc.x + ni.width > maxx)
				maxx = loc.x + ni.width;
			if (loc.y + ni.height > maxy)
				maxy = loc.y + ni.height;
		}

		return new Point(maxx + 100, maxy + 100);
	}
	
	
	// Lookup functions in schema:
	
	public TableField findField(String tablename, String fieldname)
	{
		TableMeta ti = findTable(tablename);
		if (ti!=null)
		{
			//System.out.println("Table found: "+ti.toString());
			return ti.findField(fieldname);
		}
		return null;
	}

	public WhereCondition findCondition(String tablename, String conditionname)
	{
		TableMeta ti = findTable(tablename);
		if (ti!=null)
		{
			return ti.findCondition(conditionname);
		}
		return null;
	}
	
	public TableMeta[] getTablesInvolved(TableField fields[])
	{
		Hashtable lookup = new Hashtable();
		
		for (int i=0;i<fields.length;i++)
		{
			/*
			if (fields[i].getTable()==null)
			{
				System.out.println("fields #"+i+" ["+fields[i].getName()+"]");
			}
			*/
			lookup.put(fields[i].getTable(), "OK");
		}
		
		Enumeration en = lookup.keys();
		TableMeta retval[] = new TableMeta[lookup.size()];
		int i=0;
		while (en.hasMoreElements()) 
		{
			retval[i] = (TableMeta)en.nextElement();
			i++;
		}
		return retval;
	}
	
	public RelationshipMeta[] getRelationsInvolved(TableMeta tabs[])
	{
		Hashtable lookup = new Hashtable();
		
		// Store the relationships that are used by the tables...
		for (int i=0;i<nrRelationships();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			for (int j=0;j<tabs.length;j++)
			{
				if (rel.isUsingTable(tabs[j]))
				{
					lookup.put(rel, "OK");
				}
			}
		}
		
		Enumeration en = lookup.keys();
		RelationshipMeta retval[] = new RelationshipMeta[lookup.size()];
		int i=0;
		while (en.hasMoreElements()) 
		{
			retval[i] = (RelationshipMeta)en.nextElement();
			i++;
		}
		return retval;
	}
	
	public int nrNextRelationships(TableMeta table)
	{
		int nr=0;
		
		for (int i=0;i<nrRelationships();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			
			if ( rel.isUsingTable(table))
			{
				nr++;
			}
		}
		
		return nr;
	}

	public RelationshipMeta getNextRelationship(TableMeta table, int getnr)
	{
		int nr=0;
				
		for (int i=0;i<nrRelationships();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			
			if ( rel.isUsingTable(table) )
			{
				if (getnr==nr) 
				{
					RelationshipMeta retval = (RelationshipMeta)rel.clone();
					if (!retval.getTableFrom().getName().equalsIgnoreCase(table.getName()))
					{
						retval.flip();
					}
					return retval;
				} 
				nr++;
			}
		}
		
		return null;
	}
	
	public int getMinimumSize(Vector paths)
	{
		// What's the shortest number of steps between the two tables?
		int min = -1;
		for (int i=0;i<paths.size();i++)
		{
			Path p = (Path)paths.get(i);
			if (min<0 || p.size()<min) min=p.size();
		}
		return min;
	}
	
	public void onlyKeepSize(Vector paths, int min)
	{
		// OK, now only keep the smallest paths between these 2 tables...
		// No need to wander around and keep all junk!
		for (int i=paths.size()-1;i>=0;i--)
		{
			Path p = (Path)paths.get(i);
			if (p.size()>min) paths.remove(i);
		}
	}
	
	public RelationshipMeta findJoin(TableMeta one, TableMeta two)
	{
		for (int i=0;i<nrRelationships();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			if (rel.isUsingTable(one) && rel.isUsingTable(two)) return rel;
		}
		return null;
	}
	
	// Finds the joins between all tables in tabs[]
	//	
	public Joins getJoinsBetween(TableMeta tabs[])
	{
		Joins joins = new Joins(); // list of paths...
		
		Joins alljoins = new Joins(); // All relationships...
		for (int i=0;i<nrRelationships();i++) alljoins.addRelationship(getRelationship(i));
		
		if (tabs.length<2) return joins;
		
		for (int i=0;i<tabs.length;i++)
		{
			for (int j=0;j<tabs.length;j++)
			{
				if (i!=j)
				{
					TableMeta one = tabs[i];
					TableMeta two = tabs[j];
					
					RelationshipMeta rel = findJoin(one, two);
					if (rel!=null) // Ok, we found a relationship.
					{
						if (!joins.contains(rel))
						{
							joins.addRelationship(rel);
							// System.out.println("joins: "+joins);
						}
					}
				}
			}
		}
				
		return joins;
	}

	public int countRelationshipsUsing(TableMeta one)
	{
		int nr=0;
		for (int i = 0; i < nrRelationships(); i++)
		{
			if (getRelationship(i).isUsingTable(one)) nr++;
		}
		return nr;
	}
	
	public RelationshipMeta[] findRelationshipsUsing(TableMeta one)
	{
		RelationshipMeta rels[] = new RelationshipMeta[countRelationshipsUsing(one)];
		
		int nr = 0;
		for (int i = 0; i < nrRelationships(); i++)
		{
			if (getRelationship(i).isUsingTable(one))
			{
				rels[nr] = getRelationship(i);
				nr++;
			}
		}
		
		return rels;
	}
	
	public ArrayList findListOfJoinsBetween(TableMeta tabs[])
	{
		ArrayList list = new ArrayList();
		
		for (int i=0;i<tabs.length;i++)
		{
			for (int j = 0; j < tabs.length; j++)
			{
				TableMeta one = tabs[i];
				TableMeta two = tabs[j];
				Joins path =new Joins();
				
				findListOfJoinsBetween(one, two, list, path);
			}
		}
		
		return list;
	}
	
	public void findListOfJoinsBetween(TableMeta one, TableMeta two, ArrayList list, Joins path)
	{
		// Start by finding all possible joins using table one:
		RelationshipMeta[] rels = findRelationshipsUsing(one);
		for (int i = 0; i < rels.length; i++)
		{
			if (!path.contains(rels[i])) // we haven't gone there yet!
			{
				path.addRelationship(rels[i]); // Go there
				TableMeta next = rels[i].getTableTo();
				if (next.equals(one)) next=rels[i].getTableFrom(); // Oops, it's the other one
				
				if (next.equals(two)) // We reached the other table!
				{
					
				}
			}
		}
	}
		
	public Vector getShortestPathsBetween(TableMeta tabs[])
	{
		if (tabs==null || tabs.length<2) return new Vector();
		
		Vector pathlist[] = new Vector[tabs.length-1];
		Path path = new Path(); // empty path;
		Vector paths=null;
		
		for (int i=0;i<tabs.length-1;i++)
		{
			TableMeta one = tabs[i];		
			TableMeta two = tabs[i+1];
			
			//System.out.println("Checking paths between "+one.getName()+" and "+two.getName()+" (i="+i+")");
			pathlist[i]=new Vector();
			if (i>0)
			{
				Vector prev = pathlist[i-1];
				for (int p=0;p<prev.size();p++) // use the previous list of paths...
				{
					getShortestPathsBetween(one, two, pathlist[i], (Path)prev.get(p));
				}
			}
			else
			{
				getShortestPathsBetween(one, two, pathlist[i], path);
			}
			int min = getMinimumSize(pathlist[i]);
			onlyKeepSize(pathlist[i], min);
			//System.out.println("Keeping "+pathlist[i].size()+" shortest paths between "+one.getName()+" and "+two.getName());
			//System.out.println();
		}
		
		paths=pathlist[tabs.length-2];
		quickSort(paths);
		/*
		for (int p=0;p<paths.size();p++)
		{
			Path pth = (Path)paths.get(p);
			System.out.println("   #"+p+" : "+pth+" ("+pth.score()+")");
		}
		*/
		return paths;
	}
	
	public Joins getAllJoinsBetween(TableMeta tabs[])
	{
		Joins joins = new Joins();
		
		Vector paths = getShortestPathsBetween(tabs);
		if (paths.size()>0)
		{
			// get the one with the smallest score! --> The FIRST, is sorted on score()
			Path path = (Path)paths.get(0);
			
			for (int i=0;i<path.size();i++)
			{
				RelationshipMeta rel = path.getRelationship(i);
				if (!joins.contains(rel)) joins.addRelationship(rel);
			}
		}
		
		return joins;
	}

	
	public void getShortestPathsBetween(TableMeta one, TableMeta two, Vector paths, Path path)
	{
		RelationshipMeta rels[] = findRelationshipsUsing(one);
		for (int i=0;i<rels.length;i++)
		{
			RelationshipMeta rel = (RelationshipMeta)rels[i].clone();
			if (!rel.getTableFrom().equals(one))
			{
				rel.flip();
			}
			
			// System.out.println("  Checking out relationship : "+rel+" after path ["+path+"]");
			
			if (!path.contains(rel)) // Let's not go endlessly round and round!
			{
				path.addRelationship(rel); // go and explore this possibility...
				
				TableMeta next = rel.getTableTo();
				if (!next.equals(two))
				{
					getShortestPathsBetween(rel.getTableTo(), two, paths, path);
				}
				else
				{
					paths.add((Path)path.clone());
				}
				
				path.removeRelationship(); // Undo this possibility
			}
		}
	}
		
	// Make a list of all facts with aggr. type <> none
	// if the list is not empty, make a group by list: all dimensions & aggr.type==none
	// place sum()/etc functions over field
	// add group by line...
	// Klair!
	
	public String getSQL(TableField fields[], Joins joins, WhereCondition c[])
	{
		String sql = null;
		
		TableMeta tabs[] = joins.getUsedTables();
		
		if (tabs.length>0)
		{
			sql="SELECT ";
			
			// ADD THE FIELDS
			// Format :   TABELNAAM.REAL
			// 
			
			boolean group = hasFactsInIt(fields);
			
			if (!group) sql+="DISTINCT ";
			sql+=Const.CR;
			
			for (int i=0;i<fields.length;i++)
			{
				TableField f = fields[i];
				DatabaseMeta dbinfo = f.getTable().getDatabase();
				
				if (i>0) sql+="         ,"; else sql+="          ";

				sql+=fields[i].getSelectField(dbinfo, i);				
				
				sql+=Const.CR;
			}
			
			sql+="FROM "+Const.CR;
			for (int i=0;i<tabs.length;i++)
			{
				TableMeta table = tabs[i];
				
				if (i>0) sql+="         ,"; else sql+="          ";
				sql+=table.getDBName()+" "+table.getName();
				sql+=Const.CR;
			}
			
			sql+="WHERE "+Const.CR;
			int nr = 0;
			for (int i=0;i<joins.size();i++, nr++)
			{
				RelationshipMeta relation = joins.getRelationship(i);

				if (nr>0) sql+="      AND "; else sql+="          ";
				sql+=relation.getJoin();
				sql+=Const.CR;
			}
			for (int i=0;i<c.length;i++, nr++)
			{
				if (nr>0) sql+="      AND "; else sql+="          ";
				sql+=c[i].getWhereClause();
				sql+=Const.CR;
			}
			
			if (group)
			{
				sql+="GROUP BY "+Const.CR;
				boolean first=true;
				for (int i=0;i<fields.length;i++)
				{
					TableField f = fields[i];
					if (!f.hasAggregate())
					{
						if (!first) sql+="         ,"; else sql+="          ";
						first=false;
						sql+=fields[i].getAliasField();
						sql+=Const.CR;
					}
				}
			}
		}
		
		return sql;
	}
	
	public String getSQL(TableField f[], WhereCondition c[])
	{
		String sql=null;
		
		// These are the tables involved in the field selection:
		TableMeta tabs[] = getTablesInvolved(f);
		
		Joins joins = getAllJoinsBetween(tabs);
		
		if (joins.size()>0)
		{
			sql = getSQL(f, joins, c);
		}
		
		return sql;
	}

	
	public boolean hasFactsInIt(TableField fields[])
	{
		for (int i=0;i<fields.length;i++)
		{
			if (fields[i].hasAggregate()) return true;
		}
		return false;
	}
	
	
	/** Sort the entire vector, if it is not empty
	 */
	public synchronized void quickSort(Vector elements)
	{
		if (! elements.isEmpty())
		{ 
			quickSort(elements, 0, elements.size()-1);
		}
	}


	/**
	 * QuickSort.java by Henk Jan Nootenboom, 9 Sep 2002
	 * Copyright 2002-2003 SUMit. All Rights Reserved.
	 *
	 * Algorithm designed by prof C. A. R. Hoare, 1962
	 * See http://www.sum-it.nl/en200236.html
	 * for algorithm improvement by Henk Jan Nootenboom, 2002.
	 *
	 * Recursive Quicksort, sorts (part of) a Vector by
	 *  1.  Choose a pivot, an element used for comparison
	 *  2.  dividing into two parts:
	 *      - less than-equal pivot
	 *      - and greater than-equal to pivot.
	 *      A element that is equal to the pivot may end up in any part.
	 *      See www.sum-it.nl/en200236.html for the theory behind this.
	 *  3. Sort the parts recursively until there is only one element left.
	 *
	 * www.sum-it.nl/QuickSort.java this source code
	 * www.sum-it.nl/quicksort.php3 demo of this quicksort in a java applet
	 *
	 * Permission to use, copy, modify, and distribute this java source code
	 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
	 * without fee is hereby granted.
	 * See http://www.sum-it.nl/security/index.html for copyright laws.
	 */
	  private synchronized void quickSort(Vector elements, int lowIndex, int highIndex)
	  { 
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		Path pivotValue;  // values are Strings in this demo, change to suit your application
		Path lowToHighValue;
		Path highToLowValue;
		Path parking;
		int newLowIndex;
		int newHighIndex;
		int compareResult;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		/** Choose a pivot, remember it's value
		 *  No special action for the pivot element itself.
		 *  It will be treated just like any other element.
		 */
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotValue = (Path)elements.elementAt(pivotIndex);

		/** Split the Vector in two parts.
		 *
		 *  The lower part will be lowIndex - newHighIndex,
		 *  containing elements <= pivot Value
		 *
		 *  The higher part will be newLowIndex - highIndex,
		 *  containting elements >= pivot Value
		 * 
		 */
		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		// loop until low meets high
		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
		{ // loop from low to high to find a candidate for swapping
		  lowToHighValue = (Path)elements.elementAt(lowToHighIndex);
		  while (lowToHighIndex < newLowIndex
			& lowToHighValue.compare(pivotValue)<0 )
		  { 
			newHighIndex = lowToHighIndex; // add element to lower part
			lowToHighIndex ++;
			lowToHighValue = (Path)elements.elementAt(lowToHighIndex);
		  }

		  // loop from high to low find other candidate for swapping
		  highToLowValue = (Path)elements.elementAt(highToLowIndex);
		  while (newHighIndex <= highToLowIndex
			& (highToLowValue.compare(pivotValue)>0)
			)
		  { 
			newLowIndex = highToLowIndex; // add element to higher part
			highToLowIndex --;
			highToLowValue = (Path)elements.elementAt(highToLowIndex);
		  }

		  // swap if needed
		  if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
		  { 
			newHighIndex = lowToHighIndex; // move element arbitrary to lower part
		  }
		  else if (lowToHighIndex < highToLowIndex) // not last element yet
		  { 
			compareResult = lowToHighValue.compare(highToLowValue);
			if (compareResult >= 0) // low >= high, swap, even if equal
			{ 
			  parking = lowToHighValue;
			  elements.setElementAt(highToLowValue, lowToHighIndex);
			  elements.setElementAt(parking, highToLowIndex);

			  newLowIndex = highToLowIndex;
			  newHighIndex = lowToHighIndex;

			  lowToHighIndex ++;
			  highToLowIndex --;
			}
		  }
		}

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{ 
			this.quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
		}
		if (newLowIndex < highIndex)
		{ 
			this.quickSort(elements, newLowIndex, highIndex); // sort higher subpart
		}
	  }


	public boolean loadXML(String fname, boolean merge)
	{
		if (!merge) filename=fname; else setChanged();
		
		try
		{
			Document doc = XMLHandler.loadXMLFile(fname);
			if (doc!=null)
			{
				// Clear the schema
				clearUndo();
				if (!merge) clear();
				
				// Get the root schema node
				Node schemanode = XMLHandler.getSubNode(doc, "schema");
			
				return loadXML(schemanode, merge);
			}
			else
			{
				log.logError(toString(), "Error reading/validating information from XML file : "+fname);
				return false;
			}
		}
		catch(KettleException e)
		{
			log.logError(toString(), "Error reading/validating information from XML file : "+fname);
			return false;
		}
	}

	public boolean loadXML(Node schemanode, boolean merge)
	{
		int n;
		
		System.out.println("Loading schema from XML!");
		
		try
		{			
			// Handle connections
			n = XMLHandler.countNodes(schemanode, "connection");
			log.logDebug(toString(), "We have "+n+" connections...");
			System.out.println("We have "+n+" connections...");
			for (int i=0;i<n;i++)
			{
				log.logDebug(toString(), "Looking at connection #"+i);
				Node dbnode = XMLHandler.getSubNodeByNr(schemanode, "connection", i);
				DatabaseMeta dbcon = new DatabaseMeta(dbnode);

				DatabaseMeta exist = findConnection(dbcon.getName());
				if (exist==null) addConnection(dbcon);
				else
				{
					int idx = indexOfConnection(exist);
					removeConnection(idx);
					addConnection(idx, dbcon);
				}
			}

			//
			// get transformation info:
			//
			// The Info node:
			Node infonode = XMLHandler.getSubNode(schemanode, "info");
			
			// Name
			name              = XMLHandler.getTagValue(infonode, "name");
			
			// Read the notes...
			Node notepadsnode = XMLHandler.getSubNode(schemanode, "notepads");
			int nrnotes = XMLHandler.countNodes(notepadsnode, "notepad");
			System.out.println("We have "+nrnotes+" notes.");
			for (int i=0;i<nrnotes;i++)
			{
				Node notepadnode = XMLHandler.getSubNodeByNr(notepadsnode, "notepad", i); 
				NotePadMeta ni = new NotePadMeta(notepadnode);
				notes.add(ni);
			}
			
			// Tables:
			Node tablesnode = XMLHandler.getSubNode(schemanode, "tables");
			int nrtables = XMLHandler.countNodes(tablesnode, "table");
			log.logDebug(toString(), "We have "+nrtables+" tables...");
			System.out.println("We have "+nrtables+" tables.");
			for (int i=0;i<nrtables;i++) 
			{
				Node tablenode = XMLHandler.getSubNodeByNr(tablesnode, "table", i);
				TableMeta ti = new TableMeta();
				if (ti.loadXML(tablenode, databases))
				{
					addTable(ti);
				}
				else
				{
					log.logError(toString(), "Error reading table #"+i+" from XML");
					return false;
				}
			}
							
			// Handle Relationships
			Node relsnode = XMLHandler.getSubNode(schemanode, "relationships");
			n = XMLHandler.countNodes(relsnode, "relationship");
			
			// n = sir.countRelationships();
			log.logDebug(toString(), "We have "+n+" relationships...");
			System.out.println("We have "+n+" relationships.");
			for (int i=0;i<n;i++)
			{
				Node relnode = XMLHandler.getSubNodeByNr(relsnode, "relationship", i);
				
				log.logDebug(toString(), "Looking at relationship #"+i);
				RelationshipMeta ri = new RelationshipMeta();
				if (ri.loadXML(relnode, tables))
				{
					if (merge)
					{
						RelationshipMeta exist = findRelationship(ri.toString());
						if (exist==null) addRelationship(ri);
						else
						{
							log.logDebug(toString(), "Replacing hop #"+i);
							int idx = indexOfRelationship(exist);
							removeRelationship(idx);
							addRelationship(idx, ri);
						}
						log.logDebug(toString(), "Read relation: "+ri.toString());
					}
					else // Just add the hop, it can't have doubles
					{
						addRelationship(ri);
					}
				}
				else
				{
					log.logError(toString(), "Error reading relationship #"+i+" from XML");
					return false;
				}
			}
			
			// Read the selection...
			
			// Fields:
			Node selnode = XMLHandler.getSubNode(schemanode, "selection");

			int nrselfields = XMLHandler.countNodes(selnode, "field");
			log.logDebug(toString(), "We have "+nrselfields+" fields...");
			System.out.println("We have "+nrselfields+" fields.");
			for (int i=0;i<nrselfields;i++) 
			{
				Node fieldnode = XMLHandler.getSubNodeByNr(selnode, "field", i);
				
				String field = XMLHandler.getTagValue(fieldnode, "name");
				String table = XMLHandler.getTagValue(fieldnode, "table");
				TableField f = findField(table, field);
				addSelField(f);
			}
			// Conditions:
			int nrselconditions = XMLHandler.countNodes(selnode, "condition");
			log.logDebug(toString(), "We have "+nrselconditions+" conditions...");
			System.out.println("We have "+nrselconditions+" selected conditions.");
			for (int i=0;i<nrselconditions;i++) 
			{
				Node condnode = XMLHandler.getSubNodeByNr(selnode, "condition", i);
				
				String name  = XMLHandler.getTagValue(condnode, "name");
				String table = XMLHandler.getTagValue(condnode, "table");
				WhereCondition c = findCondition(table, name);
				addSelCondition(c);
			}
			// Cube file
			selcubefile = XMLHandler.getTagValue(selnode, "cubefile");
	
			log.logDebug(toString(), "nr of connections   read : "+nrConnections());
			log.logDebug(toString(), "nr of tables        read : "+nrTables());
			log.logDebug(toString(), "nr of relationships read : "+nrRelationships());
			log.logDebug(toString(), "nr of notes         read : "+nrNotes());
			log.logDebug(toString(), "nr of selfields     read : "+nrSelFields());
	
			System.out.println("nr of connections   read : "+nrConnections());
			System.out.println("nr of tables        read : "+nrTables());
			System.out.println("nr of relationships read : "+nrRelationships());
			System.out.println("nr of notes         read : "+nrNotes());
			System.out.println("nr of selfields     read : "+nrSelFields());
			

			return true;
		}
		catch(Exception e)
		{
			log.logError(toString(), "Error reading information from file : "+e.toString());
			e.printStackTrace();
			return false;
		}
	}


	public String getXML()
	{
		String retval = "";
		retval+="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+Const.CR;
		retval+="<schema>"+Const.CR;
		
		retval+="  <info>"+Const.CR;
		
		retval+="    "+XMLHandler.addTagValue("name", name);

		retval+="    <notepads>"+Const.CR;
		if (notes!=null)
		for (int i=0;i<nrNotes();i++)
		{
			retval+= getNote(i).getXML();
		}
		retval+="      </notepads>"+Const.CR;	
		
		retval+="    </info>"+Const.CR;
		
		for (int i=0;i<nrConnections();i++)
		{
			retval+=getConnection(i).getXML();
		}
		
		retval+="  <tables>"+Const.CR;
		for (int i=0;i<nrTables();i++)
		{
			retval+=getTable(i).getXML();
		}
		retval+="  </tables>"+Const.CR+Const.CR;

		retval+="  <relationships>"+Const.CR;
		for (int i=0;i<nrRelationships();i++)
		{
			retval+=getRelationship(i).getXML();
		}
		retval+="  </relationships>"+Const.CR+Const.CR;

		retval+="  <selection>"+Const.CR;
		for (int i=0;i<nrSelFields();i++)
		{
			if (getSelField(i)!=null) retval+=getSelField(i).getXML();
		}
		for (int i=0;i<nrSelConditions();i++)
		{
			retval+=getSelCondition(i).getXML();
		}
		retval+="    "+XMLHandler.addTagValue("cubefile", selcubefile);
		
		retval+="  </selection>"+Const.CR+Const.CR;
		
		retval+="</schema>"+Const.CR;		 
		
		return retval;
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
