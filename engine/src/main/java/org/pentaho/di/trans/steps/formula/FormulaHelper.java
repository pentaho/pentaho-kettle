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

package org.pentaho.di.trans.steps.formula;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.libformula.editor.FormulaEvaluator;
import org.pentaho.libformula.editor.FormulaMessage;
import org.pentaho.libformula.editor.function.FunctionDescription;
import org.pentaho.libformula.editor.function.FunctionLib;

import java.util.Map;

public class FormulaHelper extends BaseStepHelper {

  private static final String EVALUATE_FORMULA = "evaluateFormula";
  private static final String FORMULA_TREE_DATA = "formulaTreeData";
  private static final String ERROR = "error";

  private String[] keyWords;

  public FormulaHelper() {
    super();
  }

  /**
   * Handles step-specific actions for Formula.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case EVALUATE_FORMULA:
          response = evaluateFormulaAction( transMeta, queryParams );
          break;
        case FORMULA_TREE_DATA:
          response = formulaTreeDataAction( queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
          break;
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR, ex.getMessage() );
    }
    return response;
  }

  /**
   * Evaluates a formula and returns validation messages.
   *
   * @param transMeta   The transformation metadata.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing evaluation messages.
   */
  public JSONObject evaluateFormulaAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      String formula = queryParams.get( "formulaSyntax" );
      if ( formula == null || formula.trim().isEmpty() ) {
        response.put( ERROR, "No formula provided." );
        response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
        return response;
      }

      getKeyWords( response );
      if ( response.get( StepInterface.ACTION_STATUS ) != null
        && response.get( StepInterface.ACTION_STATUS ).equals( StepInterface.FAILURE_RESPONSE ) ) {
        return response;
      }

      // Determine input fields: use query param if provided, otherwise try to get from step
      String[] evalInputFields = getEvalInputFields( transMeta, queryParams );

      FormulaEvaluator evaluator = new FormulaEvaluator( this.keyWords, evalInputFields );

      // Evaluate the formula.
      Map<String, FormulaMessage> messages = evaluator.evaluateFormula( formula );
      buildEvaluationMessageResponse( response, messages );
    } catch ( Exception e ) {
      response.put( ERROR, e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the formula function tree data.
   *
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the function tree.
   */
  public JSONObject formulaTreeDataAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      FunctionLib functionLib = new FunctionLib( "functions.xml" );

      String[] categories = functionLib.getFunctionCategories();
      JSONArray categoryArray = new JSONArray();

      for ( int i = 0; i < categories.length; i++ ) {
        String category = categories[ i ];
        String displayCategory = category;
        if ( category.startsWith( "%" ) ) {
          displayCategory = BaseMessages.getString( FunctionLib.class, category.substring( 1 ) );
        }

        JSONObject categoryObj = new JSONObject();
        categoryObj.put( "category", displayCategory );

        String[] fnames = functionLib.getFunctionsForACategory( category );
        JSONArray functionsArray = new JSONArray();
        for ( String fname : fnames ) {
          JSONObject funcObj = new JSONObject();
          funcObj.put( "name", fname );

          FunctionDescription fd = functionLib.getFunctionDescription( fname );
          if ( fd != null ) {
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
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      response.put( ERROR, e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  private void buildEvaluationMessageResponse( JSONObject response, Map<String, FormulaMessage> messages ) {
    JSONArray messagesArray = new JSONArray();
    for ( FormulaMessage msg : messages.values() ) {
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
  }

  private String[] getEvalInputFields( TransMeta transMeta, Map<String, String> queryParams ) {
    String[] evalInputFields;
    String inputFieldsParam = queryParams.get( "inputFields" );
    String stepName = queryParams.get( "stepName" );

    if ( inputFieldsParam != null && !inputFieldsParam.trim().isEmpty() ) {
      evalInputFields = inputFieldsParam.split( "," );
      for ( int i = 0; i < evalInputFields.length; i++ ) {
        evalInputFields[ i ] = evalInputFields[ i ].trim();
      }
    } else if ( stepName != null && !stepName.trim().isEmpty() ) {
      try {
        RowMetaInterface inputMeta = transMeta.getPrevStepFields( stepName );
        if ( inputMeta != null ) {
          evalInputFields = inputMeta.getFieldNames();
        } else {
          evalInputFields = new String[ 0 ];
        }
      } catch ( Exception e ) {
        evalInputFields = new String[ 0 ];
      }
    } else {
      evalInputFields = new String[ 0 ];
    }

    return evalInputFields;
  }

  private void getKeyWords( JSONObject response ) {
    if ( this.keyWords == null ) {
      try {
        FunctionLib functionLib = new FunctionLib( "functions.xml" );
        this.keyWords = functionLib.getFunctionNames();
      } catch ( Exception e ) {
        this.keyWords = new String[ 0 ];
        response.put( ERROR, "Error loading function keywords: " + e.getMessage() );
        response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
      }
    }
  }
}
