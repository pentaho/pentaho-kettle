/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 * This file is dual-licensed under the Apache Software License V2 and is
 * also available under the terms of the GNU Lesser GPL version 2.1 as provided
 * for by the author of the algorithm below (Dr. Graham King).
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

/******************************************************************************
 * Copyright (c) 2006 Graham King and 2007 Hitachi Vantara.  All rights reserved.
 * This software was developed by Hitachi Vantara and is provided under the terms
 * of the GNU Lesser General Public License, Version 2.1. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho
 * Data Integration.  The Initial Developer is samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 ******************************************************************************/

package org.pentaho.di.trans.steps.randomccnumber;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;

public class RandomCreditCardNumberGenerator {
  /*
   * 2006 Graham King graham@darkcoding.net
   *
   * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
   * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
   * later version.
   *
   * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
   * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
   * details.
   *
   * You should have received a copy of the GNU General Public License along with this program; if not, write to the
   * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
   *
   * www.darkcoding.net
   *
   * From : Graham King <graham (at) gkgk dot org> To : samatar hassan <sahass78 (at) yahoo dot fr> Envoye le : Mer 8
   * decembre 2010, 22h 30min 55s Objet : Re: CreditCardNumberGenerator LGPL grant - Hitachi Vantara data Integration Hi
   * Samatar, Thanks for getting in touch. Yes, I am happy to grant Pentaho Data Integration an LGPL exception, meaning
   * that solely for inclusion in Pentaho Data Integration, the credit card code hosted at darkcoding.net can be
   * considered LGPL licensed. All the best, Graham
   */
  private static Class<?> PKG = RandomCCNumberGeneratorMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int CARD_TYPE_AMEX = 0;
  public static final int CARD_TYPE_DINERS = 1;
  public static final int CARD_TYPE_DISCOVER = 2;
  public static final int CARD_TYPE_ENROUTE = 3;
  public static final int CARD_TYPE_JCB_15 = 4;
  public static final int CARD_TYPE_JCB_16 = 5;
  public static final int CARD_TYPE_MASTERCARD = 6;
  public static final int CARD_TYPE_VISA = 7;
  public static final int CARD_TYPE_VOYAGER = 8;
  public static final int CARD_TYPE_AIRPLUS = 9;
  public static final int CARD_TYPE_BANKCARD = 10;
  public static final int CARD_TYPE_MAESTRO = 11;
  public static final int CARD_TYPE_SOLO = 12;
  public static final int CARD_TYPE_SWITCH = 13;
  public static final int CARD_TYPE_LASER = 14;

  public static final String[] cardTypes = {
    "American Express", "Diners", "Discover", "En Route", "JCB1", "JCB2", "MasterCard", "Visa", "Voyager",
    "Airplus", "BankCard", "Maestro", "Solo", "Switch", "Laser" };

  private static final String[] VISA_PREFIX_LIST = new String[] {
    "4539", "4556", "4916", "4532", "4929", "40240071", "4485", "4716", "4" };
  private static final String[] MASTERCARD_PREFIX_LIST = new String[] { "51", "52", "53", "54", "55" };
  private static final String[] AMEX_PREFIX_LIST = new String[] { "34", "37" };
  private static final String[] DISCOVER_PREFIX_LIST = new String[] { "6011" };
  private static final String[] DINERS_PREFIX_LIST = new String[] { "300", "301", "302", "303", "36", "38" };
  private static final String[] ENROUTE_PREFIX_LIST = new String[] { "2014", "2149" };
  private static final String[] JCB_15_PREFIX_LIST = new String[] { "2100", "1800" };
  private static final String[] JCB_16_PREFIX_LIST =
    new String[] { "3088", "3096", "3112", "3158", "3337", "3528" };
  private static final String[] VOYAGER_PREFIX_LIST = new String[] { "8699" };
  private static final String[] AIRPLUS_PREFIX_LIST = new String[] { "192", "122" };
  private static final String[] BANKCARD_PREFIX_LIST = new String[] { "56" };
  private static final String[] MAESTRO_PREFIX_LIST = new String[] { "5020", "6" };
  private static final String[] SOLO_PREFIX_LIST = new String[] { "6334", "6767" };
  private static final String[] SWITCH_PREFIX_LIST = new String[] {
    "4903", "4905", "4911", "4936", "564182", "633110", "6333", "6759" };
  private static final String[] LASER_PREFIX_LIST = new String[] { "6304", "6706", "6771", "6709" };

  private static final int[] VISA_LENGTH_LIST = new int[] { 13, 16 };
  private static final int[] MASTERCARD_LENGTH_LIST = new int[] { 16 };
  private static final int[] AMEX_LENGTH_LIST = new int[] { 15 };
  private static final int[] DISCOVER_LENGTH_LIST = new int[] { 16 };
  private static final int[] DINERS_LENGTH_LIST = new int[] { 14 };
  private static final int[] ENROUTE_LENGTH_LIST = new int[] { 15 };
  private static final int[] JCB_15_LENGTH_LIST = new int[] { 15 };
  private static final int[] JCB_16_LENGTH_LIST = new int[] { 16 };
  private static final int[] VOYAGER_LENGTH_LIST = new int[] { 15 };
  private static final int[] AIRPLUS_LENGTH_LIST = new int[] {};
  private static final int[] BANKCARD_LENGTH_LIST = new int[] { 16 };
  private static final int[] MAESTRO_LENGTH_LIST = new int[] { 16 };
  private static final int[] SOLO_LENGTH_LIST = new int[] { 16, 18, 19 };
  private static final int[] SWITCH_LENGTH_LIST = new int[] { 16, 18, 19 };
  private static final int[] LASER_LENGTH_LIST = new int[] { 16, 17, 18, 19 };

  public static int getCardType( String typeName ) {
    if ( typeName == null ) {
      return 0;
    }

    for ( int i = 0; i < cardTypes.length; i++ ) {
      if ( cardTypes[i].equalsIgnoreCase( typeName ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getCardName( int id ) {
    return ( id > -1 && id < cardTypes.length ? cardTypes[id] : null );
  }

  private static String strrev( String str ) {
    if ( str == null ) {
      return "";
    }
    String revstr = "";
    for ( int i = str.length() - 1; i >= 0; i-- ) {
      revstr += str.charAt( i );
    }

    return revstr;
  }

  /*
   * 'prefix' is the start of the CC number as a string, any number of digits. 'length' is the length of the CC number
   * to generate. Typically 13 or 16
   */
  private static String completed_number( String prefix, int length ) {

    String ccnumber = prefix;

    // generate digits

    while ( ccnumber.length() < ( length - 1 ) ) {
      ccnumber += new Double( Math.floor( Math.random() * 10 ) ).intValue();
    }

    // reverse number and convert to int

    String reversedCCnumberString = strrev( ccnumber );

    List<Integer> reversedCCnumberList = new Vector<Integer>();
    for ( int i = 0; i < reversedCCnumberString.length(); i++ ) {
      reversedCCnumberList.add( new Integer( String.valueOf( reversedCCnumberString.charAt( i ) ) ) );
    }

    // calculate sum

    int sum = 0;
    int pos = 0;

    Integer[] reversedCCnumber = reversedCCnumberList.toArray( new Integer[reversedCCnumberList.size()] );
    while ( pos < length - 1 ) {

      int odd = reversedCCnumber[pos] * 2;
      if ( odd > 9 ) {
        odd -= 9;
      }

      sum += odd;

      if ( pos != ( length - 2 ) ) {
        sum += reversedCCnumber[pos + 1];
      }
      pos += 2;
    }

    // calculate check digit

    int checkdigit = new Double( ( ( Math.floor( sum / 10 ) + 1 ) * 10 - sum ) % 10 ).intValue();
    ccnumber += checkdigit;

    return ccnumber;

  }

  private static String[] credit_card_number( String[] prefixList, int length, int howMany ) {

    Stack<String> result = new Stack<String>();
    for ( int i = 0; i < howMany; i++ ) {
      int randomArrayIndex = (int) Math.floor( Math.random() * prefixList.length );
      String ccnumber = prefixList[randomArrayIndex];
      result.push( completed_number( ccnumber, length ) );
    }

    return result.toArray( new String[result.size()] );
  }

  private static void checkLength( int cardType, int size, int[] lengths ) throws KettleException {
    if ( lengths.length == 0 ) {
      return;
    }
    for ( int i = 0; i < lengths.length; i++ ) {
      if ( size == lengths[i] ) {
        // The size is supported for the card type
        return;
      }
    }
    throw new KettleException( BaseMessages
      .getString(
        PKG, "RandomCreditCardNumbberGenerator.UnSupportedLength", String.valueOf( size ),
        getCardName( cardType ) ) );
  }

  public static String[] GenerateCreditCardNumbers( int cardType, int size, int howMany ) throws KettleException {
    String[] cards = null;
    switch ( cardType ) {
      case CARD_TYPE_MASTERCARD:
        checkLength( cardType, size, MASTERCARD_LENGTH_LIST );
        cards = credit_card_number( MASTERCARD_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_AMEX:
        checkLength( cardType, size, AMEX_LENGTH_LIST );
        cards = credit_card_number( AMEX_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_DINERS:
        checkLength( cardType, size, DINERS_LENGTH_LIST );
        cards = credit_card_number( DINERS_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_DISCOVER:
        checkLength( cardType, size, DISCOVER_LENGTH_LIST );
        cards = credit_card_number( DISCOVER_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_ENROUTE:
        checkLength( cardType, size, ENROUTE_LENGTH_LIST );
        cards = credit_card_number( ENROUTE_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_JCB_15:
        checkLength( cardType, size, JCB_15_LENGTH_LIST );
        cards = credit_card_number( JCB_15_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_JCB_16:
        checkLength( cardType, size, JCB_16_LENGTH_LIST );
        cards = credit_card_number( JCB_16_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_VISA:
        checkLength( cardType, size, VISA_LENGTH_LIST );
        cards = credit_card_number( VISA_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_VOYAGER:
        checkLength( cardType, size, VOYAGER_LENGTH_LIST );
        cards = credit_card_number( VOYAGER_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_AIRPLUS:
        checkLength( cardType, size, AIRPLUS_LENGTH_LIST );
        cards = credit_card_number( AIRPLUS_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_BANKCARD:
        checkLength( cardType, size, BANKCARD_LENGTH_LIST );
        cards = credit_card_number( BANKCARD_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_MAESTRO:
        checkLength( cardType, size, MAESTRO_LENGTH_LIST );
        cards = credit_card_number( MAESTRO_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_SOLO:
        checkLength( cardType, size, SOLO_LENGTH_LIST );
        cards = credit_card_number( SOLO_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_SWITCH:
        checkLength( cardType, size, SWITCH_LENGTH_LIST );
        cards = credit_card_number( SWITCH_PREFIX_LIST, size, howMany );
        break;
      case CARD_TYPE_LASER:
        checkLength( cardType, size, LASER_LENGTH_LIST );
        cards = credit_card_number( LASER_PREFIX_LIST, size, howMany );
        break;
      default:
        throw new KettleException( BaseMessages.getString(
          PKG, "RandomCreditCardNumbberGenerator.UnknownCardtype", String.valueOf( cardType ) ) );
    }
    return cards;
  }

}
