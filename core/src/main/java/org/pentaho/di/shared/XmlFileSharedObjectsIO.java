package org.pentaho.di.shared;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.xml.XMLHandler;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provide methods to retrieve and save the shared objects defined in shared.xml.
 *
 */
public abstract class XmlFileSharedObjectsIO implements SharedObjectsIO {
  private static final Logger log = LoggerFactory.getLogger( XmlFileSharedObjectsIO.class );

  private final ReentrantLock lock = new ReentrantLock();

  public static final String XML_TAG = "sharedobjects";
  public static final String NAME_TAG = "name";
  // Type specific map.
  private Map<String, Node> connectionsNodes = new HashMap<>();
  private Map<String, Node> slaveServersNodes = new HashMap<>();
  private Map<String, Node> partitionSchemaNodes = new HashMap<>();
  private Map<String, Node> clusterSchemaNodes = new HashMap<>();

  private boolean isInitialized = false;

  public XmlFileSharedObjectsIO() {
  }

  /**
   * Load shared objects from file
   * @throws KettleXMLException
   */
  protected abstract void loadSharedObjectsNodeMap() throws KettleXMLException;

    /**
   * Save shared objects to file
   * @throws KettleXMLException
   */
  protected abstract void saveToFile() throws KettleException;

  /**
   * Return the Map of names and the corresponding nodes for the given shared object type.
   * The parsing of shared.xml happens when this method is called.
   *
   * @param type
   * @return Map<String, Node>
   * @throws KettleXMLException
   */
  @Override
  public Map<String, Node> getSharedObjects( String type ) throws KettleXMLException {
    checkLock();
    if ( !isInitialized ) {
      // Load the shared.xml file
      loadSharedObjectsNodeMap();
      isInitialized = true;
    }
    return getNodesMapForType( type );
  }

  @Override
  public void clearCache() {
    try {
      lock();
      connectionsNodes.clear();
      slaveServersNodes.clear();
      partitionSchemaNodes.clear();
      clusterSchemaNodes.clear();
      isInitialized = false;
    } finally {
      unlock();
    }
  }

  /**
   * Loads the shared objects in the map. The map will be of the form <String, Node>
   * where key can be {"connection", "slaveserver", "partitionschema" or clusterschema"} and
   * value will be xml Node.
   */
  protected void loadSharedObjectsNodeMap( Document document ) throws KettleXMLException {
    Node sharedObjectsNode = XMLHandler.getSubNode( document, XML_TAG );
    if ( sharedObjectsNode != null ) {
      NodeList childNodes = sharedObjectsNode.getChildNodes();
      for ( int i = 0; i < childNodes.getLength(); i++ ) {
        Node node = childNodes.item( i );

        String nodeName = node.getNodeName();
        // add the node to the map
        addNodeToMap( nodeName, node );

      }
    }
  }

  protected void addNodeToMap( String type, Node node ) throws KettleXMLException {
    String tagName = XMLHandler.getTagValue( node, NAME_TAG );
    if ( !Utils.isEmpty( tagName ) ) {
      log.debug( "Checking if connection exist {}", SharedObjectType.valueOf( type.toUpperCase() ) );
      getNodesMapForType( type ).put( tagName, node );
    }
  }

  /**
   * Save the name and the node for the given Shared Object Type in the file. If the SharedObject name exist but in a
   * different case, the existing entry will be deleted and the new entry will be saved with provided name
   *
   * @param type The SharedObject type
   * @param name The name of the SharedObject for a given type
   * @param node The xml node that containing the details of the SharedObject
   * @throws KettleException
   */
  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
    checkLock();
    // Get the map for the type
    Map<String, Node> nodeMap = getNodesMapForType( type );

    // strip out any Object IDs
    NodeList children = node.getChildNodes();
    for ( int i = 0; i < children.getLength(); i++ ) {
      Node childNode = children.item( i );
      if ( childNode.getNodeName().equalsIgnoreCase( SharedObjectInterface.OBJECT_ID ) ) {
        node.removeChild( childNode );
      }
    }
    // Before adding, verify that the SharedObject with the name (case insensitive) does not exist,
    // if it exist, delete the entry from map
    String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap.keySet() );
    if ( existingName != null ) {
      nodeMap.remove( existingName );
    }

    // Add or Update the map entry for this name
    nodeMap.put( name, node );

    saveToFile();
  }

  protected void writeTo( OutputStream outputStream ) throws KettleXMLException {
    var charset = StandardCharsets.UTF_8;
    try ( PrintStream out = new PrintStream( outputStream, false, charset ) ) {
      out.print( XMLHandler.getXMLHeader( charset.name() ) );
      out.println( "<" + XML_TAG + ">" );

      // Write the different SharedObject Types nodes to string
      out.println( toString( connectionsNodes ) );
      out.println( toString( slaveServersNodes ) );
      out.println( toString( partitionSchemaNodes ) );
      out.println( toString( clusterSchemaNodes ) );

      out.println( "</" + XML_TAG + ">" );
    }
  }

  /**
   * Return the node for the given SharedObject type and name. The lookup for the SharedObject
   * using the name will be case-insensitive
   * 
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and
   *             clusterschema"
   * @param name The name is the name of the sharedObject
   * @return
   * @throws KettleXMLException
   */
  @Override
  public Node getSharedObject( String type, String name ) throws KettleException {
    checkLock();
    // Get the Map using the type
    Map<String, Node> nodeMap = getNodesMapForType( type );
    return nodeMap.get( SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeMap.keySet() ) );
  }

  /**
   * Delete the SharedObject for the given type and name. The delete operation will be case-insensitive.
   * 
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and
   *             clusterschema"
   * @param name The name is the name of the sharedObject
   * @throws KettleException
   */
  @Override
  public void delete( String type, String name ) throws KettleException {
    try {
      lock();
      // Get the nodeMap for the type
      Map<String, Node> nodeTypeMap = getNodesMapForType( type );
      String existingName = SharedObjectsIO.findSharedObjectIgnoreCase( name, nodeTypeMap.keySet() );
      if ( existingName != null ) {
        nodeTypeMap.remove( existingName );
        saveToFile();
      }
    } finally {
      unlock();
    }
  }

  @Override
  public void clear( String type ) throws KettleException {
    try {
      lock();
      switch ( SharedObjectType.valueOf( type.toUpperCase() ) ) {
        case CONNECTION:
          connectionsNodes.clear();
          break;
        case SLAVESERVER:
          slaveServersNodes.clear();
          break;
        case PARTITIONSCHEMA:
          partitionSchemaNodes.clear();
          break;
        case CLUSTERSCHEMA:
          clusterSchemaNodes.clear();
          break;
        default:
          // unsupported type
          log.error( " Invalid Shared Object type " + type );
          throw new KettleXMLException( "Invalid shared object type " + type );
      }
      saveToFile();
    } finally {
      unlock();
    }
  }

  @Override
  public void lock() {
    // Lock the SharedObjectsIO for exclusive access
    lock.lock();
  }

  @Override
  public void unlock() {
    // Unlock the SharedObjectsIO after exclusive access is no longer needed
    lock.unlock();
  }

  private void checkLock() {
    if ( !lock.isHeldByCurrentThread() ) {
      throw new IllegalStateException( "SharedObjectsIO lock must be held when accessing Node objects" );
    }
  }

  private Map<String, Node> getNodesMapForType( String type ) throws KettleXMLException {
    // change the type to uppercase so we can convert it to SharedObjectType
    return getNodesMapForType( SharedObjectType.valueOf( type.toUpperCase() ) );
  }

  private Map<String, Node> getNodesMapForType( SharedObjectType type ) throws KettleXMLException {
    return switch ( type ) {
      case CONNECTION -> connectionsNodes;
      case SLAVESERVER -> slaveServersNodes;
      case PARTITIONSCHEMA -> partitionSchemaNodes;
      case CLUSTERSCHEMA -> clusterSchemaNodes;
      default -> {
        log.error( " Invalid Shared Object type " + type );
        throw new KettleXMLException( "Invalid shared object type " + type );
      }
    };
  }

  protected String toString( Map<String, Node> nodesMap ) throws KettleXMLException {
    StringBuilder xml = new StringBuilder();
    // Loop through the nodes objects from the map
    Collection<Node> collection = nodesMap.values();
    for ( Node node : collection ) {
      xml.append( XMLHandler.formatNode( node ) );
    }
    return xml.toString();
  }

}
