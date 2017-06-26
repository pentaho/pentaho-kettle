/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.pdi.engine.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.osgi.objecttunnel.TunnelSerializer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by nbaker on 3/4/17.
 */
abstract class BaseSerializer<T extends PDIEvent> implements TunnelSerializer<T> {

  ObjectMapper mapper = new ObjectMapper();
  private Class<T> tClass;

  public BaseSerializer( Class<T> tClass ) {
    this.tClass = tClass;
  }

  @Override public List<Class> getSupportedClasses() {
    return Collections.singletonList( tClass );
  }

  @Override public String serialize( Object object ) {
    try {
      return mapper.writer().writeValueAsString( object );
    } catch ( JsonProcessingException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override public T deserialize( String serializedString ) {
    try {
      return mapper.readValue( serializedString, tClass );
    } catch ( IOException e ) {
      throw new RuntimeException( e );
    }
  }
}
