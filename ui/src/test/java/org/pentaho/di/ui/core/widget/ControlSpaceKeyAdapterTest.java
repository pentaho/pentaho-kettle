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


package org.pentaho.di.ui.core.widget;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.MockedStatic;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.PropsUI;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class ControlSpaceKeyAdapterTest {

  private static MockedStatic<PropsUI> propsUIMockedStatic;

  @Before
  public void before() {
    PropsUI propsUI = mock( PropsUI.class );
    propsUIMockedStatic = mockStatic( PropsUI.class );
    BDDMockito.given( PropsUI.getInstance() ).willReturn( propsUI );
  }

  @After
  public void cleanUp() {
    propsUIMockedStatic.close();
  }

  @Test
  public void testGetVariableNames() {
    Assert.assertTrue( Const.DEPRECATED_VARIABLES.length > 0 );

    String deprecatedVariableName = Const.DEPRECATED_VARIABLES[0];
    String deprecatedPrefix = Const.getDeprecatedPrefix();
    String[] variableNames = new String[] { "test_variable1", "test_variable2", deprecatedVariableName };
    String[] expectedVariables = new String[] { "test_variable1", "test_variable2", deprecatedVariableName + deprecatedPrefix };

    VariableSpace variableSpace = mock( VariableSpace.class );

    doReturn( variableNames ).when( variableSpace ).listVariables();

    Assert.assertArrayEquals( expectedVariables, ControlSpaceKeyAdapter.getVariableNames( variableSpace ) );
  }
}
