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

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * Tests for unicode support in CsvInput step
 *
 * @author Pavel Sakun
 * @see CsvInput
 */
public class CsvInputUnicodeTest extends CsvInputUnitTestBase {
  private static final String UTF8 = "UTF-8";
  private static final String UTF16LE = "UTF-16LE";
  private static final String UTF16LEBOM = "x-UTF-16LE-BOM";
  private static final String UTF16BE = "UTF-16BE";
  private static final String ONE_CHAR_DELIM = "\t";
  private static final String MULTI_CHAR_DELIM = "|||";
  private static final String TEXT = "Header1%1$sHeader2\nValue%1$sValue\nValue%1$sValue\n";
  private static final String TEXT_WITH_ENCLOSURES = "Header1%1$sHeader2\n\"Value\"%1$s\"Value\"\n\"Value\"%1$s\"Value\"\n";
  private static final String TEST_DATA = String.format( TEXT, ONE_CHAR_DELIM );
  private static final String TEST_DATA1 = String.format( TEXT, MULTI_CHAR_DELIM );
  private static final String TEST_DATA2 = String.format( TEXT_WITH_ENCLOSURES, ONE_CHAR_DELIM );
  private static final String TEST_DATA3 = String.format( TEXT_WITH_ENCLOSURES, MULTI_CHAR_DELIM );

  private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
  private static final String TEST_DATA_UTF8_BOM =
      String.format( new String( UTF8_BOM, StandardCharsets.UTF_8 ) + TEXT, ONE_CHAR_DELIM );

  private static final byte[] UTF16LE_BOM = { (byte) 0xFF, (byte) 0xFE };
  private static final String TEST_DATA_UTF16LE_BOM =
      String.format( new String( UTF16LE_BOM, StandardCharsets.UTF_16LE ) + TEST_DATA2, ONE_CHAR_DELIM );

  private static final byte[] UTF16BE_BOM = { (byte) 0xFE, (byte) 0xFF };
  private static final String TEST_DATA_UTF16BE_BOM =
      String.format( new String( UTF16BE_BOM, StandardCharsets.UTF_16BE ) + TEST_DATA2, ONE_CHAR_DELIM );

  private static StepMockHelper<?, ?> stepMockHelper;

  @BeforeClass
  public static void setUp() throws KettleException {
    stepMockHelper =
      new StepMockHelper<CsvInputMeta, CsvInputData>( "CsvInputTest", CsvInputMeta.class, CsvInputData.class );
    Mockito.when( stepMockHelper.logChannelInterfaceFactory.create( Matchers.any(), Matchers.any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    Mockito.when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testUTF16LE() throws Exception {
    doTest( UTF16LE, UTF16LE, TEST_DATA, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16BE() throws Exception {
    doTest( UTF16BE, UTF16BE, TEST_DATA, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16BE_multiDelim() throws Exception {
    doTest( UTF16BE, UTF16BE, TEST_DATA1, MULTI_CHAR_DELIM );
  }

  @Test
  public void testUTF16LEBOM() throws Exception {
    doTest( UTF16LEBOM, UTF16LE, TEST_DATA, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF8() throws Exception {
    doTest( UTF8, UTF8, TEST_DATA, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF8_multiDelim() throws Exception {
    doTest( UTF8, UTF8, TEST_DATA1, MULTI_CHAR_DELIM );
  }

  @Test
  public void testUTF8_headerWithBOM() throws Exception {
    doTest( UTF8, UTF8, TEST_DATA_UTF8_BOM, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16LEDataWithEnclosures() throws Exception {
    doTest( UTF16LE, UTF16LE, TEST_DATA2, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16LE_headerWithBOM() throws Exception {
    doTest( UTF16LE, UTF16LE, TEST_DATA_UTF16LE_BOM, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16BEDataWithEnclosures() throws Exception {
    doTest( UTF16BE, UTF16BE, TEST_DATA2, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16BE_headerWithBOM() throws Exception {
    doTest( UTF16BE, UTF16BE, TEST_DATA_UTF16BE_BOM, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16LEBOMDataWithEnclosures() throws Exception {
    doTest( UTF16LEBOM, UTF16LE, TEST_DATA2, ONE_CHAR_DELIM );
  }

  @Test
  public void testUTF16BE_multiDelim_DataWithEnclosures() throws Exception {
    doTest( UTF16BE, UTF16BE, TEST_DATA3, MULTI_CHAR_DELIM );
  }

  @Test
  public void testUTF16LE_multiDelim_DataWithEnclosures() throws Exception {
    doTest( UTF16LE, UTF16LE, TEST_DATA3, MULTI_CHAR_DELIM );
  }

  @Test
  public void testUTF8_multiDelim_DataWithEnclosures() throws Exception {
    doTest( UTF8, UTF8, TEST_DATA3, MULTI_CHAR_DELIM );
  }

  private void doTest( final String fileEncoding, final String stepEncoding, final String testData,
    final String delimiter ) throws Exception {
    String testFilePath = createTestFile( fileEncoding, testData ).getAbsolutePath();

    CsvInputMeta meta = createStepMeta( testFilePath, stepEncoding, delimiter );
    CsvInputData data = new CsvInputData();

    CsvInput csvInput =
      new CsvInput(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );

    csvInput.init( meta, data );
    csvInput.addRowListener( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        for ( int i = 0; i < rowMeta.size(); i++ ) {
          Assert.assertEquals( "Value", row[ i ] );
        }
      }
    } );

    boolean haveRowsToRead;
    do {
      haveRowsToRead = !csvInput.processRow( meta, data );
    } while ( !haveRowsToRead );

    csvInput.dispose( meta, data );
    Assert.assertEquals( 2, csvInput.getLinesWritten() );
  }

  private CsvInputMeta createStepMeta( final String testFilePath, final String encoding, final String delimiter ) {
    final CsvInputMeta meta = new CsvInputMeta();
    meta.setFilename( testFilePath );
    meta.setDelimiter( delimiter );
    meta.setEncoding( encoding );
    meta.setEnclosure( "\"" );
    meta.setBufferSize( "50000" );
    meta.setInputFields( getInputFileFields() );
    meta.setHeaderPresent( true );
    return meta;
  }

  private TextFileInputField[] getInputFileFields() {
    return createInputFileFields( "Header1", "Header2" );
  }
}
