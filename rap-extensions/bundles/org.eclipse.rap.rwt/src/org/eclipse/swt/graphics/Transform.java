/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.graphics;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;

/**
 * Instances of this class represent transformation matrices for
 * points expressed as (x, y) pairs of floating point numbers.
 * <p>
 * Application code must explicitly invoke the <code>Transform.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @since 3.1
 */
public class Transform extends Resource {

  private final static float[] IDENTITY_MATRIX = { 1, 0, 0, 1, 0, 0 };

  private float[] elements;

  /**
   * Constructs a new identity Transform.
   * <p>
   * This operation requires the operating system's advanced
   * graphics subsystem which may not be available on some
   * platforms.
   * </p>
   *
   * @param device the device on which to allocate the Transform
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   * </ul>
   *
   * @see #dispose()
   */
  public Transform( Device device ) {
    this( device, IDENTITY_MATRIX );
  }

  /**
   * Constructs a new Transform given an array of elements that represent the matrix that describes
   * the transformation.
   * <p>
   * This operation requires the operating system's advanced graphics subsystem which may not be
   * available on some platforms.
   * </p>
   *
   * @param device the device on which to allocate the Transform
   * @param elements an array of floats that describe the transformation matrix
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device, or the
   *              elements array is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the elements array is too small to hold the matrix
   *              values</li>
   *              </ul>
   * @see #dispose()
   */
  public Transform( Device device, float[] elements ) {
    this( device,
          checkTransform( elements )[ 0 ],
          elements[ 1 ],
          elements[ 2 ],
          elements[ 3 ],
          elements[ 4 ],
          elements[ 5 ] );
  }

  /**
   * Constructs a new Transform given all of the elements that represent the matrix that describes
   * the transformation.
   * <p>
   * This operation requires the operating system's advanced graphics subsystem which may not be
   * available on some platforms.
   * </p>
   *
   * @param device the device on which to allocate the Transform
   * @param m11 the first element of the first row of the matrix
   * @param m12 the second element of the first row of the matrix
   * @param m21 the first element of the second row of the matrix
   * @param m22 the second element of the second row of the matrix
   * @param dx the third element of the first row of the matrix
   * @param dy the third element of the second row of the matrix
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *              </ul>
   * @see #dispose()
   */
  public Transform( Device device,
                    float m11,
                    float m12,
                    float m21,
                    float m22,
                    float dx,
                    float dy )
  {
    super( device );
    elements = new float[] { m11, m12, m21, m22, dx, dy };
  }

  static float[] checkTransform( float[] elements ) {
    if( elements == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( elements.length < 6 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return elements;
  }

  @Override
  void destroy() {
    elements = null;
  }

  /**
   * Fills the parameter with the values of the transformation matrix that the receiver represents,
   * in the order {m11, m12, m21, m22, dx, dy}.
   *
   * @param elements array to hold the matrix values
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the parameter is too small to hold the matrix
   *              values</li>
   *              </ul>
   */
  public void getElements( float[] elements ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    if( elements == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( elements.length < 6 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    System.arraycopy( this.elements, 0, elements, 0, 6 );
  }

  /**
   * Modifies the receiver to represent a new transformation given all of the elements that
   * represent the matrix that describes that transformation.
   *
   * @param m11 the first element of the first row of the matrix
   * @param m12 the second element of the first row of the matrix
   * @param m21 the first element of the second row of the matrix
   * @param m22 the second element of the second row of the matrix
   * @param dx the third element of the first row of the matrix
   * @param dy the third element of the second row of the matrix
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void setElements( float m11, float m12, float m21, float m22, float dx, float dy ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    elements = new float[] { m11, m12, m21, m22, dx, dy };
  }

  /**
   * Modifies the receiver such that the matrix it represents becomes the identity matrix.
   *
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void identity() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    System.arraycopy( IDENTITY_MATRIX, 0, elements, 0, 6 );
  }

  /**
   * Returns <code>true</code> if the Transform represents the identity matrix and false otherwise.
   *
   * @return <code>true</code> if the receiver is an identity Transform, and <code>false</code>
   *         otherwise
   */
  public boolean isIdentity() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return Arrays.equals( elements, IDENTITY_MATRIX );
  }

  /**
   * Modifies the receiver such that the matrix it represents becomes the mathematical inverse of
   * the matrix it previously represented.
   *
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_CANNOT_INVERT_MATRIX - if the matrix is not invertible</li>
   *              </ul>
   */
  public void invert() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    float d = elements[ 0 ] * elements[ 3 ] - elements[ 1 ] * elements[ 2 ];
    elements = new float[] {
      elements[ 3 ] / d,
      -elements[ 1 ] / d,
      -elements[ 2 ] / d,
      elements[ 0 ] / d,
      ( elements[ 2 ] * elements[ 5 ] - elements[ 4 ] * elements[ 3 ] ) / d,
      ( elements[ 4 ] * elements[ 1 ] - elements[ 5 ] * elements[ 0 ] ) / d
    };
  }

  /**
   * Modifies the receiver such that the matrix it represents becomes the the result of multiplying
   * the matrix it previously represented by the argument.
   *
   * @param matrix the matrix to multiply the receiver by
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
   *              </ul>
   */
  public void multiply( Transform matrix ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    if( matrix == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( matrix.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    float[] elements = new float[ 6 ];
    matrix.getElements( elements );
    multiply( elements[ 0 ],
              elements[ 1 ],
              elements[ 2 ],
              elements[ 3 ],
              elements[ 4 ],
              elements[ 5 ] );
  }

  /**
   * Modifies the receiver so that it represents a transformation that is equivalent to its previous
   * transformation translated by (offsetX, offsetY).
   *
   * @param offsetX the distance to translate in the X direction
   * @param offsetY the distance to translate in the Y direction
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void translate( float offsetX, float offsetY ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    multiply( 1, 0, 0, 1, offsetX, offsetY );
  }

  /**
   * Modifies the receiver so that it represents a transformation that is equivalent to its previous
   * transformation scaled by (scaleX, scaleY).
   *
   * @param scaleX the amount to scale in the X direction
   * @param scaleY the amount to scale in the Y direction
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void scale( float scaleX, float scaleY ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    multiply( scaleX, 0, 0, scaleY, 0, 0 );
  }

  /**
   * Modifies the receiver so that it represents a transformation that is equivalent to its previous
   * transformation rotated by the specified angle. The angle is specified in degrees and for the
   * identity transform 0 degrees is at the 3 o'clock position. A positive value indicates a
   * clockwise rotation while a negative value indicates a counter-clockwise rotation.
   *
   * @param angle the angle to rotate the transformation by
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void rotate( float angle ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    double radians = Math.toRadians( angle );
    float m11 = ( float )Math.cos( radians );
    float m12 = ( float )Math.sin( radians );
    float m21 = -( float )Math.sin( radians );
    float m22 = ( float )Math.cos( radians );
    multiply( m11, m12, m21, m22, 0, 0 );
  }

  /**
   * Modifies the receiver so that it represents a transformation that is equivalent to its previous
   * transformation sheared by (shearX, shearY).
   *
   * @param shearX the shear factor in the X direction
   * @param shearY the shear factor in the Y direction
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void shear( float shearX, float shearY ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    multiply( 1, shearY, shearX, 1, 0, 0 );
  }

  /**
   * Given an array containing points described by alternating x and y values, modify that array
   * such that each point has been replaced with the result of applying the transformation
   * represented by the receiver to that point.
   *
   * @param pointArray an array of alternating x and y values to be transformed
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the point array is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   *              </ul>
   */
  public void transform( float[] pointArray ) {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    if( pointArray == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    for( int i = 1; i < pointArray.length; i += 2 ) {
      float dx = pointArray[ i - 1 ];
      float dy = pointArray[ i ];
      pointArray[ i - 1 ] = elements[ 0 ] * dx + elements[ 2 ] * dy + elements[ 4 ];
      pointArray[ i ] = elements[ 1 ] * dx + elements[ 3 ] * dy + elements[ 5 ];
    }
  }

  /**
   * Returns a string containing a concise, human-readable description of the receiver.
   *
   * @return a string representation of the receiver
   */
  @Override
  public String toString() {
    if( isDisposed() ) {
      return "Transform {*DISPOSED*}";
    }
    return "Transform {"
           + elements[ 0 ]
           + ","
           + elements[ 1 ]
           + ","
           + elements[ 2 ]
           + ","
           + elements[ 3 ]
           + ","
           + elements[ 4 ]
           + ","
           + elements[ 5 ]
           + "}";
  }

  private void multiply( float m11, float m12, float m21, float m22, float dx, float dy ) {
    elements = new float[] {
      elements[ 0 ] * m11 + elements[ 2 ] * m12,
      elements[ 1 ] * m11 + elements[ 3 ] * m12,
      elements[ 0 ] * m21 + elements[ 2 ] * m22,
      elements[ 1 ] * m21 + elements[ 3 ] * m22,
      elements[ 0 ] * dx + elements[ 2 ] * dy + elements[ 4 ],
      elements[ 1 ] * dx + elements[ 3 ] * dy + elements[ 5 ]
    };
  }

}
