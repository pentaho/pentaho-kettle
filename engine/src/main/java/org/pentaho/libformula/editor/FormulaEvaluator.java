/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.libformula.editor;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.reporting.libraries.formula.lvalues.ContextLookup;
import org.pentaho.reporting.libraries.formula.lvalues.FormulaFunction;
import org.pentaho.reporting.libraries.formula.lvalues.LValue;
import org.pentaho.reporting.libraries.formula.lvalues.ParsePosition;
import org.pentaho.reporting.libraries.formula.lvalues.StaticValue;
import org.pentaho.reporting.libraries.formula.lvalues.Term;
import org.pentaho.reporting.libraries.formula.parser.FormulaParser;
import org.pentaho.reporting.libraries.formula.parser.ParseException;
import org.pentaho.reporting.libraries.formula.parser.Token;
import org.pentaho.reporting.libraries.formula.typing.coretypes.DateTimeType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.LogicalType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.NumberType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.TextType;

public class FormulaEvaluator {

  private String[] keyWords;
  private String[] inputFields;
  private FormulaParser formulaParser;

  /**
   * @param keyWords
   * @param inputFields
   */
  public FormulaEvaluator( String[] keyWords, String[] inputFields ) {
    this.keyWords = keyWords;
    this.inputFields = inputFields;

    formulaParser = new FormulaParser();
  }

  public Map<String, FormulaMessage> evaluateFormula( String expression ) {

    Map<String, FormulaMessage> messages = new HashMap<String, FormulaMessage>();

    if ( expression == null || expression.trim().equals( "" ) ) {
      return messages;
    }

    try {
      LValue lValue = formulaParser.parse( expression.trim() );

      // if (lValue.toString()!=null) report+=lValue.toString()+" ("+lValue.getClass().getName()+") \n";

      // Verify all the functions used
      // See if we can find them all in our list.
      //

      // Key - Message mapping
      //

      if ( lValue != null ) {
        try {
          verifyLValue( lValue, messages );
        } catch ( Exception e ) {
          FormulaMessage message =
            new FormulaMessage( FormulaMessage.TYPE_ERROR, "Parse Exception", "Parsing error in formula : "
              + e.getMessage() );
          messages.put( message.toString(), message );
        }
      }
    } catch ( ParseException parseException ) {
      Token token = parseException.currentToken;
      boolean handled = false;
      if ( token != null ) {
        String problem = token.toString();
        if ( problem != null ) {
          FormulaMessage message =
            new FormulaMessage(
              FormulaMessage.TYPE_ERROR, new ParsePosition(
                token.beginLine, token.beginColumn, token.endLine, token.endColumn ), "Parse Exception",
              "Parse exception near '"
                + problem + "' on line " + token.beginLine + ", column " + token.beginColumn + "\n\n"
                + parseException.getMessage() );
          messages.put( message.toString(), message );
          handled = true;
        }
      }
      if ( !handled ) {
        if ( parseException.getMessage() != null ) {
          FormulaMessage message =
            new FormulaMessage( FormulaMessage.TYPE_ERROR, "Parse Exception", "Parsing error in formula : "
              + parseException.getMessage() );
          messages.put( message.toString(), message );
        } else {
          FormulaMessage message =
            new FormulaMessage( FormulaMessage.TYPE_ERROR, "Parse Exception", "Parsing error in formula : "
              + parseException.toString() );
          messages.put( message.toString(), message );
        }
      }
    } catch ( Throwable e ) {
      if ( e.getMessage() != null ) {
        FormulaMessage message =
          new FormulaMessage( FormulaMessage.TYPE_ERROR, "Parse Exception", "Parsing error in formula : "
            + e.getMessage() );
        messages.put( message.toString(), message );
      } else {
        FormulaMessage message =
          new FormulaMessage( FormulaMessage.TYPE_ERROR, "Parse Exception", "Parsing error in formula : "
            + e.toString() );
        messages.put( message.toString(), message );
      }
    }

    return messages;
  }

  public void verifyLValue( LValue lvalue, Map<String, FormulaMessage> messages ) {

    if ( lvalue instanceof FormulaFunction ) {
      FormulaFunction formulaFunction = (FormulaFunction) lvalue;
      String functionName = formulaFunction.getFunctionName();
      if ( Const.indexOfString( functionName, keyWords ) < 0 ) {
        FormulaMessage formulaMessage =
          new FormulaMessage(
            FormulaMessage.TYPE_ERROR, formulaFunction.getParsePosition(), functionName, "Unknown function" );
        messages.put( formulaMessage.toString(), formulaMessage );
      } else {
        FormulaMessage formulaMessage =
          new FormulaMessage(
            FormulaMessage.TYPE_FUNCTION, formulaFunction.getParsePosition(), functionName, "Function" );
        messages.put( formulaMessage.toString(), formulaMessage );
      }
      LValue[] arguments = formulaFunction.getChildValues();
      if ( arguments != null ) {
        for ( LValue argument : arguments ) {
          verifyLValue( argument, messages );
        }
      }
    } else if ( lvalue instanceof Term ) {
      //
      // This is a Term (an expression, can contain several other LValues)
      //
      Term term = (Term) lvalue;

      LValue head = term.getHeadValue();
      verifyLValue( head, messages );
      LValue[] operands = term.getOperands();
      if ( operands != null ) {
        for ( LValue operand : operands ) {
          verifyLValue( operand, messages );
        }
      }
      // report.append("Term head value found : ").append(term.getHeadValue().toString());
    } else if ( lvalue instanceof StaticValue ) {
      //
      // Static value : String, number, date...
      //
      StaticValue staticValue = (StaticValue) lvalue;

      if ( staticValue.getValueType() instanceof NumberType ) {
        FormulaMessage formulaMessage =
          new FormulaMessage( FormulaMessage.TYPE_STATIC_NUMBER, staticValue.getParsePosition(), staticValue
            .toString(), "Static number" );
        messages.put( formulaMessage.toString(), formulaMessage );
      } else if ( staticValue.getValueType() instanceof TextType ) {
        FormulaMessage formulaMessage =
          new FormulaMessage( FormulaMessage.TYPE_STATIC_STRING, staticValue.getParsePosition(), staticValue
            .toString(), "Static string" );
        messages.put( formulaMessage.toString(), formulaMessage );
      } else if ( staticValue.getValueType() instanceof DateTimeType ) {
        FormulaMessage formulaMessage =
          new FormulaMessage( FormulaMessage.TYPE_STATIC_DATE, staticValue.getParsePosition(), staticValue
            .toString(), "Static date/time" );
        messages.put( formulaMessage.toString(), formulaMessage );
      } else if ( staticValue.getValueType() instanceof LogicalType ) {
        FormulaMessage formulaMessage =
          new FormulaMessage( FormulaMessage.TYPE_STATIC_LOGICAL, staticValue.getParsePosition(), staticValue
            .toString(), "Static logical" );
        messages.put( formulaMessage.toString(), formulaMessage );
      }

    } else if ( lvalue instanceof ContextLookup ) {
      //
      // A field name
      //
      ContextLookup contextLookup = (ContextLookup) lvalue;

      FormulaMessage fieldMessage =
        new FormulaMessage(
          FormulaMessage.TYPE_FIELD, contextLookup.getParsePosition(), contextLookup.getName(), "Field" );
      messages.put( fieldMessage.toString(), fieldMessage );

      String name = contextLookup.getName();
      if ( Const.indexOfString( name, inputFields ) < 0 ) {
        FormulaMessage formulaMessage =
          new FormulaMessage(
            FormulaMessage.TYPE_ERROR, contextLookup.getParsePosition(), name, "Unknown field name" );
        messages.put( formulaMessage.toString(), formulaMessage );
      }
    }
  }

}
