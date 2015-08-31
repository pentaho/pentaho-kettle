package org.pentaho.di.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test class for StringEvaluator functionality.
 *
 * @author Alexander Buloichik
 */
public class StringEvaluatorTest {

  @Test
  public void testEmpty() {
    StringEvaluator evaluator = new StringEvaluator();
    evaluator.evaluateString( "" );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );

    evaluator.evaluateString( "  " );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );
  }

  @Test
  public void testNumber() {
    StringEvaluator evaluator = new StringEvaluator();
    evaluator.evaluateString( "" );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );

    evaluator.evaluateString( "123" );
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isInteger() );
  }

  @Test
  public void testBoolean() {
    StringEvaluator evaluator = new StringEvaluator();
    evaluator.evaluateString( "" );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );

    evaluator.evaluateString( "Y" );
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isBoolean() );
  }

  @Test
  public void testDate() {
    StringEvaluator evaluator = new StringEvaluator();
    evaluator.evaluateString( "" );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );

    evaluator.evaluateString( " 03/25/1918" );
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isDate() );
  }
}
