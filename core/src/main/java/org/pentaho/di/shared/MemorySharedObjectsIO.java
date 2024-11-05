/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
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

package org.pentaho.di.shared;

import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds shared objects in memory.
 * <p>
 * Note that any shared objects stored in this cannot participate in the XmlHander cache. That is, they must pass
 * 'false' to XMLHandler.getSubNodeByNr().
 *
 */
public class MemorySharedObjectsIO implements SharedObjectsIO {
  private static Class<?> PKG = MemorySharedObjectsIO.class; // for i18n purposes, needed by Translator2!!
  private static final Logger log = LoggerFactory.getLogger( MemorySharedObjectsIO.class );

  Map<String, Map<String, Node>> storageMap = new ConcurrentHashMap<>();

  public MemorySharedObjectsIO() {
  }

  @Override
  public synchronized Map<String, Node> getSharedObjects( String type ) throws KettleException {
    return getNodesMapForType( type );
  }

  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
    // Get the map for the type
    Map<String, Node> nodeMap = getNodesMapForType( type );
    // Add or Update the map entry for this name
    nodeMap.put( name, node );
  }

  @Override
  public Node getSharedObject( String type, String name ) throws KettleException {
    // Get the Map using the type
    Map<String, Node> nodeMap = getNodesMapForType( type );
    return nodeMap.get( name );
  }

  @Override
  public void delete( String type, String name ) throws KettleException {
    // Get the nodeMap for the type
    Map<String, Node> nodeTypeMap = getNodesMapForType( type );
    Node removedNode = nodeTypeMap.remove( name );
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

}
