package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicUtils {
  public static ITransformation convert( TransMeta meta ) {
    ClassicTransformation transformation = new ClassicTransformation( meta );
    Map<String, ClassicOperation> opMap =
      meta.getSteps().stream().map( ClassicOperation::new ).collect( Collectors.toMap(
        IOperation::getId, Function.identity() ) );

    transformation.setOperations( new ArrayList<>( opMap.values() )  );

    return transformation;
  }
}