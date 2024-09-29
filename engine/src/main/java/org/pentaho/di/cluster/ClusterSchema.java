/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.www.SlaveServerDetection;
import org.w3c.dom.Node;

/**
 * A cluster schema combines a list of slave servers so that they can be set altogether. It (can) also contain a number
 * of performance tuning options regarding this cluster. For example options regarding communications with the master
 * node of the nodes themselves come to mind.
 *
 * @author Matt
 * @since 17-nov-2006
 */
public class ClusterSchema extends ChangedFlag implements Cloneable, SharedObjectInterface, VariableSpace,
  RepositoryElementInterface, XMLInterface {
  private static Class<?> PKG = ClusterSchema.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "clusterschema";

  public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.CLUSTER_SCHEMA;

  /** the name of the cluster schema */
  private String name;

  /** The list of slave servers we can address */
  private List<SlaveServer> slaveServers;

  /** The data socket port where we start numbering. The upper limit is the number of remote socket connections. */
  private String basePort;

  private boolean shared;

  /** Size of the buffer for the created socket reader/writers */
  private String socketsBufferSize;

  /** Flush outputstreams every X rows */
  private String socketsFlushInterval;

  /** flag to compress data over the sockets or not */
  private boolean socketsCompressed;

  /**
   * Flag to indicate that this cluster schema is dynamic.<br>
   * This means that the slave server configuration is taken from one of the defined master servers.<br>
   */
  private boolean dynamic;

  private VariableSpace variables = new Variables();

  private ObjectId id;

  private ObjectRevision objectRevision;

  private Date changedDate;

  public ClusterSchema() {
    id = null;
    slaveServers = new ArrayList<SlaveServer>();
    socketsBufferSize = "2000";
    socketsFlushInterval = "5000";
    socketsCompressed = true;
    basePort = "40000";
    dynamic = false;
    this.changedDate = new Date();
  }

  /**
   * @param name
   * @param slaveServers
   */
  public ClusterSchema( String name, List<SlaveServer> slaveServers ) {
    this.name = name;
    this.slaveServers = slaveServers;
    this.changedDate = new Date();
  }

  public ClusterSchema clone() {
    ClusterSchema clusterSchema = new ClusterSchema();
    clusterSchema.replaceMeta( this );
    return clusterSchema;
  }

  public void replaceMeta( ClusterSchema clusterSchema ) {
    this.name = clusterSchema.name;
    this.basePort = clusterSchema.basePort;
    this.socketsBufferSize = clusterSchema.socketsBufferSize;
    this.socketsCompressed = clusterSchema.socketsCompressed;
    this.socketsFlushInterval = clusterSchema.socketsFlushInterval;
    this.dynamic = clusterSchema.dynamic;

    this.slaveServers.clear();
    this.slaveServers.addAll( clusterSchema.slaveServers ); // no clone() of the slave server please!

    this.shared = clusterSchema.shared;
    this.id = clusterSchema.id;
    this.setChanged( true );
  }

  public String toString() {
    return name;
  }

  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    return name.equals( ( (ClusterSchema) obj ).name );
  }

  public int hashCode() {
    return name.hashCode();
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 500 );

    xml.append( "      " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );

    xml.append( "        " ).append( XMLHandler.addTagValue( "name", name ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "base_port", basePort ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "sockets_buffer_size", socketsBufferSize ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "sockets_flush_interval", socketsFlushInterval ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "sockets_compressed", socketsCompressed ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "dynamic", dynamic ) );

    xml.append( "        " ).append( XMLHandler.openTag( "slaveservers" ) ).append( Const.CR );
    for ( int i = 0; i < slaveServers.size(); i++ ) {
      SlaveServer slaveServer = slaveServers.get( i );
      xml.append( "          " ).append( XMLHandler.addTagValue( "name", slaveServer.getName() ) );
    }
    xml.append( "        " ).append( XMLHandler.closeTag( "slaveservers" ) ).append( Const.CR );
    xml.append( "      " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );
    return xml.toString();
  }

  public ClusterSchema( Node clusterSchemaNode, List<SlaveServer> referenceSlaveServers ) {
    this();

    name = XMLHandler.getTagValue( clusterSchemaNode, "name" );
    basePort = XMLHandler.getTagValue( clusterSchemaNode, "base_port" );
    socketsBufferSize = XMLHandler.getTagValue( clusterSchemaNode, "sockets_buffer_size" );
    socketsFlushInterval = XMLHandler.getTagValue( clusterSchemaNode, "sockets_flush_interval" );
    socketsCompressed = "Y".equalsIgnoreCase( XMLHandler.getTagValue( clusterSchemaNode, "sockets_compressed" ) );
    dynamic = "Y".equalsIgnoreCase( XMLHandler.getTagValue( clusterSchemaNode, "dynamic" ) );

    Node slavesNode = XMLHandler.getSubNode( clusterSchemaNode, "slaveservers" );
    int nrSlaves = XMLHandler.countNodes( slavesNode, "name" );
    for ( int i = 0; i < nrSlaves; i++ ) {
      Node serverNode = XMLHandler.getSubNodeByNr( slavesNode, "name", i );
      String serverName = XMLHandler.getNodeValue( serverNode );
      SlaveServer slaveServer = SlaveServer.findSlaveServer( referenceSlaveServers, serverName );
      if ( slaveServer != null ) {
        slaveServers.add( slaveServer );
      }
    }
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the internal (static) list of slave servers
   */
  public List<SlaveServer> getSlaveServers() {
    return slaveServers;
  }

  /**
   * @param slaveServers
   *          the slaveServers to set
   */
  public void setSlaveServers( List<SlaveServer> slaveServers ) {
    this.slaveServers = slaveServers;
  }

  /**
   * @return The slave server strings from this cluster schema
   */
  public String[] getSlaveServerStrings() {
    String[] strings = new String[slaveServers.size()];
    for ( int i = 0; i < strings.length; i++ ) {
      strings[i] = ( slaveServers.get( i ) ).toString();
    }
    return strings;
  }

  /**
   * @return the shared
   */
  public boolean isShared() {
    return shared;
  }

  /**
   * @param shared
   *          the shared to set
   */
  public void setShared( boolean shared ) {
    this.shared = shared;
  }

  /**
   * @return the basePort
   */
  public String getBasePort() {
    return basePort;
  }

  /**
   * @param basePort
   *          the basePort to set
   */
  public void setBasePort( String basePort ) {
    this.basePort = basePort;
  }

  public SlaveServer findMaster() throws KettleException {
    for ( int i = 0; i < slaveServers.size(); i++ ) {
      SlaveServer slaveServer = slaveServers.get( i );
      if ( slaveServer.isMaster() ) {
        return slaveServer;
      }
    }
    if ( slaveServers.size() > 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "ClusterSchema.NoMasterServerDefined", name ) );
    }
    throw new KettleException( BaseMessages.getString( PKG, "ClusterSchema.NoSlaveServerDefined", name ) );
  }

  /**
   * @return The number of slave servers, excluding the master server
   */
  public int findNrSlaves() {
    int nr = 0;
    for ( int i = 0; i < slaveServers.size(); i++ ) {
      SlaveServer slaveServer = slaveServers.get( i );
      if ( !slaveServer.isMaster() ) {
        nr++;
      }
    }
    return nr;
  }

  /**
   * @return the socketFlushInterval
   */
  public String getSocketsFlushInterval() {
    return socketsFlushInterval;
  }

  /**
   * @param socketFlushInterval
   *          the socketFlushInterval to set
   */
  public void setSocketsFlushInterval( String socketFlushInterval ) {
    this.socketsFlushInterval = socketFlushInterval;
  }

  /**
   * @return the socketsBufferSize
   */
  public String getSocketsBufferSize() {
    return socketsBufferSize;
  }

  /**
   * @param socketsBufferSize
   *          the socketsBufferSize to set
   */
  public void setSocketsBufferSize( String socketsBufferSize ) {
    this.socketsBufferSize = socketsBufferSize;
  }

  /**
   * @return the socketsCompressed
   */
  public boolean isSocketsCompressed() {
    return socketsCompressed;
  }

  /**
   * @param socketsCompressed
   *          the socketsCompressed to set
   */
  public void setSocketsCompressed( boolean socketsCompressed ) {
    this.socketsCompressed = socketsCompressed;
  }

  public SlaveServer findSlaveServer( String slaveServerName ) {
    for ( int i = 0; i < slaveServers.size(); i++ ) {
      SlaveServer slaveServer = slaveServers.get( i );
      if ( slaveServer.getName().equalsIgnoreCase( slaveServerName ) ) {
        return slaveServer;
      }
    }
    return null;
  }

  public ObjectId getObjectId() {
    return id;
  }

  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        return ValueMetaString.convertStringToBoolean( value );
      }
    }
    return defaultValue;
  }

  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  public String[] listVariables() {
    return variables.listVariables();
  }

  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  public void shareVariablesWith( VariableSpace space ) {
    variables = space;
  }

  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }

  /**
   * @return the dynamic
   */
  public boolean isDynamic() {
    return dynamic;
  }

  /**
   * @param dynamic
   *          the dynamic to set
   */
  public void setDynamic( boolean dynamic ) {
    this.dynamic = dynamic;
  }

  /**
   * @return A list of dynamic slave servers, retrieved from the first master server that was available.
   * @throws KettleException
   *           when none of the masters can be contacted.
   */
  public List<SlaveServer> getSlaveServersFromMasterOrLocal() throws KettleException {
    if ( isDynamic() ) {
      // Find a master that is available
      //
      List<SlaveServer> dynamicSlaves = null;
      Exception exception = null;
      for ( int i = 0; i < slaveServers.size(); i++ ) {
        SlaveServer slave = slaveServers.get( i );
        if ( slave.isMaster() && dynamicSlaves == null ) {
          try {
            List<SlaveServerDetection> detections = slave.getSlaveServerDetections();
            dynamicSlaves = new ArrayList<SlaveServer>();
            for ( SlaveServerDetection detection : detections ) {
              try {
                detection.getSlaveServer().getStatus();
              } catch ( Exception e ) {
                detection.setActive( false );
                detection.setLastInactiveDate( new Date() );
              }
              if ( detection.isActive() ) {
                dynamicSlaves.add( detection.getSlaveServer() );
              }
            }
          } catch ( Exception e ) {
            exception = e; // Remember the last exception
          }
        }
      }
      if ( dynamicSlaves == null && exception != null ) {
        throw new KettleException( exception );
      }
      return dynamicSlaves;
    } else {
      return slaveServers;
    }

  }

  /**
   * Not supported for Partition schema, return the root.
   */
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return new RepositoryDirectory();
  }

  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {
  }

  public RepositoryObjectType getRepositoryElementType() {
    return REPOSITORY_ELEMENT_TYPE;
  }

  public ObjectRevision getObjectRevision() {
    return objectRevision;
  }

  public void setObjectRevision( ObjectRevision objectRevision ) {
    this.objectRevision = objectRevision;
  }

  public String getDescription() {
    // NOT USED
    return null;
  }

  public void setDescription( String description ) {
    // NOT USED
  }

  /**
   * @return the changedDate
   */
  public Date getChangedDate() {
    return changedDate;
  }

  /**
   * @param changedDate
   *          the changedDate to set
   */
  public void setChangedDate( Date changedDate ) {
    this.changedDate = changedDate;
  }
}
