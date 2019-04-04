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

package org.pentaho.di.trans.steps.creditcardvalidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class CreditCardVerifier {
  private static Class<?> PKG = CreditCardValidatorMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int INVALID = -1;
  public static final int VISA = 0;
  public static final int MASTERCARD = 1;
  public static final int AMERICAN_EXPRESS = 2;
  public static final int EN_ROUTE = 3;
  public static final int DINERS_CLUB = 4;
  public static final int DISCOVER = 5;
  public static final int JCB1 = 6;
  public static final int JCB2 = 7;
  public static final int BANKCARD = 8;
  public static final int MAESTRO = 9;
  public static final int SOLO = 10;
  public static final int SWITCH = 11;
  public static final int AIRPLUS = 12;
  public static final int LASER = 13;
  public static final int VOYAGER = 14;

  private static final String[] cardNames = {
    "Visa", "Mastercard", "American Express", "En Route", "Diner's CLub/Carte Blanche", "Discover", "JCB1",
    "JCB2", "BankCard", "Maestro", "Solo", "Switch", "Airplus", "Laser", "Voyager" };
  private static final String[] NotValidCardNames = {
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidVisa" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidMastercard" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidAmericanExpress" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidEnRoute" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidDiners" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidDiscover" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidJcb1" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidJcb2" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidBankCard" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidMaestro" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidSolo" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidSwitch" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidAirplus" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidLaser" ),
    BaseMessages.getString( PKG, "CreditCardValidator.Log.NotValidVoyager" ) };

  public static String getCardName( int id ) {
    return ( id > -1 && id < cardNames.length ? cardNames[id] : null );
  }

  public static String getNotValidCardNames( int id ) {
    return ( id > -1 && id < NotValidCardNames.length ? NotValidCardNames[id] : null );
  }

  public static ReturnIndicator CheckCC( String CardNumber ) {
    ReturnIndicator ri = new ReturnIndicator();

    if ( Utils.isEmpty( CardNumber ) ) {
      ri.UnValidMsg = BaseMessages.getString( PKG, "CreditCardValidator.Log.EmptyNumber" );
      return ri;
    }

    Matcher m = Pattern.compile( "[^\\d\\s.-]" ).matcher( CardNumber );
    if ( m.find() ) {
      ri.UnValidMsg = BaseMessages.getString( PKG, "CreditCardValidator.OnlyNumbers" );
      return ri;
    }

    int cardId = getCardID( CardNumber );
    if ( cardId > -1 ) {
      if ( luhnValidate( CardNumber ) ) {
        ri.CardValid = true;
        ri.CardType = getCardName( cardId );
      } else {
        ri.CardValid = false;
        ri.UnValidMsg = getNotValidCardNames( cardId );
      }
    } else {
      // try luhn
      // ri.UnValidMsg="This card is unsupported!";
      if ( luhnValidate( CardNumber ) ) {
        ri.CardValid = true;
      } else {
        ri.UnValidMsg = BaseMessages.getString( PKG, "CreditCardValidator.Log.CardNotValid" );
      }
    }

    return ri;
  }

  // The Luhn algorithm is basically a CRC type
  // system for checking the validity of an entry.
  // All major credit cards use numbers that will
  // pass the Luhn check. Also, all of them are based
  // on MOD 10.

  public static boolean luhnValidate( String numberString ) {
    try {

      int j = numberString.length();

      String[] s1 = new String[j];
      for ( int i = 0; i < j; i++ ) {
        s1[i] = "" + numberString.charAt( i );
      }

      int checksum = 0;

      for ( int i = s1.length - 1; i >= 0; i -= 2 ) {
        int k = 0;

        if ( i > 0 ) {
          k = Integer.valueOf( s1[i - 1] ).intValue() * 2;
          if ( k > 9 ) {
            String s = "" + k;
            k = Integer.valueOf( s.substring( 0, 1 ) ).intValue() + Integer.valueOf( s.substring( 1 ) ).intValue();
          }
          checksum += Integer.valueOf( s1[i] ).intValue() + k;
        } else {
          checksum += Integer.valueOf( s1[0] ).intValue();
        }
      }
      return ( ( checksum % 10 ) == 0 );
    } catch ( Exception e ) {
      // e.printStackTrace();
      return false;
    }
  }

  public static int getCardID( String number ) {
    int valid = INVALID;

    String digit1 = number.substring( 0, 1 );
    String digit2 = number.substring( 0, 2 );
    String digit3 = number.substring( 0, 3 );
    String digit4 = number.substring( 0, 4 );

    if ( isNumber( number ) ) {
      if ( digit4.equals( "4903" )
        || digit4.equals( "4905" ) || digit4.equals( "4911" ) || digit4.equals( "4936" )
        || digit4.equals( "564182" ) || digit4.equals( "633110" ) || digit4.equals( "6333" )
        || digit4.equals( "6759" ) ) {
        if ( number.length() == 16 || number.length() == 18 || number.length() == 19 ) {

          /*
           * ----* SWITCH card prefix = 4903,4905,4911,4936,564182,633110,6333,6759* -------- length = 16,18,19
           */

          valid = SWITCH;
        }
      } else if ( digit4.equals( "6304" )
        || digit4.equals( "6706" ) || digit4.equals( "6771" ) || digit4.equals( "6709" ) ) {
        if ( number.length() >= 16 && number.length() <= 19 ) {

          /*
           * ----* LASER card prefix = 6304, 6706, 6771, 6709* --------
           */

          valid = LASER;
        }
      } else if ( digit1.equals( "4" ) ) {
        if ( number.length() == 13 || number.length() == 16 ) {

          /*
           * ----* VISA prefix=4* ---- length=13 or 16 (can be 15 too!?! maybe)
           */

          valid = VISA;
        }
      } else if ( digit2.compareTo( "51" ) >= 0 && digit2.compareTo( "55" ) <= 0 ) {
        if ( number.length() == 16 ) {
          valid = MASTERCARD;
        }
      } else if ( digit2.equals( "34" ) || digit2.equals( "37" ) ) {
        if ( number.length() == 15 ) {

          /*
           * ----* AMEX prefix=34 or 37* ---- length=15
           */

          valid = AMERICAN_EXPRESS;
        }
      } else if ( digit4.equals( "2014" ) || digit4.equals( "2149" ) ) {
        if ( number.length() == 15 ) {

          /*
           * -----* ENROUTE prefix=2014 or 2149* ----- length=15
           */

          valid = EN_ROUTE;
        }
      } else if ( digit2.equals( "36" )
        || digit2.equals( "38" ) || ( digit3.compareTo( "300" ) >= 0 && digit3.compareTo( "305" ) <= 0 ) ) {
        if ( number.length() == 14 ) {

          /*
           * -----* DCLUB prefix=300 ... 305 or 36 or 38* ----- length=14
           */

          valid = DINERS_CLUB;
        }
      } else if ( digit4.equals( "6011" ) ) {
        if ( number.length() == 16 ) {

          /*
           * ----* DISCOVER card prefix = 6011* -------- length = 16
           */

          valid = DISCOVER;
        }
      } else if ( digit1.equals( "3" ) ) {
        if ( number.length() == 16 ) {

          /*
           * ----* JCB1 card prefix = 3* -------- length = 16
           */

          valid = JCB1;
        }
      } else if ( digit4.equals( "2131" ) || digit4.equals( "1800" ) ) {
        if ( number.length() == 15 ) {

          /*
           * ----* JCB2 card prefix = 2131, 1800* -------- length = 15
           */

          valid = JCB2;
        }
      } else if ( digit4.equals( "5610" )
        || digit4.equals( "560221" ) || digit4.equals( "560222" ) || digit4.equals( "560223" )
        || digit4.equals( "560224" ) || digit4.equals( "560225" ) ) {
        if ( number.length() == 16 ) {

          /*
           * ----* BANKCARD card prefix = 56* -------- length = 16
           */

          valid = BANKCARD;
        }
      } else if ( digit4.equals( "5018" )
        || digit4.equals( "5020" ) || digit4.equals( "5038" ) || digit4.equals( "6304" )
        || digit4.equals( "6759" ) || digit4.equals( "6761" ) || digit4.equals( "6763" ) ) {
        if ( number.length() == 12 || number.length() == 13 || number.length() >= 14 && number.length() <= 19 ) {

          /*
           * ----* MAESTRO card prefix = 5020,6* -------- length = 16
           */

          valid = MAESTRO;
        }
      } else if ( digit4.equals( "6334" ) || digit4.equals( "6767" ) ) {
        if ( number.length() == 16 || number.length() == 18 || number.length() == 19 ) {

          /*
           * ----* SOLO card prefix = 6334, 6767* -------- length = 16,18,19
           */

          valid = SOLO;
        }
      } else if ( digit3.equals( "192" ) || digit3.equals( "122" ) ) {

        /*
         * ----* AIRLUS card prefix = 192, 122* --------
         */

        valid = AIRPLUS;
      } else if ( digit4.equals( "8699" ) ) {
        if ( number.length() == 15 ) {

          /*
           * ----* VOYAGER card prefix = 6011* -------- length = 15
           */

          valid = VOYAGER;
        }
      }
    }

    return valid;
  }

  public static boolean isNumber( String n ) {
    try {
      Double.valueOf( n ).doubleValue();
      return true;
    } catch ( NumberFormatException e ) { // ignore this exception
      return false;
    }
  }
}
