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
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.stores.memory.MemoryMetaStoreAttribute;
import org.pentaho.metastore.stores.memory.MemoryMetaStoreElement;
import org.pentaho.metastore.stores.memory.MemoryMetaStoreElementOwner;

import java.io.IOException;

import com.google.common.base.Preconditions;

/**
 * @author nhudak
 */
@JsonIgnoreProperties( { "elementType" } )
public class JsonElement extends MemoryMetaStoreElement implements AttributesInterfaceEntry {
  private final ObjectMapper objectMapper = new ObjectMapper();

  {
    SimpleModule module = new SimpleModule( "MetaStore Elements", Version.unknownVersion() );
    module.addAbstractTypeMapping( IMetaStoreAttribute.class, MemoryMetaStoreAttribute.class );
    module.addAbstractTypeMapping( IMetaStoreElementOwner.class, EmptyOwner.class );

    objectMapper.registerModule( module );
  }

  public JsonElement() {
  }

  public JsonElement( IMetaStoreElement element ) {
    super( element );
  }

  static JsonElement from( IMetaStoreElement element ) {
    return element instanceof JsonElement ? ( (JsonElement) element ) : new JsonElement( element );
  }

  @Override public String getId() {
    if ( Strings.isNullOrEmpty( super.getId() ) ) {
      setId( getName() );
    }
    return Strings.emptyToNull( super.getId() );
  }

  public static String groupName( IMetaStoreElementType elementType ) {
    ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.put( "_", "Embedded MetaStore Elements" );
    objectNode.put( "namespace", Preconditions.checkNotNull( elementType.getNamespace() ) );
    objectNode.put( "type", Preconditions.checkNotNull( elementType.getId() ) );
    return objectNode.toString();
  }

  @Override public String groupName() {
    return groupName( Preconditions.checkNotNull( getElementType() ) );
  }

  @Override public String key() {
    return Preconditions.checkNotNull( getId() );
  }

  @Override public String jsonValue() throws IOException {
    return objectMapper.writeValueAsString( this );
  }

  public JsonElement load( String jsonData ) throws IOException {
    return objectMapper.readerForUpdating( this ).readValue( jsonData );
  }

  private static class EmptyOwner extends MemoryMetaStoreElementOwner {
    public EmptyOwner() {
      super( null, null );
    }
  }
}
