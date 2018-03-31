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

package org.pentaho.di.trans.steps.selectvalues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleConversionException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Select, re-order, remove or change the meta-data of the fields in the inputstreams.
 *
 * @author Matt
 * @since 5-apr-2003
 */
public class SelectValues extends BaseStep implements StepInterface {
  private static Class<?> PKG = SelectValuesMeta.class; // for i18n purposes, needed by Translator2!!

  private SelectValuesMeta meta;
  private SelectValuesData data;

  public SelectValues( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                       Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Only select the values that are still needed...
   * <p/>
   * Put the values in the right order...
   * <p/>
   * Change the meta-data information if needed...
   * <p/>
   *
   * @param row The row to manipulate
   * @return true if everything went well, false if we need to stop because of an error!
   */
  private synchronized Object[] selectValues( RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    if ( data.firstselect ) {
      data.firstselect = false;

      // We need to create a new meta-data row to drive the output
      // We also want to know the indexes of the selected fields in the source row.
      //
      data.fieldnrs = new int[ meta.getSelectFields().length ];
      for ( int i = 0; i < data.fieldnrs.length; i++ ) {
        data.fieldnrs[ i ] = rowMeta.indexOfValue( meta.getSelectFields()[ i ].getName() );
        if ( data.fieldnrs[ i ] < 0 ) {
          logError( BaseMessages.getString( PKG, "SelectValues.Log.CouldNotFindField", meta.getSelectFields()[i]
              .getName() ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      }

      // Check for doubles in the selected fields... AFTER renaming!!
      //
      int[] cnt = new int[ meta.getSelectFields().length ];
      for ( int i = 0; i < meta.getSelectFields().length; i++ ) {
        cnt[ i ] = 0;
        for ( int j = 0; j < meta.getSelectFields().length; j++ ) {
          String one = Const.NVL( meta.getSelectFields()[ i ].getRename(), meta.getSelectFields()[ i ].getName() );
          String two = Const.NVL( meta.getSelectFields()[ j ].getRename(), meta.getSelectFields()[ j ].getName() );
          if ( one.equals( two ) ) {
            cnt[ i ]++;
          }

          if ( cnt[ i ] > 1 ) {
            logError( BaseMessages.getString( PKG, "SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice", one ) );
            setErrors( 1 );
            stopAll();
            return null;
          }
        }
      }

      // See if we need to include (and sort) the non-specified fields as well...
      //
      if ( meta.isSelectingAndSortingUnspecifiedFields() ) {
        // Select the unspecified fields.
        // Sort the fields
        // Add them after the specified fields...
        //
        List<String> extra = new ArrayList<>();
        ArrayList<Integer> unspecifiedKeyNrs = new ArrayList<>();
        for ( int i = 0; i < rowMeta.size(); i++ ) {
          String fieldName = rowMeta.getValueMeta( i ).getName();
          if ( Const.indexOfString( fieldName, meta.getSelectName() ) < 0 ) {
            extra.add( fieldName );
          }
        }
        Collections.sort( extra );
        for ( String fieldName : extra ) {
          int index = rowMeta.indexOfValue( fieldName );
          unspecifiedKeyNrs.add( index );
        }

        // Create the extra field list...
        //
        data.extraFieldnrs = new int[ unspecifiedKeyNrs.size() ];
        for ( int i = 0; i < data.extraFieldnrs.length; i++ ) {
          data.extraFieldnrs[ i ] = unspecifiedKeyNrs.get( i );
        }
      } else {
        data.extraFieldnrs = new int[] {};
      }
    }

    // Create a new output row
    Object[] outputData = new Object[ data.selectRowMeta.size() ];
    int outputIndex = 0;

    // Get the field values
    //
    for ( int idx : data.fieldnrs ) {
      // Normally this can't happen, except when streams are mixed with different
      // number of fields.
      //
      if ( idx < rowMeta.size() ) {
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( idx );

        // TODO: Clone might be a 'bit' expensive as it is only needed in case you want to copy a single field to 2 or
        // more target fields.
        // And even then it is only required for the last n-1 target fields.
        // Perhaps we can consider the requirements for cloning at init(), store it in a boolean[] and just consider
        // this at runtime
        //
        outputData[ outputIndex++ ] = valueMeta.cloneValueData( rowData[ idx ] );
      } else {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SelectValues.Log.MixingStreamWithDifferentFields" ) );
        }
      }
    }

    // Do we need to drag the rest of the row also in there?
    //
    for ( int idx : data.extraFieldnrs ) {
      outputData[ outputIndex++ ] = rowData[ idx ]; // always just a copy, can't be specified twice.
    }

    return outputData;
  }

  /**
   * Remove the values that are no longer needed.
   * <p/>
   *
   * @param row The row to manipulate
   * @return true if everything went well, false if we need to stop because of an error!
   */
  private synchronized Object[] removeValues( RowMetaInterface rowMeta, Object[] rowData ) {
    if ( data.firstdeselect ) {
      data.firstdeselect = false;

      data.removenrs = new int[ meta.getDeleteName().length ];
      for ( int i = 0; i < data.removenrs.length; i++ ) {
        data.removenrs[ i ] = rowMeta.indexOfValue( meta.getDeleteName()[ i ] );
        if ( data.removenrs[ i ] < 0 ) {
          logError( BaseMessages.getString( PKG, "SelectValues.Log.CouldNotFindField", meta.getDeleteName()[ i ] ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      }

      // Check for doubles in the selected fields...
      int[] cnt = new int[ meta.getDeleteName().length ];
      for ( int i = 0; i < meta.getDeleteName().length; i++ ) {
        cnt[ i ] = 0;
        for ( int j = 0; j < meta.getDeleteName().length; j++ ) {
          if ( meta.getDeleteName()[ i ].equals( meta.getDeleteName()[ j ] ) ) {
            cnt[ i ]++;
          }

          if ( cnt[ i ] > 1 ) {
            logError( BaseMessages.getString( PKG, "SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice2",
              meta.getDeleteName()[ i ] ) );
            setErrors( 1 );
            stopAll();
            return null;
          }
        }
      }

      // Sort removenrs descending. So that we can delete in ascending order...
      Arrays.sort( data.removenrs );
    }

    /*
     * Remove the field values Take into account that field indexes change once you remove them!!! Therefore removenrs
     * is sorted in reverse on index...
     */
    return RowDataUtil.removeItems( rowData, data.removenrs );
  }

  /**
   * Change the meta-data of certain fields.
   * <p/>
   * This, we can do VERY fast.
   * <p/>
   *
   * @param row The row to manipulate
   * @return the altered RowData array
   * @throws KettleValueException
   */
  @VisibleForTesting
  synchronized Object[] metadataValues( RowMetaInterface rowMeta, Object[] rowData ) throws KettleException {
    if ( data.firstmetadata ) {
      data.firstmetadata = false;

      data.metanrs = new int[ meta.getMeta().length ];
      for ( int i = 0; i < data.metanrs.length; i++ ) {
        data.metanrs[ i ] = rowMeta.indexOfValue( meta.getMeta()[ i ].getName() );
        if ( data.metanrs[ i ] < 0 ) {
          logError( BaseMessages
            .getString( PKG, "SelectValues.Log.CouldNotFindField", meta.getMeta()[ i ].getName() ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      }

      // Check for doubles in the selected fields...
      int[] cnt = new int[ meta.getMeta().length ];
      for ( int i = 0; i < meta.getMeta().length; i++ ) {
        cnt[ i ] = 0;
        for ( int j = 0; j < meta.getMeta().length; j++ ) {
          if ( meta.getMeta()[ i ].getName().equals( meta.getMeta()[ j ].getName() ) ) {
            cnt[ i ]++;
          }

          if ( cnt[ i ] > 1 ) {
            logError( BaseMessages.getString( PKG, "SelectValues.Log.FieldCouldNotSpecifiedMoreThanTwice2", meta
              .getMeta()[ i ].getName() ) );
            setErrors( 1 );
            stopAll();
            return null;
          }
        }
      }

      // Also apply the metadata on the row meta to allow us to convert the data correctly, with the correct mask.
      //
      for ( int i = 0; i < data.metanrs.length; i++ ) {
        SelectMetadataChange change = meta.getMeta()[ i ];
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.metanrs[ i ] );
        if ( !Utils.isEmpty( change.getConversionMask() ) ) {
          valueMeta.setConversionMask( change.getConversionMask() );
        }

        valueMeta.setDateFormatLenient( change.isDateFormatLenient() );
        valueMeta.setDateFormatLocale( EnvUtil.createLocale( change.getDateFormatLocale() ) );
        valueMeta.setDateFormatTimeZone( EnvUtil.createTimeZone( change.getDateFormatTimeZone() ) );
        valueMeta.setLenientStringToNumber( change.isLenientStringToNumber() );

        if ( !Utils.isEmpty( change.getEncoding() ) ) {
          valueMeta.setStringEncoding( change.getEncoding() );
        }
        if ( !Utils.isEmpty( change.getDecimalSymbol() ) ) {
          valueMeta.setDecimalSymbol( change.getDecimalSymbol() );
        }
        if ( !Utils.isEmpty( change.getGroupingSymbol() ) ) {
          valueMeta.setGroupingSymbol( change.getGroupingSymbol() );
        }
        if ( !Utils.isEmpty( change.getCurrencySymbol() ) ) {
          valueMeta.setCurrencySymbol( change.getCurrencySymbol() );
        }
      }
    }

    //
    // Change the data too
    //
    for ( int i = 0; i < data.metanrs.length; i++ ) {
      int index = data.metanrs[ i ];
      ValueMetaInterface fromMeta = rowMeta.getValueMeta( index );
      ValueMetaInterface toMeta = data.metadataRowMeta.getValueMeta( index );

      // If we need to change from BINARY_STRING storage type to NORMAL...
      //
      try {
        if ( fromMeta.isStorageBinaryString()
          && meta.getMeta()[ i ].getStorageType() == ValueMetaInterface.STORAGE_TYPE_NORMAL ) {
          rowData[ index ] = fromMeta.convertBinaryStringToNativeType( (byte[]) rowData[ index ] );
        }
        if ( meta.getMeta()[ i ].getType() != ValueMetaInterface.TYPE_NONE && fromMeta.getType() != toMeta.getType() ) {
          rowData[ index ] = toMeta.convertData( fromMeta, rowData[ index ] );
        }
      } catch ( KettleValueException e ) {
        throw new KettleConversionException( e.getMessage(), Collections.<Exception>singletonList( e ),
          Collections.singletonList( toMeta ), rowData );
      }
    }

    return rowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SelectValuesMeta) smi;
    data = (SelectValuesData) sdi;

    Object[] rowData = getRow(); // get row from rowset, wait for our turn, indicate busy!
    if ( rowData == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    Object[] rowCopy = null;
    if ( getStepMeta().isDoingErrorHandling() ) {
      rowCopy = getInputRowMeta().cloneRow( rowData );
    }

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "SelectValues.Log.GotRowFromPreviousStep" )
        + getInputRowMeta().getString( rowData ) );
    }

    if ( first ) {
      first = false;

      data.selectRowMeta = getInputRowMeta().clone();
      meta.getSelectFields( data.selectRowMeta, getStepname() );
      data.deselectRowMeta = data.selectRowMeta.clone();
      meta.getDeleteFields( data.deselectRowMeta );
      data.metadataRowMeta = data.deselectRowMeta.clone();
      meta.getMetadataFields( data.metadataRowMeta, getStepname(), this );
    }

    try {
      Object[] outputData = rowData;

      if ( data.select ) {
        outputData = selectValues( getInputRowMeta(), outputData );
      }
      if ( data.deselect ) {
        outputData = removeValues( data.selectRowMeta, outputData );
      }
      if ( data.metadata ) {
        outputData = metadataValues( data.deselectRowMeta, outputData );
      }

      if ( outputData == null ) {
        setOutputDone(); // signal end to receiver(s)
        return false;
      }

      // Send the row on its way
      //
      putRow( data.metadataRowMeta, outputData );
      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "SelectValues.Log.WroteRowToNextStep" )
          + data.metadataRowMeta.getString( outputData ) );
      }

    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        String field;
        if ( e instanceof KettleConversionException ) {
          List<ValueMetaInterface> fields = ( (KettleConversionException) e ).getFields();
          field = fields.isEmpty() ? null : fields.get( 0 ).getName();
        } else {
          field = null;
        }
        putError( getInputRowMeta(), rowCopy, 1, e.getMessage(), field, "SELECT001" );
      } else {
        throw e;
      }
    }

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "SelectValues.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SelectValuesMeta) smi;
    data = (SelectValuesData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.firstselect = true;
      data.firstdeselect = true;
      data.firstmetadata = true;

      data.select = false;
      data.deselect = false;
      data.metadata = false;

      if ( !Utils.isEmpty( meta.getSelectFields() ) ) {
        data.select = true;
      }
      if ( !Utils.isEmpty( meta.getDeleteName() ) ) {
        data.deselect = true;
      }
      if ( !Utils.isEmpty( meta.getMeta() ) ) {
        data.metadata = true;
      }

      boolean atLeastOne = data.select || data.deselect || data.metadata;
      if ( !atLeastOne ) {
        setErrors( 1 );
        logError( BaseMessages.getString( PKG, "SelectValues.Log.InputShouldContainData" ) );
      }

      return atLeastOne; // One of those three has to work!
    } else {
      return false;
    }
  }

}
