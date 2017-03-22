/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

package org.pentaho.di.engine.api.events;


import org.pentaho.di.engine.api.model.LogicalModelElement;

import java.io.Serializable;

public abstract class BaseEvent<S extends LogicalModelElement, D extends Serializable>  implements PDIEvent<S, D> {
  private static final long serialVersionUID = 1976966402442852547L;
  private final S source;
  private final D data;

  public BaseEvent( S source, D data ) {
    this.source = source;
    this.data = data;
  }

  @Override public S getSource() {
    return source;
  }

  @Override public D getData() {
    return data;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof BaseEvent ) ) {
      return false;
    }

    BaseEvent<?, ?> baseEvent = (BaseEvent<?, ?>) o;

    if ( !source.getId().equals( baseEvent.source.getId() ) ) {
      return false;
    }
    return data.equals( baseEvent.data );
  }

  @Override public int hashCode() {
    int result = source.getId().hashCode();
    result = 31 * result + data.hashCode();
    return result;
  }
}


