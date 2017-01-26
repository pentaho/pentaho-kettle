package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.engine.api.model.IRow;
import org.pentaho.di.engine.api.converter.IRowConverter;

import java.util.Optional;

public class SparkRowConverter implements IRowConverter<RowMetaInterface> {
  @Override public Optional<RowMetaInterface> convert( IRow row, Class<RowMetaInterface> type ) {
    if ( !(row instanceof KettleRow) && type.equals( RowMetaInterface.class ) ) {
      // dummy conversion for now.  Should attempt to infer rowmeta based on contents.
      RowMetaInterface rowMetaInterface = new RowMeta();
      rowMetaInterface.addValueMeta( new ValueMetaString( "name" ) );
      return Optional.of( rowMetaInterface );
    }
    return Optional.empty();
  }

}
