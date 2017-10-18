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

package org.pentaho.di.trans.steps.memgroupby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.memgroupby.MemoryGroupByData.HashEntry;

/**
 * Groups information based on aggregation rules. (sum, count, ...)
 *
 * @author Matt
 * @since 2-jun-2003
 */
public class MemoryGroupBy extends BaseStep implements StepInterface {
  private static Class<?> PKG = MemoryGroupByMeta.class; // for i18n purposes, needed by Translator2!!

  private MemoryGroupByMeta meta;

  private MemoryGroupByData data;

  private boolean allNullsAreZero = false;
  private boolean minNullIsValued = false;
  private boolean compatibilityMode = false;

  public MemoryGroupBy( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                        Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (MemoryGroupByMeta) getStepMeta().getStepMetaInterface();
    data = (MemoryGroupByData) stepDataInterface;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MemoryGroupByMeta) smi;
    data = (MemoryGroupByData) sdi;

    Object[] r = getRow(); // get row!

    if ( first ) {
      if ( ( r == null ) && ( !meta.isAlwaysGivingBackOneRow() ) ) {
        setOutputDone();
        return false;
      }

      String val = getVariable( Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "N" );
      allNullsAreZero = ValueMetaBase.convertStringToBoolean( val );
      val = getVariable( Const.KETTLE_AGGREGATION_MIN_NULL_IS_VALUED, "N" );
      minNullIsValued = ValueMetaBase.convertStringToBoolean( val );
      compatibilityMode = ValueMetaBase.convertStringToBoolean(
        getVariable( Const.KETTLE_COMPATIBILITY_MEMORY_GROUP_BY_SUM_AVERAGE_RETURN_NUMBER_TYPE, "N" ) );

      // What is the output looking like?
      //
      data.inputRowMeta = getInputRowMeta();

      // In case we have 0 input rows, we still want to send out a single row aggregate
      // However... the problem then is that we don't know the layout from receiving it from the previous step over the
      // row set.
      // So we need to calculated based on the metadata...
      //
      if ( data.inputRowMeta == null ) {
        data.inputRowMeta = getTransMeta().getPrevStepFields( getStepMeta() );
      }

      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Do all the work we can beforehand
      // Calculate indexes, loop up fields, etc.
      //
      data.subjectnrs = new int[meta.getSubjectField().length];
      data.groupnrs = new int[meta.getGroupField().length];

      // If the step does not receive any rows, we can not lookup field position indexes
      if ( r != null ) {
        for ( int i = 0; i < meta.getSubjectField().length; i++ ) {
          if ( meta.getAggregateType()[i] == MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY ) {
            data.subjectnrs[i] = 0;
          } else {
            data.subjectnrs[i] = data.inputRowMeta.indexOfValue( meta.getSubjectField()[i] );
          }
          if ( data.subjectnrs[i] < 0 ) {
            logError( BaseMessages.getString( PKG, "MemoryGroupBy.Log.AggregateSubjectFieldCouldNotFound", meta
              .getSubjectField()[i] ) );
            setErrors( 1 );
            stopAll();
            return false;
          }
        }

        for ( int i = 0; i < meta.getGroupField().length; i++ ) {
          data.groupnrs[i] = data.inputRowMeta.indexOfValue( meta.getGroupField()[i] );
          if ( data.groupnrs[i] < 0 ) {
            logError( BaseMessages.getString(
              PKG, "MemoryGroupBy.Log.GroupFieldCouldNotFound", meta.getGroupField()[i] ) );
            setErrors( 1 );
            stopAll();
            return false;
          }
        }
      }

      // Create a metadata value for the counter Integers
      //
      data.valueMetaInteger = new ValueMetaInteger( "count" );
      data.valueMetaNumber = new ValueMetaNumber( "sum" );

      // Initialize the group metadata
      //
      initGroupMeta( data.inputRowMeta );

    }

    if ( first ) {
      // Only calculate data.aggMeta here, not for every new aggregate.
      //
      newAggregate( r, null );

      // for speed: groupMeta+aggMeta
      //
      data.groupAggMeta = new RowMeta();
      data.groupAggMeta.addRowMeta( data.groupMeta );
      data.groupAggMeta.addRowMeta( data.aggMeta );
    }

    // Here is where we start to do the real work...
    //
    if ( r == null ) { // no more input to be expected... (or none received in the first place)
      handleLastOfGroup();

      setOutputDone();
      return false;
    }

    if ( first || data.newBatch ) {
      first = false;
      data.newBatch = false;
    }

    addToAggregate( r );

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "MemoryGroupBy.LineNumber" ) + getLinesRead() );
      }
    }

    return true;
  }

  private void handleLastOfGroup() throws KettleException {
    // Dump the content of the map...
    //
    for ( HashEntry entry : data.map.keySet() ) {
      Aggregate aggregate = data.map.get( entry );
      Object[] aggregateResult = getAggregateResult( aggregate );

      Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      int index = 0;
      for ( int i = 0; i < data.groupMeta.size(); i++ ) {
        outputRowData[index++] = data.groupMeta.getValueMeta( i ).convertToNormalStorageType( entry.getGroupData()[i] );
      }
      for ( int i = 0; i < data.aggMeta.size(); i++ ) {
        outputRowData[index++] = data.aggMeta.getValueMeta( i ).convertToNormalStorageType( aggregateResult[i] );
      }
      putRow( data.outputRowMeta, outputRowData );
    }

    // What if we always need to give back one row?
    // This means we give back 0 for count all, count distinct, null for everything else
    //
    if ( data.map.isEmpty() && meta.isAlwaysGivingBackOneRow() ) {
      Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      int index = 0;
      for ( int i = 0; i < data.groupMeta.size(); i++ ) {
        outputRowData[index++] = null;
      }
      for ( int i = 0; i < data.aggMeta.size(); i++ ) {
        if ( meta.getAggregateType()[i] == MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL
          || meta.getAggregateType()[i] == MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY
          || meta.getAggregateType()[i] == MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT ) {
          outputRowData[index++] = Long.valueOf( 0L );
        } else {
          outputRowData[index++] = null;
        }
      }
      putRow( data.outputRowMeta, outputRowData );
    }
  }

  /**
   * Used for junits in MemoryGroupByAggregationNullsTest
   *
   * @param r
   * @throws KettleException
   */
  @SuppressWarnings( "unchecked" ) void addToAggregate( Object[] r ) throws KettleException {

    Object[] groupData = new Object[data.groupMeta.size()];
    for ( int i = 0; i < data.groupnrs.length; i++ ) {
      groupData[i] = r[data.groupnrs[i]];
    }
    HashEntry entry = data.getHashEntry( groupData );

    Aggregate aggregate = data.map.get( entry );
    if ( aggregate == null ) {
      // Create a new value...
      //
      aggregate = new Aggregate();
      newAggregate( r, aggregate );

      // Store it in the map!
      //
      data.map.put( entry, aggregate );
    }

    for ( int i = 0; i < data.subjectnrs.length; i++ ) {
      Object subj = r[data.subjectnrs[i]];
      ValueMetaInterface subjMeta = data.inputRowMeta.getValueMeta( data.subjectnrs[i] );
      Object value = aggregate.agg[i];
      ValueMetaInterface valueMeta = data.aggMeta.getValueMeta( i );

      switch ( meta.getAggregateType()[i] ) {
        case MemoryGroupByMeta.TYPE_GROUP_SUM:
          aggregate.agg[i] = ValueDataUtil.sum( valueMeta, value, subjMeta, subj );
          break;
        case MemoryGroupByMeta.TYPE_GROUP_AVERAGE:
          if ( !subjMeta.isNull( subj ) ) {
            aggregate.agg[i] = ValueDataUtil.sum( valueMeta, value, subjMeta, subj );
            aggregate.counts[i]++;
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_MEDIAN:
        case MemoryGroupByMeta.TYPE_GROUP_PERCENTILE:
          if ( !subjMeta.isNull( subj ) ) {
            ( (List<Double>) aggregate.agg[i] ).add( subjMeta.getNumber( subj ) );
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
          if ( aggregate.mean == null ) {
            aggregate.mean = new double[meta.getSubjectField().length];
          }
          aggregate.counts[i]++;
          double n = aggregate.counts[i];
          double x = subjMeta.getNumber( subj );
          // for standard deviation null is exact 0
          double sum = value == null ? new Double( 0 ) : (Double) value;
          double mean = aggregate.mean[i];

          double delta = x - mean;
          mean = mean + ( delta / n );
          sum = sum + delta * ( x - mean );

          aggregate.mean[i] = mean;
          aggregate.agg[i] = sum;
          break;
        case MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
          if ( aggregate.distinctObjs == null ) {
            aggregate.distinctObjs = new Set[meta.getSubjectField().length];
          }
          if ( aggregate.distinctObjs[i] == null ) {
            aggregate.distinctObjs[i] = new TreeSet<>();
          }
          if ( !subjMeta.isNull( subj ) ) {
            Object obj = subjMeta.convertToNormalStorageType( subj );
            // byte [] is not Comparable and can not be added to TreeSet.
            // For our case it can be binary array. It was typed as String.
            // So it can be processing (comparing and displaying) correctly as String
            if ( obj instanceof byte [] ) {
              obj = new String( (byte []) obj );
            }
            if ( !aggregate.distinctObjs[i].contains( obj ) ) {
              aggregate.distinctObjs[i].add( obj );
            }
          }
          aggregate.counts[i] = aggregate.distinctObjs[i].size();
          break;
        case MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL:
          if ( !subjMeta.isNull( subj ) ) {
            aggregate.counts[i]++;
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY:
          aggregate.counts[i]++;
          break;
        case MemoryGroupByMeta.TYPE_GROUP_MIN:
          boolean subjIsNull = subjMeta.isNull( subj );
          boolean valueIsNull = valueMeta.isNull( value );
          if ( minNullIsValued || ( !subjIsNull && !valueIsNull ) ) {
            // PDI-11530 do not compare null
            aggregate.agg[i] = subjMeta.compare( subj, valueMeta, value ) < 0 ? subj : value;
          } else if ( valueIsNull && !subjIsNull ) {
            // By default set aggregate to first not null value
            aggregate.agg[i] = subj;
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_MAX:
          if ( subjMeta.compare( subj, valueMeta, value ) > 0 ) {
            aggregate.agg[i] = subj;
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_FIRST:
          if ( !subjMeta.isNull( subj ) && value == null ) {
            aggregate.agg[i] = subj;
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_LAST:
          if ( !subjMeta.isNull( subj ) ) {
            aggregate.agg[i] = subj;
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
          if ( aggregate.counts[i] == 0 ) {
            aggregate.agg[i] = subj;
            aggregate.counts[i]++;
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_LAST_INCL_NULL:
          aggregate.agg[i] = subj;
          break;
        case MemoryGroupByMeta.TYPE_GROUP_CONCAT_COMMA:
          if ( !( subj == null ) ) {
            StringBuilder sb = (StringBuilder) value;
            if ( sb.length() > 0 ) {
              sb.append( ", " );
            }
            sb.append( subjMeta.getString( subj ) );
          }
          break;
        case MemoryGroupByMeta.TYPE_GROUP_CONCAT_STRING:
          if ( !( subj == null ) ) {
            String separator = "";
            if ( !Utils.isEmpty( meta.getValueField()[i] ) ) {
              separator = environmentSubstitute( meta.getValueField()[i] );
            }
            StringBuilder sb = (StringBuilder) value;
            if ( sb.length() > 0 ) {
              sb.append( separator );
            }
            sb.append( subjMeta.getString( subj ) );
          }
          break;
        default:
          break;
      }
    }
  }

  /**
   * Used for junits in MemoryGroupByNewAggregateTest
   *
   * @param r
   * @param aggregate
   * @throws KettleException
   */
  void newAggregate( Object[] r, Aggregate aggregate ) throws KettleException {
    if ( aggregate == null ) {
      data.aggMeta = new RowMeta();
    } else {
      aggregate.counts = new long[data.subjectnrs.length];

      // Put all the counters at 0
      for ( int i = 0; i < aggregate.counts.length; i++ ) {
        aggregate.counts[i] = 0;
      }
      aggregate.distinctObjs = null;
      aggregate.agg = new Object[data.subjectnrs.length];
      aggregate.mean = new double[data.subjectnrs.length]; // sets all doubles to 0.0
    }

    for ( int i = 0; i < data.subjectnrs.length; i++ ) {
      ValueMetaInterface subjMeta = data.inputRowMeta.getValueMeta( data.subjectnrs[i] );
      Object v = null;
      ValueMetaInterface vMeta = null;
      switch ( meta.getAggregateType()[i] ) {
        case MemoryGroupByMeta.TYPE_GROUP_MEDIAN:
        case MemoryGroupByMeta.TYPE_GROUP_PERCENTILE:
          vMeta = new ValueMetaNumber( meta.getAggregateField()[i] );
          v = new ArrayList<Double>();
          break;
        case MemoryGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
          vMeta = new ValueMetaNumber( meta.getAggregateField()[i] );
          break;
        case MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
        case MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY:
        case MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL:
          vMeta = new ValueMetaInteger( meta.getAggregateField()[i] );
          break;
        case MemoryGroupByMeta.TYPE_GROUP_SUM:
        case MemoryGroupByMeta.TYPE_GROUP_AVERAGE:
          vMeta = !compatibilityMode && subjMeta.isNumeric() ? subjMeta.clone() : new ValueMetaNumber();
          vMeta.setName( meta.getAggregateField()[i] );
          break;
        case MemoryGroupByMeta.TYPE_GROUP_FIRST:
        case MemoryGroupByMeta.TYPE_GROUP_LAST:
        case MemoryGroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
        case MemoryGroupByMeta.TYPE_GROUP_LAST_INCL_NULL:
        case MemoryGroupByMeta.TYPE_GROUP_MIN:
        case MemoryGroupByMeta.TYPE_GROUP_MAX:
          vMeta = subjMeta.clone();
          vMeta.setName( meta.getAggregateField()[i] );
          v = r == null ? null : r[data.subjectnrs[i]];
          break;
        case MemoryGroupByMeta.TYPE_GROUP_CONCAT_COMMA:
          vMeta = new ValueMetaString( meta.getAggregateField()[i] );
          v = new StringBuilder();
          break;
        case MemoryGroupByMeta.TYPE_GROUP_CONCAT_STRING:
          vMeta = new ValueMetaString( meta.getAggregateField()[i] );
          v = new StringBuilder();
          break;
        default:
          throw new KettleException( "Unknown data type for aggregation : " + meta.getAggregateField()[i] );
      }

      if ( meta.getAggregateType()[i] != MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL
        && meta.getAggregateType()[i] != MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT
        && meta.getAggregateType()[i] != MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY ) {
        vMeta.setLength( subjMeta.getLength(), subjMeta.getPrecision() );
      }
      if ( aggregate == null ) {
        data.aggMeta.addValueMeta( vMeta );
      } else {
        aggregate.agg[i] = v;
      }
    }
  }

  private void initGroupMeta( RowMetaInterface previousRowMeta ) throws KettleValueException {
    data.groupMeta = new RowMeta();
    data.entryMeta = new RowMeta();

    for ( int i = 0; i < data.groupnrs.length; i++ ) {
      ValueMetaInterface valueMeta = previousRowMeta.getValueMeta( data.groupnrs[i] );
      data.groupMeta.addValueMeta( valueMeta );

      ValueMetaInterface normalMeta = valueMeta.clone();
      normalMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    }

    return;
  }

  /**
   * Used for junits in MemoryGroupByAggregationNullsTest
   *
   * @param aggregate
   * @return
   * @throws KettleValueException
   */
  Object[] getAggregateResult( Aggregate aggregate ) throws KettleValueException {
    Object[] result = new Object[data.subjectnrs.length];

    if ( data.subjectnrs != null ) {
      for ( int i = 0; i < data.subjectnrs.length; i++ ) {
        Object ag = aggregate.agg[i];
        switch ( meta.getAggregateType()[i] ) {
          case MemoryGroupByMeta.TYPE_GROUP_SUM:
            break;
          case MemoryGroupByMeta.TYPE_GROUP_AVERAGE:
            ag = ValueDataUtil.divide(
              data.aggMeta.getValueMeta( i ), ag,
              new ValueMetaInteger( "c" ), aggregate.counts[i]
            );
            break;
          case MemoryGroupByMeta.TYPE_GROUP_MEDIAN:
          case MemoryGroupByMeta.TYPE_GROUP_PERCENTILE:
            double percentile = 50.0;
            if ( meta.getAggregateType()[i] == MemoryGroupByMeta.TYPE_GROUP_PERCENTILE ) {
              percentile = Double.parseDouble( meta.getValueField()[i] );
            }
            @SuppressWarnings( "unchecked" )
            List<Double> valuesList = (List<Double>) aggregate.agg[i];
            double[] values = new double[valuesList.size()];
            for ( int v = 0; v < values.length; v++ ) {
              values[v] = valuesList.get( v );
            }
            ag = new Percentile().evaluate( values, percentile );
            break;
          case MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY:
          case MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL:
          case MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
            ag = aggregate.counts[i];
            break;
          case MemoryGroupByMeta.TYPE_GROUP_MIN:
            break;
          case MemoryGroupByMeta.TYPE_GROUP_MAX:
            break;
          case MemoryGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
            double sum = (Double) ag / aggregate.counts[i];
            ag = Double.valueOf( Math.sqrt( sum ) );
            break;
          case MemoryGroupByMeta.TYPE_GROUP_CONCAT_COMMA:
          case MemoryGroupByMeta.TYPE_GROUP_CONCAT_STRING:
            ag = ( (StringBuilder) ag ).toString();
            break;
          default:
            break;
        }
        if ( ag == null && allNullsAreZero ) {
          // PDI-11530 seems all rows for min function was nulls...
          ValueMetaInterface vm = data.aggMeta.getValueMeta( i );
          ag = ValueDataUtil.getZeroForValueMetaType( vm );
        }
        result[i] = ag;
      }
    }

    return result;

  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MemoryGroupByMeta) smi;
    data = (MemoryGroupByData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.map = new HashMap<HashEntry, Aggregate>( 5000 );
      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
    ( (MemoryGroupByData) sdi ).clear();
  }

  @Override
  public void batchComplete() throws KettleException {
    // Empty the hash table
    //
    handleLastOfGroup();

    // Clear the complete cache...
    //
    data.map.clear();

    data.newBatch = true;
  }

  /**
   * Used for junits in MemoryGroupByAggregationNullsTest
   *
   * @param allNullsAreZero the allNullsAreZero to set
   */
  void setAllNullsAreZero( boolean allNullsAreZero ) {
    this.allNullsAreZero = allNullsAreZero;
  }

  /**
   * Used for junits in MemoryGroupByAggregationNullsTest
   *
   * @param minNullIsValued the minNullIsValued to set
   */
  void setMinNullIsValued( boolean minNullIsValued ) {
    this.minNullIsValued = minNullIsValued;
  }
}
