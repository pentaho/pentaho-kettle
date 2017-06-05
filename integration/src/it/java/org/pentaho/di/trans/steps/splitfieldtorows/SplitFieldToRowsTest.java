/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.splitfieldtorows;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

/**
 * Test class for the SplitFieldToRows step.
 *
 * The expected results were obtained by running the "Split field To Rows" Kettle step with Spoon 4.3.0.
 *
 * @author Sean Flatley
 */
public class SplitFieldToRowsTest {

  private static final String FIELD_TO_SPLIT_NAME = "FieldToSplit";
  private static final String NEW_FIELD_NAME = "NewFieldName";

  /**
   * Creates the row meta interface.
   *
   * @return the row meta interface
   */
  private RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rowMeta = new RowMeta();
    ValueMetaInterface[] valuesMeta = { new ValueMetaString( FIELD_TO_SPLIT_NAME ) };
    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rowMeta.addValueMeta( valuesMeta[i] );
    }
    return rowMeta;
  }

  /**
   * Create a list of RowMetaAndData from the passed value and returns it.
   *
   * @param value
   * @return List<RowMetaAndData>
   */
  private List<RowMetaAndData> createData( String value ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    RowMetaInterface rm = createRowMetaInterface();
    Object[] r1 = new Object[] { value };
    list.add( new RowMetaAndData( rm, r1 ) );
    return list;
  }

  /**
   * Execute a test with the passed parameters.
   *
   * @param testName
   * @param stringToSplit
   * @param isDelimiterRegex
   * @param delimiter
   * @param delimiterVariableValue - Should be null if {@code  delimiter}  is not variable
   * @param expectedResult
   * @throws Exception
   */
  private void test( String testName, String stringToSplit, boolean isDelimiterRegex, String delimiter,
      String delimiterVariableValue, String[] expectedResult ) {
    List<RowMetaAndData> result = splitFieldToRows( testName, stringToSplit, isDelimiterRegex, delimiter, delimiterVariableValue );
    assertDataEquals( expectedResult, result );
  }

  /**
   * Splits the "stringToSplit" with the passed "delimiter". The "delimiter" is assumed by this method to be a Kettle
   * variable. The parameter "delimiterVariableValue" should contain the variables value.
   *
   * The "isDelimiterRegex" parameter will process the use regex for pattern matching if true.
   *
   * @param testName
   * @param stringToSplit
   * @param isDelimiterRegex
   * @param delimiter
   * @param delimiterVariableValue
   * @return
   * @throws Exception
   */
  private List<RowMetaAndData> splitFieldToRows( String testName, String stringToSplit, boolean isDelimiterRegex,
      String delimiter, String delimiterVariableValue ) {
    RowStepCollector rc = new RowStepCollector();
    try {
      KettleEnvironment.init();

      // Create a new transformation...
      TransMeta transMeta = new TransMeta();

      transMeta.setName( "Split field to rows test" );
      PluginRegistry registry = PluginRegistry.getInstance();

      // create an injector step...
      String injectorStepname = "injector step";
      InjectorMeta im = new InjectorMeta();

      // Set the information of the injector.
      String injectorPid = registry.getPluginId( StepPluginType.class, im );
      StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
      transMeta.addStep( injectorStep );

      // Create a Split Field to Rows step
      String splitfieldToRowsName = "Split field to rows";
      SplitFieldToRowsMeta splitFieldtoRowsMeta = new SplitFieldToRowsMeta();
      splitFieldtoRowsMeta.setDelimiter( delimiter );
      splitFieldtoRowsMeta.setDelimiterRegex( isDelimiterRegex );
      splitFieldtoRowsMeta.setSplitField( FIELD_TO_SPLIT_NAME );
      splitFieldtoRowsMeta.setNewFieldname( NEW_FIELD_NAME );

      String splitFieldTotRowsPid = registry.getPluginId( StepPluginType.class, splitFieldtoRowsMeta );
      StepMeta splitFieldToRows = new StepMeta( splitFieldTotRowsPid, splitfieldToRowsName, splitFieldtoRowsMeta );
      transMeta.addStep( splitFieldToRows );

      // hop the injector to the split field to rows step
      TransHopMeta hop_injector_splitfieldToRows = new TransHopMeta( injectorStep, splitFieldToRows );
      transMeta.addTransHop( hop_injector_splitfieldToRows );

      // Create a dummy step
      String dummyStepname = "dummy step";
      DummyTransMeta dm = new DummyTransMeta();

      String dummyPid = registry.getPluginId( StepPluginType.class, dm );
      StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
      transMeta.addStep( dummyStep );

      TransHopMeta hop_SplitFieldToRows_Dummy = new TransHopMeta( splitFieldToRows, dummyStep );
      transMeta.addTransHop( hop_SplitFieldToRows_Dummy );

      if ( !Utils.isEmpty( delimiterVariableValue ) ) {
        String delimiterVariableName = delimiter.replace( "${", "" );
        delimiterVariableName = delimiterVariableName.replace( "}", "" );
        transMeta.setVariable( delimiterVariableName, delimiterVariableValue );
      }

      // Now execute the transformation...
      Trans trans = new Trans( transMeta );
      trans.prepareExecution( null );

      StepInterface si = trans.getStepInterface( dummyStepname, 0 );

      si.addRowListener( rc );
      RowProducer rowProducer = trans.addRowProducer( injectorStepname, 0 );
      trans.startThreads();

      // add rows
      List<RowMetaAndData> inputList = createData( stringToSplit );
      for ( RowMetaAndData rm : inputList ) {
        rowProducer.putRow( rm.getRowMeta(), rm.getData() );
      }
      rowProducer.finished();
      trans.waitUntilFinished();
    } catch ( KettleException e ) {
      fail( "KettleEnvironment exception" + e.getMessage() );
    }
    List<RowMetaAndData> resultRows = rc.getRowsWritten();
    return resultRows;
  }

  /**
   * Compares the passed parameters and returns truf if they contain the same data.
   *
   * @param expectedData
   * @param result

   */
  private void assertDataEquals( String[] expectedData, List<RowMetaAndData> result ) {
    for ( int i = 0; i < expectedData.length; i++ ) {
      try {
        String resultElement = result.get( i ).getString( "NewFieldName", "String" );
        if ( !expectedData[i].equals( resultElement ) ) {
          fail("Expected element does not match result element");
        }
      } catch ( ArrayIndexOutOfBoundsException aiobe ) {
        fail( aiobe.getMessage() );
      } catch ( KettleValueException kve ) {
        fail( kve.getMessage() );
      }
    }
  }

  // Non regex tests
  /**
   * Test: Non regex, delimiter = ";"
   */
  @Test
  public void testNonRegex1() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Non Regex Test 1", "Sean;Flatley", false, ";", null, expectedResult );
  }

  /**
   * Test: Non regex, delimiter = "!;"
   */
  @Test
  public void testNonRegex2() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Non Regex Test 2", "Sean!;Flatley", false, "!;", null, expectedResult );
  }

  /**
   * Test: Non regex, delimiter = ";", "!" appears in field.
   */
  @Test
  public void testNonRegex3() {
    String[] expectedResult = { "Sean!", "Flatley" };
    test( "Non Regex Test 3", "Sean!;Flatley", false, ";", null, expectedResult );
  }

  /**
   * Test: Non regex, delimiter = "."
   */
  @Test
  public void testNonRegex4() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Non Regex Test 4", "Sean.Flatley", false, ".", null, expectedResult );
  }

  /**
   * Test: Non regex, delimiter = "\\.", delmiter "." appears in field.
   */
  @Test
  public void testNonRegex5() {
    String[] expectedResult = { "Sean.Flatley" };
    test( "Non Regex Test 5", "Sean.Flatley", false, "\\.", null, expectedResult );
  }

  /**
   * Test: Non regex, delimiter = "${DELIMITER}, value of delimiter = ";"
   */
  @Test
  public void testNonRegex6() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Non Regex Test 6", "Sean;Flatley", false, "${DELIMITER}", ";", expectedResult );
  }

  // Regex tests

  /**
   * Test: Regex test, delimiter = ";"
   */
  @Test
  public void testRegex1() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Regex Test 1", "Sean;Flatley", true, ";", null, expectedResult );
  }

  /**
   * Test: Regex, delimiter = "!;"
   */
  @Test
  public void testRegex2() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Regex Test 2", "Sean!;Flatley", true, "!;", null, expectedResult );
  }

  /**
   * Test: Regex, delimiter = ";", "!;" appears in the field.
   */
  @Test
  public void testRegex3() {
    String[] expectedResult = { "Sean!", "Flatley" };
    test( "Regex Test 3", "Sean!;Flatley", true, ";", null, expectedResult );
  }

  /**
   * Test: Regex, delimiter = "."
   */
  @Test
  public void testRegex4() {
    String[] expectedResult = {};
    test( "Regex Test 4", "SeanFlatley", true, ".", null, expectedResult );
  }

  /**
   * Test: Regex, delimiter = "\\."
   */
  @Test
  public void testRegex5() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Regex Test 5", "Sean.Flatley", true, "\\.", null, expectedResult );
  }

  /**
   * Test: Regex, delimiter = "[0-9]"
   */
  @Test
  public void testRegex6() {
    String[] expectedResult = { "Sean", "Flatley" };
    test( "Regex Test 6", "Sean1Flatley", true, "[0-9]", null, expectedResult );
  }

  /**
   * Test: Regex, delimiter = ";", empty characters http://jira.pentaho.com/browse/PDI-11477
   */
  @Test
  public void testRegex7() {
    String[] expectedResult = { "", "1", "", "1", "" };
    test( "Regex Test 7", ";1;;1;;", true, ";", null, expectedResult );
  }
}
