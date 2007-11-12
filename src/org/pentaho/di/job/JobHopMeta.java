 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.job;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;




/**
 * This class defines a hop from one job entry copy to another.
 * 
 * @author Matt 
 * @since 19-06-2003
 *
 */
public class JobHopMeta implements Cloneable, XMLInterface
{
	public JobEntryCopy from_entry, to_entry;
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
			
			from_entry = job.findJobEntry(from_name, from_nr, true);
			to_entry = job.findJobEntry(to_name, to_nr, true);
			
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
        StringBuffer retval = new StringBuffer(200);
		if ((null != from_entry) && (null != to_entry))
		{
		retval.append("    <hop>").append(Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("from",          from_entry.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("to",            to_entry.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("from_nr",       from_entry.getNr()));
		retval.append("      ").append(XMLHandler.addTagValue("to_nr",         to_entry.getNr()));
		retval.append("      ").append(XMLHandler.addTagValue("enabled",       enabled));
		retval.append("      ").append(XMLHandler.addTagValue("evaluation",    evaluation));
		retval.append("      ").append(XMLHandler.addTagValue("unconditional", unconditional));
		retval.append("    </hop>").append(Const.CR);
		}
		
		return retval.toString();
	}

	public JobHopMeta(Repository rep, long id_job_hop, JobMeta job, List<JobEntryCopy> jobcopies) throws KettleException
	{
		try
		{
			long id_jobentry_copy_from;
			long id_jobentry_copy_to;
			
			RowMetaAndData r = rep.getJobHop(id_job_hop);
			if (r!=null)
			{
				// System.out.println("Got hop row for id="+id_job_hop+" --> "+r);
	
				id_jobentry_copy_from  =  r.getInteger("ID_JOBENTRY_COPY_FROM", -1L);
				id_jobentry_copy_to    =  r.getInteger("ID_JOBENTRY_COPY_TO", -1L);
				enabled                =  r.getBoolean("ENABLED", true);
				evaluation             =  r.getBoolean("EVALUATION", true);
				unconditional          =  r.getBoolean("UNCONDITIONAL", !evaluation);
				
				from_entry = JobMeta.findJobEntryCopy(jobcopies, id_jobentry_copy_from);
				to_entry = JobMeta.findJobEntryCopy(jobcopies, id_jobentry_copy_to);
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job hop with id_job_hop = "+id_job_hop, dbe);
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