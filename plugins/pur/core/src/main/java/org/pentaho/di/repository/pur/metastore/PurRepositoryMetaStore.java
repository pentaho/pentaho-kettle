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
package org.pentaho.di.repository.pur.metastore;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Please note that for this class to work, the supplied PurRepository needs to be connected to the server.
 * 
 * @author matt
 */
public class PurRepositoryMetaStore extends MemoryMetaStore implements IMetaStore {

  public static final String ELEMENT_TYPE_DETAILS_FILENAME = "ElementTypeDetails";

  protected static final String PROP_NAME = "NAME";
  protected static final String PROP_ELEMENT_TYPE_NAME = "element_type_name";
  protected static final String PROP_ELEMENT_CHILDREN = "element_children";

  protected static final String PROP_ELEMENT_TYPE_DESCRIPTION = "element_type_description"; //$NON-NLS-1$

  protected static final String METASTORE_FOLDER_PATH = "/etc/metastore";

  protected PurRepository repository;
  protected IUnifiedRepository pur;

  /**
   * This is the folder where the namespaces folders are stored
   */
  protected RepositoryFile namespacesFolder;

  public PurRepositoryMetaStore( PurRepository repository ) throws KettleException {
    this.repository = repository;
    this.pur = repository.getUnderlyingRepository();

    namespacesFolder = pur.getFile( METASTORE_FOLDER_PATH );
    if ( namespacesFolder == null ) {
      throw new KettleException( METASTORE_FOLDER_PATH + " folder is not available" );
    }
  }

  @Override
  public String getName() {
    return repository.getRepositoryMeta().getName();
  }

  @Override
  public String getDescription() {
    return repository.getRepositoryMeta().getDescription();
  }

  // The namespaces

  @Override
  public void createNamespace( String namespace ) throws MetaStoreException {
    if ( namespaceExists( namespace ) ) {
      throw new MetaStoreNamespaceExistsException( "Namespace '" + namespace
          + "' can not be created, it already exists" );
    }
    RepositoryFile namespaceFolder = new RepositoryFile.Builder( namespace ).folder( true ).versioned( false ).build();

    pur.createFolder( namespacesFolder.getId(), namespaceFolder, "Created namespace" );
  }

  @Override
  public boolean namespaceExists( String namespace ) throws MetaStoreException {
    return getNamespaceRepositoryFile( namespace ) != null;
  }

  @Override
  public void deleteNamespace( String namespace ) throws MetaStoreException {
    RepositoryFile namespaceFile = getNamespaceRepositoryFile( namespace );
    if ( namespaceFile == null ) {
      return; // already gone.
    }
    List<RepositoryFile> children = getChildren( namespaceFile.getId() );
    if ( children == null || children.isEmpty() ) {
      // Delete the file, there are no children.
      //
      pur.deleteFile( namespaceFile.getId(), true, "Delete namespace" );
    } else {
      // Dependencies exists, throw an exception.
      //
      List<String> elementTypeIds = new ArrayList<String>();
      for ( RepositoryFile child : children ) {
        elementTypeIds.add( child.getId().toString() );
      }
      throw new MetaStoreDependenciesExistsException( elementTypeIds, "Namespace '" + namespace
          + " can not be deleted because it is not empty" );
    }
  }

  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    List<String> namespaces = new ArrayList<String>();
    List<RepositoryFile> children = getChildren( namespacesFolder.getId() );
    for ( RepositoryFile child : children ) {
      namespaces.add( child.getName() );
    }
    return namespaces;
  }

  // The element types

  @Override
  public void createElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {

    RepositoryFile namespaceFile = validateNamespace( namespace );

    IMetaStoreElementType existingType = getElementTypeByName( namespace, elementType.getName() );
    if ( existingType != null ) {
      throw new MetaStoreElementTypeExistsException( Collections.singletonList( existingType ),
          "Can not create element type with id '" + elementType.getId() + "' because it already exists" );
    }

    RepositoryFile elementTypeFile =
        new RepositoryFile.Builder( elementType.getName() ).folder( true ).versioned( false ).build();

    RepositoryFile folder = pur.createFolder( namespaceFile.getId(), elementTypeFile, null );
    elementType.setId( folder.getId().toString() );

    // In this folder there is a hidden file which contains the description
    // and the other future properties of the element type
    //
    RepositoryFile detailsFile =
        new RepositoryFile.Builder( ELEMENT_TYPE_DETAILS_FILENAME ).folder( false ).title(
            ELEMENT_TYPE_DETAILS_FILENAME ).description( elementType.getDescription() ).hidden( true ).build();

    DataNode dataNode = getElementTypeDataNode( elementType );

    pur.createFile( folder.getId(), detailsFile, new NodeRepositoryFileData( dataNode ), null );

    elementType.setMetaStoreName( getName() );
  }

  @Override
  public synchronized void updateElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    RepositoryFile folder = getElementTypeRepositoryFolder( namespace, elementType );

    RepositoryFile detailsFile = findChildByName( folder.getId(), ELEMENT_TYPE_DETAILS_FILENAME, true );

    DataNode dataNode = getElementTypeDataNode( elementType );

    pur.updateFile( detailsFile, new NodeRepositoryFileData( dataNode ), null );

    elementType.setMetaStoreName( getName() );
  }

  private DataNode getElementTypeDataNode( IMetaStoreElementType elementType ) {
    DataNode dataNode = new DataNode( ELEMENT_TYPE_DETAILS_FILENAME );
    dataNode.setProperty( PROP_ELEMENT_TYPE_DESCRIPTION, elementType.getDescription() );
    dataNode.setProperty( PROP_ELEMENT_TYPE_NAME, elementType.getName() );
    dataNode.setProperty( PROP_NAME, elementType.getName() );
    return dataNode;
  }

  @Override
  public IMetaStoreElementType getElementType( String namespace, String elementTypeId ) throws MetaStoreException {
    RepositoryFile elementTypeFolder = pur.getFileById( elementTypeId );
    if ( elementTypeFolder == null ) {
      return null;
    }
    IMetaStoreElementType elementType = newElementType( namespace );
    elementType.setId( elementTypeFolder.getId().toString() );
    elementType.setName( elementTypeFolder.getName() );

    RepositoryFile detailsFile = findChildByName( elementTypeFolder.getId(), ELEMENT_TYPE_DETAILS_FILENAME, true );
    if ( detailsFile != null ) {
      NodeRepositoryFileData data = pur.getDataForRead( detailsFile.getId(), NodeRepositoryFileData.class );
      DataProperty property = data.getNode().getProperty( "element_type_description" );
      if ( property != null ) {
        elementType.setDescription( property.getString() );
      }
    }
    return elementType;
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    List<IMetaStoreElementType> elementTypes = new ArrayList<IMetaStoreElementType>();

    RepositoryFile namespaceFile = validateNamespace( namespace );
    List<RepositoryFile> children = getChildren( namespaceFile.getId() );
    for ( RepositoryFile child : children ) {
      if ( !child.isHidden() ) {
        elementTypes.add( getElementType( namespace, child.getId().toString() ) );
      }
    }

    return elementTypes;
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName )
    throws MetaStoreException {
    RepositoryFile file = getElementTypeRepositoryFileByName( namespace, elementTypeName );
    if ( file == null ) {
      return null;
    }

    return getElementType( namespace, file.getId().toString() );
  }

  @Override
  public List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    List<String> ids = new ArrayList<String>();

    for ( IMetaStoreElementType type : getElementTypes( namespace ) ) {
      ids.add( type.getId() );
    }

    return ids;
  }

  @Override
  public void deleteElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {

    RepositoryFile namespaceRepositoryFile = validateNamespace( namespace );

    RepositoryFile elementTypeFile = findChildByName( namespaceRepositoryFile.getId(), elementType.getName() );
    List<RepositoryFile> children = getChildren( elementTypeFile.getId() );
    removeHiddenFilesFromList( children );

    if ( children.isEmpty() ) {
      pur.deleteFile( elementTypeFile.getId(), true, null );
    } else {
      List<String> ids = getElementIds( namespace, elementType );
      throw new MetaStoreDependenciesExistsException( ids, "Can't delete element type with name '"
          + elementType.getName() + "' because it is not empty" );
    }
  }

  protected void removeHiddenFilesFromList( List<RepositoryFile> children ) {

    for ( Iterator<RepositoryFile> it = children.iterator(); it.hasNext(); ) {
      RepositoryFile child = it.next();
      if ( child.isHidden() ) {
        it.remove();
      }
    }

  }

  // The elements

  public void createElement( String namespace, IMetaStoreElementType elementType, IMetaStoreElement element )
    throws MetaStoreException {
    RepositoryFile elementTypeFolder = validateElementTypeRepositoryFolder( namespace, elementType );

    RepositoryFile elementFile =
        new RepositoryFile.Builder( PurRepository.checkAndSanitize( element.getName() ) ).title( element.getName() )
            .versioned( false ).build();

    DataNode elementDataNode = new DataNode( PurRepository.checkAndSanitize( element.getName() ) );
    elementToDataNode( element, elementDataNode );

    RepositoryFile createdFile =
        pur.createFile( elementTypeFolder.getId(), elementFile, new NodeRepositoryFileData( elementDataNode ), null );
    element.setId( createdFile.getId().toString() );

    // Verify existence.
    if ( pur.getFileById( createdFile.getId() ) == null ) {
      throw new RuntimeException( "Unable to verify creation of element '" + element.getName() + "' in folder: "
          + elementTypeFolder.getPath() );
    }
  }

  protected void elementToDataNode( IMetaStoreElement element, DataNode elementDataNode ) {
    elementDataNode.setProperty( PROP_NAME, element.getName() );
    DataNode childrenNode = elementDataNode.addNode( PROP_ELEMENT_CHILDREN );
    if ( Utils.isEmpty( element.getId() ) ) {
      element.setId( element.getName() );
    }
    attributeToDataNode( element, childrenNode );
  }

  protected void dataNodeToElement( DataNode dataNode, IMetaStoreElement element ) throws MetaStoreException {
    DataProperty nameProperty = dataNode.getProperty( PROP_NAME );
    if ( nameProperty != null ) {
      element.setName( nameProperty.getString() );
    }
    DataNode childrenNode = dataNode.getNode( PROP_ELEMENT_CHILDREN );
    dataNodeToAttribute( childrenNode, element );
  }

  @Override
  public synchronized void updateElement( String namespace, IMetaStoreElementType elementType, String elementId,
      IMetaStoreElement element ) throws MetaStoreException {

    // verify that the element type belongs to this meta store
    //
    if ( elementType.getMetaStoreName() == null || !elementType.getName().equals( getName() ) ) {
      String elementTypeName = elementType.getName();
      elementType = getElementTypeByName( namespace, elementTypeName );
      if ( elementType == null ) {
        throw new MetaStoreException( "The element type '" + elementTypeName
            + "' could not be found in the meta store in which you are updating." );
      }
    }

    RepositoryFile existingFile = pur.getFileById( elementId );
    if ( existingFile == null ) {
      throw new MetaStoreException( "The element to update with id " + elementId + " could not be found in the store" );
    }

    DataNode elementDataNode = new DataNode( PurRepository.checkAndSanitize( element.getName() ) );
    elementToDataNode( element, elementDataNode );

    RepositoryFile updatedFile = pur.updateFile( existingFile, new NodeRepositoryFileData( elementDataNode ), null );
    element.setId( updatedFile.getId().toString() );
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    NodeRepositoryFileData data = pur.getDataForRead( elementId, NodeRepositoryFileData.class );
    if ( data == null ) {
      return null;
    }

    IMetaStoreElement element = newElement();
    element.setId( elementId );
    element.setElementType( elementType );
    DataNode dataNode = data.getNode();
    dataNodeToElement( dataNode, element );

    return element;
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    return getElements( namespace, elementType, true, null );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType, boolean lock,
                                              List<MetaStoreException> exceptionList ) throws MetaStoreException {
    List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();

    RepositoryFile typeFolder = validateElementTypeRepositoryFolder( namespace, elementType );
    List<RepositoryFile> children = getChildren( typeFolder.getId() );
    removeHiddenFilesFromList( children );
    for ( RepositoryFile child : children ) {
      try {
        IMetaStoreElement element = getElement( namespace, elementType, child.getId().toString() );
        elements.add( element );
      } catch ( Exception e ) {
        // If we are collecting exceptions instead of fatally exiting, add to the list of exceptions and continue
        if ( exceptionList != null ) {
          exceptionList.add( new MetaStoreException( "Could not load metaStore element '" + child.getId().toString()
            + "'", e ) );
        } else {
          // Strict run. abort list
          throw e;
        }
      }
    }

    return elements;
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    for ( IMetaStoreElement element : getElements( namespace, elementType, true,
      new ArrayList<MetaStoreException>() ) ) {
      if ( element.getName().equals( name ) ) {
        return element;
      }
    }
    return null;
  }

  @Override
  public List<String> getElementIds( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    RepositoryFile folder = validateElementTypeRepositoryFolder( namespace, elementType );
    List<RepositoryFile> children = getChildren( folder.getId() );
    removeHiddenFilesFromList( children );
    List<String> ids = new ArrayList<String>();
    for ( RepositoryFile child : children ) {
      ids.add( child.getId().toString() );
    }
    return ids;
  }

  @Override
  public void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {

    pur.deleteFile( elementId, true, null );

  }

  protected void attributeToDataNode( IMetaStoreAttribute attribute, DataNode dataNode ) {
    String id = attribute.getId();
    Object value = attribute.getValue();
    if ( id == null ) {
      throw new NullPointerException();
    } else if ( value != null ) {
      if ( value instanceof Double ) {
        dataNode.setProperty( id, (Double) value );
      } else if ( value instanceof Date ) {
        dataNode.setProperty( id, (Date) value );
      } else if ( value instanceof Long ) {
        dataNode.setProperty( id, (Long) value );
      } else {
        dataNode.setProperty( id, value.toString() );
      }
    }

    for ( IMetaStoreAttribute child : attribute.getChildren() ) {
      DataNode subNode = new DataNode( child.getId() );
      attributeToDataNode( child, subNode );
      dataNode.addNode( subNode );
    }
  }

  protected void dataNodeToAttribute( DataNode dataNode, IMetaStoreAttribute attribute ) throws MetaStoreException {
    for ( DataProperty dataProperty : dataNode.getProperties() ) {
      Object value;
      switch ( dataProperty.getType() ) {
        case DATE:
          value = ( dataProperty.getDate() );
          break;
        case DOUBLE:
          value = ( dataProperty.getDouble() );
          break;
        case LONG:
          value = ( dataProperty.getLong() );
          break;
        case STRING:
          value = ( dataProperty.getString() );
          break;
        default:
          continue;
      }

      // Backwards Compatibility
      if ( dataProperty.getName().equals( dataNode.getName() ) ) {
        attribute.setValue( value );
      }
      attribute.addChild( newAttribute( dataProperty.getName(), value ) );
    }

    for ( DataNode subNode : dataNode.getNodes() ) {
      IMetaStoreAttribute subAttr = newAttribute( subNode.getName(), null );
      dataNodeToAttribute( subNode, subAttr );
      attribute.addChild( subAttr );
    }
  }

  protected RepositoryFile validateNamespace( String namespace ) throws MetaStoreException {
    RepositoryFile namespaceFile = getNamespaceRepositoryFile( namespace );
    if ( namespaceFile == null ) {
      throw new MetaStoreException( "Namespace '" + namespace + " doesn't exist in the repository" );
    }
    return namespaceFile;
  }

  protected RepositoryFile validateElementTypeRepositoryFolder( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    // The element type needs to be known in this repository, we need to have a match by ID
    //
    RepositoryFile elementTypeFolder = pur.getFileById( elementType.getId() );
    if ( elementTypeFolder == null ) {
      StringBuilder builder = new StringBuilder();
      builder.append( namespacesFolder.getPath() ).append( Const.CR );
      String available = getMetaStoreFolders( builder, namespacesFolder, 0 );
      throw new MetaStoreException( "The element type with name '" + elementType.getName()
          + " doesn't exist in namespace '" + namespace + "'." + Const.CR + "Available nodes:" + Const.CR + available );
    }
    return elementTypeFolder;
  }

  private String getMetaStoreFolders( StringBuilder builder, RepositoryFile folder, int level ) {
    String spaces = Const.rightPad( " ", level * 2 );
    builder.append( spaces );
    if ( folder.isFolder() ) {
      builder.append( "/" );
    }
    builder.append( folder.getName() ).append( Const.CR );
    for ( RepositoryFile file : getChildren( folder.getId() ) ) {
      getMetaStoreFolders( builder, file, level + 1 );
    }
    return builder.toString();
  }

  protected RepositoryFile getNamespaceRepositoryFile( String namespace ) {
    return findChildByName( namespacesFolder.getId(), namespace );
  }

  protected RepositoryFile getElementTypeRepositoryFolder( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    RepositoryFile namespaceRepositoryFile = validateNamespace( namespace );
    return findChildByName( namespaceRepositoryFile.getId(), elementType.getName() );
  }

  protected RepositoryFile getElementTypeRepositoryFileByName( String namespace, String elementTypeName ) {
    RepositoryFile namespaceFolder = getNamespaceRepositoryFile( namespace );
    if ( namespace == null ) {
      return null;
    }
    return findChildByName( namespaceFolder.getId(), elementTypeName );
  }

  protected RepositoryFile findChildByName( Serializable folderId, String childName, boolean showHiddenFiles ) {
    for ( RepositoryFile child : getChildren( folderId, showHiddenFiles ) ) {
      if ( child.getName().equals( childName ) ) {
        return child;
      }
    }
    return null;
  }

  protected RepositoryFile findChildByName( Serializable folderId, String childName ) {
    return findChildByName( folderId, childName, false );
  }

  protected RepositoryFileAcl getAcls() {
    return null; // new RepositoryFileAcl.Builder(RepositoryFileAcl.Builder).entriesInheriting(true).build()
  }


  @Override
  public IMetaStoreElementType newElementType( String namespace ) throws MetaStoreException {
    IMetaStoreElementType elementType = super.newElementType( namespace );
    elementType.setMetaStoreName( getName() );
    return elementType;
  }

  private List<RepositoryFile> getChildren( Serializable folderId ) {
    return getChildren( folderId, false );
  }

  private List<RepositoryFile> getChildren( Serializable folderId, boolean showHiddenFiles ) {
    return pur.getChildren( new RepositoryRequest( folderId.toString(), showHiddenFiles, -1, null ) );
  }

}
