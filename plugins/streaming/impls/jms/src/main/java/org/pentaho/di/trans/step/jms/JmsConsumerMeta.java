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
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.jms.context.ActiveMQProvider;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse;

import static java.util.Collections.singletonList;
import static org.pentaho.di.core.ObjectLocationSpecificationMethod.FILENAME;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_DESTINATION_METAVERSE;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_SERVER_METAVERSE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_DATASOURCE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_MESSAGE_QUEUE;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_INPUTS;
import static org.pentaho.dictionary.DictionaryConst.LINK_PARENT_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_EXTERNAL_CONNECTION;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.FALSE;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.SUBTRANS_INPUT;
import static org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer.RESOURCE;

@InjectionSupported ( localizationPrefix = "JmsConsumerMeta.Injection.", groups = { "SSL_GROUP" } )
@Step ( id = "Jms2Consumer", image = "JMSC.svg",
  i18nPackageName = "org.pentaho.di.trans.step.jms",
  name = "JmsConsumer.TypeLongDesc",
  description = "JmsConsumer.TypeTooltipDesc",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Streaming",
  documentationUrl = "mk-95pdia003/pdi-transformation-steps/jms-consumer" )
@Metaverse.CategoryMap ( entity = JMS_DESTINATION_METAVERSE, category = CATEGORY_MESSAGE_QUEUE )
@Metaverse.CategoryMap ( entity = JMS_SERVER_METAVERSE, category = CATEGORY_DATASOURCE )
@Metaverse.EntityLink ( entity = JMS_SERVER_METAVERSE, link = LINK_PARENT_CONCEPT, parentEntity =
  NODE_TYPE_EXTERNAL_CONNECTION )
@Metaverse.EntityLink ( entity = JMS_DESTINATION_METAVERSE, link = LINK_CONTAINS_CONCEPT, parentEntity = JMS_SERVER_METAVERSE )
@Metaverse.EntityLink ( entity = JMS_DESTINATION_METAVERSE, link = LINK_PARENT_CONCEPT )
public class JmsConsumerMeta extends BaseStreamStepMeta implements ISubTransAwareMeta, StepMetaInterface {

  private static final String JMS_MESSAGE_METAVERSE = "Message";
  private static final String JMS_DESTINATION_NAME_METAVERSE = "Destination Name";
  private static final String JMS_MESSAGE_ID_METAVERSE = "Message Id";
  private static final String JMS_TIMESTAMP_METAVERSE = "JMS Timestamp";
  private static final String JMS_REDELIVERED_METAVERSE = "JMS Redelivered";
  @InjectionDeep
  public JmsDelegate jmsDelegate;

  @Injection( name = "RECEIVE_TIMEOUT" ) public String receiveTimeout = "0";

  @Injection ( name = "MESSAGE_FIELD_NAME" )
  @Metaverse.Node ( name = JMS_MESSAGE_METAVERSE, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = JMS_MESSAGE_METAVERSE, parentNodeName = JMS_MESSAGE_METAVERSE )
  @Metaverse.NodeLink ( nodeName = JMS_MESSAGE_METAVERSE, parentNodeName = JMS_DESTINATION_METAVERSE, linkDirection = "OUT" )
  public String messageField = "message";

  @Injection ( name = "DESTINATION_FIELD_NAME" )
  @Metaverse.Node ( name = JMS_DESTINATION_NAME_METAVERSE, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = JMS_DESTINATION_NAME_METAVERSE, parentNodeName = JMS_DESTINATION_NAME_METAVERSE )
  @Metaverse.NodeLink ( nodeName = JMS_DESTINATION_NAME_METAVERSE, parentNodeName = JMS_DESTINATION_METAVERSE, linkDirection = "OUT" )
  public String destinationField = "destination";

  @Injection ( name = "MESSAGE_ID" )
  @Metaverse.Node ( name = JMS_MESSAGE_ID_METAVERSE, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = JMS_MESSAGE_ID_METAVERSE, parentNodeName = JMS_MESSAGE_ID_METAVERSE )
  @Metaverse.NodeLink ( nodeName = JMS_MESSAGE_ID_METAVERSE, parentNodeName = JMS_DESTINATION_METAVERSE, linkDirection = "OUT" )
  public String messageId = "messageId";

  @Injection ( name = "JMS_TIMESTAMP" )
  @Metaverse.Node ( name = JMS_TIMESTAMP_METAVERSE, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = JMS_TIMESTAMP_METAVERSE, parentNodeName = JMS_TIMESTAMP_METAVERSE )
  @Metaverse.NodeLink ( nodeName = JMS_TIMESTAMP_METAVERSE, parentNodeName = JMS_DESTINATION_METAVERSE, linkDirection = "OUT" )
  public String jmsTimestamp = "jmsTimestamp";

  @Injection ( name = "JMS_REDELIVERED" )
  @Metaverse.Node ( name = JMS_REDELIVERED_METAVERSE, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = JMS_REDELIVERED_METAVERSE, parentNodeName = JMS_REDELIVERED_METAVERSE )
  @Metaverse.NodeLink ( nodeName = JMS_REDELIVERED_METAVERSE, parentNodeName = JMS_DESTINATION_METAVERSE, linkDirection = "OUT" )
  public String jmsRedelivered = "jmsRedelivered";

  public JmsConsumerMeta() {
    this( new JmsDelegate() );
  }

  public JmsConsumerMeta( JmsDelegate jmsDelegate ) {
    setSpecificationMethod( FILENAME );
    this.jmsDelegate = jmsDelegate;
  }

  @SuppressWarnings( "deprecation" )
  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.jms.ui.JmsConsumerDialog";
  }

  @Override public RowMeta getRowMeta( String s, VariableSpace variableSpace ) {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( messageField ) );
    rowMeta.addValueMeta( new ValueMetaString( destinationField ) );
    rowMeta.addValueMeta( new ValueMetaString( messageId ) );
    rowMeta.addValueMeta( new ValueMetaString( jmsTimestamp ) );
    rowMeta.addValueMeta( new ValueMetaString( jmsRedelivered ) );
    return rowMeta;
  }

  public JmsDelegate getJmsDelegate() {
    return jmsDelegate;
  }

  public String getReceiveTimeout() {
    return receiveTimeout;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    return new JmsConsumer( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new GenericStepData();
  }

  /**
   * Creates a rowMeta for output field names
   */
  RowMetaInterface getRowMeta() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( messageField ) );
    rowMeta.addValueMeta( new ValueMetaString( destinationField ) );
    rowMeta.addValueMeta( new ValueMetaString( messageId ) );
    rowMeta.addValueMeta( new ValueMetaString( jmsTimestamp ) );
    rowMeta.addValueMeta( new ValueMetaString( jmsRedelivered ) );
    return rowMeta;
  }
}
