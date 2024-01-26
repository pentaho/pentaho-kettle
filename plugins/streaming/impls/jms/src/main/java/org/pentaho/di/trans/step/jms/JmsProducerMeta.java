/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.core.util.serialization.BaseSerializingMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepOption;
import org.pentaho.di.trans.step.jms.context.ActiveMQProvider;
import org.pentaho.di.trans.step.jms.context.JmsProvider;
import org.pentaho.di.trans.step.jms.context.WrappedWebsphereMQProvider;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static org.pentaho.di.core.util.serialization.ConfigHelper.conf;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_SERVER_METAVERSE; //NOSONAR
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_DESTINATION_METAVERSE; //NOSONAR
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_DATASOURCE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_MESSAGE_QUEUE;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_PARENT_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_EXTERNAL_CONNECTION;

@InjectionSupported ( localizationPrefix = "JmsProducerMeta.Injection.", groups = { "PROPERTIES", "SSL_GROUP" } )
@Step ( id = "Jms2Producer", image = "JMSP.svg",
  i18nPackageName = "org.pentaho.di.trans.step.jms",
  name = "JmsProducer.TypeLongDesc",
  description = "JmsProducer.TypeTooltipDesc",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Streaming",
  documentationUrl = "mk-95pdia003/pdi-transformation-steps/jms-producer" )
@Metaverse.CategoryMap ( entity = JMS_DESTINATION_METAVERSE, category = CATEGORY_MESSAGE_QUEUE )
@Metaverse.CategoryMap ( entity = JMS_SERVER_METAVERSE, category = CATEGORY_DATASOURCE )
@Metaverse.EntityLink ( entity = JMS_SERVER_METAVERSE, link = LINK_PARENT_CONCEPT, parentEntity =
  NODE_TYPE_EXTERNAL_CONNECTION )
@Metaverse.EntityLink ( entity = JMS_DESTINATION_METAVERSE, link = LINK_CONTAINS_CONCEPT, parentEntity = JMS_SERVER_METAVERSE )
@Metaverse.EntityLink ( entity = JMS_DESTINATION_METAVERSE, link = LINK_PARENT_CONCEPT )
public class JmsProducerMeta extends BaseSerializingMeta implements StepMetaInterface {
  private static final Class<?> PKG = JmsProducerMeta.class;

  public static final String JMS_DESTINATION_METAVERSE = "JMS Destination";
  public static final String JMS_SERVER_METAVERSE = "JMS Server";

  public JmsProducerMeta() {
    this( new JmsDelegate() );
  }

  private static final String FIELD_TO_SEND = "FIELD_TO_SEND";
  private static final String PROPERTIES = "PROPERTIES";
  private static final String PROPERTY_NAMES = "PROPERTY_NAMES";
  private static final String PROPERTY_VALUES = "PROPERTY_VALUES";
  public static final String DISABLE_MESSAGE_ID = "DISABLE_MESSAGE_ID";
  public static final String DISABLE_MESSAGE_TIMESTAMP = "DISABLE_MESSAGE_TIMESTAMP";
  public static final String DELIVERY_MODE = "DELIVERY_MODE";
  public static final String PRIORITY = "PRIORITY";
  public static final String TIME_TO_LIVE = "TIME_TO_LIVE";
  public static final String DELIVERY_DELAY = "DELIVERY_DELAY";
  public static final String JMS_CORRELATION_ID = "JMS_CORRELATION_ID";
  public static final String JMS_TYPE = "JMS_TYPE";

  @InjectionDeep
  public JmsDelegate jmsDelegate;

  @Injection( name = FIELD_TO_SEND )
  private String fieldToSend = "";

  @Injection ( name = PROPERTY_NAMES, group = PROPERTIES )
  @VisibleForTesting
  List<String> propertyNames = new ArrayList<>();

  @Injection ( name = PROPERTY_VALUES, group = PROPERTIES )
  @VisibleForTesting
  List<String> propertyValues = new ArrayList<>();

  @Injection( name = DISABLE_MESSAGE_ID )
  private String disableMessageId;

  @Injection( name = DISABLE_MESSAGE_TIMESTAMP )
  private String disableMessageTimestamp;

  @Injection( name = DELIVERY_MODE )
  private String deliveryMode;

  @Injection( name = PRIORITY )
  private String prio;

  @Injection( name = TIME_TO_LIVE )
  private String timeToLive;

  @Injection( name = DELIVERY_DELAY )
  private String deliveryDelay;

  @Injection( name = JMS_CORRELATION_ID )
  private String jmsCorrelationId;

  @Injection( name = JMS_TYPE )
  private String jmsType;

  public JmsProducerMeta( JmsDelegate jmsDelegate ) {
    this.jmsDelegate = jmsDelegate;
  }

  @SuppressWarnings( "deprecation" )
  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.jms.ui.JmsProducerDialog";
  }

  @Override
  public void setDefault() {
    // no defaults to set
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    return new JmsProducer( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new GenericStepData();
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta,
                     StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output,
                     RowMetaInterface info, VariableSpace space, Repository repository,
                     IMetaStore metaStore ) {
    super.check( remarks, transMeta, stepMeta, prev, input, output, info, space, repository, metaStore );

    StepOption.checkBoolean( remarks, stepMeta, space, getString( PKG, "JmsDialog.Options.DISABLE_MESSAGE_ID" ),
      disableMessageId );
    StepOption.checkBoolean( remarks, stepMeta, space, getString( PKG, "JmsDialog.Options.DISABLE_MESSAGE_TIMESTAMP" ),
      disableMessageTimestamp );
    StepOption.checkInteger( remarks, stepMeta, space, getString( PKG, "JmsDialog.Options.DELIVERY_MODE" ),
      deliveryMode );
    StepOption.checkInteger( remarks, stepMeta, space, getString( PKG, "JmsDialog.Options.PRIORITY" ),
      prio );
    StepOption.checkLong( remarks, stepMeta, space, getString( PKG, "JmsDialog.Options.TIME_TO_LIVE" ),
      timeToLive );
    StepOption.checkLong( remarks, stepMeta, space, getString( PKG, "JmsDialog.Options.DELIVERY_DELAY" ),
      deliveryDelay );
  }

  @Metaverse.Property ( name = "Message Field" )
  public String getFieldToSend() {
    return fieldToSend;
  }

  public void setFieldToSend( String fieldToSend ) {
    this.fieldToSend = fieldToSend;
  }

  public void setPropertyValuesByName( Map<String, String> propertyValuesByName ) {
    this.propertyNames = new ArrayList<>( propertyValuesByName.keySet() );
    this.propertyValues = new ArrayList<>( propertyValuesByName.values() );
  }

  public Map<String, String> getPropertyValuesByName() {
    return conf( propertyNames, propertyValues ).asMap();
  }

  String getDisableMessageId() {
    return disableMessageId;
  }

  public void setDisableMessageId( String disableMessageId ) {
    this.disableMessageId = disableMessageId;
  }

  String getDisableMessageTimestamp() {
    return disableMessageTimestamp;
  }

  public void setDisableMessageTimestamp( String disableMessageTimestamp ) {
    this.disableMessageTimestamp = disableMessageTimestamp;
  }

  String getDeliveryMode() {
    return deliveryMode;
  }

  public void setDeliveryMode( String deliveryMode ) {
    this.deliveryMode = deliveryMode;
  }

  String getPriority() {
    return prio;
  }

  public void setPriority( String priority ) {
    this.prio = priority;
  }

  String getTimeToLive() {
    return timeToLive;
  }

  public void setTimeToLive( String timeToLive ) {
    this.timeToLive = timeToLive;
  }

  String getDeliveryDelay() {
    return deliveryDelay;
  }

  public void setDeliveryDelay( String deliveryDelay ) {
    this.deliveryDelay = deliveryDelay;
  }

  String getJmsCorrelationId() {
    return jmsCorrelationId;
  }

  public void setJmsCorrelationId( String jmsCorrelationId ) {
    this.jmsCorrelationId = jmsCorrelationId;
  }

  String getJmsType() {
    return jmsType;
  }

  public void setJmsType( String jmsType ) {
    this.jmsType = jmsType;
  }

  public List<StepOption> retriveOptions() {
    return Arrays.asList(
      new StepOption( DISABLE_MESSAGE_ID, getString( PKG, "JmsDialog.Options.DISABLE_MESSAGE_ID" ), disableMessageId ),
      new StepOption( DISABLE_MESSAGE_TIMESTAMP, getString( PKG, "JmsDialog.Options.DISABLE_MESSAGE_TIMESTAMP" ),
        disableMessageTimestamp ),
      new StepOption( DELIVERY_MODE, getString( PKG, "JmsDialog.Options.DELIVERY_MODE" ), deliveryMode ),
      new StepOption( PRIORITY, getString( PKG, "JmsDialog.Options.PRIORITY" ), prio ),
      new StepOption( TIME_TO_LIVE, getString( PKG, "JmsDialog.Options.TIME_TO_LIVE" ), timeToLive ),
      new StepOption( DELIVERY_DELAY, getString( PKG, "JmsDialog.Options.DELIVERY_DELAY" ), deliveryDelay ),
      new StepOption( JMS_CORRELATION_ID, getString( PKG, "JmsDialog.Options.JMS_CORRELATION_ID" ), jmsCorrelationId ),
      new StepOption( JMS_TYPE, getString( PKG, "JmsDialog.Options.JMS_TYPE" ), jmsType )
    );
  }
}
