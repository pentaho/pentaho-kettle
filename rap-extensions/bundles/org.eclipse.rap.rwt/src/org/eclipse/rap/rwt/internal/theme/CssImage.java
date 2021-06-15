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
package org.eclipse.rap.rwt.internal.theme;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.eclipse.rap.rwt.internal.theme.ThemePropertyAdapterRegistry.ThemePropertyAdapter;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;


public class CssImage implements CssValue, ThemeResource {

  private static final String IMAGE_DEST_PATH = "themes/images";

  private static final String NONE_INPUT = "none";

  public static final CssImage NONE = new CssImage( true, null, null, null, null, true );

  private final Size size;

  public final boolean none;
  public final String path;
  public final ResourceLoader loader;
  public final String[] gradientColors;
  public final float[] gradientPercents;
  public final boolean vertical;


  /**
   * Creates a new image from the given value.
   *
   * @param path the definition string to create the image from. Either
   *            <code>none</code> or a path to an image
   * @param loader a resource loader which is able to load the image from the
   *            given path
   * @param gradientColors an array with gradient colors
   * @param gradientPercents an array with gradient percents
   * @param vertical if true sweeps from top to bottom, else
   *        sweeps from left to right
   */
  private CssImage( boolean none,
                   String path,
                   ResourceLoader loader,
                   String[] gradientColors,
                   float[] gradientPercents,
                   boolean vertical )
  {
    this.none = none;
    this.path = path;
    this.loader = loader;
    this.gradientColors = gradientColors;
    this.gradientPercents = gradientPercents;
    this.vertical = vertical;
    if( none ) {
      size = new Size( 0, 0 );
    } else {
      try {
        size = readImageSize( path, loader );
        if( size == null ) {
          throw new IllegalArgumentException( "Failed to read image: " + path );
        }
      } catch( IOException e ) {
        throw new IllegalArgumentException( "Failed to read image: " + path, e );
      }
    }
  }

  public static CssImage valueOf( String input, ResourceLoader loader ) {
    CssImage result;
    if( NONE_INPUT.equals( input ) ) {
      result = NONE;
    } else {
      ParamCheck.notNull( input, "input" );
      ParamCheck.notNull( loader, "loader" );
      if( input.length() == 0 ) {
        throw new IllegalArgumentException( "Empty image path" );
      }
      result = new CssImage( false, input, loader, null, null, true );
    }
    return result;
  }

  public static CssImage createGradient( String[] gradientColors,
                                        float[] gradientPercents,
                                        boolean vertical )
  {
    CssImage result;
    if( gradientColors == null || gradientPercents == null ) {
      throw new NullPointerException( "null argument" );
    }
    result = new CssImage( true, null, null, gradientColors, gradientPercents, vertical );
    return result;
  }

  public boolean isGradient() {
    return gradientColors != null && gradientPercents != null;
  }

  public Size getSize() {
    return size;
  }

  @Override
  public String getResourcePath( ApplicationContext applicationContext ) {
    String result = null;
    if( !none && path != null ) {
      ThemePropertyAdapterRegistry registry
        = ThemePropertyAdapterRegistry.getInstance( applicationContext );
      ThemePropertyAdapter adapter = registry.getPropertyAdapter( CssImage.class );
      String cssKey = adapter.getKey( this );
      result = IMAGE_DEST_PATH + "/" + cssKey;
    }
    return result;
  }

  @Override
  public InputStream getResourceAsStream() throws IOException {
    InputStream inputStream = null;
    if( !none && path != null ) {
      inputStream = loader.getResourceAsStream( path );
    }
    return inputStream;
  }

  @Override
  public String toDefaultString() {
    // returns an empty string, because the default resource path is only valid
    // for the bundle that specified it
    return none ? NONE_INPUT : "";
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object.getClass() == CssImage.class ) {
      CssImage other = ( CssImage )object;
      return    ( path == null ? other.path == null : path.equals( other.path ) )
             && ( loader == null ? other.loader == null : loader.equals( other.loader ) )
             && (   gradientColors == null
                  ? other.gradientColors == null
                  : Arrays.equals( gradientColors, other.gradientColors ) )
             && (   gradientPercents == null
                  ? other.gradientPercents == null
                  : Arrays.equals( gradientPercents, other.gradientPercents ) )
             && other.vertical == vertical;
    }
    return false;
  }

  @Override
  public int hashCode() {
    CRC32 result = new CRC32();
    if( none ) {
      result.update( 1 );
      if( gradientColors != null && gradientPercents != null ) {
        for( int i = 0; i < gradientColors.length; i++ ) {
          result.update( gradientColors[ i ].getBytes() );
        }
        for( int i = 0; i < gradientPercents.length; i++ ) {
          result.update( Float.floatToIntBits( gradientPercents[ i ] ) );
        }
        if( vertical ) {
          result.update( 2 );
        }
      }
    } else {
      result.update( 3 );
      result.update( path.getBytes() );
    }
    return ( int )result.getValue();
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append( "CssImage{ " );
    if( gradientColors != null && gradientPercents != null ) {
      result.append( "gradient: " );
      result.append( vertical ? "vertical " : "horizontal " );
      for( int i = 0; i < gradientColors.length; i++ ) {
        result.append( ", " );
        result.append( gradientColors[ i ] );
        if( i < gradientPercents.length ) {
          result.append( " " );
          result.append( gradientPercents[ i ] );
        }
      }
    } else {
      result.append( none ? "none" : "path: " + path );
    }
    result.append( " }" );
    return result.toString();
  }

  public static Image createSwtImage( CssImage image ) throws IOException {
    Image result;
    if( image.loader == null ) {
      String message = "Cannot create image without resource loader";
      throw new IllegalArgumentException( message );
    }
    InputStream inputStream = image.loader.getResourceAsStream( image.path );
    try {
      result = getApplicationContext().getImageFactory().findImage( image.path, inputStream );
    } finally {
      inputStream.close();
    }
    return result;
  }

  private static Size readImageSize( String path, ResourceLoader loader ) throws IOException {
    InputStream inputStream = loader.getResourceAsStream( path );
    if( inputStream != null ) {
      try {
        ImageData data = new ImageData( inputStream );
        return new Size( data.width, data.height );
      } finally {
        inputStream.close();
      }
    }
    return null;
  }

}
