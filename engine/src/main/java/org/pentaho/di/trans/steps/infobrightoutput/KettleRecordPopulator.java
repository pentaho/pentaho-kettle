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

package org.pentaho.di.trans.steps.infobrightoutput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.infobright.etl.model.BrighthouseRecord;
import com.infobright.etl.model.ValueConverterException;

/**
 * @author geoffrey.falk@infobright.com
 */
class KettleRecordPopulator {

  private KettleValueConverter[] conv = null;

  public void populate( BrighthouseRecord record, Object[] row, RowMetaInterface rowMeta ) throws KettleException {

    // assume row metadata is same for all rows
    if ( conv == null ) {
      if ( record.size() != rowMeta.size() ) {
        throw new KettleException( "Number of columns passed to Infobright "
          + "doesn't match the table definition!" );
      }
      init( rowMeta );
    }

    for ( int colidx = 0; colidx < record.size(); colidx++ ) {
      Object value = row[colidx];
      try {
        record.setData( colidx, value, conv[colidx] );
      } catch ( ValueConverterException e ) {
        Throwable cause = e.getCause();
        if ( cause instanceof KettleException ) {
          throw (KettleException) cause;
        } else if ( cause instanceof RuntimeException ) {
          throw (RuntimeException) cause;
        } else if ( cause instanceof Error ) {
          throw (Error) cause;
        } else {
          throw e;
        }
      }
    }
  }

  private void init( RowMetaInterface rowMeta ) {
    int size = rowMeta.size();
    conv = new KettleValueConverter[size];
    for ( int colidx = 0; colidx < size; colidx++ ) {
      ValueMetaInterface meta = rowMeta.getValueMeta( colidx );
      conv[colidx] = new KettleValueConverter( meta );
    }
  }
}
