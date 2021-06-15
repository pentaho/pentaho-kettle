/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.util;

import java.text.MessageFormat;


public final class ParamCheck {

  private static final String NOT_NULL_TEXT = "The parameter ''{0}'' must not be null.";
  private static final String NOT_EMPTY_TEXT = "The parameter ''{0}'' must not be empty.";

  private ParamCheck() {
    // prevent instantiation
  }

  public static void notNull( Object param, String paramName ) {
    if ( param == null ) {
      String msg = MessageFormat.format( NOT_NULL_TEXT, paramName );
      throw new NullPointerException( msg );
    }
  }

  public static void notNullOrEmpty( String param, String paramName ) {
    ParamCheck.notNull( param, paramName );
    if( param.trim().length() == 0 ) {
      String msg = MessageFormat.format( NOT_EMPTY_TEXT, paramName );
      throw new IllegalArgumentException( msg );
    }
  }

}
