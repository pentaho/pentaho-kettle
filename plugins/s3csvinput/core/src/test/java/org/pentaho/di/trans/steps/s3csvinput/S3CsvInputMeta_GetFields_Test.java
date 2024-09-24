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
