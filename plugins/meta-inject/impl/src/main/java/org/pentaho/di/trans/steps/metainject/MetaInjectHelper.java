/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.trans.steps.metainject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.bean.BeanInjectionInfo;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.core.KettleAttributeInterface;
import org.pentaho.di.trans.step.StepAttributesInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.core.util.Utils;

public class MetaInjectHelper extends BaseStepHelper {

  protected static final String META_INJECT_REFERENCE_PATH = "referencePath";
  protected static final String META_INJECT_ACTIVE_REFERENCE_PATH = "activeReferencePath";
  protected static final String GET_INJECT_TRANS_STEPS = "getInjectTransSteps";
  protected static final String GET_STEP_NAMES = "getStepNames";
  protected static final String GET_TARGET_ATTRIBUTE_KEYS = "getTargetAttributeKeys";
  protected static final String STEP_NAME = "stepName";

  protected static final String INJECT_TRANS_STEPS = "injectTransSteps";
  protected static final String STEP_NAMES = "stepNames";
  protected static final String TRANS_NAME = "transName";
  protected static final String SOURCE_FIELDS = "sourceFields";

  private final MetaInjectMeta metaInjectMeta;

  /** Server-lifetime cache keyed by inject-template identity (filename|transName|directoryPath). */
  private static final ConcurrentHashMap<String, TransMeta> injectTransMetaCache = new ConcurrentHashMap<>();

  public MetaInjectHelper( MetaInjectMeta metaInjectMeta ) {
    this.metaInjectMeta = metaInjectMeta;
  }

  /**
   * Returns the inject-template TransMeta from the cache, or loads it via the existing
   * MetaInjectMeta.loadTransformationMeta() on first access and caches the result.
   */
  private TransMeta getOrLoadInjectTransMeta( TransMeta transMeta ) throws Exception {
    String cacheKey = Const.NVL( metaInjectMeta.getFileName(), "" )
      + "|" + Const.NVL( metaInjectMeta.getTransName(), "" )
      + "|" + Const.NVL( metaInjectMeta.getDirectoryPath(), "" );
    return injectTransMetaCache.computeIfAbsent( cacheKey, k -> {
      try {
        TransMeta loaded = MetaInjectMeta.loadTransformationMeta(
          transMeta.getBowl(), metaInjectMeta, transMeta.getRepository(), null, transMeta );
        loaded.clearChanged();
        return loaded;
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    } );
  }

  /** Handles step-specific actions for ETL Metadata Injection. */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                        Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    switch ( method ) {
      case GET_INJECT_TRANS_STEPS:
        response = getInjectTransSteps( transMeta );
        break;
      case GET_STEP_NAMES:
        response = getStepNames( transMeta );
        break;
      case GET_TARGET_ATTRIBUTE_KEYS:
        response = getTargetAttributeKeys( transMeta, queryParams );
        break;
      default:
        if ( META_INJECT_REFERENCE_PATH.equalsIgnoreCase( method ) ) {
          response = getReferencePath( transMeta );
        } else if ( META_INJECT_ACTIVE_REFERENCE_PATH.equalsIgnoreCase( method ) ) {
          response = getActiveReferencePath( transMeta );
        } else {
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        }
    }

    return response;
  }

  /** Returns the reference path and validity of the injected transformation. */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( REFERENCE_PATH, getReferencePath( transMeta, metaInjectMeta.getDirectoryPath(), metaInjectMeta.getTransName(),
        metaInjectMeta.getSpecificationMethod(), metaInjectMeta.getFileName() ) );
    try {
      getOrLoadInjectTransMeta( transMeta );
      response.put( IS_VALID_REFERENCE, true );
      response.put( IS_TRANS_REFERENCE, true );
    } catch ( Exception exception ) {
      response.put( IS_VALID_REFERENCE, false );
      response.put( IS_TRANS_REFERENCE, true );
    }
    return response;
  }

  /**
   * Returns the path of the transformation written after ETL metadata injection.
   * This mirrors Java TransGraph#openMapping(stepMeta, -1) which opens the live
   * in-memory post-injection TransMeta from getActiveSubTransformation().
   * In the web context the equivalent is the file written to targetFile during execution.
   */
  private JSONObject getActiveReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    String targetFile = metaInjectMeta.getTargetFile();
    if ( !Utils.isEmpty( targetFile ) ) {
      String resolvedTargetFile = transMeta.environmentSubstitute( targetFile );
      response.put( REFERENCE_PATH, resolvedTargetFile );
      try {
        // Validate that the generated KTR can actually be loaded from the target path
        getOrLoadInjectTransMeta( transMeta );
        // Also verify the written target file can be opened as a TransMeta
        new TransMeta( transMeta.getBowl(), resolvedTargetFile, null, transMeta.getRepository(), false, transMeta, null );
        response.put( IS_VALID_REFERENCE, true );
        response.put( IS_TRANS_REFERENCE, true );
      } catch ( Exception exception ) {
        response.put( IS_VALID_REFERENCE, false );
        response.put( IS_TRANS_REFERENCE, true );
      }
    } else {
      response.put( REFERENCE_PATH, "" );
      response.put( IS_VALID_REFERENCE, false );
      response.put( IS_TRANS_REFERENCE, true );
    }
    return response;
  }

  /** Returns injectable steps with their properties for refreshing the injection mapping tree. */
  @SuppressWarnings( "unchecked" )
  private JSONObject getInjectTransSteps( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    try {
      // Normalize any legacy "<const>" source step names to proper null stepname
      metaInjectMeta.getTargetSourceMapping().replaceAll( ( k, v ) -> {
        if ( v != null && MetaInjectDialog.CONST_VALUE.equals( v.getStepname() ) ) {
          return new SourceStepField( null, v.getField() );
        }
        return v;
      } );

      TransMeta injectTransMeta = getOrLoadInjectTransMeta( transMeta );

      List<StepMeta> injectSteps = MetaInjectDialog.getInjectableSteps( injectTransMeta );

      // Strip mapping entries whose target key is not a valid injectable property on the target step.
      // This cleans up group names (e.g. FIELDS, REMOVES, METAS) that were incorrectly saved as
      // target_attribute_key values instead of only their child property keys.
      for ( StepMeta stepMeta : injectSteps ) {
        StepMetaInterface intf = stepMeta.getStepMetaInterface();
        if ( BeanInjectionInfo.isInjectionSupported( intf.getClass() ) ) {
          Set<String> validKeys = new BeanInjectionInfo( intf.getClass() ).getProperties().keySet();
          final String stepName = stepMeta.getName();
          metaInjectMeta.getTargetSourceMapping().entrySet().removeIf(
            e -> e.getKey().getStepname().equalsIgnoreCase( stepName )
              && !validKeys.contains( e.getKey().getAttributeKey() ) );
        }
      }

      JSONObject stepsObject = new JSONObject();
      for ( StepMeta stepMeta : injectSteps ) {
        stepsObject.put( stepMeta.getName(),
            getStepPropertiesAsJson( stepMeta.getName(), stepMeta.getStepMetaInterface(), metaInjectMeta.getTargetSourceMapping() ) );
      }

      response.put( TRANS_NAME, injectTransMeta.getName() );
      response.put( INJECT_TRANS_STEPS, stepsObject );
    } catch ( Exception exception ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Builds injectable properties as nested JSON. Root properties are keyed directly on the step;
   * grouped properties appear under groupName → { description, attributes }.
   * Falls back to StepMetaInjectionInterface for non-bean-injection steps.
   */
  @SuppressWarnings( "unchecked" )
  private static JSONObject getStepPropertiesAsJson( String stepName, StepMetaInterface metaInterface,
                                                     Map<TargetStepAttribute, SourceStepField> mapping ) {
    JSONObject stepJson = new JSONObject();
    if ( BeanInjectionInfo.isInjectionSupported( metaInterface.getClass() ) ) {
      BeanInjectionInfo injectionInfo = new BeanInjectionInfo( metaInterface.getClass() );
      for ( BeanInjectionInfo.Group gr : injectionInfo.getGroups() ) {
        boolean rootGroup = gr.getName() == null || gr.getName().isEmpty();
        JSONObject attributesTarget = rootGroup ? stepJson : new JSONObject();
        for ( BeanInjectionInfo.Property prop : gr.getGroupProperties() ) {
          SourceStepField source = mapping.get( new TargetStepAttribute( stepName, prop.getName(), !rootGroup ) );
          JSONObject propJson = new JSONObject();
          propJson.put( "description", prop.getDescription() );
          propJson.put( "required", injectionInfo.getProperties().get( prop.getName() ).isRequire() ? "Y" : "" );
          propJson.put( "targetDetail", !rootGroup );
          String srcStep = source != null ? source.getStepname() : null;
          propJson.put( "source_step", ( srcStep == null || MetaInjectDialog.CONST_VALUE.equals( srcStep ) ) ? "" : srcStep );
          propJson.put( "source_field", source != null && source.getField() != null ? source.getField() : "" );
          attributesTarget.put( prop.getName(), propJson );
        }
        if ( !rootGroup ) {
          JSONObject groupJson = new JSONObject();
          groupJson.put( "description", gr.getDescription() );
          groupJson.put( "attributes", attributesTarget );
          stepJson.put( gr.getName(), groupJson );
        }
      }
    } else {
      StepMetaInjectionInterface injection = metaInterface.getStepMetaInjectionInterface();
      if ( injection == null ) {
        return stepJson;
      }
      StepAttributesInterface attrInterface = ( metaInterface instanceof StepAttributesInterface )
        ? (StepAttributesInterface) metaInterface : null;
      try {
        List<StepInjectionMetaEntry> entries = injection.getStepInjectionMetadataEntries();
        for ( StepInjectionMetaEntry entry : entries ) {
          if ( entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {
            // Root-level (ungrouped) property — register canonical key plus xmlCode/repCode aliases
            stepJson.put( entry.getKey(), buildLegacyPropJson( stepName, entry.getKey(), false,
              entry.getDescription(), mapping ) );
            putLegacyAliases( stepJson, attrInterface, stepName, entry.getKey(), false,
              entry.getDescription(), mapping );
          } else if ( entry.getDetails() != null && !entry.getDetails().isEmpty() ) {
            // Group entry: details[0].details holds the individual attributes
            List<StepInjectionMetaEntry> groupItems = entry.getDetails().get( 0 ).getDetails();
            JSONObject attributesJson = new JSONObject();
            for ( StepInjectionMetaEntry me : groupItems ) {
              // Register canonical key plus xmlCode/repCode aliases for each group item
              attributesJson.put( me.getKey(), buildLegacyPropJson( stepName, me.getKey(), true,
                me.getDescription(), mapping ) );
              putLegacyAliases( attributesJson, attrInterface, stepName, me.getKey(), true,
                me.getDescription(), mapping );
            }
            JSONObject groupJson = new JSONObject();
            groupJson.put( "description", entry.getDescription() );
            groupJson.put( "attributes", attributesJson );
            stepJson.put( entry.getKey(), groupJson );
          }
        }
      } catch ( Exception ex ) {
        // Return whatever was built so far; empty is acceptable
      }
    }
    return stepJson;
  }

  /**
   * Mirrors MetaInject's convertToUpperCaseSet / toUpperCase pattern: iterates the provided keys
   * in order and returns the first matching SourceStepField from the mapping. Callers pass both
   * the canonical key and its uppercase form so that KTRs saved by Spoon (which store attribute
   * keys in uppercase) are resolved even when the step metadata returns a different casing.
   */
  private static SourceStepField findMappingSource( String stepName, String[] keys, boolean isDetail,
                                                     Map<TargetStepAttribute, SourceStepField> mapping ) {
    for ( String key : keys ) {
      SourceStepField src = mapping.get( new TargetStepAttribute( stepName, key, isDetail ) );
      if ( src != null ) {
        return src;
      }
    }
    return null;
  }

  /**
   * Builds a single injectable property JSON object for the legacy (StepMetaInjectionInterface) path.
   */
  @SuppressWarnings( "unchecked" )
  private static JSONObject buildLegacyPropJson( String stepName, String key, boolean isDetail,
                                                  String description,
                                                  Map<TargetStepAttribute, SourceStepField> mapping ) {
    SourceStepField src = findMappingSource( stepName, new String[]{ key, key.toUpperCase() }, isDetail, mapping );
    JSONObject json = new JSONObject();
    json.put( "description", description );
    json.put( "required", "" );
    json.put( "targetDetail", isDetail );
    String srcStep = src != null ? src.getStepname() : null;
    json.put( "source_step", ( srcStep == null || MetaInjectDialog.CONST_VALUE.equals( srcStep ) ) ? "" : srcStep );
    json.put( "source_field", src != null && src.getField() != null ? src.getField() : "" );
    return json;
  }

  /**
   * Adds xmlCode and repCode alias entries for a legacy attribute into the given JSON object.
   * Spoon registers all three key forms (canonical id, xmlCode, repCode) as separate injectable targets,
   * so KTRs saved by Spoon that reference the alias keys are correctly resolved here.
   */
  @SuppressWarnings( "unchecked" )
  private static void putLegacyAliases( JSONObject targetJson, StepAttributesInterface attrInterface,
                                         String stepName, String canonicalKey, boolean isDetail,
                                         String description,
                                         Map<TargetStepAttribute, SourceStepField> mapping ) {
    if ( attrInterface == null ) {
      return;
    }
    KettleAttributeInterface kAttr = attrInterface.findAttribute( canonicalKey );
    if ( kAttr == null ) {
      return;
    }
    String xmlCode = kAttr.getXmlCode();
    String repCode = kAttr.getRepCode();
    if ( !Utils.isEmpty( xmlCode ) && !xmlCode.equalsIgnoreCase( canonicalKey ) && !targetJson.containsKey( xmlCode ) ) {
      targetJson.put( xmlCode, buildLegacyPropJson( stepName, xmlCode, isDetail, description, mapping ) );
    }
    if ( !Utils.isEmpty( repCode ) && !repCode.equalsIgnoreCase( canonicalKey ) && !repCode.equalsIgnoreCase( xmlCode )
        && !targetJson.containsKey( repCode ) ) {
      targetJson.put( repCode, buildLegacyPropJson( stepName, repCode, isDetail, description, mapping ) );
    }
  }

  /** Returns a sorted list of all step names in the injected transformation. */
  @SuppressWarnings( "unchecked" )
  private JSONObject getStepNames( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    try {
      TransMeta injectTransMeta = getOrLoadInjectTransMeta( transMeta );

      String[] names = injectTransMeta.getStepNames();
      Arrays.sort( names );

      response.put( STEP_NAMES, Arrays.stream( names ).toList() );
    } catch ( Exception exception ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Returns the available source step:field pairs reachable from the MetaInject step,
   * along with the currently mapped source step and field for the given target attribute key.
   * Mirrors the logic in MetaInjectDialog handleEvent (SWT.MouseDown) for the web layer.
   */
  @SuppressWarnings( "unchecked" )
  private JSONObject getTargetAttributeKeys( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray sourceFields = new JSONArray();
    try {
      String stepName = queryParams.get( STEP_NAME );
      if ( Utils.isEmpty( stepName ) ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }
      StepMeta currentStep = transMeta.findStep( stepName );
      if ( currentStep == null || !( currentStep.getStepMetaInterface() instanceof MetaInjectMeta ) ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      // Normalize any legacy "<const>" source step names to proper null stepname
      metaInjectMeta.getTargetSourceMapping().replaceAll( ( k, v ) -> {
        if ( v != null && MetaInjectDialog.CONST_VALUE.equals( v.getStepname() ) ) {
          return new SourceStepField( null, v.getField() );
        }
        return v;
      } );

      // Get all previous step names and their fields (mirrors transMeta.getPrevStepNames + getStepFields)
      String[] prevStepNames = transMeta.getPrevStepNames( currentStep );
      Arrays.sort( prevStepNames );

      for ( String prevStepName : prevStepNames ) {
        RowMetaInterface fields = transMeta.getStepFields( prevStepName );
        for ( ValueMetaInterface field : fields.getValueMetaList() ) {
          sourceFields.add( prevStepName + " : " + field.getName() );
        }
      }

      response.put( SOURCE_FIELDS, sourceFields );
    } catch ( Exception exception ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }
}