/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput.text;

import java.io.UnsupportedEncodingException;

import org.pentaho.di.core.util.Utils;

public enum EncodingType {
  SINGLE( 1, 0, '\r', '\n' ), DOUBLE_BIG_ENDIAN( 2, 0xFEFF, 0x000d, 0x000a ), DOUBLE_LITTLE_ENDIAN( 2, 0xFFFE, 0x0d00,
      0x0a00 );

  private int length;

  /**
   * Byte Order Mark (BOM): http://en.wikipedia.org/wiki/Byte_Order_Mark
   */
  private int bom;
  private int carriageReturnChar;
  private int lineFeedChar;

  /**
   * @param length
   * @param bom
   */
  private EncodingType( int length, int bom, int carriageReturnChar, int lineFeedChar ) {
    this.length = length;
    this.bom = bom;
    this.carriageReturnChar = carriageReturnChar;
    this.lineFeedChar = lineFeedChar;
  }

  public int getLength() {
    return length;
  }

  public int getBom() {
    return bom;
  }

  public int getCarriageReturnChar() {
    return carriageReturnChar;
  }

  public int getLineFeedChar() {
    return lineFeedChar;
  }

  public boolean isReturn( int c ) {
    return c == carriageReturnChar || c == '\r';
  }

  public boolean isLinefeed( int c ) {
    return c == lineFeedChar || c == '\n';
  }

  public static EncodingType guessEncodingType( String encoding ) {

    EncodingType encodingType;

    if ( Utils.isEmpty( encoding ) ) {
      encodingType = EncodingType.SINGLE;
    } else if ( encoding.startsWith( "UnicodeBig" ) || encoding.equals( "UTF-16BE" ) ) {
      encodingType = EncodingType.DOUBLE_BIG_ENDIAN;
    } else if ( encoding.startsWith( "UnicodeLittle" ) || encoding.equals( "UTF-16LE" ) ) {
      encodingType = EncodingType.DOUBLE_LITTLE_ENDIAN;
    } else if ( encoding.equals( "UTF-16" ) ) {
      encodingType = EncodingType.DOUBLE_BIG_ENDIAN; // The default, no BOM
    } else {
      encodingType = EncodingType.SINGLE;
    }

    return encodingType;
  }

  public byte[] getBytes( String string, String encoding ) throws UnsupportedEncodingException {
    byte[] withBom;
    if ( Utils.isEmpty( encoding ) ) {
      withBom = string.getBytes();
    } else {
      withBom = string.getBytes( encoding );
    }

    switch ( length ) {
      case 1:
        return withBom;
      case 2:
        if ( withBom.length < 2 ) {
          return withBom;
        }
        if ( withBom[0] < 0 && withBom[1] < 0 ) {
          byte[] b = new byte[withBom.length - 2];
          for ( int i = 0; i < withBom.length - 2; i++ ) {
            b[i] = withBom[i + 2];
          }
          return b;
        } else {
          return withBom;
        }
      default:
        return withBom;
    }
  }
}
