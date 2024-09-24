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

package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.model.LogicalModelElement;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by hudak on 1/25/17.
 */
public class Topic implements Serializable {
  private static final long serialVersionUID = 3421934238701207191L;
  private final LogicalModelElement source;
  private final Class<? extends Serializable> eventType;

  public Topic( LogicalModelElement source, Class<? extends Serializable> eventType ) {
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

  public LogicalModelElement getSource() {
    return source;
  }

  public String getSourceId() {
    return source.getId();
  }

  public Class<? extends Serializable> getEventType() {
    return eventType;
  }
}
