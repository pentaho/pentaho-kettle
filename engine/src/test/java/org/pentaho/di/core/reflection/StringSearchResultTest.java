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

package org.pentaho.di.core.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class StringSearchResultTest {

  private Class<?> PKG = Const.class;

  @Test
  public void testgetResultRowMeta() {
    RowMetaInterface rm = StringSearchResult.getResultRowMeta();
    assertNotNull( rm );
    assertEquals( 4, rm.getValueMetaList().size() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 0 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "SearchResult.TransOrJob" ), rm.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 1 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "SearchResult.StepDatabaseNotice" ), rm.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 2 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "SearchResult.String" ), rm.getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rm.getValueMeta( 3 ).getType() );
    assertEquals( BaseMessages.getString( PKG, "SearchResult.FieldName" ), rm.getValueMeta( 3 ).getName() );
  }
}
