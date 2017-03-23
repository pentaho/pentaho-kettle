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

package org.pentaho.di.engine.api.model;

import org.pentaho.di.engine.api.events.PDIEvent;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nbaker on 1/18/17.
 */
@Deprecated
public interface MaterializedModelElement extends ModelElement {

  <D extends Serializable> List<Publisher<? extends PDIEvent>> getPublisher( Class<D> type );

  <D extends Serializable> List<Serializable> getEventTypes();

  LogicalModelElement getLogicalElement();

  /**
   * Called right before the transformation will be executed.
   */
  void init();
}
