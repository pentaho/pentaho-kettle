/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.dnd;

import java.io.UnsupportedEncodingException;

import org.eclipse.rap.rwt.SingletonUtil;

/**
 * The class <code>TextTransfer</code> provides a platform specific mechanism
 * for converting plain text represented as a java <code>String</code> to a
 * platform specific representation of the data and vice versa.
 * <p>
 * An example of a java <code>String</code> containing plain text is shown
 * below:
 * </p>
 * <code><pre>
 *     String textData = "Hello World";
 * </code></pre>
 * 
 * <p>Note the <code>TextTransfer</code> does not change the content of the text
 * data. For a better integration with the platform, the application should convert
 * the line delimiters used in the text data to the standard line delimiter used by the
 * platform.
 * </p>
 * 
 * @see Transfer
 * @since 1.3
 */
public class TextTransfer extends ByteArrayTransfer {

  private static final String TYPE_NAME = "text";
  private static final int TYPE_ID = registerType( TYPE_NAME );

  private TextTransfer() {
  }

  /**
   * Returns the singleton instance of the TextTransfer class.
   * 
   * @return the singleton instance of the TextTransfer class
   */
  public static TextTransfer getInstance() {
    return SingletonUtil.getSessionInstance( TextTransfer.class );
  }

  /**
   * This implementation of <code>javaToNative</code> converts plain text
   * represented by a java <code>String</code> to a platform specific
   * representation.
   * 
   * @param object a java <code>String</code> containing text
   * @param transferData an empty <code>TransferData</code> object that will be
   *          filled in on return with the platform specific format of the data
   * @see Transfer#nativeToJava
   */
  public void javaToNative( Object object, TransferData transferData ) {
    if( !checkText( object ) || !isSupportedType( transferData ) ) {
      DND.error( DND.ERROR_INVALID_DATA );
    }
    transferData.data = object;
    transferData.result = 1;
  }

  /**
   * This implementation of <code>nativeToJava</code> converts a platform
   * specific representation of plain text to a java <code>String</code>.
   * 
   * @param transferData the platform specific representation of the data to be
   *          converted
   * @return a java <code>String</code> containing text if the conversion was
   *         successful; otherwise null
   * @see Transfer#javaToNative
   */
  public Object nativeToJava( TransferData transferData ) {
    if( !isSupportedType( transferData ) || transferData.data == null ) {
      return null;
    }
    if( transferData.result != 1 ) 
      return null;
    return transferData.data;
  }

  protected int[] getTypeIds() {
    return new int[]{ TYPE_ID };
  }

  protected String[] getTypeNames() {
    return new String[]{ TYPE_NAME };
  }

  boolean checkText( Object object ) {
    return ( object != null && object instanceof String && ( ( String )object ).length() > 0 );
  }

  protected boolean validate( Object object ) {
    return checkText( object );
  }
  
  static byte[] stringToBytes( String string ) {
    byte[] bytes = null;
    try {
      bytes = string.getBytes( "UTF-8" );
    } catch( UnsupportedEncodingException ignore ) {
    }
    return bytes;
  }
}
