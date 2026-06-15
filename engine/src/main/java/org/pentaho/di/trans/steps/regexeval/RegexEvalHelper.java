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

package org.pentaho.di.trans.steps.regexeval;

import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexEvalHelper extends BaseStepHelper {

  private static final String TEST_REGEX = "testRegex";

    private static final String ERROR_MSG = "errorMessage";

  private final RegexEvalMeta regexEvalMeta;

  public RegexEvalHelper( RegexEvalMeta regexEvalMeta ) {
    this.regexEvalMeta = regexEvalMeta;
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( TEST_REGEX.equals( method ) ) {
      response = testRegexAction( transMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  public JSONObject testRegexAction( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    try {
      String script = regexEvalMeta.getScript();
      if ( script != null && regexEvalMeta.isUseVariableInterpolationFlagSet() && transMeta != null ) {
          script = transMeta.environmentSubstitute( script );
      }
      if ( script == null || script.trim().isEmpty() ) {
        response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
        response.put( ERROR_MSG, org.pentaho.di.i18n.BaseMessages.getString( RegexEvalMeta.class, "RegexEvalHelperDialog.EnterScript.Message" ) );
        return response;
      }

      String regexOptions = regexEvalMeta.getRegexOptions();
      String fullPattern = regexOptions + script;

      int flags = 0;
      if ( regexEvalMeta.isCanonicalEqualityFlagSet() ) {
        flags |= Pattern.CANON_EQ;
      }

      Pattern.compile( fullPattern, flags );

      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      response.put( "message", org.pentaho.di.i18n.BaseMessages.getString( RegexEvalMeta.class, "RegexEvalHelperDialog.ScriptSuccessfullyCompiled" ) );
    } catch ( PatternSyntaxException e ) {
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
      response.put( ERROR_MSG, org.pentaho.di.i18n.BaseMessages.getString( RegexEvalMeta.class, "RegexEvalHelperDialog.ErrorCompiling.Message" )  + " " + e.getDescription() );
    } catch ( Exception e ) {
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
      response.put( ERROR_MSG, org.pentaho.di.i18n.BaseMessages.getString( RegexEvalMeta.class, "RegexEvalDialog.Exception.CouldNotCompileScript" ) + " " + e.getMessage() );
    }
    return response;
  }
}
