/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections.IteratorUtils;

/**
 * Thread Safe version of Java Properties class.
 */
public class ConcurrentMapProperties extends Properties {

  private static final long serialVersionUID = -7444528393201025496L;

  protected ConcurrentMap<Object, Object> storageMap = new ConcurrentHashMap<>();

  public ConcurrentMapProperties() {
    this( null );
  }

  public ConcurrentMapProperties( Properties defaults ) {
    if ( defaults != null ) {
      if ( defaults instanceof ConcurrentMapProperties ) {
        this.defaults = defaults;
      } else {
        this.defaults = convertProperties( defaults );
      }
    }
  }

  @Override
  public synchronized Object put( Object key, Object value ) {
    return storageMap.put( key, value );
  }

  @Override
  public synchronized Object remove( Object key ) {
    return storageMap.remove( key );
  }

  @Override
  public synchronized void clear() {
    storageMap.clear();
  }

  @Override
  public synchronized Object clone() {
    ConcurrentMapProperties cloned = new ConcurrentMapProperties();
    cloned.putAll( storageMap );
    return cloned;
  }

  @Override
  public boolean containsValue( Object value ) {
    return storageMap.containsValue( value );
  }

  @Override
  public synchronized Object get( Object key ) {
    return storageMap.get( key );
  }

  @Override
  public synchronized Object compute( Object key,
                                      BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction ) {
    return storageMap.compute( key, remappingFunction );
  }

  @Override
  public synchronized Object computeIfAbsent( Object key, Function<? super Object, ? extends Object> mappingFunction ) {
    return storageMap.computeIfAbsent( key, mappingFunction );
  }

  @Override
  public synchronized Object computeIfPresent( Object key,
                                               BiFunction<? super Object, ? super Object, ? extends Object>
                                                 remappingFunction ) {
    return storageMap.computeIfPresent( key, remappingFunction );
  }

  @Override
  public synchronized boolean contains( Object value ) {
    return storageMap.containsValue( value );
  }

  @Override
  public synchronized boolean isEmpty() {
    return storageMap.isEmpty();
  }

  @Override
  public synchronized int size() {
    return storageMap.size();
  }

  @Override
  public synchronized boolean containsKey( Object key ) {
    return storageMap.containsKey( key );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public synchronized Enumeration<Object> elements() {
    return (Enumeration<Object>) IteratorUtils.asEnumeration( storageMap.values().iterator() );
  }

  @Override
  public Set<java.util.Map.Entry<Object, Object>> entrySet() {
    return storageMap.entrySet();
  }

  @Override
  public synchronized void forEach( BiConsumer<? super Object, ? super Object> action ) {
    storageMap.forEach( action );
  }

  @Override
  public synchronized Object getOrDefault( Object key, Object defaultValue ) {
    return storageMap.getOrDefault( key, defaultValue );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public synchronized Enumeration<Object> keys() {
    return (Enumeration<Object>) IteratorUtils.asEnumeration( storageMap.keySet().iterator() );
  }

  @Override
  public Set<Object> keySet() {
    return storageMap.keySet();
  }

  @Override
  public synchronized Object merge( Object key, Object value,
                                    BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction ) {
    return storageMap.merge( key, value, remappingFunction );
  }

  @Override
  public synchronized void putAll( Map<? extends Object, ? extends Object> t ) {
    storageMap.putAll( t );
  }

  @Override
  public synchronized Object putIfAbsent( Object key, Object value ) {
    return storageMap.putIfAbsent( key, value );
  }

  @Override
  public synchronized boolean remove( Object key, Object value ) {
    return storageMap.remove( key, value );
  }

  @Override
  public synchronized boolean replace( Object key, Object oldValue, Object newValue ) {
    return storageMap.replace( key, oldValue, newValue );
  }

  @Override
  public synchronized void replaceAll( BiFunction<? super Object, ? super Object, ? extends Object> function ) {
    storageMap.replaceAll( function );
  }

  @Override
  public synchronized Object replace( Object key, Object value ) {
    return storageMap.replace( key, value );
  }

  @Override
  public Collection<Object> values() {
    return storageMap.values();
  }

  @Override
  public String getProperty( String key ) {
    Object oval = storageMap.get( key );
    String sval = ( oval instanceof String ) ? (String) oval : null;
    return ( ( sval == null ) && ( defaults != null ) ) ? defaults.getProperty( key ) : sval;
  }

  @Override
  public String getProperty( String key, String defaultValue ) {
    /*
     * This method simply uses getProperty(String key), so it is fine to use the original implementation
     */
    return super.getProperty( key, defaultValue );
  }

  @Override
  public synchronized Object setProperty( String key, String value ) {
    return storageMap.put( key, value );
  }

  @Override
  public synchronized boolean equals( Object o ) {
    super.putAll( storageMap );
    boolean result = super.equals( o );
    super.clear();
    return result;
  }

  @Override
  public synchronized String toString() {
    super.putAll( storageMap );
    String result = super.toString();
    super.clear();
    return result;
  }

  @Override
  public synchronized int hashCode() {
    super.putAll( storageMap );
    int result = super.hashCode();
    super.clear();
    return result;
  }

  @Override
  public synchronized Enumeration<?> propertyNames() {
    return IteratorUtils.asEnumeration( stringPropertyNames().iterator() );
  }

  @Override
  public synchronized Set<String> stringPropertyNames() {
    Set<String> copiedSet = new HashSet<>();
    if ( defaults != null ) {
      defaults.keySet().forEach( x -> copiedSet.add( (String) x ) );
    }
    storageMap.keySet().forEach( x -> copiedSet.add( (String) x ) );
    return copiedSet;
  }

  @Override
  public synchronized void list( PrintStream out ) {
    super.putAll( storageMap );
    super.list( out );
    super.clear();
  }

  @Override
  public synchronized void list( PrintWriter out ) {
    super.putAll( storageMap );
    super.list( out );
    super.clear();
  }

  @Override
  public synchronized void load( InputStream inStream ) throws IOException {
    super.putAll( storageMap );
    super.load( inStream );
    super.forEach( ( key, value ) -> storageMap.put( key, value ) );
    super.clear();
  }

  @Override
  public synchronized void load( Reader reader ) throws IOException {
    super.putAll( storageMap );
    super.load( reader );
    super.forEach( ( key, value ) -> storageMap.put( key, value ) );
    super.clear();
  }

  @Override
  public synchronized void loadFromXML( InputStream in ) throws IOException, InvalidPropertiesFormatException {
    super.putAll( storageMap );
    super.loadFromXML( in );
    super.forEach( ( key, value ) -> storageMap.putIfAbsent( key, value ) );
    super.clear();
  }

  @Override
  @Deprecated
  public synchronized void save( OutputStream out, String comments ) {
    super.putAll( storageMap );
    super.save( out, comments );
    super.clear();
  }

  @Override
  public synchronized void store( OutputStream out, String comments ) throws IOException {
    super.putAll( storageMap );
    super.store( out, comments );
    super.clear();
  }

  @Override
  public synchronized void store( Writer writer, String comments ) throws IOException {
    super.putAll( storageMap );
    super.store( writer, comments );
    super.clear();
  }

  @Override
  public synchronized void storeToXML( OutputStream os, String comment ) throws IOException {
    super.putAll( storageMap );
    super.storeToXML( os, comment );
    super.clear();
  }

  @Override
  public synchronized void storeToXML( OutputStream os, String comment, String encoding ) throws IOException {
    super.putAll( storageMap );
    super.storeToXML( os, comment, encoding );
    super.clear();
  }

  /**
   * Converts a Properties object to a ConcurrentMapProperties object
   *
   * @param props
   * @return A new ConcurrentMapProperties with all properties enumerated (including defaults)
   */
  public static ConcurrentMapProperties convertProperties( Properties props ) {
    if ( props != null ) {
      if ( !( props instanceof ConcurrentMapProperties ) ) {
        ConcurrentMapProperties result = new ConcurrentMapProperties( null );
        synchronized ( props ) {
          for ( String prop : props.stringPropertyNames() ) {
            result.put( prop, props.getProperty( prop ) );
          }
        }
        return result;
      } else {
        //Already a ConcurrentMapProperties
        return (ConcurrentMapProperties) props;
      }
    }
    return null;
  }

}
