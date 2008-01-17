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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlagInterface;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.SharedObjectInterface;
import be.ibridge.kettle.core.SharedObjects;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.reflection.StringSearchResult;
import be.ibridge.kettle.core.reflection.StringSearcher;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.spoon.UndoInterface;
import be.ibridge.kettle.trans.HasDatabasesInterface;

/**
 * Defines a Job and provides methods to load, save, verify, etc.
 * 
 * @author Matt
 * @since 11-08-2003
 * 
 */
public class JobMeta implements Cloneable, Comparable, XMLInterface, UndoInterface, HasDatabasesInterface, ChangedFlagInterface
{
    public static final String  XML_TAG              = "job"; //$NON-NLS-1$

    public LogWriter            log;

	private static final String STRING_CREATED_DATE = "CREATED_DATE";
    private static final String STRING_MODIFIED_DATE = "MODIFIED_DATE"; //$NON-NLS-1$

    private long                id;

    private String              name;

	private String              description;

	private String             extended_description;

	private String				job_version;

	private int 				job_status;

    private String              filename;

    public ArrayList            jobentries;

    public ArrayList            jobcopies;

    public ArrayList            jobhops;

    public ArrayList            notes;

    public ArrayList            databases;

    private RepositoryDirectory directory;

    private String              arguments[];

    private boolean             changed, changed_entries, changed_hops, changed_notes, changed_databases;

    private DatabaseMeta        logconnection;

    private String              logTable;

    public DBCache              dbcache;

    private ArrayList           undo;

    private int                 max_undo;

    private int                 undo_position;

    public static final int     TYPE_UNDO_CHANGE     = 1;

    public static final int     TYPE_UNDO_NEW        = 2;

    public static final int     TYPE_UNDO_DELETE     = 3;

    public static final int     TYPE_UNDO_POSITION   = 4;

    public static final String STRING_SPECIAL        = "SPECIAL"; //$NON-NLS-1$
    public static final String STRING_SPECIAL_START  = "START"; //$NON-NLS-1$
    public static final String STRING_SPECIAL_DUMMY  = "DUMMY"; //$NON-NLS-1$
    public static final String STRING_SPECIAL_OK     = "OK"; //$NON-NLS-1$
    public static final String STRING_SPECIAL_ERROR  = "ERROR"; //$NON-NLS-1$



    // Remember the size and position of the different windows...
    public boolean              max[]                = new boolean[1];

    public Rectangle            size[]               = new Rectangle[1];

    public String               created_user, modifiedUser;

    public Value                created_date, modifiedDate;

    private boolean             useBatchId;

    private boolean             batchIdPassed;

    private boolean             logfieldUsed;

    /** If this is null, we load from the default shared objects file : $KETTLE_HOME/.kettle/shared.xml */
    private String              sharedObjectsFile;

    public JobMeta(LogWriter l)
    {
        log = l;
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
        jobcopies = new ArrayList();
        jobentries = new ArrayList();
        jobhops = new ArrayList();
        notes = new ArrayList();
        databases = new ArrayList();
        logconnection = null;
        logTable = null;
        arguments = null;

        max_undo = Const.MAX_UNDO;

        dbcache = DBCache.getInstance();

        undo = new ArrayList();
        undo_position = -1;

        addDefaults();
        setChanged(false);

		created_user = "-"; //$NON-NLS-1$
		created_date = new Value("create_date", Value.VALUE_TYPE_DATE).sysdate(); //$NON-NLS-1$

        modifiedUser = "-"; //$NON-NLS-1$
        modifiedDate = new Value("modifiedDate", Value.VALUE_TYPE_DATE).sysdate(); //$NON-NLS-1$
        directory = new RepositoryDirectory();
		description=null;
		job_status=-1;
        job_version=null;
		extended_description=null;
        useBatchId=true;
        logfieldUsed=true;

        // setInternalKettleVariables(); Don't clear the internal variables for ad-hoc jobs, it's ruins the previews
        // etc.
    }

    public void addDefaults()
    {
        /*
        addStart(); // Add starting point!
        addDummy(); // Add dummy!
        addOK(); // errors == 0 evaluation
        addError(); // errors != 0 evaluation
        */
        
        clearChanged();
    }

    public static final JobEntryCopy createStartEntry()
    {
        JobEntrySpecial jobEntrySpecial = new JobEntrySpecial(STRING_SPECIAL_START, true, false);
        JobEntryCopy jobEntry = new JobEntryCopy();
        jobEntry.setID(-1L);
        jobEntry.setEntry(jobEntrySpecial);
        jobEntry.setLocation(50, 50);
        jobEntry.setDrawn(false);
        jobEntry.setDescription(Messages.getString("JobMeta.StartJobEntry.Description")); //$NON-NLS-1$
        return jobEntry;

    }

    public static final JobEntryCopy createDummyEntry()
    {
        JobEntrySpecial jobEntrySpecial = new JobEntrySpecial(STRING_SPECIAL_DUMMY, false, true);
        JobEntryCopy jobEntry = new JobEntryCopy();
        jobEntry.setID(-1L);
        jobEntry.setEntry(jobEntrySpecial);
        jobEntry.setLocation(50, 50);
        jobEntry.setDrawn(false);
        jobEntry.setDescription(Messages.getString("JobMeta.DummyJobEntry.Description")); //$NON-NLS-1$
        return jobEntry;
    }

    public JobEntryCopy getStart()
    {
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy cge = getJobEntry(i);
            if (cge.isStart()) return cge;
        }
        return null;
    }

    public JobEntryCopy getDummy()
    {
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy cge = getJobEntry(i);
            if (cge.isDummy()) return cge;
        }
        return null;
    }

    /**
     * Compares two transformation on name, filename
     */
    public int compare(Object o1, Object o2)
    {
        JobMeta t1 = (JobMeta) o1;
        JobMeta t2 = (JobMeta) o2;

        if (Const.isEmpty(t1.getName()) && !Const.isEmpty(t2.getName())) return -1;
        if (!Const.isEmpty(t1.getName()) && Const.isEmpty(t2.getName())) return 1;
        if (Const.isEmpty(t1.getName()) && Const.isEmpty(t2.getName()))
        {
            if (Const.isEmpty(t1.getFilename()) && !Const.isEmpty(t2.getFilename())) return -1;
            if (!Const.isEmpty(t1.getFilename()) && Const.isEmpty(t2.getFilename())) return 1;
            if (Const.isEmpty(t1.getFilename()) && Const.isEmpty(t2.getFilename())) { return 0; }
            return t1.getFilename().compareTo(t2.getFilename());
        }
        return t1.getName().compareTo(t2.getName());
    }
    
    public int compareTo(Object o)
    {
        return compare(this, o);
    }

    public boolean equals(Object obj)
    {
        return compare(this, obj) == 0;
    }

    public Object clone()
    {
        try
        {
            Object retval = super.clone();
            return retval;
        }
        catch (CloneNotSupportedException e)
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
        this.name = name;
        setInternalKettleVariables();
    }

    /**
     * Builds a name - if no name is set, yet - from the filename
     */
    public void nameFromFilename()
    {
        if (!Const.isEmpty(filename))
        {
            name = Const.createName(filename);
        }
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
        setInternalKettleVariables();
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
        setInternalKettleVariables();
    }

    public DatabaseMeta getLogConnection()
    {
        return logconnection;
    }

    public void setLogConnection(DatabaseMeta ci)
    {
        logconnection = ci;
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
        changed = ch;
    }

    public void clearChanged()
    {
        changed_entries = false;
        changed_hops = false;
        changed_notes = false;
        changed_databases = false;

        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy entry = getJobEntry(i);
            entry.setChanged(false);
        }
        for (int i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta hop = getJobHop(i);
            hop.setChanged(false);
        }
        for (int i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta db = getDatabase(i);
            db.setChanged(false);
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta note = getNote(i);
            note.setChanged(false);
        }
        changed = false;
    }

    public boolean hasChanged()
    {
        if (changed) return true;
        
        if (haveJobEntriesChanged()) return true;
        if (haveJobHopsChanged()) return true;
        if (haveConnectionsChanged()) return true;
        if (haveNotesChanged()) return true;
        
        return false;
    }

    private void saveRepJob(Repository rep) throws KettleException
    {
        try
        {
            // The ID has to be assigned, even when it's a new item...
            rep.insertJob(getID(), directory.getID(), getName(), logconnection == null ? -1 : logconnection.getID(), logTable, modifiedUser,
                    modifiedDate, useBatchId, batchIdPassed, logfieldUsed, sharedObjectsFile,description,extended_description,job_version,
					job_status, created_user,created_date);
        }
        catch (KettleDatabaseException dbe)
        {
            throw new KettleException(Messages.getString("JobMeta.Exception.UnableToSaveJobToRepository"), dbe); //$NON-NLS-1$
        }
    }

    public boolean showReplaceWarning(Repository rep)
    {
        if (getID() < 0)
        {
            try
            {
                if (rep.getJobID(getName(), directory.getID()) > 0) return true;
            }
            catch (KettleDatabaseException dbe)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * This method asks all steps in the transformation whether or not the specified database connection is used.
     * The connection is used in the transformation if any of the steps uses it or if it is being used to log to.
     * @param databaseMeta The connection to check
     * @return true if the connection is used in this transformation.
     */
    public boolean isDatabaseConnectionUsed(DatabaseMeta databaseMeta)
    {
        for (int i=0;i<nrJobEntries();i++)
        {
            JobEntryCopy jobEntry = getJobEntry(i);
            DatabaseMeta dbs[] = jobEntry.getEntry().getUsedDatabaseConnections();
            for (int d=0;d<dbs.length;d++)
            {
                if (dbs[d]!=null && dbs[d].equals(databaseMeta)) return true;
            }
        }

        if (logconnection!=null && logconnection.equals(databaseMeta)) return true;

        return false;
    }

    public String getXML()
    {
        Props props = null;
        if (Props.isInitialized()) props=Props.getInstance();

        DatabaseMeta ci = getLogConnection();
        StringBuffer retval = new StringBuffer(500);

        retval.append("<").append(XML_TAG).append(">").append(Const.CR); //$NON-NLS-1$
        
        retval.append("  ").append(XMLHandler.addTagValue("name", getName())); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("description", description)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("extended_description", extended_description)); 
		retval.append("    ").append(XMLHandler.addTagValue("job_version", job_version));
		if ( job_status >= 0 )
		{
		    retval.append("    ").append(XMLHandler.addTagValue("job_status", job_status));
		}

        retval.append("  ").append(XMLHandler.addTagValue("directory", directory.getPath())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("  ").append(XMLHandler.addTagValue("created_user", created_user)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("  ").append(XMLHandler.addTagValue("created_date", created_date != null ? created_date.getString() : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("  ").append(XMLHandler.addTagValue("modified_user", modifiedUser)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("  ").append(XMLHandler.addTagValue("modified_date", modifiedDate != null ? modifiedDate.getString() : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Save the database connections...
        for (int i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta dbMeta = getDatabase(i);
            if (props!=null && props.areOnlyUsedConnectionsSavedToXML())
            {
                if (isDatabaseConnectionUsed(dbMeta)) 
                {
                    retval.append(dbMeta.getXML());
                }
            }
            else
            {
                retval.append(dbMeta.getXML());
            }
        }
        
        retval.append("  ").append(XMLHandler.addTagValue("logconnection", ci == null ? "" : ci.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("  ").append(XMLHandler.addTagValue("logtable", logTable)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("   ").append(XMLHandler.addTagValue("use_batchid", useBatchId)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("   ").append(XMLHandler.addTagValue("pass_batchid", batchIdPassed)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("   ").append(XMLHandler.addTagValue("use_logfield", logfieldUsed)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("   ").append(XMLHandler.addTagValue("shared_objects_file", sharedObjectsFile)); // $NON-NLS-1$

        retval.append("  <entries>").append(Const.CR); //$NON-NLS-1$
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy jge = getJobEntry(i);
            retval.append(jge.getXML());
        }
        retval.append("  </entries>").append(Const.CR); //$NON-NLS-1$

        retval.append("  <hops>").append(Const.CR); //$NON-NLS-1$
        for (int i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta hi = getJobHop(i);
            retval.append(hi.getXML());
        }
        retval.append("  </hops>").append(Const.CR); //$NON-NLS-1$

        retval.append("  <notepads>").append(Const.CR); //$NON-NLS-1$
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            retval.append(ni.getXML());
        }
        retval.append("  </notepads>").append(Const.CR); //$NON-NLS-1$

        retval.append("</").append(XML_TAG).append(">").append(Const.CR); //$NON-NLS-1$

        return retval.toString();
    }

    /**
     * Load the job from the XML file specified.
     * 
     * @param log the logging channel
     * @param fname The filename to load as a job
     * @param rep The repository to bind againt, null if there is no repository available.
     * @throws KettleXMLException
     */
    public JobMeta(LogWriter log, String fname, Repository rep) throws KettleXMLException
    {
        this.log = log;
        try
        {
            // OK, try to load using the VFS stuff...
            Document doc = XMLHandler.loadXMLFile(KettleVFS.getFileObject(fname));
            if (doc != null)
            {
                // Clear the job
                clear();

                // The jobnode
                Node jobnode = XMLHandler.getSubNode(doc, XML_TAG);

                loadXML(jobnode, rep);

                // Do this at the end
                setFilename(fname);
            }
            else
            {
                throw new KettleXMLException(Messages.getString("JobMeta.Exception.ErrorReadingFromXMLFile") + fname); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("JobMeta.Exception.UnableToLoadJobFromXMLFile") + fname + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public JobMeta(LogWriter log, Node jobnode, Repository rep) throws KettleXMLException
    {
        this.log = log;

        loadXML(jobnode, rep);
    }

    public void loadXML(Node jobnode, Repository rep) throws KettleXMLException
    {
        Props props = null;
        if (Props.isInitialized()) props = Props.getInstance();
       
        try
        {
            // clear the jobs;
            clear();
            
            //
            // get job info:
            //
            name = XMLHandler.getTagValue(jobnode, "name"); //$NON-NLS-1$

			// Optionally load the repository directory...
			//
			if (rep!=null) {
				String directoryPath = XMLHandler.getTagValue(jobnode, "directory");
				if (directoryPath!=null) {
					directory = rep.getDirectoryTree().findDirectory(directoryPath);
				}
			}
			
			// description
			description = XMLHandler.getTagValue(jobnode, "description"); 

			// extended description
			extended_description = XMLHandler.getTagValue(jobnode, "extended_description"); 

			// job version
			job_version = XMLHandler.getTagValue(jobnode, "job_version"); 

			// job status
			job_status = Const.toInt(XMLHandler.getTagValue(jobnode, "job_status"),-1); 

			// Created user/date
			created_user = XMLHandler.getTagValue(jobnode, "created_user"); //$NON-NLS-1$
			String createDate = XMLHandler.getTagValue(jobnode, "created_date"); //$NON-NLS-1$

			if (createDate != null)
			{
				created_date = new Value(STRING_CREATED_DATE, createDate);
				created_date.setType(Value.VALUE_TYPE_DATE);
			}

            // Changed user/date
            modifiedUser = XMLHandler.getTagValue(jobnode, "modified_user"); //$NON-NLS-1$
            String modDate = XMLHandler.getTagValue(jobnode, "modified_date"); //$NON-NLS-1$
            if (modDate != null)
            {
                modifiedDate = new Value(STRING_MODIFIED_DATE, modDate);
                modifiedDate.setType(Value.VALUE_TYPE_DATE);
            }

            // Load the default list of databases
            // Read objects from the shared XML file & the repository
            try
            {
                sharedObjectsFile = XMLHandler.getTagValue(jobnode, "shared_objects_file"); //$NON-NLS-1$ //$NON-NLS-2$
                readSharedObjects(rep);
            }
            catch(Exception e)
            {
                LogWriter.getInstance().logError(toString(), Messages.getString("JobMeta.ErrorReadingSharedObjects.Message", e.toString())); // $NON-NLS-1$ //$NON-NLS-1$
                LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
            }

            // 
            // Read the database connections
            //
            int nr = XMLHandler.countNodes(jobnode, "connection"); //$NON-NLS-1$
            for (int i = 0; i < nr; i++)
            {
                Node dbnode = XMLHandler.getSubNodeByNr(jobnode, "connection", i); //$NON-NLS-1$
                DatabaseMeta dbcon = new DatabaseMeta(dbnode);

                DatabaseMeta exist = findDatabase(dbcon.getName());
                if (exist == null)
                {
                    addDatabase(dbcon);
                }
                else
                {
                    boolean askOverwrite = Props.isInitialized() ? props.askAboutReplacingDatabaseConnections() : false;
                    boolean overwrite = Props.isInitialized() ? props.replaceExistingDatabaseConnections() : true;
                    if (askOverwrite)
                    {
                        // That means that we have a Display variable set in Props...
                        if (props.getDisplay() != null)
                        {
                            Shell shell = props.getDisplay().getActiveShell();

                            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, "Warning", null, 
                                    Messages.getString("JobMeta.Dialog.ConnectionExistsOverWrite.Message", dbcon.getName())  //$NON-NLS-1$ //$NON-NLS-2$
                                    , MessageDialog.WARNING, 
                                    new String[] { Messages.getString("System.Button.Yes"), //$NON-NLS-1$ 
                                                   Messages.getString("System.Button.No") },//$NON-NLS-1$
                                    1, Messages.getString("JobMeta.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage"), !props.askAboutReplacingDatabaseConnections()); //$NON-NLS-1$
                            int idx = md.open();
                            props.setAskAboutReplacingDatabaseConnections(!md.getToggleState());
                            overwrite = ((idx & 0xFF) == 0); // Yes means: overwrite
                        }
                    }

                    if (overwrite)
                    {
                        int idx = indexOfDatabase(exist);
                        removeDatabase(idx);
                        addDatabase(idx, dbcon);
                    }
                }
            }

            /*
             * Get the log database connection & log table
             */
            String logcon = XMLHandler.getTagValue(jobnode, "logconnection"); //$NON-NLS-1$
            logconnection = findDatabase(logcon);
            logTable = XMLHandler.getTagValue(jobnode, "logtable"); //$NON-NLS-1$

            useBatchId = "Y".equalsIgnoreCase(XMLHandler.getTagValue(jobnode, "use_batchid")); //$NON-NLS-1$ //$NON-NLS-2$
            batchIdPassed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(jobnode, "pass_batchid")); //$NON-NLS-1$ //$NON-NLS-2$
            logfieldUsed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(jobnode, "use_logfield")); //$NON-NLS-1$ //$NON-NLS-2$

            /*
             * read the job entries...
             */
            Node entriesnode = XMLHandler.getSubNode(jobnode, "entries"); //$NON-NLS-1$
            int tr = XMLHandler.countNodes(entriesnode, "entry"); //$NON-NLS-1$
            for (int i = 0; i < tr; i++)
            {
                Node entrynode = XMLHandler.getSubNodeByNr(entriesnode, "entry", i); //$NON-NLS-1$
                // System.out.println("Reading entry:\n"+entrynode);

                JobEntryCopy je = new JobEntryCopy(entrynode, databases, rep);
                JobEntryCopy prev = findJobEntry(je.getName(), 0, true);
                if (prev != null)
                {
                    if (je.getNr() == 0) // See if the #0 already exists!
                    {
                        // Replace previous version with this one: remove it first
                        int idx = indexOfJobEntry(prev);
                        removeJobEntry(idx);
                    }
                    else
                        if (je.getNr() > 0) // Use previously defined JobEntry info!
                        {
                            je.setEntry(prev.getEntry());

                            // See if entry already exists...
                            prev = findJobEntry(je.getName(), je.getNr(), true);
                            if (prev != null) // remove the old one!
                            {
                                int idx = indexOfJobEntry(prev);
                                removeJobEntry(idx);
                            }
                        }
                }
                // Add the JobEntryCopy...
                addJobEntry(je);
            }

            Node hopsnode = XMLHandler.getSubNode(jobnode, "hops"); //$NON-NLS-1$
            int ho = XMLHandler.countNodes(hopsnode, "hop"); //$NON-NLS-1$
            for (int i = 0; i < ho; i++)
            {
                Node hopnode = XMLHandler.getSubNodeByNr(hopsnode, "hop", i); //$NON-NLS-1$
                JobHopMeta hi = new JobHopMeta(hopnode, this);
                jobhops.add(hi);
            }

            // Read the notes...
            Node notepadsnode = XMLHandler.getSubNode(jobnode, "notepads"); //$NON-NLS-1$
            int nrnotes = XMLHandler.countNodes(notepadsnode, "notepad"); //$NON-NLS-1$
            for (int i = 0; i < nrnotes; i++)
            {
                Node notepadnode = XMLHandler.getSubNodeByNr(notepadsnode, "notepad", i); //$NON-NLS-1$
                NotePadMeta ni = new NotePadMeta(notepadnode);
                notes.add(ni);
            }

            // Do we have the special entries?
            // if (findJobEntry(STRING_SPECIAL_START, 0, true) == null) addJobEntry(JobMeta.createStartEntry()); // TODO: remove this
            // if (findJobEntry(STRING_SPECIAL_DUMMY, 0, true) == null) addJobEntry(JobMeta.createDummyEntry()); // TODO: remove this

            clearChanged();
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("JobMeta.Exception.UnableToLoadJobFromXMLNode"), e); //$NON-NLS-1$
        }
        finally
        {
            setInternalKettleVariables();
        }
    }

    /**
     * Read the database connections in the repository and add them to this job if they are not yet present.
     * 
     * @param rep The repository to load the database connections from.
     * @throws KettleException
     */
    public void readDatabases(Repository rep) throws KettleException
    {
        readDatabases(rep, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see be.ibridge.kettle.trans.HasDatabaseInterface#readDatabases(be.ibridge.kettle.repository.Repository, boolean)
     */
    public void readDatabases(Repository rep, boolean overWriteShared) throws KettleException
    {
        try
        {
            long dbids[] = rep.getDatabaseIDs();
            for (int i = 0; i < dbids.length; i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(rep, dbids[i]);
                DatabaseMeta check = findDatabase(databaseMeta.getName()); // Check if there already is one in the
                                                                            // transformation
                if (check == null || overWriteShared) // We only add, never overwrite database connections.
                {
                    if (databaseMeta.getName() != null)
                    {
                        addOrReplaceDatabase(databaseMeta);
                        if (!overWriteShared) databaseMeta.setChanged(false);
                    }
                }
            }
            setChanged(false);
        }
        catch (KettleDatabaseException dbe)
        {
            throw new KettleException(Messages.getString("JobMeta.Log.UnableToReadDatabaseIDSFromRepository"), dbe); //$NON-NLS-1$
        }
        catch (KettleException ke)
        {
            throw new KettleException(Messages.getString("JobMeta.Log.UnableToReadDatabasesFromRepository"), ke); //$NON-NLS-1$
        }
    }
    
    public void readSharedObjects(Repository rep) throws KettleException
    {
        // Extract the shared steps, connections, etc. using the SharedObjects class
        //
        String soFile = StringUtil.environmentSubstitute(sharedObjectsFile);
        SharedObjects sharedObjects = new SharedObjects(soFile); 
        Map objectsMap = sharedObjects.getObjectsMap();
        Collection objects = objectsMap.values();
        
        // First read the databases...
        // We read databases & slaves first because there might be dependencies that need to be resolved.
        //
        for (Iterator iter = objects.iterator(); iter.hasNext();)
        {
            Object object = iter.next();
            if (object instanceof DatabaseMeta)
            {
                DatabaseMeta databaseMeta = (DatabaseMeta) object;
                addOrReplaceDatabase(databaseMeta);
            }
        }

        if (rep!=null)
        {
            readDatabases(rep, true);
        }
    }
    
    public void saveSharedObjects() throws KettleException
    {
        try
        {
            // First load all the shared objects...
            String soFile = StringUtil.environmentSubstitute(sharedObjectsFile);
            SharedObjects sharedObjects = new SharedObjects(soFile);
            
            // Now overwrite the objects in there
            List shared = new ArrayList();
            shared.addAll(databases);
            
            // The databases connections...
            for (int i=0;i<shared.size();i++)
            {
                SharedObjectInterface sharedObject = (SharedObjectInterface) shared.get(i);
                if (sharedObject.isShared()) 
                {
                    sharedObjects.storeObject(sharedObject);
                }
            }
            
            // Save the objects
            sharedObjects.saveToFile();
        }
        catch(IOException e)
        {
            
        }
    }   

    /**
     * Find a database connection by it's name
     * 
     * @param name The database name to look for
     * @return The database connection or null if nothing was found.
     */
    public DatabaseMeta findDatabase(String name)
    {
        for (int i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta ci = getDatabase(i);
            if (ci.getName().equalsIgnoreCase(name)) { return ci; }
        }
        return null;
    }

    public void saveRep(Repository rep) throws KettleException
    {
        saveRep(rep, null);
    }

    public void saveRep(Repository rep, IProgressMonitor monitor) throws KettleException
    {
        try
        {
            int nrWorks = 2 + nrDatabases() + nrNotes() + nrJobEntries() + nrJobHops();
            if (monitor != null) monitor.beginTask(Messages.getString("JobMeta.Monitor.SavingTransformation") + directory + Const.FILE_SEPARATOR + getName(), nrWorks); //$NON-NLS-1$

            rep.lockRepository();
            
            rep.insertLogEntry("save job '"+getName()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            
            // Before we start, make sure we have a valid job ID!
            // Two possibilities:
            // 1) We have a ID: keep it
            // 2) We don't have an ID: look it up.
            // If we find a transformation with the same name: ask!
            //
            if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.HandlingPreviousVersionOfJob")); //$NON-NLS-1$
            setID(rep.getJobID(getName(), directory.getID()));

            // If no valid id is available in the database, assign one...
            if (getID() <= 0)
            {
                setID(rep.getNextJobID());
            }
            else
            {
                // If we have a valid ID, we need to make sure everything is cleared out
                // of the database for this id_job, before we put it back in...
                rep.delAllFromJob(getID());
            }
            if (monitor != null) monitor.worked(1);

            // Now, save the job entry in R_JOB
            // Note, we save this first so that we have an ID in the database.
            // Everything else depends on this ID, including recursive job entries to the save job. (retry)
            if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.SavingJobDetails")); //$NON-NLS-1$
            log.logDetailed(toString(), "Saving job info to repository..."); //$NON-NLS-1$
            saveRepJob(rep);
            if (monitor != null) monitor.worked(1);

            //
            // Save the notes
            //
            log.logDetailed(toString(), "Saving notes to repository..."); //$NON-NLS-1$
            for (int i = 0; i < nrNotes(); i++)
            {
                if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.SavingNoteNr") + (i + 1) + "/" + nrNotes()); //$NON-NLS-1$ //$NON-NLS-2$
                NotePadMeta ni = getNote(i);
                ni.saveRep(rep, getID());
                if (ni.getID() > 0)
                {
                    rep.insertJobNote(getID(), ni.getID());
                }
                if (monitor != null) monitor.worked(1);
            }

            //
            // Save the job entries
            //
            log.logDetailed(toString(), "Saving " + nrJobEntries() + " ChefGraphEntries to repository..."); //$NON-NLS-1$ //$NON-NLS-2$
            rep.updateJobEntryTypes();
            for (int i = 0; i < nrJobEntries(); i++)
            {
                if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.SavingJobEntryNr") + (i + 1) + "/" + nrJobEntries()); //$NON-NLS-1$ //$NON-NLS-2$
                JobEntryCopy cge = getJobEntry(i);
                cge.saveRep(rep, getID());
                if (monitor != null) monitor.worked(1);
            }

            log.logDetailed(toString(), "Saving job hops to repository..."); //$NON-NLS-1$
            for (int i = 0; i < nrJobHops(); i++)
            {
                if (monitor != null) monitor.subTask("Saving job hop #" + (i + 1) + "/" + nrJobHops()); //$NON-NLS-1$ //$NON-NLS-2$
                JobHopMeta hi = getJobHop(i);
                hi.saveRep(rep, getID());
                if (monitor != null) monitor.worked(1);
            }

            // Commit this transaction!!
            rep.commit();

            clearChanged();
            if (monitor != null) monitor.done();
        }
        catch (KettleDatabaseException dbe)
        {
            rep.rollback();
            throw new KettleException(Messages.getString("JobMeta.Exception.UnableToSaveJobInRepositoryRollbackPerformed"), dbe); //$NON-NLS-1$
        }
        finally
        {
            // don't forget to unlock the repository.
            // Normally this is done by the commit / rollback statement, but hey there are some freaky database out
            // there...
            rep.unlockRepository();
        }

    }

    /**
     * Load a job in a directory
     * 
     * @param log the logging channel
     * @param rep The Repository
     * @param jobname The name of the job
     * @param repdir The directory in which the job resides.
     * @throws KettleException
     */
    public JobMeta(LogWriter log, Repository rep, String jobname, RepositoryDirectory repdir) throws KettleException
    {
        this(log, rep, jobname, repdir, null);
    }

    /**
     * Load a job in a directory
     * 
     * @param log the logging channel
     * @param rep The Repository
     * @param jobname The name of the job
     * @param repdir The directory in which the job resides.
     * @throws KettleException
     */
    public JobMeta(LogWriter log, Repository rep, String jobname, RepositoryDirectory repdir, IProgressMonitor monitor) throws KettleException
    {
        this.log = log;

        try
        {
            // Clear everything...
            clear();

            directory = repdir;

            // Get the transformation id
            setID(rep.getJobID(jobname, repdir.getID()));

            // If no valid id is available in the database, then give error...
            if (getID() > 0)
            {
                // Load the notes...
                long noteids[] = rep.getJobNoteIDs(getID());
                long jecids[] = rep.getJobEntryCopyIDs(getID());
                long hopid[] = rep.getJobHopIDs(getID());

                int nrWork = 2 + noteids.length + jecids.length + hopid.length;
                if (monitor != null) monitor.beginTask(Messages.getString("JobMeta.Monitor.LoadingJob") + repdir + Const.FILE_SEPARATOR + jobname, nrWork); //$NON-NLS-1$

                //
                // get job info:
                //
                if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.ReadingJobInformation")); //$NON-NLS-1$
                Row jobRow = rep.getJob(getID());

                name = jobRow.searchValue("NAME").getString(); //$NON-NLS-1$
				description = jobRow.searchValue("DESCRIPTION").getString(); //$NON-NLS-1$
				extended_description = jobRow.searchValue("EXTENDED_DESCRIPTION").getString(); //$NON-NLS-1$
				job_version = jobRow.searchValue("JOB_VERSION").getString(); //$NON-NLS-1$
				job_status = Const.toInt(jobRow.searchValue("JOB_STATUS").getString(),-1); //$NON-NLS-1$
                logTable = jobRow.searchValue("TABLE_NAME_LOG").getString(); //$NON-NLS-1$

				created_user = jobRow.searchValue("CREATED_USER").getString(); //$NON-NLS-1$
				created_date = jobRow.searchValue("CREATED_DATE"); //$NON-NLS-1$

				modifiedUser = jobRow.searchValue("MODIFIED_USER").getString(); //$NON-NLS-1$
				modifiedDate = jobRow.searchValue("MODIFIED_DATE"); //$NON-NLS-1$

                long id_logdb = jobRow.searchValue("ID_DATABASE_LOG").getInteger(); //$NON-NLS-1$
                if (id_logdb > 0)
                {
                    // Get the logconnection
                    logconnection = new DatabaseMeta(rep, id_logdb);
                }
                useBatchId = jobRow.getBoolean("USE_BATCH_ID", false); //$NON-NLS-1$
                batchIdPassed = jobRow.getBoolean("PASS_BATCH_ID", false); //$NON-NLS-1$
                logfieldUsed = jobRow.getBoolean("USE_LOGFIELD", false); //$NON-NLS-1$

                if (monitor != null) monitor.worked(1);
                // 
                // Load the common database connections
                //
                if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.ReadingAvailableDatabasesFromRepository")); //$NON-NLS-1$
                // Read objects from the shared XML file & the repository
                try
                {
                    sharedObjectsFile = jobRow.getString("SHARED_FILE", null);
                    readSharedObjects(rep);
                }
                catch(Exception e)
                {
                    LogWriter.getInstance().logError(toString(), Messages.getString("JobMeta.ErrorReadingSharedObjects.Message", e.toString())); // $NON-NLS-1$ //$NON-NLS-1$
                    LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                }
                if (monitor != null) monitor.worked(1);

                
                log.logDetailed(toString(), "Loading " + noteids.length + " notes"); //$NON-NLS-1$ //$NON-NLS-2$
                for (int i = 0; i < noteids.length; i++)
                {
                    if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.ReadingNoteNr") + (i + 1) + "/" + noteids.length); //$NON-NLS-1$ //$NON-NLS-2$
                    NotePadMeta ni = new NotePadMeta(log, rep, noteids[i]);
                    if (indexOfNote(ni) < 0) addNote(ni);
                    if (monitor != null) monitor.worked(1);
                }

                // Load the job entries...
                log.logDetailed(toString(), "Loading " + jecids.length + " job entries"); //$NON-NLS-1$ //$NON-NLS-2$
                for (int i = 0; i < jecids.length; i++)
                {
                    if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.ReadingJobEntryNr") + (i + 1) + "/" + (jecids.length)); //$NON-NLS-1$ //$NON-NLS-2$

                    JobEntryCopy jec = new JobEntryCopy(log, rep, getID(), jecids[i], jobentries, databases);
                    
    				// Also set the copy number...
    				// We count the number of job entry copies that use the job entry
                    //
                    int copyNr = 0;
                    for (int c=0;c<nrJobEntries();c++) {
                    	JobEntryCopy copy = getJobEntry(c);
                    	if (jec.getEntry()==copy.getEntry()) {
                    		copyNr++;
                    	}
                    }
                    jec.setNr(copyNr);
                    
                    int idx = indexOfJobEntry(jec);
                    if (idx < 0)
                    {
                        if (jec.getName() != null && jec.getName().length() > 0) addJobEntry(jec);
                    }
                    else
                    {
                        setJobEntry(idx, jec); // replace it!
                    }
                    if (monitor != null) monitor.worked(1);
                }

                // Load the hops...
                log.logDetailed(toString(), "Loading " + hopid.length + " job hops"); //$NON-NLS-1$ //$NON-NLS-2$
                for (int i = 0; i < hopid.length; i++)
                {
                    if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.ReadingJobHopNr") + (i + 1) + "/" + (jecids.length)); //$NON-NLS-1$ //$NON-NLS-2$
                    JobHopMeta hi = new JobHopMeta(rep, hopid[i], this, jobcopies);
                    jobhops.add(hi);
                    if (monitor != null) monitor.worked(1);
                }

                // Finally, clear the changed flags...
                clearChanged();
                if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.FinishedLoadOfJob")); //$NON-NLS-1$
                if (monitor != null) monitor.done();
            }
            else
            {
                throw new KettleException(Messages.getString("JobMeta.Exception.CanNotFindJob") + jobname); //$NON-NLS-1$
            }
        }
        catch (KettleException dbe)
        {
            throw new KettleException(Messages.getString("JobMeta.Exception.AnErrorOccuredReadingJob", jobname), dbe);
        }
        finally
        {
            setInternalKettleVariables();
        }
    }

    public JobEntryCopy getChefGraphEntry(int x, int y, int iconsize)
    {
        int i, s;
        s = nrJobEntries();
        for (i = s - 1; i >= 0; i--) // Back to front because drawing goes from start to end
        {
            JobEntryCopy je = getJobEntry(i);
            Point p = je.getLocation();
            if (p != null)
            {
                if (x >= p.x && x <= p.x + iconsize && y >= p.y && y <= p.y + iconsize) { return je; }
            }
        }
        return null;
    }

    public int nrJobEntries()
    {
        return jobcopies.size();
    }

    public int nrJobHops()
    {
        return jobhops.size();
    }

    public int nrNotes()
    {
        return notes.size();
    }

    public int nrDatabases()
    {
        return databases.size();
    }

    public JobHopMeta getJobHop(int i)
    {
        return (JobHopMeta) jobhops.get(i);
    }

    public JobEntryCopy getJobEntry(int i)
    {
        return (JobEntryCopy) jobcopies.get(i);
    }

    public NotePadMeta getNote(int i)
    {
        return (NotePadMeta) notes.get(i);
    }

    public DatabaseMeta getDatabase(int i)
    {
        return (DatabaseMeta) databases.get(i);
    }

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
        changed_databases = true;
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
        changed_databases = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see be.ibridge.kettle.trans.HasDatabaseInterface#addOrReplaceDatabase(be.ibridge.kettle.core.database.DatabaseMeta)
     */
    public void addOrReplaceDatabase(DatabaseMeta databaseMeta)
    {
        int index = databases.indexOf(databaseMeta);
        if (index < 0)
        {
            databases.add(databaseMeta);
        }
        else
        {
            DatabaseMeta previous = getDatabase(index);
            previous.replaceMeta(databaseMeta);
        }
        changed_databases = true;
    }

    public void removeJobEntry(int i)
    {
        jobcopies.remove(i);
        setChanged();
    }

    public void removeJobHop(int i)
    {
        jobhops.remove(i);
        setChanged();
    }

    public void removeNote(int i)
    {
        notes.remove(i);
        setChanged();
    }

    public void removeDatabase(int i)
    {
        if (i < 0 || i >= databases.size()) return;
        databases.remove(i);
        changed_databases = true;
    }

    public int indexOfJobHop(JobHopMeta he)
    {
        return jobhops.indexOf(he);
    }

    public int indexOfNote(NotePadMeta ni)
    {
        return notes.indexOf(ni);
    }

    public int indexOfJobEntry(JobEntryCopy ge)
    {
        return jobcopies.indexOf(ge);
    }

    public int indexOfDatabase(DatabaseMeta di)
    {
        return databases.indexOf(di);
    }

    public void setJobEntry(int idx, JobEntryCopy jec)
    {
        jobcopies.set(idx, jec);
    }

    /**
     * Find an existing JobEntryCopy by it's name and number
     * 
     * @param name The name of the job entry copy
     * @param nr The number of the job entry copy
     * @return The JobEntryCopy or null if nothing was found!
     */
    public JobEntryCopy findJobEntry(String name, int nr, boolean searchHiddenToo)
    {
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy jec = getJobEntry(i);
            if (jec.getName().equalsIgnoreCase(name) && jec.getNr() == nr)
            {
                if (searchHiddenToo || jec.isDrawn()) { return jec; }
            }
        }
        return null;
    }

    public JobEntryCopy findJobEntry(String full_name_nr)
    {
        int i;
        for (i = 0; i < nrJobEntries(); i++)
        {
            // log.logDebug("findChefGraphEntry()", "looking at nr: "+i);

            JobEntryCopy jec = getJobEntry(i);
            JobEntryInterface je = jec.getEntry();
            if (je.toString().equalsIgnoreCase(full_name_nr)) { return jec; }
        }
        return null;
    }

    public JobHopMeta findJobHop(String name)
    {
        int i;
        for (i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta hi = getJobHop(i);
            if (hi.toString().equalsIgnoreCase(name)) { return hi; }
        }
        return null;
    }

    public JobHopMeta findJobHopFrom(JobEntryCopy jge)
    {
        int i;
        for (i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta hi = getJobHop(i);
            if (hi.from_entry.equals(jge)) // return the first
            { return hi; }
        }
        return null;
    }

    public JobHopMeta findJobHop(JobEntryCopy from, JobEntryCopy to)
    {
        int i;
        for (i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta hi = getJobHop(i);
            if (hi.isEnabled())
            {
                if (hi != null && hi.from_entry != null && hi.to_entry != null && hi.from_entry.equals(from) && hi.to_entry.equals(to)) { return hi; }
            }
        }
        return null;
    }

    public JobHopMeta findJobHopTo(JobEntryCopy jge)
    {
        int i;
        for (i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta hi = getJobHop(i);
            if (hi != null && hi.to_entry != null && hi.to_entry.equals(jge)) // Return the first!
            { return hi; }
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
        int count = 0;
        int i;

        for (i = 0; i < nrJobHops(); i++) // Look at all the hops;
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
        int count = 0;
        int i;

        for (i = 0; i < nrJobHops(); i++) // Look at all the hops;
        {
            JobHopMeta hi = getJobHop(i);
            if (hi.isEnabled() && hi.to_entry.equals(to))
            {
                if (count == nr) { return hi.from_entry; }
                count++;
            }
        }
        return null;
    }

    public int findNrNextChefGraphEntries(JobEntryCopy from)
    {
        int count = 0;
        int i;
        for (i = 0; i < nrJobHops(); i++) // Look at all the hops;
        {
            JobHopMeta hi = getJobHop(i);
            if (hi.isEnabled() && hi.from_entry.equals(from)) count++;
        }
        return count;
    }

    public JobEntryCopy findNextChefGraphEntry(JobEntryCopy from, int cnt)
    {
        int count = 0;
        int i;

        for (i = 0; i < nrJobHops(); i++) // Look at all the hops;
        {
            JobHopMeta hi = getJobHop(i);
            if (hi.isEnabled() && hi.from_entry.equals(from))
            {
                if (count == cnt) { return hi.to_entry; }
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
        if (fr != null || to != null) return true;
        return false;
    }

    public int countEntries(String name)
    {
        int count = 0;
        int i;
        for (i = 0; i < nrJobEntries(); i++) // Look at all the hops;
        {
            JobEntryCopy je = getJobEntry(i);
            if (je.getName().equalsIgnoreCase(name)) count++;
        }
        return count;
    }

    public int generateJobEntryNameNr(String basename)
    {
        int nr = 1;

        JobEntryCopy e = findJobEntry(basename + " " + nr, 0, true); //$NON-NLS-1$
        while (e != null)
        {
            nr++;
            e = findJobEntry(basename + " " + nr, 0, true); //$NON-NLS-1$
        }
        return nr;
    }

    public int findUnusedNr(String name)
    {
        int nr = 1;
        JobEntryCopy je = findJobEntry(name, nr, true);
        while (je != null)
        {
            nr++;
            // log.logDebug("findUnusedNr()", "Trying unused nr: "+nr);
            je = findJobEntry(name, nr, true);
        }
        return nr;
    }

    public int findMaxNr(String name)
    {
        int max = 0;
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy je = getJobEntry(i);
            if (je.getName().equalsIgnoreCase(name))
            {
                if (je.getNr() > max) max = je.getNr();
            }
        }
        return max;
    }

    /**
     * Proposes an alternative job entry name when the original already exists...
     * 
     * @param entryname The job entry name to find an alternative for..
     * @return The alternative stepname.
     */
    public String getAlternativeJobentryName(String entryname)
    {
        String newname = entryname;
        JobEntryCopy jec = findJobEntry(newname);
        int nr = 1;
        while (jec != null)
        {
            nr++;
            newname = entryname + " " + nr; //$NON-NLS-1$
            jec = findJobEntry(newname);
        }

        return newname;
    }

    public JobEntryCopy[] getAllChefGraphEntries(String name)
    {
        int count = 0;
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy je = getJobEntry(i);
            if (je.getName().equalsIgnoreCase(name)) count++;
        }
        JobEntryCopy retval[] = new JobEntryCopy[count];

        count = 0;
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy je = getJobEntry(i);
            if (je.getName().equalsIgnoreCase(name))
            {
                retval[count] = je;
                count++;
            }
        }
        return retval;
    }

    public JobHopMeta[] getAllJobHopsUsing(String name)
    {
        List hops = new ArrayList();

        for (int i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta hi = getJobHop(i);
            if (hi.from_entry != null && hi.to_entry != null)
            {
                if (hi.from_entry.getName().equalsIgnoreCase(name) || hi.to_entry.getName().equalsIgnoreCase(name))
                {
                    hops.add(hi);
                }
            }
        }
        return (JobHopMeta[]) hops.toArray(new JobHopMeta[hops.size()]);
    }

    public NotePadMeta getNote(int x, int y)
    {
        int i, s;
        s = notes.size();
        for (i = s - 1; i >= 0; i--) // Back to front because drawing goes from start to end
        {
            NotePadMeta ni = (NotePadMeta) notes.get(i);
            Point loc = ni.getLocation();
            Point p = new Point(loc.x, loc.y);
            if (x >= p.x && x <= p.x + ni.width + 2 * Const.NOTE_MARGIN && y >= p.y && y <= p.y + ni.height + 2 * Const.NOTE_MARGIN) { return ni; }
        }
        return null;
    }

    public void selectAll()
    {
        int i;
        for (i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy ce = getJobEntry(i);
            ce.setSelected(true);
        }
    }

    public void unselectAll()
    {
        int i;
        for (i = 0; i < nrJobEntries(); i++)
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
            if (((p.x >= rect.x && p.x <= rect.x + rect.width) || (p.x >= rect.x + rect.width && p.x <= rect.x))
                    && ((p.y >= rect.y && p.y <= rect.y + rect.height) || (p.y >= rect.y + rect.height && p.y <= rect.y))) je.setSelected(true);
        }
    }

    public int getMaxUndo()
    {
        return max_undo;
    }

    public void setMaxUndo(int mu)
    {
        max_undo = mu;
        while (undo.size() > mu && undo.size() > 0)
            undo.remove(0);
    }

    public int getUndoSize()
    {
        if (undo == null) return 0;
        return undo.size();
    }
    
    public void clearUndo()
    {
        undo = new ArrayList();
        undo_position = -1;
    }

    public void addUndo(Object from[], Object to[], int pos[], Point prev[], Point curr[], int type_of_change, boolean nextAlso)
    {
        // First clean up after the current position.
        // Example: position at 3, size=5
        // 012345
        // ^
        // remove 34
        // Add 4
        // 01234

        while (undo.size() > undo_position + 1 && undo.size() > 0)
        {
            int last = undo.size() - 1;
            undo.remove(last);
        }

        TransAction ta = new TransAction();
        switch (type_of_change)
        {
        case TYPE_UNDO_CHANGE:
            ta.setChanged(from, to, pos);
            break;
        case TYPE_UNDO_DELETE:
            ta.setDelete(from, pos);
            break;
        case TYPE_UNDO_NEW:
            ta.setNew(from, pos);
            break;
        case TYPE_UNDO_POSITION:
            ta.setPosition(from, pos, prev, curr);
            break;
        }
        undo.add(ta);
        undo_position++;

        if (undo.size() > max_undo)
        {
            undo.remove(0);
            undo_position--;
        }
    }

    // get previous undo, change position
    public TransAction previousUndo()
    {
        if (undo.size() == 0 || undo_position < 0) return null; // No undo left!

        TransAction retval = (TransAction) undo.get(undo_position);

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
        if (undo.size() == 0 || undo_position < 0) return null; // No undo left!

        TransAction retval = (TransAction) undo.get(undo_position);

        return retval;
    }

    // View previous undo, don't change position
    public TransAction viewPreviousUndo()
    {
        if (undo.size() == 0 || undo_position < 0) return null; // No undo left!

        TransAction retval = (TransAction) undo.get(undo_position);

        return retval;
    }

    public TransAction nextUndo()
    {
        int size = undo.size();
        if (size == 0 || undo_position >= size - 1) return null; // no redo left...

        undo_position++;

        TransAction retval = (TransAction) undo.get(undo_position);

        return retval;
    }

    public TransAction viewNextUndo()
    {
        int size = undo.size();
        if (size == 0 || undo_position >= size - 1) return null; // no redo left...

        TransAction retval = (TransAction) undo.get(undo_position + 1);

        return retval;
    }

    public Point getMaximum()
    {
        int maxx = 0, maxy = 0;
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy entry = getJobEntry(i);
            Point loc = entry.getLocation();
            if (loc.x > maxx) maxx = loc.x;
            if (loc.y > maxy) maxy = loc.y;
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            Point loc = ni.getLocation();
            if (loc.x + ni.width > maxx) maxx = loc.x + ni.width;
            if (loc.y + ni.height > maxy) maxy = loc.y + ni.height;
        }

        return new Point(maxx + 100, maxy + 100);
    }

    public Point[] getSelectedLocations()
    {
        int sels = nrSelected();
        Point retval[] = new Point[sels];
        for (int i = 0; i < sels; i++)
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
        if (sels == 0) return null;

        JobEntryCopy retval[] = new JobEntryCopy[sels];
        for (int i = 0; i < sels; i++)
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
            if (je.isSelected() && je.isDrawn()) count++;
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

        for (int i = 0; i < entries.length; i++)
            retval[i] = indexOfJobEntry(entries[i]);

        return retval;
    }

    public JobEntryCopy findStart()
    {
        for (int i = 0; i < nrJobEntries(); i++)
        {
            if (getJobEntry(i).isStart()) return getJobEntry(i);
        }
        return null;
    }

    public String toString()
    {
        if (name != null) return name;
        if (filename != null)
            return filename;
        else
            return getClass().getName();
    }

    /**
     * @return Returns the logfieldUsed.
     */
    public boolean isLogfieldUsed()
    {
        return logfieldUsed;
    }

    /**
     * @param logfieldUsed The logfieldUsed to set.
     */
    public void setLogfieldUsed(boolean logfieldUsed)
    {
        this.logfieldUsed = logfieldUsed;
    }

    /**
     * @return Returns the useBatchId.
     */
    public boolean isBatchIdUsed()
    {
        return useBatchId;
    }

    /**
     * @param useBatchId The useBatchId to set.
     */
    public void setUseBatchId(boolean useBatchId)
    {
        this.useBatchId = useBatchId;
    }

    /**
     * @return Returns the batchIdPassed.
     */
    public boolean isBatchIdPassed()
    {
        return batchIdPassed;
    }

    /**
     * @param batchIdPassed The batchIdPassed to set.
     */
    public void setBatchIdPassed(boolean batchIdPassed)
    {
        this.batchIdPassed = batchIdPassed;
    }

    /**
     * Builds a list of all the SQL statements that this transformation needs in order to work properly.
     * 
     * @return An ArrayList of SQLStatement objects.
     */
    public ArrayList getSQLStatements(Repository repository, IProgressMonitor monitor) throws KettleException
    {
        if (monitor != null) monitor.beginTask(Messages.getString("JobMeta.Monitor.GettingSQLNeededForThisJob"), nrJobEntries() + 1); //$NON-NLS-1$
        ArrayList stats = new ArrayList();

        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy copy = getJobEntry(i);
            if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.GettingSQLForJobEntryCopy") + copy + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            ArrayList list = copy.getEntry().getSQLStatements(repository);
            stats.addAll(list);
            if (monitor != null) monitor.worked(1);
        }

        // Also check the sql for the logtable...
        if (monitor != null) monitor.subTask(Messages.getString("JobMeta.Monitor.GettingSQLStatementsForJobLogTables")); //$NON-NLS-1$
        if (logconnection != null && logTable != null && logTable.length() > 0)
        {
            Database db = new Database(logconnection);
            try
            {
                db.connect();
                Row fields = Database.getJobLogrecordFields(useBatchId, logfieldUsed);
                String sql = db.getDDL(logTable, fields);
                if (sql != null && sql.length() > 0)
                {
                    SQLStatement stat = new SQLStatement(Messages.getString("JobMeta.SQLFeedback.ThisJob"), logconnection, sql); //$NON-NLS-1$
                    stats.add(stat);
                }
            }
            catch (KettleDatabaseException dbe)
            {
                SQLStatement stat = new SQLStatement(Messages.getString("JobMeta.SQLFeedback.ThisJob"), logconnection, null); //$NON-NLS-1$
                stat.setError(Messages.getString("JobMeta.SQLFeedback.ErrorObtainingJobLogTableInfo") + dbe.getMessage()); //$NON-NLS-1$
                stats.add(stat);
            }
            finally
            {
                db.disconnect();
            }
        }
        if (monitor != null) monitor.worked(1);
        if (monitor != null) monitor.done();

        return stats;
    }

    /**
     * @return Returns the logTable.
     */
    public String getLogTable()
    {
        return logTable;
    }

    /**
     * @param logTable The logTable to set.
     */
    public void setLogTable(String logTable)
    {
        this.logTable = logTable;
    }

    /**
     * @return Returns the arguments.
     */
    public String[] getArguments()
    {
        return arguments;
    }

    /**
     * @param arguments The arguments to set.
     */
    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    /**
     * Get a list of all the strings used in this job.
     *
     * @return A list of StringSearchResult with strings used in the job
     */
    public List getStringList(boolean searchSteps, boolean searchDatabases, boolean searchNotes)
    {
        ArrayList stringList = new ArrayList();

        if (searchSteps)
        {
            // Loop over all steps in the transformation and see what the used vars are...
            for (int i = 0; i < nrJobEntries(); i++)
            {
                JobEntryCopy entryMeta = getJobEntry(i);
                stringList.add(new StringSearchResult(entryMeta.getName(), entryMeta, this, Messages.getString("JobMeta.SearchMetadata.JobEntryName"))); //$NON-NLS-1$
                if (entryMeta.getDescription() != null)
                    stringList.add(new StringSearchResult(entryMeta.getDescription(), entryMeta, this, Messages.getString("JobMeta.SearchMetadata.JobEntryDescription"))); //$NON-NLS-1$
                JobEntryInterface metaInterface = entryMeta.getEntry();
                StringSearcher.findMetaData(metaInterface, 1, stringList, entryMeta, this);
            }
        }

        // Loop over all steps in the transformation and see what the used vars are...
        if (searchDatabases)
        {
            for (int i = 0; i < nrDatabases(); i++)
            {
                DatabaseMeta meta = getDatabase(i);
                stringList.add(new StringSearchResult(meta.getName(), meta, this, Messages.getString("JobMeta.SearchMetadata.DatabaseConnectionName"))); //$NON-NLS-1$
                if (meta.getDatabaseName() != null) stringList.add(new StringSearchResult(meta.getDatabaseName(), meta, this, Messages.getString("JobMeta.SearchMetadata.DatabaseName"))); //$NON-NLS-1$
                if (meta.getUsername() != null) stringList.add(new StringSearchResult(meta.getUsername(), meta, this, Messages.getString("JobMeta.SearchMetadata.DatabaseUsername"))); //$NON-NLS-1$
                if (meta.getDatabaseTypeDesc() != null)
                    stringList.add(new StringSearchResult(meta.getDatabaseTypeDesc(), meta, this, Messages.getString("JobMeta.SearchMetadata.DatabaseTypeDescription"))); //$NON-NLS-1$
                if (meta.getDatabasePortNumberString() != null)
                    stringList.add(new StringSearchResult(meta.getDatabasePortNumberString(), meta, this, Messages.getString("JobMeta.SearchMetadata.DatabasePort"))); //$NON-NLS-1$
            }
        }

        // Loop over all steps in the transformation and see what the used vars are...
        if (searchNotes)
        {
            for (int i = 0; i < nrNotes(); i++)
            {
                NotePadMeta meta = getNote(i);
                if (meta.getNote() != null) stringList.add(new StringSearchResult(meta.getNote(), meta, this, Messages.getString("JobMeta.SearchMetadata.NotepadText"))); //$NON-NLS-1$
            }
        }

        return stringList;
    }

    public List getUsedVariables()
    {
        // Get the list of Strings.
        List stringList = getStringList(true, true, false);

        List varList = new ArrayList();

        // Look around in the strings, see what we find...
        for (int i = 0; i < stringList.size(); i++)
        {
            StringSearchResult result = (StringSearchResult) stringList.get(i);
            StringUtil.getUsedVariables(result.getString(), varList, false);
        }

        return varList;
    }

    /**
     * Get an array of all the selected job entries
     *
     * @return A list containing all the selected & drawn job entries.
     */
    public List getSelectedDrawnJobEntryList()
    {
        List list = new ArrayList();

        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy jobEntryCopy = getJobEntry(i);
            if (jobEntryCopy.isDrawn() && jobEntryCopy.isSelected())
            {
                list.add(jobEntryCopy);
            }

        }
        return list;
    }

    /**
     * This method sets various internal kettle variables that can be used by the transformation.
     */
    public void setInternalKettleVariables()
    {
        KettleVariables variables = KettleVariables.getInstance();

        if (filename!=null) // we have a finename that's defined.
        {
            try
            {
                FileSystemManager fsManager = VFS.getManager();
                FileObject fileObject = fsManager.resolveFile( filename );
                FileName fileName = fileObject.getName();
                
                // The filename of the transformation
                variables.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName.getBaseName());

                // The directory of the transformation
                FileName fileDir = fileName.getParent();
                variables.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, fileDir.getURI());
            }
            catch(IOException e)
            {
                variables.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "");
                variables.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "");
            }
        }
        else
        {
            variables.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, ""); //$NON-NLS-1$
            variables.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, ""); //$NON-NLS-1$
        }

        // The name of the job
        variables.setVariable(Const.INTERNAL_VARIABLE_JOB_NAME, Const.NVL(name, "")); //$NON-NLS-1$

        // The name of the directory in the repository
        variables.setVariable(Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, directory != null ? directory.getPath() : ""); //$NON-NLS-1$
        
        // Undefine the transformation specific variables
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null);
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null);
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null);
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null);
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, null);
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, null);
    }

    public boolean haveConnectionsChanged()
    {
        if (changed_databases) return true;

        for (int i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta ci = getDatabase(i);
            if (ci.hasChanged()) return true;
        }
        return false;
    }
    
    public boolean haveJobEntriesChanged()
    {
        if (changed_entries) return true;
        
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy entry = getJobEntry(i);
            if (entry.hasChanged()) return true;
        }
        return false;
    }
    
    public boolean haveJobHopsChanged()
    {
        if (changed_hops) return true;
        
        for (int i = 0; i < nrJobHops(); i++)
        {
            JobHopMeta jobHop = getJobHop(i);
            if (jobHop.hasChanged()) return true;
        }
        return false;
    }
    
    public boolean haveNotesChanged()
    {
        if (changed_notes) return true;
        
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta note = getNote(i);
            if (note.hasChanged()) return true;
        }
        return false;
    }

    /**
     * @return the sharedObjectsFile
     */
    public String getSharedObjectsFile()
    {
        return sharedObjectsFile;
    }

    /**
     * @param sharedObjectsFile the sharedObjectsFile to set
     */
    public void setSharedObjectsFile(String sharedObjectsFile)
    {
        this.sharedObjectsFile = sharedObjectsFile;
    }

	/**
	 * @param modifiedUser The modifiedUser to set.
	 */
	public void setModifiedUser(String modified_User)
	{
		modifiedUser = modified_User;
	}

	/**
	 * @return Returns the modifiedUser.
	 */
	public String getModifiedUser()
	{
		return modifiedUser;
	}

	/**
	 * @param modifiedDate The modifiedDate to set.
	 */
	public void setModifiedDate(Value modified_Date)
	{
		modifiedDate = modified_Date;
	}

	/**
	 * @return Returns the modifiedDate.
	 */
	public Value getModifiedDate()
	{
		return modifiedDate;
	}

	/**
	 * @return The description of the job
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return The extended description of the job
	 */
	public String getExtendedDescription()
	{
		return extended_description;
	}

	/**
	 * @return The version of the job
	 */
	public String getJobversion()
	{
		return job_version;
	}


	/**
	 * Get the status of the job
	 */
	public int getJobstatus()
	{
		return job_status;
	}

	/**
	 * Set the description of the job.
	 *
	 * @param n The new description of the job
	 */
	public void setDescription(String n)
	{
		description = n;
	}

	/**
	 * Set the description of the job.
	 *
	 * @param n The new extended description of the job
	 */
	public void setExtendedDescription(String n)
	{
		extended_description = n;
	}
	/**
	 * Set the version of the job.
	 *
	 * @param n The new version description of the job
	 */
	public void setJobversion(String n)
	{
		job_version = n;
	}

	/**
	 * Set the status of the job.
	 *
	 * @param n The new status description of the job
	 */
	public void setJobstatus(int n)
	{
		job_status = n;
	}

	/**
	 * @return Returns the createdDate.
	 */
	public Value getCreatedDate()
	{
		return created_date;
	}

	/**
	 * @param createdDate The createdDate to set.
	 */
	public void setCreatedDate(Value createddate)
	{
		created_date = createddate;
	}

	/**
	 * @param createdUser The createdUser to set.
	 */
	public void setCreatedUser(String createduser)
	{
		created_user = createduser;
	}

	/**
	 * @return Returns the createdUser.
	 */
	public String getCreatedUser()
	{
		return created_user;
	}

}