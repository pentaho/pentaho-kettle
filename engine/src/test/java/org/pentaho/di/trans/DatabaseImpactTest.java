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

package org.pentaho.di.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class DatabaseImpactTest {

  private static Class<?> PKG = Trans.class;

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
