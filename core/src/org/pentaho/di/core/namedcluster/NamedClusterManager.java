/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.namedcluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.util.PentahoDefaults;

public class NamedClusterManager {

  private static NamedClusterManager instance = new NamedClusterManager();
  
  private Map<IMetaStore, MetaStoreFactory<NamedCluster>> factoryMap = new HashMap<IMetaStore, MetaStoreFactory<NamedCluster>>();
  
  private NamedCluster clusterTemplate;
  
  private NamedClusterManager() {
  }

  public static NamedClusterManager getInstance() {
    return instance;
  }

  private MetaStoreFactory<NamedCluster> getMetaStoreFactory( IMetaStore metastore ) {
    if ( factoryMap.get( metastore ) == null ) {
     factoryMap.put( metastore, new MetaStoreFactory<NamedCluster>( NamedCluster.class, metastore, PentahoDefaults.NAMESPACE ) );
    }
    return factoryMap.get( metastore );
  }
  
  /**
   * This method returns the named cluster template used to configure new NamedClusters.
   * 
   * Note that this method returns a clone (deep) of the template.
   * 
   * @return the NamedCluster template
   */  
  public NamedCluster getClusterTemplate() {
    if ( clusterTemplate == null ) {
      clusterTemplate = new NamedCluster();
      clusterTemplate.setName(  "new cluster" );
      clusterTemplate.setNameNodeHost( "localhost" );
      clusterTemplate.setNameNodePort( 50070 );
      clusterTemplate.setHdfsHost( "localhost" );
      clusterTemplate.setHdfsPort( 50075 );
      clusterTemplate.setHdfsUsername( "user" );
      clusterTemplate.setHdfsPassword( "password" );
      clusterTemplate.setJobTrackerHost( "localhost" );
      clusterTemplate.setJobTrackerPort( 50030 );
      clusterTemplate.setZooKeeperHost( "localhost" );
      clusterTemplate.setZooKeeperPort( 2888 );
      clusterTemplate.setOozieUrl( "http://localhost:8080/oozie" );
    }
    return clusterTemplate.clone();
  }

  /**
   * This method will set the cluster template used when creating new NamedClusters
   * 
   * @param clusterTemplate the NamedCluster template to set
   */  
  public void setClusterTemplate( NamedCluster clusterTemplate ) {
    this.clusterTemplate = clusterTemplate;
  }
  
  /**
   * Saves a named cluster in the provided IMetaStore
   * 
   * @param namedCluster the NamedCluster to save
   * @param metastore the IMetaStore to operate with
   * @throws MetaStoreException
   */
  public void create( NamedCluster namedCluster, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedCluster> factory = getMetaStoreFactory( metastore );
    factory.saveElement( namedCluster );
  }
  
  /**
   * Reads a NamedCluster from the provided IMetaStore
   * 
   * @param clusterName the name of the NamedCluster to load
   * @param metastore the IMetaStore to operate with
   * @return the NamedCluster that was loaded
   * @throws MetaStoreException
   */
  public NamedCluster read( String clusterName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedCluster> factory = getMetaStoreFactory( metastore );
    return factory.loadElement( clusterName );
  }


  /**
   * Updates a NamedCluster in the provided IMetaStore
   * 
   * @param namedCluster the NamedCluster to update
   * @param metastore the IMetaStore to operate with
   * @throws MetaStoreException
   */
  public void update( NamedCluster namedCluster, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedCluster> factory = getMetaStoreFactory( metastore );
    factory.deleteElement( namedCluster.getName() );
    factory.saveElement( namedCluster );
  }  

  /**
   * Deletes a NamedCluster from the provided IMetaStore
   * 
   * @param clusterName the NamedCluster to delete
   * @param metastore the IMetaStore to operate with
   * @throws MetaStoreException
   */
  public void delete( String clusterName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedCluster> factory = getMetaStoreFactory( metastore );
    factory.deleteElement( clusterName );
  }  
  
  /**
   * This method lists the NamedCluster in the given IMetaStore
   * 
   * @param metastore the IMetaStore to operate with
   * @return the list of NamedClusters in the provided IMetaStore
   * @throws MetaStoreException
   */
  public List<NamedCluster> list( IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedCluster> factory = getMetaStoreFactory( metastore );
    return factory.getElements();
  }  
  
  /**
   * This method returns the list of NamedCluster names in the IMetaStore
   * 
   * @param metastore the IMetaStore to operate with
   * @return the list of NamedCluster names (Strings)
   * @throws MetaStoreException
   */
  public List<String> listNames( IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedCluster> factory = getMetaStoreFactory( metastore );
    return factory.getElementNames();
  }   
  
  /**
   * This method checks if the NamedCluster exists in the metastore
   * 
   * @param clusterName the name of the NamedCluster to check
   * @param metastore the IMetaStore to operate with
   * @return true if the NamedCluster exists in the given metastore
   * @throws MetaStoreException
   */
  public boolean contains( String clusterName, IMetaStore metastore ) throws MetaStoreException {
    if ( metastore == null ) {
      return false;
    }
    for ( String name : listNames( metastore ) ) {
      if ( name.equals( clusterName ) ) {
        return true;
      }
    }
    return false;
  }
  
}