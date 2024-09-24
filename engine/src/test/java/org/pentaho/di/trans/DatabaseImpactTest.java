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

package org.pentaho.di.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class DatabaseImpactTest {

  private Class<?> PKG = Trans.class;

  @Test
  public void testGetRow() throws KettleValueException {
    DatabaseImpact testObject =
        new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, "myTrans", "aStep", "ProdDB", "DimCustomer",
            "Customer_Key", "MyValue", "Calculator 2", "SELECT * FROM dimCustomer", "Some remarks" );
    RowMetaAndData rmd = testObject.getRow();

    assertNotNull( rmd );
    assertEquals( 10, rmd.size() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 0 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Type" ), rmd.getValueMeta( 0 ).getName() );
    assertEquals( "Read", rmd.getString( 0, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 1 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Transformation" ), rmd.getValueMeta( 1 )
        .getName() );
    assertEquals( "myTrans", rmd.getString( 1, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 2 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Step" ), rmd.getValueMeta( 2 ).getName() );
    assertEquals( "aStep", rmd.getString( 2, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 3 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Database" ), rmd.getValueMeta( 3 )
        .getName() );
    assertEquals( "ProdDB", rmd.getString( 3, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 4 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Table" ), rmd.getValueMeta( 4 )
        .getName() );
    assertEquals( "DimCustomer", rmd.getString( 4, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 5 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Field" ), rmd.getValueMeta( 5 )
        .getName() );
    assertEquals( "Customer_Key", rmd.getString( 5, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 6 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Value" ), rmd.getValueMeta( 6 )
        .getName() );
    assertEquals( "MyValue", rmd.getString( 6, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 7 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.ValueOrigin" ), rmd.getValueMeta( 7 )
        .getName() );
    assertEquals( "Calculator 2", rmd.getString( 7, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 8 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.SQL" ), rmd.getValueMeta( 8 ).getName() );
    assertEquals( "SELECT * FROM dimCustomer", rmd.getString( 8, "default" ) );
    assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getValueMeta( 9 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "DatabaseImpact.RowDesc.Label.Remarks" ), rmd.getValueMeta( 9 )
        .getName() );
    assertEquals( "Some remarks", rmd.getString( 9, "default" ) );
  }
}
