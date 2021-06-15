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
 * The class <code>FileTransfer</code> provides a platform specific mechanism
 * for converting a list of files represented as a java <code>String[]</code> to
 * a platform specific representation of the data and vice versa. Each
 * <code>String</code> in the array contains the absolute path for a single file
 * or directory.
 * <p>
 * An example of a java <code>String[]</code> containing a list of files is
 * shown below:
 * </p>
 * <code><pre>
 *     File file1 = new File("C:\temp\file1");
 *     File file2 = new File("C:\temp\file2");
 *     String[] fileData = new String[2];
 *     fileData[0] = file1.getAbsolutePath();
 *     fileData[1] = file2.getAbsolutePath();
 * </code></pre>
 * 
 * @see Transfer
 * @since 1.3
 */
public class FileTransfer extends ByteArrayTransfer {

  private static final String TYPE_NAME = "CF_HDROP "; //$NON-NLS-1$
  private static final int TYPE_ID = registerType( TYPE_NAME );

  private FileTransfer() {
  }

  /**
   * Returns the singleton instance of the FileTransfer class.
   * 
   * @return the singleton instance of the FileTransfer class
   */
  public static FileTransfer getInstance() {
    return SingletonUtil.getSessionInstance( FileTransfer.class );
  }

  /**
   * This implementation of <code>javaToNative</code> converts a list of file
   * names represented by a java <code>String[]</code> to a platform specific
   * representation. Each <code>String</code> in the array contains the absolute
   * path for a single file or directory.
   * 
   * @param object a java <code>String[]</code> containing the file names to be
   *          converted
   * @param transferData an empty <code>TransferData</code> object that will be
   *          filled in on return with the platform specific format of the data
   * @see Transfer#nativeToJava
   */
  public void javaToNative( Object object, TransferData transferData ) {
    if( !checkFile( object ) || !isSupportedType( transferData ) ) {
      DND.error( DND.ERROR_INVALID_DATA );
    }
    String[] fileNames = ( String[] )object;
    transferData.data = fileNames;
    transferData.result = 1;
  }

  /**
   * This implementation of <code>nativeToJava</code> converts a platform
   * specific representation of a list of file names to a java
   * <code>String[]</code>. Each String in the array contains the absolute path
   * for a single file or directory.
   * 
   * @param transferData the platform specific representation of the data to be
   *          converted
   * @return a java <code>String[]</code> containing a list of file names if the
   *         conversion was successful; otherwise null
   * @see Transfer#javaToNative
   */
  public Object nativeToJava( TransferData transferData ) {
    if( !isSupportedType( transferData ) || transferData.data == null )
      return null;
    if( transferData.result != 1 )
      return null;
    String[] fileNames = ( String[] )transferData.data;
    String[] result = new String[ fileNames.length ];
    System.arraycopy( fileNames, 0, result, 0, fileNames.length );
    return result;
  }

  protected int[] getTypeIds() {
    return new int[]{
      TYPE_ID
    };
  }

  protected String[] getTypeNames() {
    return new String[]{
      TYPE_NAME
    };
  }

  boolean checkFile( Object object ) {
    if(    object == null
        || !( object instanceof String[] )
        || ( ( String[] )object ).length == 0 )
      return false;
    String[] strings = ( String[] )object;
    for( int i = 0; i < strings.length; i++ ) {
      if( strings[ i ] == null || strings[ i ].length() == 0 )
        return false;
    }
    return true;
  }

  protected boolean validate( Object object ) {
    return checkFile( object );
  }
}
