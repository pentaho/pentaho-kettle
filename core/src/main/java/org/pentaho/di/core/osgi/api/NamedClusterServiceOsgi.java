/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.osgi.api;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.List;
import java.util.Map;

/**
 * Created by tkafalas on 7/6/2017.
 */
public interface NamedClusterServiceOsgi {

  /**
   * This method returns the named cluster template used to configure new NamedClusters.
   * <p/>
   * Note that this method returns a clone (deep) of the template.
   *
   * @return the NamedCluster template
   */
  NamedClusterOsgi getClusterTemplate();

  /**
   * This method will set the cluster template used when creating new NamedClusters
   *
   * @param clusterTemplate the NamedCluster template to set
   */
  void setClusterTemplate( NamedClusterOsgi clusterTemplate );

  /**
   * Saves a named cluster in the provided IMetaStore
   *
   * @param namedCluster the NamedCluster to save
   * @param metastore    the IMetaStore to operate with
   * @throws MetaStoreException
   */
  void create( NamedClusterOsgi namedCluster, IMetaStore metastore ) throws MetaStoreException;

  /**
   * Reads a NamedCluster from the provided IMetaStore
   *
   * @param clusterName the name of the NamedCluster to load
   * @param metastore   the IMetaStore to operate with
   * @return the NamedCluster that was loaded
   * @throws MetaStoreException
   */
  NamedClusterOsgi read( String clusterName, IMetaStore metastore ) throws MetaStoreException;

  /**
   * Updates a NamedCluster in the provided IMetaStore
   *
   * @param namedCluster the NamedCluster to update
   * @param metastore    the IMetaStore to operate with
   * @throws MetaStoreException
   */
  void update( NamedClusterOsgi namedCluster, IMetaStore metastore ) throws MetaStoreException;

  /**
   * Deletes a NamedCluster from the provided IMetaStore
   *
   * @param clusterName the NamedCluster to delete
   * @param metastore   the IMetaStore to operate with
   * @throws MetaStoreException
   */
  void delete( String clusterName, IMetaStore metastore ) throws MetaStoreException;

  /**
   * This method lists the NamedCluster in the given IMetaStore
   *
   * @param metastore the IMetaStore to operate with
   * @return the list of NamedClusters in the provided IMetaStore
   * @throws MetaStoreException
   */
  List<NamedClusterOsgi> list( IMetaStore metastore ) throws MetaStoreException;

  /**
   * This method returns the list of NamedCluster names in the IMetaStore
   *
   * @param metastore the IMetaStore to operate with
   * @return the list of NamedCluster names (Strings)
   * @throws MetaStoreException
   */
  List<String> listNames( IMetaStore metastore ) throws MetaStoreException;

  /**
   * This method checks if the NamedCluster exists in the metastore
   *
   * @param clusterName the name of the NamedCluster to check
   * @param metastore   the IMetaStore to operate with
   * @return true if the NamedCluster exists in the given metastore
   * @throws MetaStoreException
   */
  boolean contains( String clusterName, IMetaStore metastore ) throws MetaStoreException;

  NamedClusterOsgi getNamedClusterByName( String namedCluster, IMetaStore metastore );

  /**
   * This method load the properties for named cluster from /etc/config folder
   *
   * @return map with properties for named cluster
   */
  Map<String, Object> getProperties();

  /**
   * If the metastore object temporary and should not be kept active indefinitely, this method will release all
   * resources associated with the metastore.
   * @param metastore the IMetaStore being disposed.
   */
  void close( IMetaStore metastore );
}

