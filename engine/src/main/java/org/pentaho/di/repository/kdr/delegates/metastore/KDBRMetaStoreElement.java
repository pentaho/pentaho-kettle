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
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryMetaStoreDelegate;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;

public class KDBRMetaStoreElement implements IMetaStoreElement {
  private LongObjectId namespaceId;
  private LongObjectId elementTypeId;
  private LongObjectId objectId;

  private String id;
  private Object value;
  private String name;

  public KettleDatabaseRepositoryMetaStoreDelegate delegate;
  private IMetaStoreElementType elementType;
  private IMetaStoreElementOwner owner;

  private List<IMetaStoreAttribute> children;
  private List<MetaStoreOwnerPermissions> ownerPermissions;

  public KDBRMetaStoreElement() {
    children = new ArrayList<IMetaStoreAttribute>();
    ownerPermissions = new ArrayList<MetaStoreOwnerPermissions>();
  }

  public KDBRMetaStoreElement( KettleDatabaseRepositoryMetaStoreDelegate delegate,
    IMetaStoreElementType elementType, String id, Object value ) {
    this();
    this.delegate = delegate;
    this.elementType = elementType;
    this.id = id;
    this.value = value;
  }

  public LongObjectId getObjectId() {
    return objectId;
  }

  public void setObjectId( LongObjectId objectId ) {
    this.objectId = objectId;
  }

  public LongObjectId getNamespaceId() {
    return namespaceId;
  }

  public void setNamespaceId( LongObjectId namespaceId ) {
    this.namespaceId = namespaceId;
  }

  public LongObjectId getElementTypeId() {
    return elementTypeId;
  }

  public void setElementTypeId( LongObjectId elementTypeId ) {
    this.elementTypeId = elementTypeId;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public Object getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<IMetaStoreAttribute> getChildren() {
    return children;
  }

  @Override
  public void addChild( IMetaStoreAttribute attribute ) {
    children.add( attribute );
  }

  @Override
  public void clearChildren() {
    children.clear();
  }

  @Override
  public void deleteChild( String attributeId ) {
    Iterator<IMetaStoreAttribute> iterator = children.iterator();
    while ( iterator.hasNext() ) {
      IMetaStoreAttribute attribute = iterator.next();
      if ( attribute.getId().equals( attributeId ) ) {
        iterator.remove();
        return;
      }
    }
  }

  @Override
  public IMetaStoreAttribute getChild( String id ) {
    Iterator<IMetaStoreAttribute> iterator = children.iterator();
    while ( iterator.hasNext() ) {
      IMetaStoreAttribute attribute = iterator.next();
      if ( attribute.getId().equals( id ) ) {
        return attribute;
      }
    }

    return null;
  }

  @Override
  public IMetaStoreElementType getElementType() {
    return elementType;
  }

  @Override
  public void setElementType( IMetaStoreElementType elementType ) {
    this.elementType = elementType;
  }

  @Override
  public IMetaStoreElementOwner getOwner() {
    return owner;
  }

  @Override
  public void setOwner( IMetaStoreElementOwner owner ) {
    this.owner = owner;

  }

  @Override
  public List<MetaStoreOwnerPermissions> getOwnerPermissionsList() {
    return ownerPermissions;
  }

  @Override
  public void setOwnerPermissionsList( List<MetaStoreOwnerPermissions> ownerPermissions ) {
    this.ownerPermissions = ownerPermissions;
  }

}
