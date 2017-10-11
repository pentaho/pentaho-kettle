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

package org.pentaho.di.trans.steps.switchcase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.RowSet;

public class KeyToRowSetMap {

  protected Map<Object, Set<RowSet>> map;

  protected KeyToRowSetMap() {
    map = new HashMap<Object, Set<RowSet>>();
  }

  /**
   * Support custom runtime implementation.
   *
   * @param map
   */
  protected KeyToRowSetMap( Map<Object, Set<RowSet>> map ) {
    this.map = map;
  }

  protected Set<RowSet> get( Object key ) {
    return map.get( key );
  }

  protected void put( Object key, RowSet rowSet ) {
    Set<RowSet> existing = map.get( key );
    if ( existing == null ) {
      existing = new HashSet<RowSet>();
      map.put( key, existing );
    }
    existing.add( rowSet );
  }

  public boolean containsKey( Object key ) {
    return map.containsKey( key );
  }

  public boolean isEmpty() {
    return map.keySet().isEmpty();
  }

  protected Set<Object> keySet() {
    return map.keySet();
  }

  protected Set<java.util.Map.Entry<Object, Set<RowSet>>> entrySet() {
    return map.entrySet();
  }
}
