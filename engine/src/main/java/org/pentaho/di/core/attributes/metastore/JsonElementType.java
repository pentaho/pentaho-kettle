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

package org.pentaho.di.core.attributes.metastore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.pentaho.metastore.api.BaseElementType;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.io.IOException;

import com.google.common.base.Preconditions;

/**
 * @author nhudak
 */
public abstract class JsonElementType extends BaseElementType implements AttributesInterfaceEntry {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JsonElementType( String namespace ) {
    super( namespace );
  }

  public static JsonElementType from( final IMetaStoreElementType elementType ) {
    if ( elementType instanceof JsonElementType ) {
      return (JsonElementType) elementType;
    } else {
      return new DerivedJsonElementType( elementType );
    }
  }

  @Override public String getId() {
    if ( Strings.isNullOrEmpty( super.getId() ) ) {
      setId( getName() );
    }
    return Strings.emptyToNull( super.getId() );
  }

  public static String groupName( String namespace ) {
    return EmbeddedMetaStore.METASTORE_PREFIX + namespace;
  }

  @Override public String groupName() {
    return groupName( getNamespace() );
  }

  @Override public String key() {
    return Preconditions.checkNotNull( getId() );
  }

  @Override public String jsonValue() throws IOException {
    return objectMapper.writeValueAsString( this );
  }

  public JsonElementType load( String jsonData ) throws IOException {
    return objectMapper.readerForUpdating( this ).readValue( jsonData );
  }

  private static class DerivedJsonElementType extends JsonElementType {
    private final IMetaStoreElementType elementType;

    public DerivedJsonElementType( IMetaStoreElementType elementType ) {
      super( elementType.getNamespace() );
      this.elementType = elementType;
      copyFrom( elementType );
    }

    @Override public void save() throws MetaStoreException {
      elementType.save();
    }
  }
}
