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

package org.pentaho.di;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.pentaho.di.core.database.DatabaseIT;
import org.pentaho.di.core.parameters.ParameterSimpleTransIT;
import org.pentaho.di.core.util.StringEvaluatorIT;
import org.pentaho.di.trans.HopIT;
import org.pentaho.di.trans.steps.addsequence.AddSequenceIT;
import org.pentaho.di.trans.steps.append.AppendIT;
import org.pentaho.di.trans.steps.blockingstep.BlockingStepIT;
import org.pentaho.di.trans.steps.combinationlookup.CombinationLookupIT;
import org.pentaho.di.trans.steps.constant.ConstantIT;
import org.pentaho.di.trans.steps.csvinput.CsvInput1IT;
import org.pentaho.di.trans.steps.csvinput.CsvInput2IT;
import org.pentaho.di.trans.steps.detectlastrow.DetectLastRowStepIT;
import org.pentaho.di.trans.steps.filterrows.FilterRowsIT;
import org.pentaho.di.trans.steps.gpload.GPLoadIT;
import org.pentaho.di.trans.steps.injector.InjectorIT;
import org.pentaho.di.trans.steps.nullif.NullIfIT;
import org.pentaho.di.trans.steps.regexeval.RegexEvalIT;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorIT;
import org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptSpecialIT;
import org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptStringIT;
import org.pentaho.di.trans.steps.sort.SortRowsIT;
import org.pentaho.di.trans.steps.tableinput.TableInputIT;
import org.pentaho.di.trans.steps.tableoutput.TableOutputIT;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorIT;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperIT;
import org.pentaho.di.trans.steps.webservices.WebServiceIT;

/**
 * Regression tests for the PDI framework.
 *
 * @author sboden
 */
public class AllRegressionTestsIT {
  public static Test suite() throws Exception {
    TestSuite suite = new TestSuite( "Run regression tests" );

    // The testcases should be executed from easy to hard. It
    // actually defines the debugging sequence if ever required.
    // If some of the suites fail you should start checking/debugging
    // the suites from the first that failed onwards.
    //
    // So adding testcases in the right order is important.
    //

    suite.addTest( new JUnit4TestAdapter( StringEvaluatorIT.class ) );
    suite.addTestSuite( ParameterSimpleTransIT.class );
//    suite.addTestSuite( ValueDataUtilTest.class );
    suite.addTest( new JUnit4TestAdapter( DatabaseIT.class ) );
//    suite.addTest( new JUnit4TestAdapter( ConnectionPoolUtilIntegrationIT.class ) );
    suite.addTestSuite( HopIT.class );
    suite.addTestSuite( InjectorIT.class );
    suite.addTestSuite( RowGeneratorIT.class );
    suite.addTestSuite( ConstantIT.class );
    suite.addTestSuite( AppendIT.class );
    suite.addTestSuite( DetectLastRowStepIT.class );
    suite.addTestSuite( BlockingStepIT.class );
    suite.addTest( new JUnit4TestAdapter( SortRowsIT.class ) );
    suite.addTest( new JUnit4TestAdapter( FilterRowsIT.class ) );
    suite.addTestSuite( ValueMapperIT.class );
    suite.addTestSuite( NullIfIT.class );
    suite.addTestSuite( RegexEvalIT.class );
    suite.addTestSuite( AddSequenceIT.class );
    suite.addTestSuite( TableInputIT.class );
    suite.addTestSuite( TableOutputIT.class );
    //        suite.addTestSuite(DatabaseLookupTest.class);    Now a JUnit 4 testcase
    suite.addTestSuite( CombinationLookupIT.class );
    suite.addTestSuite( JavaScriptStringIT.class );
    suite.addTestSuite( JavaScriptSpecialIT.class );
//    suite.addTestSuite( GetXMLDataTest.class );
    suite.addTestSuite( CsvInput1IT.class );
    suite.addTestSuite( CsvInput2IT.class );
    suite.addTestSuite( WebServiceIT.class );
    suite.addTest( new JUnit4TestAdapter( GPLoadIT.class ) );
    suite.addTest( new JUnit4TestAdapter( TransExecutorIT.class ) );

    // Temporarily disable this test, it never worked on Windows or Unix so
    // it doesn't make sense executing it for the moment.
    // suite.addTestSuite( BlackBoxTests.class );

    return suite;
  }
}
