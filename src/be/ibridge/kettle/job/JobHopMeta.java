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
 
package be.ibridge.kettle.job;
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.repository.Repository;


/**
 * This class defines a hop from one job entry copy to another.
 * 
 * @author Matt 
 * @since 19-06-2003
 *
 */

public class JobHopMeta implements Cloneable, XMLInterface
{
	//public String from_step, to_step;
	public JobEntryCopy from_entry, to_entry;
	//public int from_nr, to_nr;
	private boolean enabled;
	private boolean split;
	private boolean evaluation;
	private boolean unconditional;
	
	private boolean changed;
	
	private long id;

	public JobHopMeta()
	{
		this((JobEntryCopy)null, (JobEntryCopy)null);
	}
	
	public JobHopMeta(JobEntryCopy from, JobEntryCopy to)
	{
		from_entry    = from;
		to_entry      = to;
		enabled       = true;
		split         = false;
		evaluation    = true;
		unconditional = false;
		id            = -1L;
		
		if (from.isStart()) setUnconditional();
	}
	
	public JobHopMeta(Node hopnode, JobMeta job)
		throws KettleXMLException
	{
		try
		{
			String from_name      = XMLHandler.getTagValue(hopnode, "from");
			String to_name        = XMLHandler.getTagValue(hopnode, "to");
			String sfrom_nr       = XMLHandler.getTagValue(hopnode, "from_nr");
			String sto_nr         = XMLHandler.getTagValue(hopnode, "to_nr");
			String senabled       = XMLHandler.getTagValue(hopnode, "enabled");
			String sevaluation    = XMLHandler.getTagValue(hopnode, "evaluation");
			String sunconditional = XMLHandler.getTagValue(hopnode, "unconditional");
			
			int from_nr, to_nr;
			from_nr = Const.toInt(sfrom_nr, 0);
			to_nr = Const.toInt(sto_nr, 0);
			
			from_entry = job.findJobEntry(from_name, from_nr);
			to_entry = job.findJobEntry(to_name, to_nr);
			
			if (senabled==null) enabled=true; else enabled="Y".equalsIgnoreCase(senabled);
			if (sevaluation==null) evaluation=true; else evaluation="Y".equalsIgnoreCase(sevaluation);
			unconditional="Y".equalsIgnoreCase(sunconditional);
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load job hop info from XML node", e);
		}
	}

	public String getXML()
	{
		String retval="";
		
		retval+="    <hop>"+Const.CR;
		retval+="      "+XMLHandler.addTagValue("from",          from_entry.getName());
		retval+="      "+XMLHandler.addTagValue("to",            to_entry.getName());
		retval+="      "+XMLHandler.addTagValue("from_nr",       from_entry.getNr());
		retval+="      "+XMLHandler.addTagValue("to_nr",         to_entry.getNr());
		retval+="      "+XMLHandler.addTagValue("enabled",       enabled);
		retval+="      "+XMLHandler.addTagValue("evaluation",    evaluation);
		retval+="      "+XMLHandler.addTagValue("unconditional", unconditional);
		retval+="      </hop>"+Const.CR;
		
		return retval;
	}

	public JobHopMeta(Repository rep, long id_job_hop, JobMeta job, ArrayList jobcopies)
		throws KettleException
	{
		try
		{
			long id_jobentry_copy_from;
			long id_jobentry_copy_to;
			
			Row r = rep.getJobHop(id_job_hop);
			if (r!=null)
			{
				// System.out.println("Got hop row for id="+id_job_hop+" --> "+r);
	
				id_jobentry_copy_from  =  r.getInteger("ID_JOBENTRY_COPY_FROM", -1L);
				id_jobentry_copy_to    =  r.getInteger("ID_JOBENTRY_COPY_TO", -1L);
				enabled                =  r.getBoolean("ENABLED", true);
				evaluation             =  r.getBoolean("EVALUATION", true);
				unconditional          =  r.getBoolean("UNCONDITIONAL", !evaluation);
				
				from_entry = Const.findJobEntryCopy(jobcopies, id_jobentry_copy_from);
				to_entry = Const.findJobEntryCopy(jobcopies, id_jobentry_copy_to);
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job hop with id_job_hop="+id_job_hop, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			long id_jobentry_from=-1, id_jobentry_to=-1;
			
			id_jobentry_from = from_entry==null ? -1 : from_entry.getID();
			id_jobentry_to = to_entry==null ? -1 : to_entry.getID();
			
			// Insert new transMeta hop in repository
			setID( rep.insertJobHop(id_job, id_jobentry_from, id_jobentry_to, enabled, evaluation, unconditional) );
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job hop with id_job = "+id_job, dbe);
		}
	}
	
	public void setID(long id)
	{
		this.id = id;
	}
	
	public long getID()
	{
		return id;
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
	
	public boolean getEvaluation()
	{
		return evaluation;
	}

	public void setEvaluation()
	{
		setEvaluation(true);
	}

	public void setEvaluation(boolean e)
	{
		evaluation=e;
	}
	
	public void setUnconditional()
	{
		if (!unconditional) setChanged();
		unconditional=true;
	}
	
	public void setConditional()
	{
		if (unconditional) setChanged();
		unconditional=false;
	}
	
	public boolean isUnconditional()
	{
		return unconditional;
	}
	
	public void setSplit(boolean split)
	{
		if (this.split!=split) setChanged();
		this.split = split;
	}
	
	public boolean isSplit()
	{
		return split;
	}
	
	public String getDescription()
	{
		if (isUnconditional()) return "Execute the next job entry unconditonally";
		else
		{
			if (getEvaluation()) return "Execute the next job entry if the previous one ran flawless.";
			else                 return "Execute the next job entry if the previous one failed.";
		}
	}

	public String toString()
	{
		return getDescription();
		//return from_entry.getName()+"."+from_entry.getNr()+" --> "+to_entry.getName()+"."+to_entry.getNr();
	}	
}
