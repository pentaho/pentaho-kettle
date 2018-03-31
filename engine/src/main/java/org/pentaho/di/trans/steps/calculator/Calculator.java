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

package org.pentaho.di.trans.steps.calculator;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
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
 * Calculate new field values using pre-defined functions.
 *
 * @author Matt
 * @since 8-sep-2005
 */
public class Calculator extends BaseStep implements StepInterface {
  private static Class<?> PKG = CalculatorMeta.class; // for i18n purposes, needed by Translator2!!

  public class FieldIndexes {
    public int indexName;
    public int indexA;
    public int indexB;
    public int indexC;
  }

  private CalculatorMeta meta;
  private CalculatorData data;

  public Calculator( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                     Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (CalculatorMeta) smi;
    data = (CalculatorData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      data.clearValuesMetaMapping();
      return false;
    }

    if ( first ) {
      first = false;
      data.setOutputRowMeta( getInputRowMeta().clone() );
      meta.getFields( data.getOutputRowMeta(), getStepname(), null, null, this, repository, metaStore );

      // get all metadata, including source rows and temporary fields.
      data.setCalcRowMeta( meta.getAllFields( getInputRowMeta() ) );

      data.setFieldIndexes( new FieldIndexes[meta.getCalculation().length] );
      List<Integer> tempIndexes = new ArrayList<Integer>();

      // Calculate the indexes of the values and arguments in the target data or temporary data
      // We do this in advance to save time later on.
      //
      //CHECKSTYLE:Indentation:OFF
      for ( int i = 0; i < meta.getCalculation().length; i++ ) {
        CalculatorMetaFunction function = meta.getCalculation()[i];
        data.getFieldIndexes()[i] = new FieldIndexes();

        if ( !Utils.isEmpty( function.getFieldName() ) ) {
          data.getFieldIndexes()[i].indexName = data.getCalcRowMeta().indexOfValue( function.getFieldName() );
          if ( data.getFieldIndexes()[i].indexName < 0 ) {
            // Nope: throw an exception
            throw new KettleStepException( BaseMessages.getString(
              PKG, "Calculator.Error.UnableFindField", function.getFieldName(), "" + ( i + 1 ) ) );
          }
        } else {
          throw new KettleStepException( BaseMessages.getString( PKG, "Calculator.Error.NoNameField", ""
            + ( i + 1 ) ) );
        }

        if ( !Utils.isEmpty( function.getFieldA() ) ) {
          if ( function.getCalcType() != CalculatorMetaFunction.CALC_CONSTANT ) {
            data.getFieldIndexes()[i].indexA = data.getCalcRowMeta().indexOfValue( function.getFieldA() );
            if ( data.getFieldIndexes()[i].indexA < 0 ) {
              // Nope: throw an exception
              throw new KettleStepException( "Unable to find the first argument field '"
                + function.getFieldName() + " for calculation #" + ( i + 1 ) );
            }
          } else {
            data.getFieldIndexes()[i].indexA = -1;
          }
        } else {
          throw new KettleStepException( "There is no first argument specified for calculated field #" + ( i + 1 ) );
        }

        if ( !Utils.isEmpty( function.getFieldB() ) ) {
          data.getFieldIndexes()[i].indexB = data.getCalcRowMeta().indexOfValue( function.getFieldB() );
          if ( data.getFieldIndexes()[i].indexB < 0 ) {
            // Nope: throw an exception
            throw new KettleStepException( "Unable to find the second argument field '"
              + function.getFieldName() + " for calculation #" + ( i + 1 ) );
          }
        }
        data.getFieldIndexes()[i].indexC = -1;
        if ( !Utils.isEmpty( function.getFieldC() ) ) {
          data.getFieldIndexes()[i].indexC = data.getCalcRowMeta().indexOfValue( function.getFieldC() );
          if ( data.getFieldIndexes()[i].indexC < 0 ) {
            // Nope: throw an exception
            throw new KettleStepException( "Unable to find the third argument field '"
              + function.getFieldName() + " for calculation #" + ( i + 1 ) );
          }
        }

        if ( function.isRemovedFromResult() ) {
          tempIndexes.add( getInputRowMeta().size() + i );
        }
      }

      // Convert temp indexes to int[]
      data.setTempIndexes( new int[tempIndexes.size()] );
      for ( int i = 0; i < data.getTempIndexes().length; i++ ) {
        data.getTempIndexes()[i] = tempIndexes.get( i );
      }
    }

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "Calculator.Log.ReadRow" )
        + getLinesRead() + " : " + getInputRowMeta().getString( r ) );
    }

    try {
      Object[] row = calcFields( getInputRowMeta(), r );
      putRow( data.getOutputRowMeta(), row ); // copy row to possible alternate rowset(s).

      if ( log.isRowLevel() ) {
        logRowlevel( "Wrote row #" + getLinesWritten() + " : " + getInputRowMeta().getString( r ) );
      }
      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "Calculator.Log.Linenr", "" + getLinesRead() ) );
        }
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        putError( getInputRowMeta(), r, 1, e.toString(), null, "CALC001" );
      } else {
        logError( BaseMessages.getString( PKG, "Calculator.ErrorInStepRunning" + " : " + e.getMessage() ) );
        throw new KettleStepException( BaseMessages.getString( PKG, "Calculator.ErrorInStepRunning" ), e );
      }
    }
    return true;
  }

  /**
   * @param inputRowMeta
   *          the input row metadata
   * @param r
   *          the input row (data)
   * @return A row including the calculations, excluding the temporary values
   * @throws KettleValueException
   *           in case there is a calculation error.
   */
  private Object[] calcFields( RowMetaInterface inputRowMeta, Object[] r ) throws KettleValueException {
    // First copy the input data to the new result...
    Object[] calcData = RowDataUtil.resizeArray( r, data.getCalcRowMeta().size() );

    for ( int i = 0, index = inputRowMeta.size() + i; i < meta.getCalculation().length; i++, index++ ) {
      CalculatorMetaFunction fn = meta.getCalculation()[i];
      if ( !Utils.isEmpty( fn.getFieldName() ) ) {
        ValueMetaInterface targetMeta = data.getCalcRowMeta().getValueMeta( index );

        // Get the metadata & the data...
        // ValueMetaInterface metaTarget = data.calcRowMeta.getValueMeta(i);

        ValueMetaInterface metaA = null;
        Object dataA = null;

        if ( data.getFieldIndexes()[i].indexA >= 0 ) {
          metaA = data.getCalcRowMeta().getValueMeta( data.getFieldIndexes()[ i ].indexA );
          dataA = calcData[ data.getFieldIndexes()[i].indexA];
        }

        ValueMetaInterface metaB = null;
        Object dataB = null;

        if ( data.getFieldIndexes()[i].indexB >= 0 ) {
          metaB = data.getCalcRowMeta().getValueMeta( data.getFieldIndexes()[ i ].indexB );
          dataB = calcData[ data.getFieldIndexes()[i].indexB];
        }

        ValueMetaInterface metaC = null;
        Object dataC = null;

        if ( data.getFieldIndexes()[i].indexC >= 0 ) {
          metaC = data.getCalcRowMeta().getValueMeta( data.getFieldIndexes()[ i ].indexC );
          dataC = calcData[ data.getFieldIndexes()[i].indexC];
        }

        int calcType = fn.getCalcType();
        // The data types are those of the first argument field, convert to the target field.
        // Exceptions:
        // - multiply can be string
        // - constant is string
        // - all date functions except add days/months
        // - hex encode / decodes

        int resultType;
        if ( metaA != null ) {
          resultType = metaA.getType();
        } else {
          resultType = ValueMetaInterface.TYPE_NONE;
        }

        switch ( calcType ) {
          case CalculatorMetaFunction.CALC_NONE:
            break;
          case CalculatorMetaFunction.CALC_COPY_OF_FIELD: // Create a copy of field A

            calcData[index] = dataA;

            break;
          case CalculatorMetaFunction.CALC_ADD: // A + B
            calcData[index] = ValueDataUtil.plus( metaA, dataA, metaB, dataB );
            if ( metaA.isString() || metaB.isString() ) {
              resultType = ValueMetaInterface.TYPE_STRING;
            }
            break;
          case CalculatorMetaFunction.CALC_SUBTRACT: // A - B
            calcData[index] = ValueDataUtil.minus( metaA, dataA, metaB, dataB );
            if ( metaA.isDate() ) {
              resultType = ValueMetaInterface.TYPE_INTEGER;
            }
            break;
          case CalculatorMetaFunction.CALC_MULTIPLY: // A * B
            calcData[index] = ValueDataUtil.multiply( metaA, dataA, metaB, dataB );
            if ( metaA.isString() || metaB.isString() ) {
              resultType = ValueMetaInterface.TYPE_STRING;
            }
            break;
          case CalculatorMetaFunction.CALC_DIVIDE: // A / B
            calcData[index] = ValueDataUtil.divide( metaA, dataA, metaB, dataB );
            break;
          case CalculatorMetaFunction.CALC_SQUARE: // A * A
            calcData[index] = ValueDataUtil.multiply( metaA, dataA, metaA, dataA );
            break;
          case CalculatorMetaFunction.CALC_SQUARE_ROOT: // SQRT( A )
            calcData[index] = ValueDataUtil.sqrt( metaA, dataA );
            break;
          case CalculatorMetaFunction.CALC_PERCENT_1: // 100 * A / B
            calcData[index] = ValueDataUtil.percent1( metaA, dataA, metaB, dataB );
            break;
          case CalculatorMetaFunction.CALC_PERCENT_2: // A - ( A * B / 100 )
            calcData[index] = ValueDataUtil.percent2( metaA, dataA, metaB, dataB );
            break;
          case CalculatorMetaFunction.CALC_PERCENT_3: // A + ( A * B / 100 )
            calcData[index] = ValueDataUtil.percent3( metaA, dataA, metaB, dataB );
            break;
          case CalculatorMetaFunction.CALC_COMBINATION_1: // A + B * C
            calcData[index] = ValueDataUtil.combination1( metaA, dataA, metaB, dataB, metaC, dataC );
            break;
          case CalculatorMetaFunction.CALC_COMBINATION_2: // SQRT( A*A + B*B )
            calcData[index] = ValueDataUtil.combination2( metaA, dataA, metaB, dataB );
            break;
          case CalculatorMetaFunction.CALC_ROUND_1: // ROUND( A )
            calcData[index] = ValueDataUtil.round( metaA, dataA );
            break;
          case CalculatorMetaFunction.CALC_ROUND_2: // ROUND( A , B )
            calcData[index] = ValueDataUtil.round( metaA, dataA, metaB, dataB );
            break;
          case CalculatorMetaFunction.CALC_ROUND_CUSTOM_1: // ROUND( A , B )
            calcData[index] = ValueDataUtil.round( metaA, dataA, metaB.getNumber( dataB ).intValue() );
            break;
          case CalculatorMetaFunction.CALC_ROUND_CUSTOM_2: // ROUND( A , B, C )
            calcData[index] = ValueDataUtil.round( metaA, dataA, metaB, dataB, metaC.getNumber( dataC ).intValue() );
            break;
          case CalculatorMetaFunction.CALC_ROUND_STD_1: // ROUND( A )
            calcData[index] = ValueDataUtil.round( metaA, dataA, java.math.BigDecimal.ROUND_HALF_UP );
            break;
          case CalculatorMetaFunction.CALC_ROUND_STD_2: // ROUND( A , B )
            calcData[index] = ValueDataUtil.round( metaA, dataA, metaB, dataB, java.math.BigDecimal.ROUND_HALF_UP );
            break;
          case CalculatorMetaFunction.CALC_CEIL: // CEIL( A )
            calcData[index] = ValueDataUtil.ceil( metaA, dataA );
            break;
          case CalculatorMetaFunction.CALC_FLOOR: // FLOOR( A )
            calcData[index] = ValueDataUtil.floor( metaA, dataA );
            break;
          case CalculatorMetaFunction.CALC_CONSTANT: // Set field to constant value...
            calcData[index] = fn.getFieldA(); // A string
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_NVL: // Replace null values with another value
            calcData[index] = ValueDataUtil.nvl( metaA, dataA, metaB, dataB );
            break;
          case CalculatorMetaFunction.CALC_ADD_DAYS: // Add B days to date field A
            calcData[index] = ValueDataUtil.addDays( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ADD_HOURS: // Add B hours to date field A
            calcData[index] = ValueDataUtil.addHours( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ADD_MINUTES: // Add B minutes to date field A
            calcData[index] = ValueDataUtil.addMinutes( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_YEAR_OF_DATE: // What is the year (Integer) of a date?
            calcData[index] = ValueDataUtil.yearOfDate( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_MONTH_OF_DATE: // What is the month (Integer) of a date?
            calcData[index] = ValueDataUtil.monthOfDate( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DAY_OF_YEAR: // What is the day of year (Integer) of a date?
            calcData[index] = ValueDataUtil.dayOfYear( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DAY_OF_MONTH: // What is the day of month (Integer) of a date?
            calcData[index] = ValueDataUtil.dayOfMonth( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DAY_OF_WEEK: // What is the day of week (Integer) of a date?
            calcData[index] = ValueDataUtil.dayOfWeek( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_WEEK_OF_YEAR: // What is the week of year (Integer) of a date?
            calcData[index] = ValueDataUtil.weekOfYear( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_WEEK_OF_YEAR_ISO8601: // What is the week of year (Integer) of a date ISO8601
                                                                 // style?
            calcData[index] = ValueDataUtil.weekOfYearISO8601( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_YEAR_OF_DATE_ISO8601: // What is the year (Integer) of a date ISO8601 style?
            calcData[index] = ValueDataUtil.yearOfDateISO8601( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_BYTE_TO_HEX_ENCODE: // Byte to Hex encode string field A
            calcData[index] = ValueDataUtil.byteToHexEncode( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_HEX_TO_BYTE_DECODE: // Hex to Byte decode string field A
            calcData[index] = ValueDataUtil.hexToByteDecode( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;

          case CalculatorMetaFunction.CALC_CHAR_TO_HEX_ENCODE: // Char to Hex encode string field A
            calcData[index] = ValueDataUtil.charToHexEncode( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_HEX_TO_CHAR_DECODE: // Hex to Char decode string field A
            calcData[index] = ValueDataUtil.hexToCharDecode( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_CRC32: // CRC32
            calcData[index] = ValueDataUtil.ChecksumCRC32( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ADLER32: // ADLER32
            calcData[index] = ValueDataUtil.ChecksumAdler32( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_MD5: // MD5
            calcData[index] = ValueDataUtil.createChecksum( metaA, dataA, "MD5" );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_SHA1: // SHA-1
            calcData[index] = ValueDataUtil.createChecksum( metaA, dataA, "SHA-1" );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_LEVENSHTEIN_DISTANCE: // LEVENSHTEIN DISTANCE
            calcData[index] = ValueDataUtil.getLevenshtein_Distance( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_METAPHONE: // METAPHONE
            calcData[index] = ValueDataUtil.get_Metaphone( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DOUBLE_METAPHONE: // Double METAPHONE
            calcData[index] = ValueDataUtil.get_Double_Metaphone( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ABS: // ABS( A )
            calcData[index] = ValueDataUtil.abs( metaA, dataA );
            break;
          case CalculatorMetaFunction.CALC_REMOVE_TIME_FROM_DATE: // Remove Time from field A
            calcData[index] = ValueDataUtil.removeTimeFromDate( metaA, dataA );
            break;
          case CalculatorMetaFunction.CALC_DATE_DIFF: // DateA - DateB
            calcData[index] = ValueDataUtil.DateDiff( metaA, dataA, metaB, dataB, "d" );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ADD3: // A + B + C
            calcData[index] = ValueDataUtil.plus3( metaA, dataA, metaB, dataB, metaC, dataC );
            if ( metaA.isString() || metaB.isString() || metaC.isString() ) {
              resultType = ValueMetaInterface.TYPE_STRING;
            }
            break;
          case CalculatorMetaFunction.CALC_INITCAP: // InitCap( A )
            calcData[index] = ValueDataUtil.initCap( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_UPPER_CASE: // UpperCase( A )
            calcData[index] = ValueDataUtil.upperCase( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_LOWER_CASE: // UpperCase( A )
            calcData[index] = ValueDataUtil.lowerCase( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_MASK_XML: // escapeXML( A )
            calcData[index] = ValueDataUtil.escapeXML( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_USE_CDATA: // CDATA( A )
            calcData[index] = ValueDataUtil.useCDATA( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_REMOVE_CR: // REMOVE CR FROM A
            calcData[index] = ValueDataUtil.removeCR( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_REMOVE_LF: // REMOVE LF FROM A
            calcData[index] = ValueDataUtil.removeLF( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_REMOVE_CRLF: // REMOVE CRLF FROM A
            calcData[index] = ValueDataUtil.removeCRLF( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_REMOVE_TAB: // REMOVE TAB FROM A
            calcData[index] = ValueDataUtil.removeTAB( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_GET_ONLY_DIGITS: // GET ONLY DIGITS FROM A
            calcData[index] = ValueDataUtil.getDigits( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_REMOVE_DIGITS: // REMOVE DIGITS FROM A
            calcData[index] = ValueDataUtil.removeDigits( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_STRING_LEN: // RETURN THE LENGTH OF A
            calcData[index] = ValueDataUtil.stringLen( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_LOAD_FILE_CONTENT_BINARY: // LOAD CONTENT OF A FILE A IN A BLOB
            calcData[index] = ValueDataUtil.loadFileContentInBinary( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ADD_TIME_TO_DATE: // Add time B to a date A
            calcData[index] = ValueDataUtil.addTimeToDate( metaA, dataA, metaB, dataB, metaC, dataC );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_QUARTER_OF_DATE: // What is the quarter (Integer) of a date?
            calcData[index] = ValueDataUtil.quarterOfDate( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_SUBSTITUTE_VARIABLE: // variable substitution in string
            calcData[index] = environmentSubstitute( dataA.toString() );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_UNESCAPE_XML: // UnescapeXML( A )
            calcData[index] = ValueDataUtil.unEscapeXML( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ESCAPE_HTML: // EscapeHTML( A )
            calcData[index] = ValueDataUtil.escapeHTML( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_UNESCAPE_HTML: // UnescapeHTML( A )
            calcData[index] = ValueDataUtil.unEscapeHTML( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ESCAPE_SQL: // EscapeSQL( A )
            calcData[index] = ValueDataUtil.escapeSQL( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DATE_WORKING_DIFF: // DateWorkingDiff( A , B)
            calcData[index] = ValueDataUtil.DateWorkingDiff( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ADD_MONTHS: // Add B months to date field A
            calcData[index] = ValueDataUtil.addMonths( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_CHECK_XML_FILE_WELL_FORMED: // Check if file A is well formed
            calcData[index] = ValueDataUtil.isXMLFileWellFormed( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_CHECK_XML_WELL_FORMED: // Check if xml A is well formed
            calcData[index] = ValueDataUtil.isXMLWellFormed( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_GET_FILE_ENCODING: // Get file encoding from a file A
            calcData[index] = ValueDataUtil.getFileEncoding( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DAMERAU_LEVENSHTEIN: // DAMERAULEVENSHTEIN DISTANCE
            calcData[index] = ValueDataUtil.getDamerauLevenshtein_Distance( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_NEEDLEMAN_WUNSH: // NEEDLEMANWUNSH DISTANCE
            calcData[index] = ValueDataUtil.getNeedlemanWunsch_Distance( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_JARO: // Jaro DISTANCE
            calcData[index] = ValueDataUtil.getJaro_Similitude( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_JARO_WINKLER: // Jaro DISTANCE
            calcData[index] = ValueDataUtil.getJaroWinkler_Similitude( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_SOUNDEX: // SOUNDEX
            calcData[index] = ValueDataUtil.get_SoundEx( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_REFINED_SOUNDEX: // REFINEDSOUNDEX
            calcData[index] = ValueDataUtil.get_RefinedSoundEx( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DATE_DIFF_MSEC: // DateA - DateB (ms)
            calcData[index] = ValueDataUtil.DateDiff( metaA, dataA, metaB, dataB, "ms" );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DATE_DIFF_SEC: // DateA - DateB (s)
            calcData[index] = ValueDataUtil.DateDiff( metaA, dataA, metaB, dataB, "s" );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DATE_DIFF_MN: // DateA - DateB (mn)
            calcData[index] = ValueDataUtil.DateDiff( metaA, dataA, metaB, dataB, "mn" );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_DATE_DIFF_HR: // DateA - DateB (h)
            calcData[index] = ValueDataUtil.DateDiff( metaA, dataA, metaB, dataB, "h" );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_HOUR_OF_DAY:
            calcData[index] = ValueDataUtil.hourOfDay( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_MINUTE_OF_HOUR:
            calcData[index] = ValueDataUtil.minuteOfHour( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_SECOND_OF_MINUTE:
            calcData[index] = ValueDataUtil.secondOfMinute( metaA, dataA );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_ADD_SECONDS: // Add B seconds to date field A
            calcData[index] = ValueDataUtil.addSeconds( metaA, dataA, metaB, dataB );
            resultType = CalculatorMetaFunction.calcDefaultResultType[calcType];
            break;
          case CalculatorMetaFunction.CALC_REMAINDER:
            if ( targetMeta.getType() != metaA.getType() || targetMeta.getType() != metaB.getType() ) {
              dataA = targetMeta.convertData( metaA, dataA );
              metaA = targetMeta.clone();
              dataB = targetMeta.convertData( metaB, dataB );
              metaB = targetMeta.clone();
            }
            calcData[index] = ValueDataUtil.remainder( metaA, dataA, metaB, dataB );
            resultType = targetMeta.getType();
            break;
          default:
            throw new KettleValueException( BaseMessages.getString( PKG, "Calculator.Log.UnknownCalculationType" )
              + fn.getCalcType() );
        }

        // If we don't have a target data type, throw an error.
        // Otherwise the result is non-deterministic.
        //
        if ( targetMeta.getType() == ValueMetaInterface.TYPE_NONE ) {
          throw new KettleValueException( BaseMessages.getString( PKG, "Calculator.Log.NoType" )
            + ( i + 1 ) + " : " + fn.getFieldName() + " = " + fn.getCalcTypeDesc() + " / "
            + fn.getCalcTypeLongDesc() );
        }

        // Convert the data to the correct target data type.
        //
        if ( calcData[index] != null ) {
          if ( targetMeta.getType() != resultType ) {
            ValueMetaInterface resultMeta;
            try {
              // clone() is not necessary as one data instance belongs to one step instance and no race condition occurs
              resultMeta = data.getValueMetaFor( resultType, "result" );
            } catch ( Exception exception ) {
              throw new KettleValueException( "Error creating value" );
            }
            resultMeta.setConversionMask( fn.getConversionMask() );
            resultMeta.setGroupingSymbol( fn.getGroupingSymbol() );
            resultMeta.setDecimalSymbol( fn.getDecimalSymbol() );
            resultMeta.setCurrencySymbol( fn.getCurrencySymbol() );
            try {
              calcData[index] = targetMeta.convertData( resultMeta, calcData[index] );
            } catch ( Exception ex ) {
              throw new KettleValueException( "resultType: "
                + resultType + "; targetMeta: " + targetMeta.getType(), ex );
            }
          }
        }
      }
    }

    // OK, now we should refrain from adding the temporary fields to the result.
    // So we remove them.
    //
    return RowDataUtil.removeItems( calcData, data.getTempIndexes() );
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CalculatorMeta) smi;
    data = (CalculatorData) sdi;

    return super.init( smi, sdi );
  }
}
