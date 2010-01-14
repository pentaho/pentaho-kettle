package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;

public interface Repository {
	
	/**
	 * @return The name of the repository
	 */
	public String getName();
	
	/**
	 * Get the repository version.
	 * @return The repository version as a string
	 */
	public String getVersion();
	
	/**
	 * @return the metadata describing this repository.
	 */
	public RepositoryMeta getRepositoryMeta();
	
	/**
	 * @return the currently logged on user. (also available through the repository security provider)
	 */
	public UserInfo getUserInfo();
	
	/**
	 * @return The security provider for this repository.
	 */
	public RepositorySecurityProvider getSecurityProvider();
		
	/**
	 * @return the logging channel of this repository
	 */
	public LogChannelInterface getLog();
	
	/**
	 * Connect to the repository.  Make sure you don't connect more than once to the same repository with this repository object. 
	 * 
	 * @throws KettleSecurityException in case the supplied user or password is incorrect.
	 * @throws KettleException in case there is a general unexpected error OR if we're already connected to the repository.
	 */
	public void connect() throws KettleException, KettleSecurityException;
	
	/**
	 * Disconnect from the repository.
	 */
	public void disconnect();

    public boolean isConnected();
    
    
    /** Initialize the repository with the repository metadata and user information.
     * */
    public void init(RepositoryMeta repositoryMeta, UserInfo userInfo);
    
	// Common methods...

    /**
     * See if a repository object exists in the repository
     * 
     * @param name the name of the object
     * @param repositoryDirectory the directory in which it should reside
     * @param objectType the type of repository object
     * @return true if the job exists
     * @throws KettleException in case something goes wrong.
     */
    public boolean exists(String name, RepositoryDirectory repositoryDirectory, RepositoryObjectType objectType) throws KettleException;
    
    public ObjectId getTransformationID(String name, RepositoryDirectory repositoryDirectory) throws KettleException;
    public ObjectId getJobId(String name, RepositoryDirectory repositoryDirectory) throws KettleException;

    public void save(RepositoryElementInterface repositoryElement, String versionComment, ProgressMonitorListener monitor) throws KettleException;
        
	
    // Transformations : Loading & saving objects...
	
	/**
	 * Load a transformation with a name from a folder in the repository
	 * 
	 *  @param transname the name of the transformation to load
	 *  @param The folder to load it from
	 *  @param monitor the progress monitor to use (UI feedback)
	 *  @param setInternalVariables set to true if you want to automatically set the internal variables of the loaded transformation. (true is the default with very few exceptions!) 
	 *  @param revision the revision to load.  Specify null to load the last version.
	 */
    public TransMeta loadTransformation(String transname, RepositoryDirectory repdir, ProgressMonitorListener monitor, boolean setInternalVariables, String revision) throws KettleException;

	public SharedObjects readTransSharedObjects(TransMeta transMeta) throws KettleException;
	
	/**
   * Move / rename a transformation
   * 
   * @param id The ObjectId of the transformation to move
   * @param newParentDir The RepositoryDirectory that will be the new parent of the transformation (May be null if a move is not desired)
   * @param newName The new name of the transformation (May be null if a rename is not desired)
   * @return The ObjectId of the transformation that was moved 
   * @throws KettleException
   */
	public ObjectId renameTransformation(ObjectId id_transformation, RepositoryDirectory newDirectory, String newName) throws KettleException;

	/**
	 * Delete everything related to a transformation from the repository.
	 * This does not included shared objects : databases, slave servers, cluster and partition schema.
	 * 
	 * @param id_transformation the transformation id to delete
	 * @throws KettleException
	 */
	public void deleteTransformation(ObjectId id_transformation) throws KettleException;
	
	/**
	 * Locks this transformation for exclusive use by the current user of the repository
	 * @param id_transformation the id of the transformation to lock
	 * @param isSessionScoped If isSessionScoped is true then this lock will expire upon the expiration of the current session (either through an automatic or explicit Session.logout); if false, this lock does not expire until explicitly unlocked or automatically unlocked due to a implementation-specific limitation, such as a timeout. 
	 * @param message the lock message 
	 * @throws KettleException in case something goes wrong or the transformation is already locked by someone else.
	 */
	public void lockTransformation(ObjectId id_transformation, String message) throws KettleException;

	/**
	 * Unlocks a transformation, allowing other people to modify it again.
	 * @param id_transformation the id of the transformation to unlock
	 * @throws KettleException in case something goes wrong with the database or connection
	 */
	public void unlockTransformation(ObjectId id_transformation) throws KettleException;

	/**
	 * Return the lock object for this transformation.  Returns null if there is no lock present.
	 * 
	 * @param id_transformation
	 * @return the lock object for this transformation, null if no lock is present.
	 * @throws KettleDatabaseException
	 */
	public RepositoryLock getTransformationLock(ObjectId id_transformation) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Jobs: Loading & saving objects...
	/////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Load a job from the repository
	 * @param jobname the name 
	 * @param repdir the directory
	 * @param monitor the progress monitor or null
	 * @param revision the revision to load.  Specify null to load the last version.
	 */
	public JobMeta loadJob(String jobname, RepositoryDirectory repdir, ProgressMonitorListener monitor, String revision) throws KettleException;
	
    public SharedObjects readJobMetaSharedObjects(JobMeta jobMeta) throws KettleException;

    /**
     * Move / rename a job
     * 
     * @param id The ObjectId of the job to move
     * @param newParentDir The RepositoryDirectory that will be the new parent of the job (May be null if a move is not desired)
     * @param newName The new name of the job (May be null if a rename is not desired)
     * @return The ObjectId of the job that was moved 
     * @throws KettleException
     */
    public ObjectId renameJob(ObjectId id_job, RepositoryDirectory newDirectory, String newName) throws KettleException;

	public void deleteJob(ObjectId id_job) throws KettleException;
		
	/**
	 * Locks this job for exclusive use by the current user of the repository
	 * @param id_job the id of the job to lock 
	 * @param message the lock message
	 * @throws KettleException in case something goes wrong or the job is already locked by someone else.
	 */
	public void lockJob(ObjectId id_job, String message) throws KettleException;
	
	/**
	 * Unlocks a job, allowing other people to modify it again.
	 * @param id_job the id of the transformation to unlock
	 * @throws KettleException in case something goes wrong with the database or connection
	 */
	public void unlockJob(ObjectId id_job) throws KettleException;

	/**
	 * Return the lock object for this job.  Returns null if there is no lock present.
	 * 
	 * @param id_job
	 * @return the lock object for this job, null if no lock is present.
	 * @throws KettleDatabaseException
	 */
	public RepositoryLock getJobLock(ObjectId id_job) throws KettleException;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Databases : loading, saving, renaming, etc.
	/////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Load the Database connection Metadata from the repository
	 * @param id_database the id of the database connection to load
	 * @param revision the revision to load.  Specify null to load the last version.
	 * @throws KettleException in case something goes wrong with database, connection, etc.
	 */
	public DatabaseMeta loadDatabaseMeta(ObjectId id_database, String revision) throws KettleException;
	
	/**
	 * Remove a database connection from the repository
	 * @param databaseName The name of the connection to remove
	 * @throws KettleException In case something went wrong: database error, insufficient permissions, depending objects, etc.
	 */
	public void deleteDatabaseMeta(String databaseName) throws KettleException;

	public ObjectId[] getDatabaseIDs(boolean includeDeleted) throws KettleException;
    
	public String[] getDatabaseNames(boolean includeDeleted) throws KettleException;
	
	public ObjectId renameDatabase(ObjectId id_database, String newname) throws KettleException;

	/**
	 * Read all the databases defined in the repository
	 * @return a list of all the databases defined in the repository
	 * @throws KettleException
	 */
	public List<DatabaseMeta> readDatabases() throws KettleException;

	public ObjectId getDatabaseID(String name) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// ClusterSchema
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
    public ClusterSchema loadClusterSchema(ObjectId id_cluster_schema, List<SlaveServer> slaveServers, String versionLabel) throws KettleException;

    public ObjectId[] getClusterIDs(boolean includeDeleted) throws KettleException;

    public String[] getClusterNames(boolean includeDeleted) throws KettleException;

    public ObjectId getClusterID(String name) throws KettleException;

    public void deleteClusterSchema(ObjectId id_cluster) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // SlaveServer
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
    public SlaveServer loadSlaveServer(ObjectId id_slave_server, String versionLabel) throws KettleException;
	
    public ObjectId[] getSlaveIDs(boolean includeDeleted) throws KettleException;

    public String[] getSlaveNames(boolean includeDeleted) throws KettleException;
    
    /**
     * @return a list of all the slave servers in the repository.
     * @throws KettleException
     */
    public List<SlaveServer> getSlaveServers() throws KettleException;

    public ObjectId getSlaveID(String name) throws KettleException;

    public void deleteSlave(ObjectId id_slave) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // PartitionSchema
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	public PartitionSchema loadPartitionSchema(ObjectId id_partition_schema, String versionLabel) throws KettleException;

    public ObjectId[] getPartitionSchemaIDs(boolean includeDeleted) throws KettleException;

    // public ObjectId[] getPartitionIDs(ObjectId id_partition_schema) throws KettleException;

    public String[] getPartitionSchemaNames(boolean includeDeleted) throws KettleException;

	public ObjectId getPartitionSchemaID(String name) throws KettleException;
	
    public void deletePartitionSchema(ObjectId id_partition_schema) throws KettleException;
	 
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Directory stuff
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public RepositoryDirectory loadRepositoryDirectoryTree() throws KettleException;
	
	// public RepositoryDirectory loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException;

	public void saveRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

	public void deleteRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

	/**
	 * Move / rename a repository directory
	 * 
	 * @param id The ObjectId of the repository directory to move
   * @param newParentDir The RepositoryDirectory that will be the new parent of the repository directory (May be null if a move is not desired)
   * @param newName The new name of the repository directory (May be null if a rename is not desired)
   * @return The ObjectId of the repository directory that was moved 
   * @throws KettleException
	 */
  public ObjectId renameRepositoryDirectory(ObjectId id, RepositoryDirectory newParentDir, String newName) throws KettleException;

  @Deprecated
	public ObjectId renameRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

	/**
	 * Create a new directory, possibly by creating several sub-directies of / at the same time.
	 * 
	 * @param parentDirectory the parent directory
	 * @param directoryPath The path to the new Repository Directory, to be created.
	 * @return The created sub-directory
	 * @throws KettleException In case something goes wrong
	 */
	public RepositoryDirectory createRepositoryDirectory(RepositoryDirectory parentDirectory, String directoryPath) throws KettleException;
	
	public String[] getTransformationNames(ObjectId id_directory, boolean includeDeleted) throws KettleException;
    
    public List<RepositoryObject> getJobObjects(ObjectId id_directory, boolean includeDeleted) throws KettleException;

    public List<RepositoryObject> getTransformationObjects(ObjectId id_directory, boolean includeDeleted) throws KettleException;


	public String[] getJobNames(ObjectId id_directory, boolean includeDeleted) throws KettleException;

	public String[] getDirectoryNames(ObjectId id_directory) throws KettleException;


	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Logging...
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
    /**
     * Insert an entry in the audit trail of the repository.
     * This is an optional operation and depends on the capabilities of the underlying repository.
     * 
     * @param The description to be put in the audit trail of the repository.
     */
    public ObjectId insertLogEntry(String description) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // Relationships between objects !!!!!!!!!!!!!!!!!!!!!!  <-----------------
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	public void insertStepDatabase(ObjectId id_transformation, ObjectId id_step, ObjectId id_database) throws KettleException;
	public void insertJobEntryDatabase(ObjectId id_job, ObjectId id_jobentry, ObjectId id_database) throws KettleException;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Condition
	/////////////////////////////////////////////////////////////////////////////////////////////////
    	
	/**
	 * This method saves the object ID of the condition object (if not null) in the step attributes
	 * @param id_step
	 * @param code
	 * @param condition
	 */
	public void saveConditionStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, Condition condition) throws KettleException;

	/**
	 * Load a condition from the repository with the Object ID stored in a step attribute.
	 * @param id_step
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public Condition loadConditionFromStepAttribute(ObjectId id_step, String code) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Attributes for steps...
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean getStepAttributeBoolean(ObjectId id_step, int nr, String code, boolean def) throws KettleException;
	public boolean getStepAttributeBoolean(ObjectId id_step, int nr, String code) throws KettleException;
	public boolean getStepAttributeBoolean(ObjectId id_step, String code) throws KettleException;
	public long getStepAttributeInteger(ObjectId id_step, int nr, String code) throws KettleException;
	public long getStepAttributeInteger(ObjectId id_step, String code) throws KettleException;
	public String getStepAttributeString(ObjectId id_step, int nr, String code) throws KettleException;
	public String getStepAttributeString(ObjectId id_step, String code) throws KettleException;

	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, String value) throws KettleException;
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, String value) throws KettleException;
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, boolean value) throws KettleException;
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, boolean value) throws KettleException;
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, long value) throws KettleException;
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, long value) throws KettleException;
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, double value) throws KettleException;
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, double value) throws KettleException;
	
	public int countNrStepAttributes(ObjectId id_step, String code) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Attributes for job entries...
	/////////////////////////////////////////////////////////////////////////////////////////////////

	public int countNrJobEntryAttributes(ObjectId id_jobentry, String code) throws KettleException;
	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, String code) throws KettleException;
	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, int nr, String code) throws KettleException;
	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, String code, boolean def) throws KettleException;
	public long getJobEntryAttributeInteger(ObjectId id_jobentry, String code) throws KettleException;
	public long getJobEntryAttributeInteger(ObjectId id_jobentry, int nr, String code) throws KettleException;
	public String getJobEntryAttributeString(ObjectId id_jobentry, String code) throws KettleException;
	public String getJobEntryAttributeString(ObjectId id_jobentry, int nr, String code) throws KettleException;
	
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, String value) throws KettleException;
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, String value) throws KettleException;
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, boolean value) throws KettleException;
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, boolean value) throws KettleException;
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, long value) throws KettleException;
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, long value) throws KettleException;

	/**
	 * This method is introduced to avoid having to go over an integer/string/whatever in the interface and the step code.
	 * 
	 * @param id_step
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public DatabaseMeta loadDatabaseMetaFromStepAttribute(ObjectId id_step, String code, List<DatabaseMeta> databases) throws KettleException;

	/**
	 * This method saves the object ID of the database object (if not null) in the step attributes
	 * @param id_transformation
	 * @param id_step
	 * @param code
	 * @param database
	 */
	public void saveDatabaseMetaStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, DatabaseMeta database) throws KettleException;

	/**
	 * This method is introduced to avoid having to go over an integer/string/whatever in the interface and the job entry code.
	 * 
	 * @param id_step
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute(ObjectId id_jobentry, String nameCode, String idCode, List<DatabaseMeta> databases) throws KettleException;

	/**
	 * This method saves the object ID of the database object (if not null) in the job entry attributes
	 * @param id_job
	 * @param id_jobentry
	 * @param code
	 * @param database
	 */
	public void saveDatabaseMetaJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String nameCode, String idCode, DatabaseMeta database) throws KettleException;

	/**
	 * Get the revision history of a repository element.
	 * If the capabilities of this repositories do not include revision support, this method can return null.
	 * 
	 * @param element the element.  If the ID is specified, this will be taken.  Otherwise it will be looked up.
	 * 
	 * @return The revision history, sorted from first to last.
	 * @throws KettleException in case something goes horribly wrong
	 */
	public List<ObjectRevision> getRevisions(RepositoryElementLocationInterface element) throws KettleException;
	
	/**
	 * Removes he deleted flag from a repository element in the repository.  
	 * If it wasn't deleted, it remains untouched.
	 * 
	 * @param element the repository element to restore
	 * @throws KettleException get throws in case something goes horribly wrong.
	 */
	public void undeleteObject(RepositoryElementLocationInterface element) throws KettleException;
	
	
	/**
	 * Get a hold of the version registry of this repository.
	 * 
	 * @return the version registry.
	 * @throws KettleException in case something goes horribly wrong.
	 */
	public RepositoryVersionRegistry getVersionRegistry() throws KettleException;
}
