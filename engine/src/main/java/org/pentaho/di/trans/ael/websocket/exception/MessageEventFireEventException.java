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
package org.pentaho.di.trans.ael.websocket.exception;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fcamara on 8/17/17.
 */
public class MessageEventFireEventException extends KettleException {

  private static final long serialVersionUID = 1107793379573661562L;

  private List<Exception> handlerExceptions;

  public MessageEventFireEventException( final String message ) {
    super( message );
  }

  public void addHandlerException( final Exception e ) {
    if ( handlerExceptions == null ) {
      handlerExceptions = new ArrayList<>();
    }
    handlerExceptions.add( e );
  }

  private List<Exception> getHandlerExceptions() {
    if ( handlerExceptions != null ) {
      return handlerExceptions;
    }
    return Collections.emptyList();
  }

  @Override
  public String getMessage() {
    StringBuilder retval = new StringBuilder();
    retval.append( Const.CR ).append( super.getMessage() ).append( Const.CR );

    List<Exception> exceptions = getHandlerExceptions();
    for ( Exception e : exceptions ) {
      String message = e.getMessage();
      if ( message != null ) {
        retval.append( message ).append( Const.CR );
      } else {
        // Add with stack trace elements of cause...
        StackTraceElement[] ste = e.getStackTrace();
        for ( int i = ste.length - 1; i >= 0; i-- ) {
          retval.append( " at " ).append( ste[ i ].getClassName() )
            .append( "." )
            .append( ste[ i ].getMethodName() )
            .append( " (" )
            .append( ste[ i ].getFileName() )
            .append( ":" )
            .append( ste[ i ].getLineNumber() )
            .append( ")" )
            .append( Const.CR );
        }
      }
    }
    return retval.toString();
  }
}
