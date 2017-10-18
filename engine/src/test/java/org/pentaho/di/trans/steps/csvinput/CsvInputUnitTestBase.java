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
public class CsvInputUnitTestBase {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  File createTestFile( final String encoding, final String content ) throws IOException {
    File tempFile = File.createTempFile( "PDI_tmp", ".tmp" );
    tempFile.deleteOnExit();

    PrintWriter osw = new PrintWriter( tempFile, encoding );
    try {
      osw.write( content );
    } finally {
      osw.close();
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
}
