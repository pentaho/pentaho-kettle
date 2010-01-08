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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.ObjectId;
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
	private static final String	XML_TAG	= "hop";

	private static Class<?> PKG = JobHopMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private JobEntryCopy from_entry, to_entry;
	private boolean enabled;
	public boolean split = false;
	private boolean evaluation;
	private boolean unconditional;
	
	private boolean changed;
	
	private ObjectId id;

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
		id            = null;
		
		if (from!=null && from.isStart()) {
			setUnconditional();
		}
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
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobHopMeta.Exception.UnableToLoadHopInfoXML"), e);
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
		if ((null != from_entry) && (null != to_entry))
		{
		retval.append("    ").append(XMLHandler.openTag(XML_TAG)).append(Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("from",          from_entry.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("to",            to_entry.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("from_nr",       from_entry.getNr()));
		retval.append("      ").append(XMLHandler.addTagValue("to_nr",         to_entry.getNr()));
		retval.append("      ").append(XMLHandler.addTagValue("enabled",       enabled));
		retval.append("      ").append(XMLHandler.addTagValue("evaluation",    evaluation));
		retval.append("      ").append(XMLHandler.addTagValue("unconditional", unconditional));
		retval.append("    ").append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);
		}
		
		return retval.toString();
	}
	
	public void setObjectId(ObjectId id)
	{
		this.id = id;
	}
	
	public ObjectId getObjectId()
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
		if (isUnconditional()) return BaseMessages.getString(PKG, "JobHopMeta.Msg.ExecNextJobEntryUncondition");
		else
		{
			if (getEvaluation()) return BaseMessages.getString(PKG, "JobHopMeta.Msg.ExecNextJobEntryFlawLess");
			else                 return BaseMessages.getString(PKG, "JobHopMeta.Msg.ExecNextJobEntryFailed");
		}
	}

	public String toString()
	{
		return getDescription();
		//return from_entry.getName()+"."+from_entry.getNr()+" --> "+to_entry.getName()+"."+to_entry.getNr();
	}	
	
	public JobEntryCopy getFromEntry() {
		return from_entry;
	}
	
	public void setFromEntry(JobEntryCopy fromEntry) {
		this.from_entry = fromEntry;
	}
	
	public JobEntryCopy getToEntry() {
		return to_entry;
	}
	
	public void setToEntry(JobEntryCopy toEntry) {
		this.to_entry = toEntry;
	}

	/**
	 * @param unconditional the unconditional to set
	 */
	public void setUnconditional(boolean unconditional) {
		this.unconditional = unconditional;
	}

}