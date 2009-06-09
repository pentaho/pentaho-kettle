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

package org.pentaho.di.repository;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.delegates.RepositoryClusterSchemaDelegate;
import org.pentaho.di.repository.delegates.RepositoryConditionDelegate;
import org.pentaho.di.repository.delegates.RepositoryConnectionDelegate;
import org.pentaho.di.repository.delegates.RepositoryDatabaseDelegate;
import org.pentaho.di.repository.delegates.RepositoryDirectoryDelegate;
import org.pentaho.di.repository.delegates.RepositoryJobDelegate;
import org.pentaho.di.repository.delegates.RepositoryJobEntryDelegate;
import org.pentaho.di.repository.delegates.RepositoryNotePadDelegate;
import org.pentaho.di.repository.delegates.RepositoryPartitionSchemaDelegate;
import org.pentaho.di.repository.delegates.RepositoryPermissionDelegate;
import org.pentaho.di.repository.delegates.RepositoryProfileDelegate;
import org.pentaho.di.repository.delegates.RepositorySlaveServerDelegate;
import org.pentaho.di.repository.delegates.RepositoryStepDelegate;
import org.pentaho.di.repository.delegates.RepositoryTransDelegate;
import org.pentaho.di.repository.delegates.RepositoryUserDelegate;
import org.pentaho.di.repository.delegates.RepositoryValueDelegate;
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
public class KettleDatabaseRepository extends BaseRepository implements Repository
{
//	private static Class<?> PKG = Repository.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public RepositoryTransDelegate transDelegate;
	public RepositoryJobDelegate jobDelegate;
	public RepositoryDatabaseDelegate databaseDelegate;
	public RepositorySlaveServerDelegate slaveServerDelegate;
	public RepositoryClusterSchemaDelegate clusterSchemaDelegate;
	public RepositoryPartitionSchemaDelegate partitionSchemaDelegate;
	public RepositoryDirectoryDelegate directoryDelegate;
	public RepositoryConnectionDelegate connectionDelegate;
	public RepositoryProfileDelegate profileDelegate;
	public RepositoryUserDelegate userDelegate;
	public RepositoryPermissionDelegate permissionDelegate;
	public RepositoryConditionDelegate conditionDelegate;
	public RepositoryValueDelegate valueDelegate;
	public RepositoryNotePadDelegate notePadDelegate;
	public RepositoryStepDelegate stepDelegate;
	public RepositoryJobEntryDelegate jobEntryDelegate;
	
	public KettleDatabaseRepository(RepositoryMeta repositoryMeta, UserInfo userinfo)
	{
		super(repositoryMeta, userinfo);
		
		// Create the delegates...
		//
		this.transDelegate = new RepositoryTransDelegate(this);
		this.jobDelegate = new RepositoryJobDelegate(this);
		this.databaseDelegate = new RepositoryDatabaseDelegate(this);
		this.slaveServerDelegate = new RepositorySlaveServerDelegate(this);
		this.clusterSchemaDelegate = new RepositoryClusterSchemaDelegate(this);
		this.partitionSchemaDelegate = new RepositoryPartitionSchemaDelegate(this);
		this.directoryDelegate = new RepositoryDirectoryDelegate(this);
		this.connectionDelegate = new RepositoryConnectionDelegate(this, repositoryMeta.getConnection());
		this.profileDelegate = new RepositoryProfileDelegate(this);
		this.userDelegate = new RepositoryUserDelegate(this);
		this.permissionDelegate = new RepositoryPermissionDelegate(this);
		this.conditionDelegate = new RepositoryConditionDelegate(this);
		this.valueDelegate = new RepositoryValueDelegate(this);
		this.notePadDelegate = new RepositoryNotePadDelegate(this);
		this.stepDelegate = new RepositoryStepDelegate(this);
		this.jobEntryDelegate = new RepositoryJobEntryDelegate(this);
		
		this.creationHelper = new RepositoryCreationHelper(this);
	}
	
	/**
	 * Connect to the repository 
	 * @param locksource
	 * @return true if the connection went well, false if we couldn't connect.
	 */
	public synchronized boolean connect(String locksource) throws KettleException {
		return connectionDelegate.connect(locksource);
	}

	public synchronized void commit() throws KettleException {
		connectionDelegate.commit();
	}

	public synchronized void rollback() {
		connectionDelegate.rollback();
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
    
    public TransMeta loadTransformation(String transname, RepositoryDirectory repdir, ProgressMonitorListener monitor, boolean setInternalVariables) throws KettleException {
    	return transDelegate.loadTransformation(new TransMeta(), transname, repdir, monitor, setInternalVariables);
	}
        
	public SharedObjects readTransSharedObjects(TransMeta transMeta) throws KettleException {
		return transDelegate.readTransSharedObjects(transMeta);
	}
	
	public synchronized void moveTransformation(String transname, long id_directory_from, long id_directory_to) throws KettleException {
		transDelegate.moveTransformation(transname, id_directory_from, id_directory_to);
	}
	
	public synchronized void renameTransformation(long id_transformation, String newname) throws KettleException {
		transDelegate.renameTransformation(id_transformation, newname);
	}


    // JobMeta
    
	
	/** Load a job in a directory
	 * 
	 * @param jobname The name of the job
	 * @param repdir The directory in which the job resides.
	 * @param the monitor to use as feedback in a UI (or null if not used)
	 * @throws KettleException
	 */
	public JobMeta loadJobMeta(String jobname, RepositoryDirectory repdir, ProgressMonitorListener monitor) throws KettleException {
		return jobDelegate.loadJobMeta(jobname, repdir, monitor);
	}
	
    public SharedObjects readJobMetaSharedObjects(JobMeta jobMeta) throws KettleException {
    	return jobDelegate.readSharedObjects(jobMeta);
    }

	public synchronized void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleException {
		jobDelegate.moveJob(jobname, id_directory_from, id_directory_to);
	}

	public synchronized void renameJob(long id_job, String newname) throws KettleException {
		jobDelegate.renameJob(id_job, newname);
	}

	
	// Common methods...
	//////////////////////////////

    /**
     * See if a repository object exists in the repository
     * @param repositoryElement the repository element to verify
     * @return true if the job exists
     * @throws KettleException in case something goes wrong.
     */
    public boolean exists(RepositoryElementInterface repositoryElement) throws KettleException {
    	
    	if (JobMeta.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
    		return jobDelegate.existsJobMeta(repositoryElement);
    	} else

    	if (TransMeta.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
    		return transDelegate.existsTransMeta(repositoryElement);
    	} else
    	
    	if (UserInfo.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
    		return userDelegate.existsUserInfo(repositoryElement);
    	} else

    	throw new KettleException("We can't verify the existance of repository element type ["+repositoryElement.getRepositoryElementType()+"]");
    }
    
    public void save(RepositoryElementInterface repositoryElement) throws KettleException {
    	save(repositoryElement, null);
    }

    public void save(RepositoryElementInterface repositoryElement, ProgressMonitorListener monitor) throws KettleException {
    	save(repositoryElement, monitor, 0L, false);
    }
    	 
    public void save(RepositoryElementInterface repositoryElement, ProgressMonitorListener monitor, long parentId, boolean used) throws KettleException {
    	
    	try {
    		lockRepository();
        
	    	if (JobMeta.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
	    		jobDelegate.saveJob((JobMeta)repositoryElement, monitor);
	    	} else
	
	    	if (TransMeta.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
	    		transDelegate.saveTransformation((TransMeta)repositoryElement, monitor);
	    	} else
	    	
	    	if (DatabaseMeta.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
	    		databaseDelegate.saveDatabaseMeta((DatabaseMeta)repositoryElement);
	    	} else
	
	    	if (SlaveServer.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
	    		slaveServerDelegate.saveSlaveServer((SlaveServer)repositoryElement, parentId, used);
	    	} else

	    	if (PartitionSchema.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
	    		partitionSchemaDelegate.savePartitionSchema((PartitionSchema)repositoryElement, parentId, used);
	    	} else

	    	if (ClusterSchema.REPOSITORY_ELEMENT_TYPE.equals(repositoryElement.getRepositoryElementType())) {
	    		clusterSchemaDelegate.saveClusterSchema((ClusterSchema)repositoryElement, parentId, used);
	    	} else

	    	throw new KettleException("We can't save the element with type ["+repositoryElement.getRepositoryElementType()+"] in the repository");
	    	
			// Automatically commit changes to these elements.
	    	//
			commit();
    	} finally {
            unlockRepository();
    	}
    }
    
    // ProfileMeta

    public ProfileMeta loadProfileMeta(long id_profile) throws KettleException {
    	return profileDelegate.loadProfileMeta(new ProfileMeta(), id_profile);
    }
    
    public void saveProfile(ProfileMeta profileMeta) throws KettleException {
    	profileDelegate.saveProfileMeta(profileMeta);
    }
    
    
    // UserInfo

    public UserInfo loadUserInfo(String login) throws KettleException {
    	return userDelegate.loadUserInfo(new UserInfo(), login);
    }
    
    public UserInfo loadUserInfo(String login, String password) throws KettleException {
    	return userDelegate.loadUserInfo(new UserInfo(), login, password);
    }
    
    public void saveUserInfo(UserInfo userInfo) throws KettleException {
    	userDelegate.saveUserInfo(userInfo);
    }
	 
    
    // PermissionMeta
    
	/**
	 * Load a permission from the repository
	 * 
	 * @param id_permission The id of the permission to load
	 * @throws KettleException
	 */
	public PermissionMeta loadPermissionMeta(long id_permission) throws KettleException {
		return permissionDelegate.loadPermissionMeta(id_permission);
	}
    
	
	// Condition
    
	/**
     *  
	 * Read a condition from the repository.
	 * @param id_condition The condition id
	 * @throws KettleException if something goes wrong.
	 */
	public Condition loadCondition(long id_condition) throws KettleException {
		return conditionDelegate.loadCondition(id_condition);
	}
	
	public long saveCondition(Condition condition) throws KettleException
	{
		return saveCondition(condition, 0L);
	}
	
	public long saveCondition(Condition condition, long id_condition_parent) throws KettleException {
		return conditionDelegate.saveCondition(condition, id_condition_parent);
	}
	
	// DatabaseMeta

	/**
	 * Load the Database connection Metadata from the repository
	 * @param id_database the id of the database connection to load
	 * @throws KettleException in case something goes wrong with database, connection, etc.
	 */
	public DatabaseMeta loadDatabaseMeta(long id_database) throws KettleException {
		return databaseDelegate.loadDatabaseMeta(id_database);
	}
	
	/**
	 * Remove a database connection from the repository
	 * @param databaseName The name of the connection to remove
	 * @throws KettleException In case something went wrong: database error, insufficient permissions, depending objects, etc.
	 */
	public void deleteDatabaseMeta(String databaseName) throws KettleException {
		databaseDelegate.deleteDatabaseMeta(databaseName);
	}

	// ClusterSchema
	
    public ClusterSchema loadClusterSchema(long id_cluster_schema, List<SlaveServer> slaveServers) throws KettleException {
    	return clusterSchemaDelegate.loadClusterSchema(id_cluster_schema, slaveServers);
    }

    public void saveClusterSchema(ClusterSchema clusterSchema, long id_transformation, boolean isUsedByTransformation) throws KettleException {
    	clusterSchemaDelegate.saveClusterSchema(clusterSchema, id_transformation, isUsedByTransformation);
    }

	
    // SlaveServer
    
    public void saveSlaveServer(SlaveServer slaveServer) throws KettleException {
        slaveServerDelegate.saveSlaveServer(slaveServer);
    }
    
    public void saveSlaveServer(SlaveServer slaveServer, long parent_id, boolean isUsedByTransformation) throws KettleException {
        slaveServerDelegate.saveSlaveServer(slaveServer, parent_id, isUsedByTransformation);
    }
    
    public SlaveServer loadSlaveServer(long id_slave_server) throws KettleException {
    	return slaveServerDelegate.loadSlaveServer(id_slave_server);
    }
    
    // PartitionSchema
    
	public void savePartitionSchema(PartitionSchema partitionSchema, long id_transformation, boolean isUsedByTransformation) throws KettleException {
		partitionSchemaDelegate.savePartitionSchema(partitionSchema, id_transformation, isUsedByTransformation);
	}

	public void savePartitionSchema(PartitionSchema partitionSchema) throws KettleException {
		partitionSchemaDelegate.savePartitionSchema(partitionSchema);
	}

	public PartitionSchema loadPartitionSchema(long id_partition_schema) throws KettleException {
		return partitionSchemaDelegate.loadPartitionSchema(id_partition_schema);
	}

    

	// ValueMetaAndData
    
	public ValueMetaAndData loadValueMetaAndData(long id_value) throws KettleException {
		return valueDelegate.loadValueMetaAndData(id_value);
	}
	 
	
	// NotePadMeta
	
	public NotePadMeta loadNotePadMeta(long id_note) throws KettleException {
		return notePadDelegate.loadNotePadMeta(id_note);
	}

	public void saveNotePadMeta(NotePadMeta note, long id_transformation) throws KettleException {
		notePadDelegate.saveNotePadMeta(note, id_transformation);
	}
	
	
	// Directory stuff
	
	public RepositoryDirectory loadRepositoryDirectoryTree() throws KettleException {
		return directoryDelegate.loadRepositoryDirectoryTree(new RepositoryDirectory());
	}
	
	public RepositoryDirectory loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException {
		return directoryDelegate.loadRepositoryDirectoryTree(root);
	}

    public synchronized RepositoryDirectory refreshRepositoryDirectoryTree() throws KettleException {
    	return directoryDelegate.refreshRepositoryDirectoryTree();
    }

	public void saveRepositoryDirectory(RepositoryDirectory dir) throws KettleException {
		directoryDelegate.saveRepositoryDirectory(dir);
	}

	public void delRepositoryDirectory(RepositoryDirectory dir) throws KettleException {
		directoryDelegate.delRepositoryDirectory(dir);
	}

	public void renameRepositoryDirectory(RepositoryDirectory dir) throws KettleException {
		directoryDelegate.renameRepositoryDirectory(dir);
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
		return directoryDelegate.createRepositoryDirectory(parentDirectory, directoryPath);
	}

	
	
	
    

	/////////////////////////////////////////////////////////////////////////////////////
	// LOOKUP ID          TODO: get rid of these as well!  Move to a delegate
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized long getRootDirectoryID() throws KettleException
	{
		RowMetaAndData result = connectionDelegate.getOneRow("SELECT "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = 0");
		if (result != null && result.isNumeric(0))
			return result.getInteger(0, -1);
		return -1;
	}

	public synchronized int getNrSubDirectories(long id_directory) throws KettleException {
		return directoryDelegate.getNrSubDirectories(id_directory);
	}

	public synchronized long[] getSubDirectoryIDs(long id_directory) throws KettleException {
		return directoryDelegate.getSubDirectoryIDs(id_directory);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// INSERT VALUES
	/////////////////////////////////////////////////////////////////////////////////////

    public synchronized long insertLogEntry(String description) throws KettleException
    {
        long id = connectionDelegate.getNextLogID();

        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_REP_VERSION, ValueMetaInterface.TYPE_STRING), getVersion());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_DATE, ValueMetaInterface.TYPE_DATE), new Date());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_USER, ValueMetaInterface.TYPE_STRING), userinfo!=null?userinfo.getLogin():"admin");
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_REPOSITORY_LOG_OPERATION_DESC, ValueMetaInterface.TYPE_STRING), description);

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_REPOSITORY_LOG, table);

        return id;
    }

	public synchronized void insertTransNote(long id_transformation, long id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), new Long(id_note));

		connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_NOTE, table);
	}

	public synchronized void insertJobNote(long id_job, long id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), new Long(id_note));

		connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_JOB_NOTE, table);
	}

	public synchronized void insertStepDatabase(long id_transformation, long id_step, long id_database)
			throws KettleException
	{
		// First check if the relationship is already there.
		// There is no need to store it twice!
		RowMetaAndData check = getStepDatabase(id_step);
		if (check.getInteger(0) == null)
		{
			RowMetaAndData table = new RowMetaAndData();

			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

			connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_STEP_DATABASE, table);
		}
	}
	public synchronized void insertJobEntryDatabase(long id_job, long id_jobentry, long id_database)
	throws KettleException
	{
		// First check if the relationship is already there.
		// There is no need to store it twice!
		RowMetaAndData check = getJobEntryDatabase(id_jobentry);
		
		if (check.getInteger(0) == null)
		{
			RowMetaAndData table = new RowMetaAndData();

			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
			table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

			connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE, table);
		}
	}


    public synchronized long insertTransformationPartitionSchema(long id_transformation, long id_partition_schema) throws KettleException
    {
        long id = connectionDelegate.getNextTransformationPartitionSchemaID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id_partition_schema));

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA, table);

        return id;
    }
    
 
    public synchronized long insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException
    {
        long id = connectionDelegate.getNextClusterSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), new Long(clusterSchema.getID()));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(slaveServer.getID()));

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE, table);

        return id;
    }

    public synchronized long insertTransformationCluster(long id_transformation, long id_cluster) throws KettleException
    {
        long id = connectionDelegate.getNextTransformationClusterID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), new Long(id_cluster));

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER, table);

        return id;
    }

    public synchronized long insertTransformationSlave(long id_transformation, long id_slave) throws KettleException
    {
        long id = connectionDelegate.getNextTransformationSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), new Long(id_slave));

        connectionDelegate.insertTableRow(KettleDatabaseRepository.TABLE_R_TRANS_SLAVE, table);

        return id;
    }
    
    

	public synchronized void insertTransStepCondition(long id_transformation, long id_step, long id_condition)
			throws KettleException
	{
		String tablename = KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER), new Long(id_condition));

		connectionDelegate.insertTableRow(tablename, table);
	}




	//////////////////////////////////////////////////////////////////////////////////////////
	// READ DATA FROM REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public synchronized String[] getTransformationNames(long id_directory) throws KettleException
	{
		return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANSFORMATION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_NAME));
	}
    
    public List<RepositoryObject> getJobObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects(quoteTable(KettleDatabaseRepository.TABLE_R_JOB), RepositoryObject.STRING_OBJECT_TYPE_JOB, id_directory);
    }

    public List<RepositoryObject> getTransformationObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects(quoteTable(KettleDatabaseRepository.TABLE_R_TRANSFORMATION), RepositoryObject.STRING_OBJECT_TYPE_TRANSFORMATION, id_directory);
    }

    /**
     * @param id_directory
     * @return A list of RepositoryObjects
     * 
     * @throws KettleException
     */
    private synchronized List<RepositoryObject> getRepositoryObjects(String tableName, String objectType, long id_directory) throws KettleException
    {
    	return connectionDelegate.getRepositoryObjects(tableName, objectType, id_directory);
    }
    

	public synchronized String[] getJobNames(long id_directory) throws KettleException
	{
        return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY)+" = " + id_directory + " ORDER BY "+quote(KettleDatabaseRepository.FIELD_JOB_NAME));
	}

	public synchronized String[] getDirectoryNames(long id_directory) throws KettleException
	{
        return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory + " ORDER BY "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME));
	}

	public synchronized String[] getJobNames() throws KettleException
	{
        return connectionDelegate.getStrings("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_NAME)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_JOB_NAME));
	}

	public long[] getSubConditionIDs(long id_condition) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION_PARENT)+" = " + id_condition);
	}

	public long[] getTransNoteIDs(long id_transformation) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_NOTE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getConditionIDs(long id_transformation) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_CONDITION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getDatabaseIDs(long id_transformation) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public long[] getJobNoteIDs(long id_job) throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_NOTE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB)+" = " + id_job);
	}

	public long[] getDatabaseIDs() throws KettleException
	{
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE));
	}
    
    public long[] getDatabaseAttributeIDs(long id_database) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = "+id_database);
    }
    
    public long[] getPartitionSchemaIDs() throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_NAME));
    }
    
    public long[] getPartitionIDs(long id_partition_schema) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }

    public long[] getTransformationPartitionSchemaIDs(long id_transformation) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = "+id_transformation);
    }
    
    public long[] getTransformationClusterSchemaIDs(long id_transformation) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT ID_TRANS_CLUSTER FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER)+" WHERE ID_TRANSFORMATION = " + id_transformation);
    }
    
    public long[] getClusterIDs() throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER)+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_CLUSTER_NAME)); 
    }

    public long[] getSlaveIDs() throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_SLAVE));
    }

    public long[] getSlaveIDs(long id_cluster_schema) throws KettleException
    {
        return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_SLAVE)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster_schema);
    }
    
	public synchronized String[] getDatabaseNames() throws KettleException
	{
		String nameField = quote(KettleDatabaseRepository.FIELD_DATABASE_NAME);
		return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE)+" ORDER BY "+nameField);
	}
    
    public synchronized String[] getPartitionSchemaNames() throws KettleException
    {
        String nameField = quote(KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_NAME);
        return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION_SCHEMA)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getSlaveNames() throws KettleException
    {
        String nameField = quote(KettleDatabaseRepository.FIELD_SLAVE_NAME);
        return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_SLAVE)+" ORDER BY "+nameField);
    }
    
    public synchronized String[] getClusterNames() throws KettleException
    {
        String nameField = quote(KettleDatabaseRepository.FIELD_CLUSTER_NAME);
        return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER)+" ORDER BY "+nameField);
    }

	public long[] getStepIDs(long id_transformation) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_STEP_ID_STEP)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public synchronized String[] getTransformationsUsingDatabase(long id_database) throws KettleException
	{
		String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE)+" = " + id_database;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
	}
	
	public synchronized String[] getJobsUsingDatabase(long id_database) throws KettleException
	{
		String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_DATABASE)+" = " + id_database;
        return jobDelegate.getJobsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
	}
	
    public synchronized String[] getClustersUsingSlave(long id_slave) throws KettleException
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
                RowMetaAndData transRow =  clusterSchemaDelegate.getClusterSchema(id_cluster_schema);
                if (transRow!=null)
                {
                    String clusterName = transRow.getString(quote(KettleDatabaseRepository.FIELD_CLUSTER_NAME), "<name not found>");
                    if (clusterName!=null) clusterList.add(clusterName);
                }
            }
        }

        return (String[]) clusterList.toArray(new String[clusterList.size()]);
    }

	public synchronized String[] getTransformationsUsingSlave(long id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_SLAVE)+" = " + id_slave;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingPartitionSchema(long id_partition_schema) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+
                     " FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" = " + id_partition_schema;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingCluster(long id_cluster) throws KettleException
    {
        String sql = "SELECT DISTINCT "+quote(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_CLUSTER)+" = " + id_cluster;
        return transDelegate.getTransformationsWithIDList( connectionDelegate.getRows(sql, 100), connectionDelegate.getReturnRowMeta() );
    }


	public long[] getJobHopIDs(long id_job) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_HOP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB)+" = " + id_job);
	}

	public long[] getTransDependencyIDs(long id_transformation) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DEPENDENCY_ID_DEPENDENCY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DEPENDENCY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation);
	}

	public synchronized long getUserID(String login) throws KettleException {
		return userDelegate.getUserID(login);
	}

	public long[] getUserIDs() throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_USER_ID_USER)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_USER));
	}

	public synchronized String[] getUserLogins() throws KettleException
	{
		String loginField = quote(KettleDatabaseRepository.FIELD_USER_LOGIN);
		return connectionDelegate.getStrings("SELECT "+loginField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_USER)+" ORDER BY "+loginField);
	}

	public long[] getPermissionIDs(long id_profile) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_PROFILE_PERMISSION_ID_PERMISSION)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile);
	}

	public long[] getJobEntryIDs(long id_job) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOBENTRY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOB)+" = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job, long id_jobentry) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY)+
				" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job + " AND "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY)+" = " + id_jobentry);
	}

	public synchronized String[] getProfiles() throws KettleException
	{
		String nameField = quote(KettleDatabaseRepository.FIELD_PROFILE_NAME);
		return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE)+" ORDER BY "+nameField);
	}

	private RowMetaAndData getStepDatabase(long id_step) throws KettleException
	{
		return connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE), quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_STEP), id_step);
	}
	
	private RowMetaAndData getJobEntryDatabase(long id_jobentry) throws KettleException
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

	public synchronized void delSteps(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delCondition(long id_condition) throws KettleException
	{
		boolean ok = true;
		long ids[] = getSubConditionIDs(id_condition);
		if (ids.length > 0)
		{
			// Delete the sub-conditions...
			for (int i = 0; i < ids.length && ok; i++)
			{
				delCondition(ids[i]);
			}

			// Then delete the main condition
			delCondition(id_condition);
		}
		else
		{
			String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION)+" = " + id_condition;
			execStatement(sql);
		}
	}

	public synchronized void delStepConditions(long id_transformation) throws KettleException
	{
		long ids[] = getConditionIDs(id_transformation);
		for (int i = 0; i < ids.length; i++)
		{
			delCondition(ids[i]);
		}
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	/**
	 * Delete the relationships between the transformation/steps and the databases.
	 * @param id_transformation the transformation for which we want to delete the databases.
	 * @throws KettleException in case something unexpected happens.
	 */
	public synchronized void delStepDatabases(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}
	/**
	 * Delete the relationships between the job/job entries and the databases.
	 * @param id_job the job for which we want to delete the databases.
	 * @throws KettleDatabaseException in case something unexpected happens.
	 */
	public synchronized void delJobEntryDatabases(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}
	public synchronized void delJobEntries(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delJobEntryCopies(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_COPY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delDependencies(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DEPENDENCY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DEPENDENCY_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delStepAttributes(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

    public synchronized void delTransAttributes(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }

    public synchronized void delJobAttributes(long id_job) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_ID_JOB)+" = " + id_job;
        execStatement(sql);
    }   
    
    public synchronized void delPartitionSchemas(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }

    public synchronized void delPartitions(long id_partition_schema) throws KettleException
    {
        // First see if the partition is used by a step, transformation etc.
        // 
        execStatement("DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PARTITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
    }
    
    public synchronized void delClusterSlaves(long id_cluster) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER)+" = " + id_cluster;
        execStatement(sql);
    }
    
    public synchronized void delTransformationClusters(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }

    public synchronized void delTransformationSlaves(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_SLAVE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION)+" = " + id_transformation;
        execStatement(sql);
    }


	public synchronized void delJobEntryAttributes(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOBENTRY_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTransHops(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_HOP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_HOP_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delJobHops(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_HOP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTransNotes(long id_transformation) throws KettleException
	{
		long ids[] = getTransNoteIDs(id_transformation);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			execStatement(sql);
		}

		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delJobNotes(long id_job) throws KettleException
	{
		long ids[] = getJobNoteIDs(id_job);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_NOTE_ID_NOTE)+" = " + ids[i];
			execStatement(sql);
		}

		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_NOTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTrans(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANSFORMATION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delJob(long id_job) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ID_JOB)+" = " + id_job;
		execStatement(sql);
	}

	public synchronized void delTransStepCondition(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION)+" = " + id_transformation;
		execStatement(sql);
	}

	public synchronized void delValue(long id_value) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_VALUE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_VALUE_ID_VALUE)+" = " + id_value;
		execStatement(sql);
	}

	public synchronized void delUser(long id_user) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_USER)+" WHERE "+quote(KettleDatabaseRepository.FIELD_USER_ID_USER)+" = " + id_user;
		execStatement(sql);
	}

	public synchronized void delProfile(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE)+" = " + id_profile;
		execStatement(sql);
	}

	public synchronized void delProfilePermissions(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(KettleDatabaseRepository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
		execStatement(sql);
	}
    
    public synchronized void delSlave(long id_slave) throws KettleException
    {
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
   
    public synchronized void delPartitionSchema(long id_partition_schema) throws KettleException {
    	partitionSchemaDelegate.delPartitionSchema(id_partition_schema);
    }
    
    public synchronized void delClusterSchema(long id_cluster) throws KettleException {
       clusterSchemaDelegate.delClusterSchema(id_cluster);
    }

	public synchronized void delAllFromTrans(long id_transformation) throws KettleException
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
	
	public synchronized void renameUser(long id_user, String newname) throws KettleException {
		userDelegate.renameUser(id_user, newname);
	}

	public synchronized void renameProfile(long id_profile, String newname) throws KettleException {
		profileDelegate.renameProfile(id_profile, newname);
	}


	public synchronized void delAllFromJob(long id_job) throws KettleException
	{
		// log.logBasic(toString(), "Deleting info in repository on ID_JOB: "+id_job);

		delJobNotes(id_job);
		delJobAttributes(id_job);
		delJobEntryAttributes(id_job);
		delJobEntryDatabases(id_job);
		delJobEntries(id_job);
		delJobEntryCopies(id_job);
		delJobHops(id_job);
		delJob(id_job);

		// log.logBasic(toString(), "All deleted on job with ID_JOB: "+id_job);
	}

	public synchronized void renameDatabase(long id_database, String newname) throws KettleException {
		databaseDelegate.renameDatabase(id_database, newname);
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
                if (log.isDetailed()) log.logDetailed(toString(), "dropped table "+repositoryTableNames[i]);
			}
			catch (KettleException dbe)
			{
                if (log.isDetailed()) log.logDetailed(toString(), "Unable to drop table: " + repositoryTableNames[i]);
			}
		}
        log.logBasic(toString(), "Dropped all "+repositoryTableNames.length+" repository tables.");
        
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

    /**
     * @return Returns the directoryTree.
     */
    public RepositoryDirectory getDirectoryTree()
    {
        return directoryTree;
    }

    /**
     * @param directoryTree The directoryTree to set.
     */
    public synchronized void setDirectoryTree(RepositoryDirectory directoryTree)
    {
        this.directoryTree = directoryTree;
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
        long[] databaseIDs = getDatabaseIDs();
        for (int i=0;i<databaseIDs.length;i++)
        {
            DatabaseMeta databaseMeta = loadDatabaseMeta(databaseIDs[i]);
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
        long[] slaveIDs = getSlaveIDs();
        for (int i=0;i<slaveIDs.length;i++)
        {
            SlaveServer slaveServer = loadSlaveServer(slaveIDs[i]);
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
		long[] ids = getDatabaseIDs();
		for (int i=0;i<ids.length;i++) 
		{
			DatabaseMeta databaseMeta = loadDatabaseMeta(ids[i]);
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
	 * @param userinfo the UserInfo object to set
	 */
	public void setUserInfo(UserInfo userinfo) {
		this.userinfo = userinfo;
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
	public synchronized int countNrStepAttributes(long id_step, String code) throws KettleException {
		return connectionDelegate.countNrStepAttributes(id_step, code);
	}

	public synchronized int countNrJobEntryAttributes(long id_jobentry, String code) throws KettleException {
		return connectionDelegate.countNrJobEntryAttributes(id_jobentry, code);
	}

	public synchronized void disconnect() {
		connectionDelegate.disconnect();
	}

	// Job Entry attributes...
	
	// get
	
	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeBoolean(id_jobentry, code);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeBoolean(id_jobentry, nr, code);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code, boolean def) throws KettleException {
		return connectionDelegate.getJobEntryAttributeBoolean(id_jobentry, code, def);
	}

	public long getJobEntryAttributeInteger(long id_jobentry, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeInteger(id_jobentry, code);
	}

	public long getJobEntryAttributeInteger(long id_jobentry, int nr, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeInteger(id_jobentry, nr, code);
	}

	public String getJobEntryAttributeString(long id_jobentry, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeString(id_jobentry, code);
	}

	public String getJobEntryAttributeString(long id_jobentry, int nr, String code) throws KettleException {
		return connectionDelegate.getJobEntryAttributeString(id_jobentry, nr, code);
	}
	
	// put
	
	public void saveJobEntryAttribute(long id_job, long id_jobentry, int nr, String code, String value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveJobEntryAttribute(long id_job, long id_jobentry, String code, String value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, code, value);
	}
	public void saveJobEntryAttribute(long id_job, long id_jobentry, int nr, String code, boolean value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveJobEntryAttribute(long id_job, long id_jobentry, String code, boolean value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, code, value);
	}
	public void saveJobEntryAttribute(long id_job, long id_jobentry, int nr, String code, long value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveJobEntryAttribute(long id_job, long id_jobentry, String code, long value) throws KettleException {
		connectionDelegate.saveJobEntryAttribute(id_job, id_jobentry, code, value);
	}


	// Step attributes 

	// get
	
	public boolean getStepAttributeBoolean(long id_step, int nr, String code, boolean def) throws KettleException {
		return connectionDelegate.getStepAttributeBoolean(id_step, nr, code, def);
	}
	public boolean getStepAttributeBoolean(long id_step, int nr, String code) throws KettleException {
		return connectionDelegate.getStepAttributeBoolean(id_step, nr, code);
	}
	public boolean getStepAttributeBoolean(long id_step, String code) throws KettleException {
		return connectionDelegate.getStepAttributeBoolean(id_step, code);
	}
	public long getStepAttributeInteger(long id_step, int nr, String code) throws KettleException {
		return connectionDelegate.getStepAttributeInteger(id_step, nr, code);
	}
	public long getStepAttributeInteger(long id_step, String code) throws KettleException {
		return connectionDelegate.getStepAttributeInteger(id_step, code);
	}
	public String getStepAttributeString(long id_step, int nr, String code) throws KettleException {
		return connectionDelegate.getStepAttributeString(id_step, nr, code);
	}
	public String getStepAttributeString(long id_step, String code) throws KettleException {
		return connectionDelegate.getStepAttributeString(id_step, code);
	}

	// put
	
	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, String value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveStepAttribute(long id_job, long id_jobentry, String code, String value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, code, value);
	}
	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, boolean value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveStepAttribute(long id_job, long id_jobentry, String code, boolean value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, code, value);
	}
	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, long value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveStepAttribute(long id_job, long id_jobentry, String code, long value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, code, value);
	}
	public void saveStepAttribute(long id_job, long id_jobentry, int nr, String code, double value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, nr, code, value);
	}
	public void saveStepAttribute(long id_job, long id_jobentry, String code, double value) throws KettleException {
		connectionDelegate.saveStepAttribute(id_job, id_jobentry, code, value);
	}

	

	/**
	 * This method is only used to check backward compatibility with the 2.x series.
	 * TODO : See what we can do to remove this from Repository
	 * 
	 * @param id_step
	 * @param nr
	 * @param code
	 * @return
	 * @throws KettleException
	 * @deprecated
	 */
	public long findStepAttributeID(long id_step, int nr, String code) throws KettleException {
		return connectionDelegate.findStepAttributeID(id_step, nr, code);
	}


	private void execStatement(String sql) throws KettleException {
		connectionDelegate.getDatabase().execStatement(sql);
	}
	
	public void loadJobEntry(JobEntryBase jobEntryBase, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
		jobEntryDelegate.loadJobEntryBase(jobEntryBase, id_jobentry, databases, slaveServers);
	}

	public long getClusterID(String name) throws KettleException {
		return clusterSchemaDelegate.getClusterID(name);
	}

	public long getDatabaseID(String name) throws KettleException {
		return databaseDelegate.getDatabaseID(name);
	}

	public long getJobID(String name, RepositoryDirectory repositoryDirectory) throws KettleException {
		return jobDelegate.getJobID(name, repositoryDirectory.getID());
	}

	public long getPartitionSchemaID(String name) throws KettleException {
		return partitionSchemaDelegate.getPartitionSchemaID(name);
	}

	public long getProfileID(String profilename) throws KettleException {
		return profileDelegate.getProfileID(profilename);
	}

	public long getSlaveID(String name) throws KettleException {
		return slaveServerDelegate.getSlaveID(name);
	}

	public long getTransformationID(String name, RepositoryDirectory repositoryDirectory) throws KettleException {
		return transDelegate.getTransformationID(name, repositoryDirectory.getID());
	}

	public long insertJobEntry(long id_job, JobEntryBase jobEntryBase) throws KettleException {
		return jobEntryDelegate.insertJobEntry(id_job, jobEntryBase);
	}

	public boolean isConnected() {
		return !Const.isEmpty(locksource);
	}
}
