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

package org.pentaho.di.trans.steps.csvinput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import junit.framework.TestCase;

public abstract class CsvInputBase extends TestCase {

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaString( "filename" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createData( String fileName ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { fileName };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
   *
   * @param rows1
   *          set 1 of rows to compare
   * @param rows2
   *          set 2 of rows to compare
   * @param fileNameColumn
   *          Number of the column containing the filename. This is only checked for being non-null (some systems maybe
   *          canonize names differently than we input).
   */
  public void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2, int fileNameColumn ) {
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
        fail( "row nr " + idx + " is not equal" );
      }
      int[] fields = new int[r1.length];
      for ( int ydx = 0; ydx < r1.length; ydx++ ) {
        fields[ydx] = ydx;
      }
      try {
        r1[fileNameColumn] = r2[fileNameColumn];
        if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
          fail( "row nr " + idx + " is not equal" );
        }
      } catch ( KettleValueException e ) {
        fail( "row nr " + idx + " is not equal" );
      }

      idx++;
    }
  }

  protected Trans createAndTestTrans( PluginRegistry registry, TransMeta transMeta, StepMeta injectorStep,
    StepMeta csvInputStep, String fileName, int numRows ) throws KettleException {
    TransHopMeta hi = new TransHopMeta( injectorStep, csvInputStep );
    transMeta.addTransHop( hi );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( csvInputStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector dummyRc1 = new RowStepCollector();
    si.addRowListener( dummyRc1 );

    RowProducer rp = trans.addRowProducer( injectorStep.getName(), 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData( fileName );
    Iterator<RowMetaAndData> it = inputList.iterator();
    while ( it.hasNext() ) {
      RowMetaAndData rm = it.next();
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = dummyRc1.getRowsWritten();
    List<RowMetaAndData> goldenImageRows = createResultData1();

    checkRows( goldenImageRows, resultRows, numRows );

    return trans;
  }

  public StepMeta createCsvInputStep( TransMeta transMeta, PluginRegistry registry, String enclosure,
    boolean headerPresent ) {
    //
    // Create a Csv Input step
    //
    String csvInputName = "csv input step";
    CsvInputMeta cim = new CsvInputMeta();

    String csvInputPid = registry.getPluginId( StepPluginType.class, cim );
    StepMeta csvInputStep = new StepMeta( csvInputPid, csvInputName, cim );
    transMeta.addStep( csvInputStep );

    TextFileInputField[] fields = createTextFileInputFields();

    cim.setIncludingFilename( true );
    cim.setFilename( "" );
    cim.setFilenameField( "filename" );
    cim.setDelimiter( ";" );
    cim.setEnclosure( enclosure );
    cim.setBufferSize( "50000" );
    cim.setLazyConversionActive( false );
    cim.setHeaderPresent( headerPresent );
    cim.setAddResultFile( false );
    cim.setIncludingFilename( true );
    cim.setRowNumField( "" );
    cim.setRunningInParallel( false );
    cim.setInputFields( fields );

    return csvInputStep;
  }

  public StepMeta createInjectorStep( TransMeta transMeta, PluginRegistry registry ) {
    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    return injectorStep;
  }

  public abstract List<RowMetaAndData> createResultData1();

  protected abstract TextFileInputField[] createTextFileInputFields();
}
