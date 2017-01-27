package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.model.ILogicalModelElement;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by hudak on 1/25/17.
 */
public class Topic {
  private final ILogicalModelElement source;
  private final Class<? extends Serializable> eventType;

  public Topic( ILogicalModelElement source, Class<? extends Serializable> eventType ) {
    this.source = source;
    this.eventType = eventType;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    Topic topic = (Topic) o;
    return Objects.equals( source, topic.source ) && Objects.equals( eventType, topic.eventType );
  }

  @Override public int hashCode() {
    return Objects.hash( source, eventType );
  }

  public ILogicalModelElement getSource() {
    return source;
  }

  public String getSourceId() {
    return source.getId();
  }

  public Class<? extends Serializable> getEventType() {
    return eventType;
  }
}
