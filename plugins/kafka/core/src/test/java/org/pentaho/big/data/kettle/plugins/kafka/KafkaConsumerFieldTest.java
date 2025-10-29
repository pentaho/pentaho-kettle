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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.row.ValueMetaInterface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 6/19/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class KafkaConsumerFieldTest {
  KafkaConsumerField field;
  @Mock ValueMetaInterface vmi;

  @Test
  public void testEmptyConstructor() {
    field = new KafkaConsumerField();

    assertNull( field.getKafkaName() );
    assertNull( field.getOutputName() );
    assertEquals( KafkaConsumerField.Type.String, field.getOutputType() );
  }

  @Test
  public void testSettersGetters() {
    field = new KafkaConsumerField();
    field.setKafkaName( KafkaConsumerField.Name.MESSAGE );
    field.setOutputName( "MSG" );
    field.setOutputType( KafkaConsumerField.Type.Integer );

    assertEquals( KafkaConsumerField.Name.MESSAGE, field.getKafkaName() );
    assertEquals( "MSG", field.getOutputName() );
    assertEquals( KafkaConsumerField.Type.Integer, field.getOutputType() );
  }

  @Test
  public void testConstructor_noType() {
    field = new KafkaConsumerField( KafkaConsumerField.Name.KEY, "Test Name" );

    assertEquals( KafkaConsumerField.Name.KEY, field.getKafkaName() );
    assertEquals( "Test Name", field.getOutputName() );
    assertEquals( KafkaConsumerField.Type.String, field.getOutputType() );
  }

  @Test
  public void testConstructor_allProps() {
    field = new KafkaConsumerField( KafkaConsumerField.Name.KEY, "Test Name", KafkaConsumerField.Type.Binary );

    assertEquals( KafkaConsumerField.Name.KEY, field.getKafkaName() );
    assertEquals( "Test Name", field.getOutputName() );
    assertEquals( KafkaConsumerField.Type.Binary, field.getOutputType() );
  }

  @Test
  public void testSerializersSet() {
    field = new KafkaConsumerField( KafkaConsumerField.Name.KEY, "Test Name" );
    assertEquals( "class org.apache.kafka.common.serialization.StringSerializer", field.getOutputType().getKafkaSerializerClass().toString() );
    assertEquals( "class org.apache.kafka.common.serialization.StringDeserializer", field.getOutputType().getKafkaDeserializerClass().toString() );

    field = new KafkaConsumerField( KafkaConsumerField.Name.KEY, "Test Name", KafkaConsumerField.Type.Integer );
    assertEquals( "class org.apache.kafka.common.serialization.LongSerializer", field.getOutputType().getKafkaSerializerClass().toString() );
    assertEquals( "class org.apache.kafka.common.serialization.LongDeserializer", field.getOutputType().getKafkaDeserializerClass().toString() );

    field = new KafkaConsumerField( KafkaConsumerField.Name.KEY, "Test Name", KafkaConsumerField.Type.Binary );
    assertEquals( "class org.apache.kafka.common.serialization.ByteArraySerializer", field.getOutputType().getKafkaSerializerClass().toString() );
    assertEquals( "class org.apache.kafka.common.serialization.ByteArrayDeserializer", field.getOutputType().getKafkaDeserializerClass().toString() );

    field = new KafkaConsumerField( KafkaConsumerField.Name.KEY, "Test Name", KafkaConsumerField.Type.Number );
    assertEquals( "class org.apache.kafka.common.serialization.DoubleSerializer", field.getOutputType().getKafkaSerializerClass().toString() );
    assertEquals( "class org.apache.kafka.common.serialization.DoubleDeserializer", field.getOutputType().getKafkaDeserializerClass().toString() );
  }

  @Test
  public void testFromValueMetaInterface() {
    when( vmi.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );
    KafkaConsumerField.Type t = KafkaConsumerField.Type.fromValueMetaInterface( vmi );
    assertEquals( "String", t.toString() );

    when( vmi.getType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );
    t = KafkaConsumerField.Type.fromValueMetaInterface( vmi );
    assertEquals( "Integer", t.toString() );

    when( vmi.getType() ).thenReturn( ValueMetaInterface.TYPE_BINARY );
    t = KafkaConsumerField.Type.fromValueMetaInterface( vmi );
    assertEquals( "Binary", t.toString() );

    when( vmi.getType() ).thenReturn( ValueMetaInterface.TYPE_NUMBER );
    t = KafkaConsumerField.Type.fromValueMetaInterface( vmi );
    assertEquals( "Number", t.toString() );
  }
}
