package org.pentaho.di.engine.kettlenative.impl;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.api.events.PDIEventSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class SparkDataEvent implements DataEvent {

  private final IExecutableOperation operation;
  private  STATE state;
  private final List<Row> data;
  private final JavaSparkContext sc;
  private FlatMapFunction<Row, Row> function;

  private final Optional<JavaRDD<Row>> parentRDD;

  public SparkDataEvent( IExecutableOperation op, STATE state,
                         List<Row> data, FlatMapFunction<Row, Row> function,
                         JavaSparkContext sc, Optional<JavaRDD<Row>> parentRDD ) {
    this.operation = op;
    this.state = state;
    this.function = function;
    this.data = data;
    this.sc = sc;
    this.parentRDD = parentRDD;
  }

  @Override public TYPE getType() {
    return null;
  }

  @Override public STATE getState() {
    return state;
  }

  // materializes rdd
  @Override public List<Row> getRows() {
    List<Row> collect = getRDD().collect();
    if ( collect.size() == 0 ) {
      state = STATE.COMPLETE;
    }
    System.out.println( "*Materialized*");
    collect.stream()
      .forEach( data -> System.out.println( Arrays.toString( data.getObjects().get() ) ) );
    return collect;
  }

  JavaRDD<Row> getRDD() {
    JavaRDD<Row> rdd = parentRDD.map( prdd -> prdd.flatMap( function ) )    // RDD is incoming
      .orElse( sc.parallelize( data ).flatMap( function ) );     // No parent RDD, create one from incoming data
    System.out.println( rdd.toDebugString() );
    return rdd;
  }

  @Override public PDIEventSource<DataEvent> getEventSource() {
    return operation;
  }
}
