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

public class Joins
{
	private ArrayList joins;  // contains Relationship objects
	
	public Joins()
	{
		joins=new ArrayList();
	}
	
	public void addRelationship(RelationshipMeta rel)
	{
		joins.add(rel);
	}
	
	public void removeRelationship()
	{
		joins.remove(size()-1);
	}
	
	public RelationshipMeta getLastRelationship()
	{
		return (RelationshipMeta)joins.get(size()-1);
	}

	public int size()
	{
		return joins.size();
	}
	
	public int score()
	{
		int score=0;
		for (int i=0;i<size();i++)
		{
			RelationshipMeta rel = getRelationship(i);
			TableMeta from = rel.getTableFrom();
			if (from.getSize()>0) score+=from.getSize();
			TableMeta to = rel.getTableTo();
			if (to.getSize()>0) score+=to.getSize();
		}
		return score;
	}
	
	public RelationshipMeta getRelationship(int i)
	{
		return (RelationshipMeta)joins.get(i);
	}
		
	public boolean contains(Joins in)
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
			if (check.isUsingTable(rel.getTableFrom()) && check.isUsingTable(rel.getTableTo())) return true;
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
		Joins retval   = new Joins();
		
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
		for (int i=0;i<size();i++)
		{
			if (i>0) path+=", ";
			path+=getRelationship(i).getTableFrom().getName()+"-->"+getRelationship(i).getTableTo().getName();
		}
		return path;
	}
	
	public int compare(Joins path)
	{
		int diff=size()-path.size();
		if (diff==0)
		{
			diff=score() - path.score();
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
