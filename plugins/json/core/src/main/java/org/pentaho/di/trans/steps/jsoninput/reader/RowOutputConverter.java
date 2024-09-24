/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
