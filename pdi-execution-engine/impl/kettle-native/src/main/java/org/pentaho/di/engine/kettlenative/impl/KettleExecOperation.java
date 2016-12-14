package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableList;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class KettleExecOperation implements IExecutableOperation {

  private final IOperation operation;
  private final Trans trans;
  private List<org.reactivestreams.Subscriber<? super IDataEvent>> subscribers = new ArrayList<>();
  private AtomicBoolean done = new AtomicBoolean( false );
  private StepDataInterface data;
  private StepInterface step;

  private RowSet inputRowset, outputRowset;
  private StepMeta stepMeta;

  private TransMeta transMeta;
  private AtomicInteger inCount = new AtomicInteger( 0 );
  private AtomicInteger outCount = new AtomicInteger( 0 );
  private AtomicInteger droppedCount = new AtomicInteger( 0 );
  private AtomicInteger inFlightCount = new AtomicInteger( 0 );


  protected KettleExecOperation( IOperation op, ITransformation transformation ) {
    this.operation = op;
    trans = createTrans();
    inputRowset = new SingleRowRowSet();
    inputRowset.setThreadNameFromToCopy( "", 0, "", 1 );
    outputRowset = new SingleRowRowSet();
    outputRowset.setThreadNameFromToCopy( "", 0, "", 1 );
    transMeta = getTransMeta( op, transformation );
    stepMeta = transMeta.findStep( op.getId() );

    trans.getRowsets().add( inputRowset );
    trans.getRowsets().add( outputRowset );

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
    if ( dataEvent != null ) {
      System.out.println( Arrays.toString( dataEvent.getData().getData() ) );
      inputRowset.putRow( ( (KettleDataEvent) dataEvent ).getRowMeta(), dataEvent.getData().getData() );
    }
    try {
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
    stepMeta.setRowDistribution( getRowDistribution() );

    trans.setRunning( true );
    trans.setLog( LogChannel.GENERAL );

    List<StepMeta> nextSteps = stepMeta.getParentTransMeta().findNextSteps( stepMeta );
    List<StepMeta> prevSteps = stepMeta.getParentTransMeta().findPreviousSteps( stepMeta );

    for (StepMeta next : nextSteps) {
      RowSet out = new SingleRowRowSet();
      out.setThreadNameFromToCopy(stepMeta.getName(), 0, next.getName(), 0);
      trans.getRowsets().add(out);
    }

    for (StepMeta prev : prevSteps) {
      RowSet in = new SingleRowRowSet();
      in.setThreadNameFromToCopy(prev.getName(), 0, stepMeta.getName(), 0);
      trans.getRowsets().add(in);
    }

    data = stepMeta.getStepMetaInterface().getStepData();
    step = stepMeta.getStepMetaInterface().getStep( stepMeta, data, 1, stepMeta.getParentTransMeta(), trans );


    // Copy the variables of the transformation to the step...
    // don't share. Each copy of the step has its own variables.
    step.initializeVariablesFrom( stepMeta.getParentTransMeta() );
    step.init( stepMeta.getStepMetaInterface(), data );
    step.setUsingThreadPriorityManagment( false );

    if ( !getTo().isEmpty() ) {
      ( (BaseStep) step ).setOutputRowSets( ImmutableList.of( outputRowset ) );
    }
    if ( !getFrom().isEmpty() ) {
      ( (BaseStep) step ).setInputRowSets( ImmutableList.of( inputRowset ) );
    }

    inputRowset.setThreadNameFromToCopy( "dummyPrev", 0, stepMeta.getName(), 0 );
    inputRowset.setThreadNameFromToCopy( stepMeta.getName(), 0, "dummyNext", 0 );

    // Pass the connected repository & metaStore to the steps runtime
    step.setRepository( null );
    step.setMetaStore( null );
  }

  /**
   * RowDistribution which publishes distributed rows to all subscribers.
   */
  private RowDistributionInterface getRowDistribution() {
    return new RowDistributionInterface() {
      @Override public String getCode() {
        return "spark-distributor";
      }

      @Override public String getDescription() {
        return "bypasses output rowsets, all distribute thru this guy";
      }

      @Override
      public void distributeRow( RowMetaInterface rowMetaInterface, Object[] objects, StepInterface stepInterface )
        throws KettleStepException {
        outCount.incrementAndGet();
        subscribers.stream()
          .forEach( sub -> sub.onNext(
            new KettleDataEvent( rowMetaInterface, objects ) ) );
      }

      @Override public PrimitiveGCInterface.EImage getDistributionImage() {
        return null;
      }
    };
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
