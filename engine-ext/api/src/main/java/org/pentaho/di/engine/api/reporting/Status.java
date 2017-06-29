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

import java.io.Serializable;

/**
 * Created by hudak on 1/6/17.
 */
public enum Status implements Serializable {
  RUNNING( false ),
  PAUSED( false ),
  STOPPED( true ),
  FAILED( true ),
  FINISHED( true );

  private static final long serialVersionUID = -938695168387846889L;
  final boolean finalState;

  Status( Boolean finalState ) {
    this.finalState = finalState;
  }

  public boolean isFinal() {
    return finalState;
  }
}
