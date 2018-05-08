/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.Const;
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

/**
 * Test class covers http://jira.pentaho.com/browse/PDI-15270 issue.
 * Csv data is taken from the attachment to the issue.
 *
 * Created by Yury_Bakhmutski on 10/7/2016.
 */
public class PDI_15270_Test extends CsvInputUnitTestBase {
  private CsvInput csvInput;
  private String[] expected;
  private String content;
  private String delimiter = ",";
  private String enclosure = "\"";
  private String encoding = "utf-8";

  @Before
  public void setUp() throws Exception {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "Y" );
    StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper = StepMockUtil
        .getStepMockHelper( CsvInputMeta.class, "Pdi15270Test" );
    csvInput = new CsvInput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
  }

  @Test
  public void noEnclosures() throws Exception {
    String field1 = "FIRST_NM";
    String field2 = "MIDDLE_NM";
    String field3 = "LAST_NM";
    content = field1 + delimiter + field2 + delimiter + field3;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void noEnclosuresWithEmptyFieldTest() throws Exception {
    String field1 = "Ima";
    String field2 = "";
    String field3 = "Rose";
    content = field1 + delimiter + field2 + delimiter + field3;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void withEnclosuresTest() throws Exception {
    String field1 = "Tom Tom";
    String field2 = "the";
    String field3 = "Piper's Son";
    content =
        enclosure + field1 + enclosure + delimiter + enclosure + field2 + enclosure + delimiter + enclosure + field3
            + enclosure;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void withEnclosuresOnOneFieldTest() throws Exception {
    String field1 = "Martin";
    String field2 = "Luther";
    String field3 = "King, Jr.";
    content = field1 + delimiter + field2 + delimiter + enclosure + field3 + enclosure;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void withEnclosuresInMiddleOfFieldTest() throws Exception {
    String field1 = "John \"Duke\"";
    String field2 = "";
    String field3 = "Wayne";
    content = field1 + delimiter + field2 + delimiter + field3;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  public void doTest( String content, String[] expected ) throws Exception {
    RowSet output = new QueueRowSet();

    File tmp = createTestFile( encoding, content );
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
    assertEquals( expected[0], row[0] );
    assertEquals( expected[1], row[1] );
    assertEquals( expected[2], row[2] );

    assertNull( output.getRowImmediate() );
  }

  private CsvInputMeta createMeta( File file, TextFileInputField[] fields ) {
    CsvInputMeta meta = new CsvInputMeta();
    meta.setFilename( file.getAbsolutePath() );
    meta.setDelimiter( delimiter );
    meta.setEncoding( encoding );
    meta.setEnclosure( enclosure );
    meta.setBufferSize( "1024" );
    meta.setInputFields( fields );
    meta.setHeaderPresent( false );
    return meta;
  }
}
