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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.plugins.ParentFirst;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.hadoop.shim.api.cluster.NamedCluster;
import org.pentaho.hadoop.shim.api.cluster.NamedClusterService;
import org.pentaho.hadoop.shim.api.services.BigDataServicesProxy;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerInputMeta.ConnectionType.DIRECT;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaLineageConstants.KAFKA_SERVER_METAVERSE;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaLineageConstants.KAFKA_TOPIC_METAVERSE;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaLineageConstants.KEY_FIELD_NAME;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaLineageConstants.MSG_FIELD_NAME;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_DATASOURCE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_MESSAGE_QUEUE;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_INPUTS;
import static org.pentaho.dictionary.DictionaryConst.LINK_PARENT_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_READBY;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_EXTERNAL_CONNECTION;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.FALSE;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.SUBTRANS_INPUT;
import static org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer.RESOURCE;

@Step( id = "KafkaConsumerInput", image = "KafkaConsumerInput.svg",
  i18nPackageName = "org.pentaho.big.data.kettle.plugins.kafka",
  name = "KafkaConsumer.TypeLongDesc",
  description = "KafkaConsumer.TypeTooltipDesc",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Streaming" )
@InjectionSupported( localizationPrefix = "KafkaConsumerInputMeta.Injection.", groups = { "CONFIGURATION_PROPERTIES" } )
@Metaverse.CategoryMap ( entity = KAFKA_TOPIC_METAVERSE, category = CATEGORY_MESSAGE_QUEUE )
@Metaverse.CategoryMap ( entity = KAFKA_SERVER_METAVERSE, category = CATEGORY_DATASOURCE )
@Metaverse.EntityLink ( entity = KAFKA_SERVER_METAVERSE, link = LINK_PARENT_CONCEPT, parentEntity = NODE_TYPE_EXTERNAL_CONNECTION )
@Metaverse.EntityLink ( entity = KAFKA_TOPIC_METAVERSE, link = LINK_CONTAINS_CONCEPT, parentEntity = KAFKA_SERVER_METAVERSE )
@Metaverse.EntityLink ( entity = KAFKA_TOPIC_METAVERSE, link = LINK_PARENT_CONCEPT )
@ParentFirst( patterns = { ".*" } )
public class KafkaConsumerInputMeta extends BaseStreamStepMeta implements StepMetaInterface {
  public enum ConnectionType {
    DIRECT,
    CLUSTER
  }

  public static final String CLUSTER_NAME = "clusterName";
  public static final String TOPIC = "topic";
  public static final String CONSUMER_GROUP = "consumerGroup";
  public static final String TRANSFORMATION_PATH = "transformationPath";
  public static final String BATCH_SIZE = "batchSize";
  public static final String BATCH_DURATION = "batchDuration";
  public static final String PREFETCH_MESSAGE_COUNT = "prefetchMessageCount";
  public static final String CONNECTION_TYPE = "connectionType";
  public static final String DIRECT_BOOTSTRAP_SERVERS = "directBootstrapServers";
  public static final String ADVANCED_CONFIG = "advancedConfig";
  public static final String CONFIG_OPTION = "option";
  public static final String OPTION_PROPERTY = "property";
  public static final String OPTION_VALUE = "value";
  public static final String TOPIC_FIELD_NAME = TOPIC;
  public static final String OFFSET_FIELD_NAME = "offset";
  public static final String PARTITION_FIELD_NAME = "partition";
  public static final String TIMESTAMP_FIELD_NAME = "timestamp";
  public static final String OUTPUT_FIELD_TAG_NAME = "OutputField";
  public static final String KAFKA_NAME_ATTRIBUTE = "kafkaName";
  public static final String TYPE_ATTRIBUTE = "type";
  public static final String AUTO_COMMIT = "AUTO_COMMIT";

  private static final Class<?> PKG = KafkaConsumerInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  @Injection( name = "CONNECTION_TYPE" )
  private ConnectionType connectionType = DIRECT;

  @Injection( name = "DIRECT_BOOTSTRAP_SERVERS" )
  private String directBootstrapServers;

  @Injection( name = "CLUSTER_NAME" )
  private String clusterName;

  @Injection( name = "TOPICS" )
  private List<String> topics = new ArrayList<>();

  @Injection( name = "CONSUMER_GROUP" )
  private String consumerGroup;

  @InjectionDeep( prefix = "KEY" )
  private KafkaConsumerField keyField;

  @InjectionDeep( prefix = "MESSAGE" )
  private KafkaConsumerField messageField;

  @Injection( name = "NAMES", group = "CONFIGURATION_PROPERTIES" )
  public List<String> injectedConfigNames;

  @Injection( name = "VALUES", group = "CONFIGURATION_PROPERTIES" )
  public List<String> injectedConfigValues;

  @Injection( name = AUTO_COMMIT )
  private boolean autoCommit = true;

  private Map<String, String> config = new LinkedHashMap<>();

  private KafkaConsumerField topicField;

  private KafkaConsumerField offsetField;

  private KafkaConsumerField partitionField;

  private KafkaConsumerField timestampField;

  protected KafkaFactory kafkaFactory;

  private NamedClusterService namedClusterService;

  private MetastoreLocator metastoreLocator;

  //private NamedClusterServiceLocator namedClusterServiceLocator;

  public KafkaConsumerInputMeta() {
    super(); // allocate BaseStepMeta
    kafkaFactory = KafkaFactory.defaultFactory();
    prepare();
  }

  protected  void prepare() {
    try {
      Collection<MetastoreLocator> metastoreLocators = PluginServiceLoader.loadServices( MetastoreLocator.class );
      this.metastoreLocator = metastoreLocators.stream().findFirst().get();
    } catch ( Exception e ) {
      getLog().logError( "Error getting MetastoreLocator", e );
    }

    try {
      Collection<BigDataServicesProxy> bigDataServicesProxies = PluginServiceLoader.loadServices( BigDataServicesProxy.class);
      if ( bigDataServicesProxies != null && !bigDataServicesProxies.isEmpty() ) {
        BigDataServicesProxy bigDataServicesProxy = bigDataServicesProxies.stream().findFirst().get();
        this.namedClusterService = bigDataServicesProxy.getNamedClusterService();
      }
    } catch ( Exception e ) {
      getLog().logError( "Error getting NamedClusterService", e );
    }


    keyField = new KafkaConsumerField(
       KafkaConsumerField.Name.KEY,
       BaseMessages.getString( PKG, "KafkaConsumerInputDialog.KeyField" )
    );

    messageField = new KafkaConsumerField(
       KafkaConsumerField.Name.MESSAGE,
       BaseMessages.getString( PKG, "KafkaConsumerInputDialog.MessageField" )
    );

    topicField = new KafkaConsumerField(
      KafkaConsumerField.Name.TOPIC,
      BaseMessages.getString( PKG, "KafkaConsumerInputDialog.TopicField" )
    );

    partitionField = new KafkaConsumerField(
      KafkaConsumerField.Name.PARTITION,
      BaseMessages.getString( PKG, "KafkaConsumerInputDialog.PartitionField" ),
      KafkaConsumerField.Type.Integer
    );

    offsetField = new KafkaConsumerField(
      KafkaConsumerField.Name.OFFSET,
      BaseMessages.getString( PKG, "KafkaConsumerInputDialog.OffsetField" ),
      KafkaConsumerField.Type.Integer
    );

    timestampField = new KafkaConsumerField(
      KafkaConsumerField.Name.TIMESTAMP,
      BaseMessages.getString( PKG, "KafkaConsumerInputDialog.TimestampField" ),
      KafkaConsumerField.Type.Integer
    );
    setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Override public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) {
    readData( stepnode );
  }

  private void readData( Node stepnode ) {
    setClusterName( XMLHandler.getTagValue( stepnode, CLUSTER_NAME ) );

    List<Node> topicsNode = XMLHandler.getNodes( stepnode, TOPIC );
    topicsNode.forEach( node -> {
      String displayName = XMLHandler.getNodeValue( node );
      addTopic( displayName );
    } );

    setConsumerGroup( XMLHandler.getTagValue( stepnode, CONSUMER_GROUP ) );
    setTransformationPath( XMLHandler.getTagValue( stepnode, TRANSFORMATION_PATH ) );
    String subStepTag = XMLHandler.getTagValue( stepnode, SUB_STEP );
    if ( !StringUtil.isEmpty( subStepTag ) ) {
      setSubStep( subStepTag );
    }
    setFileName( XMLHandler.getTagValue( stepnode, TRANSFORMATION_PATH ) );
    setBatchSize( Optional.ofNullable( XMLHandler.getTagValue( stepnode, BATCH_SIZE ) ).orElse( "" ) );
    setBatchDuration( Optional.ofNullable( XMLHandler.getTagValue( stepnode, BATCH_DURATION ) ).orElse( "" ) );
    String parallelism = XMLHandler.getTagValue( stepnode, PARALLELISM );
    setParallelism( isNullOrEmpty( parallelism ) ? "1" : parallelism );
    String prefetchCount = XMLHandler.getTagValue( stepnode, PREFETCH_MESSAGE_COUNT );
    setPrefetchCount( isNullOrEmpty( prefetchCount ) ? PREFETCH_DEFAULT : prefetchCount );
    setConnectionType( ConnectionType.valueOf( XMLHandler.getTagValue( stepnode, CONNECTION_TYPE ) ) );
    setDirectBootstrapServers( XMLHandler.getTagValue( stepnode, DIRECT_BOOTSTRAP_SERVERS ) );
    String autoCommitValue = XMLHandler.getTagValue( stepnode, AUTO_COMMIT );
    setAutoCommit( "Y".equals( autoCommitValue ) || isNullOrEmpty( autoCommitValue ) );
    List<Node> ofNode = XMLHandler.getNodes( stepnode, OUTPUT_FIELD_TAG_NAME );

    ofNode.forEach( node -> {
      String displayName = XMLHandler.getNodeValue( node );
      String kafkaName = XMLHandler.getTagAttribute( node, KAFKA_NAME_ATTRIBUTE );
      String type = XMLHandler.getTagAttribute( node, TYPE_ATTRIBUTE );
      KafkaConsumerField field = new KafkaConsumerField(
              KafkaConsumerField.Name.valueOf( kafkaName.toUpperCase() ),
              displayName,
              KafkaConsumerField.Type.valueOf( type ) );

      setField( field );
    } );

    config = new LinkedHashMap<>();

    Optional.ofNullable( XMLHandler.getSubNode( stepnode, ADVANCED_CONFIG ) ).map( Node::getChildNodes )
        .ifPresent( nodes -> IntStream.range( 0, nodes.getLength() ).mapToObj( nodes::item )
            .filter( node -> node.getNodeType() == Node.ELEMENT_NODE )
            .forEach( node -> {
              if ( CONFIG_OPTION.equals( node.getNodeName() ) ) {
                config.put( node.getAttributes().getNamedItem( OPTION_PROPERTY ).getTextContent(),
                            node.getAttributes().getNamedItem( OPTION_VALUE ).getTextContent() );
              } else {
                config.put( node.getNodeName(), node.getTextContent() );
              }
            } ) );
  }

  protected void setField( KafkaConsumerField field ) {
    field.getKafkaName().setFieldOnMeta( this, field );
  }

  @Override public void setDefault() {
    batchSize = "1000";
    batchDuration = "1000";
    parallelism = "1";
    prefetchCount = PREFETCH_DEFAULT;
  }

  @Override public void readRep( Repository rep, IMetaStore metaStore, ObjectId objectId, List<DatabaseMeta>
 databases )
    throws KettleException {
    setClusterName( rep.getStepAttributeString( objectId, CLUSTER_NAME ) );

    int topicCount = rep.countNrStepAttributes( objectId, TOPIC );
    for ( int i = 0; i < topicCount; i++ ) {
      addTopic( rep.getStepAttributeString( objectId, i, TOPIC ) );
    }

    setConsumerGroup( rep.getStepAttributeString( objectId, CONSUMER_GROUP ) );
    setTransformationPath( rep.getStepAttributeString( objectId, TRANSFORMATION_PATH ) );
    setSubStep( rep.getStepAttributeString( objectId, SUB_STEP ) );
    setFileName( rep.getStepAttributeString( objectId, TRANSFORMATION_PATH ) );
    setBatchSize( Optional.ofNullable( rep.getStepAttributeString( objectId, BATCH_SIZE ) ).orElse( "" ) );
    setBatchDuration( Optional.ofNullable( rep.getStepAttributeString( objectId, BATCH_DURATION ) ).orElse( "" ) );
    String parallelism = rep.getStepAttributeString( objectId, PARALLELISM );
    setParallelism( isNullOrEmpty( parallelism ) ? "1" : parallelism );
    String prefetchCount = rep.getStepAttributeString( objectId, PREFETCH_MESSAGE_COUNT );
    setPrefetchCount( isNullOrEmpty( prefetchCount ) ? PREFETCH_DEFAULT : prefetchCount );
    setConnectionType( ConnectionType.valueOf( rep.getStepAttributeString( objectId, CONNECTION_TYPE ) ) );
    setDirectBootstrapServers( rep.getStepAttributeString( objectId, DIRECT_BOOTSTRAP_SERVERS ) );
    setAutoCommit( rep.getStepAttributeBoolean( objectId, 0, AUTO_COMMIT, true ) );

    for ( KafkaConsumerField.Name name : KafkaConsumerField.Name.values() ) {
      String prefix = OUTPUT_FIELD_TAG_NAME + "_" + name;
      String value = rep.getStepAttributeString( objectId, prefix );
      String type = rep.getStepAttributeString( objectId, prefix + "_" + TYPE_ATTRIBUTE );
      if ( value != null ) {
        setField( new KafkaConsumerField( name, value, KafkaConsumerField.Type.valueOf( type ) ) );
      }
    }

    config = new LinkedHashMap<>();

    for ( int i = 0; i < rep.getStepAttributeInteger( objectId, ADVANCED_CONFIG + "_COUNT" ); i++ ) {
      config.put( rep.getStepAttributeString( objectId, i, ADVANCED_CONFIG + "_NAME" ),
          rep.getStepAttributeString( objectId, i, ADVANCED_CONFIG + "_VALUE" ) );
    }
  }

  @Override public void saveRep( Repository rep, IMetaStore metaStore, ObjectId transId, ObjectId stepId )
    throws KettleException {
    rep.saveStepAttribute( transId, stepId, CLUSTER_NAME, clusterName );

    int i = 0;
    for ( String topic : topics ) {
      rep.saveStepAttribute( transId, stepId, i++, TOPIC, topic );
    }

    rep.saveStepAttribute( transId, stepId, CONSUMER_GROUP, consumerGroup );
    rep.saveStepAttribute( transId, stepId, TRANSFORMATION_PATH, transformationPath );
    rep.saveStepAttribute( transId, stepId, SUB_STEP, getSubStep() );
    rep.saveStepAttribute( transId, stepId, BATCH_SIZE, batchSize );
    rep.saveStepAttribute( transId, stepId, BATCH_DURATION, batchDuration );
    rep.saveStepAttribute( transId, stepId, PARALLELISM, parallelism );
    rep.saveStepAttribute( transId, stepId, PREFETCH_MESSAGE_COUNT, prefetchCount );
    rep.saveStepAttribute( transId, stepId, CONNECTION_TYPE, connectionType.name() );
    rep.saveStepAttribute( transId, stepId, DIRECT_BOOTSTRAP_SERVERS, directBootstrapServers );
    rep.saveStepAttribute( transId, stepId, AUTO_COMMIT, autoCommit );

    List<KafkaConsumerField> fields = getFieldDefinitions();
    for ( KafkaConsumerField field : fields ) {
      String prefix = OUTPUT_FIELD_TAG_NAME + "_" + field.getKafkaName().toString();
      rep.saveStepAttribute( transId, stepId, prefix, field.getOutputName() );
      rep.saveStepAttribute( transId, stepId, prefix + "_" + TYPE_ATTRIBUTE, field.getOutputType().toString() );
    }

    rep.saveStepAttribute( transId, stepId, ADVANCED_CONFIG + "_COUNT", getConfig().size() );

    i = 0;
    for ( String propName : getConfig().keySet() ) {
      rep.saveStepAttribute( transId, stepId, i, ADVANCED_CONFIG + "_NAME", propName );
      rep.saveStepAttribute( transId, stepId, i++, ADVANCED_CONFIG + "_VALUE", getConfig().get( propName ) );
    }
  }

  @Override
  public RowMeta getRowMeta( String origin, VariableSpace space ) throws KettleStepException {
    RowMeta rowMeta = new RowMeta();
    putFieldOnRowMeta( getKeyField(), rowMeta, origin, space );
    putFieldOnRowMeta( getMessageField(), rowMeta, origin, space );
    putFieldOnRowMeta( getTopicField(), rowMeta, origin, space );
    putFieldOnRowMeta( getPartitionField(), rowMeta, origin, space );
    putFieldOnRowMeta( getOffsetField(), rowMeta, origin, space );
    putFieldOnRowMeta( getTimestampField(), rowMeta, origin, space );
    return rowMeta;
  }

  void putFieldOnRowMeta( KafkaConsumerField field, RowMetaInterface rowMeta,
                          String origin, VariableSpace space ) throws KettleStepException {
    if ( field != null && !Utils.isEmpty( field.getOutputName() ) ) {
      try {
        String value = space.environmentSubstitute( field.getOutputName() );
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( value,
        field.getOutputType().getValueMetaInterfaceType() );
        v.setOrigin( origin );
        rowMeta.addValueMeta( v );
      } catch ( KettlePluginException e ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG,
     "KafkaConsumerInputMeta.UnableToCreateValueType",
          field
        ), e );
      }
    }
  }


  @Override public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans ) {
    return new KafkaConsumerInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new KafkaConsumerInputData();
  }

  public void setTopics( List<String> topics ) {
    this.topics = topics;
  }

  public void addTopic( String topic ) {
    this.topics.add( topic );
  }

  public void setConsumerGroup( String consumerGroup ) {
    this.consumerGroup = consumerGroup;
  }

  @Metaverse.Node ( name = KAFKA_SERVER_METAVERSE, type = KAFKA_SERVER_METAVERSE )
  @Metaverse.Property ( name = KAFKA_SERVER_METAVERSE, parentNodeName = KAFKA_SERVER_METAVERSE )
  public String getBootstrapServers() {
    if ( DIRECT.equals( getConnectionType() ) ) {
      return getDirectBootstrapServers();
    }
    if ( namedClusterService != null ) {
      NamedCluster namedCluster = namedClusterService.getNamedClusterByName(parentStepMeta.getParentTransMeta().environmentSubstitute(clusterName), metastoreLocator.getMetastore());
      if (namedCluster != null) {
        return namedCluster.getKafkaBootstrapServers();
      }
    }
    getLog().logError( "Unable to get the named cluster service" );
    return "";
  }

  @Metaverse.Node ( name = KAFKA_TOPIC_METAVERSE, type = KAFKA_TOPIC_METAVERSE, link = LINK_READBY )
  @Metaverse.Property ( name = TOPIC, parentNodeName = KAFKA_TOPIC_METAVERSE )
  public List<String> getTopics() {
    return topics;
  }

  public String getConsumerGroup() {
    return consumerGroup;
  }

  public KafkaConsumerField getKeyField() {
    return keyField;
  }

  public KafkaConsumerField getMessageField() {
    return messageField;
  }

  public KafkaConsumerField getTopicField() {
    return topicField;
  }

  public KafkaConsumerField getOffsetField() {
    return offsetField;
  }

  public KafkaConsumerField getPartitionField() {
    return partitionField;
  }

  public KafkaConsumerField getTimestampField() {
    return timestampField;
  }

  public String getDirectBootstrapServers() {
    return directBootstrapServers;
  }

  public void setKeyField( KafkaConsumerField keyField ) {
    this.keyField = keyField;
  }

  public void setMessageField( KafkaConsumerField messageField ) {
    this.messageField = messageField;
  }

  public void setTopicField( KafkaConsumerField topicField ) {
    this.topicField = topicField;
  }

  public void setOffsetField( KafkaConsumerField offsetField ) {
    this.offsetField = offsetField;
  }

  public void setPartitionField( KafkaConsumerField partitionField ) {
    this.partitionField = partitionField;
  }

  public void setTimestampField( KafkaConsumerField timestampField ) {
    this.timestampField = timestampField;
  }

  public void setDirectBootstrapServers( final String directBootstrapServers ) {
    this.directBootstrapServers = directBootstrapServers;
  }

  public ConnectionType getConnectionType() {
    return connectionType;
  }

  public void setConnectionType( final ConnectionType connectionType ) {
    this.connectionType = connectionType;
  }

  @Override public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "    " ).append( XMLHandler.addTagValue( CLUSTER_NAME, clusterName ) );
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( "hc://" + clusterName );

    getTopics().forEach( topic ->
      retval.append( "    " ).append( XMLHandler.addTagValue( TOPIC, topic ) ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( CONSUMER_GROUP, consumerGroup ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( TRANSFORMATION_PATH, transformationPath ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( SUB_STEP, getSubStep() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( BATCH_SIZE, batchSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( BATCH_DURATION, batchDuration ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( PARALLELISM, parallelism ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( PREFETCH_MESSAGE_COUNT, prefetchCount ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONNECTION_TYPE, connectionType.name() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( DIRECT_BOOTSTRAP_SERVERS, directBootstrapServers ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( AUTO_COMMIT, autoCommit ) );

    getFieldDefinitions().forEach( field ->
            retval.append( "    " ).append(
              XMLHandler.addTagValue( OUTPUT_FIELD_TAG_NAME, field.getOutputName(), true,
                KAFKA_NAME_ATTRIBUTE, field.getKafkaName().toString(),
                TYPE_ATTRIBUTE, field.getOutputType().toString() ) ) );

    retval.append( "    " ).append( XMLHandler.openTag( ADVANCED_CONFIG ) ).append( Const.CR );
    getConfig().forEach( ( key, value ) -> retval.append( "        " )
        .append( XMLHandler.addTagValue( CONFIG_OPTION, "", true,
                    OPTION_PROPERTY, (String) key, OPTION_VALUE, (String) value ) ) );
    retval.append( "    " ).append( XMLHandler.closeTag( ADVANCED_CONFIG ) ).append( Const.CR );

    return retval.toString();
  }

  public List<KafkaConsumerField> getFieldDefinitions() {
    return Lists.newArrayList(
      getKeyField(),
      getMessageField(),
      getTopicField(),
      getPartitionField(),
      getOffsetField(),
      getTimestampField() );
  }

  public KafkaFactory getKafkaFactory() {
    return kafkaFactory;
  }

  protected void setKafkaFactory( KafkaFactory kafkaFactory ) {
    this.kafkaFactory = kafkaFactory;
  }

  public NamedClusterService getNamedClusterService() {
    return namedClusterService;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName( String clusterName ) {
    this.clusterName = clusterName;
  }

  public MetastoreLocator getMetastoreLocator() {
    return metastoreLocator;
  }

  public void setNamedClusterService( NamedClusterService namedClusterService ) {
    this.namedClusterService = namedClusterService;
  }

  public void setAutoCommit( boolean autoCommit ) {
    this.autoCommit = autoCommit;
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  /*
     Per https://jira.pentaho.com/browse/PDI-19585 this capability was never reproduced when the multishim
     capability was added.  It has been missing since Pentaho 9.0.
   */
//  public Optional<JaasConfigService> getJaasConfigService() {
//    if ( DIRECT.equals( getConnectionType() ) ) {
//      return Optional.empty();
//    }
//    try {
//      return Optional.ofNullable( namedClusterServiceLocator.getService(
//        namedClusterService.getNamedClusterByName( parentStepMeta.getParentTransMeta().environmentSubstitute( getClusterName() ), getMetastoreLocator().getMetastore() ),
//        JaasConfigService.class ) );
//    } catch ( Exception e ) {
//      getLog().logDebug( "problem getting jaas config", e );
//      return Optional.empty();
//    }
//  }

//  public NamedClusterServiceLocator getNamedClusterServiceLocator() {
//    return namedClusterServiceLocator;
//  }
//
//  public void setNamedClusterServiceLocator( NamedClusterServiceLocator namedClusterServiceLocator ) {
//    this.namedClusterServiceLocator = namedClusterServiceLocator;
//  }

  public void setMetastoreLocator( MetastoreLocator metastoreLocator ) {
    this.metastoreLocator = metastoreLocator;
  }

  public void setConfig( Map<String, String> config ) {
    this.config = config;
  }

  public Map<String, String> getConfig() {
    applyInjectedProperties();
    return config;
  }

  public void applyInjectedProperties() {
    if ( injectedConfigNames != null || injectedConfigValues != null ) {
      Preconditions.checkState( injectedConfigNames != null, "Options names were not injected" );
      Preconditions.checkState( injectedConfigValues != null, "Options values were not injected" );
      Preconditions.checkState( injectedConfigNames.size() == injectedConfigValues.size(),
  "Injected different number of options names and value" );

      setConfig( IntStream.range( 0, injectedConfigNames.size() ).boxed().collect( Collectors
              .toMap( injectedConfigNames::get, injectedConfigValues::get, ( v1, v2 ) -> v1,
                      LinkedHashMap::new ) ) );

      injectedConfigNames = null;
      injectedConfigValues = null;
    }
  }

  @Metaverse.Node ( name = KEY_FIELD_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = KEY_FIELD_NAME, parentNodeName = KEY_FIELD_NAME )
  @Metaverse.NodeLink ( nodeName = KEY_FIELD_NAME, parentNodeName = KAFKA_TOPIC_METAVERSE, linkDirection = "OUT" )
  public String getKeyOutputName() {
    return keyField.getOutputName();
  }

  @Metaverse.Node ( name = MSG_FIELD_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.NodeLink ( nodeName = MSG_FIELD_NAME, parentNodeName = KAFKA_TOPIC_METAVERSE, linkDirection = "OUT" )
  public String getMessageOutputName() {
    return messageField.getOutputName();
  }

  @Metaverse.Node ( name = TOPIC_FIELD_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.NodeLink ( nodeName = TOPIC_FIELD_NAME, parentNodeName = KAFKA_TOPIC_METAVERSE, linkDirection = "OUT" )
  public String getTopicOutputName() {
    return topicField.getOutputName();
  }

  @Metaverse.Node ( name = PARTITION_FIELD_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.NodeLink ( nodeName = PARTITION_FIELD_NAME, parentNodeName = KAFKA_TOPIC_METAVERSE, linkDirection = "OUT" )
  public String getPartitionOutputName() {
    return partitionField.getOutputName();
  }

  @Metaverse.Node ( name = OFFSET_FIELD_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.NodeLink ( nodeName = OFFSET_FIELD_NAME, parentNodeName = KAFKA_TOPIC_METAVERSE, linkDirection = "OUT" )
  public String getOffsetOutputName() {
    return offsetField.getOutputName();
  }

  @Metaverse.Node ( name = TIMESTAMP_FIELD_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.NodeLink ( nodeName = TIMESTAMP_FIELD_NAME, parentNodeName = KAFKA_TOPIC_METAVERSE, linkDirection = "OUT" )
  public String getTimestampOutputName() {
    return timestampField.getOutputName();
  }

}
