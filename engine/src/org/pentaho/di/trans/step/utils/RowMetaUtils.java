package org.pentaho.di.trans.step.utils;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class RowMetaUtils {

  public static RowMetaInterface getRowMetaForUpdate( RowMetaInterface prev, String[] keyLookup, String[] keyStream,
      String[] updateLookup, String[] updateStream ) throws KettleStepException {
    RowMetaInterface tableFields = new RowMeta();

    // Now change the field names
    // the key fields
    if ( keyLookup != null ) {
      for ( int i = 0; i < keyLookup.length; i++ ) {
        ValueMetaInterface v = prev.searchValueMeta( keyStream[i] );
        if ( v != null ) {
          ValueMetaInterface tableField = v.clone();
          tableField.setName( keyLookup[i] );
          tableFields.addValueMeta( tableField );
        } else {
          throw new KettleStepException( "Unable to find field [" + keyStream[i] + "] in the input rows" );
        }
      }
    }
    // the lookup fields
    for ( int i = 0; i < updateLookup.length; i++ ) {
      ValueMetaInterface v = prev.searchValueMeta( updateStream[i] );
      if ( v != null ) {
        ValueMetaInterface vk = tableFields.searchValueMeta( updateLookup[i] );
        if ( vk == null ) { // do not add again when already added as key fields
          ValueMetaInterface tableField = v.clone();
          tableField.setName( updateLookup[i] );
          tableFields.addValueMeta( tableField );
        }
      } else {
        throw new KettleStepException( "Unable to find field [" + updateStream[i] + "] in the input rows" );
      }
    }
    return tableFields;
  }

}
