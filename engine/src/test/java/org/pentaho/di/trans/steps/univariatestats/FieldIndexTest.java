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

package org.pentaho.di.trans.steps.univariatestats;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class FieldIndexTest {
  @Test
  public void testCalculateDerived0Count() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_sum = 250;
    fieldIndex.m_count = 0;
    fieldIndex.m_sumSq = 35000.3;
    fieldIndex.calculateDerived();
    assertEquals( Double.NaN, fieldIndex.m_mean, 0 );
    assertEquals( Double.NaN, fieldIndex.m_stdDev, 0 );
  }

  @Test
  public void testCalculateDerived1Count() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_sum = 250;
    fieldIndex.m_count = 1;
    fieldIndex.m_sumSq = 35000.3;
    fieldIndex.calculateDerived();
    assertEquals( fieldIndex.m_sum / fieldIndex.m_count, fieldIndex.m_mean, 0 );
    assertEquals( Double.POSITIVE_INFINITY, fieldIndex.m_stdDev, 0 );
  }

  @Test
  public void testCalculateDerived3CountPositiveStdDev() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_sum = 250;
    fieldIndex.m_count = 3;
    fieldIndex.m_sumSq = 35000.3;
    fieldIndex.calculateDerived();
    assertEquals( fieldIndex.m_sum / fieldIndex.m_count, fieldIndex.m_mean, 0 );
    assertEquals( Math.sqrt( ( fieldIndex.m_sumSq - ( fieldIndex.m_sum * fieldIndex.m_sum ) / fieldIndex.m_count )
        / ( fieldIndex.m_count - 1 ) ), fieldIndex.m_stdDev, 0 );
  }

  @Test
  public void testCalculateDerived3CountNegativeStdDev() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_sum = 250;
    fieldIndex.m_count = 3;
    fieldIndex.m_sumSq = 350.3;
    fieldIndex.calculateDerived();
    assertEquals( fieldIndex.m_sum / fieldIndex.m_count, fieldIndex.m_mean, 0 );
    assertEquals( 0.0, fieldIndex.m_stdDev, 0 );
  }

  @Test
  public void testGenerateOutputValuesNoCacheNoCalc() {
    FieldIndex fieldIndex = new FieldIndex();
    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, -1, false ), null );
    assertEquals( 0, outputValues.length );
  }

  @Test
  public void testGenerateOutputValuesNoCacheAllCalc() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 1;
    fieldIndex.m_mean = 2;
    fieldIndex.m_stdDev = 3;
    fieldIndex.m_min = 4;
    fieldIndex.m_max = 5;
    fieldIndex.m_median = 6;
    fieldIndex.m_arbitraryPercentile = 7;
    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, true, true, true, true, true, true,
            .55, false ), null );
    int index = 0;
    assertEquals( ( (Double) outputValues[index++] ).doubleValue(), fieldIndex.m_count, 0 );
    assertEquals( ( (Double) outputValues[index++] ).doubleValue(), fieldIndex.m_mean, 0 );
    assertEquals( ( (Double) outputValues[index++] ).doubleValue(), fieldIndex.m_stdDev, 0 );
    assertEquals( ( (Double) outputValues[index++] ).doubleValue(), fieldIndex.m_min, 0 );
    assertEquals( ( (Double) outputValues[index++] ).doubleValue(), fieldIndex.m_max, 0 );
    assertEquals( ( (Double) outputValues[index++] ).doubleValue(), fieldIndex.m_median, 0 );
    assertEquals( ( (Double) outputValues[index++] ).doubleValue(), fieldIndex.m_arbitraryPercentile, 0 );
  }

  @Test
  public void testGenerateOutputValuesCacheInterpolateSpecialCasesMin() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 10;
    fieldIndex.m_min = -350;
    fieldIndex.m_max = 350;

    ArrayList<Number> cache = new ArrayList<Number>();
    cache.add( 3 );
    cache.add( 4 );
    cache.add( 5 );

    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, 0, true ), cache );
    assertEquals( 1, outputValues.length );
    assertEquals( fieldIndex.m_min, outputValues[0] );
  }

  @Test
  public void testGenerateOutputValuesCacheInterpolateSpecialCasesMax() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 10;
    fieldIndex.m_min = -350;
    fieldIndex.m_max = 350;

    ArrayList<Number> cache = new ArrayList<Number>();
    cache.add( 3 );
    cache.add( 4 );
    cache.add( 5 );

    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, 1, true ), cache );
    assertEquals( 1, outputValues.length );
    assertEquals( fieldIndex.m_max, outputValues[0] );
  }

  @Test
  public void testGenerateOutputValuesCacheInterpolate() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 5;
    fieldIndex.m_min = -350;
    fieldIndex.m_max = 350;

    ArrayList<Number> cache = new ArrayList<Number>();
    cache.add( 3 );
    cache.add( 4 );
    cache.add( 5 );
    cache.add( 6 );
    cache.add( 7 );

    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, .55, true ), cache );
    assertEquals( 1, outputValues.length );
    assertEquals( 5.3, ( (Double) outputValues[0] ).doubleValue(), .0005 );
  }

  @Test
  public void testGenerateOutputValuesCacheSimpleSpecialCasesMin() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 10;
    fieldIndex.m_min = -350;
    fieldIndex.m_max = 350;

    ArrayList<Number> cache = new ArrayList<Number>();
    cache.add( 3 );
    cache.add( 4 );
    cache.add( 5 );

    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, 0, false ), cache );
    assertEquals( 1, outputValues.length );
    assertEquals( fieldIndex.m_min, outputValues[0] );
  }

  @Test
  public void testGenerateOutputValuesCacheSimpleSpecialCasesMax() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 10;
    fieldIndex.m_min = -350;
    fieldIndex.m_max = 350;

    ArrayList<Number> cache = new ArrayList<Number>();
    cache.add( 3 );
    cache.add( 4 );
    cache.add( 5 );

    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, 1, false ), cache );
    assertEquals( 1, outputValues.length );
    assertEquals( fieldIndex.m_max, outputValues[0] );
  }

  @Test
  public void testGenerateOutputValuesCacheSimpleOdd() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 5;
    fieldIndex.m_min = -350;
    fieldIndex.m_max = 350;

    ArrayList<Number> cache = new ArrayList<Number>();
    cache.add( 3 );
    cache.add( 4 );
    cache.add( 5 );
    cache.add( 6 );
    cache.add( 7 );

    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, .50, false ), cache );
    assertEquals( 1, outputValues.length );
    assertEquals( 5.0, ( (Double) outputValues[0] ).doubleValue(), .0005 );
  }

  @Test
  public void testGenerateOutputValuesCacheSimpleEven() {
    FieldIndex fieldIndex = new FieldIndex();
    fieldIndex.m_count = 6;
    fieldIndex.m_min = -350;
    fieldIndex.m_max = 350;

    ArrayList<Number> cache = new ArrayList<Number>();
    cache.add( 3 );
    cache.add( 4 );
    cache.add( 5 );
    cache.add( 6 );
    cache.add( 7 );
    cache.add( 8 );

    Object[] outputValues =
        fieldIndex.generateOutputValues( new UnivariateStatsMetaFunction( null, false, false, false, false, false,
            false, .50, false ), cache );
    assertEquals( 1, outputValues.length );
    assertEquals( 5.5, ( (Double) outputValues[0] ).doubleValue(), .0005 );
  }
}
