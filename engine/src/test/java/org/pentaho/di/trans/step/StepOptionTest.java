/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.pentaho.di.i18n.BaseMessages.getString;

@RunWith( MockitoJUnitRunner.class )
public class StepOptionTest {
  @Mock StepMeta stepMeta;
  @Mock VariableSpace space;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setup() {
    when( space.environmentSubstitute( anyString() ) ).thenAnswer( incovacationMock -> {
      Object[] arguments = incovacationMock.getArguments();
      return arguments[ 0 ];
    } );
  }

  @Test
  public void testCheckPass() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    StepOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", "9" );
    StepOption.checkLong( remarks, stepMeta, space, "IDENTIFIER", "9" );
    StepOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", "true" );
    StepOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", "false" );
    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckPassEmpty() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    StepOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", "" );
    StepOption.checkLong( remarks, stepMeta, space, "IDENTIFIER", "" );
    StepOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", "" );
    StepOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", null );
    StepOption.checkLong( remarks, stepMeta, space, "IDENTIFIER", null );
    StepOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", null );
    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckFailInteger() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    StepOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", "asdf" );
    assertEquals( 1, remarks.size() );
    assertEquals( remarks.get( 0 ).getText(),
      getString( StepOption.class, "StepOption.CheckResult.NotAInteger", "IDENTIFIER" ) );
  }

  @Test
  public void testCheckFailLong() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    StepOption.checkLong( remarks, stepMeta, space, "IDENTIFIER", "asdf" );
    assertEquals( 1, remarks.size() );
    assertEquals( remarks.get( 0 ).getText(),
      getString( StepOption.class, "StepOption.CheckResult.NotAInteger", "IDENTIFIER" ) );
  }

  @Test
  public void testCheckFailBoolean() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    StepOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", "asdf" );
    assertEquals( 1, remarks.size() );
    assertEquals( remarks.get( 0 ).getText(),
      getString( StepOption.class, "StepOption.CheckResult.NotABoolean", "IDENTIFIER" ) );
  }
}
