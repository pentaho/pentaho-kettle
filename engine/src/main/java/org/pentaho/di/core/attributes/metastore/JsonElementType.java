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

package org.pentaho.di.core.attributes.metastore;

import com.google.common.base.Strings;
import org.codehaus.jackson.map.ObjectMapper;
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
