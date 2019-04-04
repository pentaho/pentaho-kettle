/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Transformation;

import java.io.Serializable;

public class SubTransCreation implements Serializable {
  private static final long serialVersionUID = 6249199047103429616L;
  private ExecutionContext context;
  private Transformation transformation;

  public ExecutionContext getContext() {
    return context;
  }

  public void setContext( ExecutionContext context ) {
    this.context = context;
  }

  public Transformation getTransformation() {
    return transformation;
  }

  public void setTransformation( Transformation transformation ) {
    this.transformation = transformation;
  }
}
