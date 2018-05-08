/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CsvInputMultiCharDelimiterTest extends CsvInputUnitTestBase {

  private CsvInput csvInput;

  @Before
  public void setUp() throws Exception {
    StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper =
      StepMockUtil.getStepMockHelper( CsvInputMeta.class, "CsvInputEnclosureTest" );
    csvInput = new CsvInput(
      stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
      stepMockHelper.trans );
  }

  @Test
  public void multiChar_hasEnclosures_HasNewLine() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"\n" );
  }

  @Test
  public void multiChar_hasEnclosures_HasNewLineDoubleEnd() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"\r\n" );
  }

  @Test
  public void multiChar_hasEnclosures_HasNotNewLine() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNewLine() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3\n" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNewLineDoubleEnd() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3\r\n" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNotNewLine() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3" );
  }

  private void doTest( String content ) throws Exception {
    RowSet output = new QueueRowSet();

    File tmp = createTestFile( "utf-8", content );
    try {
      CsvInputMeta meta = createMeta( tmp, createInputFileFields( "f1", "f2", "f3" ) );
      CsvInputData data = new CsvInputData();
      csvInput.init( meta, data );

      csvInput.addRowSetToOutputRowSets( output );

      try {
        csvInput.processRow( meta, data );
      } finally {
        csvInput.dispose( meta, data );
      }

    } finally {
      tmp.delete();
    }

    Object[] row = output.getRowImmediate();
    assertNotNull( row );
    assertEquals( "value1", row[ 0 ] );
    assertEquals( "value2", row[ 1 ] );
    assertEquals( "value3", row[ 2 ] );

    assertNull( output.getRowImmediate() );
  }

  private CsvInputMeta createMeta( File file, TextFileInputField[] fields ) {
    CsvInputMeta meta = new CsvInputMeta();
    meta.setFilename( file.getAbsolutePath() );
    meta.setDelimiter( "delimiter" );
    meta.setEncoding( "utf-8" );
    meta.setEnclosure( "\"" );
    meta.setBufferSize( "1024" );
    meta.setInputFields( fields );
    meta.setHeaderPresent( false );
    return meta;
  }
}
