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

import org.eclipse.rap.rwt.SingletonUtil;

/**
 * The class <code>RTFTransfer</code> provides a platform specific mechanism for
 * converting text in RTF format represented as a java <code>String</code> to a
 * platform specific representation of the data and vice versa.
 * <p>
 * An example of a java <code>String</code> containing RTF text is shown below:
 * </p>
 * <code><pre>
 *     String rtfData = "{\\rtf1{\\colortbl;\\red255\\green0\\blue0;}\\uc1\\b\\i Hello World}";
 * </code></pre>
 * 
 * @see Transfer
 * @since 1.3
 */
public class RTFTransfer extends ByteArrayTransfer {

  private static final String CF_RTF = "rtf";
  private static final int CF_RTFID = registerType( CF_RTF );

  private RTFTransfer() {
  }

  /**
   * Returns the singleton instance of the RTFTransfer class.
   * 
   * @return the singleton instance of the RTFTransfer class
   */
  public static RTFTransfer getInstance() {
    return SingletonUtil.getSessionInstance( RTFTransfer.class );
  }

  /**
   * This implementation of <code>javaToNative</code> converts RTF-formatted
   * text represented by a java <code>String</code> to a platform specific
   * representation.
   * 
   * @param object a java <code>String</code> containing RTF text
   * @param transferData an empty <code>TransferData</code> object that will be
   *          filled in on return with the platform specific format of the data
   * @see Transfer#nativeToJava
   */
  public void javaToNative( Object object, TransferData transferData ) {
    if( !checkRTF( object ) || !isSupportedType( transferData ) ) {
      DND.error( DND.ERROR_INVALID_DATA );
    }
    transferData.data = object;
    transferData.result = 1;
  }

  /**
   * This implementation of <code>nativeToJava</code> converts a platform
   * specific representation of RTF text to a java <code>String</code>.
   * 
   * @param transferData the platform specific representation of the data to be
   *          converted
   * @return a java <code>String</code> containing RTF text if the conversion
   *         was successful; otherwise null
   * @see Transfer#javaToNative
   */
  public Object nativeToJava( TransferData transferData ) {
    if( !isSupportedType( transferData ) || transferData.data == null )
      return null;
    if( transferData.result != 1 ) 
      return null;
    return transferData.data;
  }

  protected int[] getTypeIds() {
    return new int[]{ CF_RTFID };
  }

  protected String[] getTypeNames() {
    return new String[]{ CF_RTF };
  }

  boolean checkRTF( Object object ) {
    return ( object != null && object instanceof String && ( ( String )object ).length() > 0 );
  }

  protected boolean validate( Object object ) {
    return checkRTF( object );
  }
}
