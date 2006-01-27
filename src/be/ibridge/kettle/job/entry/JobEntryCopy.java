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
 
package be.ibridge.kettle.job.entry;
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;


/**
 * This class describes the fact that a single JobEntry can be used multiple times in the same Job.
 * Therefor it contains a link to a JobEntry, a position, a number, etc.
 * 
 * @author Matt
 * @since 01-10-2003
 *
 */

public class JobEntryCopy implements Cloneable, XMLInterface 
{
	private JobEntryInterface entry;
	private int     nr;          // Copy nr. 0 is the base copy...

	private boolean selected;
	private Point   location;
	private boolean parallel;
	private boolean draw;
	
	private long id;
		
	public JobEntryCopy(LogWriter log)
	{
		clear();
	}
	
	public JobEntryCopy(LogWriter log, JobEntryInterface entry)
	{
		this.entry = entry;
	}

	public String getXML()
	{
		String retval="";
		
		retval+="    <entry>"+Const.CR;
		
		retval+=entry.getXML();
		
		retval+="      "+XMLHandler.addTagValue("parallel", parallel);
		retval+="      "+XMLHandler.addTagValue("draw",     draw);
		retval+="      "+XMLHandler.addTagValue("nr",       nr);
		retval+="      "+XMLHandler.addTagValue("xloc",     location.x);
		retval+="      "+XMLHandler.addTagValue("yloc",     location.y);
		
		retval+="      </entry>"+Const.CR;
		
		return retval;
	}
	
	public JobEntryCopy(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			String stype = XMLHandler.getTagValue(entrynode, "type");
			int type = JobEntryCopy.getType(stype);
			
			// Get an empty JobEntry of the appropriate class...
			entry = JobEntryBase.newJobEntryInterface(type);		
			if (entry!=null)
			{
				// System.out.println("New JobEntryInterface built of type: "+entry.getTypeDesc());
				entry.loadXML(entrynode, databases);
				
				// Handle GUI information: nr & location?
				setNr( Const.toInt( XMLHandler.getTagValue(entrynode, "nr"), 0) );
				setParallel("Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "parallel") ) );
				setDrawn(   "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "draw") ) );
				int x=Const.toInt(XMLHandler.getTagValue(entrynode, "xloc"), 0);
				int y=Const.toInt(XMLHandler.getTagValue(entrynode, "yloc"), 0);
				setLocation(x, y);
				
				// System.out.println("name=["+getName()+"], drawn="+isDrawn()+", location="+getLocation());
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read Job Entry copy info from XML node", e);
		}
	}
	

	/**
	 * Load the chef graphical entry from repository
	 * We load type, name & description if no entry can be found.
	 * @param log the logging channel
	 * @param rep the Repository
	 * @param id_job The job ID
	 * @param id_jobentry_copy The jobentry copy ID
	 * @param jobentries A list with all jobentries
	 * @param databases A list with all defined databases
	 */
	public JobEntryCopy(LogWriter log, Repository rep, long id_job, long id_jobentry_copy, ArrayList jobentries, ArrayList databases) throws KettleException
	{
		try
		{
			setID(id_jobentry_copy);
					
			// Handle GUI information: nr, location, ...
			Row r = rep.getJobEntryCopy(id_jobentry_copy);
			if (r!=null)
			{
				// These are the jobentry_copy fields...
				
				//System.out.println("JobEntryCopy = "+r);
				
				long id_jobentry      = r.searchValue("ID_JOBENTRY").getInteger();
				long id_jobentry_type = r.searchValue("ID_JOBENTRY_TYPE").getInteger();
				setNr( (int)r.searchValue("NR").getInteger() );
				int locx = (int)r.searchValue("GUI_LOCATION_X").getInteger();
				int locy = (int)r.searchValue("GUI_LOCATION_Y").getInteger();
				boolean isdrawn    = r.searchValue("GUI_DRAW").getBoolean();
				boolean isparallel = r.searchValue("PARALLEL").getBoolean();
	
				// Do we have the jobentry already?
				entry = Const.findJobEntry(jobentries, id_jobentry);
				if (entry==null)
				{
					// What type of jobentry do we load now?
					// Get the jobentry type code
					Row rt = rep.getJobEntryType(id_jobentry_type);
					if (rt!=null)
					{
						String jet_code = rt.searchValue("CODE").getString();
						int jet_type = JobEntryBase.getType(jet_code);
						
						// Instantiate a new copy of that type of JobEntry
						entry = JobEntryBase.newJobEntryInterface(jet_type);
						
						// Load the attributes for that jobentry
						entry.loadRep(rep, id_jobentry, databases);
						jobentries.add(entry);
					}
					else
					{
					    throw new KettleException("Unable to find Job Entry Type with id="+id_jobentry_type+" in the repository");
					}
				}
	
				setLocation(locx, locy);
				setDrawn(isdrawn);
				setParallel(isparallel);
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry copy from repository with id_jobentry_copy="+id_jobentry_copy, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			/*
			 *   --1-- Save the JobEntryCopy details...
			 *   --2-- If we don't find a id_jobentry, save the jobentry (meaning: only once) 
			 */
			
			// See if an entry with the same name is already available...
			long id_jobentry = rep.getJobEntryID(getName(), id_job);
			if (id_jobentry<=0)
			{
				entry.saveRep(rep, id_job );
				id_jobentry = entry.getID();
			}
			
			// OK, the entry is saved.
			// Get the entry type...
			long id_jobentry_type = rep.getJobEntryTypeID( entry.getTypeDesc() );
			
			// Oops, not found: update the repository!
			if (id_jobentry_type<0)
			{
			    rep.updateJobEntryTypes();
			    
			    // Try again!
			    id_jobentry_type = rep.getJobEntryTypeID( entry.getTypeDesc() );
			}
			
			// Save the entry copy..
			setID( rep.insertJobEntryCopy(id_job, 
										  id_jobentry, 
										  id_jobentry_type, 
										  getNr(), 
										  getLocation().x, 
										  getLocation().y, 
										  isDrawn(), 
										  isParallel() 
								   	     ));
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry copy to the repository, id_job="+id_job, dbe);
		}
	}

	public void clear()
	{
		location=null;
		entry=null;
		nr=0;
		parallel=false;
		setID(-1L);
	}
	
	public Object clone()
	{
		JobEntryCopy ge=null;
		try
		{
			ge = (JobEntryCopy)super.clone();
		}
		catch(CloneNotSupportedException cnse) { }
		
		return ge;
	}

	public Object clone_deep()
	{
		JobEntryCopy ge=null;
		try
		{
			ge = (JobEntryCopy)super.clone();

			// Copy underlying object as well...
			ge.entry = (JobEntryInterface)entry.clone();
		}
		catch(CloneNotSupportedException cnse) { }
		
		return ge;
	}
	

	public void setID(long id)
	{
		this.id=id;
	}
	
	public boolean equals(Object o)
	{
		JobEntryCopy je = (JobEntryCopy)o;
		return je.entry.getName().equalsIgnoreCase(entry.getName()) && je.getNr() == getNr();
	}
	

	public long getID()
	{
		return id;
	}

	public void setEntry(JobEntryInterface je)
	{
		entry = je;
	}

	public JobEntryInterface getEntry()
	{
		return entry;
	}
	
	public int getType()
	{
		return entry.getType();
	}
	
	public String getTypeDesc()
	{
		return getTypeDesc( entry.getType() );
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
		nr=n;
	}

	public void setParallel()
	{
		setParallel(true);
	}
	
	public void setParallel(boolean p)
	{
		parallel=p;
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
		draw=d;
	}
	
	public boolean isParallel()
	{
		return parallel;
	}
		
	public static final int getType(String dsc)
	{
		if (dsc!=null) 
		for (int i=0;i<JobEntryInterface.type_desc.length;i++)
		{
			if (JobEntryInterface.type_desc[i].equalsIgnoreCase(dsc)) return i; 
		}
		// Try the long description!
		for (int i=0;i<JobEntryInterface.type_desc_long.length;i++)
		{
			if (JobEntryInterface.type_desc_long[i].equalsIgnoreCase(dsc)) return i; 
		}
		
		return JobEntryInterface.TYPE_JOBENTRY_NONE;
	}
	
	public static final String getTypeDesc(int ty)
	{
		if (ty>0 && ty<JobEntryInterface.type_desc.length) return JobEntryInterface.type_desc[ty];
		
		return JobEntryInterface.type_desc[0]; 
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
		return getType()==JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION;
	}
	
	public boolean isJob()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_JOB;
	}
	
	public boolean evaluates()
	{
		if (entry!=null) return entry.evaluates();
		return false;
	}
	
	public boolean isUnconditional()
	{
		if (entry!=null) return entry.isUnconditional();
		return true;
	}

	
	public boolean isEvaluation()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_EVALUATION;
	}

	public boolean isMail()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_MAIL;
	}

	public boolean isSQL()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_MAIL;
	}

	public boolean isSpecial()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_SPECIAL;
	}
	
	public String toString()
	{
		return entry.getName()+"."+getNr();
	}
	
	public String getName()
	{
		return entry.getName();
	}
	
	public void setName(String name)
	{
		entry.setName(name);
	}
			
}
