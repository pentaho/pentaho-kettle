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


package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds shared objects in memory.
 * <p>
 * Note that any shared objects stored in this cannot participate in the XmlHander cache. That is, they must pass
 * 'false' to XMLHandler.getSubNodeByNr().
 *
 */
public class MemorySharedObjectsIO implements SharedObjectsIO {

  private Map<String, Map<String, Node>> storageMap = new ConcurrentHashMap<>();

  @Override
  public synchronized Map<String, Node> getSharedObjects( String type ) throws KettleException {
    return getNodesMapForType( type );
  }

  /**
   * Save the SharedObject in memory. This operation is case-insensitive. If the SharedObject name exist in a different case,
   * the existing entry will be deleted and the new entry will be saved with provided name
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and clusterschema"
   * @param name The name is the name of the sharedObject
   * @param node The Xml node containing the details of the shared object
   * @throws KettleException
   */
  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
    // Get the map for the type
    Map<String, Node> nodeMap = getNodesMapForType( type );
    String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap.keySet() );
    if ( existingName != null ) {
      nodeMap.remove( existingName );
    }
    // Add or Update the map entry for this name
    nodeMap.put( name, node );
  }

  /**
   * Return the node for the given SharedObject type and name. The lookup for the SharedObject
   * using the name will be case-insensitive
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and clusterschema"
   * @param name The name is the name of the sharedObject
   * @return
   * @throws KettleException
   */
  @Override
  public Node getSharedObject( String type, String name ) throws KettleException {
    // Get the Map using the type
    Map<String, Node> nodeMap = getNodesMapForType( type );
    return nodeMap.get( SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap.keySet() ) );
  }

  /**
   * Remove the SharedObject for the type and name. The lookup for the SharedObject
   * using the name will be case-insensitive
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and clusterschema"
   * @param name The name is the name of the sharedObject
   * @throws KettleException
   */
  @Override
  public void delete( String type, String name ) throws KettleException {
    // Get the nodeMap for the type
    Map<String, Node> nodeTypeMap = getNodesMapForType( type );
    String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeTypeMap.keySet() );
    if ( existingName != null ) {
      nodeTypeMap.remove( existingName );
    }
  }

  @Override
  public void clear( String type ) throws KettleException {
    storageMap.remove( type );
  }

  public void clear() {
    storageMap.clear();
  }

  private Map<String,Node> getNodesMapForType( String type ) {
    return storageMap.computeIfAbsent( type, k -> new HashMap<>() );
  }

  @Override
  public void clearCache() {
    // No "caching" in memory implementation. The data is the data. 
  }

}
