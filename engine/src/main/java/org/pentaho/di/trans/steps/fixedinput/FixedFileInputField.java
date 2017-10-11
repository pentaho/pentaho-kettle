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

package org.pentaho.di.trans.steps.fixedinput;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;

public class FixedFileInputField implements Cloneable, XMLInterface {

  public static final String XML_TAG = "field";

  private static final String[] date_formats = new String[] {
    "yyyy/MM/dd HH:mm:ss.SSS", "yyyy/MM/dd HH:mm:ss", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd", "yyyy-MM-dd",
    "yyyyMMdd", "ddMMyyyy", "d-M-yyyy", "d/M/yyyy", "d-M-yy", "d/M/yy", };

  private static final String[] number_formats = new String[] {
    "", "#", Const.DEFAULT_NUMBER_FORMAT, "0.00", "0000000000000", "###,###,###.#######",
    "###############.###############", "#####.###############%", };

  private String name;

  private int type;

  private int width;

  private int length;

  private int precision;

  private String format;

  private int trimType;

  private String decimal;

  private String grouping;

  private String currency;

  private String[] samples;

  public FixedFileInputField( Node fnode ) {
    name = XMLHandler.getTagValue( fnode, "name" );
    type = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) );
    format = XMLHandler.getTagValue( fnode, "format" );
    trimType = ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) );
    currency = XMLHandler.getTagValue( fnode, "currency" );
    decimal = XMLHandler.getTagValue( fnode, "decimal" );
    grouping = XMLHandler.getTagValue( fnode, "group" );
    width = Const.toInt( XMLHandler.getTagValue( fnode, "width" ), -1 );
    length = Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 );
    precision = Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 );

  }

  public FixedFileInputField() {
    type = ValueMetaInterface.TYPE_STRING;
    length = -1;
    precision = -1;
    trimType = ValueMetaInterface.TRIM_TYPE_NONE;
  }

  @Override
  public boolean equals( Object obj ) {
    return name.equalsIgnoreCase( ( (FixedFileInputField) obj ).name );
  }

  @Override
  public int hashCode() {
    return Objects.hashCode( name );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "      " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    retval.append( "        " ).append( XMLHandler.addTagValue( "name", name ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "type",
      ValueMetaFactory.getValueMetaName( type ) ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "format", format ) );
    retval.append( "        " ).append(
      XMLHandler.addTagValue( "trim_type", ValueMetaString.getTrimTypeCode( trimType ) ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "currency", currency ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", decimal ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "group", grouping ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "width", width ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "length", length ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "precision", precision ) );
    retval.append( "      " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return retval.toString();
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the type
   */
  public int getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType( int type ) {
    this.type = type;
  }

  /**
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * @param width
   *          the width to set
   */
  public void setWidth( int width ) {
    this.width = width;
  }

  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * @param length
   *          the length to set
   */
  public void setLength( int length ) {
    this.length = length;
  }

  /**
   * @return the precision
   */
  public int getPrecision() {
    return precision;
  }

  /**
   * @param precision
   *          the precision to set
   */
  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  /**
   * @return the format
   */
  public String getFormat() {
    return format;
  }

  /**
   * @param format
   *          the format to set
   */
  public void setFormat( String format ) {
    this.format = format;
  }

  /**
   * @return the decimal
   */
  public String getDecimal() {
    return decimal;
  }

  /**
   * @param decimal
   *          the decimal to set
   */
  public void setDecimal( String decimal ) {
    this.decimal = decimal;
  }

  /**
   * @return the grouping
   */
  public String getGrouping() {
    return grouping;
  }

  /**
   * @param grouping
   *          the grouping to set
   */
  public void setGrouping( String grouping ) {
    this.grouping = grouping;
  }

  /**
   * @return the currency
   */
  public String getCurrency() {
    return currency;
  }

  /**
   * @param currency
   *          the currency to set
   */
  public void setCurrency( String currency ) {
    this.currency = currency;
  }

  public void setSamples( String[] samples ) {
    this.samples = samples;

  }

  /**
   * @return the samples
   */
  public String[] getSamples() {
    return samples;
  }

  public void guess() {
    guessType();
  }

  public void guessType() {
    NumberFormat nf = NumberFormat.getInstance();
    DecimalFormat df = (DecimalFormat) nf;
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    SimpleDateFormat daf = new SimpleDateFormat();

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
    decimal = "" + dfs.getDecimalSeparator();
    grouping = "" + dfs.getGroupingSeparator();

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
        if ( contains_dot && !contains_comma ) {
          // American style 174.5

          dfs.setDecimalSeparator( '.' );
          decimal = ".";
          dfs.setGroupingSeparator( ',' );
          grouping = ",";
        } else if ( !contains_dot && contains_comma ) {
          // European style 174,5

          dfs.setDecimalSeparator( ',' );
          decimal = ",";
          dfs.setGroupingSeparator( '.' );
          grouping = ".";
        } else if ( contains_dot && contains_comma ) {
          // Both appear!

          // What's the last occurance: decimal point!
          int idx_dot = field.indexOf( '.' );
          int idx_com = field.indexOf( ',' );
          if ( idx_dot > idx_com ) {
            dfs.setDecimalSeparator( '.' );
            decimal = ".";
            dfs.setGroupingSeparator( ',' );
            grouping = ",";
          } else {
            dfs.setDecimalSeparator( ',' );
            decimal = ",";
            dfs.setGroupingSeparator( '.' );
            grouping = ".";
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

              if ( !islong ) {
                // Try the double

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
    decimal = "";
    grouping = "";
    currency = "";
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

  public int getTrimType() {
    return trimType;
  }

  public void setTrimType( int trimType ) {
    this.trimType = trimType;
  }
}
