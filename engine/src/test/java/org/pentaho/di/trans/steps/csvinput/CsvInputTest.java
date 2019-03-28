/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Test;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CsvInputTest extends CsvInputUnitTestBase {

  private StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper;
  private LogChannelInterface logChannelInterface;
  private CsvInputMeta csvInputMeta;

  @Before
  public void setUp() throws Exception {
    logChannelInterface = mock( LogChannelInterface.class );
    stepMockHelper = StepMockUtil
      .getStepMockHelper( CsvInputMeta.class, "CsvInputTest" );
    csvInputMeta = mock( CsvInputMeta.class );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void guessStringsFromLineWithEmptyLine() throws Exception {
    // This only validates that, given a null 'line', a null is returned!
    String[] saData = CsvInput.guessStringsFromLine( logChannelInterface, null, csvInputMeta.getDelimiter(),
      csvInputMeta.getEnclosure(), csvInputMeta.getEscapeCharacter() );

    assertNull( saData );
  }

  // PDI-17831
  @Test
  public void testFileIsReleasedAfterProcessing() throws Exception {
    // Create a file with some content to be processed
    TextFileInputField[] inputFileFields = createInputFileFields( "f1", "f2", "f3" );
    String fileContents = "Something" + DELIMITER + "" + DELIMITER + "The former was empty!";
    File tmpFile = createTestFile( ENCODING, fileContents );

    // Create and configure the step
    CsvInputMeta meta = createMeta( tmpFile, inputFileFields );
    CsvInputData data = new CsvInputData();
    RowSet output = new QueueRowSet();
    CsvInput csvInput =
      new CsvInput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    csvInput.init( meta, data );
    csvInput.addRowSetToOutputRowSets( output );

    // Start processing
    csvInput.processRow( meta, data );

    // Finish processing
    csvInput.dispose( meta, data );

    // And now the file must be free to be deleted
    assertTrue( tmpFile.delete() );
    assertFalse( tmpFile.exists() );
  }
}
