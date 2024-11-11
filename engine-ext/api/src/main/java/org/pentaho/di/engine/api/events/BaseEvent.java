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


package org.pentaho.di.engine.api.events;


import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.remote.RemoteSource;

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
    int result = 0;
    if ( source instanceof RemoteSource ) {
      result = this.getClass().getName().hashCode();
      result = 31 * result + ( ( (RemoteSource) source ).getModelType() != null
        ? ( (RemoteSource) source ).getModelType().toString().hashCode() : 0 );
      result =
        31 * result + ( ( (RemoteSource) source ).getId() != null ? ( (RemoteSource) source ).getId().hashCode() : 0 );
    } else {
      result = source.getId().hashCode();
      result = 31 * result + data.hashCode();
    }
    return result;
  }
}


