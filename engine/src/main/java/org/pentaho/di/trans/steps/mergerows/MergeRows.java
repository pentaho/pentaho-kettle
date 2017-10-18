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

package org.pentaho.di.trans.steps.mergerows;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

/**
 * Merge rows from 2 sorted streams to detect changes. Use this as feed for a dimension in case you have no time stamps
 * in your source system.
 *
 * @author Matt
 * @since 19-dec-2005
 */
public class MergeRows extends BaseStep implements StepInterface {
  private static Class<?> PKG = MergeRowsMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String VALUE_IDENTICAL = "identical";
  private static final String VALUE_CHANGED = "changed";
  private static final String VALUE_NEW = "new";
  private static final String VALUE_DELETED = "deleted";

  private MergeRowsMeta meta;
  private MergeRowsData data;
  private boolean useRefWhenIdentical = false;

  public MergeRows( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MergeRowsMeta) smi;
    data = (MergeRowsData) sdi;
    if ( first ) {
      first = false;

      // Find the appropriate RowSet
      //
      List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();

      //oneRowSet is the "Reference" stream
      data.oneRowSet = findInputRowSet( infoStreams.get( 0 ).getStepname() );
      //twoRowSet is the "Comparison" stream
      data.twoRowSet = findInputRowSet( infoStreams.get( 1 ).getStepname() );

      //rowSetWhenIdentical is use in case the comparison is IDENTICAL.
      //this should be the "Comparison" stream but can be the "Reference" stream for backward compatibility (PDI-736)
      String useRefWhenIdenticalVar = Const.NVL( System.getProperty( Const.KETTLE_COMPATIBILITY_MERGE_ROWS_USE_REFERENCE_STREAM_WHEN_IDENTICAL ), "N" );
      if ( "N".equalsIgnoreCase( useRefWhenIdenticalVar ) ) {
        //use the reference stream (as per documentation)
        useRefWhenIdentical = false;
      } else {
        //use the comparison stream (for backward compatibility)
        useRefWhenIdentical = true;
      }
      data.one = getRowFrom( data.oneRowSet );
      data.two = getRowFrom( data.twoRowSet );

      try {
        checkInputLayoutValid( data.oneRowSet.getRowMeta(), data.twoRowSet.getRowMeta() );
      } catch ( KettleRowException e ) {
        throw new KettleException( BaseMessages.getString( PKG, "MergeRows.Exception.InvalidLayoutDetected" ), e );
      }

      if ( data.one != null ) {
        // Find the key indexes:
        data.keyNrs = new int[meta.getKeyFields().length];
        for ( int i = 0; i < data.keyNrs.length; i++ ) {
          data.keyNrs[i] = data.oneRowSet.getRowMeta().indexOfValue( meta.getKeyFields()[i] );
          if ( data.keyNrs[i] < 0 ) {
            String message =
              BaseMessages.getString( PKG, "MergeRows.Exception.UnableToFindFieldInReferenceStream", meta
                .getKeyFields()[i] );
            logError( message );
            throw new KettleStepException( message );
          }
        }
      }

      if ( data.two != null ) {
        data.valueNrs = new int[meta.getValueFields().length];
        for ( int i = 0; i < data.valueNrs.length; i++ ) {
          data.valueNrs[i] = data.twoRowSet.getRowMeta().indexOfValue( meta.getValueFields()[i] );
          if ( data.valueNrs[i] < 0 ) {
            String message =
              BaseMessages.getString( PKG, "MergeRows.Exception.UnableToFindFieldInReferenceStream", meta
                .getValueFields()[i] );
            logError( message );
            throw new KettleStepException( message );
          }
        }
      }
    }

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "MergeRows.Log.DataInfo", data.one + "" ) + data.two );
    }

    if ( data.one == null && data.two == null ) {
      setOutputDone();
      return false;
    }

    if ( data.outputRowMeta == null ) {
      data.outputRowMeta = new RowMeta();
      if ( data.one != null ) {
        meta.getFields(
          data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.oneRowSet.getRowMeta() }, null, this,
          repository, metaStore );
      } else {
        meta.getFields(
          data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.twoRowSet.getRowMeta() }, null, this,
          repository, metaStore );
      }
    }

    Object[] outputRow;
    int outputIndex;
    String flagField = null;

    if ( data.one == null && data.two != null ) { // Record 2 is flagged as new!

      outputRow = data.two;
      outputIndex = data.twoRowSet.getRowMeta().size();
      flagField = VALUE_NEW;

      // Also get a next row from compare rowset...
      data.two = getRowFrom( data.twoRowSet );
    } else if ( data.one != null && data.two == null ) { // Record 1 is flagged as deleted!
      outputRow = data.one;
      outputIndex = data.oneRowSet.getRowMeta().size();
      flagField = VALUE_DELETED;

      // Also get a next row from reference rowset...
      data.one = getRowFrom( data.oneRowSet );
    } else { // OK, Here is the real start of the compare code!

      int compare = data.oneRowSet.getRowMeta().compare( data.one, data.two, data.keyNrs );
      if ( compare == 0 ) { // The Key matches, we CAN compare the two rows...

        int compareValues = data.oneRowSet.getRowMeta().compare( data.one, data.two, data.valueNrs );
        if ( compareValues == 0 ) {
          if ( useRefWhenIdentical ) {  //backwards compatible behavior: use the reference stream (PDI-736)
            outputRow = data.one;
            outputIndex = data.oneRowSet.getRowMeta().size();
          } else {
            outputRow = data.two;       //documented behavior: use the comparison stream (PDI-736)
            outputIndex = data.twoRowSet.getRowMeta().size();
          }
          flagField = VALUE_IDENTICAL;
        } else {
          // Return the compare (most recent) row
          //
          outputRow = data.two;
          outputIndex = data.twoRowSet.getRowMeta().size();
          flagField = VALUE_CHANGED;
        }

        // Get a new row from both streams...
        data.one = getRowFrom( data.oneRowSet );
        data.two = getRowFrom( data.twoRowSet );
      } else {
        if ( compare < 0 ) { // one < two

          outputRow = data.one;
          outputIndex = data.oneRowSet.getRowMeta().size();
          flagField = VALUE_DELETED;

          data.one = getRowFrom( data.oneRowSet );
        } else {
          outputRow = data.two;
          outputIndex = data.twoRowSet.getRowMeta().size();
          flagField = VALUE_NEW;

          data.two = getRowFrom( data.twoRowSet );
        }
      }
    }

    // send the row to the next steps...
    putRow( data.outputRowMeta, RowDataUtil.addValueData( outputRow, outputIndex, flagField ) );

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "MergeRows.LineNumber" ) + getLinesRead() );
      }
    }

    return true;
  }

  /**
   * @see StepInterface#init(org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
   */
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MergeRowsMeta) smi;
    data = (MergeRowsData) sdi;

    if ( super.init( smi, sdi ) ) {
      List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();

      if ( infoStreams.get( 0 ).getStepMeta() != null ^ infoStreams.get( 1 ).getStepMeta() != null ) {
        logError( BaseMessages.getString( PKG, "MergeRows.Log.BothTrueAndFalseNeeded" ) );
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether 2 template rows are compatible for the mergestep.
   *
   * @param referenceRow
   *          Reference row
   * @param compareRow
   *          Row to compare to
   *
   * @return true when templates are compatible.
   * @throws KettleRowException
   *           in case there is a compatibility error.
   */
  static void checkInputLayoutValid( RowMetaInterface referenceRowMeta, RowMetaInterface compareRowMeta ) throws KettleRowException {
    if ( referenceRowMeta != null && compareRowMeta != null ) {
      BaseStep.safeModeChecking( referenceRowMeta, compareRowMeta );
    }
  }

}
