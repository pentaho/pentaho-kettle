package org.pentaho.di.engine.api.converter;

import org.pentaho.di.engine.api.IRow;

import java.io.Serializable;
import java.util.Optional;

public interface IRowConverter<T> extends Serializable {

  Optional<T> convert( IRow row, Class<T> type );


}
