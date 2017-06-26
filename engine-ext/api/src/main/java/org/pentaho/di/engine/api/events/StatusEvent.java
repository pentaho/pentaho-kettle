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
import org.pentaho.di.engine.api.reporting.Status;

/**
 * Created by nbaker on 1/17/17.
 */
public class StatusEvent<S extends LogicalModelElement> extends BaseEvent<S, Status> {
  private static final long serialVersionUID = 5019758572364906951L;

  public StatusEvent( S source, Status status ) {
    super( source, status );
  }
}
