/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
