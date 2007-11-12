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

package org.pentaho.di.job.entry;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * This class describes the fact that a single JobEntry can be used multiple
 * times in the same Job. Therefor it contains a link to a JobEntry, a position,
 * a number, etc.
 * 
 * @author Matt
 * @since 01-10-2003
 * 
 */

public class JobEntryCopy implements Cloneable, XMLInterface, GUIPositionInterface, ChangedFlagInterface
{
	private JobEntryInterface entry;

	private int nr; // Copy nr. 0 is the base copy...

	private boolean selected;

	private Point location;

	private boolean parallel;

	private boolean draw;

	private long id;

	public JobEntryCopy()
	{
		clear();
	}

	/**
	 * @deprecated Log is no longer required.
	 * @param log
	 */
	public JobEntryCopy(LogWriter log)
	{
		clear();
	}

	public JobEntryCopy(LogWriter log, JobEntryInterface entry)
	{
		setEntry(entry);
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer();

		retval.append("    <entry>").append(Const.CR);
		retval.append(entry.getXML());

		retval.append("      ").append(XMLHandler.addTagValue("parallel", parallel));
		retval.append("      ").append(XMLHandler.addTagValue("draw", draw));
		retval.append("      ").append(XMLHandler.addTagValue("nr", nr));
		retval.append("      ").append(XMLHandler.addTagValue("xloc", location.x));
		retval.append("      ").append(XMLHandler.addTagValue("yloc", location.y));

		retval.append("      </entry>").append(Const.CR);
		return retval.toString();
	}

	public JobEntryCopy(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			String stype = XMLHandler.getTagValue(entrynode, "type");

			JobPlugin jobPlugin = JobEntryLoader.getInstance().findJobEntriesWithID(stype);
			if (jobPlugin == null)
				throw new KettleStepLoaderException("No valid step/plugin specified (jobPlugin=null) for " + stype);

			// Get an empty JobEntry of the appropriate class...
			entry = JobEntryLoader.getInstance().getJobEntryClass(jobPlugin);
			if (entry != null)
			{
				// System.out.println("New JobEntryInterface built of type:
				// "+entry.getTypeDesc());
				entry.loadXML(entrynode, databases, slaveServers, rep);

				// Handle GUI information: nr & location?
				setNr(Const.toInt(XMLHandler.getTagValue(entrynode, "nr"), 0));
				setParallel("Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "parallel")));
				setDrawn("Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "draw")));
				int x = Const.toInt(XMLHandler.getTagValue(entrynode, "xloc"), 0);
				int y = Const.toInt(XMLHandler.getTagValue(entrynode, "yloc"), 0);
				setLocation(x, y);
			}
		} catch (Exception e)
		{
			String message = "Unable to read Job Entry copy info from XML node : " + e.toString();
			LogWriter log = LogWriter.getInstance();
			log.logError(toString(), message);
			log.logError(toString(), Const.getStackTracker(e));
			throw new KettleXMLException(message, e);
		}
	}

	/**
	 * Load the chef graphical entry from repository We load type, name &
	 * description if no entry can be found.
	 * 
	 * @param log
	 *            the logging channel
	 * @param rep
	 *            the Repository
	 * @param id_job
	 *            The job ID
	 * @param id_jobentry_copy
	 *            The jobentry copy ID
	 * @param jobentries
	 *            A list with all jobentries
	 * @param databases
	 *            A list with all defined databases
	 */
	public JobEntryCopy(LogWriter log, Repository rep, long id_job, long id_jobentry_copy, List<JobEntryInterface> jobentries, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			setID(id_jobentry_copy);

			// Handle GUI information: nr, location, ...
			RowMetaAndData r = rep.getJobEntryCopy(id_jobentry_copy);
			if (r != null)
			{
				// These are the jobentry_copy fields...

				// System.out.println("JobEntryCopy = "+r);

				long id_jobentry = r.getInteger("ID_JOBENTRY", 0);
				long id_jobentry_type = r.getInteger("ID_JOBENTRY_TYPE", 0);
				setNr((int) r.getInteger("NR", 0));
				int locx = (int) r.getInteger("GUI_LOCATION_X", 0);
				int locy = (int) r.getInteger("GUI_LOCATION_Y", 0);
				boolean isdrawn = r.getBoolean("GUI_DRAW", false);
				boolean isparallel = r.getBoolean("PARALLEL", false);

				// Do we have the jobentry already?
				entry = JobMeta.findJobEntry(jobentries, id_jobentry);
				if (entry == null)
				{
					// What type of jobentry do we load now?
					// Get the jobentry type code
					RowMetaAndData rt = rep.getJobEntryType(id_jobentry_type);
					if (rt != null)
					{
						String jet_code = rt.getString("CODE", null);

						JobEntryLoader jobLoader = JobEntryLoader.getInstance();
						JobPlugin jobPlugin = jobLoader.findJobEntriesWithID(jet_code);
						if (jobPlugin != null)
						{
							entry = jobLoader.getJobEntryClass(jobPlugin);

							// Load the attributes for that jobentry
							entry.loadRep(rep, id_jobentry, databases, slaveServers);
							entry.setID(id_jobentry);
							
							jobentries.add(entry);
						} else
						{
							throw new KettleException(
									"JobEntryLoader was unable to find Job Entry Plugin with description ["
											+ jet_code + "].");
						}
					} else
					{
						throw new KettleException("Unable to find Job Entry Type with id=" + id_jobentry_type
								+ " in the repository");
					}
				}

				setLocation(locx, locy);
				setDrawn(isdrawn);
				setParallel(isparallel);
			}
		} catch (KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry copy from repository with id_jobentry_copy="
					+ id_jobentry_copy, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job) throws KettleException
	{
		try
		{
			/*
			 * --1-- Save the JobEntryCopy details... --2-- If we don't find a
			 * id_jobentry, save the jobentry (meaning: only once)
			 */

			// See if an entry with the same name is already available...
			long id_jobentry = rep.getJobEntryID(getName(), id_job);
			if (id_jobentry <= 0)
			{
				entry.saveRep(rep, id_job);
				id_jobentry = entry.getID();
			}

			// OK, the entry is saved.
			// Get the entry type...
			long id_jobentry_type = rep.getJobEntryTypeID(entry.getTypeCode());

			// Oops, not found: update the repository!
			if (id_jobentry_type < 0)
			{
				rep.updateJobEntryTypes();

				// Try again!
				id_jobentry_type = rep.getJobEntryTypeID(entry.getTypeCode());
			}

			// Save the entry copy..
			setID(rep.insertJobEntryCopy(id_job, id_jobentry, id_jobentry_type, getNr(), getLocation().x,
					getLocation().y, isDrawn(), isParallel()));
		} catch (KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry copy to the repository, id_job=" + id_job,
					dbe);
		}
	}

	public void clear()
	{
		location = null;
		entry = null;
		nr = 0;
		parallel = false;
		setID(-1L);
	}

	public Object clone()
	{
		JobEntryCopy ge = new JobEntryCopy();
		ge.replaceMeta(this);
		ge.setID(-1L);
		return ge;
	}

	public void replaceMeta(JobEntryCopy jobEntryCopy)
	{
		entry = jobEntryCopy.entry;
		nr = jobEntryCopy.nr; // Copy nr. 0 is the base copy...

		selected = jobEntryCopy.selected;
		if (jobEntryCopy.location != null)
			location = new Point(jobEntryCopy.location.x, jobEntryCopy.location.y);
		parallel = jobEntryCopy.parallel;
		draw = jobEntryCopy.draw;

		id = jobEntryCopy.id;
	}

	public Object clone_deep()
	{
		JobEntryCopy ge = (JobEntryCopy) clone();

		// Copy underlying object as well...
		ge.entry = (JobEntryInterface) entry.clone();

		return ge;
	}

	public void setID(long id)
	{
		this.id = id;
	}

	public boolean equals(Object o)
	{
		if (o == null) return false;
		JobEntryCopy je = (JobEntryCopy) o;
		return je.entry.getName().equalsIgnoreCase(entry.getName()) && je.getNr() == getNr();
	}

	public long getID()
	{
		return id;
	}

	public void setEntry(JobEntryInterface je)
	{
		entry = je;
		if (entry!=null)
		{
			if (entry.getConfigId()==null)
		    {
				entry.setConfigId( JobEntryLoader.getInstance().getJobEntryID(entry) );
		    }
		}
	}

	public JobEntryInterface getEntry()
	{
		return entry;
	}

	public JobEntryType getJobEntryType()
	{
		return entry.getJobEntryType();
	}

	/**
	 * @return entry in JobEntryInterface.typeCode[] for native jobs,
	 *         entry.getTypeCode() for plugins
	 */
	public String getTypeDesc()
	{
		if (getJobEntryType() == JobEntryType.NONE)
			return entry.getTypeCode();
		return getTypeDesc(entry);
	}

	public void setLocation(int x, int y)
	{
		int nx = (x >= 0 ? x : 0);
		int ny = (y >= 0 ? y : 0);

		Point loc = new Point(nx, ny);
		if (!loc.equals(location))
			setChanged();
		location = loc;
	}

	public void setLocation(Point loc)
	{
		if (loc != null && !loc.equals(location))
			setChanged();
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
		entry.setChanged(ch);
	}

	public boolean hasChanged()
	{
		return entry.hasChanged();
	}

	public int getNr()
	{
		return nr;
	}

	public void setNr(int n)
	{
		nr = n;
	}

	public void setParallel()
	{
		setParallel(true);
	}

	public void setParallel(boolean p)
	{
		parallel = p;
	}

	public boolean isDrawn()
	{
		return draw;
	}

	public void setDrawn()
	{
		setDrawn(true);
	}

	public void setDrawn(boolean d)
	{
		draw = d;
	}

	public boolean isParallel()
	{
		return parallel;
	}

	public static final JobEntryType getType(String dsc)
	{
		if (dsc != null)
		{
			JobEntryType[] types = JobEntryType.values();

			for (JobEntryType type : types)
			{
				if (type.getTypeCode().equalsIgnoreCase(dsc))
					return type;
			}
			// Try the long description!
			for (JobEntryType type : types)
			{
				if (type.getDescription().equalsIgnoreCase(dsc))
					return type;
			}
		}

		return JobEntryType.NONE;
	}

	public static final String getTypeDesc(int ty)
	{
		if (ty > 0 && ty < JobEntryType.values().length)
			return JobEntryType.values()[ty].toString();

		return JobEntryType.NONE.toString();
	}

	public static final String getTypeDesc(JobEntryInterface ty)
	{
		return ty.getJobEntryType().toString();
	}

	public void setSelected(boolean sel)
	{
		selected = sel;
	}

	public void flipSelected()
	{
		selected = !selected;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setDescription(String description)
	{
		entry.setDescription(description);
	}

	public String getDescription()
	{
		return entry.getDescription();
	}

	public boolean isStart()
	{
		return entry.isStart();
	}

	public boolean isDummy()
	{
		return entry.isDummy();
	}

	public boolean isTransformation()
	{
		return getJobEntryType() == JobEntryType.TRANS;
	}

	public boolean isJob()
	{
		return getJobEntryType() == JobEntryType.JOB;
	}

	public boolean evaluates()
	{
		if (entry != null)
			return entry.evaluates();
		return false;
	}

	public boolean isUnconditional()
	{
		if (entry != null)
			return entry.isUnconditional();
		return true;
	}

	public boolean isEvaluation()
	{
		return getJobEntryType() == JobEntryType.EVAL;
	}

	public boolean isMail()
	{
		return getJobEntryType() == JobEntryType.MAIL;
	}

	public boolean isSQL()
	{
		return getJobEntryType() == JobEntryType.MAIL;
	}

	public boolean isSpecial()
	{
		return getJobEntryType() == JobEntryType.SPECIAL;
	}

	public String toString()
	{
		if( entry != null ) 
		{
			return entry.getName() + "." + getNr();
		} else {
			return "null."+getNr();
		}
	}

	public String getName()
	{
		if( entry != null ) 
		{
			return entry.getName();
		} else {
			return "null";
		}
	}

	public void setName(String name)
	{
		entry.setName(name);
	}

	public boolean resetErrorsBeforeExecution()
	{
		return entry.resetErrorsBeforeExecution();
	}
}
