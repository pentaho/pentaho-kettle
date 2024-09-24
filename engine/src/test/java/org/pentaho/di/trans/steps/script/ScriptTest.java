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

package org.pentaho.di.trans.steps.script;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class ScriptTest {
  private StepMockHelper<ScriptMeta, ScriptData> helper;

  @Before
  public void setUp() throws Exception {
    helper = new StepMockHelper<>( "test-script", ScriptMeta.class, ScriptData.class );
    when( helper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      helper.logChannelInterface );
    when( helper.trans.isRunning() ).thenReturn( true );
    when( helper.initStepMetaInterface.getJSScripts() ).thenReturn(
      new ScriptValuesScript[] { new ScriptValuesScript( ScriptValuesScript.NORMAL_SCRIPT, "", "var i = 0;" ) } );
  }

  @After
  public void tearDown() throws Exception {
    helper.cleanUp();
  }

  @Test
  public void testOutputDoneIfInputEmpty() throws Exception {
    Script step = new Script( helper.stepMeta, helper.stepDataInterface, 1, helper.transMeta, helper.trans );
    step.init( helper.initStepMetaInterface, helper.initStepDataInterface );

    RowSet rs = helper.getMockInputRowSet( new Object[ 0 ][ 0 ] );
    List<RowSet> in = new ArrayList<RowSet>();
    in.add( rs );
    step.setInputRowSets( in );

    TransTestingUtil.execute( step, helper.processRowsStepMetaInterface, helper.processRowsStepDataInterface, 0, true );
    rs.getRow();
  }

}
