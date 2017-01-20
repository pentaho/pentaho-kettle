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
  public static ITransformation convert( TransMeta meta ) {
    final Transformation transformation = new Transformation( meta.getName() );
    meta.getSteps().forEach( stepMeta -> {
      Operation operation = transformation.createOperation( stepMeta.getName() );
      operation.setConfig( "StepMeta", stepMeta );
    } );
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