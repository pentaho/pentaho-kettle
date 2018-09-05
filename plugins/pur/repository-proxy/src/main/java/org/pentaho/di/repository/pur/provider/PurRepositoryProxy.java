/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur.provider;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

/**
 * Provides a bridge between OSGI and non OSGI worlds by delegating the calls to PurRepository, to
 * the repository instance loaded from the plugin class loader.
 */
public class PurRepositoryProxy implements Repository {

  private static final Logger logger = LoggerFactory.getLogger( PurRepositoryProxy.class );
  private static final String PUR_PLUGIN_ID = "PentahoEnterpriseRepository";
  private ClassLoader purPluginClassLoader;
  private Repository delegate;
  private String locationUrl;

  public PurRepositoryProxy( PluginRegistry registry ) throws KettlePluginException {
    PluginInterface purPlugin = registry.findPluginWithId( RepositoryPluginType.class, PUR_PLUGIN_ID );
    purPluginClassLoader = registry.getClassLoader( purPlugin );
  }

  protected Repository getDelegate() {
    if ( this.delegate != null ) {
      return this.delegate;
    }
    Repository repository = null;
    try {
      repository = (Repository) purPluginClassLoader.loadClass( "org.pentaho.di.repository.pur.PurRepository" ).newInstance();
    } catch ( Exception e ) {
      logger.error( "Unable to load delegate class for plugin id \"{}\". PUR plugin is most likely not installed or "
          + "changed id.", PUR_PLUGIN_ID );
    }
    return this.delegate = repository;
  }

  public void setLocationUrl( String locationUrl ) {
    RepositoryMeta repositoryMeta = this.createPurRepositoryMetaRepositoryMeta( locationUrl );
    this.getDelegate().init( repositoryMeta );
    this.locationUrl = locationUrl;
  }

  public String getLocationUrl() {
    return this.locationUrl;
  }


  /**
   * Uses reflection to create a PurRepositoryMeta with its locationUrl set to the passed url argument.
   *
   * @param url The location url to set on the PurRepositoryMeta
   * @return a new PurRepositoryMeta with set location url
   */
  protected RepositoryMeta createPurRepositoryMetaRepositoryMeta( String url ) {
    RepositoryMeta purRepositoryMeta = null;
    try {
      Class<?> purRepositoryLocationClass = purPluginClassLoader.loadClass( "org.pentaho.di.repository.pur"
          + ".PurRepositoryLocation" );

      Constructor<?> purRepositoryLocationConstructor = purRepositoryLocationClass.getConstructor( String.class );
      Object purRepositoryLocation = purRepositoryLocationConstructor.newInstance( url );

      Class<?> purRepositoryMetaClass = purPluginClassLoader.loadClass( "org.pentaho.di.repository.pur.PurRepositoryMeta" );
      purRepositoryMeta = (RepositoryMeta) purRepositoryMetaClass.newInstance();

      Method setRepositoryLocationMethod = purRepositoryMetaClass.getMethod( "setRepositoryLocation", purRepositoryLocationClass );
      setRepositoryLocationMethod.invoke( purRepositoryMeta, purRepositoryLocation );
    } catch ( Exception e ) {
      logger.error( "Unable to instantiate repository meta!" );
    }
    return purRepositoryMeta;
  }

  //region delegated methods: all methods inside this region were generated to delegate Repository methods. As this
  // is auto-generated code, coverage is ignored.
  @Override
  public String getName() {
    return getDelegate().getName(); // $COVERAGE-IGNORE$
  }

  @Override
  public String getVersion() {
    return getDelegate().getVersion(); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryMeta getRepositoryMeta() {
    return getDelegate().getRepositoryMeta(); // $COVERAGE-IGNORE$
  }

  @Override
  public IUser getUserInfo() {
    return getDelegate().getUserInfo(); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositorySecurityProvider getSecurityProvider() {
    return getDelegate().getSecurityProvider(); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositorySecurityManager getSecurityManager() {
    return getDelegate().getSecurityManager(); // $COVERAGE-IGNORE$
  }

  @Override
  public LogChannelInterface getLog() {
    return getDelegate().getLog(); // $COVERAGE-IGNORE$
  }

  @Override
  public void connect( String s, String s1 ) throws KettleException, KettleSecurityException {
    getDelegate().connect( s, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void disconnect() {
    getDelegate().disconnect(); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean isConnected() {
    return getDelegate().isConnected(); // $COVERAGE-IGNORE$
  }

  @Override
  public void init( RepositoryMeta repositoryMeta ) {
    getDelegate().init( repositoryMeta ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean exists( String s, RepositoryDirectoryInterface repositoryDirectoryInterface, RepositoryObjectType repositoryObjectType ) throws KettleException {
    return getDelegate().exists( s, repositoryDirectoryInterface, repositoryObjectType ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId getTransformationID( String s, RepositoryDirectoryInterface repositoryDirectoryInterface ) throws KettleException {
    return getDelegate().getTransformationID( s, repositoryDirectoryInterface ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId getJobId( String s, RepositoryDirectoryInterface repositoryDirectoryInterface ) throws KettleException {
    return getDelegate().getJobId( s, repositoryDirectoryInterface ); // $COVERAGE-IGNORE$
  }

  @Override
  public void save( RepositoryElementInterface repositoryElementInterface, String s, ProgressMonitorListener progressMonitorListener ) throws KettleException {
    getDelegate().save( repositoryElementInterface, s, progressMonitorListener ); // $COVERAGE-IGNORE$
  }

  @Override
  public void save( RepositoryElementInterface repositoryElementInterface, String s, ProgressMonitorListener progressMonitorListener, boolean b ) throws KettleException {
    getDelegate().save( repositoryElementInterface, s, progressMonitorListener, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public void save( RepositoryElementInterface repositoryElementInterface, String s, Calendar calendar, ProgressMonitorListener progressMonitorListener, boolean b ) throws KettleException {
    getDelegate().save( repositoryElementInterface, s, calendar, progressMonitorListener, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryDirectoryInterface getDefaultSaveDirectory( RepositoryElementInterface repositoryElementInterface ) throws KettleException {
    return getDelegate().getDefaultSaveDirectory( repositoryElementInterface ); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    return getDelegate().getUserHomeDirectory(); // $COVERAGE-IGNORE$
  }

  @Override
  public void clearSharedObjectCache() {
    getDelegate().clearSharedObjectCache(); // $COVERAGE-IGNORE$
  }

  @Override
  public TransMeta loadTransformation( String s, RepositoryDirectoryInterface repositoryDirectoryInterface, ProgressMonitorListener progressMonitorListener, boolean b, String s1 ) throws KettleException {
    return getDelegate().loadTransformation( s, repositoryDirectoryInterface, progressMonitorListener, b, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public TransMeta loadTransformation( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().loadTransformation( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public SharedObjects readTransSharedObjects( TransMeta transMeta ) throws KettleException {
    return getDelegate().readTransSharedObjects( transMeta ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId renameTransformation( ObjectId objectId, RepositoryDirectoryInterface repositoryDirectoryInterface, String s ) throws KettleException {
    return getDelegate().renameTransformation( objectId, repositoryDirectoryInterface, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId renameTransformation( ObjectId objectId, String s, RepositoryDirectoryInterface repositoryDirectoryInterface, String s1 ) throws KettleException {
    return getDelegate().renameTransformation( objectId, s, repositoryDirectoryInterface, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void deleteTransformation( ObjectId objectId ) throws KettleException {
    getDelegate().deleteTransformation( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public JobMeta loadJob( String s, RepositoryDirectoryInterface repositoryDirectoryInterface, ProgressMonitorListener progressMonitorListener, String s1 ) throws KettleException {
    return getDelegate().loadJob( s, repositoryDirectoryInterface, progressMonitorListener, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public JobMeta loadJob( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().loadJob( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public SharedObjects readJobMetaSharedObjects( JobMeta jobMeta ) throws KettleException {
    return getDelegate().readJobMetaSharedObjects( jobMeta ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId renameJob( ObjectId objectId, String s, RepositoryDirectoryInterface repositoryDirectoryInterface, String s1 ) throws KettleException {
    return getDelegate().renameJob( objectId, s, repositoryDirectoryInterface, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId renameJob( ObjectId objectId, RepositoryDirectoryInterface repositoryDirectoryInterface, String s ) throws KettleException {
    return getDelegate().renameJob( objectId, repositoryDirectoryInterface, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void deleteJob( ObjectId objectId ) throws KettleException {
    getDelegate().deleteJob( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public DatabaseMeta loadDatabaseMeta( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().loadDatabaseMeta( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void deleteDatabaseMeta( String s ) throws KettleException {
    getDelegate().deleteDatabaseMeta( s ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId[] getDatabaseIDs( boolean b ) throws KettleException {
    return getDelegate().getDatabaseIDs( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getDatabaseNames( boolean b ) throws KettleException {
    return getDelegate().getDatabaseNames( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public List<DatabaseMeta> readDatabases() throws KettleException {
    return getDelegate().readDatabases(); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId getDatabaseID( String s ) throws KettleException {
    return getDelegate().getDatabaseID( s ); // $COVERAGE-IGNORE$
  }

  @Override
  public ClusterSchema loadClusterSchema( ObjectId objectId, List<SlaveServer> list, String s ) throws KettleException {
    return getDelegate().loadClusterSchema( objectId, list, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId[] getClusterIDs( boolean b ) throws KettleException {
    return getDelegate().getClusterIDs( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getClusterNames( boolean b ) throws KettleException {
    return getDelegate().getClusterNames( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId getClusterID( String s ) throws KettleException {
    return getDelegate().getClusterID( s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void deleteClusterSchema( ObjectId objectId ) throws KettleException {
    getDelegate().deleteClusterSchema( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public SlaveServer loadSlaveServer( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().loadSlaveServer( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId[] getSlaveIDs( boolean b ) throws KettleException {
    return getDelegate().getSlaveIDs( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getSlaveNames( boolean b ) throws KettleException {
    return getDelegate().getSlaveNames( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public List<SlaveServer> getSlaveServers() throws KettleException {
    return getDelegate().getSlaveServers(); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId getSlaveID( String s ) throws KettleException {
    return getDelegate().getSlaveID( s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void deleteSlave( ObjectId objectId ) throws KettleException {
    getDelegate().deleteSlave( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public PartitionSchema loadPartitionSchema( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().loadPartitionSchema( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId[] getPartitionSchemaIDs( boolean b ) throws KettleException {
    return getDelegate().getPartitionSchemaIDs( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getPartitionSchemaNames( boolean b ) throws KettleException {
    return getDelegate().getPartitionSchemaNames( b ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId getPartitionSchemaID( String s ) throws KettleException {
    return getDelegate().getPartitionSchemaID( s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void deletePartitionSchema( ObjectId objectId ) throws KettleException {
    getDelegate().deletePartitionSchema( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
    return getDelegate().loadRepositoryDirectoryTree(); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( String s ) throws KettleException {
    return getDelegate().findDirectory( s ); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( ObjectId objectId ) throws KettleException {
    return getDelegate().findDirectory( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectoryInterface ) throws KettleException {
    getDelegate().saveRepositoryDirectory( repositoryDirectoryInterface ); // $COVERAGE-IGNORE$
  }

  @Override
  public void deleteRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectoryInterface ) throws KettleException {
    getDelegate().deleteRepositoryDirectory( repositoryDirectoryInterface ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId renameRepositoryDirectory( ObjectId objectId, RepositoryDirectoryInterface repositoryDirectoryInterface, String s ) throws KettleException {
    return getDelegate().renameRepositoryDirectory( objectId, repositoryDirectoryInterface, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryDirectoryInterface createRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectoryInterface, String s ) throws KettleException {
    return getDelegate().createRepositoryDirectory( repositoryDirectoryInterface, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getTransformationNames( ObjectId objectId, boolean b ) throws KettleException {
    return getDelegate().getTransformationNames( objectId, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobObjects( ObjectId objectId, boolean b ) throws KettleException {
    return getDelegate().getJobObjects( objectId, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public List<RepositoryElementMetaInterface> getTransformationObjects( ObjectId objectId, boolean b ) throws KettleException {
    return getDelegate().getTransformationObjects( objectId, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobAndTransformationObjects( ObjectId objectId, boolean b ) throws KettleException {
    return getDelegate().getJobAndTransformationObjects( objectId, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getJobNames( ObjectId objectId, boolean b ) throws KettleException {
    return getDelegate().getJobNames( objectId, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getDirectoryNames( ObjectId objectId ) throws KettleException {
    return getDelegate().getDirectoryNames( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public ObjectId insertLogEntry( String s ) throws KettleException {
    return getDelegate().insertLogEntry( s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void insertStepDatabase( ObjectId objectId, ObjectId objectId1, ObjectId objectId2 ) throws KettleException {
    getDelegate().insertStepDatabase( objectId, objectId1, objectId2 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void insertJobEntryDatabase( ObjectId objectId, ObjectId objectId1, ObjectId objectId2 ) throws KettleException {
    getDelegate().insertJobEntryDatabase( objectId, objectId1, objectId2 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveConditionStepAttribute( ObjectId objectId, ObjectId objectId1, String s, Condition condition ) throws KettleException {
    getDelegate().saveConditionStepAttribute( objectId, objectId1, s, condition ); // $COVERAGE-IGNORE$
  }

  @Override
  public Condition loadConditionFromStepAttribute( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().loadConditionFromStepAttribute( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId objectId, int i, String s, boolean b ) throws KettleException {
    return getDelegate().getStepAttributeBoolean( objectId, i, s, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId objectId, int i, String s ) throws KettleException {
    return getDelegate().getStepAttributeBoolean( objectId, i, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().getStepAttributeBoolean( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public long getStepAttributeInteger( ObjectId objectId, int i, String s ) throws KettleException {
    return getDelegate().getStepAttributeInteger( objectId, i, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public long getStepAttributeInteger( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().getStepAttributeInteger( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public String getStepAttributeString( ObjectId objectId, int i, String s ) throws KettleException {
    return getDelegate().getStepAttributeString( objectId, i, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public String getStepAttributeString( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().getStepAttributeString( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, String s1 ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, i, s, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, String s, String s1 ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, s, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, boolean b ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, i, s, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, String s, boolean b ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, s, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, long l ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, i, s, l ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, String s, long l ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, s, l ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, double v ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, i, s, v ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveStepAttribute( ObjectId objectId, ObjectId objectId1, String s, double v ) throws KettleException {
    getDelegate().saveStepAttribute( objectId, objectId1, s, v ); // $COVERAGE-IGNORE$
  }

  @Override
  public int countNrStepAttributes( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().countNrStepAttributes( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public int countNrJobEntryAttributes( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().countNrJobEntryAttributes( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().getJobEntryAttributeBoolean( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId objectId, int i, String s ) throws KettleException {
    return getDelegate().getJobEntryAttributeBoolean( objectId, i, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId objectId, String s, boolean b ) throws KettleException {
    return getDelegate().getJobEntryAttributeBoolean( objectId, s, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().getJobEntryAttributeInteger( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId objectId, int i, String s ) throws KettleException {
    return getDelegate().getJobEntryAttributeInteger( objectId, i, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public String getJobEntryAttributeString( ObjectId objectId, String s ) throws KettleException {
    return getDelegate().getJobEntryAttributeString( objectId, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public String getJobEntryAttributeString( ObjectId objectId, int i, String s ) throws KettleException {
    return getDelegate().getJobEntryAttributeString( objectId, i, s ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveJobEntryAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, String s1 ) throws KettleException {
    getDelegate().saveJobEntryAttribute( objectId, objectId1, i, s, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveJobEntryAttribute( ObjectId objectId, ObjectId objectId1, String s, String s1 ) throws KettleException {
    getDelegate().saveJobEntryAttribute( objectId, objectId1, s, s1 ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveJobEntryAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, boolean b ) throws KettleException {
    getDelegate().saveJobEntryAttribute( objectId, objectId1, i, s, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveJobEntryAttribute( ObjectId objectId, ObjectId objectId1, String s, boolean b ) throws KettleException {
    getDelegate().saveJobEntryAttribute( objectId, objectId1, s, b ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveJobEntryAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, long l ) throws KettleException {
    getDelegate().saveJobEntryAttribute( objectId, objectId1, i, s, l ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveJobEntryAttribute( ObjectId objectId, ObjectId objectId1, String s, long l ) throws KettleException {
    getDelegate().saveJobEntryAttribute( objectId, objectId1, s, l ); // $COVERAGE-IGNORE$
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId objectId, String s, List<DatabaseMeta> list ) throws KettleException {
    return getDelegate().loadDatabaseMetaFromStepAttribute( objectId, s, list ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveDatabaseMetaStepAttribute( ObjectId objectId, ObjectId objectId1, String s, DatabaseMeta databaseMeta ) throws KettleException {
    getDelegate().saveDatabaseMetaStepAttribute( objectId, objectId1, s, databaseMeta ); // $COVERAGE-IGNORE$
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId objectId, String s, String s1, List<DatabaseMeta> list ) throws KettleException {
    return getDelegate().loadDatabaseMetaFromJobEntryAttribute( objectId, s, s1, list ); // $COVERAGE-IGNORE$
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId objectId, String s, int i, String s1, List<DatabaseMeta> list ) throws KettleException {
    return getDelegate().loadDatabaseMetaFromJobEntryAttribute( objectId, s, i, s1, list ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId objectId, ObjectId objectId1, String s, String s1, DatabaseMeta databaseMeta ) throws KettleException {
    getDelegate().saveDatabaseMetaJobEntryAttribute( objectId, objectId1, s, s1, databaseMeta ); // $COVERAGE-IGNORE$
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId objectId, ObjectId objectId1, int i, String s, String s1, DatabaseMeta databaseMeta ) throws KettleException {
    getDelegate().saveDatabaseMetaJobEntryAttribute( objectId, objectId1, i, s, s1, databaseMeta ); // $COVERAGE-IGNORE$
  }

  @Override
  public void undeleteObject( RepositoryElementMetaInterface repositoryElementMetaInterface ) throws KettleException {
    getDelegate().undeleteObject( repositoryElementMetaInterface ); // $COVERAGE-IGNORE$
  }

  @Override
  public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    return getDelegate().getServiceInterfaces(); // $COVERAGE-IGNORE$
  }

  @Override
  public IRepositoryService getService( Class<? extends IRepositoryService> aClass ) throws KettleException {
    return getDelegate().getService( aClass ); // $COVERAGE-IGNORE$
  }

  @Override
  public boolean hasService( Class<? extends IRepositoryService> aClass ) throws KettleException {
    return getDelegate().hasService( aClass ); // $COVERAGE-IGNORE$
  }

  @Override
  public RepositoryObject getObjectInformation( ObjectId objectId, RepositoryObjectType repositoryObjectType ) throws KettleException {
    return getDelegate().getObjectInformation( objectId, repositoryObjectType ); // $COVERAGE-IGNORE$
  }

  @Override
  public String getConnectMessage() {
    return getDelegate().getConnectMessage(); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getJobsUsingDatabase( ObjectId objectId ) throws KettleException {
    return getDelegate().getJobsUsingDatabase( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public String[] getTransformationsUsingDatabase( ObjectId objectId ) throws KettleException {
    return getDelegate().getTransformationsUsingDatabase( objectId ); // $COVERAGE-IGNORE$
  }

  @Override
  public IRepositoryImporter getImporter() {
    return getDelegate().getImporter(); // $COVERAGE-IGNORE$
  }

  @Override
  public IRepositoryExporter getExporter() throws KettleException {
    return getDelegate().getExporter(); // $COVERAGE-IGNORE$
  }

  @Override
  public IMetaStore getMetaStore() {
    return getDelegate().getMetaStore(); // $COVERAGE-IGNORE$
  }

  @Override
  public IUnifiedRepository getUnderlyingRepository() {
    return getDelegate().getUnderlyingRepository(); // $COVERAGE-IGNORE$
  }
  //endregion: T

}
