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
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.model.Rows;

/**
 * A {@link PDIEvent} associated with a list of {@link Row} elements.
 * <p>
 * Created by nbaker on 5/30/16.
 */
public class DataEvent<S extends LogicalModelElement> extends BaseEvent<S, Rows> {

  private static final long serialVersionUID = 106147921259300759L;

  public DataEvent( S source, Rows rows ) {
    super( source, rows );
  }

}
