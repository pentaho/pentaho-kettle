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

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Created by tkafalas on 7/6/2017.
 */
public interface NamedClusterOsgi extends VariableSpace {

  public static final String NAMED_CLUSTERS_FS_OPTION = "namedClustersFSOption";

  public static final String NAMED_CLUSTER_FS_OPTION = "namedClusterFSOption";

  public static final String NAMED_CLUSTER_XML_TAG = "namedClusterTag";

  public static final String KNOX_GATEWAY_ROOT = "KnoxGatewayRoot";

  String getName();

  void setName( String name );

  String getShimIdentifier();

  void setShimIdentifier( String shimIdentifier );

  String getStorageScheme();

  void setStorageScheme( String storageScheme );

  //void replaceMeta( NamedCluster nc );

  String getHdfsHost();

  void setHdfsHost( String hdfsHost );

  String getHdfsPort();

  void setHdfsPort( String hdfsPort );

  String getHdfsUsername();

  void setHdfsUsername( String hdfsUsername );

  String getHdfsPassword();

  void setHdfsPassword( String hdfsPassword );

  String getJobTrackerHost();

  void setJobTrackerHost( String jobTrackerHost );

  String getJobTrackerPort();

  void setJobTrackerPort( String jobTrackerPort );

  String getZooKeeperHost();

  void setZooKeeperHost( String zooKeeperHost );

  String getZooKeeperPort();

  void setZooKeeperPort( String zooKeeperPort );

  String getOozieUrl();

  void setOozieUrl( String oozieUrl );

  long getLastModifiedDate();

  void setLastModifiedDate( long lastModifiedDate );

  boolean isMapr();

  void setMapr( boolean mapr );

  String getGatewayUrl();

  void setGatewayUrl( String gatewayUrl );

  String getGatewayUsername();

  void setGatewayUsername( String gatewayUsername );

  String getGatewayPassword();

  void setGatewayPassword( String gatewayPassword );

  String getKafkaBootstrapServers();

  void setKafkaBootstrapServers( String kafkaBootstrapServers );

  //NamedCluster clone();

  String toXmlForEmbed( String rootTag );

  //NamedCluster fromXmlForEmbed( Node node );

  /**
   * This is the NamedClusterOsgi equivalent of the line commented above.  The NamedCluster interface is used in OSGI
   * and should be phased out over time as this interface is adopted instead.  We had to create a unique method name
   * here for the time being since the signature is identical.
   */
  NamedClusterOsgi nonOsgiFromXmlForEmbed( Node node );

  /**
   * This method performs the root URL substitution with the URL of the specified NamedCluster
   *
   * @param incomingURL   the URL whose root will be replaced
   * @param metastore     the metastore
   * @param variableSpace the variable space
   * @return the generated URL or the incoming URL if an error occurs
   */
  String processURLsubstitution( String incomingURL, IMetaStore metastore, VariableSpace variableSpace );

  void setUseGateway( boolean selection );

  boolean isUseGateway();

}
