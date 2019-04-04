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
package org.pentaho.di.core.util;

import java.util.Arrays;

/**
 * A similarity algorithm indicating the percentage of matched characters between two character sequences.
 *
 * <p>
 * The Jaro measure is the weighted sum of percentage of matched characters
 * from each file and transposed characters. Winkler increased this measure
 * for matching initial characters.
 * </p>
 *
 * <p>
 * This implementation is based on the Jaro Winkler similarity algorithm
 * from <a href="http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">
 * http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>.
 * </p>
 *
 * <p>
 * This code has been adapted from Apache Commons Lang 3.3.
 * </p>
 *
 * @since 1.0
 */
public class PentahoJaroWinklerDistance {

  /**
   * Represents a failed index search.
   */
  public static final int INDEX_NOT_FOUND = -1;
  private double j = 0D;
  private double jw = 0D;

  public Double getJaroDistance() {
    return new Double( j );
  }

  public Double getJaroWinklerDistance() {
    return new  Double( jw );
  }

  /**
   * Find the Jaro Winkler Distance which indicates the similarity score
   * between two CharSequences.
   *
   * <pre>
   * distance.apply(null, null)          = IllegalArgumentException
   * distance.apply("","")               = 0.0
   * distance.apply("","a")              = 0.0
   * distance.apply("aaapppp", "")       = 0.0
   * distance.apply("frog", "fog")       = 0.93
   * distance.apply("fly", "ant")        = 0.0
   * distance.apply("elephant", "hippo") = 0.44
   * distance.apply("hippo", "elephant") = 0.44
   * distance.apply("hippo", "zzzzzzzz") = 0.0
   * distance.apply("hello", "hallo")    = 0.88
   * distance.apply("ABC Corporation", "ABC Corp") = 0.93
   * distance.apply("D N H Enterprises Inc", "D &amp; H Enterprises, Inc.") = 0.95
   * distance.apply("My Gym Children's Fitness Center", "My Gym. Childrens Fitness") = 0.92
   * distance.apply("PENNSYLVANIA", "PENNCISYLVNIA")    = 0.88
   * </pre>
   *
   * @param left the first String, must not be null
   * @param right the second String, must not be null
   * @return result distance
   * @throws IllegalArgumentException if either String input {@code null}
   */
  public void apply( final CharSequence left, final CharSequence right ) {
    final double defaultScalingFactor = 0.1;

    if ( left == null || right == null ) {
      throw new IllegalArgumentException( "Strings must not be null" );
    }

    final int[] mtp = matches( left, right );
    final double m = mtp[0];
    if ( m == 0 ) {
      j = 0D;
      jw = 0D;
    } else {
      j = ( ( m / left.length() + m / right.length() + ( m - mtp[1] ) / m ) ) / 3;
      jw = j < 0.7D ? j : j + Math.min( defaultScalingFactor, 1D / mtp[3] ) * mtp[2] * ( 1D - j );
    }
  }

  /**
   * This method returns the Jaro-Winkler string matches, transpositions, prefix, max array.
   *
   * @param first the first string to be matched
   * @param second the second string to be matched
   * @return mtp array containing: matches, transpositions, prefix, and max length
   */
  protected static int[] matches( final CharSequence first, final CharSequence second ) {
    CharSequence max, min;
    if ( first.length() > second.length() ) {
      max = first;
      min = second;
    } else {
      max = second;
      min = first;
    }
    final int range = Math.max( max.length() / 2 - 1, 0 );
    final int[] matchIndexes = new int[min.length()];
    Arrays.fill( matchIndexes, -1 );
    final boolean[] matchFlags = new boolean[max.length()];
    int matches = 0;
    for ( int mi = 0; mi < min.length(); mi++ ) {
      final char c1 = min.charAt( mi );
      for ( int xi = Math.max( mi - range, 0 ), xn = Math.min( mi + range + 1, max.length() ); xi < xn; xi++ ) {
        if ( !matchFlags[xi] && c1 == max.charAt( xi ) ) {
          matchIndexes[mi] = xi;
          matchFlags[xi] = true;
          matches++;
          break;
        }
      }
    }
    final char[] ms1 = new char[matches];
    final char[] ms2 = new char[matches];
    for ( int i = 0, si = 0; i < min.length(); i++ ) {
      if ( matchIndexes[i] != -1 ) {
        ms1[si] = min.charAt( i );
        si++;
      }
    }
    for ( int i = 0, si = 0; i < max.length(); i++ ) {
      if ( matchFlags[i] ) {
        ms2[si] = max.charAt( i );
        si++;
      }
    }
    int transpositions = 0;
    for ( int mi = 0; mi < ms1.length; mi++ ) {
      if ( ms1[mi] != ms2[mi] ) {
        transpositions++;
      }
    }
    int prefix = 0;
    for ( int mi = 0; mi < min.length(); mi++ ) {
      if ( first.charAt( mi ) == second.charAt( mi ) ) {
        prefix++;
      } else {
        break;
      }
    }
    return new int[] { matches, transpositions / 2, prefix, max.length() };
  }

  public void reset() {
    j = 0D;
    jw = 0D;
  }
}
