/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.formula;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.libformula.editor.FormulaEvaluator;
import org.pentaho.libformula.editor.FormulaMessage;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;
import org.pentaho.reporting.libraries.formula.parser.FormulaParser;

/**
 * Calculate new field values using pre-defined functions.
 *
 * @author Matt
 * @since 8-sep-2005
 */
public class Formula extends BaseStep implements StepInterface {
  private FormulaMeta meta;
  private FormulaData data;

  public Formula( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                  Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (FormulaMeta) smi;
    data = (FormulaData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Create the context
      data.context = new RowForumulaContext( data.outputRowMeta );
      data.parser = new FormulaParser();

      // Calculate replace indexes...
      //
      data.replaceIndex = new int[ meta.getFormula().length ];
      for ( int i = 0; i < meta.getFormula().length; i++ ) {
        FormulaMetaFunction fn = meta.getFormula()[ i ];
        if ( !Utils.isEmpty( fn.getReplaceField() ) ) {
          data.replaceIndex[ i ] = getInputRowMeta().indexOfValue( fn.getReplaceField() );
          if ( data.replaceIndex[ i ] < 0 ) {
            throw new KettleException( "Unknown field specified to replace with a formula result: ["
              + fn.getReplaceField() + "]" );
          }
        } else {
          data.replaceIndex[ i ] = -1;
        }
      }
    }

    if ( log.isRowLevel() ) {
      logRowlevel( "Read row #" + getLinesRead() + " : " + Arrays.toString( r ) );
    }

    Object[] outputRowData = calcFields( getInputRowMeta(), r );
    putRow( data.outputRowMeta, outputRowData ); // copy row to possible alternate rowset(s).

    if ( log.isRowLevel() ) {
      logRowlevel( "Wrote row #" + getLinesWritten() + " : " + Arrays.toString( r ) );
    }
    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( "Linenr " + getLinesRead() );
    }

    return true;
  }

  private Object[] calcFields( RowMetaInterface rowMeta, Object[] r ) throws KettleValueException {
    try {
      Object[] outputRowData = RowDataUtil.createResizedCopy( r, data.outputRowMeta.size() );
      int tempIndex = rowMeta.size();

      // Assign this tempRowData to the formula context
      //
      data.context.setRowData( outputRowData );

      // Initialize parsers etc. Only do it once.
      //
      if ( data.formulas == null ) {
        // Create a set of LValues to put the parsed results in...
        data.formulas = new org.pentaho.reporting.libraries.formula.Formula[ meta.getFormula().length ];
        for ( int i = 0; i < meta.getFormula().length; i++ ) {
          FormulaMetaFunction fn = meta.getFormula()[ i ];
          if ( !Utils.isEmpty( fn.getFieldName() ) ) {
            data.formulas[ i ] = data.createFormula( meta.getFormula()[ i ].getFormula() );
          } else {
            throw new KettleException( "Unable to find field name for formula ["
              + Const.NVL( fn.getFormula(), "" ) + "]" );
          }
        }
      }

      for ( int i = 0; i < meta.getFormula().length; i++ ) {
        FormulaMetaFunction fn = meta.getFormula()[ i ];
        if ( !Utils.isEmpty( fn.getFieldName() ) ) {
          if ( data.formulas[ i ] == null ) {
            data.formulas[ i ] = data.createFormula( meta.getFormula()[ i ].getFormula() );
          }

          // this is main part of all this step: calculate formula
          Object formulaResult = data.formulas[ i ].evaluate();
          if ( formulaResult instanceof LibFormulaErrorValue ) {
            // inspect why it is happens to get clear error message.
            throw new KettleException( "Error calculate formula. Formula "
              + fn.getFormula() + " output field: " + fn.getFieldName() + ", error is: " + formulaResult.toString() );
          }

          // Calculate the return type on the first row...
          // for most cases we can try to convert data on a fly.
          if ( data.returnType[ i ] < 0 ) {
            if ( formulaResult instanceof String ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_STRING;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_STRING );
            } else if ( formulaResult instanceof Integer ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_INTEGER;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_INTEGER );
            } else if ( formulaResult instanceof Long ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_LONG;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_INTEGER );
            } else if ( formulaResult instanceof Date ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_DATE;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_DATE );
            } else if ( formulaResult instanceof BigDecimal ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_BIGDECIMAL;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_BIGNUMBER );
            } else if ( formulaResult instanceof Number ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_NUMBER;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_NUMBER );
              // this types we will not make attempt to auto-convert
            } else if ( formulaResult instanceof byte[] ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_BYTE_ARRAY;
              if ( fn.getValueType() != ValueMetaInterface.TYPE_BINARY ) {
                throw new KettleValueException( "Please specify a Binary type for field ["
                  + fn.getFieldName() + "] as a result of formula [" + fn.getFormula() + "]" );
              }
            } else if ( formulaResult instanceof Boolean ) {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_BOOLEAN;
              if ( fn.getValueType() != ValueMetaInterface.TYPE_BOOLEAN ) {
                throw new KettleValueException( "Please specify a Boolean type for field ["
                  + fn.getFieldName() + "] as a result of formula [" + fn.getFormula() + "]" );
              }
            } else {
              data.returnType[ i ] = FormulaData.RETURN_TYPE_STRING;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_STRING );
            }
          }

          int realIndex = ( data.replaceIndex[ i ] < 0 ) ? tempIndex++ : data.replaceIndex[ i ];
          outputRowData[ realIndex ] = getReturnValue( formulaResult, data.returnType[ i ], realIndex, fn );
        }
      }

      return outputRowData;
    } catch ( Throwable e ) {
      throw new KettleValueException( e );
    }
  }

  protected Object getReturnValue( Object formulaResult, int returnType, int realIndex, FormulaMetaFunction fn )
    throws KettleException {
    if ( formulaResult == null ) {
      return null;
    }
    Object value = null;
    switch ( returnType ) {
      case FormulaData.RETURN_TYPE_STRING:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult.toString();
        }
        break;
      case FormulaData.RETURN_TYPE_NUMBER:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = ( (Number) formulaResult ).doubleValue();
        }
        break;
      case FormulaData.RETURN_TYPE_INTEGER:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = (long) (Integer) formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_LONG:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_DATE:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_BIGDECIMAL:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_BYTE_ARRAY:
        value = formulaResult;
        break;
      case FormulaData.RETURN_TYPE_BOOLEAN:
        value = formulaResult;
        break;
      case FormulaData.RETURN_TYPE_TIMESTAMP:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult;
        }
        break;
    } //if none case is caught - null is returned.
    return value;
  }

  private Object convertDataToTargetValueMeta( int i, Object formulaResult ) throws KettleException {
    if ( formulaResult == null ) {
      return formulaResult;
    }
    ValueMetaInterface target = data.outputRowMeta.getValueMeta( i );
    ValueMetaInterface actual = ValueMetaFactory.guessValueMetaInterface( formulaResult );
    Object value = target.convertData( actual, formulaResult );
    return value;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (FormulaMeta) smi;
    data = (FormulaData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.

      // Return data type discovery is expensive, let's discover them one time only.
      //
      data.returnType = new int[ meta.getFormula().length ];
      for ( int i = 0; i < meta.getFormula().length; i++ ) {
        data.returnType[ i ] = -1;
      }
      return true;
    }
    return false;
  }

  private String[] keyWords;

  public JSONObject doAction( String fieldName, StepMetaInterface stepMetaInterface, TransMeta transMeta,
                              Trans trans, Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      Method actionMethod = this.getClass().getDeclaredMethod( fieldName + "Action", Map.class );
      response = (JSONObject) actionMethod.invoke( this, queryParamToValues );
    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      System.err.println( "Error in doAction: " + e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "unchecked" )
  private JSONObject evaluateFormulaAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      String formula = queryParams.get( "formulaSyntax" );
      if ( formula == null || formula.trim().isEmpty() ) {
        response.put( "error", "No formula provided." );
        response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
        return response;
      }

      // Determine input fields: use query param if provided, otherwise use getInputRowMeta()
      String inputFieldsParam = queryParams.get( "inputFields" );
      String[] evalInputFields;
      if ( inputFieldsParam != null && !inputFieldsParam.trim().isEmpty() ) {
        evalInputFields = inputFieldsParam.split( "," );
        for ( int i = 0; i < evalInputFields.length; i++ ) {
          evalInputFields[ i ] = evalInputFields[ i ].trim();
        }
      } else {
        RowMetaInterface inputMeta = getInputRowMeta();
        if ( inputMeta != null ) {
          evalInputFields = inputMeta.getFieldNames();
        } else {
          evalInputFields = new String[ 0 ];
        }
      }

      // Load keyWords from functions.xml if not already loaded.
      if ( this.keyWords == null ) {
        try {
          // Adjust the package if necessary.
          org.pentaho.libformula.editor.function.FunctionLib functionLib =
            new org.pentaho.libformula.editor.function.FunctionLib( "functions.xml" );
          this.keyWords = functionLib.getFunctionNames();
        } catch ( Exception e ) {
          System.err.println( "Error loading function keywords: " + e.getMessage() );
          this.keyWords = new String[ 0 ];
        }
      }

      // Create a new FormulaEvaluator instance.
      org.pentaho.libformula.editor.FormulaEvaluator evaluator =
        new org.pentaho.libformula.editor.FormulaEvaluator( this.keyWords, evalInputFields );
      // Evaluate the formula.
      java.util.Map<String, org.pentaho.libformula.editor.FormulaMessage> messages =
        evaluator.evaluateFormula( formula );

      // Build a JSON array from the evaluation messages.
      JSONArray messagesArray = new JSONArray();
      for ( org.pentaho.libformula.editor.FormulaMessage msg : messages.values() ) {
        JSONObject msgJson = new JSONObject();
        msgJson.put( "message", msg.toString() );
        msgJson.put( "type", msg.getType() );
        msgJson.put( "subject", msg.getSubject() );
        msgJson.put( "detail", msg.getMessage() );
        if ( msg.getPosition() != null ) {
          JSONObject posJson = new JSONObject();
          posJson.put( "startLine", msg.getPosition().getStartLine() );
          posJson.put( "startColumn", msg.getPosition().getStartColumn() );
          posJson.put( "endLine", msg.getPosition().getEndLine() );
          posJson.put( "endColumn", msg.getPosition().getEndColumn() );
          msgJson.put( "position", posJson );
        }
        messagesArray.add( msgJson );
      }
      response.put( "messages", messagesArray );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      response.put( "error", e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "unchecked" )
  private JSONObject formulaTreeDataAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      String someData = queryParams.get( "someData" );

      // Load the function library from functions.xml.
      // Adjust the file path if necessary.
      org.pentaho.libformula.editor.function.FunctionLib functionLib =
        new org.pentaho.libformula.editor.function.FunctionLib( "functions.xml" );

      // Get all function categories.
      String[] categories = functionLib.getFunctionCategories();
      JSONArray categoryArray = new JSONArray();

      // Iterate over each category.
      for ( int i = 0; i < categories.length; i++ ) {
        String category = categories[ i ];
        String displayCategory = category;
        // If the category starts with "%" use i18n lookup.
        if ( category.startsWith( "%" ) ) {
          displayCategory = org.pentaho.di.i18n.BaseMessages.getString(
            org.pentaho.libformula.editor.function.FunctionLib.class, category.substring( 1 ) );
        }

        JSONObject categoryObj = new JSONObject();
        categoryObj.put( "category", displayCategory );

        // Get the functions for this category.
        String[] fnames = functionLib.getFunctionsForACategory( category );
        JSONArray functionsArray = new JSONArray();
        for ( String fname : fnames ) {
          JSONObject funcObj = new JSONObject();
          funcObj.put( "name", fname );

          // Retrieve function details.
          org.pentaho.libformula.editor.function.FunctionDescription fd = functionLib.getFunctionDescription( fname );
          if ( fd != null ) {
            // Assume these getters exist in FunctionDescription.
            funcObj.put( "description", fd.getDescription() );
            funcObj.put( "syntax", fd.getSyntax() );
            funcObj.put( "returns", fd.getReturns() );
            funcObj.put( "constraints", fd.getConstraints() );
            funcObj.put( "semantics", fd.getSemantics() );
            funcObj.put( "htmlreport", fd.getHtmlReport() );
          }
          functionsArray.add( funcObj );
        }
        categoryObj.put( "functions", functionsArray );
        categoryArray.add( categoryObj );
      }
      response.put( "functionTree", categoryArray );
      response.put( org.pentaho.di.trans.step.StepInterface.ACTION_STATUS,
        org.pentaho.di.trans.step.StepInterface.SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      response.put( "error", e.getMessage() );
      response.put( org.pentaho.di.trans.step.StepInterface.ACTION_STATUS,
        org.pentaho.di.trans.step.StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }
}
