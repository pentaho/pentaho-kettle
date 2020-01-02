/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.workarounds.ResolvableResource;

import static java.util.stream.Collectors.toMap;

public class TransMetaConverter {

  public static final String TRANS_META_CONF_KEY = "TransMeta";
  public static final String TRANS_META_NAME_CONF_KEY = "TransMetaName";
  public static final String SUB_TRANSFORMATIONS_KEY = "SubTransformations";
  public static final String STEP_META_CONF_KEY = "StepMeta";
  public static final String TRANS_DEFAULT_NAME = "No Name";


  public static Transformation convert( TransMeta transMeta ) {
    final org.pentaho.di.engine.model.Transformation transformation =
      new org.pentaho.di.engine.model.Transformation( createTransformationId( transMeta ) );
    try {
      TransMeta copyTransMeta = (TransMeta) transMeta.realClone( false );

      cleanupDisabledHops( copyTransMeta );

      // Turn off lazy conversion for AEL for now
      disableLazyConversion( copyTransMeta );

      // This is needed to resolve resources if a parameter is used within steps like text file out or Hadoop out.
      // It is unclear why this is needed, as the resource location can be resolved on the AEL/Driver/Executor side.
      // This should be removed a long with org.pentaho.di.workarounds.ResolvableResource which is apparently deprecated
      // since 8.1
      copyTransMeta.activateParameters();
      resolveStepMetaResources( copyTransMeta );

      copyTransMeta.getSteps().forEach( createOperation( transformation ) );
      findHops( copyTransMeta, hop -> true ).forEach( createHop( transformation ) );

      transformation.setConfig( TRANS_META_CONF_KEY, copyTransMeta.getXML() );
      transformation.setConfig( TRANS_META_NAME_CONF_KEY,
        Optional.ofNullable( transMeta.getName() ).orElse( TRANS_DEFAULT_NAME ) );
      Map<String, Transformation> subTransformations = copyTransMeta.getResourceDependencies().stream()
        .flatMap( resourceReference -> resourceReference.getEntries().stream() )
        .filter( entry -> ResourceEntry.ResourceType.ACTIONFILE.equals( entry.getResourcetype() ) )
        .collect( toMap( ResourceEntry::getResource, entry -> {
          try {
            Repository repository = copyTransMeta.getRepository();
            if ( repository != null ) {
              Path path = Paths.get( entry.getResource() );
              RepositoryDirectoryInterface directory =
                repository.findDirectory( path.getParent().toString().replace( File.separator, "/" ) );
              return convert(
                repository.loadTransformation( path.getFileName().toString(), directory, null, true, null ) );
            }
            return convert( new TransMeta( entry.getResource(), copyTransMeta.getParentVariableSpace() ) );
          } catch ( KettleException e ) {
            throw new RuntimeException( e );
          }
        } ) );
      transformation.setConfig( SUB_TRANSFORMATIONS_KEY, (Serializable) subTransformations );
    } catch ( KettleException e ) {
      Throwables.propagate( e );
    }
    return transformation;
  }

  private static void disableLazyConversion( TransMeta transMeta ) {
    transMeta.getSteps().stream().filter( step -> "CsvInput".equals( step.getStepID() ) )
        .forEach( step -> ( (CsvInputMeta) step.getStepMetaInterface() ).setLazyConversionActive( false ) );
    transMeta.getSteps().stream().filter( step -> "TableInput".equals( step.getStepID() ) )
        .forEach( step -> ( (TableInputMeta) step.getStepMetaInterface() ).setLazyConversionActive( false ) );
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
        Operation from = getOp( transformation, hop.getFromStep() );
        Operation to = getOp( transformation, hop.getToStep() );
        transformation.createHop( from, to, hop.isErrorHop() ? Hop.TYPE_ERROR : Hop.TYPE_NORMAL );
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

  /**
   * Removes disabled hops, unreachable steps and unused inputs.
   *
   * @param transMeta transMeta to process
   * @return processed clone of input transMeta
   */
  private static void cleanupDisabledHops( TransMeta transMeta ) {
    removeDisabledInputs( transMeta );
    removeInactivePaths( transMeta, null );
  }

  /**
   * Removes steps which cannot be reached using enabled hops. Steps removed along with every input and
   * output hops they have. Downstream steps processed recursively in the same way. Should be invoked with null second arg.
   *
   * @param trans trans object to process
   * @param steps
   */
  private static void removeInactivePaths( TransMeta trans, List<StepMeta> steps ) {
    if ( steps == null ) {
      List<TransHopMeta> disabledHops = findHops( trans, hop -> !hop.isEnabled() );

      List<StepMeta> disabledSteps = disabledHops.stream()
          .map( hop -> hop.getToStep() ).collect( Collectors.toList() );

      removeInactivePaths( trans, disabledSteps );
    } else {
      for ( StepMeta step : steps ) {
        List<TransHopMeta> enabledInHops = findHops( trans, hop -> hop.getToStep().equals( step )
            && hop.isEnabled() );
        List<TransHopMeta> disabledInHops = findHops( trans, hop -> hop.getToStep().equals( step )
            && !hop.isEnabled() );

        if ( enabledInHops.size() == 0 ) {
          List<StepMeta> nextSteps = findHops( trans, hop -> hop.getFromStep().equals( step ) ).stream().map(
              TransHopMeta::getToStep ).collect( Collectors.toList() );
          findHops( trans, hop -> hop.getToStep().equals( step ) || hop.getFromStep().equals( step ) )
              .forEach( trans::removeTransHop );
          trans.getSteps().remove( step );

          removeInactivePaths( trans, nextSteps );
        } else {
          disabledInHops.forEach( trans::removeTransHop );
        }
      }
    }
  }

  /**
   * Removes input steps having only disabled output hops so they will not be executed.
   * @param transMeta transMeta to process
   */
  private static void removeDisabledInputs( TransMeta transMeta ) {
    List<StepMeta> unusedInputs = findHops( transMeta, hop -> !hop.isEnabled() ).stream()
        .map( hop -> hop.getFromStep() )
        .filter( step -> isUnusedInput( transMeta, step ) )
        .collect( Collectors.toList() );
    for ( StepMeta unusedInput : unusedInputs ) {
      List<TransHopMeta> outHops = transMeta.findAllTransHopFrom( unusedInput );
      List<StepMeta> subsequentSteps = outHops.stream().map( hop -> hop.getToStep() ).collect( Collectors.toList() );
      outHops.forEach( transMeta::removeTransHop );
      transMeta.getSteps().remove( unusedInput );
      removeInactivePaths( transMeta, subsequentSteps );
    }
  }

  private static List<TransHopMeta> findHops( TransMeta trans, Predicate<TransHopMeta> condition ) {
    return IntStream.range( 0, trans.nrTransHops() ).mapToObj( trans::getTransHop ).filter( condition ).collect(
        Collectors.toList() );
  }

  private static boolean isUnusedInput( TransMeta trans, StepMeta step ) {
    int nrEnabledOutHops = findHops( trans, hop -> hop.getFromStep().equals( step ) && hop.isEnabled() ).size();
    int nrDisabledOutHops = findHops( trans, hop -> hop.getFromStep().equals( step ) && !hop.isEnabled() ).size();
    int nrInputHops = findHops( trans, hop -> hop.getToStep().equals( step ) ).size();

    return ( nrEnabledOutHops == 0 && nrDisabledOutHops > 0 && nrInputHops == 0 );
  }

  private static void resolveStepMetaResources( TransMeta transMeta ) {
    for ( StepMeta stepMeta : transMeta.getSteps() ) {
      StepMetaInterface smi = stepMeta.getStepMetaInterface();
      if ( smi instanceof ResolvableResource ) {
        ResolvableResource resolvableMeta = (ResolvableResource) smi;
        resolvableMeta.resolve();
      }
    }
  }

}
