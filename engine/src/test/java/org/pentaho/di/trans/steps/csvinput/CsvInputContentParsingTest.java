/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.csvinput;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvInputContentParsingTest extends BaseCsvParsingTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testDefaultOptions() throws Exception {
    init( "default.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" } } );
  }

  @Test
  public void testColumnNameWithSpaces() throws Exception {
    init( "column_name_with_spaces.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" } } );
  }

  @Test
  public void testSemicolonOptions() throws Exception {
    meta.setDelimiter( ";" );
    init( "semicolon.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" }, {
        "\u043d\u0435-\u043b\u0430\u0446\u0456\u043d\u043a\u0430(non-latin)", "4", "4" } } );
  }

  @Test
  public void testMultiCharDelimOptions() throws Exception {
    meta.setDelimiter( "|||" );
    init( "multi_delim.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" }, {
        "\u043d\u0435-\u043b\u0430\u0446\u0456\u043d\u043a\u0430(non-latin)", "4", "4" } } );
  }

  @Test
  public void testMixFileFormat() throws Exception {
    String data = "データ1,データ2,データ3,データ4\n"
      + "111,\"a\n"
      + "bc\",あいう,さしす\n"
      + "222,def,かきく,たちつ\r\n"
      + "333,,かきく,たちつ\n"
      + "444,,\n"
      + "555,かきく,\r\n"
      + "666,かきく\r\n"
      + "\n"
      + "777,\n"
      + "888,かきく\r\n"
      + "\n"
      + "999,123,123,123,132,132,132,132,132\r";

    String file = createTestFile( "UTF-8", data ).getAbsolutePath();
    meta.setFileFormat( "mixed" );
    init( file, true );

    setFields( new TextFileInputField( "Col 1", -1, -1 ), new TextFileInputField( "Col 2", -1, -1 ),
      new TextFileInputField( "Col 3", -1, -1 ), new TextFileInputField( "Col 4", -1, -1 ) );

    process();

    check( new Object[][] {
      { "111", "a\nbc", "あいう", "さしす" },
      { "222", "def", "かきく", "たちつ" },
      { "333", "", "かきく", "たちつ" },
      { "444", "", "", null },
      { "555", "かきく", "", null },
      { "666", "かきく", null, null },
      { },
      { "777", "", null, null },
      { "888", "かきく", null, null },
      { },
      { "999", "123", "123", "123" } }
    );
  }

  @Test
  public void testDosFileFormat() throws Exception {
    String data = "データ1,データ2,データ3,データ4\r\n"
      + "111,\"a\r\n"
      + "bc\",あいう,さしす\r\n"
      + "222,def,かきく,たちつ\r\n"
      + "333,,かきく,たちつ\r\n"
      + "444,,\r\n"
      + "555,かきく,\r\n"
      + "666,かきく\r\n"
      + "\r\n"
      + "777,\r\n"
      + "888,かきく\r\n"
      + "\r\n"
      + "999,123,123,123,132,132,132,132,132\r\n";

    String file = createTestFile( "UTF-8", data ).getAbsolutePath();
    meta.setFileFormat( "DOS" );
    init( file, true );

    setFields( new TextFileInputField( "Col 1", -1, -1 ), new TextFileInputField( "Col 2", -1, -1 ),
      new TextFileInputField( "Col 3", -1, -1 ), new TextFileInputField( "Col 4", -1, -1 ) );

    process();

    check( new Object[][] {
      { "111", "a\r\nbc", "あいう", "さしす" },
      { "222", "def", "かきく", "たちつ"},
      { "333", "", "かきく", "たちつ" },
      { "444", "", "", null },
      { "555", "かきく", "", null },
      { "666", "かきく", null, null },
      { },
      { "777", "", null, null },
      { "888", "かきく", null, null },
      { },
      { "999", "123", "123", "123" } }
    );
  }

  @Test
  public void testUnixFileFormat() throws Exception {
    String data = "データ1,データ2,データ3,データ4\n"
      + "111,\"a\n"
      + "bc\",\n"
      + "\n"
      + "444,,\n"
      + "555,かきく,\n"
      + "\n"
      + "\n"
      + "777,\n"
      + "888,かきく\n"
      + "999,123,123,123,132,132,132,132,132\n";

    String file = createTestFile( "UTF-8", data ).getAbsolutePath();
    meta.setFileFormat( "Unix" );
    init( file, true );

    setFields( new TextFileInputField( "Col 1", -1, -1 ), new TextFileInputField( "Col 2", -1, -1 ),
      new TextFileInputField( "Col 3", -1, -1 ), new TextFileInputField( "Col 4", -1, -1 ) );

    process();

    check( new Object[][] {
      { "111", "a\nbc", "", null },
      { },
      { "444", "", "", null },
      { "555", "かきく", "", null },
      { },
      { },
      { "777", "", null, null },
      { "888", "かきく", null, null },
      { "999", "123", "123", "123" } }
    );
  }

  @Test
  public void testEnclosures() throws Exception {
    meta.setDelimiter( ";" );
    meta.setEnclosure( "'" );
    init( "enclosures.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
      new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "1", "This line is un-even enclosure-wise because I'm using an escaped enclosure", "a" },
      { "2", "Test isn't even\nhere", "b" } } );
  }

  @Test( expected = KettleStepException.class )
  public void testNoHeaderOptions() throws Exception {
    meta.setHeaderPresent( false );
    init( "default.csv" );

    setFields( new TextFileInputField(), new TextFileInputField(), new TextFileInputField() );

    process();
  }

  File createTestFile( final String encoding, final String content ) throws IOException {
    File tempFile = File.createTempFile( "PDI_tmp", ".csv" );
    tempFile.deleteOnExit();

    try ( PrintWriter osw = new PrintWriter( tempFile, encoding ) ) {
      osw.write( content );
    }

    return tempFile;
  }

  @Test
  public void testSkipColumns() throws Exception {
    String data = "field1,field2,field3,field4\n"
      + "aaa,bbb,ccc,ddd\n"
      + "111,222,333,444\n";

    String file = createTestFile( "UTF-8", data ).getAbsolutePath();
    meta.setFileFormat( "Unix" );
    init( file, true );

    setFields( new TextFileInputField( "field1", -1, -1 ), new TextFileInputField( "field2", -1, -1 ),
      new TextFileInputField( "field4", -1, -1 ) );

    process();

    check( new Object[][] {
      { "aaa", "bbb", "ddd" },
      { "111", "222", "444" } }
    );
  }
}
