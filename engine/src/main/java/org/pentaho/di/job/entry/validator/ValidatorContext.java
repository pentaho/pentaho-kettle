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
