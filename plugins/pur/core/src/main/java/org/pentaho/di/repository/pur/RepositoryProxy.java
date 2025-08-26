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

package org.pentaho.di.repository.pur;

import java.util.Calendar;
import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
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
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryLock;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

/**
 * A {@link Repository} that stands in for the real repository, collecting entry and step attributes and loading or
 * saving them as a batch. Use one instance of this class per entry or step!
 */
public class RepositoryProxy extends AbstractRepository implements ILockService, java.io.Serializable {

  private static final long serialVersionUID = -8017798761569450448L; /* EESOURCE: UPDATE SERIALVERUID */

  private static Class<?> PKG = RepositoryProxy.class;

  public static final String PROP_CODE_NR_SEPARATOR = "_#_"; //$NON-NLS-1$

  private static final String PROPERTY_XML = "XML"; //$NON-NLS-1$

  private static final String NAME_EXT = "name";

  private DataNode node;

  private Repository parentRep = null;

  public RepositoryProxy( final DataNode node ) {
    super();
    this.node = node;
  }

  public RepositoryProxy( final DataNode node, Repository parentRep ) {
    super();
    this.node = node;
    this.parentRep = parentRep;
  }

  @Override
  public void connect( String username, String password ) throws KettleException, KettleSecurityException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int countNrJobEntryAttributes( ObjectId idJobentry, String code ) throws KettleException {
    return getPropertyCount( idJobentry, code );
  }

  protected int getPropertyCount( final ObjectId idStep, final String code ) throws KettleException {
    int count = 0;
    for ( DataProperty prop : node.getProperties() ) {
      if ( prop.getName().startsWith( code + PROP_CODE_NR_SEPARATOR ) ) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int countNrStepAttributes( final ObjectId idStep, final String code ) throws KettleException {
    return getPropertyCount( idStep, code );
  }

  @Override
  public RepositoryDirectoryInterface createRepositoryDirectory( final RepositoryDirectoryInterface parentDirectory,
      final String directoryPath ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteClusterSchema( ObjectId idCluster ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteDatabaseMeta( String databaseName ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteJob( ObjectId idJob ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deletePartitionSchema( ObjectId idPartitionSchema ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteSlave( ObjectId idSlave ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteTransformation( ObjectId idTransformation ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void disconnect() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean
    exists( String name, RepositoryDirectoryInterface repositoryDirectory, RepositoryObjectType objectType )
      throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId getClusterID( String name ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId[] getClusterIDs( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getClusterNames( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId getDatabaseID( String name ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId[] getDatabaseIDs( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getDatabaseNames( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getDirectoryNames( ObjectId idDirectory ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId idJobentry, int nr, String code, boolean def )
    throws KettleException {
    String attribute = code + PROP_CODE_NR_SEPARATOR + nr;
    if ( node.hasProperty( attribute ) ) {
      return node.getProperty( attribute ).getBoolean();
    } else if ( nr == 0 && node.hasProperty( code ) ) {
      // Old pur stored elements with no nr when not specified
      return node.getProperty( code ).getBoolean();
    } else {
      return def;
    }
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId idJobentry, int nr, String code ) throws KettleException {
    if ( node.hasProperty( code + PROP_CODE_NR_SEPARATOR + nr ) ) {
      return node.getProperty( code + PROP_CODE_NR_SEPARATOR + nr ).getLong();
    } else if ( nr == 0 && node.hasProperty( code ) ) {
      // Old pur stored elements with no nr when not specified
      return node.getProperty( code ).getLong();
    } else {
      return 0;
    }
  }

  @Override
  public String getJobEntryAttributeString( ObjectId idJobentry, int nr, String code ) throws KettleException {
    String propName = code + PROP_CODE_NR_SEPARATOR + nr;
    if ( node.hasProperty( propName ) ) {
      return node.getProperty( propName ).getString();
    } else if ( nr == 0 && node.hasProperty( code ) ) {
      // Old pur stored elements with no nr when not specified
      return node.getProperty( code ).getString();
    } else {
      return null;
    }
  }

  @Override
  public ObjectId getJobId( String name, RepositoryDirectoryInterface repositoryDirectory ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryLock getJobLock( ObjectId idJob ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getJobNames( ObjectId idDirectory, boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobObjects( ObjectId idDirectory, boolean includeDeleted )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public LogChannelInterface getLog() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId getPartitionSchemaID( String name ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId[] getPartitionSchemaIDs( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getPartitionSchemaNames( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryMeta getRepositoryMeta() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositorySecurityProvider getSecurityProvider() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositorySecurityManager getSecurityManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId getSlaveID( String name ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId[] getSlaveIDs( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getSlaveNames( boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<SlaveServer> getSlaveServers() throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId idStep, int nr, String code, boolean def ) throws KettleException {
    if ( node.hasProperty( code + PROP_CODE_NR_SEPARATOR + nr ) ) {
      return node.getProperty( code + PROP_CODE_NR_SEPARATOR + nr ).getBoolean();
    } else if ( nr == 0 && node.hasProperty( code ) ) {
      // Old pur stored elements with no nr when not specified
      return node.getProperty( code ).getBoolean();
    } else {
      return def;
    }
  }

  @Override
  public long getStepAttributeInteger( ObjectId idStep, int nr, String code ) throws KettleException {
    if ( node.hasProperty( code + PROP_CODE_NR_SEPARATOR + nr ) ) {
      return node.getProperty( code + PROP_CODE_NR_SEPARATOR + nr ).getLong();
    } else if ( nr == 0 && node.hasProperty( code ) ) {
      // Old pur stored elements with no nr when not specified
      return node.getProperty( code ).getLong();
    } else {
      return 0;
    }
  }

  @Override
  public String getStepAttributeString( ObjectId idStep, int nr, String code ) throws KettleException {
    String propName = code + PROP_CODE_NR_SEPARATOR + nr;
    if ( node.hasProperty( propName ) ) {
      return node.getProperty( propName ).getString();
    } else if ( nr == 0 && node.hasProperty( code ) ) {
      // Old pur stored elements with no nr when not specified
      return node.getProperty( code ).getString();
    } else {
      return null;
    }
  }

  @Override
  public ObjectId getTransformationID( String name, RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryLock getTransformationLock( ObjectId idTransformation ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getTransformationNames( ObjectId idDirectory, boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<RepositoryElementMetaInterface> getTransformationObjects( ObjectId idDirectory, boolean includeDeleted )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UserInfo getUserInfo() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getVersion() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void init( RepositoryMeta repositoryMeta ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void insertJobEntryDatabase( ObjectId idJob, ObjectId idJobentry, ObjectId idDatabase ) throws KettleException {
    DataNodeRef ref = new DataNodeRef( idDatabase.getId() );
    node.setProperty( idDatabase.getId(), ref );
  }

  @Override
  public ObjectId insertLogEntry( String description ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void insertStepDatabase( ObjectId idTransformation, ObjectId idStep, ObjectId idDatabase )
    throws KettleException {
    if ( idDatabase != null ) {
      DataNodeRef ref = new DataNodeRef( idDatabase.getId() );
      node.setProperty( idDatabase.getId(), ref );
    }
  }

  @Override
  public boolean isConnected() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ClusterSchema
    loadClusterSchema( ObjectId idClusterSchema, List<SlaveServer> slaveServers, String versionLabel )
      throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Condition loadConditionFromStepAttribute( ObjectId idStep, String code ) throws KettleException {
    DataNode conditionNode = node.getNode( code );
    if ( conditionNode.hasProperty( PROPERTY_XML ) ) {
      String xml = conditionNode.getProperty( PROPERTY_XML ).getString();
      Condition condition = new Condition( XMLHandler.getSubNode( XMLHandler.loadXMLString( xml ), Condition.XML_TAG ) );
      return condition;
    } else {
      return null;
    }
  }

  @Override
  public DatabaseMeta loadDatabaseMeta( ObjectId idDatabase, String revision ) throws KettleException {
    if ( parentRep != null ) {
      return parentRep.loadDatabaseMeta( idDatabase, revision );
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId idJobentry, String nameCode, int nr, String code,
      List<DatabaseMeta> databases ) throws KettleException {
    String attribute = code + PROP_CODE_NR_SEPARATOR + nr;
    return loadDatabaseMeta( attribute, databases );
  }

  private DatabaseMeta loadDatabaseMeta( String code, List<DatabaseMeta> databases ) throws KettleException {
    DataProperty codeProp = node.getProperty( code );
    if ( codeProp != null ) {
      if ( DataNodeRef.REF_MISSING.equals( codeProp.getRef().getId() )
          && System.getProperty( "kettle.allow_missing_refs" ) == null ) {
        throw new KettleException( BaseMessages.getString( PKG, "RepositoryProxy.ERROR_0001_MISSING_REF" ) );
      }
      ObjectId databaseId = new StringObjectId( node.getProperty( code ).getRef().getId().toString() );
      DatabaseMeta databaseMeta = DatabaseMeta.findDatabase( databases, databaseId );
      if ( databaseMeta != null ) {
        return databaseMeta;
      } else {
        // the referenced ObjectId is not in the current set of DBs. It may have been overridden by another.
        // Try to find the original DB and load the overriding one by name.
        DatabaseMeta orig = loadDatabaseMeta( databaseId, null );
        if ( orig != null ) {
          return DatabaseMeta.findDatabase( databases, orig.getName() );
        }
      }
    } else {
      DataProperty nameProp = node.getProperty( code + PROP_CODE_NR_SEPARATOR + NAME_EXT );
      if ( nameProp != null ) {
        String dbName = nameProp.getString();
        return DatabaseMeta.findDatabase( databases, dbName );
      }
    }
    return null;
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId idStep, String code, List<DatabaseMeta> databases )
    throws KettleException {
    if ( code != null ) {
      return loadDatabaseMeta( code, databases );
    }
    return null;
  }

  @Override
  public JobMeta loadJob( String jobname, RepositoryDirectoryInterface repdir, ProgressMonitorListener monitor,
      String revision ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PartitionSchema loadPartitionSchema( ObjectId idPartitionSchema, String versionLabel ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( String directory ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( ObjectId directory ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public SlaveServer loadSlaveServer( ObjectId idSlaveServer, String versionLabel ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public TransMeta loadTransformation( String transname, RepositoryDirectoryInterface repdir,
      ProgressMonitorListener monitor, boolean setInternalVariables, String revision ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryLock lockJob( ObjectId idJob, String message ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryLock lockTransformation( ObjectId idTransformation, String message ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DatabaseMeta> readDatabases() throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearSharedObjectCache() {
    // no op
  }

  @Override
  public void readJobMetaSharedObjects( JobMeta jobMeta ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readTransSharedObjects( TransMeta transMeta ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId renameJob( ObjectId idJob, RepositoryDirectoryInterface newDirectory, String newName )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId renameJob( ObjectId idJob, String versionComment, RepositoryDirectoryInterface newDirectory,
      String newName ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId renameRepositoryDirectory( ObjectId id, RepositoryDirectoryInterface newParentDir, String newName )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId renameTransformation( ObjectId idTransformation, RepositoryDirectoryInterface newDirectory,
      String newName ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId renameTransformation( ObjectId idTransformation, String versionComment,
      RepositoryDirectoryInterface newDirectory, String newName ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void save( RepositoryElementInterface repositoryElement, String versionComment,
      ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void save( RepositoryElementInterface repositoryElementInterface, String s, Calendar calendar,
      ProgressMonitorListener progressMonitorListener, boolean b ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveConditionStepAttribute( ObjectId idTransformation, ObjectId idStep, String code, Condition condition )
    throws KettleException {
    DataNode conditionNode = node.addNode( code );
    conditionNode.setProperty( PROPERTY_XML, condition.getXML() );
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String nameCode,
      String code, DatabaseMeta database ) throws KettleException {
    if ( database != null) {
      if ( database.getObjectId() != null ) {
        DataNodeRef ref = new DataNodeRef( database.getObjectId().getId() );
        node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, ref );
      } else if ( database.getName() != null ) {
        node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr + PROP_CODE_NR_SEPARATOR + NAME_EXT, database.getName() );
      }
    }
  }

  @Override
  public void saveDatabaseMetaStepAttribute( ObjectId idTransformation, ObjectId idStep, String code,
      DatabaseMeta database ) throws KettleException {
    if ( database != null ) {
      if ( database.getObjectId() != null ) {
        DataNodeRef ref = new DataNodeRef( database.getObjectId().getId() );
        node.setProperty( code, ref );
      } else if ( database.getName() != null ) {
        node.setProperty( code + PROP_CODE_NR_SEPARATOR + NAME_EXT, database.getName() );
      }
    }
  }

  @Override
  public void saveJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String code, String value )
    throws KettleException {
    node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String code, boolean value )
    throws KettleException {
    node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, value );
  }

  @Override
  public void saveJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String code, long value )
    throws KettleException {
    node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, value );
  }

  @Override
  public void saveRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, String value )
    throws KettleException {
    node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, value );
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, boolean value )
    throws KettleException {
    node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, value );
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, long value )
    throws KettleException {
    node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, value );
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, double value )
    throws KettleException {
    node.setProperty( code + PROP_CODE_NR_SEPARATOR + nr, value );
  }

  @Override
  public void undeleteObject( RepositoryElementMetaInterface element ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unlockJob( ObjectId idJob ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unlockTransformation( ObjectId idTransformation ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobAndTransformationObjects( ObjectId idDirectory,
      boolean includeDeleted ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IRepositoryService getService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryDirectoryInterface getDefaultSaveDirectory( RepositoryElementInterface arg0 ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canUnlockFileById( ObjectId id ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryObject getObjectInformation( ObjectId objectId, RepositoryObjectType objectType )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public JobMeta loadJob( ObjectId idJob, String versionLabel ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public TransMeta loadTransformation( ObjectId idTransformation, String versionLabel ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getConnectMessage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getJobsUsingDatabase( ObjectId id_database ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getTransformationsUsingDatabase( ObjectId id_database ) throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IRepositoryExporter getExporter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IRepositoryImporter getImporter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IMetaStore getRepositoryMetaStore() {
    return null;
  }

  @Override
  public boolean test() {
    return true;
  }
}
