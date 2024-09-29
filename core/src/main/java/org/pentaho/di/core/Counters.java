/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains the counters for Kettle, the transformations, jobs and also the repository.
 *
 * @author Matt
 * @since 17-apr-2005
 *
 */
public class Counters {
  private static Counters counters = null;
  private Map<String, Counter> counterTable = null;

  private Counters() {
    counterTable = new ConcurrentHashMap<>();
  }

  public static Counters getInstance() {
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
