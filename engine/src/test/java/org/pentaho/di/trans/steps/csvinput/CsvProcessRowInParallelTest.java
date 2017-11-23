/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Test;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;
import static org.junit.Assert.assertEquals;

/**
 *  We take file with content
 *  and run it parallel with several steps.
 *  see docs for {@link CsvInput#prepareToRunInParallel} to understand how running file in parallel works
 *
 *  We measure the correctness of work by counting the number of lines, written on each step.
 *  As a result, we should come to this pseudo formula: numberOfLines = sum of number of lines written by each step.
 *
 *  Just a simple example:
 *  Assume, we have file with this content:
 *
 *  a,b\r\n
 *  c,d\r\n
 *
 *  If we will run it with 2 steps, we expect the first step to read 1st line, and the second step to read second line.
 *
 *  Every test is built in this pattern.
 *
 *  We actually play with 4 things:
 *  - file content
 *  - number of threads (it's actually same as number of steps)
 *  - representation of new line (it can be 2 bytes: '\r\n' (windows) or 1 byte: '\r' or '\n' (Mac, Linux) .
 *  Representation can differ. So, if we have different types of new lines in one file - it's ok.
 *  - file ends with new line or not
 */
public class CsvProcessRowInParallelTest extends CsvInputUnitTestBase {


  @Test
  public void oneByteNewLineIndicator_NewLineAtTheEnd_2Threads() throws Exception {
    final int totalNumberOfSteps = 2;
    final String fileContent =
          "a;1\r"
        + "b;2\r";

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 1 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 1 );
  }

  @Test
  public void oneByteNewLineIndicator_NoNewLineAtTheEnd_2Threads() throws Exception {
    final int totalNumberOfSteps = 2;

    final String fileContent =
            "a;1\r"
          + "b;2\r"
          + "c;3";

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 2 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 1 );
  }

  @Test
  public void PDI_15162_mixedByteNewLineIndicator_NewLineAtTheEnd_2Threads() throws Exception {
    final int totalNumberOfSteps = 2;

    final String fileContent =
          "ab;111\r\n"
        + "bc;222\r\n"
        + "cd;333\r\n"
        + "de;444\r\n"
        + "ef;555\r"
        + "fg;666\r\n"
        + "gh;777\r\n"
        + "hi;888\r\n"
        + "ij;999\r"
        + "jk;000\r";

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 5 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 5 );
  }

  @Test
  public void PDI_15162_mixedByteNewLineIndicator_NoNewLineAtTheEnd_2Threads() throws Exception {
    final int totalNumberOfSteps = 2;

    final String fileContent =
          "ab;111\r\n"
        + "bc;222\r\n"
        + "cd;333\r\n"
        + "de;444\r\n"
        + "ef;555\r"
        + "fg;666\r\n"
        + "gh;777\r\n"
        + "hi;888\r\n"
        + "ij;999\r"
        + "jk;000";

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 5 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 5 );
  }


  @Test
  public void twoByteNewLineIndicator_NewLineAtTheEnd_2Threads() throws Exception {
    final String fileContent =
            "a;1\r\n"
          + "b;2\r\n";
    final int totalNumberOfSteps = 2;

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 1 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 1 );
  }

  @Test
  public void twoByteNewLineIndicator_NoNewLineAtTheEnd_2Threads() throws Exception {
    final String fileContent =
            "a;1\r\n"
          + "b;2";
    final int totalNumberOfSteps = 2;

    File sharedFile = createTestFile( "UTF-8", fileContent );

    int t1 = createAndRunOneStep( sharedFile, 0, totalNumberOfSteps );
    int t2 = createAndRunOneStep( sharedFile, 1, totalNumberOfSteps );

    assertEquals( 2, t1 + t2 );
  }


  @Test
  public void twoByteNewLineIndicator_NewLineAtTheEnd_3Threads() throws Exception {
    final String fileContent =
            "a;1\r\n"
          + "b;2\r\n"
        // thread 1 should read until this line
          + "c;3\r\n"
          + "d;4\r\n"
        // thread 2 should read until this line
          + "e;5\r\n"
          + "f;6\r\n";
        // thread 3 should read until this line


    final int totalNumberOfSteps = 3;

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 2 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 2 );
    assertEquals( createAndRunOneStep( sharedFile, 2, totalNumberOfSteps ), 2 );
  }

  /**
   * Here files content is 16 bytes summary, where 8 of this bytes is the first line, 5 is the second one, 3 is the
   * last.
   * <p>
   * As we are running this with 2 threads, we expect: 1st thread to read 1st line 2nd thread to read 2nd and 3d line.
   */
  @Test
  public void mixedBytesNewLineIndicator_NoNewLineAtTheEnd_2Threads() throws Exception {
    final String fileContent =
            "abcd;1\r\n"
          + "b;2\r\n"
          + "d;3";


    final int totalNumberOfSteps = 2;

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 1 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 2 );
  }

  @Test
  public void mixedBytesNewLineIndicator_NewLineAtTheEnd_2Threads() throws Exception {
    final String fileContent =
            "abcd;1\r\n"
          + "b;2\r"
          + "d;3\r";


    final int totalNumberOfSteps = 2;

    File sharedFile = createTestFile( "UTF-8", fileContent );

    assertEquals( createAndRunOneStep( sharedFile, 0, totalNumberOfSteps ), 1 );
    assertEquals( createAndRunOneStep( sharedFile, 1, totalNumberOfSteps ), 2 );
  }

  @Test
  public void PDI_16589_twoByteNewLineIndicator_withHeaders_NewLineAtTheEnd_4Threads() throws Exception {
    final int totalNumberOfSteps = 4;

    final String fileContent =
      "Col1,Col2\r\n"
        + "a,1\r\n"
        + "b,2\r\n"
        + "c,3\r\n"
        + "d,4\r\n"
        + "e,5\r\n"
        + "f,6\r\n"
        + "g,7\r\n"
        + "h,8\r\n"
        + "i,9\r\n"
        + "jk,10\r\n"
        + "lm,11\r\n";

    File sharedFile = createTestFile( "UTF-8", fileContent );

    int t1 = createAndRunOneStep( sharedFile, 0, totalNumberOfSteps, true, "," );
    int t2 = createAndRunOneStep( sharedFile, 1, totalNumberOfSteps, true, "," );
    int t3 = createAndRunOneStep( sharedFile, 2, totalNumberOfSteps, true, "," );
    int t4 = createAndRunOneStep( sharedFile, 3, totalNumberOfSteps, true, "," );

    assertEquals( 11, t1 + t2 + t3 + t4 );
  }

  /**
   * So as not to heap up list of taken parameters, we are passing combi, but we expect to see CsvInput class instances
   * in it's content.
   */
  private int processRows( StepMetaDataCombi combi ) throws Exception {

    CsvInput csvInput = (CsvInput) combi.step;
    CsvInputData stepData = (CsvInputData) combi.data;
    CsvInputMeta stepMeta = (CsvInputMeta) combi.meta;

    final int[] writtenRows = { 0 };

    csvInput.addRowListener( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        writtenRows[ 0 ]++;
      }
    } );

    boolean haveRowsToRead;
    do {
      haveRowsToRead = !csvInput.processRow( stepMeta, stepData );
    } while ( !haveRowsToRead );

    csvInput.dispose( stepMeta, stepData );

    return writtenRows[ 0 ];
  }

  private CsvInput createCsvInput() {
    StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper =
      StepMockUtil.getStepMockHelper( CsvInputMeta.class, "CsvInputEnclosureTest" );

    return new CsvInput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
      stepMockHelper.transMeta, stepMockHelper.trans );
  }


  private int createAndRunOneStep( File sharedFile, int stepNr, int totalNumberOfSteps )
    throws Exception {
    return createAndRunOneStep( sharedFile, stepNr, totalNumberOfSteps, false, ";" );
  }

  private int createAndRunOneStep( File sharedFile, int stepNr, int totalNumberOfSteps, boolean headersPresent, String delimiter )
    throws Exception {
    StepMetaDataCombi combiStep1 = createBaseCombi( sharedFile, headersPresent, delimiter );
    configureData( (CsvInputData) combiStep1.data, stepNr, totalNumberOfSteps );

    return processRows( combiStep1 );
  }

  private StepMetaDataCombi createBaseCombi( File sharedFile, boolean headerPresent, String delimiter ) {

    StepMetaDataCombi combi = new StepMetaDataCombi();

    CsvInputData data = new CsvInputData();
    CsvInputMeta meta = createMeta( sharedFile, createInputFileFields( "Field_000", "Field_001" ), headerPresent, delimiter );

    CsvInput csvInput = createCsvInput();
    csvInput.init( meta, data );

    combi.step = csvInput;
    combi.data = data;
    combi.meta = meta;

    return combi;
  }

  private CsvInputMeta createMeta( File file, TextFileInputField[] fields, boolean headerPresent, String delimiter ) {
    CsvInputMeta meta = new CsvInputMeta();

    meta.setFilename( file.getAbsolutePath() );
    meta.setDelimiter( delimiter );
    meta.setEncoding( "utf-8" );
    meta.setEnclosure( "\"" );
    meta.setBufferSize( "1024" );

    if ( !headerPresent ) {
      meta.setInputFields( fields );
    }

    meta.setHeaderPresent( headerPresent );
    meta.setRunningInParallel( true );

    return meta;
  }

  private void configureData( CsvInputData data, int currentStepNr, int totalNumberOfSteps ) {
    data.parallel = true;
    data.stepNumber = currentStepNr;
    data.totalNumberOfSteps = totalNumberOfSteps;
  }
}
