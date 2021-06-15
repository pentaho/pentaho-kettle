/*******************************************************************************
 * Copyright (c) 2000, 20007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.dnd;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.rap.rwt.SingletonUtil;

/**
 * The class <code>URLTransfer</code> provides a platform specific mechanism for
 * converting text in URL format represented as a java <code>String</code> to a
 * platform specific representation of the data and vice versa. The string must
 * contain a fully specified url.
 * <p>
 * An example of a java <code>String</code> containing a URL is shown below:
 * </p>
 * <code><pre>
 *     String url = "http://www.eclipse.org";
 * </code></pre>
 * 
 * @see Transfer
 * @since 1.3
 */
public class URLTransfer extends ByteArrayTransfer {

  static final String TYPE_NAME = "url";
  static final int TYPE_ID = registerType( TYPE_NAME );

  private URLTransfer() {
  }

  /**
   * Returns the singleton instance of the URLTransfer class.
   * 
   * @return the singleton instance of the URLTransfer class
   */
  public static URLTransfer getInstance() {
    return SingletonUtil.getSessionInstance( URLTransfer.class );
  }

  /**
   * This implementation of <code>javaToNative</code> converts a URL represented
   * by a java <code>String</code> to a platform specific representation.
   * 
   * @param object a java <code>String</code> containing a URL
   * @param transferData an empty <code>TransferData</code> object that will be
   *          filled in on return with the platform specific format of the data
   * @see Transfer#nativeToJava
   */
  public void javaToNative( Object object, TransferData transferData ) {
    if( !checkURL( object ) || !isSupportedType( transferData ) ) {
      DND.error( DND.ERROR_INVALID_DATA );
    }
    transferData.result = 1;
    try {
      transferData.data = new URL( ( String )object );
    } catch( MalformedURLException e ) {
      transferData.result = 0;
    }
  }

  /**
   * This implementation of <code>nativeToJava</code> converts a platform
   * specific representation of a URL to a java <code>String</code>.
   * 
   * @param transferData the platform specific representation of the data to be
   *          converted
   * @return a java <code>String</code> containing a URL if the conversion was
   *         successful; otherwise null
   * @see Transfer#javaToNative
   */
  public Object nativeToJava( TransferData transferData ) {
    if( !isSupportedType( transferData ) || transferData.data == null )
      return null;
    if( transferData.result != 1 )
      return null;
    return ( ( URL )transferData.data ).toString();
  }

  protected int[] getTypeIds() {
    return new int[]{ TYPE_ID };
  }

  protected String[] getTypeNames() {
    return new String[]{ TYPE_NAME };
  }

  boolean checkURL( Object object ) {
    return    object != null
           && ( object instanceof String )
           && ( ( String )object ).length() > 0;
  }

  protected boolean validate( Object object ) {
    return checkURL( object );
  }
}
