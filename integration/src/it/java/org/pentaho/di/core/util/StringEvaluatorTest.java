/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.*;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaPluginType;

import static org.junit.Assert.*;

public class StringEvaluatorTest {

  @Test
  public void testSeries1() throws Exception {
    String[] series1 = new String[] { "Foo", "Bar", "One", "", "Two", "Three", };
    StringEvaluator evaluator = new StringEvaluator( false );
    for ( String string : series1 ) {
      evaluator.evaluateString( string );
    }

    assertEquals( evaluator.getCount(), series1.length );
    assertEquals( evaluator.getMaxLength(), 5 );

    // We expect to find nothing. This means that we have to revert to a String data type.
    //
    assertEquals( evaluator.getStringEvaluationResults().size(), 0 );
  }

  @Test
  public void testSeries2() throws Exception {
    String[] series2 = new String[] { "2009/12/31 12:34:56", "2010/02/14 23:22:01", };
    StringEvaluator evaluator = new StringEvaluator( false );
    for ( String string : series2 ) {
      evaluator.evaluateString( string );
    }

    assertEquals( evaluator.getCount(), series2.length );
    assertEquals( evaluator.getMaxLength(), 19 );
    StringEvaluationResult result = evaluator.getStringEvaluationResults().get( 0 );

    assertEquals( "Not a date detected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_DATE );
    Date minDate = result.getConversionMeta().getDate( result.getMin() );
    Date expectedMinDate = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).parse( "2009/12/31 12:34:56" );
    assertEquals( minDate.getTime(), expectedMinDate.getTime() );

    int nrEmpty = result.getNrNull();
    assertEquals( nrEmpty, 0 );
    assertEquals( evaluator.getValues().size(), series2.length );
  }

  @Test
  public void testSeries3() throws Exception {
    String[] series3 = new String[] { "1234,56", "12394,26", "1934,34", "19245,23", "" };
    StringEvaluator evaluator = new StringEvaluator( false );
    for ( String string : series3 ) {
      evaluator.evaluateString( string );
    }

    assertEquals( evaluator.getCount(), series3.length );
    assertEquals( evaluator.getMaxLength(), 8 );
    StringEvaluationResult result = evaluator.getStringEvaluationResults().get( 0 );
    assertEquals( "Not a number detected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_NUMBER );

    int nrEmpty = result.getNrNull();
    assertEquals( nrEmpty, 1 );
    assertEquals( evaluator.getValues().size(), series3.length );
  }

  @Test
  public void testSeries4() throws Exception {
    String[] series4 = new String[] {
      "01234,56     ", "             ", "98765,43     ", "12394,26     ", "01934,34     ", "19245,23     ",
      "00045,67     ", };
    StringEvaluator evaluator = new StringEvaluator( true );
    for ( String string : series4 ) {
      evaluator.evaluateString( string );
    }

    assertEquals( evaluator.getCount(), series4.length );
    assertEquals( evaluator.getMaxLength(), 13 );
    StringEvaluationResult result = evaluator.getStringEvaluationResults().get( 0 );
    assertEquals( "Not a number detected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_NUMBER );

    int nrEmpty = result.getNrNull();
    assertEquals( nrEmpty, 1 );
    assertEquals( evaluator.getValues().size(), series4.length );
  }

  @Test
  public void testCurrencyData() {
    StringEvaluator eval = new StringEvaluator( true );
    String[] values = new String[] { "$300.00", "$3,400", "$23.00", "($0.50)" };
    for ( String value : values ) {
      eval.evaluateString( value );
    }
    assertEquals( values.length, eval.getCount() );
    StringEvaluationResult result = eval.getAdvicedResult();
    assertEquals( "Not a number detected", ValueMetaInterface.TYPE_NUMBER, result.getConversionMeta().getType() );
    assertEquals( "Precision not correct", 2, result.getConversionMeta().getPrecision() );
    assertEquals( "Currency format mask is incorrect", "$#,##0.00;($#,##0.00)", result
      .getConversionMeta().getConversionMask() );
  }

  @Test
  public void testCurrencyData_UK() {
    Locale orig = Locale.getDefault();
    try {
      Locale.setDefault( Locale.UK );
      StringEvaluator eval = new StringEvaluator( true );

      DecimalFormat currencyFormat = ( (DecimalFormat) NumberFormat.getCurrencyInstance() );
      try {
        currencyFormat.parse( "-£400.059" );
      } catch ( ParseException e ) {
        fail();
      }
      String[] values = new String[] { "£400.019", "£3,400.029", "£23.00", "-£400.059" };
      for ( String value : values ) {
        eval.evaluateString( value );
      }
      assertEquals( values.length, eval.getCount() );
      StringEvaluationResult result = eval.getAdvicedResult();
      assertEquals( "Not a number detected", ValueMetaInterface.TYPE_NUMBER, result.getConversionMeta().getType() );
      assertEquals( "Precision not correct", 2, result.getConversionMeta().getPrecision() );
      assertEquals( "Currency format mask is incorrect", "£#,##0.00", result.getConversionMeta().getConversionMask() );
    } finally {
      Locale.setDefault( orig );
    }
  }

  @Test
  public void testCustomDateFormats() {
    List<String> dates = Collections.singletonList( "MM/dd/yyyy" );
    List<String> numbers = Collections.singletonList( "#,##0.###" );

    StringEvaluator eval = new StringEvaluator( true, numbers, dates );
    String[] goodDateValues = new String[] { "01/01/2000", "02/02/2000", "03/03/2000" };
    String[] badDateValues = new String[] { "01-01-2000", "02-02-2000", "03-03-2000" };

    for ( String value : goodDateValues ) {
      eval.evaluateString( value );
    }
    assertEquals( goodDateValues.length, eval.getCount() );
    StringEvaluationResult result = eval.getAdvicedResult();
    assertEquals( "Not a date detected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_DATE );

    eval = new StringEvaluator( true, numbers, dates );
    for ( String value : badDateValues ) {
      eval.evaluateString( value );
    }
    assertEquals( badDateValues.length, eval.getCount() );
    result = eval.getAdvicedResult();
    assertFalse( "Date detected", result.getConversionMeta().getType() == ValueMetaInterface.TYPE_DATE );
  }

  @Test
  public void testCustomNumberFormats() {
    loadValueMetaPlugins();

    // Now get to the real testing
    Locale orig = Locale.getDefault();
    try {
      Locale.setDefault( Locale.US );

      StringEvaluator eval = new StringEvaluator();
      String[] goodValues = new String[] { "200.00", "999.99", "4,309.88" };
      String[] badValues = new String[] { "9 00", "$30.00", "3.999,00" };

      for ( String value : goodValues ) {
        eval.evaluateString( value );
      }
      assertEquals( goodValues.length, eval.getCount() );
      StringEvaluationResult result = eval.getAdvicedResult();
      assertEquals( "Not a number detected", result.getConversionMeta().getTypeDesc(), "Number" );

      eval = new StringEvaluator();
      for ( String value : badValues ) {
        eval.evaluateString( value );
      }
      assertEquals( badValues.length, eval.getCount() );
      result = eval.getAdvicedResult();
      assertFalse( "Number detected", result.getConversionMeta().getType() == ValueMetaInterface.TYPE_NUMBER );
    } finally {
      Locale.setDefault( orig );
    }
  }

  @Test
  public void testDeterminePrecision() {
    assertEquals( 4, StringEvaluator.determinePrecision( "#.0000" ) );
    assertEquals( 4, StringEvaluator.determinePrecision( "0.#### $" ) );
    assertEquals( 0, StringEvaluator.determinePrecision( null ) );
    assertEquals( 4, StringEvaluator.determinePrecision( "0.##00 $" ) );
    assertEquals( 4, StringEvaluator.determinePrecision( "##,##0.#0## $" ) );
  }

  @Test
  public void testLength_IfEvaluationResultIsNumber() {
    loadValueMetaPlugins();
    StringEvaluator eval = new StringEvaluator();

    String[] numbers = new String[] { "1010.10101010", "10.01", "4,309.88" };
    for ( String value : numbers ) {
      eval.evaluateString( value );
    }
    StringEvaluationResult result = eval.getAdvicedResult();
    assertEquals( "Number", result.getConversionMeta().getTypeDesc() );
    assertEquals( 8, result.getConversionMeta().getPrecision() );
    assertEquals( 13, result.getConversionMeta().getLength() );
  }

  private void loadValueMetaPlugins() {
    // Need to load the ValueMeta plugins
    PluginRegistry registry = PluginRegistry.getInstance();
    assertNotNull( "Registry singleton was not found!", registry );

    // Register a new plugin type...
    //
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );

    // Plugin Registry should initialize without exception
    Exception initException = null;
    try {
      PluginRegistry.init();
    } catch ( Exception e ) {
      initException = e;
    }
    assertNull( initException );

    // There will always be a PluginRegistryPluginType, so see if we enough plugin types here.
    //
    List<Class<? extends PluginTypeInterface>> pluginTypes = registry.getPluginTypes();
    assertTrue( "At least two plugin types expected in the registry", pluginTypes.size() > 1 );

    // ... and have at least 1 ValueMetaPlugin
    List<PluginInterface> valueMetaPlugins = registry.getPlugins( ValueMetaPluginType.class );
    assertTrue( "Size of plugins list expected to be >1", valueMetaPlugins.size() > 1 );
  }


  @Test
  public void recognisesNumeric_WhenParenthesesMeanNegative_Integer() throws Exception {
    String[] samples = { "1,234,567,890", "(1,234,567,890)" };

    final Locale environmentLocale = Locale.getDefault();
    try {
      Locale.setDefault( Locale.US );

      StringEvaluationResult numericResult = doEvaluation( new StringEvaluator(), samples );
      ValueMetaInterface meta = numericResult.getConversionMeta();
      assertTrue( Integer.toString( meta.getType() ), ValueMetaBase.isNumeric( meta.getType() ) );
      assertEquals( "#,##0.00;(#,##0.00)", meta.getConversionMask() );
    } finally {
      Locale.setDefault( environmentLocale );
    }
  }

  @Test
  public void recognisesNumeric_WhenParenthesesMeanNegative_Double() throws Exception {
    String[] samples = { "1,234,567,890.12", "(1,234,567,890.12)" };

    final Locale environmentLocale = Locale.getDefault();
    try {
      Locale.setDefault( Locale.US );

      StringEvaluationResult numericResult = doEvaluation( new StringEvaluator(), samples );
      ValueMetaInterface meta = numericResult.getConversionMeta();
      assertTrue( Integer.toString( meta.getType() ), ValueMetaBase.isNumeric( meta.getType() ) );
      assertEquals( "#,##0.00;(#,##0.00)", meta.getConversionMask() );
    } finally {
      Locale.setDefault( environmentLocale );
    }
  }

  private StringEvaluationResult doEvaluation( StringEvaluator evaluator, String[] samples ) {
    for ( String sample : samples ) {
      evaluator.evaluateString( sample );
    }
    assertEquals( evaluator.getCount(), samples.length );
    return evaluator.getAdvicedResult();
  }
}
