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

package org.pentaho.di.www;




import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Collections;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;

/**
 * This is a map between the transformation name and the (running/waiting/finished) transformation.
 *
 * @author Matt
 *
 */
public class TransformationMap {
  private final Map<CarteObjectEntry, TransData> transMap;

  private final Map<String, List<SocketPortAllocation>> hostServerSocketPortsMap;

  private SlaveServerConfig slaveServerConfig;

  public TransformationMap() {
    transMap = new ConcurrentHashMap<>();
    hostServerSocketPortsMap = new ConcurrentHashMap<>();
  }

  /**
   * Add a transformation to the map
   *
   * @param transformationName
   *          The name of the transformation to add
   * @param containerObjectId
   *          the unique ID of the transformation in this container.
   * @param trans
   *          The transformation to add
   * @param transConfiguration
   *          the transformation configuration to add
   */
  public void addTransformation( String transformationName, String containerObjectId, Trans trans,
    TransConfiguration transConfiguration ) {
    CarteObjectEntry entry = new CarteObjectEntry( transformationName, containerObjectId );
    transMap.put( entry, new TransData( trans, transConfiguration ) );
  }

  public void registerTransformation( Trans trans, TransConfiguration transConfiguration ) {
    trans.setContainerObjectId( UUID.randomUUID().toString() );
    CarteObjectEntry entry = new CarteObjectEntry( trans.getTransMeta().getName(), trans.getContainerObjectId() );
    transMap.put( entry, new TransData( trans, transConfiguration ) );
  }

  /**
   * Find the first transformation in the list that comes to mind!
   *
   * @param transformationName
   * @return the first transformation with the specified name
   */
  public Trans getTransformation( String transformationName ) {
    for ( CarteObjectEntry entry : transMap.keySet() ) {
      if ( entry.getName().equals( transformationName ) ) {
        return transMap.get( entry ).getTrans();
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte transformation object
   * @return the transformation with the specified entry
   */
  public Trans getTransformation( CarteObjectEntry entry ) {
    return transMap.get( entry ).getTrans();
  }

  /**
   * @param transformationName
   * @return The first transformation configuration with the specified name
   */
  public TransConfiguration getConfiguration( String transformationName ) {
    for ( CarteObjectEntry entry : transMap.keySet() ) {
      if ( entry.getName().equals( transformationName ) ) {
        return transMap.get( entry ).getConfiguration();
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte transformation object
   * @return the transformation configuration with the specified entry
   */
  public TransConfiguration getConfiguration( CarteObjectEntry entry ) {
    return transMap.get( entry ).getConfiguration();
  }

  /**
   *
   * @param entry
   *          the Carte object entry
   */
  public void removeTransformation( CarteObjectEntry entry ) {
    transMap.remove( entry );
  }

  public List<CarteObjectEntry> getTransformationObjects() {
    return new ArrayList<>( transMap.keySet() );
  }


  /**
   * This is the meat of the whole problem. We'll allocate a port for a given slave, transformation and step copy,
   * always on the same host. Algorithm: 1) Search for the right map in the hostPortMap
   *
   * @param portRangeStart
   *          the start of the port range as described in the used cluster schema
   * @param hostname
   *          the host name to allocate this address for
   * @param clusteredRunId
   *          A unique id, created for each new clustered run during transformation split.
   * @param transformationName
   * @param sourceStepName
   * @param sourceStepCopy
   * @return
   */
  public SocketPortAllocation allocateServerSocketPort( int portRangeStart, String hostname,
    String clusteredRunId, String transformationName, String sourceSlaveName, String sourceStepName,
    String sourceStepCopy, String targetSlaveName, String targetStepName, String targetStepCopy ) {

    // Do some validations first...
    //
    if ( Utils.isEmpty( clusteredRunId ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a cluster run ID but it was empty" );
    }
    if ( portRangeStart <= 0 ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by port range start > 0 but it was "
          + portRangeStart );
    }
    if ( Utils.isEmpty( hostname ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a hostname but it was empty" );
    }
    if ( Utils.isEmpty( transformationName ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a transformation name but it was empty" );
    }
    if ( Utils.isEmpty( sourceSlaveName ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a source slave server name but it was empty" );
    }
    if ( Utils.isEmpty( targetSlaveName ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a target slave server name but it was empty" );
    }
    if ( Utils.isEmpty( sourceStepName ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a source step name but it was empty" );
    }
    if ( Utils.isEmpty( targetStepName ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a target step name but it was empty" );
    }
    if ( Utils.isEmpty( sourceStepCopy ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a source step copy but it was empty" );
    }
    if ( Utils.isEmpty( targetStepCopy ) ) {
      throw new RuntimeException(
        "A server socket allocation always has to accompanied by a target step copy but it was empty" );
    }

      // Look up the sockets list for the given host
      //
    List<SocketPortAllocation> serverSocketPorts;
    serverSocketPorts = hostServerSocketPortsMap.computeIfAbsent( hostname, k -> new CopyOnWriteArrayList<>() );
    serverSocketPorts = serverSocketPorts != null ? serverSocketPorts : hostServerSocketPortsMap.get( hostname );

        // Find the socket port allocation in the list...
        //
    SocketPortAllocation socketPortAllocation = null;
    int maxPort = portRangeStart - 1;
    for ( int index = 0; index < serverSocketPorts.size(); index++ ) {
      SocketPortAllocation spa = serverSocketPorts.get( index );
      if ( spa.getPort() > maxPort ) {
        maxPort = spa.getPort();
      }
      synchronized ( spa ) {

        if ( spa.getClusterRunId().equalsIgnoreCase( clusteredRunId )
            && spa.getSourceSlaveName().equalsIgnoreCase( sourceSlaveName )
            && spa.getTargetSlaveName().equalsIgnoreCase( targetSlaveName )
            && spa.getTransformationName().equalsIgnoreCase( transformationName )
            && spa.getSourceStepName().equalsIgnoreCase( sourceStepName )
            && spa.getSourceStepCopy().equalsIgnoreCase( sourceStepCopy )
            && spa.getTargetStepName().equalsIgnoreCase( targetStepName )
            && spa.getTargetStepCopy().equalsIgnoreCase( targetStepCopy ) ) {
            // This is the port we want, return it. Make sure it's allocated.
            //
          spa.setAllocated( true );
          socketPortAllocation = spa;
          break;
        } else {
            // If we find an available spot, take it.
            //
          if ( !spa.isAllocated() ) {
              // This is not an allocated port.
              // So we can basically use this port slot to put our own allocation
              // in it.
              //
              // However, that is ONLY possible if the port belongs to the same
              // slave server couple.
              // Otherwise, we keep on searching.
              //
            if ( spa.getSourceSlaveName().equalsIgnoreCase( sourceSlaveName )
                && spa.getTargetSlaveName().equalsIgnoreCase( targetSlaveName ) ) {
              socketPortAllocation =
                  new SocketPortAllocation(
                    spa.getPort(), new Date(), clusteredRunId, transformationName, sourceSlaveName,
                    sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy );
              serverSocketPorts.set( index, socketPortAllocation );
              break;
            }
          }
        }
      }
    }
    if ( socketPortAllocation == null ) {
          // Allocate a new port and add it to the back of the list
          // Normally this list should stay sorted on port number this way
          //
      socketPortAllocation =
            new SocketPortAllocation(
              maxPort + 1, new Date(), clusteredRunId, transformationName, sourceSlaveName, sourceStepName,
              sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy );
      serverSocketPorts.add( socketPortAllocation );
    }

        // DEBUG : Do a verification on the content of the list.
        // If we find a port twice in the list, complain!
        //
        /*
         * for (int i = 0; i < serverSocketPortsMap.size(); i++) { for (int j = 0; j < serverSocketPortsMap.size(); j++)
         * { if (i != j) { SocketPortAllocation one = serverSocketPortsMap.get(i); SocketPortAllocation two =
         * serverSocketPortsMap.get(j); if (one.getPort() == two.getPort()) { throw new
         * RuntimeException("Error detected !! Identical ports discovered in the ports list."); } } } }
         */

        // give back the good news too...
        //
    return socketPortAllocation;
  }

  /**
   * Deallocate all the ports for the given transformation name, across all hosts.
   *
   * @param transName
   *          the transformation name to release
   * @param carteObjectId
   *          the carte object ID to reference
   */
  public void deallocateServerSocketPorts( String transName, String carteObjectId ) {
    for ( String hostname : hostServerSocketPortsMap.keySet() ) {
      List<SocketPortAllocation> spas = hostServerSocketPortsMap.get( hostname );
      for ( SocketPortAllocation spa : spas ) {
        synchronized ( spa ) {
          if ( spa.getTransformationName().equalsIgnoreCase( transName )
              && ( Utils.isEmpty( carteObjectId ) || spa.getClusterRunId().equals( carteObjectId ) ) ) {
            spa.setAllocated( false );
          }
        }
      }
    }
  }

  /**
   * Deallocate all the ports for the given transformation entry, across all hosts.
   *
   * @param entry
   *          the transformation object entry name to release the sockets for
   */
  public void deallocateServerSocketPorts( CarteObjectEntry entry ) {
    for ( String hostname : hostServerSocketPortsMap.keySet() ) {
      List<SocketPortAllocation> serverSocketPorts = hostServerSocketPortsMap.get( hostname );
      for ( SocketPortAllocation spa : hostServerSocketPortsMap.get( hostname ) ) {
        synchronized ( spa ) {
          if ( spa.getTransformationName().equalsIgnoreCase( entry.getName() ) ) {
            spa.setAllocated( false );
          }
        }
      }
    }
  }

  public void deallocateServerSocketPort( int port, String hostname ) {
    // Look up the sockets list for the given host
    //
    List<SocketPortAllocation> serverSocketPorts = hostServerSocketPortsMap.get( hostname );

    if ( serverSocketPorts == null ) {
      return; // nothing to deallocate
    }
      // Find the socket port allocation in the list...
      //
    for ( SocketPortAllocation spa : serverSocketPorts ) {
      synchronized ( spa ) {
        if ( spa.getPort() == port ) {
          spa.setAllocated( false );
          return;
        }
      }
    }
  }

  public CarteObjectEntry getFirstCarteObjectEntry( String transName ) {
    for ( CarteObjectEntry key : transMap.keySet() ) {
      if ( key.getName().equals( transName ) ) {
        return key;
      }
    }
    return null;
  }

  /**
   * @return the slaveServerConfig
   */
  public SlaveServerConfig getSlaveServerConfig() {
    return slaveServerConfig;
  }

  /**
   * @param slaveServerConfig
   *          the slaveServerConfig to set
   */
  public void setSlaveServerConfig( SlaveServerConfig slaveServerConfig ) {
    this.slaveServerConfig = slaveServerConfig;
  }

  /**
   * @return the hostServerSocketPortsMap
   */
  public List<SocketPortAllocation> getHostServerSocketPorts( String hostname ) {
    List<SocketPortAllocation> ports = hostServerSocketPortsMap.get( hostname );
    return ports == null ? Collections.emptyList() : Collections.unmodifiableList( ports );
  }

  public SlaveSequence getSlaveSequence( String name ) {
    return SlaveSequence.findSlaveSequence( name, slaveServerConfig.getSlaveSequences() );
  }

  public boolean isAutomaticSlaveSequenceCreationAllowed() {
    return slaveServerConfig.isAutomaticCreationAllowed();
  }

  public SlaveSequence createSlaveSequence( String name ) throws KettleException {
    SlaveSequence auto = slaveServerConfig.getAutoSequence();
    if ( auto == null ) {
      throw new KettleException( "No auto-sequence information found in the slave server config.  "
        + "Slave sequence could not be created automatically." );
    }

    SlaveSequence slaveSequence =
      new SlaveSequence( name, auto.getStartValue(), auto.getDatabaseMeta(), auto.getSchemaName(), auto
        .getTableName(), auto.getSequenceNameField(), auto.getValueField() );

    slaveServerConfig.getSlaveSequences().add( slaveSequence );

    return slaveSequence;
  }

  private static class TransData {

    private Trans trans;

    private TransConfiguration configuration;

    TransData( Trans trans, TransConfiguration configuration ) {
      this.trans = trans;
      this.configuration = configuration;
    }

    public Trans getTrans() {
      return trans;
    }

    public void setTrans( Trans trans ) {
      this.trans = trans;
    }

    public TransConfiguration getConfiguration() {
      return configuration;
    }

    public void setConfiguration( TransConfiguration configuration ) {
      this.configuration = configuration;
    }
  }
}
