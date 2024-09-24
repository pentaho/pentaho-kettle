/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.util.Hashtable;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.di.compatibility.Row;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.compatibility.ValueUsedListener;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.util.JavaScriptUtils;

/**
 * Executes a JavaScript on the values in the input stream. Selected calculated values can then be put on the output
 * stream.
 *
 * @author Matt
 * @since 5-April-2003
 */
public class ScriptValuesMod extends BaseStep implements StepInterface {
  private static Class<?> PKG = ScriptValuesMetaMod.class; // for i18n purposes, needed by Translator2!!

  private ScriptValuesMetaMod meta;

  private ScriptValuesModData data;

  public static final int SKIP_TRANSFORMATION = 1;

  public static final int ABORT_TRANSFORMATION = -1;

  public static final int ERROR_TRANSFORMATION = -2;

  public static final int CONTINUE_TRANSFORMATION = 0;

  private boolean bWithTransStat = false;

  private boolean bRC = false;

  private int iTranStat = CONTINUE_TRANSFORMATION;

  private boolean bFirstRun = false;

  private ScriptValuesScript[] jsScripts;

  private String strTransformScript = "";

  private String strStartScript = "";

  private String strEndScript = "";

  // public static Row insertRow;

  public Script script;

  public ScriptValuesMod( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                          Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private void determineUsedFields( RowMetaInterface row ) {
    int nr = 0;
    // Count the occurrences of the values.
    // Perhaps we find values in comments, but we take no risk!
    //
    for ( int i = 0; i < row.size(); i++ ) {
      String valname = row.getValueMeta( i ).getName().toUpperCase();
      if ( strTransformScript.toUpperCase().indexOf( valname ) >= 0 ) {
        nr++;
      }
    }

    // Allocate fields_used
    data.fields_used = new int[ nr ];
    data.values_used = new Value[ nr ];

    nr = 0;
    // Count the occurrences of the values.
    // Perhaps we find values in comments, but we take no risk!
    //
    for ( int i = 0; i < row.size(); i++ ) {
      // Values are case-insensitive in JavaScript.
      //
      String valname = row.getValueMeta( i ).getName();
      if ( strTransformScript.indexOf( valname ) >= 0 ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "ScriptValuesMod.Log.UsedValueName", String.valueOf( i ), valname ) );
        }
        data.fields_used[ nr ] = i;
        nr++;
      }
    }

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "ScriptValuesMod.Log.UsingValuesFromInputStream", String
        .valueOf( data.fields_used.length ) ) );
    }
  }

  private boolean addValues( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    if ( first ) {
      first = false;

      // What is the output row looking like?
      //
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Determine the indexes of the fields used!
      //
      determineUsedFields( rowMeta );

      // Get the indexes of the replaced fields...
      //
      data.replaceIndex = new int[ meta.getFieldname().length ];
      for ( int i = 0; i < meta.getFieldname().length; i++ ) {
        if ( meta.getReplace()[ i ] ) {
          data.replaceIndex[ i ] = rowMeta.indexOfValue( meta.getFieldname()[ i ] );
          if ( data.replaceIndex[ i ] < 0 ) {
            if ( Utils.isEmpty( meta.getFieldname()[ i ] ) ) {
              throw new KettleStepException( BaseMessages.getString(
                PKG, "ScriptValuesMetaMod.Exception.FieldToReplaceNotFound", meta.getFieldname()[ i ] ) );
            }
            data.replaceIndex[ i ] = rowMeta.indexOfValue( meta.getRename()[ i ] );
            if ( data.replaceIndex[ i ] < 0 ) {
              throw new KettleStepException( BaseMessages.getString(
                PKG, "ScriptValuesMetaMod.Exception.FieldToReplaceNotFound", meta.getRename()[ i ] ) );
            }
          }
        } else {
          data.replaceIndex[ i ] = -1;
        }
      }

      // set the optimization level
      data.cx = ContextFactory.getGlobal().enterContext();

      try {
        String optimizationLevelAsString = environmentSubstitute( meta.getOptimizationLevel() );
        if ( !Utils.isEmpty( Const.trim( optimizationLevelAsString ) ) ) {
          data.cx.setOptimizationLevel( Integer.parseInt( optimizationLevelAsString.trim() ) );
          logBasic( BaseMessages.getString( PKG, "ScriptValuesMod.Optimization.Level", environmentSubstitute( meta
            .getOptimizationLevel() ) ) );
        } else {
          data.cx.setOptimizationLevel( Integer.parseInt( ScriptValuesMetaMod.OPTIMIZATION_LEVEL_DEFAULT ) );
          logBasic( BaseMessages.getString(
            PKG, "ScriptValuesMod.Optimization.UsingDefault", ScriptValuesMetaMod.OPTIMIZATION_LEVEL_DEFAULT ) );
        }
      } catch ( NumberFormatException nfe ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "ScriptValuesMetaMod.Exception.NumberFormatException", environmentSubstitute( meta
            .getOptimizationLevel() ) ) );
      } catch ( IllegalArgumentException iae ) {
        throw new KettleException( iae.getMessage() );
      }

      data.scope = data.cx.initStandardObjects( null, false );

      bFirstRun = true;

      Scriptable jsvalue = Context.toObject( this, data.scope );
      data.scope.put( "_step_", data.scope, jsvalue );

      // Adding the existing Scripts to the Context
      for ( int i = 0; i < meta.getNumberOfJSScripts(); i++ ) {
        Scriptable jsR = Context.toObject( jsScripts[ i ].getScript(), data.scope );
        data.scope.put( jsScripts[ i ].getScriptName(), data.scope, jsR );
      }

      // Adding the Name of the Transformation to the Context
      data.scope.put( "_TransformationName_", data.scope, getTransMeta().getName() );

      try {
        // add these now (they will be re-added later) to make compilation succeed
        //

        // Add the old style row object for compatibility reasons...
        //
        if ( meta.isCompatible() ) {
          Row v2Row = RowMeta.createOriginalRow( rowMeta, row );
          Scriptable jsV2Row = Context.toObject( v2Row, data.scope );
          data.scope.put( "row", data.scope, jsV2Row );
        } else {
          Scriptable jsrow = Context.toObject( row, data.scope );
          data.scope.put( "row", data.scope, jsrow );
        }

        // Add the used fields...
        //
        for ( int i = 0; i < data.fields_used.length; i++ ) {
          ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.fields_used[ i ] );
          Object valueData = row[ data.fields_used[ i ] ];

          if ( meta.isCompatible() ) {
            data.values_used[ i ] = valueMeta.createOriginalValue( valueData );

            Scriptable jsarg = Context.toObject( data.values_used[ i ], data.scope );
            data.scope.put( valueMeta.getName(), data.scope, jsarg );
          } else {
            Object normalStorageValueData = valueMeta.convertToNormalStorageType( valueData );
            Scriptable jsarg;
            if ( normalStorageValueData != null ) {
              jsarg = Context.toObject( normalStorageValueData, data.scope );
            } else {
              jsarg = null;
            }
            data.scope.put( valueMeta.getName(), data.scope, jsarg );
          }
        }

        // also add the meta information for the whole row
        //
        Scriptable jsrowMeta = Context.toObject( rowMeta, data.scope );
        data.scope.put( "rowMeta", data.scope, jsrowMeta );

        // Modification for Additional Script parsing
        //
        try {
          if ( meta.getAddClasses() != null ) {
            for ( int i = 0; i < meta.getAddClasses().length; i++ ) {
              Object jsOut = Context.javaToJS( meta.getAddClasses()[ i ].getAddObject(), data.scope );
              ScriptableObject.putProperty( data.scope, meta.getAddClasses()[ i ].getJSName(), jsOut );
            }
          }
        } catch ( Exception e ) {
          throw new KettleValueException( BaseMessages.getString(
            PKG, "ScriptValuesMod.Log.CouldNotAttachAdditionalScripts" ), e );
        }

        // Adding some default JavaScriptFunctions to the System
        try {
          Context.javaToJS( ScriptValuesAddedFunctions.class, data.scope );
          ( (ScriptableObject) data.scope ).defineFunctionProperties(
            ScriptValuesAddedFunctions.jsFunctionList, ScriptValuesAddedFunctions.class,
            ScriptableObject.DONTENUM );
        } catch ( Exception ex ) {
          // System.out.println(ex.toString());
          throw new KettleValueException( BaseMessages.getString(
            PKG, "ScriptValuesMod.Log.CouldNotAddDefaultFunctions" ), ex );
        }

        // Adding some Constants to the JavaScript
        try {

          data.scope.put( "SKIP_TRANSFORMATION", data.scope, Integer.valueOf( SKIP_TRANSFORMATION ) );
          data.scope.put( "ABORT_TRANSFORMATION", data.scope, Integer.valueOf( ABORT_TRANSFORMATION ) );
          data.scope.put( "ERROR_TRANSFORMATION", data.scope, Integer.valueOf( ERROR_TRANSFORMATION ) );
          data.scope.put( "CONTINUE_TRANSFORMATION", data.scope, Integer.valueOf( CONTINUE_TRANSFORMATION ) );

        } catch ( Exception ex ) {
          // System.out.println("Exception Adding the Constants " + ex.toString());
          throw new KettleValueException( BaseMessages.getString(
            PKG, "ScriptValuesMod.Log.CouldNotAddDefaultConstants" ), ex );
        }

        try {
          // Checking for StartScript
          if ( strStartScript != null && strStartScript.length() > 0 ) {
            Script startScript = data.cx.compileString( strStartScript, "trans_Start", 1, null );
            startScript.exec( data.cx, data.scope );
            if ( log.isDetailed() ) {
              logDetailed( ( "Start Script found!" ) );
            }
          } else {
            if ( log.isDetailed() ) {
              logDetailed( ( "No starting Script found!" ) );
            }
          }
        } catch ( Exception es ) {
          // System.out.println("Exception processing StartScript " + es.toString());
          throw new KettleValueException( BaseMessages.getString(
            PKG, "ScriptValuesMod.Log.ErrorProcessingStartScript" ), es );

        }
        // Now Compile our Script
        data.script = data.cx.compileString( strTransformScript, "script", 1, null );
      } catch ( Exception e ) {
        throw new KettleValueException( BaseMessages.getString(
          PKG, "ScriptValuesMod.Log.CouldNotCompileJavascript" ), e );
      }
    }

    // Filling the defined TranVars with the Values from the Row
    //
    Object[] outputRow = RowDataUtil.resizeArray( row, data.outputRowMeta.size() );

    // Keep an index...
    int outputIndex = rowMeta.size();

    // Keep track of the changed values...
    //
    final Map<Integer, Value> usedRowValues;

    if ( meta.isCompatible() ) {
      usedRowValues = new Hashtable<Integer, Value>();
    } else {
      usedRowValues = null;
    }

    try {
      try {
        if ( meta.isCompatible() ) {
          Row v2Row = RowMeta.createOriginalRow( rowMeta, row );
          Scriptable jsV2Row = Context.toObject( v2Row, data.scope );
          data.scope.put( "row", data.scope, jsV2Row );
          v2Row.getUsedValueListeners().add( new ValueUsedListener() {
            public void valueIsUsed( int index, Value value ) {
              usedRowValues.put( index, value );
            }
          } );
        } else {
          Scriptable jsrow = Context.toObject( row, data.scope );
          data.scope.put( "row", data.scope, jsrow );
        }

        for ( int i = 0; i < data.fields_used.length; i++ ) {
          ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.fields_used[ i ] );
          Object valueData = row[ data.fields_used[ i ] ];

          if ( meta.isCompatible() ) {
            data.values_used[ i ] = valueMeta.createOriginalValue( valueData );

            Scriptable jsarg = Context.toObject( data.values_used[ i ], data.scope );
            data.scope.put( valueMeta.getName(), data.scope, jsarg );
          } else {
            Object normalStorageValueData = valueMeta.convertToNormalStorageType( valueData );
            Scriptable jsarg;
            if ( normalStorageValueData != null ) {
              jsarg = Context.toObject( normalStorageValueData, data.scope );
            } else {
              jsarg = null;
            }
            data.scope.put( valueMeta.getName(), data.scope, jsarg );
          }
        }

        // also add the meta information for the hole row
        Scriptable jsrowMeta = Context.toObject( rowMeta, data.scope );
        data.scope.put( "rowMeta", data.scope, jsrowMeta );
      } catch ( Exception e ) {
        throw new KettleValueException( BaseMessages.getString( PKG, "ScriptValuesMod.Log.UnexpectedeError" ), e );
      }

      // Executing our Script
      data.script.exec( data.cx, data.scope );

      if ( bFirstRun ) {
        bFirstRun = false;
        // Check if we had a Transformation Status
        Object tran_stat = data.scope.get( "trans_Status", data.scope );
        if ( tran_stat != ScriptableObject.NOT_FOUND ) {
          bWithTransStat = true;
          if ( log.isDetailed() ) {
            logDetailed( ( "tran_Status found. Checking transformation status while script execution." ) );
          }
        } else {
          if ( log.isDetailed() ) {
            logDetailed( ( "No tran_Status found. Transformation status checking not available." ) );
          }
          bWithTransStat = false;
        }
      }

      if ( bWithTransStat ) {
        iTranStat = (int) Context.toNumber( data.scope.get( "trans_Status", data.scope ) );
      } else {
        iTranStat = CONTINUE_TRANSFORMATION;
      }

      if ( iTranStat == CONTINUE_TRANSFORMATION ) {
        bRC = true;
        for ( int i = 0; i < meta.getFieldname().length; i++ ) {
          Object result = data.scope.get( meta.getFieldname()[ i ], data.scope );
          Object valueData = getValueFromJScript( result, i );
          if ( data.replaceIndex[ i ] < 0 ) {
            outputRow[ outputIndex++ ] = valueData;
          } else {
            outputRow[ data.replaceIndex[ i ] ] = valueData;
          }
        }

        // Also modify the "in-place" value changes:
        // --> the field.trim() type of changes...
        // As such we overwrite all the used fields again.
        //
        if ( meta.isCompatible() ) {
          for ( int i = 0; i < data.values_used.length; i++ ) {
            ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.fields_used[ i ] );
            outputRow[ data.fields_used[ i ] ] = valueMeta.getValueData( data.values_used[ i ] );
          }

          // Grab the variables in the "row" object too.
          //
          for ( Map.Entry<Integer, Value> entry : usedRowValues.entrySet() ) {
            Value value = entry.getValue();
            Integer index = entry.getKey();
            ValueMetaInterface valueMeta = rowMeta.getValueMeta( index );
            outputRow[ index ] = valueMeta.getValueData( value );
          }

        }
        putRow( data.outputRowMeta, outputRow );
      } else {
        switch ( iTranStat ) {
          case SKIP_TRANSFORMATION:
            // eat this row.
            bRC = true;
            break;
          case ABORT_TRANSFORMATION:
            if ( data.cx != null ) {
              Context.exit();
            }
            stopAll();
            setOutputDone();
            bRC = false;
            break;
          case ERROR_TRANSFORMATION:
            if ( data.cx != null ) {
              Context.exit();
            }
            setErrors( 1 );
            stopAll();
            bRC = false;
            break;
          default:
            break;
        }

        // TODO: kick this "ERROR handling" junk out now that we have solid error handling in place.
        //
      }
    } catch ( Exception e ) {
      throw new KettleValueException( BaseMessages.getString( PKG, "ScriptValuesMod.Log.JavascriptError" ), e );
    }
    return bRC;
  }

  public Object getValueFromJScript( Object result, int i ) throws KettleValueException {
    String fieldName = meta.getFieldname()[ i ];
    if ( !Utils.isEmpty( fieldName ) ) {
      // res.setName(meta.getRename()[i]);
      // res.setType(meta.getType()[i]);

      try {
        return ( result == null ) ? null
          : JavaScriptUtils.convertFromJs( result, meta.getType()[ i ], fieldName );
      } catch ( Exception e ) {
        throw new KettleValueException( BaseMessages.getString( PKG, "ScriptValuesMod.Log.JavascriptError" ), e );
      }
    } else {
      throw new KettleValueException( "No name was specified for result value #" + ( i + 1 ) );
    }
  }

  public RowMetaInterface getOutputRowMeta() {
    return data.outputRowMeta;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (ScriptValuesMetaMod) smi;
    data = (ScriptValuesModData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) {
      // Modification for Additional End Function
      try {
        if ( data.cx != null ) {
          // Checking for EndScript
          if ( strEndScript != null && strEndScript.length() > 0 ) {
            Script endScript = data.cx.compileString( strEndScript, "trans_End", 1, null );
            endScript.exec( data.cx, data.scope );
            if ( log.isDetailed() ) {
              logDetailed( ( "End Script found!" ) );
            }
          } else {
            if ( log.isDetailed() ) {
              logDetailed( ( "No end Script found!" ) );
            }
          }
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "ScriptValuesMod.Log.UnexpectedeError" ) + " : " + e.toString() );
        logError( BaseMessages.getString( PKG, "ScriptValuesMod.Log.ErrorStackTrace" )
          + Const.CR + Const.getStackTracker( e ) );
        setErrors( 1 );
        stopAll();
      }

      try {
        if ( data.cx != null ) {
          Context.exit();
        }
      } catch ( Exception er ) {
        // Eat this error, it's typically : "Calling Context.exit without previous Context.enter"
        // logError(BaseMessages.getString(PKG, "System.Log.UnexpectedError"), er);
      }

      setOutputDone();
      return false;
    }

    // Getting the Row, with the Transformation Status
    try {
      addValues( getInputRowMeta(), r );
    } catch ( KettleValueException e ) {
      String location = null;
      if ( e.getCause() instanceof EvaluatorException ) {
        EvaluatorException ee = (EvaluatorException) e.getCause();
        location = "--> " + ee.lineNumber() + ":" + ee.columnNumber();
      }

      if ( getStepMeta().isDoingErrorHandling() ) {
        putError( getInputRowMeta(), r, 1, e.getMessage() + Const.CR + location, null, "SCR-001" );
        bRC = true; // continue by all means, even on the first row and out of this ugly design
      } else {
        throw ( e );
      }
    }

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "ScriptValuesMod.Log.LineNumber" ) + getLinesRead() );
    }
    return bRC;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ScriptValuesMetaMod) smi;
    data = (ScriptValuesModData) sdi;

    if ( super.init( smi, sdi ) ) {

      // Add init code here.
      // Get the actual Scripts from our MetaData
      jsScripts = meta.getJSScripts();
      for ( int j = 0; j < jsScripts.length; j++ ) {
        switch ( jsScripts[ j ].getScriptType() ) {
          case ScriptValuesScript.TRANSFORM_SCRIPT:
            strTransformScript = jsScripts[ j ].getScript();
            break;
          case ScriptValuesScript.START_SCRIPT:
            strStartScript = jsScripts[ j ].getScript();
            break;
          case ScriptValuesScript.END_SCRIPT:
            strEndScript = jsScripts[ j ].getScript();
            break;
          default:
            break;
        }
      }

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    try {
      if ( data.cx != null ) {
        Context.exit();
      }
    } catch ( Exception er ) {
      // Eat this error, it's typically : "Calling Context.exit without previous Context.enter"
      // logError(BaseMessages.getString(PKG, "System.Log.UnexpectedError"), er);
    }

    super.dispose( smi, sdi );
  }

}
