/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.www;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;

/**
 * This is a map between the transformation name and the
 * (running/waiting/finished) transformation.
 * 
 * @author Matt
 * 
 */
public class TransformationMap {
  private Map<CarteObjectEntry, Trans>              transformationMap;
  private Map<CarteObjectEntry, TransConfiguration> configurationMap;

  private Map<String, List<SocketPortAllocation>>   hostServerSocketPortsMap;

  private SlaveServerConfig                         slaveServerConfig;

  public TransformationMap() {
    transformationMap = new Hashtable<CarteObjectEntry, Trans>();
    configurationMap = new Hashtable<CarteObjectEntry, TransConfiguration>();

    hostServerSocketPortsMap = new Hashtable<String, List<SocketPortAllocation>>();
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
  public synchronized void addTransformation(String transformationName, String containerObjectId, Trans trans, TransConfiguration transConfiguration) {
    CarteObjectEntry entry = new CarteObjectEntry(transformationName, containerObjectId);
    transformationMap.put(entry, trans);
    configurationMap.put(entry, transConfiguration);
  }

  /**
   * Find the first transformation in the list that comes to mind!
   * 
   * @param transformationName
   * @return the first transformation with the specified name
   */
  public synchronized Trans getTransformation(String transformationName) {
    for (CarteObjectEntry entry : transformationMap.keySet()) {
      if (entry.getName().equals(transformationName)) {
        return transformationMap.get(entry);
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte transformation object
   * @return the transformation with the specified entry
   */
  public synchronized Trans getTransformation(CarteObjectEntry entry) {
    return transformationMap.get(entry);
  }

  /**
   * @param transformationName
   * @return The first transformation configuration with the specified name
   */
  public synchronized TransConfiguration getConfiguration(String transformationName) {
    for (CarteObjectEntry entry : configurationMap.keySet()) {
      if (entry.getName().equals(transformationName)) {
        return configurationMap.get(entry);
      }
    }
    return null;
  }

  /**
   * @param entry
   *          The Carte transformation object
   * @return the transformation configuration with the specified entry
   */
  public synchronized TransConfiguration getConfiguration(CarteObjectEntry entry) {
    return configurationMap.get(entry);
  }

  /**
   * 
   * @param entry
   *          the Carte object entry
   */
  public synchronized void removeTransformation(CarteObjectEntry entry) {
    transformationMap.remove(entry);
    configurationMap.remove(entry);
  }

  public List<CarteObjectEntry> getTransformationObjects() {
    return new ArrayList<CarteObjectEntry>(transformationMap.keySet());
  }

  /**
   * @return the configurationMap
   */
  public Map<CarteObjectEntry, TransConfiguration> getConfigurationMap() {
    return configurationMap;
  }

  /**
   * @param configurationMap
   *          the configurationMap to set
   */
  public void setConfigurationMap(Map<CarteObjectEntry, TransConfiguration> configurationMap) {
    this.configurationMap = configurationMap;
  }

  /**
   * This is the meat of the whole problem. We'll allocate a port for a given
   * slave, transformation and step copy, always on the same host. Algorithm: 1)
   * Search for the right map in the hostPortMap
   * 
   * @param portRangeStart
   *          the start of the port range as described in the used cluster
   *          schema
   * @param hostname
   *          the host name to allocate this address for
   * @param carteObjectId
   * @param transformationName
   * @param sourceStepName
   * @param sourceStepCopy
   * @return
   */
  public synchronized SocketPortAllocation allocateServerSocketPort(int portRangeStart, String hostname, String carteObjectId, String transformationName, String sourceSlaveName, String sourceStepName, String sourceStepCopy, String targetSlaveName,
      String targetStepName, String targetStepCopy) {

    synchronized (hostServerSocketPortsMap) {
      // Look up the sockets list for the given host
      //
      List<SocketPortAllocation> serverSocketPortsMap = hostServerSocketPortsMap.get(hostname);
      if (serverSocketPortsMap == null) {
        serverSocketPortsMap = new ArrayList<SocketPortAllocation>();
        hostServerSocketPortsMap.put(hostname, serverSocketPortsMap);
      }

      // Find the socket port allocation in the list...
      //
      SocketPortAllocation socketPortAllocation = null;
      int maxPort = portRangeStart - 1;
      for (int index = 0; index < serverSocketPortsMap.size(); index++) {
        SocketPortAllocation spa = serverSocketPortsMap.get(index);
        if (spa.getPort() > maxPort) {
          maxPort = spa.getPort();
        }

        if (spa.getSourceSlaveName().equalsIgnoreCase(sourceSlaveName) && spa.getTargetSlaveName().equalsIgnoreCase(targetSlaveName) && spa.getTransformationName().equalsIgnoreCase(transformationName)
            && spa.getSourceStepName().equalsIgnoreCase(sourceStepName) && spa.getSourceStepCopy().equalsIgnoreCase(sourceStepCopy) && spa.getTargetStepName().equalsIgnoreCase(targetStepName)
            && spa.getTargetStepCopy().equalsIgnoreCase(targetStepCopy)) {
          // This is the port we want, return it. Make sure it's allocated.
          //
          spa.setAllocated(true);
          socketPortAllocation = spa;
          break;
        } else {
          // If we find an available spot, take it.
          //
          if (!spa.isAllocated()) {
            // This is not an allocated port.
            // So we can basically use this port slot to put our own allocation
            // in it.
            //
            // However, that is ONLY possible if the port belongs to the same
            // slave server couple.
            // Otherwise, we keep on searching.
            //
            if (spa.getSourceSlaveName().equalsIgnoreCase(sourceSlaveName) && spa.getTargetSlaveName().equalsIgnoreCase(targetSlaveName)) {
              socketPortAllocation = new SocketPortAllocation(spa.getPort(), new Date(), carteObjectId, transformationName, sourceSlaveName, sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy);
              serverSocketPortsMap.set(index, socketPortAllocation);
              break;
            }
          }
        }
      }

      if (socketPortAllocation == null) {
        // Allocate a new port and add it to the back of the list
        // Normally this list should stay sorted on port number this way
        //
        socketPortAllocation = new SocketPortAllocation(maxPort + 1, new Date(), carteObjectId, transformationName, sourceSlaveName, sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy);
        serverSocketPortsMap.add(socketPortAllocation);
      }

      // DEBUG : Do a verification on the content of the list.
      // If we find a port twice in the list, complain!
      //
      for (int i = 0; i < serverSocketPortsMap.size(); i++) {
        for (int j = 0; j < serverSocketPortsMap.size(); j++) {
          if (i != j) {
            SocketPortAllocation one = serverSocketPortsMap.get(i);
            SocketPortAllocation two = serverSocketPortsMap.get(j);
            if (one.getPort() == two.getPort()) {
              System.out.println("!! Error detected !! Identical ports discovered in the ports list.");
            }
          }
        }
      }

      // give back the good news too...
      //
      return socketPortAllocation;
    }
  }

  /**
   * Deallocate all the ports for the given transformation name, across all
   * hosts.
   * 
   * @param transName
   *          the transformation name to release
   * @param carteObjectId
   *          the carte object ID to reference
   */
  public void deallocateServerSocketPorts(String transName, String carteObjectId) {
    for (String hostname : hostServerSocketPortsMap.keySet()) {
      for (SocketPortAllocation spa : hostServerSocketPortsMap.get(hostname)) {
        if (spa.getTransformationName().equalsIgnoreCase(transName) && (Const.isEmpty(carteObjectId) || spa.getCarteObjectId().equals(carteObjectId))) {
          spa.setAllocated(false);
        }
      }
    }
  }

  /**
   * Deallocate all the ports for the given transformation entry, across all
   * hosts.
   * 
   * @param entry
   *          the transformation object entry name to release the sockets for
   */
  public void deallocateServerSocketPorts(CarteObjectEntry entry) {
    for (String hostname : hostServerSocketPortsMap.keySet()) {
      for (SocketPortAllocation spa : hostServerSocketPortsMap.get(hostname)) {
        if (spa.getTransformationName().equalsIgnoreCase(entry.getName())) { // TODO:
                                                                             // also
                                                                             // include
                                                                             // the
                                                                             // carte
                                                                             // object
                                                                             // ID?
          spa.setAllocated(false);
        }
      }
    }
  }

  public void deallocateServerSocketPort(int port, String hostname) {
    // Look up the sockets list for the given host
    //
    List<SocketPortAllocation> serverSocketPortsMap = hostServerSocketPortsMap.get(hostname);
    if (serverSocketPortsMap == null) {
      return; // nothing to deallocate
    }

    // Find the socket port allocation in the list...
    //
    for (SocketPortAllocation spa : new ArrayList<SocketPortAllocation>(serverSocketPortsMap)) {

      if (spa.getPort() == port) {
        spa.setAllocated(false);
        return;
      }
    }
  }

  public CarteObjectEntry getFirstCarteObjectEntry(String transName) {
    for (CarteObjectEntry key : transformationMap.keySet()) {
      if (key.getName().equals(transName))
        return key;
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
  public void setSlaveServerConfig(SlaveServerConfig slaveServerConfig) {
    this.slaveServerConfig = slaveServerConfig;
  }

  /**
   * @return the hostServerSocketPortsMap
   */
  public Map<String, List<SocketPortAllocation>> getHostServerSocketPortsMap() {
    return hostServerSocketPortsMap;
  }

  /**
   * @param hostServerSocketPortsMap the hostServerSocketPortsMap to set
   */
  public void setHostServerSocketPortsMap(Map<String, List<SocketPortAllocation>> hostServerSocketPortsMap) {
    this.hostServerSocketPortsMap = hostServerSocketPortsMap;
  }
}
