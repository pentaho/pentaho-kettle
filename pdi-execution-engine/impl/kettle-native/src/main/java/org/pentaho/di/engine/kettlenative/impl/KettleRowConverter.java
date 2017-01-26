package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.engine.api.model.IRow;
import org.pentaho.di.engine.api.converter.IRowConverter;

import java.util.Optional;

public class KettleRowConverter implements IRowConverter<RowMetaInterface> {
  @Override public Optional<RowMetaInterface> convert( IRow row, Class<RowMetaInterface> type ) {
    if ( row instanceof KettleRow && type.equals( RowMetaInterface.class ) ) {
      return Optional.of(((KettleRow) row).getRowMeta());
    }
    return Optional.empty();
  }

}
