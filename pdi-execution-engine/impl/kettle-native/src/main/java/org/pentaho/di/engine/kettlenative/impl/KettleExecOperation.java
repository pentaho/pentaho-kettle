package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.base.Preconditions;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.engine.api.IData;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IPDIEventSource;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.Status;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RunThread;
import org.pentaho.di.trans.step.StepAdapter;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pentaho.di.engine.kettlenative.impl.KettleNativeUtil.createTrans;
import static org.pentaho.di.engine.kettlenative.impl.KettleNativeUtil.getTransMeta;

public class KettleExecOperation implements IExecutableOperation, Subscriber<IDataEvent> {

  private final IOperation operation;
  private transient final Trans trans;
  private transient final ExecutorService executor;
  private List<Subscriber<? super IDataEvent>> subscribers = new ArrayList<>();
  private AtomicBoolean done = new AtomicBoolean( false );
  private transient StepDataInterface data;
  private transient StepInterface step;
  private transient StepMeta stepMeta;
  private transient TransMeta transMeta;

  private AtomicBoolean started = new AtomicBoolean( false );

  protected KettleExecOperation( IOperation op, ITransformation transformation, ExecutorService executorService ) {
    this.operation = op;
    trans = createTrans();
    transMeta = getTransMeta( transformation );
    stepMeta = transMeta.findStep( op.getId() );
    this.executor = executorService;
    initializeStepMeta();
  }

  public static IExecutableOperation compile( IOperation operation, ITransformation trans,
                                              ExecutorService executorService ) {
    return new KettleExecOperation( operation, trans, executorService );
  }

  @Override public void subscribe( Subscriber<? super IDataEvent> subscriber ) {
    subscribers.add( subscriber );
  }

  @Override public boolean isRunning() {
    return !done.get();
  }

  @Override public void subscribeTo( IPDIEventSource<IDataEvent> source ) {
    source.subscribe( this );
  }

  @Override public void start() {
    onNext( KettleDataEvent.empty() );
  }

  @Override public IOperation getParent() {
    return operation;
  }

  @Override public Metrics getMetrics() {
    return new Metrics( getIn(), getOut(), getDropped(), getInFlight() );
  }

  @Override public String getId() {
    return operation.getId();
  }

  @Override public void onComplete() {

  }

  @Override public void onError( Throwable throwable ) {

  }

  @Override public void onSubscribe( Subscription subscription ) {

  }

  @Override public void onNext( IDataEvent dataEvent ) {
    Preconditions.checkNotNull( dataEvent );
    if ( !started.getAndSet( true ) ) {
      startStep();
    }
    try {
      switch( dataEvent.getState() ) {
        case ACTIVE:
          final RowMetaInterface rowMetaInterface = getRowMetaInterface( dataEvent );
          getInputRowset( dataEvent ).ifPresent(
            rowset -> {
              List<IData> data = dataEvent.getData();
              if ( data.size() > 1 ) {
                // TEMP hack, this only happens with Spark right now
                data.stream()
                  .forEach( d -> rowset.putRow( rowMetaInterface, d.getData() ) );
                rowset.putRow( rowMetaInterface, null );
                rowset.setDone();
                return;
              } else {
                rowset.putRow(
                  rowMetaInterface, data.get( 0 ).getData() );
              }
            } );  // TODO assuming 1 for now

          break;
        case COMPLETE:
          // terminal row
          terminalRow( dataEvent );
          break;
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }

  }

  private void terminalRow( IDataEvent dataEvent ) {
    try {
      getInputRowset( dataEvent ).ifPresent( rowset -> rowset.setDone() );
    } catch ( KettleStepException e ) {
      throw new RuntimeException( e );
    }
  }

  private RowMetaInterface getRowMetaInterface( IDataEvent dataEvent ) {
    RowMetaInterface rowMetaInterface = new RowMeta();
    rowMetaInterface.addValueMeta( new ValueMetaString( "name" ) );
    if ( dataEvent instanceof KettleDataEvent ) {
      rowMetaInterface = ( (KettleDataEvent) dataEvent ).getRowMeta();
    }
    return rowMetaInterface;
  }

  private Optional<RowSet> getInputRowset( IDataEvent dataEvent ) throws KettleStepException {
    return Optional.ofNullable( ( (BaseStep) step ).findInputRowSet( dataEvent.getEventSource().getId() ) );
  }

  private void startStep() {
    StepMetaDataCombi combi = new StepMetaDataCombi();
    combi.step = step;
    combi.meta = stepMeta.getStepMetaInterface();
    combi.data = data;
    executor.submit( new RunThread( combi ) );
  }

  private void initializeStepMeta() {
    trans.setRunning( true );


    List<RowSet> outRowSets = nextStepStream()
      .map( next -> createRowSet( stepMeta, next ) )
      .collect( Collectors.toList() );
    List<RowSet> inRowSets = prevStepStream()
      .map( prev -> createRowSetNoSub( prev, stepMeta ) )
      .collect( Collectors.toList() );

    trans.getRowsets().addAll( outRowSets );
    trans.getRowsets().addAll( inRowSets );

    data = stepMeta.getStepMetaInterface().getStepData();
    step = stepMeta.getStepMetaInterface().getStep( stepMeta, data, 0, stepMeta.getParentTransMeta(), trans );

    // Copy the variables of the transformation to the step...
    // don't share. Each copy of the step has its own variables.
    step.initializeVariablesFrom( stepMeta.getParentTransMeta() );
    step.init( stepMeta.getStepMetaInterface(), data );
    step.setUsingThreadPriorityManagment( false );

    step.addStepListener( new StepAdapter() {
      @Override
      public void stepFinished( Trans trans, StepMeta stepMeta, StepInterface step ) {
        done.set( true );
        subscribers.stream()
          .forEach( sub -> {
            sub.onNext( KettleDataEvent.complete( KettleExecOperation.this ) ); // terminal row
            sub.onComplete();
          } );

      }
    } );
    ( (BaseStep) step ).dispatch();

    step.setRepository( null );
    step.setMetaStore( null );
  }

  private Stream<StepMeta> prevStepStream() {
    return stepMeta.getParentTransMeta().findPreviousSteps( stepMeta ).stream();
  }

  private Stream<StepMeta> nextStepStream() {
    return stepMeta.getParentTransMeta().findNextSteps( stepMeta ).stream();
  }

  protected List<Subscriber<? super IDataEvent>> getSubscribers() {
    return subscribers;
  }

  private RowSet createRowSetNoSub( StepMeta prev, StepMeta next ) {
    RowSet out = new QueueRowSet();
    out.setThreadNameFromToCopy( prev.getName(), 0, next.getName(), 0 );
    return out;
  }

  private RowSet createRowSet( StepMeta prev, StepMeta next ) {
    RowSet out = new QueueRowSet() {
      @Override public boolean putRow( RowMetaInterface rowMeta, Object[] rowData ) {
        getSubscriber().ifPresent( sub -> sub.onNext(
          KettleDataEvent.active( KettleExecOperation.this, rowMeta, rowData ) ) );
        return super.putRow( rowMeta, rowData );
      }

      private Optional<Subscriber<? super IDataEvent>> getSubscriber() {
        return KettleExecOperation.this.subscribers.stream()
          .filter( sub -> ( (IExecutableOperation) sub ).getId().equals( next.getName() ) )
          .findFirst();
      }
    };
    out.setThreadNameFromToCopy( prev.getName(), 0, next.getName(), 0 );
    return out;
  }


  public long getIn() {
    return step.getLinesRead();
  }

  public long getOut() {
    return step.getLinesOutput() + step.getLinesWritten();
  }

  public long getDropped() {
    return step.getLinesRejected();
  }

  public long getInFlight() {
    return 0; // ?
  }

  public Status getStatus() {
    return isRunning() ? Status.RUNNING : Status.FINISHED;
  }

  @Override public String toString() {
    StringBuilder theString = new StringBuilder();
    theString.append( "Operation " + this.getId() + "\n" );
    theString.append( " IN:   " + getIn() + "\n" );
    theString.append( " OUT:  " + getOut() + "\n" );
    return theString.toString();
  }
}
