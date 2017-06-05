/*! ****************************************************************************
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

package org.pentaho.di.trans.steps.csvinput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * Regression test case for PDI JIRA-1317 (case 2): a csv input step with more columns in certain rows than the number
 * of columns defined in the step.
 *
 * In the original problem (in v3.1-M2) this caused the filename column to be in the wrong places.
 *
 * @author Sven Boden
 */
public class CsvInput2Test extends CsvInputBase {

  /**
   * Write the file to be used as input (as a temporary file).
   *
   * @return Absolute file name/path of the created file.
   * @throws IOException
   *           UPON
   */
  public String writeInputFile() throws IOException {

    String rcode = null;

    File tempFile = File.createTempFile( "PDI_tmp", ".tmp" );
    tempFile.deleteOnExit();

    rcode = tempFile.getAbsolutePath();

    FileWriter fout = new FileWriter( tempFile );
    fout.write( "A;B;C;D;E\n" );
    fout.write( "1;b0;c0\n" );
    fout.write( "2;b1;c1;d1;e1\n" );
    fout.write( "3;b2;c2\n" );

    fout.close();

    return rcode;
  }

  public RowMetaInterface createResultRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaInteger( "a" ), new ValueMetaString( "b" ),
      new ValueMetaString( "c" ), new ValueMetaString( "filename" ), };

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
  @Override
  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface();

    Object[] r1 = new Object[] { new Long( 1L ), "b0", "c0", "fileName" };
    Object[] r2 = new Object[] { new Long( 2L ), "b1", "c1", "fileName" };
    Object[] r3 = new Object[] { new Long( 3L ), "b2", "c2", "fileName" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );

    return list;
  }

  /**
   * Test case for Get XML Data step, very simple example.
   *
   * @throws Exception
   *           Upon any exception
   */
  @Test
  public void testCSVInput1() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "csvinput1" );

    PluginRegistry registry = PluginRegistry.getInstance();

    String fileName = writeInputFile();

    StepMeta injectorStep = createInjectorStep( transMeta, registry );
    StepMeta csvInputStep = createCsvInputStep( transMeta, registry, "\"", true );

    createAndTestTrans(
      registry, transMeta, injectorStep, csvInputStep, fileName, createTextFileInputFields().length );
  }

  @Override
  protected TextFileInputField[] createTextFileInputFields() {
    TextFileInputField[] fields = new TextFileInputField[3];

    for ( int idx = 0; idx < fields.length; idx++ ) {
      fields[idx] = new TextFileInputField();
    }

    fields[0].setName( "a" );
    fields[0].setType( ValueMetaInterface.TYPE_INTEGER );
    fields[0].setFormat( "" );
    fields[0].setLength( -1 );
    fields[0].setPrecision( -1 );
    fields[0].setCurrencySymbol( "" );
    fields[0].setDecimalSymbol( "" );
    fields[0].setGroupSymbol( "" );
    fields[0].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    fields[1].setName( "b" );
    fields[1].setType( ValueMetaInterface.TYPE_STRING );
    fields[1].setFormat( "" );
    fields[1].setLength( -1 );
    fields[1].setPrecision( -1 );
    fields[1].setCurrencySymbol( "" );
    fields[1].setDecimalSymbol( "" );
    fields[1].setGroupSymbol( "" );
    fields[1].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    fields[2].setName( "c" );
    fields[2].setType( ValueMetaInterface.TYPE_STRING );
    fields[2].setFormat( "" );
    fields[2].setLength( -1 );
    fields[2].setPrecision( -1 );
    fields[2].setCurrencySymbol( "" );
    fields[2].setDecimalSymbol( "" );
    fields[2].setGroupSymbol( "" );
    fields[2].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    return fields;
  }
}
