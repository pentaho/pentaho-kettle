/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
