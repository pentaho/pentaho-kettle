/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.fuzzymatch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class FuzzyMatchMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "value", "valueName", "algorithm", "lookupfield", "mainstreamfield",
            "outputmatchfield", "outputvaluefield", "caseSensitive", "minimalValue",
            "maximalValue", "separator", "closervalue" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "value", "getValue" );
        put( "valueName", "getValueName" );
        put( "algorithm", "getAlgorithmType" );
        put( "lookupfield", "getLookupField" );
        put( "mainstreamfield", "getMainStreamField" );
        put( "outputmatchfield", "getOutputMatchField" );
        put( "outputvaluefield", "getOutputValueField" );
        put( "caseSensitive", "isCaseSensitive" );
        put( "minimalValue", "getMinimalValue" );
        put( "maximalValue", "getMaximalValue" );
        put( "separator", "getSeparator" );
        put( "closervalue", "isGetCloserValue" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "value", "setValue" );
        put( "valueName", "setValueName" );
        put( "algorithm", "setAlgorithmType" );
        put( "lookupfield", "setLookupField" );
        put( "mainstreamfield", "setMainStreamField" );
        put( "outputmatchfield", "setOutputMatchField" );
        put( "outputvaluefield", "setOutputValueField" );
        put( "caseSensitive", "setCaseSensitive" );
        put( "minimalValue", "setMinimalValue" );
        put( "maximalValue", "setMaximalValue" );
        put( "separator", "setSeparator" );
        put( "closervalue", "setGetCloserValue" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 3 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "value", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "valueName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "algorithm", new AlgorithmLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    // typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), 3 ) );

    loadSaveTester = new LoadSaveTester( FuzzyMatchMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  // Clone test removed as it's covered by the load/save tester now.

  public class AlgorithmLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
    final Random rand = new Random();
    @Override
    public Integer getTestObject() {
      return rand.nextInt( 10 );
    }
    @Override
    public boolean validateTestObject( Integer testObject, Object actual ) {
      if ( !( actual instanceof Integer ) ) {
        return false;
      }
      Integer actualInt = (Integer) actual;
      return actualInt.equals( testObject );
    }
  }

}
