/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

package org.pentaho.di.trans.ael.adapters;

import com.google.common.base.Throwables;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class TransMetaConverter {

  public static final String TRANS_META_CONF_KEY = "TransMeta";
  public static final String TRANS_META_NAME_CONF_KEY = "TransMetaName";
  public static final String STEP_META_CONF_KEY = "StepMeta";
  public static final String TRANS_DEFAULT_NAME = "No Name";


  public static Transformation convert( TransMeta transMeta ) {
    final org.pentaho.di.engine.model.Transformation transformation =
      new org.pentaho.di.engine.model.Transformation( createTransformationId( transMeta ) );
    try {
      TransMeta copyTransMeta = (TransMeta) transMeta.clone();

      List<TransHopMeta> disabledHops = findHops( copyTransMeta, hop -> !hop.isEnabled() );
      List<StepMeta> disabledSteps = disabledHops.stream().map( hop -> hop.getToStep() ).collect( Collectors.toList() );
      disabledHops.forEach( copyTransMeta::removeTransHop );

      removeUnreachableSteps( copyTransMeta, disabledSteps );

      copyTransMeta.getSteps().forEach( createOperation( transformation ) );
      findHops( copyTransMeta, hop -> true ).forEach( createHop( transformation ) );

      transformation.setConfig( TRANS_META_CONF_KEY, copyTransMeta.getXML() );
      transformation.setConfig( TRANS_META_NAME_CONF_KEY,
        Optional.ofNullable( transMeta.getName() ).orElse( TRANS_DEFAULT_NAME ) );
    } catch ( KettleException e ) {
      Throwables.propagate( e );
    }
    return transformation;
  }

  private static String createTransformationId( TransMeta transMeta ) {
    String filename = transMeta.getFilename();
    if ( !Utils.isEmpty( filename ) ) {
      return filename;
    }

    return transMeta.getPathAndName();
  }

  private static Consumer<StepMeta> createOperation( org.pentaho.di.engine.model.Transformation transformation ) {
    return stepMeta -> {
      org.pentaho.di.engine.model.Operation operation = transformation.createOperation( stepMeta.getName() );
      try {
        operation.setConfig( STEP_META_CONF_KEY, stepMeta.getXML() );
      } catch ( KettleException e ) {
        Throwables.propagate( e );
      }
    };
  }

  private static Consumer<TransHopMeta> createHop( org.pentaho.di.engine.model.Transformation transformation ) {
    return hop -> {
      try {
        transformation.createHop(
          getOp( transformation, hop.getFromStep() ), getOp( transformation, hop.getToStep() ) );
      } catch ( KettleException e ) {
        Throwables.propagate( e );
      }
    };
  }

  private static Operation getOp( org.pentaho.di.engine.model.Transformation transformation, StepMeta step )
    throws KettleException {
    return transformation.getOperations().stream()
      .filter( op -> step.getName().equals( op.getId() ) )
      .findFirst()
      .orElseThrow( () -> new KettleException( "Could not find operation: " + step.getName() ) );
  }

  private static void removeUnreachableSteps( TransMeta trans, List<StepMeta> steps ) {
    for ( StepMeta step : steps ) {
      List<TransHopMeta> inHops = findHops( trans, hop -> hop.getToStep().equals( step ) );
      List<TransHopMeta>
          disabledInHops =
          inHops.stream().filter( hop -> !hop.isEnabled() ).collect( Collectors.toList() );

      if ( inHops.size() == disabledInHops.size() ) {
        List<StepMeta> nextSteps = trans.findNextSteps( step );
        findHops( trans, hop -> hop.getToStep().equals( step ) || hop.getFromStep().equals( step ) )
            .forEach( trans::removeTransHop );
        trans.getSteps().remove( step );

        removeUnreachableSteps( trans, nextSteps );
      }
    }
  }

  private static List<TransHopMeta> findHops( TransMeta trans, Predicate<TransHopMeta> condition ) {
    return IntStream.range( 0, trans.nrTransHops() ).mapToObj( trans::getTransHop ).filter( condition ).collect(
        Collectors.toList() );
  }
}
