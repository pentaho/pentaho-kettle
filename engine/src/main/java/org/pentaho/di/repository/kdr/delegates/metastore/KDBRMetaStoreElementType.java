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

import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryMetaStoreDelegate;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class KDBRMetaStoreElementType implements IMetaStoreElementType {

  private String metaStoreName;
  private String namespace;
  private ObjectId namespaceId;
  private String name;
  private String description;
  private LongObjectId objectId;
  private KettleDatabaseRepositoryMetaStoreDelegate delegate;

  public KDBRMetaStoreElementType( KettleDatabaseRepositoryMetaStoreDelegate delegate, String namespace,
    ObjectId namespaceId, String name, String description ) {
    this.delegate = delegate;
    this.namespace = namespace;
    this.namespaceId = namespaceId;
    this.name = name;
    this.description = description;
  }

  @Override
  public void save() throws MetaStoreException {
    try {
      delegate.insertElementType( this );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to save element type in the database repository", e );
    }
  }

  @Override
  public String getId() {
    return objectId != null ? objectId.toString() : null;
  }

  @Override
  public void setId( String id ) {
    this.objectId = new LongObjectId( new StringObjectId( id ) );
  }

  public String getMetaStoreName() {
    return metaStoreName;
  }

  public void setMetaStoreName( String metaStoreName ) {
    this.metaStoreName = metaStoreName;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public void setId( LongObjectId id ) {
    this.objectId = id;
  }

  public LongObjectId getObjectId() {
    return objectId;
  }

  public void setObjectId( LongObjectId objectId ) {
    this.objectId = objectId;
  }

  public ObjectId getNamespaceId() {
    return namespaceId;
  }

  public void setNamespaceId( ObjectId namespaceId ) {
    this.namespaceId = namespaceId;
  }
}
