/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

import java.util.Hashtable;

/**
 * This class contains the counters for Kettle, the transformations, jobs and also the repository.
 *
 * @author Matt
 * @since 17-apr-2005
 *
 */
public class Counters {
  private static Counters counters = null;
  private Hashtable<String, Counter> counterTable = null;

  private Counters() {
    counterTable = new Hashtable<String, Counter>();
  }

  public static final Counters getInstance() {
    if ( counters != null ) {
      return counters;
    }
    counters = new Counters();
    return counters;
  }

  public Counter getCounter( String name ) {
    return counterTable.get( name );
  }

  public void setCounter( String name, Counter counter ) {
    counterTable.put( name, counter );
  }

  public void clearCounter( String name ) {
    counterTable.remove( name );
  }

  public void clear() {
    counterTable.clear();
  }
}
