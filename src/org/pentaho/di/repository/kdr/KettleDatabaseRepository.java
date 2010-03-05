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

package org.pentaho.di.repository.kdr;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleDependencyException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementLocationInterface;
import org.pentaho.di.repository.RepositoryLock;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.RepositoryVersionRegistry;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryClusterSchemaDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConditionDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryDatabaseDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryDirectoryDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryJobDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryJobEntryDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryNotePadDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryPartitionSchemaDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositorySlaveServerDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryStepDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryTransDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryUserDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryValueDelegate;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;



/**
 * 
 * This class handles interactions with a Kettle repository.
 * 
 * @author Matt
 * Created on 31-mrt-2004
 *
 */
public class KettleDatabaseRepository extends KettleDatabaseRepositoryBase implements Repository
{
//	private static Class<?> PKG = Repository.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public KettleDatabaseRepositoryTransDelegate transDelegate;
	public KettleDatabaseRepositoryJobDelegate jobDelegate;
	public KettleDatabaseRepositoryDatabaseDelegate databaseDelegate;
	public KettleDatabaseRepositorySlaveServerDelegate slaveServerDelegate;
	public KettleDatabaseRepositoryClusterSchemaDelegate clusterSchemaDelegate;
	public KettleDatabaseRepositoryPartitionSchemaDelegate partitionSchemaDelegate;
	public KettleDatabaseRepositoryDirectoryDelegate directoryDelegate;
	public KettleDatabaseRepositoryConnectionDelegate connectionDelegate;
	public KettleDatabaseRepositoryUserDelegate userDelegate;
	public KettleDatabaseRepositoryConditionDelegate conditionDelegate;
	public KettleDatabaseRepositoryValueDelegate valueDelegate;
	public KettleDatabaseRepositoryNotePadDelegate notePadDelegate;
	public KettleDatabaseRepositoryStepDelegate stepDelegate;
	public KettleDatabaseRepositoryJobEntryDelegate jobEntryDelegate;
	private KettleDatabaseRepositorySecurityProvider	securityProvider;
	private Map<Class<? extends IRepositoryService>, IRepositoryService> serviceMap;
	private List<Class<? extends IRepositoryService>> serviceList;
	public KettleDatabaseRepository() {
		super();
    		
	}
	
  /**
   * Initialize the repository with the repository metadata and user information.
   */
  public void init(RepositoryMeta repositoryMeta) {
		this.repositoryMeta = (KettleDatabaseRepositoryMeta)repositoryMeta;
		this.serviceList = new ArrayList<Class<? extends IRepositoryService>>();
    this.serviceMap = new HashMap<Class<? extends IRepositoryService>, IRepositoryService>();
		this.log = new LogChannel(this);
		init();
	}
	
	
	private void init() {
		// Create the delegates...
		//
		this.transDelegate = new KettleDatabaseRepositoryTransDelegate(this);
		this.jobDelegate = new KettleDatabaseRepositoryJobDelegate(this);
		this.databaseDelegate = new KettleDatabaseRepositoryDatabaseDelegate(this);
		this.slaveServerDelegate = new KettleDatabaseRepositorySlaveServerDelegate(this);
		this.clusterSchemaDelegate = new KettleDatabaseRepositoryClusterSchemaDelegate(this);
		this.partitionSchemaDelegate = new KettleDatabaseRepositoryPartitionSchemaDelegate(this);
		this.directoryDelegate = new KettleDatabaseRepositoryDirectoryDelegate(this);
		this.connectionDelegate = new KettleDatabaseRepositoryConnectionDelegate(this, repositoryMeta.getConnection());
		this.userDelegate = new KettleDatabaseRepositoryUserDelegate(this);
		this.conditionDelegate = new KettleDatabaseRepositoryConditionDelegate(this);
		this.valueDelegate = new KettleDatabaseRepositoryValueDelegate(this);
		this.notePadDelegate = new KettleDatabaseRepositoryNotePadDelegate(this);
		this.stepDelegate = new KettleDatabaseRepositoryStepDelegate(this);
		this.jobEntryDelegate = new KettleDatabaseRepositoryJobEntryDelegate(this);
		this.creationHelper = new KettleDatabaseRepositoryCreationHelper(this);
	}
	
    /** 
     * @return A new repository meta object
     */
    public RepositoryMeta createRepositoryMeta() {
    	return new KettleDatabaseRepositoryMeta();
    }
    
	/**
	 * Connect to the repository. 
	 * 
	 * @throws KettleSecurityException in case the supplied user or password is not valid
	 * @throws KettleException in case there is a general unexpected error or if we're already connected
	 */
	public void connect(String username, String password) throws KettleException {
	  // first disconnect if already connected
	  connectionDelegate.connect();
		try {
		  IUser userinfo = userDelegate.loadUserInfo(new UserInfo(), username, password);
		  securityProvider = new KettleDatabaseRepositorySecurityProvider(this, repositoryMeta, userinfo);
	    
	    // We need to add services in the list in the order of dependencies
		  registerRepositoryService(RepositorySecurityProvider.class, securityProvider);
		  registerRepositoryService(RepositorySecurityManager.class, securityProvider);
		} catch (KettleDatabaseException e) {
		  // if we fail to log in, disconnect and then rethrow the exception
		  connectionDelegate.disconnect();
		  throw e;
		}
	}

  /**
   * Add the repository service to the map and add the interface to the list
   * @param clazz
   * @param repositoryService
   */
  private void registerRepositoryService(Class<? extends IRepositoryService> clazz, IRepositoryService repositoryService) {
    this.serviceMap.put(clazz, repositoryService);
    this.serviceList.add(clazz);
  }
  
	public synchronized void commit() throws KettleException {
		connectionDelegate.commit();
	}

	public synchronized void rollback() {
		connectionDelegate.rollback();
	}
	
	public IUser getUserInfo() {
	  if (securityProvider != null) {
	    return securityProvider.getUserInfo();
	  } else {
	    return null;
	  }
	}

	/**
	 * Return the major repository version.
	 * @return the major repository version.
	 */
	public int getMajorVersion() {
		return connectionDelegate.getMajorVersion();
	}

	/**
	 * Return the minor repository version.
	 * @return the minor repository version.
	 */
	public int getMinorVersion() {
		return connectionDelegate.getMinorVersion();
	}

	/**
	 * Get the repository version.
	 * @return The repository version as major version + "." + minor version
	 */
	public String getVersion() {
		return connectionDelegate.getVersion();
	}
    
    // TransMeta
    
    public TransMeta loadTransformation(String transname, RepositoryDirectory repdir, ProgressMonitorListener monitor, boolean setInternalVariables, String versionName) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.READ_TRANSFORMATION);
    	return transDelegate.loadTransformation(new TransMeta(), transname, repdir, monitor, setInternalVariables);
	}
        
	public SharedObjects readTransSharedObjects(TransMeta transMeta) throws KettleException {
		return transDelegate.readTransSharedObjects(transMeta);
	}
		
	public synchronized ObjectId renameTransformation(ObjectId id_transformation, RepositoryDirectory newDir, String newName) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.MODIFY_TRANSFORMATION);

    	transDelegate.renameTransformation(id_transformation, newDir, newName);
    	
      return id_transformation; // The same in our case.
	}

	public void lockTransformation(ObjectId id_transformation, String message) throws KettleException {
		transDelegate.lockTransformation(id_transformation, message);
	}

	public void unlockTransformation(ObjectId id_transformation) throws KettleException {
		transDelegate.unlockTransformation(id_transformation);
	}

	public List<RepositoryLock> getTransformationLocks() throws KettleException {
		return transDelegate.getTransformationLocks();
	}
	
	public RepositoryLock getTransformationLock(ObjectId id_transformation) throws KettleDatabaseException {
		return transDelegate.getTransformationLock(id_transformation);
	}

    // JobMeta
    
	
	/** Load a job in a directory
	 * 
	 * @param jobname The name of the job
	 * @param repdir The directory in which the job resides.
	 * @param the monitor to use as feedback in a UI (or null if not used)
	 * @throws KettleException
	 */
	public JobMeta loadJob(String jobname, RepositoryDirectory repdir, ProgressMonitorListener monitor, String versionName) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.READ_JOB);
    	
		return jobDelegate.loadJobMeta(jobname, repdir, monitor);
	}
	
    public SharedObjects readJobMetaSharedObjects(JobMeta jobMeta) throws KettleException {
    	return jobDelegate.readSharedObjects(jobMeta);
    }

	public synchronized ObjectId renameJob(ObjectId id_job, RepositoryDirectory dir, String newname) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.MODIFY_TRANSFORMATION);

    	jobDelegate.renameJob(id_job, dir, newname);
    	
      return id_job; // Same in this case
	}

	public void lockJob(ObjectId id_job, String message) throws KettleException {
		jobDelegate.lockJob(id_job, message);
	}
	
	public void unlockJob(ObjectId id_job) throws KettleException {
		jobDelegate.unlockJob(id_job);
	}

	public List<RepositoryLock> getJobLocks() throws KettleException {
		return jobDelegate.getJobLocks();
	}

	public RepositoryLock getJobLock(ObjectId id_job) throws KettleDatabaseException {
		return jobDelegate.getJobLock(id_job);
	}

	// Common methods...
	//////////////////////////////

	public boolean exists(String name, RepositoryDirectory repositoryDirectory, RepositoryObjectType objectType) throws KettleException {
    	
		switch(objectType) {
		case JOB : 
        	securityProvider.validateAction(RepositoryOperation.READ_JOB);
    		return jobDelegate.existsJobMeta(name, repositoryDirectory, objectType);
    		
		case TRANSFORMATION :
        	securityProvider.validateAction(RepositoryOperation.READ_TRANSFORMATION);
    		return transDelegate.existsTransMeta(name, repositoryDirectory, objectType);

    	default : 
    		throw new KettleException("We can't verify the existance of repository element type ["+objectType+"]");
    	}
    }
    
    public void save(RepositoryElementInterface repositoryElement, String versionComment) throws KettleException {
    	save(repositoryElement, versionComment, null);
    }

    public void save(RepositoryElementInterface repositoryElement, String versionComment, ProgressMonitorListener monitor) throws KettleException {
    	save(repositoryElement, versionComment, monitor, null, false);
    }
    	 
    public void save(RepositoryElementInterface repositoryElement, String versionComment, ProgressMonitorListener monitor, ObjectId parentId, boolean used) throws KettleException {
    	
    	try {
    		lockRepository();
    		
    		if (!Const.isEmpty(versionComment)) {
    			insertLogEntry(versionComment);
    		}
    		
    		switch(repositoryElement.getRepositoryElementType()) {
    		case JOB : 
	        	securityProvider.validateAction(RepositoryOperation.MODIFY_JOB);
	    		jobDelegate.saveJob((JobMeta)repositoryElement, versionComment, monitor);
    			break;
    		case TRANSFORMATION : 
	        	securityProvider.validateAction(RepositoryOperation.MODIFY_TRANSFORMATION);
	    		transDelegate.saveTransformation((TransMeta)repositoryElement, versionComment, monitor);
    			break;
    		case DATABASE : 
	        	securityProvider.validateAction(RepositoryOperation.MODIFY_DATABASE);
	    		databaseDelegate.saveDatabaseMeta((DatabaseMeta)repositoryElement);
    			break;
    		case SLAVE_SERVER :
	        	securityProvider.validateAction(RepositoryOperation.MODIFY_SLAVE_SERVER);
	    		slaveServerDelegate.saveSlaveServer((SlaveServer)repositoryElement, parentId, used);
    			break;
    		case CLUSTER_SCHEMA :
	        	securityProvider.validateAction(RepositoryOperation.MODIFY_CLUSTER_SCHEMA);
	    		clusterSchemaDelegate.saveClusterSchema((ClusterSchema)repositoryElement, versionComment, parentId, used);
    			break;
    		case PARTITION_SCHEMA :
	        	securityProvider.validateAction(RepositoryOperation.MODIFY_PARTITION_SCHEMA);
	    		partitionSchemaDelegate.savePartitionSchema((PartitionSchema)repositoryElement, parentId, used);
	    		break;
	    	default:
	    		throw new KettleException("We can't save the element with type ["+repositoryElement.getRepositoryElementType()+"] in the repository");
    		}
	    	
			// Automatically commit changes to these elements.
	    	//
			commit();
    	} finally {
            unlockRepository();
    	}
    }
    
	// Condition
    
	/**
     *  
	 * Read a condition from the repository.
	 * @param id_condition The condition id
	 * @throws KettleException if something goes wrong.
	 */
	public Condition loadCondition(ObjectId id_condition) throws KettleException {
		return conditionDelegate.loadCondition(id_condition);
	}
	
	public ObjectId saveCondition(Condition condition) throws KettleException
	{
		return saveCondition(condition, null);
	}
	
	public ObjectId saveCondition(Condition condition, ObjectId id_condition_parent) throws KettleException {
		return conditionDelegate.saveCondition(condition, id_condition_parent);
	}
	
	// DatabaseMeta

	/**
	 * Load the Database connection Metadata from the repository
	 * @param id_database the id of the database connection to load
	 * @throws KettleException in case something goes wrong with database, connection, etc.
	 */
	public DatabaseMeta loadDatabaseMeta(ObjectId id_database, String versionName) throws KettleException {
		return databaseDelegate.loadDatabaseMeta(id_database);
	}
	
	/**
	 * Remove a database connection from the repository
	 * @param databaseName The name of the connection to remove
	 * @throws KettleException In case something went wrong: database error, insufficient permissions, depending objects, etc.
	 */
	public void deleteDatabaseMeta(String databaseName) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.DELETE_DATABASE);
		databaseDelegate.deleteDatabaseMeta(databaseName);
	}

	// ClusterSchema
	
    public ClusterSchema loadClusterSchema(ObjectId id_cluster_schema, List<SlaveServer> slaveServers, String versionName) throws KettleException {
    	return clusterSchemaDelegate.loadClusterSchema(id_cluster_schema, slaveServers);
    }
	
    // SlaveServer
    
    public SlaveServer loadSlaveServer(ObjectId id_slave_server, String versionName) throws KettleException {
    	return slaveServerDelegate.loadSlaveServer(id_slave_server);
    }
    
    // PartitionSchema
    
	public PartitionSchema loadPartitionSchema(ObjectId id_partition_schema, String versionName) throws KettleException {
		return partitionSchemaDelegate.loadPartitionSchema(id_partition_schema);
	}

	// ValueMetaAndData
    
	public ValueMetaAndData loadValueMetaAndData(ObjectId id_value) throws KettleException {
		return valueDelegate.loadValueMetaAndData(id_value);
	}
	 
	
	// NotePadMeta
	
	public NotePadMeta loadNotePadMeta(ObjectId id_note) throws KettleException {
		return notePadDelegate.loadNotePadMeta(id_note);
	}

	public void saveNotePadMeta(NotePadMeta note, ObjectId id_transformation) throws KettleException {
		notePadDelegate.saveNotePadMeta(note, id_transformation);
	}
	
	
	// Directory stuff
	
	public RepositoryDirectory loadRepositoryDirectoryTree() throws KettleException {
		RepositoryDirectory root = new RepositoryDirectory();
		root.setObjectId(new LongObjectId(0L));
		return directoryDelegate.loadRepositoryDirectoryTree(root);
	}
	
	public RepositoryDirectory loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException {
		return directoryDelegate.loadRepositoryDirectoryTree(root);
	}

	public void saveRepositoryDirectory(RepositoryDirectory dir) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.CREATE_DIRECTORY);
		directoryDelegate.saveRepositoryDirectory(dir);
	}

	public void deleteRepositoryDirectory(RepositoryDirectory dir) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.DELETE_DIRECTORY);
		directoryDelegate.delRepositoryDirectory(dir);
	}

	public ObjectId renameRepositoryDirectory(RepositoryDirectory dir) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.RENAME_DIRECTORY);
		return directoryDelegate.renameRepositoryDirectory(dir);
	}
	
	 public ObjectId renameRepositoryDirectory(ObjectId id, RepositoryDirectory newParentDir, String newName) throws KettleException {
	   ObjectId result = null;
	   securityProvider.validateAction(RepositoryOperation.RENAME_DIRECTORY);
	   result = directoryDelegate.renameRepositoryDirectory(id, newParentDir, newName);
	   
	   return result;
	 }
	
	/**
	 * Create a new directory, possibly by creating several sub-directies of / at the same time.
	 * 
	 * @param parentDirectory the parent directory
	 * @param directoryPath The path to the new Repository Directory, to be created.
	 * @return The created sub-directory
	 * @throws KettleException In case something goes wrong
	 */
	public RepositoryDirectory createRepositoryDirectory(RepositoryDirectory parentDirectory, String directoryPath) throws KettleException {
    	securityProvider.validateAction(RepositoryOperation.CREATE_DIRECTORY);
		return directoryDelegate.createRepositoryDirectory(parentDirectory, directoryPath);
	}

	
	
	
    

	/////////////////////////////////////////////////////////////////////////////////////
	// LOOKUP ID          TODO: get rid of these as well!  Move to a delegate
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized ObjectId getRootDirectoryID() throws KettleException
	{
		RowMetaAndData result = connectionDelegate.getOneRow("SELECT "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = 0");
		if (result != null && result.isNumeric(0))
			return new LongObjectId( result.getInteger(0, -1) );
		return null;
	}

	public synchronized int getNrSubDirectories(ObjectId id_directory) throws KettleException {
		return directoryDelegate.getNrSubDirectories(id_directory);
	}

	public synchronized ObjectId[] getSubDirectoryIDs(ObjectId id_directory) throws KettleException {
		return directoryDelegate.getSubDirectoryIDs(id_directory);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// INSERT VALUES
	/////////////////////////////////////////////////////////////////////////////////////

    public synchronized ObjectId insertLogEntry(String description) throws KettleException
    {
        ObjectId id = connectionDelegate.getNextLogID();

        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, ValueMetaInterface.TYPE_INTEGER), id);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_REP_VERSION, ValueMetaInterface.TYPE_STRING), getVersion());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_DATE, ValueMetaInterface.TYPE_DATE), new Date());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_USER, ValueMetaInterface.TYPE_STRING), getUserInfo() !=null ? getUserInfo().getLogin():"admin");
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_OPERATION_DESC, ValueMetaInterface.TYPE_STRING), description);

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_REPOSITORY_LOG, table);

        return id;
    }

	public synchronized void insertTransNote(ObjectId id_transformation, ObjectId id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), id_transformation);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), id_note);

		connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_NOTE, table);
	}

	public synchronized void insertJobNote(ObjectId id_job, ObjectId id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), id_job);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), id_note);

		connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_JOB_NOTE, table);
	}

	public synchronized void insertStepDatabase(ObjectId id_transformation, ObjectId id_step, ObjectId id_database)
			throws KettleException
	{
		// First check if the relationship is already there.
		// There is no need to store it twice!
		RowMetaAndData check = getStepDatabase(id_step);
		if (check.getInteger(0) == null)
		{
			RowMetaAndData table = new RowMetaAndData();

			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), id_transformation);
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_STEP, ValueMetaInterface.TYPE_INTEGER), id_step);
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), id_database);

			connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_STEP_DATABASE, table);
		}
	}
	public synchronized void insertJobEntryDatabase(ObjectId id_job, ObjectId id_jobentry, ObjectId id_database)
	throws KettleException
	{
		// First check if the relationship is already there.
		// There is no need to store it twice!
		RowMetaAndData check = getJobEntryDatabase(id_jobentry);
		
		if (check.getInteger(0) == null)
		{
			RowMetaAndData table = new RowMetaAndData();

			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), id_job);
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), id_jobentry);
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), id_database);

			connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE, table);
		}
	}


    public synchronized ObjectId insertTransformationPartitionSchema(ObjectId id_transformation, ObjectId id_partition_schema) throws KettleException
    {
        ObjectId id = connectionDelegate.getNextTransformationPartitionSchemaID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), id);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), id_transformation);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), id_partition_schema);

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA, table);

        return id;
    }
    
 
    public synchronized ObjectId insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException
    {
        ObjectId id = connectionDelegate.getNextClusterSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, ValueMetaInterface.TYPE_INTEGER), id);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), clusterSchema.getObjectId());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), slaveServer.getObjectId());

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE, table);

        return id;
    }

    public synchronized ObjectId insertTransformationCluster(ObjectId id_transformation, ObjectId id_cluster) throws KettleException
    {
        ObjectId id = connectionDelegate.getNextTransformationClusterID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, ValueMetaInterface.TYPE_INTEGER), id);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), id_transformation);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), id_cluster);

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER, table);

        return id;
    }

    public synchronized ObjectId insertTransformationSlave(ObjectId id_transformation, ObjectId id_slave) throws KettleException
    {
        ObjectId id = connectionDelegate.getNextTransformationSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, ValueMetaInterface.TYPE_INTEGER), id);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), id_transformation);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), id_slave);

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_SLAVE, table);

        return id;
    }
    
    

	public synchronized void insertTransStepCondition(ObjectId id_transformation, ObjectId id_step, ObjectId id_condition)
			throws KettleException
	{
		String tablename = KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), id_transformation);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_STEP, ValueMetaInterface.TYPE_INTEGER), id_step);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER), id_condition);

		connectionDelegate.insertTableRow(tablename, table);
	}




	//////////////////////////////////////////////////////////////////////////////////////////
	// READ DATA FROM REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public synchronized String[] getTransformationNames(ObjectId id_directory, boolean includeDeleted) throws KettleException
	{
		return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANSFORMATION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_NAME));
	}
    
    public List<RepositoryObject> getJobObjects(ObjectId id_directory, boolean includeDeleted) throws KettleException
    {
        return getRepositoryObjects(quoteTable(KettleDatabaseRepository.TABLE_R_JOB), RepositoryObjectType.JOB, id_directory);
    }

    public List<RepositoryObject> getTransformationObjects(ObjectId id_directory, boolean includeDeleted) throws KettleException
    {
        return getRepositoryObjects(quoteTable(KettleDatabaseRepository.TABLE_R_TRANSFORMATION), RepositoryObjectType.TRANSFORMATION, id_directory);
    }

    /**
     * @param id_directory
     * @return A list of RepositoryObjects
     * 
     * @throws KettleException
     */
    private synchronized List<RepositoryObject> getRepositoryObjects(String tableName, RepositoryObjectType objectType, ObjectId id_directory) throws KettleException
    {
    	return connectionDelegate.getRepositoryObjects(tableName, objectType, id_directory);
    }
    

	public synchronized String[] getJobNames(ObjectId id_directory, boolean includeDeleted) throws KettleException
	{
        return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(KettleDatabaseRepository.FIELD_JOB_NAME));
	}

	public synchronized String[] getDirectoryNames(ObjectId id_directory) throws KettleException
	{
        return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory + " ORDER BY "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME));
	}

	public synchronized String[] getJobNames() throws KettleException
	{
        return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_JOB_NAME));
	}

	public ObjectId[] getSubConditionIDs(ObjectId id_condition) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION_PARENT)+" = " + id_condition);
	}

	public ObjectId[] getTransNoteIDs(ObjectId id_transformation) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_NOTE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public ObjectId[] getTransformationConditionIDs(ObjectId id_transformation) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_CONDITION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public ObjectId[] getTransformationDatabaseIDs(ObjectId id_transformation) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public ObjectId[] getJobNoteIDs(ObjectId id_job) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_NOTE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB)+" = " + id_job);
	}

	public ObjectId[] getDatabaseIDs(boolean includeDeleted) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE));
	}
    
    public ObjectId[] getDatabaseAttributeIDs(ObjectId id_database) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = "+id_database);
    }
    
    public ObjectId[] getPartitionSchemaIDs(boolean includeDeleted) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_NAME));
    }
    
    public ObjectId[] getPartitionIDs(ObjectId id_partition_schema) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }

    public ObjectId[] getTransformationPartitionSchemaIDs(ObjectId id_transformation) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = "+id_transformation);
    }
    
    public ObjectId[] getTransformationClusterSchemaIDs(ObjectId id_transformation) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT ID_TRANS_CLUSTER FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER)+" WHERE ID_TRANSFORMATION = " + id_transformation);
    }
    
    public ObjectId[] getClusterIDs(boolean includeDeleted) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_CLUSTER_NAME)); 
    }

    public ObjectId[] getSlaveIDs(boolean includeDeleted) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_SLAVE));
    }

    public ObjectId[] getClusterSlaveIDs(ObjectId id_cluster_schema) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_SLAVE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster_schema);
    }
    
	public synchronized String[] getDatabaseNames(boolean includeDeleted) throws KettleException
	{
		String nameField = quote(KettleDatabaseRepository.FIELD_DATABASE_NAME);
		return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE)+" ORDER BY "+nameField);
	}
    
    public synchronized String[] getPartitionSchemaNames(boolean includeDeleted) throws KettleException
    {
        String nameField = quote(KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_NAME);
        return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getSlaveNames(boolean includeDeleted) throws KettleException
    {
        String nameField = quote(KettleDatabaseRepository.FIELD_SLAVE_NAME);
        return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_SLAVE)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getClusterNames(boolean includeDeleted) throws KettleException
    {
        String nameField = quote(KettleDatabaseRepository.FIELD_CLUSTER_NAME);
        return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER)+" ORDER BY "+nameField);
    }

	public ObjectId[] getStepIDs(ObjectId id_transformation) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_STEP_ID_STEP)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public synchronized String[] getTransformationsUsingDatabase(ObjectId id_database) throws KettleException
	{
		String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE)+" = " + id_database;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
	}
	
	public synchronized String[] getJobsUsingDatabase(ObjectId id_database) throws KettleException
	{
		String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_DATABASE)+" = " + id_database;
        return jobDelegate.getJobsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
	}
	
    public synchronized String[] getClustersUsingSlave(ObjectId id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_SLAVE)+" = " + id_slave;

        List<Object[]> list = connectionDelegate.getRows(sql, 100);
        RowMetaInterface rowMeta = connectionDelegate.getReturnRowMeta();
        List<String> clusterList = new ArrayList<String>();

        for (int i=0;i<list.size();i++)
        {
        	long id_cluster_schema = rowMeta.getInteger(list.get(i), quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER), -1L);
            if (id_cluster_schema > 0)
            {
                RowMetaAndData transRow =  clusterSchemaDelegate.getClusterSchema(new LongObjectId(id_cluster_schema));
                if (transRow!=null)
                {
                    String clusterName = transRow.getString(quote(KettleDatabaseRepository.FIELD_CLUSTER_NAME), "<name not found>");
                    if (clusterName!=null) clusterList.add(clusterName);
                }
            }
        }

        return (String[]) clusterList.toArray(new String[clusterList.size()]);
    }

	public synchronized String[] getTransformationsUsingSlave(ObjectId id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_SLAVE)+" = " + id_slave;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingPartitionSchema(ObjectId id_partition_schema) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+
                     " FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" = " + id_partition_schema;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingCluster(ObjectId id_cluster) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_CLUSTER)+" = " + id_cluster;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
    }


	public ObjectId[] getJobHopIDs(ObjectId id_job) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_HOP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB)+" = " + id_job);
	}

	public ObjectId[] getTransDependencyIDs(ObjectId id_transformation) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DEPENDENCY_ID_DEPENDENCY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DEPENDENCY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public ObjectId[] getJobEntryIDs(ObjectId id_job) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOBENTRY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOB)+" = " + id_job);
	}

	public ObjectId[] getJobEntryCopyIDs(ObjectId id_job) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job);
	}

	public ObjectId[] getJobEntryCopyIDs(ObjectId id_job, ObjectId id_jobentry) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+
				" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job + " AND "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY)+" = " + id_jobentry);
	}

	private RowMetaAndData getStepDatabase(ObjectId id_step) throws KettleException
	{
		return connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE), quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_STEP), id_step);
	}
	
	private RowMetaAndData getJobEntryDatabase(ObjectId id_jobentry) throws KettleException
	{
		return connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE), quote(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOBENTRY), id_jobentry);
	}



	// STEP ATTRIBUTES: SAVE

  /**
   * GZips and then base64 encodes an array of bytes to a String
   *
   * @param val the array of bytes to convert to a string
   * @return the base64 encoded string
   * @throws IOException in the case there is a Base64 or GZip encoding problem
   */
  public static final String byteArrayToString(byte[] val) throws IOException {
    
    String string;
    if (val == null) {
      string = null;
    } else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzos = new GZIPOutputStream(baos);
      BufferedOutputStream bos = new BufferedOutputStream(gzos);
      bos.write( val );
      bos.flush();
      bos.close();
      
      string = new String(Base64.encodeBase64(baos.toByteArray()));
    }

    return string;
  }

	// STEP ATTRIBUTES: GET
    

	//////////////////////////////////////////////////////////////////////////////////////////
	// DELETE DATA IN REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public synchronized void delSteps(ObjectId id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void deleteCondition(ObjectId id_condition) throws KettleException
	{
		boolean ok = true;
		ObjectId ids[] = getSubConditionIDs(id_condition);
		if (ids.length > 0)
		{
			// Delete the sub-conditions...
			for (int i = 0; i < ids.length && ok; i++)
			{
				deleteCondition(ids[i]);
			}

			// Then delete the main condition
			deleteCondition(id_condition);
		}
		else
		{
			String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION)+" = " + id_condition;
			execStatement(sql);
		}
	}

	public synchronized void delStepConditions(ObjectId id_transformation) throws KettleException
	{
		ObjectId ids[] = getTransformationConditionIDs(id_transformation);
		for (int i = 0; i < ids.length; i++)
		{
			deleteCondition(ids[i]);
		}
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	/**
	 * Delete the relationships between the transformation/steps and the databases.
	 * @param id_transformation the transformation for which we want to delete the databases.
	 * @throws KettleException in case something unexpected happens.
	 */
	public synchronized void delStepDatabases(ObjectId id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}
	/**
	 * Delete the relationships between the job/job entries and the databases.
	 * @param id_job the job for which we want to delete the databases.
	 * @throws KettleDatabaseException in case something unexpected happens.
	 */
	public synchronized void delJobEntryDatabases(ObjectId id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}
	public synchronized void delJobEntries(ObjectId id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delJobEntryCopies(ObjectId id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delDependencies(ObjectId id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DEPENDENCY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delStepAttributes(ObjectId id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

    public synchronized void delTransAttributes(ObjectId id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }

    public synchronized void delJobAttributes(ObjectId id_job) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_ID_JOB)+" = " + id_job;
        execStatement(sql);
    }   
    
    public synchronized void delPartitionSchemas(ObjectId id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }

    public synchronized void delPartitions(ObjectId id_partition_schema) throws KettleException
    {
        // First see if the partition is used by a step, transformation etc.
        // 
        execStatement("DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }
    
    public synchronized void delClusterSlaves(ObjectId id_cluster) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster;
        execStatement(sql);
    }
    
    public synchronized void delTransformationClusters(ObjectId id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }

    public synchronized void delTransformationSlaves(ObjectId id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }


	public synchronized void delJobEntryAttributes(ObjectId id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTransHops(ObjectId id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_HOP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delJobHops(ObjectId id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_HOP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTransNotes(ObjectId id_transformation) throws KettleException
	{
		ObjectId ids[] = getTransNoteIDs(id_transformation);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			execStatement(sql);
		}

		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delJobNotes(ObjectId id_job) throws KettleException
	{
		ObjectId ids[] = getJobNoteIDs(id_job);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			execStatement(sql);
		}

		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTrans(ObjectId id_transformation) throws KettleException
	{
		securityProvider.validateAction(RepositoryOperation.DELETE_TRANSFORMATION);

		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANSFORMATION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delJob(ObjectId id_job) throws KettleException
	{
		securityProvider.validateAction(RepositoryOperation.DELETE_TRANSFORMATION);

		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTransStepCondition(ObjectId id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delValue(ObjectId id_value) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_VALUE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_VALUE_ID_VALUE)+" = " + id_value;
		execStatement(sql);
	}

    public synchronized void deleteSlave(ObjectId id_slave) throws KettleException
    {
		securityProvider.validateAction(RepositoryOperation.DELETE_SLAVE_SERVER);

		// First, see if the slave is still used by other objects...
        // If so, generate an error!!
        // We look in table R_TRANS_SLAVE to see if there are any transformations using this slave.
        // We obviously also look in table R_CLUSTER_SLAVE to see if there are any clusters that use this slave.
    	//
        String[] transList = getTransformationsUsingSlave(id_slave);
        String[] clustList = getClustersUsingSlave(id_slave);

        if (transList.length==0 && clustList.length==0)
        {
            execStatement("DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE)+" = " + id_slave);
            execStatement("DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_SLAVE)+" = " + id_slave);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            if (transList.length>0)
            {
                message.append("Slave used by the following transformations:").append(Const.CR);
                for (int i = 0; i < transList.length; i++)
                {
                    message.append("  ").append(transList[i]).append(Const.CR);
                }
                message.append(Const.CR);
            }
            if (clustList.length>0)
            {
                message.append("Slave used by the following cluster schemas:").append(Const.CR);
                for (int i = 0; i < clustList.length; i++)
                {
                    message.append("  ").append(clustList[i]).append(Const.CR);
                }
            }
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This slave server is still in use by one or more transformations ("+transList.length+") or cluster schemas ("+clustList.length+") :", e);
        }
    }
   
    public synchronized void deletePartitionSchema(ObjectId id_partition_schema) throws KettleException {
    	partitionSchemaDelegate.delPartitionSchema(id_partition_schema);
    }
    
    public synchronized void deleteClusterSchema(ObjectId id_cluster) throws KettleException {
       clusterSchemaDelegate.delClusterSchema(id_cluster);
    }

	public synchronized void deleteTransformation(ObjectId id_transformation) throws KettleException
	{
		delTransNotes(id_transformation);
		delStepAttributes(id_transformation);
		delSteps(id_transformation);
		delStepConditions(id_transformation);
		delStepDatabases(id_transformation);
		delTransHops(id_transformation);
		delDependencies(id_transformation);
        delTransAttributes(id_transformation);
        delPartitionSchemas(id_transformation);
        delTransformationClusters(id_transformation);
        delTransformationSlaves(id_transformation);
		delTrans(id_transformation);
	}
	
	public synchronized void deleteJob(ObjectId id_job) throws KettleException
	{
		// logBasic("Deleting info in repository on ID_JOB: "+id_job);

		delJobNotes(id_job);
		delJobAttributes(id_job);
		delJobEntryAttributes(id_job);
		delJobEntryDatabases(id_job);
		delJobEntries(id_job);
		delJobEntryCopies(id_job);
		delJobHops(id_job);
		delJob(id_job);

		// logBasic("All deleted on job with ID_JOB: "+id_job);
	}

	public synchronized ObjectId renameDatabase(ObjectId id_database, String newname) throws KettleException {
		databaseDelegate.renameDatabase(id_database, newname);
		return id_database; // doesn't change in this case
	}



	public boolean dropRepositorySchema() throws KettleException
	{
		// Make sure we close shop before dropping everything. 
		// Some DB's can't handle the drop otherwise.
		//
		connectionDelegate.closeStepAttributeInsertPreparedStatement();
		connectionDelegate.closeLookupJobEntryAttribute();
		
		for (int i = 0; i < repositoryTableNames.length; i++)
		{
			try
			{
				execStatement("DROP TABLE " + quoteTable(repositoryTableNames[i]));
                if (log.isDetailed()) log.logDetailed("dropped table "+repositoryTableNames[i]);
			}
			catch (KettleException dbe)
			{
                if (log.isDetailed()) log.logDetailed("Unable to drop table: " + repositoryTableNames[i]);
			}
		}
        log.logBasic("Dropped all "+repositoryTableNames.length+" repository tables.");
        
        // perform commit, for some DB's drop is not auto commit.
        commit(); 
        
		return true;
	}

	/**
	 * Update the list in R_STEP_TYPE using the StepLoader StepPlugin entries
	 * 
	 * @return the SQL statements executed
	 * @throws KettleException if the update didn't go as planned.
	 */
	public void updateStepTypes() throws KettleException
	{
		creationHelper.updateStepTypes(new ArrayList<String>(), false, false);
	}
	
	/**
	 * Update the list in R_JOBENTRY_TYPE 
	 * 
	 * @exception KettleException if something went wrong during the update.
	 */
	public void updateJobEntryTypes() throws KettleException
	{
		creationHelper.updateJobEntryTypes(new ArrayList<String>(), false, false);
	}


	public synchronized String toString()
	{
		if (repositoryMeta == null)
			return getClass().getName();
		return repositoryMeta.getName();
	}

	/**
	 * @return Returns the database.
	 */
	public Database getDatabase()
	{
		return connectionDelegate.getDatabase();
	}
	
	/**
	 * @param database The database to set.
	 */
	public void setDatabase(Database database)
	{
		connectionDelegate.setDatabase(database);
		connectionDelegate.setDatabaseMeta(database.getDatabaseMeta());
	}
    
    public synchronized void lockRepository() throws KettleException
    {
        connectionDelegate.lockRepository();
    }
    
    public synchronized void unlockRepository() throws KettleException
    {
        connectionDelegate.unlockRepository();
    }
    
    /**
     * @return a list of all the databases in the repository.
     * @throws KettleException
     */
    public List<DatabaseMeta> getDatabases() throws KettleException
    {
        List<DatabaseMeta> list = new ArrayList<DatabaseMeta>();
        ObjectId[] databaseIDs = getDatabaseIDs(false);
        for (int i=0;i<databaseIDs.length;i++)
        {
            DatabaseMeta databaseMeta = loadDatabaseMeta(databaseIDs[i], null); // reads last version
            list.add(databaseMeta);
        }
            
        return list;
    }
    
    /**
     * @return a list of all the slave servers in the repository.
     * @throws KettleException
     */
    public List<SlaveServer> getSlaveServers() throws KettleException
    {
        List<SlaveServer> list = new ArrayList<SlaveServer>();
        ObjectId[] slaveIDs = getSlaveIDs(false);
        for (int i=0;i<slaveIDs.length;i++)
        {
            SlaveServer slaveServer = loadSlaveServer(slaveIDs[i], null);  // Load last version
            list.add(slaveServer);
        }
            
        return list;
    }

	
	/**
	 * @return the databaseMeta
	 */
	public DatabaseMeta getDatabaseMeta() {
		return connectionDelegate.getDatabaseMeta();
	}
	
	/**
	 * Read all the databases defined in the repository
	 * @return a list of all the databases defined in the repository
	 * @throws KettleException
	 */
	public List<DatabaseMeta> readDatabases() throws KettleException
	{
		List<DatabaseMeta> databases = new ArrayList<DatabaseMeta>();
		ObjectId[] ids = getDatabaseIDs(false);
		for (int i=0;i<ids.length;i++) 
		{
			DatabaseMeta databaseMeta = loadDatabaseMeta(ids[i], null); // reads last versions
			databases.add(databaseMeta);
		}
		return databases;
	}

	/**
	 * @return the useBatchProcessing
	 */
	public boolean isUseBatchProcessing() {
		return connectionDelegate.isUseBatchProcessing();
	}
	
	/**
	 * Set this directory during import to signal that job entries like Trans and Job need to point to job entries relative to this directory.
	 * 
	 * @param importBaseDirectory the base import directory, selected by the user
	 */
	public void setImportBaseDirectory(RepositoryDirectory importBaseDirectory) {
		this.importBaseDirectory = importBaseDirectory;
	}
	
	/**
	 * The directory set during import to signal that job entries like Trans and Job need to point to job entries relative to this directory
	 * 
	 * @return the base import directory, selected by the user
	 */
	public RepositoryDirectory getImportBaseDirectory() {
		return importBaseDirectory;
	}

	/**
     * Create or upgrade repository tables & fields, populate lookup tables, ...
     * 
     * @param monitor The progress monitor to use, or null if no monitor is present.
     * @param upgrade True if you want to upgrade the repository, false if you want to create it.
     * @param statements the list of statements to populate
     * @param dryrun true if we don't actually execute the statements
     * 
     * @throws KettleException in case something goes wrong!
     */
	public void createRepositorySchema(ProgressMonitorListener monitor, boolean upgrade, List<String> statements, boolean dryRun) throws KettleException {
		creationHelper.createRepositorySchema(monitor, upgrade, statements, dryRun);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    

    
    // REQUIRED INTERFACE METHODS...
    //
	public synchronized int countNrStepAttributes(ObjectId id_step, String code) throws KettleException {
		return connectionDelegate.countNrStepAttributes(id_step, code);
	}

	public synchronized int countNrJobEntryAttributes(ObjectId id_jobentry, String code) throws KettleException {
		return connectionDelegate.countNrJobEntryAttributes(id_jobentry, code);
	}

	public synchronized void disconnect() {
		connectionDelegate.disconnect();
	}

	// Job Entry attributes...
	
	// get
	
	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeBoolean(id_jobentry, code);
	}

	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, int nr, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeBoolean(id_jobentry, nr, code);
	}

	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, String code, boolean def) throws KettleException {
		return connectionDelegate.getJobEntryAttributeBoolean(id_jobentry, code, def);
	}

	public long getJobEntryAttributeInteger(ObjectId id_jobentry, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeInteger(id_jobentry, code);
	}

	public long getJobEntryAttributeInteger(ObjectId id_jobentry, int nr, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeInteger(id_jobentry, nr, code);
	}

	public String getJobEntryAttributeString(ObjectId id_jobentry, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeString(id_jobentry, code);
	}

	public String getJobEntryAttributeString(ObjectId id_jobentry, int nr, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeString(id_jobentry, nr, code);
	}
	
	// put
	
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, String value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, String value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, code, value);
	}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, boolean value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, boolean value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, code, value);
	}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, long value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, long value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, code, value);
	}


	// Step attributes 

	// get
	
	public boolean getStepAttributeBoolean(ObjectId id_step, int nr, String code, boolean def) throws KettleException {
		return connectionDelegate.getStepAttributeBoolean(id_step, nr, code, def);
	}
	public boolean getStepAttributeBoolean(ObjectId id_step, int nr, String code) throws KettleException {
		return connectionDelegate.getStepAttributeBoolean(id_step, nr, code);
	}
	public boolean getStepAttributeBoolean(ObjectId id_step, String code) throws KettleException {
		return connectionDelegate.getStepAttributeBoolean(id_step, code);
	}
	public long getStepAttributeInteger(ObjectId id_step, int nr, String code) throws KettleException {
		return connectionDelegate.getStepAttributeInteger(id_step, nr, code);
	}
	public long getStepAttributeInteger(ObjectId id_step, String code) throws KettleException {
		return connectionDelegate.getStepAttributeInteger(id_step, code);
	}
	public String getStepAttributeString(ObjectId id_step, int nr, String code) throws KettleException {
		return connectionDelegate.getStepAttributeString(id_step, nr, code);
	}
	public String getStepAttributeString(ObjectId id_step, String code) throws KettleException {
		return connectionDelegate.getStepAttributeString(id_step, code);
	}

	// put
	
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, String value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, nr, code, value);
	}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, String value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, code, value);
	}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, boolean value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, nr, code, value);
	}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, boolean value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, code, value);
	}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, long value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, nr, code, value);
	}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, long value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, code, value);
	}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, double value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, nr, code, value);
	}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, double value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_transformation, id_step, code, value);
	}

	

	/**
	 * This method is only used to check backward compatibility with the 2.x series.
	 * 
	 * @param id_step
	 * @param nr
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public ObjectId findStepAttributeID(ObjectId id_step, int nr, String code) throws KettleException {
		return connectionDelegate.findStepAttributeID(id_step, nr, code);
	}


	public void execStatement(String sql) throws KettleException {
		connectionDelegate.getDatabase().execStatement(sql);
	}
	
	public void loadJobEntry(JobEntryBase jobEntryBase, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
		jobEntryDelegate.loadJobEntryBase(jobEntryBase, id_jobentry, databases, slaveServers);
	}

	public ObjectId getClusterID(String name) throws KettleException {
		return clusterSchemaDelegate.getClusterID(name);
	}

	public ObjectId getDatabaseID(String name) throws KettleException {
		return databaseDelegate.getDatabaseID(name);
	}

	public ObjectId getJobId(String name, RepositoryDirectory repositoryDirectory) throws KettleException {
		return jobDelegate.getJobID(name, repositoryDirectory.getObjectId());
	}

	public ObjectId getPartitionSchemaID(String name) throws KettleException {
		return partitionSchemaDelegate.getPartitionSchemaID(name);
	}

	public ObjectId getSlaveID(String name) throws KettleException {
		return slaveServerDelegate.getSlaveID(name);
	}

	public ObjectId getTransformationID(String name, RepositoryDirectory repositoryDirectory) throws KettleException {
		return transDelegate.getTransformationID(name, repositoryDirectory.getObjectId());
	}

	public ObjectId insertJobEntry(ObjectId id_job, JobEntryBase jobEntryBase) throws KettleException {
		return jobEntryDelegate.insertJobEntry(id_job, jobEntryBase);
	}
	
	public DatabaseMeta loadDatabaseMetaFromStepAttribute(ObjectId id_step, String code, List<DatabaseMeta> databases) throws KettleException {
		
		long id_database = getStepAttributeInteger(id_step, code);
		if (id_database<=0) {
			return null;
		}
		return DatabaseMeta.findDatabase(databases, new LongObjectId(id_database));
	}

	/**
	 * This method saves the object ID of the database object (if not null) in the step attributes
	 * @param id_transformation
	 * @param id_step
	 * @param string
	 * @param database
	 */
	public void saveDatabaseMetaStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, DatabaseMeta database) throws KettleException {
		ObjectId id = null;
		if (database!=null) {
			id = database.getObjectId();
			Long id_database = id==null ? Long.valueOf(-1L) : new LongObjectId(id).longValue();
			saveStepAttribute(id_transformation, id_step, code, id_database);
		}
	}

	public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute(ObjectId id_jobentry, String nameCode, String idCode, List<DatabaseMeta> databases) throws KettleException {
		
		long id_database = getJobEntryAttributeInteger(id_jobentry, idCode);
		if (id_database<=0) {
			String name = getJobEntryAttributeString(id_jobentry, nameCode);
			if (name==null) {
				return null;
			}
			return DatabaseMeta.findDatabase(databases, name);
		}
		return DatabaseMeta.findDatabase(databases, new LongObjectId(id_database));
	}
	
	
	/**
	 * This method saves the object ID of the database object (if not null) in the step attributes
	 * @param id_transformation
	 * @param id_step
	 * @param linkCode
	 * @param nameCode
	 * @param idCode
	 * @param database
	 */
	public void saveDatabaseMetaJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String nameCode, String idCode, DatabaseMeta database) throws KettleException {
		ObjectId id = null;
		if (database!=null) {
			id = database.getObjectId();
			Long id_database = id==null ? Long.valueOf(-1L) : new LongObjectId(id).longValue();
			
			// Save both the ID and the name of the database connection...
			//
			saveJobEntryAttribute(id_job, id_jobentry, idCode, id_database);
			saveJobEntryAttribute(id_job, id_jobentry, nameCode, id_database);
			
			insertJobEntryDatabase(id_job, id_jobentry, id);
		}
	}

	/**
	 * Load a condition from the repository with the Object ID stored in a step attribute.
	 * @param id_step
	 * @param code
	 * @return
	 * @throws KettleException 
	 */
	public Condition loadConditionFromStepAttribute(ObjectId id_step, String code) throws KettleException {
		long id_condition = getStepAttributeInteger(id_step, code);
		if (id_condition>0) {
			return loadCondition(new LongObjectId(id_condition)); // this repository uses longs
		} else {
			return null;
		}
		
	}

	/**
	 * This method saves the object ID of the condition object (if not null) in the step attributes
	 * @param id_step
	 * @param code
	 * @param condition
	 */
	public void saveConditionStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, Condition condition) throws KettleException {
		ObjectId id = null;
		if (condition!=null) {
			id = condition.getObjectId();
			if (id==null) {
				id = saveCondition(condition);
			}
			
			Long id_condition = id==null ? Long.valueOf(-1L) : new LongObjectId(id).longValue();
			saveStepAttribute(id_transformation, id_step, code, id_condition);
			
			insertTransStepCondition(id_transformation, id_step, condition.getObjectId());
		}
	}

	/**
	 * @return the securityProvider
	 */
	public KettleDatabaseRepositorySecurityProvider getSecurityProvider() {
		return securityProvider;
	}

	 public KettleDatabaseRepositorySecurityProvider getSecurityManager() {
	    return securityProvider;
	  }

	
	public List<ObjectRevision> getRevisions(RepositoryElementLocationInterface element) throws KettleException {
    throw new UnsupportedOperationException();
	}
	
	public void undeleteObject(RepositoryElementLocationInterface element) throws KettleException {
    throw new UnsupportedOperationException();
	}

	public RepositoryVersionRegistry getVersionRegistry() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<ObjectRevision> getRevisions(ObjectId id) throws KettleException {
    throw new UnsupportedOperationException();
   }
  
  public List<RepositoryObject> getJobAndTransformationObjects(ObjectId id_directory, boolean includeDeleted)
      throws KettleException {
    // TODO not the most efficient impl; also, no sorting is done
    List<RepositoryObject> objs = new ArrayList<RepositoryObject>();
    objs.addAll(getJobObjects(id_directory, includeDeleted));
    objs.addAll(getTransformationObjects(id_directory, includeDeleted));
    return objs;
  }

  public IRepositoryService getService(Class<? extends IRepositoryService> clazz) throws KettleException {
    return serviceMap.get(clazz);
  }

  public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    return serviceList;
  }

  public boolean hasService(Class<? extends IRepositoryService> clazz) throws KettleException {
    return serviceMap.containsKey(clazz);
  }
}