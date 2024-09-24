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

import org.junit.BeforeClass;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Andrey Khayrutdinov
 */
public abstract class CsvInputUnitTestBase {

  static final String BUFFER_SIZE = "1024";
  static final String ENCODING = "utf-8";
  static final String ENCLOSURE = "\"";
  static final String DELIMITER = ",";

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  File createTestFile( final String encoding, final String content ) throws IOException {
    File tempFile = File.createTempFile( "PDI_tmp", ".tmp" );
    tempFile.deleteOnExit();

    try ( PrintWriter osw = new PrintWriter( tempFile, encoding ) ) {
      osw.write( content );
    }

    return tempFile;
  }

  TextFileInputField[] createInputFileFields( String... names ) {
    TextFileInputField[] fields = new TextFileInputField[ names.length ];
    for ( int i = 0; i < names.length; i++ ) {
      fields[ i ] = createField( names[ i ] );
    }
    return fields;
  }

  TextFileInputField createField( String name ) {
    TextFileInputField field = new TextFileInputField();
    field.setName( name );
    field.setType( ValueMetaInterface.TYPE_STRING );
    return field;
  }

  CsvInputMeta createMeta( File file, TextFileInputField[] fields ) {
    CsvInputMeta meta = new CsvInputMeta();
    meta.setFilename( file.getAbsolutePath() );
    meta.setBufferSize( BUFFER_SIZE );
    meta.setDelimiter( DELIMITER );
    meta.setEnclosure( ENCLOSURE );
    meta.setEncoding( ENCODING );
    meta.setInputFields( fields );
    meta.setHeaderPresent( false );
    return meta;
  }
}
