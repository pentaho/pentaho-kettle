/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.pentaho.di.compatibility.ValueBigNumberTest;
import org.pentaho.di.compatibility.ValueBooleanTest;
import org.pentaho.di.compatibility.ValueDateTest;
import org.pentaho.di.compatibility.ValueIntegerTest;
import org.pentaho.di.compatibility.ValueNumberTest;
import org.pentaho.di.compatibility.ValueStringTest;
import org.pentaho.di.compatibility.ValueTest;
import org.pentaho.di.core.ConstTest;
import org.pentaho.di.core.CounterTest;
import org.pentaho.di.core.CountersTest;
import org.pentaho.di.core.RowSetTest;
import org.pentaho.di.core.database.DatabaseTest;
import org.pentaho.di.core.encryption.EncrTest;
import org.pentaho.di.core.parameters.ParameterSimpleTransTest;
import org.pentaho.di.core.row.RowDataUtilTest;
import org.pentaho.di.core.row.RowTest;
import org.pentaho.di.core.row.ValueDataUtilTest;
import org.pentaho.di.core.row.ValueMetaTest;
import org.pentaho.di.core.util.StringUtilTest;
import org.pentaho.di.trans.HopTest;
import org.pentaho.di.trans.steps.addsequence.AddSequenceTest;
import org.pentaho.di.trans.steps.append.AppendTest;
import org.pentaho.di.trans.steps.blockingstep.BlockingStepTest;
import org.pentaho.di.trans.steps.combinationlookup.CombinationLookupTest;
import org.pentaho.di.trans.steps.constant.ConstantTest;
import org.pentaho.di.trans.steps.csvinput.CsvInput1Test;
import org.pentaho.di.trans.steps.csvinput.CsvInput2Test;
import org.pentaho.di.trans.steps.detectlastrow.DetectLastRowStepTest;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataTest;
import org.pentaho.di.trans.steps.injector.InjectorTest;
import org.pentaho.di.trans.steps.nullif.NullIfTest;
import org.pentaho.di.trans.steps.regexeval.RegexEvalTest;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorTest;
import org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptSpecialTest;
import org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptStringTest;
import org.pentaho.di.trans.steps.sort.SortRowsTest;
import org.pentaho.di.trans.steps.tableinput.TableInputTest;
import org.pentaho.di.trans.steps.tableoutput.TableOutputTest;
import org.pentaho.di.trans.steps.valuemapper.ValueMapperTest;


/**
 * Regression tests for the PDI framework.
 * 
 * @author sboden
 */
public class AllRegressionTests
{   
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite("Run regression tests");

        // The testcases should be executed from easy to hard. It
        // actually defines the debugging sequence if ever required.
        // If some of the suites fail you should start checking/debugging
        // the suites from the first that failed onwards.
        //
        // So adding testcases in the right order is important.
        //
        
        suite.addTestSuite(ValueStringTest.class);
        suite.addTestSuite(ValueIntegerTest.class);
        suite.addTestSuite(ValueNumberTest.class);
        suite.addTestSuite(ValueBigNumberTest.class);
        suite.addTestSuite(ValueDateTest.class);
        suite.addTestSuite(ValueBooleanTest.class);
        suite.addTestSuite(ValueTest.class);
        suite.addTestSuite(ConstTest.class);
        suite.addTestSuite(StringUtilTest.class);
        suite.addTestSuite(EncrTest.class);
        suite.addTestSuite(ParameterSimpleTransTest.class);
        
        suite.addTestSuite(ValueMetaTest.class);
        suite.addTestSuite(RowDataUtilTest.class);
        suite.addTestSuite(ValueDataUtilTest.class);
        suite.addTestSuite(DatabaseTest.class);
        suite.addTestSuite(RowTest.class);
        suite.addTestSuite(RowSetTest.class);
        suite.addTestSuite(HopTest.class);        
        suite.addTestSuite(CounterTest.class);
        suite.addTestSuite(CountersTest.class);
        
        suite.addTestSuite(InjectorTest.class);
        suite.addTestSuite(RowGeneratorTest.class);
        suite.addTestSuite(ConstantTest.class);
        suite.addTestSuite(AppendTest.class);   
        suite.addTestSuite(DetectLastRowStepTest.class);        
        suite.addTestSuite(BlockingStepTest.class);
        suite.addTestSuite(SortRowsTest.class);
        suite.addTestSuite(ValueMapperTest.class);
        suite.addTestSuite(NullIfTest.class);
        suite.addTestSuite(RegexEvalTest.class);
        suite.addTestSuite(AddSequenceTest.class);  
        suite.addTestSuite(TableInputTest.class);        
        suite.addTestSuite(TableOutputTest.class);
//        suite.addTestSuite(DatabaseLookupTest.class);    Now a JUnit 4 testcase
        suite.addTestSuite(CombinationLookupTest.class);
        suite.addTestSuite(JavaScriptStringTest.class);
        suite.addTestSuite(JavaScriptSpecialTest.class);
        suite.addTestSuite(GetXMLDataTest.class);
        suite.addTestSuite(CsvInput1Test.class);
        suite.addTestSuite(CsvInput2Test.class);

        // Temporarily disable this test, it never worked on Windows or Unix so
        // it doesn't make sense executing it for the moment.
        // suite.addTestSuite( BlackBoxTests.class );
        
        return suite;
    }
}