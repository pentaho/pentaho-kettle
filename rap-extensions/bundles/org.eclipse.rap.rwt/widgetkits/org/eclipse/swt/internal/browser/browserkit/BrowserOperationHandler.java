/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.browser.browserkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.swt.internal.browser.browserkit.BrowserLCA.EXECUTED_FUNCTION_ERROR;
import static org.eclipse.swt.internal.browser.browserkit.BrowserLCA.EXECUTED_FUNCTION_NAME;
import static org.eclipse.swt.internal.browser.browserkit.BrowserLCA.EXECUTED_FUNCTION_RESULT;
import static org.eclipse.swt.internal.events.EventTypes.PROGRESS_CHANGED;
import static org.eclipse.swt.internal.events.EventTypes.PROGRESS_COMPLETED;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.internal.widgets.IBrowserAdapter;
import org.eclipse.swt.widgets.Event;


public class BrowserOperationHandler extends ControlOperationHandler<Browser> {

  private static final String METHOD_EXECUTE_FUNCTION = "executeFunction";
  private static final String PARAM_NAME = "name";
  private static final String PARAM_ARGUMENTS = "arguments";
  private static final String METHOD_EVALUATION_SUCCEEDED = "evaluationSucceeded";
  private static final String METHOD_EVALUATION_FAILED = "evaluationFailed";
  private static final String PARAM_RESULT = "result";
  private static final String EVENT_PROGRESS = "Progress";

  public BrowserOperationHandler( Browser browser ) {
    super( browser );
  }

  @Override
  public void handleCall( Browser browser, String method, JsonObject properties ) {
    if( METHOD_EXECUTE_FUNCTION.equals( method ) ) {
      handleCallExecuteFunction( browser, properties );
    } else if( METHOD_EVALUATION_SUCCEEDED.equals( method ) ) {
      handleCallEvaluationSucceeded( browser, properties );
    } else if( METHOD_EVALUATION_FAILED.equals( method ) ) {
      handleCallEvaluationFailed( browser );
    }
  }

  @Override
  public void handleNotify( Browser browser, String eventName, JsonObject properties ) {
    if( EVENT_PROGRESS.equals( eventName ) ) {
      handleNotifyProgress( browser );
    } else {
      super.handleNotify( browser, eventName, properties );
    }
  }

  /*
   * PROTOCOL CALL executeFunction
   *
   * @param name (string) the name of the function
   * @param arguments ([object]) array with function arguments
   */
  public void handleCallExecuteFunction( final Browser browser, JsonObject properties ) {
    String name = properties.get( PARAM_NAME ).asString();
    final BrowserFunction function = findBrowserFunction( browser, name );
    if( function != null ) {
      final Object[] arguments = jsonToJava( properties.get( PARAM_ARGUMENTS ).asArray() );
      ProcessActionRunner.add( new Runnable() {
        @Override
        public void run() {
          executeFunction( browser, function, arguments );
        }
      } );
    }
  }

  /*
   * PROTOCOL CALL evaluationSucceeded
   *
   * @param result ([object]) array with one element that contains evaluation result
   */
  public void handleCallEvaluationSucceeded( Browser browser, JsonObject properties ) {
    JsonValue value = properties.get( PARAM_RESULT );
    Object result = null;
    if( value != null && !value.isNull() ) {
      result = jsonToJava( value.asArray() )[ 0 ];
    }
    getAdapter( browser ).setExecuteResult( true, result );
  }

  /*
   * PROTOCOL CALL evaluationFailed
   *
   */
  public void handleCallEvaluationFailed( Browser browser ) {
    getAdapter( browser ).setExecuteResult( false, null );
  }

  /*
   * PROTOCOL NOTIFY Progress
   *
   */
  public void handleNotifyProgress( Browser browser ) {
    browser.notifyListeners( PROGRESS_CHANGED, new Event() );
    browser.notifyListeners( PROGRESS_COMPLETED, new Event() );
  }

  private static BrowserFunction findBrowserFunction( Browser browser, String name ) {
    BrowserFunction[] functions = getAdapter( browser ).getBrowserFunctions();
    for( BrowserFunction function : functions ) {
      if( function.getName().equals( name ) ) {
        return function;
      }
    }
    return null;
  }

  private static void executeFunction( Browser browser, BrowserFunction function, Object[] arguments )
  {
    try {
      JsonValue result = javaToJson( function.function( arguments ) );
      setExecutedFunctionResult( browser, result );
    } catch( Exception exception ) {
      setExecutedFunctionError( browser, exception.getMessage() );
    }
    setExecutedFunctionName( browser, function.getName() );
  }

  private static void setExecutedFunctionResult( Browser browser, Object result ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( EXECUTED_FUNCTION_RESULT + getId( browser ), result );
  }

  private static void setExecutedFunctionError( Browser browser, String error ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( EXECUTED_FUNCTION_ERROR + getId( browser ), error );
  }

  private static void setExecutedFunctionName( Browser browser, String name ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( EXECUTED_FUNCTION_NAME + getId( browser ), name );
  }

  static Object jsonToJava( JsonValue value ) {
    Object result;
    if( value.isNull() ) {
      result = null;
    } else if( value.isBoolean() ) {
      result = Boolean.valueOf( value.asBoolean() );
    } else if( value.isNumber() ) {
      result = Double.valueOf( value.asDouble() );
    } else if( value.isString() ) {
      result = value.asString();
    } else if( value.isArray() ) {
      result = jsonToJava( value.asArray() );
    } else {
      throw new RuntimeException( "Unable to convert JsonValue to Java: " + value );
    }
    return result;
  }

  private static Object[] jsonToJava( JsonArray value ) {
    Object[] result = new Object[ value.size() ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = jsonToJava( value.get( i ) );
    }
    return result;
  }

  static JsonValue javaToJson( Object value ) {
    if( value == null ) {
      return JsonValue.NULL;
    }
    if( value instanceof String ) {
      return JsonValue.valueOf( ( String )value );
    }
    if( value instanceof Number ) {
      return JsonValue.valueOf( ( ( Number )value ).doubleValue() );
    }
    if( value instanceof Boolean ) {
      return JsonValue.valueOf( ( ( Boolean )value ).booleanValue() );
    }
    if( value instanceof Object[] ) {
      return javaToJson( ( Object[] )value );
    }
    SWT.error( SWT.ERROR_INVALID_RETURN_VALUE );
    return null;
  }

  private static JsonValue javaToJson( Object[] array ) {
    JsonArray jsonArray = new JsonArray();
    for( int i = 0; i < array.length; i++ ) {
      jsonArray.add( javaToJson( array[ i ] ) );
    }
    return jsonArray;
  }

  private static IBrowserAdapter getAdapter( Browser browser ) {
    return browser.getAdapter( IBrowserAdapter.class );
  }

}
