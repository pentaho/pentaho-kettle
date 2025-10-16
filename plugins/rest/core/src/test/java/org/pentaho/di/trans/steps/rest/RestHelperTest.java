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

package org.pentaho.di.trans.steps.rest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class RestHelperTest {

  TransMeta transMeta;
  RestHelper underTest;

  @Before
  public void setUp() {
    underTest = new RestHelper();
    transMeta = mock( TransMeta.class );
  }

  @Test
  public void testApplicationTypesAction() {
    JSONObject response = underTest.stepAction( "applicationTypes", transMeta, new HashMap<>() );

    assertNotNull( response );
    JSONArray applicationTypes = (JSONArray) response.get( "applicationTypes" );
    assertNotNull( applicationTypes );
    assertEquals( RestMeta.APPLICATION_TYPES.length, applicationTypes.size() );
  }

  @Test
  public void testHttpMethodsAction() {
    JSONObject response = underTest.stepAction( "httpMethods", transMeta, new HashMap<>() );

    assertNotNull( response );
    JSONArray httpMethods = (JSONArray) response.get( "httpMethods" );
    assertNotNull( httpMethods );
    assertEquals( RestMeta.HTTP_METHODS.length, httpMethods.size() );
  }
}
