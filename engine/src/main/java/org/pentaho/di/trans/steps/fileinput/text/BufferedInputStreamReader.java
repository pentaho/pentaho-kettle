/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.fileinput.text;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Wraps an InputStreamReader with a BufferedReader enabling mark/reset and other buffered features while still being
 * able to report the original encoding.
 */
public class BufferedInputStreamReader extends BufferedReader {
  InputStreamReader inputStreamReader;
  BufferedReader bufferedReader;

  public BufferedInputStreamReader( InputStreamReader inputStreamReader ) {
    super( inputStreamReader );
    this.inputStreamReader = inputStreamReader;
  }

  public String getEncoding() {
    return inputStreamReader.getEncoding();
  }
}
