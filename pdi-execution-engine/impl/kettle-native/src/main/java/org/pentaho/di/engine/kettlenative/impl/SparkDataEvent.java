package org.pentaho.di.engine.kettlenative.impl;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.pentaho.di.engine.api.IData;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IPDIEventSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class SparkDataEvent implements IDataEvent {

  private final IExecutableOperation operation;
  private  STATE state;
  private final List<IData> data;
  private final JavaSparkContext sc;
  private FlatMapFunction<IData, IData> function;

  private final Optional<JavaRDD<IData>> parentRDD;

  public SparkDataEvent( IExecutableOperation op, STATE state,
                         List<IData> data, FlatMapFunction<IData, IData> function,
                         JavaSparkContext sc, Optional<JavaRDD<IData>> parentRDD ) {
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
  @Override public List<IData> getData() {
    List<IData> collect = getRDD().collect();
    if ( collect.size() == 0 ) {
      state = STATE.COMPLETE;
    }
    System.out.println( "*Materialized*");
    collect.stream()
      .forEach( data -> System.out.println( Arrays.toString( data.getData() ) ) );
    return collect;
  }

  JavaRDD<IData> getRDD() {
    JavaRDD<IData> rdd = parentRDD.map( prdd -> prdd.flatMap( function ) )    // RDD is incoming
      .orElse( sc.parallelize( data ).flatMap( function ) );     // No parent RDD, create one from incoming data
    System.out.println( rdd.toDebugString() );
    return rdd;
  }

  @Override public IPDIEventSource<IDataEvent> getEventSource() {
    return operation;
  }
}
