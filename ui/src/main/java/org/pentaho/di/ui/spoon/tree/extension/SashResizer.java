/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.tree.extension;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles simplified resizing weights for a SashForm, storing last known sizes and normalizing them to 100.
 */
public class SashResizer {
  private static Logger log = LoggerFactory.getLogger( SashResizer.class );

  private static final int NORM_TARGET = 100;
  private static final int DEFAULT_WEIGHT = 30;

  /** list of last non-zero percentage sizes recorded for elements (can add up to more than 100) */
  private int[] weights;
  private boolean[] enabled;

  /**
   * @param iniFormWeights
   *          initial weights (may not be normalized)
   */
  public SashResizer( int[] iniFormWeights ) {
    this.weights = normalize( iniFormWeights );
    this.enabled = initEnabledFromWeights( this.weights );
  }

  public int[] enable( int pos, int[] formWeights ) {
    updateLastWeights( normalize( formWeights ) );
    enabled[pos] = true;
    return normalizeFor( enabledWeights(), pos );
  }

  public int[] disable( int pos, int[] formWeights ) {
    updateLastWeights( normalize( formWeights ) );
    enabled[pos] = false;
    return normalize( enabledWeights() );
  }

  public int[] weights() {
    return weights;
  }

  /** returns weight with disabled ones zeroed out */
  private int[] enabledWeights() {
    int[] res = new int[weights.length];
    for ( int i = 0; i < weights.length; i++ ) {
      if ( enabled[i] ) {
        res[i] = weights[i];
      }
    }
    return res;
  }

  private static boolean[] initEnabledFromWeights( int[] weights ) {
    boolean[] enabled = new boolean[weights.length];
    for ( int i = 0; i < weights.length; i++ ) {
      if ( weights[i] > 0 ) {
        enabled[i] = true;
      }
    }
    return enabled;
  }

  /**
   * keep the given index weight, and normalize the others so it adds up to 100, keeping their ratios
   *
   * ([50, 50, 50], refIdx=2) -> [25, 25, 50]
   */
  private static int[] normalizeFor( int[] weights, int refIdx ) {
    if ( log.isTraceEnabled() ) {
      log.trace( "normalizing: {} for ref={}", printWeights( weights ), refIdx );
    }
    int weight = weights[refIdx];
    if ( weight <= 0 ) {
      weight = DEFAULT_WEIGHT;
      weights[refIdx] = weight;
    }

    final int rest = NORM_TARGET - weight;
    int remainder = NORM_TARGET;
    int sum = Arrays.stream( weights ).sum();
    int oldSum = sum - weight;
    for ( int i = 0; i < weights.length; i++ ) {
      if ( i != refIdx ) {
        weights[i] = rest * weights[i] / oldSum;
      }
      remainder -= weights[i];
    }
    if ( remainder > 0 ) {
      distributeRest( weights, remainder );
    }

    if ( log.isTraceEnabled() ) {
      log.trace( "normalized: {}", printWeights( weights ) );
    }
    return weights;
  }

  /** normalizes all weights so they add up to 100 */
  private static int[] normalize( int[] weights ) {
    if ( log.isTraceEnabled() ) {
      log.trace( "normalizing: {}", printWeights( weights ) );
    }
    int sum = Arrays.stream( weights ).sum();
    if ( sum != NORM_TARGET ) {
      weights = Arrays.stream( weights ).map( i -> NORM_TARGET * i / sum ).toArray();
      int newSum = Arrays.stream( weights ).sum();
      if ( newSum < NORM_TARGET ) {
        distributeRest( weights, NORM_TARGET - newSum );
      }
    }
    if ( log.isTraceEnabled() ) {
      log.trace( "normalized: {}", printWeights( weights ) );
    }
    return weights;
  }

  private static void distributeRest( int[] weights, int rest ) {
    // just add excess to last item
    weights[weights.length - 1] += rest;
  }

  private void updateLastWeights( int[] realSizes ) {
    if ( log.isDebugEnabled() ) {
      log.trace( "update: {}", printWeights( realSizes ) );
    }
    if ( realSizes.length != weights.length ) {
      log.error( "size mismatch updating sizes, expected {} but was {}", weights.length, realSizes.length );
      return;
    }
    for ( int i = 0; i < realSizes.length; i++ ) {
      int rw = realSizes[i];
      if ( rw > 0 ) {
        weights[i] = rw;
      }
    }
  }

  private static String printWeights( int[] weights ) {
    return IntStream.of( weights ).boxed().map( Object::toString ).collect( Collectors.joining( ", " ) );
  }
}
