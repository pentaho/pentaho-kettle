/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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
