package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicUtils {

  public static final String TRANS_META_CONF_KEY = "TransMeta";
  public static final String STEP_META_CONF_KEY = "StepMeta";

  public static Transformation convert( TransMeta transMeta ) {
    final org.pentaho.di.engine.model.Transformation transformation = new org.pentaho.di.engine.model.Transformation( transMeta.getName() );
    transMeta.getSteps().forEach( stepMeta -> {
      org.pentaho.di.engine.model.Operation operation = transformation.createOperation( stepMeta.getName() );
      operation.setConfig( STEP_META_CONF_KEY, stepMeta );
    } );
    transformation.setConfig( TRANS_META_CONF_KEY, transMeta );
    return transformation;
  }

  public static ClassicTransformation materialize( ExecutionContext context, Transformation iTransformation ) {
    ClassicTransformation transformation = new ClassicTransformation( context, iTransformation );
    List<ClassicOperation> materializedOps =
      iTransformation.getOperations().stream().map( op -> new ClassicOperation( context, op ) ).collect( toList() );
    transformation.setOperations( materializedOps );
    return transformation;
  }

  public static Operation getOperation( Transformation transformation, StepMeta selectedStep ) {
    return transformation.getOperations().stream().filter( op -> op.getId().equals( selectedStep.getStepID() ) ).findFirst().get();
  }
}