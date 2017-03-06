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

import org.pentaho.di.engine.api.RowException;
import org.pentaho.di.engine.api.model.Row;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * We don't have access to a generic Row from the API. As such we're owning an implementation for deserialized rows.
 * <p>
 * This will need to be accompanied by a RowConverter in the future.
 * <p>
 * TODO: Investigate moving to API bundle as a generic class.
 * <p>
 * Created by nbaker on 3/6/17.
 */
public class DeserializedRow implements Row {
  private List<String> names;
  private List<Object> objects;
  private List<Class> types;

  public DeserializedRow( List<String> names, List<Class> types, List<Object> objects ) {

    this.names = names;
    this.types = types;
    this.objects = objects;
  }

  @Override public int size() {
    return objects.size();
  }

  @Override public List<String> getColumnNames() {
    return Collections.unmodifiableList( names );
  }

  @Override public List<Class> getColumnTypes() {
    return Collections.unmodifiableList( types );
  }

  @Override public Optional<String> getString( int index ) throws RowException {
    return Optional.of( (String) objects.get( index ) );
  }

  @Override public Optional<Long> getLong( int index ) throws RowException {
    return Optional.of( (Long) objects.get( index ) );
  }

  @Override public Optional<Double> getNumber( int index ) throws RowException {
    return Optional.of( (Double) objects.get( index ) );
  }

  @Override public Optional<Date> getDate( int index ) throws RowException {
    return Optional.of( (Date) objects.get( index ) );
  }

  @Override public Optional<BigDecimal> getBigNumber( int index ) throws RowException {
    return Optional.of( (BigDecimal) objects.get( index ) );
  }

  @Override public Optional<Boolean> getBoolean( int index ) throws RowException {
    return Optional.of( (Boolean) objects.get( index ) );
  }

  @Override public Optional<byte[]> getBinary( int index ) throws RowException {
    return Optional.of( (byte[]) objects.get( index ) );
  }

  @Override public Optional<Object> getObject( int index ) throws RowException {
    return Optional.of( objects.get( index ) );
  }

  @Override public Optional<String> getString( String name ) throws RowException {
    return getString( names.indexOf( name ) );
  }

  @Override public Optional<Long> getLong( String name ) throws RowException {
    return getLong( names.indexOf( name ) );
  }

  @Override public Optional<Double> getNumber( String name ) throws RowException {
    return getNumber( names.indexOf( name ) );
  }

  @Override public Optional<Date> getDate( String name ) throws RowException {
    return getDate( names.indexOf( name ) );
  }

  @Override public Optional<BigDecimal> getBigNumber( String name ) throws RowException {
    return getBigNumber( names.indexOf( name ) );
  }

  @Override public Optional<Boolean> getBoolean( String name ) throws RowException {
    return getBoolean( names.indexOf( name ) );
  }

  @Override public Optional<byte[]> getBinary( String name ) throws RowException {
    return getBinary( names.indexOf( name ) );
  }

  @Override public Optional<Object> getObject( String name ) throws RowException {
    return getObject( names.indexOf( name ) );
  }

  @Override public Optional<Object[]> getObjects() {
    return Optional.of( Collections.unmodifiableList( objects ).toArray() );
  }

  @Override public Optional<Integer> getInteger( int index ) throws RowException {
    return Optional.of( (Integer) objects.get( index ) );
  }

  @Override public Optional<Integer> getInteger( String name ) throws RowException {
    return getInteger( names.indexOf( name ) );
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof DeserializedRow ) ) {
      return false;
    }

    DeserializedRow that = (DeserializedRow) o;

    if ( !names.equals( that.names ) ) {
      return false;
    }
    if ( !objects.equals( that.objects ) ) {
      return false;
    }
    return types.equals( that.types );
  }

  @Override public int hashCode() {
    int result = names.hashCode();
    result = 31 * result + objects.hashCode();
    result = 31 * result + types.hashCode();
    return result;
  }
}