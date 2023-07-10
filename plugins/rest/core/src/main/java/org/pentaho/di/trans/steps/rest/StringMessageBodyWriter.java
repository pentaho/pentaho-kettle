/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.rest;

import com.sun.jersey.core.impl.provider.entity.StringProvider;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This class is a custom provider to replace the default com.sun.jersey.core.impl.provider.entity.StringProvider
 * which it has a bad implementation related with get size.
 *
 *
 */
@Produces( { "text/plain", "*/*" } )
final class StringMessageBodyWriter implements MessageBodyWriter<String> {

  private final StringProvider stringProvider;

  public StringMessageBodyWriter() {
    this.stringProvider = new StringProvider();
  }

  @Override
  public long getSize( String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return s == null ? -1 : s.getBytes().length;
  }

  @Override
  public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    return this.stringProvider.isWriteable( type, genericType, annotations, mediaType );
  }

  @Override
  public void writeTo( String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream )
    throws IOException {
    this.stringProvider.writeTo( s, type, genericType, annotations, mediaType, httpHeaders, entityStream );
  }

}
