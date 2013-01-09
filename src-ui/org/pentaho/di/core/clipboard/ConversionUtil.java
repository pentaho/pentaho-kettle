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

/**
 * Converts between byte[] and simple types (and BITMAPINFOHEADER)
 *
 * If you found this class useful (or made some improvements) drop me a line.
 *
 * @author Philip Schatz ( www.philschatz.com )
 */
class ConversionUtil {
        private ConversionUtil() {
                //non-instantiable
        }

        public static int bytesToInt(byte[] bytes, int start) {
                int a = 0xff & bytes[start + 0];
                int b = 0xff00 & (bytes[start + 1] << 8);
                int c = 0xff0000 & (bytes[start + 2] << 16);
                int d = 0xff000000 & (bytes[start + 3] << 24);
                return a + b + c + d;
        }

        public static short bytesToShort(byte[] bytes, int start) {
                short a = (short) (0xff & bytes[start + 0]);
                short b = (short) (0xff00 & (bytes[start + 1] << 8));
                return (short) (a + b);
        }

        public static void intToBytes(int val, byte[] bytes, int start) {
                bytes[start + 0] = (byte) val;
                bytes[start + 1] = (byte) (0xff & (val >> 8));
                bytes[start + 2] = (byte) (0xff & (val >> 16));
                bytes[start + 3] = (byte) (0xff & (val >> 24));
        }

        public static void shortToBytes(short val, byte[] bytes, int start) {
                bytes[start + 0] = (byte) val;
                bytes[start + 1] = (byte) (0xff & (val >> 8));
        }

        public static void fromBytes(BITMAPINFOHEADER dest, byte[] src, int start) {
                dest.biSize = bytesToInt(src, start + 0);
                dest.biWidth = bytesToInt(src, start + 4);
                dest.biHeight = bytesToInt(src, start + 8);
                dest.biPlanes = bytesToShort(src, start + 12);
                dest.biBitCount = bytesToShort(src, start + 14);
                dest.biCompression = bytesToInt(src, start + 16);
                dest.biSizeImage = bytesToInt(src, start + 20);
                dest.biXPelsPerMeter = bytesToInt(src, start + 24);
                dest.biYPelsPerMeter = bytesToInt(src, start + 28);
                dest.biClrUsed = bytesToInt(src, start + 32);
                dest.biClrImportant = bytesToInt(src, start + 36);
                if (dest.biPlanes != 1)
                        throw new IllegalArgumentException("incorrect bitmap info format.");
                if (dest.biSize != BITMAPINFOHEADER.sizeof)
                        throw new IllegalArgumentException(
                                        "incorrect size. cannot do v4 or v5 bitmaps yet");
        }

        public static void toBytes(BITMAPINFOHEADER src, byte[] dest, int start) {
                intToBytes(src.biSize, dest, start);
                intToBytes(src.biWidth, dest, start + 4);
                intToBytes(src.biHeight, dest, start + 8);
                shortToBytes(src.biPlanes, dest, start + 12);
                shortToBytes(src.biBitCount, dest, start + 14);
                intToBytes(src.biCompression, dest, start + 16);
                intToBytes(src.biSizeImage, dest, start + 20);
                intToBytes(src.biXPelsPerMeter, dest, start + 24);
                intToBytes(src.biYPelsPerMeter, dest, start + 28);
                intToBytes(src.biClrUsed, dest, start + 32);
                intToBytes(src.biClrImportant, dest, start + 36);
        }
}