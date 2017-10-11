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

package org.pentaho.di.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.TruePredicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 */
public class KeyValueSet implements Iterable<KeyValue<?>>, Serializable {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 925133158112717153L;

  private final Map<String, KeyValue<?>> entries = new TreeMap<String, KeyValue<?>>();

  /**
   * Add key value(s).
   *
   * @param keyValues
   *          key values to add.
   * @return this.
   */
  public KeyValueSet add( final KeyValue<?>... keyValues ) {
    for ( KeyValue<?> keyValue : keyValues ) {
      if ( this.entries.containsKey( keyValue.getKey() ) ) {
        throw new IllegalArgumentException( "Key already added [key=" + keyValue.getKey() + "]" );
      }
      this.entries.put( keyValue.getKey(), keyValue );
    }
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<KeyValue<?>> iterator() {
    return this.keyValues().iterator();
  }

  /**
   * @param key
   *          the key.
   * @return key value or null.
   */
  public KeyValue<?> get( final String key ) {
    if ( key == null ) {
      return null;
    }
    return this.entries.get( StringUtils.lowerCase( key ) );
  }

  /**
   * @param filter
   *          filter to use.
   * @return matching key values.
   * @throws IllegalArgumentException
   *           if filter is null.
   */
  public List<KeyValue<?>> get( final Predicate filter ) throws IllegalArgumentException {
    final AddClosureArrayList<KeyValue<?>> result = new AddClosureArrayList<KeyValue<?>>();
    this.walk( result, filter );
    return result;
  }

  /**
   * @param key
   *          the key.
   * @return key value, never null.
   */
  public KeyValue<?> getRequired( final String key ) {
    final KeyValue<?> keyValue = this.get( key );
    if ( keyValue == null ) {
      throw new IllegalArgumentException( "Entry not found [key=" + key + "]" );
    }
    return keyValue;
  }

  /**
   * @return keys.
   */
  public List<String> keys() {
    return new ArrayList<String>( this.entries.keySet() );
  }

  /**
   * @return key values/entries.
   */
  public List<KeyValue<?>> keyValues() {
    return new ArrayList<KeyValue<?>>( this.entries.values() );
  }

  /**
   * @return values.
   */
  public List<Object> values() {
    final List<Object> result = new ArrayList<Object>();
    for ( KeyValue<?> keyValue : this.entries.values() ) {
      result.add( keyValue.getValue() );
    }
    return result;
  }

  /**
   * @return entries as map.
   */
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new TreeMap<String, Object>();
    for ( KeyValue<?> keyValue : this.entries.values() ) {
      map.put( keyValue.getKey(), keyValue.getValue() );
    }
    return map;
  }

  /**
   * Walk entries.
   *
   * @param handler
   *          handler to call.
   * @param filter
   *          filter to use.
   * @throws IllegalArgumentException
   *           if closure or filter is null.
   */
  public void walk( final Closure handler, final Predicate filter ) throws IllegalArgumentException {
    Assert.assertNotNull( handler, "Handler cannot be null" );
    Assert.assertNotNull( filter, "Filter cannot be null" );
    for ( KeyValue<?> keyValue : this.entries.values() ) {
      if ( filter.evaluate( keyValue ) ) {
        handler.execute( keyValue );
      }
    }
  }

  /**
   * Walk entries.
   *
   * @param handler
   *          handler to call.
   * @throws IllegalArgumentException
   *           if handler is null.
   */
  public void walk( final Closure handler ) throws IllegalArgumentException {
    this.walk( handler, TruePredicate.INSTANCE );
  }

  /**
   * @param key
   *          the key.
   * @return previous or null.
   */
  public KeyValue<?> remove( final String key ) {
    if ( key == null ) {
      return null;
    }
    return this.entries.remove( key );
  }

  /**
   * @param key
   *          key to test.
   * @return true if ...
   */
  public boolean containsKey( final String key ) {
    if ( key == null ) {
      return false;
    }
    return this.entries.containsKey( key );
  }

  /**
   * @return size.
   */
  public int size() {
    return this.entries.size();
  }

  /**
   * @return true if empty.
   */
  public boolean isEmpty() {
    return this.entries.isEmpty();
  }

  /**
   * Clear entries.
   *
   * @return this.
   */
  public KeyValueSet clear() {
    this.entries.clear();
    return this;
  }

  /**
   * @return string representation.
   */
  public String toMultiLineString() {
    final ToStringBuilder builder = new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE );
    for ( KeyValue<?> keyValue : this.entries.values() ) {
      builder.append( keyValue.getKey(), keyValue.getValue() );
    }
    return builder.toString();
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE );
    builder.append( "size", this.size() );
    return builder.toString();
  }

}
