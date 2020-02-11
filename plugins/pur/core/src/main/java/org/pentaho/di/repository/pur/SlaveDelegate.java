/*!
 * Copyright 2010 - 2020 Hitachi Vantara.  All rights reserved.
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

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

public class SlaveDelegate extends AbstractDelegate implements ITransformer, SharedObjectAssembler<SlaveServer>,
    java.io.Serializable {

  static final long serialVersionUID = -8084266831877112729L; /* EESOURCE: UPDATE SERIALVERUID */
  static final String NODE_ROOT = "Slave";
  static final String PROP_PASSWORD = "PASSWORD";
  static final String PROP_USERNAME = "USERNAME";
  static final String PROP_PORT = "PORT";
  static final String PROP_HOST_NAME = "HOST_NAME";
  static final String PROP_PROXY_HOST_NAME = "PROXY_HOST_NAME";
  static final String PROP_PROXY_PORT = "PROXY_PORT";
  static final String PROP_WEBAPP_NAME = "WEBAPP_NAME";
  static final String PROP_NON_PROXY_HOSTS = "NON_PROXY_HOSTS";
  static final String PROP_MASTER = "MASTER";
  static final String PROP_USE_HTTPS_PROTOCOL = "USE_HTTPS_PROTOCOL";

  // ~ Instance fields =================================================================================================

  private PurRepository repo;

  // ~ Constructors ====================================================================================================

  public SlaveDelegate( final PurRepository repo ) {
    super();
    this.repo = repo;
  }

  public RepositoryElementInterface dataNodeToElement( DataNode rootNode ) throws KettleException {
    SlaveServer slaveServer = new SlaveServer();
    dataNodeToElement( rootNode, slaveServer );
    return slaveServer;
  }

  public void dataNodeToElement( DataNode rootNode, RepositoryElementInterface element ) throws KettleException {
    SlaveServer slaveServer = (SlaveServer) element;
    slaveServer.setHostname( getString( rootNode, PROP_HOST_NAME ) );
    slaveServer.setPort( getString( rootNode, PROP_PORT ) );
    slaveServer.setUsername( getString( rootNode, PROP_USERNAME ) );
    slaveServer.setPassword( Encr.decryptPasswordOptionallyEncrypted( getString( rootNode, PROP_PASSWORD ) ) );
    slaveServer.setProxyHostname( getString( rootNode, PROP_PROXY_HOST_NAME ) );
    slaveServer.setProxyPort( getString( rootNode, PROP_PROXY_PORT ) );
    slaveServer.setWebAppName( getString( rootNode, PROP_WEBAPP_NAME ) );
    slaveServer.setNonProxyHosts( getString( rootNode, PROP_NON_PROXY_HOSTS ) );
    slaveServer.setMaster( getBoolean( rootNode, PROP_MASTER ) );
    slaveServer.setSslMode( getBoolean( rootNode, PROP_USE_HTTPS_PROTOCOL ) );
  }

  public DataNode elementToDataNode( RepositoryElementInterface element ) throws KettleException {
    SlaveServer slaveServer = (SlaveServer) element;
    DataNode rootNode = new DataNode( NODE_ROOT );

    /*
     * // Check for naming collision ObjectId slaveId = repo.getSlaveID(slaveServer.getName()); if (slaveId != null &&
     * slaveServer.getObjectId()!=null && !slaveServer.getObjectId().equals(slaveId)) { // We have a naming collision,
     * abort the save throw new KettleException("Failed to save object to repository. Object [" + slaveServer.getName()
     * + "] already exists."); }
     */

    // Create or version a new slave node
    //
    rootNode.setProperty( PROP_HOST_NAME, slaveServer.getHostname() );
    rootNode.setProperty( PROP_PORT, slaveServer.getPort() );
    rootNode.setProperty( PROP_WEBAPP_NAME, slaveServer.getWebAppName() );
    rootNode.setProperty( PROP_USERNAME, slaveServer.getUsername() );
    rootNode.setProperty( PROP_PASSWORD, Encr.encryptPasswordIfNotUsingVariables( slaveServer.getPassword() ) );
    rootNode.setProperty( PROP_PROXY_HOST_NAME, slaveServer.getProxyHostname() );
    rootNode.setProperty( PROP_PROXY_PORT, slaveServer.getProxyPort() );
    rootNode.setProperty( PROP_NON_PROXY_HOSTS, slaveServer.getNonProxyHosts() );
    rootNode.setProperty( PROP_MASTER, slaveServer.isMaster() );
    rootNode.setProperty( PROP_USE_HTTPS_PROTOCOL, slaveServer.isSslMode() );
    return rootNode;
  }

  protected Repository getRepository() {
    return repo;
  }

  public SlaveServer assemble( RepositoryFile file, NodeRepositoryFileData data, VersionSummary version )
    throws KettleException {
    SlaveServer slaveServer = (SlaveServer) dataNodeToElement( data.getNode() );
    slaveServer.setName( file.getTitle() );
    slaveServer.setObjectId( new StringObjectId( file.getId().toString() ) );
    slaveServer.setObjectRevision( repo.createObjectRevision( version ) );
    slaveServer.clearChanged();
    return slaveServer;
  }
}
