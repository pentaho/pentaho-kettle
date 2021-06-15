/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.rwt.internal.theme.ThemePropertyAdapterRegistry.ThemePropertyAdapter;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceLoader;


public class CssCursor implements CssValue, ThemeResource {

  private static final String CURSOR_DEST_PATH = "themes/cursors";

  private static final String[] PREDEFINED_CURSORS = {
    "default",
    "wait",
    "crosshair",
    "help",
    "move",
    "text",
    "pointer",
    "e-resize",
    "n-resize",
    "w-resize",
    "s-resize",
    "ne-resize",
    "se-resize",
    "nw-resize",
    "sw-resize",
    "col-resize",
    "row-resize",
    "progress",
    "not-allowed",
    "no-drop"
  };

  public final String value;
  public final ResourceLoader loader;

  private CssCursor( String value, ResourceLoader loader ) {
    this.value = value;
    this.loader = loader;
    if( isCustomCursor() ) {
      try {
        InputStream inputStream = loader.getResourceAsStream( value );
        if( inputStream == null ) {
          throw new IllegalArgumentException( "Failed to read cursor '"
                                              + value
                                              + "'" );
        }
      } catch( IOException e ) {
        throw new IllegalArgumentException( "Failed to read cursor "
                                            + value
                                            + ": "
                                            + e.getMessage() );
      }
    }
  }

  public static CssCursor valueOf( String input, ResourceLoader loader ) {
    if( input == null || loader == null ) {
      throw new NullPointerException( "null argument" );
    }
    if( input.length() == 0 ) {
      throw new IllegalArgumentException( "Empty cursor path" );
    }
    return new CssCursor( input, loader );
  }

  public static CssCursor valueOf( String input ) {
    if( !isPredefinedCursor( input ) ) {
      throw new IllegalArgumentException( "Invalid value for cursor: " + input );
    }
    return new CssCursor( input, null );
  }

  public static boolean isPredefinedCursor( String value ) {
    boolean result = false;
    for( int i = 0; i < PREDEFINED_CURSORS.length && !result; i++ ) {
      if( PREDEFINED_CURSORS[ i ].equalsIgnoreCase( value ) ) {
        result = true;
      }
    }
    return result;
  }

  public boolean isCustomCursor() {
    return !isPredefinedCursor( value );
  }

  @Override
  public String getResourcePath( ApplicationContext applicationContext ) {
    String result = null;
    if( isCustomCursor() ) {
      ThemePropertyAdapterRegistry registry
        = ThemePropertyAdapterRegistry.getInstance( applicationContext );
      ThemePropertyAdapter adapter = registry.getPropertyAdapter( CssCursor.class );
      String cssKey = adapter.getKey( this );
      result = CURSOR_DEST_PATH + "/" + cssKey;
    }
    return result;
  }

  @Override
  public InputStream getResourceAsStream() throws IOException {
    InputStream inputStream = null;
    if( isCustomCursor() ) {
      inputStream = loader.getResourceAsStream( value );
    }
    return inputStream;
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object instanceof CssCursor ) {
      CssCursor other = ( CssCursor )object;
      return    ( value == null ? other.value == null : value.equals( other.value ) )
             && ( loader == null ? other.loader == null : loader.equals( other.loader ) );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toDefaultString() {
    // returns an empty string for custom cursor , because the default resource
    // path is only valid for the bundle that specified it
    return isCustomCursor() ? "" : value;
  }

  @Override
  public String toString() {
    return "CssCursor{ " + value + " }";
  }

}
