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

package org.pentaho.di.repository.kdr.delegates.metastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryMetaStoreDelegate;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStoreElementOwner;

public class KettleDatabaseRepositoryMetaStore extends MemoryMetaStore implements IMetaStore {

  protected KettleDatabaseRepository repository;
  private KettleDatabaseRepositoryMetaStoreDelegate delegate;

  public KettleDatabaseRepositoryMetaStore( KettleDatabaseRepository repository ) {
    this.repository = repository;
    delegate = repository.metaStoreDelegate;
  }

  // Handle namespaces...

  @Override
  public List<String> getNamespaces() throws MetaStoreException {

    try {
      List<String> namespaces = new ArrayList<String>();
      Collection<RowMetaAndData> namespaceRows = delegate.getNamespaces();
      for ( RowMetaAndData namespaceRow : namespaceRows ) {
        String namespace = namespaceRow.getString( KettleDatabaseRepository.FIELD_NAMESPACE_NAME, null );
        if ( !Utils.isEmpty( namespace ) ) {
          namespaces.add( namespace );
        }
      }
      return namespaces;

    } catch ( Exception e ) {
      throw new MetaStoreException( e );
    }
  }

  @Override
  public void createNamespace( String namespace ) throws MetaStoreException, MetaStoreNamespaceExistsException {
    try {
      ObjectId namespaceId = delegate.getNamespaceId( namespace );
      if ( namespaceId != null ) {
        throw new MetaStoreNamespaceExistsException( "Namespace with name '" + namespace + "' already exists" );
      }

      // insert namespace into R_NAMESPACE
      //
      delegate.insertNamespace( namespace );
      repository.commit();
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( e );
    }
  }

  @Override
  public void deleteNamespace( String namespace ) throws MetaStoreException, MetaStoreDependenciesExistsException {
    try {
      ObjectId namespaceId = delegate.verifyNamespace( namespace );

      List<IMetaStoreElementType> elementTypes = getElementTypes( namespace );
      if ( !elementTypes.isEmpty() ) {

        List<String> dependencies = new ArrayList<String>();
        for ( IMetaStoreElementType elementType : elementTypes ) {
          dependencies.add( elementType.getId() );
        }
        throw new MetaStoreDependenciesExistsException( dependencies, "The namespace to delete, '"
          + namespace + "' is not empty" );
      }

      // Now delete the namespace
      //
      delegate.deleteNamespace( namespaceId );
      repository.commit();
    } catch ( MetaStoreDependenciesExistsException e ) {
      throw e;
    } catch ( MetaStoreException e ) {
      repository.rollback();
      throw e;
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( "Unable to delete namespace '" + namespace + "'", e );
    }
  }

  @Override
  public boolean namespaceExists( String namespace ) throws MetaStoreException {
    try {
      return delegate.getNamespaceId( namespace ) != null;
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to verify if namespace '" + namespace + "' exists.", e );
    }
  }

  // Handle the element types

  public void createElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreElementTypeExistsException {
    try {

      ObjectId namespaceId = delegate.verifyNamespace( namespace );

      // See if the element already exists in this namespace
      //
      IMetaStoreElementType existingType = getElementTypeByName( namespace, elementType.getName() );
      if ( existingType != null ) {
        throw new MetaStoreElementTypeExistsException(
          Arrays.asList( existingType ), "Can not create element type with id '"
            + elementType.getId() + "' because it already exists" );
      }

      KDBRMetaStoreElementType newElementType =
        new KDBRMetaStoreElementType( delegate, namespace, namespaceId, elementType.getName(), elementType
          .getDescription() );
      newElementType.save();
      elementType.setId( newElementType.getId() );
      repository.commit();
    } catch ( MetaStoreElementTypeExistsException e ) {
      throw e;
    } catch ( MetaStoreException e ) {
      repository.rollback();
      throw e;
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( e );
    }
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    try {
      LongObjectId namespaceId = delegate.getNamespaceId( namespace );
      if ( namespaceId == null ) {
        return new ArrayList<IMetaStoreElementType>();
      }

      Collection<RowMetaAndData> elementTypeRows = delegate.getElementTypes( namespaceId );

      List<IMetaStoreElementType> list = new ArrayList<IMetaStoreElementType>();
      for ( RowMetaAndData elementTypeRow : elementTypeRows ) {
        KDBRMetaStoreElementType elementType = delegate.parseElementType( namespace, namespaceId, elementTypeRow );
        list.add( elementType );
      }

      return list;
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get list of element types for namespace '" + namespace + "'", e );
    }

  }

  @Override
  public List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    List<IMetaStoreElementType> elementTypes = getElementTypes( namespace );
    ArrayList<String> ids = new ArrayList<String>();
    for ( IMetaStoreElementType elementType : elementTypes ) {
      ids.add( elementType.getId() );
    }
    return ids;
  }

  @Override
  public IMetaStoreElementType getElementType( String namespace, String elementTypeId ) throws MetaStoreException {
    try {

      ObjectId namespaceId = delegate.getNamespaceId( namespace );
      if ( namespaceId == null ) {
        return null;
      }

      RowMetaAndData elementTypeRow =
        delegate.getElementType( new LongObjectId( new StringObjectId( elementTypeId ) ) );

      return delegate.parseElementType( namespace, namespaceId, elementTypeRow );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get element type with id '"
        + elementTypeId + "' in namespace '" + namespace + "'", e );
    }
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName ) throws MetaStoreException {
    try {
      LongObjectId namespaceId = delegate.getNamespaceId( namespace );
      if ( namespaceId == null ) {
        return null;
      }
      LongObjectId elementTypeId = delegate.getElementTypeId( namespaceId, elementTypeName );
      if ( elementTypeId == null ) {
        return null;
      }
      RowMetaAndData elementTypeRow = delegate.getElementType( elementTypeId );

      return delegate.parseElementType( namespace, namespaceId, elementTypeRow );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get element type with name '"
        + elementTypeName + "' in namespace '" + namespace + "'", e );
    }
  }

  @Override
  public void updateElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    try {
      ObjectId namespaceId = delegate.verifyNamespace( namespace );
      String elementTypeId = elementType.getId();
      if ( elementTypeId == null ) {
        IMetaStoreElementType type = getElementTypeByName( namespace, elementType.getName() );
        if ( type != null ) {
          elementTypeId = type.getId();
        }
      }

      if ( elementTypeId != null ) {
        delegate.updateElementType(
          namespaceId, new LongObjectId( new StringObjectId( elementType.getId() ) ), elementType );
        repository.commit();
      } else {
        throw new MetaStoreException( "Unable to update element type: no id was provided and the name '"
          + elementType.getName() + "' didn't match" );
      }
    } catch ( MetaStoreException e ) {
      throw e;
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( "Unable to update element type", e );
    }
  }

  @Override
  public void deleteElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreDependenciesExistsException {
    try {
      Collection<RowMetaAndData> elementTypeRows =
        delegate.getElements( new LongObjectId( new StringObjectId( elementType.getId() ) ) );
      if ( !elementTypeRows.isEmpty() ) {
        List<String> dependencies = new ArrayList<String>();
        for ( RowMetaAndData elementTypeRow : elementTypeRows ) {
          Long elementTypeId =
            elementTypeRow.getInteger( KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_ELEMENT_TYPE );
          dependencies.add( Long.toString( elementTypeId ) );
        }
        throw new MetaStoreDependenciesExistsException( dependencies, "The namespace to delete, '"
          + namespace + "' is not empty" );
      }

      delegate.deleteElementType( new LongObjectId( new StringObjectId( elementType.getId() ) ) );
      repository.commit();
    } catch ( MetaStoreDependenciesExistsException e ) {
      throw e;
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( e );
    }
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    try {
      IMetaStoreElementType type = getElementTypeByName( namespace, elementType.getName() );
      if ( type == null ) {
        return new ArrayList<IMetaStoreElement>();
      }
      Collection<RowMetaAndData> elementRows =
        delegate.getElements( new LongObjectId( new StringObjectId( type.getId() ) ) );
      List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();
      for ( RowMetaAndData elementRow : elementRows ) {
        IMetaStoreElement element = delegate.parseElement( elementType, elementRow );
        elements.add( element );
      }
      return elements;
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get list of elements from namespace '"
        + namespace + "' and for element type '" + elementType.getName() + "'", e );
    }
  }

  @Override
  public List<String> getElementIds( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    List<String> ids = new ArrayList<String>();
    List<IMetaStoreElement> elements = getElements( namespace, elementType );
    for ( IMetaStoreElement element : elements ) {
      ids.add( element.getId() );
    }
    return ids;
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId ) throws MetaStoreException {
    try {
      RowMetaAndData elementRow = delegate.getElement( new LongObjectId( new StringObjectId( elementId ) ) );
      if ( elementRow == null ) {
        return null;
      }
      return delegate.parseElement( elementType, elementRow );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get element", e );
    }
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name ) throws MetaStoreException {
    try {
      LongObjectId namespaceId = delegate.getNamespaceId( namespace );
      if ( namespaceId == null ) {
        return null;
      }
      LongObjectId elementTypeId = delegate.getElementTypeId( namespaceId, elementType.getName() );
      if ( elementTypeId == null ) {
        return null;
      }
      LongObjectId elementId = delegate.getElementId( elementTypeId, name );
      if ( elementId == null ) {
        return null;
      }
      RowMetaAndData elementRow = delegate.getElement( elementId );
      if ( elementRow == null ) {
        return null;
      }

      return delegate.parseElement( elementType, elementRow );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get element by name '"
        + name + "' from namespace '" + namespace + "'", e );
    }
  }

  @Override
  public void createElement( String namespace, IMetaStoreElementType elementType, IMetaStoreElement element ) throws MetaStoreException, MetaStoreElementExistException {
    try {
      IMetaStoreElement found = getElementByName( namespace, elementType, element.getName() );
      if ( found != null ) {
        throw new MetaStoreElementExistException( Arrays.asList( found ), "The element with name '"
          + element.getName() + "' already exists" );
      }

      delegate.insertElement( elementType, element );
      repository.commit();
    } catch ( MetaStoreElementExistException e ) {
      throw e;
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( "Unable to create element with name '"
        + element.getName() + "' of type '" + elementType.getName() + "'", e );
    }
  }

  @Override
  public void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId ) throws MetaStoreException {
    try {
      IMetaStoreElementType type = getElementTypeByName( namespace, elementType.getName() );
      if ( type == null ) {
        throw new MetaStoreException( "Unable to find element type with name '" + elementType.getName() + "'" );
      }

      delegate.deleteElement( new LongObjectId( new StringObjectId( elementId ) ) );
      repository.commit();
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( "Unable to delete element with id '"
        + elementId + "' of type '" + elementType.getName() + "'", e );
    }
  }

  @Override
  public void updateElement( String namespace, IMetaStoreElementType elementType, String elementId,
    IMetaStoreElement element ) throws MetaStoreException {
    try {
      // This is a delete/insert operation
      //
      deleteElement( namespace, elementType, elementId );
      createElement( namespace, elementType, element );
      repository.commit();
    } catch ( Exception e ) {
      repository.rollback();
      throw new MetaStoreException( "Unable to update element with id '"
        + elementId + "' called '" + element.getName() + "' in type '" + elementType.getName() + "'", e );
    }
  }

  @Override
  public IMetaStoreElementType newElementType( String namespace ) throws MetaStoreException {
    return new KDBRMetaStoreElementType( delegate, namespace, null, null, null );
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return new KDBRMetaStoreElement();
  }

  @Override
  public IMetaStoreElement newElement( IMetaStoreElementType elementType, String id, Object value ) throws MetaStoreException {
    return new KDBRMetaStoreElement( delegate, elementType, id, value );
  }

  @Override
  public IMetaStoreAttribute newAttribute( String id, Object value ) throws MetaStoreException {
    return new KDBRMetaStoreAttribute( delegate, id, value );
  }

  @Override
  public IMetaStoreElementOwner newElementOwner( String name, MetaStoreElementOwnerType ownerType ) throws MetaStoreException {
    return new MemoryMetaStoreElementOwner( name, ownerType );
  }

  @Override
  public String getName() {
    return repository.getName();
  }

  @Override
  public String getDescription() {
    return repository.getRepositoryMeta().getDescription();
  }

}
