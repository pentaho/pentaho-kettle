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

package org.pentaho.di.trans.steps.valuemapper;

import java.util.Hashtable;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Convert Values in a certain fields to other values
 *
 * @author Matt
 * @since 3-apr-2006
 */
public class ValueMapper extends BaseStep implements StepInterface {
  private static Class<?> PKG = ValueMapperMeta.class; // for i18n purposes, needed by Translator2!!

  private ValueMapperMeta meta;
  private ValueMapperData data;
  private boolean nonMatchActivated = false;

  public ValueMapper( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ValueMapperMeta) smi;
    data = (ValueMapperData) sdi;

    // Get one row from one of the rowsets...
    //
    Object[] r = getRow();
    if ( r == null ) { // means: no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.previousMeta = getInputRowMeta().clone();
      data.outputMeta = data.previousMeta.clone();
      meta.getFields( data.outputMeta, getStepname(), null, null, this, repository, metaStore );

      data.keynr = data.previousMeta.indexOfValue( meta.getFieldToUse() );
      if ( data.keynr < 0 ) {
        String message =
          BaseMessages.getString( PKG, "ValueMapper.RuntimeError.FieldToUseNotFound.VALUEMAPPER0001", meta
            .getFieldToUse(), Const.CR, getInputRowMeta().getString( r ) );
        logError( message );
        setErrors( 1 );
        stopAll();
        return false;
      }

      // If there is an empty entry: we map null or "" to the target at the index
      // 0 or 1 empty mapping is allowed, not 2 or more.
      //
      for ( int i = 0; i < meta.getSourceValue().length; i++ ) {
        if ( Utils.isEmpty( meta.getSourceValue()[i] ) ) {
          if ( data.emptyFieldIndex < 0 ) {
            data.emptyFieldIndex = i;
          } else {
            throw new KettleException( BaseMessages.getString(
              PKG, "ValueMapper.RuntimeError.OnlyOneEmptyMappingAllowed.VALUEMAPPER0004" ) );
          }
        }
      }

      data.sourceValueMeta = getInputRowMeta().getValueMeta( data.keynr );

      if ( Utils.isEmpty( meta.getTargetField() ) ) {
        data.outputValueMeta = data.outputMeta.getValueMeta( data.keynr ); // Same field

      } else {
        data.outputValueMeta = data.outputMeta.searchValueMeta( meta.getTargetField() ); // new field
      }
    }

    Object sourceData = r[data.keynr];
    String source = data.sourceValueMeta.getCompatibleString( sourceData );
    String target = null;

    // Null/Empty mapping to value...
    //
    if ( data.emptyFieldIndex >= 0 && ( r[data.keynr] == null || Utils.isEmpty( source ) ) ) {
      target = meta.getTargetValue()[data.emptyFieldIndex]; // that's all there is to it.
    } else {
      if ( !Utils.isEmpty( source ) ) {
        target = data.hashtable.get( source );
        if ( nonMatchActivated && target == null ) {
          // If we do non matching and we don't have a match
          target = meta.getNonMatchDefault();
        }
      }
    }

    if ( !Utils.isEmpty( meta.getTargetField() ) ) {
      // room for the target
      r = RowDataUtil.resizeArray( r, data.outputMeta.size() );
      // Did we find anything to map to?
      if ( !Utils.isEmpty( target ) ) {
        r[data.outputMeta.size() - 1] = target;
      } else {
        r[data.outputMeta.size() - 1] = null;
      }
    } else {
      // Don't set the original value to null if we don't have a target.
      if ( target != null ) {
        if ( target.length() > 0 ) {
          // See if the expected type is a String...
          //
          if ( data.sourceValueMeta.isString() ) {
            r[data.keynr] = target;
          } else {
            // Do implicit conversion of the String to the target type...
            //
            r[data.keynr] = data.outputValueMeta.convertData( data.stringMeta, target );
          }
        } else {
          // allow target to be set to null since 3.0
          r[data.keynr] = null;
        }
      } else {
        // Convert to normal storage type.
        // Otherwise we're going to be mixing storage types.
        //
        if ( data.sourceValueMeta.isStorageBinaryString() ) {
          Object normal = data.sourceValueMeta.convertToNormalStorageType( r[data.keynr] );
          r[data.keynr] = normal;
        }
      }
    }
    putRow( data.outputMeta, r );

    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ValueMapperMeta) smi;
    data = (ValueMapperData) sdi;

    super.dispose( smi, sdi );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ValueMapperMeta) smi;
    data = (ValueMapperData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.hashtable = new Hashtable<String, String>();
      data.emptyFieldIndex = -1;

      if ( !Utils.isEmpty( meta.getNonMatchDefault() ) ) {
        nonMatchActivated = true;
      }

      // Add all source to target mappings in here...
      for ( int i = 0; i < meta.getSourceValue().length; i++ ) {
        String src = meta.getSourceValue()[i];
        String tgt = meta.getTargetValue()[i];

        if ( !Utils.isEmpty( src ) && !Utils.isEmpty( tgt ) ) {
          data.hashtable.put( src, tgt );
        } else {
          if ( Utils.isEmpty( tgt ) ) {
            // allow target to be set to null since 3.0
            data.hashtable.put( src, "" );
          }
        }
      }
      return true;
    }
    return false;
  }

}
