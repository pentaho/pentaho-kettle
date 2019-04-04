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

package org.pentaho.di.trans.steps.denormaliser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Denormalises data based on key-value pairs
 *
 * @author Matt
 * @since 17-jan-2006
 */
public class Denormaliser extends BaseStep implements StepInterface {
  private static Class<?> PKG = DenormaliserMeta.class; // for i18n purposes, needed by Translator2!!

  private DenormaliserMeta meta;
  private DenormaliserData data;
  private boolean allNullsAreZero = false;
  private boolean minNullIsValued = false;

  private Map<String, ValueMetaInterface> conversionMetaCache = new HashMap<String, ValueMetaInterface>();

  public Denormaliser( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (DenormaliserMeta) getStepMeta().getStepMetaInterface();
    data = (DenormaliserData) stepDataInterface;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = getRow(); // get row!

    if ( r == null ) {
      // no more input to be expected...
      handleLastRow();
      setOutputDone();
      return false;
    }

    if ( first ) {
      // perform all allocations
      if ( !processFirstRow() ) {
        // we failed on first row....
        return false;
      }

      newGroup(); // Create a new result row (init)
      deNormalise( data.inputRowMeta, r );
      data.previous = r; // copy the row to previous

      // we don't need feedback here
      first = false;

      // ok, we done with first row
      return true;
    }

    if ( !sameGroup( data.inputRowMeta, data.previous, r ) ) {

      Object[] outputRowData = buildResult( data.inputRowMeta, data.previous );
      putRow( data.outputRowMeta, outputRowData ); // copy row to possible alternate rowset(s).
      newGroup(); // Create a new group aggregate (init)
      deNormalise( data.inputRowMeta, r );
    } else {
      deNormalise( data.inputRowMeta, r );
    }

    data.previous = r;

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "Denormaliser.Log.LineNumber" ) + getLinesRead() );
      }
    }

    return true;
  }

  private boolean processFirstRow() throws KettleStepException {
    String val = getVariable( Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "N" );
    this.allNullsAreZero = ValueMetaBase.convertStringToBoolean( val );
    val = getVariable( Const.KETTLE_AGGREGATION_MIN_NULL_IS_VALUED, "N" );
    this.minNullIsValued = ValueMetaBase.convertStringToBoolean( val );
    data.inputRowMeta = getInputRowMeta();
    data.outputRowMeta = data.inputRowMeta.clone();
    meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

    data.keyFieldNr = data.inputRowMeta.indexOfValue( meta.getKeyField() );
    if ( data.keyFieldNr < 0 ) {
      logError( BaseMessages.getString( PKG, "Denormaliser.Log.KeyFieldNotFound", meta.getKeyField() ) );
      setErrors( 1 );
      stopAll();
      return false;
    }

    Map<Integer, Integer> subjects = new Hashtable<Integer, Integer>();
    data.fieldNameIndex = new int[meta.getDenormaliserTargetField().length];
    for ( int i = 0; i < meta.getDenormaliserTargetField().length; i++ ) {
      DenormaliserTargetField field = meta.getDenormaliserTargetField()[i];
      int idx = data.inputRowMeta.indexOfValue( field.getFieldName() );
      if ( idx < 0 ) {
        logError( BaseMessages.getString( PKG, "Denormaliser.Log.UnpivotFieldNotFound", field.getFieldName() ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
      data.fieldNameIndex[i] = idx;
      subjects.put( Integer.valueOf( idx ), Integer.valueOf( idx ) );

      // See if by accident, the value fieldname isn't the same as the key fieldname.
      // This is not supported of-course and given the complexity of the step, you can miss:
      if ( data.fieldNameIndex[i] == data.keyFieldNr ) {
        logError( BaseMessages.getString( PKG, "Denormaliser.Log.ValueFieldSameAsKeyField", field.getFieldName() ) );
        setErrors( 1 );
        stopAll();
        return false;
      }

      // Fill a hashtable with the key strings and the position(s) of the field(s) in the row to take.
      // Store the indexes in a List so that we can accommodate multiple key/value pairs...
      //
      String keyValue = environmentSubstitute( field.getKeyValue() );
      List<Integer> indexes = data.keyValue.get( keyValue );
      if ( indexes == null ) {
        indexes = new ArrayList<Integer>( 2 );
      }
      indexes.add( Integer.valueOf( i ) ); // Add the index to the list...
      data.keyValue.put( keyValue, indexes ); // store the list
    }

    Set<Integer> subjectSet = subjects.keySet();
    data.fieldNrs = subjectSet.toArray( new Integer[subjectSet.size()] );

    data.groupnrs = new int[meta.getGroupField().length];
    for ( int i = 0; i < meta.getGroupField().length; i++ ) {
      data.groupnrs[i] = data.inputRowMeta.indexOfValue( meta.getGroupField()[i] );
      if ( data.groupnrs[i] < 0 ) {
        logError( BaseMessages.getString( PKG, "Denormaliser.Log.GroupingFieldNotFound", meta.getGroupField()[i] ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
    }

    List<Integer> removeList = new ArrayList<Integer>();
    removeList.add( Integer.valueOf( data.keyFieldNr ) );
    for ( int i = 0; i < data.fieldNrs.length; i++ ) {
      removeList.add( data.fieldNrs[i] );
    }
    Collections.sort( removeList );

    data.removeNrs = new int[removeList.size()];
    for ( int i = 0; i < removeList.size(); i++ ) {
      data.removeNrs[i] = removeList.get( i );
    }
    return true;
  }

  private void handleLastRow() throws KettleException {
    // Don't forget the last set of rows...
    if ( data.previous != null ) {
      // deNormalise(data.previous); --> That would over-do it.
      //
      Object[] outputRowData = buildResult( data.inputRowMeta, data.previous );
      putRow( data.outputRowMeta, outputRowData );
    }
  }

  /**
   * Used for junits in DenormaliserAggregationsTest
   *
   * @param rowMeta
   * @param rowData
   * @return
   * @throws KettleValueException
   */
  Object[] buildResult( RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    // Deleting objects: we need to create a new object array
    // It's useless to call RowDataUtil.resizeArray
    //
    Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    int outputIndex = 0;

    // Copy the data from the incoming row, but remove the unwanted fields in the same loop...
    //
    int removeIndex = 0;
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      if ( removeIndex < data.removeNrs.length && i == data.removeNrs[removeIndex] ) {
        removeIndex++;
      } else {
        outputRowData[outputIndex++] = rowData[i];
      }
    }

    // Add the unpivoted fields...
    //
    for ( int i = 0; i < data.targetResult.length; i++ ) {
      Object resultValue = data.targetResult[i];
      DenormaliserTargetField field = meta.getDenormaliserTargetField()[i];
      switch ( field.getTargetAggregationType() ) {
        case DenormaliserTargetField.TYPE_AGGR_AVERAGE:
          long count = data.counters[i];
          Object sum = data.sum[i];
          if ( count > 0 ) {
            if ( sum instanceof Long ) {
              resultValue = (Long) sum / count;
            } else if ( sum instanceof Double ) {
              resultValue = (Double) sum / count;
            } else if ( sum instanceof BigDecimal ) {
              resultValue = ( (BigDecimal) sum ).divide( new BigDecimal( count ) );
            } else {
              resultValue = null; // TODO: perhaps throw an exception here?<
            }
          }
          break;
        case DenormaliserTargetField.TYPE_AGGR_COUNT_ALL:
          if ( resultValue == null ) {
            resultValue = Long.valueOf( 0 );
          }
          if ( field.getTargetType() != ValueMetaInterface.TYPE_INTEGER ) {
            resultValue =
                data.outputRowMeta.getValueMeta( outputIndex ).convertData(
                    new ValueMetaInteger( "num_values_aggregation" ), resultValue );
          }
          break;
        default:
          break;
      }
      if ( resultValue == null && allNullsAreZero ) {
        // PDI-9662 seems all rows for min function was nulls...
        resultValue = getZero( outputIndex );
      }
      outputRowData[outputIndex++] = resultValue;
    }

    return outputRowData;
  }

  private Object getZero( int field ) throws KettleValueException {
    ValueMetaInterface vm = data.outputRowMeta.getValueMeta( field );
    return ValueDataUtil.getZeroForValueMetaType( vm );
  }

  // Is the row r of the same group as previous?
  private boolean sameGroup( RowMetaInterface rowMeta, Object[] previous, Object[] rowData ) throws KettleValueException {
    return rowMeta.compare( previous, rowData, data.groupnrs ) == 0;
  }

  /**
   * Initialize a new group...
   *
   * @throws KettleException
   */
  private void newGroup( ) throws KettleException {
    // There is no need anymore to take care of the meta-data.
    // That is done once in DenormaliserMeta.getFields()
    //
    data.targetResult = new Object[meta.getDenormaliserTargetFields().length];

    DenormaliserTargetField[] fields = meta.getDenormaliserTargetField();

    for ( int i = 0; i < fields.length; i++ ) {
      data.counters[i] = 0L; // set to 0
      data.sum[i] = null;
    }
  }

  /**
   * This method de-normalizes a single key-value pair. It looks up the key and determines the value name to store it
   * in. It converts it to the right type and stores it in the result row.
   *
   * Used for junits in DenormaliserAggregationsTest
   *
   * @param r
   * @throws KettleValueException
   */
  void deNormalise( RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.keyFieldNr );
    Object valueData = rowData[data.keyFieldNr];

    String key = valueMeta.getCompatibleString( valueData );
    if ( Utils.isEmpty( key ) ) {
      return;
    }
    // Get all the indexes for the given key value...
    //
    List<Integer> indexes = data.keyValue.get( key );
    if ( indexes == null ) { // otherwise we're not interested.
      return;
    }

    for ( Integer keyNr : indexes ) {
      if ( keyNr == null ) {
        continue;
      }
      // keyNr is the field in DenormaliserTargetField[]
      //
      int idx = keyNr.intValue();
      DenormaliserTargetField field = meta.getDenormaliserTargetField()[idx];

      // This is the value we need to de-normalise, convert, aggregate.
      //
      ValueMetaInterface sourceMeta = rowMeta.getValueMeta( data.fieldNameIndex[idx] );
      Object sourceData = rowData[data.fieldNameIndex[idx]];
      Object targetData;
      // What is the target value metadata??
      //
      ValueMetaInterface targetMeta =
          data.outputRowMeta.getValueMeta( data.inputRowMeta.size() - data.removeNrs.length + idx );
      // What was the previous target in the result row?
      //
      Object prevTargetData = data.targetResult[idx];

      // clone source meta as it can be used by other steps ans set conversion meta
      // to convert date to target format
      // See PDI-4910 for details
      ValueMetaInterface origSourceMeta = sourceMeta;
      if ( targetMeta.isDate() ) {
        sourceMeta = origSourceMeta.clone();
        sourceMeta.setConversionMetadata( getConversionMeta( field.getTargetFormat() ) );
      }

      switch ( field.getTargetAggregationType() ) {
        case DenormaliserTargetField.TYPE_AGGR_SUM:
          targetData = targetMeta.convertData( sourceMeta, sourceData );
          if ( prevTargetData != null ) {
            prevTargetData = ValueDataUtil.sum( targetMeta, prevTargetData, targetMeta, targetData );
          } else {
            prevTargetData = targetData;
          }
          break;
        case DenormaliserTargetField.TYPE_AGGR_MIN:
          if ( sourceData == null && !minNullIsValued ) {
            // PDI-9662 do not compare null
            break;
          }
          if ( ( prevTargetData == null && !minNullIsValued )
                  || sourceMeta.compare( sourceData, targetMeta, prevTargetData ) < 0 ) {
            prevTargetData = targetMeta.convertData( sourceMeta, sourceData );
          }
          break;
        case DenormaliserTargetField.TYPE_AGGR_MAX:
          if ( sourceMeta.compare( sourceData, targetMeta, prevTargetData ) > 0 ) {
            prevTargetData = targetMeta.convertData( sourceMeta, sourceData );
          }
          break;
        case DenormaliserTargetField.TYPE_AGGR_COUNT_ALL:
          prevTargetData = ++data.counters[idx];
          break;
        case DenormaliserTargetField.TYPE_AGGR_AVERAGE:
          targetData = targetMeta.convertData( sourceMeta, sourceData );
          if ( !sourceMeta.isNull( sourceData ) ) {
            prevTargetData = data.counters[idx]++;
            if ( data.sum[idx] == null ) {
              data.sum[idx] = targetData;
            } else {
              data.sum[idx] = ValueDataUtil.plus( targetMeta, data.sum[idx], targetMeta, targetData );
            }
            // data.sum[idx] = (Integer)data.sum[idx] + (Integer)sourceData;
          }
          break;
        case DenormaliserTargetField.TYPE_AGGR_CONCAT_COMMA:
          String separator = ",";

          targetData = targetMeta.convertData( sourceMeta, sourceData );
          if ( prevTargetData != null ) {
            prevTargetData = prevTargetData + separator + targetData;
          } else {
            prevTargetData = targetData;
          }
          break;
        case DenormaliserTargetField.TYPE_AGGR_NONE:
        default:
          prevTargetData = targetMeta.convertData( sourceMeta, sourceData ); // Overwrite the previous
          break;
      }

      // Update the result row too
      //
      data.targetResult[idx] = prevTargetData;
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DenormaliserMeta) smi;
    data = (DenormaliserData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.counters = new long[meta.getDenormaliserTargetField().length];
      data.sum = new Object[meta.getDenormaliserTargetField().length];

      return true;
    }
    return false;
  }

  @Override
  public void batchComplete() throws KettleException {
    handleLastRow();
    data.previous = null;
  }

  /**
   * Get the metadata used for conversion to date format See related PDI-4019
   *
   * @param mask
   * @return
   */
  private ValueMetaInterface getConversionMeta( String mask ) {
    ValueMetaInterface meta = null;
    if ( !Utils.isEmpty( mask ) ) {
      meta = conversionMetaCache.get( mask );
      if ( meta == null ) {
        meta = new ValueMetaDate();
        meta.setConversionMask( mask );
        conversionMetaCache.put( mask, meta );
      }
    }
    return meta;
  }

  /**
   * Used for junits in DenormaliserAggregationsTest
   *
   * @param allNullsAreZero
   *          the allNullsAreZero to set
   */
  void setAllNullsAreZero( boolean allNullsAreZero ) {
    this.allNullsAreZero = allNullsAreZero;
  }

  /**
   * Used for junits in DenormaliserAggregationsTest
   *
   * @param minNullIsValued
   *          the minNullIsValued to set
   */
  void setMinNullIsValued( boolean minNullIsValued ) {
    this.minNullIsValued = minNullIsValued;
  }

}
