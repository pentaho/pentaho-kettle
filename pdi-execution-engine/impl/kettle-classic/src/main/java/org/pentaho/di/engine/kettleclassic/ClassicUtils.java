package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.model.Operation;
import org.pentaho.di.engine.model.Transformation;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicUtils {

  public static final String TRANS_META_CONF_KEY = "TransMeta";
  public static final String STEP_META_CONF_KEY = "StepMeta";

  public static ITransformation convert( TransMeta transMeta ) {
    final Transformation transformation = new Transformation( transMeta.getName() );
    transMeta.getSteps().forEach( stepMeta -> {
      Operation operation = transformation.createOperation( stepMeta.getName() );
      operation.setConfig( STEP_META_CONF_KEY, stepMeta );
    } );
    transformation.setConfig( TRANS_META_CONF_KEY, transMeta );
    return transformation;
  }

  public static ClassicTransformation materialize( IExecutionContext context, ITransformation iTransformation ) {
    ClassicTransformation transformation = new ClassicTransformation( context, iTransformation );
    List<ClassicOperation> materializedOps =
      iTransformation.getOperations().stream().map( op -> new ClassicOperation( context, op ) ).collect( toList() );
    transformation.setOperations( materializedOps );
    return transformation;
  }
}