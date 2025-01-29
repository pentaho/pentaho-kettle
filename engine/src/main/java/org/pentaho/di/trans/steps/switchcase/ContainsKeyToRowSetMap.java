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


package org.pentaho.di.trans.steps.switchcase;

import java.util.ArrayList;
import java.util.Set;

import org.pentaho.di.core.RowSet;

public class ContainsKeyToRowSetMap extends KeyToRowSetMap {
  protected ArrayList<String> list = new ArrayList<String>();

  protected ContainsKeyToRowSetMap() {
    super();
  }

  public Set<RowSet> get( Object value ) {
    String valueStr = (String) value;
    for ( String key : list ) {
      if ( valueStr.contains( key ) ) {
        return super.get( key );
      }
    }
    return null;
  }

  protected void put( Object key, RowSet rowSet ) {
    super.put( key, rowSet );
    list.add( (String) key );
  }

  public boolean containsKey( Object key ) {
    String keyStr = (String) key;
    for ( String value : list ) {
      if ( keyStr.contains( value ) ) {
        return true;
      }
    }
    return false;
  }
}
