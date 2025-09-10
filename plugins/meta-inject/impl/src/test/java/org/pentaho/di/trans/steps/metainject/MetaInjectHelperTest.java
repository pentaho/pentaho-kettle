/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.metainject;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.pentaho.di.trans.step.BaseStepHelper.IS_TRANS_REFERENCE;
import static org.pentaho.di.trans.step.BaseStepHelper.REFERENCE_PATH;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;

public class MetaInjectHelperTest {

  MetaInjectMeta metaInjectMeta;
  MetaInjectHelper metaInjectHelper;
  TransMeta transMeta;

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    metaInjectMeta = mock( MetaInjectMeta.class );
    metaInjectHelper = new MetaInjectHelper( metaInjectMeta );
  }

  @Test
  public void testReferencePath() {
    when( metaInjectMeta.getFileName() ).thenReturn( "/path/transFile.ktr" );
    when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    JSONObject response = metaInjectHelper.stepAction( REFERENCE_PATH, transMeta, null );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( REFERENCE_PATH ) );
    assertEquals( "/path/transFile.ktr", response.get( REFERENCE_PATH ) );
    assertEquals( true, response.get( IS_TRANS_REFERENCE ) );
  }
}
