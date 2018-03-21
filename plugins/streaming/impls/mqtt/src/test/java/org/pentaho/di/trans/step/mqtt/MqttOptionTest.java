/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.step.mqtt;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith( MockitoJUnitRunner.class )
public class MqttOptionTest {
  @Mock StepMeta stepMeta;
  @Mock VariableSpace space;

  @Test
  public void testCheckPass() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MqttOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", "9" );
    MqttOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", "true" );
    MqttOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", "false" );
    MqttOption.checkVersion( remarks, stepMeta, space, "0" );
    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckPassEmpty() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MqttOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", "" );
    MqttOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", "" );
    MqttOption.checkVersion( remarks, stepMeta, space, "" );
    MqttOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", null );
    MqttOption.checkBoolean( remarks, stepMeta, space, "IDENTIFIER", null );
    MqttOption.checkVersion( remarks, stepMeta, space, null );
    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckFailInteger() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MqttOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", "asdf" );
  }

  @Test
  public void testCheckFailBoolean() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MqttOption.checkInteger( remarks, stepMeta, space, "IDENTIFIER", "asdf" );
  }

  @Test
  public void testCheckFailVersion() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MqttOption.checkVersion( remarks, stepMeta, space, "asdf" );
    MqttOption.checkVersion( remarks, stepMeta, space, "9" );
  }
}
