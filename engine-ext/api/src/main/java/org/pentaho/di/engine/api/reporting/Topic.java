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
