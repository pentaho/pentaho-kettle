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

import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Utility class to hold the result of a set of string evaluations: a valid conversion metadata object (with data type,
 * trim options, etc) and the minimum and maximum value encountered.
 *
 * @author matt
 *
 */
public class StringEvaluationResult {

  private ValueMetaInterface conversionMeta;
  private Object min;
  private Object max;
  private int nrNull;
  private int nrSuccesses;
  private int nrFailures;

  public StringEvaluationResult( ValueMetaInterface conversionMeta ) {
    this.conversionMeta = conversionMeta;
    this.nrNull = 0;
  }

  @Override
  public String toString() {
    return conversionMeta.toStringMeta()
      + " "
      + ( conversionMeta.isNumeric() ? conversionMeta.getConversionMask()
        + " : " + conversionMeta.getDecimalSymbol() + conversionMeta.getGroupingSymbol() : conversionMeta
        .isDate() ? conversionMeta.getConversionMask() : "" );
  }

  /**
   * @return the conversionMeta
   */
  public ValueMetaInterface getConversionMeta() {
    return conversionMeta;
  }

  /**
   * @param conversionMeta
   *          the conversionMeta to set
   */
  public void setConversionMeta( ValueMetaInterface conversionMeta ) {
    this.conversionMeta = conversionMeta;
  }

  /**
   * @return the min
   */
  public Object getMin() {
    return min;
  }

  /**
   * @param min
   *          the min to set
   */
  public void setMin( Object min ) {
    this.min = min;
  }

  /**
   * @return the max
   */
  public Object getMax() {
    return max;
  }

  /**
   * @param max
   *          the max to set
   */
  public void setMax( Object max ) {
    this.max = max;
  }

  /**
   * @return The number of null values encountered
   */
  public int getNrNull() {
    return nrNull;
  }

  /**
   * @param nrNull
   *          Set the number of null values to set
   */
  public void setNrNull( int nrNull ) {
    this.nrNull = nrNull;
  }

  /**
   * Increment the number of null values encountered.
   */
  public void incrementNrNull() {
    nrNull++;
  }

  /**
   * Increment the number of successes by one.
   */
  public void incrementSuccesses() {
    nrSuccesses++;
  }

  /**
   * Increment the number of failures by one.
   */
  public void incrementFailures() {
    nrFailures++;
  }

  /**
   * @return the nrSuccesses
   */
  public int getNrSuccesses() {
    return nrSuccesses;
  }

  /**
   * @param nrSuccesses
   *          the nrSuccesses to set
   */
  public void setNrSuccesses( int nrSuccesses ) {
    this.nrSuccesses = nrSuccesses;
  }

  /**
   * @return the nrFailures
   */
  public int getNrFailures() {
    return nrFailures;
  }

  /**
   * @param nrFailures
   *          the nrFailures to set
   */
  public void setNrFailures( int nrFailures ) {
    this.nrFailures = nrFailures;
  }

}
