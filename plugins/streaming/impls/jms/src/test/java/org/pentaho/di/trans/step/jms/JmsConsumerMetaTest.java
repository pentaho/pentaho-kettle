/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.step.jms;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class JmsConsumerMetaTest {

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
    StepPluginType.getInstance().handlePluginAnnotation(
      JmsConsumerMeta.class,
      JmsConsumerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }
  @Test
  public void withVariablesGetsNewObjectFromRegistry() throws KettleXMLException, KettleMissingPluginsException {
    String path = getClass().getResource( "/jms-consumer.ktr" ).getPath();
    TransMeta transMeta = new TransMeta( path, new Variables() );
    StepMeta step = transMeta.getStep( 0 );
    JmsConsumerMeta jmsMeta = (JmsConsumerMeta) step.getStepMetaInterface();
    assertEquals( "${testOne}", jmsMeta.jmsDelegate.amqUrl );
    Variables variables = new Variables();
    variables.setVariable( "testOne", "changedValue" );
    JmsConsumerMeta jmsMetaWithVars = (JmsConsumerMeta) jmsMeta.withVariables( variables );
    assertEquals( "changedValue", jmsMetaWithVars.jmsDelegate.amqUrl );
  }
}
