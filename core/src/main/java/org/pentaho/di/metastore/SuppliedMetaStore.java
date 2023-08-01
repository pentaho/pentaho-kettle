/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.metastore;

import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.ITwoWayPasswordEncoder;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

import java.util.function.Supplier;
import java.util.List;

public class SuppliedMetaStore implements IMetaStore {
  private final Supplier<IMetaStore> metaStoreSupplier;

  public SuppliedMetaStore( Supplier<IMetaStore> metaStoreSupplier ) {
    this.metaStoreSupplier = metaStoreSupplier;
  }

  /**
   * Returns the current metastore as supplied by the Supplier.
   *
   * The object returned by this method should not be held on to permanently.
   *
   *
   * @return IMetaStore The current metastore.
   */
  public IMetaStore getCurrentMetaStore() {
    return metaStoreSupplier.get();
  }

  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    return metaStoreSupplier.get().getNamespaces();
  }

  @Override
  public void createNamespace( String namespace ) throws MetaStoreException, MetaStoreNamespaceExistsException {
    metaStoreSupplier.get().createNamespace( namespace );
  }

  @Override
  public void deleteNamespace( String namespace ) throws MetaStoreException, MetaStoreDependenciesExistsException {
    metaStoreSupplier.get().deleteNamespace( namespace );
  }

  @Override
  public boolean namespaceExists( String namespace ) throws MetaStoreException {
    return metaStoreSupplier.get().namespaceExists( namespace );
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    return metaStoreSupplier.get().getElementTypes( namespace );
  }

  @Override
  public List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    return metaStoreSupplier.get().getElementTypeIds( namespace );
  }

  @Override
  public IMetaStoreElementType getElementType( String namespace, String elementTypeId ) throws MetaStoreException {
    return metaStoreSupplier.get().getElementType( namespace, elementTypeId );
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElementTypeByName( namespace, elementTypeName );
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName, boolean lock )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElementTypeByName( namespace, elementTypeName, lock );
  }

  @Override
  public void createElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException, MetaStoreElementTypeExistsException {
    metaStoreSupplier.get().createElementType( namespace, elementType );
  }

  @Override
  public void updateElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    metaStoreSupplier.get().updateElementType( namespace, elementType );
  }

  @Override
  public void deleteElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException, MetaStoreDependenciesExistsException {
    metaStoreSupplier.get().deleteElementType( namespace, elementType );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElements( namespace, elementType );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType, boolean lock )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElements( namespace, elementType, lock );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType, boolean lock,
      List<MetaStoreException> exceptionList )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElements( namespace, elementType, lock, exceptionList );
  }

  @Override
  public List<String> getElementIds( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    return metaStoreSupplier.get().getElementIds( namespace, elementType );
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElement( namespace, elementType, elementId );
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElementByName( namespace, elementType, name );
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name,
      boolean lock )
    throws MetaStoreException {
    return metaStoreSupplier.get().getElementByName( namespace, elementType, name, lock );
  }

  @Override
  public void createElement( String namespace, IMetaStoreElementType elementType, IMetaStoreElement element )
    throws MetaStoreException, MetaStoreElementExistException {
    metaStoreSupplier.get().createElement( namespace, elementType, element );
  }

  @Override
  public void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    metaStoreSupplier.get().deleteElement( namespace, elementType, elementId );
  }

  @Override
  public void updateElement( String namespace, IMetaStoreElementType elementType, String elementId,
      IMetaStoreElement element )
    throws MetaStoreException {
    metaStoreSupplier.get().updateElement( namespace, elementType, elementId, element );
  }

  @Override
  public IMetaStoreElementType newElementType( String namespace ) throws MetaStoreException {
    return metaStoreSupplier.get().newElementType( namespace );
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return metaStoreSupplier.get().newElement();
  }

  @Override
  public IMetaStoreElement newElement( IMetaStoreElementType elementType, String id, Object value )
    throws MetaStoreException {
    return metaStoreSupplier.get().newElement( elementType, id, value );
  }

  @Override
  public IMetaStoreAttribute newAttribute( String id, Object value ) throws MetaStoreException {
    return metaStoreSupplier.get().newAttribute( id, value );
  }

  @Override
  public IMetaStoreElementOwner newElementOwner( String name, MetaStoreElementOwnerType ownerType )
    throws MetaStoreException {
    return metaStoreSupplier.get().newElementOwner( name, ownerType );
  }

  @Override
  public String getName() throws MetaStoreException {
    return metaStoreSupplier.get().getName();
  }

  @Override
  public String getDescription() throws MetaStoreException {
    return metaStoreSupplier.get().getDescription();
  }

  @Override
  public void setTwoWayPasswordEncoder( ITwoWayPasswordEncoder encoder ) {
    metaStoreSupplier.get().setTwoWayPasswordEncoder( encoder );
  }

  @Override
  public ITwoWayPasswordEncoder getTwoWayPasswordEncoder() {
    return metaStoreSupplier.get().getTwoWayPasswordEncoder();
  }

}
