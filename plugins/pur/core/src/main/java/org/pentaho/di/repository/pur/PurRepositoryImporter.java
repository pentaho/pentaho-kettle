/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.repository.pur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.RepositoryImporter;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.TransMeta;

public class PurRepositoryImporter extends RepositoryImporter implements java.io.Serializable {
  private static final long serialVersionUID = 2853810493291696227L; /* EESOURCE: UPDATE SERIALVERUID */

  private PurRepository rep;

  private Map<Class<?>, List<?>> sharedObjectsByType = null;

  public PurRepositoryImporter( PurRepository repository ) {
    super( repository, new LogChannel( "Repository import" ) );
    this.rep = repository;
  }

  @Override
  protected <T extends SharedObjectInterface> List<T> getSharedObjects( Class<T> clazz ) {
    List<T> result = new ArrayList<T>();
    List<?> typeList = sharedObjectsByType.get( clazz );
    if ( typeList != null ) {
      for ( Object obj : typeList ) {
        result.add( clazz.cast( obj ) );
      }
    }
    return result;
  }

  private void populateSharedObjectsMap() throws KettleException {
    sharedObjectsByType = new HashMap<Class<?>, List<?>>();
    for ( Entry<RepositoryObjectType, List<? extends SharedObjectInterface>> entry : rep.loadAndCacheSharedObjects()
        .entrySet() ) {
      Class<?> clazz = null;
      switch ( entry.getKey() ) {
        case DATABASE:
          clazz = DatabaseMeta.class;
          break;
        case SLAVE_SERVER:
          clazz = SlaveServer.class;
          break;
        case PARTITION_SCHEMA:
          clazz = PartitionSchema.class;
          break;
        case CLUSTER_SCHEMA:
          clazz = ClusterSchema.class;
          break;
        default:
          break;
      }
      if ( clazz != null ) {
        sharedObjectsByType.put( clazz, new ArrayList<Object>( entry.getValue() ) );
      }
    }
  }

  @Override
  protected void loadSharedObjects() throws KettleException {
    // Noop
  }

  @Override
  protected void replaceSharedObjects( TransMeta transMeta ) throws KettleException {
    populateSharedObjectsMap();
    super.replaceSharedObjects( transMeta );
  }

  @Override
  protected void replaceSharedObjects( JobMeta jobMeta ) throws KettleException {
    populateSharedObjectsMap();
    super.replaceSharedObjects( jobMeta );
  }

  @Override
  protected boolean equals( DatabaseMeta databaseMeta, DatabaseMeta databaseMeta2 ) {
    return rep.getDatabaseMetaTransformer().equals( databaseMeta, databaseMeta2 );
  }

  @Override
  protected boolean equals( SlaveServer slaveServer, SlaveServer slaveServer2 ) {
    return rep.getSlaveTransformer().equals( slaveServer, slaveServer2 );
  }

  @Override
  protected boolean equals( ClusterSchema clusterSchema, ClusterSchema clusterSchema2 ) {
    return rep.getClusterTransformer().equals( clusterSchema, clusterSchema2 );
  }

  @Override
  protected boolean equals( PartitionSchema partitionSchema, PartitionSchema partitionSchema2 ) {
    return rep.getPartitionSchemaTransformer().equals( partitionSchema, partitionSchema2 );
  }

  @Override
  protected void saveTransMeta( TransMeta transMeta ) throws KettleException {
    rep.saveKettleEntity( transMeta, getVersionComment(), null, true, false, false, false, false );
  }

  @Override
  protected void saveJobMeta( JobMeta jobMeta ) throws KettleException {
    rep.saveKettleEntity( jobMeta, getVersionComment(), null, true, false, false, false, false );
  }
}
