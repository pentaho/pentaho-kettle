package org.pentaho.di.engine.api.converter;

import org.pentaho.di.engine.api.model.Row;

import java.io.Serializable;
import java.util.Optional;

public interface RowConverter<T> extends Serializable {

  Optional<T> convert( Row row, Class<T> type );


}
