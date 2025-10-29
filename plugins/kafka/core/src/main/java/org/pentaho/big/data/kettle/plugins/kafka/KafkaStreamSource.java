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

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.streaming.common.BlockingQueueStreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerField.Name.OFFSET;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerField.Name.PARTITION;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerField.Name.TOPIC;

public class KafkaStreamSource extends BlockingQueueStreamSource<List<Object>> {

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  private final VariableSpace variables;
  private KafkaConsumerInputMeta kafkaConsumerInputMeta;
  private KafkaConsumerInputData kafkaConsumerInputData;
  private EnumMap<KafkaConsumerField.Name, Integer> positions;

  private Consumer consumer;
  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private KafkaConsumerCallable callable;
  private Future<Void> future;

  public KafkaStreamSource( Consumer consumer, KafkaConsumerInputMeta inputMeta,
                            KafkaConsumerInputData kafkaConsumerInputData, VariableSpace variables,
                            KafkaConsumerInput kafkaStep ) {
    super( kafkaStep );
    positions = new EnumMap<>( KafkaConsumerField.Name.class );
    this.consumer = consumer;
    this.variables = variables;
    this.kafkaConsumerInputData = kafkaConsumerInputData;
    this.kafkaConsumerInputMeta = inputMeta;
  }

  @Override public void close() {
    callable.shutdown();
  }

  @Override public void open()  {
    if ( future != null ) {
      logger.warn( "open() called more than once" );
      return;
    }

    List<ValueMetaInterface> valueMetas = kafkaConsumerInputData.outputRowMeta.getValueMetaList();
    positions = new EnumMap<>( KafkaConsumerField.Name.class );

    IntStream.range( 0, valueMetas.size() )
      .forEach( idx -> {
        Optional<KafkaConsumerField.Name> match = Arrays.stream( KafkaConsumerField.Name.values() )
          .filter( name -> {
            KafkaConsumerField f = name.getFieldFromMeta( kafkaConsumerInputMeta );
            String fieldName = variables.environmentSubstitute( f.getOutputName() );
            return fieldName != null && fieldName.equals( valueMetas.get( idx ).getName() );
          } )
          .findFirst();

        match.ifPresent( name -> positions.put( name, idx ) );
      } );

    callable = new KafkaConsumerCallable( consumer, super::close );
    future = executorService.submit( callable );
  }

  class KafkaConsumerCallable implements Callable<Void> {
    private final AtomicBoolean closed = new AtomicBoolean( false );
    private final Consumer consumer;
    private Runnable onClose;
    private ConcurrentLinkedQueue<Map<TopicPartition, OffsetAndMetadata>> toCommit = new ConcurrentLinkedQueue<>();

    public KafkaConsumerCallable( Consumer consumer, Runnable onClose ) {
      this.consumer = consumer;
      this.onClose = onClose;
    }

    public void queueCommit( Map<TopicPartition, OffsetAndMetadata> offsets ) {
      toCommit.add( offsets );
    }

    @Override public Void call() {
      try {
        while ( !closed.get() ) {
          commitOffsets();
          @SuppressWarnings( "unchecked" ) //should revisit generic type here
          ConsumerRecords<String, String> records = consumer.poll( 1000 );

          List<List<Object>> rows = new ArrayList<>();
          for ( ConsumerRecord<String, String> record : records ) {
            rows.add( processMessageAsRow( record ) );
          }

          acceptRows( rows );
        }
        return null;
      } catch ( WakeupException e ) {
        // Ignore exception if closing
        if ( !closed.get() ) {
          throw e;
        }
        return null;
      } catch ( Exception ef ) {
        KafkaStreamSource.this.streamStep.logError( "Exception consuming messages.", ef );
        return null;
      } finally {
        commitOffsets();
        consumer.close();
        onClose.run();
      }
    }

    private void commitOffsets() {
      while ( !toCommit.isEmpty() ) {
        consumer.commitSync( toCommit.poll() );
      }
    }

    // Shutdown hook which can be called from a separate thread
    public void shutdown() {
      closed.set( true );
      consumer.wakeup();
    }

  }

  public void commitOffsets( List<List<Object>> rows ) {
    Map<Object, Map<Object, Optional<List<Object>>>> maxRows = rows.stream().collect(
      groupingBy(
        row -> row.get( positions.get( TOPIC ) ),
        groupingBy(
          row -> row.get( positions.get( PARTITION ) ),
          maxBy( comparingLong( row -> (long) row.get( positions.get( OFFSET ) ) ) ) ) ) );

    Map<TopicPartition, OffsetAndMetadata> offsets =
      maxRows.values().stream().flatMap( m -> m.values().stream() ).map( Optional::get ).collect(
        Collectors.toMap( row -> new TopicPartition( (String) row.get( positions.get( TOPIC ) ),
            ( (Long) row.get( positions.get( PARTITION ) ) ).intValue() ),
          row -> new OffsetAndMetadata( (long) row.get( positions.get( OFFSET ) ) + 1 ) )
      );
    callable.queueCommit( offsets );
  }

  List<Object> processMessageAsRow( ConsumerRecord<String, String> record ) {
    Object[] rowData = RowDataUtil.allocateRowData( kafkaConsumerInputData.outputRowMeta.size() );

    if ( positions.get( KafkaConsumerField.Name.KEY ) != null ) {
      rowData[ positions.get( KafkaConsumerField.Name.KEY ) ] = record.key();
    }

    if ( positions.get( KafkaConsumerField.Name.MESSAGE ) != null ) {
      rowData[ positions.get( KafkaConsumerField.Name.MESSAGE ) ] = record.value();
    }

    if ( positions.get( TOPIC ) != null ) {
      rowData[ positions.get( TOPIC ) ] = record.topic();
    }

    if ( positions.get( PARTITION ) != null ) {
      rowData[ positions.get( PARTITION ) ] = (long) record.partition();
    }

    if ( positions.get( OFFSET ) != null ) {
      rowData[ positions.get( OFFSET ) ] = record.offset();
    }

    if ( positions.get( KafkaConsumerField.Name.TIMESTAMP ) != null ) {
      rowData[ positions.get( KafkaConsumerField.Name.TIMESTAMP ) ] = record.timestamp();
    }

    return Arrays.asList( rowData );
  }
}
