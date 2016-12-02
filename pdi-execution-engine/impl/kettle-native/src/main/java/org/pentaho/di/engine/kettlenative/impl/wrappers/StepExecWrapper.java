package org.pentaho.di.engine.kettlenative.impl.wrappers;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginLoaderException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITuple;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

public class StepExecWrapper {


  final private Trans trans;
  private RowMetaInterface inputRowMeta;
  RowProducer producer = null;

  public StepExecWrapper( IExecutableOperation execOp, List<IOperation> from ) {
    try {
      trans = createTrans( execOp, from );

      prepare( execOp );

    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  private void prepare( final IExecutableOperation execOp ) throws KettleException {
    trans.prepareExecution( new String[] {} );
    StepInterface step = trans.findRunThread( execOp.getId() );

    if (execOp.getFrom().size() > 0 ) {
      producer = trans.addRowProducer( "Injector", 0 );  // assumes no copies
    }

    step.addRowListener( new RowAdapter() {
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row) {
        execOp.next( () -> row );
      }
    });
    step.addStepListener( new StepAdapter() {
      public void stepFinished(Trans trans, StepMeta stepMeta, StepInterface step) {
        execOp.done();
      }
    } );
    System.out.println(step);
  }

  private StepMeta getStepMeta( IOperation op ) throws KettleXMLException, KettlePluginLoaderException {
    String config = op.getConfig();
    Document doc = XMLHandler.loadXMLString( config );
    Node stepNode = XMLHandler.getSubNode( doc, "step" );
    return new StepMeta( stepNode, null, ( IMetaStore ) null );
  }

  private Trans createTrans( IOperation current, List<IOperation> from ) {

    try {
      StepMeta currentOp = getStepMeta( current );
      StepMeta injector  = generateInjectorStep( from );

      TransMeta transMeta = new TransMeta();
      transMeta.addStep( currentOp );
      if ( from.size() > 0 ) {
        transMeta.addStep( injector );
        transMeta.addTransHop( new TransHopMeta( injector, currentOp ) );
      }
      return new Trans( transMeta );

    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  public void next( ITuple tuple ) {
    if ( producer != null ) {
      producer.putRow( inputRowMeta, tuple.getValues() );
    }

  }

  public void finished() {
    if ( producer != null ) {
      producer.finished();
    }
  }


  public boolean isRunning() {
    return trans.isRunning();
  }

  public void exec() {
    try {
      trans.startThreads();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  private StepMeta generateInjectorStep( List<IOperation> from )
    throws KettleException {
    InjectorMeta meta = new InjectorMeta();

    if ( from == null || from.isEmpty() ) {
      return new StepMeta();
    }
    // TODO - cleanup
    // assumes no mixed-layouts-- each incoming row should have same layout
    StepMeta previous =  getStepMeta( from.get( 0 ) );
    TransMeta transMeta = new TransMeta();
    transMeta.addStep( previous );
    inputRowMeta = transMeta.getStepFields( previous );

    meta.allocate( inputRowMeta.size() );

    for ( int i = 0; i < inputRowMeta.size(); i++ ) {
      ValueMetaInterface valueMeta = inputRowMeta.getValueMeta( i );
      meta.getFieldname()[i] = valueMeta.getName();
      meta.getType()[i] = valueMeta.getType();
      meta.getLength()[i] = valueMeta.getLength();
      meta.getPrecision()[i] = valueMeta.getPrecision();
    }
    StepMeta stepMeta = new StepMeta( "Injector", meta );
    return stepMeta;
  }

}
