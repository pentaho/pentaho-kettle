package org.pentaho.di.engine.api.converter;

import org.pentaho.di.engine.api.IRow;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public final class RowConversionManager implements Serializable {

  List<IRowConverter> converters;

  public RowConversionManager( List<IRowConverter> converters ) {
    this.converters = converters;
  }

  public <T> T convert( IRow row, Class<T> clazz ) {
    return (T) converters.stream()
      .map( converter -> converter.convert( row, clazz ) )
      .filter( Optional::isPresent )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "failed to convert IRow to " + clazz.toString() ) )
      .get();



  }

}
