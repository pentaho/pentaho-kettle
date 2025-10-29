/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.big.data.kettle.plugins.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class KafkaProducerOutputData extends BaseStepData implements StepDataInterface {
  Producer<Object, Object> kafkaProducer;
  int keyFieldIndex;
  int messageFieldIndex;
  boolean isOpen;

  public KafkaProducerOutputData() {
    super();
  }
}
