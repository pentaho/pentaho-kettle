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

package org.pentaho.di.trans.steps.groupby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;

import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Groups informations based on aggregation rules. (sum, count, ...)
 *
 * @author Matt
 * @since 2-jun-2003
 */
public class GroupBy extends BaseStep implements StepInterface {
  private static Class<?> PKG = GroupByMeta.class; // for i18n purposes, needed by Translator2!!

  private GroupByMeta meta;

  private GroupByData data;

  private boolean allNullsAreZero = false;
  private boolean minNullIsValued = false;

  public GroupBy( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (GroupByMeta) getStepMeta().getStepMetaInterface();
    data = (GroupByData) stepDataInterface;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (GroupByMeta) smi;
    data = (GroupByData) sdi;

    Object[] r = getRow(); // get row!

    if ( first ) {
      String val = getVariable( Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "N" );
      allNullsAreZero = ValueMetaBase.convertStringToBoolean( val );
      val = getVariable( Const.KETTLE_AGGREGATION_MIN_NULL_IS_VALUED, "N" );
      minNullIsValued = ValueMetaBase.convertStringToBoolean( val );

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
      data.counts = new long[ meta.getSubjectField().length ];
      data.subjectnrs = new int[ meta.getSubjectField().length ];

      data.cumulativeSumSourceIndexes = new ArrayList<>();
      data.cumulativeSumTargetIndexes = new ArrayList<>();

      data.cumulativeAvgSourceIndexes = new ArrayList<>();
      data.cumulativeAvgTargetIndexes = new ArrayList<>();

      for ( int i = 0; i < meta.getSubjectField().length; i++ ) {
        if ( meta.getAggregateType()[ i ] == GroupByMeta.TYPE_GROUP_COUNT_ANY ) {
          data.subjectnrs[ i ] = 0;
        } else {
          data.subjectnrs[ i ] = data.inputRowMeta.indexOfValue( meta.getSubjectField()[i] );
        }
        if ( ( r != null ) && ( data.subjectnrs[ i ] < 0 ) ) {
          logError( BaseMessages.getString( PKG, "GroupBy.Log.AggregateSubjectFieldCouldNotFound",
              meta.getSubjectField()[ i ] ) );
          setErrors( 1 );
          stopAll();
          return false;
        }

        if ( meta.getAggregateType()[ i ] == GroupByMeta.TYPE_GROUP_CUMULATIVE_SUM ) {
          data.cumulativeSumSourceIndexes.add( data.subjectnrs[ i ] );

          // The position of the target in the output row is the input row size + i
          //
          data.cumulativeSumTargetIndexes.add( data.inputRowMeta.size() + i );
        }
        if ( meta.getAggregateType()[ i ] == GroupByMeta.TYPE_GROUP_CUMULATIVE_AVERAGE ) {
          data.cumulativeAvgSourceIndexes.add( data.subjectnrs[ i ] );

          // The position of the target in the output row is the input row size + i
          //
          data.cumulativeAvgTargetIndexes.add( data.inputRowMeta.size() + i );
        }

      }

      data.previousSums = new Object[ data.cumulativeSumTargetIndexes.size() ];

      data.previousAvgSum = new Object[ data.cumulativeAvgTargetIndexes.size() ];
      data.previousAvgCount = new long[ data.cumulativeAvgTargetIndexes.size() ];

      data.groupnrs = new int[ meta.getGroupField().length ];
      for ( int i = 0; i < meta.getGroupField().length; i++ ) {
        data.groupnrs[ i ] = data.inputRowMeta.indexOfValue( meta.getGroupField()[i] );
        if ( ( r != null ) && ( data.groupnrs[ i ] < 0 ) ) {
          logError( BaseMessages.getString( PKG, "GroupBy.Log.GroupFieldCouldNotFound", meta.getGroupField()[ i ] ) );
          setErrors( 1 );
          stopAll();
          return false;
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

    if ( first || data.newBatch ) {
      // Create a new group aggregate (init)
      //
      newAggregate( r );
    }

    if ( first ) {
      // for speed: groupMeta+aggMeta
      //
      data.groupAggMeta = new RowMeta();
      data.groupAggMeta.addRowMeta( data.groupMeta );
      data.groupAggMeta.addRowMeta( data.aggMeta );
    }

    if ( r == null ) { // no more input to be expected... (or none received in the first place)
      handleLastOfGroup();
      setOutputDone();
      return false;
    }

    if ( first || data.newBatch ) {
      first = false;
      data.newBatch = false;

      data.previous = data.inputRowMeta.cloneRow( r ); // copy the row to previous
    } else {
      calcAggregate( data.previous );

      if ( meta.passAllRows() ) {
        addToBuffer( data.previous );
      }
    }

    if ( !sameGroup( data.previous, r ) ) {
      if ( meta.passAllRows() ) {
        // Not the same group: close output (if any)
        closeOutput();
        // Get all rows from the buffer!
        data.groupResult = getAggregateResult();
        Object[] row = getRowFromBuffer();

        long lineNr = 0;
        while ( row != null ) {
          int size = data.inputRowMeta.size();

          row = RowDataUtil.addRowData( row, size, data.groupResult );
          size += data.groupResult.length;

          lineNr++;

          if ( meta.isAddingLineNrInGroup() && !Utils.isEmpty( meta.getLineNrInGroupField() ) ) {
            Object lineNrValue = new Long( lineNr );
            // ValueMetaInterface lineNrValueMeta = new ValueMeta(meta.getLineNrInGroupField(),
            // ValueMetaInterface.TYPE_INTEGER);
            // lineNrValueMeta.setLength(9);
            row = RowDataUtil.addValueData( row, size, lineNrValue );
            size++;
          }

          addCumulativeSums( row );
          addCumulativeAverages( row );

          putRow( data.outputRowMeta, row );
          row = getRowFromBuffer();
        }
        closeInput();
      } else {
        Object[] result = buildResult( data.previous );
        if ( result != null ) {
          putRow( data.groupAggMeta, result ); // copy row to possible alternate rowset(s).
        }
      }
      newAggregate( r ); // Create a new group aggregate (init)
    }

    data.previous = data.inputRowMeta.cloneRow( r );

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "GroupBy.LineNumber" ) + getLinesRead() );
      }
    }

    return true;
  }

  private void handleLastOfGroup() throws KettleException {
    if ( meta.passAllRows() ) {
      // ALL ROWS

      if ( data.previous != null ) {
        calcAggregate( data.previous );
        addToBuffer( data.previous );
      }
      data.groupResult = getAggregateResult();

      Object[] row = getRowFromBuffer();

      long lineNr = 0;
      while ( row != null ) {
        int size = data.inputRowMeta.size();
        row = RowDataUtil.addRowData( row, size, data.groupResult );
        size += data.groupResult.length;
        lineNr++;

        if ( meta.isAddingLineNrInGroup() && !Utils.isEmpty( meta.getLineNrInGroupField() ) ) {
          Object lineNrValue = new Long( lineNr );
          // ValueMetaInterface lineNrValueMeta = new ValueMeta(meta.getLineNrInGroupField(),
          // ValueMetaInterface.TYPE_INTEGER);
          // lineNrValueMeta.setLength(9);
          row = RowDataUtil.addValueData( row, size, lineNrValue );
          size++;
        }

        addCumulativeSums( row );
        addCumulativeAverages( row );

        putRow( data.outputRowMeta, row );
        row = getRowFromBuffer();
      }
      closeInput();
    } else {
      // JUST THE GROUP + AGGREGATE

      // Don't forget the last set of rows...
      if ( data.previous != null ) {
        calcAggregate( data.previous );
      }
      Object[] result = buildResult( data.previous );
      if ( result != null ) {
        putRow( data.groupAggMeta, result );
      }
    }
  }

  private void addCumulativeSums( Object[] row ) throws KettleValueException {

    // We need to adjust this row with cumulative averages?
    //
    for ( int i = 0; i < data.cumulativeSumSourceIndexes.size(); i++ ) {
      int sourceIndex = data.cumulativeSumSourceIndexes.get( i );
      Object previousTarget = data.previousSums[ i ];
      Object sourceValue = row[ sourceIndex ];

      int targetIndex = data.cumulativeSumTargetIndexes.get( i );

      ValueMetaInterface sourceMeta = data.inputRowMeta.getValueMeta( sourceIndex );
      ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta( targetIndex );

      // If the first values where null, or this is the first time around, just take the source value...
      //
      if ( targetMeta.isNull( previousTarget ) ) {
        row[ targetIndex ] = sourceMeta.convertToNormalStorageType( sourceValue );
      } else {
        // If the source value is null, just take the previous target value
        //
        if ( sourceMeta.isNull( sourceValue ) ) {
          row[ targetIndex ] = previousTarget;
        } else {
          row[ targetIndex ] = ValueDataUtil.plus( targetMeta, data.previousSums[ i ], sourceMeta, row[ sourceIndex ] );
        }
      }
      data.previousSums[ i ] = row[ targetIndex ];
    }

  }

  private void addCumulativeAverages( Object[] row ) throws KettleValueException {

    // We need to adjust this row with cumulative sums
    //
    for ( int i = 0; i < data.cumulativeAvgSourceIndexes.size(); i++ ) {
      int sourceIndex = data.cumulativeAvgSourceIndexes.get( i );
      Object previousTarget = data.previousAvgSum[ i ];
      Object sourceValue = row[ sourceIndex ];

      int targetIndex = data.cumulativeAvgTargetIndexes.get( i );

      ValueMetaInterface sourceMeta = data.inputRowMeta.getValueMeta( sourceIndex );
      ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta( targetIndex );

      // If the first values where null, or this is the first time around, just take the source value...
      //
      Object sum = null;

      if ( targetMeta.isNull( previousTarget ) ) {
        sum = sourceMeta.convertToNormalStorageType( sourceValue );
      } else {
        // If the source value is null, just take the previous target value
        //
        if ( sourceMeta.isNull( sourceValue ) ) {
          sum = previousTarget;
        } else {
          if ( sourceMeta.isInteger() ) {
            sum = ValueDataUtil.plus( data.valueMetaInteger, data.previousAvgSum[ i ], sourceMeta, row[ sourceIndex ] );
          } else {
            sum = ValueDataUtil.plus( targetMeta, data.previousAvgSum[ i ], sourceMeta, row[ sourceIndex ] );
          }
        }
      }
      data.previousAvgSum[ i ] = sum;

      if ( !sourceMeta.isNull( sourceValue ) ) {
        data.previousAvgCount[ i ]++;
      }

      if ( sourceMeta.isInteger() ) {
        // Change to number as the exception
        //
        if ( sum == null ) {
          row[ targetIndex ] = null;
        } else {
          row[ targetIndex ] = new Double( ( (Long) sum ).doubleValue() / data.previousAvgCount[ i ] );
        }
      } else {
        row[ targetIndex ] = ValueDataUtil.divide( targetMeta, sum, data.valueMetaInteger, data.previousAvgCount[ i ] );
      }
    }

  }

  // Is the row r of the same group as previous?
  boolean sameGroup( Object[] previous, Object[] r ) throws KettleValueException {
    return data.inputRowMeta.compare( previous, r, data.groupnrs ) == 0;
  }

  /**
   * used for junits in GroupByAggregationNullsTest
   *
   * @param row
   * @throws KettleValueException
   */
  @SuppressWarnings( "unchecked" ) void calcAggregate( Object[] row ) throws KettleValueException {
    for ( int i = 0; i < data.subjectnrs.length; i++ ) {
      Object subj = row[ data.subjectnrs[ i ] ];
      ValueMetaInterface subjMeta = data.inputRowMeta.getValueMeta( data.subjectnrs[ i ] );
      Object value = data.agg[ i ];
      ValueMetaInterface valueMeta = data.aggMeta.getValueMeta( i );

      switch ( meta.getAggregateType()[ i ] ) {
        case GroupByMeta.TYPE_GROUP_SUM:
          data.agg[ i ] = ValueDataUtil.sum( valueMeta, value, subjMeta, subj );
          break;
        case GroupByMeta.TYPE_GROUP_AVERAGE:
          if ( !subjMeta.isNull( subj ) ) {
            data.agg[ i ] = ValueDataUtil.sum( valueMeta, value, subjMeta, subj );
            data.counts[ i ]++;
          }
          break;
        case GroupByMeta.TYPE_GROUP_MEDIAN:
        case GroupByMeta.TYPE_GROUP_PERCENTILE:
        case GroupByMeta.TYPE_GROUP_PERCENTILE_NEAREST_RANK:
          if ( !subjMeta.isNull( subj ) ) {
            ( (List<Double>) data.agg[ i ] ).add( subjMeta.getNumber( subj ) );
          }
          break;
        case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
        case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION_SAMPLE:
          if ( !subjMeta.isNull( subj ) ) {
            data.counts[ i ]++;
            double n = data.counts[ i ];
            double x = subjMeta.getNumber( subj );
            // for standard deviation null is exact 0
            double sum = value == null ? new Double( 0 ) : (Double) value;
            double mean = data.mean[ i ];

            double delta = x - mean;
            mean = mean + ( delta / n );
            sum = sum + delta * ( x - mean );

            data.mean[ i ] = mean;
            data.agg[ i ] = sum;
          }
          break;
        case GroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
          if ( !subjMeta.isNull( subj ) ) {
            if ( data.distinctObjs == null ) {
              data.distinctObjs = new Set[ meta.getSubjectField().length ];
            }
            if ( data.distinctObjs[ i ] == null ) {
              data.distinctObjs[ i ] = new TreeSet<Object>();
            }
            Object obj = subjMeta.convertToNormalStorageType( subj );
            if ( !data.distinctObjs[ i ].contains( obj ) ) {
              data.distinctObjs[ i ].add( obj );
              // null is exact 0, or we will not be able to ++.
              value = value == null ? new Long( 0 ) : value;
              data.agg[ i ] = (Long) value + 1;
            }
          }
          break;
        case GroupByMeta.TYPE_GROUP_COUNT_ALL:
          if ( !subjMeta.isNull( subj ) ) {
            data.counts[ i ]++;
          }
          break;
        case GroupByMeta.TYPE_GROUP_COUNT_ANY:
          data.counts[ i ]++;
          break;
        case GroupByMeta.TYPE_GROUP_MIN: {
          if ( subj == null && !minNullIsValued ) {
            // PDI-10250 do not compare null
            break;
          }
          // PDI-15648 set the initial value for further comparing
          if ( value == null && subj != null && !minNullIsValued ) {
            data.agg[ i ] = subj;
            break;
          }

          if ( subjMeta.isSortedDescending() ) {
            // Account for negation in ValueMeta.compare() - See PDI-2302
            if ( subjMeta.compare( value, valueMeta, subj ) < 0 ) {
              data.agg[ i ] = subj;
            }
          } else {
            if ( subjMeta.compare( subj, valueMeta, value ) < 0 ) {
              data.agg[ i ] = subj;
            }
          }
          break;
        }
        case GroupByMeta.TYPE_GROUP_MAX:
          if ( subjMeta.isSortedDescending() ) {
            // Account for negation in ValueMeta.compare() - See PDI-2302
            if ( subjMeta.compare( value, valueMeta, subj ) > 0 ) {
              data.agg[ i ] = subj;
            }
          } else {
            if ( subjMeta.compare( subj, valueMeta, value ) > 0 ) {
              data.agg[ i ] = subj;
            }
          }
          break;
        case GroupByMeta.TYPE_GROUP_FIRST:
          if ( !( subj == null ) && value == null ) {
            data.agg[ i ] = subj;
          }
          break;
        case GroupByMeta.TYPE_GROUP_LAST:
          if ( !( subj == null ) ) {
            data.agg[ i ] = subj;
          }
          break;
        case GroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
          // This is on purpose. The calculation of the
          // first field is done when setting up a new group
          // This is just the field of the first row
          // if (linesWritten==0) value.setValue(subj);
          break;
        case GroupByMeta.TYPE_GROUP_LAST_INCL_NULL:
          data.agg[ i ] = subj;
          break;
        case GroupByMeta.TYPE_GROUP_CONCAT_COMMA:
          if ( !( subj == null ) ) {
            StringBuilder sb = (StringBuilder) value;
            if ( sb.length() > 0 ) {
              sb.append( ", " );
            }
            sb.append( subjMeta.getString( subj ) );
          }
          break;
        case GroupByMeta.TYPE_GROUP_CONCAT_STRING:
          if ( !( subj == null ) ) {
            String separator = "";
            if ( !Utils.isEmpty( meta.getValueField()[ i ] ) ) {
              separator = environmentSubstitute( meta.getValueField()[ i ] );
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
   * used for junits in GroupByAggregationNullsTest
   *
   * @param r
   */
  void newAggregate( Object[] r ) {
    // Put all the counters at 0
    for ( int i = 0; i < data.counts.length; i++ ) {
      data.counts[ i ] = 0;
    }
    data.distinctObjs = null;
    data.agg = new Object[ data.subjectnrs.length ];
    data.mean = new double[ data.subjectnrs.length ]; // sets all doubles to 0.0
    data.aggMeta = new RowMeta();

    for ( int i = 0; i < data.subjectnrs.length; i++ ) {
      ValueMetaInterface subjMeta = data.inputRowMeta.getValueMeta( data.subjectnrs[ i ] );
      Object v = null;
      ValueMetaInterface vMeta = null;
      int aggType = meta.getAggregateType()[ i ];
      switch ( aggType ) {
        case GroupByMeta.TYPE_GROUP_SUM:
        case GroupByMeta.TYPE_GROUP_AVERAGE:
        case GroupByMeta.TYPE_GROUP_CUMULATIVE_SUM:
        case GroupByMeta.TYPE_GROUP_CUMULATIVE_AVERAGE:
          if ( subjMeta.isNumeric() ) {
            try {
              vMeta = ValueMetaFactory.createValueMeta( meta.getAggregateField()[ i ], subjMeta.getType() );
            } catch ( KettlePluginException e ) {
              vMeta = new ValueMetaNone( meta.getAggregateField()[ i ] );
            }
          } else {
            vMeta = new ValueMetaNumber( meta.getAggregateField()[ i ] );
          }
          break;
        case GroupByMeta.TYPE_GROUP_MEDIAN:
        case GroupByMeta.TYPE_GROUP_PERCENTILE:
        case GroupByMeta.TYPE_GROUP_PERCENTILE_NEAREST_RANK:
          vMeta = new ValueMetaNumber( meta.getAggregateField()[ i ] );
          v = new ArrayList<Double>();
          break;
        case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
        case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION_SAMPLE:
          vMeta = new ValueMetaNumber( meta.getAggregateField()[ i ] );
          break;
        case GroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
        case GroupByMeta.TYPE_GROUP_COUNT_ANY:
        case GroupByMeta.TYPE_GROUP_COUNT_ALL:
          vMeta = new ValueMetaInteger( meta.getAggregateField()[ i ] );
          break;
        case GroupByMeta.TYPE_GROUP_FIRST:
        case GroupByMeta.TYPE_GROUP_LAST:
        case GroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
        case GroupByMeta.TYPE_GROUP_LAST_INCL_NULL:
        case GroupByMeta.TYPE_GROUP_MIN:
        case GroupByMeta.TYPE_GROUP_MAX:
          vMeta = subjMeta.clone();
          vMeta.setName( meta.getAggregateField()[ i ] );
          v = r == null ? null : r[ data.subjectnrs[ i ] ];
          break;
        case GroupByMeta.TYPE_GROUP_CONCAT_COMMA:
          vMeta = new ValueMetaString( meta.getAggregateField()[ i ] );
          v = new StringBuilder();
          break;
        case GroupByMeta.TYPE_GROUP_CONCAT_STRING:
          vMeta = new ValueMetaString( meta.getAggregateField()[ i ] );
          v = new StringBuilder();
          break;
        default:
          // TODO raise an error here because we cannot continue successfully maybe the UI should validate this
          break;
      }

      if ( ( subjMeta != null )
          && ( aggType != GroupByMeta.TYPE_GROUP_COUNT_ALL
          && aggType != GroupByMeta.TYPE_GROUP_COUNT_DISTINCT
          && aggType != GroupByMeta.TYPE_GROUP_COUNT_ANY ) ) {
        vMeta.setLength( subjMeta.getLength(), subjMeta.getPrecision() );
      }
      data.agg[ i ] = v;
      data.aggMeta.addValueMeta( vMeta );
    }

    // Also clear the cumulative data...
    //
    for ( int i = 0; i < data.previousSums.length; i++ ) {
      data.previousSums[ i ] = null;
    }
    for ( int i = 0; i < data.previousAvgCount.length; i++ ) {
      data.previousAvgCount[ i ] = 0L;
      data.previousAvgSum[ i ] = null;
    }
  }

  private Object[] buildResult( Object[] r ) throws KettleValueException {
    Object[] result = null;
    if ( r != null || meta.isAlwaysGivingBackOneRow() ) {
      result = RowDataUtil.allocateRowData( data.groupnrs.length );
      if ( r != null ) {
        for ( int i = 0; i < data.groupnrs.length; i++ ) {
          result[ i ] = r[ data.groupnrs[ i ] ];
        }
      }

      result = RowDataUtil.addRowData( result, data.groupnrs.length, getAggregateResult() );
    }

    return result;
  }

  private void initGroupMeta( RowMetaInterface previousRowMeta ) throws KettleValueException {
    data.groupMeta = new RowMeta();
    for ( int i = 0; i < data.groupnrs.length; i++ ) {
      data.groupMeta.addValueMeta( previousRowMeta.getValueMeta( data.groupnrs[ i ] ) );
    }
  }

  /**
   * Used for junits in GroupByAggregationNullsTest
   *
   * @return
   * @throws KettleValueException
   */
  Object[] getAggregateResult() throws KettleValueException {

    if ( data.subjectnrs == null ) {
      return new Object[ 0 ];
    }

    Object[] result = new Object[ data.subjectnrs.length ];

    for ( int i = 0; i < data.subjectnrs.length; i++ ) {
      Object ag = data.agg[ i ];
      switch ( meta.getAggregateType()[ i ] ) {
        case GroupByMeta.TYPE_GROUP_SUM:
          break;
        case GroupByMeta.TYPE_GROUP_AVERAGE:
          ag =
              ValueDataUtil.divide( data.aggMeta.getValueMeta( i ), ag,
                  new ValueMetaInteger( "c" ), new Long( data.counts[ i ] ) );
          break;
        case GroupByMeta.TYPE_GROUP_MEDIAN:
        case GroupByMeta.TYPE_GROUP_PERCENTILE:
          double percentile = 50.0;
          if ( meta.getAggregateType()[ i ] == GroupByMeta.TYPE_GROUP_PERCENTILE ) {
            percentile = Double.parseDouble( meta.getValueField()[ i ] );
          }
          @SuppressWarnings( "unchecked" )
          List<Double> valuesList = (List<Double>) data.agg[ i ];
          double[] values = new double[ valuesList.size() ];
          for ( int v = 0; v < values.length; v++ ) {
            values[ v ] = valuesList.get( v );
          }
          ag = new Percentile().evaluate( values, percentile );
          break;
        case GroupByMeta.TYPE_GROUP_PERCENTILE_NEAREST_RANK:
          double percentileValue = 50.0;
          if ( meta.getAggregateType()[ i ] == GroupByMeta.TYPE_GROUP_PERCENTILE_NEAREST_RANK ) {
            percentileValue = Double.parseDouble( meta.getValueField()[ i ] );
          }
          @SuppressWarnings( "unchecked" )
          List<Double> latenciesList = (List<Double>) data.agg[ i ];
          Collections.sort( latenciesList );
          Double[] latencies = new Double[ latenciesList.size() ];
          latencies = latenciesList.toArray( latencies );
          int index = (int) Math.ceil( ( percentileValue / 100 ) * latencies.length );
          ag = latencies[ index - 1 ];
          break;
        case GroupByMeta.TYPE_GROUP_COUNT_ANY:
        case GroupByMeta.TYPE_GROUP_COUNT_ALL:
          ag = new Long( data.counts[ i ] );
          break;
        case GroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
          break;
        case GroupByMeta.TYPE_GROUP_MIN:
          break;
        case GroupByMeta.TYPE_GROUP_MAX:
          break;
        case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION: {
          if ( ag == null ) {
            // PMD-1037 - when all input data is null ag is null, npe on access ag
            break;
          }
          double sum = (Double) ag / data.counts[ i ];
          ag = Math.sqrt( sum );
          break;
        }
        case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION_SAMPLE: {
          if ( ag == null ) {
            break;
          }
          double sum = (Double) ag / ( data.counts[ i ] - 1 );
          ag = Math.sqrt( sum );
          break;
        }
        case GroupByMeta.TYPE_GROUP_CONCAT_COMMA:
        case GroupByMeta.TYPE_GROUP_CONCAT_STRING:
          ag = ( (StringBuilder) ag ).toString();
          break;
        default:
          break;
      }
      if ( ag == null && allNullsAreZero ) {
        // PDI-10250, 6960 seems all rows for min function was nulls...
        // get output subject meta based on original subject meta calculation
        ValueMetaInterface vm = data.aggMeta.getValueMeta( i );
        ag = ValueDataUtil.getZeroForValueMetaType( vm );
      }
      result[ i ] = ag;
    }

    return result;

  }

  // Method is defined as package-protected in order to be accessible by unit tests
  void addToBuffer( Object[] row ) throws KettleFileException {
    data.bufferList.add( row );
    if ( data.bufferList.size() > 5000 && data.rowsOnFile == 0 ) {
      String pathToTmp = environmentSubstitute( getMeta().getDirectory() );
      try {
        File ioFile = new File( pathToTmp );
        if ( !ioFile.exists() ) {
          // try to resolve as Apache VFS file
          pathToTmp = retrieveVfsPath( pathToTmp );
        }
        data.tempFile = File.createTempFile( getMeta().getPrefix(), ".tmp", new File( pathToTmp ) );
        data.fosToTempFile = new FileOutputStream( data.tempFile );
        data.dosToTempFile = new DataOutputStream( data.fosToTempFile );
        data.firstRead = true;
      } catch ( IOException e ) {
        throw new KettleFileException( BaseMessages.getString( PKG, "GroupBy.Exception.UnableToCreateTemporaryFile" ),
            e );
      }
      // OK, save the oldest rows to disk!
      Object[] oldest = data.bufferList.get( 0 );
      data.inputRowMeta.writeData( data.dosToTempFile, oldest );
      data.bufferList.remove( 0 );
      data.rowsOnFile++;
    }
  }

  // Method is defined as public in order to be accessible by unit tests
  public String retrieveVfsPath( String pathToTmp ) throws KettleFileException {
    FileObject vfsFile = KettleVFS.getFileObject( pathToTmp );
    String path = vfsFile.getName().getPath();
    return path;
  }

  private Object[] getRowFromBuffer() throws KettleFileException {
    if ( data.rowsOnFile > 0 ) {
      if ( data.firstRead ) {
        // Open the inputstream first...
        try {
          data.fisToTmpFile = new FileInputStream( data.tempFile );
          data.disToTmpFile = new DataInputStream( data.fisToTmpFile );
          data.firstRead = false;
        } catch ( IOException e ) {
          throw new KettleFileException( BaseMessages.getString(
              PKG, "GroupBy.Exception.UnableToReadBackRowFromTemporaryFile" ), e );
        }
      }

      // Read one row from the file!
      Object[] row;
      try {
        row = data.inputRowMeta.readData( data.disToTmpFile );
      } catch ( SocketTimeoutException e ) {
        throw new KettleFileException( e ); // Shouldn't happen on files
      }
      data.rowsOnFile--;

      return row;
    } else {
      if ( data.bufferList.size() > 0 ) {
        Object[] row = data.bufferList.get( 0 );
        data.bufferList.remove( 0 );
        return row;
      } else {
        return null; // Nothing left!
      }
    }
  }

  private void closeOutput() throws KettleFileException {
    try {
      if ( data.dosToTempFile != null ) {
        data.dosToTempFile.close();
        data.dosToTempFile = null;
      }
      if ( data.fosToTempFile != null ) {
        data.fosToTempFile.close();
        data.fosToTempFile = null;
      }
      data.firstRead = true;
    } catch ( IOException e ) {
      throw new KettleFileException(
          BaseMessages.getString( PKG, "GroupBy.Exception.UnableToCloseInputStream", data.tempFile.getPath() ), e );
    }
  }

  private void closeInput() throws KettleFileException {
    try {
      if ( data.fisToTmpFile != null ) {
        data.fisToTmpFile.close();
        data.fisToTmpFile = null;
      }
      if ( data.disToTmpFile != null ) {
        data.disToTmpFile.close();
        data.disToTmpFile = null;
      }
    } catch ( IOException e ) {
      throw new KettleFileException(
          BaseMessages.getString( PKG, "GroupBy.Exception.UnableToCloseInputStream", data.tempFile.getPath() ), e );
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GroupByMeta) smi;
    data = (GroupByData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.bufferList = new ArrayList<>();

      data.rowsOnFile = 0;

      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( data.tempFile != null ) {
      try {
        closeInput();
        closeOutput();
      } catch ( KettleFileException e ) {
        log.logError( e.getLocalizedMessage() );
      }

      boolean tempFileDeleted = data.tempFile.delete();

      if ( !tempFileDeleted && log.isDetailed() ) {
        log.logDetailed(
            BaseMessages.getString( PKG, "GroupBy.Exception.UnableToDeleteTemporaryFile", data.tempFile.getPath() ) );
      }
    }

    super.dispose( smi, sdi );
  }

  @Override
  public void batchComplete() throws KettleException {
    handleLastOfGroup();
    data.newBatch = true;
  }

  /**
   * Used for junits in GroupByAggregationNullsTest
   *
   * @param allNullsAreZero the allNullsAreZero to set
   */
  void setAllNullsAreZero( boolean allNullsAreZero ) {
    this.allNullsAreZero = allNullsAreZero;
  }

  /**
   * Used for junits in GroupByAggregationNullsTest
   *
   * @param minNullIsValued the minNullIsValued to set
   */
  void setMinNullIsValued( boolean minNullIsValued ) {
    this.minNullIsValued = minNullIsValued;
  }

  public GroupByMeta getMeta() {
    return meta;
  }
}
