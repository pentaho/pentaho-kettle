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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.function.Consumer;
import java.util.stream.IntStream;

public class TransMetaConverter {

  public static final String TRANS_META_CONF_KEY = "TransMeta";
  public static final String STEP_META_CONF_KEY = "StepMeta";


  public static Transformation convert( TransMeta transMeta ) {
    final org.pentaho.di.engine.model.Transformation transformation =
      new org.pentaho.di.engine.model.Transformation( transMeta.getName() );
    transMeta.getSteps().forEach( stepMeta -> {
      org.pentaho.di.engine.model.Operation operation = transformation.createOperation( stepMeta.getName() );
      operation.setConfig( STEP_META_CONF_KEY, stepMeta );
    } );
    IntStream.iterate( 0, i -> i + 1 )
      .limit( transMeta.nrTransHops() )
      .mapToObj( transMeta::getTransHop  )
      .forEach( createHop( transformation ) );
    transformation.setConfig( TRANS_META_CONF_KEY, transMeta );

    return transformation;
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
}
