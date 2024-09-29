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

package org.pentaho.di.trans.steps.fuzzymatch;

import java.util.ArrayList;

import org.pentaho.di.core.util.Utils;

public class LetterPairSimilarity {

  /*
   * This class is being used by permission of Simon White The source is directly from a catalysoft.com article
   * http://www.catalysoft.com/articles/StrikeAMatch.html
   *
   *
   * >> I'm happy for you to reuse parts of my articles provided that you give>> suitable attribution (preferably a link
   * back to the original article).>>>> Best Regards,>> Simon
   *
   * > Will it be ok with you for us to use it in an open source project> offered under the Apache Software License,
   * provided I give you both> attribution, and a link back to the original article?> Marc
   *
   * Hi Marc, Yes, that's fine.
   *
   * Best Regards, Simon
   */

  /** @return an array of adjacent letter pairs contained in the input string */

  private static String[] letterPairs( String str ) {
    int numPairs = str.length() - 1;
    if ( str.length() == 0 ) {
      numPairs = 0;
    }
    String[] pairs = new String[numPairs];
    for ( int i = 0; i < numPairs; i++ ) {
      pairs[i] = str.substring( i, i + 2 );
    }
    return pairs;
  }

  /** @return an ArrayList of 2-character Strings. */

  private static ArrayList<String> wordLetterPairs( String str ) {
    ArrayList<String> allPairs = new ArrayList<String>();
    // Tokenize the string and put the tokens/words into an array
    String[] words = str.split( "\\s" );
    // For each word
    for ( int w = 0; w < words.length; w++ ) {
      // Find the pairs of characters
      String[] pairsInWord = letterPairs( words[w] );
      for ( int p = 0; p < pairsInWord.length; p++ ) {
        allPairs.add( pairsInWord[p] );
      }
    }
    return allPairs;
  }

  /** @return lexical similarity value in the range [0,1] */

  public static double getSimiliarity( String str1, String str2 ) {
    if ( Utils.isEmpty( str1 ) && Utils.isEmpty( str2 ) ) {
      return new Double( 1 );
    }
    ArrayList<String> pairs1 = wordLetterPairs( str1.toUpperCase() );
    ArrayList<String> pairs2 = wordLetterPairs( str2.toUpperCase() );
    int intersection = 0;
    int union = pairs1.size() + pairs2.size();

    for ( int i = 0; i < pairs1.size(); i++ ) {
      Object pair1 = pairs1.get( i );
      for ( int j = 0; j < pairs2.size(); j++ ) {
        Object pair2 = pairs2.get( j );
        if ( pair1.equals( pair2 ) ) {
          intersection++;
          pairs2.remove( j );
          break;
        }
      }
    }
    return ( 2.0 * intersection ) / union;
  }
}
