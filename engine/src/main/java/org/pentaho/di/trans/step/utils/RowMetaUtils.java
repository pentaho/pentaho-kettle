/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
