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
import org.junit.runner.RunWith;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

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
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class PDI_15270_Test extends CsvInputUnitTestBase {
  private CsvInput csvInput;
  private String[] expected;
  private String content;
  private StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUp() throws Exception {
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "Y" );
    stepMockHelper = StepMockUtil
        .getStepMockHelper( CsvInputMeta.class, "Pdi15270Test" );
    csvInput = new CsvInput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void noEnclosures() throws Exception {
    String field1 = "FIRST_NM";
    String field2 = "MIDDLE_NM";
    String field3 = "LAST_NM";
    content = field1 + DELIMITER + field2 + DELIMITER + field3;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void noEnclosuresWithEmptyFieldTest() throws Exception {
    String field1 = "Ima";
    String field2 = "";
    String field3 = "Rose";
    content = field1 + DELIMITER + field2 + DELIMITER + field3;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void withEnclosuresTest() throws Exception {
    String field1 = "Tom Tom";
    String field2 = "the";
    String field3 = "Piper's Son";
    content =
        ENCLOSURE + field1 + ENCLOSURE + DELIMITER + ENCLOSURE + field2 + ENCLOSURE + DELIMITER + ENCLOSURE + field3
            + ENCLOSURE;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void withEnclosuresOnOneFieldTest() throws Exception {
    String field1 = "Martin";
    String field2 = "Luther";
    String field3 = "King, Jr.";
    content = field1 + DELIMITER + field2 + DELIMITER + ENCLOSURE + field3 + ENCLOSURE;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  @Test
  public void withEnclosuresInMiddleOfFieldTest() throws Exception {
    String field1 = "John \"Duke\"";
    String field2 = "";
    String field3 = "Wayne";
    content = field1 + DELIMITER + field2 + DELIMITER + field3;
    expected = new String[] { field1, field2, field3 };
    doTest( content, expected );
  }

  public void doTest( String content, String[] expected ) throws Exception {
    RowSet output = new QueueRowSet();

    File tmp = createTestFile( ENCODING, content );
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
}
