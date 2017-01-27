package org.pentaho.di.engine.api.converter;

import org.pentaho.di.engine.api.model.Row;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public final class RowConversionManager implements Serializable {

  List<RowConverter> converters;

  public RowConversionManager( List<RowConverter> converters ) {
    this.converters = converters;
  }

  public <T> T convert( Row row, Class<T> clazz ) {
    return (T) converters.stream()
      .map( converter -> converter.convert( row, clazz ) )
      .filter( Optional::isPresent )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "failed to convert IRow to " + clazz.toString() ) )
      .get();



  }

}
