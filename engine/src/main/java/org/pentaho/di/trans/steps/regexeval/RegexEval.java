/*******************************************************************************
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

package org.pentaho.di.trans.steps.regexeval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Use regular expression to validate a field or capture new fields out of an existing field.
 *
 * @author deinspanjer
 * @since 27-03-2008
 * @author Matt
 * @since 15-08-2007
 */
public class RegexEval extends BaseStep implements StepInterface {
  private static Class<?> PKG = RegexEvalMeta.class; // for i18n purposes,
  // needed by
  // Translator2!!

  private RegexEvalMeta meta;
  private RegexEvalData data;

  public RegexEval( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (RegexEvalMeta) smi;
    data = (RegexEvalData) sdi;

    Object[] row = getRow();

    if ( row == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) { // we just got started

      first = false;

      // get the RowMeta
      data.outputRowMeta = getInputRowMeta().clone();
      int captureIndex = getInputRowMeta().size();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Let's check that Result Field is given
      if ( Utils.isEmpty( environmentSubstitute( meta.getResultFieldName() ) ) ) {
        if ( !meta.isAllowCaptureGroupsFlagSet() ) {
          // Result field is missing !
          logError( BaseMessages.getString( PKG, "RegexEval.Log.ErrorResultFieldMissing" ) );
          throw new KettleStepException( BaseMessages.getString(
            PKG, "RegexEval.Exception.ErrorResultFieldMissing" ) );
        }
        data.indexOfResultField = -1;
      } else {
        if ( meta.isReplacefields() ) {
          data.indexOfResultField = getInputRowMeta().indexOfValue( meta.getResultFieldName() );
        }
        if ( data.indexOfResultField < 0 ) {
          data.indexOfResultField = getInputRowMeta().size();
          captureIndex++;
        }
      }

      // Check if a Field (matcher) is given
      if ( meta.getMatcher() == null ) {
        // Matcher is missing !
        logError( BaseMessages.getString( PKG, "RegexEval.Log.ErrorMatcherMissing" ) );
        throw new KettleStepException( BaseMessages.getString( PKG, "RegexEval.Exception.ErrorMatcherMissing" ) );
      }

      // Cache the position of the Field
      data.indexOfFieldToEvaluate = getInputRowMeta().indexOfValue( meta.getMatcher() );
      if ( data.indexOfFieldToEvaluate < 0 ) {
        // The field is unreachable !
        logError( BaseMessages.getString( PKG, "RegexEval.Log.ErrorFindingField" ) + "[" + meta.getMatcher() + "]" );
        throw new KettleStepException( BaseMessages.getString( PKG, "RegexEval.Exception.CouldnotFindField", meta
          .getMatcher() ) );
      }

      // Cache the position of the CaptureGroups
      if ( meta.isAllowCaptureGroupsFlagSet() ) {
        data.positions = new int[meta.getFieldName().length];
        String[] fieldName = meta.getFieldName();
        for ( int i = 0; i < fieldName.length; i++ ) {
          if ( fieldName[i] == null || fieldName[i].length() == 0 ) {
            continue;
          }
          if ( meta.isReplacefields() ) {
            data.positions[i] = data.outputRowMeta.indexOfValue( fieldName[i] );
          } else {
            data.positions[i] = captureIndex;
            captureIndex++;
          }
        }
      } else {
        data.positions = new int[0];
      }

      // Now create objects to do string to data type conversion...
      //
      data.conversionRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
    }

    // reserve room
    Object[] outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    System.arraycopy( row, 0, outputRow, 0, getInputRowMeta().size() );

    try {
      // Get the Field value
      String fieldValue;
      boolean isMatch;

      if ( getInputRowMeta().isNull( row, data.indexOfFieldToEvaluate ) ) {
        fieldValue = "";
        isMatch = false;
      } else {
        fieldValue = getInputRowMeta().getString( row, data.indexOfFieldToEvaluate );

        // Start search engine
        Matcher m = data.pattern.matcher( fieldValue );
        isMatch = m.matches();

        if ( meta.isAllowCaptureGroupsFlagSet() && data.positions.length != m.groupCount() ) {
          // Runtime exception case. The number of capture groups in the
          // regex doesn't match the number of fields.
          logError( BaseMessages.getString( PKG, "RegexEval.Log.ErrorCaptureGroupFieldsMismatch", String
            .valueOf( m.groupCount() ), String.valueOf( data.positions.length ) ) );
          throw new KettleStepException( BaseMessages.getString(
            PKG, "RegexEval.Exception.ErrorCaptureGroupFieldsMismatch", String.valueOf( m.groupCount() ), String
              .valueOf( data.positions.length ) ) );
        }

        for ( int i = 0; i < data.positions.length; i++ ) {
          int index = data.positions[i];
          String value;
          if ( isMatch ) {
            value = m.group( i + 1 );
          } else {
            value = null;
          }

          // this part (or possibly the whole) of the regex didn't match
          // preserve the incoming data, but allow for "trim type", etc.
          if ( value == null ) {
            try {
              value = data.outputRowMeta.getString( outputRow, index );
            } catch ( ArrayIndexOutOfBoundsException err ) {
              // Ignore errors
            }
          }

          ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta( index );
          ValueMetaInterface conversionValueMeta = data.conversionRowMeta.getValueMeta( index );
          Object convertedValue =
            valueMeta.convertDataFromString( value, conversionValueMeta, meta.getFieldNullIf()[i], meta
              .getFieldIfNull()[i], meta.getFieldTrimType()[i] );

          outputRow[index] = convertedValue;
        }
      }

      if ( data.indexOfResultField >= 0 ) {
        outputRow[data.indexOfResultField] = isMatch;
      }

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "RegexEval.Log.ReadRow" )
          + " " + getInputRowMeta().getString( row ) );
      }

      // copy row to output rowset(s);
      //
      putRow( data.outputRowMeta, outputRow );
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        throw new KettleStepException( BaseMessages.getString( PKG, "RegexEval.Log.ErrorInStep" ), e );
      }

      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), outputRow, 1, errorMessage, null, "REGEX001" );
      }
    }
    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RegexEvalMeta) smi;
    data = (RegexEvalData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Embedded options
      String options = meta.getRegexOptions();

      // Regular expression
      String regularexpression = meta.getScript();
      if ( meta.isUseVariableInterpolationFlagSet() ) {
        regularexpression = environmentSubstitute( meta.getScript() );
      }
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "RegexEval.Log.Regexp" ) + " " + options + regularexpression );
      }

      if ( meta.isCanonicalEqualityFlagSet() ) {
        data.pattern = Pattern.compile( options + regularexpression, Pattern.CANON_EQ );
      } else {
        data.pattern = Pattern.compile( options + regularexpression );
      }
      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RegexEvalMeta) smi;
    data = (RegexEvalData) sdi;

    data.pattern = null;

    super.dispose( smi, sdi );
  }

}
