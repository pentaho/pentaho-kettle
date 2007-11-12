/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.clipboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

/**
 * Singleton class that reads Images (CF_DIB) from the Windows Clipboard as
 * ImageData objects.
 *
 * If you found this class useful (or made some improvements)   drop me a line.
 *
 *
 * @author Philip Schatz ( www.philschatz.com )
 */
public class ImageDataTransfer extends ByteArrayTransfer {

    public static final int COM_CF_DIB          = 8;
    
      private static final ImageDataTransfer INSTANCE = new   ImageDataTransfer();
      private static final String CF_DIB = "CF_DIB";
      private static final int CF_DIBID = COM_CF_DIB;

      private ImageDataTransfer() {
              //Singleton
      }

      public static ImageDataTransfer getInstance() {
              return ImageDataTransfer.INSTANCE;
      }

      protected final int[] getTypeIds() {
              return new int[]{ImageDataTransfer.CF_DIBID};
      }

      protected final String[] getTypeNames() {
              return new String[]{ImageDataTransfer.CF_DIB};
      }

      protected Object nativeToJava(TransferData transferData) {
              final Object o = super.nativeToJava(transferData);
              final byte[] bytes = (byte[]) o;

              try {
                      final InputStream bis = new PrependWinBMPHeaderFilterInputStream(
                                      new UncompressDibFilterInputStream(new ByteArrayInputStream(
                                                      bytes)));
                      final ImageData[] data = new ImageLoader().load(bis);
                      if (data.length < 1) {
                              return null;
                      }
                      return data[0];
              } catch (IOException e) {
                      return null;
              }
      }
      protected void javaToNative(Object object, TransferData transferData) {
              final ImageData imgData = (ImageData) object;
              final ImageLoader loader = new ImageLoader();
              final ByteArrayOutputStream bos = new ByteArrayOutputStream();
              final byte[] bytes;

              loader.data = new ImageData[]{imgData};
              loader.save(new RemoveWinBMPHeaderFilterOutputStream(bos), SWT.IMAGE_BMP);
              bytes = bos.toByteArray();
              super.javaToNative(bytes, transferData);
      }
}
