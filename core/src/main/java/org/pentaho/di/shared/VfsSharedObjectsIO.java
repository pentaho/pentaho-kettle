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

import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provide methods to retrieve and save the shared objects defined in shared.xml that is stored in file system.
 *
 */
public class VfsSharedObjectsIO implements SharedObjectsIO {
  private static Class<?> PKG = VfsSharedObjectsIO.class; // for i18n purposes, needed by Translator2!!
  private static final Logger log = LoggerFactory.getLogger( VfsSharedObjectsIO.class );

  String sharedObjectFile;
  String rootFolder;
  private static final String XML_TAG = "sharedobjects";
  // Type specific map.
  private Map<String, Node> connectionsNodes = new HashMap<>();
  private Map<String, Node> slaveServersNodes = new HashMap<>();
  private Map<String, Node> partitionSchemaNodes = new HashMap<>();
  private Map<String, Node> clusterSchemaNodes = new HashMap<>();

  private boolean isInitialized = false;

  /**
   * Creates the instance of VfsSharedObjectsIO using the default location of the shared file.
   *
   */
  public VfsSharedObjectsIO() {
    this( getDefaultSharedObjectFileLocation() );
  }

  /**
   * Creates the instance of VfsSharedObjectsIO using the rootFolder.
   *
   * @param rootFolder the root folder containing the shared object file.
   */
  public VfsSharedObjectsIO( String rootFolder ) {
    this.rootFolder = rootFolder;
    // Get the complete path to shared.xml
    this.sharedObjectFile = getSharedObjectFilePath( rootFolder );
  }


  /**
   * Return the Map of names and the corresponding nodes for the given shared object type.
   * The parsing of shared.xml happens when this method is called.
   * @param type
   * @return Map<String, Node>
   * @throws KettleXMLException
   */
  @Override
  public synchronized Map<String, Node> getSharedObjects( String type ) throws KettleXMLException {
    if ( !isInitialized ) {
      // Load the shared.xml file
      loadSharedObjectNodeMap( sharedObjectFile );
      isInitialized = true;
    }
    return getNodesMapForType( type );
  }

  /**
   * Loads the shared objects in the map. The map will be of the form <String, Node>
   * where key can be {"connection", "slaveserver", "partitionschema" or  clusterschema"} and
   * value will be xml Node.
   *
   * @param pathToSharedObjectFile The path to the shared object file
   * @throws KettleXMLException
   */
  private void loadSharedObjectNodeMap( String pathToSharedObjectFile ) throws KettleXMLException {

    try {
      // Get the FileObject
      FileObject file = KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( pathToSharedObjectFile );

      // If we have a shared file, load the content, otherwise, just keep this one empty
      if ( file.exists() ) {
        Document document = XMLHandler.loadXMLFile( file );
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
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "SharedOjects.ReadingError",
        pathToSharedObjectFile ), e );
    }

  }

  private void addNodeToMap( String type, Node node ) throws KettleXMLException {
    String tagName = XMLHandler.getTagValue( node, "name" );
    if ( !Utils.isEmpty( tagName ) ) {
      log.info( " Checking if connection exist  " + SharedObjectType.valueOf( type.toUpperCase() ) );
      getNodesMapForType( type ).put( tagName, node );
    }
  }

  /**
   * Return the complete path to the shared.xml. If the given path is blank,
   * it returns the default location of file.
   * @param path the root folder for shared object file
   * @return
   */
  private static final String getSharedObjectFilePath( String path ) {
    String filename = path;
    if ( Utils.isEmpty( path ) ) {
      filename = getDefaultSharedObjectFileLocation();
    } else if ( !path.endsWith( Const.SHARED_DATA_FILE ) ) {
      filename = path + File.separator + Const.SHARED_DATA_FILE;
    }
    return filename;
  }

  /**
   * Returns the default location of shared.xml. If checks the kettle environment varibale and if that is not set
   * retruns the location in user's home folder.
   * @return String
   */
  private static String getDefaultSharedObjectFileLocation() {
    // First fallback is the environment/kettle variable ${KETTLE_SHARED_OBJECTS}
    // This points to the file
    String filename = Variables.getADefaultVariableSpace().getVariable( Const.KETTLE_SHARED_OBJECTS );

    // Last line of defence...
    if ( Utils.isEmpty( filename ) ) {
      filename = Const.getSharedObjectsFile();
    }
    return filename;
  }

  /**
   * Save the name and the node for the given Shared Object Type in the file
   * @param type The SharedObject type
   * @param name The name of the SharedObject for a given type
   * @param node The xml node that containing the details of the SharedObject
   * @throws KettleException
   */
  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
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

    // Add or Update the map entry for this name
    nodeMap.put( name, node );

    saveToFile();
  }

  protected void saveToFile() throws KettleException {
    try {
      FileObject fileObject = KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( sharedObjectFile );
      Optional<String> backupFileName = createOrGetFileBackup( fileObject );
      writeToFile( fileObject, backupFileName );
      isInitialized = false;
    } catch ( IOException ex ) {
      throw new KettleException( ex );
    }
  }

  protected void writeToFile( FileObject fileObject, Optional<String> backupFileName )
    throws IOException, KettleException {
    try ( OutputStream outputStream = KettleVFS.getInstance( DefaultBowl.getInstance() ).getOutputStream( fileObject, false );
         PrintStream out = new PrintStream( outputStream ) ) {

      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
      out.println( "<" + XML_TAG + ">" );

      // Write the different SharedObject Types nodes to string
      out.println( toString( connectionsNodes ) );
      out.println( toString( slaveServersNodes ) );
      out.println( toString( partitionSchemaNodes ) );
      out.println( toString( clusterSchemaNodes ) );

      out.println( "</" + XML_TAG + ">" );
    } catch ( Exception e ) {
      // restore file if something wrong
      boolean isRestored = false;
      if ( backupFileName.isPresent() ) {
        restoreFileFromBackup( backupFileName.get() );
        isRestored = true;
      }
      throw new KettleException( BaseMessages.getString( PKG, "SharedOjects.ErrorWritingFile", isRestored ), e );
    }
  }

  private Optional<String> createOrGetFileBackup( FileObject fileObject ) throws IOException, KettleException {
    String backupFileName = sharedObjectFile + ".backup";
    boolean isBackupFileExist;
    if ( fileObject.exists() ) {
      isBackupFileExist = createFileBackup( backupFileName );
    } else {
      isBackupFileExist = getBackupFileFromFileSystem( backupFileName );
    }
    return isBackupFileExist ? Optional.ofNullable( backupFileName ) : Optional.empty();
  }

  private boolean createFileBackup( String backupFileName ) throws IOException, KettleFileException {
    return copyFile( sharedObjectFile, backupFileName );
  }

  protected void restoreFileFromBackup( String backupFileName ) throws IOException, KettleFileException {
    copyFile( backupFileName, sharedObjectFile );
  }

  private boolean getBackupFileFromFileSystem( String backupFileName ) throws KettleException {
    FileObject fileObject = KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( backupFileName );
    try {
      return fileObject.exists();
    } catch ( FileSystemException e ) {
      return false;
    }
  }

  private boolean copyFile( String src, String dest ) throws IOException, KettleFileException {
    IKettleVFS vfs = KettleVFS.getInstance( DefaultBowl.getInstance() );
    FileObject srcFile = vfs.getFileObject( src );
    FileObject destFile = vfs.getFileObject( dest );
    try ( InputStream in = KettleVFS.getInputStream( srcFile );
         OutputStream out = vfs.getOutputStream( destFile, false ) ) {
      IOUtils.copy( in, out );
    }
    return true;
  }

  /**
   * Return the node for the given SharedObject type and name.
   * @param type
   * @param name
   * @return
   * @throws KettleXMLException
   */
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

    saveToFile();
  }

  @Override
  public void clear( String type ) throws KettleException {
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
  }

  private Map<String, Node> getNodesMapForType( String type ) throws KettleXMLException {
    // change the type to uppercase so we can convert it to SharedObjectType
    return getNodesMapForType( SharedObjectType.valueOf( type.toUpperCase() ) );
  }

  private Map<String, Node> getNodesMapForType( SharedObjectType type ) throws KettleXMLException {
    Map<String, Node> nodeMap = new HashMap();
    switch ( type ) {
      case CONNECTION:
        nodeMap = connectionsNodes;
        break;
      case SLAVESERVER:
        nodeMap = slaveServersNodes;
        break;
      case PARTITIONSCHEMA:
        nodeMap = partitionSchemaNodes;
        break;
      case CLUSTERSCHEMA:
        nodeMap = clusterSchemaNodes;
        break;
      default:
        // unsupported type
        log.error( " Invalid Shared Object type " + type );
        throw new KettleXMLException( "Invalid shared object type " + type );
    }
    return nodeMap;
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
