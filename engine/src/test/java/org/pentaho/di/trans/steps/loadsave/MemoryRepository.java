/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/



package org.pentaho.di.trans.steps.loadsave;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MemoryRepository extends AbstractRepository {
  protected final Map<ObjectId, Map<Integer, Map<String, String>>> stepAttributeMap =
      new HashMap<ObjectId, Map<Integer, Map<String, String>>>();
  protected final Map<ObjectId, Map<Integer, Map<String, String>>> jobAttributeMap =
      new HashMap<ObjectId, Map<Integer, Map<String, String>>>();
  protected final Map<ObjectId, RepositoryElementInterface> elements = new HashMap<>();

  public MemoryRepository() {

  }

  private void populateMap( Map<ObjectId, Map<Integer, Map<String, String>>> attributeMap, JSONObject jsonObject ) {
    for ( Object objectId : jsonObject.keySet() ) {
      JSONObject nrsObject = (JSONObject) jsonObject.get( objectId );
      for ( Object nrKey : nrsObject.keySet() ) {
        JSONObject nrObject = (JSONObject) nrsObject.get( nrKey );
        for ( Object stringKey : nrObject.keySet() ) {
          setAttribute( attributeMap, new StringObjectId( objectId.toString() ), Integer.valueOf( nrKey.toString() ),
              stringKey.toString(), nrObject.get( stringKey ).toString() );
        }
      }
    }
  }

  public MemoryRepository( String json ) throws ParseException {
    Object repoObj = new JSONParser().parse( json );
    JSONObject jsonRepoObj = (JSONObject) repoObj;
    populateMap( stepAttributeMap, (JSONObject) jsonRepoObj.get( "step" ) );
    populateMap( jobAttributeMap, (JSONObject) jsonRepoObj.get( "job" ) );
  }

  private String getAttribute( Map<ObjectId, Map<Integer, Map<String, String>>> attributeMap, ObjectId id, int nr,
      String code, String def ) {
    String value = null;
    Map<Integer, Map<String, String>> stepMap = attributeMap.get( id );
    if ( stepMap != null ) {
      Map<String, String> numberMap = stepMap.get( nr );
      if ( numberMap != null ) {
        value = numberMap.get( code );
      }
    }
    return value == null ? def : value;
  }

  private void setAttribute( Map<ObjectId, Map<Integer, Map<String, String>>> attributeMap, ObjectId id, int nr,
      String code, String value ) {
    Map<Integer, Map<String, String>> stepMap = attributeMap.get( id );
    if ( stepMap == null ) {
      stepMap = new HashMap<Integer, Map<String, String>>();
      attributeMap.put( id, stepMap );
    }
    Map<String, String> numberMap = stepMap.get( nr );
    if ( numberMap == null ) {
      numberMap = new HashMap<String, String>();
      stepMap.put( nr, numberMap );
    }
    if ( numberMap.containsKey( code ) ) {
      // PDI-15793
      throw new RuntimeException(
        "Tried to insert code [" + code + "] twice, which may not be supported by all repository types." );
    }
    numberMap.put( code, value );
  }

  private String getStepAttribute( ObjectId id_step, int nr, String code, String def ) {
    return getAttribute( stepAttributeMap, id_step, nr, code, def );
  }

  private void setStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code, String value ) {
    setAttribute( stepAttributeMap, id_step, nr, code, value );
  }

  private String getJobAttribute( ObjectId id_job, int nr, String code, String def ) {
    return getAttribute( jobAttributeMap, id_job, nr, code, def );
  }

  private void setJobAttribute( ObjectId id_job, int nr, String code, String value ) {
    setAttribute( jobAttributeMap, id_job, nr, code, value );
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryMeta getRepositoryMeta() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IUser getUserInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositorySecurityProvider getSecurityProvider() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositorySecurityManager getSecurityManager() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LogChannelInterface getLog() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void connect( String username, String password ) throws KettleException, KettleSecurityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void disconnect() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isConnected() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void init( RepositoryMeta repositoryMeta ) {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean
    exists( String name, RepositoryDirectoryInterface repositoryDirectory, RepositoryObjectType objectType ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ObjectId getTransformationID( String name, RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId getJobId( String name, RepositoryDirectoryInterface repositoryDirectory ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void save( RepositoryElementInterface repositoryElement, String versionComment,
      ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {
    save( repositoryElement );
  }

  @Override
  public void save( RepositoryElementInterface repositoryElement, String versionComment, Calendar versionDate,
      ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {
    save( repositoryElement );
  }

  private void save( RepositoryElementInterface repositoryElement ) {
    if ( repositoryElement.getObjectId() == null ) {
      RepositoryElementInterface existingElement = findExisting( repositoryElement );
      if ( existingElement != null ) {
        repositoryElement.setObjectId( existingElement.getObjectId() );
      } else {
        repositoryElement.setObjectId( new StringObjectId( UUID.randomUUID().toString() ) );
      }
    }
    elements.put( repositoryElement.getObjectId(), repositoryElement );
  }

  private RepositoryElementInterface findExisting( RepositoryElementInterface candidate ) {
    return elements.values().stream()
      .filter( e -> e.getClass().equals( candidate.getClass() ) && candidate.getName().equals( e.getName() ) )
      .findAny().orElse( null );
    }

  @Override
  public RepositoryDirectoryInterface getDefaultSaveDirectory( RepositoryElementInterface repositoryElement )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clearSharedObjectCache() {
    // TODO Auto-generated method stub

  }

  @Override
  public TransMeta loadTransformation( String transname, RepositoryDirectoryInterface repdir,
      ProgressMonitorListener monitor, boolean setInternalVariables, String revision ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TransMeta loadTransformation( ObjectId id_transformation, String versionLabel ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void readTransSharedObjects( TransMeta transMeta ) throws KettleException {
    // TODO Auto-generated method stub
  }

  @Override
  public ObjectId renameTransformation( ObjectId id_transformation, RepositoryDirectoryInterface newDirectory,
      String newName ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId renameTransformation( ObjectId id_transformation, String versionComment,
    RepositoryDirectoryInterface newDirectory, String newName ) throws KettleException {
    return null;
  }

  @Override
  public void deleteTransformation( ObjectId id_transformation ) throws KettleException {
    // TODO Auto-generated method stub

  }

  @Override
  public JobMeta loadJob( String jobname, RepositoryDirectoryInterface repdir, ProgressMonitorListener monitor,
      String revision ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JobMeta loadJob( ObjectId id_job, String versionLabel ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void readJobMetaSharedObjects( JobMeta jobMeta ) throws KettleException {
    // TODO Auto-generated method stub
  }

  @Override
  public ObjectId renameJob( ObjectId id_job, RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId renameJob( ObjectId id_job, String versionComment, RepositoryDirectoryInterface newDirectory,
    String newName ) throws KettleException {
    return null;
  }

  @Override
  public void deleteJob( ObjectId id_job ) throws KettleException {
    // TODO Auto-generated method stub

  }

  @Override
  public DatabaseMeta loadDatabaseMeta( ObjectId id_database, String revision ) throws KettleException {
    RepositoryElementInterface element = elements.get( id_database );
    if ( element instanceof DatabaseMeta ) {
      return (DatabaseMeta) element;
    }
    return null;
  }

  @Override
  public void deleteDatabaseMeta( String databaseName ) throws KettleException {
    ObjectId id = getDatabaseID( databaseName );
    if ( id != null ) {
      elements.remove( id );
    }
  }

  @Override
  public ObjectId[] getDatabaseIDs( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof DatabaseMeta )
      .map( RepositoryElementInterface::getObjectId )
      .toArray( ObjectId[]::new );
  }

  @Override
  public String[] getDatabaseNames( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof DatabaseMeta )
      .map( RepositoryElementInterface::getName )
      .toArray( String[]::new );
  }

  @Override
  public List<DatabaseMeta> readDatabases() throws KettleException {
    return elements.values().stream().filter( e -> e instanceof DatabaseMeta )
      .map( e -> (DatabaseMeta) e ).collect( Collectors.toList() );
  }

  @Override
  public ObjectId getDatabaseID( String name ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof DatabaseMeta && name.equals( e.getName() ) )
      .map( RepositoryElementInterface::getObjectId )
      .findAny().orElse( null );
  }

  @Override
  public ClusterSchema loadClusterSchema( ObjectId id_cluster_schema, List<SlaveServer> slaveServers,
      String versionLabel ) throws KettleException {
    RepositoryElementInterface element = elements.get( id_cluster_schema );
    if ( element instanceof ClusterSchema ) {
      return (ClusterSchema) element;
    }
    return null;
  }

  @Override
  public ObjectId[] getClusterIDs( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof ClusterSchema )
      .map( RepositoryElementInterface::getObjectId )
      .toArray( ObjectId[]::new );
  }

  @Override
  public String[] getClusterNames( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof ClusterSchema )
      .map( RepositoryElementInterface::getName )
      .toArray( String[]::new );
  }

  @Override
  public ObjectId getClusterID( String name ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof ClusterSchema && name.equals( e.getName() ) )
      .map( RepositoryElementInterface::getObjectId )
      .findAny().orElse( null );
  }

  @Override
  public void deleteClusterSchema( ObjectId id_cluster ) throws KettleException {
    Object value = elements.get( id_cluster );
    if ( value instanceof ClusterSchema ) {
      elements.remove( id_cluster );
    }
  }

  @Override
  public SlaveServer loadSlaveServer( ObjectId id_slave_server, String versionLabel ) throws KettleException {
    RepositoryElementInterface element = elements.get( id_slave_server );
    if ( element instanceof SlaveServer ) {
      return (SlaveServer) element;
    }
    return null;
  }

  @Override
  public ObjectId[] getSlaveIDs( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof SlaveServer )
      .map( RepositoryElementInterface::getObjectId )
      .toArray( ObjectId[]::new );
  }

  @Override
  public String[] getSlaveNames( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof SlaveServer )
      .map( RepositoryElementInterface::getName )
      .toArray( String[]::new );
  }

  @Override
  public List<SlaveServer> getSlaveServers() throws KettleException {
    return elements.values().stream().filter( e -> e instanceof SlaveServer )
      .map( e -> (SlaveServer) e ).collect( Collectors.toList() );
  }

  @Override
  public ObjectId getSlaveID( String name ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof SlaveServer && name.equals( e.getName() ) )
      .map( RepositoryElementInterface::getObjectId )
      .findAny().orElse( null );
  }

  @Override
  public void deleteSlave( ObjectId id_slave ) throws KettleException {
    Object value = elements.get( id_slave );
    if ( value instanceof SlaveServer ) {
      elements.remove( id_slave );
    }
  }

  @Override
  public PartitionSchema loadPartitionSchema( ObjectId id_partition_schema, String versionLabel )
    throws KettleException {
    RepositoryElementInterface element = elements.get( id_partition_schema );
    if ( element instanceof PartitionSchema ) {
      return (PartitionSchema) element;
    }
    return null;
  }

  @Override
  public ObjectId[] getPartitionSchemaIDs( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof PartitionSchema )
      .map( RepositoryElementInterface::getObjectId )
      .toArray( ObjectId[]::new );
  }

  @Override
  public String[] getPartitionSchemaNames( boolean includeDeleted ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof PartitionSchema )
      .map( RepositoryElementInterface::getName )
      .toArray( String[]::new );
  }

  @Override
  public ObjectId getPartitionSchemaID( String name ) throws KettleException {
    return elements.values().stream()
      .filter( e -> e instanceof PartitionSchema && name.equals( e.getName() ) )
      .map( RepositoryElementInterface::getObjectId )
      .findAny().orElse( null );
  }

  @Override
  public void deletePartitionSchema( ObjectId id_partition_schema ) throws KettleException {
    Object value = elements.get( id_partition_schema );
    if ( value instanceof PartitionSchema ) {
      elements.remove( id_partition_schema );
    }
  }

  @Override
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( String directory ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( ObjectId directory ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void saveRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    // TODO Auto-generated method stub

  }

  @Override
  public ObjectId renameRepositoryDirectory( ObjectId id, RepositoryDirectoryInterface newParentDir, String newName )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface createRepositoryDirectory( RepositoryDirectoryInterface parentDirectory,
      String directoryPath ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getTransformationNames( ObjectId id_directory, boolean includeDeleted ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobObjects( ObjectId id_directory, boolean includeDeleted )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryElementMetaInterface> getTransformationObjects( ObjectId id_directory, boolean includeDeleted )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobAndTransformationObjects( ObjectId id_directory,
      boolean includeDeleted ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getJobNames( ObjectId id_directory, boolean includeDeleted ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getDirectoryNames( ObjectId id_directory ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId insertLogEntry( String description ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void insertStepDatabase( ObjectId id_transformation, ObjectId id_step, ObjectId id_database )
    throws KettleException {
    // TODO Auto-generated method stub

  }

  @Override
  public void insertJobEntryDatabase( ObjectId id_job, ObjectId id_jobentry, ObjectId id_database )
    throws KettleException {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveConditionStepAttribute( ObjectId id_transformation, ObjectId id_step, String code, Condition condition )
    throws KettleException {
    this.saveStepAttribute( id_transformation, id_step, code, condition.getXML() );
  }

  @Override
  public Condition loadConditionFromStepAttribute( ObjectId id_step, String code ) throws KettleException {
    // TODO Auto-generated method stub
    String tmp = this.getStepAttributeString( id_step, code );
    return new Condition( tmp );
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId id_step, int nr, String code, boolean def ) throws KettleException {
    return "Y".equalsIgnoreCase( getStepAttribute( id_step, nr, code, def ? "Y" : "N" ) );
  }

  @Override
  public long getStepAttributeInteger( ObjectId id_step, int nr, String code ) throws KettleException {
    return Long.valueOf( getStepAttribute( id_step, nr, code, "0" ) );
  }

  @Override
  public String getStepAttributeString( ObjectId id_step, int nr, String code ) throws KettleException {
    return getStepAttribute( id_step, nr, code, null );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code, String value )
    throws KettleException {
    setStepAttribute( id_transformation, id_step, nr, code, value );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code, boolean value )
    throws KettleException {
    setStepAttribute( id_transformation, id_step, nr, code, value ? "Y" : "N" );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code, long value )
    throws KettleException {
    setStepAttribute( id_transformation, id_step, nr, code, Long.toString( value ) );
  }

  @Override
  public void saveStepAttribute( ObjectId id_transformation, ObjectId id_step, int nr, String code, double value )
    throws KettleException {
    setStepAttribute( id_transformation, id_step, nr, code, Double.toString( value ) );
  }

  @Override
  public int countNrStepAttributes( ObjectId id_step, String code ) throws KettleException {
    Map<Integer, Map<String, String>> stepMap = stepAttributeMap.get( id_step );
    int count = 0;
    if ( stepMap != null ) {
      for ( Entry<Integer, Map<String, String>> entry : stepMap.entrySet() ) {
        Map<String, String> value = entry.getValue();
        if ( value != null && value.get( code ) != null ) {
          count++;
        }
      }
    }

    return count;
  }

  @Override
  public int countNrJobEntryAttributes( ObjectId id_jobentry, String code ) throws KettleException {
    Map<Integer, Map<String, String>> jobMap = jobAttributeMap.get( id_jobentry );
    int count = 0;
    if  ( jobMap != null ) {
      for ( Entry<Integer, Map<String, String>> entry : jobMap.entrySet() ) {
        Map<String, String> value = entry.getValue();
        if ( value != null && value.get( code ) != null ) {
          count++;
        }
      }
    }

    return count;
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId id_jobentry, int nr, String code ) throws KettleException {
    return Long.parseLong( getJobAttribute( id_jobentry, nr, code, "0" ) );
  }

  @Override
  public String getJobEntryAttributeString( ObjectId id_jobentry, int nr, String code ) throws KettleException {
    return getJobAttribute( id_jobentry, nr, code, null );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, String value )
    throws KettleException {
    setJobAttribute( id_jobentry, nr, code, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, boolean value )
    throws KettleException {
    setJobAttribute( id_jobentry, nr, code, value ? "Y" : "N" );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String code, long value )
    throws KettleException {
    setJobAttribute( id_jobentry, nr, code, Long.toString( value ) );
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId id_step, String code, List<DatabaseMeta> databases )
    throws KettleException {
    long id_database = getStepAttributeInteger( id_step, code );
    if ( id_database <= 0 ) {
      return null;
    }
    return DatabaseMeta.findDatabase( databases, new LongObjectId( id_database ) );
  }

  @Override
  public void saveDatabaseMetaStepAttribute( ObjectId id_transformation, ObjectId id_step, String code,
      DatabaseMeta database ) throws KettleException {
    ObjectId id = null;
    if ( database != null ) {
      id = database.getObjectId();
      Long id_database = id == null ? Long.valueOf( -1L ) : new LongObjectId( id ).longValue();
      saveStepAttribute( id_transformation, id_step, code, id_database );
    }
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId id_jobentry, String nameCode, int nr,
      String idCode, List<DatabaseMeta> databases ) throws KettleException {
    long id_database = getJobEntryAttributeInteger( id_jobentry, nr, idCode );
    if ( id_database <= 0 ) {
      String name = getJobEntryAttributeString( id_jobentry, nr, nameCode );
      if ( name == null ) {
        return null;
      }
      return DatabaseMeta.findDatabase( databases, name );
    }
    return DatabaseMeta.findDatabase( databases, new LongObjectId( id_database ) );
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId id_job, ObjectId id_jobentry, int nr, String nameCode,
      String idCode, DatabaseMeta database ) throws KettleException {
    ObjectId id = null;
    if ( database != null ) {
      id = database.getObjectId();
      Long id_database = id == null ? Long.valueOf( -1L ) : new LongObjectId( id ).longValue();

      // Save both the ID and the name of the database connection...
      //
      saveJobEntryAttribute( id_job, id_jobentry, nr, idCode, id_database );
      saveJobEntryAttribute( id_job, id_jobentry, nr, nameCode, id_database );

      insertJobEntryDatabase( id_job, id_jobentry, id );
    }
  }

  @Override
  public void undeleteObject( RepositoryElementMetaInterface repositoryObject ) throws KettleException {
    // TODO Auto-generated method stub

  }

  @Override
  public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IRepositoryService getService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RepositoryObject getObjectInformation( ObjectId objectId, RepositoryObjectType objectType )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getConnectMessage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getJobsUsingDatabase( ObjectId id_database ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getTransformationsUsingDatabase( ObjectId id_database ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IRepositoryImporter getImporter() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IRepositoryExporter getExporter() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStore getRepositoryMetaStore() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId id_jobentry, int nr, String code, boolean def )
    throws KettleException {
    return "Y".equalsIgnoreCase( getJobAttribute( id_jobentry, nr, code, def ? "Y" : "N" ) );
  }

}
