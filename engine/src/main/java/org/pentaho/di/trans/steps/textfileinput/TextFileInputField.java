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

package org.pentaho.di.trans.steps.textfileinput;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.TextFileInputFieldInterface;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;


/**
 * Describes a single field in a text file
 *
 * @author Matt
 * @since 19-04-2004
 *
 * @deprecated replaced by implementation in the ...steps.fileinput.text package
 */
@Deprecated
public class TextFileInputField implements Cloneable, TextFileInputFieldInterface {

  @Injection( name = "INPUT_NAME", group = "INPUT_FIELDS" )
  private String name;

  @Injection( name = "INPUT_POSITION", group = "INPUT_FIELDS" )
  private int position;

  @Injection( name = "INPUT_LENGTH", group = "INPUT_FIELDS" )
  private int length;

  private int type;

  @Injection( name = "INPUT_IGNORE", group = "INPUT_FIELDS" )
  private boolean ignore;

  @Injection( name = "INPUT_FORMAT", group = "INPUT_FIELDS" )
  private String format;

  private int trimtype;

  @Injection( name = "INPUT_PRECISION", group = "INPUT_FIELDS" )
  private int precision;

  @Injection( name = "INPUT_CURRENCY", group = "INPUT_FIELDS" )
  private String currencySymbol;

  @Injection( name = "INPUT_DECIMAL", group = "INPUT_FIELDS" )
  private String decimalSymbol;

  @Injection( name = "INPUT_GROUP", group = "INPUT_FIELDS" )
  private String groupSymbol;

  @Injection( name = "INPUT_REPEAT", group = "INPUT_FIELDS" )
  private boolean repeat;

  @Injection( name = "INPUT_NULL_STRING", group = "INPUT_FIELDS" )
  private String nullString;

  @Injection( name = "INPUT_IF_NULL", group = "INPUT_FIELDS" )
  private String ifNullValue;

  private String[] samples;

  // Guess fields...
  private NumberFormat nf;
  private DecimalFormat df;
  private DecimalFormatSymbols dfs;
  private SimpleDateFormat daf;

  // private boolean containsDot;
  // private boolean containsComma;

  private static final String[] date_formats = new String[] {
    "yyyy/MM/dd HH:mm:ss.SSS", "yyyy/MM/dd HH:mm:ss", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd", "yyyy-MM-dd",
    "yyyyMMdd", "ddMMyyyy", "d-M-yyyy", "d/M/yyyy", "d-M-yy", "d/M/yy", };

  private static final String[] number_formats = new String[] {
    "", "#", Const.DEFAULT_NUMBER_FORMAT, "0.00", "0000000000000", "###,###,###.#######",
    "###############.###############", "#####.###############%", };

  public TextFileInputField( String fieldname, int position, int length ) {
    this.name = fieldname;
    this.position = position;
    this.length = length;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.ignore = false;
    this.format = "";
    this.trimtype = ValueMetaInterface.TRIM_TYPE_NONE;
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
    this.nullString = "";
    this.ifNullValue = "";
    // this.containsDot=false;
    // this.containsComma=false;
  }

  public TextFileInputField() {
    this( null, -1, -1 );
  }

  public int compare( Object obj ) {
    TextFileInputField field = (TextFileInputField) obj;

    return position - field.getPosition();
  }

  @Override
  public int compareTo( TextFileInputFieldInterface field ) {
    return position - field.getPosition();
  }

  public boolean equal( Object obj ) {
    TextFileInputField field = (TextFileInputField) obj;

    return ( position == field.getPosition() );
  }

  @Override
  public Object clone() {
    try {
      Object retval = super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  @Override
  public int getPosition() {
    return position;
  }

  public void setPosition( int position ) {
    this.position = position;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public void setLength( int length ) {
    this.length = length;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName( String fieldname ) {
    this.name = fieldname;
  }

  public int getType() {
    return type;
  }

  public String getTypeDesc() {
    return ValueMetaFactory.getValueMetaName( type );
  }

  public void setType( int type ) {
    this.type = type;
  }

  public boolean isIgnored() {
    return ignore;
  }

  public void setIgnored( boolean ignore ) {
    this.ignore = ignore;
  }

  public void flipIgnored() {
    ignore = !ignore;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public void setSamples( String[] samples ) {
    this.samples = samples;
  }

  public String[] getSamples( ) {
    return this.samples;
  }

  public int getTrimType() {
    return trimtype;
  }

  public String getTrimTypeCode() {
    return ValueMetaString.getTrimTypeCode( trimtype );
  }

  public String getTrimTypeDesc() {
    return ValueMetaString.getTrimTypeDesc( trimtype );
  }

  public void setTrimType( int trimtype ) {
    this.trimtype = trimtype;
  }

  public String getGroupSymbol() {
    return groupSymbol;
  }

  public void setGroupSymbol( String group_symbol ) {
    this.groupSymbol = group_symbol;
  }

  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  public void setDecimalSymbol( String decimal_symbol ) {
    this.decimalSymbol = decimal_symbol;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol( String currency_symbol ) {
    this.currencySymbol = currency_symbol;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  public boolean isRepeated() {
    return repeat;
  }

  public void setRepeated( boolean repeat ) {
    this.repeat = repeat;
  }

  public void flipRepeated() {
    repeat = !repeat;
  }

  public String getNullString() {
    return nullString;
  }

  public void setNullString( String null_string ) {
    this.nullString = null_string;
  }

  public String getIfNullValue() {
    return ifNullValue;
  }

  public void setIfNullValue( String ifNullValue ) {
    this.ifNullValue = ifNullValue;
  }

  @Override
  public String toString() {
    return name + "@" + position + ":" + length;
  }

  public void guess() {
    guessTrimType();
    guessType();
    guessIgnore();
  }

  public void guessTrimType() {
    boolean spaces_before = false;
    boolean spaces_after = false;

    for ( int i = 0; i < samples.length; i++ ) {
      spaces_before |= Const.nrSpacesBefore( samples[i] ) > 0;
      spaces_after |= Const.nrSpacesAfter( samples[i] ) > 0;
      samples[i] = Const.trim( samples[i] );
    }

    trimtype = ValueMetaInterface.TRIM_TYPE_NONE;

    if ( spaces_before ) {
      trimtype |= ValueMetaInterface.TRIM_TYPE_LEFT;
    }
    if ( spaces_after ) {
      trimtype |= ValueMetaInterface.TRIM_TYPE_RIGHT;
    }
  }

  public void guessType() {
    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();
    daf = new SimpleDateFormat();

    daf.setLenient( false );

    // Start with a string...
    type = ValueMetaInterface.TYPE_STRING;

    // If we have no samples, we assume a String...
    if ( samples == null ) {
      return;
    }

    // ////////////////////////////
    // DATES
    // ////////////////////////////

    // See if all samples can be transformed into a date...
    int datefmt_cnt = date_formats.length;
    boolean[] datefmt = new boolean[date_formats.length];
    for ( int i = 0; i < date_formats.length; i++ ) {
      datefmt[i] = true;
    }
    int datenul = 0;

    for ( int i = 0; i < samples.length; i++ ) {
      if ( samples[i].length() > 0 && samples[i].equalsIgnoreCase( nullString ) ) {
        datenul++;
      } else {
        for ( int x = 0; x < date_formats.length; x++ ) {
          if ( samples[i] == null || Const.onlySpaces( samples[i] ) || samples[i].length() == 0 ) {
            datefmt[x] = false;
            datefmt_cnt--;
          }

          if ( datefmt[x] ) {
            try {
              daf.applyPattern( date_formats[x] );
              Date date = daf.parse( samples[i] );

              Calendar cal = Calendar.getInstance();
              cal.setTime( date );
              int year = cal.get( Calendar.YEAR );

              if ( year < 1800 || year > 2200 ) {
                datefmt[x] = false; // Don't try it again in the future.
                datefmt_cnt--; // One less that works..
              }
            } catch ( Exception e ) {
              datefmt[x] = false; // Don't try it again in the future.
              datefmt_cnt--; // One less that works..
            }
          }
        }
      }
    }

    // If it is a date, copy info over to the format etc. Then return with the info.
    // If all samples where NULL values, we can't really decide what the type is.
    // So we're certainly not going to take a date, just take a string in that case.
    if ( datefmt_cnt > 0 && datenul != samples.length ) {
      int first = -1;
      for ( int i = 0; i < date_formats.length && first < 0; i++ ) {
        if ( datefmt[i] ) {
          first = i;
        }
      }

      type = ValueMetaInterface.TYPE_DATE;
      format = date_formats[first];

      return;
    }

    // ////////////////////////////
    // NUMBERS
    // ////////////////////////////

    boolean isnumber = true;

    // Set decimal symbols to default
    decimalSymbol = "" + dfs.getDecimalSeparator();
    groupSymbol = "" + dfs.getGroupingSeparator();

    boolean[] numfmt = new boolean[number_formats.length];
    int[] maxprecision = new int[number_formats.length];
    for ( int i = 0; i < numfmt.length; i++ ) {
      numfmt[i] = true;
      maxprecision[i] = -1;
    }
    int numfmt_cnt = number_formats.length;
    int numnul = 0;

    for ( int i = 0; i < samples.length && isnumber; i++ ) {
      boolean contains_dot = false;
      boolean contains_comma = false;

      String field = samples[i];

      if ( field.length() > 0 && field.equalsIgnoreCase( nullString ) ) {
        numnul++;
      } else {
        for ( int x = 0; x < field.length() && isnumber; x++ ) {
          char ch = field.charAt( x );
          if ( !Character.isDigit( ch )
            && ch != '.' && ch != ',' && ( ch != '-' || x > 0 ) && ch != 'E' && ch != 'e' // exponential
          ) {
            isnumber = false;
            numfmt_cnt = 0;
          } else {
            if ( ch == '.' ) {
              contains_dot = true;
              // containsDot = true;
            }
            if ( ch == ',' ) {
              contains_comma = true;
              // containsComma = true;
            }
          }
        }
        // If it's still a number, try to parse it as a double
        if ( isnumber ) {
          if ( contains_dot && !contains_comma ) { // American style 174.5

            dfs.setDecimalSeparator( '.' );
            decimalSymbol = ".";
            dfs.setGroupingSeparator( ',' );
            groupSymbol = ",";
          } else if ( !contains_dot && contains_comma ) { // European style 174,5

            dfs.setDecimalSeparator( ',' );
            decimalSymbol = ",";
            dfs.setGroupingSeparator( '.' );
            groupSymbol = ".";
          } else if ( contains_dot && contains_comma ) { // Both appear!

            // What's the last occurance: decimal point!
            int idx_dot = field.indexOf( '.' );
            int idx_com = field.indexOf( ',' );
            if ( idx_dot > idx_com ) {
              dfs.setDecimalSeparator( '.' );
              decimalSymbol = ".";
              dfs.setGroupingSeparator( ',' );
              groupSymbol = ",";
            } else {
              dfs.setDecimalSeparator( ',' );
              decimalSymbol = ",";
              dfs.setGroupingSeparator( '.' );
              groupSymbol = ".";
            }
          }

          // Try the remaining possible number formats!
          for ( int x = 0; x < number_formats.length; x++ ) {
            if ( numfmt[x] ) {
              boolean islong = true;

              try {
                int prec = -1;
                // Try long integers first....
                if ( !contains_dot && !contains_comma ) {
                  try {
                    Long.parseLong( field );
                    prec = 0;
                  } catch ( Exception e ) {
                    islong = false;
                  }
                }

                if ( !islong ) { // Try the double

                  df.setDecimalFormatSymbols( dfs );
                  df.applyPattern( number_formats[x] );

                  double d = df.parse( field ).doubleValue();
                  prec = guessPrecision( d );
                }
                if ( prec > maxprecision[x] ) {
                  maxprecision[x] = prec;
                }
              } catch ( Exception e ) {
                numfmt[x] = false; // Don't try it again in the future.
                numfmt_cnt--; // One less that works..
              }
            }
          }
        }
      }
    }

    // Still a number? Grab the result and return.
    // If all sample strings are empty or represent NULL values we can't take a number as type.
    if ( numfmt_cnt > 0 && numnul != samples.length ) {
      int first = -1;
      for ( int i = 0; i < number_formats.length && first < 0; i++ ) {
        if ( numfmt[i] ) {
          first = i;
        }
      }

      type = ValueMetaInterface.TYPE_NUMBER;
      format = number_formats[first];
      precision = maxprecision[first];

      // Wait a minute!!! What about Integers?
      // OK, only if the precision is 0 and the length <19 (java long integer)
      /*
       * if (length<19 && precision==0 && !containsDot && !containsComma) { type=ValueMetaInterface.TYPE_INTEGER;
       * decimalSymbol=""; groupSymbol=""; }
       */

      return;
    }

    //
    // Assume it's a string...
    //
    type = ValueMetaInterface.TYPE_STRING;
    format = "";
    precision = -1;
    decimalSymbol = "";
    groupSymbol = "";
    currencySymbol = "";
  }

  public static final int guessPrecision( double d ) {
    int maxprec = 4;
    double maxdiff = 0.00005;

    // Make sure that 7.99995 == 8.00000
    // This is usually a rounding error!
    double diff = Math.abs( Math.floor( d ) - d );
    if ( diff < maxdiff ) {
      return 0; // nothing behind decimal point...
    }

    // System.out.println("d="+d+", diff="+diff);

    // remainder: 12.345678 --> 0.345678
    for ( int i = 1; i < maxprec; i++ ) { // cap off precision at a reasonable maximum
      double factor = Math.pow( 10.0, i );
      diff = Math.abs( Math.floor( d * factor ) - ( d * factor ) );
      if ( diff < maxdiff ) {
        return i;
      }

      // System.out.println("d="+d+", diff="+diff+", factor="+factor);

      factor *= 10;
    }

    // Unknown length!
    return -1;
  }

  // Should a field be ignored?
  public void guessIgnore() {
    // If the string contains only spaces?
    boolean stop = false;
    for ( int i = 0; i < samples.length && !stop; i++ ) {
      if ( !Const.onlySpaces( samples[i] ) ) {
        stop = true;
      }
    }
    if ( !stop ) {
      ignore = true;
      return;
    }

    // If all the strings are empty
    stop = false;
    for ( int i = 0; i < samples.length && !stop; i++ ) {
      if ( samples[i].length() > 0 ) {
        stop = true;
      }
    }
    if ( !stop ) {
      ignore = true;
      return;
    }

    // If all the strings are equivalent to NULL
    stop = false;
    for ( int i = 0; i < samples.length && !stop; i++ ) {
      if ( !samples[i].equalsIgnoreCase( nullString ) ) {
        stop = true;
      }
    }
    if ( !stop ) {
      ignore = true;
      return;
    }
  }

  @Override
  public TextFileInputFieldInterface createNewInstance( String newFieldname, int x, int newlength ) {
    return new TextFileInputField( newFieldname, x, newlength );
  }
}
