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
