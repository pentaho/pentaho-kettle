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


package org.pentaho.di.trans.steps.groupby;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class GroupByHelperTest {

  TransMeta transMeta;
  GroupByHelper underTest;

  @Before
  public void setUp() {
    underTest = new GroupByHelper();
    transMeta = mock( TransMeta.class );
  }

  @Test
  public void testTypeGroupCodeAction() {
    JSONObject response = underTest.stepAction( "typeGroupCode", transMeta, null );

    assertNotNull( response );
    JSONArray typeGroupCodes = (JSONArray) response.get( "typeGroupCode" );
    assertNotNull( typeGroupCodes );
    assertEquals( GroupByMeta.typeGroupCode.length, typeGroupCodes.size() );
  }
}
