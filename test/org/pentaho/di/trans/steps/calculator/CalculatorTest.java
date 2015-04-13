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

package org.pentaho.di.trans.steps.calculator;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

public class CalculatorTest extends TestCase {

  public RowMetaInterface createResultRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();
    try {
      ValueMetaInterface[] valuesMeta = {
        ValueMetaFactory.createValueMeta( "timestamp1", ValueMetaInterface.TYPE_TIMESTAMP ),
        ValueMetaFactory.createValueMeta( "int1", ValueMetaInterface.TYPE_INTEGER ),
        ValueMetaFactory.createValueMeta( "timestamp plus 1 day", ValueMetaInterface.TYPE_DATE )
      };
      for ( int i = 0; i < valuesMeta.length; i++ ) {
        rm.addValueMeta( valuesMeta[i] );
      }
    } catch ( Exception ex ) {
      return null;
    }
    return rm;
  }

  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface();

    Date date = null;
    try {
      date = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" ).parse( "1970-01-02 00:00:00.100" );
    } catch ( Exception ex ) {
      throw new IllegalArgumentException( ex );
    }
    Object[] r1 = new Object[] {
      Timestamp.valueOf( "1970-01-01 00:00:00.100100" ),
      new Long( 1 ),
      date
    };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  /**
   *  Check the 2 lists comparing the rows in order.
   *  If they are not the same fail the test. 
   */
  public void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2 ) {
    int idx = 1;
    if ( rows1.size() != rows2.size() ) {
      fail( "Number of rows is not the same: " + rows1.size() + " and " + rows2.size() );
    }
    Iterator<RowMetaAndData> it1 = rows1.iterator();
    Iterator<RowMetaAndData> it2 = rows2.iterator();

    while ( it1.hasNext() && it2.hasNext() ) {
      RowMetaAndData rm1 = it1.next();
      RowMetaAndData rm2 = it2.next();

      Object[] r1 = rm1.getData();
      Object[] r2 = rm2.getData();

      if ( rm1.size() != rm2.size() ) {
        fail( "row size of row at " + idx + " is not equal (" + rm1.size() + "," + rm2.size() + ")" );
      }
      int[] fields = new int[1];
      for ( int ydx = 0; ydx < rm1.size(); ydx++ ) {
        fields[0] = ydx;
        try {
          if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
            fail( "row nr " + idx + " is not equal at field nr "
              + ydx + "(" + rm1.toString() + ";" + rm2.toString() + ")" );
          }
        } catch ( KettleValueException e ) {
          fail( "row nr " + idx + " is not equal at field nr "
            + ydx + "(" + rm1.toString() + ";" + rm2.toString() + ")" );
        }
      }

      idx++;
    }
  }

  public void testCalculator1() throws Exception {
    KettleEnvironment.init();

    PluginRegistry registry = PluginRegistry.getInstance();
    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "calculatortest1" );

    // 
    // create a row generator step...
    //
    String rowGeneratorStepname = "row generator step";
    RowGeneratorMeta rm = new RowGeneratorMeta();

    // Set the information of the row generator.                
    String rowGeneratorPid = registry.getPluginId( StepPluginType.class, rm );
    StepMeta rowGeneratorStep = new StepMeta( rowGeneratorPid, rowGeneratorStepname, rm );
    transMeta.addStep( rowGeneratorStep );

    //
    // Generate 1 empty row
    //
    String[] strDummies = {};
    int[] intDummies = {};

    rm.setDefault();
    rm.setFieldName( strDummies );
    rm.setFieldType( strDummies );
    rm.setValue( strDummies );
    rm.setFieldLength( intDummies );
    rm.setFieldPrecision( intDummies );
    rm.setRowLimit( "1" );
    rm.setFieldFormat( strDummies );
    rm.setGroup( strDummies );
    rm.setDecimal( strDummies );

    // 
    // Add calculator step.
    //
    String calculatorStepname1 = "calculator 1";
    CalculatorMeta calc1 = new CalculatorMeta();

    CalculatorMetaFunction[] calculations = new CalculatorMetaFunction[] {
      new CalculatorMetaFunction(
        "timestamp1", //fieldName
        CalculatorMetaFunction.CALC_CONSTANT, //calctype
        "1970-01-01 00:00:00.100100", //fieldA
        "", //String fieldB
        "", //String fieldC
        ValueMetaInterface.TYPE_TIMESTAMP, //valueType, 
        0, //int valueLength, 
        0, //int valuePrecision, 
        false, //boolean removedFromResult, 
        "", //String conversionMask,
        "", // String decimalSymbol, 
        "", //String groupingSymbol, 
        "" //String currencySymbol
      ),
      new CalculatorMetaFunction(
        "int1", //fieldName
        CalculatorMetaFunction.CALC_CONSTANT, //calctype
        "1", //fieldA
        "", //String fieldB
        "", //String fieldC
        ValueMetaInterface.TYPE_INTEGER, //valueType, 
        0, //int valueLength, 
        0, //int valuePrecision, 
        false, //boolean removedFromResult, 
        "", //String conversionMask,
        "", // String decimalSymbol, 
        "", //String groupingSymbol, 
        "" //String currencySymbol
      ),
      new CalculatorMetaFunction(
        "timestamp plus 1 day", //fieldName
        CalculatorMetaFunction.CALC_ADD_DAYS, //calctype
        "timestamp1", //fieldA
        "int1", //String fieldB
        "", //String fieldC
        ValueMetaInterface.TYPE_DATE, //valueType, 
        0, //int valueLength, 
        0, //int valuePrecision, 
        false, //boolean removedFromResult, 
        "", //String conversionMask,
        "", // String decimalSymbol, 
        "", //String groupingSymbol, 
        "" //String currencySymbol
      )
    };
    calc1.setCalculation( calculations );
    //
    String calculatorPid1 = registry.getPluginId( StepPluginType.class, calc1 );
    StepMeta calcualtorStep1 = new StepMeta( calculatorPid1, calculatorStepname1, calc1 );
    transMeta.addStep( calcualtorStep1 );

    //
    TransHopMeta hi1 = new TransHopMeta( rowGeneratorStep, calcualtorStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( calculatorStepname1, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();
    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData1();
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }

}
