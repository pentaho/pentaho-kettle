/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step.common;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;

public class CsvInputAwareImportProgressDialogTest {

  @Test
  public void testGetStringFromRow() throws KettleException {

    final String[] row = new String[ 2 ];
    row[ 0 ] = "foo";
    row[ 1 ] = null;
    final CsvInputAwareImportProgressDialog dlg = ( failOnParseError ) -> {
      return null;
    };
    final RowMetaInterface rowMeta = Mockito.mock( RowMeta.class );

    // verify that when 'failOnParseError' is false, and row index is out of bounds, we get a null value back and no
    // Exception
    try {
      Assert.assertNull( dlg.getStringFromRow( rowMeta, row, 2, false ) );
    } catch ( final KettleException e ) {
      Assert.fail( "Exception should not have been thrown" );
    }

    // we should always get back a 'null', when the index is valid and the value is null
    try {
      Assert.assertNull( dlg.getStringFromRow( rowMeta, row, 1, false ) );
      Assert.assertNull( dlg.getStringFromRow( rowMeta, row, 1, true ) );
    } catch ( final KettleException e ) {
      Assert.fail( "Exception should not have been thrown" );
    }

    // verify that when 'failOnParseError' is true, and row value is null or index is out of bounds, we get a NPE
    // wrapped in a KettleException
    try {
      Assert.assertNull( dlg.getStringFromRow( rowMeta, row, 2, true ) );
      Assert.fail( "Exception should not have been thrown" );
    } catch ( final KettleException e ) {
    }

    // when 'failOnParseError' is false, we do not want to throw an exception - we also need to verify that the value we
    // get is the one that came from a call to RowMetaInterfece.getString and not from the row object itself, hence
    // we mock this method to return something other than what is in the row object
    Mockito.when( rowMeta.getString( row, 0 ) ).thenReturn( "foe" );
    try {
      Assert.assertEquals( "foe", dlg.getStringFromRow( rowMeta, row, 0, false ) );
    } catch ( final KettleException e ) {
      Assert.fail( "Exception should not have been thrown" );
    }

    // when RowMetaInterfece.getString is mocked to throw an NPE and 'failOnParseError' is false, we expect to get
    // back the value from the row object
    Mockito.when( rowMeta.getString( row, 0 ) ).thenThrow( new NullPointerException( "NPE" ) );
    try {
      Assert.assertEquals( "foo", dlg.getStringFromRow( rowMeta, row, 0, false ) );
    } catch ( final KettleException e ) {
      Assert.fail( "Exception should not have been thrown" );
    }

    // when 'failOnParseError' is true and RowMetaInterfece.getString throws an exception, we expect to get back
    // a KettleException
    try {
      dlg.getStringFromRow( rowMeta, row, 0, true );
      Assert.fail( "Exception should have been thrown" );
    } catch ( final KettleException e ) {
    }
  }
}
