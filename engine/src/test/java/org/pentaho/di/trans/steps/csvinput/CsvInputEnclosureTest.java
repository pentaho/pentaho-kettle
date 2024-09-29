/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Andrey Khayrutdinov
 */
public class CsvInputEnclosureTest extends CsvInputUnitTestBase {
  private static final String QUOTATION_AND_EXCLAMATION_MARK = "\"!";
  private static final String QUOTATION_MARK = "\"";
  private static final String SEMICOLON = ";";
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private CsvInput csvInput;
  private StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper;

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
      StepMockUtil.getStepMockHelper( CsvInputMeta.class, "CsvInputEnclosureTest" );
    csvInput = new CsvInput(
      stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
      stepMockHelper.trans );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void hasEnclosures_HasNewLine() throws Exception {
    doTest( "\"value1\";\"value2\"\n", QUOTATION_MARK );
  }

  @Test
  public void hasEnclosures_HasNotNewLine() throws Exception {
    doTest( "\"value1\";\"value2\"", QUOTATION_MARK );
  }

  @Test
  public void hasNotEnclosures_HasNewLine() throws Exception {
    doTest( "value1;value2\n", QUOTATION_MARK );
  }

  @Test
  public void hasNotEnclosures_HasNotNewLine() throws Exception {
    doTest( "value1;value2", QUOTATION_MARK );
  }

  @Test
  public void hasMultiSymbolsEnclosureWithoutEnclosureAndEndFile() throws Exception {
    doTest( "value1;value2", QUOTATION_AND_EXCLAMATION_MARK );
  }

  @Test
  public void hasMultiSymbolsEnclosureWithEnclosureAndWithoutEndFile() throws Exception {
    doTest( "\"!value1\"!;value2", QUOTATION_AND_EXCLAMATION_MARK );
  }

  @Test
  public void hasMultiSymbolsEnclosurewithEnclosureInBothfield() throws Exception {
    doTest( "\"!value1\"!;\"!value2\"!", QUOTATION_AND_EXCLAMATION_MARK );
  }

  @Test
  public void hasMultiSymbolsEnclosureWithoutEnclosureAndWithEndfileRN() throws Exception {
    doTest( "value1;value2\r\n", QUOTATION_AND_EXCLAMATION_MARK );
  }

  @Test
  public void hasMultiSymbolsEnclosureWithEnclosureAndWithEndfileRN() throws Exception {
    doTest( "value1;\"!value2\"!\r\n", QUOTATION_AND_EXCLAMATION_MARK );
  }

  @Test
  public void hasMultiSymbolsEnclosureWithoutEnclosureAndWithEndfileN() throws Exception {
    doTest( "value1;value2\n", QUOTATION_AND_EXCLAMATION_MARK );
  }

  @Test
  public void hasMultiSymbolsEnclosureWithEnclosureAndWithEndfileN() throws Exception {
    doTest( "value1;\"!value2\"!\n", QUOTATION_AND_EXCLAMATION_MARK );
  }

  public void doTest( String content, String enclosure ) throws Exception {
    RowSet output = new QueueRowSet();

    File tmp = createTestFile( "utf-8", content );
    try {
      CsvInputMeta meta = createMeta( tmp, createInputFileFields( "f1", "f2" ), enclosure );
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

    assertNull( output.getRowImmediate() );
  }

  private CsvInputMeta createMeta( File file, TextFileInputField[] fields, String enclosure ) {
    CsvInputMeta meta = createMeta( file, fields );
    meta.setDelimiter( SEMICOLON );
    meta.setEnclosure( enclosure );
    return meta;
  }
}
