/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2020 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.util.serialization.BaseSerializingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.pentaho.di.i18n.BaseMessages.getString;

public class MQTTProducer extends BaseStep implements StepInterface {
  private static final Class<?> PKG = MQTTProducer.class;

  private MQTTProducerMeta meta;

  @SuppressWarnings( { "squid:S4738", "Guava" } )  //using guava memoize, so no gain switching to java Supplier
  Supplier<MqttClient> client = Suppliers.memoize( this::connectToClient );
  private AtomicBoolean connectionError = new AtomicBoolean( false );

  /**
   * This is the base step that forms that basis for all steps. You can derive from this class to implement your own
   * steps.
   *
   * @param stepMeta          The StepMeta object to run.
   * @param stepDataInterface the data object to store temporary data, database connections, caches, result sets,
   *                          hashtables etc.
   * @param copyNr            The copynumber for this step.
   * @param transMeta         The TransInfo of which the step stepMeta is part of.
   * @param trans             The (running) transformation to obtain information shared among the steps.
   */
  public MQTTProducer( StepMeta stepMeta,
                       StepDataInterface stepDataInterface, int copyNr,
                       TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    boolean isInitalized = super.init( stepMetaInterface, stepDataInterface );
    BaseSerializingMeta serializingMeta = (BaseSerializingMeta) stepMetaInterface;
    meta = (MQTTProducerMeta) serializingMeta.withVariables( this ); // handle variable substitution up-front

    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check(
      remarks, getTransMeta(), meta.getParentStepMeta(),
      null, null, null, null, //these parameters are not used inside the method
      variables, getRepository(), getMetaStore() );
    @SuppressWarnings( "squid:S3864" ) //peek used appropriately here
    boolean errorsPresent =
      remarks.stream().filter( result -> result.getType() == CheckResultInterface.TYPE_RESULT_ERROR )
        .peek( result -> logError( result.getText() ) )
        .count() > 0;
    return !errorsPresent && isInitalized;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] row = getRow();

    if ( null == row ) {
      setOutputDone();
      return false;
    }
    try {
      client.get()  // client is memoized, loaded on first use
        .publish( getTopic( row ), getMessage( row ) );

      incrementLinesOutput();
      putRow( getInputRowMeta(), row ); // copy row to possible alternate rowset(s).

      if ( checkFeedback( getLinesRead() ) && log.isBasic() ) {
        logBasic( getString( PKG, "MQTTProducer.Log.LineNumber" ) + getLinesRead() );
      }
    } catch ( MqttException e ) {
      logError( getString( PKG, "MQTTProducer.Error.QOSNotSupported", meta.qos ) );
      logError( e.getMessage(), e );
      setErrors( 1 );
      stopAll();
      return false;
    } catch ( RuntimeException re ) {
      stopAll();
      logError( re.getMessage() );
      return false;
    }
    return true;
  }

  private MqttClient connectToClient() {
    logDebug( "Publishing using a quality of service level of " + meta.qos );
    try {
      return
        MQTTClientBuilder.builder()
          .withBroker( this.meta.mqttServer )
          .withClientId( meta.clientId )
          .withQos( meta.qos )
          .withStep( this )
          .withUsername( meta.username )
          .withPassword( meta.password )
          .withSslConfig( meta.getSslConfig() )
          .withIsSecure( meta.useSsl )
          .withKeepAliveInterval( meta.keepAliveInterval )
          .withMaxInflight( meta.maxInflight )
          .withConnectionTimeout( meta.connectionTimeout )
          .withCleanSession( meta.cleanSession )
          .withStorageLevel( meta.storageLevel )
          .withServerUris( meta.serverUris )
          .withMqttVersion( meta.mqttVersion )
          .withAutomaticReconnect( meta.automaticReconnect )
          .buildAndConnect();
    } catch ( MqttException e ) {
      connectionError.set( true );
      throw new IllegalStateException( e );
    } catch ( IllegalArgumentException iae ) {
      connectionError.set( true );
      throw iae;
    }
  }

  private MqttMessage getMessage( Object[] row ) throws KettleStepException {
    MqttMessage mqttMessage = new MqttMessage();
    try {
      mqttMessage.setQos( Integer.parseInt( meta.qos ) );
    } catch ( NumberFormatException e ) {
      throw new KettleStepException(
        getString( PKG, "MQTTProducer.Error.QOS", meta.qos ) );
    }
    //noinspection ConstantConditions
    mqttMessage.setPayload( getFieldData( row, meta.messageField )
      .map( this::dataAsBytes )
      .orElse( null ) ); //allow nulls to pass through
    return mqttMessage;
  }

  private byte[] dataAsBytes( Object data ) {
    if ( getInputRowMeta().searchValueMeta( meta.messageField ).isBinary() ) {
      return (byte[]) data;
    } else {
      return Objects.toString( data ).getBytes( UTF_8 );
    }
  }

  /**
   * Retrieves the topic, either a raw string, or a field value if meta.topicInField==true
   */
  private String getTopic( Object[] row ) {
    String topic;
    if ( meta.topicInField ) {
      topic = getFieldData( row, meta.fieldTopic ).map( Objects::toString ).orElse( "" );
    } else {
      topic = meta.topic;
    }
    return topic;
  }

  private Optional<Object> getFieldData( Object[] row, String field ) {
    int messageFieldIndex = getInputRowMeta().indexOfValue( field );
    checkArgument( messageFieldIndex > -1, getString( PKG, "MQTTProducer.Error.FieldNotFound", field ) );
    return ofNullable( row[ messageFieldIndex ] );
  }

  @Override public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface )
    throws KettleException {
    stopMqttClient();
    super.stopRunning( stepMetaInterface, stepDataInterface );
  }

  @Override public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
    stopMqttClient();
  }

  private void stopMqttClient() {
    try {
      // Check if connected so subsequent calls does not produce an already stopped exception
      if ( !connectionError.get()
        && client.get() != null
        && client.get().isConnected() ) {
        log.logDebug( getString( PKG, "MQTTProducer.Log.Closing" ) );
        client.get().disconnect();
        client.get().close();
      }
    } catch ( IllegalArgumentException | MqttException e ) {
      logError( e.getMessage() );
    }
  }


}
