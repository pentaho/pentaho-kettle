/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.reader;

import java.util.Map;

import net.minidev.json.JSONObject;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.jsoninput.JsonInputData;

/**
 * Converts raw reader to output row values
 */
public class RowOutputConverter {

  private final LogChannelInterface log;

  public RowOutputConverter( LogChannelInterface log ) {
    this.log = log;
  }

  private Object getValue( ValueMetaInterface targetMeta, ValueMetaInterface strConvertMeta, Object value )
      throws KettleValueException {
    if ( targetMeta.isNumeric() ) {
      try {
        // attempt direct conversion
        return targetMeta.getNativeDataType( value );
      } catch ( KettleValueException e ) {
        if ( log.isDebug() ) {
          log.logDebug( e.getLocalizedMessage(), e );
        }
      }
    }
    // convert from string
    String strValue = getStringValue( value );
    return targetMeta.convertDataFromString( strValue, strConvertMeta, null, null, targetMeta.getTrimType() );
  }

  private String getStringValue( Object jo ) {
    String nodevalue = null;
    if ( jo != null ) {
      if ( jo instanceof Map ) {
        @SuppressWarnings( "unchecked" )
        Map<String, ?> asStrMap = (Map<String, ?>) jo;
        nodevalue = JSONObject.toJSONString( asStrMap );
      } else {
        nodevalue = jo.toString();
      }
    }
    return nodevalue;
  }

  public Object[] getRow( Object[] baseOutputRow, Object[] rawPartRow, JsonInputData data ) throws KettleException {
    if ( rawPartRow == null ) {
      return null;
    }
    for ( int i = 0; i < rawPartRow.length; i++ ) {
      int outIdx = data.totalpreviousfields + i;
      Object val =
          getValue( data.outputRowMeta.getValueMeta( outIdx ), data.convertRowMeta.getValueMeta( outIdx ),
              rawPartRow[i] );
      rawPartRow[i] = val;
      if ( val == null && data.repeatedFields.get( i ) && data.previousRow != null ) {
        rawPartRow[i] = data.previousRow[outIdx];
      }
    }
    data.previousRow = RowDataUtil.addRowData( baseOutputRow, data.totalpreviousfields, rawPartRow );
    return data.previousRow;
  }

}
