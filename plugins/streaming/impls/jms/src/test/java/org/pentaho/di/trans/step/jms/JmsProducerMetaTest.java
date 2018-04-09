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

package org.pentaho.di.trans.step.jms;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DELIVERY_DELAY;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DELIVERY_MODE;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DISABLE_MESSAGE_ID;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DISABLE_MESSAGE_TIMESTAMP;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_CORRELATION_ID;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_TYPE;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.PRIORITY;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.TIME_TO_LIVE;

public class JmsProducerMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testRetriveOptions() {
    List<StepOption> compareStepOptions = Arrays.asList(
      new StepOption( DISABLE_MESSAGE_ID, getString( JmsProducerMeta.class, "JmsDialog.Options.DISABLE_MESSAGE_ID" ),
        "false" ),
      new StepOption( DISABLE_MESSAGE_TIMESTAMP,
        getString( JmsProducerMeta.class, "JmsDialog.Options.DISABLE_MESSAGE_TIMESTAMP" ), "true" ),
      new StepOption( DELIVERY_MODE, getString( JmsProducerMeta.class, "JmsDialog.Options.DELIVERY_MODE" ), "2" ),
      new StepOption( PRIORITY, getString( JmsProducerMeta.class, "JmsDialog.Options.PRIORITY" ), "3" ),
      new StepOption( TIME_TO_LIVE, getString( JmsProducerMeta.class, "JmsDialog.Options.TIME_TO_LIVE" ), "100" ),
      new StepOption( DELIVERY_DELAY, getString( JmsProducerMeta.class, "JmsDialog.Options.DELIVERY_DELAY" ), "20" ),
      new StepOption( JMS_CORRELATION_ID, getString( JmsProducerMeta.class, "JmsDialog.Options.JMS_CORRELATION_ID" ),
        "asdf" ),
      new StepOption( JMS_TYPE, getString( JmsProducerMeta.class, "JmsDialog.Options.JMS_TYPE" ), "myType" )
    );

    JmsProducerMeta jmsProducerMeta = new JmsProducerMeta();
    jmsProducerMeta.setDisableMessageId( "false" );
    jmsProducerMeta.setDisableMessageTimestamp( "true" );
    jmsProducerMeta.setDeliveryMode( "2" );
    jmsProducerMeta.setPriority( "3" );
    jmsProducerMeta.setTimeToLive( "100" );
    jmsProducerMeta.setDeliveryDelay( "20" );
    jmsProducerMeta.setJmsCorrelationId( "asdf" );
    jmsProducerMeta.setJmsType( "myType" );
    List<StepOption> stepOptions = jmsProducerMeta.retriveOptions();

    assertNotNull( stepOptions );
    assertEquals( 8, stepOptions.size() );

    assertOptions( compareStepOptions, stepOptions );
  }

  @Test
  public void testCheck() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    JmsProducerMeta jmsProducerMeta = new JmsProducerMeta();
    jmsProducerMeta.setDisableMessageId( "asdf" );
    jmsProducerMeta.setDisableMessageTimestamp( "asdf" );
    jmsProducerMeta.setDeliveryMode( "asdf" );
    jmsProducerMeta.setPriority( "asdf" );
    jmsProducerMeta.setTimeToLive( "asdf" );
    jmsProducerMeta.setDeliveryDelay( "asdf" );
    jmsProducerMeta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );

    assertEquals( 6, remarks.size() );
    assertTrue( remarks.get( 0 ).getText()
      .contains( BaseMessages.getString( JmsProducerMeta.class, "JmsDialog.Options.DISABLE_MESSAGE_ID" ) ) );
    assertTrue( remarks.get( 1 ).getText()
      .contains( BaseMessages.getString( JmsProducerMeta.class, "JmsDialog.Options.DISABLE_MESSAGE_TIMESTAMP" ) ) );
    assertTrue( remarks.get( 2 ).getText()
      .contains( BaseMessages.getString( JmsProducerMeta.class, "JmsDialog.Options.DELIVERY_MODE" ) ) );
    assertTrue( remarks.get( 3 ).getText()
      .contains( BaseMessages.getString( JmsProducerMeta.class, "JmsDialog.Options.PRIORITY" ) ) );
    assertTrue( remarks.get( 4 ).getText()
      .contains( BaseMessages.getString( JmsProducerMeta.class, "JmsDialog.Options.TIME_TO_LIVE" ) ) );
    assertTrue( remarks.get( 5 ).getText()
      .contains( BaseMessages.getString( JmsProducerMeta.class, "JmsDialog.Options.DELIVERY_DELAY" ) ) );

    remarks = new ArrayList<>();
    jmsProducerMeta.setDisableMessageId( "true" );
    jmsProducerMeta.setDisableMessageTimestamp( "false" );
    jmsProducerMeta.setDeliveryMode( "1" );
    jmsProducerMeta.setPriority( "2" );
    jmsProducerMeta.setTimeToLive( "3" );
    jmsProducerMeta.setDeliveryDelay( "4" );
    jmsProducerMeta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );

    assertEquals( 0, remarks.size() );
  }

  private void assertOptions( List<StepOption> expected, List<StepOption> actual ) {
    for ( StepOption expectedOption : expected ) {
      boolean isFound = false;

      for ( StepOption actualOption : actual ) {
        if ( expectedOption.getKey().equals( actualOption.getKey() ) ) {
          isFound = true;
          assertEquals( expectedOption.getText(), actualOption.getText() );
          assertEquals( expectedOption.getValue(), actualOption.getValue() );
          break;
        }
      }
      assertTrue( expectedOption.getKey() + " was not found", isFound );
    }
  }
}
