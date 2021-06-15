/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.dnd;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;

/**
 * The class <code>ImageTransfer</code> provides a platform specific mechanism
 * for converting an Image represented as a java <code>ImageData</code> to a
 * platform specific representation of the data and vice versa.
 * <p>
 * An example of a java <code>ImageData</code> is shown below:
 * </p>
 * <code><pre>
 *     Image image = new Image(display, "C:\temp\img1.gif");
 * 	   ImageData imgData = image.getImageData();
 * </code></pre>
 * 
 * @see Transfer
 * @since 1.3
 */
public class ImageTransfer extends ByteArrayTransfer {

  private static final String TYPE_NAME = "image";
  private static final int TYPE_ID = registerType( TYPE_NAME );

  private ImageTransfer() {
  }

  /**
   * Returns the singleton instance of the ImageTransfer class.
   * 
   * @return the singleton instance of the ImageTransfer class
   */
  public static ImageTransfer getInstance() {
    return SingletonUtil.getSessionInstance( ImageTransfer.class );
  }

  /**
   * This implementation of <code>javaToNative</code> converts an ImageData
   * object represented by java <code>ImageData</code> to a platform specific
   * representation.
   * 
   * @param object a java <code>ImageData</code> containing the ImageData to be
   *          converted
   * @param transferData an empty <code>TransferData</code> object that will be
   *          filled in on return with the platform specific format of the data
   * @see Transfer#nativeToJava
   */
  public void javaToNative( Object object, TransferData transferData ) {
    if( !checkImage( object ) || !isSupportedType( transferData ) ) {
      DND.error( DND.ERROR_INVALID_DATA );
    }
    ImageData imageData = ( ImageData )object;
    if( imageData == null )
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    transferData.data = imageData;
    transferData.result = 1;
  }

  /**
   * This implementation of <code>nativeToJava</code> converts a platform
   * specific representation of an image to java <code>ImageData</code>.
   * 
   * @param transferData the platform specific representation of the data to be
   *          converted
   * @return a java <code>ImageData</code> of the image if the conversion was
   *         successful; otherwise null
   * @see Transfer#javaToNative
   */
  public Object nativeToJava( TransferData transferData ) {
    if( !isSupportedType( transferData ) || transferData.data == null )
      return null;
    ImageData imageData = ( ImageData )transferData.data;
    Object result = imageData.clone();
    return result;
  }

  protected int[] getTypeIds() {
    return new int[]{ TYPE_ID };
  }

  protected String[] getTypeNames() {
    return new String[]{ TYPE_NAME };
  }

  boolean checkImage( Object object ) {
    if( object == null || !( object instanceof ImageData ) )
      return false;
    return true;
  }

  protected boolean validate( Object object ) {
    return checkImage( object );
  }
}
