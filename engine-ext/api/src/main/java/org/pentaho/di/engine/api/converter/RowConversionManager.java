/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

package org.pentaho.di.engine.api.converter;

import org.pentaho.di.engine.api.model.Row;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public final class RowConversionManager implements Serializable {

  private static final long serialVersionUID = 4901109916386185410L;
  List<RowConverter> converters;

  public RowConversionManager( List<RowConverter> converters ) {
    this.converters = converters;
  }

  public <T> T convert( Row row, Class<T> clazz ) {
    return (T) converters.stream()
      .map( converter -> converter.convert( row, clazz ) )
      .filter( Optional::isPresent )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "failed to convert Row to " + clazz.toString() ) )
      .get();
  }

}
