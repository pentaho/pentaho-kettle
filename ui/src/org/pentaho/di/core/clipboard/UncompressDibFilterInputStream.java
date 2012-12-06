/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.clipboard;

import java.io.IOException;
import java.io.InputStream;

/**
 * As of Eclipse 3.0 SWT does not recognize Bitmaps with
 * BITMAPINFOHEADER.biCompression set to BI_BITFIELDS
 *
 * So, this class converts a Bitmap with compression set to   BI_BITFIELDS to one
 * with compression set to BI_RGB (none)
 *
 * See Definition of BITMAPINFOHEADER :
 * msdn.microsoft.com/library/en-us/gdi/bitmaps_1rw2.asp
 *
 * If you found this class useful (or made some improvements)   drop me a line.
 *
 *
 * @author Philip Schatz ( www.philschatz.com )
 */
class UncompressDibFilterInputStream extends InputStream {

    public static final int OS_BI_BITFIELDS = 3;
    public static final int OS_BI_RGB = 0;
    
      private final InputStream in;
      private byte[] buffer;
      private int index = 0;
      private final boolean isCompressed;

      //The following are only used when isCompressed==true
      /** bits per pixel */
      private short bitCount = -1;
      private int redMask = -1;
      private int greenMask = -1;
      private int blueMask = -1;

      public UncompressDibFilterInputStream(InputStream bmpFileStream)
                      throws IOException {
              this.in = bmpFileStream;
              this.buffer = new byte[BITMAPINFOHEADER.sizeof];

              //read in the BITMAPINFOHEADER from the stream
              this.in.read(this.buffer, 0, this.buffer.length);
              BITMAPINFOHEADER origInfoHeader = new BITMAPINFOHEADER();
              ConversionUtil.fromBytes(origInfoHeader, this.buffer, 0);

              this.isCompressed = origInfoHeader.biCompression == OS_BI_BITFIELDS;

              if (this.isCompressed) {
                      this.bitCount = origInfoHeader.biBitCount;

                      origInfoHeader.biCompression = OS_BI_RGB;
                      origInfoHeader.biSizeImage = 0;

                      ConversionUtil.toBytes(origInfoHeader, this.buffer, 0);

                      //read the next 12 bytes and just ignore them
                      final byte[] redMaskBytes = new byte[4];
                      final byte[] greenMaskBytes = new byte[4];
                      final byte[] blueMaskBytes = new byte[4];

                      this.in.read(redMaskBytes);
                      this.in.read(greenMaskBytes);
                      this.in.read(blueMaskBytes);

                      this.redMask = ConversionUtil.bytesToInt(redMaskBytes, 0);
                      this.greenMask = ConversionUtil.bytesToInt(greenMaskBytes, 0);
                      this.blueMask = ConversionUtil.bytesToInt(blueMaskBytes, 0);
              }
      }

      public int read() throws IOException {
              //first try and read from the buffer
              if (this.index < this.buffer.length) {
                      final byte b = this.buffer[this.index++];
                      return 0xff & b;
              }

              //do bitmask conversions and throw them in the buffer.
              if (this.isCompressed) {
                      switch (this.bitCount) {
                              case 16 :
                                      //each pixel is a WORD (short) (2 bytes)
                                      final byte[] pixelBytes = new byte[2];
                                      final short pixel;
                                      final int red;
                                      final int green;
                                      final int blue;
                                      //used in calculating the new pixelBytes
                                      final short a;
                                      final short b;
                                      final short c;
                                      final short newPixel;

                                      this.in.read(pixelBytes);
                                      pixel = ConversionUtil.bytesToShort(pixelBytes, 0);
                                      red = deMask(pixel, this.redMask);
                                      green = deMask(pixel, this.greenMask);
                                      blue = deMask(pixel, this.blueMask);

                                      //since green was 6 bits before, shift it down to 5
                                      a = (short) ((0x1f) & blue);
                                      b = (short) ((0x3e0) & ((green >> 1) << 5));
                                      c = (short) ((0x7c00) & (red << 10));
                                      newPixel = (short) (a + b + c);

                                      this.buffer = new byte[2];
                                      ConversionUtil.shortToBytes(newPixel, this.buffer, 0);
                                      this.index = 1;
                                      return 0xff & this.buffer[0];

                              default :
                      }
              }

              return this.in.read();
      }

      /**
       * not too elegant way of reading bitmasked color info.
       * val.. : 10110100 10011011
       * mask. : 00000111 11100000
       * return: 00100100
       */
      private static int deMask(int val, int mask) {
              int a = val & mask;
              while (mask % 2 == 0) {
                      a = a >> 1;
                      mask = mask >> 1;
              }
              return a;
      }
}