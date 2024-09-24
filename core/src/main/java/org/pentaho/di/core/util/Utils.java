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

import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.List;

/* Levenshtein in Java, originally from Josh Drew's code at
 * http://joshdrew.com/
 * Code from http://blog.lolyco.com
 *
 */
public class Utils {
  private static final int[] ZERO_LENGTH_INT_ARRAY = new int[0];

  private static int damerauLevenshteinDistance( String s, String t, int[] workspace ) {
    int lenS = s.length();
    int lenT = t.length();
    int lenS1 = lenS + 1;
    int lenT1 = lenT + 1;
    if ( lenT1 == 1 ) {
      return lenS1 - 1;
    }
    if ( lenS1 == 1 ) {
      return lenT1 - 1;
    }
    int[] dl = workspace;
    int dlIndex = 0;
    int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0, min = 0, cost = 0, tmp = 0;
    int tri = lenS1 + 2;
    // start row with constant
    dlIndex = 0;
    for ( tmp = 0; tmp < lenT1; tmp++ ) {
      dl[dlIndex] = tmp;
      dlIndex += lenS1;
    }
    for ( int sIndex = 0; sIndex < lenS; sIndex++ ) {
      dlIndex = sIndex + 1;
      dl[dlIndex] = dlIndex; // start column with constant
      for ( int tIndex = 0; tIndex < lenT; tIndex++ ) {
        rowBefore = dlIndex;
        dlIndex += lenS1;
        // deletion
        min = dl[rowBefore] + 1;
        // insertion
        tmp = dl[dlIndex - 1] + 1;
        if ( tmp < min ) {
          min = tmp;
        }
        cost = 1;
        if ( s.charAt( sIndex ) == t.charAt( tIndex ) ) {
          cost = 0;
        }
        if ( sIndex > 0 && tIndex > 0 ) {
          if ( s.charAt( sIndex ) == t.charAt( tPrevIndex ) && s.charAt( sPrevIndex ) == t.charAt( tIndex ) ) {
            tmp = dl[rowBefore - tri] + cost;
            // transposition
            if ( tmp < min ) {
              min = tmp;
            }
          }
        }
        // substitution
        tmp = dl[rowBefore - 1] + cost;
        if ( tmp < min ) {
          min = tmp;
        }
        dl[dlIndex] = min;
        tPrevIndex = tIndex;
      }
      sPrevIndex = sIndex;
    }
    return dl[dlIndex];
  }

  private static int[] getWorkspace( int sl, int tl ) {
    return new int[( sl + 1 ) * ( tl + 1 )];
  }

  public static int getDamerauLevenshteinDistance( String s, String t ) {
    if ( s != null && t != null ) {
      return damerauLevenshteinDistance( s, t, getWorkspace( s.length(), t.length() ) );
    } else {
      return damerauLevenshteinDistance( s, t, ZERO_LENGTH_INT_ARRAY );
    }
  }

  /**
   * Check if the CharSequence supplied is empty. A CharSequence is empty when it is null or when the length is 0
   *
   * @param val
   *          The stringBuffer to check
   * @return true if the stringBuffer supplied is empty
   */
  public static boolean isEmpty( CharSequence val ) {
    return val == null || val.length() == 0;
  }

  /**
   * Check if the CharSequence array supplied is empty. A CharSequence array is empty when it is null or when the number of elements
   * is 0
   *
   * @param strings
   *          The string array to check
   * @return true if the string array supplied is empty
   */
  public static boolean isEmpty( CharSequence[] strings ) {
    return strings == null || strings.length == 0;
  }

  /**
   * Check if the array supplied is empty. An array is empty when it is null or when the length is 0
   *
   * @param array
   *          The array to check
   * @return true if the array supplied is empty
   */
  public static boolean isEmpty( Object[] array ) {
    return array == null || array.length == 0;
  }

  /**
   * Check if the list supplied is empty. An array is empty when it is null or when the length is 0
   *
   * @param list
   *          the list to check
   * @return true if the supplied list is empty
   */
  public static boolean isEmpty( List<?> list ) {
    return list == null || list.size() == 0;
  }

  /**
   * Resolves password from variable if it's necessary and decrypts if the password was encrypted
   *
   *
   * @param variables
   *          VariableSpace is used for resolving
   * @param password
   *          the password for resolving and decrypting
   * @return resolved decrypted password
   */
  public static String resolvePassword( VariableSpace variables, String password ) {
    String resolvedPassword = variables.environmentSubstitute( password );
    if ( resolvedPassword != null ) {
      // returns resolved decrypted password
      return Encr.decryptPasswordOptionallyEncrypted( resolvedPassword );
    } else {
      // actually null
      return resolvedPassword;
    }
  }

  /**
   * Normalize String array lengths for synchronization of arrays within steps
   */

  public static String[][] normalizeArrays( int normalizeToLength, String[]... arraysToNormalize ) {
    if ( arraysToNormalize == null ) {
      return null;
    }
    int arraysToProcess = arraysToNormalize.length;
    String[][] rtn = new String[ arraysToProcess ][];
    for ( int i = 0; i < arraysToNormalize.length; i++ ) {
      String[] nextArray = arraysToNormalize[ i ];
      if ( nextArray != null ) {
        if ( nextArray.length < normalizeToLength ) {
          String[] newArray = new String[ normalizeToLength ];
          System.arraycopy( nextArray, 0, newArray, 0, nextArray.length );
          rtn[ i ] = newArray;
        } else {
          rtn[ i ] = nextArray;
        }
      } else {
        rtn[ i ] = new String[ normalizeToLength ];
      }
    }
    return rtn;
  }

  /**
   * Normalize long array lengths for synchronization of arrays within steps
   */

  public static long[][] normalizeArrays( int normalizeToLength, long[]... arraysToNormalize ) {
    if ( arraysToNormalize == null ) {
      return null;
    }
    int arraysToProcess = arraysToNormalize.length;
    long[][] rtn = new long[ arraysToProcess ][];
    for ( int i = 0; i < arraysToNormalize.length; i++ ) {
      long[] nextArray = arraysToNormalize[ i ];
      if ( nextArray != null ) {
        if ( nextArray.length < normalizeToLength ) {
          long[] newArray = new long[ normalizeToLength ];
          System.arraycopy( nextArray, 0, newArray, 0, nextArray.length );
          rtn[ i ] = newArray;
        } else {
          rtn[ i ] = nextArray;
        }
      } else {
        rtn[ i ] = new long[ normalizeToLength ];
      }
    }
    return rtn;
  }

  /**
   * Normalize int array lengths for synchronization of arrays within steps
   */

  public static int[][] normalizeArrays( int normalizeToLength, int[]... arraysToNormalize ) {
    if ( arraysToNormalize == null ) {
      return null;
    }
    int arraysToProcess = arraysToNormalize.length;
    int[][] rtn = new int[ arraysToProcess ][];
    for ( int i = 0; i < arraysToNormalize.length; i++ ) {
      int[] nextArray = arraysToNormalize[ i ];
      if ( nextArray != null ) {
        if ( nextArray.length < normalizeToLength ) {
          int[] newArray = new int[ normalizeToLength ];
          System.arraycopy( nextArray, 0, newArray, 0, nextArray.length );
          rtn[ i ] = newArray;
        } else {
          rtn[ i ] = nextArray;
        }
      } else {
        rtn[ i ] = new int[ normalizeToLength ];
      }
    }
    return rtn;
  }

  /**
   * Normalize boolean array lengths for synchronization of arrays within steps
   */

  public static boolean[][] normalizeArrays( int normalizeToLength, boolean[]... arraysToNormalize ) {
    if ( arraysToNormalize == null ) {
      return null;
    }
    int arraysToProcess = arraysToNormalize.length;
    boolean[][] rtn = new boolean[ arraysToProcess ][];
    for ( int i = 0; i < arraysToNormalize.length; i++ ) {
      boolean[] nextArray = arraysToNormalize[ i ];
      if ( nextArray != null ) {
        if ( nextArray.length < normalizeToLength ) {
          boolean[] newArray = new boolean[ normalizeToLength ];
          System.arraycopy( nextArray, 0, newArray, 0, nextArray.length );
          rtn[ i ] = newArray;
        } else {
          rtn[ i ] = nextArray;
        }
      } else {
        rtn[ i ] = new boolean[ normalizeToLength ];
      }
    }
    return rtn;
  }

  /**
   * Normalize short array lengths for synchronization of arrays within steps
   */

  public static short[][] normalizeArrays( int normalizeToLength, short[]... arraysToNormalize ) {
    if ( arraysToNormalize == null ) {
      return null;
    }
    int arraysToProcess = arraysToNormalize.length;
    short[][] rtn = new short[ arraysToProcess ][];
    for ( int i = 0; i < arraysToNormalize.length; i++ ) {
      short[] nextArray = arraysToNormalize[ i ];
      if ( nextArray != null ) {
        if ( nextArray.length < normalizeToLength ) {
          short[] newArray = new short[ normalizeToLength ];
          System.arraycopy( nextArray, 0, newArray, 0, nextArray.length );
          rtn[ i ] = newArray;
        } else {
          rtn[ i ] = nextArray;
        }
      } else {
        rtn[ i ] = new short[ normalizeToLength ];
      }
    }
    return rtn;
  }

}
