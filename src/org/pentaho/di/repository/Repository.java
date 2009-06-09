package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
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
	 * Connect to the repository 
	 * @param locksource the name of the process or program that has a lock on the repository.  
	 * Only one can connect to the repository at the same time.
	 * If more try to connect simultaneously, an error will be given.
	 * 
	 * @return true if the connection went well, false if we couldn't connect.
	 */
	public boolean connect(String locksource) throws KettleException;
	
	/**
	 * Disconnect from the repository.
	 */
	public void disconnect();

    public boolean isConnected();

	// Common methods...

    /**
     * See if a repository object exists in the repository
     * @param repositoryElement the repository element to verify
     * @return true if the job exists
     * @throws KettleException in case something goes wrong.
     */
    public boolean exists(RepositoryElementInterface repositoryElement) throws KettleException;
    
    public long getTransformationID(String name, RepositoryDirectory repositoryDirectory) throws KettleException;
    public long getJobID(String name, RepositoryDirectory repositoryDirectory) throws KettleException;

    public void save(RepositoryElementInterface repositoryElement) throws KettleException;
    public void save(RepositoryElementInterface repositoryElement, ProgressMonitorListener monitor) throws KettleException;
    public void save(RepositoryElementInterface repositoryElement, ProgressMonitorListener monitor, long parentId, boolean used) throws KettleException;
    
    
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
	
	public void moveTransformation(String transname, long id_directory_from, long id_directory_to) throws KettleException;
	
	public void renameTransformation(long id_transformation, String newname) throws KettleException;

	/**
	 * Delete everything related to a transformation from the repository.
	 * This does not included shared objects : databases, slave servers, cluster and partition schema.
	 * 
	 * @param id_transformation the transformation id to delete
	 * @throws KettleException
	 */
	public void delAllFromTrans(long id_transformation) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Jobs: Loading & saving objects...
	/////////////////////////////////////////////////////////////////////////////////////////////////

	public JobMeta loadJobMeta(String jobname, RepositoryDirectory repdir, ProgressMonitorListener monitor) throws KettleException;
	
    public SharedObjects readJobMetaSharedObjects(JobMeta jobMeta) throws KettleException;

    public void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleException;

	public void renameJob(long id_job, String newname) throws KettleException;

	public void delAllFromJob(long id_job) throws KettleException;
		
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Databases : loading, saving, renaming, etc.
	/////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Load the Database connection Metadata from the repository
	 * @param id_database the id of the database connection to load
	 * @throws KettleException in case something goes wrong with database, connection, etc.
	 */
	public DatabaseMeta loadDatabaseMeta(long id_database) throws KettleException;
	
	/**
	 * Remove a database connection from the repository
	 * @param databaseName The name of the connection to remove
	 * @throws KettleException In case something went wrong: database error, insufficient permissions, depending objects, etc.
	 */
	public void deleteDatabaseMeta(String databaseName) throws KettleException;

	public long[] getDatabaseIDs(long id_transformation) throws KettleException;

	public long[] getDatabaseIDs() throws KettleException;
    
    public long[] getDatabaseAttributeIDs(long id_database) throws KettleException;

	public String[] getDatabaseNames() throws KettleException;
	
	public void renameDatabase(long id_database, String newname) throws KettleException;

	/**
	 * Read all the databases defined in the repository
	 * @return a list of all the databases defined in the repository
	 * @throws KettleException
	 */
	public List<DatabaseMeta> readDatabases() throws KettleException;

	public long getDatabaseID(String name) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// ClusterSchema
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
    public ClusterSchema loadClusterSchema(long id_cluster_schema, List<SlaveServer> slaveServers) throws KettleException;

    public long[] getClusterIDs() throws KettleException;

    public String[] getClusterNames() throws KettleException;

    public long getClusterID(String name) throws KettleException;

    public void delClusterSchema(long id_cluster) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // SlaveServer
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
    public SlaveServer loadSlaveServer(long id_slave_server) throws KettleException;
	
    public long[] getSlaveIDs() throws KettleException;

    public long[] getSlaveIDs(long id_cluster_schema) throws KettleException;

    public String[] getSlaveNames() throws KettleException;
    
    /**
     * @return a list of all the slave servers in the repository.
     * @throws KettleException
     */
    public List<SlaveServer> getSlaveServers() throws KettleException;

    public long getSlaveID(String name) throws KettleException;

    public void delSlave(long id_slave) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // PartitionSchema
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	public PartitionSchema loadPartitionSchema(long id_partition_schema) throws KettleException;

    public long[] getPartitionSchemaIDs() throws KettleException;

    public long[] getPartitionIDs(long id_partition_schema) throws KettleException;

    public String[] getPartitionSchemaNames() throws KettleException;

	public long getPartitionSchemaID(String name) throws KettleException;
	
    public void delPartitionSchema(long id_partition_schema) throws KettleException;


	/////////////////////////////////////////////////////////////////////////////////////////////////
	// ValueMetaAndData
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	public ValueMetaAndData loadValueMetaAndData(long id_value) throws KettleException;
	 
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// NotePadMeta
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public NotePadMeta loadNotePadMeta(long id_note) throws KettleException;

	public void saveNotePadMeta(NotePadMeta note, long id_transformation) throws KettleException;

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Directory stuff
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public RepositoryDirectory loadRepositoryDirectoryTree() throws KettleException;
	
	public RepositoryDirectory loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException;

    public RepositoryDirectory refreshRepositoryDirectoryTree() throws KettleException;
    
    /**
     * @return Returns the directoryTree.
     */
    public RepositoryDirectory getDirectoryTree();

    /**
     * @param directoryTree The directoryTree to set.
     */
    public void setDirectoryTree(RepositoryDirectory directoryTree);


	public void saveRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

	public void delRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

	public void renameRepositoryDirectory(RepositoryDirectory dir) throws KettleException;

	@Deprecated
	public long getRootDirectoryID() throws KettleException;

	@Deprecated
	public int getNrSubDirectories(long id_directory) throws KettleException;

	@Deprecated
	public long[] getSubDirectoryIDs(long id_directory) throws KettleException;

	/**
	 * Create a new directory, possibly by creating several sub-directies of / at the same time.
	 * 
	 * @param parentDirectory the parent directory
	 * @param directoryPath The path to the new Repository Directory, to be created.
	 * @return The created sub-directory
	 * @throws KettleException In case something goes wrong
	 */
	public RepositoryDirectory createRepositoryDirectory(RepositoryDirectory parentDirectory, String directoryPath) throws KettleException;
	
	public String[] getTransformationNames(long id_directory) throws KettleException;
    
    public List<RepositoryObject> getJobObjects(long id_directory) throws KettleException;

    public List<RepositoryObject> getTransformationObjects(long id_directory) throws KettleException;


	public String[] getJobNames(long id_directory) throws KettleException;

	public String[] getDirectoryNames(long id_directory) throws KettleException;

	public String[] getJobNames() throws KettleException;


	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Logging...
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
    /**
     * Insert an entry in the audit trail of the repository.
     * This is an optional operation and depends on the capabilities of the underlying repository.
     * 
     * @param The description to be put in the audit trail of the repository.
     */
    public long insertLogEntry(String description) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
    // Relationships between objects !!!!!!!!!!!!!!!!!!!!!!  <-----------------
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	public void insertTransNote(long id_transformation, long id_note) throws KettleException;
	public void insertJobNote(long id_job, long id_note) throws KettleException;
	public void insertStepDatabase(long id_transformation, long id_step, long id_database) throws KettleException;
	public void insertJobEntryDatabase(long id_job, long id_jobentry, long id_database) throws KettleException;
    public long insertTransformationPartitionSchema(long id_transformation, long id_partition_schema) throws KettleException;
    public long insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException;
    public long insertTransformationCluster(long id_transformation, long id_cluster) throws KettleException;
    public long insertTransformationSlave(long id_transformation, long id_slave) throws KettleException;
	public void insertTransStepCondition(long id_transformation, long id_step, long id_condition) throws KettleException;
	
	public long[] getTransNoteIDs(long id_transformation) throws KettleException;
	public long[] getJobNoteIDs(long id_job) throws KettleException;

    public long[] getTransformationPartitionSchemaIDs(long id_transformation) throws KettleException;
    public long[] getTransformationClusterSchemaIDs(long id_transformation) throws KettleException;

    public String[] getTransformationsUsingDatabase(long id_database) throws KettleException;
	
	public String[] getJobsUsingDatabase(long id_database) throws KettleException;
	
    public String[] getClustersUsingSlave(long id_slave) throws KettleException;

	public String[] getTransformationsUsingSlave(long id_slave) throws KettleException;
    
    public String[] getTransformationsUsingPartitionSchema(long id_partition_schema) throws KettleException;
    
    public String[] getTransformationsUsingCluster(long id_cluster) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Condition
	/////////////////////////////////////////////////////////////////////////////////////////////////
    
	/**
     *  
	 * Read a condition from the repository.
	 * @param id_condition The condition id
	 * @throws KettleException if something goes wrong.
	 */
	public Condition loadCondition(long id_condition) throws KettleException;
	
	public long saveCondition(Condition condition) throws KettleException;

	public long saveCondition(Condition condition, long id_condition_parent) throws KettleException;

	public long[] getSubConditionIDs(long id_condition) throws KettleException;

	public long[] getConditionIDs(long id_transformation) throws KettleException;

	public void delCondition(long id_condition) throws KettleException;

	
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
	
	public boolean getStepAttributeBoolean(long id_step, int nr, String code, boolean def) throws KettleException;
	public boolean getStepAttributeBoolean(long id_step, int nr, String code) throws KettleException;
	public boolean getStepAttributeBoolean(long id_step, String code) throws KettleException;
	public long getStepAttributeInteger(long id_step, int nr, String code) throws KettleException;
	public long getStepAttributeInteger(long id_step, String code) throws KettleException;
	public String getStepAttributeString(long id_step, int nr, String code) throws KettleException;
	public String getStepAttributeString(long id_step, String code) throws KettleException;

	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, String value) throws KettleException;
	public void saveStepAttribute(long id_job, long id_jobentry, String code, String value) throws KettleException;
	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, boolean value) throws KettleException;
	public void saveStepAttribute(long id_job, long id_jobentry, String code, boolean value) throws KettleException;
	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, long value) throws KettleException;
	public void saveStepAttribute(long id_job, long id_jobentry, String code, long value) throws KettleException;
	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, double value) throws KettleException;
	public void saveStepAttribute(long id_job, long id_jobentry, String code, double value) throws KettleException;
	
	public int countNrStepAttributes(long id_step, String code) throws KettleException;
	
	public long findStepAttributeID(long id_step, int nr, String code) throws KettleException;

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Attributes for job entries...
	/////////////////////////////////////////////////////////////////////////////////////////////////

	public int countNrJobEntryAttributes(long id_jobentry, String code) throws KettleException;
	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code) throws KettleException;
	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code) throws KettleException;
	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code, boolean def) throws KettleException;
	public long getJobEntryAttributeInteger(long id_jobentry, String code) throws KettleException;
	public long getJobEntryAttributeInteger(long id_jobentry, int nr, String code) throws KettleException;
	public String getJobEntryAttributeString(long id_jobentry, String code) throws KettleException;
	public String getJobEntryAttributeString(long id_jobentry, int nr, String code) throws KettleException;
	
	public void saveJobEntryAttribute(long id_job, long id_jobentry, int nr, String code, String value) throws KettleException;
	public void saveJobEntryAttribute(long id_job, long id_jobentry, String code, String value) throws KettleException;
	public void saveJobEntryAttribute(long id_job, long id_jobentry, int nr, String code, boolean value) throws KettleException;
	public void saveJobEntryAttribute(long id_job, long id_jobentry, String code, boolean value) throws KettleException;
	public void saveJobEntryAttribute(long id_job, long id_jobentry, int nr, String code, long value) throws KettleException;
	public void saveJobEntryAttribute(long id_job, long id_jobentry, String code, long value) throws KettleException;
}
