package org.pentaho.di.repository.filerep;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class KettleFileRepository implements Repository {

	private static final String	EXT_TRANSFORMATION		= ".ktr";
	private static final String EXT_JOB					= ".kjb";
	private static final String	EXT_DATABASE			= ".kdb";
	private static final String	EXT_SLAVE_SERVER 		= ".ksl";
	private static final String	EXT_CLUSTER_SCHEMA 		= ".kcs";
	private static final String	EXT_PARTITION_SCHEMA	= ".kps";
	
	private static final String LOG_FILE = "repository.log";
	
	public static final String	FILE_REPOSITORY_VERSION	= "0.1";
	
	private KettleFileRepositoryMeta repositoryMeta;
	private KettleFileRepositorySecurityProvider	securityProvider;
	
	private LogChannelInterface log;

	private Map<Class<? extends IRepositoryService>, IRepositoryService> serviceMap;
	private List<Class<? extends IRepositoryService>> serviceList;
	public void connect(String username, String password) throws KettleException {}

	public void disconnect() {}

	public void init(RepositoryMeta repositoryMeta) {
	  this.serviceMap = new HashMap<Class<? extends IRepositoryService>, IRepositoryService>(); 
	  this.serviceList = new ArrayList<Class<? extends IRepositoryService>>();
		this.repositoryMeta = (KettleFileRepositoryMeta) repositoryMeta;
		this.securityProvider = new KettleFileRepositorySecurityProvider(repositoryMeta);
    this.serviceMap.put(RepositorySecurityProvider.class, securityProvider);
    this.serviceList.add(RepositorySecurityProvider.class);
		this.log = new LogChannel(this);
	}

	public LogChannelInterface getLog() {
		return log;
	}
	
	public boolean isConnected() {
		return true;
	}
	
	public RepositorySecurityProvider getSecurityProvider() {
		return securityProvider;
	}

  public RepositorySecurityManager getSecurityManager() {
    return null;
  }

	
	private String calcDirectoryName(RepositoryDirectoryInterface dir) {
		StringBuilder directory = new StringBuilder();
		String baseDir = repositoryMeta.getBaseDirectory();
		baseDir = Const.replace(baseDir, "\\", "/");
		directory.append(baseDir);
		if (!baseDir.endsWith("/")) {
			directory.append("/");
		}

		if (dir!=null) {
			String path = calcRelativeElementDirectory(dir);
			if (path.startsWith("/")) {
				directory.append(path.substring(1));
			} else {
				directory.append(path);
			}
			if (!path.endsWith("/")) {
				directory.append("/");
			}
		}
		return directory.toString();
	}
	
	public String calcRelativeElementDirectory(RepositoryDirectoryInterface dir) {
		if (dir!=null) {
			return dir.getPath();
		} else {
			return "/";
		}
	}
	
	public String calcObjectId(RepositoryDirectoryInterface dir) {
		StringBuilder id = new StringBuilder();
		
		String path = calcRelativeElementDirectory(dir);
		id.append(path);
		if (!path.endsWith("/")) {
			id.append("/");
		}
		
		return id.toString();		
	}
		
	public String calcObjectId(RepositoryDirectoryInterface directory, String name, String extension) {
		StringBuilder id = new StringBuilder();
		
		String path = calcRelativeElementDirectory(directory);
		id.append(path);
		if (!path.endsWith("/")) {
			id.append("/");
		}
		
		if (name.startsWith("/")) {
		  id.append(name.substring(1)).append(extension);
		} else {
		  id.append(name).append(extension);
		}
		
		return id.toString();
	}
	

	public String calcObjectId(RepositoryElementInterface element) {
		RepositoryDirectoryInterface directory = element.getRepositoryDirectory();
		String name = element.getName();
		String extension = element.getRepositoryElementType().getExtension();
		
		return calcObjectId(directory, name, extension);
	}
	
	private String calcFilename(RepositoryElementInterface element) {
		return calcFilename(element.getRepositoryDirectory(), element.getName(), element.getRepositoryElementType().getExtension());
	}
	
	private String calcFilename(RepositoryDirectoryInterface dir, String name, String extension) {
		StringBuilder filename = new StringBuilder();
		filename.append(calcDirectoryName(dir));
		
		String objectName = name+extension;
		filename.append(objectName);
		
		return filename.toString();
	}
	
	// The filename of the object is the object id with the base folder before it...
	//
	public String calcFilename(ObjectId id) {
		return calcDirectoryName(null)+id.toString();
	}
	
	private FileObject getFileObject(RepositoryElementInterface element) throws KettleFileException {
		return KettleVFS.getFileObject(calcFilename(element.getRepositoryDirectory(), element.getName(), element.getRepositoryElementType().getExtension()));
	}

	public boolean exists(final String name, final RepositoryDirectoryInterface repositoryDirectory, final RepositoryObjectType objectType) throws KettleException {
		try {
			FileObject fileObject = KettleVFS.getFileObject(calcFilename(repositoryDirectory, name, objectType.getExtension())); 
			return fileObject.exists();
		} catch(Exception e) {
			throw new KettleException(e);
		}
	}

	// Common objects 
	
	public void save(RepositoryElementInterface repositoryElement, String versionComment) throws KettleException {
		save(repositoryElement, versionComment, null);
	}

	public void save(RepositoryElementInterface repositoryElement, String versionComment, ProgressMonitorListener monitor) throws KettleException {
		save(repositoryElement, versionComment, monitor, null, false);
	}

	public void save(RepositoryElementInterface repositoryElement, String versionComment, ProgressMonitorListener monitor, ObjectId parentId, boolean used) throws KettleException {
		try {
			if (!(repositoryElement instanceof XMLInterface) && !(repositoryElement instanceof SharedObjectInterface)) {
				throw new KettleException("Class ["+repositoryElement.getClass().getName()+"] needs to implement the XML Interface in order to save it to disk");
			}
			
			if (!Const.isEmpty(versionComment)) {
				insertLogEntry(versionComment);
			}
			
			ObjectId objectId = new StringObjectId(calcObjectId(repositoryElement));
			
			FileObject fileObject = getFileObject(repositoryElement);
			
			String xml = ((XMLInterface)repositoryElement).getXML();
			
			OutputStream os = KettleVFS.getOutputStream(fileObject, false);
			os.write(xml.getBytes(Const.XML_ENCODING));
			os.close();
			
			if (repositoryElement instanceof ChangedFlagInterface) {
			  ((ChangedFlagInterface)repositoryElement).clearChanged();
			}
			
			// See if the element was already saved in the repository.
			// If the object ID is different, then we created an extra copy.
			// If so, we need to now remove the old file to prevent us from having multiple copies.
			//
			if (repositoryElement.getObjectId()!=null && !repositoryElement.getObjectId().equals(objectId)) {
				delObject(repositoryElement.getObjectId());
			}

			repositoryElement.setObjectId(objectId);
		} catch(Exception e) {
			throw new KettleException("Unable to save repository element ["+repositoryElement+"] to XML file : "+calcFilename(repositoryElement), e);
		}
	}
	
	
	public RepositoryDirectoryInterface createRepositoryDirectory(RepositoryDirectoryInterface parentDirectory, String directoryPath) throws KettleException {
		String folder = calcDirectoryName(parentDirectory);
		String newFolder = folder;
		if (folder.endsWith("/")) newFolder+=directoryPath; else newFolder+="/"+directoryPath;
		
		FileObject parent = KettleVFS.getFileObject(newFolder);
		try {
			parent.createFolder();
		} catch (FileSystemException e) {
			throw new KettleException("Unable to create folder "+newFolder, e);
		}
		
		// Incremental change of the directory structure...
		//
		RepositoryDirectory newDir = new RepositoryDirectory(parentDirectory, directoryPath);
		parentDirectory.addSubdirectory(newDir);
		newDir.setObjectId(new StringObjectId(newDir.toString()));
		
		return newDir;
	}

	public void saveRepositoryDirectory(RepositoryDirectoryInterface dir) throws KettleException {
		try
		{
			String filename = calcDirectoryName(dir);
			ObjectId objectId = new StringObjectId(calcRelativeElementDirectory(dir));
			
			FileObject fileObject = KettleVFS.getFileObject(filename);
			fileObject.createFolder(); // also create parents
			
			dir.setObjectId(objectId);
			
            log.logDetailed("New id of directory = "+dir.getObjectId());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save directory ["+dir+"] in the repository", e);
		}
	}

	private void delObject(ObjectId id) throws KettleException {
		String filename = calcFilename(id);
		deleteFile(filename);
	}
	
	public void deleteJob(ObjectId id_job) throws KettleException {
		delObject(id_job);
	}

	public void deleteTransformation(ObjectId id_transformation) throws KettleException {
		delObject(id_transformation);
	}

	public void deleteClusterSchema(ObjectId id_cluster) throws KettleException {
		// ID and filename are the same
		deleteFile(id_cluster.getId());
	}

	public void deleteCondition(ObjectId id_condition) throws KettleException {
		

	}

	public void deletePartitionSchema(ObjectId id_partition_schema) throws KettleException {
		// ID and filename are the same
		deleteFile(id_partition_schema.getId());
	}

	public void deleteRepositoryDirectory(RepositoryDirectoryInterface dir) throws KettleException {
		

	}

	public void deleteSlave(ObjectId id_slave) throws KettleException {
		// ID and filename are the same
		deleteFile(id_slave.getId());
	}
	
	public void deleteDatabaseMeta(String databaseName) throws KettleException {
		deleteRootObject(databaseName, EXT_DATABASE);
	}

	public void deleteRootObject(String name, String extension) throws KettleException {
		try {
			String filename = calcDirectoryName(null)+name+extension;
			FileObject fileObject = KettleVFS.getFileObject(filename);
			fileObject.delete();
		}
		catch(Exception e) {
			throw new KettleException("Unable to delete database with name ["+name+"] and extension ["+extension+"]", e);
		}
	}
	
	public void deleteFile(String filename) throws KettleException {
		try {
			FileObject fileObject = KettleVFS.getFileObject(filename);
			fileObject.delete();
		}
		catch(Exception e) {
			throw new KettleException("Unable to delete file with name ["+filename+"]", e);
		}
	}

	public ObjectId getClusterID(String name) throws KettleException {
		// The ID is the filename relative to the base directory, including the file extension
		//
		return new StringObjectId( calcObjectId((RepositoryDirectory)null)+name+EXT_SLAVE_SERVER);
	}

	public ObjectId[] getClusterIDs(boolean includeDeleted) throws KettleException {
		return getRootObjectIDs(EXT_CLUSTER_SCHEMA);
	}

	public String[] getClusterNames(boolean includeDeleted) throws KettleException {
		return convertRootIDsToNames(getClusterIDs(false));
	}

	private String[] convertRootIDsToNames(ObjectId[] ids) {
		String[] names = new String[ids.length];
		for (int i=0;i<names.length;i++) {
			String id = ids[i].getId(); 
			names[i] = id.substring(0, id.length()-4); // get rid of the extension
		}
		return names;
	}

	public String[] getClustersUsingSlave(ObjectId id_slave) throws KettleException { return new String[] {}; }

	public ObjectId[] getTransformationConditionIDs(ObjectId id_transformation) throws KettleException { return new ObjectId[] {}; }

	public ObjectId[] getDatabaseAttributeIDs(ObjectId id_database) throws KettleException { return new ObjectId[] {}; }

	private ObjectId getObjectId(RepositoryDirectoryInterface repositoryDirectory, String name, String extension) throws KettleException {
		try {
			String filename = calcFilename(repositoryDirectory, name, extension);
			if (!KettleVFS.getFileObject(filename).exists()) {
				return null;
			}
			
			// The ID is the filename relative to the base directory, including the file extension
			//
			return new StringObjectId( calcObjectId(repositoryDirectory, name, extension) );
		} catch (Exception e) {
			throw new KettleException("Error finding ID for directory ["+repositoryDirectory+"] and name ["+name+"]", e);
		}
	}
	
	public ObjectId getDatabaseID(String name) throws KettleException {
		return getObjectId(null, name, EXT_DATABASE);
	}

	public ObjectId[] getTransformationDatabaseIDs(ObjectId id_transformation) throws KettleException { return new ObjectId[] {}; }

	public ObjectId[] getDatabaseIDs(boolean includeDeleted) throws KettleException {
		return getRootObjectIDs(EXT_DATABASE);
	}

	public String[] getDatabaseNames(boolean includeDeleted) throws KettleException {
		return convertRootIDsToNames(getDatabaseIDs(false));
	}

	public String[] getDirectoryNames(ObjectId id_directory) throws KettleException {
	  RepositoryDirectoryInterface tree = loadRepositoryDirectoryTree();
	  RepositoryDirectoryInterface directory = tree.findDirectory(id_directory);
		String[] names = new String[directory.getNrSubdirectories()];
		for (int i=0;i<names.length;i++) {
			names[i] = directory.getSubdirectory(i).getName();
		}
		return names;
	}

	public ObjectId getJobId(String name, RepositoryDirectoryInterface repositoryDirectory) throws KettleException {
		return getObjectId(repositoryDirectory, name, EXT_JOB);
	}

	public String[] getJobNames(ObjectId id_directory, boolean includeDeleted) throws KettleException {
		try {
			List<String> list = new ArrayList<String>();
			
			RepositoryDirectoryInterface tree = loadRepositoryDirectoryTree();
			RepositoryDirectoryInterface directory = tree.findDirectory(id_directory);
			
			String folderName = calcDirectoryName(directory);
			FileObject folder = KettleVFS.getFileObject(folderName);
			
			for (FileObject child : folder.getChildren()) {
				if (child.getType().equals(FileType.FILE)) {
					String name = child.getName().getBaseName();
					
					if (name.endsWith(EXT_JOB)) {
						
						String jobName = name.substring(0, name.length()-4);
						list.add( jobName );
					}
				}
			}
			
			return list.toArray(new String[list.size()]);
		}
		catch(Exception e) {
			throw new KettleException("Unable to get list of transformations names in folder with id : "+id_directory, e);
		}
	}

	public ObjectId[] getJobNoteIDs(ObjectId id_job) throws KettleException { return new ObjectId[] {}; }
	public String[] getJobsUsingDatabase(ObjectId id_database) throws KettleException { return new String[] {}; }

	public String getName() {
		
		return repositoryMeta.getName();
	}

	public ObjectId getPartitionSchemaID(String name) throws KettleException {
		// The ID is the filename relative to the base directory, including the file extension
		//
		return new StringObjectId( calcObjectId((RepositoryDirectory)null)+name+EXT_SLAVE_SERVER);
	}

	public ObjectId[] getPartitionSchemaIDs(boolean includeDeleted) throws KettleException {
		return getRootObjectIDs(EXT_PARTITION_SCHEMA);
	}

	public String[] getPartitionSchemaNames(boolean includeDeleted) throws KettleException {
		return convertRootIDsToNames(getPartitionSchemaIDs(false));
	}

	public ObjectId getRootDirectoryID() throws KettleException {
		
		return null;
	}

	public ObjectId getSlaveID(String name) throws KettleException {
		// The ID is the filename relative to the base directory, including the file extension
		//
		return new StringObjectId( calcObjectId((RepositoryDirectory)null)+name+EXT_SLAVE_SERVER);
	}

	private ObjectId[] getRootObjectIDs(String extension) throws KettleException {
		try {
			// Get all the files in the root directory with a certain extension... 
			//
			List<ObjectId> list = new ArrayList<ObjectId>();
					
			String folderName = repositoryMeta.getBaseDirectory();
			FileObject folder = KettleVFS.getFileObject(folderName);
			
			for (FileObject child : folder.getChildren()) {
				if (child.getType().equals(FileType.FILE)) {
					String name = child.getName().getBaseName();
					
					if (name.endsWith(extension)) {
						list.add(new StringObjectId(name));
					}
				}
			}
			
			return list.toArray(new ObjectId[list.size()]);
		}
		catch(Exception e) {
			throw new KettleException("Unable to get root object ids for extension ["+extension+"]", e);
		}
	}

	public ObjectId[] getSlaveIDs(boolean includeDeleted) throws KettleException {
		return getRootObjectIDs(EXT_SLAVE_SERVER);
	}

	public ObjectId[] getClusterSlaveIDs(ObjectId id_cluster_schema) throws KettleException { return new ObjectId[] {}; }

	public String[] getSlaveNames(boolean includeDeleted) throws KettleException {
		return convertRootIDsToNames(getSlaveIDs(false));
	}

	public List<SlaveServer> getSlaveServers() throws KettleException {
		List<SlaveServer> list = new ArrayList<SlaveServer>();
		for (ObjectId id : getSlaveIDs(false)) {
			list.add(loadSlaveServer(id, null));  // Load last version
		}
		return list;
	}

	public boolean getStepAttributeBoolean(ObjectId id_step, int nr, String code, boolean def) throws KettleException { return false; }
	public boolean getStepAttributeBoolean(ObjectId id_step, int nr, String code) throws KettleException { return false; }
	public boolean getStepAttributeBoolean(ObjectId id_step, String code) throws KettleException { return false; }
	public long getStepAttributeInteger(ObjectId id_step, int nr, String code) throws KettleException { return 0; }
	public long getStepAttributeInteger(ObjectId id_step, String code) throws KettleException { return 0; }
	public String getStepAttributeString(ObjectId id_step, int nr, String code) throws KettleException { return null; }
	public String getStepAttributeString(ObjectId id_step, String code) throws KettleException { return null; }
	
	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, String code) throws KettleException { return false; }
	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, int nr, String code) throws KettleException { return false; }
	public boolean getJobEntryAttributeBoolean(ObjectId id_jobentry, String code, boolean def) throws KettleException { return false; }
	public long getJobEntryAttributeInteger(ObjectId id_jobentry, String code) throws KettleException { return 0; }
	public long getJobEntryAttributeInteger(ObjectId id_jobentry, int nr, String code) throws KettleException { return 0; }
	public String getJobEntryAttributeString(ObjectId id_jobentry, String code) throws KettleException { return null; }
	public String getJobEntryAttributeString(ObjectId id_jobentry, int nr, String code) throws KettleException { return null; }


	public ObjectId[] getSubConditionIDs(ObjectId id_condition) throws KettleException {
		
		return null;
	}

	public ObjectId[] getSubDirectoryIDs(ObjectId id_directory) throws KettleException {
	  RepositoryDirectoryInterface tree = loadRepositoryDirectoryTree();
	  RepositoryDirectoryInterface directory = tree.findDirectory(id_directory);
		ObjectId[] objectIds = new ObjectId[directory.getNrSubdirectories()];
		for (int i=0;i<objectIds.length;i++) {
			objectIds[i] = directory.getSubdirectory(i).getObjectId();
		}
		return objectIds;
	}

	public ObjectId[] getTransNoteIDs(ObjectId id_transformation) throws KettleException { return new ObjectId[] {}; }
	public ObjectId[] getTransformationClusterSchemaIDs(ObjectId id_transformation) throws KettleException { return new ObjectId[] {}; }

	public ObjectId getTransformationID(String name, RepositoryDirectoryInterface repositoryDirectory) throws KettleException {
		return getObjectId(repositoryDirectory, name, EXT_TRANSFORMATION);
	}

	public String[] getTransformationNames(ObjectId id_directory, boolean includeDeleted) throws KettleException {
		try {
			List<String> list = new ArrayList<String>();
			
			RepositoryDirectoryInterface tree = loadRepositoryDirectoryTree();
			RepositoryDirectoryInterface directory = tree.findDirectory(id_directory);
			
			String folderName = calcDirectoryName(directory);
			FileObject folder = KettleVFS.getFileObject(folderName);
			
			for (FileObject child : folder.getChildren()) {
				if (child.getType().equals(FileType.FILE)) {
					String name = child.getName().getBaseName();
					
					if (name.endsWith(EXT_TRANSFORMATION)) {
						
						String transName = name.substring(0, name.length()-4);
						list.add( transName );
					}
				}
			}
			
			return list.toArray(new String[list.size()]);
		}
		catch(Exception e) {
			throw new KettleException("Unable to get list of transformations names in folder with id : "+id_directory, e);
		}
	}

	public ObjectId[] getTransformationPartitionSchemaIDs(ObjectId id_transformation) throws KettleException {return new ObjectId[] {}; };
	public String[] getTransformationsUsingCluster(ObjectId id_cluster) throws KettleException { return new String[] {}; }
	public String[] getTransformationsUsingDatabase(ObjectId id_database) throws KettleException { return new String[] {}; }
	public String[] getTransformationsUsingPartitionSchema(ObjectId id_partition_schema) throws KettleException { return new String[] {}; }
	public String[] getTransformationsUsingSlave(ObjectId id_slave) throws KettleException { return new String[] {}; }

	public String getVersion() {
		return FILE_REPOSITORY_VERSION;
	}

	public ObjectId insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException { return null; }
	public void insertJobEntryDatabase(ObjectId id_job, ObjectId id_jobentry, ObjectId id_database) throws KettleException { }
	public void insertJobNote(ObjectId id_job, ObjectId id_note) throws KettleException { }

	public ObjectId insertLogEntry(String description) throws KettleException {
		String logfile = calcDirectoryName(null)+LOG_FILE;
		try {
			OutputStream outputStream = KettleVFS.getOutputStream(logfile, true);
			outputStream.write(description.getBytes());
			outputStream.close();
			
			return new StringObjectId(logfile);
		} catch (IOException e) {
			throw new KettleException("Unable to write log entry to file ["+logfile+"]");
		}
	}

	public void insertStepDatabase(ObjectId id_transformation, ObjectId id_step, ObjectId id_database) throws KettleException {
		

	}

	public void insertTransNote(ObjectId id_transformation, ObjectId id_note) throws KettleException {
		

	}

	public void insertTransStepCondition(ObjectId id_transformation, ObjectId id_step, ObjectId id_condition) throws KettleException {
		

	}

	public ObjectId insertTransformationCluster(ObjectId id_transformation, ObjectId id_cluster) throws KettleException {
		
		return null;
	}

	public ObjectId insertTransformationPartitionSchema(ObjectId id_transformation, ObjectId id_partition_schema) throws KettleException {
		
		return null;
	}

	public ObjectId insertTransformationSlave(ObjectId id_transformation, ObjectId id_slave) throws KettleException {
		
		return null;
	}

	public ClusterSchema loadClusterSchema(ObjectId id_cluster_schema, List<SlaveServer> slaveServers, String versionName) throws KettleException {
		try {
			return new ClusterSchema(loadNodeFromXML(id_cluster_schema, ClusterSchema.XML_TAG), slaveServers);
		}
		catch(Exception e) {
			throw new KettleException("Unable to load cluster schema from the file repository", e);
		}
	}

	public Condition loadCondition(ObjectId id_condition) throws KettleException {
		
		return null;
	}

	public Condition loadConditionFromStepAttribute(ObjectId id_step, String code) throws KettleException {
		
		return null;
	}

	public Node loadNodeFromXML(ObjectId id, String tag) throws KettleException {
		try {
			// The object ID is the base name of the file in the Base directory folder
			//
			String filename = calcDirectoryName(null)+id.getId();
			FileObject fileObject = KettleVFS.getFileObject(filename);
			Document document = XMLHandler.loadXMLFile(fileObject);
			Node node = XMLHandler.getSubNode(document, tag);
			
			return node;
		}
		catch(Exception e) {
			throw new KettleException("Unable to load XML object from object with ID ["+id+"] and tag ["+tag+"]", e);
		}
	}

	public DatabaseMeta loadDatabaseMeta(ObjectId id_database, String versionName) throws KettleException {
		try {
			return new DatabaseMeta(loadNodeFromXML(id_database, DatabaseMeta.XML_TAG));
		}
		catch(Exception e) {
			throw new KettleException("Unable to load database connection from the file repository", e);
		}
	}

	public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute(ObjectId id_jobentry, String nameCode, String idCode, List<DatabaseMeta> databases) throws KettleException { return null; }
	public void saveDatabaseMetaJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String nameCode, String idCode, DatabaseMeta database) throws KettleException {}

	public DatabaseMeta loadDatabaseMetaFromStepAttribute(ObjectId id_step, String code, List<DatabaseMeta> databases) throws KettleException { return null; }
	
	public JobMeta loadJob(String jobname, RepositoryDirectoryInterface repdir, ProgressMonitorListener monitor, String versionName) throws KettleException {
		
		// This is a standard load of a transformation serialized in XML...
		//
		String filename = calcDirectoryName(repdir)+jobname+EXT_JOB;
		JobMeta jobMeta = new JobMeta(filename, this);
		jobMeta.setFilename(null);
		jobMeta.setName(jobname);
		jobMeta.setObjectId(new StringObjectId(calcObjectId(repdir, jobname, EXT_JOB)));
		
		return jobMeta;

	}

	public PartitionSchema loadPartitionSchema(ObjectId id_partition_schema, String versionName) throws KettleException {
		try {
			return new PartitionSchema(loadNodeFromXML(id_partition_schema, PartitionSchema.XML_TAG));
		}
		catch(Exception e) {
			throw new KettleException("Unable to load partition schema from the file repository", e);
		}
	}

	public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
		RepositoryDirectory root = new RepositoryDirectory();
		root.setObjectId(new StringObjectId("/"));
		return loadRepositoryDirectoryTree(root);
	}

	public RepositoryDirectoryInterface loadRepositoryDirectoryTree(RepositoryDirectoryInterface dir) throws KettleException {
		try {
			String folderName = calcDirectoryName(dir);
			FileObject folder = KettleVFS.getFileObject(folderName);
			
			for (FileObject child : folder.getChildren()) {
				if (child.getType().equals(FileType.FOLDER)) {
					RepositoryDirectory subDir = new RepositoryDirectory(dir, child.getName().getBaseName());
					subDir.setObjectId(new StringObjectId(calcObjectId(subDir)));
					dir.addSubdirectory(subDir);
					
					loadRepositoryDirectoryTree(subDir);
				}
			}
			
			return dir;
		}
		catch(Exception e) {
			throw new KettleException("Unable to load the directory tree from this file repository", e);
		}
	}
	

	public List<RepositoryElementMetaInterface> getTransformationObjects(ObjectId idDirectory, boolean includeDeleted) throws KettleException {
		
		try {
			List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();
			
			RepositoryDirectoryInterface tree = loadRepositoryDirectoryTree();
			RepositoryDirectoryInterface directory = tree.findDirectory(idDirectory);
			
			String folderName = calcDirectoryName(directory);
			String relativeFolderName = calcRelativeElementDirectory(directory);
			FileObject folder = KettleVFS.getFileObject(folderName);
			
			for (FileObject child : folder.getChildren()) {
				if (child.getType().equals(FileType.FILE)) {
					String name = child.getName().getBaseName();
					
					if (name.endsWith(EXT_TRANSFORMATION)) {
						
						String transName = name.substring(0, name.length()-4);
						
						ObjectId id = new StringObjectId(calcObjectId(directory, relativeFolderName+transName, EXT_TRANSFORMATION));
						Date date = new Date(child.getContent().getLastModifiedTime());
						list.add( new RepositoryObject(id, transName, directory, "-", date, RepositoryObjectType.TRANSFORMATION, "", false) );
					}
				}
			}
			
			return list;
		}
		catch(Exception e) {
			throw new KettleException("Unable to get list of transformations in folder with id : "+idDirectory, e);
		}
	}
	
	public List<RepositoryElementMetaInterface> getJobObjects(ObjectId id_directory, boolean includeDeleted) throws KettleException {
		
		try {
			List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();
			
			RepositoryDirectoryInterface tree = loadRepositoryDirectoryTree();
			RepositoryDirectoryInterface directory = tree.findDirectory(id_directory);
			
			String folderName = calcDirectoryName(directory);
			String relativeFolderName = calcRelativeElementDirectory(directory);
			FileObject folder = KettleVFS.getFileObject(folderName);
			
			for (FileObject child : folder.getChildren()) {
				if (child.getType().equals(FileType.FILE)) {
					String name = child.getName().getBaseName();
					
					if (name.endsWith(EXT_JOB)) {

					  String jobName = name.substring(0, name.length()-4);

					  ObjectId id = new StringObjectId(calcObjectId(directory, relativeFolderName+jobName, EXT_JOB));
						Date date = new Date(child.getContent().getLastModifiedTime());
						list.add( new RepositoryObject(id, jobName, directory, "-", date, RepositoryObjectType.JOB, "", false) );
					}
				}
			}
			
			return list;
		}
		catch(Exception e) {
			throw new KettleException("Unable to get list of jobs in folder with id : "+id_directory, e);
		}
	}



	public int getNrSubDirectories(ObjectId id_directory) throws KettleException {
		
		return 0;
	}

	public SlaveServer loadSlaveServer(ObjectId id_slave_server, String versionName) throws KettleException {
		try {
			return new SlaveServer(loadNodeFromXML(id_slave_server, SlaveServer.XML_TAG));
		}
		catch(Exception e) {
			throw new KettleException("Unable to load slave server from the file repository", e);
		}
	}

	public TransMeta loadTransformation(String transname, RepositoryDirectoryInterface repdir, ProgressMonitorListener monitor, boolean setInternalVariables, String versionName) throws KettleException {
		
		// This is a standard load of a transformation serialized in XML...
		//
		String filename = calcDirectoryName(repdir)+transname+".ktr";
		TransMeta transMeta = new TransMeta(filename, this, setInternalVariables);
		transMeta.setFilename(null);
		transMeta.setName(transname);
		transMeta.setObjectId(new StringObjectId(calcObjectId(repdir, transname, EXT_TRANSFORMATION)));
		return transMeta;
	}

	public ValueMetaAndData loadValueMetaAndData(ObjectId id_value) throws KettleException {
		
		return null;
	}

	public void moveJob(String jobname, ObjectId id_directory_from, ObjectId id_directory_to) throws KettleException {
		

	}

	public void moveTransformation(String transname, ObjectId id_directory_from, ObjectId id_directory_to) throws KettleException {
		

	}

	public List<DatabaseMeta> readDatabases() throws KettleException {
	  List<DatabaseMeta> list = new ArrayList<DatabaseMeta>();
		for (ObjectId id : getDatabaseIDs(false)) {
      list.add(loadDatabaseMeta(id, null));
		}
		
		return list;
	}

	public SharedObjects readJobMetaSharedObjects(JobMeta jobMeta) throws KettleException {
		
		// First the normal shared objects...
		//
		SharedObjects sharedObjects = jobMeta.readSharedObjects();
		
		// Then we read the databases etc...
		//
		for (ObjectId id : getDatabaseIDs(false)) {
			DatabaseMeta databaseMeta = loadDatabaseMeta(id, null); // Load last version
			jobMeta.addOrReplaceDatabase(databaseMeta);
		}
		
		for (ObjectId id : getSlaveIDs(false)) {
			SlaveServer slaveServer = loadSlaveServer(id, null);  // Load last version
			jobMeta.addOrReplaceSlaveServer(slaveServer);
		}

		return sharedObjects;
	}

	public SharedObjects readTransSharedObjects(TransMeta transMeta) throws KettleException {
		
		// First the normal shared objects...
		//
		SharedObjects sharedObjects = transMeta.readSharedObjects();
		
		// Then we read the databases etc...
		//
		for (ObjectId id : getDatabaseIDs(false)) {
			DatabaseMeta databaseMeta = loadDatabaseMeta(id, null);  // Load last version
			transMeta.addOrReplaceDatabase(databaseMeta);
		}
		
		for (ObjectId id : getSlaveIDs(false)) {
			SlaveServer slaveServer = loadSlaveServer(id, null);  // Load last version 
			transMeta.addOrReplaceSlaveServer(slaveServer);
		}

		for (ObjectId id : getClusterIDs(false)) {
			ClusterSchema clusterSchema = loadClusterSchema(id, transMeta.getSlaveServers(), null);  // Load last version 
			transMeta.addOrReplaceClusterSchema(clusterSchema);
		}

		for (ObjectId id : getPartitionSchemaIDs(false)) {
			PartitionSchema partitionSchema = loadPartitionSchema(id, null);  // Load last version
			transMeta.addOrReplacePartitionSchema(partitionSchema);
		}
		
		return sharedObjects;
	}

	private ObjectId renameObject(ObjectId id, RepositoryDirectoryInterface newDirectory, String newName, String extension) throws KettleException {
		try {
			// In case of a root object, the ID is the same as the relative filename...
			//
			FileObject fileObject = KettleVFS.getFileObject(calcDirectoryName(null)+id.getId());

			// Same name, different folder?
			if (Const.isEmpty(newName)) {
				newName = calcObjectName(id);
			}
			// The new filename can be anywhere so we re-calculate a new ID...
			//
			String newFilename = calcDirectoryName(newDirectory)+newName+extension;
			
			FileObject newObject = KettleVFS.getFileObject(newFilename);
			fileObject.moveTo(newObject);
			
			return new StringObjectId(calcObjectId(newDirectory, newName, extension));
		}
		catch (Exception e) {
			throw new KettleException("Unable to rename object with ID ["+id+"] to ["+newName+"]", e);
		}
		
	}

	private String calcObjectName(ObjectId id) {
		int slashIndex = id.getId().lastIndexOf('/');
		int dotIndex = id.getId().lastIndexOf('.');
		
		return id.getId().substring(slashIndex+1, dotIndex);
	}

	public ObjectId renameJob(ObjectId id_job, RepositoryDirectoryInterface newDir, String newName) throws KettleException {
		return renameObject(id_job, newDir, newName, EXT_JOB);

	}

	public ObjectId renameRepositoryDirectory(ObjectId id, RepositoryDirectoryInterface newParentDir, String newName) throws KettleException {
	  if(newParentDir != null || newName != null) {
  	  try {
        // In case of a root object, the ID is the same as the relative filename...
  	    RepositoryDirectoryInterface tree = loadRepositoryDirectoryTree();
  	    RepositoryDirectoryInterface dir = tree.findDirectory(id);
  	    
  	    if(dir == null) {
  	      throw new KettleException("Could not find folder [" + id + "]");
  	    }
  	    
  	    // If newName is null, keep the current name
  	    newName = (newName != null) ? newName : dir.getName();
  
        FileObject folder = KettleVFS.getFileObject(dir.getPath());
        
        String newFolderName = null;
        
        if(newParentDir != null) {
          FileObject newParentFolder = KettleVFS.getFileObject(newParentDir.getPath());
          
          newFolderName = newParentFolder.toString() + "/" + newName;
        } else {
          newFolderName = folder.getParent().toString() + "/" + newName;
        }
        
        FileObject newFolder = KettleVFS.getFileObject(newFolderName);
        folder.moveTo(newFolder);
        
        return new StringObjectId(dir.getObjectId());
      }
      catch (Exception e) {
        throw new KettleException("Unable to rename directory folder to ["+id+"]");
      }
	  }
	  return(id);
	}

	public ObjectId renameTransformation(ObjectId id_transformation, RepositoryDirectoryInterface newDir, String newName) throws KettleException {
		return renameObject(id_transformation, newDir, newName, EXT_TRANSFORMATION);
	}

	public ObjectId saveCondition(Condition condition) throws KettleException { return null; }
	public ObjectId saveCondition(Condition condition, ObjectId id_condition_parent) throws KettleException { return null; }

	public void saveConditionStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, Condition condition) throws KettleException {}
	public void saveDatabaseMetaJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, DatabaseMeta database) throws KettleException {}
	public void saveDatabaseMetaStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, DatabaseMeta database) throws KettleException {}

	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, String value) throws KettleException {}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, String value) throws KettleException {}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, boolean value) throws KettleException {}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, boolean value) throws KettleException {}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, int nr, String code, long value) throws KettleException {}
	public void saveJobEntryAttribute(ObjectId id_job, ObjectId id_jobentry, String code, long value) throws KettleException { }

	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, String value) throws KettleException {}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, String value) throws KettleException {}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, boolean value) throws KettleException {}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, boolean value) throws KettleException { }
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, long value) throws KettleException {}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, long value) throws KettleException {}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, int nr, String code, double value) throws KettleException {}
	public void saveStepAttribute(ObjectId id_transformation, ObjectId id_step, String code, double value) throws KettleException {}
	public void delUser(ObjectId id_user) throws KettleException { }
	public ObjectId getUserID(String login) throws KettleException { return null; }
	public ObjectId[] getUserIDs() throws KettleException { return new ObjectId[] {}; }
	public IUser getUserInfo() { return null; }
	public String[] getUserLogins() throws KettleException { return new String[] {}; }

	public UserInfo loadUserInfo(String login) throws KettleException { return null; }
	public UserInfo loadUserInfo(String login, String password) throws KettleException { return null; }
	public void renameUser(ObjectId id_user, String newname) throws KettleException {}
	public void saveUserInfo(UserInfo userInfo) throws KettleException {}
	
	// Not used...
	public int countNrJobEntryAttributes(ObjectId id_jobentry, String code) throws KettleException { return 0; }
	public int countNrStepAttributes(ObjectId id_step, String code) throws KettleException { return 0; }


	/**
	 * @return the repositoryMeta
	 */
	public KettleFileRepositoryMeta getRepositoryMeta() {
		return repositoryMeta;
	}

	/**
	 * @param repositoryMeta the repositoryMeta to set
	 */
	public void setRepositoryMeta(KettleFileRepositoryMeta repositoryMeta) {
		this.repositoryMeta = repositoryMeta;
	}
	
	public void undeleteObject(RepositoryElementMetaInterface repositoryObject) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<RepositoryElementMetaInterface> getJobAndTransformationObjects(ObjectId id_directory, boolean includeDeleted)
      throws KettleException {
    // TODO not the most efficient impl; also, no sorting is done
    List<RepositoryElementMetaInterface> objs = new ArrayList<RepositoryElementMetaInterface>();
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
  
  public RepositoryDirectoryInterface getDefaultSaveDirectory(RepositoryElementInterface repositoryElement)
  throws KettleException {
    return getUserHomeDirectory();
  }

  public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    RepositoryDirectory root = new RepositoryDirectory();
    root.setObjectId(null);
    return loadRepositoryDirectoryTree(root);
  }
  
  public RepositoryObject getObjectInformation(ObjectId objectId, RepositoryObjectType objectType) throws KettleException {
    try {
      String filename = calcDirectoryName(null);
      if (objectId.getId().startsWith("/")) {
        filename+=objectId.getId().substring(1);
      } else {
        filename+=objectId.getId();
      }
      FileObject fileObject = KettleVFS.getFileObject(filename);
      if (!fileObject.exists()) {
        return null;
      }
      FileName fname = fileObject.getName();
      String name = fname.getBaseName();
      if (!Const.isEmpty(fname.getExtension()) && name.length()>fname.getExtension().length()) {
        name = name.substring(0, name.length()-fname.getExtension().length()-1);
      }
      
      String filePath = fileObject.getParent().getName().getPath();
      String dirPath = filePath.substring(repositoryMeta.getBaseDirectory().length());
      RepositoryDirectoryInterface directory = loadRepositoryDirectoryTree().findDirectory(dirPath);
      Date lastModified = new Date(fileObject.getContent().getLastModifiedTime());
          
      return new RepositoryObject(objectId, name, directory, "-", lastModified, objectType, "", false);
      
    } catch(Exception e) {
      throw new KettleException("Unable to get object information for object with id="+objectId, e);
    }
  }

  public JobMeta loadJob(ObjectId idJob, String versionLabel) throws KettleException {
    RepositoryObject jobInfo = getObjectInformation(idJob, RepositoryObjectType.JOB);
    return loadJob(jobInfo.getName(), jobInfo.getRepositoryDirectory(), null, versionLabel);
  }

  public TransMeta loadTransformation(ObjectId idTransformation, String versionLabel) throws KettleException {
    RepositoryObject jobInfo = getObjectInformation(idTransformation, RepositoryObjectType.TRANSFORMATION);
    return loadTransformation(jobInfo.getName(), jobInfo.getRepositoryDirectory(), null, true, versionLabel);
  }
}
