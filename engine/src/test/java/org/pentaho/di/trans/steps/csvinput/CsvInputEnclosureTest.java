/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
