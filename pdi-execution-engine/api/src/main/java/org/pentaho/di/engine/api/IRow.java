package org.pentaho.di.engine.api;

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
public interface IRow extends Serializable {
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
