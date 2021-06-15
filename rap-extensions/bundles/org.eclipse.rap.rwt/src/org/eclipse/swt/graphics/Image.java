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
 *    Rüdiger Herrmann - bug 334511
 ******************************************************************************/
package org.eclipse.swt.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.graphics.InternalImage;
import org.eclipse.swt.internal.graphics.InternalImageFactory;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Display;


/**
 * Instances of this class are graphics which have been prepared
 * for display on a specific device. That is, they are to display
 * on widgets with, for example, <code>Button.setImage()</code>.
 *
 * <p>If loaded from a file format that supports it, an
 * <code>Image</code> may have transparency, meaning that certain
 * pixels are specified as being transparent when drawn. Examples
 * of file formats that support transparency are GIF and PNG.</p>
 */
public class Image extends Resource implements Drawable {

  /**
   * The internal resource.
   * (Warning: This field is platform dependent)
   * <p>
   * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
   * public API. It is marked public only so that it can be shared
   * within the packages provided by SWT. It is not available on all
   * platforms and should never be accessed from application code.
   * </p>
   *
   * @noreference This field is not intended to be referenced by clients.
   */
  public final InternalImage internalImage;

  /* This constructor is called by ImageFactory#createImageInstance() */
  private Image( Device device, InternalImage internalImage ) {
    super( device );
    this.internalImage = internalImage;
  }

  /**
   * Constructs an instance of this class by loading its representation
   * from the specified input stream. Throws an error if an error
   * occurs while loading the image, or if the result is an image
   * of an unsupported type.  Application code is still responsible
   * for closing the input stream.
   * <p>
   * This constructor is provided for convenience when loading a single
   * image only. If the stream contains multiple images, only the first
   * one will be loaded. To load multiple images, use
   * <code>ImageLoader.load()</code>.
   * </p><p>
   * This constructor may be used to load a resource as follows:
   * </p>
   * <pre>
   *     static Image loadImage (Display display, Class clazz, String string) {
   *          InputStream stream = clazz.getResourceAsStream (string);
   *          if (stream == null) return null;
   *          Image image = null;
   *          try {
   *               image = new Image (display, stream);
   *          } catch (SWTException ex) {
   *          } finally {
   *               try {
   *                    stream.close ();
   *               } catch (IOException ex) {}
   *          }
   *          return image;
   *     }
   * </pre>
   *
   * @param device the device on which to create the image
   * @param stream the input stream to load the image from
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the stream is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_IO - if an IO error occurs while reading from the stream</li>
   *    <li>ERROR_INVALID_IMAGE - if the image stream contains invalid data </li>
   *    <li>ERROR_UNSUPPORTED_DEPTH - if the image stream describes an image with an unsupported depth</li>
   *    <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an unrecognized format</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
   * </ul>
   * @since 1.3
   */
  public Image( Device device, InputStream stream ) {
    super( checkDevice( device ) );
    if( stream == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    internalImage = findInternalImage( stream );
  }

  /**
   * Constructs an instance of this class by loading its representation
   * from the file with the specified name. Throws an error if an error
   * occurs while loading the image, or if the result is an image
   * of an unsupported type.
   * <p>
   * This constructor is provided for convenience when loading
   * a single image only. If the specified file contains
   * multiple images, only the first one will be used.
   *
   * @param device the device on which to create the image
   * @param fileName the name of the file to load the image from
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the file name is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_IO - if an IO error occurs while reading from the file</li>
   *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
   *    <li>ERROR_UNSUPPORTED_DEPTH - if the image file describes an image with an unsupported depth</li>
   *    <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an unrecognized format</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
   * </ul>
   * @since 1.3
   */
  public Image( Device device, String fileName ) {
    super( checkDevice( device ) );
    if( fileName == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    internalImage = findInternalImage( fileName );
  }

  /**
   * Constructs a new instance of this class based on the
   * provided image, with an appearance that varies depending
   * on the value of the flag. The possible flag values are:
   * <dl>
   * <dt><b>{@link SWT#IMAGE_COPY}</b></dt>
   * <dd>the result is an identical copy of srcImage</dd>
   * <dt><b>{@link SWT#IMAGE_DISABLE}</b></dt>
   * <dd>the result is a copy of srcImage which has a <em>disabled</em> look</dd>
   * <dt><b>{@link SWT#IMAGE_GRAY}</b></dt>
   * <dd>the result is a copy of srcImage which has a <em>gray scale</em> look</dd>
   * </dl>
   *
   * @param device the device on which to create the image
   * @param srcImage the image to use as the source
   * @param flag the style, either <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code>
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if srcImage is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the flag is not one of <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code></li>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon, or is otherwise in an invalid state</li>
   *    <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the image is not supported</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
   * </ul>
   * @since 1.3
   */
  public Image( Device device, Image srcImage, int flag ) {
    super( checkDevice( device ) );
    if( srcImage == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( srcImage.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    Rectangle rect = srcImage.getBounds();
    switch( flag ) {
      case SWT.IMAGE_COPY:
        internalImage = srcImage.internalImage;
        break;
      case SWT.IMAGE_DISABLE: {
        ImageData data = srcImage.getImageData();
        PaletteData palette = data.palette;
        RGB[] rgbs = new RGB[ 3 ];
        rgbs[ 0 ] = device.getSystemColor( SWT.COLOR_BLACK ).getRGB();
        rgbs[ 1 ] = device.getSystemColor( SWT.COLOR_WIDGET_NORMAL_SHADOW ).getRGB();
        rgbs[ 2 ] = device.getSystemColor( SWT.COLOR_WIDGET_BACKGROUND ).getRGB();
        ImageData newData = new ImageData( rect.width, rect.height, 8, new PaletteData( rgbs ) );
        newData.alpha = data.alpha;
        newData.alphaData = data.alphaData;
        newData.maskData = data.maskData;
        newData.maskPad = data.maskPad;
        if( data.transparentPixel != -1 ) {
          newData.transparentPixel = 0;
        }
        /* Convert the pixels. */
        int[] scanline = new int[ rect.width ];
        int[] maskScanline = null;
        ImageData mask = null;
        if( data.maskData != null ) {
          mask = data.getTransparencyMask();
        }
        if( mask != null ) {
          maskScanline = new int[ rect.width ];
        }
        int redMask = palette.redMask;
        int greenMask = palette.greenMask;
        int blueMask = palette.blueMask;
        int redShift = palette.redShift;
        int greenShift = palette.greenShift;
        int blueShift = palette.blueShift;
        for( int y = 0; y < rect.height; y++ ) {
          int offset = y * newData.bytesPerLine;
          data.getPixels( 0, y, rect.width, scanline, 0 );
          if( mask != null ) {
            mask.getPixels( 0, y, rect.width, maskScanline, 0 );
          }
          for( int x = 0; x < rect.width; x++ ) {
            int pixel = scanline[ x ];
            if( !(    ( data.transparentPixel != -1 && pixel == data.transparentPixel )
              || ( mask != null && maskScanline[ x ] == 0 ) ) )
            {
              int red, green, blue;
              if( palette.isDirect ) {
                red = pixel & redMask;
                red = ( redShift < 0 ) ? red >>> -redShift : red << redShift;
                green = pixel & greenMask;
                green = ( greenShift < 0 ) ? green >>> -greenShift : green << greenShift;
                blue = pixel & blueMask;
                blue = ( blueShift < 0 ) ? blue >>> -blueShift : blue << blueShift;
              } else {
                red = palette.colors[ pixel ].red;
                green = palette.colors[ pixel ].green;
                blue = palette.colors[ pixel ].blue;
              }
              int intensity = red * red + green * green + blue * blue;
              if( intensity < 98304 ) {
                newData.data[ offset ] = ( byte )1;
              } else {
                newData.data[ offset ] = ( byte )2;
              }
            }
            offset++ ;
          }
        }
        internalImage = findInternalImage( newData );
        break;
      }
      case SWT.IMAGE_GRAY: {
        ImageData data = srcImage.getImageData();
        PaletteData palette = data.palette;
        ImageData newData = data;
        if( !palette.isDirect ) {
          /* Convert the palette entries to gray. */
          RGB[] rgbs = palette.getRGBs();
          for( int i = 0; i < rgbs.length; i++ ) {
            if( data.transparentPixel != i ) {
              RGB color = rgbs[ i ];
              int red = color.red;
              int green = color.green;
              int blue = color.blue;
              int intensity = ( red + red + green + green + green + green + green + blue ) >> 3;
              color.red = color.green = color.blue = intensity;
            }
          }
          newData.palette = new PaletteData( rgbs );
        } else {
          /* Create a 8 bit depth image data with a gray palette. */
          RGB[] rgbs = new RGB[ 256 ];
          for( int i = 0; i < rgbs.length; i++ ) {
            rgbs[ i ] = new RGB( i, i, i );
          }
          newData = new ImageData( rect.width, rect.height, 8, new PaletteData( rgbs ) );
          newData.alpha = data.alpha;
          newData.alphaData = data.alphaData;
          newData.maskData = data.maskData;
          newData.maskPad = data.maskPad;
          if( data.transparentPixel != -1 ) {
            newData.transparentPixel = 254;
          }
          /* Convert the pixels. */
          int[] scanline = new int[ rect.width ];
          int redMask = palette.redMask;
          int greenMask = palette.greenMask;
          int blueMask = palette.blueMask;
          int redShift = palette.redShift;
          int greenShift = palette.greenShift;
          int blueShift = palette.blueShift;
          for( int y = 0; y < rect.height; y++ ) {
            int offset = y * newData.bytesPerLine;
            data.getPixels( 0, y, rect.width, scanline, 0 );
            for( int x = 0; x < rect.width; x++ ) {
              int pixel = scanline[ x ];
              if( pixel != data.transparentPixel ) {
                int red = pixel & redMask;
                red = ( redShift < 0 ) ? red >>> -redShift : red << redShift;
                int green = pixel & greenMask;
                green = ( greenShift < 0 ) ? green >>> -greenShift : green << greenShift;
                int blue = pixel & blueMask;
                blue = ( blueShift < 0 ) ? blue >>> -blueShift : blue << blueShift;
                int intensity = ( red + red + green + green + green + green + green + blue ) >> 3;
                if( newData.transparentPixel == intensity ) {
                  intensity = 255;
                }
                newData.data[ offset ] = ( byte )intensity;
              } else {
                newData.data[ offset ] = ( byte )254;
              }
              offset++ ;
            }
          }
        }
        internalImage = findInternalImage( newData );
        break;
      }
      default:
        internalImage = null;
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        break;
    }
  }

  /**
   * Constructs an instance of this class from the given
   * <code>ImageData</code>.
   *
   * @param device the device on which to create the image
   * @param imageData the image data to create the image from (must not be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the image data is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the ImageData is not supported</li>
   * </ul>
   * </ul>
   * @since 1.3
   */
  public Image( Device device, ImageData imageData ) {
    super( checkDevice( device ) );
    if( imageData == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    internalImage = findInternalImage( imageData );
  }

  /**
   * Constructs an empty instance of this class with the
   * specified width and height. The result may be drawn upon
   * by creating a GC and using any of its drawing operations,
   * as shown in the following example:
   * <pre>
   *    Image i = new Image(device, width, height);
   *    GC gc = new GC(i);
   *    gc.drawRectangle(0, 0, 50, 50);
   *    gc.dispose();
   * </pre>
   * <p>
   * Note: Some platforms may have a limitation on the size
   * of image that can be created (size depends on width, height,
   * and depth). For example, Windows 95, 98, and ME do not allow
   * images larger than 16M.
   * </p>
   *
   * @param device the device on which to create the image
   * @param width the width of the new image
   * @param height the height of the new image
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_INVALID_ARGUMENT - if either the width or height is negative or zero</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
   * </ul>
   * @since 1.4
   */
  public Image( Device device, int width, int height ) {
    super( checkDevice( device ) );
    if( width <= 0 || height <= 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    Color white = device.getSystemColor( SWT.COLOR_WHITE );
    PaletteData palette = new PaletteData( new RGB[] { white.getRGB() } );
    ImageData imageData = new ImageData( width, height, 8, palette );
    internalImage = findInternalImage( imageData );
  }

  /**
   * Returns the bounds of the receiver. The rectangle will always
   * have x and y values of 0, and the width and height of the
   * image.
   *
   * @return a rectangle specifying the image's bounds
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
   * </ul>
   */
  public Rectangle getBounds() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return internalImage.getBounds();
  }

  /**
   * Returns an <code>ImageData</code> based on the receiver
   * Modifications made to this <code>ImageData</code> will not
   * affect the Image.
   *
   * @return an <code>ImageData</code> containing the image's data and attributes
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
   * </ul>
   *
   * @see ImageData
   * @since 1.3
   */
  public ImageData getImageData() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    ImageData result;
    if( device != null ) {
      ApplicationContextImpl applicationContext = getApplicationContext();
      result = applicationContext.getImageDataFactory().findImageData( internalImage );
    } else {
      result = internalImage.getImageData();
    }
    return result;
  }

  /**
   * Sets the color to which to map the transparent pixel.
   * <p>
   * There are certain uses of <code>Images</code> that do not support
   * transparency (for example, setting an image into a button or label).
   * In these cases, it may be desired to simulate transparency by using
   * the background color of the widget to paint the transparent pixels
   * of the image. This method specifies the color that will be used in
   * these cases. For example:
   * <pre>
   *    Button b = new Button();
   *    image.setBackground(b.getBackground());
   *    b.setImage(image);
   * </pre>
   * </p><p>
   * The image may be modified by this operation (in effect, the
   * transparent regions may be filled with the supplied color).  Hence
   * this operation is not reversible and it is not legal to call
   * this function twice or with a null argument.
   * </p><p>
   * This method has no effect if the receiver does not have a transparent
   * pixel value.
   * </p>
   *
   * @param color the color to use when a transparent pixel is specified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 1.3
   */
  public void setBackground( Color color ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    if( color == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( color.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    // do nothing
  }

  /**
   * Returns the color to which to map the transparent pixel, or null if
   * the receiver has no transparent pixel.
   * <p>
   * There are certain uses of Images that do not support transparency
   * (for example, setting an image into a button or label). In these cases,
   * it may be desired to simulate transparency by using the background
   * color of the widget to paint the transparent pixels of the image.
   * Use this method to check which color will be used in these cases
   * in place of transparency. This value may be set with setBackground().
   * <p>
   *
   * @return the background color of the image, or null if there is no
   *   transparency in the image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 1.3
   */
  public Color getBackground() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    // do nothing
    return null;
  }

  private void writeObject( ObjectOutputStream stream ) throws IOException {
    if( device == null ) {
      throw new NotSerializableException( getClass().getName() );
    }
    new ImageSerializer( this ).writeObject( stream );
  }

  private void readObject( ObjectInputStream stream ) throws IOException, ClassNotFoundException {
    new ImageSerializer( this ).readObject( stream );
  }

  private ApplicationContextImpl getApplicationContext() {
    Display display = ( Display )device;
    IDisplayAdapter adapter = display.getAdapter( IDisplayAdapter.class );
    return (ApplicationContextImpl)adapter.getUISession().getApplicationContext();
  }

  private static InternalImage findInternalImage( ImageData imageData ) {
    return getInternalImageFactory().findInternalImage( imageData );
  }

  private static InternalImage findInternalImage( InputStream stream ) {
    return getInternalImageFactory().findInternalImage( stream );
  }

  private static InternalImage findInternalImage( String fileName ) {
    return getInternalImageFactory().findInternalImage( fileName );
  }

  private static InternalImageFactory getInternalImageFactory() {
    return ContextProvider.getApplicationContext().getInternalImageFactory();
  }

}