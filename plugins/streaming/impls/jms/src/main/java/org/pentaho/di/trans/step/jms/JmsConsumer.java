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

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.FixedTimeStreamWindow;

import static java.util.Objects.requireNonNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class JmsConsumer extends BaseStreamStep {


  public JmsConsumer( StepMeta stepMeta,
                      StepDataInterface stepDataInterface, int copyNr,
                      TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    boolean superStatus = super.init( stepMetaInterface, stepDataInterface );

    JmsConsumerMeta jmsConsumerMeta = (JmsConsumerMeta) this.variablizedStepMeta;

    if ( !validateParams( jmsConsumerMeta ) ) {
      return false;
    }

    log.logDebug( "Connection Details: "
      + jmsConsumerMeta.jmsDelegate.getJmsProvider().getConnectionDetails( jmsConsumerMeta.jmsDelegate ) );

    window = new FixedTimeStreamWindow<>(
      getSubtransExecutor(), jmsConsumerMeta.getRowMeta(), getDuration(), getBatchSize(), getParallelism() );
    source = new JmsStreamSource( this, requireNonNull( jmsConsumerMeta.jmsDelegate ), getReceiverTimeout( jmsConsumerMeta ) );
    return superStatus;
  }

  private int getReceiverTimeout( JmsConsumerMeta meta ) {
    try {
      return Integer.parseInt( meta.receiveTimeout );
    } catch ( NumberFormatException nfe ) {
      logError( getString( PKG, "JmsConsumer.ReceiveTimeoutInvalid" ) );
    }
    return -1;
  }

  private boolean validateParams( JmsConsumerMeta meta ) {
    return getReceiverTimeout( meta ) > -1;
  }

}
