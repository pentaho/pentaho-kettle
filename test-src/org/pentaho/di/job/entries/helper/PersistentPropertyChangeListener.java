/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.helper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Property Change Listener that records all received events, useful for test purposes.
 */
public class PersistentPropertyChangeListener implements PropertyChangeListener {
  private List<PropertyChangeEvent> receivedEvents;

  public PersistentPropertyChangeListener() {
    receivedEvents = new ArrayList<PropertyChangeEvent>();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    receivedEvents.add(evt);
  }

  /**
   * @return every event received by this listener
   */
  public List<PropertyChangeEvent> getReceivedEvents() {
    return receivedEvents;
  }

  /**
   * @return only the events that resulted in changed values
   */
  public List<PropertyChangeEvent> getReceivedEventsWithChanges() {
    List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
    for (PropertyChangeEvent evt : receivedEvents) {
      if (!(evt.getOldValue() == null ? evt.getNewValue() == null : evt.getOldValue().equals(evt.getNewValue()))) {
        events.add(evt);
      }
    }
    return events;
  }
}
