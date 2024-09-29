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


package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;

public class DatabaseConnectionPoolParameterTest {

  @Test
  public void testGetRowList() {
    List<RowMetaAndData> result = DatabaseConnectionPoolParameter.getRowList( BaseDatabaseMeta.poolingParameters,
      "myTitleParameter", "myTitleDefaultValue", "myTitleDescription" );

    assertNotNull( result );
    for ( RowMetaAndData rmd : result ) {
      assertEquals( 3, rmd.getRowMeta().size() );
      assertEquals( "myTitleParameter", rmd.getRowMeta().getValueMeta( 0 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 0 ).getType() );
      assertEquals( "myTitleDefaultValue", rmd.getRowMeta().getValueMeta( 1 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 1 ).getType() );
      assertEquals( "myTitleDescription", rmd.getRowMeta().getValueMeta( 2 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 2 ).getType() );
    }
  }
}
