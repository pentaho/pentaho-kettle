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

import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.job.entry.eval.JobEntryEval;
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;


/**
 * Defines a Job and provides methods to load, save, verify, etc.
 * 
 * @author Matt
 * @since 11-08-2003
 *
 */

public class JobMeta implements Cloneable, XMLInterface
{
	public LogWriter log;
	
	private long id;
	
	private String     name;
	private String     filename;

	public  ArrayList  jobentries;
	public  ArrayList  jobcopies;
	public  ArrayList  jobhops;
	public  ArrayList  notes;
	public  ArrayList  databases;

	private  RepositoryDirectory directory;
	
	public  String     arguments[];
	
	private  boolean        changed, changed_entries, changed_hops, changed_notes;
	private  DatabaseMeta   logconnection;
	public   String         logtable;
	// public   Props          props;

	public  DBCache dbcache;

	private ArrayList undo;
	private int max_undo;
	private int undo_position;

	public static final int TYPE_UNDO_CHANGE   = 1;
	public static final int TYPE_UNDO_NEW      = 2;
	public static final int TYPE_UNDO_DELETE   = 3;
	public static final int TYPE_UNDO_POSITION = 4;

	public  static final String STRING_SPECIAL_START = "START";
	public  static final String STRING_SPECIAL_DUMMY = "DUMMY";

	// Remember the size and position of the different windows...
	public  boolean    max[]  = new boolean[1];
	public  Rectangle  size[] = new Rectangle[1];
	
	public  String  created_user, modified_user;
	public  Value   created_date, modified_date;

	public JobMeta(LogWriter l)
	{
		log=l;
		clear();
	}
    
	public long getID()
	{
		return id;
	}
	
	public void setID(long id)
	{
		this.id = id;
	}

	public void clear()
	{
		name = null;
		jobcopies  = new ArrayList();
		jobentries = new ArrayList();
		jobhops    = new ArrayList();
		notes      = new ArrayList();
		databases  = new ArrayList();
		logconnection = null;
		logtable = null;
		arguments = null;

		max_undo = Const.MAX_UNDO;

		dbcache = DBCache.getInstance();

		undo = new ArrayList();
		undo_position=-1;
		
		addDefaults();
		setChanged(false);

		modified_user = "-";
		modified_date = null;
		
		directory = new RepositoryDirectory();
	}
	
	public void addDefaults()
	{
		addStart(); // Add starting point!
		addDummy(); // Add dummy!
		addOK();  // errors == 0 evaluation
		addError(); // errors != 0 evaluation
				
		clearChanged();
	}
	
	private void addStart()
	{
		JobEntrySpecial je = new JobEntrySpecial(STRING_SPECIAL_START, true, false);
		JobEntryCopy jge = new JobEntryCopy(log);
		jge.setID(-1L);
		jge.setEntry(je);
		jge.setLocation(50,50);
		jge.setDrawn(false);
		jge.setDescription("A job starts to process here.");
		addJobEntry(jge);

	}
	
	private void addDummy()
	{
		JobEntrySpecial dummy = new JobEntrySpecial(STRING_SPECIAL_DUMMY, false, true);
		JobEntryCopy dummyge = new JobEntryCopy(log);
		dummyge.setID(-1L);
		dummyge.setEntry(dummy);
		dummyge.setLocation(50,50);
		dummyge.setDrawn(false);
		dummyge.setDescription("A dummy entry.");
		addJobEntry(dummyge);
	}
	
	public void addOK()
	{
		JobEntryEval ok = new JobEntryEval("OK", "errors == 0");
		JobEntryCopy jgok = new JobEntryCopy(log);
		jgok.setEntry(ok);
		jgok.setLocation(0,0);
		jgok.setDrawn(false);
		jgok.setDescription("This comparisson is true when no errors have occured.");
		addJobEntry(jgok);
	}
	
	public void addError()
	{
		JobEntryEval err = new JobEntryEval("ERROR", "errors != 0");
		JobEntryCopy jgerr = new JobEntryCopy(log);
		jgerr.setEntry(err);
		jgerr.setLocation(0,0);
		jgerr.setDrawn(false);
		jgerr.setDescription("This comparisson is true when one or more errors have occured.");
		addJobEntry(jgerr);
	}


	public JobEntryCopy getStart()
	{
		for (int i=0;i<nrJobEntries();i++) 
		{
			JobEntryCopy cge = getJobEntry(i); 
			if (cge.isStart()) return cge;
		}
		return null;
	}

	public JobEntryCopy getDummy()
	{
		for (int i=0;i<nrJobEntries();i++) 
		{
			JobEntryCopy cge = getJobEntry(i); 
			if (cge.isDummy()) return cge;
		}
		return null;
	}


	public boolean equals(Object obj)
	{
		return name.equalsIgnoreCase(((JobMeta)obj).name);
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}
	
	/**
     * @return Returns the directory.
     */
    public RepositoryDirectory getDirectory()
    {
        return directory;
    }
    
    /**
     * @param directory The directory to set.
     */
    public void setDirectory(RepositoryDirectory directory)
    {
        this.directory = directory;
    }

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename=filename;
	}

	public DatabaseMeta getLogConnection()
	{
		return logconnection;
	}

	public void setLogConnection(DatabaseMeta ci)
	{
		logconnection=ci;
	}

	/**
	 * @return Returns the databases.
	 */
	public ArrayList getDatabases()
	{
		return databases;
	}
	
	/**
	 * @param databases The databases to set.
	 */
	public void setDatabases(ArrayList databases)
	{
		this.databases = databases;
	}
	
	public void setChanged()
	{
		setChanged(true);
	}

	public void setChanged(boolean ch)
	{
		changed=ch;
	}
	
	public void clearChanged()
	{
        changed_entries = false;
        changed_hops    = false;
        changed_notes   = false;
        
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy entry = getJobEntry(i);
			entry.setChanged(false);
		}
		for (int i=0;i<nrJobHops();i++)
		{
			JobHopMeta hop = getJobHop(i);
			hop.setChanged(false);
		}
		changed=false;
	}

	public boolean hasChanged()
	{
		if (changed || changed_notes || changed_entries || changed_hops) return true;
		
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy entry = getJobEntry(i);
			if (entry.hasChanged()) return true;
		}
		for (int i=0;i<nrJobHops();i++)
		{
			JobHopMeta hop = getJobHop(i);
			if (hop.hasChanged()) return true;
		}
		return false;
	}
	
 	private void saveRepJob(Repository rep)
 		throws KettleException
	{
 		try
		{
			// The ID has to be assigned, even when it's a new item...
			rep.insertJob(
			    getID(),
				directory.getID(),
				getName(),
				logconnection==null?-1:logconnection.getID(),
				logtable,
				modified_user,
				modified_date
			);
		}
 		catch(KettleDatabaseException dbe)
		{
 			throw new KettleException("Unable to save job info to repository", dbe);
		}
	}

	public boolean showReplaceWarning(Repository rep)
	{
		if (getID()<0)
		{
			try
			{
				if ( rep.getJobID( getName(), directory.getID() )>0 ) return true;
			}
			catch(KettleDatabaseException dbe)
			{
				return true;
			}
		}
		return false;
	}

	public String getXML()
	{
		DatabaseMeta ci = getLogConnection(); 
		String retval=new String();
		
		retval+="<job>"+Const.CR;
		retval+="  "+XMLHandler.addTagValue("name", getName());
		retval+="  "+XMLHandler.addTagValue("directory", directory.getPath());
		
		for (int i=0;i<nrDatabases();i++)
		{
			DatabaseMeta dbinfo = getDatabase(i);
			retval+=dbinfo.getXML();
		}

		retval+="  "+XMLHandler.addTagValue("logconnection", ci==null?"":ci.getName());
		retval+="  "+XMLHandler.addTagValue("logtable", logtable);

		retval+="  <entries>"+Const.CR;
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy jge = getJobEntry(i);
			retval+=jge.getXML();
		}
		retval+="    </entries>"+Const.CR;

		retval+="  <hops>"+Const.CR;
		for (int i=0;i<nrJobHops();i++)
		{
			JobHopMeta hi = getJobHop(i);
			retval+=hi.getXML();
		}
		retval+="    </hops>"+Const.CR;

		retval+="  <notepads>"+Const.CR;
		for (int i=0;i<nrNotes();i++)
		{
			NotePadMeta ni= getNote(i);
			retval+=ni.getXML();
		}
		retval+="    </notepads>"+Const.CR;


		retval+="  </job>"+Const.CR;
		
		return retval;
	}

	/**
	 * Load the job from the XML file specified.
	 * @param log the logging channel
	 * @param fname The filename to load as a job
	 * @throws KettleXMLException
	 */
	public JobMeta(LogWriter log, String fname)
		throws KettleXMLException
	{
		this.log = log;
		try
		{
			Document doc = XMLHandler.loadXMLFile(fname);
			if (doc!=null)
			{
				// Clear the job
				clear();
				setFilename(fname);
			
				// The jobnode
				Node jobnode = XMLHandler.getSubNode(doc, "job");
				
				loadXML(jobnode);
			}
			else
			{
				throw new KettleXMLException("Error reading/validating information from XML file: "+fname);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load the job from XML file ["+fname+"]", e);
		}
	}
	
	public JobMeta(LogWriter log, Node jobnode)
		throws KettleXMLException
	{
		this.log = log;
		
		loadXML(jobnode);
	}

	public void loadXML(Node jobnode)
		throws KettleXMLException
	{
		try
		{			
			//
			// get job info:
			//
			name = XMLHandler.getTagValue(jobnode, "name");

			// 
			// Read the database connections
			//
			int nr = XMLHandler.countNodes(jobnode, "connection");
			for (int i=0;i<nr;i++)
			{
				Node dbnode = XMLHandler.getSubNodeByNr(jobnode, "connection", i);
				DatabaseMeta dbinf = new DatabaseMeta(dbnode);
				addDatabase(dbinf);
			}
			
			/*
			 * Get the log database connection & log table
			 */
			String logcon        = XMLHandler.getTagValue(jobnode, "logconnection");
			logconnection        = findDatabase(logcon);
			logtable             = XMLHandler.getTagValue(jobnode, "logtable");
			
			/*
			 * read the job entries...
			 */
			Node entriesnode = XMLHandler.getSubNode(jobnode, "entries");
			int tr = XMLHandler.countNodes(entriesnode, "entry");
			for (int i=0;i<tr;i++) 
			{
				Node entrynode = XMLHandler.getSubNodeByNr(entriesnode, "entry", i);
				//System.out.println("Reading entry:\n"+entrynode);
				
				JobEntryCopy je = new JobEntryCopy(entrynode, databases, null);
				JobEntryCopy prev = findJobEntry(je.getName(), 0);
				if (prev!=null)
				{
					if (je.getNr()==0) // See if the #0 already exists!
					{
						// Replace previous version with this one: remove it first
						int idx = indexOfJobEntry(prev);
						removeJobEntry(idx);
					}
					else
					if (je.getNr()>0) // Use previously defined JobEntry info!
					{
						je.setEntry(prev.getEntry());
						
						// See if entry.5 already exists...
						prev = findJobEntry(je.getName(), je.getNr());
						if (prev!=null) // remove the old one!
						{
							int idx = indexOfJobEntry(prev);
							removeJobEntry(idx);
						}
					}
				}
				// Add the JobEntryCopy...
				addJobEntry(je);
			}

			Node hopsnode = XMLHandler.getSubNode(jobnode, "hops");
			int ho = XMLHandler.countNodes(hopsnode, "hop");
			for (int i=0;i<ho;i++) 
			{
				Node hopnode = XMLHandler.getSubNodeByNr(hopsnode, "hop", i);
				JobHopMeta hi = new JobHopMeta(hopnode, this);
				jobhops.add(hi);
			}

			// Read the notes...
			Node notepadsnode = XMLHandler.getSubNode(jobnode, "notepads");
			int nrnotes = XMLHandler.countNodes(notepadsnode, "notepad");
			for (int i=0;i<nrnotes;i++)
			{
				Node notepadnode = XMLHandler.getSubNodeByNr(notepadsnode, "notepad", i); 
				NotePadMeta ni = new NotePadMeta(notepadnode);
				notes.add(ni);
			}

			// Do we have the special entries?
			if (findJobEntry(STRING_SPECIAL_START, 0)==null) addStart();
			if (findJobEntry(STRING_SPECIAL_DUMMY, 0)==null) addDummy();
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load job info from XML node", e);
		}
	}

	/**
	 * Read the database connections in the repository and add them to this job 
	 * if they are not yet present.
	 * 
	 * @param rep The repository to load the database connections from.
	 */
	public void readDatabases(Repository rep)
	{
		try
		{
			long dbids[] = rep.getDatabaseIDs();
			for (int i=0;i<dbids.length;i++)
			{
				DatabaseMeta ci = new DatabaseMeta(rep, dbids[i]);
				if (indexOfDatabase(ci)<0) 
				{
					addDatabase(ci);
					ci.setChanged(false);
				} 
			}
		}
		catch(KettleDatabaseException dbe)
		{
			log.logError(toString(), "Error reading databases from repository:"+Const.CR+dbe.getMessage());
		}
		catch(KettleException ke)
		{
			log.logError(toString(), "Error reading databases from repository:"+Const.CR+ke.getMessage());
		}

		setChanged(false);
	}

	/**
	 * Find a database connection by it's name
	 * @param name The database name to look for
	 * @return The database connection or null if nothing was found.
	 */
	public DatabaseMeta findDatabase(String name)
	{
		for (int i=0;i<nrDatabases();i++)
		{
			DatabaseMeta ci = getDatabase(i); 
			if (ci.getName().equalsIgnoreCase(name))
			{
				return ci; 
			}
		}
		return null;
	}

	public void saveRep(Repository rep)
		throws KettleException
	{
		saveRep(rep, null);
	}

	public void saveRep(Repository rep, IProgressMonitor monitor)
		throws KettleException
	{
		try
		{
			int nrWorks = 2+nrDatabases()+nrNotes()+nrJobEntries()+nrJobHops();
			if (monitor!=null) monitor.beginTask("Saving transformation "+directory+Const.FILE_SEPARATOR+getName(), nrWorks);
			
			// Before we start, make sure we have a valid job ID!
			// Two possibilities: 
			// 1) We have a ID: keep it
			// 2) We don't have an ID: look it up.
			//    If we find a transformation with the same name: ask! 
			//
			if (monitor!=null) monitor.subTask("Handling previous version of job...");
			setID( rep.getJobID(getName(), directory.getID()) );
			
			// If no valid id is available in the database, assign one...
			if (getID()<=0)
			{
				setID( rep.getNextJobID() );
			}
			else
			{
				// If we have a valid ID, we need to make sure everything is cleared out 
				// of the database for this id_job, before we put it back in...
				rep.delAllFromJob(getID());
			}
			if (monitor!=null) monitor.worked(1);

			
			
			// Now, save the job entry in R_JOB
			// Note, we save this first so that we have an ID in the database.
			// Everything else depends on this ID, including recursive job entries to the save job. (retry)
			if (monitor!=null) monitor.subTask("Saving job details...");
			log.logDetailed(toString(), "Saving job info to repository...");
			saveRepJob(rep);
			if (monitor!=null) monitor.worked(1);

			
			
			//
			// Save the notes
			//
			log.logDetailed(toString(), "Saving notes to repository...");
			for (int i=0;i<nrNotes();i++)
			{
				if (monitor!=null) monitor.subTask("Saving note #"+(i+1)+"/"+nrNotes());
				NotePadMeta ni = getNote(i);
				ni.saveRep(rep, getID());
				if (ni.getID()>0) 
				{
					rep.insertJobNote(getID(), ni.getID());
				}
				if (monitor!=null) monitor.worked(1);
			}
			
			//
			// Save the job entries
			//
			log.logDetailed(toString(), "Saving "+nrJobEntries()+" ChefGraphEntries to repository...");
			for (int i=0;i<nrJobEntries();i++)
			{
				if (monitor!=null) monitor.subTask("Saving job entry #"+(i+1)+"/"+nrJobEntries());
				JobEntryCopy cge = getJobEntry(i);
				cge.saveRep(rep, getID());
				if (monitor!=null) monitor.worked(1);
			}
			
			log.logDetailed(toString(), "Saving job hops to repository...");
			for (int i=0;i<nrJobHops();i++)
			{
				if (monitor!=null) monitor.subTask("Saving job hop #"+(i+1)+"/"+nrJobHops());
				JobHopMeta hi = getJobHop(i);
				hi.saveRep(rep, getID());
				if (monitor!=null) monitor.worked(1);
			}
						
			// Commit this transaction!!
			rep.commit();
			
			clearChanged();
			if (monitor!=null) monitor.done();
		}
		catch(KettleDatabaseException dbe)
		{
			rep.rollback();
			throw new KettleException("Unable to save Job in repository, database rollback performed.", dbe);
		}
	}

	/**
	 * Load a job in a directory
	 * @param log the logging channel
	 * @param rep The Repository
	 * @param jobname The name of the job
	 * @param repdir The directory in which the job resides.
	 * @throws KettleException
	 */
	public JobMeta(LogWriter log, Repository rep, String jobname, RepositoryDirectory repdir)
		throws KettleException
	{
		this(log, rep, jobname, repdir, null);
	}

	/**
	 * Load a job in a directory
	 * @param log the logging channel
	 * @param rep The Repository
	 * @param jobname The name of the job
	 * @param repdir The directory in which the job resides.
	 * @throws KettleException
	 */
	public JobMeta(LogWriter log, Repository rep, String jobname, RepositoryDirectory repdir, IProgressMonitor monitor)
		throws KettleException
	{
		this.log = log;
		
		try
		{
			// Clear everything...
			clear();
			
			directory = repdir;
			
			// Get the transformation id
			setID( rep.getJobID(jobname, repdir.getID()) );
		
			// If no valid id is available in the database, then give error...
			if (getID()>0)
			{
				// Load the notes...
				long noteids[] = rep.getJobNoteIDs(getID());
				long jecids[] = rep.getJobEntryCopyIDs(getID());
				long hopid[] = rep.getJobHopIDs(getID());

				int nrWork = 2+noteids.length+jecids.length+hopid.length;
				if (monitor!=null) monitor.beginTask("Loading job "+repdir+Const.FILE_SEPARATOR+jobname, nrWork);

				// 
				// Load the common database connections
				//
				if (monitor!=null) monitor.subTask("Reading the available database from the repository");
				readDatabases(rep);
				if (monitor!=null) monitor.worked(1);
				
				//
				// get job info:
				//
				if (monitor!=null) monitor.subTask("Reading the job information");
				Row jobrow = rep.getJob(getID());
				
				name                 = jobrow.searchValue("NAME").getString();
				logtable             = jobrow.searchValue("TABLE_NAME_LOG").getString();
				
				long id_logdb        = jobrow.searchValue("ID_DATABASE_LOG").getInteger();
				if (id_logdb>0)
				{
					// Get the logconnection
					logconnection = new DatabaseMeta(rep, id_logdb);
				}
				if (monitor!=null) monitor.worked(1);
	
				log.logDetailed(toString(), "Loading "+noteids.length+" notes");
				for (int i=0;i<noteids.length;i++)
				{
					if (monitor!=null) monitor.subTask("Reading note #"+(i+1)+"/"+noteids.length);
						NotePadMeta ni = new NotePadMeta(log, rep, noteids[i]);
					if (indexOfNote(ni)<0) addNote(ni);
					if (monitor!=null) monitor.worked(1);
				}
				
				// Load the job entries...
				log.logDetailed(toString(), "Loading "+jecids.length+" job entries");
				for (int i=0;i<jecids.length;i++)
				{
					if (monitor!=null) monitor.subTask("Reading job entry #"+(i+1)+"/"+(jecids.length));
					
					JobEntryCopy jec = new JobEntryCopy(log, rep, getID(), jecids[i], jobentries, databases);
					int idx = indexOfJobEntry(jec);
					if (idx < 0) 
					{
						if (jec.getName()!=null && jec.getName().length()>0) addJobEntry(jec);
					}
					else
					{
						setJobEntry(idx, jec); // replace it!
					}
					if (monitor!=null) monitor.worked(1);
				}

				// Load the hops...
				log.logDetailed(toString(), "Loading "+hopid.length+" job hops");
				for (int i=0;i<hopid.length;i++) 
				{
					if (monitor!=null) monitor.subTask("Reading job hop #"+(i+1)+"/"+(jecids.length));
					JobHopMeta hi = new JobHopMeta(rep, hopid[i], this, jobcopies);
					jobhops.add(hi);
					if (monitor!=null) monitor.worked(1);
				}
				
				// Finally, clear the changed flags...
				clearChanged();
				if (monitor!=null) monitor.subTask("Finishing load");
				if (monitor!=null) monitor.done();
			}
			else
			{
				throw new KettleException("Can't find job : "+jobname);
			}
		}
		catch(KettleException dbe)
		{
			throw new KettleException("An error occurred reading job ["+jobname+"] from the repository", dbe);
		}
	}

	public JobEntryCopy getChefGraphEntry(int x, int y, int iconsize)
	{
		int i, s;
		s = nrJobEntries();
		for (i=s-1;i>=0;i--)  // Back to front because drawing goes from start to end
		{
			JobEntryCopy je = getJobEntry(i);
			Point p = je.getLocation();
			if (p!=null)
			{
				if (   x >= p.x && x <= p.x+iconsize
					&& y >= p.y && y <= p.y+iconsize           
				   )
				{
					return je;
				}
			}
		}
		return null;
	}
	
	public int nrJobEntries() { return jobcopies.size();  }
	public int nrJobHops()    { return jobhops.size();    }
	public int nrNotes()      { return notes.size();      }
	public int nrDatabases()  { return databases.size();  }

	public JobHopMeta getJobHop(int i) { return (JobHopMeta)jobhops.get(i); }
	public JobEntryCopy getJobEntry(int i) { return (JobEntryCopy)jobcopies.get(i); }
	public NotePadMeta getNote(int i) { return (NotePadMeta)notes.get(i); }
	public DatabaseMeta getDatabase(int i) { return (DatabaseMeta)databases.get(i); }
	
	public void addJobEntry(JobEntryCopy je) 
	{ 
		jobcopies.add(je); 
		setChanged(); 
	}
	public void addJobHop(JobHopMeta hi) 
	{ 
		jobhops.add(hi); 
		setChanged(); 
	}
	public void addNote(NotePadMeta ni) 
	{ 
		notes.add(ni); 
		setChanged(); 
	}
	public void addDatabase(DatabaseMeta ci)
	{
		databases.add(ci);
		setChanged();
	}

	public void addJobEntry(int p, JobEntryCopy si)
	{
		jobcopies.add(p, si);
		changed_entries = true;
	}
	public void addJobHop(int p, JobHopMeta hi)
	{
		jobhops.add(p, hi);
		changed_hops = true;
	}
	public void addNote(int p, NotePadMeta ni)
	{
		notes.add(p, ni);
		changed_notes = true;
	}
	public void addDatabase(int p, DatabaseMeta ci)
	{
		databases.add(p, ci);
		setChanged();
	}

	public void removeJobEntry(int i) { jobcopies.remove(i); setChanged(); }
	public void removeJobHop(int i) { jobhops.remove(i); setChanged(); }
	public void removeNote(int i) { notes.remove(i); setChanged(); }
	public void removeDatabase(int i)
	{
		if (i<0 || i>=databases.size()) return;
		databases.remove(i);
		setChanged();
	}

	public int indexOfJobHop(JobHopMeta he)     { return jobhops.indexOf(he); }
	public int indexOfNote(NotePadMeta ni)      { return notes.indexOf(ni); }
	public int indexOfJobEntry(JobEntryCopy ge) { return jobcopies.indexOf(ge); }
	public int indexOfDatabase(DatabaseMeta di) { return databases.indexOf(di); }

	public void setJobEntry(int idx, JobEntryCopy jec)
	{
		jobcopies.set(idx, jec);
	}

	/**
	 * Find an existing JobEntryCopy by it's name and number
	 * @param name The name of the job entry copy
	 * @param nr The number of the job entry copy
	 * @return The JobEntryCopy or null if nothing was found!
	 */
	public JobEntryCopy findJobEntry(String name, int nr)
	{
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy jec = getJobEntry(i);
			if (jec.getName().equalsIgnoreCase(name) && jec.getNr()==nr)
			{
				return jec;
			}
		}
		return null;
	}

	public JobEntryCopy findJobEntry(String full_name_nr)
	{
		int i;
		for (i=0;i<nrJobEntries();i++)
		{
			// log.logDebug("findChefGraphEntry()", "looking at nr: "+i);

			JobEntryCopy jec = getJobEntry(i);
			JobEntryInterface je = jec.getEntry();
			if (je.toString().equalsIgnoreCase(full_name_nr))
			{
				return jec;
			}
		}
		return null;
	}

	public JobHopMeta findJobHop(String name)
	{
		int i;
		for (i=0;i<nrJobHops();i++)
		{
			JobHopMeta hi = getJobHop(i); 
			if (hi.toString().equalsIgnoreCase(name))
			{
				return hi; 
			}
		}
		return null;
	}

	public JobHopMeta findJobHopFrom(JobEntryCopy jge)
	{
		int i;
		for (i=0;i<nrJobHops();i++)
		{
			JobHopMeta hi = getJobHop(i); 
			if (hi.from_entry.equals(jge)) // return the first
			{
				return hi; 
			}
		}
		return null;
	}

	public JobHopMeta findJobHop(JobEntryCopy from, JobEntryCopy to)
	{
		int i;
		for (i=0;i<nrJobHops();i++)
		{
			JobHopMeta hi = getJobHop(i);
			if (hi.isEnabled())
			{ 
				if (hi!=null && hi.from_entry!=null && hi.to_entry!=null &&
				    hi.from_entry.equals(from) && hi.to_entry.equals(to)
				   )
				{
					return hi; 
				}
			}
		}
		return null;
	}

	public JobHopMeta findJobHopTo(JobEntryCopy jge)
	{
		int i;
		for (i=0;i<nrJobHops();i++)
		{
			JobHopMeta hi = getJobHop(i); 
			if (hi!=null && hi.to_entry!=null && hi.to_entry.equals(jge)) // Return the first!
			{
				return hi; 
			}
		}
		return null;
	}

	public int findNrPrevChefGraphEntries(JobEntryCopy from)
	{
		return findNrPrevChefGraphEntries(from, false);
	}

	public JobEntryCopy findPrevChefGraphEntry(JobEntryCopy to, int nr)
	{
		return findPrevChefGraphEntry(to, nr, false);
	}
	
	public int findNrPrevChefGraphEntries(JobEntryCopy to, boolean info)
	{
		int count=0;
		int i;
		
		for (i=0;i<nrJobHops();i++) // Look at all the hops;
		{
			JobHopMeta hi = getJobHop(i);
			if (hi.isEnabled() && hi.to_entry.equals(to))
			{
				count++; 
			} 
		}
		return count;
	}
	
	public JobEntryCopy findPrevChefGraphEntry(JobEntryCopy to, int nr, boolean info)
	{
		int count=0;
		int i;

		for (i=0;i<nrJobHops();i++) // Look at all the hops;
		{
			JobHopMeta hi = getJobHop(i);
			if (hi.isEnabled() && hi.to_entry.equals(to))
			{
				if (count==nr)
				{
					return hi.from_entry;
				}
				count++;
			}
		}
		return null;
	}

	public int findNrNextChefGraphEntries(JobEntryCopy from)
	{
		int count=0;
		int i;
		for (i=0;i<nrJobHops();i++) // Look at all the hops;
		{
			JobHopMeta hi = getJobHop(i);
			if (hi.isEnabled() && hi.from_entry.equals(from)) count++;
		}
		return count;
	}
	
	public JobEntryCopy findNextChefGraphEntry(JobEntryCopy from, int cnt)
	{
		int count=0;
		int i;

		for (i=0;i<nrJobHops();i++) // Look at all the hops;
		{
			JobHopMeta hi = getJobHop(i);
			if (hi.isEnabled() && hi.from_entry.equals(from)) 
			{
				if (count==cnt)
				{
					return hi.to_entry;
				}
				count++;
			}
		}
		return null;
	}

	public boolean hasLoop(JobEntryCopy entry)
	{
		return hasLoop(entry, null);
	}
	
	public boolean hasLoop(JobEntryCopy entry, JobEntryCopy lookup)
	{
		return false;
	}
	
	public boolean isEntryUsedInHops(JobEntryCopy jge)
	{
		JobHopMeta fr = findJobHopFrom(jge);
		JobHopMeta to = findJobHopTo(jge);
		if (fr!=null || to!=null) return true;
		return false;
	}

	public int countEntries(String name)
	{
		int count=0;
		int i;
		for (i=0;i<nrJobEntries();i++) // Look at all the hops;
		{
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name)) count++;
		}
		return count;
	}

	public int generateJobEntryNameNr(String basename)
	{
		int nr=1;
		
		JobEntryCopy e = findJobEntry(basename+" "+nr, 0); 
		while(e!=null)
		{
			nr++;
			e = findJobEntry(basename+" "+nr, 0);
		}
		return nr;
	}

	public int findUnusedNr(String name)
	{
		int nr=1;
		JobEntryCopy je = findJobEntry(name, nr);
		while (je!=null)
		{
			nr++;
			//log.logDebug("findUnusedNr()", "Trying unused nr: "+nr);
			je = findJobEntry(name, nr);
		}
		return nr;
	}

	public int findMaxNr(String name)
	{
		int max=0;
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name))
			{
				if (je.getNr()>max) max=je.getNr();
			}
		}
		return max;
	}

	/**
	 * Proposes an alternative job entry name when the original already exists...
	 * @param entryname The job entry name to find an alternative for..
	 * @return The alternative stepname.
	 */
	public String getAlternativeJobentryName(String entryname)
	{
		String newname = entryname;
		JobEntryCopy jec = findJobEntry(newname);
		int nr = 1;
		while (jec!=null)
		{
			nr++;
			newname = entryname + " "+nr;
			jec = findJobEntry(newname);
		}
			
		return newname;
	}


	public JobEntryCopy[] getAllChefGraphEntries(String name)
	{
		int count=0;
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name)) count++;
		}
		JobEntryCopy retval[] = new JobEntryCopy[count];

		count=0;
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name)) 
			{
				retval[count]=je;
				count++;
			} 
		}
		return retval;
	}

	public JobHopMeta[] getAllJobHopsUsing(String name)
	{
		int count=0;
		for (int i=0;i<nrJobHops();i++)
		{
			JobHopMeta hi = getJobHop(i);
			if (hi.from_entry.getName().equalsIgnoreCase(name) ||
				hi.to_entry.getName().equalsIgnoreCase(name) )
			{
				count++;
			}
		}
		JobHopMeta retval[] = new JobHopMeta[count];

		count=0;
		for (int i=0;i<nrJobHops();i++)
		{
			JobHopMeta hi = getJobHop(i);
			if (hi.from_entry.getName().equalsIgnoreCase(name) ||
				hi.to_entry.getName().equalsIgnoreCase(name) )
			{
				retval[count]=hi;
				count++;
			}
		}
		return retval;
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
	
	public void selectAll()
	{
		int i;
		for (i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy ce = getJobEntry(i);
			ce.setSelected(true);
		}
	}

	public void unselectAll()
	{
		int i;
		for (i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy ce = getJobEntry(i);
			ce.setSelected(false);
		}
	}

	public void selectInRect(Rectangle rect) 
	{
		int i;
		for (i = 0; i < nrJobEntries(); i++) 
		{
			JobEntryCopy je = getJobEntry(i);
			Point p = je.getLocation();
			if (((p.x >= rect.x && p.x <= rect.x + rect.width)
				|| (p.x >= rect.x + rect.width && p.x <= rect.x))
				&& ((p.y >= rect.y && p.y <= rect.y + rect.height)
					|| (p.y >= rect.y + rect.height && p.y <= rect.y))
				)
				je.setSelected(true);
		}
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

	public int getUndoSize()
	{
		if (undo==null) return 0;
		return undo.size();
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

	/** 
	 * View current undo, don't change undo position
	 * 
	 * @return The current undo transaction 
	 */
	public TransAction viewThisUndo()
	{
		if (undo.size()==0 || undo_position<0) return null;  // No undo left!
		
		TransAction retval = (TransAction)undo.get(undo_position);
		
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
	
	public Point getMaximum()
	{
		int maxx = 0, maxy = 0;
		for (int i = 0; i < nrJobEntries(); i++)
		{
			JobEntryCopy entry = getJobEntry(i);
			Point loc = entry.getLocation();
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


	public Point[] getSelectedLocations()
	{
		int sels = nrSelected();
		Point retval[] = new Point[sels];
		for (int i=0;i<sels;i++)
		{
			JobEntryCopy si = getSelected(i);
			Point p = si.getLocation();
			retval[i] = new Point(p.x, p.y); // explicit copy of location
		}
		return retval;
	}

	public JobEntryCopy[] getSelectedEntries()
	{
		int sels = nrSelected();
		if (sels==0) return null;
		
		JobEntryCopy retval[] = new JobEntryCopy[sels];
		for (int i=0;i<sels;i++)
		{
			JobEntryCopy je = getSelected(i);
			retval[i] = je;
		}
		return retval;
	}


	public int nrSelected() 
	{
		int i, count;
		count = 0;
		for (i = 0; i < nrJobEntries(); i++) 
		{
			JobEntryCopy je = getJobEntry(i);
			if (je.isSelected()) count++;
		}
		return count;
	}

	public JobEntryCopy getSelected(int nr) 
	{
		int i, count;
		count = 0;
		for (i = 0; i < nrJobEntries(); i++) 
		{
			JobEntryCopy je = getJobEntry(i);
			if (je.isSelected()) 
			{
				if (nr == count) return je;
				count++;
			}
		}
		return null;
	}

	public int[] getEntryIndexes(JobEntryCopy entries[])
	{
		int retval[] = new int[entries.length];
		
		for (int i=0;i<entries.length;i++) retval[i]=indexOfJobEntry(entries[i]);
		
		return retval;
	}
	
	public JobEntryCopy findStart()
	{
		for (int i=0;i<nrJobEntries();i++)
		{
			if (getJobEntry(i).isStart()) return getJobEntry(i);
		}
		return null;
	}

    /**
     * TODO: finish this method...
     * 
     
	public void getSQL()
	{
		ArrayList stats = new ArrayList();
		
		for (int i=0;i<nrJobEntries();i++)
		{
			JobEntryCopy jec = getJobEntry(i);
			if (jec.getType() == JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION)
			{
				JobEntryTrans jet = (JobEntryTrans)jec.getEntry();
				String transname = jet.getTransname();
				String directory = jet.getDirectory().getPath();
                
			}
			else
			if (jec.getType() == JobEntryInterface.TYPE_JOBENTRY_JOB)
			{
				
			}
		}
	}
    */

	public String toString()
	{
		if (getName()!=null) return getName();
		else return getClass().getName();	
	}


}
