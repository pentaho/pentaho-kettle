/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.s3csvinput;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import static org.junit.Assert.*;

/**
 * @author Andrey Khayrutdinov
 */
public class S3CsvInputMeta_GetFields_Test {

  @Test
  public void getFields_PicksFields() throws Exception {
    RowMeta rowMeta = new RowMeta();
    createSampleMeta().getFields( rowMeta, "", null, null, null, null, null );
    assertFieldsArePopulated( rowMeta );
  }

  @Test
  public void getFields_DeprecatedAlsoPicksFields() throws Exception {
    RowMeta rowMeta = new RowMeta();
    createSampleMeta().getFields( rowMeta, "", null, null, null );
    assertFieldsArePopulated( rowMeta );
  }

  private S3CsvInputMeta createSampleMeta() {
    S3CsvInputMeta meta = new S3CsvInputMeta();
    meta.allocate( 2 );
    meta.getInputFields()[ 0 ] = new TextFileInputField( "field1", 0, 1 );
    meta.getInputFields()[ 1 ] = new TextFileInputField( "field2", 0, 2 );
    return meta;
  }

  private void assertFieldsArePopulated( RowMeta rowMeta ) {
    assertEquals( 2, rowMeta.size() );
    assertEquals( "field1", rowMeta.getValueMeta( 0 ).getName() );
    assertEquals( "field2", rowMeta.getValueMeta( 1 ).getName() );
  }
}
