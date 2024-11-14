/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entry.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidatorContext {

  private Map<String, Object> map = new HashMap<String, Object>();

  public Map<String, Object> getMap() {
    return map;
  }

  public ValidatorContext put( String key, Object value ) {
    map.put( key, value );
    return this;
  }

  public ValidatorContext putAsList( String key, Object... value ) {
    map.put( key, value );
    return this;
  }

  public void clear() {
    map.clear();
  }

  public boolean containsKey( String key ) {
    return map.containsKey( key );
  }

  public boolean containsValue( Object value ) {
    return map.containsValue( value );
  }

  public Set<Map.Entry<String, Object>> entrySet() {
    return map.entrySet();
  }

  public Object get( String key ) {
    return map.get( key );
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set<String> keySet() {
    return map.keySet();
  }

  public ValidatorContext putAll( Map<String, Object> t ) {
    map.putAll( t );
    return this;
  }

  public Object remove( String key ) {
    return map.remove( key );
  }

  public int size() {
    return map.size();
  }

  public Collection<Object> values() {
    return map.values();
  }

}
