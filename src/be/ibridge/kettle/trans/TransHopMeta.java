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
 
package be.ibridge.kettle.trans;
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.step.StepMeta;


/*
 * Created on 19-jun-2003
 *
 */

public class TransHopMeta implements Cloneable, XMLInterface, Comparable
{
	private StepMeta from_step;
	private StepMeta to_step;
	private boolean  enabled;
	
	public boolean split = false;
	
	private boolean changed;
	
	private long id;

	public TransHopMeta(StepMeta from, StepMeta to, boolean en)
	{
		from_step  = from;
		to_step    = to;
		enabled    = en;
	}
	
	public TransHopMeta(StepMeta from, StepMeta to)
	{
		from_step = from;
		to_step   = to;
		enabled   = true;
	}
	
	public TransHopMeta()
	{
		this(null, null, false);
	}
	
	public TransHopMeta(Node hopnode, ArrayList steps)
		throws KettleXMLException
	{
		try
		{
			from_step = searchStep( steps, XMLHandler.getTagValue(hopnode, "from") );
			to_step   = searchStep( steps, XMLHandler.getTagValue(hopnode, "to") );
			String en = XMLHandler.getTagValue(hopnode, "enabled");
			
			if (en==null) enabled   =true; else enabled   =en.equalsIgnoreCase("Y");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load hop info from XML node", e);
		}
	}
	
	public void setFromStep(StepMeta from)
	{
		from_step = from;
	}

	public void setToStep(StepMeta to)
	{
		to_step = to;
	}
	
	public StepMeta getFromStep()
	{
		return from_step;
	}
	
	public StepMeta getToStep()
	{
		return to_step;
	}
	
	private StepMeta searchStep(ArrayList steps, String name)
	{
		for (int i=0;i<steps.size();i++)
		{
			StepMeta stepMeta = (StepMeta)steps.get(i);
			if (stepMeta.getName().equalsIgnoreCase(name)) return stepMeta;
		}
		return null;
	}
	
	public TransHopMeta(Repository rep, long id_trans_hop, ArrayList steps)
		throws KettleException
	{
		try
		{
			setID(id_trans_hop);
			
			Row r = rep.getTransHop(id_trans_hop);
			
			long id_step_from = r.searchValue("ID_STEP_FROM").getInteger();
			long id_step_to   = r.searchValue("ID_STEP_TO").getInteger();
			enabled           = r.searchValue("ENABLED").getBoolean();
			
			from_step = TransMeta.findStep(steps, id_step_from);
			to_step   = TransMeta.findStep(steps, id_step_to);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load Transformation hop info from the repository with id_trans_hop="+id_trans_hop, dbe);
		}
	}
		

	public void saveRep(Repository rep, long id_transformation)
		throws KettleException
	{
		try
		{
			// See if a transformation hop with the same fromstep and tostep is already available...
			long id_step_from = from_step==null ? -1 : from_step.getID();
			long id_step_to   = to_step==null   ? -1 : to_step.getID();
			
			// Insert new transMeta hop in repository
			setID( rep.insertTransHop(id_transformation, id_step_from, id_step_to, enabled) );
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save transformation hop info to the database for id_transformation="+id_transformation, dbe);
		}
	}

	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public boolean equals(Object obj)
	{
		TransHopMeta other = (TransHopMeta)obj;
		if (from_step==null || to_step==null) return false;
		return 
			from_step.equals(other.getFromStep()) && 
			to_step.equals(other.getToStep())
			;
	}

	/**
	 * Compare 2 hops.
	 */
	public int compareTo(Object obj)
	{
		return toString().compareTo(((TransHopMeta)obj).toString());
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setID(long id)
	{
		this.id = id;
	}

	public void setChanged()
	{
		setChanged(true);
	}

	public void setChanged(boolean ch)
	{
		changed=ch;
	}

	public boolean hasChanged()
	{
		return changed;
	}

	public void setEnabled()
	{
		setEnabled(true);
	}

	public void setEnabled(boolean en)
	{
		enabled=en;
		setChanged();
	}

	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void flip()
	{
		StepMeta dummy = from_step;
		from_step = to_step;
		to_step = dummy;
	}



	public String toString()
	{
		String str_fr = (from_step==null)?"(empty)":from_step.getName();
		String str_to = (to_step==null)?"(empty)":to_step.getName();
		return str_fr+" --> "+str_to+" ("+(enabled?"enabled":"disabled")+")";
	}
	
	public String getXML()
	{
		String retval="";
		
		retval+="  <hop> ";
		retval+=XMLHandler.addTagValue("from",    from_step.getName(), false);
		retval+=XMLHandler.addTagValue("to",      to_step.getName(), false);
		retval+=XMLHandler.addTagValue("enabled", enabled, false);
		retval+=" </hop>";
		
		return retval;
	}
}
