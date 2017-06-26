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

import org.pentaho.di.engine.api.model.Row;

import java.util.Collections;
import java.util.List;

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

  @Override public List<String> getColumnNames() {
    return Collections.unmodifiableList( names );
  }

  @Override public Object[] getObjects() {
    return Collections.unmodifiableList( objects ).toArray();
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
