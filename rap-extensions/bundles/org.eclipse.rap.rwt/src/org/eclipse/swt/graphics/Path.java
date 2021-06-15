/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;


/**
 * Instances of this class represent paths through the two-dimensional
 * coordinate system. Paths do not have to be continuous, and can be
 * described using lines, rectangles, arcs, cubic or quadratic bezier curves,
 * glyphs, or other paths.
 * <p>
 * Application code must explicitly invoke the <code>Path.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @since 2.1
 */
public class Path extends Resource {

  private PointF currentPoint;
  private PointF startPoint;
  private byte[] types;
  private float[] points;

  /**
   * Constructs a new empty Path.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param device the device on which to allocate the path
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle for the path could not be obtained</li>
   * </ul>
   *
   * @see #dispose()
   */
  public Path( Device device ) {
    super( device );
    currentPoint = new PointF();
    startPoint = new PointF();
    types = new byte[ 0 ];
    points = new float[ 0 ];
    moveTo( 0, 0 );
  }

  /**
   * Constructs a new Path with the specified PathData.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param device the device on which to allocate the path
   * @param data the data for the path
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the data is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES if a handle for the path could not be obtained</li>
   * </ul>
   *
   * @see #dispose()
   */
  public Path( Device device, PathData data ) {
    this( device );
    if( data == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    appendData( data );
  }

  /**
   * Adds to the receiver the path described by the parameter.
   *
   * @param path the path to add to the receiver
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void addPath( Path path ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( path.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    appendData( path.getPathData() );
    if( currentPoint.x != path.currentPoint.x || currentPoint.y != path.currentPoint.y ) {
      moveTo( path.currentPoint.x, path.currentPoint.y );
    }
  }

  /**
   * Adds to the receiver the rectangle specified by x, y, width and height.
   *
   * @param x the x coordinate of the rectangle to add
   * @param y the y coordinate of the rectangle to add
   * @param width the width of the rectangle to add
   * @param height the height of the rectangle to add
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void addRectangle( float x, float y, float width, float height ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    moveTo( x, y );
    lineTo( x + width, y );
    lineTo( x + width, y + height );
    lineTo( x, y + height );
    close();
    currentPoint.x = x;
    currentPoint.y = y;
  }

  /**
   * Sets the current point of the receiver to the point
   * specified by (x, y). Note that this starts a new
   * sub path.
   *
   * @param x the x coordinate of the new end point
   * @param y the y coordinate of the new end point
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void moveTo( float x, float y ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    addPoint( SWT.PATH_MOVE_TO, x, y );
    currentPoint.x = x;
    currentPoint.y = y;
    startPoint.x = x;
    startPoint.y = y;
  }

  /**
   * Adds to the receiver a line from the current point to
   * the point specified by (x, y).
   *
   * @param x the x coordinate of the end of the line to add
   * @param y the y coordinate of the end of the line to add
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void lineTo( float x, float y ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    addPoint( SWT.PATH_LINE_TO, x, y );
    currentPoint.x = x;
    currentPoint.y = y;
  }

  /**
   * Adds to the receiver a quadratic curve based on the parameters.
   *
   * @param cx the x coordinate of the control point of the spline
   * @param cy the y coordinate of the control point of the spline
   * @param x the x coordinate of the end point of the spline
   * @param y the y coordinate of the end point of the spline
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void quadTo( float cx, float cy, float x, float y ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    addPoint( SWT.PATH_QUAD_TO, cx, cy, x, y );
    currentPoint.x = x;
    currentPoint.y = y;
  }

  /**
   * Adds to the receiver a cubic bezier curve based on the parameters.
   *
   * @param cx1 the x coordinate of the first control point of the spline
   * @param cy1 the y coordinate of the first control of the spline
   * @param cx2 the x coordinate of the second control of the spline
   * @param cy2 the y coordinate of the second control of the spline
   * @param x the x coordinate of the end point of the spline
   * @param y the y coordinate of the end point of the spline
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void cubicTo( float cx1, float cy1, float cx2, float cy2, float x, float y ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    addPoint( SWT.PATH_CUBIC_TO, cx1, cy1, cx2, cy2, x, y );
    currentPoint.x = x;
    currentPoint.y = y;
  }

  /**
   * Closes the current sub path by adding to the receiver a line
   * from the current point of the path back to the starting point
   * of the sub path.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void close() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    addPoint( SWT.PATH_CLOSE );
    currentPoint.x = startPoint.x;
    currentPoint.y = startPoint.y;
  }

  /**
   * Returns a device independent representation of the receiver.
   *
   * @return the PathData for the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see PathData
   */
  public PathData getPathData() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    int typesLength = types.length;
    int pointsLength = points.length;
    if( getLastPointType() == SWT.PATH_MOVE_TO ) {
      typesLength--;
      pointsLength -= 2;
    }
    PathData result = new PathData();
    result.types = new byte[ typesLength ];
    result.points = new float[ pointsLength ];
    System.arraycopy( types, 0, result.types, 0, typesLength );
    System.arraycopy( points, 0, result.points, 0, pointsLength );
    return result;
  }

  /**
   * Replaces the first two elements in the parameter with values that
   * describe the current point of the path.
   *
   * @param point the array to hold the result
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the parameter is too small to hold the end point</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public void getCurrentPoint( float[] point ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    if( point == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( point.length < 2 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    point[ 0 ] = currentPoint.x;
    point[ 1 ] = currentPoint.y;
  }

  @Override
  void destroy() {
    types = null;
    points = null;
  }

  private void appendData( PathData data ) {
    byte[] types = data.types;
    float[] points = data.points;
    for( int i = 0, j = 0; i < types.length; i++ ) {
      switch( types[ i ] ) {
        case SWT.PATH_MOVE_TO:
          moveTo( points[ j++ ], points[ j++ ] );
        break;
        case SWT.PATH_LINE_TO:
          lineTo( points[ j++ ], points[ j++ ] );
        break;
        case SWT.PATH_CUBIC_TO:
          cubicTo( points[ j++ ],
                   points[ j++ ],
                   points[ j++ ],
                   points[ j++ ],
                   points[ j++ ],
                   points[ j++ ] );
        break;
        case SWT.PATH_QUAD_TO:
          quadTo( points[ j++ ], points[ j++ ], points[ j++ ], points[ j++ ] );
        break;
        case SWT.PATH_CLOSE:
          close();
        break;
        default:
          dispose();
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
    }
  }

  private void addPoint( int type, float... coords ) {
    if( type != SWT.PATH_MOVE_TO || getLastPointType() != SWT.PATH_MOVE_TO ) {
      enlargeArrays( 1, coords.length );
    }
    types[ types.length - 1 ] = ( byte )type;
    for( int i = coords.length; i > 0; i-- ) {
      points[ points.length - i ] = coords[ coords.length - i ];
    }
  }

  private int getLastPointType() {
    return types.length > 0 ? types[ types.length - 1 ] : SWT.NONE;
  }

  private void enlargeArrays( int typesAmount, int pointsAmount ) {
    if( typesAmount > 0 ) {
      byte[] newTypes = new byte[ types.length + typesAmount ];
      System.arraycopy( types, 0, newTypes, 0, types.length );
      types = newTypes;
    }
    if( pointsAmount > 0 ) {
      float[] newPoints = new float[ points.length + pointsAmount ];
      System.arraycopy( points, 0, newPoints, 0, points.length );
      points = newPoints;
    }
  }

  private final class PointF {

    public float x;
    public float y;

  }

}
