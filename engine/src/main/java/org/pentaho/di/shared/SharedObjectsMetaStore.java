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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.metastore.util.PentahoDefaults;

/*
 * unused?
 */
@Deprecated
public class SharedObjectsMetaStore extends MemoryMetaStore implements IMetaStore {

  protected IMetaStoreElementType databaseElementType;

  protected SharedObjects sharedObjects;

  public SharedObjectsMetaStore( SharedObjects sharedObjects ) throws MetaStoreException {
    this.sharedObjects = sharedObjects;

    this.databaseElementType = DatabaseMetaStoreUtil.populateDatabaseElementType( this );
  }

  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    return Arrays.asList( PentahoDefaults.NAMESPACE );
  }

  @Override
  public void createNamespace( String namespace ) throws MetaStoreException, MetaStoreNamespaceExistsException {
    throw new MetaStoreException( "The shared objects metadata store doesn't support creating namespaces" );
  }

  @Override
  public void deleteNamespace( String namespace ) throws MetaStoreException, MetaStoreDependenciesExistsException {
    throw new MetaStoreException( "The shared objects metadata store doesn't support deleting namespaces" );
  }

  @Override
  public boolean namespaceExists( String namespace ) throws MetaStoreException {
    return getNamespaces().indexOf( namespace ) >= 0;
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    return Arrays.asList( databaseElementType );
  }

  @Override
  public List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    return Arrays.asList( databaseElementType.getId() );
  }

  @Override
  public IMetaStoreElementType getElementType( String namespace, String elementTypeId ) throws MetaStoreException {
    if ( elementTypeId.equals( databaseElementType.getId() ) ) {
      return databaseElementType;
    }
    return null;
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName ) throws MetaStoreException {
    for ( IMetaStoreElementType elementType : getElementTypes( namespace ) ) {
      if ( elementType.getName() != null && elementType.getName().equalsIgnoreCase( elementTypeName ) ) {
        return elementType;
      }
    }
    return null;
  }

  @Override
  public void createElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreElementTypeExistsException {
    throw new MetaStoreException( "The shared objects metadata store doesn't support creating new element types" );
  }

  @Override
  public void updateElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    throw new MetaStoreException( "The shared objects metadata store doesn't support updating element types" );
  }

  @Override
  public void deleteElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreDependenciesExistsException {
    throw new MetaStoreException( "The shared objects metadata store doesn't support deleting element types" );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    List<IMetaStoreElement> list = new ArrayList<IMetaStoreElement>();
    for ( SharedObjectInterface sharedObject : sharedObjects.getObjectsMap().values() ) {
      // The databases...
      //
      if ( sharedObject instanceof DatabaseMeta && databaseElementType.getName().equals( elementType.getName() ) ) {
        list.add( DatabaseMetaStoreUtil.populateDatabaseElement( this, (DatabaseMeta) sharedObject ) );
      }
    }
    return list;
  }

  @Override
  public List<String> getElementIds( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    List<String> ids = new ArrayList<String>();
    for ( IMetaStoreElement element : getElements( namespace, elementType ) ) {
      ids.add( element.getId() );
    }
    return ids;
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId ) throws MetaStoreException {
    for ( IMetaStoreElement element : getElements( namespace, elementType ) ) {
      if ( element.getId().equals( elementId ) ) {
        return element;
      }
    }
    return null;
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name ) throws MetaStoreException {
    for ( IMetaStoreElement element : getElements( namespace, elementType ) ) {
      if ( ( element.getName().equalsIgnoreCase( name ) ) ) {
        return element;
      }
    }
    return null;
  }

  @Override
  public void createElement( String namespace, IMetaStoreElementType elementType, IMetaStoreElement element ) throws MetaStoreException, MetaStoreElementExistException {
    try {
      IMetaStoreElement exists = getElementByName( namespace, elementType, element.getId() );
      if ( exists != null ) {
        throw new MetaStoreException( "The shared objects meta store already contains an element with type name '"
          + elementType.getName() + "' and element name '" + element.getName() );
      }

      if ( elementType.getName().equals( databaseElementType.getName() ) ) {
        // convert the element to DatabaseMeta and store it in the shared objects file, then save the file
        //
        sharedObjects.storeObject( DatabaseMetaStoreUtil.loadDatabaseMetaFromDatabaseElement( this, element ) );
        sharedObjects.saveToFile();
        return;
      }
      throw new MetaStoreException( "Storing elements with element type name '"
        + elementType.getName() + "' is not supported in the shared objects meta store" );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unexpected error creating an element in the shared objects meta store", e );
    }
  }

  @Override
  public void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId ) throws MetaStoreException {
    try {
      if ( elementType.getName().equals( databaseElementType.getName() ) ) {
        sharedObjects.removeObject( DatabaseMetaStoreUtil.loadDatabaseMetaFromDatabaseElement( this, getElement(
          namespace, elementType, elementId ) ) );
        sharedObjects.saveToFile();
        return;
      }
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unexpected error deleting an element in the shared objects meta store", e );
    }
  }

  public SharedObjects getSharedObjects() {
    return sharedObjects;
  }

  public void setSharedObjects( SharedObjects sharedObjects ) {
    this.sharedObjects = sharedObjects;
  }

}
