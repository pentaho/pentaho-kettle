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
import java.util.Enumeration;
import java.util.Hashtable;

/*
 * Created on 30-jan-04
 *
 */

public class Path
{
	private ArrayList path;  // contains Relationship objects
	
	public Path()
	{
		path=new ArrayList();
	}
	
	public void addRelationship(RelationshipMeta rel)
	{
		path.add(rel);
	}
	
	public void removeRelationship()
	{
		path.remove(size()-1);
	}
	
	public RelationshipMeta getLastRelationship()
	{
		return (RelationshipMeta)path.get(size()-1);
	}

	public int size()
	{
		return path.size();
	}
	
	public int nrTables()
	{
		return getUsedTables().length;
	}
	
	public int score()
	{
		int score=0;
		for (int i=0;i<size();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			TableMeta from = rel.getTableFrom();
			if (from.getSize()>0) score+=from.getSize();
		}
		if (size()>0)
		{
			TableMeta to = getLastRelationship().getTableTo();
			if (to.getSize()>0) score+=to.getSize();
		}
		return score;
	}
	
	public RelationshipMeta getRelationship(int i)
	{
		return (RelationshipMeta)path.get(i);
	}
		
	public boolean contains(Path in)
	{
		if (in.size()==0) return false;
		
		for (int i=0;i<size();i++)
		{
			int nr=0;
			while (getRelationship(i+nr).equals(in.getRelationship(nr)) && 
			       nr<in.size() &&
			       i+nr<size()
			      )
			{
				nr++;
			}
			if (nr==in.size()) return true;
		}
		return false;
	}

	public boolean contains(RelationshipMeta rel)
	{
		if (rel==null) return false;
		
		for (int i=0;i<size();i++)
		{
			RelationshipMeta check = getRelationship(i);
			if (check.equals(rel)) return true;
		}
		return false;
	}

	public boolean contains(TableMeta tab)
	{
		if (tab==null) return false;
		
		for (int i=0;i<size();i++)
		{
			RelationshipMeta check = getRelationship(i);
			if (check.isUsingTable(tab)) return true;
		}
		return false;
	}

	public boolean contains(TableMeta tabs[])
	{
		if (tabs==null) return false;
		
		boolean all=true;
		for (int i=0;i<tabs.length && all;i++)
		{
			if (!contains(tabs[i])) all=false;
		}
		return all;
	}

	public boolean contains(ArrayList tabs)
	{
		if (tabs==null) return false;
		
		boolean all=true;
		for (int i=0;i<tabs.size() && all;i++)
		{
			if (!contains((TableMeta)tabs.get(i))) all=false;
		}
		return all;
	}

	public Object clone()
	{
		Path retval   = new Path();
		
		for (int i=0;i<size();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			retval.addRelationship(rel);
		}
		
		return retval;
	}
	
	public String toString()
	{
		String path="";
		if (size()>0)
		{
			path+=getRelationship(0).getTableFrom().getName();
		}
		for (int i=0;i<size();i++)
		{
			path+="-->"+getRelationship(i).getTableTo().getName();
		}
		return path;
	}
	
	// Compare two paths: first on the number of tables used!!!
	public int compare(Path path)
	{
		int diff=size()-path.size();
		if (diff==0)
		{
			diff=nrTables()-path.nrTables();
			if (diff==0)
			{
				diff=score() - path.score();
			}
		}
		if (diff<0) return -1;
		else if (diff>0) return 1;
		else return 0;
	}
	
	public TableMeta[] getUsedTables()
	{
		Hashtable hash = new Hashtable();
		
		for (int i=0;i<size();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			hash.put(rel.getTableFrom(), "OK");
			hash.put(rel.getTableTo(), "OK");
		}
		Enumeration en = hash.keys();
		TableMeta tabs[] = new TableMeta[hash.size()];
		
		int i=0;
		while (en.hasMoreElements())
		{
			tabs[i]=(TableMeta)en.nextElement();
			i++;
		}
		
		return tabs;
	}

	public RelationshipMeta[] getUsedRelationships()
	{
		ArrayList list = new ArrayList();
		
		for (int i=0;i<size();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			boolean exists=false;
			for (int j=0;j<list.size() && !exists;j++)
			{
				RelationshipMeta check = (RelationshipMeta)list.get(j);
				if ( check.isUsingTable( rel.getTableFrom()) &&
				     check.isUsingTable( rel.getTableTo())
				   ) exists=true;
			}
			if (!exists) list.add(rel);
		}
		
		RelationshipMeta rels[] = new RelationshipMeta[list.size()];
		for (int i=0;i<list.size();i++) rels[i] = (RelationshipMeta)list.get(i);

		return rels;
	}	
}
