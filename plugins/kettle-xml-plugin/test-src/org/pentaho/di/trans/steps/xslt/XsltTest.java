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

package org.pentaho.di.trans.steps.xslt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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

public class XsltTest extends TestCase {

  private static final String TEST1_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message>Yep, it worked!</message>";

  private static final String TEST1_XSL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<xsl:stylesheet version = \"1.0\" xmlns:xsl = \"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:output method = \"text\" encoding = \"UTF-8\"/>" + "<!--simply copy the message to the result tree -->"
      + "<xsl:template match = \"/\">" + "<xsl:value-of select = \"message\"/>" + "</xsl:template>"
      + "</xsl:stylesheet>";

  private static final String TEST1_FNAME = "template.xsl";

  /**
   * Write the file to be used as input (as a temporary file).
   * 
   * @return Absolute file name/path of the created file.
   * @throws IOException
   *           UPON
   */
  public String writeInputFile() throws IOException {

    String rcode = null;

    File tempFile = File.createTempFile( "template", ".xsl" );
    tempFile.deleteOnExit();

    rcode = tempFile.getAbsolutePath();

    FileWriter fout = new FileWriter( tempFile );
    fout.write( TEST1_XSL );
    fout.close();

    return rcode;
  }

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
        { new ValueMeta( "XML", ValueMeta.TYPE_STRING ), new ValueMeta( "XSL", ValueMeta.TYPE_STRING ),
          new ValueMeta( "filename", ValueMeta.TYPE_STRING ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createData( String fileName ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { TEST1_XML, TEST1_XSL, fileName };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public RowMetaInterface createResultRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
        { new ValueMeta( "XML", ValueMeta.TYPE_STRING ), new ValueMeta( "XSL", ValueMeta.TYPE_STRING ),
          new ValueMeta( "filename", ValueMeta.TYPE_STRING ), new ValueMeta( "result", ValueMeta.TYPE_STRING ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  /**
   * Create result data for test case 1.
   * 
   * @return list of metadata/data couples of how the result should look like.
   */
  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface();

    Object[] r1 = new Object[] { TEST1_XML, TEST1_XSL, TEST1_FNAME, "Yep, it worked!" };

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

  /**
   * Test case for XSLT step, getting the filename from a field, JAXP factory
   * 
   * @throws Exception
   *           Upon any exception
   */
  public void testXslt1() throws Exception {

    String fileName = writeInputFile();
    runTestWithParams( "XML", "result", true, true, "filename", fileName, "JAXP" );
  }

  /**
   * Test case for XSLT step, getting the filename from a field, SAXON factory
   * 
   * @throws Exception
   *           Upon any exception
   */
  public void testXslt2() throws Exception {

    String fileName = writeInputFile();
    runTestWithParams( "XML", "result", true, true, "filename", fileName, "SAXON" );
  }

  /**
   * Test case for XSLT step, getting the XSL from a field, JAXP factory
   * 
   * @throws Exception
   *           Upon any exception
   */
  public void testXslt3() throws Exception {
    runTestWithParams( "XML", "result", true, false, "XSL", "", "JAXP" );
  }

  /**
   * Test case for XSLT step, getting the XSL from a field, SAXON factory
   * 
   * @throws Exception
   *           Upon any exception
   */
  public void testXslt4() throws Exception {
    runTestWithParams( "XML", "result", true, false, "XSL", "", "SAXON" );
  }

  /**
   * Test case for XSLT step, getting the XSL from a file, JAXP factory
   * 
   * @throws Exception
   *           Upon any exception
   */
  public void testXslt5() throws Exception {
    String fileName = writeInputFile();
    runTestWithParams( "XML", "result", false, false, "filename", fileName, "JAXP" );
  }

  /**
   * Test case for XSLT step, getting the XSL from a file, SAXON factory
   * 
   * @throws Exception
   *           Upon any exception
   */
  public void testXslt6() throws Exception {
    String fileName = writeInputFile();
    runTestWithParams( "XML", "result", false, false, "filename", fileName, "SAXON" );
  }

  public void runTestWithParams( String xmlFieldname, String resultFieldname, boolean xslInField,
      boolean xslFileInField, String xslFileField, String xslFilename, String xslFactory ) throws Exception {

    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "xslt" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a XSLT step
    //
    String xsltName = "xslt step";
    XsltMeta xm = new XsltMeta();

    String xsltPid = registry.getPluginId( StepPluginType.class, xm );
    StepMeta xsltStep = new StepMeta( xsltPid, xsltName, xm );
    transMeta.addStep( xsltStep );

    TextFileInputField[] fields = new TextFileInputField[3];

    for ( int idx = 0; idx < fields.length; idx++ ) {
      fields[idx] = new TextFileInputField();
    }

    fields[0].setName( "XML" );
    fields[0].setType( ValueMetaInterface.TYPE_STRING );
    fields[0].setFormat( "" );
    fields[0].setLength( -1 );
    fields[0].setPrecision( -1 );
    fields[0].setCurrencySymbol( "" );
    fields[0].setDecimalSymbol( "" );
    fields[0].setGroupSymbol( "" );
    fields[0].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    fields[1].setName( "XSL" );
    fields[1].setType( ValueMetaInterface.TYPE_STRING );
    fields[1].setFormat( "" );
    fields[1].setLength( -1 );
    fields[1].setPrecision( -1 );
    fields[1].setCurrencySymbol( "" );
    fields[1].setDecimalSymbol( "" );
    fields[1].setGroupSymbol( "" );
    fields[1].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    fields[2].setName( "filename" );
    fields[2].setType( ValueMetaInterface.TYPE_STRING );
    fields[2].setFormat( "" );
    fields[2].setLength( -1 );
    fields[2].setPrecision( -1 );
    fields[2].setCurrencySymbol( "" );
    fields[2].setDecimalSymbol( "" );
    fields[2].setGroupSymbol( "" );
    fields[2].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    xm.setFieldname( xmlFieldname );
    xm.setResultfieldname( resultFieldname );
    xm.setXSLField( xslInField );
    xm.setXSLFileField( xslFileField );
    xm.setXSLFieldIsAFile( xslFileInField );
    xm.setXslFilename( xslFilename );
    xm.setXSLFactory( xslFactory );

    TransHopMeta hi = new TransHopMeta( injectorStep, xsltStep );
    transMeta.addTransHop( hi );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( xsltStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector dummyRc1 = new RowStepCollector();
    si.addRowListener( dummyRc1 );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData( xslFilename );
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

    checkRows( goldenImageRows, resultRows, 2 );
  }

}
