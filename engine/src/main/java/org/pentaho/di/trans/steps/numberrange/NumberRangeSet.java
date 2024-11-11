/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.numberrange;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;

/**
 * This class assigns numbers into ranges
 *
 * @author ronny.roeller@fredhopper.com
 *
 */
public class NumberRangeSet {

  public static final String MULTI_VALUE_SEPARATOR = ",";

  /**
   * List of all rules that have to be considered
   */
  private List<NumberRangeRule> rules;

  /**
   * Value that is returned if no rule matches
   */
  private String fallBackValue;

  public NumberRangeSet( List<NumberRangeRule> rules, String fallBackValue ) {
    this.rules = rules;
    this.fallBackValue = fallBackValue;
  }

  /**
   * Evaluates a value against all rules
   */
  protected String evaluateDouble( double value ) {
    StringBuilder result = new StringBuilder();

    // Execute all rules
    for ( NumberRangeRule rule : rules ) {
      String ruleResult = rule.evaluate( value );

      // If rule matched -> add value to the result
      if ( ruleResult != null ) {
        // Add value separator if multiple values are available
        if ( result.length() > 0 ) {
          result.append( getMultiValueSeparator() );
        }

        result.append( ruleResult );
      }
    }

    return result.toString();
  }

  /**
   * Returns separator that is added if a value matches multiple ranges.
   */
  public static String getMultiValueSeparator() {
    return MULTI_VALUE_SEPARATOR;
  }

  /**
   * Evaluates a value against all rules. Return empty value if input is not numeric.
   */
  public String evaluate( String strValue ) throws KettleException {
    if ( strValue != null ) {
      // Try to parse value to double
      try {
        double doubleValue = Double.parseDouble( strValue );
        return evaluate( doubleValue );
      } catch ( Exception e ) {
        throw new KettleException( e );
      }
    }
    return fallBackValue;
  }

  /**
   * Evaluates a value against all rules. Return empty value if input is not numeric.
   */
  public String evaluate( Double value ) throws KettleException {
    if ( value != null ) {

      String result = evaluateDouble( value );
      if ( !"".equals( result ) ) {
        return result;
      }

    }

    return fallBackValue;
  }

}
