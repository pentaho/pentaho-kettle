/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.hadoop.shim.spi;

import java.io.InputStream;
import java.io.OutputStream;

import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hadoop.shim.spi.SnappyShim;

public class MockSnappyShim implements SnappyShim {

  @Override
  public ShimVersion getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isHadoopSnappyAvailable() {
    return false;
  }

  @Override
  public InputStream getSnappyInputStream(InputStream in) throws Exception {
    return null;
  }

  @Override
  public InputStream getSnappyInputStream(int bufferSize, InputStream in) throws Exception {
    return null;
  }

  @Override
  public OutputStream getSnappyOutputStream(OutputStream out) throws Exception {
    return null;
  }

  @Override
  public OutputStream getSnappyOutputStream(int bufferSize, OutputStream out) throws Exception {
    return null;
  }

}
