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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.listeners.FilenameChangedListener;
import org.pentaho.di.core.listeners.NameChangedListener;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.reflection.StringSearcher;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Defines a Job and provides methods to load, save, verify, etc.
 * 
 * @author Matt
 * @since 11-08-2003
 * 
 */
public class JobMeta extends ChangedFlag implements Cloneable, Comparable<JobMeta>, XMLInterface, UndoInterface,
		HasDatabasesInterface, VariableSpace, EngineMetaInterface, ResourceExportInterface, HasSlaveServersInterface, NamedParams, 
		RepositoryElementInterface, LoggingObjectInterface {
	
	private static Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String XML_TAG = "job"; //$NON-NLS-1$

	private static final String XML_TAG_SLAVESERVERS = "slaveservers"; //$NON-NLS-1$
	
	public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.JOB;

	protected ObjectId objectId;
	
	private LogLevel logLevel = DefaultLogLevel.getLogLevel();
  private String containerObjectId;

	protected String name;

	protected String description;

	protected String extendedDescription;

	protected String jobVersion;

	protected int jobStatus;

	protected String filename;

	private List<JobEntryCopy> jobcopies;

	private List<JobHopMeta> jobhops;

	private List<NotePadMeta> notes;

	private List<DatabaseMeta> databases;

	private List<SlaveServer> slaveServers;

	protected RepositoryDirectoryInterface directory;

	protected String arguments[];

	protected boolean changedEntries, changedHops, changedNotes, changedDatabases;

	protected JobLogTable jobLogTable;
	protected JobEntryLogTable jobEntryLogTable;
	protected ChannelLogTable channelLogTable;

	protected List<TransAction> undo;

	private VariableSpace variables = new Variables();

	protected int max_undo;

	protected int undo_position;

	public static final int TYPE_UNDO_CHANGE = 1;

	public static final int TYPE_UNDO_NEW = 2;

	public static final int TYPE_UNDO_DELETE = 3;

	public static final int TYPE_UNDO_POSITION = 4;

	public static final String STRING_SPECIAL = "SPECIAL"; //$NON-NLS-1$

	public static final String STRING_SPECIAL_START = "START"; //$NON-NLS-1$

	public static final String STRING_SPECIAL_DUMMY = "DUMMY"; //$NON-NLS-1$

	public static final String STRING_SPECIAL_OK = "OK"; //$NON-NLS-1$

	public static final String STRING_SPECIAL_ERROR = "ERROR"; //$NON-NLS-1$

	// Remember the size and position of the different windows...
	public boolean max[] = new boolean[1];

	private String created_user, modifiedUser;

	private Date created_date, modifiedDate;


	protected boolean batchIdPassed;

	/**
	 * If this is null, we load from the default shared objects file :
	 * $KETTLE_HOME/.kettle/shared.xml
	 */
	protected String sharedObjectsFile;
	
	/** The last loaded version of the shared objects */
	private SharedObjects sharedObjects;

	private List<NameChangedListener> nameChangedListeners;

	private List<FilenameChangedListener> filenameChangedListeners;
	
    private NamedParams namedParams = new NamedParamsDefault();
    
    private static final String XML_TAG_PARAMETERS = "parameters";
    
    
	private ObjectRevision objectRevision;
	public JobMeta() {
		clear();
		initializeVariablesFrom(null);
	}

	public ObjectId getObjectId() {
		return objectId;
	}

	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	public void clear() {
		setName( null );
		setFilename( null );

		jobcopies = new ArrayList<JobEntryCopy>();
		jobhops = new ArrayList<JobHopMeta>();
		notes = new ArrayList<NotePadMeta>();
		databases = new ArrayList<DatabaseMeta>();
		slaveServers = new ArrayList<SlaveServer>();

		jobLogTable = JobLogTable.getDefault(this, this);
		channelLogTable = ChannelLogTable.getDefault(this, this);
		jobEntryLogTable = JobEntryLogTable.getDefault(this, this);
		
		arguments = null;

		max_undo = Const.MAX_UNDO;

		undo = new ArrayList<TransAction>();
		undo_position = -1;

		addDefaults();
		setChanged(false);

		created_user = "-"; //$NON-NLS-1$
		created_date = new Date();

		modifiedUser = "-"; //$NON-NLS-1$
		modifiedDate = new Date();
		directory = new RepositoryDirectory();
		description = null;
		jobStatus = -1;
		jobVersion = null;
		extendedDescription = null;
		
		// setInternalKettleVariables(); Don't clear the internal variables for
		// ad-hoc jobs, it's ruins the previews
		// etc.
	}

	public void addDefaults() {
		/*
		 * addStart(); // Add starting point! addDummy(); // Add dummy! addOK(); //
		 * errors == 0 evaluation addError(); // errors != 0 evaluation
		 */

		clearChanged();
	}

	public static final JobEntryCopy createStartEntry() {
		JobEntrySpecial jobEntrySpecial = new JobEntrySpecial(STRING_SPECIAL_START, true, false);
		JobEntryCopy jobEntry = new JobEntryCopy();
		jobEntry.setObjectId(null);
		jobEntry.setEntry(jobEntrySpecial);
		jobEntry.setLocation(50, 50);
		jobEntry.setDrawn(false);
		jobEntry.setDescription(BaseMessages.getString(PKG, "JobMeta.StartJobEntry.Description")); //$NON-NLS-1$
		return jobEntry;

	}

	public static final JobEntryCopy createDummyEntry() {
		JobEntrySpecial jobEntrySpecial = new JobEntrySpecial(STRING_SPECIAL_DUMMY, false, true);
		JobEntryCopy jobEntry = new JobEntryCopy();
		jobEntry.setObjectId(null);
		jobEntry.setEntry(jobEntrySpecial);
		jobEntry.setLocation(50, 50);
		jobEntry.setDrawn(false);
		jobEntry.setDescription(BaseMessages.getString(PKG, "JobMeta.DummyJobEntry.Description")); //$NON-NLS-1$
		return jobEntry;
	}

	public JobEntryCopy getStart() {
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy cge = getJobEntry(i);
			if (cge.isStart())
				return cge;
		}
		return null;
	}

	public JobEntryCopy getDummy() {
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy cge = getJobEntry(i);
			if (cge.isDummy())
				return cge;
		}
		return null;
	}

	/**
	 * Compares two transformation on name, filename
	 */
	public int compare(JobMeta j1, JobMeta j2) {
		if (Const.isEmpty(j1.getName()) && !Const.isEmpty(j2.getName())) return -1;
		if (!Const.isEmpty(j1.getName()) && Const.isEmpty(j2.getName())) return 1;
		if (Const.isEmpty(j1.getName()) && Const.isEmpty(j2.getName()) || j1.getName().equals(j2.getName())) {
			if (Const.isEmpty(j1.getFilename()) && !Const.isEmpty(j2.getFilename())) return -1;
			if (!Const.isEmpty(j1.getFilename()) && Const.isEmpty(j2.getFilename())) return 1;
			if (Const.isEmpty(j1.getFilename()) && Const.isEmpty(j2.getFilename())) {
				return 0;
			}
			return j1.getFilename().compareTo(j2.getFilename());
		}
		
        // Compare by name : repositories etc.
        //
        if (j1.getObjectRevision()!=null && j2.getObjectRevision()==null) return  1; 
        if (j1.getObjectRevision()==null && j2.getObjectRevision()!=null) return -1;
        int cmp;
        if (j1.getObjectRevision()==null && j2.getObjectRevision()==null) {
        	 cmp=0;
        } else {
        	cmp = j1.getObjectRevision().getName().compareTo(j2.getObjectRevision().getName());
        }
        if (cmp==0) {
        	return j1.getName().compareTo(j2.getName());
        } else {
        	return cmp;
        }
	}

	public int compareTo(JobMeta o) {
		return compare(this, o);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof JobMeta))
			return false;

		return compare(this, (JobMeta) obj) == 0;
	}

	public Object clone() {
		return realClone(true);
	}

	public Object realClone(boolean doClear) {
		try {
			JobMeta jobMeta = (JobMeta) super.clone();
			if (doClear) {
				jobMeta.clear();
			} else {
				jobMeta.jobcopies = new ArrayList<JobEntryCopy>();
				jobMeta.jobhops = new ArrayList<JobHopMeta>();
				jobMeta.notes = new ArrayList<NotePadMeta>();
				jobMeta.databases = new ArrayList<DatabaseMeta>();
				jobMeta.slaveServers = new ArrayList<SlaveServer>();
				jobMeta.namedParams = new NamedParamsDefault();
			}

			for (JobEntryCopy entry : jobcopies)
				jobMeta.jobcopies.add((JobEntryCopy) entry.clone_deep());
			for (JobHopMeta entry : jobhops)
				jobMeta.jobhops.add((JobHopMeta) entry.clone());
			for (NotePadMeta entry : notes)
				jobMeta.notes.add((NotePadMeta) entry.clone());
			for (DatabaseMeta entry : databases)
				jobMeta.databases.add((DatabaseMeta) entry.clone());
			for (SlaveServer slave : slaveServers)
				jobMeta.getSlaveServers().add((SlaveServer) slave.clone());
			for (String key : listParameters()) 
				jobMeta.addParameterDefinition(key, getParameterDefault(key), getParameterDescription(key));
			return jobMeta;
		} catch (Exception e) {
			return null;
		}
	}

	public String getName() {
		return name;
	}

  /**
   * Set the name of the job.
   * 
   * @param newName
   *          The new name of the job
   */
  public void setName(String newName) {
    fireNameChangedListeners(this.name, newName);
    this.name = newName;
    setInternalNameKettleVariable(variables);
  }
    
	/**
	 * Builds a name - if no name is set, yet - from the filename
	 */
	public void nameFromFilename() {
		if (!Const.isEmpty(filename)) {
			setName( Const.createName(filename) );
		}
	}

	/**
	 * @return Returns the directory.
	 */
	public RepositoryDirectoryInterface getRepositoryDirectory() {
		return directory;
	}

	/**
	 * @param directory
	 *            The directory to set.
	 */
	public void setRepositoryDirectory(RepositoryDirectoryInterface directory) {
		this.directory = directory;
		setInternalKettleVariables();
	}

	public String getFilename() {
		return filename;
	}

  /**
   * Set the filename of the job
   * 
   * @param newFilename
   *          The new filename of the job
   */
  public void setFilename(String newFilename) {
    fireFilenameChangedListeners(this.filename, newFilename);
    this.filename = newFilename;
    setInternalFilenameKettleVariables(variables);
  }

  public JobLogTable getJobLogTable() {
    return jobLogTable;
  }
    
    public void setJobLogTable(JobLogTable jobLogTable) {
		this.jobLogTable = jobLogTable;
	}

	/**
	 * @return Returns the databases.
	 */
	public List<DatabaseMeta> getDatabases() {
		return databases;
	}
	
	public String[] getDatabaseNames() {
		String[] names = new String[databases.size()];
		for (int i=0;i<names.length;i++) {
			names[i] = databases.get(i).getName();
		}
		return names;
	}

	/**
	 * @param databases
	 *            The databases to set.
	 */
	public void setDatabases(List<DatabaseMeta> databases) {
	  Collections.sort(databases, DatabaseMeta.comparator);
    this.databases = databases;
	}

	public void setChanged(boolean ch) {
		if (ch)
			setChanged();
		else
			clearChanged();
	}

	public void clearChanged() {
		changedEntries = false;
		changedHops = false;
		changedNotes = false;
		changedDatabases = false;

		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy entry = getJobEntry(i);
			entry.setChanged(false);
		}
		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			hi.setChanged(false);
		}
		for (int i = 0; i < nrDatabases(); i++) {
			DatabaseMeta db = getDatabase(i);
			db.setChanged(false);
		}
		for (int i = 0; i < nrNotes(); i++) {
			NotePadMeta note = getNote(i);
			note.setChanged(false);
		}
		super.clearChanged();
	}

	public boolean hasChanged() {
		if (super.hasChanged())
			return true;

		if (haveJobEntriesChanged())
			return true;
		if (haveJobHopsChanged())
			return true;
		if (haveConnectionsChanged())
			return true;
		if (haveNotesChanged())
			return true;

		return false;
	}
	
	/**
	 * This method asks all steps in the transformation whether or not the
	 * specified database connection is used. The connection is used in the
	 * transformation if any of the steps uses it or if it is being used to log
	 * to.
	 * 
	 * @param databaseMeta
	 *            The connection to check
	 * @return true if the connection is used in this transformation.
	 */
	public boolean isDatabaseConnectionUsed(DatabaseMeta databaseMeta) {
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy jobEntry = getJobEntry(i);
			DatabaseMeta dbs[] = jobEntry.getEntry().getUsedDatabaseConnections();
			for (int d = 0; d < dbs.length; d++) {
				if (dbs[d] != null && dbs[d].equals(databaseMeta))
					return true;
			}
		}

		if (jobLogTable.getDatabaseMeta()!= null && jobLogTable.getDatabaseMeta().equals(databaseMeta))
			return true;

		return false;
	}

	public String getFileType() {
		return LastUsedFile.FILE_TYPE_JOB;
	}

	public String[] getFilterNames() {
		return Const.getJobFilterNames();
	}

	public String[] getFilterExtensions() {
		return Const.STRING_JOB_FILTER_EXT;
	}

	public String getDefaultExtension() {
		return Const.STRING_JOB_DEFAULT_EXT;
	}

	public String getXML() {
		Props props = null;
		if (Props.isInitialized())
			props = Props.getInstance();

		StringBuffer retval = new StringBuffer(500);

		retval.append("<").append(XML_TAG).append(">").append(Const.CR); //$NON-NLS-1$

		retval.append("  ").append(XMLHandler.addTagValue("name", getName())); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("description", description)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("extended_description", extendedDescription));
		retval.append("    ").append(XMLHandler.addTagValue("job_version", jobVersion));
		if (jobStatus >= 0) {
			retval.append("    ").append(XMLHandler.addTagValue("job_status", jobStatus));
		}

		retval.append("  ").append(XMLHandler.addTagValue("directory", directory.getPath())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("  ").append(XMLHandler.addTagValue("created_user", created_user)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("  ").append(XMLHandler.addTagValue("created_date", XMLHandler.date2string(created_date))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("  ").append(XMLHandler.addTagValue("modified_user", modifiedUser)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("  ").append(XMLHandler.addTagValue("modified_date", XMLHandler.date2string(modifiedDate))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        retval.append("    ").append(XMLHandler.openTag(XML_TAG_PARAMETERS)).append(Const.CR); //$NON-NLS-1$
        String[] parameters = listParameters();
        for (int idx = 0; idx < parameters.length; idx++)
        {
        	retval.append("        ").append(XMLHandler.openTag("parameter")).append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
        	retval.append("            ").append(XMLHandler.addTagValue("name", parameters[idx])); //$NON-NLS-1$
        	try {
        		retval.append("            ").append(XMLHandler.addTagValue("default_value", getParameterDefault(parameters[idx]))); //$NON-NLS-1$
        		retval.append("            ").append(XMLHandler.addTagValue("description", getParameterDescription(parameters[idx]))); //$NON-NLS-1$
        	} catch (UnknownParamException e) {
				// skip the default value and/or description.  This exception should never happen because we use listParameters() above.
			}
        	retval.append("        ").append(XMLHandler.closeTag("parameter")).append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$        	
        }        
        retval.append("    ").append(XMLHandler.closeTag(XML_TAG_PARAMETERS)).append(Const.CR); //$NON-NLS-1$
				
		// Save the database connections...
		for (int i = 0; i < nrDatabases(); i++) {
			DatabaseMeta dbMeta = getDatabase(i);
			if (props != null && props.areOnlyUsedConnectionsSavedToXML()) {
				if (isDatabaseConnectionUsed(dbMeta)) {
					retval.append(dbMeta.getXML());
				}
			} else {
				retval.append(dbMeta.getXML());
			}
		}

		// The slave servers...
		//
		retval.append("    ").append(XMLHandler.openTag(XML_TAG_SLAVESERVERS)).append(Const.CR); //$NON-NLS-1$
		for (int i = 0; i < slaveServers.size(); i++) {
			SlaveServer slaveServer = slaveServers.get(i);
			retval.append("         ").append(slaveServer.getXML()).append(Const.CR);
		}
		retval.append("    ").append(XMLHandler.closeTag(XML_TAG_SLAVESERVERS)).append(Const.CR); //$NON-NLS-1$

		// Append the job logging information...
		//
		retval.append(jobLogTable.getXML());		
		retval.append(jobEntryLogTable.getXML());		
		retval.append(channelLogTable.getXML());		
        
		retval.append("   ").append(XMLHandler.addTagValue("pass_batchid", batchIdPassed)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("   ").append(XMLHandler.addTagValue("shared_objects_file", sharedObjectsFile)); // $NON-NLS-1$

		retval.append("  <entries>").append(Const.CR); //$NON-NLS-1$
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy jge = getJobEntry(i);
			retval.append(jge.getXML());
		}
		retval.append("  </entries>").append(Const.CR); //$NON-NLS-1$

		retval.append("  <hops>").append(Const.CR); //$NON-NLS-1$
		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			retval.append(hi.getXML());
		}
		retval.append("  </hops>").append(Const.CR); //$NON-NLS-1$

		retval.append("  <notepads>").append(Const.CR); //$NON-NLS-1$
		for (int i = 0; i < nrNotes(); i++) {
			NotePadMeta ni = getNote(i);
			retval.append(ni.getXML());
		}
		retval.append("  </notepads>").append(Const.CR); //$NON-NLS-1$

		retval.append("</").append(XML_TAG).append(">").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public JobMeta(String fname, Repository rep) throws KettleXMLException {
		this(null, fname, rep, null);
	}

	public JobMeta(String fname, Repository rep, OverwritePrompter prompter) throws KettleXMLException {
		this(null, fname, rep, prompter);
	}

	/**
	 * Load the job from the XML file specified.
	 * 
	 * @param log
	 *            the logging channel
	 * @param fname
	 *            The filename to load as a job
	 * @param rep
	 *            The repository to bind againt, null if there is no repository
	 *            available.
	 * @throws KettleXMLException
	 */
	public JobMeta(VariableSpace parentSpace, String fname, Repository rep, OverwritePrompter prompter)
			throws KettleXMLException {
		this.initializeVariablesFrom(parentSpace);
		try {
			// OK, try to load using the VFS stuff...
			Document doc = XMLHandler.loadXMLFile(KettleVFS.getFileObject(fname, this));
			if (doc != null) {
				// Clear the job
				clear();

				// The jobnode
				Node jobnode = XMLHandler.getSubNode(doc, XML_TAG);

				loadXML(jobnode, rep, prompter);

				// Do this at the end
				setFilename(fname);
			} else {
				throw new KettleXMLException(BaseMessages.getString(PKG, "JobMeta.Exception.ErrorReadingFromXMLFile") + fname); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobMeta.Exception.UnableToLoadJobFromXMLFile") + fname + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public JobMeta(InputStream inputStream, Repository rep, OverwritePrompter prompter) throws KettleXMLException {
	  loadXML(XMLHandler.loadXMLFile(inputStream, null, false, false), rep, prompter);
	}

  /**
   * Create a new JobMeta object by loading it from a a DOM node.   
   * 
   * @param jobnode The node to load from
   * @param rep The reference to a repository to load additional information from
   * @param prompter The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
	public JobMeta(Node jobnode, Repository rep, OverwritePrompter prompter) throws KettleXMLException {
		loadXML(jobnode, rep, false, prompter);
	}

	 /**
   * Create a new JobMeta object by loading it from a a DOM node.   
   * 
   * @param jobnode The node to load from
   * @param rep The reference to a repository to load additional information from
   * @param ignoreRepositorySharedObjects Do not load shared objects, handled separately
   * @param prompter The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public JobMeta(Node jobnode, Repository rep, boolean ignoreRepositorySharedObjects, OverwritePrompter prompter) throws KettleXMLException {
	   loadXML(jobnode, rep, ignoreRepositorySharedObjects, prompter);
	}

	public boolean isRepReference() {
		return isRepReference(getFilename(), this.getName());
	}

	public boolean isFileReference() {
		return !isRepReference(getFilename(), this.getName());
	}

	public static boolean isRepReference(String fileName, String transName) {
		return Const.isEmpty(fileName) && !Const.isEmpty(transName);
	}

	public static boolean isFileReference(String fileName, String transName) {
		return !isRepReference(fileName, transName);
	}

	public void loadXML(Node jobnode, Repository rep, OverwritePrompter prompter) throws KettleXMLException {
	  loadXML(jobnode, rep, false, prompter);
  }

	/**
	 * Load a block of XML from an DOM node.   
	 * 
	 * @param jobnode The node to load from
	 * @param rep The reference to a repository to load additional information from
	 * @param ignoreRepositorySharedObjects Do not load shared objects, handled separately
	 * @param prompter The prompter to use in case a shared object gets overwritten
	 * @throws KettleXMLException
	 */
	public void loadXML(Node jobnode, Repository rep, boolean ignoreRepositorySharedObjects, OverwritePrompter prompter) throws KettleXMLException {
		Props props = null;
		if (Props.isInitialized())
			props = Props.getInstance();

		try {
			// clear the jobs;
			clear();

			//
			// get job info:
			//
			setName( XMLHandler.getTagValue(jobnode, "name") ); //$NON-NLS-1$
			
			// Optionally load the repository directory...
			//
			if (rep!=null) {
				String directoryPath = XMLHandler.getTagValue(jobnode, "directory");
				if (directoryPath!=null) {
					directory = rep.findDirectory(directoryPath);
					if (directory==null) { // not found
						directory = new RepositoryDirectory(); // The root as default
					}
				}
			}

			// description
			description = XMLHandler.getTagValue(jobnode, "description");

			// extended description
			extendedDescription = XMLHandler.getTagValue(jobnode, "extended_description");

			// job version
			jobVersion = XMLHandler.getTagValue(jobnode, "job_version");

			// job status
			jobStatus = Const.toInt(XMLHandler.getTagValue(jobnode, "job_status"), -1);

			// Created user/date
			created_user = XMLHandler.getTagValue(jobnode, "created_user"); //$NON-NLS-1$
			String createDate = XMLHandler.getTagValue(jobnode, "created_date"); //$NON-NLS-1$

			if (createDate != null) {
				created_date = XMLHandler.stringToDate(createDate);
			}

			// Changed user/date
			modifiedUser = XMLHandler.getTagValue(jobnode, "modified_user"); //$NON-NLS-1$
			String modDate = XMLHandler.getTagValue(jobnode, "modified_date"); //$NON-NLS-1$
			if (modDate != null) {
				modifiedDate = XMLHandler.stringToDate(modDate);
			}

			// Load the default list of databases
			// Read objects from the shared XML file & the repository
			try {
				sharedObjectsFile = XMLHandler.getTagValue(jobnode, "shared_objects_file"); //$NON-NLS-1$ //$NON-NLS-2$
				if (rep==null || ignoreRepositorySharedObjects) {
				  sharedObjects = readSharedObjects();
				} else {
				  sharedObjects = rep.readJobMetaSharedObjects(this);
				}
			} catch (Exception e) {
				throw new KettleXMLException(BaseMessages.getString(PKG, "JobMeta.ErrorReadingSharedObjects.Message"), e); // $NON-NLS-1$ // //$NON-NLS-1$
			}

            // Read the named parameters.
            Node paramsNode = XMLHandler.getSubNode(jobnode, XML_TAG_PARAMETERS);
            int nrParams = XMLHandler.countNodes(paramsNode, "parameter"); //$NON-NLS-1$

            for (int i = 0; i < nrParams; i++)
            {
                Node paramNode = XMLHandler.getSubNodeByNr(paramsNode, "parameter", i); //$NON-NLS-1$

                String paramName = XMLHandler.getTagValue(paramNode, "name"); //$NON-NLS-1$
                String defValue = XMLHandler.getTagValue(paramNode, "default_value"); //$NON-NLS-1$
                String descr = XMLHandler.getTagValue(paramNode, "description"); //$NON-NLS-1$
                
                addParameterDefinition(paramName, defValue, descr);
            }            			
			
			// 
			// Read the database connections
			//
			int nr = XMLHandler.countNodes(jobnode, "connection"); //$NON-NLS-1$
			for (int i = 0; i < nr; i++) {
				Node dbnode = XMLHandler.getSubNodeByNr(jobnode, "connection", i); //$NON-NLS-1$
				DatabaseMeta dbcon = new DatabaseMeta(dbnode);
				dbcon.shareVariablesWith(this);

				DatabaseMeta exist = findDatabase(dbcon.getName());
				if (exist == null) {
					addDatabase(dbcon);
				} 
				else 
				{
					if (!exist.isShared()) // skip shared connections
					{
						boolean askOverwrite = Props.isInitialized() ? props.askAboutReplacingDatabaseConnections() : false;
						boolean overwrite = Props.isInitialized() ? props.replaceExistingDatabaseConnections() : true;
						if (askOverwrite && prompter != null) 
						{
							overwrite = prompter.overwritePrompt(
									BaseMessages.getString(PKG, "JobMeta.Dialog.ConnectionExistsOverWrite.Message", dbcon.getName()), 
									BaseMessages.getString(PKG, "JobMeta.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage"),
									Props.STRING_ASK_ABOUT_REPLACING_DATABASES
								);
						}
	
						if (overwrite) {
							int idx = indexOfDatabase(exist);
							removeDatabase(idx);
							addDatabase(idx, dbcon);
						}
					}
				}
			}

			// Read the slave servers...
			// 
			Node slaveServersNode = XMLHandler.getSubNode(jobnode, XML_TAG_SLAVESERVERS); //$NON-NLS-1$
			int nrSlaveServers = XMLHandler.countNodes(slaveServersNode, SlaveServer.XML_TAG); //$NON-NLS-1$
			for (int i = 0; i < nrSlaveServers; i++) {
				Node slaveServerNode = XMLHandler.getSubNodeByNr(slaveServersNode, SlaveServer.XML_TAG, i);
				SlaveServer slaveServer = new SlaveServer(slaveServerNode);
                slaveServer.shareVariablesWith(this);

				// Check if the object exists and if it's a shared object.
				// If so, then we will keep the shared version, not this one.
				// The stored XML is only for backup purposes.
				SlaveServer check = findSlaveServer(slaveServer.getName());
				if (check != null) {
					if (!check.isShared()) // we don't overwrite shared
											// objects.
					{
						addOrReplaceSlaveServer(slaveServer);
					}
				} else {
					slaveServers.add(slaveServer);
				}
			}

			/*
			 * Get the log database connection & log table
			 */
			// Backward compatibility...
			//
			Node jobLogNode = XMLHandler.getSubNode(jobnode, JobLogTable.XML_TAG);
			if (jobLogNode==null) {
				// Load the XML
				//
				jobLogTable.setConnectionName( XMLHandler.getTagValue(jobnode, "logconnection") );
				jobLogTable.setTableName( XMLHandler.getTagValue(jobnode, "logtable") );
				jobLogTable.setBatchIdUsed( "Y".equalsIgnoreCase(XMLHandler.getTagValue(jobnode, "use_batchid")) );
				jobLogTable.setLogFieldUsed( "Y".equalsIgnoreCase(XMLHandler.getTagValue(jobnode, "use_logfield")) );
				jobLogTable.findField(JobLogTable.ID.CHANNEL_ID).setEnabled(false);
                jobLogTable.findField(JobLogTable.ID.LINES_REJECTED).setEnabled(false);
			} else {
				jobLogTable.loadXML(jobLogNode, databases);
			}
			Node channelLogNode = XMLHandler.getSubNode(jobnode, ChannelLogTable.XML_TAG);
			if (channelLogNode!=null) {
				channelLogTable.loadXML(channelLogNode, databases);
			}
			Node jobEntryLogNode = XMLHandler.getSubNode(jobnode, JobEntryLogTable.XML_TAG);
			if (jobEntryLogNode!=null) {
				jobEntryLogTable.loadXML(jobEntryLogNode, databases);
			}

			batchIdPassed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(jobnode, "pass_batchid")); //$NON-NLS-1$ //$NON-NLS-2$

			/*
			 * read the job entries...
			 */
			Node entriesnode = XMLHandler.getSubNode(jobnode, "entries"); //$NON-NLS-1$
			int tr = XMLHandler.countNodes(entriesnode, "entry"); //$NON-NLS-1$
			for (int i = 0; i < tr; i++) {
				Node entrynode = XMLHandler.getSubNodeByNr(entriesnode, "entry", i); //$NON-NLS-1$
				// System.out.println("Reading entry:\n"+entrynode);

				JobEntryCopy je = new JobEntryCopy(entrynode, databases, slaveServers, rep);
				JobEntryCopy prev = findJobEntry(je.getName(), 0, true);
				if (prev != null) {
					// See if the #0 (root entry) already exists!
					//
					if (je.getNr() == 0) {
						
						// Replace previous version with this one: remove it first
						//
						int idx = indexOfJobEntry(prev);
						removeJobEntry(idx);
						
					} else if (je.getNr() > 0) {
						
						// Use previously defined JobEntry info!
						//
						je.setEntry(prev.getEntry());

						// See if entry already exists...
						prev = findJobEntry(je.getName(), je.getNr(), true);
						if (prev != null) {
							// remove the old one!
							//
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
			for (int i = 0; i < ho; i++) {
				Node hopnode = XMLHandler.getSubNodeByNr(hopsnode, "hop", i); //$NON-NLS-1$
				JobHopMeta hi = new JobHopMeta(hopnode, this);
				jobhops.add(hi);
			}

			// Read the notes...
			Node notepadsnode = XMLHandler.getSubNode(jobnode, "notepads"); //$NON-NLS-1$
			int nrnotes = XMLHandler.countNodes(notepadsnode, "notepad"); //$NON-NLS-1$
			for (int i = 0; i < nrnotes; i++) {
				Node notepadnode = XMLHandler.getSubNodeByNr(notepadsnode, "notepad", i); //$NON-NLS-1$
				NotePadMeta ni = new NotePadMeta(notepadnode);
				notes.add(ni);
			}

			clearChanged();
		} catch (Exception e) {
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobMeta.Exception.UnableToLoadJobFromXMLNode"), e); //$NON-NLS-1$
		} finally {
			setInternalKettleVariables();
		}
	}

	public SharedObjects readSharedObjects() throws KettleException {
		// Extract the shared steps, connections, etc. using the SharedObjects
		// class
		//
		String soFile = environmentSubstitute(sharedObjectsFile);
		SharedObjects sharedObjects = new SharedObjects(soFile);
		Map<?, SharedObjectInterface> objectsMap = sharedObjects.getObjectsMap();

		// First read the databases...
		// We read databases & slaves first because there might be dependencies
		// that need to be resolved.
		//
		for (SharedObjectInterface object : objectsMap.values()) {
			if (object instanceof DatabaseMeta) {
				DatabaseMeta databaseMeta = (DatabaseMeta) object;
                databaseMeta.shareVariablesWith(this);
				addOrReplaceDatabase(databaseMeta);
			} else if (object instanceof SlaveServer) {
				SlaveServer slaveServer = (SlaveServer) object;
                slaveServer.shareVariablesWith(this);
				addOrReplaceSlaveServer(slaveServer);
			}
		}
		
		return sharedObjects;
	}	
	
	public void saveSharedObjects() throws KettleException {
		try {
			// First load all the shared objects...
			String soFile = environmentSubstitute(sharedObjectsFile);
			SharedObjects sharedObjects = new SharedObjects(soFile);

			// Now overwrite the objects in there
			List<Object> shared = new ArrayList<Object>();
			shared.addAll(databases);
			shared.addAll(slaveServers);

			// The databases connections...
			for (int i = 0; i < shared.size(); i++) {
				SharedObjectInterface sharedObject = (SharedObjectInterface) shared.get(i);
				if (sharedObject.isShared()) {
					sharedObjects.storeObject(sharedObject);
				}
			}

			// Save the objects
			sharedObjects.saveToFile();
		} catch (Exception e) {
			throw new KettleException("Unable to save shared ojects", e);
		}
	}

	/**
	 * Find a database connection by it's name
	 * 
	 * @param name
	 *            The database name to look for
	 * @return The database connection or null if nothing was found.
	 */
	public DatabaseMeta findDatabase(String name) {
		for (int i = 0; i < nrDatabases(); i++) {
			DatabaseMeta ci = getDatabase(i);
			if (ci.getName().equalsIgnoreCase(name)) {
				return ci;
			}
		}
		return null;
	}




	public JobEntryCopy getJobEntryCopy(int x, int y, int iconsize) {
		int i, s;
		s = nrJobEntries();
		for (i = s - 1; i >= 0; i--) // Back to front because drawing goes
										// from start to end
		{
			JobEntryCopy je = getJobEntry(i);
			Point p = je.getLocation();
			if (p != null) {
				if (x >= p.x && x <= p.x + iconsize && y >= p.y && y <= p.y + iconsize) {
					return je;
				}
			}
		}
		return null;
	}

	public int nrJobEntries() {
		return jobcopies.size();
	}

	public int nrJobHops() {
		return jobhops.size();
	}

	public int nrNotes() {
		return notes.size();
	}

	public int nrDatabases() {
		return databases.size();
	}

	public JobHopMeta getJobHop(int i) {
		return jobhops.get(i);
	}

	public JobEntryCopy getJobEntry(int i) {
		return jobcopies.get(i);
	}

	public NotePadMeta getNote(int i) {
		return notes.get(i);
	}

	public DatabaseMeta getDatabase(int i) {
		return databases.get(i);
	}

	public void addJobEntry(JobEntryCopy je) {
		jobcopies.add(je);
		je.setParentJobMeta(this);
		setChanged();
	}

	public void addJobHop(JobHopMeta hi) {
		jobhops.add(hi);
		setChanged();
	}

	public void addNote(NotePadMeta ni) {
		notes.add(ni);
		setChanged();
	}

	public void addDatabase(DatabaseMeta ci) {
	  databases.add(ci);
	  Collections.sort(databases, DatabaseMeta.comparator);
		changedDatabases = true;
	}

	public void addJobEntry(int p, JobEntryCopy si) {
		jobcopies.add(p, si);
		changedEntries = true;
	}

	public void addJobHop(int p, JobHopMeta hi) {
		jobhops.add(p, hi);
		changedHops = true;
	}

	public void addNote(int p, NotePadMeta ni) {
		notes.add(p, ni);
		changedNotes = true;
	}

	public void addDatabase(int p, DatabaseMeta ci) {
		databases.add(p, ci);
		changedDatabases = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pentaho.di.trans.HasDatabaseInterface#addOrReplaceDatabase(org.pentaho.di.core.database.DatabaseMeta)
	 */
	public void addOrReplaceDatabase(DatabaseMeta databaseMeta) {
		int index = databases.indexOf(databaseMeta);
		if (index < 0) {
			addDatabase(databaseMeta);
		} else {
			DatabaseMeta previous = getDatabase(index);
			previous.replaceMeta(databaseMeta);
		}
		changedDatabases = true;
	}

	/**
	 * Add a new slave server to the transformation if that didn't exist yet.
	 * Otherwise, replace it.
	 * 
	 * @param slaveServer
	 *            The slave server to be added.
	 */
	public void addOrReplaceSlaveServer(SlaveServer slaveServer) {
		int index = slaveServers.indexOf(slaveServer);
		if (index < 0) {
			slaveServers.add(slaveServer);
		} else {
			SlaveServer previous = slaveServers.get(index);
			previous.replaceMeta(slaveServer);
		}
		setChanged();
	}

	public void removeJobEntry(int i) {
		jobcopies.remove(i);
		setChanged();
	}

	public void removeJobHop(int i) {
		jobhops.remove(i);
		setChanged();
	}

	public void removeNote(int i) {
		notes.remove(i);
		setChanged();
	}

	public void raiseNote(int p) {
		// if valid index and not last index
		if ((p >= 0) && (p < notes.size() - 1)) {
			NotePadMeta note = notes.remove(p);
			notes.add(note);
			changedNotes = true;
		}
	}

	public void lowerNote(int p) {
		// if valid index and not first index
		if ((p > 0) && (p < notes.size())) {
			NotePadMeta note = notes.remove(p);
			notes.add(0, note);
			changedNotes = true;
		}
	}

	public void removeDatabase(int i) {
		if (i < 0 || i >= databases.size())
			return;
		databases.remove(i);
		changedDatabases = true;
	}

	public int indexOfJobHop(JobHopMeta he) {
		return jobhops.indexOf(he);
	}

	public int indexOfNote(NotePadMeta ni) {
		return notes.indexOf(ni);
	}

	public int indexOfJobEntry(JobEntryCopy ge) {
		return jobcopies.indexOf(ge);
	}

	public int indexOfDatabase(DatabaseMeta di) {
		return databases.indexOf(di);
	}

	public void setJobEntry(int idx, JobEntryCopy jec) {
		jobcopies.set(idx, jec);
	}

	/**
	 * Find an existing JobEntryCopy by it's name and number
	 * 
	 * @param name
	 *            The name of the job entry copy
	 * @param nr
	 *            The number of the job entry copy
	 * @return The JobEntryCopy or null if nothing was found!
	 */
	public JobEntryCopy findJobEntry(String name, int nr, boolean searchHiddenToo) {
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy jec = getJobEntry(i);
			if (jec.getName().equalsIgnoreCase(name) && jec.getNr() == nr) {
				if (searchHiddenToo || jec.isDrawn()) {
					return jec;
				}
			}
		}
		return null;
	}

	public JobEntryCopy findJobEntry(String full_name_nr) {
		int i;
		for (i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy jec = getJobEntry(i);
			JobEntryInterface je = jec.getEntry();
			if (je.toString().equalsIgnoreCase(full_name_nr)) {
				return jec;
			}
		}
		return null;
	}

	public JobHopMeta findJobHop(String name) {
		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			if (hi.toString().equalsIgnoreCase(name)) {
				return hi;
			}
		}
		return null;
	}

	public JobHopMeta findJobHopFrom(JobEntryCopy jge) {
		if (jge != null) {
			for (JobHopMeta hi : jobhops) {
				
				// Return the first we find!
				//
				if (hi != null && (hi.getFromEntry() != null) && hi.getFromEntry().equals(jge))
				{
					return hi;
				}
			}
		}
		return null;
	}

	public JobHopMeta findJobHop(JobEntryCopy from, JobEntryCopy to) {
	  return findJobHop(from, to, false);
	}
	
	public JobHopMeta findJobHop(JobEntryCopy from, JobEntryCopy to, boolean includeDisabled) {
		for (JobHopMeta hi : jobhops) {
			if (hi.isEnabled() || includeDisabled) {
				if (hi != null && hi.getFromEntry() != null && hi.getToEntry() != null && hi.getFromEntry().equals(from)
						&& hi.getToEntry().equals(to)) {
					return hi;
				}
			}
		}
		return null;
	}
	
	public JobHopMeta findJobHopTo(JobEntryCopy jge) {
		for (JobHopMeta hi : jobhops) {
			if (hi != null && hi.getToEntry() != null && hi.getToEntry().equals(jge)) // Return
																				// the
																				// first!
			{
				return hi;
			}
		}
		return null;
	}

	public int findNrPrevJobEntries(JobEntryCopy from) {
		return findNrPrevJobEntries(from, false);
	}

	public JobEntryCopy findPrevJobEntry(JobEntryCopy to, int nr) {
		return findPrevJobEntry(to, nr, false);
	}

	public int findNrPrevJobEntries(JobEntryCopy to, boolean info) {
		int count = 0;

		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			if (hi.isEnabled() && hi.getToEntry().equals(to)) {
				count++;
			}
		}
		return count;
	}

	public JobEntryCopy findPrevJobEntry(JobEntryCopy to, int nr, boolean info) {
		int count = 0;

		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			if (hi.isEnabled() && hi.getToEntry().equals(to)) {
				if (count == nr) {
					return hi.getFromEntry();
				}
				count++;
			}
		}
		return null;
	}

	public int findNrNextJobEntries(JobEntryCopy from) {
		int count = 0;
		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			if (hi.isEnabled() && (hi.getFromEntry() != null) && hi.getFromEntry().equals(from))
				count++;
		}
		return count;
	}

	public JobEntryCopy findNextJobEntry(JobEntryCopy from, int cnt) {
		int count = 0;

		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			if (hi.isEnabled() && (hi.getFromEntry() != null) && hi.getFromEntry().equals(from)) {
				if (count == cnt) {
					return hi.getToEntry();
				}
				count++;
			}
		}
		return null;
	}

	public boolean hasLoop(JobEntryCopy entry) {
		return hasLoop(entry, null);
	}

	public boolean hasLoop(JobEntryCopy entry, JobEntryCopy lookup) {
		return false;
	}

	public boolean isEntryUsedInHops(JobEntryCopy jge) {
		JobHopMeta fr = findJobHopFrom(jge);
		JobHopMeta to = findJobHopTo(jge);
		if (fr != null || to != null)
			return true;
		return false;
	}

	public int countEntries(String name) {
		int count = 0;
		int i;
		for (i = 0; i < nrJobEntries(); i++) // Look at all the hops;
		{
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name))
				count++;
		}
		return count;
	}

	public int findUnusedNr(String name) {
		int nr = 1;
		JobEntryCopy je = findJobEntry(name, nr, true);
		while (je != null) {
			nr++;
			// log.logDebug("findUnusedNr()", "Trying unused nr: "+nr);
			je = findJobEntry(name, nr, true);
		}
		return nr;
	}

	public int findMaxNr(String name) {
		int max = 0;
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name)) {
				if (je.getNr() > max)
					max = je.getNr();
			}
		}
		return max;
	}

	/**
	 * Proposes an alternative job entry name when the original already
	 * exists...
	 * 
	 * @param entryname
	 *            The job entry name to find an alternative for..
	 * @return The alternative stepname.
	 */
	public String getAlternativeJobentryName(String entryname) {
		String newname = entryname;
		JobEntryCopy jec = findJobEntry(newname);
		int nr = 1;
		while (jec != null) {
			nr++;
			newname = entryname + " " + nr; //$NON-NLS-1$
			jec = findJobEntry(newname);
		}

		return newname;
	}

	public JobEntryCopy[] getAllJobGraphEntries(String name) {
		int count = 0;
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name))
				count++;
		}
		JobEntryCopy retval[] = new JobEntryCopy[count];

		count = 0;
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy je = getJobEntry(i);
			if (je.getName().equalsIgnoreCase(name)) {
				retval[count] = je;
				count++;
			}
		}
		return retval;
	}

	public JobHopMeta[] getAllJobHopsUsing(String name) {
		List<JobHopMeta> hops = new ArrayList<JobHopMeta>();

		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			if (hi.getFromEntry() != null && hi.getToEntry() != null) {
				if (hi.getFromEntry().getName().equalsIgnoreCase(name) || hi.getToEntry().getName().equalsIgnoreCase(name)) {
					hops.add(hi);
				}
			}
		}
		return hops.toArray(new JobHopMeta[hops.size()]);
	}

	public NotePadMeta getNote(int x, int y) {
		int i, s;
		s = notes.size();
		for (i = s - 1; i >= 0; i--) // Back to front because drawing goes
										// from start to end
		{
			NotePadMeta ni = notes.get(i);
			Point loc = ni.getLocation();
			Point p = new Point(loc.x, loc.y);
			if (x >= p.x && x <= p.x + ni.width + 2 * Const.NOTE_MARGIN && y >= p.y
					&& y <= p.y + ni.height + 2 * Const.NOTE_MARGIN) {
				return ni;
			}
		}
		return null;
	}

	public void selectAll() {
		int i;
		for (i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy ce = getJobEntry(i);
			ce.setSelected(true);
		}
		for (i = 0; i < nrNotes(); i++)
    {
        NotePadMeta ni = getNote(i);
        ni.setSelected(true);
    }
		setChanged();
		notifyObservers("refreshGraph");
	}

	public void unselectAll() {
		int i;
		for (i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy ce = getJobEntry(i);
			ce.setSelected(false);
		}
    for (i = 0; i < nrNotes(); i++)
    {
        NotePadMeta ni = getNote(i);
        ni.setSelected(false);
    }
	}

	public int getMaxUndo() {
		return max_undo;
	}

	public void setMaxUndo(int mu) {
		max_undo = mu;
		while (undo.size() > mu && undo.size() > 0)
			undo.remove(0);
	}

	public int getUndoSize() {
		if (undo == null)
			return 0;
		return undo.size();
	}

	public void clearUndo() {
		undo = new ArrayList<TransAction>();
		undo_position = -1;
	}

	public void addUndo(Object from[], Object to[], int pos[], Point prev[], Point curr[], int type_of_change, boolean nextAlso) {
		// First clean up after the current position.
		// Example: position at 3, size=5
		// 012345
		// ^
		// remove 34
		// Add 4
		// 01234

		while (undo.size() > undo_position + 1 && undo.size() > 0) {
			int last = undo.size() - 1;
			undo.remove(last);
		}

		TransAction ta = new TransAction();
		switch (type_of_change) {
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

		if (undo.size() > max_undo) {
			undo.remove(0);
			undo_position--;
		}
	}

	// get previous undo, change position
	public TransAction previousUndo() {
		if (undo.isEmpty() || undo_position < 0)
			return null; // No undo left!

		TransAction retval = undo.get(undo_position);

		undo_position--;

		return retval;
	}

	/**
	 * View current undo, don't change undo position
	 * 
	 * @return The current undo transaction
	 */
	public TransAction viewThisUndo() {
		if (undo.isEmpty() || undo_position < 0)
			return null; // No undo left!

		TransAction retval = undo.get(undo_position);

		return retval;
	}

	// View previous undo, don't change position
	public TransAction viewPreviousUndo() {
		if (undo.isEmpty() || undo_position < 0)
			return null; // No undo left!

		TransAction retval = undo.get(undo_position);

		return retval;
	}

	public TransAction nextUndo() {
		int size = undo.size();
		if (size == 0 || undo_position >= size - 1)
			return null; // no redo left...

		undo_position++;

		TransAction retval = undo.get(undo_position);

		return retval;
	}

	public TransAction viewNextUndo() {
		int size = undo.size();
		if (size == 0 || undo_position >= size - 1)
			return null; // no redo left...

		TransAction retval = undo.get(undo_position + 1);

		return retval;
	}

	public Point getMaximum() {
		int maxx = 0, maxy = 0;
		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy entry = getJobEntry(i);
			Point loc = entry.getLocation();
			if (loc.x > maxx)
				maxx = loc.x;
			if (loc.y > maxy)
				maxy = loc.y;
		}
		for (int i = 0; i < nrNotes(); i++) {
			NotePadMeta ni = getNote(i);
			Point loc = ni.getLocation();
			if (loc.x + ni.width > maxx)
				maxx = loc.x + ni.width;
			if (loc.y + ni.height > maxy)
				maxy = loc.y + ni.height;
		}

		return new Point(maxx + 100, maxy + 100);
	}

    /**
     * Get the minimum point on the canvas of a job
     * @return Minimum coordinate of a step in the job
     */
    public Point getMinimum()
    {
        int minx= Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        for (int i = 0; i < nrJobEntries(); i++)
        {
            JobEntryCopy jobEntryCopy = getJobEntry(i);
            Point loc = jobEntryCopy.getLocation();
            if (loc.x < minx) minx = loc.x;
            if (loc.y < miny) miny = loc.y;
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta notePadMeta = getNote(i);
            Point loc = notePadMeta.getLocation();
            if (loc.x < minx) minx = loc.x;
            if (loc.y < miny) miny = loc.y;
        }
        
        if (minx>20) minx-=20; else minx=0;
        if (miny>20) miny-=20; else miny=0;

        return new Point(minx, miny);
    }

	public Point[] getSelectedLocations() {
		List<JobEntryCopy> selectedEntries = getSelectedEntries();
		Point retval[] = new Point[selectedEntries.size()];
		for (int i = 0; i < retval.length; i++) {
			JobEntryCopy si = selectedEntries.get(i);
			Point p = si.getLocation();
			retval[i] = new Point(p.x, p.y); // explicit copy of location
		}
		return retval;
	}
	
    /**
     * Get all the selected note locations
     *
     * @return The selected step and notes locations.
     */
    public Point[] getSelectedNoteLocations()
    {
        List<Point> points = new ArrayList<Point>();

        for (NotePadMeta ni : getSelectedNotes())
        {
            Point p = ni.getLocation();
            points.add(new Point(p.x, p.y)); // explicit copy of location
        }

        return points.toArray(new Point[points.size()]);
    }

	public List<JobEntryCopy> getSelectedEntries() {
		List<JobEntryCopy> selection = new ArrayList<JobEntryCopy>();
		for (JobEntryCopy je : jobcopies) {
			if (je.isSelected()) {
				selection.add(je);
			}
		}
		return selection;
	}
	
    /**
     * @return A list of all the selected notes.
     */
    public List<NotePadMeta> getSelectedNotes()
    {
    	List<NotePadMeta> selection =new ArrayList<NotePadMeta>();
        for (NotePadMeta note : notes) {
            if (note.isSelected()) {
            	selection.add(note);
            }
        }
        return selection;
    }

	public int[] getEntryIndexes(List<JobEntryCopy> entries) {
		int retval[] = new int[entries.size()];

		for (int i = 0; i < entries.size(); i++)
			retval[i] = indexOfJobEntry(entries.get(i));

		return retval;
	}

    /**
     * Get an array of the locations of an array of notes
     *
     * @param notes An array of notes
     * @return an array of the locations of an array of notes
     */
    public int[] getNoteIndexes(List<NotePadMeta> notes)
    {
        int retval[] = new int[notes.size()];

        for (int i = 0; i < notes.size(); i++)
            retval[i] = indexOfNote(notes.get(i));

        return retval;
    }

	public JobEntryCopy findStart() {
		for (int i = 0; i < nrJobEntries(); i++) {
			if (getJobEntry(i).isStart())
				return getJobEntry(i);
		}
		return null;
	}

	public String toString() {
        if (!Const.isEmpty(filename)) {
        	if (Const.isEmpty(name)) {
        		return filename;
        	} else {
        		return filename+" : "+name;
        	}
        }

        if (name != null) {
        	if (directory!=null) {
        		String path = directory.getPath();
        		if (path.endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
        			return path+name;
        		} else {
        			return path+RepositoryDirectory.DIRECTORY_SEPARATOR+name;
        		}
        	} else {
        		return name;
        	}
        } else {
        	return JobMeta.class.getName();
        }
	}

	/**
	 * @return Returns the batchIdPassed.
	 */
	public boolean isBatchIdPassed() {
		return batchIdPassed;
	}

	/**
	 * @param batchIdPassed
	 *            The batchIdPassed to set.
	 */
	public void setBatchIdPassed(boolean batchIdPassed) {
		this.batchIdPassed = batchIdPassed;
	}

	/**
	 * Builds a list of all the SQL statements that this transformation needs in
	 * order to work properly.
	 * 
	 * @return An ArrayList of SQLStatement objects.
	 */
	public List<SQLStatement> getSQLStatements(Repository repository, ProgressMonitorListener monitor) throws KettleException {
		if (monitor != null)
			monitor.beginTask(BaseMessages.getString(PKG, "JobMeta.Monitor.GettingSQLNeededForThisJob"), nrJobEntries() + 1); //$NON-NLS-1$
		List<SQLStatement> stats = new ArrayList<SQLStatement>();

		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy copy = getJobEntry(i);
			if (monitor != null)
				monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.GettingSQLForJobEntryCopy") + copy + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			List<SQLStatement> list = copy.getEntry().getSQLStatements(repository, this);
			stats.addAll(list);
			if (monitor != null)
				monitor.worked(1);
		}

		// Also check the sql for the logtable...
		if (monitor != null)
			monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.GettingSQLStatementsForJobLogTables")); //$NON-NLS-1$
		if (jobLogTable.getDatabaseMeta()!=null && !Const.isEmpty(jobLogTable.getTableName())) {
			Database db = new Database(this, jobLogTable.getDatabaseMeta());
			try {
				db.connect();
				RowMetaInterface fields = jobLogTable.getLogRecord(LogStatus.START, null, null).getRowMeta();
				String sql = db.getDDL(jobLogTable.getTableName(), fields);
				if (sql != null && sql.length() > 0) {
					SQLStatement stat = new SQLStatement(BaseMessages.getString(PKG, "JobMeta.SQLFeedback.ThisJob"), jobLogTable.getDatabaseMeta(), sql); //$NON-NLS-1$
					stats.add(stat);
				}
			} catch (KettleDatabaseException dbe) {
				SQLStatement stat = new SQLStatement(BaseMessages.getString(PKG, "JobMeta.SQLFeedback.ThisJob"), jobLogTable.getDatabaseMeta(), null); //$NON-NLS-1$
				stat.setError(BaseMessages.getString(PKG, "JobMeta.SQLFeedback.ErrorObtainingJobLogTableInfo") + dbe.getMessage()); //$NON-NLS-1$
				stats.add(stat);
			} finally {
				db.disconnect();
			}
		}
		if (monitor != null)
			monitor.worked(1);
		if (monitor != null)
			monitor.done();

		return stats;
	}

	/**
	 * @return Returns the arguments.
	 */
	public String[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            The arguments to set.
	 */
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	/**
	 * Get a list of all the strings used in this job.
	 * 
	 * @return A list of StringSearchResult with strings used in the job
	 */
	public List<StringSearchResult> getStringList(boolean searchSteps, boolean searchDatabases, boolean searchNotes) {
		List<StringSearchResult> stringList = new ArrayList<StringSearchResult>();

		if (searchSteps) {
			// Loop over all steps in the transformation and see what the used
			// vars are...
			for (int i = 0; i < nrJobEntries(); i++) {
				JobEntryCopy entryMeta = getJobEntry(i);
				stringList.add(new StringSearchResult(entryMeta.getName(), entryMeta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.JobEntryName"))); //$NON-NLS-1$
				if (entryMeta.getDescription() != null)
					stringList.add(new StringSearchResult(entryMeta.getDescription(), entryMeta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.JobEntryDescription"))); //$NON-NLS-1$
				JobEntryInterface metaInterface = entryMeta.getEntry();
				StringSearcher.findMetaData(metaInterface, 1, stringList, entryMeta, this);
			}
		}

		// Loop over all steps in the transformation and see what the used vars
		// are...
		if (searchDatabases) {
			for (int i = 0; i < nrDatabases(); i++) {
				DatabaseMeta meta = getDatabase(i);
				stringList.add(new StringSearchResult(meta.getName(), meta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.DatabaseConnectionName"))); //$NON-NLS-1$
				if (meta.getDatabaseName() != null)
					stringList.add(new StringSearchResult(meta.getDatabaseName(), meta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.DatabaseName"))); //$NON-NLS-1$
				if (meta.getUsername() != null)
					stringList.add(new StringSearchResult(meta.getUsername(), meta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.DatabaseUsername"))); //$NON-NLS-1$
				if (meta.getPluginId() != null)
					stringList.add(new StringSearchResult(meta.getPluginId(), meta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.DatabaseTypeDescription"))); //$NON-NLS-1$
				if (meta.getDatabasePortNumberString() != null)
					stringList.add(new StringSearchResult(meta.getDatabasePortNumberString(), meta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.DatabasePort"))); //$NON-NLS-1$
			}
		}

		// Loop over all steps in the transformation and see what the used vars
		// are...
		if (searchNotes) {
			for (int i = 0; i < nrNotes(); i++) {
				NotePadMeta meta = getNote(i);
				if (meta.getNote() != null)
					stringList.add(new StringSearchResult(meta.getNote(), meta, this, BaseMessages.getString(PKG, "JobMeta.SearchMetadata.NotepadText"))); //$NON-NLS-1$
			}
		}

		return stringList;
	}

	public List<String> getUsedVariables() {
		// Get the list of Strings.
		List<StringSearchResult> stringList = getStringList(true, true, false);

		List<String> varList = new ArrayList<String>();

		// Look around in the strings, see what we find...
		for (StringSearchResult result : stringList) {
			StringUtil.getUsedVariables(result.getString(), varList, false);
		}

		return varList;
	}

	public boolean haveConnectionsChanged() {
		if (changedDatabases)
			return true;

		for (int i = 0; i < nrDatabases(); i++) {
			DatabaseMeta ci = getDatabase(i);
			if (ci.hasChanged())
				return true;
		}
		return false;
	}

	public boolean haveJobEntriesChanged() {
		if (changedEntries)
			return true;

		for (int i = 0; i < nrJobEntries(); i++) {
			JobEntryCopy entry = getJobEntry(i);
			if (entry.hasChanged())
				return true;
		}
		return false;
	}

	public boolean haveJobHopsChanged() {
		if (changedHops)
			return true;

		for (JobHopMeta hi : jobhops) // Look at all the hops
		{
			if (hi.hasChanged())
				return true;
		}
		return false;
	}

	public boolean haveNotesChanged() {
		if (changedNotes)
			return true;

		for (int i = 0; i < nrNotes(); i++) {
			NotePadMeta note = getNote(i);
			if (note.hasChanged())
				return true;
		}
		return false;
	}

	/**
	 * @return the sharedObjectsFile
	 */
	public String getSharedObjectsFile() {
		return sharedObjectsFile;
	}

	/**
	 * @param sharedObjectsFile
	 *            the sharedObjectsFile to set
	 */
	public void setSharedObjectsFile(String sharedObjectsFile) {
		this.sharedObjectsFile = sharedObjectsFile;
	}

	/**
	 * @param modifiedUser
	 *            The modifiedUser to set.
	 */
	public void setModifiedUser(String modifiedUser) {
		this.modifiedUser = modifiedUser;
	}

	/**
	 * @return Returns the modifiedUser.
	 */
	public String getModifiedUser() {
		return modifiedUser;
	}

	/**
	 * @param modifiedDate
	 *            The modifiedDate to set.
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * @return Returns the modifiedDate.
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @return The description of the job
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return The extended description of the job
	 */
	public String getExtendedDescription() {
		return extendedDescription;
	}

	/**
	 * @return The version of the job
	 */
	public String getJobversion() {
		return jobVersion;
	}

	/**
	 * Get the status of the job
	 */
	public int getJobstatus() {
		return jobStatus;
	}

	/**
	 * Set the description of the job.
	 * 
	 * @param description
	 *            The new description of the job
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Set the description of the job.
	 * 
	 * @param extendedDescription
	 *            The new extended description of the job
	 */
	public void setExtendedDescription(String extendedDescription) {
		this.extendedDescription = extendedDescription;
	}

	/**
	 * Set the version of the job.
	 * 
	 * @param jobVersion
	 *            The new version description of the job
	 */
	public void setJobversion(String jobVersion) {
		this.jobVersion = jobVersion;
	}

	/**
	 * Set the status of the job.
	 * 
	 * @param jobStatus
	 *            The new status description of the job
	 */
	public void setJobstatus(int jobStatus) {
		this.jobStatus = jobStatus;
	}

	/**
	 * @return Returns the createdDate.
	 */
	public Date getCreatedDate() {
		return created_date;
	}

	/**
	 * @param createdDate
	 *            The createdDate to set.
	 */
	public void setCreatedDate(Date createdDate) {
		created_date = createdDate;
	}

	/**
	 * @param createdUser
	 *            The createdUser to set.
	 */
	public void setCreatedUser(String createdUser) {
		created_user = createdUser;
	}

	/**
	 * @return Returns the createdUser.
	 */
	public String getCreatedUser() {
		return created_user;
	}

	/**
	 * Find a jobentry with a certain ID in a list of job entries.
	 * 
	 * @param jobentries
	 *            The List of jobentries
	 * @param id_jobentry
	 *            The id of the jobentry
	 * @return The JobEntry object if one was found, null otherwise.
	 */
	public static final JobEntryInterface findJobEntry(List<JobEntryInterface> jobentries, ObjectId id_jobentry) {
		if (jobentries == null)
			return null;

		for (JobEntryInterface je : jobentries) {
			if (je.getObjectId()!=null && je.getObjectId().equals(id_jobentry)) {
				return je;
			}
		}
		return null;
	}

	/**
	 * Find a jobentrycopy with a certain ID in a list of job entry copies.
	 * 
	 * @param jobcopies
	 *            The List of jobentry copies
	 * @param id_jobentry_copy
	 *            The id of the jobentry copy
	 * @return The JobEntryCopy object if one was found, null otherwise.
	 */
	public static final JobEntryCopy findJobEntryCopy(List<JobEntryCopy> jobcopies, ObjectId id_jobentry_copy) {
		if (jobcopies == null)
			return null;

		for (JobEntryCopy jec : jobcopies) {
			if (jec.getObjectId()!=null && jec.getObjectId().equals(id_jobentry_copy)) {
				return jec;
			}
		}
		return null;
	}

	/**
	 * Calls setInternalKettleVariables on the default object.
	 */
	public void setInternalKettleVariables() {
		setInternalKettleVariables(variables);
	}

	/**
	 * This method sets various internal kettle variables that can be used by
	 * the transformation.
	 */
	public void setInternalKettleVariables(VariableSpace var) {
	  setInternalFilenameKettleVariables(var);
	  setInternalNameKettleVariable(var);
	  
		// The name of the directory in the repository
		var.setVariable(Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, directory != null ? directory.getPath() : ""); //$NON-NLS-1$

		// Undefine the transformation specific variables:
		// transformations can't run jobs, so if you use these they are 99.99%
		// wrong.
		var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null);
		var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null);
		var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null);
		var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null);
		var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, null);
		var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, null);
	}

  private void setInternalNameKettleVariable(VariableSpace var) {
    // The name of the job
    var.setVariable(Const.INTERNAL_VARIABLE_JOB_NAME, Const.NVL(name, "")); //$NON-NLS-1$
  }

  private void setInternalFilenameKettleVariables(VariableSpace var) {
    if (filename != null) // we have a filename that's defined.
    {
      try {
        FileObject fileObject = KettleVFS.getFileObject(filename, var);
        FileName fileName = fileObject.getName();

        // The filename of the job
        var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName.getBaseName());

        // The directory of the job
        FileName fileDir = fileName.getParent();
        var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, fileDir.getURI());
      } catch (Exception e) {
        var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "");
        var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "");
      }
    } else {
      var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, ""); //$NON-NLS-1$
      var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, ""); //$NON-NLS-1$
    }
  }

  public void copyVariablesFrom(VariableSpace space) {
		variables.copyVariablesFrom(space);
	}

	public String environmentSubstitute(String aString) {
		return variables.environmentSubstitute(aString);
	}

	public String[] environmentSubstitute(String aString[]) {
		return variables.environmentSubstitute(aString);
	}

	public VariableSpace getParentVariableSpace() {
		return variables.getParentVariableSpace();
	}

	public void setParentVariableSpace(VariableSpace parent) {
		variables.setParentVariableSpace(parent);
	}

	public String getVariable(String variableName, String defaultValue) {
		return variables.getVariable(variableName, defaultValue);
	}

	public String getVariable(String variableName) {
		return variables.getVariable(variableName);
	}

	public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
		if (!Const.isEmpty(variableName)) {
			String value = environmentSubstitute(variableName);
			if (!Const.isEmpty(value)) {
				return ValueMeta.convertStringToBoolean(value);
			}
		}
		return defaultValue;
	}

	public void initializeVariablesFrom(VariableSpace parent) {
		variables.initializeVariablesFrom(parent);
	}

	public String[] listVariables() {
		return variables.listVariables();
	}

	public void setVariable(String variableName, String variableValue) {
		variables.setVariable(variableName, variableValue);
	}

	public void shareVariablesWith(VariableSpace space) {
		variables = space;
	}

	public void injectVariables(Map<String, String> prop) {
		variables.injectVariables(prop);
	}

	/**
	 * Check all job entries within the job. Each Job Entry has the opportunity
	 * to check their own settings.
	 * 
	 * @param remarks
	 *            List of CheckResult remarks inserted into by each JobEntry
	 * @param only_selected
	 *            true if you only want to check the selected jobs
	 * @param monitor
	 *            Progress monitor (not presently in use)
	 */
	public void checkJobEntries(List<CheckResultInterface> remarks, boolean only_selected, ProgressMonitorListener monitor) {
		remarks.clear(); // Empty remarks
		if (monitor != null)
			monitor.beginTask(BaseMessages.getString(PKG, "JobMeta.Monitor.VerifyingThisJobEntryTask.Title"), jobcopies.size() + 2); //$NON-NLS-1$
		boolean stop_checking = false;
		for (int i = 0; i < jobcopies.size() && !stop_checking; i++) {
			JobEntryCopy copy = jobcopies.get(i); // get the job entry copy
			if ((!only_selected) || (only_selected && copy.isSelected())) {
				JobEntryInterface entry = copy.getEntry();
				if (entry != null) {
					if (monitor != null)
						monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.VerifyingJobEntry.Title", entry.getName())); //$NON-NLS-1$ //$NON-NLS-2$
					entry.check(remarks, this);
					if (monitor != null) {
						monitor.worked(1); // progress bar...
						if (monitor.isCanceled()) {
							stop_checking = true;
						}
					}
				}
			}
			if (monitor != null) {
				monitor.worked(1);
			}
		}
		if (monitor != null) {
			monitor.done();
		}
	}

	public List<ResourceReference> getResourceDependencies() {
		List<ResourceReference> resourceReferences = new ArrayList<ResourceReference>();
		JobEntryCopy copy = null;
		JobEntryInterface entry = null;
		for (int i = 0; i < jobcopies.size(); i++) {
			copy = jobcopies.get(i); // get the job entry copy
			entry = copy.getEntry();
			resourceReferences.addAll(entry.getResourceDependencies(this));
		}

		return resourceReferences;
	}

	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface, Repository repository) throws KettleException {
		String resourceName = null;
		try {
			// Handle naming for both repository and XML bases resources...
			//
			String baseName;
			String originalPath;
			String fullname;
			String extension="kjb";
			if (Const.isEmpty(getFilename())) {
				// Assume repository...
				//
				originalPath = directory.getPath();
				baseName = getName();
				fullname = directory.getPath()+( directory.getPath().endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR) ? "" : RepositoryDirectory.DIRECTORY_SEPARATOR ) +getName()+"."+extension; // $NON-NLS-1$ // $NON-NLS-2$  
			} else {
				// Assume file
				//
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(getFilename()), space);
				originalPath = fileObject.getParent().getName().getPath();
				baseName = fileObject.getName().getBaseName();
				fullname = fileObject.getName().getPath();
			}
			
			resourceName = namingInterface.nameResource(baseName, originalPath, extension, ResourceNamingInterface.FileNamingType.JOB); //$NON-NLS-1$
			ResourceDefinition definition = definitions.get(resourceName);
			if (definition == null) {
				// If we do this once, it will be plenty :-)
				//
				JobMeta jobMeta = (JobMeta) this.realClone(false);

				// All objects get re-located to the root folder
				//
				jobMeta.setRepositoryDirectory(new RepositoryDirectory());

				// Add used resources, modify transMeta accordingly
				// Go through the list of steps, etc.
				// These critters change the steps in the cloned TransMeta
				// At the end we make a new XML version of it in "exported"
				// format...

				// loop over steps, databases will be exported to XML anyway.
				//
				for (JobEntryCopy jobEntry : jobMeta.jobcopies) {
					jobEntry.getEntry().exportResources(jobMeta, definitions, namingInterface, repository);
				}
				
				// Set a number of parameters for all the data files referenced so far...
				//
				Map<String, String> directoryMap = namingInterface.getDirectoryMap();
				if (directoryMap!=null) {
					for (String directory : directoryMap.keySet()) {
						String parameterName = directoryMap.get(directory);
						jobMeta.addParameterDefinition(parameterName, directory, "Data file path discovered during export");
					}
				}

				// At the end, add ourselves to the map...
				//
				String jobMetaContent = jobMeta.getXML();

				definition = new ResourceDefinition(resourceName, jobMetaContent);
				
	  			// Also remember the original filename (if any), including variables etc.
	  			//
				if (Const.isEmpty(this.getFilename())) { // Repository
					definition.setOrigin(fullname);
				} else {
					definition.setOrigin(this.getFilename());
				}

				definitions.put(fullname, definition);
			}
		} catch (FileSystemException e) {
			throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", getFilename()), e);
		} catch (KettleFileException e) {
			throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", getFilename()), e);
		}

		return resourceName;
	}

	/**
	 * @return the slaveServer list
	 */
	public List<SlaveServer> getSlaveServers() {
		return slaveServers;
	}

	/**
	 * @param slaveServers
	 *            the slaveServers to set
	 */
	public void setSlaveServers(List<SlaveServer> slaveServers) {
		this.slaveServers = slaveServers;
	}

	/**
	 * Find a slave server using the name
	 * 
	 * @param serverString
	 *            the name of the slave server
	 * @return the slave server or null if we couldn't spot an approriate entry.
	 */
	public SlaveServer findSlaveServer(String serverString) {
		return SlaveServer.findSlaveServer(slaveServers, serverString);
	}

	/**
	 * @return An array list slave server names
	 */
	public String[] getSlaveServerNames() {
		return SlaveServer.getSlaveServerNames(slaveServers);
	}

	/**
	 * See if the name of the supplied job entry copy doesn't collide with any
	 * other job entry copy in the job.
	 * 
	 * @param je
	 *            The job entry copy to verify the name for.
	 */
	public void renameJobEntryIfNameCollides(JobEntryCopy je) {
		// First see if the name changed.
		// If so, we need to verify that the name is not already used in the
		// job.
		//
		String newname = je.getName();

		// See if this name exists in the other job entries
		//
		boolean found;
		int nr = 1;
		do {
			found = false;
			for (JobEntryCopy copy : jobcopies) {
				if (copy != je && copy.getName().equalsIgnoreCase(newname) && copy.getNr() == 0)
					found = true;
			}
			if (found) {
				nr++;
				newname = je.getName() + " (" + nr + ")";
			}
		} while (found);

		// Rename if required.
		//
		je.setName(newname);
	}

	/**
	 * @return the sharedObjects
	 */
	public SharedObjects getSharedObjects() {
		return sharedObjects;
	}

	/**
	 * @param sharedObjects the sharedObjects to set
	 */
	public void setSharedObjects(SharedObjects sharedObjects) {
		this.sharedObjects = sharedObjects;
	}
	
	public void addNameChangedListener(NameChangedListener listener) {
		if (nameChangedListeners==null) {
			nameChangedListeners = new ArrayList<NameChangedListener>();
		}
		nameChangedListeners.add(listener);
	}
	
	public void removeNameChangedListener(NameChangedListener listener) {
		nameChangedListeners.remove(listener);
	}

	public void addFilenameChangedListener(FilenameChangedListener listener) {
		if (filenameChangedListeners==null) {
			filenameChangedListeners = new ArrayList<FilenameChangedListener>();
		}
		filenameChangedListeners.add(listener);
	}

	public void removeFilenameChangedListener(FilenameChangedListener listener) {
		filenameChangedListeners.remove(listener);
	}
	
	private boolean nameChanged(String oldFilename, String newFilename) {
		if (oldFilename==null && newFilename==null) return false;
		if (oldFilename==null && newFilename!=null) return true;
		return oldFilename.equals(newFilename);
	}
	
	private void fireFilenameChangedListeners(String oldFilename, String newFilename) {
		if (nameChanged(oldFilename, newFilename)) {
			if (filenameChangedListeners!=null) {
				for (FilenameChangedListener listener : filenameChangedListeners) {
					listener.filenameChanged(this, oldFilename, newFilename);
				}
			}
		}
	}

	private void fireNameChangedListeners(String oldName, String newName) {
		if (nameChanged(oldName, newName)) {
			if (nameChangedListeners!=null) {
				for (NameChangedListener listener : nameChangedListeners) {
					listener.nameChanged(this, oldName, newName);
				}
			}
		}
	}
	
	public void activateParameters() {
		String[] keys = listParameters();
		
		for ( String key : keys )  {
			String value;
			try {
				value = getParameterValue(key);
			} catch (UnknownParamException e) {
				value = "";
			}
			String defValue;
			try {
				defValue = getParameterDefault(key);
			} catch (UnknownParamException e) {
				defValue = "";
			}
			
			if ( Const.isEmpty(value) )  {
				setVariable(key, defValue);
			}
			else  {
				setVariable(key, value);
			}
		}		 		
	}

	public void addParameterDefinition(String key, String defValue, String description) throws DuplicateParamException {
		namedParams.addParameterDefinition(key, defValue, description);		
	}

	public String getParameterDescription(String key) throws UnknownParamException {
		return namedParams.getParameterDescription(key);
	}
	
	public String getParameterDefault(String key) throws UnknownParamException {
		return namedParams.getParameterDefault(key);
	}		

	public String getParameterValue(String key) throws UnknownParamException {
		return namedParams.getParameterValue(key);
	}

	public String[] listParameters() {
		return namedParams.listParameters();
	}

	public void setParameterValue(String key, String value) throws UnknownParamException {
		namedParams.setParameterValue(key, value);
	}

	public void eraseParameters() {
		namedParams.eraseParameters();		
	}

	public void clearParameters() {
		namedParams.clearParameters();		
	}	
	
	public void copyParametersFrom(NamedParams params) {
		namedParams.copyParametersFrom(params);		
	}
	
	public List<JobEntryCopy> getJobCopies() {
		return jobcopies;
	}
	
	public List<NotePadMeta> getNotes() {
		return notes;
	}
	
	public List<JobHopMeta> getJobhops() {
		return jobhops;
	}
	
	public RepositoryObjectType getRepositoryElementType() {
		return REPOSITORY_ELEMENT_TYPE;
	}
	
	public ObjectRevision getObjectRevision() {
		return objectRevision;
	}

	public void setObjectRevision(ObjectRevision objectRevision) {
		this.objectRevision = objectRevision;
	}
	
	/**
	 * Create a unique list of job entry interfaces
	 * @return
	 */
	public List<JobEntryInterface> composeJobEntryInterfaceList() {
		List<JobEntryInterface> list = new ArrayList<JobEntryInterface>();
		
		for (JobEntryCopy copy : jobcopies) {
			if (!list.contains(copy.getEntry())) {
				list.add(copy.getEntry());
			}
		}
		
		return list;
	}

	public String getLogChannelId() {
		return null;
	}

	public String getObjectName() {
		return getName();
	}

	public String getObjectCopy() {
		return null;
	}

	public LoggingObjectType getObjectType() {
		return LoggingObjectType.JOBMETA;
	}

	public LoggingObjectInterface getParent() {
		return null; // TODO return parent job metadata
	}

	public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
  }
  
  /**
	 * @return the channelLogTable
	 */
	public ChannelLogTable getChannelLogTable() {
		return channelLogTable;
	}

	/**
	 * @param channelLogTable the channelLogTable to set
	 */
	public void setChannelLogTable(ChannelLogTable channelLogTable) {
		this.channelLogTable = channelLogTable;
	}

	/**
	 * @return the jobEntryLogTable
	 */
	public JobEntryLogTable getJobEntryLogTable() {
		return jobEntryLogTable;
	}

	/**
	 * @param jobEntryLogTable the jobEntryLogTable to set
	 */
	public void setJobEntryLogTable(JobEntryLogTable jobEntryLogTable) {
		this.jobEntryLogTable = jobEntryLogTable;
	}
	
	public List<LogTableInterface> getLogTables() {
		List<LogTableInterface> logTables = new ArrayList<LogTableInterface>();
		logTables.add(jobLogTable);
		logTables.add(jobEntryLogTable);
		logTables.add(channelLogTable);
		return logTables;
	}
	
	public boolean canSave() {
    return true;
  }

  /**
   * @return the carteObjectId
   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the execution container Object id to set
   */
  public void setCarteObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;
  }

}