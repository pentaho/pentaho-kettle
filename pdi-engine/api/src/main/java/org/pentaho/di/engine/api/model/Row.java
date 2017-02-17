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

import org.pentaho.di.engine.api.RowException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * Represents a single row of data consisting of 0..N columns,
 * accessible via typed getters, along with methods for retrieving
 * column name and type info.
 *
 * Each of the getXXXX() methods should return either:
 *   1)  an Optional of the appropriately typed value, if present and compatible
 *   2)  Optional.empty() if the column exists but has no defined value.
 * If the requested type is not compatible, or if the index is OOB,
 * the implementation should throw RowException.
 */
public interface Row extends Serializable {
  int size();

  List<String> getColumnNames();

  List<Class> getColumnTypes();


  Optional<String> getString( int index ) throws RowException;

  Optional<Long> getLong( int index ) throws RowException;

  Optional<Double> getNumber( int index ) throws RowException;

  Optional<Date> getDate( int index ) throws RowException;

  Optional<BigDecimal> getBigNumber( int index ) throws RowException;

  Optional<Boolean> getBoolean( int index ) throws RowException;

  Optional<byte[]> getBinary( int index ) throws RowException;

  Optional<Object> getObject( int index ) throws RowException;

  Optional<String> getString( String name ) throws RowException;

  Optional<Long> getLong( String name ) throws RowException;

  Optional<Double> getNumber( String name ) throws RowException;

  Optional<Date> getDate( String name ) throws RowException;

  Optional<BigDecimal> getBigNumber( String name ) throws RowException;

  Optional<Boolean> getBoolean( String name ) throws RowException;

  Optional<byte[]> getBinary( String name ) throws RowException;

  Optional<Object> getObject( String name ) throws RowException;

  Optional<Object[]> getObjects();
}
