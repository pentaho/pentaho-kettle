/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


final class Utf8ResourceBundle {

  private final static Map<ResourceBundle,ResourceBundle> bundles = new HashMap<>();

  static ResourceBundle getBundle( String baseName, Locale locale, ClassLoader loader ) {
    ResourceBundle bundle = ResourceBundle.getBundle( baseName, locale, loader );
    ResourceBundle result;
    synchronized( bundles ) {
      result = bundles.get( bundle );
      if( result == null ) {
        result = createUtf8Bundle( bundle );
        bundles.put( bundle, result );
      }
    }
    return result;
  }

  private static ResourceBundle createUtf8Bundle( ResourceBundle bundle ) {
    ResourceBundle result = bundle;
    if( bundle instanceof PropertyResourceBundle ) {
      PropertyResourceBundle prb = ( PropertyResourceBundle )bundle;
      result = new Utf8PropertyResourceBundle( prb );
    }
    return result;
  }

  private static class Utf8PropertyResourceBundle extends ResourceBundle {
    private final PropertyResourceBundle bundle;

    private Utf8PropertyResourceBundle( PropertyResourceBundle bundle ) {
      this.bundle = bundle;
    }

    @Override
    public Enumeration<String> getKeys() {
      return bundle.getKeys();
    }

    @Override
    protected Object handleGetObject( String key ) {
      String result = ( String )bundle.handleGetObject( key );
      try {
        // We do not buffer the encoded result since the RWT.NLS mechanism
        // creates and buffers the completely initialized nls instance. So each
        // entry should only be read once.
        if( result != null ) {
          result = new String( result.getBytes( "ISO-8859-1" ), "UTF-8" );
        }
      } catch( UnsupportedEncodingException uee ) {
        throw new RuntimeException( "should not happen", uee );
      }
      return result;
    }

  }

}
