package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableList;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.pentaho.di.engine.api.model.IRow;
import org.pentaho.di.engine.api.events.IDataEvent;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.api.model.ITransformation;
import org.pentaho.di.engine.api.converter.RowConversionManager;
import org.pentaho.di.engine.kettlenative.impl.sparkfun.CalcSparkFlatMapFunction;
import org.pentaho.di.trans.TransMeta;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.pentaho.di.engine.kettlenative.impl.KettleNativeUtil.getTransMeta;

public class SparkExecOperation extends KettleExecOperation {

  private final JavaSparkContext context;
  private final TransMeta transMeta;
  private final List<IRow> upstreamData = new ArrayList<>();

  RowConversionManager conversionManager
    = new RowConversionManager( ImmutableList.of( new KettleRowConverter(), new SparkRowConverter() ) );


  public SparkExecOperation( IOperation op, ITransformation transformation, JavaSparkContext sc) {
    super(op, transformation, null);
    this.transMeta =  getTransMeta( transformation );
    context = sc;
  }

  @Override public long getIn() {
    return 0;
  }

  @Override public long getOut() {
    return 0;
  }

  @Override public long getDropped() {
    return 0;
  }

  @Override public void onNext( IDataEvent dataEvent ) {
    Optional<JavaRDD<IRow>> parentRDD = Optional.empty();
    if ( previousStepIsSpark( dataEvent ) ) {
      // attach upstream RDD
      parentRDD = Optional.of( ((SparkDataEvent) dataEvent).getRDD() );

    } else {
      upstreamData.addAll( dataEvent.getRows() );
      // previous step is not spark.  Do we have all rows?
      if ( !dataEvent.getState().equals( IDataEvent.STATE.COMPLETE ) ) {
        return;
      }
    }
    IDataEvent nextEvent = new SparkDataEvent( this, IDataEvent.STATE.ACTIVE, upstreamData,
      new CalcSparkFlatMapFunction( transMeta, getId(), conversionManager ), context, parentRDD );
    getSubscribers().stream()
      .forEach( sub -> sub.onNext( nextEvent ) );
    getSubscribers().stream()
      .forEach( Subscriber::onComplete );
  }

  private boolean previousStepIsSpark( IDataEvent dataEvent ) {
    return dataEvent.getEventSource() instanceof SparkExecOperation;
  }

  @Override public String toString() {
    return "Spark Operation:  " + getId() + "\n";

  }

}
