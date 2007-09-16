package org.pentaho.di;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.pentaho.di.blackbox.BlackBoxTests;
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
import org.pentaho.di.core.row.RowDataUtilTest;
import org.pentaho.di.core.row.RowTest;
import org.pentaho.di.core.row.ValueDataUtilTest;
import org.pentaho.di.core.row.ValueMetaTest;
import org.pentaho.di.trans.HopTest;
import org.pentaho.di.trans.steps.addsequence.AddSequenceTest;
import org.pentaho.di.trans.steps.append.AppendTest;
import org.pentaho.di.trans.steps.blockingstep.BlockingStepTest;
import org.pentaho.di.trans.steps.combinationlookup.CombinationLookupTest;
import org.pentaho.di.trans.steps.constant.ConstantTest;
import org.pentaho.di.trans.steps.injector.InjectorTest;
import org.pentaho.di.trans.steps.nullif.NullIfTest;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorTest;
import org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptSpecialTest;
import org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptStringTest;
import org.pentaho.di.trans.steps.sort.SortRowsTest;
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
        suite.addTestSuite(BlockingStepTest.class);
        suite.addTestSuite(SortRowsTest.class);
        suite.addTestSuite(ValueMapperTest.class);
        suite.addTestSuite(NullIfTest.class);
        suite.addTestSuite(AddSequenceTest.class);        
        suite.addTestSuite(CombinationLookupTest.class);
        suite.addTestSuite(JavaScriptStringTest.class);
        suite.addTestSuite(JavaScriptSpecialTest.class);

        suite.addTestSuite( BlackBoxTests.class );
        
        return suite;
    }
}