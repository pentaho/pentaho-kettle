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

package org.pentaho.di.core.osgi.api;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

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

  String decodePassword( String password );

  String encodePassword( String password );

  default List<NamedClusterSiteFile> getSiteFiles() {
    return Collections.emptyList();
  }

  default void setSiteFiles( List<NamedClusterSiteFile> siteFiles ) {
    //default here just for compile purposes
  }

  /*
  @deprecated As of version 9.3.  Use the {@link #addSiteFile( NamedClusterSiteFile namedClusterSiteFile ) method
   */
  @Deprecated
  default void addSiteFile( String fileName, String content ) {
    //default here just for compile purposes
  }

  default void addSiteFile( NamedClusterSiteFile namedClusterSiteFile ) {
    //default here just for compile purposes
  }

  default InputStream getSiteFileInputStream( String siteFileName ) {
    return null;
  }

}
