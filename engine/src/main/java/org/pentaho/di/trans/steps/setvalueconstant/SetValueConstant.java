/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.setvalueconstant;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.List;

/**
 * Replace Field value by a constant value.
 *
 * @author Samatar
 * @since 30-06-2008
 */

public class SetValueConstant extends BaseStep implements StepInterface {
  private static Class<?> PKG = SetValueConstantMeta.class; // for i18n purposes, needed by Translator2!!

  private SetValueConstantMeta meta;
  private SetValueConstantData data;

  public SetValueConstant( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  //CHECKSTYLE:Indentation:OFF
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SetValueConstantMeta) smi;
    data = (SetValueConstantData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      // What's the format of the output row?
      data.setOutputRowMeta( getInputRowMeta().clone() );
      meta.getFields( data.getOutputRowMeta(), getStepname(), null, null, this, repository, metaStore );
      // Create convert meta-data objects that will contain Date & Number formatters
      // data.convertRowMeta = data.outputRowMeta.clone();

      // For String to <type> conversions, we allocate a conversion meta data row as well...
      //
      data.setConvertRowMeta( data.getOutputRowMeta().cloneToType( ValueMetaInterface.TYPE_STRING ) );

      // Consider only selected fields
      List<SetValueConstantMeta.Field> fields = meta.getFields();
      int size = fields.size();
      if ( !Utils.isEmpty( fields ) ) {
        data.setFieldnrs( new int[size] );
        data.setRealReplaceByValues( new String[size] );
        for ( int i = 0; i < size; i++ ) {
          // Check if this field was specified only one time
          final SetValueConstantMeta.Field check = fields.get( i );
          for ( SetValueConstantMeta.Field field : fields ) {
            if ( field.getFieldName() != null && field != check && field.getFieldName().equalsIgnoreCase( check.getFieldName() ) ) {
              throw new KettleException( BaseMessages.getString( PKG, "SetValueConstant.Log"
                      + ".FieldSpecifiedMoreThatOne", check.getFieldName() ) );
            }
          }

          data.getFieldnrs()[i] = data.getOutputRowMeta().indexOfValue( meta.getField( i ).getFieldName() );

          if ( data.getFieldnrs()[i] < 0 ) {
            logError( BaseMessages.getString( PKG, "SetValueConstant.Log.CanNotFindField", meta.getField( i ).getFieldName() ) );
            throw new KettleException( BaseMessages.getString( PKG, "SetValueConstant.Log.CanNotFindField", meta
              .getField( i ).getFieldName() ) );
          }

          if ( meta.getField( i ).isEmptyString() ) {
            // Just set empty string
            data.getRealReplaceByValues()[i] = StringUtil.EMPTY_STRING;
          } else {
            // set specified value
            if ( meta.isUseVars() ) {
              data.getRealReplaceByValues()[i] = environmentSubstitute( meta.getField( i ).getReplaceValue() );
            } else {
              data.getRealReplaceByValues()[i] = meta.getField( i ).getReplaceValue();
            }
          }
        }
      } else {
        throw new KettleException( BaseMessages.getString( PKG, "SetValueConstant.Log.SelectFieldsEmpty" ) );
      }

      data.setFieldnr( data.getFieldnrs().length );

    } // end if first

    try {
      updateField( r );
      putRow( data.getOutputRowMeta(), r ); // copy row to output rowset(s);
    } catch ( Exception e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        // Simply add this row to the error row
        putError( data.getOutputRowMeta(), r, 1, e.toString(), null, "SVC001" );
      } else {
        logError( BaseMessages.getString( PKG, "SetValueConstant.Log.ErrorInStep", e.getMessage() ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
    }
    return true;
  }

  private void updateField( Object[] r ) throws Exception {
    // Loop through fields
    for ( int i = 0; i < data.getFieldnr(); i++ ) {
      // DO CONVERSION OF THE DEFAULT VALUE ...
      // Entered by user
      ValueMetaInterface targetValueMeta = data.getOutputRowMeta().getValueMeta( data.getFieldnrs()[i] );
      ValueMetaInterface sourceValueMeta = data.getConvertRowMeta().getValueMeta( data.getFieldnrs()[i] );

      if ( !Utils.isEmpty( meta.getField( i ).getReplaceMask() ) ) {
        sourceValueMeta.setConversionMask( meta.getField( i ).getReplaceMask() );
      }

      sourceValueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
      r[data.getFieldnrs()[i]] = targetValueMeta.convertData( sourceValueMeta, data.getRealReplaceByValues()[i] );
      targetValueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SetValueConstantMeta) smi;
    data = (SetValueConstantData) sdi;

    return super.init( smi, sdi );
  }

}
