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

package org.pentaho.di.trans.steps.numberrange;

import java.util.Objects;

/**
 * Contains one rule for a number range
 *
 * @author ronny.roeller@fredhopper.com
 *
 */
public class NumberRangeRule {

  /**
   * Lower bound for which the rule matches (lowerBound <= x)
   */
  private double lowerBound;

  /**
   * Upper bound for which the rule matches (x < upperBound)
   */
  private double upperBound;

  /**
   * Value that is returned if the number to be tested is within the range
   */
  private String value;

  public NumberRangeRule( double lowerBound, double upperBound, String value ) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.value = value;
  }

  /**
   * Evaluates if the current value is within the range. If so, it returns the value. Otherwise it returns null.
   */
  public String evaluate( double compareValue ) {
    // Check if the value is within the range
    if ( ( compareValue >= lowerBound ) && ( compareValue < upperBound ) ) {
      return value;
    }

    // Default value is null
    return null;
  }

  public double getLowerBound() {
    return lowerBound;
  }

  public double getUpperBound() {
    return upperBound;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( !obj.getClass().equals( this.getClass() ) ) {
      return false;
    } else {
      NumberRangeRule target = (NumberRangeRule) obj;
      return getLowerBound() == target.getLowerBound()
        && getUpperBound() == target.getUpperBound() && getValue().equals( target.getValue() );
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash( lowerBound, upperBound, value );
  }
}
