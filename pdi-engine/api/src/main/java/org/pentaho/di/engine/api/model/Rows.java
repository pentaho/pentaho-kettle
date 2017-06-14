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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * A List of Rows, as well as the flow information (TYPE) and STATE, indicating
 * whether the datastream is still ACTIVE, or has no remaining rows (COMPLETE).
 */
public class Rows implements List<Row>, Serializable {

  private static final long serialVersionUID = -2797398159525774206L;
  public enum TYPE { IN, OUT, ERROR }

  public enum STATE { ACTIVE, COMPLETE }

  private final List<Row> rows;
  private final STATE state;
  private final TYPE type;

  public Rows( List<Row> rowList, TYPE type, STATE state ) {
    this.rows = rowList;
    this.type = type;
    this.state = state;
  }

  @Override public int size() {
    return rows.size();
  }

  @Override public boolean isEmpty() {
    return rows.isEmpty();
  }

  @Override public boolean contains( Object o ) {
    return rows.contains( o );
  }

  @Override public Iterator<Row> iterator() {
    return rows.iterator();
  }

  @Override public Object[] toArray() {
    return rows.toArray();
  }

  @Override public <T> T[] toArray( T[] a ) {
    return rows.toArray( a );
  }

  @Override public boolean add( Row row ) {
    return rows.add( row );
  }

  @Override public boolean remove( Object o ) {
    return rows.remove( o );
  }

  @Override public boolean containsAll( Collection<?> c ) {
    return rows.containsAll( c );
  }

  @Override public boolean addAll( Collection<? extends Row> c ) {
    return rows.addAll( c );
  }

  @Override public boolean addAll( int index, Collection<? extends Row> c ) {
    return rows.addAll( index, c );
  }

  @Override public boolean removeAll( Collection<?> c ) {
    return rows.removeAll( c );
  }

  @Override public boolean retainAll( Collection<?> c ) {
    return rows.retainAll( c );
  }

  @Override public void clear() {
    rows.clear();
  }

  @Override public Row get( int index ) {
    return rows.get( index );
  }

  @Override public Row set( int index, Row element ) {
    return rows.set( index, element );
  }

  @Override public void add( int index, Row element ) {
    rows.add( index, element );
  }

  @Override public Row remove( int index ) {
    return rows.remove( index );
  }

  @Override public int indexOf( Object o ) {
    return rows.indexOf( o );
  }

  @Override public int lastIndexOf( Object o ) {
    return rows.lastIndexOf( o );
  }

  @Override public ListIterator<Row> listIterator() {
    return rows.listIterator();
  }

  @Override public ListIterator<Row> listIterator( int index ) {
    return rows.listIterator( index );
  }

  @Override public List<Row> subList( int fromIndex, int toIndex ) {
    return rows.subList( fromIndex, toIndex );
  }

  public TYPE getType() {
    return type;
  }

  public STATE getState() {
    return state;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof Rows ) ) {
      return false;
    }

    Rows rows1 = (Rows) o;

    if ( !rows.equals( rows1.rows ) ) {
      return false;
    }
    if ( state != rows1.state ) {
      return false;
    }
    return type == rows1.type;
  }

  @Override public int hashCode() {
    int result = rows.hashCode();
    result = 31 * result + state.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
