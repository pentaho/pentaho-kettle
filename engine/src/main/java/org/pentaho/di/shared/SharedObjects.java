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

package org.pentaho.di.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.poi.util.IOUtils;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.annotations.VisibleForTesting;

/**
 * Based on a piece of XML, this factory will give back a list of objects. In other words, it does XML de-serialisation
 *
 * @author Matt
 */
public class SharedObjects {
  private static Class<?> PKG = SharedObjects.class; // for i18n purposes, needed by Translator2!!

  private static final String XML_TAG = "sharedobjects";

  private String filename;

  private Map<SharedEntry, SharedObjectInterface> objectsMap;

  private class SharedEntry {
    public String className;
    public String objectName;

    /**
     * @param className
     * @param objectName
     */
    public SharedEntry( String className, String objectName ) {
      this.className = className;
      this.objectName = objectName;
    }

    public boolean equals( Object obj ) {
      SharedEntry sharedEntry = (SharedEntry) obj;
      return className.equals( sharedEntry.className ) && objectName.equalsIgnoreCase( objectName );
    }

    public int hashCode() {
      return className.hashCode() ^ objectName.hashCode();
    }

  }

  public SharedObjects( String sharedObjectsFile ) throws KettleXMLException {
    try {
      this.filename = createFilename( sharedObjectsFile );
      this.objectsMap = new Hashtable<SharedEntry, SharedObjectInterface>();

      LogChannel log = new LogChannel( this );

      // Extra information
      FileObject file = getFileObjectFromKettleVFS( filename );

      // If we have a shared file, load the content, otherwise, just keep this one empty
      if ( file.exists() ) {
        Document document = XMLHandler.loadXMLFile( file );
        Node sharedObjectsNode = XMLHandler.getSubNode( document, XML_TAG );
        if ( sharedObjectsNode != null ) {
          List<SlaveServer> privateSlaveServers = new ArrayList<SlaveServer>();
          List<DatabaseMeta> privateDatabases = new ArrayList<DatabaseMeta>();

          NodeList childNodes = sharedObjectsNode.getChildNodes();
          // First load databases & slaves
          //
          for ( int i = 0; i < childNodes.getLength(); i++ ) {
            Node node = childNodes.item( i );
            String nodeName = node.getNodeName();

            SharedObjectInterface isShared = null;

            if ( nodeName.equals( DatabaseMeta.XML_TAG ) ) {
              try {
                DatabaseMeta sharedDatabaseMeta = new DatabaseMeta( node );
                isShared = sharedDatabaseMeta;
                privateDatabases.add( sharedDatabaseMeta );
              } catch ( KettleXMLException kxe ) {
                // If this is caused because we can't find the database plugin, just log and keep going.
                // The KettleDatabaseException is doubly-wrapped in KettleXMLExceptions, so try to unravel
                Throwable firstCause = kxe.getCause();
                if ( firstCause != null ) {
                  Throwable secondCause = firstCause.getCause();

                  if ( secondCause == null || !( secondCause instanceof KettleDatabaseException ) ) {
                    throw kxe;
                  } else {
                    log.logBasic( kxe.getLocalizedMessage() );
                  }
                } else {
                  throw kxe;
                }
              }
            } else if ( nodeName.equals( SlaveServer.XML_TAG ) ) {
              SlaveServer sharedSlaveServer = new SlaveServer( node );
              isShared = sharedSlaveServer;
              privateSlaveServers.add( sharedSlaveServer );
            }

            if ( isShared != null ) {
              isShared.setShared( true );
              storeObject( isShared );
            }
          }

          // Then load the other objects that might reference databases & slaves
          //
          for ( int i = 0; i < childNodes.getLength(); i++ ) {
            Node node = childNodes.item( i );
            String nodeName = node.getNodeName();

            SharedObjectInterface isShared = null;

            if ( nodeName.equals( StepMeta.XML_TAG ) ) {
              StepMeta stepMeta = new StepMeta( node, privateDatabases, (IMetaStore) null );
              stepMeta.setDraw( false ); // don't draw it, keep it in the tree.
              isShared = stepMeta;
            } else if ( nodeName.equals( PartitionSchema.XML_TAG ) ) {
              isShared = new PartitionSchema( node );
            } else if ( nodeName.equals( ClusterSchema.XML_TAG ) ) {
              isShared = new ClusterSchema( node, privateSlaveServers );
            }

            if ( isShared != null ) {
              isShared.setShared( true );
              storeObject( isShared );
            }
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "SharedOjects.Readingfile.UnexpectedError",
        sharedObjectsFile ), e );
    }
  }

  public static final String createFilename( String sharedObjectsFile ) {
    String filename;
    if ( Utils.isEmpty( sharedObjectsFile ) ) {
      // First fallback is the environment/kettle variable ${KETTLE_SHARED_OBJECTS}
      // This points to the file
      filename = Variables.getADefaultVariableSpace().getVariable( Const.KETTLE_SHARED_OBJECTS );

      // Last line of defence...
      if ( Utils.isEmpty( filename ) ) {
        filename = Const.getSharedObjectsFile();
      }
    } else {
      filename = sharedObjectsFile;
    }
    return filename;
  }

  public SharedObjects() throws KettleXMLException {
    this( null );
  }

  public Map<SharedEntry, SharedObjectInterface> getObjectsMap() {
    return objectsMap;
  }

  public void setObjectsMap( Map<SharedEntry, SharedObjectInterface> objects ) {
    this.objectsMap = objects;
  }

  /**
   * Store the sharedObject in the object map. It is possible to have 2 different types of shared object with the same
   * name. They will be stored separately.
   *
   * @param sharedObject
   */
  public void storeObject( SharedObjectInterface sharedObject ) {
    SharedEntry key = new SharedEntry( sharedObject.getClass().getName(), sharedObject.getName() );
    objectsMap.put( key, sharedObject );
  }

  /**
   * Remove the sharedObject from the object map.
   *
   * @param sharedObject
   */
  public void removeObject( SharedObjectInterface sharedObject ) {
    SharedEntry key = new SharedEntry( sharedObject.getClass().getName(), sharedObject.getName() );
    objectsMap.remove( key );
  }

  public void saveToFile() throws IOException, KettleException {
    FileObject fileObject = getFileObjectFromKettleVFS( filename );
    String backupFileName = createOrGetFileBackup( fileObject );
    writeToFile( fileObject, backupFileName );
  }

  /**
   * Return the shared object with the given class and name
   *
   * @param clazz      The class of the shared object
   * @param objectName the name of the object
   * @return The shared object or null if none was found.
   */
  public SharedObjectInterface getSharedObject( Class<SharedObjectInterface> clazz, String objectName ) {
    return getSharedObject( clazz.getName(), objectName );
  }

  /**
   * Return the shared object with the given class name and object name
   *
   * @param className  The class name of the shared object
   * @param objectName the name of the object
   * @return The shared object or null if none was found.
   */
  public SharedObjectInterface getSharedObject( String className, String objectName ) {
    SharedEntry entry = new SharedEntry( className, objectName );
    return objectsMap.get( entry );
  }

  /**
   * Get the shared database with the specified name
   *
   * @param name The name of the shared database
   * @return The database or null if nothing was found.
   */
  public DatabaseMeta getSharedDatabase( String name ) {
    return (DatabaseMeta) getSharedObject( DatabaseMeta.class.getName(), name );
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * Write sharedObjects to file. In case of an exception are caught save backup file instead of new one.
   *
   * @param fileObject     is file for writing
   * @param backupFileName is backup file name
   * @throws IOException
   * @throws KettleException
   */
  @VisibleForTesting
  protected void writeToFile( FileObject fileObject, String backupFileName ) throws IOException, KettleException {
    OutputStream outputStream = null;
    PrintStream out = null;
    try {
      outputStream = initOutputStreamUsingKettleVFS( fileObject );

      out = new PrintStream( outputStream );

      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
      out.println( "<" + XML_TAG + ">" );

      Collection<SharedObjectInterface> collection = objectsMap.values();
      for ( SharedObjectInterface sharedObject : collection ) {
        String xmlContent = sharedObject.getXML();
        out.println( xmlContent );
      }
      out.println( "</" + XML_TAG + ">" );
    } catch ( Exception e ) {
      // restore file if something wrong
      boolean isRestored = false;
      if ( backupFileName != null ) {
        restoreFileFromBackup( backupFileName );
        isRestored = true;
      }
      throw new KettleException(
        BaseMessages.getString( PKG, "SharedOjects.WriteToFile.ErrorWritingFile", isRestored ), e );
    } finally {
      if ( out != null ) {
        out.flush();
      }
      if ( out != null ) {
        out.close();
      }
      if ( out != null ) {
        outputStream.close();
      }
    }
  }

  private FileObject getFileObjectFromKettleVFS( String filename ) throws KettleFileException {
    return KettleVFS.getFileObject( filename );
  }

  @VisibleForTesting
  protected OutputStream initOutputStreamUsingKettleVFS( FileObject fileObject ) throws IOException {
    return KettleVFS.getOutputStream( fileObject, false );
  }

  /**
   * Call {@link #copyFile(String, String)} method to restore file from backup
   *
   * @param backupFileName
   * @throws IOException
   */
  @VisibleForTesting
  protected void restoreFileFromBackup( String backupFileName ) throws IOException, KettleFileException {
    copyFile( backupFileName, filename );
  }

  /**
   * Call {@link #copyFile(String, String)} method to create file backup
   *
   * @param fileObject
   * @throws IOException
   */
  private String createOrGetFileBackup( FileObject fileObject ) throws IOException, KettleException {
    String backupFileName = filename + ".backup";
    boolean isBackupFileExist = false;
    if ( fileObject.exists() ) {
      isBackupFileExist = createFileBackup( backupFileName );
    } else {
      isBackupFileExist = getBackupFileFromFileSystem( backupFileName );
    }
    return isBackupFileExist ? backupFileName : null;
  }

  private boolean getBackupFileFromFileSystem( String backupFileName ) throws KettleException {
    FileObject fileObject = getFileObjectFromKettleVFS( backupFileName );
    try {
      return fileObject.exists();
    } catch ( FileSystemException e ) {
      return false;
    }
  }

  private boolean createFileBackup( String backupFileName ) throws IOException, KettleFileException {
    return copyFile( filename, backupFileName );
  }

  private boolean copyFile( String src, String dest ) throws KettleFileException, IOException {
    FileObject srcFile = getFileObjectFromKettleVFS( src );
    FileObject destFile = getFileObjectFromKettleVFS( dest );
    try ( InputStream in = KettleVFS.getInputStream( srcFile );
        OutputStream out = KettleVFS.getOutputStream( destFile, false ) ) {
      IOUtils.copy( in, out );
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ( !Utils.isEmpty( getFilename() ) ? " (" + getFilename() + ")" : "" );
  }
}
