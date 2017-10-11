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

package org.pentaho.di.trans.steps.clonerow;

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
 * Clone input row.
 *
 * @author Samatar
 * @since 27-06-2008
 */
public class CloneRow extends BaseStep implements StepInterface {
  private static Class<?> PKG = CloneRowMeta.class; // for i18n purposes, needed by Translator2!!

  private CloneRowMeta meta;
  private CloneRowData data;

  public CloneRow( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (CloneRowMeta) smi;
    data = (CloneRowData) sdi;

    Object[] r = getRow(); // get row, set busy!

    // if no more input to be expected set done.
    if ( r == null ) {
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      data.outputRowMeta = getInputRowMeta().clone();
      data.NrPrevFields = getInputRowMeta().size();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      data.addInfosToRow = ( meta.isAddCloneFlag() || meta.isAddCloneNum() );

      if ( meta.isAddCloneFlag() ) {
        String realflagfield = environmentSubstitute( meta.getCloneFlagField() );
        if ( Utils.isEmpty( realflagfield ) ) {
          logError( BaseMessages.getString( PKG, "CloneRow.Error.CloneFlagFieldMissing" ) );
          throw new KettleException( BaseMessages.getString( PKG, "CloneRow.Error.CloneFlagFieldMissing" ) );
        }
      }
      if ( meta.isAddCloneNum() ) {
        String realnumfield = environmentSubstitute( meta.getCloneNumField() );
        if ( Utils.isEmpty( realnumfield ) ) {
          logError( BaseMessages.getString( PKG, "CloneRow.Error.CloneNumFieldMissing" ) );
          throw new KettleException( BaseMessages.getString( PKG, "CloneRow.Error.CloneNumFieldMissing" ) );
        }
      }

      if ( meta.isNrCloneInField() ) {
        String cloneinfieldname = meta.getNrCloneField();
        if ( Utils.isEmpty( cloneinfieldname ) ) {
          logError( BaseMessages.getString( PKG, "CloneRow.Error.NrCloneInFieldMissing" ) );
          throw new KettleException( BaseMessages.getString( PKG, "CloneRow.Error.NrCloneInFieldMissing" ) );
        }
        // cache the position of the field
        if ( data.indexOfNrCloneField < 0 ) {
          data.indexOfNrCloneField = getInputRowMeta().indexOfValue( cloneinfieldname );
          if ( data.indexOfNrCloneField < 0 ) {
            // The field is unreachable !
            logError( BaseMessages.getString( PKG, "CloneRow.Log.ErrorFindingField" )
              + "[" + cloneinfieldname + "]" );
            throw new KettleException( BaseMessages.getString(
              PKG, "CloneRow.Exception.CouldnotFindField", cloneinfieldname ) );
          }
        }
      } else {
        String nrclonesString = environmentSubstitute( meta.getNrClones() );
        data.nrclones = Const.toInt( nrclonesString, 0 );
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "CloneRow.Log.NrClones", "" + data.nrclones ) );
        }
      }
    }

    Object[] outputRowData = r;

    if ( data.addInfosToRow ) {
      // It's the original row..
      // We need here to add some infos in order to identify this row
      outputRowData = RowDataUtil.createResizedCopy( r, data.outputRowMeta.size() );
      int rowIndex = data.NrPrevFields;
      if ( meta.isAddCloneFlag() ) {
        // This row is not a clone but the original row
        outputRowData[rowIndex] = false;
        rowIndex++;
      }
      if ( meta.isAddCloneNum() ) {
        // This row is the original so let's identify it as the first one (zero)
        outputRowData[rowIndex] = 0L;
      }
    }

    putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

    if ( meta.isNrCloneInField() ) {
      Long nrCloneFieldValue = getInputRowMeta().getInteger( r, data.indexOfNrCloneField );
      if ( nrCloneFieldValue == null ) {
        throw new KettleException( BaseMessages.getString( PKG, "CloneRow.Log.NrClonesIsNull" ) );
      } else {
        data.nrclones = nrCloneFieldValue;
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "CloneRow.Log.NrClones", "" + data.nrclones ) );
        }
      }
    }
    for ( int i = 0; i < data.nrclones && !isStopped(); i++ ) {
      // Output now all clones row
      outputRowData = r.clone();
      if ( data.addInfosToRow ) {
        // We need here to add more infos about clone rows
        outputRowData = RowDataUtil.createResizedCopy( r, data.outputRowMeta.size() );
        int rowIndex = data.NrPrevFields;
        if ( meta.isAddCloneFlag() ) {
          // This row is a clone row
          outputRowData[rowIndex] = true;
          rowIndex++;
        }
        if ( meta.isAddCloneNum() ) {
          // Let's add to clone number
          // Clone starts at number 1 (0 is for the original row)
          Long clonenum = new Long( i + 1 );
          outputRowData[rowIndex] = clonenum;
        }
      }
      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);
    }

    if ( log.isDetailed() && checkFeedback( getLinesRead() ) ) {
      logDetailed( BaseMessages.getString( PKG, "CloneRow.Log.LineNumber", "" + getLinesRead() ) );
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CloneRowMeta) smi;
    data = (CloneRowData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      return true;
    }
    return false;
  }

}
