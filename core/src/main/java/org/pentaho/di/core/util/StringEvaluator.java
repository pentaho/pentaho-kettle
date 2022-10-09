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

package org.pentaho.di.core.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;

/**
 * This class evaluates strings and extracts a data type. It allows you to criteria after which the analysis should be
 * completed.
 *
 * @author matt
 */
public class StringEvaluator {

  private Set<String> values;
  private List<StringEvaluationResult> evaluationResults;
  private int maxLength;
  private int maxPrecision;
  private int count;
  private boolean tryTrimming;

  private ValueMetaInterface stringMeta;

  private String[] dateFormats;
  private String[] numberFormats;

  private static final String[] DEFAULT_NUMBER_FORMATS = new String[]
  {
    "#,###,###.#",
    "#.#",
    "#",
    "#.0",
    "#.00",
    "#.000",
    "#.0000",
    "#.00000",
    "#.000000",
    " #.0#"
  };

  protected static final Pattern PRECISION_PATTERN = Pattern.compile( "[^0-9#]" );

  public StringEvaluator() {
    this( true );
  }

  public StringEvaluator( boolean tryTrimming ) {
    this( tryTrimming, DEFAULT_NUMBER_FORMATS, Const.getDateFormats() );
  }

  public StringEvaluator( boolean tryTrimming, List<String> numberFormats, List<String> dateFormats ) {
    this( tryTrimming, numberFormats.toArray( new String[ numberFormats.size() ] ), dateFormats
      .toArray( new String[ dateFormats.size() ] ) );
  }

  public StringEvaluator( boolean tryTrimming, String[] numberFormats, String[] dateFormats ) {
    this.tryTrimming = tryTrimming;

    values = new HashSet<String>();
    evaluationResults = new ArrayList<StringEvaluationResult>();
    count = 0;

    stringMeta = new ValueMetaString( "string" );
    this.numberFormats = numberFormats;
    this.dateFormats = dateFormats;

    populateConversionMetaList();
  }

  public void evaluateString( String value ) {
    count++;

    if ( !values.contains( value ) ) {
      values.add( value );

      if ( value != null ) {
        evaluateLength( value );
        evaluatePrecision( value );
        challengeConversions( value );
      }
    }
  }

  private void challengeConversions( String value ) {
    List<StringEvaluationResult> all = new ArrayList<StringEvaluationResult>( evaluationResults );
    ValueMetaInterface stringMetaClone = null;
    for ( StringEvaluationResult cmm : all ) {
      if ( cmm.getConversionMeta().isBoolean() ) {
        // Boolean conversion never fails.
        // If it's a Y, N, true, false it's a boolean otherwise it ain't.
        //
        String string;
        if ( tryTrimming ) {
          string = Const.trim( value );
        } else {
          string = value;
        }
        if ( StringUtils.isEmpty( value ) ) {
          cmm.incrementNrNull();
        } else if ( !( "Y".equalsIgnoreCase( string ) || "N".equalsIgnoreCase( string ) || "TRUE".equalsIgnoreCase(
            string ) || "FALSE".equalsIgnoreCase( string ) ) ) {
          evaluationResults.remove( cmm );
        } else {
          cmm.incrementSuccesses();
        }
      } else if ( cmm.getConversionMeta().isDate() ) {
        String dateFormat = cmm.getConversionMeta().getConversionMask();
        if ( !DateDetector.isValidDateFormatToStringDate( dateFormat, value, "en_US" ) ) {
          evaluationResults.remove( cmm );
        } else {
          try {
            Object object = DateDetector.getDateFromStringByFormat( value, dateFormat );
            cmm.incrementSuccesses();
            if ( cmm.getMin() == null || cmm.getConversionMeta().compare( cmm.getMin(), object ) > 0 ) {
              cmm.setMin( object );
            }
            if ( cmm.getMax() == null || cmm.getConversionMeta().compare( cmm.getMax(), object ) < 0 ) {
              cmm.setMax( object );
            }
          } catch ( ParseException e ) {
            evaluationResults.remove( cmm );
          } catch ( KettleValueException e ) {
            evaluationResults.remove( cmm );
          }
        }
      } else {
        try {
          if ( cmm.getConversionMeta().isNumeric() ) {
            boolean stop = false;
            int nrDots = 0;
            int nrCommas = 0;
            int pos = 0;
            for ( char c : value.toCharArray() ) {

              boolean currencySymbolMatch = !String.valueOf( c ).equals( cmm.getConversionMeta().getCurrencySymbol() )
                  && c != '('
                  && c != ')';

              if ( !Character.isDigit( c )
                  && c != '.'
                  && c != ','
                  && !Character.isSpaceChar( c )
                  && currencySymbolMatch
                  && ( pos > 0 && ( c == '+' || c == '-' ) ) // allow + & - at the 1st position
              ) {
                evaluationResults.remove( cmm );
                stop = true;
                break;
              }

              // If the value contains a decimal or grouping symbol or some sort, it's not an integer
              //
              if ( ( c == '.' && cmm.getConversionMeta().isInteger() )
                  || ( c == ',' && cmm.getConversionMeta().isInteger() ) ) {
                evaluationResults.remove( cmm );
                stop = true;
                break;
              }
              if ( c == '.' ) {
                nrDots++;
              }
              if ( c == ',' ) {
                nrCommas++;
              }
              pos++;
            }

            if ( nrDots > 1 && nrCommas > 1 ) {
              evaluationResults.remove( cmm );
              stop = true;
            }

            if ( stop ) {
              continue;
            }

          }

          if ( stringMetaClone == null ) {
            // avoid cloning each time
            stringMetaClone = stringMeta.clone();
          }
          stringMetaClone.setConversionMetadata( cmm.getConversionMeta() );
          stringMetaClone.setTrimType( cmm.getConversionMeta().getTrimType() );
          Object object = stringMetaClone.convertDataUsingConversionMetaData( value );

          // Still here? Evaluate the data...
          // Keep track of null values, min, max, etc.
          //
          if ( cmm.getConversionMeta().isNull( object ) ) {
            cmm.incrementNrNull();
          } else {
            cmm.incrementSuccesses();
          }
          if ( cmm.getMin() == null || cmm.getConversionMeta().compare( cmm.getMin(), object ) > 0 ) {
            cmm.setMin( object );
          }
          if ( cmm.getMax() == null || cmm.getConversionMeta().compare( cmm.getMax(), object ) < 0 ) {
            cmm.setMax( object );
          }
        } catch ( KettleValueException e ) {
          // This one doesn't work, remove it from the list!
          //
          evaluationResults.remove( cmm );
        }
      }
    }
  }

  private void evaluateLength( String value ) {
    if ( value.length() > maxLength ) {
      maxLength = value.length();
    }
  }

  private void evaluatePrecision( String value ) {
    int p = determinePrecision( value );
    if ( p > maxPrecision ) {
      maxPrecision = p;
    }
  }

  private boolean containsInteger() {
    for ( StringEvaluationResult result : evaluationResults ) {
      if ( result.getConversionMeta().isInteger() && result.getNrSuccesses() > 0 ) {
        return true;
      }
    }
    return false;
  }

  private boolean containsNumber() {
    for ( StringEvaluationResult result : evaluationResults ) {
      if ( result.getConversionMeta().isNumber() && result.getNrSuccesses() > 0 ) {
        return true;
      }
    }
    return false;
  }

  private boolean containsDate() {
    for ( StringEvaluationResult result : evaluationResults ) {
      if ( result.getConversionMeta().isDate() && result.getNrSuccesses() > 0 ) {
        return true;
      }
    }
    return false;
  }

  public StringEvaluationResult getAdvicedResult() {
    if ( evaluationResults.isEmpty() ) {
      ValueMetaInterface adviced = new ValueMetaString( "adviced" );
      adviced.setLength( maxLength );
      int nrNulls = 0;
      String min = null;
      String max = null;
      for ( String string : values ) {
        if ( string != null ) {
          if ( min == null || min.compareTo( string ) > 0 ) {
            min = string;
          }
          if ( max == null || max.compareTo( string ) < 0 ) {
            max = string;
          }
        } else {
          nrNulls++;
        }
      }

      StringEvaluationResult result = new StringEvaluationResult( adviced );
      result.setNrNull( nrNulls );
      result.setMin( min );
      result.setMax( max );
      return result;

    } else {
      // If there are Numbers and Integers, pick the integers...
      //
      if ( containsInteger() && containsNumber() ) {
        for ( Iterator<StringEvaluationResult> iterator = evaluationResults.iterator(); iterator.hasNext(); ) {
          StringEvaluationResult result = iterator.next();
          if ( maxPrecision == 0 && result.getConversionMeta().isNumber() ) {
            // no precision, don't bother with a number
            iterator.remove();
          } else if ( maxPrecision > 0 && result.getConversionMeta().isInteger() ) {
            // precision is needed, can't use integer
            iterator.remove();
          }
        }
      }
      // If there are Dates and Integers, pick the dates...
      //
      if ( containsInteger() && containsDate() ) {
        for ( Iterator<StringEvaluationResult> iterator = evaluationResults.iterator(); iterator.hasNext(); ) {
          StringEvaluationResult result = iterator.next();
          if ( result.getConversionMeta().isInteger() ) {
            iterator.remove();
          }
        }
      }

      Comparator<StringEvaluationResult> compare = null;
      if ( containsDate() ) {
        // want the longest format for dates
        compare = new Comparator<StringEvaluationResult>() {
          @Override
          public int compare( StringEvaluationResult r1, StringEvaluationResult r2 ) {
            Integer length1 =
                r1.getConversionMeta().getConversionMask() == null ? 0 : r1
                .getConversionMeta().getConversionMask().length();
            Integer length2 =
                r2.getConversionMeta().getConversionMask() == null ? 0 : r2
                .getConversionMeta().getConversionMask().length();
            return length2.compareTo( length1 );
          }
        };
      } else {
        // want the shortest format mask for numerics & integers
        compare = new Comparator<StringEvaluationResult>() {
          @Override
          public int compare( StringEvaluationResult r1, StringEvaluationResult r2 ) {
            Integer length1 =
                r1.getConversionMeta().getConversionMask() == null ? 0 : r1
                .getConversionMeta().getConversionMask().length();
            Integer length2 =
                r2.getConversionMeta().getConversionMask() == null ? 0 : r2
                .getConversionMeta().getConversionMask().length();
            return length1.compareTo( length2 );
          }
        };
      }

      Collections.sort( evaluationResults, compare );

      StringEvaluationResult result = evaluationResults.get( 0 );
      ValueMetaInterface conversionMeta = result.getConversionMeta();
      if ( conversionMeta.isNumber() && conversionMeta.getCurrencySymbol() == null ) {
        conversionMeta.setPrecision( maxPrecision );
        if ( maxPrecision > 0 && maxLength > 0 ) {
          conversionMeta.setLength( maxLength );
        }
      }

      return result;
    }

  }

  public String[] getDateFormats() {
    return dateFormats;
  }

  public String[] getNumberFormats() {
    return numberFormats;
  }

  private void populateConversionMetaList() {

    int[] trimTypes;
    if ( tryTrimming ) {
      trimTypes = new int[] { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_BOTH, };
    } else {
      trimTypes = new int[] { ValueMetaInterface.TRIM_TYPE_NONE, };
    }

    for ( int trimType : trimTypes ) {
      for ( String format : getDateFormats() ) {
        ValueMetaInterface conversionMeta = new ValueMetaDate( "date" );
        conversionMeta.setConversionMask( format );
        conversionMeta.setTrimType( trimType );
        conversionMeta.setDateFormatLenient( false );
        evaluationResults.add( new StringEvaluationResult( conversionMeta ) );
      }

      EvalResultBuilder numberUsBuilder = new EvalResultBuilder( "number-us", ValueMetaInterface.TYPE_NUMBER, 15, trimType, ".", "," );
      EvalResultBuilder numberEuBuilder = new EvalResultBuilder( "number-eu", ValueMetaInterface.TYPE_NUMBER, 15, trimType, ",", "." );

      for ( String format : getNumberFormats() ) {

        if ( format.equals( "#" ) || format.equals( "0" ) ) {
          // skip the integer ones. we'll get those later
          continue;
        }

        int precision = determinePrecision( format );
        evaluationResults.add( numberUsBuilder.format( format, precision ).build() );
        evaluationResults.add( numberEuBuilder.format( format, precision ).build() );
      }

      // Try the locale's Currency
      DecimalFormat currencyFormat = ( (DecimalFormat) NumberFormat.getCurrencyInstance() );

      ValueMetaInterface conversionMeta = new ValueMetaNumber( "number-currency" );
      // replace the universal currency symbol with the locale's currency symbol for user recognition
      String currencyMask = currencyFormat.toLocalizedPattern().replace( "\u00A4", currencyFormat.getCurrency().getSymbol() );
      conversionMeta.setConversionMask( currencyMask );
      conversionMeta.setTrimType( trimType );
      conversionMeta.setDecimalSymbol( String.valueOf( currencyFormat.getDecimalFormatSymbols().getDecimalSeparator() ) );
      conversionMeta.setGroupingSymbol( String.valueOf( currencyFormat.getDecimalFormatSymbols().getGroupingSeparator() ) );
      conversionMeta.setCurrencySymbol( currencyFormat.getCurrency().getSymbol() );
      conversionMeta.setLength( 15 );
      int currencyPrecision = currencyFormat.getCurrency().getDefaultFractionDigits();
      conversionMeta.setPrecision( currencyPrecision );

      evaluationResults.add( new StringEvaluationResult( conversionMeta ) );

      // add same mask w/o currency symbol
      String currencyMaskAsNumeric = currencyMask.replaceAll( Pattern.quote( currencyFormat.getCurrency().getSymbol() ), "" );
      evaluationResults.add( numberUsBuilder.format( currencyMaskAsNumeric, currencyPrecision ).build() );
      evaluationResults.add( numberEuBuilder.format( currencyMaskAsNumeric, currencyPrecision ).build() );

      // Integer
      //
      conversionMeta = new ValueMetaInteger( "integer" );
      conversionMeta.setConversionMask( "#" );
      conversionMeta.setLength( 15 );
      evaluationResults.add( new StringEvaluationResult( conversionMeta ) );

      conversionMeta = new ValueMetaInteger( "integer" );
      conversionMeta.setConversionMask( " #" );
      conversionMeta.setLength( 15 );
      evaluationResults.add( new StringEvaluationResult( conversionMeta ) );

      // Add support for left zero padded integers
      //
      for ( int i = 1; i <= 15; i++ ) {

        String mask = " ";
        for ( int x = 0; x < i; x++ ) {
          mask += "0";
        }
        mask += ";-";
        for ( int x = 0; x < i; x++ ) {
          mask += "0";
        }

        conversionMeta = new ValueMetaInteger( "integer-zero-padded-" + i );
        conversionMeta.setConversionMask( mask );
        conversionMeta.setLength( i );
        evaluationResults.add( new StringEvaluationResult( conversionMeta ) );
      }

      // Boolean
      //
      conversionMeta = new ValueMetaBoolean( "boolean" );
      evaluationResults.add( new StringEvaluationResult( conversionMeta ) );
    }
  }

  protected static int determinePrecision( String numericFormat ) {
    if ( numericFormat != null ) {
      char decimalSymbol = ( (DecimalFormat) NumberFormat.getInstance() ).getDecimalFormatSymbols().getDecimalSeparator();
      int loc = numericFormat.lastIndexOf( decimalSymbol );
      if ( loc >= 0 && loc < numericFormat.length() ) {
        Matcher m = PRECISION_PATTERN.matcher( numericFormat.substring( loc + 1 ) );
        int nonDigitLoc = numericFormat.length();
        if ( m.find() ) {
          nonDigitLoc = loc + 1 + m.start();
        }
        return numericFormat.substring( loc + 1, nonDigitLoc ).length();
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  }

  /**
   * @return The distinct set of string values
   */
  public Set<String> getValues() {
    return values;
  }

  /**
   * PDI-7736: Only list of successful evaluations returned.
   * 
   * @return The list of string evaluation results
   */
  public List<StringEvaluationResult> getStringEvaluationResults() {
    List<StringEvaluationResult> result = new ArrayList<>();
    for ( StringEvaluationResult ev : evaluationResults ) {
      if ( ev.getNrSuccesses() > 0 ) {
        result.add( ev );
      }
    }
    return result;
  }

  /**
   * @return the number of values analyzed
   */
  public int getCount() {
    return count;
  }

  /**
   * @return The maximum string length encountered
   */
  public int getMaxLength() {
    return maxLength;
  }


  private static class EvalResultBuilder {
    private final String name;
    private final int type;
    private final int length;
    private final int trimType;
    private final String decimalSymbol;
    private final String groupingSymbol;

    private String format;
    private int precision;

    public StringEvaluationResult build() {
      ValueMetaInterface meta = new ValueMeta( name, type );
      meta.setConversionMask( format );
      meta.setTrimType( trimType );
      meta.setDecimalSymbol( decimalSymbol );
      meta.setGroupingSymbol( groupingSymbol );
      meta.setLength( length );
      meta.setPrecision( precision );
      return new StringEvaluationResult( meta );
    }

    public EvalResultBuilder( String name, int type, int length, int trimType, String decimalSymbol,
                              String groupingSymbol ) {
      this.name = name;
      this.type = type;
      this.length = length;
      this.trimType = trimType;
      this.decimalSymbol = decimalSymbol;
      this.groupingSymbol = groupingSymbol;
    }

    public EvalResultBuilder format( String format, int precision ) {
      this.format = format;
      this.precision = precision;
      return this;
    }
  }
}
