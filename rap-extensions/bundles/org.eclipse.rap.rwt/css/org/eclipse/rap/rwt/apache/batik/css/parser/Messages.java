/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.apache.batik.css.parser;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

  private static final String BUNDLE_NAME = Messages.class.getName();

  private static final ResourceBundle RESOURCE_BUNDLE
    = ResourceBundle.getBundle( BUNDLE_NAME );

  private Messages() {
  }

  public static String getString( String key ) {
    try {
      return RESOURCE_BUNDLE.getString( key );
    } catch( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }
}
