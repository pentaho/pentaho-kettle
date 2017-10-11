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
