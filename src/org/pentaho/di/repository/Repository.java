package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;

public interface Repository extends RepositorySecurityInterface {
	
	/**
	 * @return The name of the repository
	 */
	public String getName();
	
	/**
	 * @return the metadata describing this repository.
	 */
	public RepositoryMeta getRepositoryMeta();
	
	/**
	 * @return The user that is currently using this repository.
	 */
	public UserInfo getUserInfo();
		
	/**
	 * Connect to the repository 
	 * @param locksource the name of the process or program that has a lock on the repository.  
	 * Only one can connect to the repository at the same time.
	 * If more try to connect simultaneously, an error will be given.
	 * 
	 * @throws KettleException in case we couldn't connect to the repository.
	 */
	public void connect(String locksource) throws KettleException;
	
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
     * @param repositoryElement the repository element to verify
     * @return true if the job exists
     * @throws KettleException in case something goes wrong.
     */
    public boolean exists(RepositoryElementInterface repositoryElement) throws KettleException;
    
    public ObjectId getTransformationID(String name, RepositoryDirectory repositoryDirectory) throws KettleException;
    public ObjectId getJobId(String name, RepositoryDirectory repositoryDirectory) throws KettleException;

    public void save(RepositoryElementInterface repositoryElement) throws KettleException;
    public void save(RepositoryElementInterface repositoryElement, ProgressMonitorListener monitor) throws KettleException;
    public void save(RepositoryElementInterface repositoryElement, ProgressMonitorListener monitor, ObjectId parentId, boolean used) throws KettleException;
    
    
	// Transformations : Loading & saving objects...
	
	/**
	 * Load a transformation with a name from a folder in the repository
	 * 
	 *  @param transname the name of the transformation to load
	 *  @param The folder to load it from
	 *  @param monitor the progress monitor to use (UI feedback)
	 *  @param setInternalVariables set to true if you want to automatically set the internal variables of the loaded transformation. (true is the default with very few exceptions!) 
	 */
    public TransMeta loadTransformation(String transname, RepositoryDirectory repdir, ProgressMonitorListener monitor, boolean setInternalVariables) throws KettleException;

	public SharedObjects readTransSharedObjects(TransMeta transMeta) throws KettleException;
	
	public ObjectId renameTransformation(ObjectId id_transformation, RepositoryDirectory newDirectory, String newName) throws KettleException;

	/**
	 * Delete everything related to a transformation from the repository.
	 * This does not included shared objects : databases, slave servers, cluster and partition schema.
	 * 
	 * @param id_transformation the transformation id to delete
	 * @throws KettleException
	 */
	public void delAllFromTrans(ObjectId id_transformation) throws KettleException;
	
	/**
	 * Locks this transformation for exclusive use by the current user of the repository
	 * @param id_transformation the id of the transformation to lock
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
	 * @return a list of the IDs of all the transformations that are locked at this time. 
	 * @throws KettleException
	 */
	public List<RepositoryLock> getTransformationLocks() throws KettleException; 

	/**
	 * Return the lock object for this transformation.  Returns null if there is no lock present.
	 * 
	 * @param id_transformation
	 * @return the lock object for this transformation, null if no lock is present.
	 * @throws KettleDatabaseException
	 */
	public RepositoryLock getTransformationLock(ObjectId id_transformation) throws KettleDatabaseException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Jobs: Loading & saving objects...
	/////////////////////////////////////////////////////////////////////////////////////////////////

	public JobMeta loadJob(String jobname, RepositoryDirectory repdir, ProgressMonitorListener monitor) throws KettleException;
	
    public SharedObjects readJobMetaSharedObjects(JobMeta jobMeta) throws KettleException;

    public ObjectId renameJob(ObjectId id_job, RepositoryDirectory newDirectory, String newName) throws KettleException;

	public void delAllFromJob(ObjectId id_job) throws KettleException;
		
	/**
	 * @return a list of the IDs of all the jobs that are locked at this time. 
	 * @throws KettleException
	 */
	public List<RepositoryLock> getJobLocks() throws KettleException; 

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
	public RepositoryLock getJobLock(ObjectId id_job) throws KettleDatabaseException;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Databases : loading, saving, renaming, etc.
	/////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Load the Database connection Metadata from the repository
	 * @param id_database the id of the database connection to load
	 * @throws KettleException in case something goes wrong with database, connection, etc.
	 */
	public DatabaseMeta loadDatabaseMeta(ObjectId id_database) throws KettleException;
	
	/**
	 * Remove a database connection from the repository
	 * @param databaseName The name of the connection to remove
	 * @throws KettleException In case something went wrong: database error, insufficient permissions, depending objects, etc.
	 */
	public void deleteDatabaseMeta(String databaseName) throws KettleException;

	public ObjectId[] getTransformationDatabaseIDs(ObjectId id_transformation) throws KettleException;

	public ObjectId[] getDatabaseIDs() throws KettleException;
    
    public ObjectId[] getDatabaseAttributeIDs(ObjectId id_database) throws KettleException;

	public String[] getDatabaseNames() throws KettleException;
	
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
	
    public ClusterSchema loadClusterSchema(ObjectId id_cluster_schema, List<SlaveServer> slaveServers) throws KettleException;

    public ObjectId[] getClusterIDs() throws KettleException;

    public String[] getClusterNames() throws KettleException;

    public ObjectId getClusterID(String name) throws KettleException;

    public void delClusterSchema(ObjectId id_cluster) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // SlaveServer
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
    public SlaveServer loadSlaveServer(ObjectId id_slave_server) throws KettleException;
	
    public ObjectId[] getSlaveIDs() throws KettleException;

    public ObjectId[] getClusterSlaveIDs(ObjectId id_cluster_schema) throws KettleException;

    public String[] getSlaveNames() throws KettleException;
    
    /**
     * @return a list of all the slave servers in the repository.
     * @throws KettleException
     */
    public List<SlaveServer> getSlaveServers() throws KettleException;

    public ObjectId getSlaveID(String name) throws KettleException;

    public void delSlave(ObjectId id_slave) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // PartitionSchema
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	public PartitionSchema loadPartitionSchema(ObjectId id_partition_schema) throws KettleException;

    public ObjectId[] getPartitionSchemaIDs() throws KettleException;

    // public ObjectId[] getPartitionIDs(ObjectId id_partition_schema) throws KettleException;

    public String[] getPartitionSchemaNames() throws KettleException;

	public ObjectId getPartitionSchemaID(String name) throws KettleException;
	
    public void delPartitionSchema(ObjectId id_partition_schema) throws KettleException;


	/////////////////////////////////////////////////////////////////////////////////////////////////
	// ValueMetaAndData
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	public ValueMetaAndData loadValueMetaAndData(ObjectId id_value) throws KettleException;
	 
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// NotePadMeta
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	// public void saveNotePadMeta(NotePadMeta note, ObjectId id_transformation) throws KettleException;

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Directory stuff
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public RepositoryDirectory loadRepositoryDirectoryTree() throws KettleException;
	
	public RepositoryDirectory loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException;

	public void saveRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

	public void delRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

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
	
	public String[] getTransformationNames(ObjectId id_directory) throws KettleException;
    
    public List<RepositoryObject> getJobObjects(ObjectId id_directory) throws KettleException;

    public List<RepositoryObject> getTransformationObjects(ObjectId id_directory) throws KettleException;


	public String[] getJobNames(ObjectId id_directory) throws KettleException;

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
    
	public void insertTransNote(ObjectId id_transformation, ObjectId id_note) throws KettleException;
	public void insertJobNote(ObjectId id_job, ObjectId id_note) throws KettleException;
	public void insertStepDatabase(ObjectId id_transformation, ObjectId id_step, ObjectId id_database) throws KettleException;
	public void insertJobEntryDatabase(ObjectId id_job, ObjectId id_jobentry, ObjectId id_database) throws KettleException;
    public ObjectId insertTransformationPartitionSchema(ObjectId id_transformation, ObjectId id_partition_schema) throws KettleException;
    public ObjectId insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException;
    public ObjectId insertTransformationCluster(ObjectId id_transformation, ObjectId id_cluster) throws KettleException;
    public ObjectId insertTransformationSlave(ObjectId id_transformation, ObjectId id_slave) throws KettleException;
	public void insertTransStepCondition(ObjectId id_transformation, ObjectId id_step, ObjectId id_condition) throws KettleException;
	
	public ObjectId[] getTransNoteIDs(ObjectId id_transformation) throws KettleException;
	public ObjectId[] getJobNoteIDs(ObjectId id_job) throws KettleException;

    public ObjectId[] getTransformationPartitionSchemaIDs(ObjectId id_transformation) throws KettleException;
    public ObjectId[] getTransformationClusterSchemaIDs(ObjectId id_transformation) throws KettleException;

    public String[] getTransformationsUsingDatabase(ObjectId id_database) throws KettleException;
	
	public String[] getJobsUsingDatabase(ObjectId id_database) throws KettleException;
	
    public String[] getClustersUsingSlave(ObjectId id_slave) throws KettleException;

	public String[] getTransformationsUsingSlave(ObjectId id_slave) throws KettleException;
    
    public String[] getTransformationsUsingPartitionSchema(ObjectId id_partition_schema) throws KettleException;
    
    public String[] getTransformationsUsingCluster(ObjectId id_cluster) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Condition
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	/**
     *  
	 * Read a condition from the repository.
	 * @param id_condition The condition id
	 * @throws KettleException if something goes wrong.
	 */
	public Condition loadCondition(ObjectId id_condition) throws KettleException;
	
	public ObjectId saveCondition(Condition condition) throws KettleException;

	public ObjectId saveCondition(Condition condition, ObjectId id_condition_parent) throws KettleException;

	public ObjectId[] getSubConditionIDs(ObjectId id_condition) throws KettleException;

	public ObjectId[] getTransformationConditionIDs(ObjectId id_transformation) throws KettleException;

	public void delCondition(ObjectId id_condition) throws KettleException;

	
	  /////////////////////////////////////////////////////
	 //// SUSPECT METHODS, ARE THEY REALLY NEEDED?    ////
    /////////////////////////////////////////////////////
	
	/**
	 * Return the major repository version.
	 * @return the major repository version.
	 */
	public int getMajorVersion();
	/**
	 * Return the minor repository version.
	 * @return the minor repository version.
	 */
	public int getMinorVersion();

	/**
	 * Get the repository version.
	 * @return The repository version as major version + "." + minor version
	 */
	public String getVersion();
	
	
	
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
	
	public ObjectId findStepAttributeID(ObjectId id_step, int nr, String code) throws KettleException;

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
	public DatabaseMeta loadDatabaseMetaFromStepAttribute(ObjectId id_step, String code) throws KettleException;

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
	public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute(ObjectId id_jobentry, String code) throws KettleException;

	/**
	 * This method saves the object ID of the database object (if not null) in the job entry attributes
	 * @param id_job
	 * @param id_jobentry
	 * @param code
	 * @param database
	 */
	public void saveDatabaseMetaJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, DatabaseMeta database) throws KettleException;

	/**
	 * Load a condition from the repository with the Object ID stored in a step attribute.
	 * @param id_step
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public Condition loadConditionFromStepAttribute(ObjectId id_step, String code) throws KettleException;

	/**
	 * This method saves the object ID of the condition object (if not null) in the step attributes
	 * @param id_step
	 * @param code
	 * @param condition
	 */
	public void saveConditionStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, Condition condition) throws KettleException;

}
