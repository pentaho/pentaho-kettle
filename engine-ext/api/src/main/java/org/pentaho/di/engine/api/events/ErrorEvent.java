/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.engine.api.events;

import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.reporting.LogEntry;

/**
 * LogEvents encapsulate a LogEntry and contain a reference to the origin Element from the Logical Model.
 * <p>
 * Created by fcamara on 8/23/2017.
 */
public class ErrorEvent<S extends LogicalModelElement> extends BaseEvent<S, LogEntry> {

  private static final long serialVersionUID = 6308895090845470781L;

  public ErrorEvent( S source, LogEntry log ) {
    super( source, log );
  }

}
