package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.SingleRowRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.PrimitiveGCInterface;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IOperationVisitor;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowDistributionInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KettleExecOperation implements IExecutableOperation {

  private final IOperation operation;
  private final Trans trans;
  private List<Subscriber<? super IDataEvent>> subscribers = new ArrayList<>();
  private AtomicBoolean done = new AtomicBoolean( false );
  private StepDataInterface data;
  private StepInterface step;

  private StepMeta stepMeta;

  private TransMeta transMeta;
  private AtomicInteger inCount = new AtomicInteger( 0 );
  private AtomicInteger outCount = new AtomicInteger( 0 );
  private AtomicInteger droppedCount = new AtomicInteger( 0 );
  private AtomicInteger inFlightCount = new AtomicInteger( 0 );


  protected KettleExecOperation( IOperation op, ITransformation transformation ) {
    this.operation = op;
    trans = createTrans();
    transMeta = getTransMeta( op, transformation );
    stepMeta = transMeta.findStep( op.getId() );

    initializeStepMeta();
  }

  public static KettleExecOperation compile( IOperation operation, ITransformation trans ) {
    return new KettleExecOperation( operation, trans );
  }

  @Override public void subscribe( Subscriber<? super IDataEvent> subscriber ) {
    subscribers.add( subscriber );
  }

  @Override public boolean isRunning() {
    return !done.get();
  }

  @Override public List<IHop> getHopsIn() {
    return operation.getHopsIn();
  }

  @Override public List<IHop> getHopsOut() {
    return operation.getHopsOut();
  }

  @Override public String getId() {
    return operation.getId();
  }

  @Override public List<IOperation> getFrom() {
    return operation.getFrom();
  }

  @Override public List<IOperation> getTo() {
    return operation.getTo();
  }


  @Override public String getConfig() {
    return operation.getConfig();
  }

  @Override public <T> T accept( IOperationVisitor<T> visitor ) {
    return operation.accept( visitor );
  }

  @Override public void onComplete() {
    System.out.println( getId() + " is DONE" );
    done.set( true );
    step.dispose( stepMeta.getStepMetaInterface(), data );
    subscribers.stream()
      .forEach( sub -> sub.onComplete() );
  }

  @Override public void onError( Throwable throwable ) {

  }

  @Override public void onSubscribe( Subscription subscription ) {

  }

  @Override public void onNext( IDataEvent dataEvent ) {
    if ( dataEvent != null ) {
      inCount.incrementAndGet();
    }
    try {
      if ( dataEvent != null ) {
        System.out.println( Arrays.toString( dataEvent.getData().getData() ) );

        RowSet inputRow = ( (BaseStep) step ).findInputRowSet( dataEvent.getEventSource().getId() );
        inputRow.putRow( ( (KettleDataEvent) dataEvent ).getRowMeta(), dataEvent.getData().getData() );
      }

      boolean ongoing = step.processRow( stepMeta.getStepMetaInterface(), data );
      if ( !ongoing ) {
        done.set( true );
        subscribers.stream()
          .forEach( Subscriber::onComplete );
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }

  }

  private void initializeStepMeta() {
    trans.setRunning( true );
    trans.setLog( LogChannel.GENERAL );

    List<RowSet> outRowSets = nextStepStream()
      .map( next -> createRowSet( stepMeta, next ) )
      .collect( Collectors.toList());
    List<RowSet> inRowSets = prevStepStream()
      .map( prev -> createRowSet( prev, stepMeta ) )
      .collect( Collectors.toList());

    trans.getRowsets().addAll( outRowSets );
    trans.getRowsets().addAll( inRowSets );

    data = stepMeta.getStepMetaInterface().getStepData();
    step = stepMeta.getStepMetaInterface().getStep( stepMeta, data, 0, stepMeta.getParentTransMeta(), trans );

    // Copy the variables of the transformation to the step...
    // don't share. Each copy of the step has its own variables.
    step.initializeVariablesFrom( stepMeta.getParentTransMeta() );
    step.init( stepMeta.getStepMetaInterface(), data );
    step.setUsingThreadPriorityManagment( false );

    if ( !getTo().isEmpty() ) {
      ( (BaseStep) step ).setOutputRowSets( outRowSets );
    }
    if ( !getFrom().isEmpty() ) {
      ( (BaseStep) step ).setInputRowSets( inRowSets );
    }
    // Pass the connected repository & metaStore to the steps runtime
    step.setRepository( null );
    step.setMetaStore( null );
  }

  private Stream<StepMeta> prevStepStream() {
    return stepMeta.getParentTransMeta().findPreviousSteps( stepMeta ).stream();
  }

  private Stream<StepMeta> nextStepStream() {
    return stepMeta.getParentTransMeta().findNextSteps( stepMeta ).stream();
  }


  private RowSet createRowSet( StepMeta prev, StepMeta next ) {
    RowSet out = new QueueRowSet( ) {
      @Override public boolean putRow(RowMetaInterface rowMeta, Object[] rowData) {
        outCount.incrementAndGet();
        getSubscriber().ifPresent( sub -> sub.onNext(
          new KettleDataEvent( KettleExecOperation.this, rowMeta, rowData) ) );
        return super.putRow( rowMeta, rowData );
      }

      private Optional<Subscriber<? super IDataEvent>> getSubscriber() {
        return KettleExecOperation.this.subscribers.stream()
          .filter( sub -> (( KettleExecOperation ) sub).getId().equals( next.getName() ) )
          .findFirst();
      }
    };
    out.setThreadNameFromToCopy(prev.getName(), 0, next.getName(), 0);
    return out;
  }

  private TransMeta getTransMeta( IOperation op, ITransformation transformation ) {
    String config = transformation.getConfig();
    Document doc;
    try {
      doc = XMLHandler.loadXMLString( config );
      Node stepNode = XMLHandler.getSubNode( doc, "transformation" );
      return new TransMeta( stepNode, null );
    } catch ( KettleXMLException | KettleMissingPluginsException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Temp hack to set rowsets w/o modifying kettle Trans
   */
  public Trans createTrans() {
    Trans trans = new Trans();
    try {
      Field rowsets = Trans.class.getDeclaredField( "rowsets" );
      rowsets.setAccessible( true );
      rowsets.set( trans, new ArrayList<RowSet>() );
      return trans;
    } catch ( NoSuchFieldException | IllegalAccessException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override public int getIn() {
    return inCount.get();
  }

  @Override public int getOut() {
    return outCount.get();
  }

  @Override public int getDropped() {
    return droppedCount.get();
  }

  @Override public int getInFlight() {
    return inFlightCount.get();
  }

  @Override public Status getStatus() {
    return null;
  }

  @Override public String toString() {
    StringBuilder theString = new StringBuilder();
    theString.append( "Operation " + this.getId() + "\n" );
    theString.append( " IN:   " + getIn() + "\n" );
    theString.append( " OUT:  " + getOut() + "\n" );
    return theString.toString();
  }
}
