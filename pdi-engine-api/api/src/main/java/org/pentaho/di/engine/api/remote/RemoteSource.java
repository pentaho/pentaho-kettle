package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.LogicalModelElement;

import java.io.Serializable;

/**
 * Placeholder event source for remote events.
 * <p>
 * {@link PDIEvent} objects usually have a reference to a logical-model event source.
 * Since these sources may not be serializable, and the client will have different references, this source can
 * be used as a placeholder when the event is tunneled back to to the client.
 * <p>
 * Created by hudak on 2/10/17.
 */
public final class RemoteSource implements LogicalModelElement, Serializable {
  private final String id;

  public RemoteSource( String id ) {
    this.id = id;
  }

  @Override public String getId() {
    return id;
  }
}
