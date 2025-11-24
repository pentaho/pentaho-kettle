/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package org.pentaho.di.trans.steps.util;

import org.json.simple.JSONObject;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;
import org.json.simple.JSONArray;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.Arrays;
import java.util.List;

public class StepKeyUtils {

  private StepKeyUtils() {
      // Prevent instantiation
  }

  public static final String STEP_KEYS = "stepKeys";

  public static JSONObject buildSuccessResponse( JSONObject  response, JSONObject stepKeys ) {
    response.put( STEP_KEYS, stepKeys );
    response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    return response;
  }


    /**
     * Retrieves field names from a step's row metadata.
     *
     * @param transMeta The transformation metadata.
     * @param stepMeta  The step metadata to get fields from.
     * @return A list of field names, or an empty JSONArray if not available.
     * @throws KettleException if there's an error retrieving step fields.
     */
  public static Object getStepFields( TransMeta transMeta, StepMeta stepMeta ) throws KettleException {
    if ( stepMeta == null ) {
      return new JSONArray();
    }
    RowMetaInterface rowMeta = transMeta.getStepFields( stepMeta );
    return rowMeta != null ? Arrays.asList( rowMeta.getFieldNames() ) : new JSONArray();
  }

    /**
     * Builds a JSON object containing step keys from info streams.
     *
     * @param transMeta   The transformation metadata.
     * @param infoStreams List of info streams to process.
     * @return A JSON object with step names as keys and field lists as values.
     * @throws KettleException if there's an error retrieving step information.
     */
  public static JSONObject buildStepKeysFromStreams( TransMeta transMeta, List<StreamInterface> infoStreams )
            throws KettleException {
    JSONObject stepKeys = new JSONObject();
    for ( StreamInterface stream : infoStreams ) {
      StepMeta inputStepMeta = stream.getStepMeta();
      if ( inputStepMeta != null ) {
        stepKeys.put( inputStepMeta.getName(), getStepFields( transMeta, inputStepMeta ) );
      }
    }
    return stepKeys;
  }

    /**
     * Validates if a step name is valid (not null or empty).
     *
     * @param stepName The step name to validate.
     * @return true if the step name is valid, false otherwise.
     */
  public static boolean isValidStepName( String stepName ) {
    return stepName != null && !stepName.isEmpty();
  }

    /**
     * Finds and validates a step with a specific meta interface type.
     *
     * @param transMeta     The transformation metadata.
     * @param stepName      The name of the step to find.
     * @param expectedClass The expected class type of the step meta interface.
     * @return The StepMeta if found and valid, null otherwise.
     */
  public static StepMeta findAndValidateStep( TransMeta transMeta, String stepName, Class<?> expectedClass ) {
    if ( !isValidStepName( stepName ) ) {
      return null;
    }
    StepMeta stepMeta = transMeta.findStep( stepName );
    if ( stepMeta == null || !expectedClass.isInstance( stepMeta.getStepMetaInterface() ) ) {
      return null;
    }
    return stepMeta;
  }

    /**
     * Builds a JSON object containing step keys from info streams with custom step names.
     *
     * @param transMeta      The transformation metadata.
     * @param infoStreams    List of info streams to process.
     * @param inputStepNames Array of custom step names to use as keys.
     * @return A JSON object with custom step names as keys and field lists as values.
     * @throws KettleException if there's an error retrieving step information.
     */
  public static JSONObject buildStepKeysWithCustomNames( TransMeta transMeta, List<StreamInterface> infoStreams,
                                                          String[] inputStepNames ) throws KettleException {
    JSONObject stepKeys = new JSONObject();

        // 🔥 Explicit null check so Sonar knows inputStepNames is non-null below
    if ( inputStepNames == null || infoStreams == null ) {
      return stepKeys;
    }

    int streamCount = Math.min( infoStreams.size(), inputStepNames.length );

    for ( int i = 0; i < streamCount; i++ ) {
      StreamInterface stream = infoStreams.get( i );
      StepMeta inputStepMeta = stream.getStepMeta();
      if ( inputStepMeta != null ) {
        String stepName = inputStepNames[i];
        stepKeys.put( stepName, getStepFields( transMeta, inputStepMeta ) );
      }
    }
    return stepKeys;
  }

}
