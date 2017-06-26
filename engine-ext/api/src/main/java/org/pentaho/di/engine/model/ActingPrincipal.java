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
package org.pentaho.di.engine.model;


import java.io.Serializable;
import java.security.Principal;

/**
 * Created by jchilton on 3/15/2017.
 */
public class ActingPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = -8326458100440326949L;
  private String name;

  public ActingPrincipal( String name ) {
    this.name = name;
  }

  @Override
  public String getName( ) {
    return name;
  }

  public boolean equals( Object object ) {
    if ( object instanceof ActingPrincipal ) {
      return this.name.equals( object.toString() );
    } else {
      return false;
    }
  }

  public String toString( ) {
    return this.name;
  }

  public int hashCode( ) {
    return this.name.hashCode();
  }

}
