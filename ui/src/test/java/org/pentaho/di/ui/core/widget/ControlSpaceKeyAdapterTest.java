/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
